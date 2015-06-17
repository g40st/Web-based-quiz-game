
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.TimerTask;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.fhwgt.quiz.application.Player;
import de.fhwgt.quiz.application.Question;
import de.fhwgt.quiz.application.Quiz;
import de.fhwgt.quiz.error.QuizError;

@ServerEndpoint("/player")
public class PlayerEndpoint {
	Quiz quiz = Quiz.getInstance();
	QuizError quizError = new QuizError(); 
	Thread playerBroadcast = new BroadcastThread();
	
	@SuppressWarnings("unchecked")
	@OnOpen
	public void opened(Session session) {
		// Session temporär speichern um Spielerliste zu aktualisieren
		ConnectionManager.addTmpSession(session);
		if(!playerBroadcast.isAlive()) {
			playerBroadcast.start();
		}
		// aktuelle Catalogauswahl verschicken
		if(!ConnectionManager.gameLeaderPresent()) {
			if(quiz.getCurrentCatalog() != null) {
				JSONObject catalogChange = new JSONObject();
				catalogChange.put("Type", "5");
				catalogChange.put("Length", quiz.getCurrentCatalog().getName().length());
				catalogChange.put("Message", quiz.getCurrentCatalog().getName());
				// Anpassen an alle außer Spielleiter
				sendToAllSessions(catalogChange);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@OnClose
	public void closed(Session session) {
		if(!ConnectionManager.removeTmpSession(session)) {
			Player tmp = ConnectionManager.getPlayer(session);
			if(tmp.getId() == 0) { // Superuser left
				quiz.removePlayer(tmp, quizError);
				ConnectionManager.removeSession(session);
				JSONObject error = new JSONObject();
				error.put("Type", "255");
				error.put("Length", 1 + 14);
				error.put("Subtype", "1");
				error.put("Message", "Superuser left");
				sendToAllSessions(error);
				
			} else {
				quiz.removePlayer(tmp, quizError);
				ConnectionManager.removeSession(session);
			}
		}
		if(!playerBroadcast.isAlive()) {
			playerBroadcast = new BroadcastThread();
			playerBroadcast.start();
		} 
	}
	
	@SuppressWarnings("unchecked")
	@OnMessage
	public void receiveMessage(Session session, String message) throws ParseException, IOException, EncodeException {
		JSONObject jsonMessage = (JSONObject)new JSONParser().parse(message);
		int msgType = Integer.parseInt((String) jsonMessage.get("Type"));
		System.out.println("Nachricht von Client: " + jsonMessage);
		// Message vom Type: LoginRequest
		if(msgType == 1) {
			int msgLength = Integer.parseInt((String) jsonMessage.get("Length").toString());
			if(msgLength > 0) {
				Player tmpPlayer = quiz.createPlayer((String) jsonMessage.get("Name"), quizError);
				if(tmpPlayer == null) { // Fehlerfall
					JSONObject error = new JSONObject();
					error.put("Type", "255");
					error.put("Length", 1 + quizError.getDescription().length());
					error.put("Subtype", "1");
					error.put("Message", quizError.getDescription());
					System.out.println(error);
					session.getBasicRemote().sendText(error.toJSONString());
				} else { // Username in Ordnung
					JSONObject player = new JSONObject();
					player.put("Type", "2");
					player.put("Length", "1");
					player.put("ClientID", tmpPlayer.getId());
					ConnectionManager.addSession(session, tmpPlayer);
					ConnectionManager.removeTmpSession(session);
					session.getBasicRemote().sendText(player.toJSONString());
					playerBroadcast = new BroadcastThread();
					playerBroadcast.start();
				}
			} else { // kein Username eingegeben
				JSONObject error = new JSONObject();
				error.put("Type", "255");
				error.put("Length", 17);
				error.put("Subtype", "1");
				error.put("Message", "Username required");
				session.getBasicRemote().sendText(error.toJSONString());
			}	
		} else if (msgType == 5) { // CatalogChange  
			int msgLength = Integer.parseInt((String) jsonMessage.get("Length").toString());
			if(msgLength > 0) {
				if(quiz.changeCatalog(ConnectionManager.getPlayer(session), (String) jsonMessage.get("Filename"), quizError) == null) {
					JSONObject error = new JSONObject();
					error.put("Type", "255");
					error.put("Length", 1 + quizError.getDescription().length());
					error.put("Subtype", "0");
					error.put("Message", quizError.getDescription());
					session.getBasicRemote().sendText(error.toJSONString());
				} else {
					JSONObject catalogChange = new JSONObject();
					catalogChange.put("Type", "5");
					catalogChange.put("Length", quiz.getCurrentCatalog().getName().length());
					catalogChange.put("Message", quiz.getCurrentCatalog().getName());
					sendToAllSessions(catalogChange);
				}
			}
		} else if(msgType == 7) { // startGame message
			if(!quiz.startGame(ConnectionManager.getPlayer(session), quizError)) {
				JSONObject error = new JSONObject();
				error.put("Type", "255");
				error.put("Length", 1 + quizError.getDescription().length());
				error.put("Subtype", "0");
				error.put("Message", quizError.getDescription());
				// Fehler direkt an Spielleiter
				session.getBasicRemote().sendText(error.toJSONString());
			} else {
				JSONObject startGame = new JSONObject();
				startGame.put("Type", "7");
				startGame.put("Length", 0);
				startGame.put("Filename", "");
				sendToAllSessions(startGame);
			}
		} else if(msgType == 8) { // questionRequest received
			TimerTask timerTask = new TimerTaskThread(session);
			Question question = new Question("dummy");
			// TODO Fehlerfall
			question = quiz.requestQuestion(ConnectionManager.getPlayer(session), timerTask, quizError);
			if(question == null) {
				JSONObject error = new JSONObject();
				error.put("Type", "255");
				error.put("Length", 1 + quizError.getDescription().length());
				error.put("Subtype", "0");
				error.put("Message", quizError.getDescription());
				// Fehler direkt an Spielleiter
				session.getBasicRemote().sendText(error.toJSONString());	
			} else {
				System.out.println("QuestionObjekt: " + question);
				JSONObject jsonQuestion = new JSONObject();
				jsonQuestion.put("Type", "9");
				jsonQuestion.put("Length", "769");
				jsonQuestion.put("Frage", question.getQuestion());
				JSONArray arrAnswer = new JSONArray();
				for (String tempAnswer : question.getAnswerList()) {
					arrAnswer.add(tempAnswer);
				}
				jsonQuestion.put("arrAnswer", arrAnswer);
				jsonQuestion.put("Zeitlimit", question.getTimeout());
				session.getBasicRemote().sendText(jsonQuestion.toJSONString());
			}
			
		} else if(msgType == 10) { // qestionResult
			long questionAnswered = 0;
			long clientAnswer = Long.parseLong((String) jsonMessage.get("Selection"));
			questionAnswered = quiz.answerQuestion(ConnectionManager.getPlayer(session), clientAnswer , quizError);
			if(questionAnswered >-1) {
				JSONObject jsonQuestionResult = new JSONObject();
				jsonQuestionResult.put("Type", "11");
				jsonQuestionResult.put("Length", "2");
				jsonQuestionResult.put("TimedOut", "0");
				jsonQuestionResult.put("Correct", questionAnswered);
				session.getBasicRemote().sendText(jsonQuestionResult.toJSONString());	
			} else {
				JSONObject error = new JSONObject();
				error.put("Type", "255");
				error.put("Length", 1 + quizError.getDescription().length());
				error.put("Subtype", "0");
				error.put("Message", quizError.getDescription());
				// Fehler direkt an Spielleiter
				session.getBasicRemote().sendText(error.toJSONString());
			}
			
			
		}
	}
	
	private static void sendToAllSessions(JSONObject error) {
		// Alle aktiven Sessions durchgehen
		Set<Session> tmpMap = ConnectionManager.getSessions();
		for(Iterator<Session> iter = tmpMap.iterator(); iter.hasNext(); ) {
			Session s = iter.next();
			try {
				s.getBasicRemote().sendText(error.toJSONString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		// Alle temp Sessions durchgehen, um die Spielerliste aktuell anzuzeigen
		List<Session> tmpSessions = ConnectionManager.getTmpSessions();
		if(tmpSessions.size() > 0) {
			for (Session tempS : tmpSessions) {
				try {
					tempS.getBasicRemote().sendText(error.toJSONString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}

class BroadcastThread extends Thread {
    private PlayerEndpoint playerEndpoint;

    BroadcastThread() {}
    
    @SuppressWarnings("unchecked")
	public void run() {
    	System.out.println("--------------------------------------------------------------");
    	System.out.println("PlayerBroadcastThread:");
    	ConnectionManager.printall();
    	System.out.println("---------------------------------------------------------------");
    	// PlayerList vorbereiten und verschicken
    	System.out.println("SessionCount: " + ConnectionManager.SessionCount());
    	if(ConnectionManager.SessionCount() > 0) {
    		JSONObject playerList = new JSONObject();
    		playerList.put("Type", "6");
    		playerList.put("Length", ConnectionManager.SessionCount() * 37);
    		Collection<Player> tmpPlayer = ConnectionManager.getPlayers();
    		JSONArray players = new JSONArray();
    		for(Player entry : tmpPlayer) {
    			JSONObject tmp = new JSONObject();
    			tmp.put("Spielername", entry.getName());
    			tmp.put("Punktestand", entry.getScore());
    			tmp.put("ClientID", entry.getId());
    			players.add(tmp);
    		}
    		playerList.put("Players", players);
    		
    		// Alle aktuell angemeldeten SpielerSessions durchgehen
    		Set<Session> tmpMap = ConnectionManager.getSessions();
    		for(Iterator<Session> iter = tmpMap.iterator(); iter.hasNext(); ) {
    			Session s = iter.next();
    			// PlayerList message
    			try {
					s.getBasicRemote().sendText(playerList.toJSONString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
    		}
    		
    		// Alle temp Sessions durchgehen, um die Spielerliste aktuell anzuzeigen
    		List<Session> tmpSessions = ConnectionManager.getTmpSessions();
    		if(tmpSessions.size() > 0) {
    			for (Session tempS : tmpSessions) {
    				try {
    					tempS.getBasicRemote().sendText(playerList.toJSONString());
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
    		}
    	}
    }	
}
