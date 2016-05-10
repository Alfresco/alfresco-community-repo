package org.alfresco.rest.api.tests;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.*;

import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.Activities;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Activity;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.activities.ActivityPoster;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gethin on 22/03/16.
 */
public class ActivitiesPostingTest extends AbstractBaseApiTest
{
    protected MutableAuthenticationService authenticationService;
    protected PersonService personService;

    RepoService.TestNetwork networkOne;
    RepoService.TestPerson u1;
    RepoService.TestSite tSite;
    NodeRef docLibNodeRef;

    @Override
    public String getScope()
    {
        return "public";
    }

    @Before
    public void setup() throws Exception
    {
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        personService = applicationContext.getBean("personService", PersonService.class);

        networkOne = getTestFixture().getRandomNetwork();
        u1 = networkOne.createUser();
        tSite = createSite(networkOne, u1, SiteVisibility.PRIVATE);

        AuthenticationUtil.setFullyAuthenticatedUser(u1.getId());
        docLibNodeRef = tSite.getContainerNodeRef("documentLibrary");
        AuthenticationUtil.clearCurrentSecurityContext();
    }


    @After
    public void tearDown() throws Exception
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                if (personService.personExists(u1.getId()))
                {
                    authenticationService.deleteAuthentication(u1.getId());
                    personService.deletePerson(u1.getId());
                }
                return null;
            }
        });
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @Test
    public void testCreateUpdate() throws Exception
    {
        String folder1 = "folder" + System.currentTimeMillis() + "_1";
        Folder createdFolder = createFolder(u1.getId(), docLibNodeRef.getId(), folder1, null);
        assertNotNull(createdFolder);

        String docName = "d1.txt";
        Document documentResp = createDocument(createdFolder, docName);

        //Update the file
        Document dUpdate = new Document();
        dUpdate.setName("d1b.txt");
        HttpResponse response = put(URL_NODES, u1.getId(), documentResp.getId(), toJsonAsStringNonNull(dUpdate), null, 200);

        delete(URL_NODES, u1.getId(), documentResp.getId(), 204);
        delete(URL_NODES, u1.getId(), createdFolder.getId(), 204);

        List<Activity> activities = getMyActivites();
        assertEquals(activities.size(),5);
        Activity act = matchActivity(activities, ActivityType.FOLDER_ADDED, u1.getId(), tSite.getSiteId(), docLibNodeRef.getId(), folder1);
        assertNotNull(act);

        act = matchActivity(activities, ActivityType.FILE_ADDED, u1.getId(), tSite.getSiteId(), createdFolder.getId(), docName);
        assertNotNull(act);

        act = matchActivity(activities, ActivityType.FILE_UPDATED, u1.getId(), tSite.getSiteId(), createdFolder.getId(), dUpdate.getName());
        assertNotNull(act);

        act = matchActivity(activities, ActivityType.FOLDER_DELETED, u1.getId(), tSite.getSiteId(), docLibNodeRef.getId(), folder1);
        assertNotNull(act);

        act = matchActivity(activities, ActivityType.FILE_DELETED, u1.getId(), tSite.getSiteId(), createdFolder.getId(), dUpdate.getName());
        assertNotNull(act);
    }

    @Test
    public void testNonFileActivities() throws Exception
    {
        String folder1 = "InSitefolder" + System.currentTimeMillis() + "_1";
        Folder createdFolder = createFolder(u1.getId(), docLibNodeRef.getId(), folder1, null);
        assertNotNull(createdFolder);

        List<Activity> activities = getMyActivites();

        Node aNode = createNode(u1.getId(), createdFolder.getId(), "mynode", "cm:failedThumbnail", null);
        assertNotNull(aNode);

        delete(URL_NODES, u1.getId(), aNode.getId(), 204);

        List<Activity> activitiesAgain = getMyActivites();
        assertEquals("No activites should be created for non-file activities", activities, activitiesAgain);
    }

    @Test
    public void testNonSite() throws Exception
    {
        List<Activity> activities = getMyActivites();
        String folder1 = "nonSitefolder" + System.currentTimeMillis() + "_1";
        //Create a folder outside a site
        Folder createdFolder = createFolder(u1.getId(),  Nodes.PATH_MY, folder1, null);
        assertNotNull(createdFolder);

        String docName = "nonsite_d1.txt";
        Document documentResp = createDocument(createdFolder, docName);
        assertNotNull(documentResp);

        //Update the file
        Document dUpdate = new Document();
        dUpdate.setName("nonsite_d2.txt");
        HttpResponse response = put(URL_NODES, u1.getId(), documentResp.getId(), toJsonAsStringNonNull(dUpdate), null, 200);

        List<Activity> activitiesAgain = getMyActivites();
        assertEquals("No activites should be created for non-site nodes", activities, activitiesAgain);
    }

    private List<Activity> getMyActivites() throws Exception
    {
        repoService.generateFeed();

        publicApiClient.setRequestContext(new RequestContext(u1.getId()));
        Map<String, String> meParams = new HashMap<>();
        meParams.put("who", String.valueOf(Activities.ActivityWho.me));
        return publicApiClient.people().getActivities(u1.getId(), meParams).getList();
    }

    private Document createDocument(Folder parentFolder, String docName) throws Exception
    {
        Document d1 = new Document();
        d1.setName(docName);
        d1.setNodeType("cm:content");
        ContentInfo ci = new ContentInfo();
        ci.setMimeType("text/plain");
        d1.setContent(ci);

        // create empty file
        HttpResponse response = post(getNodeChildrenUrl(parentFolder.getId()), u1.getId(), toJsonAsStringNonNull(d1), 201);
        return RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);
    }

    private Activity matchActivity(List<Activity> list, String type, String user, String siteId, String parentId, String title)
    {
        for (Activity act:list)
        {
          if (type.equals(act.getActivityType())
                  && user.equals(act.getPostPersonId())
                  && siteId.equals(act.getSiteId())
                  && parentId.equals(act.getSummary().get("parentObjectId"))
                  && title.equals((act.getSummary().get("title"))))
          {
              return act;
          }
        }
        return null;
    }
}
