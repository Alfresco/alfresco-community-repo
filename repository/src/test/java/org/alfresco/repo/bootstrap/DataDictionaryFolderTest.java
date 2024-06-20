/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.bootstrap;


import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.WellKnownNodes;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class DataDictionaryFolderTest extends BaseSpringTest
{
    @ClassRule
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();

    @Rule
    public WellKnownNodes wellKnownNodes = new WellKnownNodes(APP_CONTEXT_INIT);

    private NodeService nodeService;
    protected Repository repositoryHelper;

    @Before
    public void before()
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) this.applicationContext.getBean("ServiceRegistry");
        this.nodeService = serviceRegistry.getNodeService();
    }

    @Test
    public void testDataDictionaryFolderIsUndeletableAndUnmovable()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        // get the company_home
        NodeRef companyHomeRef = wellKnownNodes.getCompanyHome();
        // get the Data Dictionary
        NodeRef dataDictionaryRef = nodeService.getChildByName(companyHomeRef, ContentModel.ASSOC_CONTAINS,
                "Data Dictionary");

        List<ChildAssociationRef> chilAssocsList = nodeService.getChildAssocs(dataDictionaryRef);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        for (ChildAssociationRef childAssociationRef : chilAssocsList)
        {
            NodeRef childNodeRef = childAssociationRef.getChildRef();
            assertTrue(nodeService.hasAspect(childNodeRef, ContentModel.ASPECT_UNDELETABLE));
            assertTrue(nodeService.hasAspect(childNodeRef, ContentModel.ASPECT_UNMOVABLE));
        }
    }
}
