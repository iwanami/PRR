/**
 * Nom           : Slave
 * But           : Permet de modeliser une entite esclave dans le 'Precision
 *                 Time Protocol' decrit par la norme IEEE 1588.
 * Fonctionnement: 
 * Remarques     : 
 * @author       : Numa Trezzini
 * @author       : Fabrizio Beretta Piccoli
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
 
 class Slave extends Thread{
   
   private int id;
   
   private long h_locale;
   
   private long ecart;
   
   private long delai;
   
   private long decalage;
   
   
   private InetAddress groupAddress;
   
   private MulticastSocket masterSocket;
   
   private int groupPort;
   
   public Slave(String masterAddress, int port){
      //on se connecte au groupe.
       try{
          this.groupAddress = InetAddress.getByName(masterAddress);
          this.groupPort = port;
          this.masterSocket = new MulticastSocket(port);
          this.masterSocket.joinGroup(groupAddress);
      }
      catch(UnknownHostException e){}
      catch(IOException e){}
      
      //on envoie la demande de connection pour recuperer le numero d'id et "s'inscrire" 
      //au protocole en incrementant le compteur du maitre.
      Message connect = Message.CONNECTION_REQUEST;
      long ct = System.nanoTime();
      connect.setTimeStamp(ct);
      DatagramPacket connect_datagram = new DatagramPacket(connect.toString().getBytes(),
                                                           connect.toString().getBytes().length,
                                                           this.groupAddress, this.groupPort);
      //creation du paquet de reponse pour la connexion
      byte[] buffer = new byte[13];
      DatagramPacket connect_resp = new DatagramPacket(buffer, buffer.length, this.groupAddress, this.groupPort);
      
      
      
      
      //on attend la reponse du maitre
      while(true){
          
        //TODO dans le while, ca?
        try{this.masterSocket.send(connect_datagram);}
        catch(IOException e){System.out.println("datagramme de connection pas envoye: "+e);}
        System.out.println("slave: connection sent");
          
        try{this.masterSocket.receive(connect_resp);}
        catch(IOException e){System.out.println("datagramme de connection pas recu: "+e);}
      
        System.out.println("slave: connection recieved");
        //On recupere les infos envoyees par le maitre
        connect = Message.byteArrayToMessage(connect_resp.getData());
        //on recupere les infos de connection seulement si le message correspond au timestamp de creation du
        //message de connection et si c'est un message de connection
        if (connect == Message.CONNECTION_RESPONSE && connect.getTimeStamp() == ct){
            this.id = connect.getID();
            System.out.println("slave " + id + ": connection recieved");
            break;
        }
      }
      
   }
   
       
      

  private void doSync(){
      
      byte[] buffer = new byte[13];
      DatagramPacket dp = new DatagramPacket(buffer, buffer.length, groupAddress, groupPort);
      Message recieved;
      long sync_reception_time;
      
      try{
          //on attend un message du maitre
          masterSocket.receive(dp);
          System.out.println("slave " + id + ": synch recieved");
          recieved = Message.byteArrayToMessage(dp.getData());
          //si c'est un message de type SYNC et qu'il est destine a l'esclave courant (testable avec l'ID)
          //on traite le message
          if(recieved == Message.SYNC && recieved.getID() == id){
              //lorsque le message SYNC est recu, on recupere l'heure de l'esclave
              sync_reception_time = System.nanoTime();
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
                      //une fois l'ecart calcule, on peut l'ajouter au temps local
                      h_locale = System.nanoTime()+ecart;
                      //on sort de la boucle d'attente du FOLLOW_UP et on attend le SYNC suivant
                      break;
                  }
              }
          }
      }
      catch(IOException e){}
  }/*end doSync*/
   

      
      
  public void doDelay(){

      byte[] buffer = new byte[13];
      DatagramPacket dp = new DatagramPacket(buffer, buffer.length, groupAddress, groupPort);
      Message sent = Message.DELAY_REQUEST;
      Message recieved;

      sent.setID(id);
      try{
          buffer = Message.messageToByteArray(sent);
          dp.setData(buffer);
          masterSocket.send(dp);

          masterSocket.receive(dp);
          System.out.println("slave " + id + ": delay recieved");
          recieved = Message.byteArrayToMessage(dp.getData());
          if(recieved.getID() == id){
              //TODO traitement
          }
      }
      catch(IOException e){}
  }/*end run*/
  
  @Override
  public void run(){
      while(true){
        doSync();
        //TODO wait random time
        doDelay();
        //TODO wait random time
      }
      
  }
   
   
}/*end Slave*/