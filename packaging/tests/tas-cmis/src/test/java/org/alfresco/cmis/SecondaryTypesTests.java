package org.alfresco.cmis;

import java.util.Date;

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
import org.apache.commons.lang.time.DateUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SecondaryTypesTests extends CmisTest
{
    SiteModel testSite;
    UserModel testUser, nonInvitedUser;
    private DataUser.ListUserWithRoles usersWithRoles;
    FileModel managerFile;
    FolderModel managerFolder;
    String titledAspect = "P:cm:titled";
    
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerFile = FileModel.getRandomFileModel(FileType.MSWORD2007, documentContent);
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
    
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to get secondary types for Document")
    public void userCanGetSecondaryTypesForDocument() throws Exception
    {   
        cmisApi.authenticateUser(testUser).usingResource(managerFile)
            .assertThat().secondaryTypeIsAvailable(titledAspect);
    }
    
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to get secondary types for Folder")
    public void userCanGetSecondaryTypesForFolder() throws Exception
    {
        cmisApi.authenticateUser(testUser).usingResource(managerFolder)
            .assertThat().secondaryTypeIsAvailable(titledAspect);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions = {CmisObjectNotFoundException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to get secondary types for Folder")
    public void userCannotGetSecondaryTypesForInvalidObject() throws Exception
    {
        FolderModel folder = FolderModel.getRandomFolderModel();
        folder.setCmisLocation("/" + folder.getName() + "/");
        cmisApi.authenticateUser(testUser)
            .usingResource(folder)
                .assertThat().secondaryTypeIsAvailable(titledAspect);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is NOT able to get invalid secondary type for Folder")
    public void invalidSecondaryTypeIsNotAvailableForFolder() throws Exception
    {
        FolderModel folder = FolderModel.getRandomFolderModel();
        cmisApi.authenticateUser(testUser).usingSite(testSite)
            .createFolder(folder).and().assertThat().existsInRepo()
            .and().assertThat().secondaryTypeIsNotAvailable(titledAspect + "-fake");
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to get secondary types")
    public void collaboratorCanGetSecondaryTypesForContent() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(managerFolder)
                .assertThat().secondaryTypeIsAvailable(titledAspect)
                .then().usingResource(managerFile)
                    .assertThat().secondaryTypeIsAvailable(titledAspect);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to get secondary types")
    public void contributorCanGetSecondaryTypesForContent() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingResource(managerFolder)
                .assertThat().secondaryTypeIsAvailable(titledAspect)
                .then().usingResource(managerFile)
                    .assertThat().secondaryTypeIsAvailable(titledAspect);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site consumer is able to get secondary types")
    public void consumerCanGetSecondaryTypesForContent() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .usingResource(managerFolder)
                .assertThat().secondaryTypeIsAvailable(titledAspect)
                .then().usingResource(managerFile)
                    .assertThat().secondaryTypeIsAvailable(titledAspect);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is able to get secondary types in public site")
    public void nonInvitedUserCanGetSecondaryTypesForContentInPublicSite() throws Exception
    {
        cmisApi.authenticateUser(nonInvitedUser)
            .usingResource(managerFolder)
                .assertThat().secondaryTypeIsAvailable(titledAspect)
                .then().usingResource(managerFile)
                    .assertThat().secondaryTypeIsAvailable(titledAspect);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to get secondary types in private site")
    public void nonInvitedUserCannotGetSecondaryTypesForContentInPrivateSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createPrivateRandomSite();
        FileModel privateDoc = FileModel.getRandomFileModel(FileType.MSPOWERPOINT2007);
        cmisApi.authenticateUser(testUser).usingSite(privateSite)
            .createFile(privateDoc)
            .authenticateUser(nonInvitedUser)
                .assertThat().secondaryTypeIsAvailable(titledAspect);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify non invited user is not able to get secondary types in moderated site")
    public void nonInvitedUserCannotGetSecondaryTypesInModeratedSite() throws Exception
    {
        SiteModel privateSite = dataSite.usingUser(testUser).createModeratedRandomSite();
        FileModel privateDoc = FileModel.getRandomFileModel(FileType.MSPOWERPOINT2007);
        cmisApi.authenticateUser(testUser).usingSite(privateSite)
            .createFile(privateDoc)
            .authenticateUser(nonInvitedUser)
                .assertThat().secondaryTypeIsAvailable(titledAspect);
    }
    
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to add secondary types for Document")
    public void managerCanAddSecondaryTypesForDocument() throws Exception
    {   
        cmisApi.authenticateUser(testUser).usingResource(managerFile)
            .addSecondaryTypes("P:cm:dublincore")
                .assertThat().secondaryTypeIsAvailable("P:cm:dublincore")
                    .assertThat().secondaryTypeIsAvailable(titledAspect)
                        .then().updateProperty("cm:subject", "TAS Subject")
                               .updateProperty("cm:rights", "TAS Rights")
                               .updateProperty("cm:publisher", "TAS Team")
                        .then().assertThat().objectHasProperty("cm:subject", "TAS Subject")
                               .assertThat().objectHasProperty("cm:rights", "TAS Rights")
                               .assertThat().objectHasProperty("cm:publisher", "TAS Team");
    }
    
    @Test(groups = { TestGroup.SANITY, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.SANITY,
            description = "Verify site manager is able to add secondary types for folder")
    public void managerCanAddSecondaryTypesForFolder() throws Exception
    {   
        cmisApi.authenticateUser(testUser).usingResource(managerFolder)
            .addSecondaryTypes("P:cm:geographic")
                .assertThat().secondaryTypeIsAvailable("P:cm:geographic")
                    .assertThat().secondaryTypeIsAvailable(titledAspect)
                        .then().updateProperty("cm:longitude", 455.21)
                               .updateProperty("cm:latitude", 101.32)
                        .then().assertThat().objectHasProperty("cm:longitude", 455.21)
                               .assertThat().objectHasProperty("cm:latitude", 101.32);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=CmisObjectNotFoundException.class,
            expectedExceptionsMessageRegExp="Type 'P:cm:fakeAspect' is unknown!*")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to add fake secondary type")
    public void managerCannotAddInexistentSecondaryType() throws Exception
    {   
        cmisApi.authenticateUser(testUser).usingResource(managerFolder)
            .addSecondaryTypes("P:cm:fakeAspect");
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to add twice same secondary type")
    public void managerCanAddTwiceSameSecondaryType() throws Exception
    {   
        cmisApi.authenticateUser(testUser).usingResource(managerFile)
            .addSecondaryTypes("P:cm:dublincore")
                .assertThat().secondaryTypeIsAvailable("P:cm:dublincore")
                    .then().addSecondaryTypes("P:cm:dublincore")
                        .assertThat().secondaryTypeIsAvailable("P:cm:dublincore");
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site collaborator is able to add secondary types for document created by manager")
    public void collaboratorCanAddAspectForDocument() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).usingResource(managerFile)
            .addSecondaryTypes("P:audio:audio")
                .assertThat().secondaryTypeIsAvailable("P:audio:audio")
                    .assertThat().secondaryTypeIsAvailable(titledAspect)
                        .then().updateProperty("audio:channelType", "TAS")
                               .updateProperty("audio:trackNumber", 9)
                               .updateProperty("audio:sampleType", "TAS Sample")
                        .then().assertThat().objectHasProperty("audio:channelType", "TAS")
                               .assertThat().objectHasProperty("audio:trackNumber", 9)
                               .assertThat().objectHasProperty("audio:sampleType", "TAS Sample");
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to add secondary types for document created by manager")
    public void contributorCanAddAspectForDocument() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).usingResource(managerFile)
            .addSecondaryTypes("P:dp:restrictable")
                .assertThat().secondaryTypeIsAvailable("P:dp:restrictable")
                    .assertThat().secondaryTypeIsAvailable(titledAspect)
                        .then().updateProperty("dp:offlineExpiresAfter", 2)
                               .and().assertThat().objectHasProperty("dp:offlineExpiresAfter", 2);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions={CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site contributor is able to add secondary types for document created by manager")
    public void consumerCanAddAspectForDocument() throws Exception
    {
        cmisApi.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).usingResource(managerFile)
            .addSecondaryTypes("P:dp:restrictable")
                .assertThat().secondaryTypeIsAvailable("P:dp:restrictable")
                    .assertThat().secondaryTypeIsAvailable(titledAspect)
                        .then().updateProperty("dp:offlineExpiresAfter", 2)
                               .and().assertThat().objectHasProperty("dp:offlineExpiresAfter", 2);
    }

    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS})
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is able to add twice same secondary type")
    public void managerCanAddDateProperty() throws Exception
    {
        Date today = new Date();
        Date tomorow = new Date();
        tomorow = DateUtils.addDays(tomorow, 1);
        cmisApi.authenticateUser(testUser).usingResource(managerFile)
            .addSecondaryTypes("P:cm:effectivity")
                .assertThat().secondaryTypeIsAvailable("P:cm:effectivity")
                    .then().updateProperty("cm:to", today)
                           .updateProperty("cm:from", tomorow)
                        .and().assertThat().objectHasProperty("cm:to", today)
                              .assertThat().objectHasProperty("cm:from", tomorow);
    }
    
    @Test(groups = { TestGroup.REGRESSION, TestGroup.CMIS}, expectedExceptions=IllegalArgumentException.class,
            expectedExceptionsMessageRegExp="Property 'cm:fake-prop' is not valid for this type or one of the secondary types!*")
    @TestRail(section = {"cmis-api"}, executionType= ExecutionType.REGRESSION,
            description = "Verify site manager is not able to update aspect with fake property")
    public void managerCannotUpdateFakePropertyFromAspect() throws Exception
    {   
        cmisApi.authenticateUser(testUser).usingResource(managerFile)
            .addSecondaryTypes("P:cm:dublincore")
                .assertThat().secondaryTypeIsAvailable("P:cm:dublincore")
                    .assertThat().secondaryTypeIsAvailable(titledAspect)
                        .then().updateProperty("cm:fake-prop", "fake-prop");
    }
}
