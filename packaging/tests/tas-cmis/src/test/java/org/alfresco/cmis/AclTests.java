package org.alfresco.cmis;

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
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumBasicPermissions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AclTests extends CmisTest
{
    UserModel testUser, inviteUser, unauthorizedUser;
    SiteModel testSite, privateSite;
    FileModel testFile;
    FolderModel testFolder;
    private DataUser.ListUserWithRoles usersWithRoles;
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        inviteUser = dataUser.createRandomTestUser();
        unauthorizedUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        usersWithRoles = dataUser.usingUser(testUser)
                .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Site manager can get the acls for valid document")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS })
    public void siteManagerShouldGetDocumentAcls() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                .and().assertThat().hasAcls();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Site manager can get the acls for valid folder")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerShouldGetFolderAcls() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .and().assertThat().hasAcls();
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Add Acl for valid document with AclPropagation set to REPOSITORYDETERMINED")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanAddAclWithRepositoryDeterminedPropagation() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo();
        cmisApi.usingResource(testFile).addAcl(inviteUser, UserRole.SiteContributor, AclPropagation.REPOSITORYDETERMINED)
            .then().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Add Acl for valid folder with AclPropagation set to PROPAGATE")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanAddAclWithPropagate() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo();
        cmisApi.usingResource(testFolder).addAcl(inviteUser, UserRole.SiteConsumer, AclPropagation.PROPAGATE)
            .then().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Add Acl for valid folder with AclPropagation set to OBJECTONLY")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanAddAclWithObjectOnlyPropagation() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo();
        cmisApi.usingResource(testFolder).addAcl(inviteUser, UserRole.SiteCollaborator, AclPropagation.OBJECTONLY)
            .then().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteCollaborator);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl for valid folder with AclPropagation set to OBJECTONLY")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanApplyAclWithObjectOnlyPropagation() throws Exception
    {
        testFolder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo();
        cmisApi.usingResource(testFolder).addAcl(inviteUser, UserRole.SiteCollaborator, AclPropagation.OBJECTONLY)
                .then().applyAcl(inviteUser, UserRole.SiteConsumer, UserRole.SiteCollaborator, AclPropagation.OBJECTONLY)
                .and().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteConsumer)
                    .and().assertThat().permissionIsNotSetForUser(inviteUser, UserRole.SiteCollaborator);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify appply acl with invalid role that will be removed")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class)
    public void applyAclWithInvalidAddedRole() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo()
                .then().addAcl(inviteUser, UserRole.SiteCollaborator, AclPropagation.OBJECTONLY)
                    .then().applyAcl(inviteUser, UserRole.SiteConsumer, UserRole.SiteManager, AclPropagation.OBJECTONLY);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Apply Acl for valid folder with AclPropagation set to PROPAGATE")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerCanApplyAclWithPropagate() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile)
                .and().addAcl(inviteUser, UserRole.SiteConsumer, AclPropagation.PROPAGATE)
                .when().applyAcl(inviteUser, UserRole.SiteManager, UserRole.SiteConsumer, AclPropagation.PROPAGATE)
                    .assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteManager)
                        .and().assertThat().permissionIsNotSetForUser(inviteUser, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl for valid folder with AclPropagation set to REPOSITORYDETERMINED")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanApplyAclWithRepositoryDeterminedPropagation() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile)
                .and().addAcl(inviteUser, UserRole.SiteConsumer, AclPropagation.OBJECTONLY)
                .when().applyAcl(inviteUser, UserRole.SiteManager, UserRole.SiteConsumer, AclPropagation.REPOSITORYDETERMINED)
                    .assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteManager)
                        .and().assertThat().permissionIsNotSetForUser(inviteUser, UserRole.SiteConsumer);
    }

    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Remove Acl for valid folder with AclPropagation set to REPOSITORYDETERMINED")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanRemoveAclWithRepositoryDeterminedPropagation() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile)
                .and().addAcl(inviteUser, UserRole.SiteConsumer, AclPropagation.OBJECTONLY)
                .when().removeAcl(inviteUser, UserRole.SiteConsumer, AclPropagation.REPOSITORYDETERMINED)
                    .and().assertThat().permissionIsNotSetForUser(inviteUser, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Remove Acl for valid folder with AclPropagation set to REPOSITORYDETERMINED")
    @Test(groups = {TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerCanRemoveAclWithPropagate() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile)
                .and().addAcl(inviteUser, UserRole.SiteManager, AclPropagation.OBJECTONLY)
                .when().removeAcl(inviteUser, UserRole.SiteManager, AclPropagation.PROPAGATE)
                    .and().assertThat().permissionIsNotSetForUser(inviteUser, UserRole.SiteManager);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Remove Acl for valid folder with AclPropagation set to REPOSITORYDETERMINED")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanRemoveAclWithObjectOnlyPropagation() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile)
                .and().addAcl(inviteUser, UserRole.SiteContributor, AclPropagation.OBJECTONLY)
                .when().removeAcl(inviteUser, UserRole.SiteContributor, AclPropagation.OBJECTONLY)
                    .and().assertThat().permissionIsNotSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Remove Acl for valid folder with AclPropagation set to REPOSITORYDETERMINED")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class)
    public void siteManagerCannotRemoveInvalidAcl() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile)
                .and().addAcl(inviteUser, UserRole.SiteContributor, AclPropagation.OBJECTONLY)
                .when().removeAcl(inviteUser, UserRole.SiteManager, AclPropagation.OBJECTONLY);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Site manager can add acl with null AclPropagation")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanAddAclWithNullPropagation() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF, "content");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile)
            .and().addAcl(inviteUser, UserRole.SiteContributor, null)
            .then().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Site manager cannot get acl for pwc document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisRuntimeException.class)
    public void siteManagerCannotGetAclForPwcDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF, "content");
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile, VersioningState.CHECKEDOUT)
            .usingPWCDocument().addAcl(inviteUser, UserRole.SiteContributor, AclPropagation.PROPAGATE)
                .then().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Site manager cannot get acl for invalid object")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class)
    public void siteManagerCannotGetAclForInvalidObject() throws Exception
    {
        FolderModel folder = FolderModel.getRandomFolderModel();
        folder.setCmisLocation("/" + folder.getName() + "/");
        cmisApi.authenticateUser(testUser)
            .usingResource(folder)
                .assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl for valid document with null AclPropagation")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanApplyAclWithNullPropagation() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile)
                .and().addAcl(inviteUser, UserRole.SiteConsumer, AclPropagation.OBJECTONLY)
                .when().applyAcl(inviteUser, UserRole.SiteManager, UserRole.SiteConsumer, null)
                    .assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteManager)
                        .and().assertThat().permissionIsNotSetForUser(inviteUser, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl for checked out document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisRuntimeException.class)
    public void siteManagerCannotGetAclForAppliedAclForPWC() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile)
            .and().addAcl(inviteUser, UserRole.SiteConsumer, AclPropagation.OBJECTONLY)
            .then().checkOut().assertThat().documentIsCheckedOut()
            .usingPWCDocument().applyAcl(inviteUser, UserRole.SiteManager, UserRole.SiteConsumer)
                .assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteManager);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Remove Acl from valid folder with null AclPropagation")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanRemoveAclWithNullPropagation() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.PDF);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile)
                .and().addAcl(inviteUser, UserRole.SiteContributor, AclPropagation.OBJECTONLY)
                .when().removeAcl(inviteUser, UserRole.SiteContributor)
                    .and().assertThat().permissionIsNotSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Unauthorized user cannot remove acl")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void removeAclByUnauthorizedUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile)
            .and().addAcl(inviteUser, UserRole.SiteConsumer, AclPropagation.OBJECTONLY)
                .assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteConsumer)
            .when().authenticateUser(unauthorizedUser)
                .removeAcl(inviteUser, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Unauthorized user cannot add acl")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void addAclByUnauthorizedUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile)
            .then().authenticateUser(unauthorizedUser)
                .and().addAcl(inviteUser, UserRole.SiteConsumer, AclPropagation.OBJECTONLY);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Unauthorized user cannot apply acl")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void applyAclByUnauthorizedUser() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSEXCEL);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile)
            .and().addAcl(inviteUser, UserRole.SiteConsumer)
            .when().authenticateUser(unauthorizedUser)
                .and().applyAcl(inviteUser, UserRole.SiteManager, UserRole.SiteConsumer);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Add Acl for checked out document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanAddAclForCheckedOutDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile, VersioningState.CHECKEDOUT).and().assertThat().existsInRepo()
                    .and().assertThat().documentIsCheckedOut()
                    .then().usingResource(testFile)
                        .addAcl(inviteUser, UserRole.SiteContributor)
                            .and().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Add Acl by user with collaborator role for document created by himself")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanAddAclForDocumentCreatedByHimself() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
                    .and().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Add Acl by user with collaborator role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void collaboratorCannotAddAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .addAcl(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Get Acl by user with collaborator role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanGetAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                .usingResource(testFile).refreshResource()
                    .and().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl by user with collaborator role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void collaboratorCannotApplyAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                        .applyAcl(inviteUser, UserRole.SiteCollaborator, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl by user with collaborator role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void collaboratorCannotRemoveAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
                        .removeAcl(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Add Acl by user with contributor role for document created by himself")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanAddAclForDocumentCreatedByHimself() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
                    .and().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Add Acl by user with contributor role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void contributorCannotAddAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .addAcl(inviteUser, UserRole.SiteCollaborator);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Get Acl by user with collaborator role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanGetAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                .usingResource(testFile).refreshResource()
                    .and().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl by user with contributor role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void contributorCannotApplyAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                        .applyAcl(inviteUser, UserRole.SiteCollaborator, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl by user with contributor role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void contributorCannotRemoveAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
                        .removeAcl(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Add Acl by user with consumer role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerCannotAddAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .addAcl(inviteUser, UserRole.SiteCollaborator);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Get Acl by user with consumer role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerCanGetAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .usingResource(testFile).refreshResource()
                    .and().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl by user with consumer role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerCannotApplyAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                        .applyAcl(inviteUser, UserRole.SiteCollaborator, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl by user with consumer role for document created by manager")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void consumerCannotRemoveAclForDocumentCreatedByManager() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                        .removeAcl(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Add Acl by non invited user in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotAddAclInPrivateSite() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(privateSite)
            .createFile(testFile).and().assertThat().existsInRepo()
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .addAcl(inviteUser, UserRole.SiteCollaborator);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Get Acl by non invited user in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotGetAclInPrivateSite() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(privateSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
            .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                .usingResource(testFile).refreshResource()
                    .and().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl by non invited user in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotApplyAclInPrivateSite() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(privateSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
                .applyAcl(inviteUser, UserRole.SiteCollaborator, UserRole.SiteContributor)
                    .and().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteCollaborator)
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                        .applyAcl(inviteUser, UserRole.SiteContributor, UserRole.SiteCollaborator);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Remove Acl by non invited user in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotRemoveAclFromPrivateSite() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(privateSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .addAcl(inviteUser, UserRole.SiteContributor)
                .and().assertThat().permissionIsSetForUser(inviteUser, UserRole.SiteContributor)
                    .then().authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
                        .removeAcl(inviteUser, UserRole.SiteContributor);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Add Acl for valid document with PermissionMapping")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanAddAclWithPermissionMapping() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
            .then().addAcl(inviteUser, PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT)
                .assertThat().permissionIsSetForUser(inviteUser, EnumBasicPermissions.CMIS_WRITE.value())
                    .then().addAcl(inviteUser, PermissionMapping.CAN_DELETE_OBJECT)
                        .and().assertThat().permissionIsSetForUser(inviteUser, EnumBasicPermissions.CMIS_ALL.value());
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply Acl for valid document with PermissionMapping")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanApplyAclWithPermissionMapping() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
            .then().addAcl(inviteUser, PermissionMapping.CAN_DELETE_OBJECT)
                .and().assertThat().permissionIsSetForUser(inviteUser, EnumBasicPermissions.CMIS_ALL.value())
                    .then().applyAcl(inviteUser, PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT, PermissionMapping.CAN_DELETE_OBJECT)
                        .and().assertThat().permissionIsSetForUser(inviteUser, EnumBasicPermissions.CMIS_WRITE.value())
                        .and().assertThat().permissionIsNotSetForUser(inviteUser, EnumBasicPermissions.CMIS_ALL.value());
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Remove Acl for valid document with PermissionMapping")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanRemoveAclWithPermissionMapping() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
                .createFile(testFile).and().assertThat().existsInRepo()
       .then().addAcl(inviteUser, PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT)
           .assertThat().permissionIsSetForUser(inviteUser, EnumBasicPermissions.CMIS_WRITE.value())
               .then().addAcl(inviteUser, PermissionMapping.CAN_DELETE_OBJECT)
                   .and().assertThat().permissionIsSetForUser(inviteUser, EnumBasicPermissions.CMIS_ALL.value())
                   .then().removeAcl(inviteUser, PermissionMapping.CAN_DELETE_OBJECT)
                       .and().assertThat().permissionIsNotSetForUser(inviteUser, EnumBasicPermissions.CMIS_ALL.value());
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Remove invalid Acl(that was not set) for valid document with PermissionMapping")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class,
            expectedExceptionsMessageRegExp="No matching ACE found to remove!*")
    public void siteManagerCannotRemoveInvalidAclWithPermissionMapping() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
                .then().addAcl(inviteUser, PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT)
                    .assertThat().permissionIsSetForUser(inviteUser, EnumBasicPermissions.CMIS_WRITE.value())
                        .then().removeAcl(inviteUser, PermissionMapping.CAN_CHECKIN_DOCUMENT);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Apply invalid Acl(that was not set) for valid document with PermissionMapping")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisConstraintException.class,
            expectedExceptionsMessageRegExp="No matching ACE found to remove!*")
    public void siteManagerCannotApplyInvalidAclWithPermissionMapping() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).and().assertThat().existsInRepo()
            .then().addAcl(inviteUser, PermissionMapping.CAN_DELETE_OBJECT)
                .and().assertThat().permissionIsSetForUser(inviteUser, EnumBasicPermissions.CMIS_ALL.value())
                    .then().applyAcl(inviteUser, PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT, PermissionMapping.CAN_CREATE_FOLDER_FOLDER);
    }
}
