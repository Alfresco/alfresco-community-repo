package org.alfresco.email.imap;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.email.EmailTest;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.MessagingException;

public class ImapDeleteMessagesTests extends EmailTest
{
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        adminSite = dataSite.usingAdmin().createIMAPSite();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify message can be deleted from IMAP client by admin")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void adminShouldDeleteMessage() throws Exception
    {
        testFile = dataContent.usingAdmin().usingSite(adminSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(dataUser.getAdminUser()).usingSite(adminSite).assertThat().containsMessages(testFile)
            .and().usingResource(testFile).assertThat().existsInRepo().deleteMessage()
            .and().assertThat().doesNotContainMessages(testFile)
            .then().usingResource(testFile).assertThat().doesNotExistInRepo();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify deleting message via IMAP client by user with MANAGER role")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldDeleteMessage() throws Exception
    {
        testFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(testUser).usingResource(testFile).assertThat().existsInRepo()
                .and().usingSite(testSite).assertThat().containsMessages(testFile).deleteMessage(testFile.getName())
                .and().assertThat().doesNotContainMessages(testFile);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify message has been deleted via REPOSITORU by user with MANAGER role")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerVerifyMessageHasBeenDeletedFromRepository() throws Exception
    {
        testFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        dataContent.usingUser(testUser).usingResource(testFile).deleteContent();
        imapProtocol.authenticateUser(testUser).usingResource(testFile).assertThat().doesNotExistInRepo()
                .and().usingSite(testSite).assertThat().doesNotContainMessages(testFile);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify deleting message via IMAP client by user with CONTRIBUTOR role")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteContributorShouldDeleteMessage() throws Exception
    {
        UserModel contributorUser = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(contributorUser, testSite, UserRole.SiteContributor);
        testFile = dataContent.usingUser(contributorUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(contributorUser).usingResource(testFile).deleteMessage()
                .usingResource(testFile).assertThat().doesNotExistInRepo()
                .and().usingSite(testSite).assertThat().doesNotContainMessages(testFile);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify deleting message via IMAP client by user with COLLABORATOR role")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteCollaboratorShouldDeleteMessage() throws Exception
    {
        UserModel collaboratorUser = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(collaboratorUser, testSite, UserRole.SiteCollaborator);
        testFile = dataContent.usingUser(collaboratorUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(collaboratorUser).usingResource(testFile).deleteMessage()
                .usingResource(testFile).assertThat().doesNotExistInRepo()
                .and().usingSite(testSite).assertThat().doesNotContainMessages(testFile);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify deleting message via IMAP client by user with CONSUMER role is not permitted")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE }, expectedExceptions = MessagingException.class,
            expectedExceptionsMessageRegExp = ".*No permission to set DELETED flag")
    public void siteConsumerShouldNotDeleteMessage() throws Exception
    {
        UserModel consumerUser = dataUser.createRandomTestUser();
        dataUser.usingUser(testUser).addUserToSite(consumerUser, testSite, UserRole.SiteConsumer);
        testFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(consumerUser).usingResource(testFile).deleteMessage();
    }
}
