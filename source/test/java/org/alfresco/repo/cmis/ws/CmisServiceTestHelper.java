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

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import junit.framework.TestCase;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISTypeId;
import org.alfresco.repo.cmis.ws.utils.PropertyUtil;
import org.alfresco.repo.content.MimetypeMap;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;

public class CmisServiceTestHelper extends TestCase
{
    public final static String ALFRESCO_URL = "http://localhost:8080/alfresco";

    public static final String USERNAME_ADMIN = "admin";
    public static final String PASSWORD_ADMIN = "admin";

    public static final String USERNAME_GUEST = "guest";
    public static final String PASSWORD_GUEST = "guest";

    public static final String USERNAME_USER1 = "user1";
    public static final String PASSWORD_USER1 = "user1";

    public String companyHomeId;
    public String repositoryId;

    protected ObjectFactory cmisObjectFactory = new ObjectFactory();

    protected String documentId;
    protected String folderId;
    protected String documentName;
    protected String folderName;

    protected RepositoryServicePort repositoryServicePort = null;
    protected VersioningServicePort versioningServicePort = null;
    protected ObjectServicePort objectServicePort = null;
    protected MultiFilingServicePort multiFilingServicePort = null;
    protected NavigationServicePort navigationServicePort = null;

    private static CmisServiceTestHelper helperInstance;
    private static String previousUserName = null;

    private CmisServiceTestHelper()
    {
        repositoryServicePort = getRepositoryServicePort();
        authenticateServicePort(repositoryServicePort, USERNAME_ADMIN, PASSWORD_ADMIN);

        objectServicePort = getObjectServicePort();
        authenticateServicePort(objectServicePort, USERNAME_ADMIN, PASSWORD_ADMIN);

        versioningServicePort = getVersioningServicePort();
        authenticateServicePort(versioningServicePort, USERNAME_ADMIN, PASSWORD_ADMIN);

        multiFilingServicePort = getMultiFilingServicePort();
        authenticateServicePort(multiFilingServicePort, USERNAME_ADMIN, PASSWORD_ADMIN);

        navigationServicePort = getNavigationServicePort();
        authenticateServicePort(navigationServicePort, USERNAME_ADMIN, PASSWORD_ADMIN);

        repositoryId = getRepositoryId();
        companyHomeId = getCompanyHomeId(repositoryId);
    }

    private CmisServiceTestHelper(String username, String password)
    {
        repositoryServicePort = getRepositoryServicePort();
        authenticateServicePort(repositoryServicePort, username, password);

        objectServicePort = getObjectServicePort();
        authenticateServicePort(objectServicePort, username, password);

        versioningServicePort = getVersioningServicePort();
        authenticateServicePort(versioningServicePort, username, password);

        multiFilingServicePort = getMultiFilingServicePort();
        authenticateServicePort(multiFilingServicePort, username, password);

        navigationServicePort = getNavigationServicePort();
        authenticateServicePort(navigationServicePort, username, password);

        repositoryId = getRepositoryId();
        companyHomeId = getCompanyHomeId(repositoryId);
        // Users could not create any content in Company Home, they should create test content in Users Hone folder
        companyHomeId = getUserHomeId(repositoryId, companyHomeId);
    }

    public static CmisServiceTestHelper getInstance()
    {
        if (previousUserName != null && !previousUserName.equals(USERNAME_ADMIN))
        {
            helperInstance = null;
        }

        if (helperInstance == null)
        {
            helperInstance = new CmisServiceTestHelper();
            previousUserName = USERNAME_ADMIN;
        }
        return helperInstance;
    }

    public static CmisServiceTestHelper getInstance(String userName, String password)
    {
        if (previousUserName != null && !previousUserName.equals(userName))
        {
            helperInstance = null;
        }

        if (helperInstance == null)
        {
            helperInstance = new CmisServiceTestHelper(userName, password);
            previousUserName = userName;
        }
        return helperInstance;
    }

