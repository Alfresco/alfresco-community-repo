package org.alfresco.webdav;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CreateFileTests extends WebDavTest
{
    UserModel managerUser;
    SiteModel testSite;
    FileModel testFile;
    private String content = "webdav file content";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that admin user can create a file in Guest Home folder")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void adminShouldCreateFile() throws Exception
    {
        FolderModel guestHomeFolder = FolderModel.getGuestHomeFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        webDavProtocol.authenticateUser(dataUser.getAdminUser()).usingResource(guestHomeFolder)
            .createFile(testFile).and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                .then().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that site manager can create file in site")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteManagerShouldCreateFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                .then().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav().and().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that user with contributor role can create file in site")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteContributorShouldCreateFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSPOWERPOINT, content);
        UserModel contributor = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(contributor, testSite, UserRole.SiteContributor);
        webDavProtocol.authenticateUser(contributor).usingSite(testSite)
            .createFile(testFile).assertThat().hasStatus(HttpStatus.SC_CREATED)
                .and().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav().and().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that user with collaborator role can create file in site")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteCollaboratorShouldCreateFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, content);
        UserModel collaborator = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(collaborator, testSite, UserRole.SiteCollaborator);
        webDavProtocol.authenticateUser(collaborator).usingSite(testSite)
            .createFile(testFile).assertThat().hasStatus(HttpStatus.SC_CREATED)
                .and().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav().and().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that user with consumer role cannot create file in site")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY})
    public void siteConsumerShouldCreateFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, content);
        UserModel consumer = dataUser.createRandomTestUser();
        dataUser.usingUser(managerUser).addUserToSite(consumer, testSite, UserRole.SiteConsumer);
        webDavProtocol.authenticateUser(consumer).usingSite(testSite)
            .createFile(testFile).assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
                .and().assertThat().doesNotExistInRepo()
                    .and().assertThat().doesNotExistInWebdav()
                        .and().assertThat().hasStatus(HttpStatus.SC_NOT_FOUND);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that user with manager role can create file with spaces in name")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void siteManageShouldCreateFileWithSpacesInName() throws Exception
    {
        testFile = new FileModel("test file " + RandomData.getRandomFile(FileType.PDF));
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
             .createFile(testFile).assertThat().hasStatus(HttpStatus.SC_CREATED)
                .and().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav().assertThat().hasStatus(HttpStatus.SC_OK);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site manager cannot create file in site with symbols in name")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE, TestGroup.OS_LINUX}, expectedExceptions=IllegalArgumentException.class)
    public void siteManagerShouldCreateFileWithSymbolsInName() throws Exception
    {
        testFile = new FileModel("<>.|?#()[]{}.txt", FileType.MSWORD2007, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).createFile(testFile);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that user with manager role can create file with no content")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void siteManageShouldCreateFileWithNoContent() throws Exception
    {
        FileModel noContentFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
             .createFile(noContentFile).assertThat().hasStatus(HttpStatus.SC_CREATED)
                .and().assertThat().existsInRepo()
                    .and().assertThat().existsInWebdav()
                    .and().assertThat().contentIs("");
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that inexistent user cannot create file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void inexistentUserShouldNotCreateFile() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        webDavProtocol.authenticateUser(UserModel.getRandomUserModel()).usingResource(FolderModel.getSharedFolderModel())
             .createFile(testFile).assertThat().hasStatus(HttpStatus.SC_UNAUTHORIZED)
             .then().usingAdmin().assertThat().doesNotExistInRepo()
                 .and().assertThat().doesNotExistInWebdav();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that unauthorized user cannot create file")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void unauthorizedUserShouldNotCreateFile() throws Exception
    {
        UserModel unauthorized = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        webDavProtocol.authenticateUser(unauthorized).usingRoot()
             .createFile(testFile).assertThat().hasStatus(HttpStatus.SC_FORBIDDEN)
             .then().assertThat().doesNotExistInRepo()
                 .and().assertThat().doesNotExistInWebdav();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that site manager can create file in site")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE})
    public void siteManagerShouldNotCreateFileTwice() throws Exception
    {
        SiteModel twiceSite = dataSite.usingUser(managerUser).createPublicRandomSite();
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL, content);
        webDavProtocol.authenticateUser(managerUser).usingSite(twiceSite)
            .createFile(testFile).and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                 .createFile(testFile).assertThat().hasStatus(HttpStatus.SC_NO_CONTENT);
        Assert.assertTrue(webDavProtocol.usingSite(twiceSite).getFiles().size() == 1);
    }

//    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION,
//            description ="Verify user cannot create file with a size bigger than the quota limit")
//    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL})
//    public void userCannotCreateFileGreaterThanQuotaLimit() throws Exception
//    {
//        if (!webDavProtocol.withJMX().getSystemUsagesConfigurationStatus())
//            throw new SkipException("Skipping this test because user quotas are not enabled. Please add " +
//                    "system.usages.enabled=true to alfresco-global.properties, restart Alfresco and run the test again.");
//
//        UserModel quotaUser = dataUser.createRandomTestUser();
//        dataUser.addUserToSite(quotaUser, testSite, UserRole.SiteCollaborator);
//        dataUser.usingAdmin().setUserQuota(quotaUser, 1);
//        FileModel quotaFile = FileModel.getFileModelWithContentSizeOfxMB(2);
//
//        webDavProtocol.authenticateUser(quotaUser).usingSite(testSite).createFile(quotaFile)
//                .then()
//                    .assertThat().doesNotExistInWebdav()
//                    .assertThat().doesNotExistInRepo();
//    }

    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION,
            description ="Verify user can create file with a size smaller than the quota limit")
    @Test(groups = {TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL})
    public void userCanCreateFileSmallerThanQuotaLimit() throws Exception
    {
//        if (!webDavProtocol.withJMX().getSystemUsagesConfigurationStatus())
//            throw new SkipException("Skipping this test because user quotas are not enabled. Please add " +
//                    "system.usages.enabled=true to alfresco-global.properties, restart Alfresco and run the test again.");

        UserModel quotaUser = dataUser.createRandomTestUser();
        dataUser.addUserToSite(quotaUser, testSite, UserRole.SiteCollaborator);
        dataUser.usingAdmin().setUserQuota(quotaUser, 5);
        FileModel quotaFile = FileModel.getFileModelWithContentSizeOfxMB(1);

        webDavProtocol.authenticateUser(quotaUser).usingSite(testSite).createFile(quotaFile)
                .then()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo();
    }
}
