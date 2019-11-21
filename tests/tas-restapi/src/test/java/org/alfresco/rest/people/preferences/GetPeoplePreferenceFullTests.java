package org.alfresco.rest.people.preferences;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestPreferenceModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.PreferenceName;
import org.alfresco.utility.model.*;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

public class GetPeoplePreferenceFullTests extends RestTest
{
    private UserModel userModel;
    private SiteModel siteModel;
    private RestPreferenceModel restPreferenceModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        dataSite.usingUser(userModel).usingSite(siteModel).addSiteToFavorites();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, 
        description = "Check that properties parameter is applied")
    public void propertiesParameterIsAppliedWhenRetrievingPreference() throws Exception
    {
        restPreferenceModel = restClient.authenticateUser(userModel).withParams("properties=id").withCoreAPI().usingUser(userModel)
                .getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModel.assertThat().field("id").is(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()))
            .and().field("value").isNull();
        
        restPreferenceModel = restClient.authenticateUser(userModel).withParams("properties=id,value").withCoreAPI().usingUser(userModel)
                .getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModel.assertThat().field("id").is(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()))
            .and().field("value").is("true");
     }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, 
        description = "Validate ID element in get site preference response")
    public void validateIdElementInGetSitePreferenceResponse() throws Exception
    {
        restPreferenceModel = restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
        restPreferenceModel.assertThat().field("id").is(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()))
            .and().field("value").is("true");
     }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, 
        description = "Validate ID element in get folder preference response")
    public void validateIdElementInGetFolderPreferenceResponse() throws Exception
    {
        FolderModel folderFavorite = new FolderModel("favoriteFolder");
        folderFavorite = dataContent.usingSite(siteModel).createFolder(folderFavorite);
        dataContent.getContentActions().setFolderAsFavorite(userModel.getUsername(), userModel.getPassword(), siteModel.getId(), folderFavorite.getName());
        
        restPreferenceModel = restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(PreferenceName.FOLDERS_FAVORITES_PREFIX.toString());
        restPreferenceModel.assertThat().field("id").is(PreferenceName.FOLDERS_FAVORITES_PREFIX)
            .and().field("value").is(Utility.removeLastSlash(Utility.buildPath("workspace://SpacesStore", folderFavorite.getNodeRef())));
     }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, 
        description = "Validate ID element in get file preference response")
    public void validateIdElementInGetFilePreferenceResponse() throws Exception
    {
        FileModel fileFavorite = new FileModel("favoriteFile", FileType.TEXT_PLAIN);
        fileFavorite = dataContent.usingUser(userModel).usingSite(siteModel).createContent(fileFavorite);
        dataContent.getContentActions().setFileAsFavorite(userModel.getUsername(), userModel.getPassword(), siteModel.getId(), fileFavorite.getName());

        restPreferenceModel = restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(PreferenceName.DOCUMENTS_FAVORITES_PREFIX.toString());
        restPreferenceModel.assertThat().field("id").is(PreferenceName.DOCUMENTS_FAVORITES_PREFIX)
            .and().field("value").is(Utility.removeLastSlash(Utility.buildPath("workspace://SpacesStore", fileFavorite.getNodeRefWithoutVersion())));
     }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, 
        description = "Get preference of an user that has no preferences")
    public void getPreferenceForUserWithoutPreferences() throws Exception
    {
        UserModel newUser = dataUser.createRandomTestUser();
        
        restPreferenceModel = restClient.authenticateUser(newUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(PreferenceName.DOCUMENTS_FAVORITES_PREFIX.toString());
        restClient.assertLastError().containsSummary(
                String.format("The relationship resource was not found for the" + " entity with id: %s and a relationship id of %s", newUser.getUsername(),
                        PreferenceName.DOCUMENTS_FAVORITES_PREFIX.toString()));
        
        restPreferenceModel = restClient.authenticateUser(newUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(PreferenceName.FOLDERS_FAVORITES_PREFIX.toString());
        restClient.assertLastError().containsSummary(
                String.format("The relationship resource was not found for the" + " entity with id: %s and a relationship id of %s", newUser.getUsername(),
                        PreferenceName.FOLDERS_FAVORITES_PREFIX.toString()));
        
        restPreferenceModel = restClient.authenticateUser(newUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(PreferenceName.SITES_FAVORITES_PREFIX.toString());
        restClient.assertLastError().containsSummary(
                String.format("The relationship resource was not found for the" + " entity with id: %s and a relationship id of %s", newUser.getUsername(),
                        PreferenceName.SITES_FAVORITES_PREFIX.toString()));
     }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, 
        description = "Change one preference for an user then perform get call")
    @Bug(id = "REPO-1922")
    public void changePreferenceThenPerformGetPreferenceCall() throws Exception
    {
        UserModel newUser = dataUser.createRandomTestUser();
        SiteModel site = dataSite.usingUser(newUser).createPublicRandomSite();
        
        dataSite.usingUser(newUser).usingSite(site).addSiteToFavorites();
        
        FileModel fileFavorite = new FileModel("favoriteFile", FileType.TEXT_PLAIN);
        fileFavorite = dataContent.usingSite(site).createContent(fileFavorite);
        dataContent.getContentActions().setFileAsFavorite(newUser.getUsername(), newUser.getPassword(), site.getId(), String.format("%s.%s", fileFavorite.getName(), fileFavorite.getFileType().extension));
        
        FolderModel folderFavorite = new FolderModel("favoriteFolder");
        folderFavorite = dataContent.usingSite(site).createFolder(folderFavorite);
        dataContent.getContentActions().setFolderAsFavorite(newUser.getUsername(), newUser.getPassword(), site.getId(), folderFavorite.getName());
        
        dataSite.usingUser(newUser).usingSite(site).removeSiteFromFavorites();
        dataContent.getContentActions().removeFavorite(newUser.getUsername(), newUser.getPassword(), site.getId(), folderFavorite.getName());
        dataContent.getContentActions().removeFavorite(newUser.getUsername(), newUser.getPassword(), site.getId(), Paths.get(fileFavorite.getCmisLocation()).getFileName().toString());
        
        restPreferenceModel = restClient.authenticateUser(newUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(PreferenceName.FOLDERS_FAVORITES_PREFIX.toString());
        restClient.assertLastError().containsSummary(
                String.format("The relationship resource was not found for the" + " entity with id: %s and a relationship id of %s", newUser.getUsername(),
                        PreferenceName.FOLDERS_FAVORITES_PREFIX.toString()));
        
        restPreferenceModel = restClient.authenticateUser(newUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(PreferenceName.SITES_FAVORITES_PREFIX.toString());
        restClient.assertLastError().containsSummary(
                String.format("The relationship resource was not found for the" + " entity with id: %s and a relationship id of %s", newUser.getUsername(),
                        PreferenceName.SITES_FAVORITES_PREFIX.toString()));
        
        restPreferenceModel = restClient.authenticateUser(newUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(PreferenceName.DOCUMENTS_FAVORITES_PREFIX.toString());
        restClient.assertLastError().containsSummary(
                String.format("The relationship resource was not found for the" + " entity with id: %s and a relationship id of %s", newUser.getUsername(),
                        PreferenceName.DOCUMENTS_FAVORITES_PREFIX.toString()));
        
        restPreferenceModel = restClient.authenticateUser(newUser).withCoreAPI().usingAuthUser()
                .getPersonPreferenceInformation(PreferenceName.FOLDERS_FAVORITES_PREFIX.toString());
        restClient.assertLastError().containsSummary(
                String.format("The relationship resource was not found for the" + " entity with id: %s and a relationship id of %s", newUser.getUsername(),
                        PreferenceName.FOLDERS_FAVORITES_PREFIX.toString()));
     }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, 
        description = "Verify admin is able to get preference of another user")
    public void adminIsAbleToGetOtherUserPreference() throws Exception
    {
        restPreferenceModel = restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingUser(userModel)
                .getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId()));
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restPreferenceModel.assertThat().field("id").is(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), siteModel.getId())).and().field("value").is("true");
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.PREFERENCES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION, 
        description = "Verify regular user is not able to get preference of admin user")
    public void regularUserIsNotAbleToGetAdminPreference() throws Exception
    {
        SiteModel newSite = dataSite.usingUser(dataUser.getAdminUser()).createPublicRandomSite();
        dataSite.usingUser(dataUser.getAdminUser()).usingSite(newSite).addSiteToFavorites();
        
        restClient.authenticateUser(userModel).withCoreAPI().usingUser(dataUser.getAdminUser())
                .getPersonPreferenceInformation(PreferenceName.SITES_FAVORITES_PREFIX + newSite.getId());
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
        restClient.assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }
}