    public void authenticateServicePort(Object servicePort, String username, String passwordValue)
    {
        final String password = passwordValue;

        Map<String, Object> wss4jOutInterceptorProp = new HashMap<String, Object>();
        wss4jOutInterceptorProp.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN + " " + WSHandlerConstants.TIMESTAMP);

        wss4jOutInterceptorProp.put(WSHandlerConstants.USER, username);
        wss4jOutInterceptorProp.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);

        wss4jOutInterceptorProp.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler()
        {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
            {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
                pc.setPassword(password);
            }
        });

        WSS4JOutInterceptor wss4jOutInterceptor = new WSS4JOutInterceptor(wss4jOutInterceptorProp);

        Client client = ClientProxy.getClient(servicePort);
        client.getEndpoint().getOutInterceptors().add(new SAAJOutInterceptor());
        client.getEndpoint().getOutInterceptors().add(wss4jOutInterceptor);
    }

    public String createDocument(String name, String parentFolderId) throws Exception
    {
        String content = "This is a test content";
        // Cmis Properties
        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();
        CmisPropertyString cmisProperty = new CmisPropertyString();
        cmisProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_NAME);
        cmisProperty.getValue().add(name);
        CmisPropertyId idProperty = new CmisPropertyId();
        idProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        idProperty.getValue().add(CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());

        propertiesList.add(cmisProperty);
        propertiesList.add(idProperty);

        CmisContentStreamType cmisStream = new CmisContentStreamType();
        cmisStream.setFilename(name);
        cmisStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        DataHandler dataHandler = new DataHandler(content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        cmisStream.setStream(dataHandler);

        Holder<CmisExtensionType> extensions = new Holder<CmisExtensionType>();
        Holder<String> idHolder = new Holder<String>();
        // public String createDocument(String repositoryId, String typeId, CmisPropertiesType properties, String folderId, CmisContentStreamType contentStream,
        // EnumVersioningState versioningState)
        objectServicePort.createDocument(repositoryId, properties, parentFolderId, cmisStream, EnumVersioningState.MAJOR, null, null, null, extensions, idHolder);
        // assertNotNull(objectId);
        return idHolder.value;
    }

    public String createDocument(String name, String parentFolderId, CMISTypeId typeId, EnumVersioningState enumVersioningState) throws Exception
    {
        String content = "This is a test content";
        // Cmis Properties
        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();
        CmisPropertyString cmisProperty = new CmisPropertyString();
        cmisProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_NAME);
        cmisProperty.getValue().add(name);
        CmisPropertyId idProperty = new CmisPropertyId();
        idProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        idProperty.getValue().add(typeId.getId());

        propertiesList.add(cmisProperty);
        propertiesList.add(idProperty);

        CmisContentStreamType cmisStream = new CmisContentStreamType();
        cmisStream.setFilename(name);
        cmisStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        DataHandler dataHandler = new DataHandler(content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        cmisStream.setStream(dataHandler);

        Holder<CmisExtensionType> extensions = new Holder<CmisExtensionType>();
        Holder<String> idHolder = new Holder<String>();
        // public String createDocument(String repositoryId, String typeId, CmisPropertiesType properties, String folderId, CmisContentStreamType contentStream,
        // EnumVersioningState versioningState)
        objectServicePort.createDocument(repositoryId, properties, parentFolderId, cmisStream, enumVersioningState, null, null, null, extensions, idHolder);
        // assertNotNull(objectId);
        return idHolder.value;
    }

    public String createDocumentImage(String name, String parentFolderId) throws Exception
    {
        DataSource dataSource = new URLDataSource(getClass().getResource("test.jpg"));
        DataHandler dataHandler = new DataHandler(dataSource);

        CmisContentStreamType cmisStream = new CmisContentStreamType();
        cmisStream.setFilename(name + dataSource.getName());
        cmisStream.setMimeType(MimetypeMap.MIMETYPE_BINARY);
        cmisStream.setStream(dataHandler);

        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();
        CmisPropertyString cmisProperty = new CmisPropertyString();
        cmisProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_NAME);
        cmisProperty.getValue().add(name + dataSource.getName());
        CmisPropertyId idProperty = new CmisPropertyId();
        idProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        idProperty.getValue().add(CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());

        propertiesList.add(cmisProperty);
        propertiesList.add(idProperty);

        Holder<CmisExtensionType> extensions = new Holder<CmisExtensionType>();
        Holder<String> idHolder = new Holder<String>();
        // public String createDocument(String repositoryId, String typeId, CmisPropertiesType properties, String folderId, CmisContentStreamType contentStream,
        // EnumVersioningState versioningState)
        objectServicePort.createDocument(repositoryId, properties, parentFolderId, cmisStream, EnumVersioningState.MAJOR, null, null, null, extensions, idHolder);
        // assertNotNull(objectId);
        return idHolder.value;

    }

    public String createFolder(String name, String parentFolderId) throws Exception
    {
        // CreateFolder request = cmisObjectFactory.createCreateFolder();
        // request.setRepositoryId(repositoryId);
        // request.setFolderId(CompanyHomeId);
        // request.setTypeId(CMISMapping.FOLDER_TYPE_ID.getTypeId());
        // request.setProperties(properties);

        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();

        CmisPropertyString cmisProperty = new CmisPropertyString();
        cmisProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_NAME);
        cmisProperty.getValue().add(name);
        CmisPropertyId idProperty = new CmisPropertyId();
        idProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        idProperty.getValue().add(CMISDictionaryModel.FOLDER_TYPE_ID.getId());

        propertiesList.add(cmisProperty);
        propertiesList.add(idProperty);

        Holder<CmisExtensionType> extensions = new Holder<CmisExtensionType>();
        Holder<String> idHolder = new Holder<String>();
        // public String createFolder(String repositoryId, String typeId, CmisPropertiesType properties, String folderId)
        objectServicePort.createFolder(repositoryId, properties, parentFolderId, null, null, null, extensions, idHolder);
        // assertNotNull(objectId);
        return idHolder.value;
    }

    public String createFolder(String name, String parentFolderId, CMISTypeId cmisTypeId) throws Exception
    {

        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();

        CmisPropertyString cmisProperty = new CmisPropertyString();
        cmisProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_NAME);
        cmisProperty.getValue().add(name);
        CmisPropertyId idProperty = new CmisPropertyId();
        idProperty.setPropertyDefinitionId(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        idProperty.getValue().add(cmisTypeId.getId());

        propertiesList.add(cmisProperty);
        propertiesList.add(idProperty);

        Holder<CmisExtensionType> extensions = new Holder<CmisExtensionType>();
        Holder<String> idHolder = new Holder<String>();
        // public String createFolder(String repositoryId, String typeId, CmisPropertiesType properties, String folderId)
        objectServicePort.createFolder(repositoryId, properties, parentFolderId, null, null, null, extensions, idHolder);
        // assertNotNull(objectId);
        return idHolder.value;
    }

    public void deleteDocument(String documentId) throws Exception
    {
        objectServicePort.deleteObject(repositoryId, documentId, true, new Holder<CmisExtensionType>());
        try
        {
            getObjectProperties(documentId);
            fail("Document has not been deleted");
        }
        catch (Exception e)
        {
            // Doing nothing
        }
    }

    public void deleteFolder(String folderId) throws Exception
    {
        objectServicePort.deleteTree(repositoryId, folderId, true, EnumUnfileObject.DELETE, true, null);
    }

    public CmisObjectType getObjectProperties(String objectId) throws CmisException
    {
        return getObjectProperties(objectId, "*");
    }

    public CmisObjectType getObjectProperties(String objectId, String filter) throws CmisException
    {
        CmisPropertiesType response = null;
        CmisObjectType result = null;
        response = objectServicePort.getProperties(repositoryId, objectId, filter, null);
        result = new CmisObjectType();
        result.setProperties(response);
        return result;
    }

    /**
     * This method simplify receiving of Object Identifier for Company Home Root Folder
     * 
     * @param servicesPort - <b>RepositoryServicePort</b> instance that configured with WSS4J Client
     * @return <b>String</b> representation of <b>Object Identifier</b>
     * @throws Exception This exception throws when any <b>CMIS Services</b> operations was failed
     */
    public String getCompanyHomeId(String repositoryId)
    {
        String rootFolder = null;
        try
        {
            rootFolder = repositoryServicePort.getRepositoryInfo(repositoryId, null).getRootFolderId();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return rootFolder;
    }

    public String getUserHomeId(String repositoryId, String companyHomeId)
    {
        String userHomeFolder = null;
        PropertyUtil propertiesUtil = new PropertyUtil();
        try
        {
            List<CmisObjectInFolderType> response = getChildren(companyHomeId, 0, "*");
            for (CmisObjectInFolderType object : response)
            {
                assertNotNull(object);
                assertNotNull(object.getObject());
                assertNotNull(object.getObject().getProperties());
                if (propertiesUtil.getCmisPropertyValue(object.getObject().getProperties(), CMISDictionaryModel.PROP_NAME, null).equals("User Homes"))
                {
                    return (String) propertiesUtil.getCmisPropertyValue(object.getObject().getProperties(), CMISDictionaryModel.PROP_OBJECT_ID, null);
                }
            }

            userHomeFolder = repositoryServicePort.getRepositoryInfo(repositoryId, null).getRootFolderId();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return userHomeFolder;
    }

    public final static String REPOSITORY_SERVICE_WSDL_LOCATION = ALFRESCO_URL + "/cmis/RepositoryService?wsdl";
    public final static QName REPOSITORY_SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "RepositoryService");

    protected RepositoryServicePort getRepositoryServicePort()
    {
        URL serviceWsdlURL;
        try
        {
            serviceWsdlURL = new URL(REPOSITORY_SERVICE_WSDL_LOCATION);
        }
        catch (MalformedURLException e)
        {
            throw new java.lang.RuntimeException("Cannot get service Wsdl URL", e);
        }

        Service service = Service.create(serviceWsdlURL, REPOSITORY_SERVICE_NAME);

        return (RepositoryServicePort) service.getPort(RepositoryServicePort.class);

    }

    public final static String OBJECT_SERVICE_WSDL_LOCATION = ALFRESCO_URL + "/cmis/ObjectService?wsdl";
    public final static QName OBJECT_SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "ObjectService");

    protected ObjectServicePort getObjectServicePort()
    {
        URL serviceWsdlURL;
        try
        {
            serviceWsdlURL = new URL(OBJECT_SERVICE_WSDL_LOCATION);
        }
        catch (MalformedURLException e)
        {
            throw new java.lang.RuntimeException("Cannot get service Wsdl URL", e);
        }

        Service service = Service.create(serviceWsdlURL, OBJECT_SERVICE_NAME);

        return (ObjectServicePort) service.getPort(ObjectServicePort.class);

    }

    public final static String VERSIONING_SERVICE_WSDL_LOCATION = ALFRESCO_URL + "/cmis/VersioningService?wsdl";
    public final static QName VERSIONING_SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "VersioningService");

    protected VersioningServicePort getVersioningServicePort()
    {
        URL serviceWsdlURL;
        try
        {
            serviceWsdlURL = new URL(VERSIONING_SERVICE_WSDL_LOCATION);
        }
        catch (MalformedURLException e)
        {
            throw new java.lang.RuntimeException("Cannot get service Wsdl URL", e);
        }

        Service service = Service.create(serviceWsdlURL, VERSIONING_SERVICE_NAME);
        return service.getPort(VersioningServicePort.class);
    }

    public final static String MULTIFILING_SERVICE_WSDL_LOCATION = ALFRESCO_URL + "/cmis/MultiFilingService?wsdl";
    public final static QName MULTIFILING_SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "MultiFilingService");

    protected MultiFilingServicePort getMultiFilingServicePort()
    {
        URL serviceWsdlURL;
        try
        {
            serviceWsdlURL = new URL(MULTIFILING_SERVICE_WSDL_LOCATION);
        }
        catch (MalformedURLException e)
        {
            throw new java.lang.RuntimeException("Cannot get service Wsdl URL", e);
        }

        Service service = Service.create(serviceWsdlURL, MULTIFILING_SERVICE_NAME);
        return service.getPort(MultiFilingServicePort.class);
    }

    public final static String NAVIGATION_SERVICE_WSDL_LOCATION = ALFRESCO_URL + "/cmis/NavigationService?wsdl";
    public final static QName NAVIGATION_SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "NavigationService");

    protected NavigationServicePort getNavigationServicePort()
    {
        URL serviceWsdlURL;
        try
        {
            serviceWsdlURL = new URL(NAVIGATION_SERVICE_WSDL_LOCATION);
        }
        catch (MalformedURLException e)
        {
            throw new java.lang.RuntimeException("Cannot get service Wsdl URL", e);
        }

        Service service = Service.create(serviceWsdlURL, NAVIGATION_SERVICE_NAME);

        NavigationServicePort servicePort = service.getPort(NavigationServicePort.class);

        return servicePort;
    }

    public String getRepositoryId()
    {
        try
        {
            return repositoryServicePort.getRepositories(null).get(0).getRepositoryId();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public void checkIn(Holder<String> documentId, String checkinComment, Boolean isMajor)
    {
        try
        {
            CmisPropertiesType properties = new CmisPropertiesType();
            CmisContentStreamType contentStream = new CmisContentStreamType();
            contentStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            DataHandler dataHandler = new DataHandler("Test content string :" + System.currentTimeMillis(), MimetypeMap.MIMETYPE_TEXT_PLAIN);
            contentStream.setStream(dataHandler);
            versioningServicePort.checkIn(repositoryId, documentId, isMajor, properties, contentStream, checkinComment, null, null, null, new Holder<CmisExtensionType>());
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            fail();
        }
    }

    public void checkOut(Holder<String> documentId, Holder<Boolean> contentCopied)
    {
        try
        {
            versioningServicePort.checkOut(repositoryId, documentId, new Holder<CmisExtensionType>(), contentCopied);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            fail();
        }
    }

    public List<CmisObjectType> getAllVersions(String documentId)
    {
        List<CmisObjectType> response = null;
        try
        {
            response = versioningServicePort.getAllVersions(repositoryId, documentId, "*", false, null);
            assertNotNull(response);
            assertFalse(response.isEmpty());
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            fail();
        }
        return response;
    }

    public String createRelationship(String name, String folderId, String documentId) throws Exception
    {
        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();
        propertiesList.add(createCmisPropertyString(CMISDictionaryModel.PROP_NAME, name));
        propertiesList.add(createCmisPropertyId(CMISDictionaryModel.PROP_SOURCE_ID, folderId));
        propertiesList.add(createCmisPropertyId(CMISDictionaryModel.PROP_TARGET_ID, documentId));
        propertiesList.add(createCmisPropertyId(CMISDictionaryModel.PROP_OBJECT_TYPE_ID, CMISDictionaryModel.RELATIONSHIP_TYPE_ID.getId()));
        // TODO: it need reimplementation according to valid Relationship type searching
        // createRelationship(String repositoryId, String typeId, CmisPropertiesType properties, String sourceObjectId, String targetObjectId)
        Holder<String> idHolder = new Holder<String>();
        objectServicePort.createRelationship(repositoryId, properties, null, null, null, null, idHolder);
        assertNotNull(idHolder.value);
        return idHolder.value;
    }

    private CmisProperty createCmisPropertyId(String propertyName, String propertyValue)
    {
        CmisPropertyId result = new CmisPropertyId();
        result.setPropertyDefinitionId(propertyName);
        result.setLocalName(propertyName);
        result.setDisplayName(propertyName);
        result.getValue().add(propertyValue);
        return result;
    }

    private CmisProperty createCmisPropertyString(String propertyName, String propertyValue)
    {
        CmisPropertyString result = new CmisPropertyString();
        result.setPropertyDefinitionId(propertyName);
        result.setLocalName(propertyName);
        result.setDisplayName(propertyName);
        result.getValue().add(propertyValue);
        return result;
    }

    public String updateProperty(String documentId, String propName, String propValue) throws Exception
    {

        // Cmis Properties
        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();
        CmisPropertyString cmisProperty = new CmisPropertyString();
        cmisProperty.setPropertyDefinitionId(propName);
        cmisProperty.getValue().add(propValue);
        propertiesList.add(cmisProperty);

        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<String> changeToken = new Holder<String>("");
        Holder<CmisExtensionType> extension = new Holder<CmisExtensionType>();
        // public void updateProperties(String repositoryId, Holder<String> objectId, String changeToken, CmisPropertiesType properties)
        objectServicePort.updateProperties(repositoryId, documentIdHolder, changeToken, properties, extension);
        assertEquals(documentId, documentIdHolder.value);

        return documentIdHolder.value;
    }

    public void addObjectToFolder(String documentId, String anotherFolderId)
    {
        try
        {
            multiFilingServicePort.addObjectToFolder(repositoryId, documentId, anotherFolderId, false, new Holder<CmisExtensionType>());
        }
        catch (Throwable e)
        {
            fail(e.getMessage());
        }
    }

    public void removeObjectFromFolder(String documentId, String folderId)
    {
        try
        {
            multiFilingServicePort.removeObjectFromFolder(repositoryId, documentId, folderId, new Holder<CmisExtensionType>());
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public List<CmisObjectParentsType> getObjectParents(String objectId, String filter) throws Exception
    {
        List<CmisObjectParentsType> response = null;
        try
        {
            response = navigationServicePort.getObjectParents(repositoryId, objectId, filter, false, null, null, false, null);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        return response;
    }

    public List<CmisObjectParentsType> getObjectParents(String objectId) throws Exception
    {
        return navigationServicePort.getObjectParents(repositoryId, objectId, "", false, null, null, false, null);
    }

    public void setTextContentStream(String documentId, String newContent) throws Exception
    {
        String newFileName = "New file name (" + System.currentTimeMillis() + ")";

        CmisContentStreamType contentStream = new CmisContentStreamType();
        contentStream.setFilename(newFileName);
        contentStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        DataHandler dataHandler = new DataHandler(newContent, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentStream.setStream(dataHandler);

        Holder<String> documentIdHolder = new Holder<String>(documentId);
        // public void setContentStream(String repositoryId, Holder<String> documentId, Boolean overwriteFlag, CmisContentStreamType contentStream)
        // TODO: changeToken
        objectServicePort.setContentStream(repositoryId, documentIdHolder, true, null, contentStream, null);
    }

    public CmisContentStreamType getContentStream(String documentId, long offset, long length) throws Exception
    {
        CmisContentStreamType result = null;
        result = objectServicePort.getContentStream(repositoryId, documentId, "", BigInteger.valueOf(offset), BigInteger.valueOf(length), null);
        return result;
    }

    public List<CmisObjectInFolderType> getChildren(String folderId, long maxItems, String filter) throws Exception
    {
        CmisObjectInFolderListType response = null;
        try
        {
            // TODO: orderBy
            // TODO: renditionFilter
            response = navigationServicePort.getChildren(repositoryId, folderId, filter, "", false, EnumIncludeRelationships.NONE, "", false, BigInteger.valueOf(maxItems),
                    BigInteger.ZERO, null);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }

        return response.getObjects();
    }
}
