package org.alfresco.tas.integration;

import static org.alfresco.utility.report.log.Step.STEP;

import jakarta.mail.FolderNotFoundException;

import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestDeploymentModel;
import org.alfresco.rest.model.RestDeploymentModelsCollection;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestPersonFavoritesModel;
import org.alfresco.rest.model.RestPersonFavoritesModelsCollection;
import org.alfresco.rest.model.RestRatingModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import junit.framework.Assert;

/**
 * Created by Claudia Agache on 12/13/2016.
 */
public class IntegrationCoreTests extends IntegrationTest
{
    UserModel testUser1, testUser2;
    SiteModel testSitePublic, testSiteModerated, testSitePrivate, secondPublicSite;
    FolderModel testFolder1, testFolder2, parentFolder1;
    private String renamePrefix = "edited-";
    FileModel testFile1, testFile2, childDoc1, childDoc2, childDoc3, childDoc4, childDoc5;
    RestRatingModel returnedRatingModel;
    RestPersonFavoritesModel restPersonFavoritesModel;
    private String newContent = "new TAS content";
    
    /**
     * Scenario 22
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates a public test site and U2 a public test site using CMIS
     * 3. U1 creates a folder with a file in his public site's document library using WebDav
     * 4. U1 tries to move his folder to U2 public site using IMAP
     * 5. Verify folder with file is not moved. U1 is not authorized to access the public site
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE  }, expectedExceptions = FolderNotFoundException.class)
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to move a folder with a file to a public site if he is not a member of that site.")
    public void moveFolderWithFileToPublicSiteByUninvitedUser() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public test site and U2 a public test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        secondPublicSite = dataSite.usingUser(testUser2).createIMAPSite();

        STEP("3. U1 creates a folder with a file in public site's document library using WebDav");
        testFolder1 = FolderModel.getRandomFolderModel();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic)
                .createFolder(testFolder1).assertThat().existsInWebdav()
                .usingResource(testFolder1).createFile(testFile1).assertThat().existsInRepo();

        STEP("4. U1 tries to move his folder to U2 public site using IMAP. 5. Verify folder with file is not moved. U1 is not authorized to access the U2 public site");
        FolderModel destination = new FolderModel(Utility.buildPath("Sites", secondPublicSite.getId(), "documentLibrary"));
        destination.setProtocolLocation(imapProtocol.authenticateUser(testUser2).usingSite(secondPublicSite).getLastResourceWithoutPrefix());
        imapProtocol.authenticateUser(testUser1)
                .usingResource(testFolder1).moveTo(destination)
                .assertThat().doesNotExistInRepo();
    }

    /**
     * Scenario 23
     * 1. Using CMIS create 1 test user: u1
     * 2. U1 creates a public test site
     * 3. U1 creates a folder (parentFolder) inside public site's document library using WebDav
     * 4. Inside parentFolder create 4 files using CMIS, WebDAV, FTP
     * 5. Inside parentFolder create 1 subfolder using IMAP
     * 6. Delete parentFolder using CMIS
     * 7. Verify folder is deleted along with all its children
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is able to delete parent folder along with all its children using CMIS.")
    public void deleteFolderWithChildrenUsingCMIS() throws Exception
    {
        STEP("1. Using CMIS create 1 test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createIMAPSite();

        STEP("3. U1 creates a folder (parentFolder) inside public site's document library using WebDav");
        FolderModel parentFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser1)
                .usingSite(testSitePublic).createFolder(parentFolder)
                .assertThat().existsInWebdav()
                .and().assertThat().existsInRepo();

        STEP("4. Inside parentFolder create 4 files using CMIS, WebDAV, FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel testFile2 = FileModel.getRandomFileModel(FileType.MSWORD);
        FileModel testFile3 = FileModel.getRandomFileModel(FileType.HTML);
        FileModel testFile4 = FileModel.getRandomFileModel(FileType.EXE);
        cmisAPI.authenticateUser(testUser1).usingResource(parentFolder)
                .createFile(testFile1).assertThat().existsInRepo();
        webDavProtocol.usingResource(parentFolder).createFile(testFile2)
                .assertThat().existsInWebdav().and()
                .assertThat().existsInRepo();
        cmisAPI.authenticateUser(testUser1).usingResource(parentFolder).createFile(testFile3)
                .assertThat().existsInRepo();
        ftpProtocol.authenticateUser(testUser1).usingResource(parentFolder).createFile(testFile4)
                .assertThat().existsInFtp().and()
                .assertThat().existsInRepo();

        STEP("5. Inside parentFolder create 1 subfolder using IMAP");
        testFolder1 = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(testUser1)
                .usingResource(parentFolder).createFolder(testFolder1)
                .assertThat().existsInImap().and()
                .assertThat().existsInRepo();

        STEP("6. Delete parentFolder using CMIS 7. Verify folder is deleted along with all its children");
        cmisAPI.usingResource(parentFolder).deleteFolderTree().and().assertThat().doesNotExistInRepo()
                .usingResource(testFolder1).assertThat().doesNotExistInRepo()
                .usingResource(testFile1).assertThat().doesNotExistInRepo()
                .usingResource(testFile2).assertThat().doesNotExistInRepo()
                .usingResource(testFile3).assertThat().doesNotExistInRepo()
                .usingResource(testFile4).assertThat().doesNotExistInRepo();
    }

    /**
     * Scenario 24
     * 1. Using CMIS create 1 test user: u1
     * 2. U1 creates a public test site
     * 3. U1 creates a folder (parentFolder) inside public site's document library using IMAP
     * 4. Inside parentFolder create 4 files using CMIS, WebDAV, FTP
     * 5. Inside parentFolder create 1 subfolder using WebDAV
     * 6. Delete parentFolder using WebDAV
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is able to delete parent folder along with all its children using WebDAV.")
    public void deleteFolderWithChildrenUsingWebDAV() throws Exception
    {
        STEP("1. Using CMIS create 1 test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createIMAPSite();

        STEP("3. U1 creates a folder (parentFolder) inside public site's document library using IMAP");
        FolderModel parentFolder = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(testUser1)
                .usingSite(testSitePublic).createFolder(parentFolder)
                .assertThat().existsInImap().and()
                .assertThat().existsInRepo();

        STEP("4. Inside parentFolder create 4 files using CMIS, WebDAV, FTP");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel testFile2 = FileModel.getRandomFileModel(FileType.MSWORD);
        FileModel testFile3 = FileModel.getRandomFileModel(FileType.HTML);
        FileModel testFile4 = FileModel.getRandomFileModel(FileType.EXE);
        cmisAPI.authenticateUser(testUser1).usingResource(parentFolder)
                .createFile(testFile1).assertThat().existsInRepo();
        webDavProtocol.authenticateUser(testUser1).usingResource(parentFolder).createFile(testFile2)
                .assertThat().existsInWebdav().and()
                .assertThat().existsInRepo();
        ftpProtocol.authenticateUser(testUser1).usingResource(parentFolder).createFile(testFile4)
                .assertThat().existsInFtp().and()
                .assertThat().existsInRepo();

        STEP("5. Inside parentFolder create 1 subfolder using WebDAV");
        testFolder1 = FolderModel.getRandomFolderModel();
        webDavProtocol.usingResource(parentFolder).createFolder(testFolder1)
                .assertThat().existsInWebdav()
                .and().assertThat().existsInRepo();

        STEP("6. Delete parentFolder using WebDAV 7. Verify folder is deleted along with all its children");
        webDavProtocol.usingResource(parentFolder).delete().and().assertThat().doesNotExistInRepo()
                .usingResource(testFolder1).assertThat().doesNotExistInRepo()
                .usingResource(testFile1).assertThat().doesNotExistInRepo()
                .usingResource(testFile2).assertThat().doesNotExistInRepo()
                .usingResource(testFile3).assertThat().doesNotExistInRepo()
                .usingResource(testFile4).assertThat().doesNotExistInRepo();
    }

    /**
     * Scenario 25
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates a public test site and adds u2 as manager to his site using CMIS
     * 3. U1 creates a folder (parentFolder) inside public site's document library using CMIS
     * 4. U1 creates a subfolder1 and a file1 inside parentFolder using FTP
     * 5. U1 creates a subfolder2 and a file2 inside subfolder1 using CMIS
     * 6. U2 creates a subfolder3 and a file3 using WebDAV
     * 7. U2 deletes parentFolder using FTP
     * 8. Verify folder is deleted along with all its children
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE})
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is able to delete parent folder along with all its children using FTP.")
    public void deleteFolderWithChildrenUsingFTP() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public test site using CMIS and adds u2 as manager to his site using REST");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        dataUser.usingUser(testUser1).addUserToSite(testUser2, testSitePublic, UserRole.SiteManager);
        testUser2.setUserRole(UserRole.SiteManager);

        STEP("3. U1 creates a folder (parentFolder) inside public site's document library using CMIS");
        FolderModel parentFolder = FolderModel.getRandomFolderModel();
        cmisAPI.authenticateUser(testUser1)
                .usingSite(testSitePublic).createFolder(parentFolder)
                .assertThat().existsInRepo();

        STEP("4. U1 creates a subfolder1 and a file1 inside parentFolder using FTP");
        FolderModel subFolder1 = FolderModel.getRandomFolderModel();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);

        ftpProtocol.authenticateUser(testUser1)
                .usingResource(parentFolder).createFolder(subFolder1)
                .assertThat().existsInRepo()
                .assertThat().existsInFtp()
                .usingResource(parentFolder).createFile(testFile1)
                .assertThat().existsInRepo()
                .assertThat().existsInFtp();

        STEP("5. U1 creates a subfolder2 and a file2 inside subfolder1 using CMIS");
        FolderModel subFolder2 = FolderModel.getRandomFolderModel();
        FileModel testFile2 = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisAPI.authenticateUser(testUser1).usingResource(subFolder1)
                .createFolder(subFolder2)
                .assertThat().existsInRepo()
                .createFile(testFile2)
                .assertThat().existsInRepo();

        STEP("6. U2 creates a subfolder3 and a file3 using WebDAV");
        FolderModel subFolder3 = FolderModel.getRandomFolderModel();
        FileModel testFile3 = FileModel.getRandomFileModel(FileType.HTML);

        webDavProtocol.authenticateUser(testUser2).usingResource(subFolder2)
                .createFolder(subFolder3)
                .assertThat().existsInWebdav()
                .assertThat().existsInRepo()
                .createFile(testFile3)
                .assertThat().existsInWebdav()
                .assertThat().existsInRepo();

        STEP("7. U2 deletes parentFolder using FTP");
        ftpProtocol.authenticateUser(testUser2)
                .usingResource(parentFolder).assertThat().existsInFtp()
                .then().delete()
                .assertThat().doesNotExistInRepo().and()
                .assertThat().doesNotExistInFtp()
                .usingResource(subFolder1)
                .assertThat().doesNotExistInRepo().and()
                .assertThat().doesNotExistInFtp()
                .usingResource(subFolder2)
                .assertThat().doesNotExistInRepo().and()
                .assertThat().doesNotExistInFtp()
                .usingResource(subFolder3)
                .assertThat().doesNotExistInRepo().and()
                .assertThat().doesNotExistInFtp()
                .usingResource(testFile1)
                .assertThat().doesNotExistInRepo().and()
                .assertThat().doesNotExistInFtp()
                .usingResource(testFile2)
                .assertThat().doesNotExistInRepo().and()
                .assertThat().doesNotExistInFtp()
                .usingResource(testFile3)
                .assertThat().doesNotExistInRepo().and()
                .assertThat().doesNotExistInFtp();
    }

    /**
     * Scenario 28
     * 1. Using CMIS create one test user: U1
     * 2. Using CMIS U1 creates a public test site
     * 3. Using CMIS, WebDAV and FTP U1 creates multiple files and folders in parent folder
     * 4. Using CMIS, U1 checks out a document from parent folder
     * 5. Using CMIS, U1 deletes parent folder (deleteTree)
     * 6. Using WebDAV, U1 verifies that parent folder and all children are NOT present in Repo and WebDAV
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE})
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, description = "Verify deletion of a parentFolder with checkedout file using CMIS.")
    public void deleteParentFolderWithCheckoutFileInUsingCMIS() throws Exception
    {
        STEP("1. Using CMIS create one test user: U1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. Using CMIS U1 creates a public test site");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("3. Using CMIS, WebDAV and FTP U1 creates multiple files and folders in parent folder");
        FileModel cmisTestFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file 1 content");
        FileModel webdavTestFile = FileModel.getRandomFileModel(FileType.HTML, "file 2 content");
        FileModel ftpTestFile = FileModel.getRandomFileModel(FileType.MSWORD, "file 4 content");

        testFolder1 = FolderModel.getRandomFolderModel();
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(testFolder1).assertThat().existsInRepo();
        cmisAPI.authenticateUser(testUser1).usingResource(testFolder1).createFile(cmisTestFile)
                .assertThat().existsInRepo();
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).createFile(webdavTestFile)
                .assertThat().existsInWebdav().assertThat().existsInRepo();
        ftpProtocol.authenticateUser(testUser1).usingResource(testFolder1).createFile(ftpTestFile)
                .assertThat().existsInFtp().assertThat().existsInRepo();

        FolderModel cmisTestFolder = FolderModel.getRandomFolderModel();
        FolderModel webdavTestFolder = FolderModel.getRandomFolderModel();
        FolderModel ftpTestFolder = FolderModel.getRandomFolderModel();

        cmisAPI.usingResource(testFolder1).createFolder(cmisTestFolder)
                .assertThat().existsInRepo();
        webDavProtocol.usingResource(testFolder1).createFolder(webdavTestFolder)
                .assertThat().existsInWebdav().assertThat().existsInRepo();
        ftpProtocol.usingResource(testFolder1).createFolder(ftpTestFolder)
                .assertThat().existsInFtp().assertThat().existsInRepo();

        cmisAPI.usingResource(testFolder1).assertThat()
                .hasChildren(cmisTestFile, webdavTestFile, ftpTestFile, cmisTestFolder, webdavTestFolder, ftpTestFolder);

        STEP("Step 4: Using CMIS, U1 checks out a document from parent folder");
        cmisAPI.usingResource(webdavTestFile).checkOut().assertThat().documentIsCheckedOut();

        STEP("Step 5: Using CMIS, U1 deletes parent folder (deleteTree)");

        cmisAPI.usingResource(testFolder1).deleteFolderTree()
                .assertThat().doesNotExistInRepo();
        STEP("Step 6. Using WebDAV, U1 verifies that parent folder and all children are not present");
        webDavProtocol.usingResource(testFolder1).assertThat().doesNotExistInRepo().assertThat().doesNotExistInWebdav()
                .and().usingResource(cmisTestFile).assertThat().doesNotExistInRepo().assertThat().doesNotExistInWebdav()
                .and().usingResource(webdavTestFile).assertThat().doesNotExistInRepo().assertThat().doesNotExistInWebdav()
                .and().usingResource(ftpTestFile).assertThat().doesNotExistInRepo().assertThat().doesNotExistInWebdav()
                .and().usingResource(cmisTestFolder).assertThat().doesNotExistInRepo().assertThat().doesNotExistInWebdav()
                .and().usingResource(webdavTestFolder).assertThat().doesNotExistInRepo().assertThat().doesNotExistInWebdav()
                .and().usingResource(ftpTestFolder).assertThat().doesNotExistInRepo().assertThat().doesNotExistInWebdav();
    }

    /**
     * Scenario 29
     * 1. Using CMIS create one test user: U1
     * 2. Using CMIS U1 creates a public test site
     * 3. Using FTP U1 creates a parent folder in public site's document library
     * 4. Using CMIS, WebDAV and FTP U1 creates multiple files and folders in parent folder
     * 5. Using CMIS, U1 checks out a document from parent folder
     * 6. Using WebDAV, U1 deletes parent folder (deleteFolder)
     * 7. Using FTP, U1 verifies that parent folder and all children are NOT present in Repo and FTP
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE})
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, description = "Verify deletion of a parentFolder with checkedout file using WebDAV.")
    public void deleteParentFolderWithCheckoutFileInUsingWebDAV() throws Exception
    {
        STEP("1. Using CMIS create one test user: U1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. Using CMIS U1 creates a public test site");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("3. Using FTP U1 creates a parent folder");
        testFolder1 = FolderModel.getRandomFolderModel();
        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(testFolder1).assertThat().existsInRepo();

        STEP("4. Using CMIS, WebDAV and FTP U1 creates multiple files and folders in parent folder");
        FileModel cmisTestFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file 1 content");
        FileModel webdavTestFile = FileModel.getRandomFileModel(FileType.HTML, "file 2 content");
        FileModel ftpTestFile = FileModel.getRandomFileModel(FileType.MSWORD, "file 4 content");

        cmisAPI.authenticateUser(testUser1).usingResource(testFolder1).createFile(cmisTestFile)
                .assertThat().existsInRepo();
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).createFile(webdavTestFile)
                .assertThat().existsInWebdav().assertThat().existsInRepo();
        ftpProtocol.usingResource(testFolder1).createFile(ftpTestFile)
                .assertThat().existsInFtp().assertThat().existsInRepo();

        FolderModel cmisTestFolder = FolderModel.getRandomFolderModel();
        FolderModel webdavTestFolder = FolderModel.getRandomFolderModel();

        cmisAPI.usingResource(testFolder1).createFolder(cmisTestFolder)
                .assertThat().existsInRepo();
        webDavProtocol.usingResource(testFolder1).createFolder(webdavTestFolder).assertThat().existsInWebdav()
                .and().assertThat().existsInRepo();

        cmisAPI.usingResource(testFolder1).assertThat().hasChildren(cmisTestFile, webdavTestFile, ftpTestFile, cmisTestFolder, webdavTestFolder);

        STEP("Step 5: Using CMIS, U1 checks out a document from parent folder");
        cmisAPI.usingResource(cmisTestFile).checkOut().assertThat().documentIsCheckedOut();

        STEP("Step 6: Using WebDAV, U1 deletes parent folder (deleteFolder)");
        webDavProtocol.usingResource(testFolder1).assertThat().existsInRepo().and().assertThat().existsInWebdav()
                .and().assertThat().hasFiles(cmisTestFile, webdavTestFile, ftpTestFile)
                .and().assertThat().hasFolders(cmisTestFolder, webdavTestFolder)
                .then().usingResource(testFolder1).delete()
                .assertThat().doesNotExistInWebdav().assertThat().doesNotExistInRepo();

        STEP("Step 7. Using FTP, U1 verifies that parent folder and all children are not present");
        ftpProtocol.usingResource(testFolder1).assertThat().doesNotExistInRepo().assertThat().doesNotExistInFtp()
                .and().usingResource(cmisTestFile).assertThat().doesNotExistInRepo().assertThat().doesNotExistInFtp()
                .and().usingResource(webdavTestFile).assertThat().doesNotExistInRepo().assertThat().doesNotExistInFtp()
                .and().usingResource(ftpTestFile).assertThat().doesNotExistInRepo().assertThat().doesNotExistInFtp()
                .and().usingResource(cmisTestFolder).assertThat().doesNotExistInRepo().assertThat().doesNotExistInFtp()
                .and().usingResource(webdavTestFolder).assertThat().doesNotExistInRepo().assertThat().doesNotExistInFtp();
    }

    /**
     * Scenario 30
     * 1. Using CMIS create one test user: U1
     * 2. Using CMIS U1 creates a public test site
     * 3. Using WebDAV U1 creates a parent folder in public site's document library
     * 4. Using CMIS, WebDAV and FTP U1 creates multiple files and folders in parent folder
     * 5. Using CMIS, U1 checks out a document from parent folder
     * 6. Using FTP, U1 deletes parent folder (deleteDirectory)
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE})
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT}, executionType = ExecutionType.REGRESSION, description = "Verify deletion of a parentFolder with checkedout file using FTP.")
    public void deleteParentFolderWithCheckoutFileInUsingFTP() throws Exception
    {
        STEP("1. Using CMIS create one test user: U1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. Using CMIS U1 creates a public test site");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("3. Using WebDAV U1 creates a parent folder");
        testFolder1 = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(testFolder1).assertThat().existsInRepo().assertThat()
                .existsInWebdav();

        STEP("4. Using CMIS, WebDAV and FTP U1 creates multiple files and folders in parent folder");
        FileModel cmisTestFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file 1 content");
        FileModel webdavTestFile = FileModel.getRandomFileModel(FileType.HTML, "file 2 content");

        cmisAPI.authenticateUser(testUser1).usingResource(testFolder1).createFile(cmisTestFile)
                .assertThat().existsInRepo();
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).createFile(webdavTestFile)
                .assertThat().existsInWebdav().assertThat().existsInRepo();

        FolderModel cmisTestFolder = FolderModel.getRandomFolderModel();
        FolderModel webdavTestFolder = FolderModel.getRandomFolderModel();
        FolderModel ftpTestFolder = FolderModel.getRandomFolderModel();

        cmisAPI.usingResource(testFolder1).createFolder(cmisTestFolder)
                .assertThat().existsInRepo();
        webDavProtocol.usingResource(testFolder1).createFolder(webdavTestFolder)
                .assertThat().existsInWebdav().assertThat().existsInRepo();
        ftpProtocol.authenticateUser(testUser1).usingResource(testFolder1).createFolder(ftpTestFolder)
                .assertThat().existsInFtp().assertThat().existsInRepo();

        cmisAPI.usingResource(testFolder1).assertThat()
                .hasChildren(cmisTestFile, webdavTestFile, cmisTestFolder, webdavTestFolder, ftpTestFolder);

        STEP("Step 5: Using CMIS, U1 checks out a document from parent folder");
        cmisAPI.usingResource(webdavTestFile).checkOut().assertThat().documentIsCheckedOut();

        STEP("Step 6: Using FTP, U1 deletes parent folder (deleteDirectory)");
        ftpProtocol.usingResource(testFolder1).delete()
                .and().assertThat().doesNotExistInFtp().and().assertThat().doesNotExistInRepo();
    }
    
    /**
     * Scenario 32 - Document updates
     * 
     * 1. Using CMIS create 2 test users: U1 and U2
     * 2. Using CMIS and RestAPI U1 creates a public test site and U2 user is added with collaborator role
     * 3. Using WebDAV U1 creates a folder in his public site's document library
     * 4. Using WebDAV U1 creates a document inside the above folder
     * 5. Using RestAPI U2 adds the document to favorites
     * 6. Using CMIS U1 adds content to document
     * 7. Using WebDAV U2 validates document's content
     * 8. Using WebDAV U2 updates content from document
     * 9. Using CMIS U2 validates document's content
     * 10. Using WebDAV U1 deletes the file
     * 11. Using RestAPI U1 deletes the folder
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with collaborator role can update document content in a public site.")
    public void usersCanUpdateDocumentContentInsideAPublicSite() throws Exception
    {
        String originalContent = "originalContent";
        String updatedContent = "updatedContent";
        testFolder1 = FolderModel.getRandomFolderModel();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        
        STEP("1. Using CMIS create 2 test users: U1 and U2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();
        
        STEP("2. Using CMIS and RestAPI U1 creates a public test site and U2 user is added with collaborator role");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        testUser2.setUserRole(UserRole.SiteCollaborator);
        restAPI.authenticateUser(testUser1).withCoreAPI().usingSite(testSitePublic).addPerson(testUser2);
        
        STEP("3. Using WebDAV U1 creates a folder in his public site's document library");
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(testFolder1)
            .and().assertThat().existsInRepo();
        
        STEP("4. Using WebDAV U1 creates a document inside the above folder");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).createFile(testFile1)
            .and().assertThat().existsInWebdav()
            .and().assertThat().existsInRepo();
        
        STEP("5. Using RestAPI U2 adds the document to favorites");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().addFolderToFavorites(testFolder1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("6. Using CMIS U1 adds content to document");     
        cmisAPI.authenticateUser(testUser1).usingResource(testFile1)
        .and().assertThat().existsInRepo()
        .and().update(originalContent);
        
        STEP("7. Using WebDAV U2 validates document's content");
        webDavProtocol.authenticateUser(testUser2).usingResource(testFile1).assertThat().contentIs(originalContent);
        
        STEP("8. Using WebDAV U2 updates content from document");
        webDavProtocol.usingUser(testUser2).usingResource(testFile1)
        .assertThat().existsInRepo()
        .and().update(updatedContent);
        
        STEP("9. Using CMIS U2 validates document's content");
        cmisAPI.authenticateUser(testUser2).usingResource(testFile1).assertThat().contentIs(updatedContent);
        
        STEP("10. Using WebDAV U1 deletes the file");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile1).delete()
        .and().assertThat().doesNotExistInRepo();
        
        STEP("11. Using WebDAV U1 deletes the folder");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).delete()
        .and().assertThat().doesNotExistInRepo();          
    }

    /**
     * Scenario 33 - Document likes
     *
     * 1. Using CMIS create 2 test users: U1 and U2
     * 2. Using RestAPI U1 creates a public test site and U2 user is added with contributor role
     * 3. Using WebDAV U1 creates a folder in his public site's document library 
     * 4. Using WebDAV U1 creates a document1 inside the above folder 
     * 5. Using WebDAV U2 creates a document2 inside folder 
     * 6. Using RestAPI U2 likes document1 
     * 7. Using RestAPI U1 likes document2 
     * 8. Using RestAPI U1 dislikes document2 
     * 9. Using RestAPI U2 add to favorites document1 
     * 10. Using CMIS U1 delete document1 
     * 11. Using CMIS U2 delete the folder 
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE  }, expectedExceptions = {CmisPermissionDeniedException.class, CmisUnauthorizedException.class} )
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with contributor role can like/favorite a document in a public site.")
    public void usersCanLikeADocumentInPublicSite() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: U1 and U2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. Using RestAPI U1 creates a public test site and U2 user is added with contributor role");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        testUser2.setUserRole(UserRole.SiteContributor);
        restAPI.authenticateUser(testUser1).withCoreAPI().usingSite(testSitePublic).addPerson(testUser2);

        STEP("3. Using WebDAV U1 creates a folder in his public site's document library");
        testFolder1 = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic)
                .createFolder(testFolder1).assertThat().existsInWebdav()
                .and().assertThat().existsInRepo();

        STEP("4. Using WebDAV U1 creates a document1 inside the above folder");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        webDavProtocol.usingResource(testFolder1).createFile(testFile1)
                .assertThat().existsInRepo()
                .and().assertThat().existsInWebdav();

        STEP("5. Using WebDAV U2 creates a document2 inside folder");
        testFile2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        webDavProtocol.authenticateUser(testUser2)
                .usingResource(testFolder1).createFile(testFile2).assertThat().existsInRepo()
                .and().assertThat().existsInWebdav();

        STEP("6. Using RestAPI U2 likes document1");
        RestRatingModel returnedRatingModel = restAPI.authenticateUser(testUser2).withCoreAPI().usingResource(testFile1).likeDocument();
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true")
                .and().field("id").is("likes").and().field("aggregate").isNotEmpty();

        STEP("7. Using RestAPI U1 likes document2");
        returnedRatingModel = restAPI.authenticateUser(testUser1).withCoreAPI().usingResource(testFile2).likeDocument();
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true")
                .and().field("id").is("likes").and().field("aggregate").isNotEmpty();

        STEP("8. Using RestAPI U1 dislikes document2");
        restAPI.authenticateUser(testUser1).withCoreAPI().usingResource(testFile2).deleteLikeRating();
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        STEP("9. Using RestAPI U2 add to favorites document1");
        RestPersonFavoritesModel restPersonFavoritesModel = restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().addFileToFavorites(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        restPersonFavoritesModel.assertThat().field("targetGuid").is(testFile1.getNodeRefWithoutVersion());

        STEP("10. Using CMIS U1 delete document1");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile1).delete()
                .and().assertThat().doesNotExistInRepo();

        restAPI.withCoreAPI().usingResource(testFile1).getLikeRating();
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, testFile1.getNodeRef()));

        RestPersonFavoritesModelsCollection userFavorites = restAPI.withCoreAPI().usingAuthUser().getFavorites();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        userFavorites.assertThat().entriesListIsEmpty().and().paginationField("totalItems").is("0");

        STEP("11. Using CMIS U2 delete the folder. With Contributor role, U2 does not have permission to delete files/folders created by others.");
        cmisAPI.authenticateUser(testUser2).usingResource(testFolder1).assertThat().doesNotHaveFile(testFile1)
                .and().assertThat().hasFiles(testFile2)
                .and().usingResource(testFile2).delete()
                .and().usingResource(testFolder1).delete();
    }

    /**
     * Scenario 34 - Document ratings
     *
     * 1. Using CMIS create 2 test users: U1 and U2
     * 2. Using RestAPI U1 creates a public test site and U2 user is added with collaborator role
     * 3. Using CMIS U1 creates a new folder in site
     * 4. Using WebDAV U2 creates file1 in public site document library
     * 5. Using WebDAV U1 creates a file2 in public site document library
     * 6. Using RestAPI U2 rates with 5 stars file2
     * 7. Using RestAPI U1 rates with 1 star file1
     * 8. Using RestAPI U2 likes file1
     * 9. Using RestAPI U2 removes the rating of 5 stars for file2
     * 10. Using WebDAV U1 delete file1. Using RestAPI get ratings of file1.
     * 11. Using WebDAV U1 delete the folder
     */

    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify user with collaborator role can rate a document in a public site.")
    public void usersCanRateADocumentInPublicSite() throws Exception
    {
        RestRatingModel returnedRatingModel;

        STEP("1. Using CMIS create 2 test users: U1 and U2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. Using RestAPI U1 creates a public test site and U2 user is added with collaborator role");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        testUser2.setUserRole(UserRole.SiteCollaborator);
        restAPI.authenticateUser(testUser1).withCoreAPI().usingSite(testSitePublic).addPerson(testUser2);

        STEP("3. Using CMIS U1 creates a new folder in site");
        testFolder1 = FolderModel.getRandomFolderModel();
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic)
                .createFolder(testFolder1).assertThat().existsInRepo();

