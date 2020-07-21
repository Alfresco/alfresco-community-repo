package org.alfresco.tas.integration;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import java.io.File;

import javax.json.JsonObject;
import javax.mail.Flags;
import javax.mail.MessagingException;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.SiteService.Visibility;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestItemModelsCollection;
import org.alfresco.rest.model.RestNetworkModelsCollection;
import org.alfresco.rest.model.RestPreferenceModel;
import org.alfresco.rest.model.RestPreferenceModelsCollection;
import org.alfresco.rest.model.RestProcessDefinitionModelsCollection;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessVariableCollection;
import org.alfresco.rest.model.RestProcessVariableModel;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.rest.model.RestTaskModelsCollection;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.PreferenceName;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.*;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.report.Bug.Status;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 2/7/2017.
 */
public class IntegrationFullTests extends IntegrationTest
{
    private UserModel admin, testUser1, testUser2, userToAssign;
    private SiteModel publicSite, moderatedSite, privateSite;
    private FileModel testFile1, testFile2;
    private FolderModel parentFolder;   
    private RestTaskModelsCollection processTasks;
    private RestProcessModel processModel;
    private RestItemModelsCollection taskItems;
    private TaskModel taskModel;
    private RestTaskModel restTaskModel;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception
    {
        admin = dataUser.getAdminUser();
    }

