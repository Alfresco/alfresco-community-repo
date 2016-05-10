/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.tests;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.QuickShareLinks;
import org.alfresco.rest.api.impl.QuickShareLinksImpl;
import org.alfresco.rest.api.model.QuickShareLink;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.quicksharelinks.QuickShareLinkEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.QuickShareLinkEmailRequest;
import org.alfresco.rest.api.tests.client.data.Rendition;
import org.alfresco.rest.api.tests.util.MultiPartBuilder;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.*;

/**
 * API tests for:
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links} </li>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>} </li>
 * </ul>
 *
 * @author janv
 */
public class SharedLinkApiTest extends AbstractBaseApiTest
{
    private static final String URL_SHARED_LINKS = "shared-links";

    private String user1;
    private String user2;
    private List<String> users = new ArrayList<>();

    protected MutableAuthenticationService authenticationService;
    protected PersonService personService;

    private final String RUNID = System.currentTimeMillis()+"";

    @Before
    public void setup() throws Exception
    {
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        personService = applicationContext.getBean("personService", PersonService.class);

        // note: createUser currently relies on repoService
        user1 = createUser("user1-" + RUNID);
        user2 = createUser("user2-" + RUNID);

        // We just need to clean the on-premise-users,
        // so the tests for the specific network would work.
        users.add(user1);
        users.add(user2);
    }

