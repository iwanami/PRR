// classe fournissant des fonctions de lecture au clavier
import java.io.*;
import java.util.Scanner;

public class Clavier
{

   /* Lecture d'une chaine */
   public static String lireString ()
   {
      String ligne_lue = null;
      
      try
      {
         InputStreamReader lecteur = new InputStreamReader(System.in);
         BufferedReader entree = new BufferedReader(lecteur);
         ligne_lue = entree.readLine();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
    
      return ligne_lue; 
   } // lireString
  
   /* Lecture d'un int */
   public static int lireInt(int min, int max)
   {
      Scanner input = new Scanner(System.in);
      int n;
      do
      {
          n = input.nextInt();
      }while(n < min || n > max);
      
      return n;
   } // lireInt

}


