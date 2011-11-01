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
 
 class Slave {
   
   private int id;
   
   private int h_locale;
   
   private int ecart;
   
   private int delai;
   
   private int decalage;
   
   private Thread synch_thread;
   
   private Thread delay_thread;
   
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
      Message connect = Message.SLAVE_CONNECTION;
      long ct = System.nanoTime();
      connect.setTimeStamp(ct);
      DatagramPacket connect_datagram = new DatagramPacket(connect.toString().getBytes(),
                                                           connect.toString().getBytes().length,
                                                           this.groupAddress, this.groupPort);
      //creation du paquet de reponse pour la connexion
      byte[] buffer = new byte[20];
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
        if (connect == Message.SLAVE_CONNECTION && connect.getTimeStamp() == ct){
            this.id = connect.getID();
            System.out.println("slave " + id + ": connection recieved");
            break;
        }
      }
      
      //deuxieme partie: creation du thread traitant le SYNC et le FOLLOW_UP
      this.synch_thread = new SynchThread();
      
      //troisieme partie: creation du thread traitant
      this.delay_thread = new DelayThread();
      
      //lancement des taches
      //this.synch_thread.start();
      //this.delay_thread.start();
      
   }
   
   /**
    * Nom: SynchThread
    * But: Cette tache repond aux messages SYNCH et FOLLOW UP envoyes par le maitre
    * TODO remplir un peu le but...
    */
   private class SynchThread extends Thread{
       
      byte[] buffer = new byte[20];
      DatagramPacket dp = new DatagramPacket(buffer, buffer.length, groupAddress, groupPort);
      Message recieved;

      @Override
      public void run(){
          while(true){
              try{
                  //on attend un message du maitre
                  masterSocket.receive(dp);
                  System.out.println("slave " + id + ": synch recieved");
                  recieved = Message.byteArrayToMessage(dp.getData());
                  //si c'est un message de type SYNC et qu'il est destine a l'esclave courant (testable avec l'ID)
                  //on traite le message
                  if(recieved == Message.SYNC && recieved.getID() == id){
                      //TODO traitement
                  }
              }
              catch(IOException e){}
          }
      }/*end run*/
   }/*end SynchThread*/
   
   /**
    * Nom: DelayThread
    * But: Cette tache envoie des messages DELAY REQUEST au maitre et attend en retour les messages
    *      DELAY RESPONSE
    * TODO remplir un peu le but...
    */
   private class DelayThread extends Thread{
      byte[] buffer = new byte[20];
      DatagramPacket dp = new DatagramPacket(buffer, buffer.length, groupAddress, groupPort);
      Message recieved;

      @Override
      public void run(){
          while(true){
              try{
                  masterSocket.receive(dp);
                  System.out.println("slave " + id + ": delay recieved");
                  recieved = Message.byteArrayToMessage(dp.getData());
                  if(recieved.getID() == id){
                      //TODO traitement
                  }
              }
              catch(IOException e){}
          }
      }/*end run*/
   }/*end DelayThread*/
   
   
 }/*end Slave*/