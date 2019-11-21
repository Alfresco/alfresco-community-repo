package org.alfresco.webdav;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.Test;


public class DownloadTests extends WebDavTest
{
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify that admin user can download a file")
    public void adminShouldDownloadFile() throws Exception
    {
        FolderModel guestHomeFolder = FolderModel.getGuestHomeFolderModel();
        FileModel testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        webDavProtocol.authenticateUser(dataUser.getAdminUser()).usingResource(guestHomeFolder)
            .createFile(testFile).and().assertThat().hasStatus(HttpStatus.SC_CREATED)
                    .and().assertThat().existsInWebdav()
                        .then().download()
                            .and().assertThat().isDownloaded();
    }
}
