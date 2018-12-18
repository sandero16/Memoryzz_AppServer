package sample;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface AppInterface extends Remote {
    int[]getAndereGok(String sessionToken) throws  RemoteException;
    int[]getGameGok(int i, int viewerId) throws RemoteException;
    void testConnectie() throws  RemoteException;
    boolean vindtTegenspeler(String sessionToken) throws RemoteException;
    void startGame(ArrayList<String> players) throws RemoteException;
    boolean setGame(String sessionToken) throws RemoteException;
    int getZet(int i, String sessionToken) throws RemoteException;
    void changeBeurt(String sessionToken) throws RemoteException;
    boolean checkBeurt(String sessionToken) throws RemoteException;
    int getResult(String sessionToken) throws  RemoteException;
    int getScore(String sessionToken) throws RemoteException;
    int getGame(int i) throws RemoteException;
    ArrayList<ArrayList<Integer>>getReedsGezet(int i) throws RemoteException;
    boolean getEnd(int game) throws  RemoteException;
    int getViewerId(int game) throws  RemoteException;
    void geefNotify(String sessionToken) throws RemoteException;
    HashMap<String,Object> mergeGame(int migrateAddress) throws RemoteException;
    HashMap<Integer,Object> mergeViewGame(int mergeAddres) throws RemoteException;
    void addMerge(HashMap<String, Object> tempHashMap) throws RemoteException;
    void addViewMerge(HashMap<Integer, String> tempHashMap) throws RemoteException;
    int[]getGameInhaalGok(int i, int viewerId) throws RemoteException;
    int getPunten(String sessionToken) throws RemoteException;
    HashMap sendObject() throws RemoteException;

}