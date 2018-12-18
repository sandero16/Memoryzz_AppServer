package sample;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class AppImpl extends UnicastRemoteObject implements AppInterface{
    int huidigeDBpoortNr;
    public  DBServerInterface stub;

    public AppImpl(DBServerInterface stub, int poortnr) throws RemoteException{
        this.stub = stub;
        huidigeDBpoortNr=poortnr;
        databasePorts.add(2000);databasePorts.add(2001);databasePorts.add(2002);
    }

    ArrayList<Integer> databasePorts=new ArrayList<>();
    ArrayList<String> occupiedPlayers=new ArrayList<>();
    ArrayList<Game> watchGames=new ArrayList<>();
    ArrayList<Integer> watchGameKeys=new ArrayList<>();
    HashMap<Integer, Game>watchGame=new HashMap<>();

    HashMap<String, Game> busyGame=new HashMap();
    int migrateAddress;
    boolean migrate;

    @Override
    public void changeBeurt(String sessionToken){
        Game game=(Game)busyGame.get(sessionToken);
        game.changeBeurt();

    }
    public boolean checkBeurt(String sessionToken){
        Game game=(Game)busyGame.get(sessionToken);
        System.out.println("het is nu aan"+game.checkBeurt());
        System.out.println("checkbeurt: "+sessionToken);
        return (sessionToken.equals(game.checkBeurt()));
    }


    @Override
    public void testConnectie(){
        System.out.println("connectie is er");
    }
    @Override
    public boolean setGame(String sessionToken){
        Game game=(Game)busyGame.get(sessionToken);
        if(!watchGames.contains(game)) {
            watchGames.add(game);
            Random r=new Random();
            int key=r.nextInt(1000);
            while(watchGame.containsKey(key)){
                key=r.nextInt(1000);
            }
            watchGameKeys.add(key);
            watchGame.put(key, game);
        }
        game.generateMatrix();
        if(game.checkBeurt().equals(sessionToken)){
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public int getGame(int i){
        if(watchGames.isEmpty())
            return -1;
        else
            return watchGameKeys.get(0);
    }

    @Override
    public synchronized boolean vindtTegenspeler(String sessionToken){
        try {
            while (!occupiedPlayers.contains(sessionToken)) {
                wait();
            }
            return true;
        }
        catch (Exception e){

        }
        return false;
    }
    //hier wordt er ook gekeken of er migratie is
    @Override
    public int[]getAndereGok(String sessionToken){
        if(migrate){
            int[]tempArray=new int[1];
            tempArray[0]=migrateAddress;
            return tempArray;
        }
        else {
            Game game = (Game) busyGame.get(sessionToken);
            int[] gok = game.getTegenspelerGok();
            return gok;
        }
    }
    //hier wordt er ook gekeken of er migratie is
    public int getZet(int i, String sessionToken){
        System.out.println("zet: "+i+" session: "+sessionToken);
        if(migrate){
            //De wachtende spelers die al aan het wachten zijn gaan we waarschuwen
            Game game = (Game) busyGame.get(sessionToken);
            game.notifyMigration(migrateAddress);
            return migrateAddress;
        }
        else {
            Game game = (Game) busyGame.get(sessionToken);
            int value = game.getFromMatrix(i);
            return value;
        }
    }
    @Override
    public int getResult(String sessionToken){
        Game game=(Game)busyGame.get(sessionToken);
        int result = game.getResult(sessionToken);
        int tempResult = result;
        if(tempResult>10)
            tempResult-=10;

        int ontvangenPunten = game.getAantalSpelers() + 1 - tempResult;
        updateRanking(sessionToken,ontvangenPunten);
        //plaats returnen (+10 als gedeeld)
        return result;
    }
    public void updateRanking(String sessionToken, int ontvangenPunten){
        try {
            stub.updateRanking(sessionToken,ontvangenPunten);
        } catch (RemoteException e) {
            try {
                getAndereDb();
                Registry myRegistry = LocateRegistry.getRegistry("localhost", huidigeDBpoortNr);
                stub = (DBServerInterface) myRegistry.lookup("DBService");
                updateRanking(sessionToken, ontvangenPunten);
            }catch (Exception ex){

            }
        }
    }
    public void getAndereDb(){
        for (int i:databasePorts) {
            if(i!=huidigeDBpoortNr){
                huidigeDBpoortNr=i;
            }
        }
    }
    @Override
    public int getScore(String sessionToken){
        Game game=(Game)busyGame.get(sessionToken);
        return game.getScore(sessionToken);
    }
    @Override
    public ArrayList<ArrayList<Integer>> getReedsGezet(int i){
        Game game=watchGame.get(i);
        return game.getReedsGezet();
    }
    @Override
    public boolean getEnd(int game){
        Game tempgame=watchGame.get(game);
        return tempgame.getEnd();
    }
    @Override
    public int[] getGameInhaalGok(int game, int viewerId) throws RemoteException {
        Game tempgame=watchGame.get(game);
        int temp[]=tempgame.getInhaalGok();
        for (int i: temp){
            System.out.println("gokpart: "+i);
        }
        return temp;
    }

    @Override
    public int getPunten(String sessionToken) throws RemoteException {
        Game game=(Game)busyGame.get(sessionToken);
        return game.getOntvangenRankingPunten(sessionToken);
    }

    @Override
    public int[] getGameGok(int game, int viewerId){
        if(migrate){
            int[]tempArray=new int[1];
            tempArray[0]=migrateAddress;
            return tempArray;
        }
        else {
            Game tempgame=watchGame.get(game);
            int temp[]=tempgame.getGok();
            for (int i: temp){
                System.out.println("gokpart: "+i);
            }
            return temp;
        }

    }

    @Override
    public int getViewerId(int game){
        Game tempgame=watchGame.get(game);
        return tempgame.getViewerId();
    }
    @Override
    public void geefNotify(String sessionToken){
        Game game=(Game)busyGame.get(sessionToken);
        game.geefNotify();

    }
    @Override
    public void startGame(ArrayList<String>players){
        Game tempGame=new Game();
        System.out.println("Game started");
        tempGame.startGame(players);
        for (String player:players) {
            busyGame.put(player, tempGame);
        }
    }
    @Override
    public HashMap mergeGame(int migrateAddress){
        migrate=true;
        this.migrateAddress=migrateAddress;

        return busyGame;
    }

    @Override
    public HashMap mergeViewGame(int mergeAddres) throws RemoteException {
        HashMap<Integer, String>tempHash=new HashMap<>();
        for (HashMap.Entry<Integer, Game> entry : watchGame.entrySet()) {

            Integer key = entry.getKey();
            Game gameRef1 = (Game) entry.getValue();
            for (HashMap.Entry<String, Game> entry2 : busyGame.entrySet()) {
                String key2 = entry2.getKey();
                Game gameRef2 = (Game) entry2.getValue();

                if(gameRef1.equals(gameRef2)){
                    tempHash.put(key,key2);
                    System.out.println("they match");
                }
            }


        }
        return tempHash;
    }

    @Override
    public void addViewMerge(HashMap<Integer, String> tempHashMap) throws RemoteException{
        for (HashMap.Entry<Integer, String> entry : tempHashMap.entrySet()) {
            Integer key = entry.getKey();
            String value = entry.getValue();
            Game tempGame=busyGame.get(value);
            watchGame.put(key,tempGame);
            System.out.println("mergekey: "+key);
            watchGameKeys.add(key);
            if(!watchGames.contains(tempGame)) {
                watchGames.add(tempGame);
            }
        }
    }

    @Override
    public void addMerge(HashMap<String, Object> tempHashMap) throws RemoteException {
        for (HashMap.Entry<String, Object> entry : tempHashMap.entrySet()) {
            String key = entry.getKey();
            Game value = (Game)entry.getValue();
            System.out.println("mergekey: "+key);
            busyGame.put(key, value);
        }
    }

    @Override
    public HashMap sendObject() throws RemoteException {
        return null;
    }
}
