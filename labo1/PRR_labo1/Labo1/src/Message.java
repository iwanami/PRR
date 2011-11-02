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
   CONNECTION_REQUEST,
   CONNECTION_RESPONSE;
   
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
   
   public static byte[] intToByteArray(int value) {
        byte[] array = new byte[4];
        for(int i = 0; i < 4; i++){
            array[3-i] = (byte)(value >>> 8*i);
        }
        return array;
    }
   
   public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
   
   public static byte[] longToByteArray(long value){
       byte[] array = new byte[8];
        for(int i = 0; i < 8; i++){
            array[7-i] = (byte)(value >>> 8*i);
        }
        return array;
   }
   
   public static long byteArrayToLong(byte[] b, int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            int shift = (7 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

   public static byte[] messageToByteArray(Message mess){
       byte[] array = new byte[13];
       byte[] id = intToByteArray(mess.getID());
       byte[] timestamp = longToByteArray(mess.getTimeStamp());
       array[0] = (byte)mess.ordinal();
       System.arraycopy(id, 0, array, 1, 4);
       System.arraycopy(timestamp, 0, array, 5, 8);
       return array;
   }
   
   public static Message byteArrayToMessage(byte[] array){
       Message m;
       int ordinal = array[0];
       int id = byteArrayToInt(array, 1);
       long timestamp = byteArrayToLong(array, 5);
       m = Message.values()[ordinal];
       m.setID(id);
       m.setTimeStamp(timestamp);
       return m;
   }
   
}