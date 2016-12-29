package atb.test.odata.service;

import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ClientServerError;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import atb.test.webapp.util.UserLoginUtil;

/**
 * The processor that processes OData requests and responses, and provides the
 * CUD (create, update and delete) operations on the UserCredential entity type.
 */
public class UserInfoProcessor implements EntityProcessor {

	private OData odata;
	private ServiceMetadata serviceMetadata;

	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	/**
	 * Provides the Read service on the UserCredential entity type. The method
	 * is invoked via HTTP GET
	 * 
	 * @param request
	 * @param response
	 * @param uriInfo
	 * @param contentType
	 * @throws ODataApplicationException
	 * @throws SerializerException
	 */
	@Override
	public void readEntity(ODataRequest request, ODataResponse response,
			UriInfo uriInfo, ContentType contentType)
			throws ODataApplicationException, SerializerException {

		// Retrieve the entity set and type from the URI
		EdmEntitySet edmEntitySet = this.getRequestEntitySet(uriInfo);
		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet)
				.build();
		EntitySerializerOptions options = EntitySerializerOptions.with()
				.contextURL(contextUrl).build();

		ODataFormat oDataFormat = ODataFormat.fromContentType(contentType);
		ODataSerializer serializer = this.odata.createSerializer(oDataFormat);
		SerializerResult serializedResponse;

		// Retrieve the key parameters from request URI information
		Entity entity = null;
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths
				.get(0);
		List<UriParameter> uriParams = uriResourceEntitySet.getKeyPredicates();

		//find an entity identified by key
		for (UriParameter uriParam : uriParams) {
			String paramName = uriParam.getName();
			if (paramName.equalsIgnoreCase(UserLoginUtil.USER_NAME)) {
				entity = UserDataProvider.getInstance().find(uriParam);
				break;
			}
		}
		
		//generate oData response
		if (entity != null) {
			serializedResponse = serializer.entity(serviceMetadata,
					edmEntitySet.getEntityType(), entity, options);
			response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		} else {
			ClientServerError error = new ClientServerError();
			serializedResponse = serializer.error(error
					.setMessage("User Name no found..."));
			response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
		}
		response.setContent(serializedResponse.getContent());
		response.setHeader(HttpHeader.CONTENT_TYPE,
				contentType.toContentTypeString());
	}

	/**
	 * 
	 * @param uriInfo
	 * @return
	 * @throws ODataApplicationException
	 */
	private EdmEntitySet getRequestEntitySet(UriInfoResource uriInfo)
			throws ODataApplicationException {
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		if (!(resourcePaths.get(0) instanceof UriResourceEntitySet)) {
			throw new ODataApplicationException("Invalid resource type.",
					HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
					Locale.ENGLISH);
		}
		UriResourceEntitySet uriResource = (UriResourceEntitySet) resourcePaths
				.get(0);
		return uriResource.getEntitySet();
	}

	/**
	 * 
	 */
	@Override
	public void createEntity(ODataRequest arg0, ODataResponse arg1,
			UriInfo arg2, ContentType arg3, ContentType arg4)
			throws ODataApplicationException, DeserializerException,
			SerializerException {

		throw new ODataApplicationException(
				"Create primitive is not supported yet.",
				HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);

	}

	/**
	 * 
	 */
	@Override
	public void deleteEntity(ODataRequest arg0, ODataResponse arg1, UriInfo arg2)
			throws ODataApplicationException {

		throw new ODataApplicationException(
				"Delete primitive is not supported yet.",
				HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
	}

	/**
	 * 
	 */
	@Override
	public void updateEntity(ODataRequest arg0, ODataResponse arg1,
			UriInfo arg2, ContentType arg3, ContentType arg4)
			throws ODataApplicationException, DeserializerException,
			SerializerException {

		throw new ODataApplicationException(
				"Update primitive is not supported yet.",
				HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
	}

}
