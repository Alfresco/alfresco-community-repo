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
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetExtensionTests extends CmisTest
{
    UserModel testUser, nonInvitedUser;
    SiteModel testSite;
    FileModel testFile, managerFile;
    FolderModel managerFolder;
    private DataUser.ListUserWithRoles usersWithRoles;
    private String titledAspect = "P:cm:titled";
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerFile = FileModel.getRandomFileModel(FileType.XML, documentContent);
        managerFolder = FolderModel.getRandomFolderModel();
        testUser = dataUser.createRandomTestUser();
        nonInvitedUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createPublicRandomSite();
        usersWithRoles = dataUser.usingUser(testUser)
                .addUsersWithRolesToSite(testSite, UserRole.SiteContributor, UserRole.SiteCollaborator, UserRole.SiteConsumer);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(managerFile)
            .createFolder(managerFolder);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Site manager can get extensions from a valid folder")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerCanGetExtensionsForValidFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingResource(managerFolder)
            .assertThat().hasAspectExtension(titledAspect);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Site manager can get extensions from a valid file")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void siteManagerCanGetExtensionsForValidFile() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingResource(managerFile)
            .assertThat().hasAspectExtension(titledAspect);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Site manager cannot get extensions from an invalid document - that was deleted")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    public void siteManagerCannotGetExtensionsForInvalidDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);
        cmisApi.authenticateUser(testUser).usingSite(testSite).createFile(testFile)
            .then().assertThat().hasAspectExtension(titledAspect)
            .and().delete()
                .then().assertThat().hasAspectExtension(titledAspect);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Site manager can get extensions from checkedout document")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void siteManagerCanGetExtensionsForCheckedOutDocument() throws Exception
    {
        testFile = FileModel.getRandomFileModel(FileType.MSWORD, documentContent);
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFile(testFile).checkOut()
                .then().assertThat().hasAspectExtension(titledAspect)
                    .then().usingPWCDocument()
                        .assertThat().hasAspectExtension(titledAspect);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Site collaborator can get extensions")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void collaboratorCanGetExtensions() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(managerFolder)
            .assertThat().hasAspectExtension(titledAspect)
                .then().usingResource(managerFile)
                    .assertThat().hasAspectExtension(titledAspect);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Site contributor can get extensions")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void contributorCanGetExtensions() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(managerFolder)
            .assertThat().hasAspectExtension(titledAspect)
                .then().usingResource(managerFile)
                    .assertThat().hasAspectExtension(titledAspect);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Site consumer can get extensions")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void consumerCanGetExtensions() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(managerFolder)
            .assertThat().hasAspectExtension(titledAspect)
                .then().usingResource(managerFile)
                    .assertThat().hasAspectExtension(titledAspect);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Non invited user can get extensions in public site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    public void nonInvitedUserCanGetExtensionsInPublicSite() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(managerFolder)
            .assertThat().hasAspectExtension(titledAspect)
                .then().usingResource(managerFile)
                    .assertThat().hasAspectExtension(titledAspect);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Non invited user cannot get extensions in private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotGetExtensionsInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        FileModel privateDoc = FileModel.getRandomFileModel(FileType.HTML, documentContent);
        cmisApi.authenticateUser(testUser).usingSite(privateSite)
            .createFile(privateDoc)
                .then().authenticateUser(nonInvitedUser).assertThat().hasAspectExtension(titledAspect);
    }
    
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Non invited user cannot get extensions in moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonInvitedUserCannotGetExtensionsInModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        FileModel moderatedDoc = FileModel.getRandomFileModel(FileType.HTML, documentContent);
        cmisApi.authenticateUser(testUser).usingSite(moderatedSite)
            .createFile(moderatedDoc)
                .then().authenticateUser(nonInvitedUser).assertThat().hasAspectExtension(titledAspect);
    }
}
