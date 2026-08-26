/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.template;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;

/**
 * Test for {@link TemplateNode#getShareUrl()}.
 * <p>
 * A user with access to a child folder only (not its parent) must still be able to resolve a share URL for a node in that child folder, e.g. from a 'Send email' rule template.
 */
@Category(BaseSpringTestsCategory.class)
@Transactional
public class TemplateNodeTest extends BaseAlfrescoSpringTest
{
    private PermissionService permissionService;
    private ServiceRegistry serviceRegistry;

    @Before
    public void before() throws Exception
    {
        super.before();
        this.permissionService = (PermissionService) this.applicationContext.getBean("permissionService");
        this.serviceRegistry = (ServiceRegistry) this.applicationContext.getBean("ServiceRegistry");
    }

    @Test
    public void testGetShareUrlWhenUserCannotReadParentFolder() throws Exception
    {
        String userName = "templateNodeTestUser" + GUID.generate();
        createUser(userName);

        // F1: not readable by userName
        NodeRef f1 = createNode(this.rootNodeRef, "f1", ContentModel.TYPE_FOLDER);
        this.permissionService.setInheritParentPermissions(f1, false);

        // F2: userName only has access here, not on the parent F1
        NodeRef f2 = createNode(f1, "f2", ContentModel.TYPE_FOLDER);
        this.permissionService.setInheritParentPermissions(f2, false);
        this.permissionService.setPermission(f2, userName, PermissionService.CONTRIBUTOR, true);

        String shareUrl = AuthenticationUtil.runAs(() -> {
            NodeRef doc = createNode(f2, "doc.txt", ContentModel.TYPE_CONTENT);

            TemplateNode templateNode = new TemplateNode(doc, this.serviceRegistry, null);
            try
            {
                return templateNode.getShareUrl();
            }
            catch (Exception e)
            {
                fail("getShareUrl() should not fail for a user without read access to a parent folder: " + e);
                return null;
            }
        }, userName);

        assertNotNull(shareUrl);
        assertTrue(shareUrl.contains("document-details?nodeRef="));
    }
}
