/**
 * Nom           : Message
 * But           : Permet d'envoyer des messages requerant un minimum d'octets entre maitres et esclaves du
 *                 'Precision Time Protocol' decrit par la norme IEEE 1588. Les valeurs d'un enum sont en effet codes
 *                 sur un octet, contrairement a un int, par exemple.
 * Fonctionnement: Les messages sont transmis entre maitre et esclaves sous forme de tableaux d'octets, obtenus
 *                 a partir de Strings. Un message, en plus de son type, doit pouvoir contenir l'id de l'esclave
 *                 couramment interroge, ainsi que des temps.
 */

enum Message{
   SYNC,
   FOLLOW_UP,
   DELAY_REQUEST,
   DELAY_RESPONSE,
   SLAVE_CONNECTION;
   
   private long time_stamp;
   
   private int id;
   
   public long getTimeStamp(){return time_stamp;}
   
   public void setTimeStamp(long t){this.time_stamp = t;}
   
   public int getID(){return id;}
   
   public void setID(int id){this.id = id;}
   
   @Override
   public String toString(){
       return ""+this.ordinal()+";"+this.id+";"+this.time_stamp;
   }
   
   public static Message byteArrayToMessage(byte[] array){
       Message m;
       String msg = new String(array);
       System.out.println("Message: converting "+msg);
       String[] msg_vals = msg.split(";");
       int msg_type = Integer.parseInt(msg_vals[0]);
       m = Message.values()[msg_type];
       m.setID(Integer.parseInt(msg_vals[1]));
       //m.setTimeStamp(Long.valueOf(msg_vals[2]));
       return m;
       
   }
   
}