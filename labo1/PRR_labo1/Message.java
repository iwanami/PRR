enum Message {
   SYNC,
   FOLLOW_UP,
   DELAY_REQUEST,
   DELAY_RESPONSE;
   
   private long time_stamp;
   
   private int id;
   
   public long getTimeStamp(){return time_stamp;}
   
   public void setTimeStamp(long t){this.time_stamp = t;}
   
   public int getID(){return id;}
   
   public void setID(int id){this.id = id;}
   
}