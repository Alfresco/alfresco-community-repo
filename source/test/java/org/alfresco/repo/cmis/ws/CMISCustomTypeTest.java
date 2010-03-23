/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import junit.framework.TestCase;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.repo.cmis.ws.utils.PropertyUtil;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CMISCustomTypeTest extends TestCase
{
    private static final String FILTER_ANY = "*";

    private static final String CMIS_TYPE_BASE_FOLDER = "cmis:folder";
    private static final String CMIS_TYPE_BASE_DOCUMENT = "cmis:document";

    private static final String CMISCUSTOM_TYPE_DOCUMENT = "D:cmiscustom:document";
    private static final String CMISCUSTOM_TYPE_FOLDER = "F:cmiscustom:folder";

    private static final String CMIS_NAME = "cmis:name";
    private static final String CMIS_OBJECT_ID = "cmis:objectId";
    private static final String CMIS_OBJECT_TYPE_ID = "cmis:objectTypeId";

    private static final String CMISCUSTOM_DOCPROP_STRING = "cmiscustom:docprop_string";
    private static final String CMISCUSTOM_DOCPROP_BOOLEAN_MULTI = "cmiscustom:docprop_boolean_multi";
    private static final String CMISCUSTOM_FOLDERPROP_STRING = "cmiscustom:folderprop_string";

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[] { "classpath:alfresco/application-context.xml",
            "classpath:alfresco/cmis-ws-context.xml" });
    private static AuthenticationService authenticationService;

    private static String folderId;
    private static String repositoryId;
    private static RepositoryServicePort repositoryServicePort;
    private static ObjectServicePort objectServicePort;
    private static DiscoveryServicePort discoveryServicePort;
    private static NavigationServicePort navigationServicePort;
    private static PropertyUtil propertiesUtil;
    private ObjectFactory cmisObjectFactory = new ObjectFactory();

    @Override
    protected void setUp() throws Exception
    {
        if (null == authenticationService)
        {
            ServiceRegistry serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
            authenticationService = serviceRegistry.getAuthenticationService();
            propertiesUtil = (PropertyUtil) applicationContext.getBean("propertiesUtils");
            repositoryServicePort = (RepositoryServicePort) applicationContext.getBean("dmRepositoryService");
            objectServicePort = (ObjectServicePort) applicationContext.getBean("dmObjectService");
            navigationServicePort = (NavigationServicePort) applicationContext.getBean("dmNavigationService");
            discoveryServicePort = (DiscoveryServicePort) applicationContext.getBean("dmDiscoveryService");
        }
        authenticationService.authenticate(USERNAME, PASSWORD.toCharArray());

        repositoryId = repositoryId == null ? repositoryServicePort.getRepositories(null).get(0).getRepositoryId() : repositoryId;
        if (folderId == null)
        {
            String rootFolderId = repositoryServicePort.getRepositoryInfo(repositoryId, null).getRootFolderId();
            // TODO: orderBy
            // TODO: renditionFilter
            CmisObjectInFolderListType response = navigationServicePort.getChildren(repositoryId, rootFolderId, FILTER_ANY, null, false, EnumIncludeRelationships.NONE, "", false,
                    BigInteger.ZERO, BigInteger.ZERO, null);
            assertNotNull(response);
            assertNotNull(response.getObjects());
            for (CmisObjectInFolderType cmisObjectType : response.getObjects())
            {
                assertNotNull(cmisObjectType);
                assertNotNull(cmisObjectType.getObject());
                assertNotNull(cmisObjectType.getObject().getProperties());
                CmisPropertyString propertyString = (CmisPropertyString) getCmisProperty(cmisObjectType.getObject().getProperties(), CMIS_NAME);
                if (propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() > 0 && "CMIS Tests".equals(propertyString.getValue().get(0)))
                {
                    folderId = ((CmisPropertyId) getCmisProperty(cmisObjectType.getObject().getProperties(), CMIS_OBJECT_ID)).getValue().get(0);
                    break;
                }
            }
            if (folderId == null)
            {
                folderId = createTestFolder(repositoryId, "CMISTests(" + System.currentTimeMillis() + ")", rootFolderId, false);
            }
        }

    }

    @Override
    protected void tearDown() throws Exception
    {
        authenticationService.clearCurrentSecurityContext();
    }

    public void testCreateFolder() throws Exception
    {
        String folderId1 = createTestFolder(repositoryId, ("testCreateCustomFolder(" + System.currentTimeMillis() + ")"), folderId, true);
        assertNotNull(folderId1);

        CmisPropertiesType propertiesObject = objectServicePort.getProperties(repositoryId, folderId1, FILTER_ANY, null);
        assertNotNull(propertiesObject);
        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(propertiesObject, CMIS_OBJECT_TYPE_ID);
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1 && CMISCUSTOM_TYPE_FOLDER.equals(objectTypeId.getValue().get(0)));
        CmisPropertyString customProp = (CmisPropertyString) getCmisProperty(propertiesObject, CMISCUSTOM_FOLDERPROP_STRING);
        assertTrue(customProp != null && customProp.getValue() != null && customProp.getValue().size() == 1 && "custom string".equals(customProp.getValue().get(0)));
    }

    public void testCreateDocument() throws Exception
    {
        String documentId = createTestDocument(repositoryId, "testCreateCustomDocument" + System.currentTimeMillis(), folderId, true);
        assertNotNull(documentId);

        CmisPropertiesType propertiesObject = objectServicePort.getProperties(repositoryId, documentId, FILTER_ANY, null);
        assertNotNull(propertiesObject);

        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(propertiesObject, CMIS_OBJECT_TYPE_ID);
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1
                && CMISCUSTOM_TYPE_DOCUMENT.equals(objectTypeId.getValue().get(0)));
        CmisPropertyString customProp = (CmisPropertyString) getCmisProperty(propertiesObject, CMISCUSTOM_DOCPROP_STRING);
        assertTrue(customProp != null && customProp.getValue() != null && customProp.getValue().size() == 1 && "custom string".equals(customProp.getValue().get(0)));

        CmisPropertyBoolean propertyMulti = (CmisPropertyBoolean) getCmisProperty(propertiesObject, CMISCUSTOM_DOCPROP_BOOLEAN_MULTI);
        assertTrue(propertyMulti != null && propertyMulti.getValue() != null && propertyMulti.getValue().size() == 2 && propertyMulti.getValue().get(0)
                && !propertyMulti.getValue().get(1));
    }

    public void testUpdate() throws Exception
    {
        String documentId = createTestDocument(repositoryId, ("testUpdateCustomDocument (" + System.currentTimeMillis() + ").txt"), folderId, true);
        assertNotNull(documentId);

        Holder<String> holder = new Holder<String>(documentId);
        CmisPropertiesType properties = new CmisPropertiesType();
        String newName = "Updated Title " + System.currentTimeMillis();
        String customProp = "custom " + System.currentTimeMillis();
        CmisPropertyString cmisPropertyString = new CmisPropertyString();
        cmisPropertyString.setPropertyDefinitionId(CMIS_NAME);
        cmisPropertyString.getValue().add(newName);
        properties.getProperty().add(cmisPropertyString);
        cmisPropertyString = new CmisPropertyString();
        cmisPropertyString.setPropertyDefinitionId(CMISCUSTOM_DOCPROP_STRING);
        cmisPropertyString.getValue().add(customProp);
        properties.getProperty().add(cmisPropertyString);
        CmisPropertyBoolean cmisPropertymulti = new CmisPropertyBoolean();
        cmisPropertymulti.setPropertyDefinitionId(CMISCUSTOM_DOCPROP_BOOLEAN_MULTI);
        cmisPropertymulti.getValue().add(false);
        cmisPropertymulti.getValue().add(true);
        properties.getProperty().add(cmisPropertymulti);
        objectServicePort.updateProperties(repositoryId, holder, null, properties, null);

        CmisPropertiesType propertiesObject = objectServicePort.getProperties(repositoryId, documentId, FILTER_ANY, null);
        assertNotNull(propertiesObject);

        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(propertiesObject, CMIS_OBJECT_TYPE_ID);
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1
                && CMISCUSTOM_TYPE_DOCUMENT.equals(objectTypeId.getValue().get(0)));
        CmisPropertyString propertyString = (CmisPropertyString) getCmisProperty(propertiesObject, CMIS_NAME);
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && newName.equals(propertyString.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(propertiesObject, CMISCUSTOM_DOCPROP_STRING);
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && customProp.equals(propertyString.getValue().get(0)));
        CmisPropertyBoolean propertyMulti = (CmisPropertyBoolean) getCmisProperty(propertiesObject, CMISCUSTOM_DOCPROP_BOOLEAN_MULTI);
        assertTrue(propertyMulti != null && propertyMulti.getValue() != null && propertyMulti.getValue().size() == 2 && !propertyMulti.getValue().get(0)
                && propertyMulti.getValue().get(1));
    }

    public void testDelete() throws Exception
    {
        String documentId = createTestDocument(repositoryId, ("testDeleteCustomDocument(" + System.currentTimeMillis() + ").txt"), folderId, true);
        assertNotNull(documentId);
        assertTrue(isObjectInFolder(repositoryId, documentId, folderId));
        Holder<CmisExtensionType> extensions = new Holder<CmisExtensionType>();
        objectServicePort.deleteObject(repositoryId, documentId, true, extensions);
        assertFalse(isObjectInFolder(repositoryId, documentId, folderId));
    }

    public void testQuery() throws Exception
    {
        String folderId1 = createTestFolder(repositoryId, ("testQueryCustom(" + System.currentTimeMillis() + ")"), folderId, false);
        String documentId1 = createTestDocument(repositoryId, "apple1.txt", folderId1, false);
        assertNotNull(documentId1);
        String doc2name = "name(" + System.currentTimeMillis() + ").txt";
        String documentId2 = createTestDocument(repositoryId, doc2name, folderId1, true);
        assertNotNull(documentId2);
        String documentId3 = createTestDocument(repositoryId, "banana1.txt", folderId1, true);
        assertNotNull(documentId3);

        CmisTypeDefinitionType typeDefinition = repositoryServicePort.getTypeDefinition(repositoryId, CMISCUSTOM_TYPE_DOCUMENT, null);

        String query = "SELECT " + CMIS_OBJECT_ID + ", " + CMIS_NAME + ", " + CMIS_OBJECT_TYPE_ID + ", " + CMISCUSTOM_DOCPROP_STRING + ", " + CMISCUSTOM_DOCPROP_BOOLEAN_MULTI
                + " FROM " + typeDefinition.getQueryName() + " WHERE IN_FOLDER('" + folderId1 + "') AND " + CMISCUSTOM_DOCPROP_STRING + "='custom string'";
        Query queryType = new Query();
        queryType.setRepositoryId(repositoryId);
        queryType.setStatement(query);
        queryType.setSkipCount(cmisObjectFactory.createQuerySkipCount(BigInteger.valueOf(3)));
        queryType.setMaxItems(cmisObjectFactory.createQueryMaxItems(BigInteger.valueOf(5)));

        QueryResponse response = discoveryServicePort.query(queryType);
        assertNotNull(response);
        assertNotNull(response.getObjects());
        assertNotNull(response.getObjects().getObjects());
        assertEquals(2, response.getObjects().getObjects().size());

        CmisObjectType objectType1 = null;
        CmisObjectType objectType2 = null;
        for (int i = 0; i < 2; i++)
        {
            CmisPropertyId cmisPropertyId = (CmisPropertyId) getCmisProperty(response.getObjects().getObjects().get(i).getProperties(), CMIS_OBJECT_ID);
            if (documentId2.equals(cmisPropertyId.getValue().get(0)))
            {
                objectType1 = response.getObjects().getObjects().get(i);
            }
            else if (documentId3.equals(cmisPropertyId.getValue().get(0)))
            {
                objectType2 = response.getObjects().getObjects().get(i);
            }
        }
        assertNotNull(objectType1);
        assertNotNull(objectType2);

        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(objectType1.getProperties(), CMIS_OBJECT_TYPE_ID);
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1
                && CMISCUSTOM_TYPE_DOCUMENT.equals(objectTypeId.getValue().get(0)));
        CmisPropertyString propertyString = (CmisPropertyString) getCmisProperty(objectType1.getProperties(), CMIS_NAME);
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && doc2name.equals(propertyString.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(objectType1.getProperties(), CMISCUSTOM_DOCPROP_STRING);
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && "custom string".equals(propertyString.getValue().get(0)));

        CmisPropertyBoolean propertyBoolean = (CmisPropertyBoolean) getCmisProperty(objectType1.getProperties(), CMISCUSTOM_DOCPROP_BOOLEAN_MULTI);
        assertTrue(propertyBoolean != null && propertyBoolean.getValue() != null && propertyBoolean.getValue().size() == 2 && propertyBoolean.getValue().get(0)
                && !propertyBoolean.getValue().get(1));

        objectTypeId = (CmisPropertyId) getCmisProperty(objectType2.getProperties(), CMIS_OBJECT_TYPE_ID);
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1
                && CMISCUSTOM_TYPE_DOCUMENT.equals(objectTypeId.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(objectType2.getProperties(), CMIS_NAME);
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && "banana1.txt".equals(propertyString.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(objectType2.getProperties(), CMISCUSTOM_DOCPROP_STRING);
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && "custom string".equals(propertyString.getValue().get(0)));

        propertyBoolean = (CmisPropertyBoolean) getCmisProperty(objectType2.getProperties(), CMISCUSTOM_DOCPROP_BOOLEAN_MULTI);
        assertTrue(propertyBoolean != null && propertyBoolean.getValue() != null && propertyBoolean.getValue().size() == 2 && propertyBoolean.getValue().get(0)
                && !propertyBoolean.getValue().get(1));
    }

    private CmisProperty getCmisProperty(CmisPropertiesType cmisPropertiesType, String propertyName)
    {
        for (CmisProperty property : cmisPropertiesType.getProperty())
        {
            if (propertiesUtil.getPropertyName(property).equals(propertyName))
            {
                return property;
            }
        }
        return null;
    }

    private boolean isObjectInFolder(String repositoryId, String objectId, String folderId) throws CmisException
    {
        // TODO: orderBy
        // TODO: renditionsFilter
        CmisObjectInFolderListType response = navigationServicePort.getChildren(repositoryId, folderId, FILTER_ANY, null, false, EnumIncludeRelationships.NONE, "", false,
                BigInteger.ZERO, BigInteger.ZERO, null);
        assertNotNull(response);
        assertNotNull(response.getObjects());
        for (CmisObjectInFolderType cmisObjectType : response.getObjects())
        {
            assertNotNull(cmisObjectType);
            assertNotNull(cmisObjectType.getObject());
            assertNotNull(cmisObjectType.getObject().getProperties());
            CmisPropertyId propertyId = (CmisPropertyId) getCmisProperty(cmisObjectType.getObject().getProperties(), CMIS_OBJECT_ID);
            if (propertyId != null && propertyId.getValue() != null && propertyId.getValue().size() > 0 && objectId.equals(propertyId.getValue().get(0)))
            {
                return true;
            }
        }
        return false;
    }

    private String createTestDocument(String repositoryId, String name, String folderId, boolean custom) throws CmisException
    {
        CmisPropertiesType properties = new CmisPropertiesType();
        CmisPropertyString cmisPropertyString = new CmisPropertyString();
        cmisPropertyString.setPropertyDefinitionId(CMIS_NAME);
        cmisPropertyString.getValue().add(name);
        properties.getProperty().add(cmisPropertyString);
        if (custom)
        {
            cmisPropertyString = new CmisPropertyString();
            cmisPropertyString.setPropertyDefinitionId(CMISCUSTOM_DOCPROP_STRING);
            cmisPropertyString.getValue().add("custom string");
            properties.getProperty().add(cmisPropertyString);
            CmisPropertyBoolean cmisPropertyBoolean = new CmisPropertyBoolean();
            cmisPropertyBoolean.setPropertyDefinitionId(CMISCUSTOM_DOCPROP_BOOLEAN_MULTI);
            cmisPropertyBoolean.getValue().add(true);
            cmisPropertyBoolean.getValue().add(false);
            properties.getProperty().add(cmisPropertyBoolean);
        }
        CmisPropertyId idProperty = new CmisPropertyId();
        idProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        idProperty.getValue().add(custom ? CMISCUSTOM_TYPE_DOCUMENT : CMIS_TYPE_BASE_DOCUMENT);
        properties.getProperty().add(idProperty);

        CmisContentStreamType cmisStream = new CmisContentStreamType();
        cmisStream.setFilename(name);
        cmisStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        cmisStream.setStream(new DataHandler(name, MimetypeMap.MIMETYPE_TEXT_PLAIN));
        Holder<CmisExtensionType> extentionHolder = new Holder<CmisExtensionType>();
        Holder<String> result = new Holder<String>();
        objectServicePort.createDocument(repositoryId, properties, folderId, cmisStream, null, null, null, null, extentionHolder, result);
        return result.value;
    }

    private String createTestFolder(String repositoryId, String name, String folderId, boolean custom) throws CmisException
    {
        CmisPropertiesType properties = new CmisPropertiesType();
        CmisPropertyString cmisPropertyString = new CmisPropertyString();
        cmisPropertyString.setPropertyDefinitionId(CMIS_NAME);
        cmisPropertyString.getValue().add(name);
        properties.getProperty().add(cmisPropertyString);
        if (custom)
        {
            cmisPropertyString = new CmisPropertyString();
            cmisPropertyString.setPropertyDefinitionId(CMISCUSTOM_FOLDERPROP_STRING);
            cmisPropertyString.getValue().add("custom string");
            properties.getProperty().add(cmisPropertyString);
        }
        CmisPropertyId idProperty = new CmisPropertyId();
        idProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        idProperty.getValue().add(custom ? CMISCUSTOM_TYPE_FOLDER : CMIS_TYPE_BASE_FOLDER);
        properties.getProperty().add(idProperty);
        Holder<CmisExtensionType> extensions = new Holder<CmisExtensionType>();
        Holder<String> result = new Holder<String>();
        objectServicePort.createFolder(repositoryId, properties, folderId, null, null, null, extensions, result);
        return result.value;
    }
}
