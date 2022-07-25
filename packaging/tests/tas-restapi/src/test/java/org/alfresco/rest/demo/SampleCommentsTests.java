/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
    public void dataPreparation()
    {
        userModel = dataUser.getAdminUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        folderModel = dataContent.usingUser(userModel).usingSite(siteModel).createFolder();
        restClient.authenticateUser(userModel);        
        document = dataContent.usingUser(userModel).usingResource(folderModel).createContent(DocumentType.TEXT_PLAIN);
    }

    @Test(groups = { "demo" })
    public void admiShouldAddComment() throws JsonToModelConversionException
    {
        restClient.withCoreAPI().usingResource(document).addComment("This is a new comment");
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }

    @Test(groups = { "demo" })
    public void admiShouldRetrieveComments()
    {
        restClient.withCoreAPI().usingResource(document).getNodeComments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { "demo" })
    public void adminShouldUpdateComment() throws JsonToModelConversionException
    {
        RestCommentModel commentModel = restClient.withCoreAPI().usingResource(document).addComment("This is a new comment");

        restClient.withCoreAPI().usingResource(document).updateComment(commentModel, "This is the updated comment with Collaborator user")
                    .assertThat().field("content").is("This is the updated comment with Collaborator user");
    }

}
