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

import java.util.List;

import javax.activation.DataHandler;
import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.repo.content.MimetypeMap;

public class DMVersioningServiceTest extends AbstractServiceTest
{
    private String documentId;

    public DMVersioningServiceTest()
    {
        super();
    }

    public DMVersioningServiceTest(String testCase, String username, String password)
    {
        super(testCase, username, password);
    }

    protected Object getServicePort()
    {
        return helper.versioningServicePort;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        documentId = helper.createDocument("Test cmis document (" + System.currentTimeMillis() + ")", companyHomeId);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        helper.deleteDocument(documentId);
    }

    public void testCheckOutCheckIn() throws Exception
    {
        // check out
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        ((VersioningServicePort) servicePort).checkOut(repositoryId, documentIdHolder, new Holder<CmisExtensionType>(), contentCopied);
        assertTrue(contentCopied.value);
        assertFalse(documentId.equals(documentIdHolder.value));

        // check in
        CmisPropertiesType properties = new CmisPropertiesType();// TODO
        CmisContentStreamType contentStream = new CmisContentStreamType();
        contentStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        DataHandler dataHandler = new DataHandler("Test content string: " + System.currentTimeMillis(), MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentStream.setStream(dataHandler);
        String checkinComment = "Test checkin" + System.currentTimeMillis();
        // TODO: policies
        // TODO: addACEs
        // TODO: removeACEs
        ((VersioningServicePort) servicePort).checkIn(repositoryId, documentIdHolder, true, properties, contentStream, checkinComment, null, null, null,
                new Holder<CmisExtensionType>());
        documentId = documentIdHolder.value;

        assertEquals(checkinComment, getStringProperty(((VersioningServicePort) servicePort).getPropertiesOfLatestVersion(repositoryId, documentId, true, null, null), CMISDictionaryModel.PROP_CHECKIN_COMMENT));
    }

    public void testCheckOutCheckInDefault() throws Exception
    {
        // check out
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        ((VersioningServicePort) servicePort).checkOut(repositoryId, documentIdHolder, new Holder<CmisExtensionType>(), contentCopied);
        assertTrue(contentCopied.value);
        assertFalse(documentId.equals(documentIdHolder.value));

        // check in
        ((VersioningServicePort) servicePort).checkIn(repositoryId, documentIdHolder, false, null, null, null, null, null, null, new Holder<CmisExtensionType>());
        documentId = documentIdHolder.value;
    }

    public void testCheckOutCancelCheckOut() throws Exception
    {
        // check out
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        ((VersioningServicePort) servicePort).checkOut(repositoryId, documentIdHolder, new Holder<CmisExtensionType>(), contentCopied);
        assertTrue(contentCopied.value);
        assertFalse(documentId.equals(documentIdHolder.value));

        // Cancel check out
        ((VersioningServicePort) servicePort).cancelCheckOut(repositoryId, documentIdHolder.value, new Holder<CmisExtensionType>());
        assertFalse(getBooleanProperty(helper.getObjectProperties(documentId).getProperties(), CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT));
    }

    public void testCheckinNoExistsCheckOut() throws Exception
    {
        try
        {
            Holder<String> documentIdHolder = new Holder<String>(documentId);
            CmisPropertiesType properties = new CmisPropertiesType();
            CmisContentStreamType contentStream = new CmisContentStreamType();
            contentStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            DataHandler dataHandler = new DataHandler("Test content string: " + System.currentTimeMillis(), MimetypeMap.MIMETYPE_TEXT_PLAIN);
            contentStream.setStream(dataHandler);
            String checkinComment = "Test checkin";
            // TODO: policies
            // TODO: addACEs
            // TODO: removeACEs
            ((VersioningServicePort) servicePort).checkIn(repositoryId, documentIdHolder, true, properties, contentStream, checkinComment, null, null, null, null);
            fail("Expects exception");

        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.VERSIONING));
        }
    }

    public void testCancelNotExistsCheckOut() throws Exception
    {
        try
        {
            Holder<String> documentIdHolder = new Holder<String>(documentId);
            ((VersioningServicePort) servicePort).cancelCheckOut(repositoryId, documentIdHolder.value, null);
            fail("Expects exception");

        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.VERSIONING));
        }
    }

    public void testGetPropertiesOfLatestVersion() throws Exception
    {
        CmisPropertiesType objectType = ((VersioningServicePort) servicePort).getPropertiesOfLatestVersion(repositoryId, documentId, false, "*", null);
        assertNotNull(objectType);
        assertTrue(getBooleanProperty(objectType, CMISDictionaryModel.PROP_IS_LATEST_VERSION));
    }

    public void testGetPropertiesOfLatestVersionDefault() throws Exception
    {
        CmisPropertiesType cmisObjectType = ((VersioningServicePort) servicePort).getPropertiesOfLatestVersion(repositoryId, documentId, false, "", null);
        assertNotNull(cmisObjectType);
        assertTrue(getBooleanProperty(cmisObjectType, CMISDictionaryModel.PROP_IS_LATEST_VERSION));
    }

    public void testGetAllVersionsDefault() throws Exception
    {
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        String checkinComment = "Test checkin" + System.currentTimeMillis();

        helper.checkOut(documentIdHolder, contentCopied);
        helper.checkIn(documentIdHolder, checkinComment, true);
        documentId = documentIdHolder.value;

        List<CmisObjectType> response = ((VersioningServicePort) servicePort).getAllVersions(repositoryId, documentId, "", null, null);
        assertNotNull(response);
        assertTrue("Expected three versions", response.size() == 3);
        CmisObjectType lastVersion = response.get(1);
        assertNotNull(lastVersion);
        assertEquals(checkinComment, getStringProperty(lastVersion.getProperties(), CMISDictionaryModel.PROP_CHECKIN_COMMENT));
    }

    public void testGetAllVersions() throws Exception
    {
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        String checkinComment = "Test checkin" + System.currentTimeMillis();

        helper.checkOut(documentIdHolder, contentCopied);
        helper.checkIn(documentIdHolder, checkinComment, true);
        documentId = documentIdHolder.value;

        List<CmisObjectType> response = ((VersioningServicePort) servicePort).getAllVersions(repositoryId, documentId, "*", false, null);
        assertNotNull(response);
        assertTrue("Expected three versions", response.size() == 3);
        CmisObjectType lastVersion = response.get(1);
        assertNotNull(lastVersion);
        assertEquals(checkinComment, getStringProperty(lastVersion.getProperties(), CMISDictionaryModel.PROP_CHECKIN_COMMENT));
    }

    public void testGetAllVersionsForNoVersionHistory() throws Exception
    {
        List<CmisObjectType> response = ((VersioningServicePort) servicePort).getAllVersions(repositoryId, documentId, "*", false, null);
        assertNotNull(response);
    }

    public void testGetAllVersionsCheckedOutAndPWC() throws Exception
    {
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        boolean checkedOutfound = false;
        boolean pwcFound = false;
        try
        {
            helper.checkOut(documentIdHolder, contentCopied);

            List<CmisObjectType> response = ((VersioningServicePort) servicePort).getAllVersions(repositoryId, documentId, "*", false, null);
            assertNotNull(response);
            for (CmisObjectType cmisObjectType : response)
            {
                if (!checkedOutfound)
                {
                    checkedOutfound = getBooleanProperty(cmisObjectType.getProperties(), CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT);
                }
                if (!pwcFound)
                {
                    pwcFound = (getIdProperty(cmisObjectType.getProperties(), CMISDictionaryModel.PROP_OBJECT_ID)).startsWith(documentIdHolder.value);
                }
            }
            assertTrue("No checked out version found", checkedOutfound);
            assertTrue("No private working copy version found", pwcFound);
        }
        finally
        {
            helper.checkIn(documentIdHolder, "Test Check In Comment", true);
            documentId = documentIdHolder.value;
        }
    }

    public void testObjectOfLatestVersionReceiving() throws Exception
    {
        CmisObjectType result = null;
        try
        {
            VersioningServicePort versioningServicePort = helper.getVersioningServicePort();
            helper.authenticateServicePort(versioningServicePort, CmisServiceTestHelper.USERNAME_ADMIN, CmisServiceTestHelper.PASSWORD_ADMIN);
            result = versioningServicePort.getObjectOfLatestVersion(repositoryId, documentId, false, null, false, null, null, false, false, null);
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
        assertNotNull(result);
        assertNotNull(result.getProperties());
        assertNotNull(result.getProperties().getProperty());
        assertFalse(result.getProperties().getProperty().isEmpty());
    }

    public void testObjectOfLatestMajorVersionReceiving() throws Exception
    {
        CmisObjectType result = null;
        try
        {
            VersioningServicePort versioningServicePort = helper.getVersioningServicePort();
            helper.authenticateServicePort(versioningServicePort, CmisServiceTestHelper.USERNAME_ADMIN, CmisServiceTestHelper.PASSWORD_ADMIN);
            result = versioningServicePort.getObjectOfLatestVersion(repositoryId, documentId, true, null, false, null, null, false, false, null);
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
        assertNotNull(result);
        assertNotNull(result.getProperties());
        assertNotNull(result.getProperties().getProperty());
        assertFalse(result.getProperties().getProperty().isEmpty());
        assertTrue(getBooleanProperty(result.getProperties(), CMISDictionaryModel.PROP_IS_MAJOR_VERSION));
    }

    public void testObjectOfLatestVersionReceivingWithAllowableActions() throws Exception
    {
        CmisObjectType result = null;
        try
        {
            VersioningServicePort versioningServicePort = helper.getVersioningServicePort();
            helper.authenticateServicePort(versioningServicePort, CmisServiceTestHelper.USERNAME_ADMIN, CmisServiceTestHelper.PASSWORD_ADMIN);
            result = versioningServicePort.getObjectOfLatestVersion(repositoryId, documentId, false, null, true, null, null, false, false, null);
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
        assertNotNull(result);
        assertNotNull(result.getProperties());
        assertNotNull(result.getProperties().getProperty());
        assertFalse(result.getProperties().getProperty().isEmpty());
        assertNotNull(result.getAllowableActions());
        assertTrue(result.getAllowableActions().isCanGetProperties());
        assertTrue(result.getAllowableActions().isCanDeleteObject());
    }
}
