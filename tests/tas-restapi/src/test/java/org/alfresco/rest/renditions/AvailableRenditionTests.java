package org.alfresco.rest.renditions;

import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestRenditionInfoModel;
import org.alfresco.rest.model.RestRenditionInfoModelCollection;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Sanity check for renditions through REST API.<br>
 * Tests upload files and then request renditions for those files. <br>
 * Renditions are requested based on supported renditions according to GET '/nodes/{nodeId}/renditions'. <br>
 */
@Test(groups = {TestGroup.RENDITIONS})
public class AvailableRenditionTests extends RenditionIntegrationTests
{
    /** List of parameters used as an input to supportedRenditionTest - fileName, nodeId, renditionId, expectedMimeType **/
    private List<Object[]> renditionsToTest;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        super.dataPreparation();

        // Upload files and get available renditions for each file
        List<String> toTest = Arrays.asList("doc", "xls", "ppt", "docx", "xlsx", "pptx", "msg", "pdf", "png", "gif", "jpg");
        List<Object[]> renditionsForFiles = new LinkedList<>();
        for (String extensions : toTest)
        {
            String sourceFile = "quick/quick." + extensions;
            renditionsForFiles.addAll(uploadFileAndGetAvailableRenditions(sourceFile));
        }

        renditionsToTest = Collections.unmodifiableList(renditionsForFiles);
    }

    /**
     * Upload a source file and get all supported renditions for it.
     */
    private List<Object[]> uploadFileAndGetAvailableRenditions(String sourceFile) throws Exception
    {

        // Create folder & upload file
        RestNodeModel fileNode = uploadFile(sourceFile);
        FileModel file = new FileModel();
        file.setNodeRef(fileNode.getId());

        List<Object[]> renditionsForFile = new LinkedList<>();

        // Get supported renditions
        RestRenditionInfoModelCollection renditionsInfo = restClient.withCoreAPI().usingNode(file).getNodeRenditionsInfo();
        for (RestRenditionInfoModel m : renditionsInfo.getEntries())
        {
            RestRenditionInfoModel renditionInfo = m.onModel();
            String renditionId = renditionInfo.getId();
            String targetMimeType = renditionInfo.getContent().getMimeType();

            renditionsForFile.add(new Object[]{sourceFile, fileNode.getId(), renditionId, targetMimeType});

        }

        return renditionsForFile;
    }

    /**
     *  Check that a particular rendition can be created for the file and that it has the expected Mime type
     */
    @Test(dataProvider = "RenditionTestDataProvider", groups = {TestGroup.REST_API, TestGroup.RENDITIONS, TestGroup.SANITY})
    @TestRail(section = { TestGroup.REST_API, TestGroup.RENDITIONS }, executionType = ExecutionType.SANITY,
            description = "Verify renditions created for a selection of test files via POST nodes/{nodeId}/renditions")
    public void supportedRenditionTest(String fileName, String nodeId, String renditionId, String expectedMimeType) throws Exception
    {
        checkRendition(fileName, nodeId, renditionId, expectedMimeType);
    }

    @DataProvider(name = "RenditionTestDataProvider")
    protected Iterator<Object[]> renditionTestDataProvider() throws Exception
    {
        return renditionsToTest.iterator();
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.RENDITIONS, TestGroup.SANITY})
    @TestRail(section = { TestGroup.REST_API, TestGroup.RENDITIONS }, executionType = ExecutionType.SANITY,
            description = "Verify that there are some available renditions.")
    public void renditionsAvailableTest()
    {
        Assert.assertFalse(renditionsToTest.isEmpty(), "No available renditions were reported for any of the test files.");
    }

    @Test(dataProvider = "UnsupportedRenditionTestDataProvider",groups = {TestGroup.REST_API, TestGroup.RENDITIONS, TestGroup.SANITY})
    @TestRail(section = { TestGroup.REST_API, TestGroup.RENDITIONS }, executionType = ExecutionType.SANITY,
            description = "Verify that requests for unsupported renditions return 400 ")
    public void unsupportedRenditionTest(String sourceFile, String renditionId) throws Exception
    {
        RestNodeModel fileNode = uploadFile(sourceFile);

        // 2. Request rendition of the file using RESTAPI
        FileModel file = new FileModel(sourceFile);
        file.setNodeRef(fileNode.getId());
        restClient.withCoreAPI().usingNode(file).createNodeRendition(renditionId);

        Assert.assertEquals(Integer.valueOf(restClient.getStatusCode()).intValue(), HttpStatus.BAD_REQUEST.value(),
                "Expected to see the rendition rejected. [" + sourceFile + ", " + renditionId + "] [source file, rendition ID] ");
    }

    @DataProvider(name = "UnsupportedRenditionTestDataProvider")
    protected Iterator<Object[]> unsupportedRenditionTestDataProvider() throws Exception
    {
        String renditionId = "pdf";

        List<Object[]> toTest = new LinkedList<>();
        toTest.add(new Object[]{"quick/quick.png", renditionId});
        toTest.add(new Object[]{"quick/quick.gif", renditionId});
        toTest.add(new Object[]{"quick/quick.jpg", renditionId});

        return toTest.iterator();
    }
}

