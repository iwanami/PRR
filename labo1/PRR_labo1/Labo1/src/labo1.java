/**
 * PRR - Laboratoire 1 - Precision Time Protocol
 * Nom: Labo1
 * But: Permet de lancer des maitre et/ou des esclaves pour tester 'Precision Time Protocol' decrit par la norme
 *      IEEE 1588.
 * Fonctionnement: Il faut indiquer l'adresse IP du groupe de diffusion, le port d'ecoute du protocole, et un facteur
 *                 de temps d'attente.
 *                 Pour lancer un maitre, creer une variable Master et appeler la methode start dessus
 *                 Pour lancer un esclave, creer une variable Esclave et appeler la methode start dessus
 * Remarques: "A multicast group is specified by a class D IP address and by a standard UDP port number. Class D IP
 *             addresses are in the range 224.0.0.0 to 239.255.255.255, inclusive. The address 224.0.0.0 is reserved
 *             and should not be used." [Javadoc MulticastSocket ed6]
 * @author Numa Trezzini
 * @author Fabrizio Beretta Piccoli
 */

class Labo1{

    public static void main(String[] args){
        
        String adr = "225.0.0.0";
        int port = 6789;
        int s = 1000;
        
        Master m = new Master(adr, port, s);
        m.start();
        Slave s1 = new Slave(adr, port, s);
        s1.start();
        System.out.println("s1 start");
        Slave s2 = new Slave(adr, port, s);
        s2.start();
        System.out.println("s2 start");
        Slave s3 = new Slave(adr, port, s);
        s3.start();
        System.out.println("s3 start");
        
    }
}