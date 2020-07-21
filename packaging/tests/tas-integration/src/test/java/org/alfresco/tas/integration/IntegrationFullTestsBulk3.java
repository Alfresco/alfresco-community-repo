package org.alfresco.tas.integration;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.model.RestCommentModelsCollection;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IntegrationFullTestsBulk3 extends IntegrationTest
{
    private UserModel testUser1, testUser2;
    private SiteModel publicSite, moderatedSite, privateSite;
    private RestCommentModelsCollection comments;
    private FileModel testFile1, testFile2, wordFile ;
    private ListUserWithRoles usersWithRoles;
    private FolderModel testFolder1;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser1 = dataUser.createRandomTestUser();
        testFolder1 = FolderModel.getRandomFolderModel();
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
    }
    
    /**
     * Scenario 64
     * 1. Create folder1 with WEBDAV
     * 2. Create wordFile inside folder1 with CMIS
     * 3. Delete wordFile content with CMIS
     * 4. Update content using Collaborator role with CMIS
     * 5. Append content using Manager role with WebDAV
     * 6. Delete content that Manager added using Collaborator role with CMIS
     * 7. Rename wordFile with WebDAV
     * 9. Delete wordFile with WEBDAV
     * 
     * @throws Exception
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "File handling using several protocols")
    public void fileHandlingWithCollaboratorRole() throws Exception
    {
        String contentCollaborator = "content added by Collaborator";
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteCollaborator);
        wordFile = FileModel.getRandomFileModel(FileType.MSWORD2007, "tasTesting");
        
        STEP("1. Create folder1 with webdav");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite).createFolder(testFolder1).and().assertThat().existsInRepo();
        
        STEP("2. Create testFile1 inside folder1 using CMIS");
        cmisAPI.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(publicSite).usingResource(testFolder1).createFile(wordFile)
            .assertThat().existsInRepo();

        STEP("3. Delete wordFile content with WebDAV");
        cmisAPI.authenticateUser(testUser1).usingResource(wordFile).deleteContent();
        
        STEP("4. Update content using Collaborator role with CMIS");
        cmisAPI.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .usingResource(wordFile).update(contentCollaborator)
                    .assertThat().contentIs(contentCollaborator);
        
        STEP("5. Append content using Manager role with WebDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(wordFile).update("content added by Manager").assertThat().contentIs("content added by Manager");
        
        STEP("6. Delete content that Manager added using Collaborator role with CMIS");
        cmisAPI.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .usingResource(wordFile).deleteAllVersions(false)
                    .and().assertThat().documentHasVersion(1.2);
        
        STEP("7. Rename wordFile with FTP");
        ftpProtocol.authenticateUser(testUser1).usingResource(wordFile).rename("renamedFile.docx");

        STEP("8. Delete file with WEBDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(wordFile).delete()
                .assertThat().doesNotExistInRepo();
    }
    
    /**
     * Scenario 65
     * 1. Create folder1 with WEBDAV
     * 2. Create wordFile inside folder1 with CMIS
     * 3. Delete wordFile content with CMIS
     * 4. Update content using Contributor role with CMIS
     * 5. Append content using Manager role with WebDAV
     * 6. Delete content that Manager added using Collaborator role with CMIS
     * 7. Rename wordFile with WEBDAV
     * 8. Delete wordFile with FTP
     * 
     * @throws Exception
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "File handling using several protocols")
    public void fileHandlingWithContributorRole() throws Exception
    {
        String contentContributor = "content added by Contributor";
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteContributor);
        wordFile = FileModel.getRandomFileModel(FileType.MSWORD2007, "tasTesting");
        
        STEP("1. Create folder1 with webdav");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite).createFolder(testFolder1).and().assertThat().existsInRepo();
        
        STEP("2. Create testFile1 inside folder1 using CMIS");
        cmisAPI.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .usingResource(testFolder1).createFile(wordFile)
                    .assertThat().existsInRepo();

        STEP("3. Delete wordFile content with CMIS");
        cmisAPI.authenticateUser(testUser1).usingResource(wordFile).deleteContent();
        
        STEP("4. Update content using Contributor role with CMIS");
        cmisAPI.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .usingResource(wordFile).update(contentContributor)
                    .assertThat().contentIs(contentContributor);
        
        STEP("5. Append content using Manager role with WebDAV");
        webDavProtocol.authenticateUser(testUser1)
                .usingResource(wordFile).update("content added by Manager")
                    .assertThat().contentIs("content added by Manager");
        
        STEP("6. Delete content that Manager added using Contributor role with CMIS");
        cmisAPI.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .usingResource(wordFile).deleteAllVersions(false)
                    .and().assertThat().documentHasVersion(1.2);
        
        STEP("7. Rename wordFile with WebDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(wordFile).rename("renamedFile.docx");
        
        STEP("8. Delete file with FTP");
        ftpProtocol.authenticateUser(testUser1).usingResource(wordFile).delete().assertThat().doesNotExistInRepo();
    }
    
    /**
     * Scenario 76
     * 1. Using CMIS create two users: u1 and u2
     * 2. U1 creates a public site using CMIS
     * 3. U1 creates testFile1 in public site's document library using FTP
     * 4. U1 adds comment1 for testFile1 using REST API
     * 5. U2 gets comment1 using REST API
     * 6. U1 updates testFile1 using WEBDAV
     * 7. U2 gets comment1 using REST API
     * 8. U1 deletes testFile1 using WebDAV
     * 9. U2 can not get comment1 using REST API
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION,
            description = "Verify comment is deleted .")
    public void negativeScenarioWithComments() throws Exception
    {
        STEP("* 1. Using CMIS create two users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a public site using CMIS");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. U1 creates a file in public site's document library using FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 4. U1 adds comment1 for testFile1 using REST API");
        String newComment = "This is a new comment added by " + testUser1.getUsername();
        restAPI.authenticateUser(testUser1).withCoreAPI().usingResource(testFile1).addComment(newComment).assertThat().field("content").isNotEmpty().and().field("content").is(newComment);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("* 5. U2 gets comment1 using REST API");
        comments = restAPI.authenticateUser(testUser2).withCoreAPI().usingResource(testFile1).getNodeComments();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", newComment);
        
        STEP("* 6. U1 updates testFile1 using WEBDAV");
        String newContent = "This is new content added by " + testUser1.getUsername();
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile1).update(newContent).and().assertThat().contentIs(newContent);
        
        STEP("* 7. U2 gets comment1 using REST API");
        comments = restAPI.authenticateUser(testUser2).withCoreAPI().usingResource(testFile1).getNodeComments();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        comments.assertThat().entriesListContains("content", newComment);
        
        STEP("* 8. U1 deletes testFile1 using WebDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile1).delete().and().assertThat().doesNotExistInRepo();
        
        STEP("* 9. U2 can not get comment1 using REST API");
        comments = restAPI.authenticateUser(testUser2).withCoreAPI().usingResource(testFile1).getNodeComments();
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, testFile1.getNodeRef()));
    }
    
    /**
     * Scenario 77
     * 1. Using CMIS create two users: u1 and u2
     * 2. U1 creates a public site using CMIS
     * 3. U1 creates a file in public site's document library using FTP
     * 4. U1 applyAcl(permission) for U2 with role Site Manager to the document using CMIS
     * 5. U2 edits the document using FTP
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager permission to a document in a public site - is able to edit document.")
    public void addManagerPermissionToADocumentFromPublicSite() throws Exception
    {
        STEP("* 1. Using CMIS create two users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a public site using CMIS");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. U1 creates a file in public site's document library using FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 4. U1 applyAcl(permission) for U2 with role Site Manager to the document using CMIS");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile1).addAcl(testUser2, UserRole.SiteManager).then().assertThat().permissionIsSetForUser(testUser2, UserRole.SiteManager);

        STEP("* 5. U2 edits the document using FTP");
        ftpProtocol.authenticateUser(testUser2).usingSite(publicSite).usingResource(testFile1).update("new Content").assertThat().contentIs("new Content");
    }
    
    /**
     * Scenario 78
     * 1. Using CMIS create two users: u1 and u2
     * 2. U1 creates a public site using CMIS
     * 3. U1 creates a file in public site's document library using FTP
     * 4. U1 applyAcl(permission) for U2 with role Site Collaborator to the document using CMIS
     * 5. U2 edits the document using FTP
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator permission to a document in a public site - is able to edit document.")
    public void addCollaboratorPermissionToADocumentFromPublicSite() throws Exception
    {
        STEP("* 1. Using CMIS create two users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a public site using CMIS");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. U1 creates a file in public site's document library using FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 4. U1 applyAcl(permission) for U2 with role Site Manager to the document using CMIS");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile1).addAcl(testUser2, UserRole.SiteCollaborator).then().assertThat().permissionIsSetForUser(testUser2, UserRole.SiteCollaborator);

        STEP("* 5. U2 edits the document using FTP");
        ftpProtocol.authenticateUser(testUser2).usingSite(publicSite).usingResource(testFile1).update("new Content").assertThat().contentIs("new Content");
    }
    
    /**
     * Scenario 79
     * 1. Using CMIS create two users: u1 and u2
     * 2. U1 creates a public site using CMIS
     * 3. U1 creates a file in public site's document library using FTP
     * 4. U1 applyAcl(permission) for U2 with role Site Contributor to the document using CMIS
     * 5. U2 edits the document using FTP
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor permission to a document in a public site - is not able to edit document.")
    public void addContributorPermissionToADocumentFromPublicSite() throws Exception
    {
        STEP("* 1. Using CMIS create two users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a public site using CMIS");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. U1 creates a file in public site's document library using FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 4. U1 applyAcl(permission) for U2 with role Site Manager to the document using CMIS");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile1).addAcl(testUser2, UserRole.SiteContributor).then().assertThat().permissionIsSetForUser(testUser2, UserRole.SiteContributor);

        STEP("* 5. U2 edits the document using FTP");
        ftpProtocol.authenticateUser(testUser2).usingSite(publicSite).usingResource(testFile1).update("new Content");
        ftpProtocol.authenticateUser(testUser1).usingSite(publicSite).usingResource(testFile1).assertThat().contentIs("file1 content");
    }
    
    /**
     * Scenario 80
     * 1. Using CMIS create two users: u1 and u2
     * 2. U1 creates a public site using CMIS
     * 3. U1 creates a file in public site's document library using FTP
     * 4. U1 applyAcl(permission) for U2 with role Site Consumer to the document using CMIS
     * 5. U2 edits the document using FTP
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify consumer permission to a document in a public site - is not able to edit document.")
    public void addConsumerPermissionToADocumentFromPublicSite() throws Exception
    {
        STEP("* 1. Using CMIS create two users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a public site using CMIS");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("* 3. U1 creates a file in public site's document library using FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 4. U1 applyAcl(permission) for U2 with role Site Manager to the document using CMIS");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile1).addAcl(testUser2, UserRole.SiteConsumer).then().assertThat().permissionIsSetForUser(testUser2, UserRole.SiteConsumer);

        STEP("* 5. U2 edits the document using FTP");
        ftpProtocol.authenticateUser(testUser2).usingSite(publicSite).usingResource(testFile1).update("new Content");
        ftpProtocol.authenticateUser(testUser1).usingSite(publicSite).usingResource(testFile1).assertThat().contentIs("file1 content");
    }
    
    /**
     * Scenario 107
     * 1. Using CMIS create two users: u1 and u2
     * 2. U1 creates a moderated site using CMIS
     * 3. U1 creates a file in moderated site's document library using FTP
     * 4. U1 applyAcl(permission) for U2 with role Site Manager to the document using CMIS
     * 5. U2 edits the document using FTP
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager permission to a document in a moderated site - is not able to edit document.")
    public void addManagerPermissionToADocumentFromModeratedSite() throws Exception
    {
        STEP("* 1. Using CMIS create two users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a moderated site using CMIS");
        moderatedSite = dataSite.usingUser(testUser1).createModeratedRandomSite();

        STEP("* 3. U1 creates a file in moderated site's document library using FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(moderatedSite).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 4. U1 applyAcl(permission) for U2 with role Site Manager to the document using CMIS");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile1).addAcl(testUser2, UserRole.SiteManager).then().assertThat().permissionIsSetForUser(testUser2, UserRole.SiteManager);

        STEP("* 5. U2 edits the document using FTP");
        ftpProtocol.authenticateUser(testUser2).usingSite(moderatedSite).usingResource(testFile1).update("new Content");
        ftpProtocol.authenticateUser(testUser1).usingSite(moderatedSite).usingResource(testFile1).assertThat().contentIs("file1 content");
    }
    
    /**
     * Scenario 108
     * 1. Using CMIS create two users: u1 and u2
     * 2. U1 creates a private site using CMIS
     * 3. U1 creates a file in private site's document library using FTP
     * 4. U1 applyAcl(permission) for U2 with role Site Manager to the document using CMIS
     * 5. U2 edits the document using FTP
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify manager permission to a document in a private site - is not able to edit document.")
    public void addManagerPermissionToADocumentFromPrivateSite() throws Exception
    {
        STEP("* 1. Using CMIS create two users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("* 2. U1 creates a private site using CMIS");
        privateSite = dataSite.usingUser(testUser1).createPrivateRandomSite();

        STEP("* 3. U1 creates a file in private site's document library using FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(privateSite).createFile(testFile1).then().assertThat().existsInRepo();

        STEP("* 4. U1 applyAcl(permission) for U2 with role Site Manager to the document using CMIS");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile1).addAcl(testUser2, UserRole.SiteManager).then().assertThat().permissionIsSetForUser(testUser2, UserRole.SiteManager);

        STEP("* 5. U2 edits the document using FTP");
        ftpProtocol.authenticateUser(testUser2).usingSite(privateSite).usingResource(testFile1).update("new Content");
        ftpProtocol.authenticateUser(testUser1).usingSite(privateSite).usingResource(testFile1).assertThat().contentIs("file1 content");
    }
    
    /**
     * Scenario 60
     * 1. Using CMIS creates one test user: u1
     * 2. U1 creates testFile1 in public site using FTP
     * 3. U1 creates testFile2 in public site using FTP
     * 4. U1 updates testFile2 using FTP
     * 5. Compare created date with modified date
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT}, executionType = ExecutionType.REGRESSION, description = "Compare Modified Date")
    public void manageModificationTimeOfFile() throws Exception
    {
        STEP("1. Using CMIS creates one test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. U1 creates testFile1 in a public site using FTP");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        testFile1 = new FileModel("testFile1", FileType.TEXT_PLAIN);
        ftpProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).assertThat().existsInRepo();
        String modifiedDate1 = ftpProtocol.getModificationTime();

        STEP("3. U1 creates testFile2 in a public site using FTP");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        testFile2 = new FileModel("testFile1", FileType.TEXT_PLAIN);
        Utility.waitToLoopTime(2);
        ftpProtocol.usingSite(publicSite).createFile(testFile2).assertThat().existsInRepo();
        String modifiedDate2 = ftpProtocol.getModificationTime();
        Utility.waitToLoopTime(2, "Waiting for update");

        STEP("4. U1 updates testFile2 in a public site using FTP");
        ftpProtocol.update("test update").assertThat().contentIs("test update");
        String updatedDate2 = ftpProtocol.getModificationTime();       

        STEP("5. Compare created date with modified date");
        Assert.assertNotEquals(modifiedDate1, modifiedDate2, "Updated and modified dates are equal");
        Assert.assertNotEquals(modifiedDate2, updatedDate2, "Modified and updated dates are equal");
    }

}