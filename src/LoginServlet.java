
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.fhwgt.quiz.application.*;
import de.fhwgt.quiz.error.*;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public void init() {
		
	}   
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("doPost");
		Quiz quiz = Quiz.getInstance();
		QuizError quizError = new QuizError(); 
		response.setContentType("text/html;charset=UTF-8");
		
		// den Spieler in die Spiellogik einfuegen
		if(request.getParameter("username").equals("")) {
			request.setAttribute("LoginError", true);
			request.setAttribute("LoginErrorMessage", "username required");
		} else {
			if(quiz.createPlayer((String) request.getParameter("username"), quizError) == null) {
				System.out.println(quizError.getDescription());
				request.setAttribute("LoginError",true);
				request.setAttribute("LoginErrorMessage", quizError.getDescription());
			}
		}
		// die Spieler aus der Spiellogik in eine List schreiben
		List<Player> playerList = new ArrayList<Player>(quiz.getPlayerList());
		request.setAttribute("playerList", playerList);
		RequestDispatcher requestDispatcher = request.getRequestDispatcher("index.jsp");
		requestDispatcher.forward(request, response);
		 
	}

}
