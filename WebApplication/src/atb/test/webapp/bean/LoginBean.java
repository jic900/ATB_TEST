package atb.test.webapp.bean;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atb.test.odata.service.UserEdmProvider;
import atb.test.webapp.util.SearchResult;
import atb.test.webapp.util.UserLoginUtil;

@ManagedBean(name = "login")
public class LoginBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(LoginBean.class);

	private String userName;
	private String password;
	private boolean validated = true;
	private String greetMsg;
	private List<SearchResult> searchResults;
	private List<String> messages;

	/**
	 * Retrieve oData entity for a particular user by sending odata retrieve request
	 * and verified user name and password.
	 * @return verification message.
	 */
	public String getODataResult() {

		final String SERVICE_URI = UserLoginUtil.getServiceURL("ODATA_SERVICE_URL");
		String message = "";
		ODataClient client = ODataClientFactory.getClient();
		client.getConfiguration().setDefaultPubFormat(
				ODataFormat.JSON_NO_METADATA);

		URI userEntityURI = client
				.newURIBuilder(SERVICE_URI)
				.appendEntitySetSegment(
						UserEdmProvider.ES_USER_CREDENTIALS_NAME)
				.appendKeySegment(this.getUserName()).build();
		ODataRetrieveResponse<ClientEntity> userCredential = client
				.getRetrieveRequestFactory().getEntityRequest(userEntityURI)
				.execute();

		if (userCredential.getStatusCode() != HttpStatusCode.OK.getStatusCode()) {
			message = "User name cannot be found, you may an unauthorized user.";
		} else {
			for (ClientProperty uc : userCredential.getBody().getProperties()) {
				if (uc.getName().toString().equals(UserLoginUtil.USER_PASSWORD)
						&& !uc.getValue().toString().equals(this.password)) {
					message = "You input an incorrct password.";
				}
			}
		}
		return message;
	}

	/**
	 * Retrieve results for a particular search by sending REST request to
	 * Google Custom Search API
	 * @return search result list
	 */
	public List<SearchResult> getGoogleSearchResult() {

		List<SearchResult> results = new ArrayList<>();
		
		final String urlGoogelSearchAPI = UserLoginUtil.getServiceURL("GOOGLE_CSE_API_URL")
				.concat("&q="+this.getUserName());
		
		try {
			URL url = new URL(urlGoogelSearchAPI);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			JSONTokener jt = new JSONTokener(con.getInputStream());
			JSONObject obj = new JSONObject(jt);
			JSONArray ja = obj.getJSONArray("items");
			for (int i = 0; i < 5; i++) {
				results.add(new SearchResult (ja.getJSONObject(i).getString("title"),
						ja.getJSONObject(i).getString("link"),
						ja.getJSONObject(i).getString("snippet")));
			}
		} catch (MalformedURLException e) {
			LOG.error("getGoogleSearchResult error", e);
		} catch (IOException e) {
			LOG.error("getGoogleSearchResult error", e);
		}
		return results;
	}
	
	/**
	 * Controller method 
	 * @return page name
	 */
	public String userLogin() {
		messages = loginValidation();
		if (messages.isEmpty()){
			this.setGreetMsg("Welcome ! " + this.userName);
			this.setSearchResults(getGoogleSearchResult());
		} else {
			this.setGreetMsg("Oops...   " + this.userName);
		}
		if (validated && !messages.isEmpty()) {
			return "Login";
		} else {
			return "Info";
		}
	}

	/**
	 * Back to login page when clicking "Back" 
	 * @return page name
	 */
	public String userLogout() {
		setUserName(null);
		setPassword(null);
		return "Login";
	}
	

	/**
	 * User name and Password validation on server side
	 * @return validation messages, 
	 */
	private ArrayList<String> loginValidation() {
		ArrayList<String> messages = new ArrayList<String>();
		String message = "";
		message = UserLoginUtil.ValidateUserName(this.userName);
			if (!message.isEmpty()){
				messages.add(message);
			}
		message = UserLoginUtil.ValidateUserPassword(this.password);
			if (!message.isEmpty()){
				messages.add(message);
			}
		
		if (messages.isEmpty()) {
			message = getODataResult();
			if (!message.isEmpty()){
				messages.add(message);
			}
		}
		
		return messages;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	
	public String getGreetMsg() {
		return greetMsg;
	}

	public void setGreetMsg(String greetMsg) {
		this.greetMsg = greetMsg;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	public List<SearchResult> getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(List<SearchResult> searchResults) {
		this.searchResults = searchResults;
	}

	

}
