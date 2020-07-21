package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/28/2016.
 */
public class CopyTests extends CmisTest
{
    UserModel inexistentUser;
    UserModel unauthorizedUser;
    UserModel testUser;
    UserModel contributorUser;
    UserModel collaboratorUser;
    UserModel consumerUser;
    SiteModel testSite;
    FileModel sourceFile;
    FolderModel targetFolder, sourceFolder;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        unauthorizedUser = dataUser.createRandomTestUser();
        testUser = dataUser.createRandomTestUser();
        contributorUser = dataUser.createRandomTestUser();
        collaboratorUser = dataUser.createRandomTestUser();
        consumerUser = dataUser.createRandomTestUser();
        inexistentUser = new UserModel("inexistent", "inexistent");
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();

        dataUser.addUserToSite(consumerUser, testSite, UserRole.SiteConsumer);
        dataUser.addUserToSite(collaboratorUser, testSite, UserRole.SiteCollaborator);
        dataUser.addUserToSite(contributorUser, testSite, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to copy file to an existent location in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerCopyFile() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
            .then().copyTo(targetFolder)
                .and().assertThat().existsInRepo().usingResource(sourceFile).assertThat().existsInRepo();
    }
   
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to copy folder to an existent location in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerCopyFolder() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        sourceFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(targetFolder).assertThat().existsInRepo()
                .createFolder(sourceFolder).assertThat().existsInRepo()
                .then().copyTo(targetFolder)
                    .and().assertThat().existsInRepo().usingResource(sourceFolder).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to copy file to a nonexistent location in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisObjectNotFoundException.class)
    public void siteManagerCopyFileToNonexistentTarget() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(sourceFile).assertThat().existsInRepo()
            .createFolder(targetFolder).and().delete()
            .then().copyTo(targetFolder);
    }
    
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to copy a nonexistent file in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisObjectNotFoundException.class)
    public void siteManagerCopyNonexistentSourceFile() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(targetFolder).assertThat().existsInRepo()
            .createFile(sourceFile).delete()
            .then().copyTo(targetFolder);
    }
    
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION, 
            description = "Verify non existing user is not able to copy file with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisUnauthorizedException.class)
    public void nonExistentUserIsNotAbleToCopyFile() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo();
        cmisApi.authenticateUser(inexistentUser)
                .then().copyTo(targetFolder);
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION, 
            description = "Verify non existing user is not able to copy folder with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = CmisUnauthorizedException.class)
    public void nonExistentUserIsNotAbleToCopyFolder() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        sourceFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(targetFolder).assertThat().existsInRepo()
                .createFolder(sourceFolder).assertThat().existsInRepo();
        cmisApi.authenticateUser(inexistentUser)
                .then().copyTo(targetFolder);
    }
    
