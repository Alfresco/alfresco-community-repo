package org.alfresco.rest.tags;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.REQUIRE_SOLR})
public class TagsDataPrep extends RestTest
{

    protected static UserModel adminUserModel;
    protected static UserModel userModel;
    protected static ListUserWithRoles usersWithRoles;
    protected static SiteModel siteModel;
    protected static FileModel document;
    protected static FolderModel folder;
    protected static String documentTagValue, documentTagValue2, folderTagValue;
    protected static RestTagModel documentTag, documentTag2, folderTag, returnedModel;
    protected static RestTagModelsCollection returnedCollection;
    private static boolean isInitialized = false;

    public void init() throws Exception
    {
        if(!isInitialized)
        {
            isInitialized = true;
            initialization();
        }
    }

    public void initialization() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        //Create public site
        siteModel = dataSite.usingUser(adminUserModel).createPublicRandomSite();
        usersWithRoles = dataUser.usingAdmin().addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer, UserRole.SiteContributor);
        document = dataContent.usingUser(adminUserModel).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        folder = dataContent.usingUser(adminUserModel).usingSite(siteModel).createFolder();

        documentTagValue = RandomData.getRandomName("tag");
        documentTagValue2 = RandomData.getRandomName("tag");
        folderTagValue = RandomData.getRandomName("tag");

        restClient.authenticateUser(adminUserModel);
        documentTag = restClient.withCoreAPI().usingResource(document).addTag(documentTagValue);
        documentTag2 = restClient.withCoreAPI().usingResource(document).addTag(documentTagValue2);
        folderTag = restClient.withCoreAPI().usingResource(folder).addTag(folderTagValue);

        // Allow indexing to complete.
        Utility.sleep(1000, 60000, () ->
            {
                returnedCollection = restClient.withParams("maxItems=10000").withCoreAPI().getTags();
                returnedCollection.assertThat().entriesListContains("tag", documentTagValue.toLowerCase())
                                  .and().entriesListContains("tag", documentTagValue2.toLowerCase())
                                  .and().entriesListContains("tag", folderTagValue.toLowerCase());
            });

    }
}
