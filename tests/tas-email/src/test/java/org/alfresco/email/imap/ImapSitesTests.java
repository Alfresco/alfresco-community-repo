package org.alfresco.email.imap;

import org.alfresco.email.EmailTest;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.FolderNotFoundException;
import javax.mail.MessagingException;

public class ImapSitesTests extends EmailTest
{
    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site manager can unsubscribe from a folder")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerCanUnSubscribeFromAFolder() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).assertThat().isSubscribed()
                .usingResource(testFolder).assertThat().isSubscribed()
                .unsubscribe().assertThat().isNotSubscribed()
                .usingSite(testSite).assertThat().isSubscribed();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that site manager is subscribed to folders created by self")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerIsSubscribedToFoldersCreatedBySelf() throws Exception
    {
        testFolder = new FolderModel("newFolder");
        imapProtocol.authenticateUser(testUser).usingSite(testSite).assertThat().isSubscribed()
                .createFolder(testFolder).assertThat().existsInRepo().assertThat().existsInImap()
                .assertThat().isSubscribed();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that operations on a private site are not permitted")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = MessagingException.class,
            expectedExceptionsMessageRegExp = ".* Access Denied.*You do not have the appropriate permissions to perform this operation.*")
    public void verifyOperationsOnPrivateSiteAreNotPermitted() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        adminSite = dataSite.usingAdmin().createPrivateRandomSite();
        testFolder = dataContent.usingAdmin().usingSite(adminSite).createFolder();
        dataSite.usingUser(testUser).usingSite(testSite).setIMAPFavorite();
        imapProtocol.authenticateUser(testUser).usingSite(adminSite).assertThat().contains(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify that operations on a moderated site are not permitted")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = FolderNotFoundException.class)
    public void verifyOperationsOnModeratedSiteAreNotPermitted() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        adminSite = dataSite.usingAdmin().createModeratedRandomSite();
        testFolder = dataContent.usingAdmin().usingSite(adminSite).createFolder();
        dataSite.usingUser(testUser).usingSite(testSite).setIMAPFavorite();
        imapProtocol.authenticateUser(testUser).usingSite(adminSite).assertThat().contains(testFolder);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify site cannot be accessed in IMAP client if it is no longer an IMAP site or marked as IMAP favorite")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.FULL }, expectedExceptions = FolderNotFoundException.class)
    public void userCannotAccessSiteThatIsNoLongerAnImapSiteOrMarkedAsImapFavorite() throws Exception
    {
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).assertThat().contains(testFolder);
        dataSite.usingUser(testUser).usingSite(testSite).unsetIMAPFavorite();
        imapProtocol.authenticateUser(testUser).usingSite(testSite).assertThat().contains(testFolder);
    }
}
