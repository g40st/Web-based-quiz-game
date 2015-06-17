
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

import de.fhwgt.quiz.application.Player;

public class ConnectionManager {
	
	private static Map <Session, Player> mapSessionPlayer = Collections.synchronizedMap(new ConcurrentHashMap<Session, Player>());
	// Tmp Sessionlist fuer nicht angemeldete user
	private static List<Session> tmpList = Collections.synchronizedList(new ArrayList<Session>());

    // Anzahl der Verbindungen besorgen
    public static synchronized int SessionCount() { 
    	return mapSessionPlayer.size();
    }
    
    // Verbindung hinzufügen
    public static synchronized void addSession(Session session, Player player) { 
    	(mapSessionPlayer).put(session, player);
    }
    
    // Spielleiter in Spielelogik
    public static synchronized boolean gameLeaderPresent() {
    	return mapSessionPlayer.isEmpty();
    }
    
    // Spieler aus der Logik holen
    public static synchronized Player getPlayer(Session session) {
    	Player tmp = null;
    	for (Entry<Session, Player> entry : mapSessionPlayer.entrySet()) {
    		if(entry.getKey().equals(session)){
    			tmp = entry.getValue();
    		}
    		
    	}
		return tmp;
    }
    
    // Verbindung entfernen
    public  static synchronized void removeSession(Session session) {
    	if (mapSessionPlayer.remove(session) == null) {
    		System.out.println("Not present!");
    	}
    }
    
    // Ausgabe aller aktiven Sessions mit Spieler
    public static synchronized void printall() {
    	System.out.println("Anzahl Session mit Spieler: " + SessionCount());
    	for (Entry<Session, Player> entry : mapSessionPlayer.entrySet()) {
			System.out.println(entry.getKey() + "  /  " + entry.getValue().getName());
		}
    	System.out.println("Anzahl temp Sessions: " + tmpList.size());
    	for (Session tempS : tmpList) {
    		System.out.println("tmpSessions: " + tempS);
    	}
    }
    
    // Rückgabe aller aktiven Sessions 
    public static synchronized Set<Session> getSessions() {
    	/*Set<Session> res = new HashSet<Session>();
    	for (Entry<Session, Player> entry : mapSessionPlayer.entrySet()) {
			res.add(entry.getKey());
		}
		return res; */
    	return mapSessionPlayer.keySet();
    }
    
    // Rückgabe aller Spieler
    public static synchronized Collection<Player> getPlayers() {
		return mapSessionPlayer.values();
    }

    // TmpSession handling
    public static synchronized void addTmpSession(Session session) {
    	tmpList.add(session);
    }
    
    public static synchronized boolean removeTmpSession(Session session) {
    	if(tmpList.remove(session)) {
    		return true;
    	} 
    	return false;
    }
    
    public static synchronized List<Session> getTmpSessions() {
    	return tmpList;
    }

}
