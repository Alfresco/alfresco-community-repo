package org.alfresco.tas.integration;

import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IntegrationFullTestsBulk2  extends IntegrationTest
{
    private UserModel testUser1;
    private SiteModel testSitePublic;
    private FolderModel testFolder1, testFolder2;
    private FileModel wordFile, testFile;
    private ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser1 = dataUser.createRandomTestUser();
        
        wordFile = FileModel.getRandomFileModel(FileType.MSWORD2007, "tasTesting");
    }
    
    @BeforeMethod(alwaysRun = true)
    public void setup() throws DataPreparationException {
        testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        testFolder1 = FolderModel.getRandomFolderModel();
        testFolder2 = FolderModel.getRandomFolderModel();
        testFile = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "testContent");
    }
    
    /**
     * Scenario 73
     * 1. Create file1 using ftp
     * 2. Open document for edit using CMIS
     * 3. Try to edit document using Webdav while checked-out with CMIS
     * 4. Copy document to testFolder2 with ftp
     * 5. Update document from folder2, check its content is updated with Webdav
     * 6. Update document with WebDAV - should fail since document is checked out
     * 
     * @throws Exception
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Edit checked out document using several protocols")
    public void updateFileUsingDifferentProtocolsWhileDocumentIsCheckedOut() throws Exception
    {
        STEP("1. Create testFile1 using ftp");
        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(testFolder1)
            .usingResource(testFolder1).createFile(wordFile)
            .assertThat().contentIs("tasTesting");
        
        STEP("2. Open document for edit using CMIS");
        cmisAPI.authenticateUser(testUser1).usingResource(wordFile).checkOut();
        FileModel wordFilePWC = cmisAPI.usingResource(wordFile).withCMISUtil().getPWCFileModel();
        
        STEP("3. Try to edit document using Webdav while checked-out with CMIS - content should be updated");
        webDavProtocol.authenticateUser(testUser1).usingResource(wordFilePWC).update("update")
            .and().assertThat().contentIs("update");
        
        STEP("4. Copy document to testFolder2 with ftp");
        FileModel copiedWordFile = new FileModel(wordFile);
        ftpProtocol.usingSite(testSitePublic).createFolder(testFolder2)
                .usingResource(wordFile).copyTo(testFolder2);
        copiedWordFile.setCmisLocation(ftpProtocol.getLastResourceWithoutPrefix());
        ftpProtocol.usingResource(testFolder1).assertThat().hasFiles(wordFile)
            .and().usingResource(testFolder2).assertThat().hasFiles(copiedWordFile);
        
        STEP("5. Update document from folder2, check its content is updated with Webdav");
        webDavProtocol.usingResource(copiedWordFile).update("Step5")
            .and().assertThat().contentIs("Step5");
        
        STEP("6. Update document with WebDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(wordFile).update("WebDAVUpdate");
    }
    
    /**
     * Scenario 84
     * 1. Create folder1 with webdav
     * 2. Upload new file, file1 with ftp
     * 3. Set content for file1 with cmis
     * 4. CheckOut file1 with cmis 
     * 5. Try to edit file while checked-out with ftp
     * 6. Cancel checkout with cmis
     * 7. Try again to edit file with ftp
     * 8. Edit file content using webDav
     * 9. Get content using cmis, validate it
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL } )
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Update checked out document")
    public void updateCheckedOutFile() throws Exception
    {
        STEP("1. Create folder1 with webdav");
        webDavProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(testFolder1).and().assertThat().existsInRepo();
        
        STEP("2. Upload new file, file1 with ftp");
        ftpProtocol.authenticateUser(testUser1).usingResource(testFolder1).createFile(testFile)
            .and().assertThat().existsInRepo().and().assertThat().existsInFtp();
        
        STEP("3. Set content for file1 with cmis");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile).update("cmisUpdate")
            .and().assertThat().contentIs("testContentcmisUpdate");
        
        STEP("4. CheckOut file1 with cmis");
        cmisAPI.usingResource(testFile).checkOut();
        
        STEP("5. Try to edit file while checked-out with ftp - content should not be updated since file is checked out");
        ftpProtocol.usingResource(testFile).update("ftpUpdate")
            .and().assertThat().contentIs("testContentcmisUpdate");
        
        STEP("6. Cancel checkout with cmis");
        cmisAPI.usingResource(testFile).cancelCheckOut();
        
        STEP("7. Try again to edit file with ftp");
        ftpProtocol.usingResource(testFile).update("ftpUpdate")
            .and().assertThat().contentIs("ftpUpdate");
        
        STEP("8. Edit file content using webDav");
        webDavProtocol.usingResource(testFile).update("webdavUpdate").and().assertThat().contentIs("webdavUpdate");
        
        STEP("9. Get content using cmis, validate it");
        cmisAPI.usingResource(testFile).assertThat().contentIs("webdavUpdate");
    }
    
    /**
     * 1. Create folder1 with cmis
     * 2. Add new file with webdav
     * 3. Open file with Manager, add some content, save with webdav
     * 4. Open file with Consumer, add some content, save with ftp
     * 5. Open file with Contributor, delete some content, save with webdav
     * 6. Open file with Collaborator, add some content, save with WebDAV
     * 7. Open file with Consumer, delete some content, save with webdav
     * 8. Open file with Manager, add new content, save with cmis
     * 9. Open file with Collaborator, delete content that Manager added with ftp
     * 10. Open file with Manager, add new content with webdav
     * 11. Delete all content with admin using FTP
     * 12. Delete file with cmis
     * 13. Check file is deleted with WebDAV
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, 
        description = "Update file with different user roles and protocols")
    public void updateFileWithDifferentRolesUsingDifferentProtocols() throws Exception
    {
        usersWithRoles = dataUser.addUsersWithRolesToSite(testSitePublic, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
        
        STEP("1. Create folder1 with cmis");
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic).createFolder(testFolder1).and().assertThat().existsInRepo();
        
        STEP("2. Add new file with webdav");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFolder1).createFile(testFile).and().assertThat().existsInRepo();
        
        STEP("3. Open file with Manager, add some content, save with webdav");
        webDavProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).usingResource(testFile).update("webdavUpdate")
            .and().assertThat().contentIs("webdavUpdate");
        
        STEP("4. Open file with Consumer, add some content, save with ftp");
        ftpProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .usingResource(testFile).update("ftpUpdate").and().assertThat().contentIs("webdavUpdate");
        
        STEP("5. Open file with Contributor, delete some content, save with webdav");
        webDavProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor))
            .usingResource(testFile).update("webdavUpdate").and().assertThat().contentIs("webdavUpdate");
        
        STEP("6. Open file with Collaborator, add some content, save with WebDAV");
        webDavProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(testFile).update("webdavUpdate2").and().assertThat().contentIs("webdavUpdate2");
        
        STEP("7. Open file with Consumer, delete some content, save with webdav");
        webDavProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer))
            .usingResource(testFile).update("webdavUpdate3").and().assertThat().contentIs("webdavUpdate2");
        
        STEP("8. Open file with Manager, add new content, save with cmis");
        cmisAPI.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
            .usingResource(testFile).update("cmisUpdate").and().assertThat().contentIs("webdavUpdate2cmisUpdate");
        
        STEP("9. Open file with Collaborator, delete content that Manager added with ftp");
        ftpProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator))
            .usingResource(testFile).update("ftpUpdate").and().assertThat().contentIs("ftpUpdate");
        
        STEP("10. Open file with Contributor, add new page with webdav");
        webDavProtocol.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
            .usingResource(testFile).update("webdavUpdate").and().assertThat().contentIs("webdavUpdate");
        
        STEP("11. Delete all content with admin using FTP");
        ftpProtocol.authenticateUser(dataUser.getAdminUser())
            .usingResource(testFile).update("").and().assertThat().contentIs("");
        
        STEP("12. Delete file with cmis");
        cmisAPI.usingResource(testFile).delete().and().assertThat().doesNotExistInRepo();
        
        STEP("13. Check file is deleted with WebDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile).assertThat().doesNotExistInWebdav();
    }
    
    /**
     * 1. Add file1 to document library with ftp
     * 2. Update file content with WebDAV
     * 3. Add a minor change to a document with cmis
     * 4. Add a change with webdav, check document version with cmis
     * 5. Add a major change to a document with cmis
     * 6. Delete document last version with cmis
     * 7. Try to edit previous version with cmis
     * 8. Update content with ftp, check version
     * 9. Check document content with WebDAV
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, 
        description = "Check document versioning")
    public void documentVersioningTest() throws Exception
    {
        STEP("1. Add file1 to document library with ftp");
        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic).createFile(testFile).and().assertThat().existsInRepo();
        
        STEP("2. Update file content with WebDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile).update("WebDAVUpdate").and().assertThat().contentIs("WebDAVUpdate");
        
        STEP("3. Add a minor change to a document with cmis");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile).update("cmisUpdate")
            .then().assertThat().documentHasVersion(1.1).and().assertThat().contentIs("WebDAVUpdatecmisUpdate");
        
        STEP("4. Check document version with webdav");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile).update("webdavUpdate");
        cmisAPI.assertThat().documentHasVersion(1.2);
        
        STEP("5. Add a major change to a document with cmis");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile).checkOut().prepareDocumentForCheckIn().withMajorVersion().checkIn()
            .and().assertThat().documentHasVersion(2.0);
        
        STEP("6. Delete document last version with cmis");
        cmisAPI.usingResource(testFile).deleteAllVersions(false).and().assertThat().documentHasVersion(1.2);
        
        STEP("7. Try to edit previous version with cmis");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile).update("cmisUpdate")
            .then().assertThat().documentHasVersion(1.3).and().assertThat().contentIs("webdavUpdatecmisUpdate");
        
        STEP("8. Update content with ftp, check version");
        ftpProtocol.usingResource(testFile).update("ftpUpdate");
        cmisAPI.assertThat().documentHasVersion(1.4);
        
        STEP("9. Check document content with WebDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile).assertThat().contentIs("ftpUpdate");
    }
    
    /**
     * Scenario 82
     * 1. Create user1, user2
     * 2. User1 creates site1 and invites user2 as manager
     * 3. User1 adds document1 and tag1 to doc
     * 4. User2 gets tags and verifies tag1 appears
     * 5. User2 delete tag1
     * 6. User1 tries to update tag1
     * 7. User1 add new tag tag2
     * 8. User2 verifies tag2 appears and tag1 is not in the list
     * 9. User2 deletes document1
     * 10. User1 tries to delete tag2
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT, TestGroup.TAGS }, executionType = ExecutionType.REGRESSION, description = "Check negative scenarios for tags")
    public void tagsNegativeScenariosTest() throws Exception
    {
        STEP("1. Create user1, user2");
        UserModel user1 = dataUser.createRandomTestUser();
        UserModel user2 = dataUser.createRandomTestUser();
        user2.setUserRole(UserRole.SiteManager);
        
        STEP("2. User1 creates site1 and invites user2 as manager");
        SiteModel site = dataSite.usingUser(user1).createPublicRandomSite();
        restAPI.authenticateUser(user1).withCoreAPI().usingSite(site).addPerson(user2);
        
        STEP("3. User1 adds document1 and tag1 to doc");
        dataContent.usingUser(user1).usingSite(site).createContent(testFile);
        RestTagModel tag = restAPI.withCoreAPI().usingResource(testFile).addTag("tag1");
        restAPI.withCoreAPI().usingResource(testFile).getNodeTags().assertThat().entriesListContains("tag", "tag1");
        
        STEP("4. User2 gets tags and verifies tag1 appears");
        restAPI.authenticateUser(user2).withCoreAPI().usingResource(testFile).getNodeTags().assertThat().entriesListContains("tag", "tag1");
        
        STEP("5. User2 delete tag1");
        restAPI.withCoreAPI().usingResource(testFile).deleteTag(tag);
        restAPI.withCoreAPI().usingResource(testFile).getNodeTags().assertThat().entriesListDoesNotContain("tag", "tag1");
        
        STEP("6. User1 tries to update tag1");
        restAPI.authenticateUser(user2).withCoreAPI().usingTag(tag).update("updatedTag");
        restAPI.assertStatusCodeIs(HttpStatus.FORBIDDEN)
            .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
            .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY);
        
        STEP("7. User1 add new tag tag2");
        tag = restAPI.authenticateUser(user1).withCoreAPI().usingResource(testFile).addTag("tag2");
        restAPI.withCoreAPI().usingResource(testFile).getNodeTags().assertThat().entriesListContains("tag", "tag2");
        
        STEP("8. User2 verifies tag2 appears and tag1 is not in the list");
        restAPI.authenticateUser(user2).withCoreAPI().usingResource(testFile).getNodeTags()
            .assertThat().entriesListDoesNotContain("tag", "tag1")
            .assertThat().entriesListContains("tag", "tag2");
        
        STEP("9. User2 deletes document1");
        dataContent.usingUser(user2).usingResource(testFile).deleteContent();
        
        STEP("10. User1 tries to delete tag2");
        restAPI.authenticateUser(user1).withCoreAPI().usingResource(testFile).deleteTag(tag);
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, testFile.getNodeRefWithoutVersion()))
            .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY);
    }
    
    /**
     * Scenario 83
     * 1. Create user1, user2
     * 2. User1 creates site1 and invites user2 as manager with rest api
     * 3. User1 adds document1 with webdav
     * 4. User2 reads the document1 with ftp
     * 5. User1 update document1 with WebDAV
     * 6. User2 update the document1 with WebDAV
     * 7. User1 deletes the document1 with ftp
     * 8. Verify user2 cannot update document1 with cmis
     */
        @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL }, expectedExceptions = CmisObjectNotFoundException.class, expectedExceptionsMessageRegExp = ".*Object not found:.*")
        @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, description = "Negative scenarios for update document")
        public void fileUpdateNegativeScenariosTest() throws Exception
        {
            STEP("1. Create user1, user2");
        UserModel user1 = dataUser.createRandomTestUser();
        UserModel user2 = dataUser.createRandomTestUser();
        user2.setUserRole(UserRole.SiteManager);
        
        STEP("2. User1 creates site1 and invites user2 as manager with rest api");
        SiteModel site = dataSite.usingUser(user1).createPublicRandomSite();
        restAPI.authenticateUser(user1).withCoreAPI().usingSite(site).addPerson(user2);
        
        STEP("3. User1 adds document1 with webdav");
        webDavProtocol.authenticateUser(user1).usingSite(site).createFile(testFile).and().assertThat().existsInRepo();
        
        STEP("4. User2 reads the document1 with ftp");
        ftpProtocol.authenticateUser(user2).usingResource(testFile).assertThat().contentIs("testContent");
                
        STEP("5. User1 update document1 with WebDAV");
        webDavProtocol.authenticateUser(user1).usingResource(testFile).update("WebDAVUpdate1").assertThat().contentIs("WebDAVUpdate1");
        
        STEP("6. User2 update the document1 with WebDAV");
        webDavProtocol.authenticateUser(user2).usingResource(testFile).update("WebDAVUpdate2").assertThat().contentIs("WebDAVUpdate2");

        STEP("7. User1 deletes the document1 with ftp");
        ftpProtocol.authenticateUser(user1).usingResource(testFile).delete().assertThat().doesNotExistInFtp().assertThat().doesNotExistInRepo();
        
        STEP("8. Verify user2 cannot update document1 with cmis");
        cmisAPI.authenticateUser(user2).usingResource(testFile).update("cmisUpdate");
    }
    
    /**
     * Scenario 85
     * 1. Upload file with CMIS
     * 2. Add some content with ftp 
     * 3. Add a minor change to document, 1.2 with cmis
     * 4. Add a major change to document, 2.0 with cmis
     * 5. Add a minor change to document, 2.1 with cmis
     * 6. Delete version 2.0 with cmis with cmis
     * 7. Get document version with cmis
     * 8. Try to edit version 2.0 with cmis
     * 9. Delete version 2.1 with cmis
     * 10. Get document version with cmis
     * 11. Try to edit version 2.0 with cmis
     * 12. Check document content with ftp
     * 13. Check document version with cmis
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, 
        description = "File versioning test scenarios")
    public void fileVersioningScenariosTest() throws Exception
    {
        STEP("1. Upload file with CMIS");
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic).createFile(testFile).and().assertThat().existsInRepo();
        
        STEP("2. Add some content with ftp ");
        ftpProtocol.authenticateUser(testUser1).usingResource(testFile).update("ftpUpdate").and().assertThat().contentIs("ftpUpdate");
        
        STEP("3. Add a minor change to document, 1.1 with cmis");
        cmisAPI.usingResource(testFile).checkOut().prepareDocumentForCheckIn().withContent("cmisUpdate")
            .withMinorVersion().checkIn().refreshResource().and().assertThat().documentHasVersion(1.2);
        
        STEP("4. Add a major change to document, 2.0 with cmis");
        cmisAPI.usingResource(testFile).checkOut().prepareDocumentForCheckIn().withContent("cmisUpdate")
            .withMajorVersion().checkIn().and().assertThat().documentHasVersion(2.0);
        
        STEP("5. Add a minor change to document, 2.1 with cmis");
        cmisAPI.usingResource(testFile).checkOut().prepareDocumentForCheckIn().withContent("cmisUpdate")
            .withMinorVersion().checkIn().and().assertThat().documentHasVersion(2.1);
        
        STEP("6. Delete version 2.0 with cmis with cmis");
        cmisAPI.usingResource(testFile).deleteAllVersions(false);
        
        STEP("7. Get document version with cmis");
        cmisAPI.usingResource(testFile).assertThat().documentHasVersion(2.0);
        
        STEP("8. Try to edit version 2.0 with cmis");
        cmisAPI.usingResource(testFile).update("cmisUpdate").and().assertThat().contentIs("cmisUpdatecmisUpdate")
            .and().assertThat().documentHasVersion(2.1);
        
        STEP("9. Delete version 2.1 with cmis");
        cmisAPI.usingResource(testFile).deleteAllVersions(false);
        
        STEP("10. Get document version with cmis");
        cmisAPI.usingResource(testFile).assertThat().documentHasVersion(2.0);
        
        STEP("11. Try to edit version 2.0 with cmis");
        cmisAPI.usingResource(testFile).update("cmisUpdate").and().assertThat().contentIs("cmisUpdatecmisUpdate")
            .and().assertThat().documentHasVersion(2.1);
        
        STEP("12. Update and check document content with ftp");
        ftpProtocol.authenticateUser(testUser1).usingResource(testFile).update("ftpUpdate").assertThat().contentIs("ftpUpdate");
        
        STEP("13. Check document version with cmis");
        cmisAPI.usingResource(testFile).assertThat().documentHasVersion(2.2);
    }
    
    /**
     * 1. Upload file with cmis
     * 2. Add some content with WebDAV
     * 3. Add a minor change to document, 1.2 with cmis
     * 4. Add a major change to document, 2.0 with cmis
     * 5. Add a minor change to document, 2.1 with cmis
     * 6. Delete version 2.1 with cmis
     * 7. Try to edit the last version (which is now 2.0) with webdav
     * 8. Check version with CMIS
     * 9. Add new major change, version 3.0 with cmis
     * 10. Delete previous version with cmis
     * 11. Delete document with ftp
     * 12. Check document version with cmis
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL }, expectedExceptionsMessageRegExp = ".*Version Series not found.*",
        expectedExceptions = CmisObjectNotFoundException.class)
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION, 
        description = "File versioning additional test scenarios")
    public void fileVersioningNegativeScenariosTest() throws Exception
    {
        STEP("1. Upload file with cmis");
        cmisAPI.authenticateUser(testUser1).usingSite(testSitePublic).createFile(testFile).and().assertThat().existsInRepo();
        
        STEP("2. Add some content with WebDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile).update("WebDAVUpdate").and().assertThat().contentIs("WebDAVUpdate");
        
        STEP("3. Add a minor change to document, 1.2 with cmis");
        cmisAPI.usingResource(testFile).checkOut().prepareDocumentForCheckIn().withContent("cmisUpdate")
            .withMinorVersion().checkIn().refreshResource().and().assertThat().documentHasVersion(1.2);
        
        STEP("4. Add a major change to document, 2.0 with cmis");
        cmisAPI.usingResource(testFile).checkOut().prepareDocumentForCheckIn().withContent("cmisUpdate")
            .withMajorVersion().checkIn().and().assertThat().documentHasVersion(2.0);
        
        STEP("5. Add a minor change to document, 2.1 with cmis");
        cmisAPI.usingResource(testFile).checkOut().prepareDocumentForCheckIn().withContent("cmisUpdate")
            .withMinorVersion().checkIn().refreshResource().and().assertThat().documentHasVersion(2.1);
        
        STEP("6. Delete version 2.1 with cmis");
        cmisAPI.usingResource(testFile).deleteAllVersions(false).assertThat().documentHasVersion(2.0);
        
        STEP("7. Try to edit the last version (which is now 2.0) with webdav");
        webDavProtocol.authenticateUser(testUser1).usingResource(testFile).update("webdavUpdate");
        
        STEP("8. Check version with CMIS");
        cmisAPI.usingResource(testFile).assertThat().documentHasVersion(2.1);
        
        STEP("9. Add new major change, version 3.0 with cmis");
        cmisAPI.usingResource(testFile).checkOut().prepareDocumentForCheckIn().withContent("cmisUpdate")
            .withMajorVersion().checkIn().and().assertThat().documentHasVersion(3.0);
        
        STEP("10. Delete previous version with cmis");
        cmisAPI.usingResource(testFile).deleteAllVersions(false).assertThat().documentHasVersion(2.1);
        
        STEP("11. Delete document with ftp");
        ftpProtocol.authenticateUser(testUser1).usingResource(testFile).delete().assertThat().doesNotExistInRepo();
        
        STEP("12. Check document version with cmis - should fail");
        cmisAPI.usingResource(testFile).assertThat().documentHasVersion(2.1);
    }
}