
public enum Message 
{   
    REQUETE, REPONSE, MAJ;
    
    private int newVal;
    
    public int getNewVal(){return newVal;}

    public void setNewVal(int val){newVal = val;}
    
    /*private int senderId;
    
    private int horloge;
    
    public int getSenderId(){return senderId;}

    public int getHorloge(){return horloge;}

    public void setSenderId(int id){senderId = id;}

    public void setHorloge(int h){horloge = h;}*/

    public String toString()
    {
        return this.name();
    }
}
