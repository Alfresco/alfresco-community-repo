package org.alfresco.rest.demo;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class SampleSitesTests extends RestTest
{  
    private UserModel userModel;
    private SiteModel siteModel;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws DataPreparationException
    {
        userModel = dataUser.getAdminUser();
        restClient.authenticateUser(userModel);
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();        
    }

    @Test(groups = { "demo" })
    public void adminShouldGetSiteDetails() throws JsonToModelConversionException, Exception
    {
        restClient.withCoreAPI().usingSite(siteModel).getSite()
            .assertThat().field("id").isNotNull();
    }

    @Test(groups = { "demo" })
    public void adminShouldGetSites() throws JsonToModelConversionException, Exception
    {
        restClient.withCoreAPI().usingSite(siteModel).getSite();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { "demo" })
    public void adminShouldAccessSites() throws JsonToModelConversionException, Exception
    {
        restClient.withCoreAPI().getSites().assertThat().entriesListIsNotEmpty();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { "demo" })
    public void adminShouldAccessResponsePagination() throws JsonToModelConversionException, Exception
    {
        restClient.withCoreAPI().getSites().assertThat().paginationExist();
    }

    @Test(groups = { "demo" })
    public void adminShouldAddNewSiteMember() throws JsonToModelConversionException, DataPreparationException, Exception
    {
        UserModel testUser = dataUser.createRandomTestUser("testUser");
        testUser.setUserRole(UserRole.SiteConsumer);
        restClient.withCoreAPI().usingSite(siteModel).addPerson(testUser);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { "demo" })
    public void adminShouldGetSiteFromSitesList() throws JsonToModelConversionException, Exception
    {
        restClient.withCoreAPI().getSites().assertThat().entriesListContains("id", siteModel.getId());    
    }

    @Test(groups = { "demo" })
    public void adminShouldAccessSiteDetails() throws JsonToModelConversionException, Exception
    {
      restClient.withCoreAPI().usingSite(siteModel).getSite()
            .assertThat().field("id").isNotNull()
            .and().field("description").is(siteModel.getDescription())
            .and().field("title").is(siteModel.getTitle())
            .and().field("visibility").is(siteModel.getVisibility());            
    }

}