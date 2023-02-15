/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.tags;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.TestGroup;
import org.testng.annotations.Test;

import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

public class DeleteTagsTests extends TagsDataPrep
{
    /**
     * Check we can delete a tag by its id.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteTag()
    {
        STEP("Create a tag assigned to a document and send a request to delete it.");
        document = dataContent.usingUser(adminUserModel).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestTagModel aTag = createTagForDocument(document);

        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingTag(aTag).deleteTag();
        restClient.assertStatusCodeIs(NO_CONTENT);

        STEP("Ensure that the tag has been deleted by sending a GET request and receiving 404.");
        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().getTag(aTag);
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /**
     * Attempt to delete a tag as a site manager and receive 403 error.
     * Other user roles have fewer permissions than a SiteManager and thus would also be forbidden from deleting a tag.
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteTagAsSiteManager_andFail()
    {
        STEP("Create a tag assigned to a document and attempt to delete as a site manager");
        document = dataContent.usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        RestTagModel aTag = createTagForDocument(document);

        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingTag(aTag).deleteTag();
        restClient.assertStatusCodeIs(FORBIDDEN).assertLastError().containsSummary("Current user does not have permission to manage a tag");
    }

    /**
     * Check we receive 404 error when trying to delete a tag with a non-existent id
     */
    @Test(groups = {TestGroup.REST_API})
    public void testDeleteNonExistentTag()
    {
        STEP("Attempt to delete tag with non-existent id and receive 404 error");
        final String id = "non-existing-dummy-id";
        final RestTagModel tagModel = createTagModelWithId(id);

        restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingTag(tagModel).deleteTag();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }
}
