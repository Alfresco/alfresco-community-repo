/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.demo;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.dataprep.SiteService.Visibility;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RestDemoTests extends RestTest
{
    private UserModel userModel;
    private SiteModel siteModel;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws DataPreparationException
    {       
        userModel = dataUser.getAdminUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        restClient.authenticateUser(userModel);
    }

    /**
     * Data preparation – create site with custom details <br/>
     * Perform GET sites call using admin user<br/>
     * Check that created site is included in response <br/>
     * Perform GET site call, validate that site title, description and visibility are correct <br/>
     * 
     * @throws JsonToModelConversionException
     */
    @Test(groups = { "demo" })
    public void adminRetrievesCorrectSiteDetails() throws JsonToModelConversionException
    {
        restClient.withCoreAPI().getSites().assertThat()
            .entriesListContains("id", siteModel.getId());
        
        restClient.withCoreAPI().usingSite(siteModel).getSite()
              .assertThat().field("id").isNotNull()
              .assertThat().field("description").is(siteModel.getDescription())
              .assertThat().field("title").is(siteModel.getTitle())
                  .assertThat().field("visibility").is(Visibility.PUBLIC);
    }

    /**
     * Data preparation – create site and document on server <br/>
     * POST one comment to file using admin user <br/>
     * Perform GET comments, check the new one is listed <br/>
     * Update existing comment using PUT call, check that comment content is updated <br/>
     */
    //Opened DESKTOPAPP-475 for fixing the failing test
   // @Test(groups = { "demo" })
    public void adminCanPostAndUpdateComments()
    {       
        FileModel fileModel = dataContent.usingUser(userModel)
                                  .usingResource(FolderModel.getSharedFolderModel())
                                  .createContent(DocumentType.TEXT_PLAIN);
        // add new comment
        restClient.withCoreAPI().usingResource(fileModel).addComment("This is a new comment");
        restClient.withCoreAPI().usingResource(fileModel).getNodeComments()
            .assertThat().entriesListIsNotEmpty().and()
            .entriesListContains("content", "This is a new comment");
    }

    /**
     * Data preparation – create site and a new user <br/>
     * As admin, add user as Consumer to site as a new site member using POST call <br/>
     * Update site member role to Manager using PUT call <br/>
     * Delete site member using DELETE call <br/>
     * 
     * @throws DataPreparationException
     * @throws JsonToModelConversionException
     */
    @Test(groups = { "demo" })
    public void adminCanAddAndUpdateSiteMemberDetails()
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);

        // add user as Consumer to site
        restClient.withCoreAPI().usingSite(siteModel).addPerson(testUser);
        restClient.withCoreAPI().usingSite(siteModel).getSiteMembers().assertThat().entriesListContains("id", testUser.getUsername())
                .when().getSiteMember(testUser.getUsername())
            .assertSiteMemberHasRole(UserRole.SiteConsumer);

        // update site member to Manager
        testUser.setUserRole(UserRole.SiteCollaborator);
        restClient.withCoreAPI().usingSite(siteModel).updateSiteMember(testUser);
        restClient.withCoreAPI().usingSite(siteModel).getSiteMembers().and()
            .entriesListContains("id", testUser.getUsername())
            .when().getSiteMember(testUser.getUsername())
            .assertSiteMemberHasRole(UserRole.SiteCollaborator);

        restClient.withCoreAPI().usingSite(siteModel).deleteSiteMember(testUser);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }
}
