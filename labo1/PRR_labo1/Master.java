/**
 * PRR - Laboratoire 1 - Precision Time Protocol
 * Nom           : Master
 * But           : Permet de modeliser une entite maitre dans le 'Precision Time Protocol' decrit par la norme
 *                 IEEE 1588. Le maitre a pour objectif de partager son harloge avec les esclaves
 * Fonctionnement: Le maitre possede trois fonctionalites, tournant chacune dans un thread propre:
 *                 - synchronisation: Le maitre envoie a chaque esclave, tour a tour, un message SYNC, suivi d'un
 *                   message FOLLOW UP. Celui-ci contient la mesure de l'horloge au moment de l'envoi de SYNC. Ceci
 *                   permet aux esclaves de mesurer l'ecart entre l'horloge maitre et la leur.
 *                 - mesure du delai de transmission: Le maitre attend sur les messages DELAY REQUES envoyes par les
 *                   esclaves. Le temps est mesure a la reception de ceux-cis et renvoyes dans les DELAY RESPONSE,
 *                   permettant aux esclaves de mesurer le delai de transmission avec le maitre.
 *                 - connexion de nouveaux esclaves: le maitre attend les messages CONNECTION REQUEST des esclaves et
 *                   incremente le compteur d'esclaves afin de tous les inclure dans le protocole de synchro.
 * @author       : Numa Trezzini
 * @author       : Fabrizio Beretta Piccoli
 */