//    @Bug(id="ACE-5606")
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that checked out document can be copied with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void checkedOutDocumentCanBeCopied() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
                .then().checkOut()
                    .and().copyTo(targetFolder).refreshResource()
                    .then().assertThat().existsInRepo()
                    .then().usingResource(sourceFile).assertThat().existsInRepo();
    }
    
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that whole folder structure can be copied with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void folderStructureCanBeCopied() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        FolderModel randomFolder = FolderModel.getRandomFolderModel();
        sourceFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFolder(sourceFolder).and().assertThat().existsInRepo()
                .usingResource(sourceFolder).createFolder(randomFolder)
                    .then().createFile(sourceFile).and().assertThat().existsInRepo()
                    .then().usingResource(sourceFolder)
                        .copyTo(targetFolder)
                        .and().assertThat().existsInRepo()
                              .assertThat().hasFiles(sourceFile);
    }
    
    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION, 
            description = "Verify that version history of a copied document is not kept with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void versionHistoryIsNotKeptWhenCopyingFile() throws Exception
    {
        targetFolder = FolderModel.getRandomFolderModel();
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "content");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(targetFolder).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
            .then().checkOut().refreshResource()
                .prepareDocumentForCheckIn()
                    .withContent("First update").checkIn().refreshResource()
                    .and().assertThat().documentHasVersion(1.1)
            .then().checkOut()
                .prepareDocumentForCheckIn()
                     .withContent("Second update")
                     .withMajorVersion()
                     .checkIn().refreshResource()
                     .and().assertThat().documentHasVersion(2.0)
            .then().copyTo(targetFolder)
                .and().assertThat().existsInRepo()
            .then().assertThat().documentHasVersion(1.0)
                .and().assertThat().contentIs("Second update");
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify copy PWC document object")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void managerCopyPWCDocumentObject() throws Exception
    {
        sourceFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FileModel pwcFile = cmisApi.authenticateUser(testUser).usingResource(sourceFile).checkOut().withCMISUtil().getPWCFileModel();
        cmisApi.usingResource(pwcFile).copyTo(targetFolder)
                .usingResource(targetFolder).assertThat().hasChildren(pwcFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor can copy Document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void contributorCanCopyDocument() throws Exception
    {
        sourceFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        cmisApi.authenticateUser(contributorUser).usingResource(sourceFile).copyTo(targetFolder)
                .usingResource(targetFolder).assertThat().hasChildren(sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor can copy Folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void contributorCanCopyFolder() throws Exception
    {
        sourceFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        targetFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        cmisApi.authenticateUser(contributorUser).usingResource(sourceFolder).copyTo(targetFolder)
                .usingResource(targetFolder).assertThat().hasChildren(sourceFolder);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator can copy Document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void collaboratorCanCopyDocument() throws Exception
    {
        sourceFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        cmisApi.authenticateUser(collaboratorUser).usingResource(sourceFile).copyTo(targetFolder)
                .usingResource(targetFolder).assertThat().hasChildren(sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator can copy Folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void collaboratorCanCopyFolder() throws Exception
    {
        sourceFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        targetFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        cmisApi.authenticateUser(collaboratorUser).usingResource(sourceFolder).copyTo(targetFolder)
                .usingResource(targetFolder).assertThat().hasChildren(sourceFolder);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify consumer cannot copy Document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void consumerCannotCopyDocument() throws Exception
    {
        sourceFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        cmisApi.authenticateUser(consumerUser).usingResource(sourceFile).copyTo(targetFolder)
                .usingResource(targetFolder).assertThat().hasChildren(sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify consumer cannot copy Folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void consumerCannotCopyFolder() throws Exception
    {
        sourceFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        targetFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        cmisApi.authenticateUser(consumerUser).usingResource(sourceFolder).copyTo(targetFolder)
                .usingResource(targetFolder).assertThat().hasChildren(sourceFolder);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot copy Document from private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotCopyDocumentFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        sourceFile = dataContent.usingUser(testUser).usingSite(privateSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(testUser).usingSite(privateSite).createFolder();
        cmisApi.authenticateUser(unauthorizedUser).usingResource(sourceFile).copyTo(targetFolder);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot copy Folder from private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotCopyFolderFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        sourceFolder = dataContent.usingUser(testUser).usingSite(privateSite).createFolder();
        targetFolder = dataContent.usingUser(testUser).usingSite(privateSite).createFolder();
        cmisApi.authenticateUser(unauthorizedUser).usingResource(sourceFolder).copyTo(targetFolder);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot copy Document from moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotCopyDocumentFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(testUser).createModeratedRandomSite();
        sourceFile = dataContent.usingUser(testUser).usingSite(moderatedSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        targetFolder = dataContent.usingUser(testUser).usingSite(moderatedSite).createFolder();
        cmisApi.authenticateUser(unauthorizedUser).usingResource(sourceFile).copyTo(targetFolder);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify unauthorized user cannot copy Folder from moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisUnauthorizedException.class, CmisPermissionDeniedException.class})
    public void unauthorizedUserCannotCopyFolderFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(testUser).createModeratedRandomSite();
        sourceFolder = dataContent.usingUser(testUser).usingSite(moderatedSite).createFolder();
        targetFolder = dataContent.usingUser(testUser).usingSite(moderatedSite).createFolder();
        cmisApi.authenticateUser(unauthorizedUser).usingResource(sourceFolder).copyTo(targetFolder);
    }
}
