
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RMISecurityManager;

/*
/**
 *
 * @author Numa Trezzini
 * @author
 * @version 
 */


/**
 *
 */
public class Site
{
    
    private TacheApp tache;
    
    private Gestionnaire gest;
    
    private String[] ipSites;
    
    private int nbSites;
    
    private int id;
    
    public Site(int nbSites, int id)
    {
        this.id = id;
        this.nbSites = nbSites;
        System.setProperty("java.security.policy", "security.policy");
        System.setSecurityManager(new RMISecurityManager());
        try
        {
            BufferedReader in = new BufferedReader(new FileReader("ips.txt"));
            ipSites = new String[nbSites];
            for(int i=0; i < nbSites; i++)
            {
                ipSites[i] = in.readLine();
                System.out.println(ipSites[i]);
            }
        }
        catch(FileNotFoundException e)
        {
            System.out.println("ip file not found");
            System.exit(-1);
        }
        catch(IOException e)
        {
            System.out.println("could not read ip file");
            System.exit(-1);
        }
        
        gest = new Gestionnaire(nbSites, ipSites, id);
        gest.mount(ipSites);
    }
    
    
    public void connect(){
        //System.out.println("tous les sites et gestionnaires ont-il ete lances? appuyez sur ENTER pour continuer");
        //Clavier.lireString();
        this.gest.connect(ipSites, nbSites);
        tache = new TacheApp(ipSites[id], id);
        tache.start();
    }
    
    public static void main(String[] args) {
        
        for(String arg : args)
            System.out.println(arg);
        
        Site s0 = new Site(2, Integer.parseInt(args[0]));
        //Site s1 = new Site(2, 1);
        System.out.println("Si tous les sites ont ete lances, appuyer sur ENTER");
        Clavier.lireString();
        s0.connect();
        //s1.connect();
        
    }
}
