package org.alfresco.rest.api.tests;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.*;

import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
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
 * Tests posting activities from the public api.
 *
 * @author gethin
 */
public class ActivitiesPostingTest extends AbstractSingleNetworkSiteTest
{

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

        //Now download it
        response = getSingle(NodesEntityResource.class, u1.getId(), documentResp.getId()+"/content", null, 200);
        String textContent = response.getResponse();
        assertNotNull(textContent);

        delete(URL_NODES, u1.getId(), documentResp.getId(), 204);
        delete(URL_NODES, u1.getId(), createdFolder.getId(), 204);

        List<Activity> activities = getMyActivites();
        assertEquals(activities.size(),6);
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

        act = matchActivity(activities, ActivityPoster.DOWNLOADED, u1.getId(), tSite.getSiteId(), createdFolder.getId(), dUpdate.getName());
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
