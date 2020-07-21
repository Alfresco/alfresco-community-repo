package org.alfresco.rest.downloads;

import javax.json.JsonObject;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.model.RestDownloadsModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DownloadsTests extends RestTest
{
    private UserModel adminModel;
    private FileModel document, document1;
    private FolderModel folder;
    private SiteModel siteModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {  
        adminModel = dataUser.getAdminUser();
        siteModel = dataSite.usingUser(adminModel).createPrivateRandomSite();
        folder = dataContent.usingUser(adminModel).usingSite(siteModel).createFolder();
        document = dataContent.usingSite(siteModel).usingUser(adminModel).createContent(DocumentType.TEXT_PLAIN);

        // Create document1 based on existing resource
        FileModel newFile = FileModel.getFileModelBasedOnTestDataFile("larger.pdf");
        newFile.setName("larger.pdf");
        document1 = dataContent.usingUser(adminModel).usingResource(folder).createContent(newFile);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.DOWNLOADS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.DOWNLOADS }, executionType = ExecutionType.SANITY,
            description = "Sanity tests for GET /downloads/{downloadId} and POST /downloads")
    public void createDownloadNodeAndGetInfo() throws Exception
    {
        // POST /downloads
        JsonObject postBody = JsonBodyGenerator.defineJSON()
                           .add("nodeIds", JsonBodyGenerator.defineJSONArray().add(document.getNodeRefWithoutVersion()))
                           .build();
        RestDownloadsModel downloadModel = restClient.authenticateUser(adminModel).withCoreAPI().usingDownloads().createDownload(postBody.toString());
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);
        downloadModel.assertThat().fieldsCount().is(6).and()
                                 .field("id").isNotEmpty();

        // GET /downloads/{downloadId}
        Utility.sleep(500, 15000, () -> {
            RestDownloadsModel downloadModel1 = restClient.authenticateUser(adminModel).withCoreAPI().usingDownloads(downloadModel).getDownload();
            restClient.assertStatusCodeIs(HttpStatus.OK);

            downloadModel1.assertThat().fieldsCount().is(6).and()
                                      .field("id").is(downloadModel.getId()).and()
                                      .field("filesAdded").isGreaterThan(0).and()
                                      .field("bytesAdded").isGreaterThan(0).and()
                                      .field("totalBytes").isGreaterThan(0).and()
                                      .field("totalFiles").isGreaterThan(0).and()
                                      .field("status").is("DONE");
        });

        // Check that download node has content
        restClient.authenticateUser(adminModel).withCoreAPI().usingNode().getNodeContent(downloadModel.getId());

        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.assertHeaderValueContains("Content-Type","application/octet-stream;charset=UTF-8");
        restClient.assertHeaderValueContains("Content-disposition",".zip");
        Assert.assertTrue(restClient.onResponse().getResponse().body().asInputStream().available() > 0);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.DOWNLOADS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.DOWNLOADS }, executionType = ExecutionType.SANITY,
            description = "Sanity tests for DELETE /downloads/{downloadId}")
    public void cancelDownloadNode() throws Exception
    {
        // POST /downloads
        String postBody = JsonBodyGenerator.defineJSON()
                           .add("nodeIds", JsonBodyGenerator.defineJSONArray().add(document1.getNodeRefWithoutVersion()))
                           .build().toString();
        RestDownloadsModel downloadModel = restClient.authenticateUser(adminModel).withCoreAPI().usingDownloads().createDownload(postBody);

        /*
         * DELETE /downloads/{downloadId}
         * Download canceling is async and it works only if download status is not "DONE"
         * To have this case, a large pdf file was used that takes a longer time to download
         */
        restClient.authenticateUser(adminModel).withCoreAPI().usingDownloads(downloadModel).cancelDownload();
        restClient.assertStatusCodeIs(HttpStatus.ACCEPTED);

        // GET /downloads/{downloadId} to check that the download is canceled
        Utility.sleep(500, 10000, () -> {
            RestDownloadsModel downloadModel1 = restClient.authenticateUser(adminModel).withCoreAPI().usingDownloads(downloadModel).getDownload();
            restClient.assertStatusCodeIs(HttpStatus.OK);
            downloadModel1.assertThat().field("status").is("CANCELLED");
        });
    }
}
