/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

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
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static ApplicationContext applicationContext;
    private static AuthenticationService authenticationService;

    private static String folderId;
    private static String repositoryId;
    private static RepositoryServicePort repositoryServicePort;
    private static ObjectServicePort objectServicePort;
    private static DiscoveryServicePort discoveryServicePort;
    private static NavigationServicePort navigationServicePort;
    private static PropertyUtil propertiesUtil;

    @Override
    protected void setUp() throws Exception
    {
        if (null == applicationContext)
        {
            applicationContext = new ClassPathXmlApplicationContext(new String[] {"classpath:alfresco/application-context.xml", "classpath:alfresco/cmis-ws-context.xml", "classpath:cmis/cmis-test-context.xml"});
            ServiceRegistry serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
            authenticationService = serviceRegistry.getAuthenticationService();
        }

        authenticationService.authenticate(USERNAME, PASSWORD.toCharArray());

        if(null == propertiesUtil) {
            propertiesUtil = (PropertyUtil) applicationContext.getBean("propertiesUtils");
        }
        if (repositoryServicePort == null)
        {
            repositoryServicePort = (RepositoryServicePort) applicationContext.getBean("dmRepositoryService");
        }
        if (objectServicePort == null)
        {
            objectServicePort = (ObjectServicePort) applicationContext.getBean("dmObjectService");
        }
        if (navigationServicePort == null)
        {
            navigationServicePort = (NavigationServicePort) applicationContext.getBean("dmNavigationService");
        }
        if (discoveryServicePort == null)
        {
            discoveryServicePort = (DiscoveryServicePort) applicationContext.getBean("dmDiscoveryService");
        }
        repositoryId = repositoryId == null ? repositoryServicePort.getRepositories().get(0).getId() : repositoryId;
        if (folderId == null)
        {
            String rootFolderId = repositoryServicePort.getRepositoryInfo(repositoryId).getRootFolderId();
            Holder<List<CmisObjectType>> response = new Holder<List<CmisObjectType>>(new LinkedList<CmisObjectType>());
            Holder<Boolean> hasMoreItems = new Holder<Boolean>();
            navigationServicePort.getChildren(repositoryId, folderId, "*", false, EnumIncludeRelationships.NONE, false, false, BigInteger.ZERO, BigInteger.ZERO, "", response, hasMoreItems);
            assertNotNull(response);
            assertNotNull(response.value);
            for (CmisObjectType cmisObjectType : response.value)
            {
                CmisPropertyString propertyString = (CmisPropertyString) getCmisProperty(cmisObjectType.getProperties(), "Name");
                if (propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() > 0 && "CMIS Tests".equals(propertyString.getValue().get(0)))
                {
                    folderId = ((CmisPropertyId) getCmisProperty(cmisObjectType.getProperties(), "ObjectId")).getValue().get(0);
                    break;
                }
            }
            if (folderId == null)
            {
                folderId = createTestFolder(repositoryId, "CMIS Tests", rootFolderId, false);
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
        String folderId1 = createTestFolder(repositoryId, "testCreateCustomFolder" + System.currentTimeMillis(), folderId, true);
        assertNotNull(folderId1);

        CmisObjectType propertiesObject = objectServicePort.getProperties(repositoryId, folderId1, "*", false, EnumIncludeRelationships.NONE, false);
        assertNotNull(propertiesObject);
        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(propertiesObject.getProperties(), "ObjectTypeId");
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1 && "F/cmiscustom_folder".equals(objectTypeId.getValue().get(0)));
        CmisPropertyString customProp = (CmisPropertyString) getCmisProperty(propertiesObject.getProperties(), "cmiscustom_folderprop_string");
        assertTrue(customProp != null && customProp.getValue() != null && customProp.getValue().size() == 1 && "custom string".equals(customProp.getValue().get(0)));
    }

    public void testCreateDocument() throws Exception
    {
        String documentId = createTestDocument(repositoryId, "testCreateCustomDocument" + System.currentTimeMillis(), folderId, true);
        assertNotNull(documentId);

        CmisObjectType propertiesObject = objectServicePort.getProperties(repositoryId, documentId, "*", false, EnumIncludeRelationships.NONE, false);
        assertNotNull(propertiesObject);

        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(propertiesObject.getProperties(), "ObjectTypeId");
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1 && "D/cmiscustom_document".equals(objectTypeId.getValue().get(0)));
        CmisPropertyString customProp = (CmisPropertyString) getCmisProperty(propertiesObject.getProperties(), "cmiscustom_docprop_string");
        assertTrue(customProp != null && customProp.getValue() != null && customProp.getValue().size() == 1 && "custom string".equals(customProp.getValue().get(0)));

        CmisPropertyBoolean propertyMulti = (CmisPropertyBoolean) getCmisProperty(propertiesObject.getProperties(), "cmiscustom_docprop_boolean_multi");
        assertTrue(propertyMulti != null && propertyMulti.getValue() != null && propertyMulti.getValue().size() == 2 && propertyMulti.getValue().get(0) && !propertyMulti.getValue().get(1));
    }

    public void testUpdate() throws Exception
    {
        String documentId = createTestDocument(repositoryId, "testUpdateCustomDocument" + System.currentTimeMillis(), folderId, true);
        assertNotNull(documentId);

        Holder<String> holder = new Holder<String>(documentId);
        CmisPropertiesType properties = new CmisPropertiesType();
        String newName = "Updated Title " + System.currentTimeMillis();
        String customProp = "custom " + System.currentTimeMillis();
        CmisPropertyString cmisPropertyString = new CmisPropertyString();
        cmisPropertyString.setPdid("Name");
        cmisPropertyString.getValue().add(newName);
        properties.getProperty().add(cmisPropertyString);
        cmisPropertyString = new CmisPropertyString();
        cmisPropertyString.setPdid("cmiscustom_docprop_string");
        cmisPropertyString.getValue().add(customProp);
        properties.getProperty().add(cmisPropertyString);
        CmisPropertyBoolean cmisPropertymulti = new CmisPropertyBoolean();
        cmisPropertymulti.setPdid("cmiscustom_docprop_boolean_multi");
        cmisPropertymulti.getValue().add(false);
        cmisPropertymulti.getValue().add(true);
        properties.getProperty().add(cmisPropertymulti);
        objectServicePort.updateProperties(repositoryId, holder, null, properties);

        CmisObjectType propertiesObject = objectServicePort.getProperties(repositoryId, documentId, "*", false, EnumIncludeRelationships.NONE, false);
        assertNotNull(propertiesObject);

        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(propertiesObject.getProperties(), "ObjectTypeId");
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1 && "D/cmiscustom_document".equals(objectTypeId.getValue().get(0)));
        CmisPropertyString propertyString = (CmisPropertyString) getCmisProperty(propertiesObject.getProperties(), "Name");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && newName.equals(propertyString.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(propertiesObject.getProperties(), "cmiscustom_docprop_string");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && customProp.equals(propertyString.getValue().get(0)));
        CmisPropertyBoolean propertyMulti = (CmisPropertyBoolean) getCmisProperty(propertiesObject.getProperties(), "cmiscustom_docprop_boolean_multi");
        assertTrue(propertyMulti != null && propertyMulti.getValue() != null && propertyMulti.getValue().size() == 2 && !propertyMulti.getValue().get(0) && propertyMulti.getValue().get(1));
    }

    public void testDelete() throws Exception
    {
        String documentId = createTestDocument(repositoryId, "testDeleteCustomDocument" + System.currentTimeMillis(), folderId, true);
        assertNotNull(documentId);

        assertTrue(isObjectInFolder(repositoryId, documentId, folderId));

        objectServicePort.deleteObject(repositoryId, documentId, true);

        assertFalse(isObjectInFolder(repositoryId, documentId, folderId));
    }

    public void testQuery() throws Exception
    {
        String folderId1 = createTestFolder(repositoryId, "testQueryCustom" + System.currentTimeMillis(), folderId, false);
        String documentId1 = createTestDocument(repositoryId, "apple1", folderId1, false);
        assertNotNull(documentId1);
        String doc2name = "name" + System.currentTimeMillis();
        String documentId2 = createTestDocument(repositoryId, doc2name, folderId1, true);
        assertNotNull(documentId2);
        String documentId3 = createTestDocument(repositoryId, "banana1", folderId1, true);
        assertNotNull(documentId3);

        String query = "SELECT ObjectId, Name, ObjectTypeId, cmiscustom_docprop_string, cmiscustom_docprop_boolean_multi FROM cmiscustom_document " +
        "WHERE IN_FOLDER('" + folderId1 + "') " +
        "AND cmiscustom_docprop_string = 'custom string' ";
        Query queryType = new Query();
        queryType.setRepositoryId(repositoryId);
        queryType.setStatement(query);
        queryType.setSkipCount(BigInteger.valueOf(0));
        queryType.setMaxItems(BigInteger.valueOf(5));

        QueryResponse response = discoveryServicePort.query(queryType);
        assertNotNull(response);
        assertEquals(2, response.getObject().size());

        CmisObjectType objectType1 = null;
        CmisObjectType objectType2 = null;
        for (int i = 0; i < 2; i++)
        {
            CmisPropertyId cmisPropertyId = (CmisPropertyId) getCmisProperty(response.getObject().get(i).getProperties(), "ObjectId");
            if (documentId2.equals(cmisPropertyId.getValue().get(0)))
            {
                objectType1 = response.getObject().get(i);
            }
            else if (documentId3.equals(cmisPropertyId.getValue().get(0)))
            {
                objectType2 = response.getObject().get(i);
            }
        }
        assertNotNull(objectType1);
        assertNotNull(objectType2);

        CmisPropertyId objectTypeId = (CmisPropertyId) getCmisProperty(objectType1.getProperties(), "ObjectTypeId");
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1 && "D/cmiscustom_document".equals(objectTypeId.getValue().get(0)));
        CmisPropertyString propertyString = (CmisPropertyString) getCmisProperty(objectType1.getProperties(), "Name");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && doc2name.equals(propertyString.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(objectType1.getProperties(), "cmiscustom_docprop_string");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && "custom string".equals(propertyString.getValue().get(0)));

        CmisPropertyBoolean propertyBoolean = (CmisPropertyBoolean) getCmisProperty(objectType1.getProperties(), "cmiscustom_docprop_boolean_multi");
        assertTrue(propertyBoolean != null && propertyBoolean.getValue() != null && propertyBoolean.getValue().size() == 2 && propertyBoolean.getValue().get(0)
                && !propertyBoolean.getValue().get(1));

        objectTypeId = (CmisPropertyId) getCmisProperty(objectType2.getProperties(), "ObjectTypeId");
        assertTrue(objectTypeId != null && objectTypeId.getValue() != null && objectTypeId.getValue().size() == 1 && "D/cmiscustom_document".equals(objectTypeId.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(objectType2.getProperties(), "Name");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && "banana1".equals(propertyString.getValue().get(0)));
        propertyString = (CmisPropertyString) getCmisProperty(objectType2.getProperties(), "cmiscustom_docprop_string");
        assertTrue(propertyString != null && propertyString.getValue() != null && propertyString.getValue().size() == 1 && "custom string".equals(propertyString.getValue().get(0)));

        propertyBoolean = (CmisPropertyBoolean) getCmisProperty(objectType2.getProperties(), "cmiscustom_docprop_boolean_multi");
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
        Holder<List<CmisObjectType>> response = new Holder<List<CmisObjectType>>(new LinkedList<CmisObjectType>());
        Holder<Boolean> hasMoreItems = new Holder<Boolean>();
        navigationServicePort.getChildren(repositoryId, folderId, "*", false, EnumIncludeRelationships.NONE, false, false, BigInteger.ZERO, BigInteger.ZERO, "", response, hasMoreItems);
        assertNotNull(response);
        assertNotNull(response.value);
        for (CmisObjectType cmisObjectType : response.value)
        {
            CmisPropertyId propertyId = (CmisPropertyId) getCmisProperty(cmisObjectType.getProperties(), "ObjectId");
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
        cmisPropertyString.setPdid("Name");
        cmisPropertyString.getValue().add(name);
        properties.getProperty().add(cmisPropertyString);
        if (custom)
        {
            cmisPropertyString = new CmisPropertyString();
            cmisPropertyString.setPdid("cmiscustom_docprop_string");
            cmisPropertyString.getValue().add("custom string");
            properties.getProperty().add(cmisPropertyString);
            CmisPropertyBoolean cmisPropertyBoolean = new CmisPropertyBoolean();
            cmisPropertyBoolean.setPdid("cmiscustom_docprop_boolean_multi");
            cmisPropertyBoolean.getValue().add(true);
            cmisPropertyBoolean.getValue().add(false);
            properties.getProperty().add(cmisPropertyBoolean);
        }
        CmisPropertyId idProperty = new CmisPropertyId();
        idProperty.setPdid(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        idProperty.getValue().add(custom ? "D/cmiscustom_document" : "document");
        properties.getProperty().add(idProperty);

        CmisContentStreamType cmisStream = new CmisContentStreamType();
        cmisStream.setFilename(name);
        cmisStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        cmisStream.setStream(new DataHandler(name, MimetypeMap.MIMETYPE_TEXT_PLAIN));
        return objectServicePort.createDocument(repositoryId, properties, folderId, cmisStream, null, null, null, null);
    }

    private String createTestFolder(String repositoryId, String name, String folderId, boolean custom) throws CmisException
    {
        CmisPropertiesType properties = new CmisPropertiesType();
        CmisPropertyString cmisPropertyString = new CmisPropertyString();
        cmisPropertyString.setPdid("Name");
        cmisPropertyString.getValue().add(name);
        properties.getProperty().add(cmisPropertyString);
        if (custom)
        {
            cmisPropertyString = new CmisPropertyString();
            cmisPropertyString.setPdid("cmiscustom_folderprop_string");
            cmisPropertyString.getValue().add("custom string");
            properties.getProperty().add(cmisPropertyString);
        }
        CmisPropertyId idProperty = new CmisPropertyId();
        idProperty.setPdid(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        idProperty.getValue().add(custom ? "F/cmiscustom_folder" : "folder");
        properties.getProperty().add(idProperty);

        return objectServicePort.createFolder(repositoryId, properties, folderId, null, null, null);
    }
}
