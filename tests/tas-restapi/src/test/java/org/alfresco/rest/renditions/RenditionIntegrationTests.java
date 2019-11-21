package org.alfresco.rest.renditions;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;

import org.alfresco.utility.model.UserModel;

import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

public abstract class RenditionIntegrationTests extends RestTest
{

    protected UserModel user;
    protected SiteModel site;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
    }

    /**
     * Check that a rendition can be created for the specified node id
     * @param fileName
     * @param nodeId
     * @param renditionId
     * @param expectedMimeType
     * @throws Exception
     */
    protected void checkRendition(String fileName, String nodeId, String renditionId, String expectedMimeType) throws Exception
    {
        FileModel file = new FileModel();
        file.setNodeRef(nodeId);

        // 1. Create a rendition of the file using RESTAPI
        restClient.withCoreAPI().usingNode(file).createNodeRendition(renditionId);
        Assert.assertEquals(Integer.valueOf(restClient.getStatusCode()).intValue(), HttpStatus.ACCEPTED.value(),
                "Failed to submit a request for rendition. [" + fileName+ ", " + renditionId+"] [source file, rendition ID]. ");

        // 2. Verify that a rendition of the file is created and has content using RESTAPI
        RestResponse restResponse = restClient.withCoreAPI().usingNode(file).getNodeRenditionContentUntilIsCreated(renditionId);
        Assert.assertEquals(Integer.valueOf(restClient.getStatusCode()).intValue(), HttpStatus.OK.value(),
                "Failed to produce rendition. [" + fileName+ ", " + renditionId+"] [source file, rendition ID] ");

        // 3. Check the returned content type
        Assert.assertEquals(restClient.getResponseHeaders().getValue("Content-Type"), expectedMimeType+";charset=UTF-8",
                "Rendition was created but it has the wrong Content-Type. [" + fileName+ ", " + renditionId + "] [source file, rendition ID]");


        Assert.assertTrue((restResponse.getResponse().body().asInputStream().available() > 0),
                "Rendition was created but its content is empty. [" + fileName+ ", " + renditionId+"] [source file, rendition ID] ");
    }

    /**
     * Upload a file and return its node id
     * @param sourceFile
     * @return
     * @throws Exception
     */
    protected RestNodeModel uploadFile(String sourceFile) throws Exception
    {
        FolderModel folder = FolderModel.getRandomFolderModel();
        folder = dataContent.usingUser(user).usingSite(site).createFolder(folder);
        restClient.authenticateUser(user).configureRequestSpec()
                .addMultiPart("filedata", Utility.getResourceTestDataFile(sourceFile));
        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).createNode();

        Assert.assertEquals(Integer.valueOf(restClient.getStatusCode()).intValue(), HttpStatus.CREATED.value(),
                "Failed to created a node for rendition tests using file " + sourceFile);

        return fileNode;
    }
}
