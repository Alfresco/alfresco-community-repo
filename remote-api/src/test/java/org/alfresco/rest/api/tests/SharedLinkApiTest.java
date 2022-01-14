/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.quickshare.QuickShareLinkExpiryActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.service.ServiceDescriptorRegistry;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.QuickShareLinks;
import org.alfresco.rest.api.impl.QuickShareLinksImpl;
import org.alfresco.rest.api.model.LockInfo;
import org.alfresco.rest.api.model.NodePermissions;
import org.alfresco.rest.api.model.PathInfo;
import org.alfresco.rest.api.model.QuickShareLink;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.quicksharelinks.QuickShareLinkEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Favourite;
import org.alfresco.rest.api.tests.client.data.FavouriteDocument;
import org.alfresco.rest.api.tests.client.data.FileFavouriteTarget;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.QuickShareLinkEmailRequest;
import org.alfresco.rest.api.tests.client.data.Rendition;
import org.alfresco.rest.api.tests.client.data.UserInfo;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedActionService;
import org.alfresco.service.cmr.quickshare.QuickShareLinkExpiryAction;
import org.alfresco.service.cmr.quickshare.QuickShareLinkExpiryActionPersister;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.testing.category.LuceneTests;
import org.alfresco.util.testing.category.RedundantTests;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * V1 REST API tests for Shared Links (aka public "quick shares")
 *
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links} </li>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>} </li>
 * </ul>
 *
 * @author janv
 * @author mbhave
 */
@SuppressWarnings("deprecation")
@Category(LuceneTests.class)
public class SharedLinkApiTest extends AbstractBaseApiTest
{
    private static final String URL_SHARED_LINKS = "shared-links";

    private QuickShareLinkExpiryActionPersister quickShareLinkExpiryActionPersister;
    private ScheduledPersistedActionService scheduledPersistedActionService;

    @Before
    public void setup() throws Exception
    {
        super.setup();
        quickShareLinkExpiryActionPersister = applicationContext.getBean("quickShareLinkExpiryActionPersister", QuickShareLinkExpiryActionPersister.class);
        scheduledPersistedActionService = applicationContext.getBean("scheduledPersistedActionService", ScheduledPersistedActionService.class);
    }

    /**
     * Tests shared links to file (content)
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links}
     *
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>}
     *
     * <p>GET:</p>
     * The following do not require authentication
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>/content}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>/renditions}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>/renditions/<renditionId>}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>/renditions/<renditionId>/content}
     *
     */
    @Test
    public void testSharedLinkCreateGetDelete() throws Exception
    {
        // As user 1 ...
        setRequestContext(user1);

        // create doc d1 - pdf
        String sharedFolderNodeId = getSharedNodeId();

        String fileName1 = "quick"+RUNID+"_1.pdf";
        File file1 = getResourceFile("quick.pdf");

        byte[] file1_originalBytes = Files.readAllBytes(Paths.get(file1.getAbsolutePath()));

        String file1_MimeType = MimetypeMap.MIMETYPE_PDF;

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new MultiPartBuilder.FileData(fileName1, file1, file1_MimeType));
        MultiPartBuilder.MultiPartRequest reqBody = multiPartBuilder.build();

        HttpResponse response = post(getNodeChildrenUrl(sharedFolderNodeId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document doc1 = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        String d1Id = doc1.getId();

        // create doc d2 - plain text
        String myFolderNodeId = getMyNodeId();

        String content2Text = "The quick brown fox jumps over the lazy dog 2.";
        String fileName2 = "content" + RUNID + "_2.txt";

        Document doc2 = createTextFile(myFolderNodeId, fileName2, content2Text);
        String d2Id = doc2.getId();

        String file2_MimeType = MimetypeMap.MIMETYPE_TEXT_PLAIN;

        // As user 2 ...
        setRequestContext(user2);

        response = getSingle(NodesEntityResource.class, d1Id, null, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        Date docModifiedAt = nodeResp.getModifiedAt();
        String docModifiedBy = nodeResp.getModifiedByUser().getId();
        assertEquals(user1, docModifiedBy);

        // create shared link to document 1
        Map<String, String> body = new HashMap<>();
        body.put("nodeId", d1Id);

        // TODO find a better solution to wait for the asynchronous metadata-extract/transform operation. E.g. awaitility
        Thread.sleep(3000);

        response = post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);
        QuickShareLink resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);

        String shared1Id = resp.getId();
        assertNotNull(shared1Id);

        assertEquals(d1Id, resp.getNodeId());
        assertEquals(fileName1, resp.getName());
        assertEquals("The quick brown fox jumps over the lazy dog", resp.getTitle());
        assertEquals("Pangram, fox, dog, Gym class featuring a brown fox and lazy dog", resp.getDescription());

        assertEquals(file1_MimeType, resp.getContent().getMimeType());
        assertEquals("Adobe PDF Document", resp.getContent().getMimeTypeName());

        assertEquals(new Long(file1_originalBytes.length), resp.getContent().getSizeInBytes());
        assertEquals("UTF-8", resp.getContent().getEncoding());

       // assertEquals(docModifiedAt.getTime(), resp.getModifiedAt().getTime()); // not changed
        assertEquals(docModifiedBy, resp.getModifiedByUser().getId()); // not changed (ie. not user2)
        assertEquals(UserInfo.getTestDisplayName(docModifiedBy), resp.getModifiedByUser().getDisplayName());

        assertEquals(user2, resp.getSharedByUser().getId());
        assertEquals(UserInfo.getTestDisplayName(user2), resp.getSharedByUser().getDisplayName());

        // -ve test - try to create again (same user) - already exists
        post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 409);


        // As user 1 ...
        setRequestContext(user1);

        // create shared link to document 2
        body = new HashMap<>();
        body.put("nodeId", d2Id);

        response = post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        String shared2Id = resp.getId();


        // currently passing auth should make no difference (irrespective of MT vs non-MY enb)

