package atb.test.webapp.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.edmx.EdmxReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atb.test.odata.service.UserEdmProvider;
import atb.test.odata.service.UserInfoProcessor;

/**
 * Servlet implementation class
 */

public class ODataServiceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory
			.getLogger(ODataServiceServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ODataServiceServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {

		try {
			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(
					new UserEdmProvider(), new ArrayList<EdmxReference>());
			ODataHttpHandler handler = odata.createHandler(edm);
			handler.register(new UserInfoProcessor());
			handler.process(req, resp);
		} catch (RuntimeException e) {
			LOG.error("Server Error", e);
			throw new ServletException(e);
		}

	}
}
