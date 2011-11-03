/**
 * PRR - Laboratoire 1 - Precision Time Protocol
 * Nom           : Slave
 * But           : Permet de modeliser une entite esclave dans le 'Precision Time Protocol' decrit par la norme
 *                 IEEE 1588. L'esclave a pour objectif de sychroniser son horloge avec celle du maitre
 * Fonctionnement: L'esclave fonctionne en deux etapes:
 *                 - attendre un message SYNC et FOLLOW UP du maitre. Le maitre marque le temps juste avant l'envoi du
 *                   message SYNC et l'esclave a l'arrivee de celui-ci. Ceci permet de comparer les horloges des deux
 *                   entite "au meme moment" (les delais de transmission seront pris en compte a l'etape suivante).
 *                   Pour eviter que la comparaison de temps ne soit faussee par l'execution d'instructions entre les
 *                   deux mesures de l'horloge, le temps du maitre est envoye avec le deuxieme message, FOLLOW UP.
 *                   Cette etape permet de mesurer l'ecart entre les deux horloges
 *                 - Envoyer un message DELAY REQUEST au maitre et attendre un DELAY RESPONSE de celui-ci. L'esclave
 *                   mesure le temps juste avant l'envoi du DELAY REQUEST et le maitre mesure le temps juste apres
 *                   l'arrivee de celui-ci. Le maitre renvoie a l'esclave ce temps. la difference entre le temps de
 *                   reception par le maitre et le temps d'envoi par l'esclave de DELAY_REQUEST donne le delai de
 *                   transmission aller retour (RTT) entre le maitre et l'esclave. La moitie de cette valeur donnera
 *                   ainsi le delai et permet d'affiner la synchronisation des horloges.
 *                 Lors de sa creation, un esclave envoie au maitre un message de type CONNECTION REQUEST afin de
 *                 "s'inscrire" aupres du maitre et ainsi etre inclus dans la synchronisation.
 *                 Entre les deux etapes, l'esclave attend un temps aleatoire situe entre 4*s et 60*s. s etant une
 *                 valeur fixee a la construction.
 *                 Les numeros d'identification servent a filtrer les messages diffuses, permettant ainsi de reconnaitre
 *                 ceux qui sont destines a l'esclave
 * @author       : Numa Trezzini
 * @author       : Fabrizio Beretta Piccoli
 */

