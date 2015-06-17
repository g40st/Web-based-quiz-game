import java.io.IOException;
import java.util.TimerTask;

import javax.websocket.Session;

import org.json.simple.JSONObject;

import de.fhwgt.quiz.application.Quiz;
import de.fhwgt.quiz.error.QuizError;


public class TimerTaskThread extends TimerTask{
	
	Session session;
	
	TimerTaskThread(Session session) {
		this.session = session;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		// Sende Timeout Message
		Quiz quiz = Quiz.getInstance();
		QuizError quizError = new QuizError(); 
		if(quiz.answerQuestion(ConnectionManager.getPlayer(session), 4, quizError) == -1){
			JSONObject error = new JSONObject();
			error.put("Type", "255");
			error.put("Length", 1 + quizError.getDescription().length());
			error.put("Subtype", "0");
			error.put("Message", quizError.getDescription());
			try {
				session.getBasicRemote().sendText(error.toJSONString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			JSONObject timeout = new JSONObject();
			timeout.put("Type", "11");
			timeout.put("Length", "2");
			timeout.put("TimedOut", "1");
			timeout.put("Correct", "-1");
			try {
				session.getBasicRemote().sendText(timeout.toJSONString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
