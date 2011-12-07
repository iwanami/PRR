import java.rmi.*;

public interface VariableGlobale extends Remote
{
	void modifierVar(int var) throws RemoteException;

	int lireVar() throws RemoteException;
	
	//void envoie(Message m) throws RemoteException;
	
	void recoit(Message m, int h, int senderId, int newVar) throws RemoteException;
}
