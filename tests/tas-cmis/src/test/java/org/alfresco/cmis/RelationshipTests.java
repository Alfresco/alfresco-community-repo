package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 9/29/2016.
 */
public class RelationshipTests extends CmisTest
{
    UserModel siteManager;
    SiteModel publicSite, privateSite;
    FileModel sourceFile, targetFile;
    private DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        siteManager = dataUser.createRandomTestUser();
        publicSite = dataSite.usingUser(siteManager).createPublicRandomSite();
        privateSite = dataSite.usingUser(siteManager).createPrivateRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(publicSite, UserRole.SiteManager, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to create relationship between a source object and a target object in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerCreatesRelationshipBetween2Files() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(targetFile).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
                .then().createRelationshipWith(targetFile)
                    .and().assertThat().objectHasRelationshipWith(targetFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create relationship between a invalid sources with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerCannotCreateRelWithInvalidSources() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(targetFile).and().delete().assertThat().doesNotExistInRepo()
            .createFile(sourceFile).and().delete().assertThat().doesNotExistInRepo()
                .then().createRelationshipWith(targetFile);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create relationship with checkout source document with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanCreateRelWithCheckedOutSourceDocument() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(targetFile).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
                .then().checkOut().and().assertThat().documentIsCheckedOut()
                    .then().createRelationshipWith(targetFile)
                        .and().assertThat().objectHasRelationshipWith(targetFile);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that inexistent user is not able to create relationship between documents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUnauthorizedException.class)
    public void inexistentUserCannotCreateRelationship() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(targetFile).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
                .then().authenticateUser(UserModel.getRandomUserModel())
                    .createRelationshipWith(targetFile)
                        .and().assertThat().objectHasRelationshipWith(targetFile);
    }

    @Bug(id="REPO-4301")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that user that was deleted is not able to create relationship between documents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisUnauthorizedException.class)
    public void deletedUserCannotCreateRelationship() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        UserModel toBeDeleted = dataUser.createRandomTestUser();
        FolderModel shared = FolderModel.getSharedFolderModel();
        cmisApi.authenticateUser(toBeDeleted)
            .usingResource(shared)
                .createFile(targetFile).and().assertThat().existsInRepo()
                .createFile(sourceFile).and().assertThat().existsInRepo();
        dataUser.deleteUser(toBeDeleted);
        cmisApi.createRelationshipWith(targetFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify that site manager is able to create relationship between documents with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanCreateRelationshipInPrivateSite() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(dataUser.getAdminUser())
            .usingSite(privateSite)
                .createFile(targetFile).and().assertThat().existsInRepo()
                .createFile(sourceFile).and().assertThat().existsInRepo()
                .then().authenticateUser(siteManager)
                    .createRelationshipWith(targetFile)
                        .and().assertThat().objectHasRelationshipWith(targetFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create relationship between a invalid sources with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerCannotGetRelWithInvalidObject() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel invalidFile = FileModel.getRandomFileModel(FileType.HTML);
        invalidFile.setCmisLocation("/" + invalidFile.getName() + "/");
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(targetFile)
            .createFile(sourceFile)
            .then().createRelationshipWith(targetFile)
            .and().assertThat().objectHasRelationshipWith(targetFile)
            .assertThat().objectHasRelationshipWith(invalidFile);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify admin is able to create relationship between a source folder and a target file with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void adminCreatesRelBetweenSourceFolderAndTargetFile() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FolderModel sourceFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(publicSite)
            .createFile(targetFile).assertThat().existsInRepo()
            .createFolder(sourceFolder).assertThat().existsInRepo()
            .then().createRelationshipWith(targetFile)
            .and().assertThat().objectHasRelationshipWith(targetFile);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify contributor is not able to create relationship between a source object and a target object in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS},expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void contributorCannotCreateRelationship() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(targetFile).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingResource(sourceFile).createRelationshipWith(targetFile)
            .and().assertThat().objectHasRelationshipWith(targetFile);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify collaborator is able to create relationship between a source object and a target object in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanCreateRelationship() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(targetFile).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(sourceFile).createRelationshipWith(targetFile)
            .and().assertThat().objectHasRelationshipWith(targetFile);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify consumer is not able to create relationship between a source object and a target object in DocumentLibrary with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS},expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerCannotCreateRelationship() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(targetFile).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .usingResource(sourceFile).createRelationshipWith(targetFile)
            .and().assertThat().objectHasRelationshipWith(targetFile);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify unauthorized user is not able to create relationship between a source object and a target object from a private site with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void unauthorizedUserCannotCreateRelationship() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(privateSite)
            .createFile(targetFile).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
            .usingResource(sourceFile).createRelationshipWith(targetFile);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify unauthorized user is not able to get relationship between a source object and a target object from a private site with CMIS")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void unauthorizedUserCannotGetRelationship() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(privateSite)
            .createFile(targetFile).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo()
            .usingResource(sourceFile).createRelationshipWith(targetFile)
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .and().assertThat().objectHasRelationshipWith(targetFile);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to create relationship for PWC source document with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisInvalidArgumentException.class,
        expectedExceptionsMessageRegExp = "^Source is not the latest version of a document, a folder or an item object!$")
    public void siteManagerCannotCreateRelationshipPWCSourceFile() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(targetFile).and().assertThat().existsInRepo()
            .createFile(sourceFile).and().assertThat().existsInRepo().then().checkOut()
            .assertThat().documentIsCheckedOut()
            .usingPWCDocument().createRelationshipWith(targetFile);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to create relationship with checkout target document with CMIS")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanCreateRelWithCheckedOutTargetDocument() throws Exception
    {
        sourceFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        targetFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(siteManager).usingSite(publicSite)
            .createFile(targetFile).and().assertThat().existsInRepo()
            .then().checkOut().and().assertThat().documentIsCheckedOut()
            .createFile(sourceFile).and().assertThat().existsInRepo()
            .then().createRelationshipWith(targetFile)
            .and().assertThat().objectHasRelationshipWith(targetFile);
    }
}
