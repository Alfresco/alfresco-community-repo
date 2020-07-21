/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.tests;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.alfresco.rest.api.impl.DownloadsImpl.DEFAULT_ARCHIVE_EXTENSION;
import static org.alfresco.rest.api.impl.DownloadsImpl.DEFAULT_ARCHIVE_NAME;
import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.model.AssocChild;
import org.alfresco.rest.api.model.Download;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * Tests the /downloads API
 * 
 * @author cpopa
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDownloads extends AbstractBaseApiTest
{     
    private static Log logger = LogFactory.getLog(TestDownloads.class);
    
    private static final int NUMBER_OF_TIMES_TO_RETRY_TEST_CANCEL_STATUS = 5;

    private static final int STATUS_CHECK_SLEEP_TIME = 5;

    private static final int NUMBER_OF_TIMES_TO_CHECK_STATUS = 200;

    public static final String NODES_SECONDARY_CHILDREN = "nodes/%s/secondary-children";

    public static final String API_DOWNLOADS = "downloads";
    
    private static final String DOC4_NAME = "docTest4.txt";
    private static final String SUB_FOLDER1_NAME = "subFolder1";
    private static final String DOC3_NAME = "docTest3.txt";
    private static final String FOLDER1_NAME = "folder1";
    private static final String FOLDER3_NAME = "folder3";
    private static final String ZIPPABLE_DOC1_NAME = "docTest1.txt";
    private static final String DUMMY_CONTENT = "dummy content";
    private org.alfresco.rest.api.Nodes nodesApi;
    private String zippableDocId1;
    private String zippableDocId2;
    private String zippableDocId3_InFolder1;
    private String zippableFolderId1;
    private String zippableFolderId2_InFolder1;
    private String zippableDocId4_InFolder2;
    private String zippableFolderId3;
    private String zippableDoc_user2;
    
    @Before
    public void setupTest() throws IOException, Exception{
        nodesApi = applicationContext.getBean("Nodes", org.alfresco.rest.api.Nodes.class);
        
        setRequestContext(user1);
        
        Document zippableDoc1 = createTextFile(tDocLibNodeId, ZIPPABLE_DOC1_NAME, DUMMY_CONTENT);
        zippableDocId1 = zippableDoc1.getId();
        zippableDocId2 = createTextFile(tDocLibNodeId, "docTest2", DUMMY_CONTENT).getId();
        
        Folder zippableFolder1 = createFolder(tDocLibNodeId, FOLDER1_NAME);
        zippableFolderId1 = zippableFolder1.getId();
        zippableDocId3_InFolder1 = createTextFile(zippableFolderId1, DOC3_NAME, DUMMY_CONTENT).getId();
        
        Folder zippableFolder2_InFolder1 = createFolder(zippableFolderId1, SUB_FOLDER1_NAME);
        zippableFolderId2_InFolder1 = zippableFolder2_InFolder1.getId();
        
        zippableDocId4_InFolder2 = createTextFile(zippableFolderId2_InFolder1, DOC4_NAME, DUMMY_CONTENT).getId();
        
        Folder zippableFolder3 = createFolder(tDocLibNodeId, FOLDER3_NAME);
        zippableFolderId3 = zippableFolder3.getId();
        
        setRequestContext(user2);
        String user2Site = createSite ("TestSite B - " + RUNID, SiteVisibility.PRIVATE).getId();
        String user2DocLib = getSiteContainerNodeId(user2Site, "documentLibrary");
        zippableDoc_user2 = createTextFile(user2DocLib, "user2doc", DUMMY_CONTENT).getId();
        
        setRequestContext(user1);
        AssocChild secChild = new AssocChild(zippableDoc1.getId(), ASSOC_TYPE_CM_CONTAINS);
        post(format(NODES_SECONDARY_CHILDREN, zippableFolder3.getId()), toJsonAsStringNonNull(secChild), HttpServletResponse.SC_CREATED);
    }
    
    
    /**
     * Tests the creation of download nodes.
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/private/alfresco/versions/1/downloads}
     *
     */
	@Test
	public void test001CreateDownload() throws Exception
	{   
	    //test creating a download with a single file
        Download download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableDocId1);
        
        assertPendingDownloadProps(download);
        
        assertValidZipNodeid(download);
        
        assertDoneDownload(download, 1, 13);
       
        //test creating a multiple file archive
        download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableDocId1, zippableDocId2);
        
        assertPendingDownloadProps(download);
        
        assertValidZipNodeid(download);
        
        assertDoneDownload(download, 2, 26);
        
        //test creating a zero file archive
        createDownload(HttpServletResponse.SC_BAD_REQUEST);
        
        //test creating an archive with the same file twice
        download = createDownload(HttpServletResponse.SC_BAD_REQUEST, zippableDocId1, zippableDocId1);
        
        //test creating an archive with a folder and a file which is contained in the folder
        download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableFolderId1, zippableDocId3_InFolder1);
        
        assertPendingDownloadProps(download);
        
        assertValidZipNodeid(download);
        
        assertDoneDownload(download, 3, 39);
        
        //test creating an archive with a file and a folder containing that file but only as a secondary parent child association
        download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableDocId1, zippableFolderId3);
        
        assertPendingDownloadProps(download);
        
        assertValidZipNodeid(download);
        
        assertDoneDownload(download, 2, 26);
        
        //test creating an archive with two files, one of which user1 does not have permissions for
        download = createDownload(HttpServletResponse.SC_FORBIDDEN, zippableDocId1, zippableDoc_user2);
	}   
	
	/**
     * Tests retrieving info about a download node
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/private/alfresco/versions/1/downloads/<download_id>}
     *
     */
    @Test
    public void test002GetDownloadInfo() throws Exception
    {  
        Download download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableFolderId1, zippableFolderId2_InFolder1, zippableDocId4_InFolder2);
        
        //test retrieving information about an ongoing download
        assertInProgressDownload(download, 4, 52);
             
        //test retrieving information about a finished download
        assertDoneDownload(download, 4, 52);
        
        //test retrieving the status of a cancelled download
        cancelWithRetry(() -> 
        {
            Download downloadToBeCancelled = createDownload(HttpServletResponse.SC_ACCEPTED, zippableFolderId1, zippableDocId3_InFolder1);            
            cancel(downloadToBeCancelled.getId());            
            assertCancelledDownload(downloadToBeCancelled, 3, 39);
        });
    }
    
    /**
     * Tests canceling a download.
     *
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/-default-/private/alfresco/versions/1/downloads/<download_id>}
     * 
     */
    @Test
    public void test003CancelDownload() throws Exception
    {
        //cancel a running download operation.
        cancelWithRetry(()->{
            Download download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableFolderId1, zippableDocId3_InFolder1, zippableDocId1, zippableDocId2);            
            cancel(download.getId());            
            assertCancelledDownload(download, 5, 65);
        });
        
        //cancel a completed download - should have no effect
        Download download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableDocId1, zippableDocId2);
        
        assertDoneDownload(download, 2, 26);
        
        cancel(download.getId());
        
        Thread.sleep(500);
        
        Download downloadStatus = getDownload(download.getId());
        
        assertTrue("A cancel operation on a DONE download has no effect.", downloadStatus.getStatus().equals(DownloadStatus.Status.DONE));
        
        //cancel a node which is not of a download type
        cancel(HttpServletResponse.SC_BAD_REQUEST, zippableDocId1);
        
        //user2 canceling user1 download operation - should not be allowed
        download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableDocId1);
        
        setRequestContext(user2);
        
        cancel(HttpServletResponse.SC_FORBIDDEN, download.getId());
    }
    
    /**
     * Tests downloading the content of a download node(a zip) using the /nodes API:
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>/content}
     * 
     */
    @Test
    public void test004GetDownloadContent() throws Exception{
        
        //test downloading the content of a 1 file zip
        Download download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableDocId1);
        
        assertDoneDownload(download, 1, 13);
        
        HttpResponse response = downloadContent(download);
          
        ZipInputStream zipStream = getZipStreamFromResponse(response);
        
        ZipEntry zipEntry = zipStream.getNextEntry();
        
        assertEquals("Zip entry name is not correct", ZIPPABLE_DOC1_NAME, zipEntry.getName());
        
        assertTrue("Zip entry size is not correct", zipEntry.getCompressedSize() <= 13);
        
        assertTrue("No more entries should be in this zip", zipStream.getNextEntry() == null);
        zipStream.close();        
        
        Map<String, String> responseHeaders = response.getHeaders();
        
        assertNotNull(responseHeaders);
        
        assertEquals(format("attachment; filename=\"%s\"; filename*=UTF-8''%s", ZIPPABLE_DOC1_NAME + DEFAULT_ARCHIVE_EXTENSION,
                                                                                ZIPPABLE_DOC1_NAME + DEFAULT_ARCHIVE_EXTENSION), 
                                                                        responseHeaders.get("Content-Disposition"));
        
        //test downloading the content of a multiple file zip
        download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableFolderId1, zippableDocId3_InFolder1);
        
        assertDoneDownload(download, 3, 39);
        
        response = downloadContent(download);
        
        zipStream = getZipStreamFromResponse(response);
        
        assertEquals("Zip entry name is not correct", FOLDER1_NAME + "/", zipStream.getNextEntry().getName());
        assertEquals("Zip entry name is not correct", FOLDER1_NAME + "/" + DOC3_NAME, zipStream.getNextEntry().getName());
        assertEquals("Zip entry name is not correct", FOLDER1_NAME + "/" + SUB_FOLDER1_NAME + "/", zipStream.getNextEntry().getName());
        assertEquals("Zip entry name is not correct", FOLDER1_NAME + "/" + SUB_FOLDER1_NAME + "/" + DOC4_NAME, zipStream.getNextEntry().getName());
        assertEquals("Zip entry name is not correct", DOC3_NAME, zipStream.getNextEntry().getName());
        
        assertTrue("No more entries should be in this zip", zipStream.getNextEntry() == null);
        zipStream.close();       
        
        responseHeaders = response.getHeaders();
        
        assertNotNull(responseHeaders);
        
        assertEquals(format("attachment; filename=\"%s\"; filename*=UTF-8''%s", DEFAULT_ARCHIVE_NAME,
                                                                                DEFAULT_ARCHIVE_NAME), 
                                                                        responseHeaders.get("Content-Disposition"));
        
        //test download the content of a zip which has a secondary child
        download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableDocId1, zippableFolderId3);
        assertDoneDownload(download, 2, 26);
        
        response = downloadContent(download);
        
        zipStream = getZipStreamFromResponse(response);
        
        assertEquals("Zip entry name is not correct", ZIPPABLE_DOC1_NAME, zipStream.getNextEntry().getName());
        assertEquals("Zip entry name is not correct", FOLDER3_NAME + "/", zipStream.getNextEntry().getName());
        assertEquals("Zip entry name is not correct", FOLDER3_NAME + "/" + ZIPPABLE_DOC1_NAME, zipStream.getNextEntry().getName());
        assertTrue("No more entries should be in this zip", zipStream.getNextEntry() == null);
    }
    
    /**
     * Tests deleting a download node using the /nodes API:
     *
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<nodeId>}
     * 
     */
    @Test 
    public void test005DeleteDownloadNode() throws Exception{
        
        //test deleting a download node
        Download download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableDocId1);
        
        assertDoneDownload(download, 1, 13);
        
        deleteNode(download.getId(), true, HttpServletResponse.SC_NO_CONTENT);
        
        getDownload(download.getId(), HttpServletResponse.SC_NOT_FOUND);
        
        //test user2 deleting a download node created by user1
        download = createDownload(HttpServletResponse.SC_ACCEPTED, zippableDocId1);
        
        assertDoneDownload(download, 1, 13);
        
        setRequestContext(user2);
        
        deleteNode(download.getId(), true, HttpServletResponse.SC_FORBIDDEN);

        setRequestContext(user1);
        assertDoneDownload(download, 1, 13);

        // user2 should not be able to read information about downloads started by other users
        setRequestContext(user2);
        getDownload(download.getId(), HttpServletResponse.SC_FORBIDDEN);
    }
    
    protected ZipInputStream getZipStreamFromResponse(HttpResponse response)
    {
        return new ZipInputStream(new ByteArrayInputStream(response.getResponseAsBytes()));
    }

    protected HttpResponse downloadContent(Download download) throws Exception
    {
        return getSingle(NodesEntityResource.class, download.getId() + "/content", null, HttpServletResponse.SC_OK);
    }
    
    /**
     * It may happen that a download is already done before getting a chance to call cancel(). Hence retry a couple of times.
     * @param cancelAction
     * @throws Exception
     */
    private void cancelWithRetry(CancelAction cancelAction) throws Exception{
        for(int i = 0; i<=NUMBER_OF_TIMES_TO_RETRY_TEST_CANCEL_STATUS; i++)
        {
            if (i == NUMBER_OF_TIMES_TO_RETRY_TEST_CANCEL_STATUS)
            {
                logger.error("Did not manage to test the cancel status, the download node gets to the DONE status too fast.");
            }
            try
            {
                cancelAction.run();
            }catch(DownloadAlreadyDoneException e)
            {
                continue;
            }
        }
    }
    
    private void assertDoneDownload(Download download, int expectedFilesAdded, int expectedTotal) throws Exception, InterruptedException
    {
        assertExpectedStatus(DownloadStatus.Status.DONE, download, "Download should be DONE by now.", downloadStatus -> 
        {
            assertTrue("The number of bytes added in the archive does not match the total", downloadStatus.getBytesAdded() == downloadStatus.getTotalBytes());
            assertEquals("The number of files added in the archive should be " + expectedFilesAdded, expectedFilesAdded, downloadStatus.getFilesAdded());
            assertEquals("The total number of bytes should be " + expectedTotal, expectedTotal, downloadStatus.getTotalBytes());
            assertEquals("The total number of files of the final archive should be " + expectedFilesAdded, expectedFilesAdded, downloadStatus.getTotalFiles());
        }, null, null);
    }

    protected void assertCancelledDownload(Download download, int expectedTotalFiles, int expectedTotal) throws PublicApiException, Exception, InterruptedException
    {
        assertExpectedStatus(DownloadStatus.Status.CANCELLED, download, "Download should be CANCELLED by now.", downloadStatus -> 
        {
            assertTrue("The total bytes added to the archive by now should be greater than 0", downloadStatus.getBytesAdded() > 0 && downloadStatus.getBytesAdded() <= downloadStatus.getTotalBytes());
            assertTrue("The download has been cancelled, there should still be files to be added.", downloadStatus.getFilesAdded() < downloadStatus.getTotalFiles());
            assertEquals("The total number of bytes should be " + expectedTotal, expectedTotal, downloadStatus.getTotalBytes());
            assertEquals("The total number of files to be added to the archive should be " + expectedTotalFiles, expectedTotalFiles, downloadStatus.getTotalFiles());
        }, DownloadStatus.Status.DONE, downloadStatus->
        {
            throw new DownloadAlreadyDoneException();
        });
    }

    private void assertInProgressDownload(Download download, int expectedTotalFiles, int expectedTotal) throws Exception, InterruptedException
    {
        assertExpectedStatus(DownloadStatus.Status.IN_PROGRESS, download, "Download creation is taking too long.Download status should be at least IN_PROGRESS by now.", downloadStatus -> 
        {
            //'done' can be equal to the 'total' even though the status is IN_PROGRESS. See ZipDownloadExporter line 239
            assertTrue("The total bytes added to the archive by now should be greater than 0", downloadStatus.getBytesAdded() > 0 && downloadStatus.getBytesAdded() <= downloadStatus.getTotalBytes());
            assertTrue("The download is in progress, there should still be files to be added.", downloadStatus.getFilesAdded() < downloadStatus.getTotalFiles());
            assertEquals("The total number of bytes should be " + expectedTotal, expectedTotal, downloadStatus.getTotalBytes());
            assertEquals("The total number of files to be added to the archive should be " + expectedTotalFiles, expectedTotalFiles, downloadStatus.getTotalFiles());
        }, DownloadStatus.Status.DONE, downloadStatus ->
        {
            try
            {
                assertDoneDownload(download, expectedTotalFiles, expectedTotal);
            }
            catch (Exception e)
            { 
                throw new RuntimeException(e);
            }
        });
    }
    
    private void assertExpectedStatus(DownloadStatus.Status expectedStatus, Download download, String failMessage, Consumer<Download> assertionsToDo, 
                                      DownloadStatus.Status alternateExpectedStatus, Consumer<Download> alternateAssertionsToDo) throws Exception{
        for(int i = 0; i<=NUMBER_OF_TIMES_TO_CHECK_STATUS; i++){
            if (i == NUMBER_OF_TIMES_TO_CHECK_STATUS)
            {
                fail(failMessage);
            }
            Download downloadStatus = getDownload(download.getId());
            if (alternateExpectedStatus != null && downloadStatus.getStatus().equals(alternateExpectedStatus))
            {
                alternateAssertionsToDo.accept(downloadStatus);
                break;
            }
            else if (!downloadStatus.getStatus().equals(expectedStatus))
            {
                Thread.sleep(STATUS_CHECK_SLEEP_TIME);
            }else
            {
                assertionsToDo.accept(downloadStatus);
                break;
            }
        }
    }

    protected void setRequestContext(String user)
    {
        setRequestContext(networkOne.getId(), user, null);
    }

    private void assertValidZipNodeid(Download download)
    {
        try{
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
            {

                @Override
                public Void doWork() throws Exception
                {
                    nodesApi.validateNode(download.getId());
                    return null;
                }
            }, user1, networkOne.getId());
            
        }catch(ApiException ex){
            org.junit.Assert.fail("The download nodeid is not valid." + ex.getMessage());
        }
    }

    private void assertPendingDownloadProps(Download download)
    {
        assertEquals("The download request hasn't been processed yet, the status is not correct", DownloadStatus.Status.PENDING, download.getStatus());
        assertEquals("Should be 0, the download req hasn't been processed yet", 0, download.getBytesAdded());
        assertEquals("Should be 0, the download req hasn't been processed yet", 0, download.getFilesAdded());
        assertEquals("Should be 0, the download req hasn't been processed yet", 0, download.getTotalBytes());
        assertEquals("Should be 0, the download req hasn't been processed yet", 0, download.getTotalFiles());
    }


    @Override
    public String getScope()
    {
        return "public";
    }    
    
    private Download createDownload(int expectedStatus, String ... nodeIds) throws Exception
    {
        Download downloadRequest = new Download();
        downloadRequest.setNodeIds(Arrays.asList(nodeIds));
        
        setRequestContext(user1);
        
        Download download = create(downloadRequest, expectedStatus);
        return download;
    }

    public Download create(Download download, int expectedStatus) throws Exception
    {
        HttpResponse response  = post(API_DOWNLOADS, RestApiUtil.toJsonAsStringNonNull(download), expectedStatus);
        return getDownloadFromResponse(response);
    }
    
    public Download getDownload(String downloadId, int expectedStatus) throws Exception
    {
        HttpResponse response = getSingle(API_DOWNLOADS, downloadId, expectedStatus);

        return getDownloadFromResponse(response);
    }

    public Download getDownload(String downloadId) throws Exception
    {
        return getDownload(downloadId, HttpServletResponse.SC_OK);
    }
    
    public void cancel(String downloadId) throws Exception
    {
        cancel(HttpServletResponse.SC_ACCEPTED, downloadId);
    }
    
    public void cancel(int expectedStatusCode, String downloadId) throws Exception
    {
        delete(API_DOWNLOADS, downloadId, expectedStatusCode);
    }
    
    protected Download getDownloadFromResponse(HttpResponse response) throws Exception
    {
        if (asList(SC_ACCEPTED, SC_OK).contains(response.getStatusCode()))
        {
            return RestApiUtil.parseRestApiEntry((JSONObject) response.getJsonResponse(), Download.class);
        }
        return null;
    }
    
    private static class DownloadAlreadyDoneException extends RuntimeException
    {        
    }
    
    private interface CancelAction{
        void run() throws Exception;
    }
}
