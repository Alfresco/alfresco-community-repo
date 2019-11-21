package org.alfresco.email.imap;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.email.EmailTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.Flags;
import javax.mail.MessagingException;
import java.io.IOException;

public class ImapReadMessagesTests extends EmailTest
{
    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        adminUser = dataUser.getAdminUser();
        adminSite = dataSite.usingAdmin().createIMAPSite();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify folders created in repository in other folder via IMAP client by admin user")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void adminShouldGetFoldersCreatedInRepositoryViaImap() throws Exception
    {
        testFolder = dataContent.usingAdmin().usingSite(testSite).createFolder();
        FolderModel testFolder1 = FolderModel.getRandomFolderModel();
        FolderModel testFolder2 = FolderModel.getRandomFolderModel();
        imapProtocol.authenticateUser(adminUser).usingSite(adminSite).usingResource(testFolder).createFolder(testFolder1)
                .and().assertThat().existsInRepo()
                .and().assertThat().existsInImap()
                .then().usingResource(testFolder).createFolder(testFolder2).assertThat().existsInRepo()
                .and().assertThat().existsInImap();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify file and its content are displayed via IMAP client when the file is created by site manager")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldGetFileAndItsContentsViaImap() throws Exception
    {
        FileModel fileModel = dataContent.usingUser(testUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(testUser).usingSite(testSite).assertThat().containsMessages(fileModel)
                .and().usingResource(fileModel).assertThat().existsInRepo()
                .then().assertThat().fileContentIsDisplayed();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify that file content in IMAP client contains creator, title, description, created date, " +
                    "modifier, modified date, size, three links to content folder, to content url, to download url")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldVerifyFileContent() throws Exception
    {
        FileModel fileModel = dataContent.usingUser(testUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(testUser).usingSite(testSite).assertThat().containsMessages(fileModel)
                .and().usingResource(fileModel).assertThat().existsInRepo()
                .then().assertThat().messageContentMatchesFileModelData(fileModel);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that the admin user can mark a message as read")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void adminCanMarkMessageAsRead() throws Exception
    {
        testFile = dataContent.usingUser(adminUser).usingSite(adminSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        imapProtocol.authenticateUser(adminUser).usingResource(testFile).withMessage().setSeenFlag().updateFlags()
                .then().assertThat().messageContainsFlags(Flags.Flag.SEEN);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site manager can see wiki pages via IMAP")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerCanViewWikiPages() throws Exception
    {
        dataWiki.usingUser(testUser).usingSite(testSite).createRandomWiki();
        imapProtocol.authenticateUser(testUser).usingSiteWikiContainer(testSite).assertThat().countMessagesIs(1);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that collaborator can see files created by self")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void collaboratorCanViewFileCreatedBySelf() throws Exception
    {
        UserModel collaboratorUser = dataUser.createRandomTestUser();
        dataUser.addUserToSite(collaboratorUser, testSite, UserRole.SiteCollaborator);
        testFile = dataContent.usingUser(collaboratorUser).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        imapProtocol.authenticateUser(collaboratorUser).usingSite(testSite).assertThat().containsMessages(testFile);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that contributor can see files created by self")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void contributorCanViewFileCreatedBySelf() throws Exception
    {
        UserModel contributorUser = dataUser.createRandomTestUser();
        dataUser.addUserToSite(contributorUser, testSite, UserRole.SiteContributor);
        testFile = dataContent.usingUser(contributorUser).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        imapProtocol.authenticateUser(contributorUser).usingSite(testSite).assertThat().containsMessages(testFile);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify file created with spaces in the name is displayed in IMAP client")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void fileWithSpacesInNameIsDisplayedInImap() throws Exception
    {
        testFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(new FileModel("name with spaces.txt", FileType.TEXT_PLAIN));
        imapProtocol.authenticateUser(testUser).usingSite(testSite).assertThat().containsMessages(testFile);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that a file created with name which contains special characters is visible in IMAP")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void userCanViewFileWithSpecialCharactersInNameViaIMAP() throws Exception
    {
        testFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(new FileModel("(a)[b]!#%^.txt", FileType.TEXT_PLAIN));
        imapProtocol.authenticateUser(testUser)
                .usingSite(testSite).assertThat().containsMessages(testFile)
                .usingResource(testFile).assertThat().existsInRepo();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that a file created with name which contains symbols is visible in IMAP")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void userCanViewFileWithSymbolsInNameViaIMAP() throws Exception
    {
        testFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(new FileModel("a£¥€$♊♎♏♐♑♒♓Ω.txt", FileType.TEXT_PLAIN));
        imapProtocol.authenticateUser(testUser)
                .usingSite(testSite).assertThat().containsMessages(testFile)
                .usingResource(testFile).assertThat().existsInRepo();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site manager can see links via IMAP")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void siteManagerCanViewLinks() throws Exception
    {
        dataLink.usingUser(testUser).usingSite(testSite).createRandomLink();
        imapProtocol.authenticateUser(testUser).usingSiteLinksContainer(testSite).assertThat().countMessagesIs(1);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site manager can see calendar events via IMAP")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL })
    public void siteManagerCanViewCalendarEvents() throws Exception
    {
        dataCalendarEvent.usingUser(testUser).usingSite(testSite).createRandomCalendarEvent();
        imapProtocol.authenticateUser(testUser).usingSiteCalendarContainer(testSite).assertThat().countMessagesIs(1);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site manager cannot read file via IMAP if it is already deleted from repository")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = MessagingException.class,
            expectedExceptionsMessageRegExp = "No message with subject .* has been found")
    public void siteManagerCannotReadFileInImapIfItWasDeletedFromRepository() throws Exception
    {
        testFile = dataContent.usingUser(testUser).usingSite(testSite)
                .createContent(new FileModel(RandomData.getRandomName("File"), FileType.TEXT_PLAIN));
        dataContent.usingUser(testUser).usingSite(testSite).usingResource(testFile).deleteContent();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFile).assertThat().fileContentIsDisplayed();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site manager cannot read file via IMAP client if it is locked by an user in repository")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = IOException.class,
            expectedExceptionsMessageRegExp = "No content")
    public void siteManagerCannotReadFileInImapIfItsLocked() throws Exception
    {
        String content = RandomData.getRandomAlphanumeric();
        testFile = dataContent.usingUser(testUser).usingSite(testSite)
                .createContent(new FileModel(RandomData.getRandomName("File"), FileType.TEXT_PLAIN, content));
        dataContent.usingUser(testUser).usingSite(testSite).usingResource(testFile).checkOutDocument();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFile).assertThat().fileContentIsDisplayed();
    }
}
