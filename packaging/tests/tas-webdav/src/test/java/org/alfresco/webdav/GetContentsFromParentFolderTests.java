package org.alfresco.webdav;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class GetContentsFromParentFolderTests extends WebDavTest
{
    UserModel managerUser;
    SiteModel testSite;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        managerUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
    }
    
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
                description ="Verify that site manager can get contents from parent folder")
    public void siteManagerShouldGetContentsFromParent() throws Exception
    {
        FolderModel parentFolder = FolderModel.getRandomFolderModel();
        FolderModel subFolder1 = FolderModel.getRandomFolderModel();
        FolderModel subFolder2 = FolderModel.getRandomFolderModel();
        FolderModel subFolder3 = FolderModel.getRandomFolderModel();
        FileModel subFile1 = FileModel.getRandomFileModel(FileType.HTML);
        FileModel subFile2 = FileModel.getRandomFileModel(FileType.MSEXCEL);
        FileModel subFile3 = FileModel.getRandomFileModel(FileType.MSWORD);
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite)
            .createFolder(parentFolder)
                .then().usingResource(parentFolder)
                    .createFolder(subFolder1)
                    .createFolder(subFolder2)
                    .createFile(subFile1)
                    .createFile(subFile2)
                .then().usingResource(subFolder1)
                    .createFolder(subFolder3)
                    .createFile(subFile3)
                .when().usingResource(parentFolder)
                    .assertThat().hasChildren(subFolder1, subFolder2, subFile1, subFile2)
                        .and().assertThat().hasFolders(subFolder1, subFolder2)
                              .assertThat().hasFiles(subFile1, subFile2)
                .then().usingResource(subFolder1)
                    .assertThat().hasChildren(subFolder3, subFile3)
                        .and().assertThat().hasFolders(subFolder3)
                              .assertThat().hasFiles(subFile3)
                .then().usingSite(testSite)
                    .assertThat().hasChildren(parentFolder);
    }
}
