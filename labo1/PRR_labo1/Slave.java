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
   
 }