import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class TacheApp extends Thread
{	

    private VariableGlobale gestionnaire;


    public TacheApp(String ipGestionnaire, int idGestionnaire)
    {	
        System.setProperty("java.security.policy", "security.policy");
        System.setSecurityManager(new RMISecurityManager());
        Registry registry;
        try
        {
            registry = LocateRegistry.getRegistry(ipGestionnaire, 1099);
            gestionnaire = (VariableGlobale)registry.lookup("VariableGlobale"+idGestionnaire);
        }
        catch(RemoteException e)
        {
            e.printStackTrace();
        }
        catch(NotBoundException e)
        {
            e.printStackTrace();
        }
    }
	
    @Override
    public void run()
    {	
        while(true)
        {
            System.out.println("Que voulez vous faire: ");
            System.out.println("[ 0 sortir, 1 lire , 2 modifier]");
            switch (Clavier.lireInt(0, 2))
            {
                case 0 : 
                {
                    System.out.println("bye bye");
                    System.exit(0);
                }
                case 1 :
                {

                    try{System.out.println("reponse = " + gestionnaire.lireVar());}
                    catch(RemoteException e)
                    {
                        System.out.println("echec de la lecture de la var partagee");
                    }
                    break;
                }
                case 2 :
                {
                    try
                    {
                        System.out.print("entrer une nouvelle valeur: ");
                        int nvx = Clavier.lireInt(Integer.MIN_VALUE, Integer.MAX_VALUE);						
                        gestionnaire.modifierVar(nvx);
                    }
                    catch(RemoteException e)
                    {
                        e.printStackTrace();
                        //System.out.println(e);
                        System.out.println("echec de la modification de la var partagee");
                    }
                }					
            }
        }
    }
	
}
