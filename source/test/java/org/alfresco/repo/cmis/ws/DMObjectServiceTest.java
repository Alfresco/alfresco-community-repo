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

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.io.IOUtils;

/**
 * @author Alexander Tsvetkov
 */

public class DMObjectServiceTest extends AbstractServiceTest
{
    private CmisObjectType resultObject;

    public DMObjectServiceTest()
    {
        super();
    }

    public DMObjectServiceTest(String testCase, String username, String password)
    {
        super(testCase, username, password);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        createInitialContent();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        deleteInitialContent();
    }

    protected Object getServicePort()
    {
        return helper.objectServicePort;
    }

    public void testCreateDocument() throws Exception
    {

        documentName = "Test cmis document (" + System.currentTimeMillis() + ")";

        String content = "This is a test content";
        // Cmis Properties
        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();
        CmisPropertyString cmisProperty = new CmisPropertyString();
        cmisProperty.setLocalName(CMISDictionaryModel.PROP_NAME);
        cmisProperty.getValue().add(documentName);
        propertiesList.add(cmisProperty);

        CmisContentStreamType cmisStream = new CmisContentStreamType();
        cmisStream.setFilename(documentName);
        cmisStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        DataHandler dataHandler = new DataHandler(content, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        cmisStream.setStream(dataHandler);

        String documentId;
        // MAJOR
        documentName = "Test cmis document (" + System.currentTimeMillis() + ")";
        documentId = helper.createDocument(documentName, folderId, CMISDictionaryModel.DOCUMENT_TYPE_ID, EnumVersioningState.MAJOR);
        resultObject = helper.getObjectProperties(documentId);
        assertObjectPropertiesNotNull(resultObject);
        // assertTrue(getPropertyBooleanValue(propertiesObject, CMISMapping.PROP_IS_MAJOR_VERSION));
        assertFalse(getBooleanProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT));
        helper.deleteDocument(documentId);

        // MINOR
        documentName = "Test cmis document (" + System.currentTimeMillis() + ")";
        documentId = helper.createDocument(documentName, folderId, CMISDictionaryModel.DOCUMENT_TYPE_ID, EnumVersioningState.MINOR);
        resultObject = helper.getObjectProperties(documentId);
        assertObjectPropertiesNotNull(resultObject);
        assertFalse(getBooleanProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT));
        helper.deleteDocument(documentId);

    }

    public void testCreateDocumentFromSource() throws Exception
    {
        String folderForCopyId = helper.createFolder("FolderForCopy" + System.currentTimeMillis(), folderId);
        String newName = "CopyName" + System.currentTimeMillis();
        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();
        CmisPropertyString cmisProperty = new CmisPropertyString();
        cmisProperty.setLocalName(CMISDictionaryModel.PROP_NAME);
        cmisProperty.getValue().add(newName);
        propertiesList.add(cmisProperty);
        Holder<String> objectId = new Holder<String>();
        ((ObjectServicePort) servicePort).createDocumentFromSource(repositoryId, documentId, properties, folderForCopyId, EnumVersioningState.NONE, null, null, null,
                new Holder<CmisExtensionType>(), objectId);
        assertNotNull(objectId.value);
        resultObject = helper.getObjectProperties(objectId.value);
        assertTrue(newName.equals(getStringProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_NAME)));
    }

    public void testCreateDocument_Versioning() throws Exception
    {
        // CHECKEDOUT
        documentName = "Test cmis document (" + System.currentTimeMillis() + ")";
        String documentId = helper.createDocument(documentName, folderId, CMISDictionaryModel.DOCUMENT_TYPE_ID, EnumVersioningState.CHECKEDOUT);
        resultObject = helper.getObjectProperties(documentId);

        assertObjectPropertiesNotNull(resultObject);

        assertNotNull(getIdProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_VERSION_SERIES_ID));
        // Bug
        assertTrue(getBooleanProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT));
        assertNotNull(getStringProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY));

        Holder<String> documentIdHolder = new Holder<String>(documentId);
        helper.checkIn(documentIdHolder, "checkin Comment", true);
        assertTrue(getStringProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_VERSION_LABEL).equals("1.0"));

    }

    public void testCreateDocument_Exceptions() throws Exception
    {
        // If unfiling is not supported and a Folder is not specified, throw FolderNotValidException.
        try
        {
            documentName = "Test cmis document (" + System.currentTimeMillis() + ")";
            documentId = helper.createDocument(documentName, null, CMISDictionaryModel.DOCUMENT_TYPE_ID, EnumVersioningState.MAJOR);
            fail();
        }
        catch (CmisException e)
        {

        }
        catch (Exception e)
        { // Bug
            e.printStackTrace(); // org.alfresco.repo.cmis.ws.RuntimeException: Runtime error. Message: null
            fail(e.getMessage());
        }
    }

    public void testCreateFolder() throws Exception
    {
        String folderId1;
        folderName = "Test Cmis Folder (" + System.currentTimeMillis() + ")" + "testCreateFolder";
        folderId1 = helper.createFolder(folderName, folderId, CMISDictionaryModel.FOLDER_TYPE_ID);

        resultObject = helper.getObjectProperties(folderId1);

        assertObjectPropertiesNotNull(resultObject);

        assertNotNull(getStringProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_NAME));
        assertNotNull(getIdProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_PARENT_ID));

        helper.deleteFolder(folderId1);
    }

    public void testGetDocumentProperties() throws Exception
    {
        String filter;
        filter = "*";
        resultObject = helper.getObjectProperties(documentId, filter);

        assertObjectPropertiesNotNull(resultObject);

        assertNotNull(getStringProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_NAME));
        assertNotNull(getStringProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME));
        assertNotNull(getStringProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE));
        assertTrue(getBooleanProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_IS_LATEST_VERSION));

    }

    public void testGetObject() throws Exception
    {
        CmisObjectType resultObject = ((ObjectServicePort) servicePort).getObject(repositoryId, documentId, "*", false, EnumIncludeRelationships.NONE, null, null, null, null);

        assertObjectPropertiesNotNull(resultObject);

        assertNotNull(getStringProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_NAME));
        assertNotNull(getStringProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME));
        assertNotNull(getStringProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE));
        assertTrue(getBooleanProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_IS_LATEST_VERSION));

    }

    public void testGetDocumentProperties_Versioning() throws Exception
    {
        CmisObjectType response = helper.getObjectProperties(documentId);

        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        String checkinComment = "Test checkin" + System.currentTimeMillis();

        helper.checkOut(documentIdHolder, contentCopied);
        // new version of doc
        assertObjectPropertiesNotNull(response);
        response = helper.getObjectProperties(documentIdHolder.value);
        assertNotNull(getStringProperty(response.getProperties(), CMISDictionaryModel.PROP_NAME));
        assertNotNull(getStringProperty(response.getProperties(), CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME));
        assertNotNull(getStringProperty(response.getProperties(), CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE));
        assertTrue(getBooleanProperty(response.getProperties(), CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT));
        assertNotNull(getStringProperty(response.getProperties(), CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY));

        helper.checkIn(documentIdHolder, checkinComment, true);

        response = helper.getObjectProperties(documentId);
        assertObjectPropertiesNotNull(response);
        assertNotNull(getStringProperty(response.getProperties(), CMISDictionaryModel.PROP_NAME));
        assertNotNull(getStringProperty(response.getProperties(), CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME));
        assertNotNull(getStringProperty(response.getProperties(), CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE));
        assertTrue(getBooleanProperty(response.getProperties(), CMISDictionaryModel.PROP_IS_LATEST_VERSION));
        assertTrue(getBooleanProperty(response.getProperties(), CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION));
        assertTrue(getBooleanProperty(response.getProperties(), CMISDictionaryModel.PROP_IS_MAJOR_VERSION));

        // Returns the list of all document versions for the specified version series, sorted by CREATION_DATE descending.
        List<CmisObjectType> responseVersions = helper.getAllVersions(documentId);
        assertNotNull(responseVersions);

        // Last version

        assertEquals(2, responseVersions.size());
        assertTrue("Initial version was not returned", isExistItemWithProperty(responseVersions, CMISDictionaryModel.PROP_VERSION_LABEL, "1.0"));
        assertTrue("Invalid response ordering: First object is not latest version", getBooleanProperty(responseVersions.get(0).getProperties(),
                CMISDictionaryModel.PROP_IS_LATEST_VERSION));
        assertEquals("Invalid response ordering: Second object is not head version", "1.0", getStringProperty(responseVersions.get(1).getProperties(),
                CMISDictionaryModel.PROP_VERSION_LABEL));
    }

    // This test don't asserts until CMIS setProperty()/setProperties() logic is unimplemented
    public void testGetDocumentProperties_Other() throws Exception
    {
        // If includeAllowableActions� is TRUE, the repository will return the allowable actions for the current user for the object as part of the output.
        // "IncludeRelationships" indicates whether relationships are also returned for the object. If it is set to "source" or "target", relationships for which the returned
        // object is a source, or respectively a target, will also be returned. If it is set to "both", relationships for which the returned object is either a source or a target
        // will be returned. If it is set to "none", relationships are not returned.

        CmisObjectType response = helper.getObjectProperties(documentId);
        @SuppressWarnings("unused")
        CmisObjectType object = response;
        // TODO: not implemented
        // assertNotNull(object.getAllowableActions());
        // assertNotNull(object.getRelationship());
    }

    public void testGetPropertiesForInvalidOID() throws Exception
    {
        GetProperties request = new GetProperties();
        request.setObjectId("invalid OID");
        request.setRepositoryId("invalid OID");
        try
        {
            ((ObjectServicePort) servicePort).getProperties("invalid OID", "invalid OID", "*", null);
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.INVALID_ARGUMENT));
        }
    }

    public void testGetContentStream() throws Exception
    {
        GetContentStream contStream = new GetContentStream();
        contStream.setObjectId(documentId);
        contStream.setRepositoryId(repositoryId);

        CmisContentStreamType result = ((ObjectServicePort) servicePort).getContentStream(repositoryId, documentId, null, null, null, null);
        if (result.getLength().intValue() == 0)
        {
            fail();
        }
        try
        {
            contStream.setObjectId(documentId + "s");
            {
                result = ((ObjectServicePort) servicePort).getContentStream(repositoryId, documentId, null, null, null, null);
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            fail();
        }

        // Content Stream of checked out doc
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        String checkinComment = "Test checkin" + System.currentTimeMillis();

        helper.checkOut(documentIdHolder, contentCopied);

        result = ((ObjectServicePort) servicePort).getContentStream(repositoryId, documentIdHolder.value, null, null, null, null);
        if (result.getLength().intValue() == 0)
        {
            fail();
        }

        helper.checkIn(documentIdHolder, checkinComment, true);

    }

    public void testGetContentStreamPortioning() throws Exception
    {
        String newDocumentId = helper.createDocument("TestFile" + System.currentTimeMillis(), folderId, CMISDictionaryModel.DOCUMENT_TYPE_ID, EnumVersioningState.NONE);
        String newFileName = "New file name (" + System.currentTimeMillis() + ")";
        String newContent = "123456789";
        CmisContentStreamType contentStream = new CmisContentStreamType();
        contentStream.setFilename(newFileName);
        contentStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        DataHandler dataHandler = new DataHandler(newContent, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentStream.setStream(dataHandler);
        Holder<String> documentIdHolder = new Holder<String>(newDocumentId);
        ((ObjectServicePort) servicePort).setContentStream(repositoryId, documentIdHolder, true, new Holder<String>(), contentStream, new Holder<CmisExtensionType>());

        // Test length
        CmisContentStreamType result = ((ObjectServicePort) servicePort).getContentStream(repositoryId, documentIdHolder.value, null, null, BigInteger.valueOf(5), null);
        if (result.getLength().intValue() == 0)
        {
            fail("Content Stream is empty");
        }
        StringWriter writer = new StringWriter();
        IOUtils.copy(result.stream.getInputStream(), writer);
        String content = writer.toString();
        assertEquals("12345", content);

        // Test offset+length
        result = ((ObjectServicePort) servicePort).getContentStream(repositoryId, documentIdHolder.value, null, BigInteger.valueOf(4), BigInteger.valueOf(3), null);
        if (result.getLength().intValue() == 0)
        {
            fail("Content Stream is empty");
        }
        writer = new StringWriter();
        IOUtils.copy(result.stream.getInputStream(), writer);
        content = writer.toString();
        assertEquals("567", content);
    }

    public void testCreateRelationship() throws Exception
    {
        // TODO: uncomment
        // String name = "Cmis Test Relationship";
        // String objectId = null;
        // try
        // {
        // objectId = helper.createRelationship(name, folderId, documentId);
        // }
        // catch (Exception e)
        // {
        // fail(e.getMessage());
        // }
        //
        // GetPropertiesResponse response = helper.getObjectProperties(objectId);
        // assertEquals(name, getPropertyValue(response, CMISMapping.PROP_NAME));
        //
        // helper.deleteFolder(folderId);
        //
        // response = helper.getObjectProperties(documentId);
        // assertNull(response);
    }

    public void testDeleteContentStream() throws Exception
    {
        // public void deleteContentStream(String repositoryId, String documentId)
        ((ObjectServicePort) servicePort).deleteContentStream(repositoryId, new Holder<String>(documentId), new Holder<String>(), new Holder<CmisExtensionType>());

        try
        {
            String filter = cmisObjectFactory.createGetPropertiesOfLatestVersionFilter("*").getValue();
            CmisPropertiesType object = helper.getVersioningServicePort().getPropertiesOfLatestVersion(repositoryId, documentId, false, filter, null);
            documentId = getIdProperty(object, CMISDictionaryModel.PROP_OBJECT_ID);
            ((ObjectServicePort) servicePort).getContentStream(repositoryId, documentId, null, BigInteger.ZERO, BigInteger.ZERO, null);
            fail("Content stream was not deleted");
        }
        catch (Exception e)
        {

        }

        // on content update and on content delete new version should be created
        List<CmisObjectType> responseVersions = helper.getAllVersions(documentId);
        assertNotNull(responseVersions);
        assertTrue("new version of document should be created", responseVersions.size() > 1);

    }

    public void testDeleteObject() throws Exception
    {
        // public void deleteObject(String repositoryId, String objectId)
        ((ObjectServicePort) servicePort).deleteObject(repositoryId, documentId, true, new Holder<CmisExtensionType>());
        assertObjectAbsence(documentId);
    }

    private void assertObjectAbsence(String objectId)
    {
        try
        {
            helper.getObjectProperties(objectId);
            fail("Object with Id='" + objectId + "' was not deleted");
        }
        catch (CmisException e)
        {
            assertEquals(EnumServiceException.OBJECT_NOT_FOUND, e.getFaultInfo().getType());
        }
    }

    public void testDeleteObject_Exceptions() throws Exception
    {
        // If the object is a Folder with at least one child, throw ConstraintViolationException.
        // If the object is the Root Folder, throw OperationNotSupportedException.

        documentName = "Test cmis document (" + System.currentTimeMillis() + ")";
        @SuppressWarnings("unused")
        String documentId = helper.createDocument(documentName, folderId);

        // Try to delete folder with child
        try
        {
            ((ObjectServicePort) servicePort).deleteObject(repositoryId, folderId, true, new Holder<CmisExtensionType>());
            fail("Try to delere folder with child");
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.CONSTRAINT));
        }

        // Try to delete root folder
        try
        {
            ((ObjectServicePort) servicePort).deleteObject(repositoryId, helper.getCompanyHomeId(repositoryId), true, new Holder<CmisExtensionType>());
            fail("Try to delere root folder");
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.NOT_SUPPORTED));
        }

    }

    public void testDeleteTree() throws Exception
    {
        String folderName;
        String folderId1;
        String folderId2;
        String documentId2;

        folderName = "Test Cmis Folder (" + System.currentTimeMillis() + ")";
        folderId1 = helper.createFolder(folderName, folderId);

        folderName = "Test Cmis Folder (" + System.currentTimeMillis() + ")";
        folderId2 = helper.createFolder(folderName, folderId1);

        documentName = "Test cmis document (" + System.currentTimeMillis() + ")";
        documentId2 = helper.createDocument(documentName, folderId2);

        // public FailedToDelete deleteTree(String repositoryId, String folderId, EnumUnfileNonfolderObjects unfileNonfolderObjects, Boolean continueOnFailure)
        DeleteTreeResponse.FailedToDelete response = ((ObjectServicePort) servicePort).deleteTree(repositoryId, folderId1, true, EnumUnfileObject.DELETE, true, null);
        assertTrue("All objects should be deleted", response.getObjectIds().size() == 0);

        assertObjectAbsence(folderId1);
        assertObjectAbsence(folderId2);
        assertObjectAbsence(documentId2);
    }

    public void testDeleteTree_Exceptions() throws Exception
    {
        // Try to delete root folder
        try
        {
            @SuppressWarnings("unused")
            DeleteTreeResponse.FailedToDelete response = ((ObjectServicePort) servicePort).deleteTree(repositoryId, helper.getCompanyHomeId(repositoryId), true,
                    EnumUnfileObject.DELETE, true, null);
            fail("Try to delere root folder");
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.NOT_SUPPORTED));
        }

    }

    public void testGetAllowableActions() throws Exception
    {
        CmisAllowableActionsType response;
        // CmisAllowableActionsType getAllowableActions(String repositoryId, String objectId)
        response = ((ObjectServicePort) servicePort).getAllowableActions(repositoryId, documentId, null);
        assertNotNull(response);
        assertTrue(response.canGetProperties);
        assertTrue(response.canCheckOut);

        response = ((ObjectServicePort) servicePort).getAllowableActions(repositoryId, folderId, null);
        assertNotNull(response);
        assertTrue(response.canGetProperties);
        assertNull(response.canCheckOut);
    }

    public void testMoveObject() throws Exception
    {
        // test should be passed after transaction problem is fixed
        ((ObjectServicePort) servicePort).moveObject(repositoryId, new Holder<String>(documentId), folderId, companyHomeId, new Holder<CmisExtensionType>());

        CmisObjectType response = helper.getObjectProperties(documentId);
        assertObjectPropertiesNotNull(response);

        assertNotNull(response);
        assertNotNull(getStringProperty(response.getProperties(), CMISDictionaryModel.PROP_NAME));

        List<CmisObjectParentsType> parentsResponse = helper.getObjectParents(documentId, "*");
        assertNotNull(parentsResponse);
        assertNotNull(parentsResponse.get(0));
        assertNotNull(parentsResponse.get(0).getObject());
        assertNotNull(parentsResponse.get(0).getObject().getProperties());
        assertTrue(parentsResponse.size() == 1);
        assertTrue(getStringProperty(parentsResponse.get(0).getObject().getProperties(), CMISDictionaryModel.PROP_NAME).equals(folderName));
    }

    // The moveObject() method must throw InvalidArgumentException for null SourceFolderId only if specified Object (Folder or Document) has SEVERAL parents.
    // In other case this parameter may be null.
    public void testMoveObject_Exceptions() throws Exception
    {
        // sourceFolderId is not specified - throw InvalidArgumentException
        // If Object is multi-filed and source folder is not specified, throw InvalidArgumentException.

        helper.addObjectToFolder(documentId, folderId);

        try
        {
            ((ObjectServicePort) servicePort).moveObject(repositoryId, new Holder<String>(documentId), folderId, null, new Holder<CmisExtensionType>());
            fail("sourceFolderId is not specified - should throw InvalidArgumentException");
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.INVALID_ARGUMENT));
        }

    }

    public void testSetContentStream() throws Exception
    {
        String newFileName = "New file name (" + System.currentTimeMillis() + ")";
        String newContent = "New content test";

        CmisContentStreamType contentStream = new CmisContentStreamType();
        contentStream.setFilename(newFileName);
        contentStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        // contentStream.setLength(BigInteger.valueOf(256));
        // contentStream.setUri("test uri");
        // DataHandler dataHandler = new DataHandler(new FileDataSource("D:/test.txt"));
        DataHandler dataHandler = new DataHandler(newContent, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentStream.setStream(dataHandler);

        Holder<String> documentIdHolder = new Holder<String>(documentId);
        // public void setContentStream(String repositoryId, Holder<String> documentId, Boolean overwriteFlag, CmisContentStreamType contentStream)
        ((ObjectServicePort) servicePort).setContentStream(repositoryId, documentIdHolder, true, new Holder<String>(), contentStream, new Holder<CmisExtensionType>());

        CmisContentStreamType result = ((ObjectServicePort) servicePort).getContentStream(repositoryId, documentIdHolder.value, null, null, null, null);
        if (result.getLength().intValue() == 0)
        {
            fail("Content Stream is empty");
        }
        // FIXME: uncomment when transaction problem will be fixed
        // assertEquals(newContent, result.getStream().getContent());

        // Alfresco create new version of document
        resultObject = helper.getObjectProperties(documentIdHolder.value);
        assertObjectPropertiesNotNull(resultObject);
        assertFalse("new version of document should be created", getIdProperty(resultObject.getProperties(), CMISDictionaryModel.PROP_OBJECT_ID).equals(documentId));
        List<CmisObjectType> responseVersions = helper.getAllVersions(documentId);
        assertNotNull(responseVersions);
        assertTrue("new version of document should be created", responseVersions.size() > 1);
    }

    public void testSetContentStream_Exceptions() throws Exception
    {
        String newFileName = "New file name (" + System.currentTimeMillis() + ")";
        String newContent = "New content test";

        CmisContentStreamType contentStream = new CmisContentStreamType();
        contentStream.setFilename(newFileName);
        contentStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        DataHandler dataHandler = new DataHandler(newContent, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentStream.setStream(dataHandler);
        Holder<String> holder = new Holder<String>(documentId);
        try
        {
            // public void setContentStream(String repositoryId, Holder<String> documentId, Boolean overwriteFlag, CmisContentStreamType contentStream)
            ((ObjectServicePort) servicePort).setContentStream(repositoryId, holder, false, null, contentStream, null);
            fail("ContentAlreadyExists should be thrown");
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.CONTENT_ALREADY_EXISTS));
        }

        documentId = holder.value;
        // now we can not set any property (beside name) when we create new document - so this case not working
        resultObject = helper.getObjectProperties(documentId);
        try
        {
            // public void setContentStream(String repositoryId, Holder<String> documentId, Boolean overwriteFlag, CmisContentStreamType contentStream)
            ((ObjectServicePort) servicePort).setContentStream(repositoryId, holder, true, null, null, null);
            fail("'storage' Exception should be thrown");
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.STORAGE));
        }
    }

    public void testFolderByPathReceiving() throws Exception
    {
        CmisPropertiesType actualObject = ((ObjectServicePort) servicePort).getProperties(repositoryId, folderId, CMISDictionaryModel.PROP_NAME, null);
        assertPropertiesObject(actualObject);
        String nameProperty = getStringProperty(actualObject, CMISDictionaryModel.PROP_NAME);
        assertNotNull(nameProperty);
        assertNotSame("", nameProperty);
        CmisObjectType response = ((ObjectServicePort) servicePort).getObjectByPath(repositoryId, ("/" + nameProperty), "*", false, EnumIncludeRelationships.NONE, "", false,
                false, null);
        assertNotNull(response);
        assertPropertiesObject(response.getProperties());
        assertEquals(nameProperty, getStringProperty(response.getProperties(), CMISDictionaryModel.PROP_NAME));
    }

    private void assertPropertiesObject(CmisPropertiesType actualObject)
    {
        assertNotNull(actualObject);
        assertNotNull(actualObject.getProperty());
        assertFalse(actualObject.getProperty().isEmpty());
    }

    public void testUpdateProperties() throws Exception
    {

        String newName = "New Cmis Test Node Name (" + System.currentTimeMillis() + ")";

        // Cmis Properties
        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();
        CmisPropertyString cmisProperty = new CmisPropertyString();
        cmisProperty.setLocalName(CMISDictionaryModel.PROP_NAME);
        cmisProperty.getValue().add(newName);
        propertiesList.add(cmisProperty);

        // public void updateProperties(String repositoryId, Holder<String> objectId, String changeToken, CmisPropertiesType properties)
        Holder<String> changeToken = new Holder<String>();
        ((ObjectServicePort) servicePort).updateProperties(repositoryId, new Holder<String>(documentId), changeToken, properties, new Holder<CmisExtensionType>());

        @SuppressWarnings("unused")
        CmisObjectType response = helper.getObjectProperties(documentId);
        // FIXME: uncomment this when transaction problem will be fixed
        // assertEquals(newName, getObjectName(response));
    }

    public void testUpdateProperties_Exceptions() throws Exception
    {
        // Now we can set up only name propery

        // try to update read only property
        CmisPropertiesType properties = new CmisPropertiesType();
        properties = new CmisPropertiesType();
        List<CmisProperty> propertiesList = properties.getProperty();
        CmisPropertyString cmisProperty = new CmisPropertyString();
        cmisProperty.setLocalName(CMISDictionaryModel.PROP_OBJECT_ID);
        cmisProperty.getValue().add("new id value");
        propertiesList.add(cmisProperty);
        try
        {
            Holder<String> holder = new Holder<String>(documentId);
            // public void updateProperties(String repositoryId, Holder<String> objectId, String changeToken, CmisPropertiesType properties)
            Holder<String> changeToken = new Holder<String>();
            ((ObjectServicePort) servicePort).updateProperties(repositoryId, holder, changeToken, properties, null);
            documentId = holder.value;
            GetProperties getProperties = new GetProperties();
            getProperties.setRepositoryId(repositoryId);
            getProperties.setObjectId(holder.value);
            CmisPropertiesType object = ((ObjectServicePort) servicePort).getProperties(repositoryId, holder.value, null, null);
            String documentIdAfterUpdate = getIdProperty(object, CMISDictionaryModel.PROP_OBJECT_ID);
            assertTrue("should not update read only propery", !"new id value".equals(documentIdAfterUpdate) && documentId.equals(documentIdAfterUpdate));
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.CONSTRAINT));
        }
    }
}