package org.alfresco.rest.api.tests;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.*;

import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.Activities;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.data.Activity;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
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

        Document d1 = new Document();
        d1.setName("d1.txt");
        d1.setNodeType("cm:content");
        ContentInfo ci = new ContentInfo();
        ci.setMimeType("text/plain");
        d1.setContent(ci);

        // create empty file
        HttpResponse response = post(getNodeChildrenUrl(createdFolder.getId()), u1.getId(), toJsonAsStringNonNull(d1), 201);
        Document documentResp = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Document.class);

        //Update the file
        Document dUpdate = new Document();
        dUpdate.setName("d1b.txt");
        response = put(URL_NODES, u1.getId(), documentResp.getId(), toJsonAsStringNonNull(dUpdate), null, 200);

        repoService.generateFeed();

        Map<String, String> meParams = new HashMap<>();
        meParams.put("who", String.valueOf(Activities.ActivityWho.me));
        PublicApiClient.ListResponse<Activity> activities = publicApiClient.people().getActivities(u1.getId(), meParams);
        assertEquals(activities.getList().size(),3);
        Activity act = matchActivity(activities.getList(), ActivityType.FOLDER_ADDED, u1.getId(), tSite.getSiteId(), docLibNodeRef.getId(), folder1);
        assertNotNull(act);

        act = matchActivity(activities.getList(), ActivityType.FILE_ADDED, u1.getId(), tSite.getSiteId(), createdFolder.getId(), d1.getName());
        assertNotNull(act);

        act = matchActivity(activities.getList(), ActivityType.FILE_UPDATED, u1.getId(), tSite.getSiteId(), createdFolder.getId(), dUpdate.getName());
        assertNotNull(act);

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
