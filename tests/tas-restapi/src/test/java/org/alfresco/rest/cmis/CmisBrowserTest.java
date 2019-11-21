package org.alfresco.rest.cmis;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.utility.model.FileModel;
import org.testng.annotations.Test;

public class CmisBrowserTest extends RestTest
{    
    /*
     * @author: Paul Brodner
     * simple test for demo purposes on how to use CMIS browser binding with Rest
     */
    @Test(enabled=false)
    public void assertContentDispositionHeaderOnCmisFile() throws Exception
    {
        FileModel document = dataContent.usingUser(dataUser.getAdminUser())
                                        .usingSite(testSite).createContent(DocumentType.HTML);

        RestResponse response = restClient.authenticateUser(dataUser.getAdminUser()).withCMISApi().getRootObjectByID(document);
        response.assertThat().header("Content-Disposition", String.format("attachment; filename=%s", document.getName()));
    }

}