        // access to get shared link info - pass user1 (but ignore in non-MT)
        Map<String, String> params = Collections.singletonMap("include", "allowableOperations");
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id, params, 200);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);

        assertEquals(shared1Id, resp.getId());
        assertEquals(fileName1, resp.getName());
        assertEquals("The quick brown fox jumps over the lazy dog", resp.getTitle());
        assertEquals("Pangram, fox, dog, Gym class featuring a brown fox and lazy dog", resp.getDescription());
        assertEquals(d1Id, resp.getNodeId());
        assertNull(resp.getAllowableOperations()); // include is ignored
        assertNull(resp.getAllowableOperationsOnTarget()); // include is ignored

        assertNull(resp.getModifiedByUser().getId()); // userId not returned
        assertEquals(UserInfo.getTestDisplayName(user1), resp.getModifiedByUser().getDisplayName());
        assertNull(resp.getSharedByUser().getId()); // userId not returned
        assertEquals(UserInfo.getTestDisplayName(user2), resp.getSharedByUser().getDisplayName());

        // access to get shared link info - pass user2 (but ignore in non-MT)
        params = Collections.singletonMap("include", "allowableOperations");
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id, params, 200);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);

        assertEquals(shared1Id, resp.getId());
        assertEquals(fileName1, resp.getName());
        assertEquals(d1Id, resp.getNodeId());
        assertNull(resp.getAllowableOperations()); // include is ignored
        assertNull(resp.getAllowableOperationsOnTarget()); // include is ignored


        assertNull(resp.getModifiedByUser().getId()); // userId not returned
        assertEquals(UserInfo.getTestDisplayName(user1), resp.getModifiedByUser().getDisplayName());
        assertNull(resp.getSharedByUser().getId()); // userId not returned
        assertEquals(UserInfo.getTestDisplayName(user2), resp.getSharedByUser().getDisplayName());


        // allowable operations not included - no params
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id, null, 200);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        assertNull(resp.getAllowableOperations());
        assertNull(resp.getAllowableOperationsOnTarget());

        setRequestContext(null);

        // unauth access to get shared link info
        params = Collections.singletonMap("include", "allowableOperations"); // note: this will be ignore for unauth access
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id, params, 200);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);

        assertEquals(shared1Id, resp.getId());
        assertEquals(fileName1, resp.getName());
        assertEquals(d1Id, resp.getNodeId());
        assertNull(resp.getAllowableOperations()); // include is ignored
        assertNull(resp.getAllowableOperationsOnTarget()); // include is ignored


        assertNull(resp.getModifiedByUser().getId()); // userId not returned
        assertEquals(UserInfo.getTestDisplayName(user1), resp.getModifiedByUser().getDisplayName());
        assertNull(resp.getSharedByUser().getId()); // userId not returned
        assertEquals(UserInfo.getTestDisplayName(user2), resp.getSharedByUser().getDisplayName());

        // unauth access to file 1 content (via shared link)
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id + "/content", null, 200);
        assertArrayEquals(file1_originalBytes, response.getResponseAsBytes());
        Map<String, String> responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(file1_MimeType+";charset=UTF-8", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get("Expires"));
        assertEquals("attachment; filename=\"" + fileName1 + "\"; filename*=UTF-8''" + fileName1 + "", responseHeaders.get("Content-Disposition"));
        String lastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(lastModifiedHeader);
        // Test 304 response
        Map<String, String> headers = Collections.singletonMap(IF_MODIFIED_SINCE_HEADER, lastModifiedHeader);
        getSingle(URL_SHARED_LINKS, shared1Id + "/content", null, headers, 304);

        // unauth access to file 1 content (via shared link) - without Content-Disposition header (attachment=false)
        params = new HashMap<>();
        params.put("attachment", "false");
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id + "/content", params, 200);
        assertArrayEquals(file1_originalBytes, response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(file1_MimeType+";charset=UTF-8", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get(LAST_MODIFIED_HEADER));
        assertNotNull(responseHeaders.get("Expires"));
        assertNull(responseHeaders.get("Content-Disposition"));


        // unauth access to file 2 content (via shared link)
        response = getSingle(QuickShareLinkEntityResource.class, shared2Id + "/content", null, 200);
        assertArrayEquals(content2Text.getBytes(), response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(file2_MimeType+";charset=ISO-8859-1", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get("Expires"));
        assertNotNull(responseHeaders.get(LAST_MODIFIED_HEADER));
        assertEquals("attachment; filename=\"" + fileName2 + "\"; filename*=UTF-8''" + fileName2 + "", responseHeaders.get("Content-Disposition"));

        // -ve test - unauth access to get shared link file content - without Content-Disposition header (attachment=false) - header ignored (plain text is not in white list)
        params = new HashMap<>();
        params.put("attachment", "false");
        response = getSingle(QuickShareLinkEntityResource.class, shared2Id + "/content", params, 200);
        assertEquals("attachment; filename=\"" + fileName2 + "\"; filename*=UTF-8''" + fileName2 + "", response.getHeaders().get("Content-Disposition"));

        // -ve shared link rendition tests
        {
            // -ve test - try to get non-existent rendition content
            getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/doclib/content", null, 404);

            // -ve test - try to get unregistered rendition content
            getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/dummy/content", null, 404);
        }

        // unauth access to get rendition info for a shared link (available => CREATED rendition only)
        // -ve shared link rendition tests
        {
            // -ve test - try to get not created rendition for the given shared link
            getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/doclib", null, 404);

            // -ve test - try to get unregistered rendition
            getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/dummy", null, 404);
        }

        // unauth access to get shared link renditions info (available => CREATED renditions only)
        response = getAll(URL_SHARED_LINKS + "/" + shared1Id + "/renditions", null, 200);
        List<Rendition> renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals(0, renditions.size());

        // create rendition of pdf doc - note: for some reason create rendition of txt doc fail on build m/c (TBC) ?
        setRequestContext(user2);

        Rendition rendition = createAndGetRendition(d1Id, "doclib");
        assertNotNull(rendition);
        assertEquals(Rendition.RenditionStatus.CREATED, rendition.getStatus());

        setRequestContext(null);


        // unauth access to get shared link renditions info (available => CREATED renditions only)
        response = getAll(URL_SHARED_LINKS + "/" + shared1Id + "/renditions", null, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals(1, renditions.size());
        assertEquals(Rendition.RenditionStatus.CREATED, renditions.get(0).getStatus());
        assertEquals("doclib", renditions.get(0).getId());

        {
            // try to get a created rendition for the given shared link
            getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/doclib", null, 200);
        }

        // unauth access to get shared link file rendition content
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/doclib/content", null, 200);
        assertTrue(response.getResponseAsBytes().length > 0);
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG+";charset=UTF-8", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get(LAST_MODIFIED_HEADER));
        assertNotNull(responseHeaders.get("Expires"));
        String docName = "doclib";
        assertEquals("attachment; filename=\"" + docName + "\"; filename*=UTF-8''" + docName + "", responseHeaders.get("Content-Disposition"));

        // unauth access to get shared link file rendition content - without Content-Disposition header (attachment=false)
        params = new HashMap<>();
        params.put("attachment", "false");
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/doclib/content", params, 200);
        assertTrue(response.getResponseAsBytes().length > 0);
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG+";charset=UTF-8", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get("Expires"));
        assertNull(responseHeaders.get("Content-Disposition"));
        lastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(lastModifiedHeader);
        // Test 304 response
        headers = Collections.singletonMap(IF_MODIFIED_SINCE_HEADER, lastModifiedHeader);
        getSingle(URL_SHARED_LINKS, shared1Id + "/renditions/doclib/content", null, headers, 304);


        // -ve delete tests
        {
            // -ve test - unauthenticated
            setRequestContext(null);
            deleteSharedLink(shared1Id, 401);

            setRequestContext(user1);

            // -ve test - user1 cannot delete shared link
            deleteSharedLink(shared1Id, 403);

            // -ve test - delete - cannot delete non-existent link
            deleteSharedLink("dummy", 404);
        }


        // -ve create tests
        {
            // As user 1 ...

            // -ve test - try to create again (different user, that has read permission) - already exists
            body = new HashMap<>();
            body.put("nodeId", d1Id);
            post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 409);

            // -ve - create - missing nodeId
            body = new HashMap<>();
            post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 400);

            // -ve - create - unknown nodeId
            body = new HashMap<>();
            body.put("nodeId", "dummy");
            post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 404);

            // -ve - create - try to link to folder (ie. not a file)
            String f1Id = createFolder(myFolderNodeId, "f1 " + RUNID).getId();
            body = new HashMap<>();
            body.put("nodeId", f1Id);
            post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 400);

            // -ve test - cannot create if user does not have permission to read
            setRequestContext(user2);
            body = new HashMap<>();
            body.put("nodeId", d2Id);
            post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 403);

            // -ve test - unauthenticated
            setRequestContext(null);
            body = new HashMap<>();
            body.put("nodeId", d1Id);
            post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 401);
        }


        // delete shared link
        setRequestContext(user2);
        deleteSharedLink(shared1Id);

        // -ve test - delete - cannot delete non-existent link
        setRequestContext(user1);
        deleteSharedLink(shared1Id, 404);

        setRequestContext(user2);

        response = getSingle(NodesEntityResource.class, d1Id, null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);

