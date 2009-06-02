/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
        ((VersioningServicePort) servicePort).checkOut(repositoryId, documentIdHolder, contentCopied);
        assertTrue(contentCopied.value);
        assertFalse(documentId.equals(documentIdHolder.value));

        // check in
        CmisPropertiesType properties = new CmisPropertiesType();// TODO
        CmisContentStreamType contentStream = new CmisContentStreamType();
        contentStream.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        DataHandler dataHandler = new DataHandler("Test content string: " + System.currentTimeMillis(), MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentStream.setStream(dataHandler);
        String checkinComment = "Test checkin" + System.currentTimeMillis();
        ((VersioningServicePort) servicePort).checkIn(repositoryId, documentIdHolder, Boolean.TRUE, properties, contentStream, checkinComment);

        assertEquals(checkinComment, propertiesUtil.getCmisPropertyValue(helper.getObjectProperties(documentId).getObject().getProperties(), CMISDictionaryModel.PROP_CHECKIN_COMMENT, null));
    }

    public void testCheckOutCheckInDefault() throws Exception
    {
        // check out
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        ((VersioningServicePort) servicePort).checkOut(repositoryId, documentIdHolder, contentCopied);
        assertTrue(contentCopied.value);
        assertFalse(documentId.equals(documentIdHolder.value));

        // check in
        ((VersioningServicePort) servicePort).checkIn(repositoryId, documentIdHolder, null, null, null, null);
    }

    public void testCheckOutCancelCheckOut() throws Exception
    {
        // check out
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        ((VersioningServicePort) servicePort).checkOut(repositoryId, documentIdHolder, contentCopied);
        assertTrue(contentCopied.value);
        assertFalse(documentId.equals(documentIdHolder.value));

        // Cancel check out
        ((VersioningServicePort) servicePort).cancelCheckOut(repositoryId, documentIdHolder.value);
        assertFalse((Boolean) propertiesUtil.getCmisPropertyValue(helper.getObjectProperties(documentId).getObject().getProperties(), CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT, null));
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
            ((VersioningServicePort) servicePort).checkIn(repositoryId, documentIdHolder, Boolean.TRUE, properties, contentStream, checkinComment);
            fail("Expects exception");

        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.NOT_SUPPORTED));
        }
    }

    public void testCancelNotExistsCheckOut() throws Exception
    {
        try
        {
            Holder<String> documentIdHolder = new Holder<String>(documentId);
            ((VersioningServicePort) servicePort).cancelCheckOut(repositoryId, documentIdHolder.value);
            fail("Expects exception");

        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.NOT_SUPPORTED));
        }
    }

    public void testGetPropertiesOfLatestVersion() throws Exception
    {
        GetPropertiesOfLatestVersion request = new GetPropertiesOfLatestVersion();
        request.setRepositoryId(repositoryId);
        request.setFilter(cmisObjectFactory.createGetPropertiesOfLatestVersionFilter("*"));
        request.setMajorVersion(Boolean.TRUE);
        request.setVersionSeriesId(documentId);
        GetPropertiesOfLatestVersionResponse response = ((VersioningServicePort) servicePort).getPropertiesOfLatestVersion(request);
        assertNotNull(response);
        assertNotNull(response.getObject());
        CmisObjectType objectType = response.getObject();
        assertNotNull(objectType.getProperties());
        assertTrue((Boolean) propertiesUtil.getCmisPropertyValue(objectType.getProperties(), CMISDictionaryModel.PROP_IS_LATEST_VERSION, null));
    }

    public void testGetPropertiesOfLatestVersionDefault() throws Exception
    {
        GetPropertiesOfLatestVersion request = new GetPropertiesOfLatestVersion();
        request.setRepositoryId(repositoryId);
        request.setVersionSeriesId(documentId);
        GetPropertiesOfLatestVersionResponse response = ((VersioningServicePort) servicePort).getPropertiesOfLatestVersion(request);
        assertNotNull(response);
        assertNotNull(response.getObject());
        CmisObjectType objectType = response.getObject();
        assertNotNull(objectType.getProperties());
        assertTrue((Boolean) propertiesUtil.getCmisPropertyValue(objectType.getProperties(), CMISDictionaryModel.PROP_IS_LATEST_VERSION, null));
    }

    public void testGetAllVersionsDefault() throws Exception
    {
        GetAllVersions request = new GetAllVersions();

        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        String checkinComment = "Test checkin" + System.currentTimeMillis();

        helper.checkOut(documentIdHolder, contentCopied);
        helper.checkIn(documentIdHolder, checkinComment, true);

        request.setRepositoryId(repositoryId);
        request.setVersionSeriesId(documentId);

        GetAllVersionsResponse response = ((VersioningServicePort) servicePort).getAllVersions(request);
        assertNotNull(response);
        assertNotNull(response.getObject());
        assertEquals(checkinComment, propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_CHECKIN_COMMENT, null));
    }

    public void testGetAllVersions() throws Exception
    {
        GetAllVersions request = new GetAllVersions();

        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        String checkinComment = "Test checkin" + System.currentTimeMillis();

        helper.checkOut(documentIdHolder, contentCopied);
        helper.checkIn(documentIdHolder, checkinComment, true);

        request.setRepositoryId(repositoryId);
        request.setVersionSeriesId(documentId);
        request.setFilter(cmisObjectFactory.createGetAllVersionsFilter("*"));
        request.setIncludeAllowableActions(cmisObjectFactory.createGetAllVersionsIncludeAllowableActions(Boolean.FALSE));
        request.setIncludeRelationships(cmisObjectFactory.createGetAllVersionsIncludeRelationships(EnumIncludeRelationships.NONE));

        GetAllVersionsResponse response = ((VersioningServicePort) servicePort).getAllVersions(request);
        assertNotNull(response);
        assertNotNull(response.getObject());
        assertEquals(checkinComment, propertiesUtil.getCmisPropertyValue(response.getObject().get(0).getProperties(), CMISDictionaryModel.PROP_CHECKIN_COMMENT, null));
    }

    public void testGetAllVersionsForNoVersionHistory() throws Exception
    {
        GetAllVersions request = new GetAllVersions();

        request.setRepositoryId(repositoryId);
        request.setVersionSeriesId(documentId);
        request.setFilter(cmisObjectFactory.createGetAllVersionsFilter("*"));
        request.setIncludeAllowableActions(cmisObjectFactory.createGetAllVersionsIncludeAllowableActions(Boolean.FALSE));
        request.setIncludeRelationships(cmisObjectFactory.createGetAllVersionsIncludeRelationships(EnumIncludeRelationships.NONE));

        GetAllVersionsResponse response = ((VersioningServicePort) servicePort).getAllVersions(request);
        assertNotNull(response);
        assertNotNull(response.getObject());
    }

    public void testGetAllVersionsCheckedOutAndPWC() throws Exception
    {
        GetAllVersions request = new GetAllVersions();
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        boolean checkedOutfound = false;
        boolean pwcFound = false;
        try
        {
            helper.checkOut(documentIdHolder, contentCopied);
            request.setRepositoryId(repositoryId);
            request.setVersionSeriesId(documentId);
            request.setFilter(cmisObjectFactory.createGetAllVersionsFilter("*"));
            request.setIncludeAllowableActions(cmisObjectFactory.createGetAllVersionsIncludeAllowableActions(Boolean.FALSE));
            request.setIncludeRelationships(cmisObjectFactory.createGetAllVersionsIncludeRelationships(EnumIncludeRelationships.NONE));

            GetAllVersionsResponse response = ((VersioningServicePort) servicePort).getAllVersions(request);
            assertNotNull(response);
            assertNotNull(response.getObject());
            for (CmisObjectType cmisObjectType : response.getObject())
            {
                if (!checkedOutfound)
                {
                    checkedOutfound = (Boolean) propertiesUtil.getCmisPropertyValue(cmisObjectType.getProperties(), CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT, null);
                }
                if (!pwcFound)
                {
                    pwcFound = ((String) propertiesUtil.getCmisPropertyValue(cmisObjectType.getProperties(), CMISDictionaryModel.PROP_OBJECT_ID, null)).startsWith(documentIdHolder.value);
                }
            }
            assertTrue("No checked out version found", checkedOutfound);
            assertTrue("No private working copy version found", pwcFound);
        }
        finally
        {
            helper.checkIn(documentIdHolder, "Hello", true);
        }
    }

    public void testDeleteAllVersions() throws Exception
    {
        ((VersioningServicePort) servicePort).deleteAllVersions(repositoryId, documentId);
    }
}