    @After
    public void tearDown() throws Exception
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        for (final String user : users)
        {
            transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    if (personService.personExists(user))
                    {
                        authenticationService.deleteAuthentication(user);
                        personService.deletePerson(user);
                    }
                    return null;
                }
            });
        }
        users.clear();
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * Tests shared links to file (content)
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>/content}
     *
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/shared-links/<sharedId>}
     */
    @Test
    public void testSharedLinkCreateGetDelete() throws Exception
    {
        // As user 1 ...

        // create doc d1 - plain text
        String sharedFolderNodeId = getSharedNodeId(user1);
        String content1Text = "The quick brown fox jumps over the lazy dog 1.";
        String fileName1 = "content" + RUNID + "_1.txt";
        Document doc1 = createTextFile(user1, sharedFolderNodeId, fileName1, content1Text);
        String d1Id = doc1.getId();

        // create doc d2 - pdf
        String myFolderNodeId = getMyNodeId(user1);

        String fileName2 = "quick"+RUNID+"_2.pdf";
        File file = getResourceFile("quick.pdf");
        byte[] file2_originalBytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));

        String file2_MimeType = MimetypeMap.MIMETYPE_PDF;

        MultiPartBuilder multiPartBuilder = MultiPartBuilder.create()
                .setFileData(new MultiPartBuilder.FileData(fileName2, file, file2_MimeType));
        MultiPartBuilder.MultiPartRequest reqBody = multiPartBuilder.build();

        HttpResponse response = post(getNodeChildrenUrl(myFolderNodeId), user1, reqBody.getBody(), null, reqBody.getContentType(), 201);
        Document doc2 = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        String d2Id = doc2.getId();

        // As user 2 ...

        response = getSingle(NodesEntityResource.class, user2, d1Id, null, 200);
        Node nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        Date docModifiedAt = nodeResp.getModifiedAt();
        String docModifiedBy = nodeResp.getModifiedByUser().getId();
        assertEquals(user1, docModifiedBy);

        // create shared link to document 1
        Map<String, String> body = new HashMap<>();
        body.put("nodeId", d1Id);

        response = post(URL_SHARED_LINKS, user2, toJsonAsStringNonNull(body), 201);
        QuickShareLink resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);

        String shared1Id = resp.getId();
        assertNotNull(shared1Id);

        assertEquals(d1Id, resp.getNodeId());
        assertEquals(fileName1, resp.getName());

        String file1_MimeType = MimetypeMap.MIMETYPE_TEXT_PLAIN;
        assertEquals(file1_MimeType, resp.getContent().getMimeType());
        assertEquals("Plain Text", resp.getContent().getMimeTypeName());

        assertEquals(new Long(content1Text.length()), resp.getContent().getSizeInBytes());
        assertEquals("UTF-8", resp.getContent().getEncoding());

        assertEquals(docModifiedAt.getTime(), resp.getModifiedAt().getTime()); // not changed
        assertEquals(docModifiedBy, resp.getModifiedByUser().getId()); // not changed (ie. not user2)
        assertEquals(docModifiedBy+" "+docModifiedBy, resp.getModifiedByUser().getDisplayName());

        assertEquals(user2, resp.getSharedByUser().getId());
        assertEquals(user2+" "+user2, resp.getSharedByUser().getDisplayName());

        // -ve test - try to create again (same user) - already exists
        post(URL_SHARED_LINKS, user2, toJsonAsStringNonNull(body), 409);


        // As user 1 ...

        // create shared link to document 2
        body = new HashMap<>();
        body.put("nodeId", d2Id);

        response = post(URL_SHARED_LINKS, user1, toJsonAsStringNonNull(body), 201);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        String shared2Id = resp.getId();


        // currently passing auth should make no difference (irrespective of MT vs non-MY enb)

        // access to get shared link info - pass user1 (but ignore in non-MT)
        Map<String, String> params = Collections.singletonMap("include", "allowableOperations");
        response = getSingle(QuickShareLinkEntityResource.class, user1, shared1Id, params, 200);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);

        assertEquals(shared1Id, resp.getId());
        assertEquals(fileName1, resp.getName());
        assertEquals(d1Id, resp.getNodeId());
        assertNull(resp.getAllowableOperations()); // include is ignored

        assertNull(resp.getModifiedByUser().getId()); // userId not returned
        assertEquals(user1+" "+user1, resp.getModifiedByUser().getDisplayName());
        assertNull(resp.getSharedByUser().getId()); // userId not returned
        assertEquals(user2+" "+user2, resp.getSharedByUser().getDisplayName());

        // access to get shared link info - pass user2 (but ignore in non-MT)
        params = Collections.singletonMap("include", "allowableOperations");
        response = getSingle(QuickShareLinkEntityResource.class, user2, shared1Id, params, 200);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);

        assertEquals(shared1Id, resp.getId());
        assertEquals(fileName1, resp.getName());
        assertEquals(d1Id, resp.getNodeId());
        assertNull(resp.getAllowableOperations()); // include is ignored

        assertNull(resp.getModifiedByUser().getId()); // userId not returned
        assertEquals(user1+" "+user1, resp.getModifiedByUser().getDisplayName());
        assertNull(resp.getSharedByUser().getId()); // userId not returned
        assertEquals(user2+" "+user2, resp.getSharedByUser().getDisplayName());


        // allowable operations not included - no params
        response = getSingle(QuickShareLinkEntityResource.class, user2, shared1Id, null, 200);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        assertNull(resp.getAllowableOperations());


        // unauth access to get shared link info
        params = Collections.singletonMap("include", "allowableOperations"); // note: this will be ignore for unauth access
        response = getSingle(QuickShareLinkEntityResource.class, null, shared1Id, params, 200);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);

        assertEquals(shared1Id, resp.getId());
        assertEquals(fileName1, resp.getName());
        assertEquals(d1Id, resp.getNodeId());
        assertNull(resp.getAllowableOperations()); // include is ignored

        assertNull(resp.getModifiedByUser().getId()); // userId not returned
        assertEquals(user1+" "+user1, resp.getModifiedByUser().getDisplayName());
        assertNull(resp.getSharedByUser().getId()); // userId not returned
        assertEquals(user2+" "+user2, resp.getSharedByUser().getDisplayName());


        // unauth access to file 1 content (via shared link)
        response = getSingle(QuickShareLinkEntityResource.class, null, shared1Id + "/content", null, 200);
        assertArrayEquals(content1Text.getBytes(), response.getResponseAsBytes());
        Map<String, String> responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(file1_MimeType+";charset=UTF-8", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get("Expires"));
        assertEquals("attachment; filename=\"" + fileName1 + "\"; filename*=UTF-8''" + fileName1 + "", responseHeaders.get("Content-Disposition"));
        String lastModifiedHeader = responseHeaders.get(LAST_MODIFIED_HEADER);
        assertNotNull(lastModifiedHeader);
        // Test 304 response
        Map<String, String> headers = Collections.singletonMap(IF_MODIFIED_SINCE_HEADER, lastModifiedHeader);
        getSingle(URL_SHARED_LINKS, null, shared1Id + "/content", null, headers, 304);

        // -ve test - unauth access to get shared link file content - without Content-Disposition header (attachment=false) - header ignored (plain text is not in white list)
        params = new HashMap<>();
        params.put("attachment", "false");
        response = getSingle(QuickShareLinkEntityResource.class, null, shared1Id + "/content", params, 200);
        assertEquals("attachment; filename=\"" + fileName1 + "\"; filename*=UTF-8''" + fileName1 + "", response.getHeaders().get("Content-Disposition"));


        // unauth access to file 2 content (via shared link)
        response = getSingle(QuickShareLinkEntityResource.class, null, shared2Id + "/content", null, 200);
        assertArrayEquals(file2_originalBytes, response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(file2_MimeType+";charset=UTF-8", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get("Expires"));
        assertNotNull(responseHeaders.get(LAST_MODIFIED_HEADER));
        assertEquals("attachment; filename=\"" + fileName2 + "\"; filename*=UTF-8''" + fileName2 + "", responseHeaders.get("Content-Disposition"));

        // unauth access to file 2 content (via shared link) - without Content-Disposition header (attachment=false)
        params = new HashMap<>();
        params.put("attachment", "false");
        response = getSingle(QuickShareLinkEntityResource.class, null, shared2Id + "/content", params, 200);
        assertArrayEquals(file2_originalBytes, response.getResponseAsBytes());
        responseHeaders = response.getHeaders();
        assertNotNull(responseHeaders);
        assertEquals(file2_MimeType+";charset=UTF-8", responseHeaders.get("Content-Type"));
        assertNotNull(responseHeaders.get(LAST_MODIFIED_HEADER));
        assertNotNull(responseHeaders.get("Expires"));
        assertNull(responseHeaders.get("Content-Disposition"));

        // -ve shared link rendition tests
        {
            // -ve test - try to get non-existent rendition content
            getSingle(QuickShareLinkEntityResource.class, null, shared1Id + "/renditions/doclib/content", null, 404);

            // -ve test - try to get unregistered rendition content
            getSingle(QuickShareLinkEntityResource.class, null, shared1Id + "/renditions/dummy/content", null, 404);
        }


        // create rendition
        Rendition rendition = createAndGetRendition(user2, d1Id, "doclib");
        assertNotNull(rendition);
        assertEquals(Rendition.RenditionStatus.CREATED, rendition.getStatus());

        // unauth access to get shared link file rendition content
        response = getSingle(QuickShareLinkEntityResource.class, null, shared1Id + "/renditions/doclib/content", null, 200);
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
        response = getSingle(QuickShareLinkEntityResource.class, null, shared1Id + "/renditions/doclib/content", params, 200);
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
        getSingle(URL_SHARED_LINKS, null, shared1Id + "/renditions/doclib/content", null, headers, 304);


        // -ve delete tests
        {
            // -ve test - user1 cannot delete shared link
            delete(URL_SHARED_LINKS, user1, shared1Id, 403);

            // -ve test - unauthenticated
            delete(URL_SHARED_LINKS, null, shared1Id, 401);

            // -ve test - delete - cannot delete non-existent link
            delete(URL_SHARED_LINKS, user1, "dummy", 404);
        }


        // -ve create tests
        {
            // As user 1 ...

            // -ve test - try to create again (different user, that has read permission) - already exists
            body = new HashMap<>();
            body.put("nodeId", d1Id);
            post(URL_SHARED_LINKS, user1, toJsonAsStringNonNull(body), 409);

            // -ve - create - missing nodeId
            body = new HashMap<>();
            post(URL_SHARED_LINKS, user1, toJsonAsStringNonNull(body), 400);

            // -ve - create - unknown nodeId
            body = new HashMap<>();
            body.put("nodeId", "dummy");
            post(URL_SHARED_LINKS, user1, toJsonAsStringNonNull(body), 404);

            // -ve - create - try to link to folder (ie. not a file)
            String f1Id = createFolder(user1, myFolderNodeId, "f1 " + RUNID).getId();
            body = new HashMap<>();
            body.put("nodeId", f1Id);
            post(URL_SHARED_LINKS, user1, toJsonAsStringNonNull(body), 400);

            // -ve test - cannot create if user does not have permission to read
            body = new HashMap<>();
            body.put("nodeId", d2Id);
            post(URL_SHARED_LINKS, user2, toJsonAsStringNonNull(body), 403);

            // -ve test - unauthenticated
            body = new HashMap<>();
            body.put("nodeId", d1Id);
            post(URL_SHARED_LINKS, null, toJsonAsStringNonNull(body), 401);
        }


        // delete shared link
        delete(URL_SHARED_LINKS, user2, shared1Id, 204);

        // -ve test - delete - cannot delete non-existent link
        delete(URL_SHARED_LINKS, user1, shared1Id, 404);


        response = getSingle(NodesEntityResource.class, user2, d1Id, null, 200);
        nodeResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);

        assertEquals(docModifiedAt.getTime(), nodeResp.getModifiedAt().getTime()); // not changed
        assertEquals(docModifiedBy, nodeResp.getModifiedByUser().getId()); // not changed (ie. not user2)


        // -ve get tests
        {
            // try to get link that has been deleted (see above)
            getSingle(QuickShareLinkEntityResource.class, null, shared1Id, null, 404);
            getSingle(QuickShareLinkEntityResource.class, null, shared1Id + "/content", null, 404);

            // try to get non-existent link
            getSingle(QuickShareLinkEntityResource.class, null, "dummy", null, 404);
            getSingle(QuickShareLinkEntityResource.class, null, "dummy/content", null, 404);
        }

        // TODO if and when these tests are optionally runnable via remote env then we could skip this part of the test
        // (else need to verify test mechanism for enterprise admin via jmx ... etc)

        QuickShareLinksImpl quickShareLinks = applicationContext.getBean("quickShareLinks", QuickShareLinksImpl.class);
        try
        {
            quickShareLinks.setEnabled(false);

            // -ve - disabled service tests
            body.put("nodeId", "dummy");
            post(URL_SHARED_LINKS, user1, toJsonAsStringNonNull(body), 501);

            getSingle(QuickShareLinkEntityResource.class, null, "dummy", null, 501);
            getSingle(QuickShareLinkEntityResource.class, null, "dummy/content", null, 501);
            delete(URL_SHARED_LINKS, user1, "dummy", 501);
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
    public void testSharedLinkFind() throws Exception
    {
        Paging paging = getPaging(0, 100);

        // Get all shared links visible to user 1 (note: for now assumes clean repo)
        HttpResponse response = getAll(URL_SHARED_LINKS, user1, paging, 200);
        List<QuickShareLink> sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(0, sharedLinks.size());

        // As user 1 ...

        // create doc d1 - in "My" folder
        String myFolderNodeId = getMyNodeId(user1);
        String content1Text = "The quick brown fox jumps over the lazy dog 1.";
        String docName1 = "content" + RUNID + "_1.txt";
        Document doc1 = createTextFile(user1, myFolderNodeId, docName1, content1Text);
        String d1Id = doc1.getId();

        // create doc d2 - in "Shared" folder
        String sharedFolderNodeId = getSharedNodeId(user1);
        String content2Text = "The quick brown fox jumps over the lazy dog 2.";
        String docName2 = "content" + RUNID + "_2.txt";
        Document doc2 = createTextFile(user1, sharedFolderNodeId, docName2, content1Text);
        String d2Id = doc2.getId();


        // As user 1 ...

        // create shared link to doc 1
        Map<String, String> body = new HashMap<>();
        body.put("nodeId", d1Id);
        response = post(URL_SHARED_LINKS, user1, toJsonAsStringNonNull(body), 201);
        QuickShareLink resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        String shared1Id = resp.getId();

        // As user 2 ...

        // create shared link to doc 2
        body = new HashMap<>();
        body.put("nodeId", d2Id);
        response = post(URL_SHARED_LINKS, user2, toJsonAsStringNonNull(body), 201);
        resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        String shared2Id = resp.getId();


        //
        // find links
        //

        response = getAll(URL_SHARED_LINKS, user1, paging, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(2, sharedLinks.size());
        assertEquals(shared2Id, sharedLinks.get(0).getId());
        assertEquals(d2Id, sharedLinks.get(0).getNodeId());
        assertEquals(shared1Id, sharedLinks.get(1).getId());
        assertEquals(d1Id, sharedLinks.get(1).getNodeId());

        response = getAll(URL_SHARED_LINKS, user2, paging, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(1, sharedLinks.size());
        assertEquals(shared2Id, sharedLinks.get(0).getId());
        assertEquals(d2Id, sharedLinks.get(0).getNodeId());

        // find my links
        Map<String, String> params = new HashMap<>();
        params.put("where", "("+ QuickShareLinks.PARAM_SHAREDBY+"='"+People.DEFAULT_USER+"')");

        response = getAll(URL_SHARED_LINKS, user1, paging, params, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(1, sharedLinks.size());
        assertEquals(shared1Id, sharedLinks.get(0).getId());
        assertEquals(d1Id, sharedLinks.get(0).getNodeId());

        // find links shared by a given user
        params = new HashMap<>();
        params.put("where", "("+ QuickShareLinks.PARAM_SHAREDBY+"='"+user2+"')");

        response = getAll(URL_SHARED_LINKS, user1, paging, params, 200);
        sharedLinks = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), QuickShareLink.class);
        assertEquals(1, sharedLinks.size());
        assertEquals(shared2Id, sharedLinks.get(0).getId());
        assertEquals(d2Id, sharedLinks.get(0).getNodeId());


        // -ve test - unauthenticated
        getAll(URL_SHARED_LINKS, null, paging, params, 401);


        // delete the shared links
        delete(URL_SHARED_LINKS, user1, shared1Id, 204);
        delete(URL_SHARED_LINKS, user2, shared2Id, 204);


        // TODO if and when these tests are optionally runnable via remote env then we could skip this part of the test
        // (else need to verify test mechanism for enterprise admin via jmx ... etc)

        QuickShareLinksImpl quickShareLinks = applicationContext.getBean("quickShareLinks", QuickShareLinksImpl.class);
        try
        {
            quickShareLinks.setEnabled(false);

            // -ve - disabled service tests
            getAll(URL_SHARED_LINKS, user1, paging, 501);
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
        // Create plain text document
        String myFolderNodeId = getMyNodeId(user1);
        String contentText = "The quick brown fox jumps over the lazy dog.";
        String fileName = "file-" + RUNID + ".txt";
        Document doc = createTextFile(user1, myFolderNodeId, fileName, contentText);
        String docId = doc.getId();

        // Create shared link to document
        Map<String, String> body = Collections.singletonMap("nodeId", docId);
        HttpResponse response = post(URL_SHARED_LINKS, user1, toJsonAsStringNonNull(body), 201);
        QuickShareLink resp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), QuickShareLink.class);
        String sharedId = resp.getId();
        assertNotNull(sharedId);
        assertEquals(fileName, resp.getName());

        // Email request with minimal properties
        QuickShareLinkEmailRequest request = new QuickShareLinkEmailRequest();
        request.setClient("sfs");
        List<String> recipients = new ArrayList<>(2);
        recipients.add(user2 + "@acme.test");
        recipients.add(user2 + "@ping.test");
        request.setRecipientEmails(recipients);
        post(getEmailSharedLinkUrl(sharedId), user1, RestApiUtil.toJsonAsString(request), 202);

        // Email request with all the properties
        request = new QuickShareLinkEmailRequest();
        request.setClient("sfs");
        request.setMessage("My custom message!");
        request.setLocale(Locale.UK.toString());
        recipients = Collections.singletonList(user2 + "@acme.test");
        request.setRecipientEmails(recipients);
        post(getEmailSharedLinkUrl(sharedId), user1, RestApiUtil.toJsonAsString(request), 202);

        // -ve tests
        // sharedId path parameter does not exist
        post(getEmailSharedLinkUrl(sharedId + System.currentTimeMillis()), user1, RestApiUtil.toJsonAsString(request), 404);

        // Unregistered client
        request = new QuickShareLinkEmailRequest();
        request.setClient("VeryCoolClient" + System.currentTimeMillis());
        List<String> user2Email = Collections.singletonList(user2 + "@acme.test");
        request.setRecipientEmails(user2Email);
        post(getEmailSharedLinkUrl(sharedId), user1, RestApiUtil.toJsonAsString(request), 400);

        // client is mandatory
        request.setClient(null);
        post(getEmailSharedLinkUrl(sharedId), user1, RestApiUtil.toJsonAsString(request), 400);

        // recipientEmails is mandatory
        request.setClient("sfs");
        request.setRecipientEmails(null);
        post(getEmailSharedLinkUrl(sharedId), user1, RestApiUtil.toJsonAsString(request), 400);

        // TODO if and when these tests are optionally runnable via remote env then we could skip this part of the test
        // (else need to verify test mechanism for enterprise admin via jmx ... etc)
        QuickShareLinksImpl quickShareLinks = applicationContext.getBean("quickShareLinks", QuickShareLinksImpl.class);
        try
        {
            quickShareLinks.setEnabled(false);
            request = new QuickShareLinkEmailRequest();
            request.setClient("sfs");
            request.setRecipientEmails(user2Email);
            post(getEmailSharedLinkUrl(sharedId), user1, RestApiUtil.toJsonAsString(request), 501);
        }
        finally
        {
            quickShareLinks.setEnabled(true);
        }
    }

    @Override
    public String getScope()
    {
        return "public";
    }

    private String getEmailSharedLinkUrl(String sharedId)
    {
        return URL_SHARED_LINKS + '/' + sharedId + "/email";
    }
}
