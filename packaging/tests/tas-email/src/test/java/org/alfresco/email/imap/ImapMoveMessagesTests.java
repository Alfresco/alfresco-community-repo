package org.alfresco.email.imap;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.email.EmailTest;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.Test;

public class ImapMoveMessagesTests extends EmailTest
{
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can move message via IMAP client to a different location")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldMoveNonEmptyFile() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
        testFolder = dataContent.usingUser(testUser).usingSite(testSite).createFolder();
        FileModel fileModel = dataContent.usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(fileModel).assertThat().existsInRepo()
                .and().assertThat().existsInImap()
                .then().moveMessageTo(testFolder).assertThat().containsMessages(fileModel)
                .and().usingSite(testSite).assertThat().doesNotContainMessages(fileModel);
    }
}