import java.awt.Dimension;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
 
 class Slave extends Thread{
   
   //dernier id attribue
   private static int current_id = 0;
   
   //id de l'esclave
   private int id;
   
   //horloge locale de l'esclave. C'est cette valeur qui sera synchronisee sur le maitre
   private long h_locale;
   
   //valeur de l'ecart avec l'horloge du maitre
   private long ecart;
   
   //valeur du delai de transmission entre le maitre et l'esclave
   private long delai;
   
   //valeur totale du decalage entre les horloges maitre et esclave
   private long decalage;
   
   //coefficient du temps d'attente entre les etapes
   private int s;
   
   //adresse du groupe de diffusion des packets de synchro
   private InetAddress groupAddress;
   
   //port du groupe de diffusion des packets de synchro
   private int groupPort;
   
   //socket d'ecoute et d'envoi des packets de synchro
   private MulticastSocket masterSocket;
   
   
   
   //permet a chaque esclave d'avoir une fenetre d'affichage propre
   private JFrame fenetre_console = new JFrame();
   private JScrollPane scroll_pane;
   private JTextArea console = new JTextArea(); 
   
   public Slave(String masterAddress, int port, int s){
      this.s = s;
      id = current_id++;
      
      //permet d'afficher les actions de chaque esclave dans une fenetre separee
      this.scroll_pane = new JScrollPane(console);
      this.fenetre_console.setTitle("esclave "+this.id);
      this.fenetre_console.add(scroll_pane);
      this.fenetre_console.setPreferredSize(new Dimension(300, 500));
      this.fenetre_console.setVisible(true);
      this.fenetre_console.pack();
      
      //on se connecte au groupe.
       try{
          this.groupAddress = InetAddress.getByName(masterAddress);
          this.groupPort = port;
          this.masterSocket = new MulticastSocket(port);
          this.masterSocket.joinGroup(groupAddress);   
      }
      catch(UnknownHostException e){}
      catch(IOException e){}
       
       //on s'annonce aupres du maitre afin d'incrementer son compteur d'esclaves
       Message connexion = Message.CONNECTION_REQUEST;
       byte[] buffer = new byte[13];
       buffer = Message.messageToByteArray(connexion);
       DatagramPacket dp = new DatagramPacket(buffer, buffer.length, groupAddress, groupPort);
       
       try{masterSocket.send(dp);}
       catch(IOException e){}

   }
   
       
      
   /**
   * Nom: doSync
   * But: Attend le message SYNC et FOLLOW_UP du maitre afin d'obtenir une correction sur l'horloge locale par rapport
   *      a celle du maitre
   * Fonctionnement: Cette fonction attend le message du maitre qui lui est destine (a l'aide de l'id esclave) et marque
    *                le temps de reception. Le message FOLLOW_UP qui suit contient l'heure d'envoi du message SYNC par
    *                le maitre. La difference entre ces deux temps donne la valeur de l'ecart. On peut ainsi mette a
    *                jour le decalage de l'esclave
   */
   private void doSync(){
      
       byte[] buffer = new byte[13];
       DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
       Message recieved;
       long sync_reception_time;
       try{
           
           while(true){
               //on attend un message du maitre
               masterSocket.receive(dp);
               //lorsque le message est recu, on recupere l'heure de l'esclave
               sync_reception_time = System.nanoTime();
               recieved = Message.byteArrayToMessage(dp.getData());
               //si c'est un message de type SYNC et qu'il est destine a l'esclave courant (testable avec l'ID)
               //on traite le message
               if(recieved == Message.SYNC && recieved.getID() == id){
                   
                   //Comme le message FOLLOW_UP est envoye immediatement apres le SYNC par le maitre, on l'attend
                   //dans l'esclave
                   //il se peut cependant que le prochain message recu ne soit pas immediatement le FOLLOW_UP, on
                   //patiente jusqu'a reception du bon message
                   while(true){
                       //on attend le message du maitre
                       masterSocket.receive(dp);
                       recieved = Message.byteArrayToMessage(dp.getData());
                       //si le message correspond au follow_up destine a l'esclave courant, on recupere le temps
                       //et on calcule l'ecart
                       if(recieved == Message.FOLLOW_UP && recieved.getID() == id){
                           ecart = recieved.getTimeStamp()-sync_reception_time;
                           decalage = ecart+delai;
                           //une fois l'ecart calcule, on peut l'ajouter au temps mesure a la reception, ceci donne
                           //l'heure locale
                           return;
                       }
                   }
               }
           }
           
       }
       catch(IOException e){System.out.println("erreur dans la fonction doSync: "+e);}
   }/*end doSync*/
   

      
  /**
   * Nom: doDelay
   * But: Envoie la demande DELAY et attend sa reponse du maitre afin d'obtenir une estimation du temps latence de la 
   *      transmission. Nous mesurons donc de Round Time Trip (temps d'aller retour) entre l'esclave et le maitre
   * Fonctionnement: L'esclave envoie un message DELAY REQUEST au maitre, qui va lui repondre avec le temps au moment de
   *                 la reception avec un message DELAY_RESPONSE. La difference entre le temps mesure a l'envoi de
   *                 DELAY_REQUEST et le temps envoye par le maitre donne le temps d'aller-retour d'un packet sur
   *                 le reseau. La moitie de cette valeur donne ainsi le delai de transmission.
   */
  public void doDelay(){
      long send_time;
      //preparation du message a envoyer
      Message recieved;
      Message sent = Message.DELAY_REQUEST;
      sent.setID(id);
      //preparation du message a envoyer sous forme d'octets
      byte[] buffer = new byte[13];
      buffer = Message.messageToByteArray(sent);
      //preparation du datagramme a envoyer
      DatagramPacket dp = new DatagramPacket(buffer, buffer.length, groupAddress, groupPort);
      dp.setData(buffer);
      try{
          
          while(true){
              //a l'envoi du datagramme, on recupere le temps (juste avant l'envoi, comme dans le maitre)
              send_time = System.nanoTime();
              masterSocket.send(dp);
              //on attend la reponse du maitre
              masterSocket.receive(dp);
              //decodage du message
              recieved = Message.byteArrayToMessage(dp.getData());
              //si le message est adresse a cet esclave, on le traite
              if(recieved == Message.DELAY_RESPONSE && recieved.getID() == id){
                  //calcul du delai de transmission
                  delai = (recieved.getTimeStamp()-send_time)/2;
                  decalage = ecart+delai;
                  break;
              }
          }
          
      }
      catch(IOException e){}
  }/*end doDelay*/
  
  /**
   * Nom: run
   * But: Cette methode effectue l'une apres l'autre la demande de synchronisation et la demande de delai permettant
   *      a l'esclave de se synchroniser sur l'horloge du maitre en mesurant le decalage entre les deux horloges et le
   *      delai de transmission d'un packet entre le maitre et l'esclave
   * Fonctionnement: L'esclave commence par attendre la synchronisation du maitre, met a jour l'ecart, patiente un temps
   *                 aleatoire, puis fait une demande de delai. Ceci permet de mettre a jour la valeur delai, et ainsi
   *                 finir de synchroniser l'esclave avec le maitre
   */
  @Override
  public void run(){
      while(true){
        doSync();
        h_locale = System.nanoTime()+decalage;
        console.append("heure locale apres sync s"+id+": "+h_locale+"\n");
        try{sleep(random(4*s, 60*s));}
        catch(InterruptedException e){}
        doDelay();
        console.append("heure locale apres delay s"+id+": "+h_locale+"\n");
      }
      
  }/*end run*/
  
  /**
     * Nom: random
     * @param min valeur minimum du generateur
     * @param max valeur maximum du generateur
     * @return int: un nombre aleatoire entre min et max
     */
    public int random(int min, int max){
        return (int)(Math.random() * (max-min)) + min;
    }/*end random*/
   
   
}/*end Slave*/