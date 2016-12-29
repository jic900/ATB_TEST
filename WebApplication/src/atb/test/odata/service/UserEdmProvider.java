package atb.test.odata.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.ODataException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

import atb.test.webapp.util.UserLoginUtil;
/**
 * The class is a Entity Data Model(edm) that provides the entire structure of the OData service.
 * It defines service document that can be invoked via <serviceroot>/, service metadata document that can be
 * invoked at <serviceroot>/$metadata. The metadata document includes the structure of what entity types and entity
 * collections are provided in this Session service.
 */
public class UserEdmProvider extends CsdlAbstractEdmProvider {
	
	 public static final String NAMESPACE = "OData.UserCredential";

	    // EDM Container
	    public static final String CONTAINER_NAME = "Container";
	    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

	    // Entity Types Names
	    public static final String ET_USER_CREDENTIAL_NAME = "UserCredential";
	    public static final FullQualifiedName USER_CREDENTIAL_FQN = new FullQualifiedName(NAMESPACE, ET_USER_CREDENTIAL_NAME);

	    // Entity Set Names
	    public static final String ES_USER_CREDENTIALS_NAME = "UserCredentials";

	    /**
	     * The root element that includes all entities and entity sets defined
	     * in the service.
	     * 
	     * @return  all defined schemas.
	     * @throws ODataException 
	     */
	    @Override
	    public List<CsdlSchema> getSchemas() throws ODataException {
	        CsdlSchema schema = new CsdlSchema();
	        schema.setNamespace(NAMESPACE);

	        List<CsdlEntityType> entityTypes = new ArrayList<>();
	        entityTypes.add(getEntityType(USER_CREDENTIAL_FQN));
	        schema.setEntityTypes(entityTypes);
	        schema.setEntityContainer(getEntityContainer());

	        List<CsdlSchema> schemas = new ArrayList<>();
	        schemas.add(schema);
	        return schemas;
	    }

	    /**
	     * Create entity types that can be retrieved by a type name.
	     * Specifically, it creates the AuthSession entity type for the session
	     * OData service.
	     * 
	     * @param entityTypeName  a specific entity type name
	     * @return  a new entity type based on the type name
	     * @throws ODataException 
	     */
	    @Override
	    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {

	        CsdlEntityType entityType = null;
	        // this method is called for one of the EntityTypes that are configured in the Schema
	        if (entityTypeName.equals(USER_CREDENTIAL_FQN)) {
	            //create EntityType properties
	            CsdlProperty userName = new CsdlProperty().setName(UserLoginUtil.USER_NAME).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
	            CsdlProperty userPassword = new CsdlProperty().setName(UserLoginUtil.USER_PASSWORD).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
	            
	            // create CsdlPropertyRef for Key element
	            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
	            propertyRef.setName(UserLoginUtil.USER_NAME);

	            // configure EntityType
	            entityType = new CsdlEntityType();
	            entityType.setName(ET_USER_CREDENTIAL_NAME);
	            entityType.setProperties(Arrays.asList(userName, userPassword));
	            entityType.setKey(Collections.singletonList(propertyRef));
	        }
	        return entityType;
	    }

	    /**
	     * Create entity set types that can be retrieved by a container name 
	     * and a set type name. Specifically, it creates the AuthSessions entity set type for the session
	     * OData service.
	     * 
	     * @param entityContainer  a container name that needs to be matched
	     * @param entitySetName  a specific entity set name
	     * @return  the entity set created upon success, otherwise null
	     * @throws ODataException 
	     */
	    @Override
	    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
	        CsdlEntitySet entitySet = null;
	        if (entityContainer.equals(CONTAINER)) {
	            if (entitySetName.equals(ES_USER_CREDENTIALS_NAME)) {
	                entitySet = new CsdlEntitySet();
	                entitySet.setName(ES_USER_CREDENTIALS_NAME);
	                entitySet.setType(USER_CREDENTIAL_FQN);
	            }
	        }
	        return entitySet;
	    }

	    /**
	     * Retrieve the container that carries all entity sets.
	     * 
	     * @return  the defined container
	     * @throws ODataException 
	     */
	    @Override
	    public CsdlEntityContainer getEntityContainer() throws ODataException {
	        // create EntitySets
	        List<CsdlEntitySet> entitySets = new ArrayList<>();
	        entitySets.add(getEntitySet(CONTAINER, ES_USER_CREDENTIALS_NAME));

	        // create EntityContainer
	        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
	        entityContainer.setName(CONTAINER_NAME);
	        entityContainer.setEntitySets(entitySets);
	        return entityContainer;
	    }

	    /**
	     * Retrieve the service document for a specific container. The service
	     * document is invoked via <serviceroot>/, e.g., http://localhost:8080/odata
	     * 
	     * @param entityContainerName  a specific container that the service document is requested for
	     * @return  the retrieved container information
	     * @throws ODataException 
	     */
	    @Override
	    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) throws ODataException {
	        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
	            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
	            entityContainerInfo.setContainerName(CONTAINER);
	            return entityContainerInfo;
	        }
	        return null;
	    }
	
}
