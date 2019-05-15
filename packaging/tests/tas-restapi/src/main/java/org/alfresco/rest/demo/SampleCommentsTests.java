package org.alfresco.rest.demo;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestCommentModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class SampleCommentsTests extends RestTest
{    
    private UserModel userModel;
    private FolderModel folderModel;
    private SiteModel siteModel;
    private FileModel document;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.getAdminUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        restClient.authenticateUser(userModel);        
        document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
    }

    @Test(groups = { "demo" })
    public void admiShouldAddComment() throws JsonToModelConversionException, Exception
    {
        restClient.withCoreAPI().usingResource(document).addComment("This is a new comment");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { "demo" })
    public void admiShouldRetrieveComments() throws Exception
    {
        restClient.withCoreAPI().usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { "demo" })
    public void adminShouldUpdateComment() throws JsonToModelConversionException, Exception
    {
        RestCommentModel commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment");

        restClient.withCoreAPI().usingResource(document).updateComment(commentModel, "This is the updated comment with Collaborator user")
                    .assertThat().field("content").is("This is the updated comment with Collaborator user");
    }

}