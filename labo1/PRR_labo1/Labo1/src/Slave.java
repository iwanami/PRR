
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * Nom           : Slave
 * But           : Permet de modeliser une entité esclave dans le 'Precision
 *                 Time Protocol' décrit par la norme IEEE 1588.
 * Fonctionnement: 
 * Remarques     : 
 */
 
 class Slave {
   
   private final int id;
   
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
       //
      Message connect = Message.SLAVE_CONNECTION;
      DatagramPacket connect_datagram = new DatagramPacket(connect.toString().getBytes(),
                                                           connect.toString().getBytes().length);
      try{this.masterSocket.send(connect_datagram);}
      catch(IOException e){System.out.println("datagramme de connection pas envoye: "+e);}
      
      //creation du paquet de reponse
      byte[] buffer = new byte[20];
      DatagramPacket connect_resp = new DatagramPacket(buffer, buffer.length);
      //on attend la reponse du maitre
      while(true){
        try{this.masterSocket.receive(connect_resp);}
        catch(IOException e){System.out.println("datagramme de connection pas recu: "+e);}
      
        //TODO lire le type du message
        int mess_id = 1;
        Message mess_type;// = (int)connect_resp.getData()[1];
        if (mess_type == Message.SLAVE_CONNECTION){
            this.id = mess_id;
            break;
        }
      }
      
      //deuxieme partie: creation du thread traitant le SYNC et le FOLLOW_UP
      this.synch_thread = new Thread(){
          
      };
      
      
   }
   
   
 }