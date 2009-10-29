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
        ((VersioningServicePort) servicePort).checkIn(repositoryId, documentIdHolder, Boolean.TRUE, properties, contentStream, checkinComment, null, null, null);

        assertEquals(checkinComment, getStringProperty(helper.getObjectProperties(documentIdHolder.value).getProperties(), CMISDictionaryModel.PROP_CHECKIN_COMMENT));
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
        ((VersioningServicePort) servicePort).checkIn(repositoryId, documentIdHolder, null, null, null, null, null, null, null);
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
            ((VersioningServicePort) servicePort).checkIn(repositoryId, documentIdHolder, Boolean.TRUE, properties, contentStream, checkinComment, null, null, null);
            fail("Expects exception");

        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.UPDATE_CONFLICT));
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
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.UPDATE_CONFLICT));
        }
    }

    public void testGetPropertiesOfLatestVersion() throws Exception
    {
        CmisObjectType objectType = ((VersioningServicePort) servicePort).getPropertiesOfLatestVersion(repositoryId, documentId, true, "*", false);
        assertNotNull(objectType);
        assertNotNull(objectType.getProperties());
        assertTrue(getBooleanProperty(objectType.getProperties(), CMISDictionaryModel.PROP_IS_LATEST_VERSION));
    }

    public void testGetPropertiesOfLatestVersionDefault() throws Exception
    {
        CmisObjectType cmisObjectType = ((VersioningServicePort) servicePort).getPropertiesOfLatestVersion(repositoryId, documentId, true, "", null);
        assertNotNull(cmisObjectType);
        assertNotNull(cmisObjectType.getProperties());
        assertTrue(getBooleanProperty(cmisObjectType.getProperties(), CMISDictionaryModel.PROP_IS_LATEST_VERSION));
    }

    public void testGetAllVersionsDefault() throws Exception
    {
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        String checkinComment = "Test checkin" + System.currentTimeMillis();

        helper.checkOut(documentIdHolder, contentCopied);
        helper.checkIn(documentIdHolder, checkinComment, true);

        List<CmisObjectType> response = ((VersioningServicePort) servicePort).getAllVersions(repositoryId, documentId, "", null, null);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        CmisObjectType firstElement = response.iterator().next();
        assertNotNull(firstElement);
        assertEquals(checkinComment, getStringProperty(firstElement.getProperties(), CMISDictionaryModel.PROP_CHECKIN_COMMENT));
    }

    public void testGetAllVersions() throws Exception
    {
        Holder<String> documentIdHolder = new Holder<String>(documentId);
        Holder<Boolean> contentCopied = new Holder<Boolean>();
        String checkinComment = "Test checkin" + System.currentTimeMillis();

        helper.checkOut(documentIdHolder, contentCopied);
        helper.checkIn(documentIdHolder, checkinComment, true);

        List<CmisObjectType> response = ((VersioningServicePort) servicePort).getAllVersions(repositoryId, documentId, "*", false, EnumIncludeRelationships.NONE);
        assertNotNull(response);
        assertFalse(response.isEmpty());
        CmisObjectType firstElement = response.iterator().next();
        assertNotNull(firstElement);
        assertEquals(checkinComment, getStringProperty(firstElement.getProperties(), CMISDictionaryModel.PROP_CHECKIN_COMMENT));
    }

    public void testGetAllVersionsForNoVersionHistory() throws Exception
    {
        List<CmisObjectType> response = ((VersioningServicePort) servicePort).getAllVersions(repositoryId, documentId, "*", false, EnumIncludeRelationships.NONE);
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

            List<CmisObjectType> response = ((VersioningServicePort) servicePort).getAllVersions(repositoryId, documentId, "*", false, EnumIncludeRelationships.NONE);
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
            helper.checkIn(documentIdHolder, "Hello", true);
        }
    }

    public void testDeleteAllVersions() throws Exception
    {
        // TODO: Schema bug. It possible should be returned
        // ((VersioningServicePort) servicePort).deleteAllVersions(repositoryId, documentId);
    }
}
