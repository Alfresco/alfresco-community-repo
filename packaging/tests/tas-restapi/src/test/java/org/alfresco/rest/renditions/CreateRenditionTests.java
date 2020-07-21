package org.alfresco.rest.renditions;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.report.Bug.Status;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Handles tests related to POST api-explorer/#!/renditions
 * @author Cristina Axinte
 *
 */
@Test(groups = {TestGroup.RENDITIONS})
public class CreateRenditionTests  extends RestTest
{
    private UserModel adminUser, user;
    private SiteModel site;
    private FileModel document;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
    }
    
    @BeforeMethod(alwaysRun = true)
    public void createDocument() throws Exception
    {
        document = dataContent.usingUser(user).usingSite(site).createContent(DocumentType.TEXT_PLAIN);
    }

    @Bug(id = "REPO-2042", description = "Should fail only on MAC OS System and Linux", status = Status.FIXED )
    @TestRail(section = { TestGroup.REST_API, TestGroup.RENDITIONS }, executionType = ExecutionType.SANITY, 
            description = "Verify admin user creates rendition with Rest API and status code is 202")
    @Test(groups = { TestGroup.REST_API, TestGroup.RENDITIONS, TestGroup.SANITY })
    public void adminCanCreateRenditionToExistingNode() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI().usingNode(document).createNodeRendition("pdf");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        
        restClient.withCoreAPI().usingNode(document).getNodeRenditionUntilIsCreated("pdf")
            .assertThat().field("status").is("CREATED");
    }
    
    @Bug(id = "REPO-2042", description = "Should fail only on MAC OS System and Linux", status = Status.FIXED )
    @TestRail(section = { TestGroup.REST_API, TestGroup.RENDITIONS }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user that created the document can also creates 'pdf' rendition for it with Rest API and status code is 202")
    @Test(groups = { TestGroup.REST_API, TestGroup.RENDITIONS, TestGroup.REGRESSION })
    public void userThatCreatedFileCanCreatePdfRenditionForIt() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(user).withCoreAPI().usingNode(document).createNodeRendition("pdf");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        
        restClient.withCoreAPI().usingNode(document).getNodeRenditionUntilIsCreated("pdf")
            .assertThat().field("status").is("CREATED");
    }
    
    @Bug(id = "REPO-2042", description = "Should fail only on MAC OS System and Linux", status = Status.FIXED )
    @TestRail(section = { TestGroup.REST_API, TestGroup.RENDITIONS }, executionType = ExecutionType.REGRESSION,
            description = "Verify user that created the document can also creates 'doclib' rendition for it with Rest API and status code is 202")
    @Test(groups = { TestGroup.REST_API, TestGroup.RENDITIONS, TestGroup.REGRESSION, TestGroup.NOT_SUPPORTED_BY_ATS })
    public void userThatCreatedFileCanCreateDoclibRenditionForIt() throws JsonToModelConversionException, Exception
    {
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        restClient.authenticateUser(user)
        .configureRequestSpec() 
        .addMultiPart("filedata", Utility.getResourceTestDataFile("my-file.tif"));

        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingResource(folder).createNode();
        restClient.assertStatusCodeIs(HttpStatus.CREATED); 
        document = new FileModel("my-file.tif");
        document.setCmisLocation(folder.getCmisLocation() + "/my-file.tif");
        document.setNodeRef(fileNode.getId());
        
        restClient.authenticateUser(user).withCoreAPI().usingNode(document).createNodeRendition("doclib");
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);

        // Renditions are async
        Utility.sleep(500, 60000, () ->
        {
            restClient.withCoreAPI().usingNode(document).getNodeRenditionUntilIsCreated("doclib")
                    .assertThat().field("status").is("CREATED");
        });
    }
}
