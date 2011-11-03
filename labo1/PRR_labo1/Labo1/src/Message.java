/**
 * PRR - Laboratoire 1 - Precision Time Protocol
 * Nom           : Message
 * But           : Permet d'envoyer des messages requerant un minimum d'octets entre maitres et esclaves du
 *                 'Precision Time Protocol' decrit par la norme IEEE 1588. Les valeurs d'un enum sont en effet codes
 *                 sur un octet, contrairement a un int, par exemple.
 * Fonctionnement: Les messages sont transmis entre maitre et esclaves sous forme de tableaux d'octets. Un message doit
 *                 contenir un type, un id et un temps. Les types de messages sont:
 *                 - SYNC
 *                 - FOLLOW_UP
 *                 - DELAY_REQUEST
 *                 - DELAY_RESPONSE
 *                 - CONNECTION_REQUEST
 *                 Pour coder et decoder les messages, on utilise les fonctions suivantes:
 *                 - messageToByteArray: cette fonction transforme un objet de type Message en tableau de 13 octets:
 *                      -un pour le type de message
 *                      -4 pour l'id, qui est code sur un int (32 bits)
 *                      -8 pour le temps, qui est code sur un long (64 bits)
 *                 - byteArrayToMessage: cette fonction transforme un tableau de 13 octets en Message, en recuperant le
 *                   numero du type de message, l'id et le temps
 *                 Pour realiser les fonctions ci-dessus, nous avons besoin de pouvoir coder et decoder les types:
 *                 - int vers tableau de 4 octets, pour l'id. fait grace a la fonction intToByteArray
 *                 - int vers un seul octet, pour le type de message. fait grace a la fonction intToByte
 *                 - long vers tableau de 8 octets, pour le temps. fait grace a la fonction longToByteArray
 *                 - ainsi que leurs reciproques (tableaux d'octets vers types numeriques):
 *                      - byteArrayToInt
 *                      - byteToInt
 *                      - longToByteArray
 */

enum Message{
   SYNC,
   FOLLOW_UP,
   DELAY_REQUEST,
   DELAY_RESPONSE,
   CONNECTION_REQUEST,
   CONNECTION_RESPONSE;
   
   //informations contenues dans le message
   private long time_stamp;
   
   private int id;
   
   //permet de modifier et recuperer les informations contenues dans le message
   public long getTimeStamp(){return time_stamp;}
   
   public void setTimeStamp(long t){this.time_stamp = t;}
   
   public int getID(){return id;}
   
   public void setID(int id){this.id = id;}
   
   /**
    * Nom: intToByteArray
    * But: recuperer les octets d'un entier sous forme de tableau
    * Fonctionnement: Les bits sont copies octet par octet dans le tableau grace a l'operateur de decalage
    * @param value: valeur a convertir
    * @return byte[]: octets de l'entier passe en parametre
    */
   public static byte[] intToByteArray(int value) {
        byte[] array = new byte[4];
        for(int i = 0; i < 4; i++){
            array[3-i] = (byte)(value >>> 8*i);
        }
        return array;
    }
   
   /**
    * Nom: byteArrayToInt
    * But: transformer un tableau d'octets en entier
    * Fonctionnement: 4 octets sont lus dans le tableau passe en parametre a partir de l'indice passe en parametre
    *                 (offset, permet de pouvoir lire depuis un tableau de plus de 4 octets). Les bits du tableau
    *                 sont decales dans l'entier et compares avec un masque afin qu'ils prennent leur place correcte
    *                 dans l'entier
    * @param b: tableau duquel lire les octets
    * @param offset: indice du tableau depuis lequel lire
    * @return int: valeur entiere contenue dans le tableau
    */
   public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
   
   /**
    * Nom: byteToInt
    * But: transforme un unique octet en valeur entiere sur 32 bits
    * Fonctionnement: similaire a la fonction precedente, il suffit ici de considerer les bits de poids faible
    * @param b: octet a convertir
    * @return int: valeur entiere contenue dans l'octet
    */
   public static int byteToInt(byte b){
       int value = 0;
       value += (b & 0x000000FF);
       return value;
   }
   
   /**
    * Nom: intToByte
    * But: transforme une valeur entiere, ne depassant pas un codage d'un octet, en octet
    * Fonctionnement: similaire a intToByteArray. il suffit ici de considerer l'octet de poids faible
    * @param i: valeur a transformer en octet
    * @return byte: valeur en octet de l'entier
    */
   public static byte intToByte(int i){
       byte value = (byte)(i >>> 32);
       return value;
   }
   
   /**
    * Nom: longToByteArray
    * But: transforme un long en tableau de 8 octets
    * Fonctionnement: similaire a celui de intToByteArray, seulement sur 8 octets au lieu de 4
    * @param value: valeur a transformer en tableau d'octets
    * @return byte[]: bits du long, arranges en octets
    */
   public static byte[] longToByteArray(long value){
       byte[] array = new byte[8];
        for(int i = 0; i < 8; i++){
            array[7-i] = (byte)(value >>> 8*i);
        }
        return array;
   }
   
   /**
    * Nom: byteArrayToLong
    * But: transformer un tableau d'octets en long
    * Fonctionnement: 8 octets sont lus dans le tableau passe en parametre a partir de l'indice passe en parametre
    *                 (offset, permet de pouvoir lire depuis un tableau de plus de 8 octets). Les bits du tableau
    *                 sont decales dans le long et compares avec un masque afin qu'ils prennent leur place correcte
    *                 dans le long
    * @param b: tableau duquel lire les octets
    * @param offset: indice du tableau depuis lequel lire
    * @return long: valeur long contenue dans le tableau
    */
   public static long byteArrayToLong(byte[] b, int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++){
            value = (value << 8) + (b[i+offset] & 0xff);
        }
        return value;
   }

   /**
    * Nom: messageToByteArray
    * But: transforme un message et les informations qu'il contient en tableau d'octets, afin de pouvoir les envoyer sur
    *      le reseau
    * Fonctionnement: transforme les informations en tableaux d'octets puis les combine dans un tableau unique
    * @param mess: message a transformer
    * @return byte[]: les octets des informations du message
    */
   public static byte[] messageToByteArray(Message mess){
       byte[] array = new byte[13];
       byte[] id = intToByteArray(mess.getID());
       byte[] timestamp = longToByteArray(mess.getTimeStamp());
       array[0] = Message.intToByte(mess.ordinal());
       System.arraycopy(id, 0, array, 1, 4);
       System.arraycopy(timestamp, 0, array, 5, 8);
       return array;
   }
   
   /**
    * Nom: byteArrayToMessage
    * But: transformer un tableau d'octets en message
    * Fonctionnement: lit tour a tour les octets des differentes informations: type de message, id, temps
    * @param array: tableau d'octets a lire
    * @return Message: message de type contenu dans le tableau, et disposant des informations du tableau
    */
   public static Message byteArrayToMessage(byte[] array){
       Message m;
       int ordinal = byteToInt(array[0]);
       int id = byteArrayToInt(array, 1);
       long timestamp = byteArrayToLong(array, 5);
       m = Message.values()[ordinal];
       m.setID(id);
       m.setTimeStamp(timestamp);
       return m;
   }
   
}