    /**
     * Scenario
     * 1. Using CMIS create test user testUser
     * 2. Using CMIS create public IMAP site testSite
     * 3. Using CMIS create file testFile in testSite document library
     * 4. Using IMAP delete testFile
     * 5. Using CMIS try to rename testFile
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  }, expectedExceptions = CmisObjectNotFoundException.class)
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify the file cannot be renamed in repository if it is deleted from IMAP client")
    public void verifyFileCannotBeRenamedInRepoIfItWasAlreadyDeletedViaIMAP() throws Exception
    {
        UserModel testUser = dataUser.createRandomTestUser();
        SiteModel testSite = dataSite.usingUser(testUser).createIMAPSite();
        FileModel testFile = dataContent.usingUser(testUser).usingSite(testSite).createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
        imapProtocol.authenticateUser(testUser).usingSite(testSite).usingResource(testFile).deleteMessage();
        cmisAPI.authenticateUser(testUser).usingSite(testSite).usingResource(testFile).rename("new file name");
    }

    /**
     * Scenario 51
     * 1. Using CMIS create a test user: u1
     * 2. U1 creates a private site using CMIS
     * 3. U1 creates a folder in site's document library using WebDAV
     * 4. U1 uploads a document with size > x MB in folder1 using FTP
     * 5. Verify file is present in folder1 using WebDAV
     * 6. Verify size of the document from folder1 is exact as the size of the uploaded document CMIS
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify a 1MB file is uploaded in a private site")
    public void userShouldUploadFileInPrivateSite() throws Exception
    {
        STEP("1. Using CMIS create a test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. U1 creates a private site using CMIS");
        privateSite = dataSite.usingUser(testUser1).createPrivateRandomSite();

        STEP("3. U1 creates a folder in site's document library using WebDav");
        parentFolder = FolderModel.getRandomFolderModel();
        webDavProtocol.authenticateUser(testUser1)
                .usingSite(privateSite).createFolder(parentFolder)
                    .assertThat().existsInWebdav();

        STEP("4. U1 uploads a document with size > x MB in folder1 using FTP");
        File fileForUpload = Utility.getTestResourceFile("shared-resources/testdata/flower.jpg");
        ftpProtocol.authenticateUser(testUser1)
                .usingResource(parentFolder).uploadFile(fileForUpload)
                    .assertThat().existsInRepo()
                    .and().assertThat().existsInFtp();
        testFile1 = new FileModel(fileForUpload.getName());
        testFile1.setCmisLocation(ftpProtocol.getLastResourceWithoutPrefix());

        STEP("5. Verify file is present in folder1 using WebDAV");
        webDavProtocol.authenticateUser(testUser1).usingResource(parentFolder).assertThat().hasFiles(testFile1);

        STEP("6. Verify size of the document from folder1 is exact as the size of the uploaded document CMIS");
        cmisAPI.authenticateUser(testUser1)
                .usingResource(testFile1).assertThat().contentLengthIs(fileForUpload.length());
    }

    /**
     * Scenario 52
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates a private site using CMIS
     * 3. U1 creates 2 documents in his site using WebDAV
     * 4. U1 creates a new task with the documents created above and assigns the task to U2 using REST
     * 5. Verify that U2 doesn't have access to te documents attached to the task using FTP
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to access files from a private site, even if they are attached to a task assigned to him.")
    public void assigneeCantAccessFilesFromPrivateSiteIfHeIsNotAMember() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates a private site using CMIS");
        privateSite = dataSite.usingUser(testUser1).createPrivateRandomSite();

        STEP("3. U1 creates 2 documents in his site using WebDav");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        testFile2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file2 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(privateSite)
                .createFile(testFile1)
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo()
                .createFile(testFile2)
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo();

        STEP("4. U1 creates a new task with the documents created above and assigns the task to U2 using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", testUser2, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("5. Verify that U2 doesn't have access to te documents attached to the task using FTP");
        ftpProtocol.authenticateUser(testUser2)
                .usingResource(testFile1).assertThat().hasReplyCode(FTPReply.FILE_UNAVAILABLE).and().assertThat().doesNotExistInFtp()
                .usingResource(testFile2).assertThat().hasReplyCode(FTPReply.FILE_UNAVAILABLE).and().assertThat().doesNotExistInFtp();
    }

    /**
     * Scenario 53
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates a moderated site using CMIS
     * 3. U1 creates 2 documents in his site using WebDAV
     * 4. U1 creates a new task with the documents created above and assigns the task to U2 using REST
     * 5. Verify that U2 doesn't have access to te documents attached to the task using WebDav
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  }, expectedExceptionsMessageRegExp = "^Access is denied.$")
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to access files from a moderated site, even if they are attached to a task assigned to him.")
    public void assigneeCantAccessFilesFromModeratedSiteIfHeIsNotAMember() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates a moderated site using CMIS");
        moderatedSite = dataSite.usingUser(testUser1).createModeratedRandomSite();

        STEP("3. U1 creates 2 documents in his site using WebDAV");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        testFile2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file2 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(moderatedSite)
                .createFile(testFile1)
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo()
                .createFile(testFile2)
                    .assertThat().existsInWebdav()
                    .assertThat().existsInRepo();

        STEP("4. U1 creates a new task with the documents created above and assigns the task to U2 using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", testUser2, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("5. Verify that U2 doesn't have access to te documents attached to the task using WebDav");
        webDavProtocol.authenticateUser(testUser2)
                .usingResource(testFile1).assertThat().doesNotExistInWebdav()
                .usingResource(testFile2).assertThat().doesNotExistInWebdav();
    }

    /**
     * Scenario 54
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates a public site using CMIS
     * 3. U1 creates 2 documents in his site using CMIS
     * 4. U1 creates a new task with the documents created above and assigns the task to U2 using REST
     * 5. Verify that U2 have access to te documents attached to the task using WebDAV
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is able to access files from a public site, even if they are attached to a task assigned to him.")
    public void assigneeCanAccessFilesFromPublicSiteIfHeIsNotAMember() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public site using CMIS");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("3. U1 creates 2 documents in his site using CMIS");
        testFile1 = FileModel.getRandomFileModel(FileType.MSWORD, "file1 content");
        testFile2 = FileModel.getRandomFileModel(FileType.MSWORD, "file2 content");
        cmisAPI.authenticateUser(testUser1).usingSite(publicSite)
                .createFile(testFile1)
                    .assertThat().existsInRepo()
                    .usingSite(publicSite).createFile(testFile2)
                    .assertThat().existsInRepo();

        STEP("4. U1 creates a new task with the documents created above and assigns the task to U2 using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", testUser2, false, CMISUtil.Priority.Normal);
        testFile1.setNodeRef(dataContent.usingResource(testFile1).getNodeRef());
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        testFile2.setNodeRef(dataContent.usingResource(testFile2).getNodeRef());
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("5. Verify that U2 have access to te documents attached to the task using WebDAV");
        webDavProtocol.authenticateUser(testUser2)
                .usingSite(publicSite).assertThat().hasFiles(testFile1, testFile2);
    }
    
    /**
     * Scenario 55
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates 2 documents in User Home with CMIS
     * 3. U1 creates a new task with the documents created above and assigns the task to U2 using REST
     * 4. Verify that U2 doesn't have access to te documents attached to the task using CMIS
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  }, expectedExceptions = {CmisPermissionDeniedException.class, CmisUnauthorizedException.class})
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to access files from another user User Home repo, even if they are attached to a task assigned to him.")
    public void assigneeCantAccessFilesFromAnotherUserHome() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates 2 documents in User Home with CMIS");
        testFile1 = FileModel.getRandomFileModel(FileType.MSWORD, "file1 content");
        testFile2 = FileModel.getRandomFileModel(FileType.MSWORD, "file2 content");

        cmisAPI.authenticateUser(testUser1).usingUserHome()
                .createFile(testFile1)
                    .assertThat().existsInRepo()
                .createFile(testFile2)
                    .assertThat().existsInRepo();

        STEP("3. U1 creates a new task with the documents created above and assigns the task to U2 using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", testUser2, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("4. Verify that U2 doesn't have access to te documents attached to the task using CMIS");
        cmisAPI.authenticateUser(testUser2)
                .usingResource(testFile1).assertThat().doesNotExistInRepo();
    }

    /**
     * Scenario 56
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates document1 in a public site using CMIS
     * 3. U2 is added to that public site with manager role using REST
     * 4. U2 opens the document and renames it to document2 using WebDAV
     * 5. Verify that U1 can't delete anymore document1 using FTP
     * 6. U1 deletes document2 using WebDAV
     * 7. Verify U2 can't update document2 using WebDAV
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Check that user can't delete/update a file that is renamed/deleted by another user.")
    public void deleteFileAfterItIsRenamedByAnotherUser() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates document1 in a public site using CMIS");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        cmisAPI.authenticateUser(testUser1).usingSite(publicSite)
                .createFile(testFile1).assertThat().existsInRepo();

        STEP("3. U2 is added to that public site with manager role using REST");
        testUser2.setUserRole(UserRole.SiteManager);
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingSite(publicSite).addPerson(testUser2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("4. U2 opens the document and renames it to document2 using WebDAV");
        FileModel oldFile = new FileModel(testFile1);
        webDavProtocol.authenticateUser(testUser2)
                .usingResource(testFile1).rename("new" + testFile1.getName())
                    .assertThat().existsInRepo();

        STEP("5. Verify that U1 can't delete anymore document1 using FTP");
        ftpProtocol.authenticateUser(testUser1)
                .usingResource(oldFile).delete()
                    .assertThat().hasReplyCode(550);

        STEP("6. U1 deletes document2 using WebDAV");
        webDavProtocol.authenticateUser(testUser1)
                .usingResource(testFile1).delete()
                    .assertThat().doesNotExistInWebdav()
                    .assertThat().doesNotExistInRepo();

        STEP("7. Verify U2 can't update document2 using WebDAV");
        webDavProtocol.authenticateUser(testUser2)
                .usingResource(testFile1).update("file2 content")
                    .assertThat().hasStatus(404);
    }

    /**
     * Scenario 57
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates document1 in a public site using FTP
     * 3. U2 is added to that public site with manager role using REST
     * 4. U1 adds comment to document1 using REST
     * 5. U2 gets document1 comments using REST
     * 6. U1 renames document1 using CMIS
     * 7. U2 deletes comment using REST
     * 8. Verify U1 cannot get comment using REST
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT, TestGroup.COMMENTS }, executionType = ExecutionType.REGRESSION,
            description = "Check that user can't get a comment that is deleted by another user.")
    public void getCommentAfterItIsDeleted() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates document1 in a public site using FTP");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).assertThat().existsInRepo();

        STEP("3. U2 is added to that public site with manager role using REST");
        testUser2.setUserRole(UserRole.SiteManager);
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingSite(publicSite).addPerson(testUser2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("4. U1 adds comment to document1 using REST");
        RestCommentModel user1Comment = restAPI.authenticateUser(testUser1).withCoreAPI().usingResource(testFile1).addComment("user1 comment");
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("5. U2 gets document1 comments using REST");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingResource(testFile1).getNodeComments()
                .assertThat().entriesListContains("content", "user1 comment");

        STEP("6. U1 renames document1 using CMIS");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile1).rename("new" + testFile1.getName())
                    .assertThat().existsInRepo()
                    .assertThat().contentPropertyHasValue("cmis:name", "new" + testFile1.getName())
                .usingResource(testFile1).assertThat().doesNotExistInRepo();

        STEP("7. U2 deletes comment using REST");
        restAPI.withCoreAPI().usingResource(testFile1).deleteComment(user1Comment);
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        STEP("8. Verify U1 cannot get comment using REST");
        restAPI.authenticateUser(testUser1).withCoreAPI().usingResource(testFile1).getNodeComments()
                .assertThat().entriesListIsEmpty();
    }

    /**
     * Scenario 59
     * 1. Using CMIS create one test user: u1
     * 2. U1 creates a public site using CMIS
     * 3. Admin creates a folder with a document (doc1.txt) in the public site using WebDAV
     * 4. Admin renames document1 (doc1-edited.txt) using WebDAV
     * 5. U1 tries to move doc1.txt to another location using IMAP
     * 6. Verify that document is not moved (catch Exceptions)
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  }, expectedExceptions = MessagingException.class)
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is not able to move a renamed file using its initial name.")
    public void moveRenamedFileUsingInitialFilename() throws Exception
    {
        STEP("1. Using CMIS create one test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public site using CMIS");
        publicSite = dataSite.usingUser(testUser1).createIMAPSite();

        STEP("3. Admin creates a folder with a document (doc1.txt) in the public site using WebDav");
        parentFolder = FolderModel.getRandomFolderModel();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file 1 content");

        webDavProtocol.authenticateUser(admin)
                .usingSite(publicSite).createFolder(parentFolder)
                    .assertThat().existsInWebdav()
                .usingResource(parentFolder).createFile(testFile1)
                    .assertThat().existsInWebdav();

        STEP("4. Admin renames document1 (doc1-edited.txt) using WebDAV");
        FileModel oldFile = new FileModel(testFile1);
        webDavProtocol.authenticateUser(admin)
                .usingResource(testFile1).rename("new" + testFile1.getName())
                    .assertThat().existsInRepo();

        STEP("5. U1 tries to move doc1.txt to another location using IMAP");
        FolderModel destination = new FolderModel(Utility.buildPath("Sites", publicSite.getId(), "documentLibrary"));
        destination.setProtocolLocation(imapProtocol.authenticateUser(testUser1).usingSite(publicSite).getLastResourceWithoutPrefix());
        imapProtocol.usingResource(oldFile).moveMessageTo(destination);
    }
    
    /**
     * Scenario 61
     * 1. Using CMIS create an user and a site.
     * 2. Using FTP create two folders.
     * 3. Using CMIS create documents in folder1: a-childDoc1, childDoc11, child21.
     * 4. Using CMIS create documents in folder2: childDoc112, childDoc2, achild2.
     * 5. Using IMAP client flag content 'childDoc112'.
     * 6. Using IMAP find documents after search term 'child'.
     * 7. Using IMAP find documents after search term '.*child2.*'.
     * 8. Using IMAP find documents after search term 'childa'. No results should be displayed.
     * 9. Using IMAP find documents after search term 'child*2'.
     * 10. Using IMAP verify that 'childDoc112' is flagged.
     * 
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL})
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Find files and flag files.")
    public void findFilesAndFlagFiles() throws Exception
    {
        STEP("1. Using CMIS create an user and a site.");
        UserModel testUser = dataUser.createRandomTestUser();
        SiteModel testSitePublic = dataSite.usingUser(testUser).createIMAPSite();
        
        STEP("2. Using FTP create two folders.");
        FolderModel folder1 = FolderModel.getRandomFolderModel();
        FolderModel folder2 = FolderModel.getRandomFolderModel();
        
        ftpProtocol.authenticateUser(testUser).usingSite(testSitePublic).createFolder(folder1).and().createFolder(folder2);
        
        STEP("3. Using CMIS create documents in folder1: a-childDoc1, childDoc11, child21.");
        
        FileModel file11 = dataContent.usingUser(testUser).usingSite(testSitePublic).usingResource(folder1)
                .createContent(new FileModel("a-childDoc1", FileType.TEXT_PLAIN, "content"));
        FileModel file12 = dataContent.usingUser(testUser).usingSite(testSitePublic).usingResource(folder1)
                .createContent(new FileModel("childDoc11", FileType.TEXT_PLAIN, "content"));
        FileModel file13 = dataContent.usingUser(testUser).usingSite(testSitePublic).usingResource(folder1)
                .createContent(new FileModel("child21", FileType.TEXT_PLAIN, "content"));

        STEP("4. Using CMIS create documents in folder2: childDoc112, childDoc2, achild2.");
        
        FileModel file21 = dataContent.usingUser(testUser).usingSite(testSitePublic).usingResource(folder2)
                .createContent(new FileModel("childDoc112", FileType.TEXT_PLAIN, "content"));
        FileModel file22 = dataContent.usingUser(testUser).usingSite(testSitePublic).usingResource(folder2)
                .createContent(new FileModel("childDoc2", FileType.TEXT_PLAIN, "content"));
        FileModel file23 = dataContent.usingUser(testUser).usingSite(testSitePublic).usingResource(folder2)
                .createContent(new FileModel("achild2", FileType.TEXT_PLAIN, "content"));
        
        STEP("5. Using IMAP client flag content 'childDoc112'.");
        imapProtocol.authenticateUser(testUser).usingResource(file21).withMessage().setFlags(Flags.Flag.ANSWERED, Flags.Flag.SEEN).updateFlags();
        
        STEP("6. Using IMAP find documents after search term 'child'.");
        imapProtocol.usingResource(folder1).searchSubjectFor("child")
            .assertThat().resultsContainMessage(file11,file12,file13)
            .usingResource(folder2).searchSubjectFor("child")
            .assertThat().resultsContainMessage(file21,file22,file23);
     
        STEP("7. Using IMAP find documents after search term '.*child2.*'.");
        imapProtocol.usingResource(folder2).searchSubjectWithWildcardsFor(".*child2.*")
            .assertThat().resultsContainMessage(file23)
            .assertThat().resultsDoNotContainMessage(file21,file22);
        
        STEP("8. Using IMAP find documents after search term 'childa'. No results should be displayed.");
        imapProtocol.usingResource(folder1).searchSubjectFor("childa")
            .assertThat().resultsDoNotContainMessage(file11,file12,file13)
            .usingResource(folder2).searchSubjectFor("childa")
            .assertThat().resultsDoNotContainMessage(file21,file22,file23);
        
        STEP("9. Using IMAP find documents after search term 'child*2'.");
        imapProtocol.usingResource(folder1).searchSubjectWithWildcardsFor("child.*2.*")
            .assertThat().resultsContainMessage(file13)
            .assertThat().resultsDoNotContainMessage(file11,file12)
            .usingResource(folder2).searchSubjectWithWildcardsFor("child.*2.*")
            .assertThat().resultsContainMessage(file21,file22)
            .assertThat().resultsDoNotContainMessage(file23);
        
        STEP("10. Using IMAP verify that 'childDoc112' is flagged.");
        imapProtocol.usingResource(file21)
            .assertThat().messageContainsFlags(Flags.Flag.ANSWERED, Flags.Flag.SEEN);
    }
    
    /**
     * Scenario 62
     * 1. Using CMIS create two test users: u1 and userToAssign
     * 2. U1 creates a public site using CMIS: publicSite
     * 3. U1 creates a document in public site using WebDAV: documentTest
     * 4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST
     * 5. Verify that userToAssign receives the task using REST
     * 6. U1 adds items to task using REST
     * 7. U1 removes items from task using REST
     * 8. userToAssign gets the list of the updated items using REST
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify assigner user is able to add and remove items from task.")
    public void assignerIsAbleToAddAndRemoveItemsFromTask() throws Exception
    {    
        STEP("1. Using CMIS create two test users: u1 and userToAssign");
        testUser1 = dataUser.createRandomTestUser();
        UserModel userToAssign = dataUser.createRandomTestUser();
 
        STEP("2. U1 creates a public site using CMIS: publicSite");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        
        STEP("3. U1 creates a document in public site using WebDAV: documentTest");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite)
                    .createFile(testFile1)
                    .assertThat().webDavWrapper()
                    .assertThat().existsInRepo();
        
        STEP("4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST");    
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", userToAssign, false, CMISUtil.Priority.Normal);   
        processTasks = restAPI.authenticateUser(userToAssign).withWorkflowAPI()
                               .usingProcess(processModel)
                               .getProcessTasks();
        restAPI.withWorkflowAPI().usingTask(processTasks.getTaskModelByAssignee(userToAssign))
                                           .addTaskItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
             
        STEP("5. Verify that userToAssign receives the task using REST");      
        processTasks = restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingProcess(processModel)
                                                        .getProcessTasks();
        processTasks.assertThat().entriesListContains("assignee", userToAssign.getUsername()) 
                     .and().entriesListContains("state", "claimed") 
                     .and().paginationField("count").is("1");      
                
        STEP("6. U1 adds items to task using REST");  
        testFile2 = dataContent.usingSite(publicSite).createContent(CMISUtil.DocumentType.HTML);
        FileModel testFile3 = dataContent.usingSite(publicSite).createContent(CMISUtil.DocumentType.PDF);
        taskItems = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                                                    .usingTask(processTasks.getTaskModelByAssignee(userToAssign))
                                                    .addTaskItems(testFile2, testFile3);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        
        STEP("7. U1 removes items from task using REST");
        restAPI.authenticateUser(testUser1).withWorkflowAPI().usingTask(processTasks.getTaskModelByAssignee(userToAssign))
               .deleteTaskItem(taskItems.getEntries().get(0).onModel());                        
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        STEP("8. userToAssign gets the list of the updated items using REST");   
        restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingTask(processTasks.getTaskModelByAssignee(userToAssign))
               .getTaskItems()
               .assertThat().entriesListContains("name", testFile1.getName())           
               .assertThat().entriesListContains("name", testFile3.getName())            
               .and().paginationField("count").is("2");      
       }
    
    /**
     * Scenario 63
     * 1. Using CMIS create two test users: u1 and userToAssign
     * 2. U1 creates a public site using CMIS: publicSite
     * 3. U1 creates a document in public site using webDAV: documentTest
     * 4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST
     * 5. Verify that userToAssign receives the task using REST
     * 6. userToAssign adds items to task using REST
     * 7. userToAssign removes items from task using REST
     * 8. u1 gets the list of the updated items using REST
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee user is to add and remove items from task.")
    public void assigneeIsAbleToAddAndRemoveItemsFromTask() throws Exception
    {    
        STEP("1. Using CMIS create two test users: u1 and userToAssign");
        testUser1 = dataUser.createRandomTestUser();
        UserModel userToAssign = dataUser.createRandomTestUser();
 
        STEP("2. U1 creates a public site using CMIS: publicSite");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        
        STEP("3. U1 creates a document in public site using webDAV: documentTest");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite)
                      .createFile(testFile1)
                      .assertThat().existsInWebdav()
                      .assertThat().existsInRepo();
        
        STEP("4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST");    
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", userToAssign, false, CMISUtil.Priority.Normal);   
        processTasks = restAPI.authenticateUser(userToAssign).withWorkflowAPI()
                               .usingProcess(processModel)
                               .getProcessTasks();
        restAPI.withWorkflowAPI().usingTask(processTasks.getTaskModelByAssignee(userToAssign))
                                           .addTaskItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
             
        STEP("5. Verify that userToAssign receives the task using REST");      
        processTasks = restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingProcess(processModel)
                               .getProcessTasks();
        processTasks.assertThat().entriesListContains("assignee", userToAssign.getUsername()) 
                     .and().entriesListContains("state", "claimed") 
                     .and().paginationField("count").is("1");      
                
        STEP("6. userToAssign adds items to task using REST");       
        testFile2 = dataContent.usingSite(publicSite).createContent(CMISUtil.DocumentType.HTML);
        FileModel testFile3 = dataContent.usingSite(publicSite).createContent(CMISUtil.DocumentType.PDF);
      
        taskItems = restAPI.withWorkflowAPI().usingTask(processTasks.getTaskModelByAssignee(userToAssign))
                                             .addTaskItems(testFile2, testFile3);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
     
        STEP("7. userToAssign removes items from task using REST");
        restAPI.withWorkflowAPI().usingTask(processTasks.getTaskModelByAssignee(userToAssign))
               .deleteTaskItem(taskItems.getEntries().get(1).onModel());                        
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        STEP("8. U1 gets the list of the updated items using REST");   
        restAPI.authenticateUser(testUser1).withWorkflowAPI().usingTask(processTasks.getTaskModelByAssignee(userToAssign))
               .getTaskItems()
               .assertThat().entriesListContains("name", testFile2.getName())             
               .assertThat().entriesListContains("name", testFile1.getName())       
               .and().paginationField("count").is("2");      
       }
    
    /**
     * Scenario 66
     * 1. Using CMIS create two test users: u1 and userToAssign
     * 2. U1 creates a public site using CMIS: publicSite
     * 3. U1 creates a document in public site using webDavProtocol: documentTest
     * 4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST
     * 5. Verify that userToAssign receives the task using REST
     * 6. Status is changed by userToAssign using REST
     * 7. U1 gets the modified values from the task using REST
     * 8. userToAssign marks the task as 'Resolved' using REST
     * 9. U1 receives the task using REST
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee user is able to update task.")
    public void assigneeIsAbleToUpdateTask() throws Exception
    {
        STEP("1. Using CMIS create two test users: u1 and userToAssign");
        testUser1 = dataUser.createRandomTestUser();
        userToAssign = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public site using CMIS: publicSite");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        
        STEP("3. U1 creates a document in public site using webDavProtocol: documentTest");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).assertThat().existsInRepo();

        STEP("4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST");    
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                              .addProcess("activitiAdhoc", userToAssign, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        
        STEP("5. Verify that userToAssign receives the task using REST");
        taskModel = restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingProcess(processModel).getProcessTasks()
                           .getTaskModelByAssignee(userToAssign)    
                           .assertThat().field("assignee").is(userToAssign.getUsername());
                    
        STEP("6. Status is changed by userToAssign using REST");  
        restTaskModel = restAPI.authenticateUser(userToAssign).withParams("select=state").withWorkflowAPI().usingTask(taskModel)
                               .updateTask("claimed");
        restAPI.assertStatusCodeIs(HttpStatus.OK);       

        restTaskModel.assertThat().field("id").is(taskModel.getId())
               .and().field("state").is("claimed");
                      
        STEP("7. U1 gets the modified values from the task using REST");
        restAPI.authenticateUser(testUser1).withWorkflowAPI().usingTask(restTaskModel).getTask()
               .assertThat().field("id").is(taskModel.getId())
               .and().field("state").is("claimed");    
        
        STEP("8. userToAssign marks the task as 'Resolved' using REST"); 
        restTaskModel = restAPI.authenticateUser(userToAssign).withParams("select=state").withWorkflowAPI().usingTask(taskModel)
                               .updateTask("completed");
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                     .and().field("state").is("completed")                   
                     .and().field("assignee").is(userToAssign.getUsername());                       
                   
        STEP("9. U1 receives the task using REST");  
        restAPI.authenticateUser(testUser1).withWorkflowAPI().usingTask(restTaskModel).getTask()
               .assertThat().field("state").is("completed");       
        }    
    
    /**
     * Scenario 67
     * 1. Using CMIS create two test users: U1 and userToAssign
     * 2. U1 creates a public site using CMIS: publicSite
     * 3. U1 creates a document in public site using webDavProtocol: documentTest
     * 4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST
     * 5. Verify that userToAssign receives the task using REST
     * 6. Status is changed by U1 using REST
     * 7. userToAssign gets the modified values from the task using REST
     * 8. userToAssign marks the task as 'Resolved' using REST
     * 9. U1 receives the task using REST
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify assigner user is able to update task.")
    public void assignerIsAbleToUpdateTask() throws Exception
    {
        STEP("1. Using CMIS create two test users: u1 and userToAssign");
        testUser1 = dataUser.createRandomTestUser();
        userToAssign = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public site using CMIS: publicSite");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        
        STEP("3. U1 creates a document in public site using webDavProtocol: documentTest");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).assertThat().existsInRepo();

        STEP("4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST");    
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                              .addProcess("activitiAdhoc", userToAssign, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        
        STEP("5. Verify that userToAssign receives the task using REST");
        taskModel = restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingProcess(processModel).getProcessTasks().getEntryByIndex(0)
                           .assertThat().field("assignee").is(userToAssign.getUsername());
                    
        STEP("6. Status is changed by U1 using REST");  
        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", userToAssign.getUsername()).build();
        restTaskModel = restAPI.authenticateUser(testUser1).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel)
                               .updateTask(inputJson);
        restAPI.assertStatusCodeIs(HttpStatus.OK);       
        
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                     .and().field("state").is("delegated")
                     .and().field("owner").is(testUser1.getUsername())
                     .and().field("assignee").is(userToAssign.getUsername());
                      
        STEP("7. userToAssign gets the modified values from the task using REST");
        restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingProcess(processModel).getProcessTasks().getTaskModelByAssignee(userToAssign)
               .assertThat().field("id").is(taskModel.getId())
               .and().field("state").is("delegated");    
        
        STEP("8. userToAssign marks the task as 'Resolved' using REST"); 
        restTaskModel =  restAPI.withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                     .and().field("state").is("resolved");                 
                   
        STEP("9. U1 receives the task using REST");  
        restAPI.authenticateUser(testUser1).withWorkflowAPI().usingProcess(processModel).getProcessTasks().getEntryByIndex(0)
               .assertThat().field("state").is("resolved")
               .assertThat().field("id").is(taskModel.getId())
               .assertThat().field("assignee").is(testUser1.getUsername());        
        }
    
    /**
     * Scenario 68
     * 1. Using CMIS create two test users: u1 and userToAssign
     * 2. U1 creates a public site using CMIS: publicSite
     * 3. U1 creates a document in public site using webDavProtocol: documentTest
     * 4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST
     * 5. Verify that userToAssign receives the task using REST
     * 6. U1 cancels workflow using REST
     * 7. Verify task is no longer present for userToAssign
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify assigner user is able to cancel an workflow.")
    public void assignerIsAbleToCancelWorkflow() throws Exception
    {
        STEP("1. Using CMIS create two test users: u1 and userToAssign");
        testUser1 = dataUser.createRandomTestUser();
        userToAssign = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public site using CMIS: publicSite");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        
        STEP("3. U1 creates a document in public site using webDavProtocol: documentTest");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).assertThat().existsInRepo();

        STEP("4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI().addProcess("activitiAdhoc", userToAssign, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        
        restAPI.withWorkflowAPI().getProcesses()
//                due changing to java 11 the id changed
//               .assertThat().entriesListContains("processDefinitionId", WorkflowService.WorkflowType.NewTask.getId())
               .assertThat().entriesListContains("startActivityId", "start")           
               .and().paginationField("count").is("1");
        
        STEP("5. Verify that userToAssign receives the task using REST");
        restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingProcess(processModel).getProcessTasks()
               .getTaskModelByAssignee(userToAssign)
               .assertThat().field("assignee").is(userToAssign.getUsername());

        STEP("6. U1 cancels workflow using REST");
        Assert.assertTrue(dataWorkflow.usingUser(testUser1).cancelProcess(processModel), "User is able to cancel the workflow");

        restAPI.withWorkflowAPI().getProcesses().assertThat()
               .entriesListDoesNotContain("id", processModel.getId())
               .and().paginationField("count").is("0");
      
        STEP("7. Verify task is no longer present for userToAssign: MyTasks");
        restAPI.authenticateUser(userToAssign).withWorkflowAPI().getProcesses().assertThat()
               .entriesListDoesNotContain("id", processModel.getId());
    }

    /**
     * Scenario 69
     * 1. Using CMIS create two test users: u1 and userToAssign
     * 2. U1 creates a public site using CMIS: publicSite
     * 3. U1 creates a document in public site using webDavProtocol: documentTest
     * 4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST
     * 5. Verify that userToAssign receives the task using REST
     * 6. userToAssign is not able to cancel workflow using REST
     * 7. Verify U1 still has the workflow
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee user is able to cancel an workflow.")
    public void assigneeIsNotAbleToCancelWorkflow() throws Exception
    {
        STEP("1. Using CMIS create two test users: u1 and userToAssign");
        testUser1 = dataUser.createRandomTestUser();
        userToAssign = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public site using CMIS: publicSite");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        
        STEP("3. U1 creates a document in public site using webDavProtocol: documentTest");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).assertThat().existsInRepo();

        STEP("4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST");    
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI().addProcess("activitiAdhoc", userToAssign, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        
        restAPI.withWorkflowAPI().getProcesses()               
               .assertThat().entriesListContains("startActivityId", "start")           
               .and().paginationField("count").is("1");
        
        STEP("5. Verify that userToAssign receives the task using REST");
        restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingProcess(processModel).getProcessTasks()
               .getTaskModelByAssignee(userToAssign)
               .assertThat().field("assignee").is(userToAssign.getUsername());

        STEP("6. userToAssign is not able to cancel workflow using REST");
        Assert.assertFalse(dataWorkflow.usingUser(userToAssign).cancelProcess(processModel), "User is unable to cancel workflow.");

        restAPI.withWorkflowAPI().getProcesses().assertThat()
               .entriesListContains("id", processModel.getId())
               .and().paginationField("count").is("1");
      
        STEP("7. Verify U1 still has the workflow");
        restAPI.authenticateUser(userToAssign).withWorkflowAPI().getProcesses().assertThat()
               .entriesListContains("id", processModel.getId())
               .and().paginationField("count").is("1");
    }

    /**
     * Scenario 70
     * 1. Using CMIS create two test users: u1 and userToAssign
     * 2. U1 creates a public site using CMIS: publicSite
     * 3. U1 creates a document in public site using webDavProtocol: documentTest
     * 4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST
     * 5. Verify that userToAssign receives the task using REST
     * 6. U1 renames the document using webDAV
     * 7. Verify renamed document with users: U1 and userToAssign using RestAPI
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify assigner user is able to rename items from a task.")
    public void assignerIsAbleToRenameItemsFromTask() throws Exception
    {
        STEP("1. Using CMIS create two test users: u1 and userToAssign");
        testUser1 = dataUser.createRandomTestUser();
        userToAssign = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public site using CMIS: publicSite");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("3. U1 creates a document in public site using webDavProtocol: documentTest");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).assertThat().existsInRepo();

        STEP("4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", userToAssign, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("5. Verify that userToAssign receives the task using REST");
        taskModel = restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingProcess(processModel).getProcessTasks()
                .getTaskModelByAssignee(userToAssign)
                .assertThat().field("assignee").is(userToAssign.getUsername());

        STEP("6. U1 renames the document using webDAV");
        FileModel originalFileModel = new FileModel(testFile1);
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite)
                .usingResource(testFile1).and().assertThat().existsInRepo()
                .when().rename(testFile1.getName() + "-edit").assertThat().existsInRepo()
                .and().assertThat().hasStatus(org.apache.commons.httpclient.HttpStatus.SC_CREATED)
                .then().usingSite(publicSite).usingResource(originalFileModel).assertThat().doesNotExistInRepo();

        STEP("7. Verify renamed document with users: U1 and userToAssign using RestAPI");
        processTasks = restAPI.authenticateUser(testUser1).withWorkflowAPI().usingProcess(processModel)
                .getProcessTasks();
        restAPI.withWorkflowAPI().usingTask(processTasks.getTaskModelByAssignee(userToAssign))
                .getTaskItems()
                .assertThat().entriesListContains("name", testFile1.getName()+ "-edit")
                .assertThat().entriesListDoesNotContain("name", originalFileModel.getName())
                .and().paginationField("count").is("1");

        processTasks = restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingProcess(processModel)
                .getProcessTasks();
        restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingTask(processTasks.getTaskModelByAssignee(userToAssign))
                .getTaskItems()
                .assertThat().entriesListContains("name", testFile1.getName()+ "-edit")
                .assertThat().entriesListDoesNotContain("name", originalFileModel.getName())
                .and().paginationField("count").is("1");
    }

    /**
     * Scenario 71
     * 1. Using CMIS create two test users: u1 and userToAssign
     * 2. U1 creates a public site using CMIS: publicSite
     * 3. U1 creates a document in public site using webDavProtocol: documentTest
     * 4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST
     * 5. Verify that userToAssign receives the task using REST
     * 6. U1 deletes the document using webDAV
     * 7. Verify no documents are present with users: U1 and userToAssign using RestAPI
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify assigner user is able to delete items from a task.")
    public void assignerIsAbleToDeleteItemsFromTask() throws Exception
    {
        STEP("1. Using CMIS create two test users: u1 and userToAssign");
        testUser1 = dataUser.createRandomTestUser();
        userToAssign = dataUser.createRandomTestUser();

        STEP("2. U1 creates a public site using CMIS: publicSite");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("3. U1 creates a document in public site using webDavProtocol: documentTest");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).assertThat().existsInRepo();

        STEP("4. U1 creates a new task with the documents created above and assigns the task to userToAssign using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", userToAssign, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("5. Verify that userToAssign receives the task using REST");
        taskModel = restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingProcess(processModel).getProcessTasks()
                .getTaskModelByAssignee(userToAssign)
                .assertThat().field("assignee").is(userToAssign.getUsername());

        STEP("6. U1 deletes the document using webDAV");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite)
                .usingResource(testFile1).delete()
                .assertThat().hasStatus(org.apache.commons.httpclient.HttpStatus.SC_OK)
                .and().assertThat().doesNotExistInRepo()
                .and().assertThat().doesNotExistInWebdav()
                .and().assertThat().hasStatus(org.apache.commons.httpclient.HttpStatus.SC_NOT_FOUND);

        STEP("7. Verify no documents are present with users: U1 and userToAssign using RestAPI");
        processTasks = restAPI.authenticateUser(testUser1).withWorkflowAPI().usingProcess(processModel)
                .getProcessTasks();
        restAPI.withWorkflowAPI().usingTask(processTasks.getTaskModelByAssignee(userToAssign)).getTaskItems().assertThat()
                .entriesListDoesNotContain("name", testFile1.getName()).and()
                .paginationField("count").is("0");

        processTasks = restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingProcess(processModel).getProcessTasks();
        restAPI.authenticateUser(userToAssign).withWorkflowAPI().usingTask(processTasks.getTaskModelByAssignee(userToAssign)).getTaskItems().assertThat()
                .entriesListDoesNotContain("name", testFile1.getName()).and()
                .paginationField("count").is("0");
    }


    /**
     * Scenario 72
     * 1. Using CMIS create 1 test user: u1
     * 2. U1 creates folder1 in a public site using IMAP
     * 3. U1 creates file with special symbols in its name using FTP inside the public site
     * 4. U1 adds some special characters in document content using CMIS
     * 5. U1 renames document to a different name using special chars using FTP
     * 6. U1 copy document to the folder created above using IMAP
     * 7. U1 deletes document from initial location using CMIS
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is able to create, rename, update content, delete a file with special symbols in its name.")
    public void userIsAbleToDoCRUDActionsOnFileWithSpecialName() throws Exception
    {
        STEP("1. Using CMIS create 1 test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. U1 creates folder1 in a public site using IMAP");
        publicSite = dataSite.usingUser(testUser1).createIMAPSite();
        parentFolder = FolderModel.getRandomFolderModel();

        imapProtocol.authenticateUser(testUser1)
                .usingSite(publicSite).createFolder(parentFolder)
                .assertThat().existsInImap().and()
                .assertThat().existsInRepo();

        STEP("3. U1 creates file with special symbols in its name using FTP inside the public site");
        String fileName = RandomData.getRandomAlphanumeric();
        testFile1 = new FileModel(fileName + "\ufeff\u6768\u6728\u91d1.doc");
        ftpProtocol.authenticateUser(testUser1)
                .then().usingSite(publicSite)
                .createFile(testFile1).and().assertThat().existsInRepo()
                .and().assertThat().existsInFtp();

        STEP("4. U1 adds some special characters in document content using CMIS");
        cmisAPI.authenticateUser(testUser1).usingResource(testFile1).update("!@#$%^&()-_+={}[].,")
                .assertThat().contentIs("!@#$%^&()-_+={}[].,");

        STEP("5. U1 renames document to a different name using special chars using FTP");
        ftpProtocol.usingResource(testFile1).rename(fileName + "!@#$%^&()-_+={}[].,")
                        .assertThat().existsInRepo();

        testFile2 = new FileModel(testFile1);
        STEP("6. U1 copy document to the folder created above using IMAP");
        imapProtocol.authenticateUser(testUser1).usingSite(publicSite)
                .usingResource(testFile1)
                    .assertThat().existsInRepo()
                    .assertThat().existsInImap()
                .copyMessageTo(parentFolder)
                    .assertThat().containsMessages(testFile1);

        STEP("7. U1 deletes document from initial location using CMIS");
        cmisAPI.usingResource(testFile2).delete().assertThat().doesNotExistInRepo()
                .usingResource(parentFolder).assertThat().hasFiles(testFile1);
    }

    /**
     * Scenario 75
     * 1. Using CMIS create 3 test users: u1, u2 and u3
     * 2. U1 creates document1 in a public site using WebDAV
     * 3. U1 adds U3  to his site as Site manager using REST
     * 4. Check that U2 is not able to update document using FTP.
     * 5. Check that U3 is able to update document using WebDAV.
     * 6. U1 changes site visibility to moderated using REST
     * 7. Check that U2 is not able to update document using FTP.
     * 8. Check that U3 is able to update document WebDAV.
     * 9. U1 changes site visibility to private using REST
     * 10. Check that U2 is not able to update document using FTP.
     * 11. Check that U3 is able to update document WebDAV.
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT }, executionType = ExecutionType.REGRESSION,
            description = "Verify users ability to update files when site visibility is changed.")
    public void updateFileByRegularUserAndSiteMemberWhenSiteVisibilityIsChanged() throws Exception
    {
        STEP("1. Using CMIS create 3 test users: u1, u2 and u3");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();
        UserModel testUser3 = dataUser.createRandomTestUser();

        STEP("2. U1 creates document1 in a public site using WebDAV");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1)
                .usingSite(publicSite).createFile(testFile1)
                    .assertThat().existsInRepo();

        STEP("3. U1 adds U3  to his site as Site manager using REST");
        testUser3.setUserRole(UserRole.SiteManager);
        restAPI.authenticateUser(testUser1).withCoreAPI()
                .usingSite(publicSite).addPerson(testUser3);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("4. Check that U2 is able to update document using FTP.");
        ftpProtocol.authenticateUser(testUser2).usingResource(testFile1).update("u2 update when site visibility is public")
                .assertThat().contentIs("file1 content");

        STEP("5. Check that U3 is able to update document using WebDav.");
        webDavProtocol.authenticateUser(testUser3).usingResource(testFile1).update("u3 update when site visibility is public")
                .assertThat().contentIs("u3 update when site visibility is public");

        STEP("6. U1 changes site visibility to moderated using REST");
        dataSite.usingUser(testUser1).updateSiteVisibility(publicSite, Visibility.MODERATED);

        STEP("7. Check that U2 is not able to update document using FTP.");
        ftpProtocol.authenticateUser(testUser2).usingResource(testFile1).update("u2 update when site visibility is moderated")
                .assertThat().hasReplyCode(FTPReply.ACTION_ABORTED);

        STEP("8. Check that U3 is able to update document WebDAV.");
        webDavProtocol.authenticateUser(testUser3).usingResource(testFile1).update("u3 update when site visibility is moderated")
                .assertThat().contentIs("u3 update when site visibility is moderated");

        STEP("9. U1 changes site visibility to private using REST");
        dataSite.usingUser(testUser1).updateSiteVisibility(publicSite, Visibility.PRIVATE);

        STEP("10. Check that U2 is not able to update document using FTP.");
        ftpProtocol.authenticateUser(testUser2).usingResource(testFile1).update("u2 update when site visibility is private")
                .assertThat().hasReplyCode(FTPReply.ACTION_ABORTED);

        STEP("11. Check that U3 is able to update document WebDAV.");
        webDavProtocol.authenticateUser(testUser3).usingResource(testFile1).update("u3 update when site visibility is private")
                .assertThat().contentIs("u3 update when site visibility is private");
    }

    /**
     * Scenario 89
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates document1 in a public site using FTP
     * 3. U1 creates a New Task with the document created above and assigns it to U2 using REST
     * 4. U2 updates the task as resolved using REST
     * 5. U1 deletes the task process using REST. Assert that process is deleted successfully among with its tasks.
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify initiator is able to delete a process after its task is updated by assignee as resolved.")
    public void deleteWorkflowByInitiator() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates document1 in a public site using FTP");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        ftpProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).assertThat().existsInRepo();

        STEP("3. U1 creates a New Task with the document created above and assigns it to U2 using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", testUser2, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("4. U2 updates the task as Done using REST");
        processTasks = restAPI.authenticateUser(testUser2).withWorkflowAPI().usingProcess(processModel).getProcessTasks();
        restAPI.assertStatusCodeIs(HttpStatus.OK);

        RestTaskModel processTask = processTasks.getTaskModelByAssignee(testUser2);

        restAPI.withParams("select=state").withWorkflowAPI().usingTask(processTask).updateTask("resolved");
        restAPI.assertStatusCodeIs(HttpStatus.OK);

        STEP("5. U1 deletes the task process using REST. Assert that process is deleted successfully among with its tasks.");
        restAPI.authenticateUser(testUser1).withWorkflowAPI().usingProcess(processModel).deleteProcess();
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restAPI.withWorkflowAPI().getProcesses().assertThat().entriesListDoesNotContain("id", processModel.getId());
        restAPI.withWorkflowAPI().getTasks().assertThat().entriesListDoesNotContain("id", processTask.getId());
    }

    /**
     * Scenario 90
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates document1 in a public site using WebDAV
     * 3. U1 creates a New Task with the document created above and assigns it to U2 using REST
     * 4. U2 updates the task as resolved using REST
     * 5. U1 updates the task as completed using REST
     * 6. U2 deletes the task process using REST. Assert that process is not deleted.
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee is not able to delete a process after its task is completed.")
    public void deleteWorkflowByAssignee() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates document1 in a public site using WebDAV");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        webDavProtocol.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).assertThat().existsInRepo();

        STEP("3. U1 creates a New Task with the document created above and assigns it to U2 using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", testUser2, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("4. U2 updates the task as resolved using REST");
        processTasks = restAPI.authenticateUser(testUser2).withWorkflowAPI().usingProcess(processModel).getProcessTasks();
        restAPI.assertStatusCodeIs(HttpStatus.OK);

        RestTaskModel processTask = processTasks.getTaskModelByAssignee(testUser2);

        restAPI.withParams("select=state").withWorkflowAPI().usingTask(processTask).updateTask("resolved");
        restAPI.assertStatusCodeIs(HttpStatus.OK);

        STEP("5. U1 updates the task as completed using REST");
        restAPI.authenticateUser(testUser1).withParams("select=state").withWorkflowAPI().usingTask(processTask).updateTask("completed");
        restAPI.assertStatusCodeIs(HttpStatus.OK);

        STEP("6. U2 deletes the task process using REST. Assert that process is not deleted. ");
        restAPI.authenticateUser(testUser2).withWorkflowAPI().usingProcess(processModel).deleteProcess();
        restAPI.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError().containsSummary(String.format(RestErrorModel.ACCESS_INFORMATION_NOT_ALLOWED, processModel.getId()));
    }

    /**
     * Scenario 91
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates document1 in a public site using CMIS
     * 3. U1 creates a New Task with the document created above and assigns it to U2 using REST
     * 4. U1 deletes the task process using REST. Assert that process is deleted.
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL  })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW }, executionType = ExecutionType.REGRESSION,
            description = "Verify initiator is able to delete a process even if its task is not completed.")
    public void deleteWorkflowWithoutCompletingIt() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates document1 in a public site using CMIS");
        publicSite = dataSite.usingUser(testUser1).createPublicRandomSite();
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        cmisAPI.authenticateUser(testUser1).usingSite(publicSite).createFile(testFile1).assertThat().existsInRepo();

        STEP("3. U1 creates a New Task with the document created above and assigns it to U2 using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", testUser2, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("4. U1 deletes the task process using REST. Assert that process is deleted.");
        restAPI.withWorkflowAPI().usingProcess(processModel).deleteProcess();
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restAPI.withWorkflowAPI().getProcesses().assertThat().entriesListDoesNotContain("id", processModel.getId());
    }

    /**
     * Scenario 94
     *  1. Using CMIS create one test user: u1
     *  2. Using FTP create file in unauthorized folder (Data Dictionary)
     *  3. Verify that document is not created
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.CONTENT}, executionType = ExecutionType.REGRESSION,
            description = "Verify that document is not created in unauthorized folder (Data Dictonary)")
    public void createUserMakeUnathorizedAction() throws Exception
    {
        STEP("1. Using CMIS creates one test user: u1");
        testUser1 = dataUser.createRandomTestUser();

        STEP("2. Using FTP create file in unauthorized folder (Data Dictionary). 3. Verify that document is not created");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");

        ftpProtocol.authenticateUser(testUser1)
                .changeWorkingDirectory(ftpProtocol.getDataDictionaryPath())
                .assertThat().currentFolderIs(ftpProtocol.getDataDictionaryPath())
                .createFile(testFile1).assertThat().doesNotExistInFtp();
    }

    /**
     * Scenario 95
     * 1. Using CMIS create 2 test users: u1 and u2
     * 2. U1 creates 2 documents in User Homes using CMIS
     * 3. U1 verifies the list of available process definitions using REST
     * 4. U1 creates a workflow of type "activitiAdhoc" with the documents created above and assign it to u2 using REST
     * 5. U1 adds a new process variable using REST
     * 6. U2 gets the process variables using REST
     * 7. U2 closes the task using REST
     * 8. Verify that user2 doesn't have access to the documents attached to the task using FTP
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW}, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee user is able to get a process variable added by another user.")
    public void createWorkflowProcessWithNewProcessVariable() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates 2 documents in User Homes using CMIS");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        testFile2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file2 content");
        cmisAPI.authenticateUser(testUser1).usingUserHome()
                .createFile(testFile1).assertThat().existsInRepo()
                .createFile(testFile2).assertThat().existsInRepo();

        STEP("3. U1 verifies the list of available process definitions using REST");
        RestProcessDefinitionModelsCollection procDefinitions = restAPI.authenticateUser(testUser1).withWorkflowAPI().getAllProcessDefinitions();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        procDefinitions.assertThat().entriesListContains("key", "activitiAdhoc");

        STEP("4. U1 creates a workflow of type 'activitiAdhoc' with the documents created above and assign it to u2 using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiAdhoc", testUser2, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("5. U1 adds a new process variable using REST");
        RestProcessVariableModel variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");

        RestProcessVariableModel processVariable = restAPI.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        processVariable.assertThat().field("name").is(variableModel.getName())
                .and().field("type").is(variableModel.getType())
                .and().field("value").is(variableModel.getValue());

        STEP("6. U2 gets the process variables using REST");
        RestProcessVariableCollection processVariables = restAPI.authenticateUser(testUser2).withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        processVariables.getProcessVariableByName(processVariable.getName())
                .assertThat().field("type").is(processVariable.getType())
                .assertThat().field("value").is(processVariable.getValue());

        processVariables.getProcessVariableByName("bpm_assignee")
                .assertThat().field("type").is("cm:person")
                .assertThat().field("value").is(testUser2.getUsername());

        processVariables.getProcessVariableByName("bpm_priority")
                .assertThat().field("type").is("d:int")
                .assertThat().field("value").is(CMISUtil.Priority.Normal.getLevel());

        processVariables.getProcessVariableByName("bpm_sendEMailNotifications")
                .assertThat().field("type").is("d:boolean")
                .assertThat().field("value").is(false);

        processVariables.getProcessVariableByName("initiator")
                .assertThat().field("type").is("d:noderef")
                .assertThat().field("value").is(testUser1.getUsername());

        processVariables.getProcessVariableByName("bpm_percentComplete")
                .assertThat().field("type").is("d:int")
                .assertThat().field("value").is(0);

        STEP("7. U2 closes the task using REST");
        RestTaskModel processTask = restAPI.withWorkflowAPI().usingProcess(processModel).getProcessTasks().getTaskModelByAssignee(testUser2);
        restAPI.withParams("select=state").withWorkflowAPI().usingTask(processTask).updateTask("completed");
        restAPI.assertStatusCodeIs(HttpStatus.OK);

        STEP("8. Verify that user2 doesn't have access to the documents attached to the task using FTP");
        ftpProtocol.authenticateUser(testUser2)
                .usingResource(testFile1).assertThat().hasReplyCode(FTPReply.FILE_UNAVAILABLE).and().assertThat().doesNotExistInFtp()
                .usingResource(testFile2).assertThat().hasReplyCode(FTPReply.FILE_UNAVAILABLE).and().assertThat().doesNotExistInFtp();
    }

    /**
     * Scenario 96
     * 1. Using CMIS create 2 test users: u1 and u2.
     * 2. U1 creates 2 documents in User Homes using CMIS
     * 3. U1 verifies the list of available process definitions using REST
     * 4. U1 creates a workflow of type 'activitiReview' with the documents created above and assign it to u2 using REST
     * 5. U1 adds a new process variable using REST
     * 6. U2 gets the process variables using REST
     * 7. U2 closes the task using REST
     * 8. U1 deletes the new added process variable
     * 9. U1 starts new 'activitiReview' process. Verify the process doesn't have the process variable added above.
     */
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.WORKFLOW}, executionType = ExecutionType.REGRESSION,
            description = "Verify process variable is process specific, not process definition.")
    public void createWorkflowProcessAndDeleteProcessVariable() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2.");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates 2 documents in User Homes using CMIS");
        testFile1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file1 content");
        testFile2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "file2 content");
        cmisAPI.authenticateUser(testUser1).usingUserHome()
                .createFile(testFile1).assertThat().existsInRepo()
                .createFile(testFile2).assertThat().existsInRepo();

        STEP("3. U1 verifies the list of available process definitions using REST");
        RestProcessDefinitionModelsCollection procDefinitions = restAPI.authenticateUser(testUser1).withWorkflowAPI().getAllProcessDefinitions();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        procDefinitions.assertThat().entriesListContains("key", "activitiReview");


        STEP("4. U1 creates a workflow of type 'activitiParallelGroupReview' with the documents created above and assign it to u2 using REST");
        processModel = restAPI.authenticateUser(testUser1).withWorkflowAPI()
                .addProcess("activitiReview", testUser2, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        restAPI.withWorkflowAPI().usingProcess(processModel).addProcessItem(testFile2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("5. U1 adds a new process variable using REST");
        RestProcessVariableModel variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");

        RestProcessVariableModel processVariable = restAPI.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        processVariable.assertThat().field("name").is(variableModel.getName())
                .and().field("type").is(variableModel.getType())
                .and().field("value").is(variableModel.getValue());

        STEP("6. U2 gets the process variables using REST");
        RestProcessVariableCollection processVariables = restAPI.authenticateUser(testUser2).withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        processVariables.getProcessVariableByName(processVariable.getName())
                .assertThat().field("type").is(processVariable.getType())
                .assertThat().field("value").is(processVariable.getValue());

        processVariables.getProcessVariableByName("bpm_assignee")
                .assertThat().field("type").is("cm:person")
                .assertThat().field("value").is(testUser2.getUsername());

        processVariables.getProcessVariableByName("bpm_priority")
                .assertThat().field("type").is("d:int")
                .assertThat().field("value").is(CMISUtil.Priority.Normal.getLevel());

        processVariables.getProcessVariableByName("bpm_sendEMailNotifications")
                .assertThat().field("type").is("d:boolean")
                .assertThat().field("value").is(false);

        processVariables.getProcessVariableByName("initiator")
                .assertThat().field("type").is("d:noderef")
                .assertThat().field("value").is(testUser1.getUsername());

        processVariables.getProcessVariableByName("wf_reviewOutcome")
                .assertThat().field("type").is("d:text")
                .assertThat().field("value").is("Reject");

        STEP("7. U2 closes the task using REST");
        RestTaskModel processTask = restAPI.withWorkflowAPI().usingProcess(processModel).getProcessTasks().getTaskModelByAssignee(testUser2);
        restAPI.withParams("select=state").withWorkflowAPI().usingTask(processTask).updateTask("completed");
        restAPI.assertStatusCodeIs(HttpStatus.OK);

        STEP("8. U1 deletes the new added process variable");
        restAPI.authenticateUser(testUser1).withWorkflowAPI().usingProcess(processModel).deleteProcessVariable(processVariable);
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        STEP("9. U1 starts new 'activitiReview' process. Verify the process doesn't have the process variable added above.");
        processModel = restAPI.withWorkflowAPI().addProcess("activitiReview", testUser2, false, CMISUtil.Priority.Normal);
        restAPI.withWorkflowAPI().usingProcess(processModel).getProcessVariables().assertThat()
                .entriesListDoesNotContain("name", processVariable.getName());
    }

    /**
     * Scenario 97
     * 1. Using CMIS create 2 public sites: s1, s2.
     * 2. Using CMIS create test users: u1 and u2.
     * 3. Using RestAPI add u1 to s1 and s2, u2 to s2.
     * 4. Using RestAPI add all sites to favorites for every user.
     * 5. Using RestAPI get preferences and check that favorited sites are listed.
     * 6. Using RestAPI, u1 removes site1 from favorites, u2 removes site2 from favorites.
     * 7. Using RestAPI get preferences and check that favorited sites are listed. Removed sites are not listed.
     * 8. Using FTP U1 creates file1 in s1 document library and adds it to favorites using RestAPI. Same for u2 and s2
     * 9. Using RestAPI U1 and U2 get preferences. Files added to favorites are listed.
     * 10. Using WebDAV rename file1 and file2.
     * 11. Using RestAPI U1 and U2 get preferences. Files renamed are listed.
     * 12. Using FTP delete files.
     * 13. Using CMIS delete site1.
     * 14. Using RestAPI get preferences. Files and sites deleted are not listed anymore as favorites.
     */

    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL})
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can get preferences of sites and documents which were updated or deleted")
    @Bug(id = "ACE-5769")
    public void getUserPreferencesForSitesAndFiles() throws Exception
    {
        RestPreferenceModelsCollection restPreferenceModelsCollection;

        STEP("1. Using CMIS create 2 public sites: s1, s2.");
        SiteModel testSitePublic1 = dataSite.usingUser(testUser1).createPublicRandomSite();
        SiteModel testSitePublic2 = dataSite.usingUser(testUser1).createPublicRandomSite();

        STEP("2. Using CMIS create test users: u1 and u2.");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("3. Using RestAPI add u1 to s1 and s2, u2 to s2.");
        dataUser.addUserToSite(testUser1, testSitePublic1, UserRole.SiteManager);
        dataUser.addUserToSite(testUser1, testSitePublic2, UserRole.SiteCollaborator);
        dataUser.addUserToSite(testUser2, testSitePublic2, UserRole.SiteManager);

        STEP("4. Using RestAPI add all sites to favorites for every user.");
        restAPI.authenticateUser(testUser1).withCoreAPI().usingAuthUser().addSiteToFavorites(testSitePublic1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        restAPI.withCoreAPI().usingAuthUser().addSiteToFavorites(testSitePublic2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().addSiteToFavorites(testSitePublic1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        restAPI.withCoreAPI().usingAuthUser().addSiteToFavorites(testSitePublic2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("5. Using RestAPI get preferences and check that favorited sites are listed.");
        restPreferenceModelsCollection = restAPI.authenticateUser(testUser1).withCoreAPI().usingAuthUser().getPersonPreferences();
        restPreferenceModelsCollection.assertThat().paginationField("count").is("4");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListContains("value", "true");

        restPreferenceModelsCollection = restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferences();
        restPreferenceModelsCollection.assertThat().paginationField("count").is("4");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListContains("value", "true");

        STEP("6. Using RestAPI, u1 removes site1 from favorites, u2 removes site2 from favorites.");
        restAPI.authenticateUser(testUser1).withCoreAPI().usingAuthUser().removeFavoriteSite(testSitePublic1);
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().removeFavoriteSite(testSitePublic2);
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        STEP("7. Using RestAPI get preferences and check that favorited sites are listed. Removed sites are not listed.");
        restPreferenceModelsCollection = restAPI.authenticateUser(testUser1).withCoreAPI().usingAuthUser().getPersonPreferences();
        restPreferenceModelsCollection.assertThat().paginationField("count").is("3");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListContains("value", "true");

        restPreferenceModelsCollection = restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferences();
        restPreferenceModelsCollection.assertThat().paginationField("count").is("3");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListContains("value", "true");

        STEP("8. Using FTP U1 creates file1 in s1 document library and adds it to favorites using RestAPI. Same for u2 and s2");
        FileModel file1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        FileModel file2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);

        ftpProtocol.authenticateUser(testUser1).usingSite(testSitePublic1).createFile(file1);
        restAPI.authenticateUser(testUser1).withCoreAPI().usingAuthUser().addFileToFavorites(file1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        ftpProtocol.authenticateUser(testUser2).usingSite(testSitePublic2).createFile(file2);
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().addFileToFavorites(file2);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("9. Using RestAPI U1 and U2 get preferences. Files added to favorites are listed.");
        restPreferenceModelsCollection = restAPI.authenticateUser(testUser1).withCoreAPI().usingAuthUser().getPersonPreferences();
        restPreferenceModelsCollection.assertThat().paginationField("count").is("5");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListContains("id", "org.alfresco.ext.documents.favourites.workspace://SpacesStore/" + file1.getNodeRef() +".createdAt")
                .and().entriesListContains("value", "workspace://SpacesStore/" + file1.getNodeRef())
                .and().entriesListContains("value", "true");

        restPreferenceModelsCollection = restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferences();
        restPreferenceModelsCollection.assertThat().paginationField("count").is("5");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListContains("id", "org.alfresco.ext.documents.favourites.workspace://SpacesStore/" + file2.getNodeRef() +".createdAt")
                .and().entriesListContains("value", "workspace://SpacesStore/" + file2.getNodeRef())
                .and().entriesListContains("value", "true");

        STEP("10. Using WebDAV rename file1 and file2.");
        webDavProtocol.authenticateUser(testUser1).usingResource(file1).rename(file1.getName()+ "-updated-file1");
        webDavProtocol.authenticateUser(testUser2).usingResource(file2).rename(file2.getName()+ "-updated-file2");

        STEP("11. Using RestAPI U1 and U2 get preferences. Files renamed are listed.");
        restPreferenceModelsCollection = restAPI.authenticateUser(testUser1).withCoreAPI().usingAuthUser().getPersonPreferences();
        restPreferenceModelsCollection.assertThat().paginationField("count").is("5");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListContains("id", "org.alfresco.ext.documents.favourites.workspace://SpacesStore/" + file1.getNodeRef() +".createdAt")
                .and().entriesListContains("value", "workspace://SpacesStore/" + file1.getNodeRef())
                .and().entriesListContains("value", "true");

        restPreferenceModelsCollection = restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferences();
        restPreferenceModelsCollection.assertThat().paginationField("count").is("5");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListContains("id", "org.alfresco.ext.documents.favourites.workspace://SpacesStore/" + file2.getNodeRef() +".createdAt")
                .and().entriesListContains("value", "workspace://SpacesStore/" + file2.getNodeRef())
                .and().entriesListContains("value", "true");

        STEP("12. Using FTP delete files.");
        ftpProtocol.authenticateUser(testUser1).usingResource(file1).delete();
        ftpProtocol.authenticateUser(testUser2).usingResource(file2).delete();

        STEP("13. Using CMIS delete site1.");
        dataSite.usingUser(admin).deleteSite(testSitePublic1);

        STEP("14. Using RestAPI get preferences. Files and sites deleted are not listed anymore as favorites.");
        restPreferenceModelsCollection = restAPI.authenticateUser(testUser1).withCoreAPI().usingAuthUser().getPersonPreferences();
        restPreferenceModelsCollection.assertThat().paginationField("count").is("2");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListContains("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListContains("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic2.getId()))
                .and().entriesListDoesNotContain("id", "org.alfresco.ext.documents.favourites.workspace://SpacesStore/" + file1.getNodeRef() +".createdAt")
                .and().entriesListDoesNotContain("value", "workspace://SpacesStore/" + file1.getNodeRef())
                .and().entriesListContains("value", "true");

        restPreferenceModelsCollection = restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferences();
        restPreferenceModelsCollection.assertThat().paginationField("count").is("2");
        restPreferenceModelsCollection.assertThat().entriesListIsNotEmpty()
                .and().entriesListDoesNotContain("id", String.format(PreferenceName.EXT_SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListDoesNotContain("id", String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic1.getId()))
                .and().entriesListDoesNotContain("id", "org.alfresco.ext.documents.favourites.workspace://SpacesStore/" + file2.getNodeRef() +".createdAt")
                .and().entriesListDoesNotContain("value", "workspace://SpacesStore/" + file2.getNodeRef())
                .and().entriesListContains("value", "true");
    }
    
    /**
     * Scenario 101
     * 1. Using CMIS create test users: u1 and u2
     * 2. U1 creates public test site U1 creates a public test site using CMIS
     * 3. U1 adds U2 as site member with manager role using RestAPI
     * 4. U2 adds site to favorites using RestAPI
     * 5. U2 gets preference 'org.alfresco.ext.sites.favourites.testsite.createdAt' using RestAPI
     * 6. U2 remove site from favorites using RestAPI
     * 7. U2 gets preference 'org.alfresco.ext.sites.favourites.testsite.createdAt' using RestAPI
     * 8. U2 creates file1 in test site's document library using FTP
     * 9. U2 gets preference 'org.alfresco.ext.documents.favourites.workspace://SpacesStore/<guid>.createdAt' using RestAPI
     * 10. U2 removes file1 from favorites using RestAPI
     * 11. U2 gets preference 'org.alfresco.share.documents.favourite' using RestAPI
     * 12. U2 renames file1 to file-updated with WebDAV
     * 13. U2 gets preference 'org.alfresco.ext.documents.favourites.workspace://SpacesStore/<guid>.createdAt' using RestAPI
     * 14. U2 deletes file-updated with CMIS
     * 15. U2 gets preference 'org.alfresco.ext.documents.favourites.workspace://SpacesStore/<guid>.createdAt' using RestAPI
     * 16. U2 adds site to favorites with RestAPI and U1 deletes site1 with CMIS
     * 17. U2 gets preference 'org.alfresco.ext.sites.favourites.site1.createdAt' using RestAPI
     */
    @Bug(id = "ACE-5769")
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.FULL})
    @TestRail(section = { TestGroup.INTEGRATION, TestGroup.PREFERENCES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user can get preference of sites and documents which were updated or deleted")
    public void getPreferenceForSiteAndFiles() throws Exception
    {
        STEP("1. Using CMIS create 2 test users: u1 and u2");
        testUser1 = dataUser.createRandomTestUser();
        testUser2 = dataUser.createRandomTestUser();

        STEP("2. U1 creates public test site U1 creates a public test site using CMIS");
        SiteModel testSitePublic = dataSite.usingUser(testUser1).createPublicRandomSite();
        
        STEP("3. U1 adds U2 as site member with manager role using RestAPI");
        dataUser.usingUser(testUser1).addUserToSite(testUser2, testSitePublic, UserRole.SiteManager);
        testUser2.setUserRole(UserRole.SiteManager);

        STEP("4. U2 adds site to favorites using RestAPI");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().addSiteToFavorites(testSitePublic);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        
        STEP("5. U2 gets preference 'org.alfresco.ext.sites.favourites.testsite.createdAt' using RestAPI");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic.getId()));
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        
        STEP("6. U2 remove site from favorites using RestAPI");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().removeFavoriteSite(testSitePublic);
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        STEP("7. U2 gets preference 'org.alfresco.ext.sites.favourites.testsite.createdAt' using RestAPI");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic.getId()));
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        
        STEP("8. U2 creates file1 in test site's document library using FTP and adds it to favorites using RestAPI");
        FileModel file1 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN);
        ftpProtocol.authenticateUser(testUser2).usingSite(testSitePublic).createFile(file1);
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().addFileToFavorites(file1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        
        STEP("9. U2 gets preference 'org.alfresco.ext.documents.favourites.workspace://SpacesStore/<guid>.createdAt' using RestAPI");
        RestPreferenceModel preference = restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferenceInformation(PreferenceName.DOCUMENTS_FAVORITES_PREFIX.toString());
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        preference.assertThat().field("value").is("workspace://SpacesStore/" + file1.getNodeRefWithoutVersion());
        
        STEP("10. U2 removes file1 from favorites using RestAPI");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().deleteFileFromFavorites(file1);
        restAPI.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        STEP("11. U2 gets preference 'org.alfresco.share.documents.favourite' using RestAPI");
        preference = restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferenceInformation(PreferenceName.DOCUMENTS_FAVORITES_PREFIX.toString());
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        preference.assertThat().field("value").isNull();
        
        STEP("12. U2 adds file1 to favorites using RestAPI and renames file1 to file-updated with WebDAV");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().addFileToFavorites(file1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        webDavProtocol.authenticateUser(testUser2).usingResource(file1).rename(file1.getName()+ "-updated");
        
        STEP("13. U2 gets preference 'org.alfresco.ext.documents.favourites.workspace://SpacesStore/<guid>.createdAt' using RestAPI");
        preference = restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferenceInformation(PreferenceName.DOCUMENTS_FAVORITES_PREFIX.toString());
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        preference.assertThat().field("value").is("workspace://SpacesStore/" + file1.getNodeRefWithoutVersion());
        
        STEP("14. U2 adds file1 to favorites using RestAPI and deletes file-updated with CMIS");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().addFileToFavorites(file1);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        cmisAPI.authenticateUser(testUser2).usingResource(file1).delete();
        
        STEP("15. U2 gets preference 'org.alfresco.ext.documents.favourites.workspace://SpacesStore/<guid>.createdAt' using RestAPI");
        preference = restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferenceInformation(PreferenceName.DOCUMENTS_FAVORITES_PREFIX.toString());
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        preference.assertThat().field("value").isNull();
        
        STEP("16. U2 adds site to favorites with RestAPI and U1 deletes site1 with CMIS");
        restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().addSiteToFavorites(testSitePublic);
        restAPI.assertStatusCodeIs(HttpStatus.CREATED);
        dataSite.usingUser(testUser1).deleteSite(testSitePublic);
        
        STEP("17. U2 gets preference 'org.alfresco.ext.sites.favourites.site1.createdAt' using RestAPI");
        preference = restAPI.authenticateUser(testUser2).withCoreAPI().usingAuthUser().getPersonPreferenceInformation(String.format(PreferenceName.SITES_FAVORITES_PREFIX.toString(), testSitePublic.getId()));
        restAPI.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        preference.assertThat().field("value").isNull();
    }
    
    @Bug(id = "REPO-2419", status = Status.FIXED)
    @Test(groups = { TestGroup.PROTOCOLS, TestGroup.WEBDAV, TestGroup.FULL })
    @TestRail(section = { TestGroup.PROTOCOLS, TestGroup.WEBDAV }, executionType = ExecutionType.REGRESSION, description = "Verify that the version of a new file uploaded through WebDAV is 1.0 with the real content size.")
    public void uploadedFileThroughWebdavHasFirstVersion() throws Exception
    {
        UserModel managerUser = dataUser.createRandomTestUser();
        SiteModel testSite = dataSite.usingUser(managerUser).createPublicRandomSite();
        
        STEP("0. Verify versionable aspect is set for all contents using webscript");
        String fileCreationWebScript = "alfresco/s/api/classes/cm_content";

        RestAssured.basePath = "";
        restAPI.configureRequestSpec().setBasePath(RestAssured.basePath);
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, fileCreationWebScript);
        RestResponse response = restAPI.authenticateUser(admin).process(request);
        Assert.assertEquals(response.getResponse().getStatusCode(), HttpStatus.OK.value());
        try 
        {
            response.assertThat().body(containsString("cm:versionable"));
            response.assertThat().body("defaultAspects.'cm:versionable'.title", equalTo("Versionable"));
        }
        catch(AssertionError ae)
        {
            throw new SkipException("Skipping this test because the versionable aspect is not applied. Please add "
                    + "the versionable aspect to all content in contentModel.xml and run the test again.");
        }
        
        STEP("1. Upload a local file on a new folder using WebDAV protocol");
        FolderModel folder = dataContent.usingUser(managerUser).usingSite(testSite).createFolder();
        File fileToUpload = Utility.getTestResourceFile("shared-resources/testdata/nonemptyupload.txt");
        webDavProtocol.authenticateUser(managerUser).usingSite(testSite).usingResource(folder).uploadFile(fileToUpload);
        FileModel file = new FileModel(fileToUpload.getName());

        STEP("2. Verify version is exactly 1.0 and the size is the same as the local file");
        dataContent.usingResource(file).assertContentVersionIs("1.0");
        dataContent.usingResource(file).assertContentSizeIs(19);
    }
}
