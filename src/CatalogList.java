

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.*;

import de.fhwgt.quiz.application.Catalog;
import de.fhwgt.quiz.application.Quiz;
import de.fhwgt.quiz.loader.LoaderException;


/**
 * Servlet implementation class CatalogList
 */
@WebServlet("/catalogList")
public class CatalogList extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CatalogList() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		Quiz quiz = Quiz.getInstance();
		int type = Integer.parseInt(request.getParameter("Type"));
		JSONArray arrCatalogList = new JSONArray();
		// CatalogResponse
		if(type == 3) {
			try {
				// JSONArray mit den Katalogen fuellen
				Map<String, Catalog> map = quiz.getCatalogList();
				List<String> catalogList = new ArrayList<String>(map.keySet());
				JSONObject catalog;
				for(int i = 0; i < catalogList.size(); i++) {
					String tmpCatalog = (String) catalogList.get(i);
					tmpCatalog = tmpCatalog.substring(0, tmpCatalog.length() - 4);
					catalogList.set(i,tmpCatalog);
					catalog = new JSONObject();
					catalog.put("Type", "4");
					catalog.put("Length", String.valueOf(catalogList.get(i).length()));
					catalog.put("Name", (String) catalogList.get(i));
					arrCatalogList.add(catalog);
				}
				// Null-Terminierung
				catalog = new JSONObject();
				catalog.put("Type", "4");
				catalog.put("Length", "0");
				catalog.put("Name", "");
				arrCatalogList.add(catalog);
				
				PrintWriter writer = response.getWriter();	
				String json= arrCatalogList.toJSONString();
				writer.print(json);
		    } catch (IOException e) {
		        e.printStackTrace();
		    } catch (LoaderException e) {
				e.printStackTrace();
		    }
		} else {
			// ToDo sende Fehler!
		}
	}
}
