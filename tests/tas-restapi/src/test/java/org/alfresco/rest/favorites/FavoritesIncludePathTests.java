package org.alfresco.rest.favorites;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class includes Sanity tests for the favorites api. Detailed tests would be covered in the alfresco-remote-api test project
 * 
 * @author meenal bhave
 */
public class FavoritesIncludePathTests extends RestTest
{
    private UserModel adminUser;
    private UserModel testUser1;
    
    private SiteModel siteModel1;
    
    private FolderModel folder1;
    
    private FileModel file1;
    
    private FolderModel subFolder1;
    private FileModel fileInSubFolder;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);

        // Create Standard User
        testUser1 = dataUser.usingUser(adminUser).createRandomTestUser();

        // Create Site
        siteModel1 = dataSite.usingUser(testUser1).createPublicRandomSite();

        folder1 = dataContent.usingUser(adminUser).usingSite(siteModel1).createFolder();
        subFolder1 = dataContent.usingUser(adminUser).usingResource(folder1).createFolder();
        file1 = dataContent.usingUser(adminUser).usingResource(folder1).createContent(DocumentType.TEXT_PLAIN);
        fileInSubFolder = dataContent.usingUser(adminUser).usingResource(subFolder1).createContent(DocumentType.TEXT_PLAIN);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION, description = "Verify that get favorite site request does not include Path")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION})
    public void testGetFavoriteIncludePathForSite() throws Exception
    {
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().includePath().getFavorite(siteModel1.getGuid());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.onResponse().assertThat().body("entry.target.site.id", org.hamcrest.Matchers.equalTo(siteModel1.getId()));//(org.hamcrest.Matchers.nullValue()));        
        restClient.onResponse().assertThat().body("entry.target.site.path", org.hamcrest.Matchers.is(org.hamcrest.Matchers.nullValue()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION, description = "Verify that get favorite file or folder request includes path when requested")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION})
    public void testGetFavouriteIncludePathForFileFolder() throws Exception
    {
        STEP("1. Favourite Folder and File");
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().addFolderToFavorites(folder1);
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().addFileToFavorites(file1);

        STEP("2. Check Path for Folder: Displayed when requested");
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().includePath().getFavorite(folder1.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.onResponse().assertThat().body("entry.target.folder.id", org.hamcrest.Matchers.equalTo(folder1.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("entry.target.folder.path", org.hamcrest.Matchers.notNullValue());

        STEP("3. Check Path for file: Displayed when requested");
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().includePath().getFavorite(file1.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.onResponse().assertThat().body("entry.target.file.id", org.hamcrest.Matchers.equalTo(file1.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("entry.target.file.path", org.hamcrest.Matchers.notNullValue());   

        STEP("4. Check Path for Folder: Not Displayed when not requested");
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().getFavorite(folder1.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.onResponse().assertThat().body("entry.target.folder.id", org.hamcrest.Matchers.equalTo(folder1.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("entry.target.folder.path", org.hamcrest.Matchers.nullValue());

        STEP("5. Check Path for file: Not Displayed when not requested");
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().getFavorite(file1.getNodeRefWithoutVersion());
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.onResponse().assertThat().body("entry.target.file.id", org.hamcrest.Matchers.equalTo(file1.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("entry.target.file.path", org.hamcrest.Matchers.nullValue());        
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION, description = "Verify path in get favorites")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION})
    public void testGetFavouritesIncludePath() throws Exception
    {
        STEP("1. Favourite Folder and File");
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().addFolderToFavorites(folder1);
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().addFileToFavorites(file1);

        STEP("2. Check Path: Displayed when appropriate");
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().includePath().getFavorites();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        // Folder
        restClient.onResponse().assertThat().body("list.entries.entry.target.folder.id", org.hamcrest.Matchers.contains(folder1.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("list.entries.entry.target.folder.path", org.hamcrest.Matchers.notNullValue());
        // File
        restClient.onResponse().assertThat().body("list.entries.entry.target.file.id", org.hamcrest.Matchers.contains(file1.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("list.entries.entry.target.file.path", org.hamcrest.Matchers.notNullValue());   
        // Site
        restClient.onResponse().assertThat().body("list.entries.entry.target.site.id", org.hamcrest.Matchers.contains(siteModel1.getId()));
        restClient.onResponse().assertThat().body("list.entries.entry.target.site.path", org.hamcrest.Matchers.contains(org.hamcrest.Matchers.nullValue()));       
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.FAVORITES }, executionType = ExecutionType.REGRESSION, description = "Verify path in post favorites")
    @Test(groups = { TestGroup.REST_API, TestGroup.FAVORITES, TestGroup.REGRESSION})
    public void testPostFavouritesIncludePath() throws Exception
    {
        STEP("1. Favourite Site");
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().deleteSiteFromFavorites(siteModel1);
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().usingParams("include=path").addFavoriteSite(siteModel1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.onResponse().assertThat().body("entry.id", org.hamcrest.Matchers.equalTo(siteModel1.getId()));
        restClient.onResponse().assertThat().body("entry.path", org.hamcrest.Matchers.nullValue());
        
        STEP("2. Favourite Folder");
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().usingParams("include=path").addFolderToFavorites(subFolder1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.onResponse().assertThat().body("entry.target.folder.id", org.hamcrest.Matchers.equalTo(subFolder1.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("entry.target.folder.path", org.hamcrest.Matchers.notNullValue());
        
        STEP("3. Favourite File");
        restClient.authenticateUser(testUser1).withCoreAPI().usingAuthUser().includePath().addFileToFavorites(fileInSubFolder);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.onResponse().assertThat().body("entry.target.file.id", org.hamcrest.Matchers.equalTo(fileInSubFolder.getNodeRefWithoutVersion()));
        restClient.onResponse().assertThat().body("entry.target.file.path", org.hamcrest.Matchers.notNullValue());
    }
}