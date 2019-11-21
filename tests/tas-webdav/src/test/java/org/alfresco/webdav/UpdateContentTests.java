package org.alfresco.webdav;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UpdateContentTests extends WebDavTest
{
    UserModel managerUser, adminUser;
    SiteModel testSite;
    FileModel testFile, nonExistingFile;
    private String content = "content webdav file";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        managerUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY, 
        description = "Verify that admin user can update the content of a file created by self.")
    public void adminShouldEditContentFile() throws Exception
    {
        FolderModel guestHomeFolder = FolderModel.getGuestHomeFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);

        webDavProtocol.authenticateUser(adminUser).usingResource(guestHomeFolder).createFile(testFile).and().assertThat().existsInRepo().then()
                .update(content + "-update").and().assertThat().hasStatus(HttpStatus.SC_NO_CONTENT).and().assertThat().contentIs(content + "-update");
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that SiteManager user can update the content of a file created by self.")
    public void siteManagerShouldEditContentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile).and().assertThat().existsInRepo().then()
                .update(content + "-update " + UserRole.SiteManager).and().assertThat().hasStatus(HttpStatus.SC_NO_CONTENT).and().assertThat()
                .contentIs(content + "-update " + UserRole.SiteManager);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that SiteCollaborator user can update the content of a file created by other user.")
    public void siteCollaboratorShouldEditContentFileCreatedByOtherUser() throws Exception
    {
        UserModel collaborator = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);

        testFile = FileModel.getRandomFileModel(FileType.HTML, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile).and().assertThat().existsInRepo().then()
                .authenticateUser(collaborator).usingResource(testFile).update(content + "-update " + UserRole.SiteCollaborator).and().assertThat()
                .hasStatus(HttpStatus.SC_NO_CONTENT).and().assertThat().contentIs(content + "-update " + UserRole.SiteCollaborator);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that SiteCollaborator user can update the content of a file created by self.")
    public void siteCollaboratorShouldEditContentFileCreatedBySelf() throws Exception
    {
        UserModel collaborator = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);

        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(collaborator).usingSite(testSite).createFile(testFile).and().assertThat().existsInRepo().then()
                .update(content + "-update " + UserRole.SiteCollaborator).and().assertThat().hasStatus(HttpStatus.SC_NO_CONTENT).and().assertThat()
                .contentIs(content + "-update " + UserRole.SiteCollaborator);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION,
            description = "Verify that SiteContributor user cannot update inline the content of a file created by other user.")
    public void siteContributorShouldNotEditContentFileCreatedByOtherUser() throws Exception
    {
        UserModel contributor = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteContributor);

        testFile = FileModel.getRandomFileModel(FileType.MSPOWERPOINT, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile).and().assertThat().existsInRepo().then()
                .authenticateUser(contributor).usingResource(testFile).update(content + "-update").and().assertThat().hasStatus(HttpStatus.SC_FORBIDDEN).and()
                .assertThat().contentIs(content);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that SiteContributor user can update the content of a file created by self.")
    public void siteContributorShouldEditContentFileCreatedBySelf() throws Exception
    {
        UserModel contributor = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteManager);

        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL, content);
        webDavProtocol.authenticateUser(contributor).usingSite(testSite).createFile(testFile).and().assertThat().existsInRepo().then()
                .update(content + "-update " + UserRole.SiteManager).and().assertThat().hasStatus(HttpStatus.SC_NO_CONTENT).and().assertThat()
                .contentIs(content + "-update " + UserRole.SiteManager);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that SiteConsumer user cannot update the content of a file created by other user.")
    public void siteConsumerShouldNotEditContentFileCreatedByOtherUser() throws Exception
    {
        UserModel consumer = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);

        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile).and().assertThat().existsInRepo().then()
                .authenticateUser(consumer).usingResource(testFile).update(content + "-update " + UserRole.SiteConsumer).and().assertThat()
                .hasStatus(HttpStatus.SC_FORBIDDEN).and().assertThat().contentIs(content);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that unauthenticated user cannot update the content of a file.")
    public void unauthenticatedUserShouldNotEditContentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile).and().assertThat().existsInRepo().then().disconnect()
                .usingResource(testFile).update(content + "-update " + "unauthenticated").and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that inexistent user cannot update the content of a file.")
    public void inexistentUserShouldNotEditContentFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .then().authenticateUser(UserModel.getRandomUserModel())
                    .update(content + "-update")
                        .and().assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED);
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that SiteManager user cannot update the content of a folder")
    public void siteManagerShouldNotEditFolderContent() throws Exception
    {
        FolderModel folder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(managerUser)
            .usingSite(testSite).createFolder(folder).and().assertThat().existsInRepo()
                .then().update(content + "-update ").assertThat().hasStatus(HttpStatus.SC_BAD_REQUEST);
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that SiteManager user cannot update the content of an inexisting file.")
    public void editContentFileForNonExistingFile() throws Exception
    {
        nonExistingFile = FileModel.getRandomFileModel(FileType.PDF, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(nonExistingFile).delete()
            .and().assertThat().doesNotExistInRepo().and().assertThat().doesNotExistInWebdav()
                .when().update(content + "-update").assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, 
        description = "Verify that SiteManager user cannot update the content of a locked file")
    public void siteManagerCannotEditContentOfLockedFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(managerUser)
            .usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                .then().lock().assertThat().isLocked()
                    .then().update(content + "-update " ).and().assertThat().hasStatus(HttpStatus.SC_LOCKED)
                    .and().assertThat().contentIs(content);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can update file with a size smaller than the quota limit")
    public void userCanUpdateFileSmallerThanQuotaLimit() throws Exception
    {
//        if (!webDavProtocol.withJMX().getSystemUsagesConfigurationStatus())
//            throw new SkipException("Skipping this test because user quotas are not enabled. Please add " +
//                    "system.usages.enabled=true to alfresco-global.properties, restart Alfresco and run the test again.");

        UserModel quotaUser = dataUser.createRandomTestUser();
        dataUser.addUserToSite(quotaUser, testSite, UserRole.SiteManager);
        dataUser.usingAdmin().setUserQuota(quotaUser, 5);
        FileModel quotaFile = FileModel.getFileModelWithContentSizeOfxMB(1);
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);

        webDavProtocol.authenticateUser(quotaUser).usingSite(testSite).createFile(testFile).and().assertThat().existsInRepo()
                .then().update(quotaFile.getContent()).and().assertThat().contentIs(quotaFile.getContent());
    }

//    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL })
//    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION,
//            description = "Verify user cannot update file with a size bigger than the quota limit")
//    public void userCannotUpdateFileBiggerThanQuotaLimit() throws Exception
//    {
//        if (!webDavProtocol.withJMX().getSystemUsagesConfigurationStatus())
//            throw new SkipException("Skipping this test because user quotas are not enabled. Please add " +
//                    "system.usages.enabled=true to alfresco-global.properties, restart Alfresco and run the test again.");
//
//        UserModel quotaUser = dataUser.createRandomTestUser();
//        dataUser.addUserToSite(quotaUser, testSite, UserRole.SiteManager);
//        dataUser.usingAdmin().setUserQuota(quotaUser, 1);
//        FileModel quotaFile = FileModel.getFileModelWithContentSizeOfxMB(2);
//        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
//
//        webDavProtocol.authenticateUser(quotaUser).usingSite(testSite).createFile(testFile).and().assertThat().existsInRepo()
//                .then().update(quotaFile.getContent()).and().assertThat().contentIs(content);
//    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION,
            description = "Verify that file version is incremented after file is edited")
    public void verifyFileVersionIsIncrementedAfterEdit() throws Exception
    {
        testFile = dataContent.usingUser(managerUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        webDavProtocol.authenticateUser(managerUser).usingResource(testFile).update("new content");
        dataContent.usingResource(testFile).assertContentVersionIs("1.1");
    }
}
