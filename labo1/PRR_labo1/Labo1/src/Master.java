
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
            DatagramPacket response = new DatagramPacket(temp, temp.length);
            while(true){
                try{masterSocket.receive(connect);}
                catch(IOException e){System.out.println("connection non recue: "+e);}
                //TODO lire le tableau, voir si le message est de type connexion
                //si c'est le cas
                if(mess_type == Message.SLAVE_CONNECTION){
                    Message mess = Message.SLAVE_CONNECTION;
                    mess.setID(last_id);
                    temp = mess.toString().getBytes();
                    try{masterSocket.send(connect.setData(temp));}
                    catch(IOException e){System.out.println("Message de connection non envoye: "+e);
                    }
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
               
               //TODO diffusion message sync
               try{masterSocket.send(syncPacket);}
               catch(IOException e){System.out.println("could not send SYNC: "+e);}
               
               //TODO diffusion message follow_up
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
            //accept message d'un esclave
            this.reception_time = System.nanoTime();
         }
         
      };/*end delay_thread*/
     
   }/*end Master*/
   
 }/*end Master class*/