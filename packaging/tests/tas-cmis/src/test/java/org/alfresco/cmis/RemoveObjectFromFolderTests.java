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
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 10/3/2016.
 */
public class RemoveObjectFromFolderTests extends CmisTest
{
    UserModel unauthorizedUser;
    UserModel siteManager;
    UserModel contributorUser;
    UserModel collaboratorUser;
    UserModel consumerUser;
    SiteModel publicSite;
    FolderModel parentFolder, testFolder;
    FileModel testFile;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        unauthorizedUser = dataUser.createRandomTestUser();
        siteManager = dataUser.createRandomTestUser();
        contributorUser = dataUser.createRandomTestUser();
        collaboratorUser = dataUser.createRandomTestUser();
        consumerUser = dataUser.createRandomTestUser();
        publicSite = dataSite.usingUser(siteManager).createPublicRandomSite();
        dataUser.addUserToSite(consumerUser, publicSite, UserRole.SiteConsumer);
        dataUser.addUserToSite(collaboratorUser, publicSite, UserRole.SiteCollaborator);
        dataUser.addUserToSite(contributorUser, publicSite, UserRole.SiteContributor);
        parentFolder = FolderModel.getRandomFolderModel();
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFolder(testFolder).assertThat().existsInRepo()
                .createFolder(parentFolder).assertThat().existsInRepo();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to remove a document from folder in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldRemoveFileToFolder() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingResource(parentFolder)
                .createFile(testFile).assertThat().existsInRepo()
                .and().addDocumentToFolder(testFolder, true).then().assertThat().existsInRepo()
                .when().removeDocumentFromFolder(testFolder).assertThat().doesNotExistInRepo()
                .usingResource(testFile).assertThat().existsInRepo();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to leave a document without parent folder with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisInvalidArgumentException.class, expectedExceptionsMessageRegExp = "^Object is not a document!$")
    public void siteManagerShouldNotRemoveFolderFromFolder() throws Exception
    {
        FolderModel subFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager).usingResource(parentFolder)
                .createFolder(subFolder).assertThat().existsInRepo()
                .when().removeDocumentFromFolder(testFolder).assertThat().doesNotExistInRepo();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to remove a document without parent folder with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerShouldNotRemoveInvalidFileFromFolder() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        testFile.setCmisLocation("/" + testFile.getName() + "/");
        cmisApi.authenticateUser(siteManager).usingResource(testFile)
                .when().removeDocumentFromFolder(testFolder).assertThat().doesNotExistInRepo()
                .usingResource(testFile).assertThat().existsInRepo();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to remove valid document from invalid folder with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerShouldNotRemoveFileToInvalidFolder() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FolderModel invalidFolder = FolderModel.getRandomFolderModel();
        invalidFolder.setCmisLocation("/" + invalidFolder.getName() + "/");
        cmisApi.authenticateUser(siteManager).usingResource(parentFolder)
                .createFile(testFile).assertThat().existsInRepo()
                .and().addDocumentToFolder(testFolder, true).then().assertThat().existsInRepo()
                .when().removeDocumentFromFolder(invalidFolder)
                .assertThat().doesNotExistInRepo()
                .usingResource(testFile).assertThat().existsInRepo();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to remove a checked out document from folder in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerShouldRemovePWCFileToFolder() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingResource(parentFolder)
                .createFile(testFile).assertThat().existsInRepo()
                    .then().checkOut().and().assertThat().documentIsCheckedOut()
                .usingPWCDocument().addDocumentToFolder(testFolder, true)
                    .then().assertThat().existsInRepo()
                    .and().assertThat().documentIsCheckedOut()
                .when().removeDocumentFromFolder(testFolder).assertThat().doesNotExistInRepo()
                .usingResource(testFile).assertThat().existsInRepo()
                .and().assertThat().documentIsCheckedOut();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify unauthorized user is not able to remove a document from folder")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void unauthorizedUserShouldNotRemoveFileToFolder() throws Exception
    {
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager)
                .usingResource(parentFolder)
                .createFile(testFile).assertThat().existsInRepo()
                .then().authenticateUser(unauthorizedUser)
                .removeDocumentFromFolder(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor can remove document from folder created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void contributorCanRemoveDocumentFromFolderCreatedBySelf() throws Exception
    {
        parentFolder = dataContent.usingUser(contributorUser).usingSite(publicSite).createFolder();
        testFolder = dataContent.usingUser(contributorUser).usingSite(publicSite).createFolder();
        testFile = dataContent.usingUser(contributorUser).usingResource(parentFolder)
                .createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));

        cmisApi.authenticateUser(contributorUser).usingResource(testFile)
                .addDocumentToFolder(testFolder, true).then().assertThat().existsInRepo()
                .removeDocumentFromFolder(testFolder).assertThat().doesNotExistInRepo()
                .usingResource(testFile).assertThat().existsInRepo();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify contributor can remove document from folder created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void contributorCannotRemoveDocumentFromFolderCreatedByManager() throws Exception
    {
        parentFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        testFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        testFile = dataContent.usingUser(siteManager).usingResource(parentFolder)
                .createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        cmisApi.authenticateUser(siteManager).usingResource(testFile)
                .addDocumentToFolder(testFolder, true);

        cmisApi.authenticateUser(contributorUser).usingResource(testFile).removeDocumentFromFolder(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator can remove document from folder created by self")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void collaboratorCanRemoveDocumentFromFolderCreatedBySelf() throws Exception
    {
        parentFolder = dataContent.usingUser(collaboratorUser).usingSite(publicSite).createFolder();
        testFolder = dataContent.usingUser(collaboratorUser).usingSite(publicSite).createFolder();
        testFile = dataContent.usingUser(collaboratorUser).usingResource(parentFolder)
                .createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));

        cmisApi.authenticateUser(collaboratorUser).usingResource(testFile)
                .addDocumentToFolder(testFolder, true).then().assertThat().existsInRepo()
                .removeDocumentFromFolder(testFolder).assertThat().doesNotExistInRepo()
                .usingResource(testFile).assertThat().existsInRepo();
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify collaborator can remove document from folder created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void collaboratorCannotRemoveDocumentFromFolderCreatedByManager() throws Exception
    {
        parentFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        testFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        testFile = dataContent.usingUser(siteManager).usingResource(parentFolder)
                .createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        cmisApi.authenticateUser(siteManager).usingResource(testFile)
                .addDocumentToFolder(testFolder, true);

        cmisApi.authenticateUser(collaboratorUser).usingResource(testFile).removeDocumentFromFolder(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify consumer cannot remove document from folder")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerCannotRemoveDocumentFromFolder() throws Exception
    {
        parentFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        testFolder = dataContent.usingUser(siteManager).usingSite(publicSite).createFolder();
        testFile = dataContent.usingUser(siteManager).usingResource(parentFolder)
                .createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        cmisApi.authenticateUser(siteManager).usingResource(testFile)
                .addDocumentToFolder(testFolder, true).then().assertThat().existsInRepo();

        cmisApi.authenticateUser(consumerUser).usingResource(testFile).removeDocumentFromFolder(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify remove document from folder by user that is outside a private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void unauthorizedUserCannotRemoveDocumentFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(siteManager).createPrivateRandomSite();
        parentFolder = dataContent.usingUser(siteManager).usingSite(privateSite).createFolder();
        testFolder = dataContent.usingUser(siteManager).usingSite(privateSite).createFolder();
        testFile = dataContent.usingUser(siteManager).usingResource(parentFolder)
                .createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        cmisApi.authenticateUser(siteManager).usingResource(testFile)
                .addDocumentToFolder(testFolder, true).then().assertThat().existsInRepo();

        cmisApi.authenticateUser(unauthorizedUser).usingResource(testFile).removeDocumentFromFolder(testFolder);
    }

    @TestRail(section = { "cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify remove document from folder by user that is outside a moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void unauthorizedUserCannotRemoveDocumentFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(siteManager).createModeratedRandomSite();
        parentFolder = dataContent.usingUser(siteManager).usingSite(moderatedSite).createFolder();
        testFolder = dataContent.usingUser(siteManager).usingSite(moderatedSite).createFolder();
        testFile = dataContent.usingUser(siteManager).usingResource(parentFolder)
                .createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        cmisApi.authenticateUser(siteManager).usingResource(testFile)
                .addDocumentToFolder(testFolder, true).then().assertThat().existsInRepo();

        cmisApi.authenticateUser(unauthorizedUser).usingResource(testFile).removeDocumentFromFolder(testFolder);
    }
}