        STEP("4. Using WebDAV U2 creates file1 in public site document library");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        webDavProtocol.authenticateUser(testUser2).usingResource(testFolder1)
                .createFile(testFile1).assertThat().existsInRepo()
                .and().assertThat().existsInWebdav();

        STEP("5. Using WebDAV U1 creates a file2 in public site document library");
        testFile2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic)
                .usingResource(testFolder1).createFile(testFile2).assertThat().existsInRepo();

        STEP("6. Using RestAPI U2 rates with 5 stars file2");
        returnedRatingModel = restAPI.authenticateUser(testUser2).withCoreAPI().usingResource(testFile2)
                .rateStarsToDocument(5);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();

        STEP("7. Using RestAPI U1 rates with 1 star file1");
        returnedRatingModel = restAPI.authenticateUser(testUser1).withCoreAPI().usingResource(testFile1)
                .rateStarsToDocument(1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("1").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();

        STEP("8. Using RestAPI U2 likes file1");
        returnedRatingModel = restAPI.authenticateUser(testUser2).withCoreAPI().usingResource(testFile1)
                .likeDocument();
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        returnedRatingModel.assertThat().field("myRating").is("true").and().field("id").is("likes").and().field("aggregate").isNotEmpty();

        STEP("9. Using RestAPI U2 removes the rating of 5 stars for file2");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingResource(testFile2).deleteFiveStarRating();
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        returnedRatingModel = restAPI.withCoreAPI().usingResource(testFile2).getFiveStarRating();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        returnedRatingModel.getAggregate().assertThat().field("numberOfRatings").is("0");

        STEP("10. Using WebDAV U1 delete file1. Using RestAPI get ratings of file1.");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile1).delete()
                .and().assertThat().doesNotExistInRepo()
                .and().assertThat().doesNotExistInWebdav();

        restAPI.withCoreAPI().usingResource(testFile1).getLikeRating();
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, testFile1.getNodeRef()));

        restAPI.withCoreAPI().usingResource(testFile1).getFiveStarRating();
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, testFile1.getNodeRef()));

        STEP("11. Using WebDAV U1 delete the folder");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).assertThat().hasFiles(testFile2)
                .and().usingResource(testFolder1).delete()
                .and().assertThat().doesNotExistInRepo();
    }
    
    /**
     * Scenario 35
     * 1. Using CMIS create 2 test user: U1 and U2
     * 2. Using CMIS U1 creates a public test site
     * 3. Using FTP U1 creates a folder in public site's document library: folder1
     * 4. Using WebDAV U1 creates inside folder1 a new folder: folder2
     * 5. Using WebDAV U1 creates inside folder2 a new folder: folder3
     * 6. Using CMIS U1 creates inside folder1 a new file: file1
     * 7. Using WebDAV U1 creates inside folder2 a new file: file2
     * 8. Using FTP U1 creates inside folder3 a new file: file3
     * 9. Using WebDAV U1 deletes file1
     * 10. Using IMAP U1 deletes file2
     * 11. Using WebDAV U1 updates content of file3
     * 12. Using RestAPI U1 adds a comment to file3
     * 13. Using RestAPI U1 likes file3 and user U2 rates file3
     * 14. Using CMIS U1 deletes file3
     */
  
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "File handling - perform actions: create, delete, add comment, like and rate, update file content ")
    public void fileHandlingCreateUpdateContentDeleteLikeAndRate() throws Exception
    {
        STEP("1. Using CMIS create 1 test user: U1 and U2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createIMAPSite();

        STEP("3. Using FTP U1 creates folder: folder1"); 
        FolderModel folder1 = FolderModel.getRandomFolderModel();
        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(folder1)
                   .assertThat().existsInRepo();
        
        STEP("4. Using WebDAV U1 creates folder2 inside folder1");
        FolderModel folder2 = FolderModel.getRandomFolderModel();        
        webDavProtocol.authenticateUser(testUser1).usingResource(folder1).createFolder(folder2);
        
        STEP("5. Using webDAV U1 creates folder3 inside folder2");
        FolderModel folder3 = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser1).usingResource(folder2).createFolder(folder3);
        
        STEP("6. Using CMIS U1 creates file1 inside folder1");         
        FileModel file1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file 1 content");
        cmisAPI.authenticateUser(testUser1).usingResource(folder1).createFile(file1)
        .assertThat().existsInRepo();
        
        STEP("7. Using webDAV U1 creates file2 inside folder2");        
        FileModel file2 = FileModel.getRandomFileModel(FileType.HTML, "file 2 content");
        webDavProtocol.authenticateUser(testUser1).usingResource(folder2).createFile(file2)
        .assertThat().existsInWebdav().assertThat().existsInRepo();
        
        STEP("8. Using FTP U1 creates file3 inside folder3");
        FileModel file3 = FileModel.getRandomFileModel(FileType.MSWORD, "file 3 content");
        ftpProtocol.authenticateUser(testUser1).usingResource(folder3).createFile(file3)
                   .assertThat().existsInRepo();
        
        STEP("9. Using webDAV U1 deletes file1"); 
        webDavProtocol.authenticateUser(testUser1).usingResource(file1).delete()
                      .assertThat().doesNotExistInRepo().and().assertThat().doesNotExistInWebdav();
        
        STEP("10. Using IMAP U1 deletes file2");    
        imapProtocol.authenticateUser(testUser1).usingResource(file2).deleteMessage()
                    .and().usingResource(file2).assertThat().doesNotExistInRepo();
        
        STEP("11. Using WebDAV U1 update content of file3");
        webDavProtocol.authenticateUser(testUser1)
                       .usingResource(file3).assertThat().existsInRepo()
                       .update(newContent).assertThat().contentIs(newContent);
        
        STEP("12. Using RestAPI U1 adds a comment to file3"); 
        String comment = RandomData.getRandomName("comment1");
        file3.setNodeRef(dataContent.usingUser(testUser1).usingSite(testSitePublic)
             .usingResource(folder3).usingResource(file3).getNodeRef());
        RestCommentModel commentModel  = restAPI.authenticateUser(testUser1).withCoreAPI()
                                         .usingResource(file3).addComment(comment);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);       
        commentModel.assertThat().field("content").isNotEmpty()
                     .and().field("content").is(comment);
                                  
        STEP("13. Using RestAPI U1 likes file3 and user U2 rates file3"); 
        returnedRatingModel = restAPI.authenticateUser(testUser1).withCoreAPI().usingResource(file3).likeDocument();
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("true").and().field("id").is("likes").and().field("aggregate").isNotEmpty();
        
        returnedRatingModel = restAPI.authenticateUser(testUser2).withCoreAPI()
                              .usingResource(file3).rateStarsToDocument(5);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar").and().field("aggregate").isNotEmpty();
                
        STEP("14. Using CMIS U1 deletes file3"); 
        cmisAPI.authenticateUser(testUser1).usingResource(file3).delete()
                .assertThat().doesNotExistInRepo();        
    }

    /**
     * Scenario 37
     * 1. Using CMIS creates 2 users: U1 and U  
     * 2. U1 creates a public test site using CMIS
     * 3. Using CMIS U1 creates folder: folder1
     * 4. Using WebDAV U1 creates folder2 and folder3 inside folder1
     * 5. Using FTP U1 creates file1 inside folder1
     * 6. Using CMIS U1 copies file1 to folder2
     * 7. Using WebDAV U1 update content of file1 from folder2
     * 8. Using FTP U1 tries to move file1 from folder2 to folder1
     * 9. Using WebDAV U1 updates content of file1 from folder1
     * 10. Using RestAPI U1 adds file1 to favorites
     * 11. Using RestAPI U1 likes file1 and U2 user rates file1
     * 12. Using CMIS U1 moves file1 from folder1 to folder3
     * 13. Using CMIS/RestAPI U1 checks that content, favorites and ratings are kept
      */
  
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "File handling - perform actions: copy, move, add/update content, favorites, like and rate")
    public void fileHandlingCopyMoveAddUpdateContentFavoritesLikeAndRate() throws Exception
    {
        STEP("1. Using CMIS creates 2 users: U1 and U2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();
        
        STEP("2. U1 creates a public test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("3. Using CMIS U1 creates folder: folder1"); 
        FolderModel folder1 = FolderModel.getRandomFolderModel();
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(folder1)
                   .assertThat().existsInRepo();
        
        STEP("4. Using WebDAV U1 creates folder2 and folder3 inside folder1");
        FolderModel folder2 = FolderModel.getRandomFolderModel();        
        FolderModel folder3 = FolderModel.getRandomFolderModel();        
        webDavProtocol.authenticateUser(testUser1)
                .usingResource(folder1).createFolder(folder2).and().assertThat().existsInWebdav()
                .usingResource(folder1).createFolder(folder3).and().assertThat().existsInWebdav();
        
        STEP("5. Using FTP U1 creates file1 inside folder1");
        FileModel file1 = FileModel.getRandomFileModel(FileType.MSWORD2007, "tasTesting");
        ftpProtocol.authenticateUser(testUser1).usingResource(folder1).createFile(file1)
             .assertThat().existsInRepo();             
        
        STEP("6. Using CMIS U1 copies file1 to folder2");
        FileModel copiedFile = new FileModel(file1);
        cmisAPI.authenticateUser(testUser1).usingResource(file1).copyTo(folder2);
        copiedFile.setCmisLocation(cmisAPI.getLastResource());
        cmisAPI.usingResource(folder2).assertThat().hasFiles(file1);
        
        STEP("7. Using WebDAV U1 update content of file1 from folder2");
        webDavProtocol.usingResource(copiedFile).assertThat().existsInRepo()
                       .update(newContent).assertThat().contentIs(newContent);
        
        STEP("8. Using FTP U1 tries to move file1 from folder2 to folder1");
        ftpProtocol.authenticateUser(testUser1)
                .usingResource(copiedFile).moveTo(folder1).assertThat().existsInRepo();
        Assert.assertTrue(ftpProtocol.usingResource(folder1).getFiles().size() == 1);
        
        STEP("9. Using WebDAV U1 updates content of file1 from folder1");
        webDavProtocol.usingResource(file1).update("content folder1")
                   .assertThat().contentIs("content folder1");
        
        STEP("10. Using RestAPI U1 adds file1 to favorites"); 
        file1.setNodeRef(dataContent.usingUser(testUser1).usingSite(testSitePublic)
                .usingResource(folder1).usingResource(file1).getNodeRef());
        
        restPersonFavoritesModel = restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingAuthUser().addFileToFavorites(file1);
        restAPI.assertStatusCodeIs(org.springframework.http.HttpStatus.CREATED);
        restPersonFavoritesModel.assertThat().field("targetGuid").is(file1.getNodeRefWithoutVersion());     
        
        STEP("11. Using RestAPI U1 likes file1 and U2 user rates file1");         
        returnedRatingModel = restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingResource(file1).likeDocument();
        restAPI.assertStatusCodeIs(org.springframework.http.HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("true").and().field("id").is("likes")
                           .and().field("aggregate").isNotEmpty();
        
        returnedRatingModel = restAPI.authenticateUser(testUser2).withCoreAPI()
                .usingResource(file1).rateStarsToDocument(5);
        restAPI.assertStatusCodeIs(org.springframework.http.HttpStatus.CREATED);
        returnedRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar") 
                           .and().field("aggregate").isNotEmpty();
        
        STEP("12. Using CMIS U1 moves file1 from folder1 to folder3"); 
        cmisAPI.usingSite(testSitePublic).usingResource(file1).moveTo(folder3)
                .and().assertThat().existsInRepo();    
        
        STEP("13. Using CMIS/RestAPI U1 checks that content, favorites and ratings are kept");
        returnedRatingModel.assertThat().field("myRating").is("5").and().field("id").is("fiveStar")
                           .and().field("aggregate").isNotEmpty();
        restPersonFavoritesModel.assertThat().field("targetGuid").is(file1.getNodeRefWithoutVersion());            
    }
    
    /**
     * Scenario 38
     * 1. Using CMIS create one test user: U1
     * 2. Using CMIS U1 creates a new document in Share
     * 3. Using WebDAV U1 creates a new document in User Home
     * 4. Using WebDAV U1 updates both documents
     * 5. Verify if only first document's version is increased using CMIS
     * 6. Verify if content is updated using WebDav
     * 7. Verify if the size of the document is increased
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, description = "Verify document versioning after appending content using WebDAV.")
    public void checkDocumentVersionAfterAppendingContent() throws Exception
    {
        STEP("1. Using CMIS create one test user: U1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. Using CMIS U1 creates a new document in Share");
        FileModel cmisTestFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file 1 content");
        cmisAPI.authenticateUser(testUser1)
                .usingShared().createFile(cmisTestFile)
                .assertThat().existsInRepo();

        STEP("3. Using WebDAV U1 creates a new document in User Home");
        FileModel webDAVTestFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file 2 content");
        webDavProtocol.authenticateUser(testUser1)
                .usingUserHome().createFile(webDAVTestFile)
                .assertThat().existsInWebdav().and()
                .assertThat().existsInRepo();

        STEP("4. Using WebDAV U1 updates both documents");
        webDavProtocol.usingResource(cmisTestFile).update("cmis file content")
                .usingResource(webDAVTestFile).update("WebDAV file content");

        STEP("5. Verify if only first document's version is increased using CMIS");
        cmisAPI.usingResource(cmisTestFile).assertThat().documentHasVersion(1.1)
                .usingResource(webDAVTestFile).assertThat().documentHasVersion(1.0);

        STEP("6. Verify if content is updated using WebDav");
        webDavProtocol.authenticateUser(testUser1)
                .usingResource(cmisTestFile).assertThat().contentIs("cmis file content")
                .usingResource(webDAVTestFile).assertThat().contentIs("WebDAV file content");

        STEP("7. Verify if the size of the document is increased");
        cmisAPI.usingResource(cmisTestFile).assertThat().contentLengthIs(17)
                .usingResource(webDAVTestFile).assertThat().contentLengthIs(19);
    }

    /**
     * Scenario 40
     * 1. Using CMIS create test user: U1
     * 2. U1 creates a private test site using CMIS
     * 3. Using WebDAV U1 creates a folder
     * 4. Using WebDAV U1 creates a file inside the folder
     * 5. Using CMIS, check out the document
     * 6. Using CMIS verify if PWC is created
     * 7. Using CMIS check in document with content
     * 8. Using CMIS verify that version does not increase
     * 9. Using CMIS verify new content is added to document
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
        description = "Verify increased version and new content of a file that was check out then check in.")
    public void verifyIncreasedVersionAndNewContentOfCheckInDocument() throws Exception
    {
        STEP("Step 1. Using CMIS create test user: U1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("Step 2. U1 creates a private test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPrivateRandomSite();

        STEP("Step 3. Using WebDAV U1 creates a folder");
        FolderModel folder = FolderModel.getRandomFolderModel();        
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(folder);
        
        STEP("Step 4. Using WebDAV U1 creates a file inside the folder"); 
        FileModel file = FileModel.getRandomFileModel(FileType.XML, "file content");
        webDavProtocol.authenticateUser(testUser1).usingResource(folder).createFile(file)
                      .assertThat().existsInWebdav().assertThat().existsInRepo();
        
        STEP("Step 5: Using CMIS, check out the document and verify document version");
        cmisAPI.authenticateUser(testUser1).usingResource(file).checkOut()
               .assertThat().documentIsCheckedOut();

        STEP("Step 6: Using CMIS verify if PWC is created");
        FileModel filePWC = cmisAPI.usingResource(file).withCMISUtil().getPWCFileModel();
        cmisAPI.usingResource(filePWC).assertThat().existsInRepo();
        
        STEP("Step 7: Using CMIS check in document with content");
        String newContent = "new major content";
        cmisAPI.usingResource(folder).assertThat().folderHasCheckedOutDocument(file);
        cmisAPI.usingResource(file).prepareDocumentForCheckIn()
               .withMajorVersion()
               .withContent(newContent)
               .checkIn().refreshResource()
               .and().assertThat().documentIsNotCheckedOut();    
        
        STEP("Step 8: Using CMIS verify that version does not increase");
        cmisAPI.usingResource(file).assertThat().documentHasVersion(1.0);
        
        STEP("Step 9: Using CMIS verify new content is added to document");
        cmisAPI.usingResource(file).assertThat().contentIs(newContent);
    }

    /**
     * Scenario 41
     * 1. Using CMIS create test user: U1
     * 2. U1 creates a private test site using CMIS
     * 3. Using WebDAV U1 creates a folder
     * 4. Using WebDAV U1 creates a file inside the folder
     * 5. Using CMIS, check out the document
     * 6. Using CMIS verify if PWC is created
     * 7. Using CMIS, cancel check out
     * 8. Using CMIS, verify original document has version 1.0
     * 9. Using CMIS, verify original document has same content
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify that version and the content of a file are not changed if the checkout is canceled.")
    public void verifyVersionAndContentOfACanceledCheckoutDocument() throws Exception
    {
        STEP("Step 1. Using CMIS create test user: U1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("Step 2. U1 creates a private test site using CMIS");
        testSitePublic = dataSite.usingUser(testUser1).createPrivateRandomSite();

        STEP("Step 3. Using WebDAV U1 creates a folder");
        FolderModel folder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(folder);

        STEP("Step 4. Using WebDAV U1 creates a file inside the folder");
        FileModel file = FileModel.getRandomFileModel(FileType.XML, "file content");
        webDavProtocol.authenticateUser(testUser1).usingResource(folder).createFile(file)
                .assertThat().existsInWebdav().assertThat().existsInRepo();

        STEP("Step 5: Using CMIS, check out the document");
        cmisAPI.authenticateUser(testUser1).usingResource(file).checkOut();

        STEP("Step 6: Using CMIS verify if PWC is created");
        FileModel filePWC = cmisAPI.usingResource(file).withCMISUtil().getPWCFileModel();
        cmisAPI.usingResource(filePWC).assertThat().existsInRepo();

        STEP("Step 7: Using CMIS, cancel check out");
        cmisAPI.usingResource(file).cancelCheckOut().then()
                .assertThat().documentIsNotCheckedOut();

        STEP("Step 8: Using CMIS, verify original document has version 1.0");
        cmisAPI.usingResource(file).assertThat().isNotPrivateWorkingCopy()
                .then().assertThat().documentHasVersion(1.0);

        STEP("Step 9: Using CMIS, verify original document has same content");
        cmisAPI.usingResource(file).assertThat().contentIs("file content");
    }

    /**
     * Scenario 42
     * 1. Using CMIS create one test user: U1
     * 2. Using CMIS U1 creates a file: file
     * 3. Using WebDAV U1 renames file and verify if the new document exists
     * 4. Using FTP verify old document does not exists
     */
  
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Edit document name and verify document with new name")
    public void renameDocument() throws Exception
    {
        STEP("1. Using CMIS create test user: U1");
        testUser1 = dataUser.createRandomTestUser();       

        STEP("2. Using CMIS U1 creates file"); 
        FileModel file = FileModel.getRandomFileModel(FileType.PDF, "file content");
        cmisAPI.authenticateUser(testUser1).usingUserHome().createFile(file)
               .assertThat().existsInRepo();

        STEP("3. Using WebDAV U1 renames file and verify if the new document exists");
        FileModel oldFile = new FileModel(file);
        webDavProtocol.authenticateUser(testUser1).usingResource(file).rename(renamePrefix + file.getName())
                    .and().assertThat().existsInRepo().and().assertThat().existsInWebdav();
        
        STEP("4. Using FTP verify old document does not exists");      
        ftpProtocol.authenticateUser(testUser1).usingResource(oldFile)
                   .assertThat().doesNotExistInRepo()
                   .and().assertThat().doesNotExistInFtp();
    }
    
    /**
     * Scenario 43
     * 1. Using CMIS create test user: U1   
     * 2. Using WebDAV U1 creates a file using shared
     * 3. Using webDAV append content to document
     * 4. Using CMIS, verify original document has version 1.0
     * 5. Using CMIS delete document
     * 6. Using WebDAV, verify document is deleted
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
        description = "Delete document after append content and verify document version.")
    public void deleteDocumentAfterAppendContent() throws Exception
    {
        STEP("Step 1. Using CMIS create test user: U1");
        testUser1 = dataUser.createRandomTestUser();
                    
        STEP("Step 2. Using WebDAV U1 creates a file using shared");
        FileModel file = FileModel.getRandomFileModel(FileType.XML, "file content");
        webDavProtocol.authenticateUser(testUser1).usingUserHome().createFile(file)
                      .assertThat().existsInRepo();
        
        STEP("Step 3. Using webDAV append content to document"); 
        String newContentToAppend = " - append this text to the file";
        webDavProtocol.authenticateUser(testUser1).usingResource(file).assertThat().contentIs("file content")
                   .then().update(file.getContent() + newContentToAppend)
                   .assertThat().contentIs(file.getContent() + newContentToAppend);    
        
        STEP("Step 4: Using CMIS, verify original document has version 1.0");
        cmisAPI.authenticateUser(testUser1).usingResource(file).assertThat().isNotPrivateWorkingCopy()
                .then().assertThat().documentHasVersion(1.0);
        
        STEP("Step 4: Using CMIS, delete document");
        cmisAPI.usingResource(file).assertThat().existsInRepo().delete();
        
        STEP("Step 5: Using WebDAV, verify document is deleted");
        webDavProtocol.authenticateUser(testUser1).usingResource(file).assertThat().doesNotExistInWebdav()
                    .and().assertThat().doesNotExistInRepo();
    }

    /**
     * Scenario 44
     * 1. Using CMIS create one test user: U1
     * 2. Using CMIS U1 creates a public test site
     * 3. Using WebDAV U1 creates a new file in User Home
     * 4. Using WebDAV U1 creates another file in public site document library.
     * 5. Using CMIS U1 creates a relationship between documents
     * 6. Verify if relationship is created
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, description = "Verify if relationship is created between 2 items using CMIS.")
    public void checkRelationshipBetween2Files() throws Exception
    {
        STEP("1. Using CMIS create one test user: U1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. Using CMIS U1 creates a public test site");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("3. Using WebDAV U1 creates a new file in User Home");
        FileModel sourceFile= FileModel.getRandomFileModel(FileType.PDF, "file content");
        webDavProtocol.authenticateUser(testUser1).usingUserHome()
                .createFile(sourceFile).assertThat().existsInRepo();

        STEP("4. Using WebDAV U1 creates another file in public site document library.");
        FileModel targetFile= FileModel.getRandomFileModel(FileType.PDF, "file content");
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic)
                .createFile(targetFile).assertThat().existsInRepo();

        STEP("5. Using CMIS U1 creates a relationship between documents. 6. Verify if relationship is created");
        cmisAPI.authenticateUser(testUser1).usingResource(sourceFile).createRelationshipWith(targetFile)
                .assertThat().objectHasRelationshipWith(targetFile);
    }

    /**
     * Scenario 46
     * 1. Using CMIS create one test user: U1
     * 2. Using CMIS U1 creates an imap test site
     * 3. Using CMIS U1 creates cmisFolder in Shared
     * 4. Using IMAP U1 creates parentFolder in private site document library.
     * 5. Using FTP U1 creates a subfolder in parentFolder
     * 6. Using WebDAV U1 creates a webdavFolder in User Home
     * 7. Using WebDAV U1 creates a document in parentFolder
     * 8. Using CMIS U1 adds document to cmisFolder, subfolder, webdavFolder
     * 9. Using WebDAV verify document is present in all folders
     * 10. Using CMIS U1 removes document from subFolder
     * 11. Using WebDAV verify document is not present in subFolder
     * 12. Using FTP U1 deletes original document from parentFolder
     * 13. Using CMIS verify document does not exist
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, description = "Verify parents list for an object using CMIS.")
    public void addAndRemoveDocumentFromFolders() throws Exception
    {
        STEP("1. Using CMIS create one test user: U1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. Using CMIS U1 creates an imap test site");
        testSitePublic = dataSite.usingUser(testUser1).createIMAPSite();

        STEP("3. Using CMIS U1 creates cmisFolder in Shared");
        FolderModel cmisFolder = FolderModel.getRandomFolderModel();
        cmisAPI.authenticateUser(testUser1)
                .usingShared().createFolder(cmisFolder)
                .assertThat().existsInRepo();

        STEP("4. Using IMAP U1 creates parentFolder in private site document library.");
        FolderModel parentFolder = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(testUser1)
                .usingSite(testSitePublic).createFolder(parentFolder)
                .assertThat().existsInRepo();

        STEP("5. Using FTP U1 creates a subfolder in parentFolder");
        FolderModel subFolder = FolderModel.getRandomFolderModel();
        ftpProtocol.authenticateUser(testUser1)
                .usingResource(parentFolder).createFolder(subFolder)
                .assertThat().existsInRepo();

        STEP("6. Using WebDAV U1 creates a webdavFolder in User Home");
        FolderModel webdavFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser1)
                .usingUserHome().createFolder(webdavFolder)
                .assertThat().existsInRepo();

        STEP("7. Using WebDAV U1 creates a document in parentFolder");
        testFile1= FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file content");
        webDavProtocol.authenticateUser(testUser1)
                .usingResource(parentFolder).createFile(testFile1)
                .assertThat().existsInRepo();

        STEP("8. Using CMIS U1 adds document to cmisFolder, subfolder, webdavFolder");
        cmisAPI.usingResource(testFile1)
                .addDocumentToFolder(cmisFolder, true).assertThat().hasParents(parentFolder.getName(), cmisFolder.getName())
                .addDocumentToFolder(subFolder, true).assertThat().hasParents(parentFolder.getName(), cmisFolder.getName(), subFolder.getName())
                .addDocumentToFolder(webdavFolder, true).assertThat().hasParents(parentFolder.getName(), cmisFolder.getName(), subFolder.getName(), webdavFolder.getName());

        STEP("9. Using WebDAV verify document is present in all folders");
        FileModel fileAddedInCMISFolder = new FileModel(testFile1.getName(), testFile1.getTitle(), testFile1.getDescription(), testFile1.getFileType(), testFile1.getContent());
        fileAddedInCMISFolder.setCmisLocation(Utility.buildPath(cmisFolder.getCmisLocation(), testFile1.getName()));

        FileModel fileAddedInSubFolder = new FileModel(testFile1.getName(), testFile1.getTitle(), testFile1.getDescription(), testFile1.getFileType(), testFile1.getContent());
        fileAddedInSubFolder.setCmisLocation(Utility.buildPath(subFolder.getCmisLocation(), testFile1.getName()));

        FileModel fileAddedInWebDAVFolder = new FileModel(testFile1.getName(), testFile1.getTitle(), testFile1.getDescription(), testFile1.getFileType(), testFile1.getContent());
        fileAddedInWebDAVFolder.setCmisLocation(Utility.buildPath(webdavFolder.getCmisLocation(), testFile1.getName()));

        webDavProtocol.usingResource(fileAddedInCMISFolder).assertThat().existsInWebdav()
                .usingResource(fileAddedInSubFolder).assertThat().existsInWebdav()
                .usingResource(fileAddedInWebDAVFolder).assertThat().existsInWebdav();

        STEP("10. Using CMIS U1 removes document from subFolder");
        cmisAPI.usingResource(testFile1).removeDocumentFromFolder(subFolder)
                .assertThat().hasParents(parentFolder.getName(), cmisFolder.getName(), webdavFolder.getName());

        STEP("11. Using WebDAV verify document is not present in subFolder");
        webDavProtocol.usingResource(testFile1).assertThat().existsInWebdav()
                .usingResource(fileAddedInCMISFolder).assertThat().existsInWebdav()
                .usingResource(fileAddedInSubFolder).assertThat().doesNotExistInWebdav()
                .usingResource(fileAddedInWebDAVFolder).assertThat().existsInWebdav();

        STEP("12. Using FTP U1 deletes original document from parentFolder");
        ftpProtocol.usingResource(testFile1).delete().assertThat().doesNotExistInFtp();

        STEP("13. Using CMIS verify document does not exist");
        cmisAPI.usingResource(fileAddedInCMISFolder).assertThat().doesNotExistInRepo()
                .usingResource(fileAddedInWebDAVFolder).assertThat().doesNotExistInRepo();
    }

    /**
     * Scenario 47 - Delete many documents
     *
     * 1. Using CMIS create test user: U1
     * 2. Using CMIS U1 creates a public test site
     * 3. Using CMIS and WebDAV U1 creates parentFolder1, doc1 and doc2 in document library
     * 4. Using WebDAV and FTP U1 creates inside parentFolder1: childDoc1 to childDoc5
     * 5. Using IMAP bulk delete doc1 and doc2
     * 6. Using CMIS verify that doc1 and doc2 are deleted
     * 7. Using IMAP delete childDoc1 to childDoc4
     * 8. Using WebDAV and FTP verify if docs are deleted from their folders
     */

    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT}, executionType = ExecutionType.REGRESSION,
            description = "Verify users can delete many documents in a public site using different protocols.")
    public void usersCanDeleteManyDocuments() throws Exception
    {
        FolderModel parentFolder1 = FolderModel.getRandomFolderModel();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        testFile2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel childDoc1, childDoc2, childDoc3, childDoc4, childDoc5;
        childDoc1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        childDoc2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        childDoc3 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        childDoc4 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        childDoc5 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);

        STEP("1. Using CMIS create test user: U1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. Using CMIS U1 creates a public test site");
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("3. Using CMIS and WebDAV U1 creates parentFolder1, doc1 and doc2 in document library");
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(parentFolder1)
                .and().assertThat().existsInRepo();

        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFile(testFile1)
                .and().createFile(testFile2);

        STEP("4. Using WebDAV and FTP U1 creates inside parentFolder1: childDoc1 to childDoc5");
        webDavProtocol.authenticateUser(testUser1).usingResource(parentFolder1)
                .createFile(childDoc1).and().createFile(childDoc2);

        ftpProtocol.authenticateUser(testUser1).usingResource(parentFolder1)
                .createFile(childDoc3).and().createFile(childDoc4).and().createFile(childDoc5);

        STEP("5. Using IMAP bulk delete doc1 and doc2");
        imapProtocol.authenticateUser(testUser1).usingSite(testSitePublic).deleteMessage(testFile1.getName(), testFile2.getName()).assertThat().doesNotContainMessages(testFile1, testFile2);

        STEP("6. Using CMIS verify that doc1 and doc2 are deleted");
        Utility.sleep(500, 10000, () ->
                cmisAPI.authenticateUser(testUser1)
                    .usingResource(testFile1).assertThat().doesNotExistInRepo()
                    .and().usingResource(testFile2).assertThat().doesNotExistInRepo());

        STEP("7. Using IMAP delete childDoc1 to childDoc4");
        imapProtocol.authenticateUser(testUser1).usingResource(parentFolder1).deleteMessage(childDoc1.getName(), childDoc2.getName(), childDoc3.getName(), childDoc4.getName())
                .assertThat().doesNotContainMessages(childDoc1, childDoc2, childDoc3, childDoc4);

        STEP("8. Using WebDAV and FTP verify if docs are deleted from their folders");
        Utility.sleep(500, 10000, () ->
                webDavProtocol.authenticateUser(testUser1).usingResource(parentFolder1).assertThat().hasFiles(childDoc5)
                    .and().usingResource(childDoc1).assertThat().doesNotExistInRepo()
                    .and().usingResource(childDoc2).assertThat().doesNotExistInRepo());

        ftpProtocol.authenticateUser(testUser1).usingResource(childDoc3)
                .assertThat().doesNotExistInRepo()
                .and().usingResource(childDoc4).assertThat().doesNotExistInRepo();
    }

    /**
     * Scenario 48 - Move folder in another folder
     * 
     * 1. Using CMIS create user u1 and public site
     * 2. Using WebDAV U1 creates folder1 and folder2
     * 3. Using WebDAV U1 creates doc1 in folder1
     * 4. Using FTP U1 moves folder1 in folder2
     * 5. Using WebDAV verify that folder1 is not in the original location and is present in folder2
     * 6. Using CMIS verify if doc1 is still present in folder1
     */
    
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, description = "Verify user is able to move a non empty folder to another folder from the same site.")
    public void moveFolderInAnotherFolder() throws Exception
    {
        testFolder1 = FolderModel.getRandomFolderModel();
        testFolder2 = FolderModel.getRandomFolderModel();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        
        STEP("1. Using CMIS create user u1 and public site");
        testUser1 = dataUser.createRandomTestUser();
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        
        STEP("2. Using WebDAV U1 creates folder1 and folder2");
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic)
        .createFolder(testFolder1)
        .and().assertThat().existsInRepo().and().assertThat().existsInWebdav()
        .then().createFolder(testFolder2)
        .and().assertThat().existsInRepo().and().assertThat().existsInWebdav();
        
        STEP("3. Using WebDAV U1 creates doc1 in folder1");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).createFile(testFile1)
        .and().assertThat().existsInRepo().and().assertThat().existsInWebdav()
        .and().usingResource(testFolder1).assertThat().hasFiles(testFile1);
              
        STEP("4. Using FTP U1 moves folder1 in folder2");
        ftpProtocol.authenticateUser(testUser1)
            .usingResource(testFolder1).moveTo(testFolder2)
            .assertThat().existsInRepo()
                .then().usingResource(testFolder1).assertThat().doesNotExistInRepo();
     
        STEP("5. Using WebDAV verify that folder1 is not in the original location and is present in folder2");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).assertThat().doesNotExistInRepo()
        .and().usingResource(testFolder2).assertThat().hasFolders(testFolder1);
        
        STEP("6. Using CMIS verify if doc1 is still present in folder1");
        FolderModel newTestFolder1 = new FolderModel(testFolder1.getName(), testFolder1.getTitle(), testFolder1.getDescription());
        newTestFolder1.setCmisLocation(Utility.buildPath(testFolder2.getCmisLocation(), testFolder1.getName()));
        
        FileModel newTestFile1 = new FileModel(testFile1.getName(), testFile1.getTitle(), testFile1.getDescription(), testFile1.getFileType(), testFile1.getContent());
        newTestFile1.setCmisLocation(Utility.buildPath(newTestFolder1.getCmisLocation(), testFile1.getName()));
        
        cmisAPI.authenticateUser(testUser1).usingResource(newTestFolder1)
        .assertThat().existsInRepo()
        .assertThat().hasFiles(newTestFile1);
    }
    
    /**
     * Scenario 49 - Copy folder in another folder
     * 
     * 1. Using CMIS create user u1 and public site
     * 2. Using CMIS U1 creates folder1 and folder2
     * 3. Using WebDAV U1 creates doc1 in folder1
     * 4. Using WebDAV U1 copies folder1 in folder2
     * 5. Using FTP verify that folder1 is in the original location and contains doc1
     * 6. Using WebDAV verify that folder1 is in folder2 and contains doc1
     */
    
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, description = "Verify user is able to copy non empty folder to another folder in the same site.")
    public void copyFolderInAnotherFolder() throws Exception
    {       
        testFolder1 = FolderModel.getRandomFolderModel();
        testFolder2 = FolderModel.getRandomFolderModel();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);

        STEP("1. Using CMIS create user u1 and public site");
        testUser1 = dataUser.createUser(RandomStringUtils.randomAlphanumeric(20));
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        
        STEP("2. Using CMIS U1 creates folder1 and folder2");               
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic)
                .createFolder(testFolder1).and().assertThat().existsInRepo().and()
                .createFolder(testFolder2).and().assertThat().existsInRepo();
        
        STEP("3. Using WebDAV U1 creates doc1 in folder1");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1)
        .createFile(testFile1).and().assertThat().existsInRepo()
                .and().assertThat().existsInWebdav();
        
        STEP("4. Using WebDAV U1 copies folder1 in folder2");
        testFolder2.setProtocolLocation(webDavProtocol.getPrefixSpace() + testFolder2.getCmisLocation());
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).copyTo(testFolder2)
                .assertThat().existsInRepo()
                .then().usingResource(testFolder1).assertThat().existsInRepo();
        
        STEP("5. Using FTP verify that folder1 is in the original location and contains doc1");
        ftpProtocol.authenticateUser(testUser1).usingResource(testFolder1)
                .assertThat().existsInRepo()
                .and().assertThat().existsInFtp()
                .and().assertThat().hasFiles(testFile1);

        STEP("6. Using WebDAV verify that folder1 is in folder2 and contains doc1");
        FolderModel newTestFolder1 = new FolderModel(testFolder1.getName(), testFolder1.getTitle(), testFolder1.getDescription());
        newTestFolder1.setCmisLocation(Utility.buildPath(testFolder2.getCmisLocation(), testFolder1.getName()));
        
        FileModel newTestFile1 = new FileModel(testFile1.getName(), testFile1.getTitle(), testFile1.getDescription(), testFile1.getFileType(), testFile1.getContent());
        newTestFile1.setCmisLocation(Utility.buildPath(newTestFolder1.getCmisLocation(), testFile1.getName()));
        
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder2)
                .assertThat().hasFolders(newTestFolder1)
                .and().usingResource(newTestFolder1)
                .assertThat().existsInRepo().and().assertThat().existsInWebdav()
                .and().assertThat().hasFiles(newTestFile1);
    }

    /**
     * Scenario 50 - Rename site
     *
     * 1. Using CMIS create one test user: U1
     * 2. Using CMIS U1 creates a moderated site
     * 3. Using WebDAV U1 creates a folder in the moderated site document library
     * 4. Using CMIS U1 tries to rename site
     * 5. Verify that a site cannot be renamed
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.CORE }, expectedExceptions = CmisRuntimeException.class, expectedExceptionsMessageRegExp = "^.*Sites can not be renamed.$")
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, description = "Verify sites cannot be renamed using CMIS.")
    public void renameSite() throws Exception
    {
        STEP("1. Using CMIS create one test user: U1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2.  Using CMIS U1 creates a moderated site");
        testSiteModerated = dataSite.usingUser(testUser1).createModeratedRandomSite();

        STEP("3. Using WebDAV U1 creates a folder in the moderated site document library");
        testFolder1 = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser1)
                .usingSite(testSiteModerated).createFolder(testFolder1)
                .assertThat().existsInWebdav()
                .assertThat().existsInRepo();

        STEP("4. Using CMIS U1 tries to rename site 5. Verify that a site cannot be renamed");
        FolderModel siteFolder = new FolderModel(String.format("/Sites/%s", testSiteModerated.getId()));
        cmisAPI.authenticateUser(testUser1).usingResource(siteFolder).rename("renamedSite");
    }
}
