package org.alfresco.email.imap;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.email.EmailTest;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.Test;

import javax.mail.Flags;

public class ImapMessageFlagsTests extends EmailTest
{
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can set flags action to a content(message) through IMAP Client")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldSetFlagsToAContent() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        FileModel fileModel = dataContent.usingUser(testUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(testUser).usingResource(fileModel).assertThat().existsInRepo()
                .and().assertThat().existsInImap()
                .and().withMessage().setAnsweredFlag().setSeenFlag().updateFlags()
                .then().assertThat().messageContainsFlags(Flags.Flag.ANSWERED, Flags.Flag.SEEN);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can set flags to a content(message) through IMAP Client")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerShouldSetFlagsToContent() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        FileModel fileModel = dataContent.usingUser(testUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(testUser).usingResource(fileModel).assertThat().existsInRepo()
                .and().assertThat().existsInImap()
                .and().withMessage().setFlags(Flags.Flag.ANSWERED, Flags.Flag.SEEN).updateFlags()
                .then().assertThat().messageContainsFlags(Flags.Flag.ANSWERED, Flags.Flag.SEEN);
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can remove flags from a content(message) through IMAP Client")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.CORE })
    public void siteManagerShouldRemoveFlagsFromContent() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        FileModel fileModel = dataContent.usingUser(testUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(testUser).usingResource(fileModel).assertThat().existsInRepo()
                .and().assertThat().existsInImap()
                .and().withMessage().setFlags(Flags.Flag.ANSWERED, Flags.Flag.SEEN).updateFlags()
                .and().assertThat().messageContainsFlags(Flags.Flag.ANSWERED, Flags.Flag.SEEN)
                .and().withMessage().removeFlags(Flags.Flag.ANSWERED, Flags.Flag.SEEN).updateFlags()
                .then().assertThat().messageDoesNotContainFlags(Flags.Flag.ANSWERED, Flags.Flag.SEEN);
    }
}
