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
   
   static int last_id = 0;
   
   public Master(){
      Master.last_id++;
      
      /**
       * Creation du thread Maitre envoyant les messages de synchronisation
       * aux esclaves. Creer les threads dans le constructeur permet non
       * seulement d'avoir acces depuis les threads de communication aux
       * informations contenues dans le maitre, mais aussi a ce que le maitre
       * soit operationnel des sa creation.
       */
      this.synch_thread = new Thread(){
         
         int current_id = 1;
         
         Message synch_mess;
         Message follow_up_mess;
         
         public void run(){
            while(true){
               //calcul du nouvel id. lorsque l'ID courant vaut last_id, il 
               //passe a 0, qui est l'identifiant du maitre. nous evitons de
               //lui envoyer un message en ajoutant 1. le range parcouru par
               //current_id est donc [1;last_id]
               this.current_id = (this.current_id%Master.last_id)+1;
               
               //creation du message SYNC
               this.synch_mess = Message.SYNC;
               this.synch_mess.setID(this.current_id);
               
               //creation du message FOLLOW_UP
               this.follow_up_mess = Message.FOLLOW_UP;
               this.follow_up_mess.setID(this.current_id);
               this.follow_up_mess.setTimeStamp(System.nanoTime());
               
               //TODO diffusion message
               
               //TODO diffusion message
               
               //attente avant la synchronisation du prochain esclave.
               try{
                  wait(5000);
               }
               catch(InterruptedException e){}
            }
         } /*end run*/
      }/*end synch_thread*/
     
   }/*end Master*/
   
 }/*end Master class*/