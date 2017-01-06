package atb.test.webapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class UserLoginUtil {

	public static final String USER_NAME = "UserName";
	public static final String USER_PASSWORD = "Password";

	/**
	 * User password validation implementation
	 * 
	 * @param password
	 * @return validation message
	 */
	public static String ValidateUserPassword(String password) {

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
			if (cnt > 3) {
				errorMsg = "Invaild password: password cannot have more than 3 letters together";
			}
		} else {
			errorMsg = "Invaild password: password must have at least one captial, one lower case letter and one of  %#*&!@";
		}
		return errorMsg;
	}

	/**
	 * User name validation implementation
	 * 
	 * @param userName
	 * @return validation message
	 */
	public static String ValidateUserName(String userName) {

		String errorMsg = "";
		Pattern namePattern = Pattern.compile("^[a-zA-Z]{3,10}$");
		if (!namePattern.matcher(userName).matches()) {
			errorMsg = "Invaild user name: user name has to be 3 - 10 characters";
		}
		return errorMsg;
	}

	public static String getServiceURL(String serviceName) {

		String url = null;
		Properties prop = new Properties();
		try {
			InputStream in = UserLoginUtil.class.getClassLoader()
					.getResourceAsStream("url.properties");

			prop.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		url = prop.getProperty(serviceName);

		return url;
	}
}