import java.awt.Dimension;
import java.io.IOException;
import java.net.*;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
 
 class Master {
   
   //declaration des taches realisant les fonctionalites presentees ci-dessus
   private Thread synch_thread;
   
   private Thread delay_thread;
   
   private Thread connection_thread;
   
   //dernier ID attribue a un esclave
   private int last_id = 0;
   
   //facteur pour les temps d'attente
   private int s;
   
   //adresse du groupe de diffusion des messages
   private InetAddress groupAddress;
   
   //port du groupe de diffusion des messages
   private int groupPort;
   
   //socket de communication entre maitre et esclaves
   private MulticastSocket masterSocket;
   
   private int nbreEsclave;
   
   //permet au maitre d'avoir une fenetre d'affichage propre
   private JFrame fenetre_console = new JFrame();
   private JScrollPane scroll_pane;
   private JTextArea console = new JTextArea(); 
        
   
   public Master(String masterAddress, int port, int s, int nbreEsclave){
      this.s = s;
      this.nbreEsclave = nbreEsclave;
      //initialisation de la console maitre
      this.scroll_pane = new JScrollPane(console);
      this.fenetre_console.setTitle("maitre "+0);
      this.fenetre_console.add(scroll_pane);
      this.fenetre_console.setPreferredSize(new Dimension(300, 500));
      this.fenetre_console.setVisible(true);
      this.fenetre_console.pack();
      this.fenetre_console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      //on se connecte au groupe
      try{
          this.groupAddress = InetAddress.getByName(masterAddress);
          this.groupPort = port;
          this.masterSocket = new MulticastSocket(port);
          this.masterSocket.joinGroup(groupAddress);
      }
      catch(UnknownHostException e){}
      catch(IOException e){}
      
      //on cree les threads
      this.connection_thread = new ConnectionThread();
      this.synch_thread = new SynchThread();
      this.delay_thread = new DelayThread();
     
   }/*end Master*/
   
   /**
    * Nom: start
    * But: demarre les threads du maitre
    */
   public void start(){
      this.connection_thread.start();
      this.synch_thread.start();
      this.delay_thread.start();
   }
   
   /**
    * Nom: ConnectionThread
    * But: attend les demandes de connexion des esclaves
    * Fonctionnement: Attend les messages de type CONNECTION REQUEST et incremente le compteur d'esclaves sur
    *                 reception
    */
   private class ConnectionThread extends Thread{
       @Override 
       public void run(){
            byte[] temp = new byte[13];
            DatagramPacket dp = new DatagramPacket(temp, temp.length);
            Message recieve;
            //DatagramPacket response = new DatagramPacket(temp, temp.length);
            while(true){
                //on attend les demandes de connexions des esclaves pour leur attibuer un id
                try{masterSocket.receive(dp);}
                catch(IOException e){System.out.println("connection non recue: "+e);}

                //on décode puis verifie le type du message
                recieve = Message.byteArrayToMessage(dp.getData());
                if(recieve == Message.CONNECTION_REQUEST){
                    last_id++;
                    // si tous les esclaves sont inscrit on sort
                    if (last_id == nbreEsclave)
                    	break;
                }
            }
        }/*end run*/
   }/*end ConnectionThread*/
   
   /**
    * Nom: SynchThread
    * But: cette tache a pour objectif d'envoyer les messages SYNC et FOLLOW UP a chaque esclave
    *      ecoutant le maitre. L'envoi, malgre le multicast, est filtre par les esclaves en fonction
    *      du numero d'id envoye par le maitre. Ainsi, chaque esclave reagira tour a tour aux messages recus
    * Fonctionnement: Cette tache envoie a chaque esclave, tour a tour, un message SYNC, juste apres avoir mesure
    *                 l'horloge. Le message FOLLOW UP est ensuite construit et envoye avec le temps mesure precedemment
    *                 effectuee. La tache patiente le temps indique par s avant de contacter l'esclave suivant.
    *                 Les esclaves filtrent les messages qui leurs sont destines grace a l'id envoye avec les messages
    */
   private class SynchThread extends Thread{
     

     @Override
     public void run(){
         
        int current_id = 1;
        Message synch_mess = Message.SYNC;
        Message follow_up_mess = Message.FOLLOW_UP;
         
        byte[] temp_sync_mess = new byte[13];

        byte[] temp_FU_mess = new byte[13];

        DatagramPacket sync_packet = new DatagramPacket(temp_sync_mess, temp_sync_mess.length, groupAddress, groupPort);
        DatagramPacket FU_packet = new DatagramPacket(temp_FU_mess, temp_FU_mess.length, groupAddress, groupPort);
        
        long send_time;
        while(true){
            
           //calcul du prochain id.
           if(last_id != 0)
        	   current_id = (++current_id%last_id);
           else
        	   ++current_id;

           //creation du message SYNC
           synch_mess.setID(current_id);
           temp_sync_mess = Message.messageToByteArray(synch_mess);
           sync_packet.setData(temp_sync_mess);
           
           //diffusion du message SYNC
           send_time = System.nanoTime();
           try{masterSocket.send(sync_packet);}
           catch(IOException e){System.out.println("could not send SYNC: "+e);}
           
           //creation du message FOLLOW_UP
           follow_up_mess.setID(current_id);
           follow_up_mess.setTimeStamp(send_time);
           console.append("master time: "+send_time+"\n");
           temp_FU_mess = Message.messageToByteArray(follow_up_mess);
           FU_packet.setData(temp_FU_mess);
           //diffusion du message FOLLOW_UP
           try{masterSocket.send(FU_packet);}
           catch(IOException e){System.out.println("could not send FOLLOW_UP: "+e);}

           //attente avant la synchronisation du prochain esclave.
           try{
              sleep(s);
           }
           catch(InterruptedException e){}
        }
     } /*end run*/
   }/*end SynchThread*/
   
   /**
    * Nom: DelayThread
    * But: cette tache a pour objectif de renvoyer, lors de la reception d'un message de type DELAY REQUEST,
    *      un message DELAY RESPONSE contenant l'heure de reception et l'ID de l'envoyeur. L'envoi du maitre
    *      est filtre par les esclaves de la meme facon que dans la tache SynchThread.
    * Fonctionnement: La tache mesure l'horloge juste apres reception d'un message. Si ce message est de type
    *                 DELAY REQUEST, alors renvoie a l'esclave un message de type DELAY RESPONSE contenant l'id de
    *                 l'esclave envoyeur et le temps de reception du message. Ceci permet de determiner le delai de
    *                 transmission d'un packet entre le maitre et l'esclave
    */
   private class DelayThread extends Thread{
     

     @Override
     public void run(){
         
         long reception_time;
         Message recieve;
         byte[] temp_recieve = new byte[13];
         DatagramPacket recieve_dp = new DatagramPacket(temp_recieve, temp_recieve.length, groupAddress, groupPort);

         Message send = Message.DELAY_RESPONSE;
         byte[] temp_send = new byte[13];
         DatagramPacket send_dp = new DatagramPacket(temp_send, temp_send.length, groupAddress, groupPort);

         while(true){
            //accept message d'un esclave
            try{masterSocket.receive(recieve_dp);}
            catch(IOException e){System.out.println("delay request not recieved: "+e);}
            //calcul de l'heure de reception
            reception_time = System.nanoTime();
            //recuperation des infos du message recu
            recieve = Message.byteArrayToMessage(recieve_dp.getData());
            //si le message est de type DELAY REQUEST, on renvoie un message contenant l'ID de l'envoyeur
            //et le temps de reception du-dit message.
            if(recieve == Message.DELAY_REQUEST){

                send.setID(recieve.getID());
                send.setTimeStamp(reception_time);
                temp_send = Message.messageToByteArray(send);
                send_dp.setData(temp_send);
                try{masterSocket.send(send_dp);}
                catch(IOException e){System.out.println("Delay response not sent: "+e);}
            }
            console.append("master time: "+reception_time+"\n");
         }  
     }/*end run*/
   }/*end DelayThread*/
   
}/*end Master class*/