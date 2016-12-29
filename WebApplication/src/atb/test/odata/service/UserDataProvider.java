package atb.test.odata.service;

import java.io.InputStream;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.server.api.uri.UriParameter;
import org.json.JSONArray;
import org.json.JSONTokener;

import atb.test.webapp.util.UserLoginUtil;

public class UserDataProvider {

	private static UserDataProvider dataProvider = null;
	private EntityCollection entitySet;

	public static UserDataProvider getInstance() {
		if (dataProvider == null) {
			dataProvider = new UserDataProvider();
		}
		return dataProvider;
	}

	/**
	 * returns a single entity identified by key
	 * @param key
	 * @return
	 */
	public Entity find(UriParameter key) {

		if (entitySet == null) {
			entitySet = loadUserInfo();
		}
		for (Entity entity : entitySet.getEntities()) {
			String kValue = (key.getText().startsWith("'") ? key.getText()
					.substring(1, key.getText().length() - 1) : key.getText());
			if (entity.getProperty(key.getName()).getValue().equals(kValue))
				return entity;
		}
		return null;
	}

	/**
	 * Loads stored user information from a JSON data file and converts to an EntityCollection 
	 * @return EntityCollection 
	 */
	public EntityCollection loadUserInfo() {

		EntityCollection entitySet = new EntityCollection();
		InputStream in = getClass().getClassLoader().getResourceAsStream("users.dat");
		JSONArray users = new JSONArray(new JSONTokener(in));
		for (int i = 0; i < users.length(); i++) {
			entitySet.getEntities().add(
					new Entity().addProperty(
							createPrimitive(UserLoginUtil.USER_NAME, users.getJSONObject(i)
									.getString(UserLoginUtil.USER_NAME))).addProperty(
							createPrimitive(UserLoginUtil.USER_PASSWORD, users.getJSONObject(i)
									.getString(UserLoginUtil.USER_PASSWORD))));
		}
		return entitySet;
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	private Property createPrimitive(final String name, final Object value) {
		return new Property(null, name, ValueType.PRIMITIVE, value);
	}

}
