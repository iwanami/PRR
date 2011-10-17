
import java.io.IOException;
import java.net.*;


/**
 * Nom           : Master
 * But           : Permet de modeliser une entité maitre dans le 'Precision
 *                 Time Protocol' décrit par la norme IEEE 1588.
 * Fonctionnement: 
 * Remarques     : 
 */
 
 class Master {
   
   private Thread synch_thread;
   
   private Thread delay_thread;
   
   private Thread connection_thread;
   
   private int last_id = 0;
   
   private InetAddress groupAddress;
   
   private MulticastSocket masterSocket;
   
   private int groupPort;
   
   public Master(String masterAddress, int port){
    
      try{
          this.groupAddress = InetAddress.getByName(masterAddress);
          this.groupPort = port;
          this.masterSocket = new MulticastSocket(port);
          this.masterSocket.joinGroup(groupAddress);
      }
      catch(UnknownHostException e){}
      catch(IOException e){}
      this.last_id++;
      
      this.connection_thread = new Thread(){
        public void run(){
            byte[] temp = new byte[20];
            DatagramPacket connect = new DatagramPacket(temp, temp.length);
            Message resp_mess = Message.SLAVE_CONNECTION;
            DatagramPacket response = new DatagramPacket(temp, temp.length);
            while(true){
                try{masterSocket.receive(connect);}
                catch(IOException e){System.out.println("connection non recue: "+e);}
                //TODO lire le tableau, voir si le message est de type connexion
                //si c'est le cas
                if(resp_mess == Message.SLAVE_CONNECTION){
                    
                    resp_mess.setID(last_id);
                    temp = resp_mess.toString().getBytes();
                    connect.setData(temp);
                    try{masterSocket.send(connect);}
                    catch(IOException e){System.out.println("Message de connection non envoye: "+e);}
                }
            }
        }  
      };
      
      /**
       * Creation du thread Maitre envoyant les messages de synchronisation
       * aux esclaves. Creer les threads dans le constructeur permet non
       * seulement d'avoir acces depuis les threads de communication aux
       * informations contenues dans le maitre, mais aussi a ce que le maitre
       * soit operationnel des sa creation.
       */
      this.synch_thread = new Thread(){
         
         int current_id = 1;
         
         Message synch_mess = Message.SYNC;
         Message follow_up_mess = Message.FOLLOW_UP;
         
         @Override
         public void run(){
             
            byte[] temp_sync_mess = new byte[20];
            
            byte[] temp_FU_mess = new byte[20];
             
            DatagramPacket syncPacket = new DatagramPacket(temp_sync_mess, temp_sync_mess.length, 
                                                           groupAddress, groupPort);
            DatagramPacket FUPacket = new DatagramPacket(temp_FU_mess, temp_FU_mess.length,
                                                         groupAddress, groupPort);
            while(true){
               //calcul du nouvel id. lorsque l'ID courant vaut last_id, il 
               //passe a 0, qui est l'identifiant du maitre. nous evitons de
               //lui envoyer un message en ajoutant 1. le range parcouru par
               //current_id est donc [1;last_id]
               this.current_id = (this.current_id%last_id)+1;
               
               //creation du message SYNC
               this.synch_mess.setID(this.current_id);
               temp_sync_mess = this.synch_mess.toString().getBytes();
               
               
               //creation du message FOLLOW_UP
               this.follow_up_mess.setID(this.current_id);
               this.follow_up_mess.setTimeStamp(System.nanoTime());
               temp_FU_mess = this.follow_up_mess.toString().getBytes();
               
               // diffusion message sync
               try{masterSocket.send(syncPacket);}
               catch(IOException e){System.out.println("could not send SYNC: "+e);}
               
               // diffusion message follow_up
               try{masterSocket.send(FUPacket);}
               catch(IOException e){System.out.println("could not send FOLLOW_UP: "+e);}
               
               //attente avant la synchronisation du prochain esclave.
               try{
                  sleep(3000);
               }
               catch(InterruptedException e){}
            }
         } /*end run*/
      };/*end synch_thread*/
      
      
      this.delay_thread = new Thread(){
         long reception_time;
         
         @Override
         public void run(){
             
             Message rec_mess;
             byte[] temp_rec = new byte[20];
             DatagramPacket rec = new DatagramPacket(temp_rec, temp_rec.length);
             
             Message send_mess = Message.DELAY_RESPONSE;
             byte[] temp_send = new byte[20];
             DatagramPacket send = new DatagramPacket(temp_send, temp_send.length);
             
             while(true){
                 //accept message d'un esclave
                 try{masterSocket.receive(rec);}
                 catch(IOException e){System.out.println("delay request not recieved: "+e);}
                //calcul de l'heure de reception
                this.reception_time = System.nanoTime();
                //recuperation des infos du message recu
                rec_mess = Message.DELAY_REQUEST;
                if(rec_mess == Message.DELAY_REQUEST){
                    send_mess.setID(rec_mess.getID());
                    send_mess.setTimeStamp(this.reception_time);
                    temp_send = send_mess.toString().getBytes();
                    send.setData(temp_send);
                    try{masterSocket.send(send);}
                    catch(IOException e){System.out.println("Delay response not sent: "+e);}
                    
                }
                
                
             }
            
         }
         
      };/*end delay_thread*/
     
   }/*end Master*/
   
 }/*end Master class*/