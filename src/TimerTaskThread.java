import java.io.IOException;
import java.util.TimerTask;

import javax.websocket.Session;

import org.json.simple.JSONObject;


public class TimerTaskThread extends TimerTask{
	
	Session session;
	
	TimerTaskThread(Session session) {
		this.session = session;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		// Sende Timeout Message
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
