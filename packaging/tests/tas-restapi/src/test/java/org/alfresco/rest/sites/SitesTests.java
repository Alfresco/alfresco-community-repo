package org.alfresco.rest.sites;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestSiteModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * Handles tests related to api-explorer/#!/sites
 * 
 * @author Ana Bozianu
 */
public class SitesTests extends RestTest
{

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.SANITY, description = "Tests the creation of a site")
    public void testCreateSite() throws Exception
    {
        SiteModel site = RestSiteModel.getRandomSiteModel();
        
        RestSiteModel createdSite = restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingSite(site).createSite();

        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        createdSite.assertThat().field("id").is(site.getId())
                   .assertThat().field("title").is(site.getTitle())
                   .assertThat().field("description").is(site.getDescription())
                   .assertThat().field("visibility").is(site.getVisibility());
    }

}
