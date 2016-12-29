package atb.test.webapp.util;

import java.util.regex.Pattern;

public class UserLoginUtil {
	
	public static final String ODATA_SERVICE_URL = "http://localhost:8080/WebApplication/odata";
	public static final String GOOGLE_CSE_API_URL = "https://www.googleapis.com/customsearch/v1?key=AIzaSyA0eV_mQ5sXMnHKf_qDKsdfRDU1NrK94g0&cx=017337551614757810777:tam1j_phddi";
	
	public static final String USER_NAME = "UserName";
	public static final String USER_PASSWORD = "Password";
	
	/**
	 * User password validation implementation 
	 * @param password
	 * @return validation message
	 */
	public static String ValidateUserPassword(String password){
		
		String errorMsg = "";
		Pattern pwdPattern = Pattern
				.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#&$%]).{8,12})");
		if (pwdPattern.matcher(password).matches()) {
			int cnt = 0;
			for (int i = 0; cnt < 4 && i < password.length(); i++) {
				if (Character.isLetter(password.charAt(i))) {
					cnt++;
				} else {
					cnt = 0;
				}
			}
			if (cnt > 3 ) {
				errorMsg = "Invaild password: password cannot have more than 3 letters together";
			}
		} else {
			errorMsg = "Invaild password: password must have at least one captial, one lower case letter and one of  %#*&!@";
		}
		return errorMsg;
	}
	
	/**
	 * User name validation implementation
	 * @param userName
	 * @return validation message
	 */
	public static String ValidateUserName ( String userName ){
		
		String errorMsg = "";
		Pattern namePattern = Pattern
				.compile("^[a-zA-Z]{3,10}$");
		if (!namePattern.matcher(userName).matches()) {
			errorMsg = "Invaild user name: user name has to be 3 - 10 characters" ;
		}
		return errorMsg;
	}

}
