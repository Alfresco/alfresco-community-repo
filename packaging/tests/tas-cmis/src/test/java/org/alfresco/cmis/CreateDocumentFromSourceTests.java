package org.alfresco.cmis;

import org.alfresco.cmis.exception.InvalidCmisObjectException;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/26/2016.
 */
public class CreateDocumentFromSourceTests extends CmisTest
{
    SiteModel publicSite, privateSite;
    UserModel siteManager;
    FileModel sourceFile, newFile;
    String sourceContent = "source content";
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        siteManager = dataUser.createRandomTestUser();
        publicSite = dataSite.usingUser(siteManager).createPublicRandomSite();
        privateSite = dataSite.usingUser(siteManager).createPrivateRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteContributor,
                UserRole.SiteCollaborator, UserRole.SiteConsumer);
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, sourceContent);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFile(sourceFile).and().assertThat().existsInRepo();
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify site manager is able to create file from source in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS })
    public void siteManagerShouldCreateDocumentFromSource() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFileFromSource(newFile, sourceFile).and()
                .refreshResource().then().assertThat().existsInRepo().and().assertThat().contentIs(sourceContent);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify inexistent user isn't able to create file from source in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = CmisUnauthorizedException.class)
    public void inexistentUserShouldNotCreateDocumentFromSource() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        UserModel inexistentUser = new UserModel("inexistent", "inexistent");
        cmisApi.authenticateUser(inexistentUser).usingSite(publicSite).createFileFromSource(newFile, sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.SANITY,
            description = "Verify unauthorized user isn't able to create file from source in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS }, expectedExceptions = { CmisPermissionDeniedException.class,
            CmisUnauthorizedException.class })
    public void unauthorizedUserShouldNotCreateDocumentFromSource() throws Exception
    {
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(unauthorizedUser).usingSite(publicSite).createFileFromSource(newFile, sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager user is not able to create document from source twice in the same location with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = CmisContentAlreadyExistsException.class)
    public void siteManagerCannotCreateDocumentFromSourceTwice() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFileFromSource(newFile, sourceFile).and().assertThat()
                .existsInRepo().then().usingSite(publicSite).createFileFromSource(newFile, sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin user is not able to create document from source with invalid source with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = CmisObjectNotFoundException.class)
    public void adminCannotCreateDocFromInvalidSource() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(publicSite).createFile(sourceFile).and().assertThat()
                .existsInRepo().then().delete().when().usingResource(sourceFile).createFileFromSource(newFile, sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager user is not able to create document from source with invalid characters with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = CmisConstraintException.class)
    public void siteManagerCannotCreateDocFromSourceWithInvalidChars() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel invalidCharDoc = new FileModel("/.:?|\\`\\.txt", FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFileFromSource(invalidCharDoc, sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin user is not able to create document from source at invalid location with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = CmisObjectNotFoundException.class)
    public void adminCannotCreateDocumentAtInvalidPath() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FolderModel invalidLocation = new FolderModel("/Shared/invalidFolder");
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingResource(invalidLocation).createFileFromSource(newFile,
                sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create file from folder source CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = InvalidCmisObjectException.class)
    public void siteManagerShouldNotCreateDocFromFolderSource() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FolderModel folderSource = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFolder(folderSource).then()
                .createFileFromSource(newFile, folderSource);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager user is not able to create document from source with invalid base type id with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = CmisContentAlreadyExistsException.class)
    public void siteManagerCannotCreateDocFromSourceWithInvalidBaseTypeId() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFileFromSource(newFile, sourceFile).and().assertThat()
                .existsInRepo().then().usingSite(publicSite).createFileFromSource(newFile, sourceFile, "cmis:fakeType");
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager user is not able to create an unnamed document from source with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteManagerCannotCreateUnnamedDocFromSource() throws Exception
    {
        newFile = new FileModel("");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFileFromSource(newFile, sourceFile).then()
                .usingSite(publicSite).assertThat().doesNotHaveFile(newFile);
        Utility.sleep(100, 10000, () ->
        {
            cmisApi.usingSite(publicSite).assertThat().doesNotHaveFile(newFile);
        });
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to create file from source added by another user with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void anotherSiteManagerShouldCreateDocumentFromSource() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).usingSite(publicSite)
                .createFileFromSource(newFile, sourceFile);
        Utility.sleep(100, 10000, () ->
        {
            cmisApi.refreshResource().then().assertThat().existsInRepo().and().assertThat().contentIs(sourceContent);
        });

    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site contributor is able to create file from source added by another user with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void contributorShouldCreateDocumentFromSource() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(publicSite)
                .createFileFromSource(newFile, sourceFile);
        Utility.sleep(100, 10000, () ->
        {
            cmisApi.refreshResource().then().assertThat().existsInRepo().and().assertThat().contentIs(sourceContent);
        });
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to create file from source added by another user with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void collaboratorShouldCreateDocumentFromSource() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(publicSite)
                .createFileFromSource(newFile, sourceFile);
        Utility.sleep(100, 10000, () ->
        {
            cmisApi.refreshResource().then().assertThat().existsInRepo().and()
                                            .assertThat().contentIs(sourceContent);
        });
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site consumer is not able to create file from source with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = { CmisPermissionDeniedException.class,
            CmisUnauthorizedException.class })
    public void consumerShouldNotCreateDocumentFromSource() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingSite(publicSite)
                .createFileFromSource(newFile, sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to create file from source with versioning state set to Minor with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void managerCreatesDocumentFromSourceWithVersionMinor() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFileFromSource(newFile, sourceFile, VersioningState.MINOR);
        Utility.sleep(100, 10000, () ->
        {
            cmisApi.refreshResource().then().assertThat().existsInRepo().and()
                                            .assertThat().contentIs(sourceContent).and()
                                            .assertThat().documentHasVersion(0.1).and()
                                            .assertThat().isNotLatestMajorVersion();
        });
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to create file from source with versioning state set to None with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void managerShouldCreateDocumentFromSourceWithVersionNone() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFileFromSource(newFile, sourceFile, VersioningState.NONE);
        Utility.sleep(100, 35000, () ->
        {
            cmisApi.refreshResource().then().assertThat().existsInRepo().and()
                                            .assertThat().contentIs(sourceContent).and()
                                            .assertThat().documentHasVersion(1.0);
        });
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager is able to create file from source with versioning state set to CHECKEDOUT with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void managerCreatesDocumentFromSourceWithVersionCHECKEDOUT() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
                .createFileFromSource(newFile, sourceFile, VersioningState.CHECKEDOUT);
        Utility.sleep(100, 35000, () ->
        {
            cmisApi.refreshResource().then().assertThat().existsInRepo().and()
                                            .assertThat().contentIs(sourceContent).and()
                                            .assertThat().documentIsCheckedOut();
        });
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that admin user is not able to create document from null source with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = NullPointerException.class)
    public void adminCannotCreateDocFromNullSource() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel sourceFile = null;
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(publicSite).createFileFromSource(newFile, sourceFile);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager is able to create document from a source that is checked out with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void managerShouldCreateDocFromSourceThatIsCheckedOut() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, sourceContent);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite).createFile(sourceFile).and().assertThat().existsInRepo()
                .then().checkOut().and().assertThat().documentIsCheckedOut().when().usingSite(publicSite)
                .createFileFromSource(newFile, sourceFile).then().assertThat().existsInRepo().and().assertThat()
                .contentIs(sourceContent).and().assertThat().documentIsNotCheckedOut();
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that manager is able to create document from a source from his private site with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void managerShouldCreateDocFromSourceFromHisPrivateSite() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, sourceContent);
        cmisApi.authenticateUser(siteManager).usingSite(privateSite).createFile(sourceFile).and().assertThat().existsInRepo()
                .when().usingSite(publicSite).createFileFromSource(newFile, sourceFile).then().assertThat().existsInRepo().and()
                .assertThat().contentIs(sourceContent);
    }

    @TestRail(section = {"cmis-api" }, executionType = ExecutionType.REGRESSION,
            description = "Verify that an user is not able to create document from a source from a private site with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions = { CmisPermissionDeniedException.class,
            CmisUnauthorizedException.class })
    public void userShouldNotCreateDocFromSourceFromPrivateSite() throws Exception
    {
        newFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, sourceContent);
        cmisApi.authenticateUser(siteManager).usingSite(privateSite).createFile(sourceFile).and().assertThat().existsInRepo();
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).usingSite(publicSite)
                .createFileFromSource(newFile, sourceFile);
    }

}
