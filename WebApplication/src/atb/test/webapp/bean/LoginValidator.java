package atb.test.webapp.bean;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import atb.test.webapp.util.UserLoginUtil;

@FacesValidator("loginValidator")
public class LoginValidator implements Validator {

	@Override
	/**
	 * user name and password validation on client side. 
	 * Method will be called by JSF when f:validator tag is used in page. 
	 */
	public void validate(FacesContext fContext, UIComponent uic, Object obj)
			throws ValidatorException {

		//password validation
		if (uic.getId().equalsIgnoreCase("password")) {
			String msg = UserLoginUtil.ValidateUserPassword((String) obj);
			if (!msg.isEmpty()) {
				FacesMessage faceMsg = new FacesMessage(msg);
				faceMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(faceMsg);
			}

		}
		//user name validation
		if (uic.getId().equalsIgnoreCase("username")){
			String msg = UserLoginUtil.ValidateUserName((String) obj);
			if (!msg.isEmpty()) {
				FacesMessage faceMsg = new FacesMessage(msg);
				faceMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(faceMsg);
			}
		}

	}

}