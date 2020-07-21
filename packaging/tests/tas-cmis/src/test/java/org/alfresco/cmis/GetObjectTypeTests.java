package org.alfresco.cmis;

import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetObjectTypeTests extends CmisTest
{
    UserModel siteManager;
    SiteModel testSite;
    FolderModel testFolder;
    FileModel testFile;
    DataUser.ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception 
    {
        siteManager = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(siteManager).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(testSite, UserRole.SiteCollaborator, UserRole.SiteContributor, UserRole.SiteConsumer);

        testFolder = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager).usingSite(testSite)
                .createFolder(testFolder).and().assertThat().existsInRepo()
                .createFile(testFile).and().assertThat().existsInRepo();
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.SANITY,
            description = "Verify CMIS folder type")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void verifyCmisFolderType() throws Exception
    {
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
                .usingResource(testFolder)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_FOLDER.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.SANITY,
            description = "Verify CMIS document type")
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    public void verifyCmisDocumentType() throws Exception
    {
        cmisApi.authenticateUser(siteManager).usingSite(testSite)
                .usingResource(testFile)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_DOCUMENT.value());
    }
    
    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify CMIS folder type of folder that was deleted")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    public void verifyCmisFolderTypeOfDeletedFolder() throws Exception
    {
        FolderModel folderModel = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFolder(folderModel)
                .usingResource(folderModel).delete()
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_FOLDER.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Verify CMIS document type of file that was deleted")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    public void verifyCmisDocumentTypeOfDeletedDocument() throws Exception
    {
        FileModel fileModel = FileModel.getRandomFileModel(FileType.MSWORD);

        cmisApi.authenticateUser(siteManager).usingSite(testSite).createFile(fileModel)
                .usingResource(fileModel).delete()
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_DOCUMENT.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Site contributor verifies CMIS document type")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorVerifiesCmisDocumentType() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(testFile)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_DOCUMENT.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Site contributor verifies CMIS folder type")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteContributorVerifiesCmisFolderType() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(testFolder)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_FOLDER.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Site collaborator verifies CMIS document type")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorVerifiesCmisDocumentType() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(testFile)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_DOCUMENT.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Site collaborator verifies CMIS folder type")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteCollaboratorVerifiesCmisFolderType() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(testFolder)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_FOLDER.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Site consumer verifies CMIS document type")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteConsumerVerifiesCmisDocumentType() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingResource(testFile)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_DOCUMENT.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Site consumer verifies CMIS folder type")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS })
    public void siteConsumerVerifiesCmisFolderType() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingResource(testFolder)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_FOLDER.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Non site member is not able to verify CMIS document type for a document from a private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberCannotVerifyCmisObjectTypeForADocumentFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        FileModel privateSiteFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(privateSite).createFile(privateSiteFile);

        cmisApi.authenticateUser(siteManager).usingResource(privateSiteFile)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_DOCUMENT.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Non site member is not able to verify CMIS document type for a folder from a private site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberCannotVerifyCmisFolderTypeForAFolderFromPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        FolderModel privateSiteFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(privateSite).createFolder(privateSiteFolder);

        cmisApi.authenticateUser(siteManager).usingResource(privateSiteFolder)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_FOLDER.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Non site member is not able to verify CMIS document type for a document from a moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberCannotVerifyCmisObjectTypeForADocumentFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();
        FileModel moderatedSiteFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(moderatedSite).createFile(moderatedSiteFile);

        cmisApi.authenticateUser(siteManager).usingResource(moderatedSiteFile)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_DOCUMENT.value());
    }

    @TestRail(section = {"cmis-api"}, executionType = ExecutionType.REGRESSION,
            description = "Non site member is not able to verify CMIS document type for a folder from a moderated site")
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS }, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    public void nonSiteMemberCannotVerifyCmisObjectTypeForAFolderFromModeratedSite() throws Exception
    {
        SiteModel moderatedSite = dataSite.usingAdmin().createModeratedRandomSite();
        FolderModel moderatedSiteFolder = FolderModel.getRandomFolderModel();

        cmisApi.authenticateUser(dataUser.getAdminUser()).usingSite(moderatedSite).createFolder(moderatedSiteFolder);

        cmisApi.authenticateUser(siteManager).usingResource(moderatedSiteFolder)
                .assertThat().baseTypeIdIs(BaseTypeId.CMIS_FOLDER.value());
    }
}
