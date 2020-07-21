package org.alfresco.tas.integration;

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.*;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 10/25/2016.
 */
public class IntegrationSanityTests extends IntegrationTest
{
    UserModel testUser1, testUser2, testUser3;
    SiteModel testSitePublic, testSiteModerated, testSitePrivate, secondTestSitePublic;
    FolderModel testFolder1, testFolder2, testFolder3;
    FileModel testFile1, testFile2, testFile3, testFile4;
    RestCommentModel comment;

    /**
     * Scenario 1
     * 1. Using CMIS create two test users: u1, u2
     * 2. U1 creates a test site using CMIS
     * 3. Using REST API add u2 as member with Collaborator role
     * 4. U2 creates one folder F1 using WEBDAV
     * 5. U2 creates one file in F1 using WebDAV
     * 6. U2 adds a comment to file using REST API
     * 7. U1 gets comments using REST API
     * 8. U1 updates the file content using WEBDAV. Assert with CMIS that content is updated
     * 9. U1 updates comment using REST API
     * 10. U2 gets comments using REST API
     * 11. U2 deletes comment using REST API
     * 12. U1 deletes file using FTP. Assert with CMIS that file doesn't exist in repository
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT, TestGroup.COMMENTS }, executionType = ExecutionType.SANITY,
            description = "Verify two new users are able to manage comments on the same file.")
    public void usersAreAbleToManageComments() throws Exception
    {
        STEP("* 1. Using CMIS create two test users: u1, u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a test site using CMIS");
        testSitePublic =  dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. Using REST API add u2 as member with Collaborator role");
        testUser2.setUserRole(UserRole.SiteCollaborator);
        restAPI.authenticateUser(testUser1).withCoreAPI().usingSite(testSitePublic).addPerson(testUser2);

        STEP("* 4. U2 creates one folder F1 using WEBDAV");
        testFolder1 = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser2).usingSite(testSitePublic).createFolder(testFolder1).and().assertThat().existsInWebdav();

        STEP("* 5. U2 creates one file in F1 using WebDAV");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        webDavProtocol.authenticateUser(testUser2).usingResource(testFolder1).createFile(testFile1).and().assertThat().existsInWebdav();

        STEP("* 6. U2 adds a comment to file using REST API");
        comment = restAPI.authenticateUser(testUser2).withCoreAPI().usingResource(testFile1).addComment("U2 comment");
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        comment.assertThat().field("content").is("U2 comment");

        STEP("* 7. U1 gets comments using REST API");
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingResource(testFile1).getNodeComments()
                .assertThat().entriesListContains("content", "U2 comment");

        STEP("* 8. U1 updates the file content using WEBDAV. Assert with CMIS that content is updated");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile1).update("file content");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile1).assertThat().contentIs("file content");

        STEP("* 9. U1 updates comment using REST API");
        comment = restAPI.withCoreAPI().usingResource(testFile1).updateComment(comment, "U1 updated comment");
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        comment.assertThat().field("content").is("U1 updated comment");

        STEP("* 10. U2 gets comments using REST API");
        restAPI.authenticateUser(testUser2).withCoreAPI()
                .usingResource(testFile1).getNodeComments()
                .assertThat().entriesListContains("content", "U1 updated comment");

        STEP("* 11. U2 deletes comment using REST API");
        restAPI.withCoreAPI().usingResource(testFile1).deleteComment(comment);
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        STEP("* 12. U1 deletes file using FTP. Assert with CMIS that file doesn't exist in repository");
        ftpProtocol.authenticateUser(testUser1).usingResource(testFile1).delete().assertThat().doesNotExistInRepo();
    }

    /**
     * Scenario 2
     * 1. Using CMIS create two test users: u1, u2
     * 2. U1 creates a test site using CMIS
     * 3. Using CMIS add u2 as site member with Contributor role
     * 4. U1 creates one folder F1 using FTP
     * 5. U1 creates one file in F1 using WEBDAV
     * 6. U2 adds file to favorites using REST API
     * 7. U2 gets favorites using REST API. Assert that only file is favorite.
     * 8. U1 gets favorites using REST API. Assert that U1 has his site at favorites.
     * 9. U1 adds file to favorites using REST API
     * 10. U1 adds folder to favorites using REST API
     * 11. U2 remove file from favorites using REST API
     * 12. U2 adds site to favorites using REST API
     * 13. U2 gets favorites using REST API. Assert that only site is favorite.
     * 14. U1 gets favorites using REST API. Assert that site, file and folder are favorites.
     * 15. U1 deletes file using WebDAV. Assert with WebDAV that file doesn't exist in repository
     * 16. U1 gets favorites using REST API. Assert that only site and folder are favorites.
     * 17. U2 gets favorites using REST API. Assert that only site is favorite.
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify two new users are able to manage favorites.")
    public void usersAreAbleToManageFavorites() throws Exception
    {
        STEP("* 1. Using CMIS create two test users: u1, u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a test site using CMIS");
        testSitePublic =  dataSite.usingUser(testUser1).createPublicRandomSite();
        restAPI.authenticateUser(dataUser.getAdminUser());

        STEP("* 3. Using CMIS add u2 as site member with Contributor role");
        dataUser.usingUser(testUser1).addUserToSite(testUser2, testSitePublic, UserRole.SiteContributor);

        STEP("* 4. U1 creates one folder F1 using FTP");
        testFolder1 = FolderModel.getRandomFolderModel();
        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(testFolder1).and().assertThat().existsInRepo();

        STEP("* 5. U1 creates one file in F1 using WEBDAV");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).createFile(testFile1).and().assertThat().existsInWebdav();

        STEP("* 6. U2 adds file to favorites using REST API");
        restAPI.authenticateUser(testUser2).withCoreAPI()
                .usingAuthUser().addFileToFavorites(testFile1)
                .assertThat().field("targetGuid").is(testFile1.getNodeRef().replace(";1.0", ""));

        STEP("* 7. U2 gets favorites using REST API. Assert that only the file is favorite.");
        restAPI.withCoreAPI().usingAuthUser().getFavorites()
                .assertThat().entriesListContains("targetGuid", testFile1.getNodeRef().replace(";1.0", ""))
                .and().paginationField("totalItems").is("1");

        STEP("* 8. U1 gets favorites using REST API. Assert that U1 has his site at favorites.");
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingAuthUser().getFavorites()
                    .assertThat().entriesListContains("targetGuid", testSitePublic.getGuid())
                    .and().paginationField("totalItems").is("1");

        STEP("* 9. U1 adds file to favorites using REST API");
        restAPI.withCoreAPI().usingAuthUser().addFileToFavorites(testFile1)
                .assertThat().field("targetGuid").is(testFile1.getNodeRef().replace(";1.0", ""));

        STEP("* 10. U1 adds folder to favorites using REST API");
        restAPI.withCoreAPI().usingAuthUser().addFolderToFavorites(testFolder1)
                .assertThat().field("targetGuid").is(testFolder1.getNodeRef());

        STEP("* 11. U2 remove file from favorites using REST API");
        restAPI.authenticateUser(testUser2).withCoreAPI()
                .usingAuthUser().deleteFileFromFavorites(testFile1);

        STEP("* 12. U2 adds site to favorites using REST API");
        restAPI.withCoreAPI().usingAuthUser().addSiteToFavorites(testSitePublic)
                .assertThat().field("targetGuid").is(testSitePublic.getGuid());

        STEP("* 13. U2 gets favorites using REST API. Assert that only site is favorite.");
        restAPI.withCoreAPI().usingAuthUser().getFavorites()
                .assertThat().entriesListDoesNotContain("targetGuid", testFile1.getNodeRef().replace(";1.0", "")).and()
                .entriesListContains("targetGuid", testSitePublic.getGuid()).and().paginationField("totalItems").is("1");

        STEP("* 14. U1 gets favorites using REST API. Assert that site, file and folder are favorites.");
        restAPI.authenticateUser(testUser1).withCoreAPI().usingAuthUser().getFavorites()
                .assertThat().entriesListContains("targetGuid", testSitePublic.getGuid())
                .assertThat().entriesListContains("targetGuid", testFile1.getNodeRef().replace(";1.0", ""))
                .and().entriesListContains("targetGuid", testFolder1.getNodeRef())
                .and().paginationField("totalItems").is("3");

        STEP("* 15. U1 deletes file using WebDAV. Assert with WebDAV that file doesn't exist in repository");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile1).delete().and().assertThat().doesNotExistInWebdav();

        STEP("* 16. U1 gets favorites using REST API. Assert that only site and folder are favorites.");
        restAPI.withCoreAPI().usingAuthUser().getFavorites()
                .assertThat().entriesListContains("targetGuid", testSitePublic.getGuid())
                .assertThat().entriesListContains("targetGuid", testFolder1.getNodeRef())
                .and().paginationField("totalItems").is("2");

        STEP("* 17. U2 gets favorites using REST API. Assert that only site is favorite.");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getFavorites()
                .assertThat().entriesListContains("targetGuid", testSitePublic.getGuid())
                .and().paginationField("totalItems").is("1");
    }

    /**
     * Scenario 3
     * 1. Using CMIS create two test users: u1, u2
     * 2. U1 creates a test site using CMIS
     * 3. Using CMIS add u2 as site member with Manager role
     * 4. U1 creates 3 folders: F1 using FTP, F2 using WebDAV as a subfolder of F1, F3 using WEBDAV as a subfolder of F2
     * 5. U1 creates one file in each folder using FTP and WEBDAV
     * 6. U1 renames F2 using IMAP
     * 7. U1 updates file1 using WebDAV
     * 8. U1 deletes file2 using FTP
     * 9. U1 deletes non empty folder3 using WEBDAV
     * 10. Assert with CMIS that U2 is able to see all the above changes
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.SANITY,
            description = "Verify user is able to manage files and folders.")
    public void userIsAbleToManageFilesAndFolders() throws Exception
    {
        STEP("* 1. Using CMIS create two test users: u1, u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createIMAPSite();

        STEP("* 3. Using CMIS add u2 as site member with Manager role");
        dataUser.usingUser(testUser1).addUserToSite(testUser2, testSitePublic, UserRole.SiteManager);

        STEP("* 4. U1 creates 3 folders: F1 using FTP, F2 using WebDAV as a subfolder of F1, F3 using WEBDAV as a subfolder of F2. "
                + "* 5. U1 creates one file in each folder using FTP and WEBDAV");
        testFolder1 = FolderModel.getRandomFolderModel();
        testFolder2 = FolderModel.getRandomFolderModel();
        testFolder3 = FolderModel.getRandomFolderModel();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        testFile2 = FileModel.getRandomFileModel(FileType.HTML);
        testFile3 = FileModel.getRandomFileModel(FileType.MSWORD, "some content");

        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(testFolder1).and().assertThat().existsInFtp();
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).createFolder(testFolder2).assertThat().existsInWebdav()
                .and().createFile(testFile1).assertThat().existsInWebdav();
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder2).createFolder(testFolder3).assertThat().existsInWebdav()
                .and().createFile(testFile2).assertThat().existsInWebdav();
        ftpProtocol.authenticateUser(testUser1).usingResource(testFolder3).createFile(testFile3).then().assertThat().existsInFtp();

        STEP(" * 6. U1 renames F2 using IMAP");
        imapProtocol.authenticateUser(testUser1).usingResource(testFolder2).rename("F2 rename")
                .then().usingResource(testFolder1)
                    .assertThat().contains(new FolderModel("F2 rename"))
                    .and().assertThat().doesNotContain(testFolder2);

        STEP(" * 7. U1 updates file1 using WebDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile1).update("testFile1 content.").then().assertThat().contentIs("testFile1 content.");

        STEP("* 8. U1 deletes file2 using FTP");
        ftpProtocol.authenticateUser(testUser1).usingResource(testFile2).delete().then().assertThat().doesNotExistInRepo();

        STEP(" * 9. U1 deletes non empty folder3 using WEBDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder3).delete().then().assertThat().doesNotExistInWebdav();

        STEP("* 10. Assert with CMIS that U2 is able to see all the above changes");
        cmisAPI.authenticateUser(testUser2)
                .usingResource(testFolder1).assertThat().hasChildren(testFile1)
                .usingResource(testFile1).assertThat().contentIs("testFile1 content.")
                .usingResource(testFile2).assertThat().doesNotExistInRepo()
                .usingResource(testFolder3).assertThat().doesNotExistInRepo()
                .usingResource(testFile3).assertThat().doesNotExistInRepo();

        FolderModel renamedFolder = cmisAPI.usingResource(testFolder1).getFolders().get(0);
        Assert.assertEquals(renamedFolder.getName(), "F2 rename", "User2 is able to see the renamed folder");
    }

    /**
     * Scenario 4
     * 1. Using CMIS create two test users: u1, u2
     * 2. U1 creates a test site using CMIS
     * 3. Using REST API add u2 as site member with Collaborator role
     * 4. U2 creates file1 with using CMIS
     * 5. U1 creates file2 from source using CMIS
     * 6. U1 uploads file3 using FTP
     * 7. U1 creates file4 using CMIS
     * 8. U1 marks file1 and 3 as favorite using REST API
     * 9. U2 marks file 1, 2, 4 as favorite using REST API
     * 10. U2 marks site as favorite using REST API
     * 11. U1 removes file1 from favorites using REST API
     * 12. U2 checks file favorites using REST API. Assert that U2 gets only his favorite files.
     * 13. U2 removes file1 from favorites using REST API
     * 14. U2 checks file favorites using REST API. Assert that U2 gets only his favorite files.
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify two new users are able to manage favorite files.")
    public void usersAreAbleToManageFavoriteFiles() throws Exception
    {
        STEP("* 1. Using CMIS create two test users: u1, u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        restAPI.authenticateUser(dataUser.getAdminUser());
        testSitePublic.setGuid(restAPI.withCoreAPI().usingSite(testSitePublic).getSite().getGuid());

        STEP("* 3. Using REST API add u2 as site member with Collaborator role");
        testUser2.setUserRole(UserRole.SiteCollaborator);
        restAPI.authenticateUser(testUser1).withCoreAPI().usingSite(testSitePublic).addPerson(testUser2);

        STEP(" * 4. U2 creates file1 with using CMIS");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        cmisAPI.authenticateUser(testUser2).usingSite(testSitePublic).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 5. U1 creates file2 from source using CMIS");
        testFile2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic).createFileFromSource(testFile2, testFile1).and().refreshResource().then()
                .assertThat().existsInRepo();

        STEP(" * 6. U1 uploads file3 using FTP");
        testFile3 = FileModel.getFileModelBasedOnTestDataFile("testUploadFile.txt");
        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFile(testFile3).then().assertThat().existsInRepo();

        STEP("* 7. U1 creates file4 using CMIS");
        testFile4 = FileModel.getRandomFileModel(FileType.MSWORD, "file4 content");
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic).createFile(testFile4).then().assertThat().existsInRepo();

        STEP("* 8. U1 marks file1 and 3 as favorite using REST API");
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingAuthUser().addFileToFavorites(testFile1)
                .assertThat().field("targetGuid").is(testFile1.getNodeRef().replace(";1.0", ""));
        restAPI.withCoreAPI().usingAuthUser().addFileToFavorites(testFile3)
                .assertThat().field("targetGuid").is(testFile3.getNodeRef().replace(";1.0", ""));

        STEP("* 9. U2 marks file 1, 2, 4 as favorite using REST API");
        restAPI.authenticateUser(testUser2).withCoreAPI()
                .usingAuthUser().addFileToFavorites(testFile1)
                .assertThat().field("targetGuid").is(testFile1.getNodeRef().replace(";1.0", ""));
        restAPI.withCoreAPI().usingAuthUser().addFileToFavorites(testFile2)
                .assertThat().field("targetGuid").is(testFile2.getNodeRef().replace(";1.0", ""));
        restAPI.withCoreAPI().usingAuthUser().addFileToFavorites(testFile4)
                .assertThat().field("targetGuid").is(testFile4.getNodeRef().replace(";1.0", ""));

        STEP("* 10. U2 marks site as favorite using REST API");
        restAPI.withCoreAPI().usingAuthUser().addSiteToFavorites(testSitePublic)
                .assertThat().field("targetGuid").is(testSitePublic.getGuid());

        STEP("* 11. U1 removes file1 from favorites using REST API");
        restAPI.authenticateUser(testUser1).withCoreAPI().usingAuthUser().deleteFileFromFavorites(testFile1);

        STEP("* 12. U2 checks file favorites using REST API. Assert that U2 gets only his favorite files.");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser()
                .where().targetFileExist().getFavorites().assertThat()
                .entriesListContains("targetGuid", testFile1.getNodeRef().replace(";1.0", "")).and()
                .entriesListContains("targetGuid", testFile2.getNodeRef().replace(";1.0", "")).and()
                .entriesListContains("targetGuid", testFile4.getNodeRef().replace(";1.0", "")).and()
                .paginationField("totalItems").is("3");

        STEP("* 13. U2 removes file1 from favorites using REST API");
        restAPI.withCoreAPI().usingAuthUser().deleteFileFromFavorites(testFile1);

        STEP("* 14. U2 checks file favorites using REST API. Assert that U2 gets only his favorite files.");
        restAPI.withCoreAPI().usingAuthUser()
                .where().targetFileExist().getFavorites().assertThat()
                .entriesListContains("targetGuid", testFile2.getNodeRef().replace(";1.0", "")).and()
                .entriesListContains("targetGuid", testFile4.getNodeRef().replace(";1.0", "")).and()
                .paginationField("totalItems").is("2");
    }

    /**
     * Scenario 5
     * 1. Using CMIS create a test user: u1
     * 2. U1 creates a test site using CMIS
     * 3. U1 creates 2 files in site's document library using FTP
     * 4. U1 creates folder1 in site's document library using WebDAV
     * 5. U1 creates another 2 files in folder1 using WEBDAV
     * 6. U1 adds tag1, tag2 on file1, tag2 on file2 using REST API
     * 7. U1 adds tag1 on folder1, tag3 on file3 using REST API
     * 8. U1 gets tags and verify all tags are listed
     * 9. U1 gets file1 tags and verify only tag1 and tag2 are listed
     * 10. U1 deletes tag2 from file1
     * 11. U1 deletes file3 using WebDAV
     * 12. U1 gets tags and verify all tags are listed
     * 13. U1 gets file1 tags and verify tag2 was removed.
     * 14. U1 gets folder1 tags and verify only tag1 is listed.
     * 15. U1 deletes non empty folder1 using CMIS
     * 16. U1 gets tags and verify all tags are listed
     */
    @Bug(id = "REPO-4789")
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT, TestGroup.TAGS }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to manage tags.")
    public void siteManagerIsAbleToManageTags() throws Exception
    {
        STEP("* 1. Using CMIS create a test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. U1 creates 2 files in site's document library using FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        testFile2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file2 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic)
                .createFile(testFile1).then().assertThat().existsInRepo()
                .createFile(testFile2).then().assertThat().existsInRepo();

        STEP("* 4. U1 creates folder1 in site's document library using WebDAV");
        testFolder1 = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(testFolder1).then().assertThat().existsInRepo();

        STEP("* 5. U1 creates another 2 files in folder1 using WEBDAV");
        testFile3 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file3 content");
        testFile4 = FileModel.getRandomFileModel(FileType.MSWORD, "file4 content");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1)
                .createFile(testFile3).then().assertThat().existsInRepo()
                .createFile(testFile4).then().assertThat().existsInRepo();

        STEP("* 6. U1 adds tag1, tag2 on file1, tag2 on file2 using REST API");
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingResource(testFile1).addTags("integration_tag1", "integration_tag2")
                .assertThat().entriesListContains("tag", "integration_tag1")
                .and().entriesListContains("tag", "integration_tag2");
        RestTagModel tag2 = restAPI.withCoreAPI()
                .usingResource(testFile2).addTag("integration_tag2")
                .assertThat().field("tag").is("integration_tag2");

        STEP("* 7. U1 adds tag1 on folder1, tag3 on file3 using REST API");
        restAPI.withCoreAPI()
                .usingResource(testFolder1).addTag("integration_tag1")
                .assertThat().field("tag").is("integration_tag1");

        restAPI.withCoreAPI()
                .usingResource(testFile3).addTag("integration_tag3")
                .assertThat().field("tag").is("integration_tag3");

        STEP("* 8. U1 gets tags and verify all tags are listed");
        Utility.sleep(500, 30000, () ->
                restAPI.withParams("maxItems=10000").withCoreAPI().getTags()
                        .assertThat().entriesListContains("tag", "integration_tag1")
                        .and().entriesListContains("tag", "integration_tag2")
                        .and().entriesListContains("tag", "integration_tag3"));
        STEP("* 9. U1 gets file1 tags and verify only tag1 and tag2 are listed");
        restAPI.withCoreAPI()
                .usingResource(testFile1).getNodeTags()
                .assertThat().entriesListContains("tag", "integration_tag1")
                .and().entriesListContains("tag", "integration_tag2")
                .and().paginationField("totalItems").is("2");

        STEP("* 10. U1 deletes tag2 from file1");
        restAPI.withCoreAPI().usingResource(testFile1).deleteTag(tag2);
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        STEP("* 11. U1 deletes file3 using WebDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile3).delete().then().assertThat().doesNotExistInWebdav();

        STEP("* 12. U1 gets tags and verify all tags are listed");
        restAPI.withParams("maxItems=10000").withCoreAPI().getTags()
                .assertThat().entriesListContains("tag", "integration_tag1")
                .and().entriesListContains("tag", "integration_tag2")
                .and().entriesListContains("tag", "integration_tag3");

        STEP(" * 13. U1 gets file1 tags and verify tag2 was removed.");
        restAPI.withCoreAPI().usingResource(testFile1).getNodeTags()
                .assertThat().entriesListContains("tag", "integration_tag1")
                .and().entriesListDoesNotContain("tag", "integration_tag2")
                .and().paginationField("totalItems").is("1");

        STEP("* 14. U1 gets folder1 tags and verify only tag1 is listed.");
        restAPI.withCoreAPI().usingResource(testFolder1).getNodeTags()
                .assertThat().entriesListContains("tag", "integration_tag1")
                .and().paginationField("totalItems").is("1");

        STEP("* 15. U1 deletes non empty folder1 using CMIS");
        cmisAPI.authenticateUser(testUser1).usingResource(testFolder1).deleteFolderTree().assertThat().doesNotExistInRepo();

        STEP("* 16. U1 gets tags and verify all tags are listed");
        restAPI.withParams("maxItems=10000").withCoreAPI().getTags()
                .assertThat().entriesListContains("tag", "integration_tag1")
                .and().entriesListContains("tag", "integration_tag2")
                .and().entriesListContains("tag", "integration_tag3");
    }

    /**
     * Scenario 6
     * 1. Using CMIS create 3 test user: u1, u2, u3
     * 2. Using CMIS U1 creates a public test site, a moderated test site and a private test site
     * 3. Using REST API, U2 request to join the public site
     * 4. Using REST API, U3 request to join the public site
     * 5. Using REST API, check that u2 and u3 are added to public site with Consumer role
     * 6. Using REST API, U2 request to join the moderated site
     * 7. Using REST API, U3 request to join the moderated site
     * 8. Using REST API, check that u2 and u3 requests to join the moderated site are added to site membership request list
     * 9. Using REST API, U1 cancels the request for U3
     * 10. Using REST API, check that u2 can't make requests to join the private site
     * 11. Using REST API, check that only u2 request remains in site membership request list
     * 12. Using REST API, U1 change U2 role to Collaborator in public site
     * 13. Using REST API, get site membership information for each user
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to manage site membership.")
    public void siteManagerIsAbleToManageSiteMembership() throws Exception
    {
        STEP("* 1. Using CMIS create 3 test user: u1, u2, u3");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();
        testUser3 = dataUser.createRandomTestUser();

        STEP(" * 2. Using CMIS U1 creates a public test site, a moderated test site and a private test site");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        testSiteModerated = dataSite.usingUser(testUser1).createModeratedRandomSite();
        testSitePrivate = dataSite.usingUser(testUser1).createPrivateRandomSite();

        STEP("* 3. Using REST API, U2 request to join the public site");
        restAPI.authenticateUser(testUser2).withCoreAPI()
                .usingAuthUser().addSiteMembershipRequest(testSitePublic);

        STEP("* 4. Using REST API, U3 request to join the public site");
        restAPI.authenticateUser(testUser3).withCoreAPI()
                .usingAuthUser().addSiteMembershipRequest(testSitePublic);

        STEP(" * 5. Using REST API, check that u2 and u3 are added to public site with Consumer role");
        restAPI.withCoreAPI()
                .usingSite(testSitePublic).getSiteMembers()
                .assertThat().entriesListContains("id", testUser2.getUsername())
                .and().entriesListContains("id", testUser3.getUsername());
        restAPI.withCoreAPI()
                .usingSite(testSitePublic).getSiteMember(testUser2)
                .assertThat().field("role").is("SiteConsumer");

        restAPI.withCoreAPI()
                .usingSite(testSitePublic).getSiteMember(testUser3)
                .assertThat().field("role").is("SiteConsumer");

        STEP("* 6. Using REST API, U3 request to join the moderated site");
        restAPI.withCoreAPI()
                .usingAuthUser().addSiteMembershipRequest(testSiteModerated);

        STEP("* 7. Using REST API, U2 request to join the moderated site");
        restAPI.authenticateUser(testUser2).withCoreAPI()
                .usingAuthUser().addSiteMembershipRequest(testSiteModerated);

        STEP("* 8. Using REST API, check that u2 and u3 requests to join the moderated site are added to site membership request list");
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingSite(testSiteModerated).getSiteMembers()
                .assertThat().entriesListDoesNotContain("id", testUser2.getUsername())
                .and().entriesListDoesNotContain("id", testUser3.getUsername());

        restAPI.withCoreAPI()
                .usingUser(testUser2).getSiteMembershipRequest(testSiteModerated)
                .assertThat().field("id").is(testSiteModerated.getId());

        restAPI.withCoreAPI()
                .usingUser(testUser3).getSiteMembershipRequest(testSiteModerated)
                .assertThat().field("id").is(testSiteModerated.getId());

        STEP("* 9. Using REST API, U1 cancels the request for U3");
        restAPI.withCoreAPI().usingUser(testUser3).deleteSiteMembershipRequest(testSiteModerated);

        STEP("* 10. Using REST API, check that u2 can't make requests to join the private site.");
        restAPI.authenticateUser(testUser2).withCoreAPI()
                .usingAuthUser().addSiteMembershipRequest(testSitePrivate);
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND);

        STEP("* 11. Using REST API, check that only u2 request remains in site membership request list");
        restAPI.withCoreAPI()
                .usingAuthUser().getSiteMembershipRequests()
                .assertThat().entriesListContains("id", testSiteModerated.getId())
                .and().paginationField("totalItems").is("1");

        restAPI.authenticateUser(testUser3).withCoreAPI()
                .usingAuthUser().getSiteMembershipRequests()
                .assertThat().entriesListIsEmpty();

        STEP("* 12. Using REST API, U1 change U2 role to Collaborator in public site");
        testUser2.setUserRole(UserRole.SiteCollaborator);
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingSite(testSitePublic).updateSiteMember(testUser2)
                .assertThat().field("role").is(testUser2.getUserRole());

        STEP("* 13. Using REST API, get site membership information for each user");
        restAPI.withCoreAPI()
                .usingAuthUser().getSitesMembershipInformation()
                .assertThat().paginationField("count").is("3");
        restAPI.withCoreAPI()
                .usingUser(testUser2).getSitesMembershipInformation()
                .assertThat().entriesListContains("id", testSitePublic.getId()).and()
                .entriesListContains("role", "SiteCollaborator").and().paginationField("count").is("1");
        restAPI.withCoreAPI()
                .usingUser(testUser3).getSitesMembershipInformation()
                .assertThat().entriesListContains("id", testSitePublic.getId()).and()
                .entriesListContains("role", "SiteConsumer").and().paginationField("count").is("1");
    }

    /**
     * Scenario 7
     * 1. Using CMIS create 1 test user: u1
     * 2. Using CMIS U1 creates a public site, a moderated test site and a private test site
     * 3. Using WEBDAV, U1 adds file1 to site1
     * 4. Using REST API, check the u1 favorites sites list
     * 5. Using REST API, U1 removes site1 from favorites
     * 6. Using REST API, check the u1 favorites sites list
     * 7. Using CMIS, U1 deletes site2
     * 8. Using REST API, check the u1 favorites sites list
     * 9. Using REST API, U1 marks site1 as favorite
     * 10. Using REST API, check the u1 favorites sites list
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.SITES, TestGroup.FAVORITES }, executionType = ExecutionType.SANITY,
            description = "Verify users are able to manage favorite sites.")
    public void userIsAbleToManageFavoriteSites() throws Exception
    {
        STEP("* 1. Using CMIS create a test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("* 2. Using CMIS U1 creates a public site, a moderated test site and a private test site");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        testSiteModerated = dataSite.usingUser(testUser1).createModeratedRandomSite();
        testSitePrivate = dataSite.usingUser(testUser1).createPrivateRandomSite();

        STEP("* 3. Using WEBDAV, U1 adds file1 to site1");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFile(testFile1).assertThat().existsInRepo();

        STEP("* 4. Using REST API, check the u1 favorites sites list");
        restAPI.authenticateUser(testUser1)
                .withCoreAPI().usingAuthUser().where().targetSiteExist().getFavorites().assertThat()
                .entriesListContains("targetGuid", testSitePublic.getGuid()).and()
                .entriesListContains("targetGuid", testSiteModerated.getGuid()).and()
                .entriesListContains("targetGuid", testSitePrivate.getGuid()).and()
                .paginationField("totalItems").is("3");

        STEP("* 5. Using REST API, U1 removes site1 from favorites");
        restAPI.withCoreAPI().usingAuthUser().deleteSiteFromFavorites(testSitePublic);

        STEP("* 6. Using REST API, check the u1 favorites sites list");
        restAPI.withCoreAPI().usingAuthUser()
                .where().targetSiteExist().getFavorites().assertThat()
                .entriesListContains("targetGuid", testSiteModerated.getGuid()).and()
                .entriesListContains("targetGuid", testSitePrivate.getGuid()).and()
                .paginationField("totalItems").is("2");

        STEP("* 7. Using CMIS, U1 deletes site2");
        dataSite.usingUser(testUser1).deleteSite(testSiteModerated);

        STEP("* 8. Using REST API, check the u1 favorites sites list");
        restAPI.withCoreAPI().usingAuthUser()
                .where().targetSiteExist().getFavorites().assertThat()
                .entriesListContains("targetGuid", testSitePrivate.getGuid()).and()
                .paginationField("totalItems").is("1");

        STEP("* 9. Using REST API, U1 marks site1 as favorite");
        restAPI.withCoreAPI().usingAuthUser().addSiteToFavorites(testSitePublic)
                .assertThat().field("targetGuid").is(testSitePublic.getGuid());

        STEP("* 10. Using REST API, check the u1 favorites sites list");
        restAPI.withCoreAPI().usingAuthUser().where().targetSiteExist().getFavorites().assertThat()
                .entriesListContains("targetGuid", testSitePublic.getGuid()).and()
                .entriesListContains("targetGuid", testSitePrivate.getGuid()).and()
                .paginationField("totalItems").is("2");
    }

    /**
     * Scenario 10
     * 1. Using CMIS create 3 test user: u1, u2, u3
     * 2. Using CMIS U1 creates a a moderated test site
     * 3. Using REST API, U2 request to join the moderated site
     * 4. Using REST API, U3 request to join the moderated site
     * 5. Using REST API, check that u2 and u3 requests to join the moderated site are added to site membership request list
     * 6. Using REST API, U1 cancels the request for U3
     * 7. Using REST API, check that only u2 request remains in site membership request list
     * 8. Using REST API, U1 cancels the request for U2
     * 9. Using REST API, verify that there is no request to join left in the site membership request list.
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to cancel requests to join moderated site.")
    public void siteManagerIsAbleToCancelSiteMembership() throws Exception
    {
        STEP("* 1. Using CMIS create 3 test user: u1, u2, u3");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();
        testUser3 = dataUser.createRandomTestUser();

        STEP("* 2. Using CMIS U1 creates a a moderated test site");
        testSiteModerated = dataSite.usingUser(testUser1).createModeratedRandomSite();

        STEP("* 3. Using REST API, U2 request to join the moderated site");
        restAPI.authenticateUser(testUser2).withCoreAPI()
                .usingAuthUser().addSiteMembershipRequest(testSiteModerated);

        STEP("* 4. Using REST API, U3 request to join the moderated site");
        restAPI.authenticateUser(testUser3).withCoreAPI()
                .usingAuthUser().addSiteMembershipRequest(testSiteModerated);

        STEP("* 5. Using REST API, check that u2 and u3 requests to join the moderated site are added to site membership request list");
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingUser(testUser2).getSiteMembershipRequest(testSiteModerated)
                .assertThat().field("id").is(testSiteModerated.getId());
        restAPI.withCoreAPI()
                .usingUser(testUser3).getSiteMembershipRequest(testSiteModerated)
                .assertThat().field("id").is(testSiteModerated.getId());

        STEP("* 6. Using REST API, U1 cancels the request for U3");
        restAPI.withCoreAPI()
                .usingUser(testUser3).deleteSiteMembershipRequest(testSiteModerated);

        STEP("* 7. Using REST API, check that u3 request isn't in site membership request list");
        restAPI.withCoreAPI()
                .usingUser(testUser3).getSiteMembershipRequest(testSiteModerated);
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "The relationship resource"))
                .containsSummary(testUser3.getUsername());

        STEP("* 8. Using REST API, U1 cancels the request for U2");
        restAPI.withCoreAPI()
                .usingUser(testUser2).deleteSiteMembershipRequest(testSiteModerated);

        STEP("* 9. Using REST API, check that u2 request isn't in site membership request list.");
        restAPI.withCoreAPI()
                .usingUser(testUser2).getSiteMembershipRequest(testSiteModerated);
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "The relationship resource"))
                .containsSummary(testUser2.getUsername());
    }
    
    /**
     * Scenario 11
     * 1. Using CMIS create 2 test user: u1, u2
     * 2. Using CMIS U1 creates 3 test sites
     * 3. Using CMIS U1 adds U2 to site1 with "Consumer" role
     * 4. Using CMIS U1 adds U2 to site2 with "Collaborator" role
     * 5. Using REST API, admin adds U2 to site3 with "Manager" role
     * 6. Using REST API, verify that site1, site2, site3 are present in the list of sites for U2
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Verify user is able to get site membership informations.")
    public void userIsAbleToGetSiteMembershipInfo() throws Exception
    {
        STEP("* 1. Using CMIS create 2 test user: u1, u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. Using CMIS U1 creates 3 test sites");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        testSiteModerated = dataSite.usingUser(testUser1).createModeratedRandomSite();
        testSitePrivate = dataSite.usingUser(testUser1).createPrivateRandomSite();

        STEP("* 3. Using CMIS U1 adds U2 to site1 with \"Consumer\" role");
        dataUser.usingUser(testUser1).addUserToSite(testUser2, testSitePublic, UserRole.SiteConsumer);

        STEP("* 4. Using CMIS U1 adds U2 to site2 with \"Collaborator\" role");
        dataUser.usingUser(testUser1).addUserToSite(testUser2, testSiteModerated, UserRole.SiteCollaborator);

        STEP("* 5. Using REST API, admin adds U2 to site3 with \"Manager\" role");
        testUser2.setUserRole(UserRole.SiteManager);
        restAPI.authenticateUser(dataUser.getAdminUser()).withCoreAPI()
                .usingSite(testSitePrivate).addPerson(testUser2);

        STEP("* 6. Using REST API, verify that site1, site2, site3 are present in the list of sites for U2");
        restAPI.withCoreAPI()
                .usingUser(testUser2).getSitesMembershipInformation()
                .assertThat().entriesListContains("id", testSitePublic.getId())
                .and().entriesListContains("id", testSiteModerated.getId())
                .and().entriesListContains("id", testSitePrivate.getId())
                .and().paginationField("count").is("3");

        restAPI.withCoreAPI()
                .usingSite(testSitePublic).getSiteMember(testUser2)
                .assertThat().field("role").is("SiteConsumer");
        restAPI.withCoreAPI()
                .usingSite(testSiteModerated).getSiteMember(testUser2)
                .assertThat().field("role").is("SiteCollaborator");
        restAPI.withCoreAPI()
                .usingSite(testSitePrivate).getSiteMember(testUser2)
                .assertThat().field("role").is("SiteManager");
    }
    
    /**
     * Scenario 12
     * 1. Using CMIS create a test user: u1
     * 2. U1 creates a test site using CMIS
     * 3. U1 creates a file in site's document library using WebDAV
     * 4. U1 creates another site using CMIS
     * 5. U1 creates a folder in site's document library using FTP
     * 6. U1 copy file to folder using WEBDAV
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.SANITY,
            description = "Verify user is able to copy file to a folder from another site.")
    public void userIsAbleToCopyFileToAFolderFromAnotherSite() throws Exception
    {
        STEP("* 1. Using CMIS create a test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. U1 creates a file in site's document library using WebDAV");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 4. U1 creates another site using CMIS");
        secondTestSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 5. U1 creates a folder in second site's document library using FTP");
        testFolder1 = FolderModel.getRandomFolderModel();
        ftpProtocol.authenticateUser(testUser1).usingSite(secondTestSitePublic).createFolder(testFolder1).then().assertThat().existsInRepo();

        STEP("* 6. U1 copy file to folder using WEBDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile1).copyTo(testFolder1).assertThat().existsInRepo();
    }
    
    /**
     * Scenario 13
     * 1. Using CMIS create a test user: u1
     * 2. U1 creates a test site using CMIS
     * 3. U1 creates a file in site's document library using FTP
     * 4. U1 creates another site using CMIS
     * 5. U1 creates a folder in site's document library using WebDAV
     * 6. U1 moves file to folder using FTP
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.SANITY,
            description = "Verify user is able to move file to a folder from another site.")
    public void userIsAbleToMoveFileToAFolderFromAnotherSite() throws Exception
    {
        STEP("* 1. Using CMIS create a test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. U1 creates a file in site's document library using FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 4. U1 creates another site using CMIS");
        secondTestSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 5. U1 creates a folder in second site's document library using WebDAV");
        testFolder1 = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser1).usingSite(secondTestSitePublic).createFolder(testFolder1).then().assertThat().existsInRepo();

        STEP("* 6. U1 moves file to folder using FTP");
        ftpProtocol.authenticateUser(testUser1).usingResource(testFile1).moveTo(testFolder1).assertThat().existsInRepo();
    }
    
    /**
     * Scenario 14
     * 1. Using CMIS create a test user: u1
     * 2. U1 creates a public test site, a moderated test site and a private test site using CMIS 
     * 3. U1 gets each site with REST API: public, moderated and private sites are listed
     * 4. U1 deletes moderated site using CMIS 
     * 5. U1 gets each site with REST API: public and private sites are listed
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Verify user is able to get created sites with different visibility.")
    public void userIsAbleToGetSitesWithDifferentVisibility() throws Exception
    {
        STEP("* 1. Using CMIS create a test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a public test site, a moderated test site and a private test site using CMIS ");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        testSiteModerated = dataSite.usingUser(testUser1).createModeratedRandomSite();
        testSitePrivate = dataSite.usingUser(testUser1).createPrivateRandomSite();

        STEP("* 3. U1 gets each site with REST API: public, moderated and private sites are listed");
        restAPI.authenticateUser(testUser1);
        
        List<SiteModel> createdSites = new ArrayList<SiteModel>();
        createdSites.add(testSitePublic);
        createdSites.add(testSiteModerated);
        createdSites.add(testSitePrivate);
        
        for(SiteModel site : createdSites)
        {
            restAPI.withCoreAPI()
                    .usingSite(site).getSite()
                    .assertThat().field("id").is(site.getId())
                    .and().field("title").is(site.getTitle());
            restAPI.assertStatusCodeIs(HttpStatus.OK);
        }     

        STEP("* 4. U1 deletes moderated site using CMIS");
        dataSite.usingUser(testUser1).deleteSite(testSiteModerated);

        STEP("* 5. U1 gets each site with REST API: public and private sites are listed");
        restAPI.withParams("maxItems=10000").withCoreAPI().getSites()
                .assertThat().entriesListContains("id", testSitePublic.getId())
                .assertThat().entriesListContains("id", testSitePrivate.getId())
                .assertThat().entriesListDoesNotContain("id", testSiteModerated.getId());   
    }
    
    /**
     * Scenario 15
     * 1. Using CMIS create two users: u1 and u2
     * 2. U1 creates a public site using CMIS
     * 3. U1 creates a file in public site's document library using FTP
     * 4. U2 creates a private site using CMIS
     * 5. U2 creates a folder in public site's document library using WebDAV
     * 6. U1 copies the file in private site's document library using WEBDAV
     * 7. Using WEBDAV, verify that file was not copied
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.SANITY,
            description = "Verify uninvited user is not able to copy document to a private site.")
    public void userIsNotAbleToCopyAFileToAPrivateSite() throws Exception
    {
        STEP("* 1. Using CMIS create two users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a public site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. U1 creates a file in public site's document library using FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 4. U2 creates a private site using CMIS");
        testSitePrivate = dataSite.usingUser(testUser2).createPrivateRandomSite();

        STEP("* 5. U2 creates a folder in public site's document library using WebDAV");
        testFolder1 = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser2).usingSite(testSitePrivate).createFolder(testFolder1).then().assertThat().existsInRepo();

        STEP("* 6. U1 copies the file in private site's document library using WEBDAV"
                + "* 7. Using WEBDAV, verify that file was not copied");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile1).copyTo(testFolder1).assertThat().doesNotExistInRepo();
    }
    
    /**
     * Scenario 16
     * 1. Using CMIS create two users: u1 and u2
     * 2. U1 creates a public site using CMIS
     * 3. U1 adds U2 to site with "Consumer" role using CMIS
     * 4. Using REST API, verify membership information for u2 on public site
     * 5. Using REST API, U1 change U2 role to Collaborator in public site
     * 6. Using REST API, verify membership information for u2 on public site
     * 7. Using REST API, U1 remove U2 from public site
     * 8. Using REST API, verify that u2 isn't member of u1 public site
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Verify user is able to get user membership information.")
    public void userIsAbleToGetASiteMember() throws Exception
    {
        STEP("* 1: Using CMIS create two users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2: U1 creates a public site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. U1 adds U2 to site with \"Consumer\" role using CMIS");
        dataUser.usingUser(testUser1).addUserToSite(testUser2, testSitePublic, UserRole.SiteConsumer);

        STEP("* 4. Using REST API, verify membership information for u2 on public site");
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingSite(testSitePublic).getSiteMember(testUser2)
                .assertThat().field("role").is("SiteConsumer");

        STEP("* 5. Using REST API, U1 change U2 role to Collaborator in public site");
        testUser2.setUserRole(UserRole.SiteCollaborator);
        restAPI.withCoreAPI()
                .usingSite(testSitePublic).updateSiteMember(testUser2)
                .assertThat().field("role").is(testUser2.getUserRole());

        STEP("* 6. Using REST API, verify membership information for u2 on public site");
        restAPI.withCoreAPI()
                .usingSite(testSitePublic).getSiteMember(testUser2)
                .assertThat().field("role").is("SiteCollaborator");

        STEP("* 7. Using REST API, U1 remove U2 from public site");
        restAPI.withCoreAPI()
                .usingSite(testSitePublic).deleteSiteMember(testUser2);
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        STEP("* 8. Using REST API, verify that u2 isn't member of u1 public site");
        restAPI.withCoreAPI()
                .usingSite(testSitePublic).getSiteMember(testUser2);
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format("The relationship resource was not found for the entity with id: %s and a relationship id of %s", testUser2.getUsername(), testSitePublic.getId()));
    }
    
    /**
     * Scenario 17
     * 1. Using CMIS create 3 test user: u1, u2, u3
     * 2. Using CMIS U1 creates a public site
     * 3. Using CMIS U1 adds U2 to site with "Contributor" role
     * 4. Using CMIS U1 adds U3 to site with "Collaborator" role
     * 5. Using REST API, verify the information related to site's containers and members
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.SITES }, executionType = ExecutionType.SANITY,
            description = "Verify user is able to get site membership informations.")
    public void userIsAbleToGetSite() throws Exception
    {
        STEP("* 1. Using CMIS create 3 test user: u1, u2, u3");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();
        testUser3 = dataUser.createRandomTestUser();

        STEP("* 2. Using CMIS U1 creates a public site");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. Using CMIS U1 adds U2 to site with \"Contributor\" role");
        dataUser.usingUser(testUser1).addUserToSite(testUser2, testSitePublic, UserRole.SiteContributor);

        STEP("* 4. Using CMIS U1 adds U3 to site with \"Collaborator\" role");
        dataUser.usingUser(testUser1).addUserToSite(testUser3, testSitePublic, UserRole.SiteCollaborator);

        STEP("* 5. Using REST API, verify the information related to site's containers and members");
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingSite(testSitePublic).getSite()
                .assertThat().field("visibility").is("PUBLIC")
                .and().field("id").is(testSitePublic.getId());
        restAPI.withCoreAPI()
                .usingSite(testSitePublic).getSiteMembers()
                .assertThat().paginationField("count").is("3");
        restAPI.withCoreAPI()
                .usingSite(testSitePublic).getSiteContainers()
                .assertThat().entriesListContains("folderId", "documentLibrary")
                .and().paginationField("totalItems").is("1");
    }

    /**
     * Scenario 18
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates a public test site and U2 a moderated test site using CMIS
     * 3. U1 creates a folder with a file in public site's document library using WEBDAV
     * 4. U2 creates a folder in moderated site's document library using FTP
     * 5. U1 tries to copy the folder to moderated site using WebDAV
     * 6. Verify folder is not copied. U1 is not authorized to access the moderated site
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.SANITY,
            description = "Verify user is not able to copy folder to a moderated site if it is not a member of that site.")
    public void copyFolderToModeratedSiteByUninvitedUser() throws Exception
    {
        STEP("* 1. Using CMIS create two users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a public test site and U2 a moderated test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        testSiteModerated = dataSite.usingUser(testUser2).createModeratedRandomSite();

        STEP("* 3. U1 creates a folder with a file in public site's document library using WEBDAV");
        testFolder1 = FolderModel.getRandomFolderModel();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(testFolder1).assertThat().existsInWebdav()
                .then().usingResource(testFolder1).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 4. U2 creates a folder in moderated site's document library using FTP");
        testFolder2 = FolderModel.getRandomFolderModel();
        ftpProtocol.authenticateUser(testUser2).usingSite(testSiteModerated).createFolder(testFolder2).assertThat().existsInRepo();

        STEP("* 5. U1 tries to copy the folder to moderated site using WebDAV"
                + "* 6. Verify folder is not copied. U1 is not authorized to access the moderated site");
        testFolder2.setProtocolLocation(webDavProtocol.getPrefixSpace() + testFolder2.getCmisLocation());
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).copyTo(testFolder2).assertThat().doesNotExistInRepo()
        .and().assertThat().doesNotExistInWebdav();
    }
    
    /**
     * Scenario 20
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates a public test site and U2 a private test site using CMIS
     * 3. U1 creates a file in public site's document library using WEBDAV
     * 4. U2 creates a folder in private site's document library using WebDAV
     * 5. U1 tries to move his file to folder from U2 private site using IMAP
     * 6. Verify file is not moved. U1 is not authorized to access the private site
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.SANITY  }, expectedExceptions = MessagingException.class)
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.SANITY,
        description = "Verify user is not able to move file to a private site if he is not a member of that site.")
    public void moveFileToPrivateSiteByUninvitedUser() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public test site and U2 a private test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        testSitePrivate = dataSite.usingUser(testUser2).createPrivateRandomSite();

        STEP("3. U1 creates a file in public site's document library using WEBDAV");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFile(testFile1).assertThat().existsInRepo();

        STEP("4. U2 creates a folder in private site's document library using WebDAV");
        testFolder1 = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser2).usingSite(testSitePrivate).createFolder(testFolder1).assertThat().existsInRepo();

        STEP("5. U1 tries to move his file to folder from U2 private site using IMAP. 6. Verify file is not moved. U1 is not authorized to access the private site ");
        testFolder1.setProtocolLocation(testFolder1.getCmisLocation());
        imapProtocol.authenticateUser(testUser1).usingResource(testFile1).moveMessageTo(testFolder1).assertThat().doesNotContainMessages(testFile1);
    }
}
