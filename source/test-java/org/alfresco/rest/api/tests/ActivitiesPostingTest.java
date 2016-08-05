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

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.Activities;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.data.Activity;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.service.cmr.activities.ActivityPoster;
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

    /**
     * Tests the main activites, added, updated, deleted, downloaded
     */
    @Test
    public void testCreateUpdate() throws Exception
    {
        setRequestContext(user1);
        
        String folder1 = "folder" + System.currentTimeMillis() + "_1";
        Folder createdFolder = createFolder(tDocLibNodeId, folder1, null);
        assertNotNull(createdFolder);

        String docName = "d1.txt";
        Document documentResp = createEmptyTextFile(createdFolder, docName);

        //Update the file
        Document dUpdate = new Document();
        dUpdate.setName("d1b.txt");
        put(URL_NODES, user1, documentResp.getId(), toJsonAsStringNonNull(dUpdate), null, 200);

        //Now download it
        HttpResponse response = getSingle(NodesEntityResource.class, user1, documentResp.getId()+"/content", null, 200);
        String textContent = response.getResponse();
        assertNotNull(textContent);

        deleteNode(documentResp.getId());
        deleteNode(createdFolder.getId());

        List<Activity> activities = getMyActivities();
        assertEquals(activities.size(),6);
        Activity act = matchActivity(activities, ActivityType.FOLDER_ADDED, user1, tSiteId, tDocLibNodeId, folder1);
        assertNotNull(act);

        act = matchActivity(activities, ActivityType.FILE_ADDED, user1, tSiteId, createdFolder.getId(), docName);
        assertNotNull(act);

        act = matchActivity(activities, ActivityType.FILE_UPDATED, user1, tSiteId, createdFolder.getId(), dUpdate.getName());
        assertNotNull(act);

        act = matchActivity(activities, ActivityType.FOLDER_DELETED, user1, tSiteId, tDocLibNodeId, folder1);
        assertNotNull(act);

        act = matchActivity(activities, ActivityType.FILE_DELETED, user1, tSiteId, createdFolder.getId(), dUpdate.getName());
        assertNotNull(act);

        act = matchActivity(activities, ActivityPoster.DOWNLOADED, user1, tSiteId, createdFolder.getId(), dUpdate.getName());
        assertNotNull(act);
    }

    /**
     * Tests non-file activites. So no events.
     */
    @Test
    public void testNonFileActivities() throws Exception
    {
        setRequestContext(user1);
        
        String folder1 = "InSitefolder" + System.currentTimeMillis() + "_1";
        Folder createdFolder = createFolder(tDocLibNodeId, folder1, null);
        assertNotNull(createdFolder);

        List<Activity> activities = getMyActivities();

        Node aNode = createNode(createdFolder.getId(), "mynode", "cm:failedThumbnail", null);
        assertNotNull(aNode);

        deleteNode(aNode.getId());

        List<Activity> activitiesAgain = getMyActivities();
        assertEquals("No activites should be created for non-file activities", activities, activitiesAgain);
    }

    /**
     * Tests non-site file activites. So no events.
     */
    @Test
    public void testNonSite() throws Exception
    {
        setRequestContext(user1);
        
        List<Activity> activities = getMyActivities();
        String folder1 = "nonSitefolder" + System.currentTimeMillis() + "_1";
        //Create a folder outside a site
        Folder createdFolder = createFolder(Nodes.PATH_MY, folder1, null);
        assertNotNull(createdFolder);

        String docName = "nonsite_d1.txt";
        Document documentResp = createEmptyTextFile(createdFolder, docName);
        assertNotNull(documentResp);

        //Update the file
        Document dUpdate = new Document();
        dUpdate.setName("nonsite_d2.txt");
        put(URL_NODES, user1, documentResp.getId(), toJsonAsStringNonNull(dUpdate), null, 200);

        List<Activity> activitiesAgain = getMyActivities();
        assertEquals("No activites should be created for non-site nodes", activities, activitiesAgain);
    }

    /**
     * Generate the feed an get the activities for user1
     * @return
     * @throws Exception
     */
    private List<Activity> getMyActivities() throws Exception
    {
        repoService.generateFeed();

        setRequestContext(user1);
        
        Map<String, String> meParams = new HashMap<>();
        meParams.put("who", String.valueOf(Activities.ActivityWho.me));
        return publicApiClient.people().getActivities(user1, meParams).getList();
    }


    /**
     * Match an exact activity by a combination of the parameters
     * @param list
     * @param type
     * @param user
     * @param siteId
     * @param parentId
     * @param title
     * @return
     */
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
