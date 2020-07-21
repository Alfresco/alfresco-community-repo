package org.alfresco.webdav;

import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.jackrabbit.webdav.DavException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WebDavRootTests extends WebDavTest
{
    UserModel testUser;
    FolderModel testFolder;
    FileModel testFile;
    FolderModel shared = new FolderModel("Shared");
    FolderModel imapAttachments = new FolderModel("Imap Attachments");
    FolderModel guest = new FolderModel("Guest Home");
    FolderModel userHome = new FolderModel("User Homes");
    FolderModel sites = new FolderModel("Sites");
    FolderModel dataDictionary = new FolderModel("Data Dictionary");
    FolderModel imapHome = new FolderModel("IMAP Home");

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify if valid user can get webdav root folders")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void siteManagerShouldGetRootFolders() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser();
        webDavProtocol.authenticateUser(testUser).usingRoot()
            .assertThat().hasFolders(shared, imapAttachments, guest, userHome, dataDictionary, imapHome);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.SANITY, 
            description ="Verify if admin user can get webdav root folders")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.SANITY })
    public void adminShouldGetRootFolders() throws Exception
    {
        webDavProtocol.authenticateUser(dataUser.getAdminUser()).usingRoot()
            .assertThat().hasFolders(shared, imapAttachments, guest, userHome, dataDictionary, imapHome);
    }
    
    @TestRail(section={TestGroup.PROTOCOLS, TestGroup.WEBDAV}, executionType= ExecutionType.REGRESSION, 
            description ="Verify that inexistent user cannot get webdav root folders")
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.CORE }, expectedExceptions=DavException.class)
    public void inexistentUserShouldNotGetRootFolders() throws Exception
    {
        webDavProtocol.authenticateUser(UserModel.getRandomUserModel()).usingRoot()
            .assertThat().hasFolders(shared, imapAttachments, guest, userHome, dataDictionary, imapHome);
    }
}
