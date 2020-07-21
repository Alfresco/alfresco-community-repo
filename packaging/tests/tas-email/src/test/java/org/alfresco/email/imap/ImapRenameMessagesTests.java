package org.alfresco.email.imap;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.email.EmailTest;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ImapRenameMessagesTests extends EmailTest
{
    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser).createIMAPSite();
    }

    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.IMAP }, executionType = ExecutionType.SANITY,
            description = "Verify the renamed file from repository is still preset in IMAP client with the old name for site manager")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.IMAP, TestGroup.SANITY })
    public void siteManagerShouldVerifyFileContentForRenamedFileViaImap() throws Exception
    {
        testFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        FileModel renamedFile = testFile;
        renamedFile.setName(FileModel.getRandomFileModel(FileType.TEXT_PLAIN).getName());
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFile).assertThat().existsInRepo()
                .and().assertThat().existsInImap()
                .then().renameMessageTo(renamedFile).assertThat().messageContentMatchesFileModelData(testFile);
    }
}
