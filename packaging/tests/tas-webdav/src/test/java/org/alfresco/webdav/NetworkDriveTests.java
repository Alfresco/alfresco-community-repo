package org.alfresco.webdav;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.SystemUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;

public class NetworkDriveTests extends WebDavTest
{
    UserModel siteManager;
    SiteModel testSite;
    FolderModel folderModel;
    FileModel fileModel;
    String fileContent = "webdav file content";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        if(SystemUtils.IS_OS_WINDOWS)
            webDavProtocol.unmountNetworkDrive();

        siteManager = dataUser.getAdminUser();
        testSite = dataSite.usingUser(siteManager).createPublicRandomSite();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify admin can create folder in Alfresco root folder on mapped drive")
    public void adminCanCreateFolderInRootOnMappedDrive() throws Exception
    {
        folderModel = FolderModel.getRandomFolderModel();

        webDavProtocol.authenticateUser(dataUser.getAdminUser()).usingNetworkDrive().usingRoot().createFolder(folderModel)
                .then()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can create folder on mapped drive")
    public void siteManagerCanCreateFolderOnMappedDrive() throws Exception
    {
        folderModel = FolderModel.getRandomFolderModel();

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingSite(testSite).createFolder(folderModel)
                .then()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can create folder with name that contains spaces on mapped drive")
    public void siteManagerCanCreateFolderWithSpacesInNameOnMappedDrive() throws Exception
    {
        folderModel = new FolderModel("folder with name that contains spaces");

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingSite(testSite).createFolder(folderModel)
                .then()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager can create folder with name that contains special characters on mapped drive")
    public void siteManagerCanCreateFolderWithSpecialCharactersInNameOnMappedDrive() throws Exception
    {
        folderModel = new FolderModel("!$(){}[]_folder");

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingSite(testSite).createFolder(folderModel)
                .then()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN }, expectedExceptions = FileAlreadyExistsException.class)
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION,
            description = "Verify site manager cannot create folder twice on mapped drive")
    public void siteManagerCannotCreateFolderTwiceOnMappedDrive() throws Exception
    {
        folderModel = FolderModel.getRandomFolderModel();

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingSite(testSite).createFolder(folderModel)
                .then()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo()
                .and().usingSite(testSite).createFolder(folderModel);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can create folder inside another folder on mapped drive")
    public void siteManagerCanCreateFolderInFolderOnMappedDrive() throws Exception
    {
        FolderModel parentFolder = dataContent.usingUser(siteManager).usingSite(testSite).createFolder();
        folderModel = FolderModel.getRandomFolderModel();

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingResource(parentFolder).createFolder(folderModel)
                .then()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can create folder with name prefixed by dot on mapped drive")
    public void siteManagerCanCreateFolderWithNamePrefixedByDotOnMappedDrive() throws Exception
    {
        folderModel = new FolderModel("." + RandomData.getRandomName("folder"));

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingSite(testSite).createFolder(folderModel)
                .then()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can create file on mapped drive")
    public void siteManagerCanCreateFileOnMappedDrive() throws Exception
    {
        fileModel = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingSite(testSite).createFile(fileModel)
                .then()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo()
                    .assertThat().contentIs("");
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can create file with content on mapped drive")
    public void siteManagerCanCreateFileWithContentOnMappedDrive() throws Exception
    {
        fileModel = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, fileContent);

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingSite(testSite).createFile(fileModel)
                .then()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo()
                    .assertThat().contentIs(fileContent);
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can delete file on mapped drive")
    public void siteManagerCanDeleteFileOnMappedDrive() throws Exception
    {
        fileModel = dataContent.usingUser(siteManager).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingResource(fileModel).delete()
                .then()
                    .assertThat().doesNotExistInWebdav()
                    .assertThat().doesNotExistInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can delete folder on mapped drive")
    public void siteManagerCanDeleteFolderOnMappedDrive() throws Exception
    {
        folderModel = dataContent.usingUser(siteManager).usingSite(testSite).createFolder();

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingResource(folderModel).delete()
                .then()
                    .assertThat().doesNotExistInWebdav()
                    .assertThat().doesNotExistInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can rename file on mapped drive")
    public void siteManagerCanRenameFileOnMappedDrive() throws Exception
    {
        fileModel = dataContent.usingUser(siteManager).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        FileModel originalFileModel = new FileModel(fileModel);

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingResource(fileModel).rename("renamedFile")
                .and()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo()
                .then().usingResource(originalFileModel)
                    .assertThat().doesNotExistInWebdav()
                    .assertThat().doesNotExistInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can rename folder on mapped drive")
    public void siteManagerCanRenameFolderOnMappedDrive() throws Exception
    {
        folderModel = dataContent.usingUser(siteManager).usingSite(testSite).createFolder();
        FolderModel originalFolderModel = new FolderModel(folderModel);

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingResource(folderModel).rename("renamedFolder")
                .and()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo()
                .then().usingResource(originalFolderModel)
                    .assertThat().doesNotExistInWebdav()
                    .assertThat().doesNotExistInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can copy file on mapped drive")
    public void siteManagerCanCopyFileOnMappedDrive() throws Exception
    {
        fileModel = dataContent.usingUser(siteManager).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        FileModel copiedFile = new FileModel("copiedFile.txt");
        copiedFile.setCmisLocation(fileModel.getCmisLocation().replace(fileModel.getName(), copiedFile.getName()));

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingResource(fileModel).copyTo(copiedFile)
                .and()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo()
                .then().usingResource(fileModel)
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can copy folder on mapped drive")
    public void siteManagerCanCopyFolderOnMappedDrive() throws Exception
    {
        folderModel = dataContent.usingUser(siteManager).usingSite(testSite).createFolder();
        fileModel = dataContent.usingUser(siteManager).usingSite(testSite).usingResource(folderModel)
                .createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        FolderModel copiedFolder = new FolderModel("copiedFolder");
        copiedFolder.setCmisLocation(folderModel.getCmisLocation().replace(folderModel.getName(), copiedFolder.getName()));

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingSite(testSite).assertThat().hasFolders(folderModel)
                .usingResource(folderModel).copyTo(copiedFolder)
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo()
                    .assertThat().hasChildren(fileModel)
                .assertThat().hasFiles(fileModel)
                .then().usingResource(folderModel)
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can move file on mapped drive")
    public void siteManagerCanMoveFileOnMappedDrive() throws Exception
    {
        fileModel = dataContent.usingUser(siteManager).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        FileModel movedFile = new FileModel("movedFile");
        movedFile.setCmisLocation(fileModel.getCmisLocation().replace(fileModel.getName(), movedFile.getName()));

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingResource(fileModel).moveTo(movedFile)
                .and()
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo()
                .then().usingResource(fileModel)
                    .assertThat().doesNotExistInWebdav()
                    .assertThat().doesNotExistInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can move folder on mapped drive")
    public void siteManagerCanMoveFolderOnMappedDrive() throws Exception
    {
        folderModel = dataContent.usingUser(siteManager).usingSite(testSite).createFolder();
        fileModel = dataContent.usingUser(siteManager).usingSite(testSite).usingResource(folderModel)
                .createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        FolderModel moveFolder = dataContent.usingUser(siteManager).usingSite(testSite).createFolder();

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingSite(testSite).assertThat().hasFolders(folderModel)
                .usingResource(folderModel).moveTo(moveFolder)
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo()
                    .assertThat().hasChildren(fileModel)
                    .assertThat().hasFiles(fileModel)
                .then().usingSite(testSite).usingResource(folderModel)
                    .assertThat().doesNotExistInWebdav()
                    .assertThat().doesNotExistInRepo()
                .and().usingSite(testSite).usingResource(folderModel).usingResource(fileModel)
                    .assertThat().doesNotExistInWebdav()
                    .assertThat().doesNotExistInRepo();
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager can update file content on mapped drive")
    public void siteManagerCanUpdateFileContentOnMappedDrive() throws Exception
    {
        fileModel = dataContent.usingUser(siteManager).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingResource(fileModel).assertThat().contentIs("")
                .and()
                    .update("updated content")
                .then()
                    .assertThat().contentIs("updated content");
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN }, expectedExceptions = FileNotFoundException.class)
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify site manager cannot update content for a deleted file on mapped drive")
    public void siteManagerCannotUpdateFileContentForADeletedFileOnMappedDrive() throws Exception
    {
        fileModel = dataContent.usingUser(siteManager).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingResource(fileModel).delete()
                .then()
                    .update("updated content");
    }

    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL, TestGroup.OS_WIN })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.SANITY,
            description = "Verify that file version is incremented after file is edited on mapped drive")
    public void verifyFileVersionIsIncrementedAfterEditOnMappedDrive() throws Exception
    {
        fileModel = dataContent.usingUser(siteManager).usingSite(testSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        webDavProtocol.authenticateUser(siteManager).usingNetworkDrive().usingResource(fileModel).update("new content");
        dataContent.usingResource(fileModel).assertContentVersionIs("1.1");
    }
}
