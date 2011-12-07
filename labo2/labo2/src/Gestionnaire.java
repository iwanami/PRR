import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Gestionnaire implements VariableGlobale
{
    private int varGlobale = 0;

    public final int NB_SITES;

    private int id = 0;
    
    VariableGlobale[] souche;
    
    int nbreReponseAttendue;
    
    
    boolean demande = false;
	
    boolean reponseDifferees[];
        
    int horloge = 0;
        
    int scHeure;


    public Gestionnaire(int nbSites, String[] ipSites, int id) 
    {
        NB_SITES = nbSites;
        this.id = id;
        
        System.setProperty("java.security.policy", "security.policy");
        System.setSecurityManager(new RMISecurityManager());
        
        
        reponseDifferees = new boolean[nbSites];
        for(int tmp=0; tmp<nbSites; tmp++)
            reponseDifferees[tmp] = false;
        
    }
    
    public void mount(String[] ipSites)
    {
        Registry registry;
        try
        {
            VariableGlobale var = (VariableGlobale) UnicastRemoteObject.exportObject(this);
            registry = LocateRegistry.getRegistry(ipSites[id], 1099);
            registry.rebind("VariableGlobale"+id, var);
            System.out.println(var);
        }
        catch(RemoteException e)
        {
            e.printStackTrace();
        }
    }
    
    public void connect(String[] ipSites, int nbSites)
    {
        souche = new VariableGlobale[nbSites];
        Registry registry;
        try
        {   
            for(int i = 0; i < nbSites; i++)
            {
                if(i != id)
                {
                    registry = LocateRegistry.getRegistry(ipSites[i], 1099);
                    souche[i] = (VariableGlobale) registry.lookup("VariableGlobale"+i);
                    System.out.println(souche[i].toString());
                }
                
            }
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
    public void modifierVar(int var) throws RemoteException
    {
        //System.out.println("demande");
        demande();
        //System.out.println("mod");
        varGlobale = var;
        //System.out.println("fin");
        fin();
        //System.out.println("ok");

        //System.out.println( "SVR modif");
    }

    @Override
    public int lireVar() throws RemoteException
    {
        //System.out.println( "SVR lire");
        return varGlobale;	
    }
    
    public void demande() throws RemoteException
    {
            
        scHeure = ++horloge;
        //System.out.println("demande horloge: "+horloge);
        demande = true;
        nbreReponseAttendue = NB_SITES-1;
        //System.out.println("envoi des requetes");
        for(int i=0; i<NB_SITES; i++)
            if(i != id)
            {
                //System.out.println("requete "+i);
                souche[i].recoit(Message.REQUETE, scHeure, id, varGlobale);
            }
        //System.out.println("attente des reponses");
        while(nbreReponseAttendue > 0)
        {	
            //suche.addListener(this);
            System.out.println("j'attend..." + nbreReponseAttendue);
            //Thread.sleep(1000);
            synchronized (this)
            {
                try{wait();}
                catch(InterruptedException e){}
            }
        }
    }
    
    
    public void fin() throws RemoteException
    {
        demande = false;
        //souche.envoie(Message.REPONSE, moi);	
        for(int i=0; i<NB_SITES; i++)
        {
            if(reponseDifferees[i])
            {
                //System.out.println("reponse val:"+mr.getNewVal());
                souche[i].recoit(Message.REPONSE, horloge, id, varGlobale);
                reponseDifferees[i] = false;
            }
            else if(i != id)
            {
                //System.out.println("maj val:"+mm.getNewVal());
                souche[i].recoit(Message.MAJ, horloge, id, varGlobale);
            }
            //else{System.out.println(i);}
        }

    }
    
    
    @Override
    public void recoit(Message m, int h, int senderId, int newVar) throws RemoteException
    {
        
        horloge = Math.max(horloge, h)+1;
        //System.out.println("recoit horloge: "+horloge+"; h: "+h);
        //System.out.print(id+" recoit ");
        switch(m)
        {
            case REQUETE: 
                //System.out.println("requete");
                if (demande && ((h>scHeure) || (h == scHeure && senderId>id)))
                    reponseDifferees[senderId] = true;
                else
                {
                    souche[senderId].recoit(Message.REPONSE, horloge, id, varGlobale);
                }
                break;
            case REPONSE:
                //System.out.println("reponse");
                nbreReponseAttendue--;
                //if(scHeure < h)
                    //varGlobale = m.getNewVal();
                synchronized(this){notify();}
                break;
            case MAJ:
                //System.out.println("maj");
                
                //if (scHeure < h)
                varGlobale = newVar;
                break;
        }
    }

}