//        assertEquals(docModifiedAt.getTime(), nodeResp.getModifiedAt().getTime()); // not changed - now can be as metadata extract is async
        assertEquals(docModifiedBy, nodeResp.getModifiedByUser().getId()); // not changed (ie. not user2)


        // -ve get tests
        {
            // try to get link that has been deleted (see above)
            getSingle(QuickShareLinkEntityResource.class, shared1Id, null, 404);
            getSingle(QuickShareLinkEntityResource.class, shared1Id + "/content", null, 404);

            // try to get non-existent link
            getSingle(QuickShareLinkEntityResource.class, "dummy", null, 404);
            getSingle(QuickShareLinkEntityResource.class, "dummy/content", null, 404);
        }

        // TODO if and when these tests are optionally runnable via remote env then we could skip this part of the test
        // (else need to verify test mechanism for enterprise admin via jmx ... etc)

        QuickShareLinksImpl quickShareLinks = applicationContext.getBean("quickShareLinks", QuickShareLinksImpl.class);
        try
        {
            quickShareLinks.setEnabled(false);

            setRequestContext(user1);

            // -ve - disabled service tests
            body.put("nodeId", "dummy");
            post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 501);

            setRequestContext(null);
            getSingle(QuickShareLinkEntityResource.class, "dummy", null, 501);
            getSingle(QuickShareLinkEntityResource.class, "dummy/content", null, 501);

            setRequestContext(user1);
            deleteSharedLink("dummy", 501);
        }
        finally
        {
            quickShareLinks.setEnabled(true);
        }
    }

    /**
     * Tests find shared links to file (content)
     *
     * Note: relies on search service
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links}
     */
    @Test
    @Category({LuceneTests.class, RedundantTests.class})
    public void testSharedLinkFind() throws Exception
    {
        // As user 1 ...
        setRequestContext(user1);
        
        Paging paging = getPaging(0, 100);

        // Get all shared links visible to user 1 (note: for now assumes clean repo)
        HttpResponse response = getAll(URL_SHARED_LINKS, paging, 200);
        List<QuickShareLink> sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(0, sharedLinks.size());
        
        // create doc d1 - in "My" folder
        String myFolderNodeId = getMyNodeId();
        String content1Text = "The quick brown fox jumps over the lazy dog 1.";
        String docName1 = "content" + RUNID + "_1.txt";
        Document doc1 = createTextFile(myFolderNodeId, docName1, content1Text);
        String d1Id = doc1.getId();

        // create doc2 - in "Shared" folder
        String sharedFolderNodeId = getSharedNodeId();
        String docName2 = "content" + RUNID + "_2.txt";
        Document doc2 = createTextFile(sharedFolderNodeId, docName2, content1Text);
        String d2Id = doc2.getId();

        // create shared link to doc 1
        Map<String, String> body = new HashMap<>();
        body.put("nodeId", d1Id);
        response = post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);
        QuickShareLink resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        String shared1Id = resp.getId();

        // As user 2 ...
        setRequestContext(user2);

        // create shared link to doc 2
        body = new HashMap<>();
        body.put("nodeId", d2Id);
        response = post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        String shared2Id = resp.getId();


        //
        // find links
        //

        setRequestContext(user1);

        response = getAll(URL_SHARED_LINKS, paging, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(2, sharedLinks.size());
        assertEquals(shared2Id, sharedLinks.get(0).getId());
        assertEquals(d2Id, sharedLinks.get(0).getNodeId());
        assertEquals(shared1Id, sharedLinks.get(1).getId());
        assertEquals(d1Id, sharedLinks.get(1).getNodeId());

        setRequestContext(user2);

        response = getAll(URL_SHARED_LINKS, paging, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(1, sharedLinks.size());
        assertEquals(shared2Id, sharedLinks.get(0).getId());
        assertEquals(d2Id, sharedLinks.get(0).getNodeId());

        setRequestContext(user1);

        // find my links
        Map<String, String> params = new HashMap<>();
        params.put("where", "("+ QuickShareLinks.PARAM_SHAREDBY+"='"+People.DEFAULT_USER+"')");

        response = getAll(URL_SHARED_LINKS, paging, params, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(1, sharedLinks.size());
        assertEquals(shared1Id, sharedLinks.get(0).getId());
        assertEquals(d1Id, sharedLinks.get(0).getNodeId());

        // find links shared by a given user
        params = new HashMap<>();
        params.put("where", "("+ QuickShareLinks.PARAM_SHAREDBY+"='"+user2+"')");

        response = getAll(URL_SHARED_LINKS, paging, params, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(1, sharedLinks.size());
        assertEquals(shared2Id, sharedLinks.get(0).getId());
        assertEquals(d2Id, sharedLinks.get(0).getNodeId());

        setRequestContext(null);

        // -ve test - unauthenticated
        getAll(URL_SHARED_LINKS, paging, params, 401);


        // delete the shared links
        setRequestContext(user1);
        deleteSharedLink(shared1Id);

        setRequestContext(user2);
        deleteSharedLink(shared2Id);


        // TODO if and when these tests are optionally runnable via remote env then we could skip this part of the test
        // (else need to verify test mechanism for enterprise admin via jmx ... etc)

        setRequestContext(user1);

        QuickShareLinksImpl quickShareLinks = applicationContext.getBean("quickShareLinks", QuickShareLinksImpl.class);
        try
        {
            quickShareLinks.setEnabled(false);

            // -ve - disabled service tests
            getAll(URL_SHARED_LINKS, paging, 501);
        }
        finally
        {
            quickShareLinks.setEnabled(true);
        }
    }

    /**
     * Tests emailing shared links.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>/email}
     */
    @Test
    public void testEmailSharedLink() throws Exception
    {
        setRequestContext(user1);
        
        // Create plain text document
        String myFolderNodeId = getMyNodeId();
        String contentText = "The quick brown fox jumps over the lazy dog.";
        String fileName = "file-" + RUNID + ".txt";
        Document doc = createTextFile(myFolderNodeId, fileName, contentText);
        String docId = doc.getId();

        // Create shared link to document
        Map<String, String> body = Collections.singletonMap("nodeId", docId);
        HttpResponse response = post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);
        QuickShareLink resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        String sharedId = resp.getId();
        assertNotNull(sharedId);
        assertEquals(fileName, resp.getName());

        // Email request with minimal properties
        QuickShareLinkEmailRequest request = new QuickShareLinkEmailRequest();
        request.setClient("share");
        List<String> recipients = new ArrayList<>(2);
        recipients.add(user2 + "@acme.test");
        recipients.add(user2 + "@ping.test");
        request.setRecipientEmails(recipients);
        post(getEmailSharedLinkUrl(sharedId), RestApiUtil.toJsonAsString(request), 202);

        // Email request with all the properties
        request = new QuickShareLinkEmailRequest();
        request.setClient("share");
        request.setMessage("My custom message!");
        request.setLocale(Locale.UK.toString());
        recipients = Collections.singletonList(user2 + "@acme.test");
        request.setRecipientEmails(recipients);
        post(getEmailSharedLinkUrl(sharedId), RestApiUtil.toJsonAsString(request), 202);

        // -ve tests
        // sharedId path parameter does not exist
        post(getEmailSharedLinkUrl(sharedId + System.currentTimeMillis()), RestApiUtil.toJsonAsString(request), 404);

        // Unregistered client
        request = new QuickShareLinkEmailRequest();
        request.setClient("VeryCoolClient" + System.currentTimeMillis());
        List<String> user2Email = Collections.singletonList(user2 + "@acme.test");
        request.setRecipientEmails(user2Email);
        post(getEmailSharedLinkUrl(sharedId), RestApiUtil.toJsonAsString(request), 404);

        // client is mandatory
        request.setClient(null);
        post(getEmailSharedLinkUrl(sharedId), RestApiUtil.toJsonAsString(request), 400);

        // recipientEmails is mandatory
        request.setClient("share");
        request.setRecipientEmails(null);
        post(getEmailSharedLinkUrl(sharedId), RestApiUtil.toJsonAsString(request), 400);

        // TODO if and when these tests are optionally runnable via remote env then we could skip this part of the test
        // (else need to verify test mechanism for enterprise admin via jmx ... etc)
        QuickShareLinksImpl quickShareLinks = applicationContext.getBean("quickShareLinks", QuickShareLinksImpl.class);
        try
        {
            quickShareLinks.setEnabled(false);
            request = new QuickShareLinkEmailRequest();
            request.setClient("share");
            request.setRecipientEmails(user2Email);
            post(getEmailSharedLinkUrl(sharedId), RestApiUtil.toJsonAsString(request), 501);
        }
        finally
        {
            quickShareLinks.setEnabled(true);
        }
    }

    /**
     * Tests shared links to file (content) in a multi-tenant system.
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links}
     *
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>}
     *
     * <p>GET:</p>
     * The following do not require authentication
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>/content}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>/renditions}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>/renditions/<renditionId>}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>/renditions/<renditionId>/content}
     *
     */
    // TODO now covered by testSharedLinkCreateGetDelete ? (since base class now uses tenant context by default)
    @Test
    public void testSharedLinkCreateGetDelete_MultiTenant() throws Exception
    {
        // As user1
        setRequestContext(user1);

        String docLibNodeId = getSiteContainerNodeId(tSiteId, "documentLibrary");
        
        String folderName = "folder" + System.currentTimeMillis() + "_1";
        String folderId = createFolder(docLibNodeId, folderName, null).getId();

        // create doc d1 - pdf
        String fileName1 = "quick" + RUNID + "_1.pdf";
        File file1 = getResourceFile("quick.pdf");

        byte[] file1_originalBytes = Files.readAllBytes(Paths.get(file1.getAbsolutePath()));

        String file1_MimeType = MimetypeMap.MIMETYPE_PDF;

        MultiPartBuilder.MultiPartRequest reqBody = MultiPartBuilder.create()
                    .setFileData(new MultiPartBuilder.FileData(fileName1, file1, file1_MimeType))
                    .build();

        HttpResponse response = post(getNodeChildrenUrl(folderId), reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document doc1 = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
        String d1Id = doc1.getId();
        assertNotNull(d1Id);

        // create shared link to document 1
        Map<String, String> body = new HashMap<>();
        body.put("nodeId", d1Id);
        response = post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);
        QuickShareLink resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        String shared1Id = resp.getId();
        assertNotNull(shared1Id);
        assertEquals(d1Id, resp.getNodeId());
        assertEquals(fileName1, resp.getName());
        assertEquals(file1_MimeType, resp.getContent().getMimeType());
        assertEquals(user1, resp.getSharedByUser().getId());

        // allowable operations not included - no params
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id, null, 200);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        assertNull(resp.getAllowableOperations());

        setRequestContext(null);

        // unauth access to get shared link info
        Map<String, String> params = Collections.singletonMap("include", "allowableOperations"); // note: this will be ignore for unauth access
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id, params, 200);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(shared1Id, resp.getId());
        assertEquals(fileName1, resp.getName());
        assertEquals(d1Id, resp.getNodeId());
        assertNull(resp.getAllowableOperations()); // include is ignored
        assertNull(resp.getAllowableOperationsOnTarget()); // include is ignored


        // unauth access to file 1 content (via shared link)
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id + "/content", null, 200);
        assertArrayEquals(file1_originalBytes, response.getResponseAsBytes());
        Map<String, String> responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(file1_MimeType + ";charset=UTF-8", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get("Expires"));
        assertEquals("attachment; filename=\"" + fileName1 + "\"; filename*=UTF-8''" + fileName1 + "", responseHeaders.get("Content-Disposition"));
        String lastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(lastModifiedHeader);
        // Test 304 response
        Map<String, String> headers = Collections.singletonMap(IF_MODIFIED_SINCE_HEADER, lastModifiedHeader);
        getSingle(URL_SHARED_LINKS, shared1Id + "/content", null, headers, 304);

        // unauth access to file 1 content (via shared link) - without Content-Disposition header (attachment=false)
        params = new HashMap<>();
        params.put("attachment", "false");
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id + "/content", params, 200);
        assertArrayEquals(file1_originalBytes, response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(file1_MimeType + ";charset=UTF-8", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get(LAST_MODIFIED_HEADER));
        assertNotNull(responseHeaders.get("Expires"));
        assertNull(responseHeaders.get("Content-Disposition"));

        // -ve shared link rendition tests
        {
            // -ve test - try to get non-existent rendition content
            getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/doclib/content", null, 404);

            // -ve test - try to get unregistered rendition content
            getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/dummy/content", null, 404);
        }

        // unauth access to get rendition info for a shared link (available => CREATED rendition only)
        // -ve shared link rendition tests
        {
            // -ve test - try to get not created rendition for the given shared link
            getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/doclib", null, 404);

            // -ve test - try to get unregistered rendition
            getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/dummy", null, 404);
        }

        // unauth access to get shared link renditions info (available => CREATED renditions only)
        response = getAll(URL_SHARED_LINKS + "/" + shared1Id + "/renditions", null, 200);
        List<Rendition> renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals(0, renditions.size());

        // create rendition of pdf doc - note: for some reason create rendition of txt doc fail on build m/c (TBC) ?
        setRequestContext(user1);

        Rendition rendition = createAndGetRendition(d1Id, "doclib");
        assertNotNull(rendition);
        assertEquals(Rendition.RenditionStatus.CREATED, rendition.getStatus());

        setRequestContext(null);

        // unauth access to get shared link renditions info (available => CREATED renditions only)
        response = getAll(URL_SHARED_LINKS + "/" + shared1Id + "/renditions", null, 200);
        renditions = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Rendition.class);
        assertEquals(1, renditions.size());
        assertEquals(Rendition.RenditionStatus.CREATED, renditions.get(0).getStatus());
        assertEquals("doclib", renditions.get(0).getId());

        // unauth access to get rendition info for a shared link (available => CREATED rendition only)
        {
            // get a created rendition for the given shared link
            getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/doclib", null, 200);
        }

        // unauth access to get shared link file rendition content
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/doclib/content", null, 200);
        assertTrue(response.getResponseAsBytes().length > 0);
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG + ";charset=UTF-8", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get(LAST_MODIFIED_HEADER));
        assertNotNull(responseHeaders.get("Expires"));
        String docName = "doclib";
        assertEquals("attachment; filename=\"" + docName + "\"; filename*=UTF-8''" + docName + "", responseHeaders.get("Content-Disposition"));

        // unauth access to get shared link file rendition content - without Content-Disposition header (attachment=false)
        params = new HashMap<>();
        params.put("attachment", "false");
        response = getSingle(QuickShareLinkEntityResource.class, shared1Id + "/renditions/doclib/content", params, 200);
        assertTrue(response.getResponseAsBytes().length > 0);
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(MimetypeMap.MIMETYPE_IMAGE_PNG + ";charset=UTF-8", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get("Expires"));
        assertNull(responseHeaders.get("Content-Disposition"));
        lastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(lastModifiedHeader);
        // Test 304 response
        headers = Collections.singletonMap(IF_MODIFIED_SINCE_HEADER, lastModifiedHeader);
        getSingle(URL_SHARED_LINKS, shared1Id + "/renditions/doclib/content", null, headers, 304);
        
        // -ve test - userTwoN1 cannot delete shared link
        setRequestContext(user2);
        deleteSharedLink(shared1Id, 403);

        // -ve test - unauthenticated
        setRequestContext(null);
        deleteSharedLink(shared1Id, 401);

        // delete shared link
        setRequestContext(user1);
        deleteSharedLink(shared1Id);
    }

    /**
     * Tests shared links to file with expiry date.
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links}
     */
    @Test
    public void testSharedLinkWithExpiryDate() throws Exception
    {
        // Clear any hanging security context from other tests.
        // We add it here as getSchedules method will throw an exception.
        AuthenticationUtil.clearCurrentSecurityContext();
        final int numOfSchedules = getSchedules();
        setRequestContext(user1);

        // Create plain text document
        String myFolderNodeId = getMyNodeId();
        String contentText = "The quick brown fox jumps over the lazy dog.";
        String fileName = "file-" + RUNID + ".txt";
        String docId = createTextFile(myFolderNodeId, fileName, contentText).getId();

        // Create shared link to document
        QuickShareLink body = new QuickShareLink();
        body.setNodeId(docId);
        // Invalid time - passed time
        body.setExpiresAt(DateTime.now().minusSeconds(20).toDate());
        post(URL_SHARED_LINKS, RestApiUtil.toJsonAsString(body), 400);

        // The default expiryDate period is DAYS (see: 'system.quickshare.expiry_date.enforce.minimum.period' property),
        // so the expiry date must be at least 1 day from now
        body.setExpiresAt(DateTime.now().plusMinutes(5).toDate());
        post(URL_SHARED_LINKS, RestApiUtil.toJsonAsString(body), 400);

        // Set the expiry date to be in the next 2 days
        Date time = DateTime.now().plusDays(2).toDate();
        body.setExpiresAt(time);
        // Post the share request
        HttpResponse response = post(URL_SHARED_LINKS, RestApiUtil.toJsonAsString(body), 201);
        QuickShareLink resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        assertNotNull(resp.getId());
        assertEquals(fileName, resp.getName());
        assertEquals(time, resp.getExpiresAt());
        // Check that the schedule is persisted
        // Note: No need to check for expiry actions here, as the scheduledPersistedActionService
        // checks that the expiry action is persisted first and if it wasn't will throw an exception.
        assertEquals(numOfSchedules + 1, getSchedules());
        // Delete the shared link
        deleteSharedLink(resp.getId());
        // Check the shred link has been deleted
        getSingle(QuickShareLinkEntityResource.class, resp.getId(), null, 404);
        // As we deleted the shared link, the expiry action and its related schedule should have been removed as well.
        // Check that the schedule is deleted
        assertEquals(numOfSchedules, getSchedules());

        // Set the expiry date to be in the next 24 hours
        time = DateTime.now().plusDays(1).toDate();
        body.setExpiresAt(time);
        // Post the share request
        response = post(URL_SHARED_LINKS, RestApiUtil.toJsonAsString(body), 201);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        assertNotNull(resp.getId());
        // Check that the schedule is persisted
        assertEquals(numOfSchedules + 1, getSchedules());
        // Get the shared link info
        response = getSingle(QuickShareLinkEntityResource.class, resp.getId(), null, 200);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(fileName, resp.getName());
        assertEquals(time, resp.getExpiresAt());
        // Change the expiry time to be in the next 6 seconds.
        // Here we'll bypass the QuickShareService in order to force the new time.
        // As the QuickShareService by default will enforce the expiry date to not be less than 24 hours.
        forceNewExpiryTime(resp.getId(), DateTime.now().plusSeconds(6).toDate());
        // Wait for 10 seconds - the expiry action should be triggered in the next 6 seconds.
        Thread.sleep((10000));
        // Check that the expiry action unshared the link
        getSingle(QuickShareLinkEntityResource.class, resp.getId(), null, 404);
        // The expiry action and its related schedule should have been removed after the link unshared by the action executor.
        // Check that the schedule is deleted
        assertEquals(numOfSchedules, getSchedules());

        // Create a shared link without an expiry date
        body.setExpiresAt(null);
        response = post(URL_SHARED_LINKS, RestApiUtil.toJsonAsString(body), 201);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        assertNotNull(resp.getId());
        assertNull("The 'expiryDate' property should have benn null.", resp.getExpiresAt());
        assertEquals(numOfSchedules, getSchedules());

        // Delete the share link that hasn't got an expiry date
        deleteSharedLink(resp.getId());
    }

    @Override
    public String getScope()
    {
        return "public";
    }

    /**
     * Tests for get /shared-links?include=path
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links?include=path}
     */
    @Test
    @Category({LuceneTests.class, RedundantTests.class})
    public void testGetSharedLinksIncludePath() throws Exception
    {
        String contentText = "includePathTest" + RUNID;
        
        Paging paging = getPaging(0, 100);
        
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("include", "path");
        
        // As user 1: Test the backward compatibility by checking response with and without path is consistent when no shared-links
        setRequestContext(user1);

        // Get all shared links visible to user 1
        HttpResponse response = getAll(URL_SHARED_LINKS, paging, 200);
        List<QuickShareLink> sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        
        // Check that the same no of items is returned with include=path
        response = getAll(URL_SHARED_LINKS, paging, queryParams, 200);
        List<QuickShareLink> sharedLinksWithPath = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals("get /shared-links/ API returns same no of shared-links with or without include=path, when there are no shared-links", sharedLinks, sharedLinksWithPath);
        
        // Create Files in various locations: My Files, SharedFiles, Sites with different visibility

        // Create doc in "My Files"
        Document myFile = createTextFile(getMyNodeId(), "MyFile" + RUNID + ".txt", contentText);

        // Create doc in "Shared" folder
        Document sharedFile = createTextFile(getSharedNodeId(), "SharedFile" + RUNID + ".txt", contentText);
        
        // Create Sites
        Site publicSite = createSite ("TestSite-Public-" + RUNID, SiteVisibility.PUBLIC);
        Site modSite = createSite ("TestSite-Moderate-" + RUNID, SiteVisibility.MODERATED);
        Site privateSite = createSite ("TestSite-Private-" + RUNID, SiteVisibility.PRIVATE);
        
        // Create file in Site Public > DocumentLibrary
        String docLibPub = getSiteContainerNodeId(publicSite.getId(), "documentLibrary");
        Document filePublic = createTextFile(docLibPub, "filePublic.txt", contentText);
        
        // Create files in Site Moderated > DocumentLibrary > Folder 1 and Folder 2
        String docLibMod = getSiteContainerNodeId(modSite.getId(), "documentLibrary");
        Folder folder1 = createFolder(docLibMod, "1");
        Folder folder2 = createFolder(docLibMod, "2");
        Document fileMod = createTextFile(folder1.getId(), "fileMod.txt", contentText);
        Document fileMod2 = createTextFile(folder2.getId(), "fileMod2.txt", contentText);
        
        // Create file in Site Private > DocumentLibrary
        String docLibPvt = getSiteContainerNodeId(privateSite.getId(), "documentLibrary");
        Document filePrivate = createTextFile(docLibPvt, "filePrivate.txt", contentText);
        
        // Share the files above in: My Files, SharedFiles, Sites with different visibility

        String myFileLinkId = postSharedLink(myFile);

        String sharedLinkId = postSharedLink(sharedFile);
  
        String filePublicLinkId = postSharedLink(filePublic);

        String fileModLinkId = postSharedLink(fileMod);

        String fileMod2LinkId = postSharedLink(fileMod2);

        String filePrivateLinkId = postSharedLink(filePrivate);
        
        // Grant user2: Consumer Permission for Moderated Site > File1
        List<NodePermissions.NodePermission> locallySetPermissions = new ArrayList<>();
        locallySetPermissions.add(new NodePermissions.NodePermission(user2, PermissionService.CONSUMER, AccessStatus.ALLOWED.toString()));
        
        NodePermissions nodePermissions = new NodePermissions();
        nodePermissions.setIsInheritanceEnabled(false);
        nodePermissions.setLocallySet(locallySetPermissions);
        
        Document docPermissions = new Document();
        docPermissions.setPermissions(nodePermissions);

        put(URL_NODES, fileMod.getId(), toJsonAsStringNonNull(docPermissions), null, 200);
        
        // Grant user2: Consumer Permission for Moderated Site > Folder 2, File2
        put(URL_NODES, fileMod2.getId(), toJsonAsStringNonNull(docPermissions), null, 200);
        
        Folder folderPermissions = new Folder();
        folderPermissions.setPermissions(nodePermissions);
        put(URL_NODES, folder2.getId(), toJsonAsStringNonNull(folderPermissions), null, 200);
        
        // Get links For User1
        setRequestContext(user1);
        
        response = getSingle(QuickShareLinkEntityResource.class, myFileLinkId, null, 200);
        QuickShareLink link = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        assertNull("get /shared-links/<id> API does not return Path info by default", link.getPath());
        
        // Path info is not included for get shared-links/<id>
        response = getSingle(QuickShareLinkEntityResource.class, myFileLinkId, queryParams, 200);
        link = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        assertNull("get /shared-links/<id> API ignores Path info when requested as it is a noAuth API.", link.getPath());
        
        response = getAll(URL_SHARED_LINKS, paging, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals("API returns correct shared-links as expected: without path info", 6, sharedLinks.size());
        sharedLinks.forEach(sharedLink -> assertNull("API does not return Path info for any shared-links by default", sharedLink.getPath()));
        
        // Path info is included for get shared-links when requested
        response = getAll(URL_SHARED_LINKS, paging, queryParams, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        
        // Complete path info is retrieved for the user with access to the complete path
        assertEquals("API returns correct shared-links as expected: with path info", 6, sharedLinks.size());
        sharedLinks.forEach(sharedLink -> assertTrue("API returns Complete Path info for each link when requested by content owner", sharedLink.getPath().getIsComplete()));

        // Get links For User2
        setRequestContext(user2);

        response = getAll(URL_SHARED_LINKS, paging, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        
        // Path info is not included when not requested
        assertEquals("API returns correct shared-links as expected for user2: without path info", 4, sharedLinks.size());
        sharedLinks.forEach(sharedLink -> assertNull("get /shared-links/ API does not return Path info for any shared-links by default", sharedLink.getPath()));
        
        response = getAll(URL_SHARED_LINKS, paging, queryParams, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        
        // Path info is retrieved for the user with access to the complete path: Sorted as LIFO
        assertEquals("API returns correct shared-links as expected for user2: with path info", 4, sharedLinks.size());
        sharedLinks.forEach(sharedLink -> assertNotNull("API returns Path info for each link when requested by user2", sharedLink.getPath()));
        
        // Moderated Site > fileMod2: Path only includes elements where user2 has access
        QuickShareLink sharedLink = sharedLinks.get(0);
        assertEquals("Incorrect sort order or SharedLink ID for fileMod2: " + sharedLink, fileMod2LinkId, sharedLink.getId());
        
        PathInfo path = sharedLink.getPath();
        assertEquals("Complete Path is returned even when user2 does not have appropriate permissions. SharedLink Path: " + path, false, path.getIsComplete());
        assertEquals("Path omits immediate Parent folder Name when user has access to it. SharedLink Path: " + path, "/" + folder2.getName(), path.getName());
        assertEquals("Path omits immediate Parent folder ID when user has access to it. SharedLink Path: " + path, folder2.getId(), path.getElements().get(0).getId());
        
        // Moderated Site > fileMod: Path empty when user2 does not have access to the immediate parent
        sharedLink = sharedLinks.get(1);
        assertEquals("Incorrect sort order or SharedLink ID for fileMod: " + sharedLink, fileModLinkId, sharedLink.getId());
        path = sharedLink.getPath();
        
        assertNotNull("Path info is not included in the response when user does not have right permissions. SharedLink Path: " + path, path);
        assertNull("Path Name is returned when user does not have right permissions. SharedLink Path: " + path, path.getName());
        assertNull("Path info is returned when user does not have right permissions. SharedLink Path: " + path, path.getIsComplete());
        assertNull("Path Elements are returned when user does not have right permissions. SharedLink Path: " + path, path.getElements());
       
        // Public Site > filePublic: Path includes all the elements when user2 has appropriate access
        sharedLink = sharedLinks.get(2);
        assertEquals("Incorrect sort order or SharedLink ID for filePublic: " + sharedLink, filePublicLinkId, sharedLink.getId());
        
        path = sharedLink.getPath();
        
        assertEquals("Complete Path is not returned for user2 for public files. SharedLink Path: " + path, true, path.getIsComplete());
        assertEquals("Incorrect Path Name for Public Site Files. SharedLink Path: " + path, "/Company Home/Sites/" + publicSite.getId() + "/documentLibrary", path.getName());
        assertEquals("Incorrect Path Elements for Public Site Files. SharedLink Path: " + path, 4, path.getElements().size());
        assertEquals("Incorrect ID in the Path for Company Home. SharedLink Path: " + path, getRootNodeId(), path.getElements().get(0).getId());
        assertEquals("Incorrect ID in the Path for Public Site. SharedLink Path: " + path, publicSite.getGuid(), path.getElements().get(2).getId());
        assertEquals("Incorrect ID in the Path for Public Site DocLib. SharedLink Path: " + path, docLibPub, path.getElements().get(3).getId());
        
        // Shared Files > shared: Path includes all the elements when user2 has appropriate access
        sharedLink = sharedLinks.get(3);
        assertEquals("Incorrect sort order or SharedLink ID for sharedFiles: " + sharedLink, sharedLinkId, sharedLink.getId());
        
        path = sharedLink.getPath();
        
        assertEquals("Complete Path is not returned for user2 for shared files. SharedLink Path: " + path, true, path.getIsComplete());
        assertEquals("Incorrect Path Name for Shared Files. SharedLink Path: " + path, "/Company Home/Shared", path.getName());
        assertEquals("Incorrect Path Elements for Shared Files. SharedLink Path: " + path, 2, path.getElements().size());
        assertEquals("Incorrect ID in the Path for Company Home. SharedLink Path: " + path, getRootNodeId(), path.getElements().get(0).getId());
        assertEquals("Incorrect ID in the path for Shared Files. SharedLink Path: " + path, getSharedNodeId(), path.getElements().get(1).getId());

        // Unauthorized request returns 401
        setRequestContext(null, "UserNotKnown", DEFAULT_ADMIN_PWD);
        queryParams = new HashMap<>();
        getAll(URL_SHARED_LINKS, paging, queryParams, 401);

        // Unauthenticated request returns 401
        setRequestContext(user2, null, null);
        getAll(URL_SHARED_LINKS, paging, queryParams, 401);

        // Delete the shared links
        setRequestContext(user1);
        deleteSharedLink(myFileLinkId);
        deleteSharedLink(sharedLinkId);
        deleteSharedLink(filePublicLinkId);
        deleteSharedLink(fileModLinkId);
        deleteSharedLink(fileMod2LinkId);
        deleteSharedLink(filePrivateLinkId);
    }

    /**
     * Tests create shared-links with 'include' parameter.
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links?include=path}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links?include=allowableOperations}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links?include=path,allowableOperations}
     */
    @Test
    @Category({LuceneTests.class, RedundantTests.class})
    public void testCreateSharedLinkWithIncludeParam() throws Exception
    {
        String content = "The quick brown fox jumps over the lazy dog.";
        String fileName1 = "fileOne_" + RUNID + ".txt";
        String fileName2 = "fileTwo_" + RUNID + ".txt";
        String fileName3 = "fileThree_" + RUNID + ".txt";

        // As user 1 create 3 text files in -my- folder (i.e. User's Home)
        setRequestContext(user1);
        String doc1Id = createTextFile(getMyNodeId(), fileName1, content).getId();
        String doc2Id = createTextFile(getMyNodeId(), fileName2, content).getId();
        String doc3Id = createTextFile(getMyNodeId(), fileName3, content).getId();

        // Share the 'fileName1' doc and use the query parameter 'include=path' to return path information
        QuickShareLink body = new QuickShareLink();
        body.setNodeId(doc1Id);

        HttpResponse response = post(URL_SHARED_LINKS, RestApiUtil.toJsonAsString(body), "?include=path", 201);
        QuickShareLink quickShareLinkResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        PathInfo pathInfo = quickShareLinkResponse.getPath();
        assertNotNull("API returns Path info when requested upon creation.", pathInfo);
        assertTrue("IsComplete should have been true for user1.", pathInfo.getIsComplete());
        assertEquals("Incorrect number of path elements.", 3, pathInfo.getElements().size());
        assertEquals("Incorrect path name.", "/Company Home/User Homes/" + user1, pathInfo.getName());
        assertEquals("Incorrect path element.", getRootNodeId(), pathInfo.getElements().get(0).getId());
        assertEquals("Incorrect path element.", "Company Home", pathInfo.getElements().get(0).getName());
        assertEquals("Incorrect path element", "User Homes", pathInfo.getElements().get(1).getName());
        assertEquals("Incorrect path element.", getMyNodeId(), pathInfo.getElements().get(2).getId());
        assertEquals("Incorrect path element.", user1, pathInfo.getElements().get(2).getName());

        // Share the 'fileName2' doc and use the query parameter 'include=allowableOperations' to return allowableOperations information
        body.setNodeId(doc2Id);
        response = post(URL_SHARED_LINKS, RestApiUtil.toJsonAsString(body), "?include=allowableOperations", 201);
        quickShareLinkResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        List<String> allowableOperations = quickShareLinkResponse.getAllowableOperations();
        assertNotNull("'allowableOperations' should have been returned.", allowableOperations);
        assertEquals("allowableOperations should only have 'Delete' as allowable operation.", 1, allowableOperations.size());
        assertEquals("Incorrect allowable operation.", "delete", allowableOperations.get(0));

        // Share the 'fileName3' doc and use the query parameter 'include=path,allowableOperations' to return path and allowableOperations information
        body.setNodeId(doc3Id);
        response = post(URL_SHARED_LINKS, RestApiUtil.toJsonAsString(body), "?include=path,allowableOperations", 201);
        quickShareLinkResponse = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        // Check Path info
        pathInfo = quickShareLinkResponse.getPath();
        assertNotNull("'path' should have been returned.", pathInfo);
        assertTrue("IsComplete should have been true for user1.", pathInfo.getIsComplete());
        assertEquals("Incorrect number of path elements.", 3, pathInfo.getElements().size());
        assertEquals("Incorrect path name.", "/Company Home/User Homes/" + user1, pathInfo.getName());

        // Check allowableOperations (i.e. the shared link)
        allowableOperations = quickShareLinkResponse.getAllowableOperations();
        assertNotNull("'allowableOperations' should have been returned.", allowableOperations);
        assertEquals("allowableOperations should only have 'Delete' as allowable operation.", 1, allowableOperations.size());
        assertEquals("Incorrect allowable operation.", "delete", allowableOperations.get(0));
        
        // Check allowableOperationsOnTarget (i.e. for the actual file being shared)
        allowableOperations = quickShareLinkResponse.getAllowableOperationsOnTarget();
        assertNotNull("'allowableOperationsOnTarget' should have been returned.", allowableOperations);
        Collection<String> expectedOps = Arrays.asList("delete", "update", "updatePermissions");
        assertTrue(allowableOperations.containsAll(expectedOps));
        assertEquals(expectedOps.size(), allowableOperations.size());
        assertEquals("Incorrect allowable operation.", "delete", allowableOperations.get(0));

        // Test that listing shared links also support the include parameter.
        Paging paging = getPaging(0, 100);
        response = getAll(URL_SHARED_LINKS, paging, Collections.singletonMap("include", "path,allowableOperations"), 200);
        List<QuickShareLink> sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals("Incorrect number of shared-links returned.", 3, sharedLinks.size());
        sharedLinks.forEach(sharedLink ->
        {
            // Check Path info
            PathInfo path = sharedLink.getPath();
            assertNotNull("'path' should have been returned.", path);
            assertTrue("IsComplete should have been true for user1.", path.getIsComplete());
            assertEquals("Incorrect number of path elements.", 3, path.getElements().size());
            assertEquals("Incorrect path name.", "/Company Home/User Homes/" + user1, path.getName());

            // Check allowableOperations
            List<String> operations = sharedLink.getAllowableOperations();
            assertNotNull("'allowableOperations' should have been returned.", operations);
            assertEquals("allowableOperations should only have 'Delete' as allowable operation.", 1, operations.size());
            assertEquals("Incorrect allowable operation.", "delete", operations.get(0));

            // Check allowableOperationsOnTarget (i.e. for the actual file being shared)
            operations = sharedLink.getAllowableOperationsOnTarget();
            assertNotNull("'allowableOperationsOnTarget' should have been returned.", operations);
            assertTrue(operations.containsAll(expectedOps));
            assertEquals(expectedOps.size(), operations.size());
            assertEquals("Incorrect allowable operation.", "delete", operations.get(0));

            // Quick check that some extended info is present. 
            assertEquals("The quick brown fox jumps over the lazy dog", sharedLink.getTitle());
            assertEquals("Pangram, fox, dog, Gym class featuring a brown fox and lazy dog", sharedLink.getDescription());
        });
    }


    @Test
    public void testSharedLinkFindIncludeNodeProperties() throws Exception
    {
        QuickShareLinksImpl quickShareLinks = applicationContext.getBean("quickShareLinks", QuickShareLinksImpl.class);
        ServiceDescriptorRegistry serviceRegistry = applicationContext.getBean("ServiceRegistry", ServiceDescriptorRegistry.class);
        SearchService mockSearchService = mock(SearchService.class);
        serviceRegistry.setMockSearchService(mockSearchService);

        // As user 1 ...
        setRequestContext(user1);

        Paging paging = getPaging(0, 100);

        String content = "The quick brown fox jumps over the lazy dog.";
        String fileName1 = "fileOne_" + RUNID + ".txt";
        String fileName2 = "fileTwo_" + RUNID + ".txt";

        // As user 1 create 2 text files in -my- folder (i.e. User's Home)
        setRequestContext(user1);
        Map<String, String> file1Props = new HashMap<>();
        file1Props.put("cm:title", "File one title");
        file1Props.put("cm:lastThumbnailModification", "doclib:1549351708998");
        String file1Id = createTextFile(getMyNodeId(), fileName1, content,"UTF-8", file1Props).getId();
        String file2Id = createTextFile(getMyNodeId(), fileName2, content).getId();

        // Create shared links to file 1 and 2
        QuickShareLink body = new QuickShareLink();
        body.setNodeId(file1Id);
        HttpResponse  response = post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);
        QuickShareLink resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        String shared1Id = resp.getId();

        body = new QuickShareLink();
        body.setNodeId(file2Id);
        post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);

        // Lock text file 1
        LockInfo lockInfo = new LockInfo();
        lockInfo.setTimeToExpire(60);
        lockInfo.setType("FULL");
        lockInfo.setLifetime("PERSISTENT");
        post(getNodeOperationUrl(file1Id, "lock"), toJsonAsStringNonNull(lockInfo), null, 200);

        // Find shared links without include=properties
        ResultSet mockResultSet = mockResultSet(Arrays.asList(file1Id, file2Id));
        when(mockSearchService.query(any())).thenReturn(mockResultSet);
        quickShareLinks.afterPropertiesSet();

        response = getAll(URL_SHARED_LINKS, paging, null, 200);
        List<QuickShareLink> sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(2, sharedLinks.size());
        QuickShareLink resQuickShareLink1 = sharedLinks.get(0);
        QuickShareLink resQuickShareLink2 = sharedLinks.get(1);

        assertNull("Properties were not requested therefore they should not be included", resQuickShareLink1.getProperties());
        assertNull("Properties were not requested therefore they should not be included", resQuickShareLink2.getProperties());

        // Find the shared links with include=properties
        mockResultSet = mockResultSet(Arrays.asList(file1Id, file2Id));
        when(mockSearchService.query(any())).thenReturn(mockResultSet);
        quickShareLinks.afterPropertiesSet();

        Map<String, String> params = new HashMap<>();
        params.put("include", "properties,allowableOperations");
        response = getAll(URL_SHARED_LINKS, paging, params, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(2, sharedLinks.size());
        resQuickShareLink1 = sharedLinks.get(0);

        // Check the 1st shared link and properties (properties should include a title, lastThumbnailModification and lock info)
        assertEquals(shared1Id, resQuickShareLink1.getId());
        assertEquals(file1Id, resQuickShareLink1.getNodeId());
        Map<String, Object> resQuickShareLink1Props =  resQuickShareLink1.getProperties();
        assertNotNull("Properties were requested to be included but are null.", resQuickShareLink1Props);
        assertNotNull("Properties should include cm:lockType", resQuickShareLink1Props.get("cm:lockType"));
        assertNotNull("Properties should include cm:lockOwner", resQuickShareLink1Props.get("cm:lockOwner"));
        assertNotNull("Properties should include cm:lockLifetime", resQuickShareLink1Props.get("cm:lockLifetime"));
        assertNotNull("Properties should include cm:title", resQuickShareLink1Props.get("cm:title"));
        assertNotNull("Properties should include cm:versionType", resQuickShareLink1Props.get("cm:versionType"));
        assertNotNull("Properties should include cm:versionLabel", resQuickShareLink1Props.get("cm:versionLabel"));
        assertNotNull("Properties should include cm:lastThumbnailModification", resQuickShareLink1Props.get("cm:lastThumbnailModification"));
        // Properties that should be excluded
        assertNull("Properties should NOT include cm:name", resQuickShareLink1Props.get("cm:name"));
        assertNull("Properties should NOT include qshare:sharedBy", resQuickShareLink1Props.get("qshare:sharedBy"));
        assertNull("Properties should NOT include qshare:sharedId", resQuickShareLink1Props.get("qshare:sharedId"));
        assertNull("Properties should NOT include cm:content", resQuickShareLink1Props.get("cm:content"));
        assertNull("Properties should NOT include cm:created", resQuickShareLink1Props.get("cm:created"));
        assertNull("Properties should NOT include cm:creator", resQuickShareLink1Props.get("cm:creator"));
        assertNull("Properties should NOT include cm:modifier", resQuickShareLink1Props.get("cm:modifier"));
        assertNull("Properties should NOT include cm:modified", resQuickShareLink1Props.get("cm:modified"));
        assertNull("Properties should NOT include cm:autoVersion", resQuickShareLink1Props.get("cm:autoVersion"));
        assertNull("Properties should NOT include cm:initialVersion", resQuickShareLink1Props.get("cm:initialVersion"));
        assertNull("Properties should NOT include cm:autoVersionOnUpdateProps", resQuickShareLink1Props.get("cm:autoVersionOnUpdateProps"));
        // System properties should be excluded
        boolean foundSysProp = resQuickShareLink1Props.keySet().stream().anyMatch( (s) -> s.startsWith("sys:"));
        assertFalse("System properties should be excluded", foundSysProp);

        serviceRegistry.setMockSearchService(null);
        quickShareLinks.afterPropertiesSet();
    }

    @Test
    public void testSharedLinkFindIncludeIsFavorite() throws Exception
    {
        PublicApiClient.Favourites favouritesProxy = publicApiClient.favourites();
        QuickShareLinksImpl quickShareLinks = applicationContext.getBean("quickShareLinks", QuickShareLinksImpl.class);
        ServiceDescriptorRegistry serviceRegistry = applicationContext.getBean("ServiceRegistry", ServiceDescriptorRegistry.class);
        SearchService mockSearchService = mock(SearchService.class);
        serviceRegistry.setMockSearchService(mockSearchService);

        // As user 1 ...
        setRequestContext(user1);

        Paging paging = getPaging(0, 100);

        String content = "The quick brown fox jumps over the lazy dog.";
        String fileName1 = "fileOne_" + RUNID + ".txt";
        String fileName2 = "fileTwo_" + RUNID + ".txt";

        // As user 1 create 2 text files in -my- folder (i.e. User's Home)
        setRequestContext(user1);
        String file1Id = createTextFile(getMyNodeId(), fileName1, content).getId();
        String file2Id = createTextFile(getMyNodeId(), fileName2, content).getId();

        // Create shared links to file 1 and 2
        QuickShareLink body = new QuickShareLink();
        body.setNodeId(file1Id);
        post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);

        body = new QuickShareLink();
        body.setNodeId(file2Id);
        post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);

        // Favourite file with file1Id file as user 1
        Favourite file1Favourite = makeFileFavourite(file1Id);
        favouritesProxy.createFavourite(user1, file1Favourite, null);


        // Find shared links without include=isFavorite
        ResultSet mockResultSet = mockResultSet(Arrays.asList(file1Id, file2Id));
        when(mockSearchService.query(any())).thenReturn(mockResultSet);
        quickShareLinks.afterPropertiesSet();

        HttpResponse  response = response = getAll(URL_SHARED_LINKS, paging, null, 200);
        List<QuickShareLink> sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(2, sharedLinks.size());
        QuickShareLink resQuickShareLink1 = sharedLinks.get(0);
        QuickShareLink resQuickShareLink2 = sharedLinks.get(1);

        assertNull("isFavorite was not requested therefore it should not be included", resQuickShareLink1.getIsFavorite());
        assertNull("isFavorite was not requested therefore it should not be included", resQuickShareLink2.getIsFavorite());

        // Find shared links with include=isFavorite
        mockResultSet = mockResultSet(Arrays.asList(file1Id, file2Id));
        when(mockSearchService.query(any())).thenReturn(mockResultSet);
        quickShareLinks.afterPropertiesSet();

        Map<String, String> params = new HashMap<>();
        params.put("include", "isFavorite");
        response = getAll(URL_SHARED_LINKS, paging, params, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(2, sharedLinks.size());
        resQuickShareLink1 = sharedLinks.get(0);
        resQuickShareLink2 = sharedLinks.get(1);

        assertTrue("Document should be marked as favorite.",resQuickShareLink1.getIsFavorite());
        assertFalse("Document should not be marked as favorite.", resQuickShareLink2.getIsFavorite());

        serviceRegistry.setMockSearchService(null);
        quickShareLinks.afterPropertiesSet();
    }

    @Test
    public void testSharedLinkFindIncludeAspects() throws Exception
    {
        PublicApiClient.Favourites favouritesProxy = publicApiClient.favourites();
        QuickShareLinksImpl quickShareLinks = applicationContext.getBean("quickShareLinks", QuickShareLinksImpl.class);
        ServiceDescriptorRegistry serviceRegistry = applicationContext.getBean("ServiceRegistry", ServiceDescriptorRegistry.class);
        SearchService mockSearchService = mock(SearchService.class);
        serviceRegistry.setMockSearchService(mockSearchService);

        // As user 1 ...
        setRequestContext(user1);

        Paging paging = getPaging(0, 100);

        String content = "The quick brown fox jumps over the lazy dog.";
        String fileName1 = "fileOne_" + RUNID + ".txt";
        String fileName2 = "fileTwo_" + RUNID + ".txt";

        // As user 1 create 2 text files in -my- folder (i.e. User's Home)
        setRequestContext(user1);
        String file1Id = createTextFile(getMyNodeId(), fileName1, content).getId();
        String file2Id = createTextFile(getMyNodeId(), fileName2, content).getId();

        // Create shared links to file 1 and 2
        QuickShareLink body = new QuickShareLink();
        body.setNodeId(file1Id);
        post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);

        body = new QuickShareLink();
        body.setNodeId(file2Id);
        post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);

        // Favourite file with file1Id file as user 1
        Favourite file1Favourite = makeFileFavourite(file1Id);
        favouritesProxy.createFavourite(user1, file1Favourite, null);


        // Find shared links without include=isFavorite
        ResultSet mockResultSet = mockResultSet(Arrays.asList(file1Id, file2Id));
        when(mockSearchService.query(any())).thenReturn(mockResultSet);
        quickShareLinks.afterPropertiesSet();

        HttpResponse  response = getAll(URL_SHARED_LINKS, paging, null, 200);
        List<QuickShareLink> sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(2, sharedLinks.size());
        QuickShareLink resQuickShareLink1 = sharedLinks.get(0);
        QuickShareLink resQuickShareLink2 = sharedLinks.get(1);

        assertNull("aspectNames was not requested therefore it should not be included", resQuickShareLink1.getAspectNames());
        assertNull("aspectNames was not requested therefore it should not be included", resQuickShareLink2.getAspectNames());

        // Find shared links with include=isFavorite
        mockResultSet = mockResultSet(Arrays.asList(file1Id, file2Id));
        when(mockSearchService.query(any())).thenReturn(mockResultSet);
        quickShareLinks.afterPropertiesSet();

        Map<String, String> params = new HashMap<>();
        params.put("include", "aspectNames");
        response = getAll(URL_SHARED_LINKS, paging, params, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(2, sharedLinks.size());
        resQuickShareLink1 = sharedLinks.get(0);
        resQuickShareLink2 = sharedLinks.get(1);

        assertNotNull("aspectNames was not requested therefore it should not be included", resQuickShareLink1.getAspectNames());
        assertNotNull("aspectNames was not requested therefore it should not be included", resQuickShareLink2.getAspectNames());

        serviceRegistry.setMockSearchService(null);
        quickShareLinks.afterPropertiesSet();
    }

    // A one time use ResultSet of nodes with specified node ids
    private static ResultSet mockResultSet(List<String> nodeIds)
    {
        List<ResultSetRow> resultSetRows = new LinkedList<>();
        for (String nodeId : nodeIds)
        {
            ResultSetRow mockResultSetRow1 = mock(ResultSetRow.class);
            when(mockResultSetRow1.getNodeRef()).thenReturn(new NodeRef("workspace","SpacesStore" ,nodeId));
            resultSetRows.add(mockResultSetRow1);
        }

        ResultSet mockSearchServiceQueryResultSet = mock(ResultSet.class);
        when(mockSearchServiceQueryResultSet.hasMore()).thenReturn(false);
        when(mockSearchServiceQueryResultSet.getNumberFound()).thenReturn(Long.valueOf(nodeIds.size()));
        when(mockSearchServiceQueryResultSet.length()).thenReturn(nodeIds.size());
        when(mockSearchServiceQueryResultSet.iterator()).thenReturn(resultSetRows.iterator());
        return mockSearchServiceQueryResultSet;
    }

    private static Favourite makeFileFavourite(String targetGuid) throws ParseException
    {
        FavouriteDocument document = new FavouriteDocument(targetGuid);
        FileFavouriteTarget target = new FileFavouriteTarget(document);
        Date creationData = new Date();
        Favourite favourite = new Favourite(creationData, null, target, null);
        return favourite;
    }

    private String getEmailSharedLinkUrl(String sharedId)
    {
        return URL_SHARED_LINKS + '/' + sharedId + "/email";
    }
    
    private String postSharedLink(Document file)
    {
        Map<String, String> body = new HashMap<>();
        body.put("nodeId", file.getId());
        HttpResponse response;
        try
        {
            response = post(URL_SHARED_LINKS, toJsonAsStringNonNull(body), 201);
            QuickShareLink resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
            return resp.getId();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error sharing link for File: " + file.getId(), e);
        }
    }

    private void deleteSharedLink(String sharedId) throws Exception
    {
        deleteSharedLink(sharedId, 204);
    }

    private void deleteSharedLink(String sharedId, int expectedStatus) throws Exception
    {
        delete(URL_SHARED_LINKS, sharedId, expectedStatus);
    }

    private void forceNewExpiryTime(String sharedId, Date date)
    {
        TenantUtil.runAsSystemTenant(() -> {
            // Load the expiry action and attach the schedule
            QuickShareLinkExpiryAction linkExpiryAction = quickShareLinkExpiryActionPersister
                        .loadQuickShareLinkExpiryAction(QuickShareLinkExpiryActionImpl.createQName(sharedId));
            linkExpiryAction.setSchedule(scheduledPersistedActionService.getSchedule(linkExpiryAction));
            linkExpiryAction.setScheduleStart(date);

            // save the expiry action and the schedule
            transactionHelper.doInTransaction(() -> {
                quickShareLinkExpiryActionPersister.saveQuickShareLinkExpiryAction(linkExpiryAction);
                scheduledPersistedActionService.saveSchedule(linkExpiryAction.getSchedule());
                return null;
            });

            return null;
        }, TenantUtil.getCurrentDomain());
    }

    private int getSchedules()
    {
        return TenantUtil.runAsSystemTenant(() -> scheduledPersistedActionService.listSchedules().size(), TenantUtil.getCurrentDomain());
    }
}
