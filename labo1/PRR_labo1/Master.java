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
      
      this.synch_thread = new Thread(){
         
         int current_id = 1;
         
         Message mess;
         
         long master_time;
         
         public void run(){
            while(true){
               this.current_id = (this.current_id%Master.last_id)+1;
               this.mess = Message.SYNC;
               mess.setID(this.current_id);
               this.master_time = System.nanoTime();
               //TODO diffusion message
               this.mess = Message.FOLLOW_UP;
               mess.setID(this.current_id);
               mess.setTime(this.master_time);
               //TODO diffusion message
               try{
                  wait(5000);
               }
               catch(InterruptedException e){}
            }
         }
         
         
      }
     
   }
   
 }