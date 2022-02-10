/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.imap;

import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.MockAuthenticationUtilHelper;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.extensions.webscripts.GUID;

/**
 * Unit test for ExtendedImapServiceImpl
 * @author Ana Bozianu
 */
public class ExtendedImapServiceImplUnitTest
{
    /* service mocks */
    private @Mock NodeService mockedNodeService;
    private @Mock BehaviourFilter mockedPolicyBehaviourFilter;
    private @Mock DictionaryService mockedDictionaryService;
    private @Mock AuthenticationUtil mockedAuthenticationUtil;

    /* test instance of extended IMAP service implementation */
    private @InjectMocks ExtendedImapServiceImpl extendedImapServiceImpl;

    /* test data */
    private NodeRef rmSite = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
    private final String RM_SITE_NAME = "RM";
    private NodeRef rmFilePlan = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
    private final String RM_FILEPLAN_NAME = "fileplan"; 
    private NodeRef rmCategory = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
    private final String RM_CATEGORY_NAME = "C1"; 

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        // setup mocked authentication util
        MockAuthenticationUtilHelper.setup(mockedAuthenticationUtil);

        // node names
        when(mockedNodeService.getProperty(rmSite, ContentModel.PROP_NAME)).thenReturn(RM_SITE_NAME);
        when(mockedNodeService.getProperty(rmCategory, ContentModel.PROP_NAME)).thenReturn(RM_CATEGORY_NAME);
        when(mockedNodeService.getProperty(rmFilePlan, ContentModel.PROP_NAME)).thenReturn(RM_FILEPLAN_NAME);

        // node types
        when(mockedNodeService.getType(rmSite)).thenReturn(RecordsManagementModel.TYPE_RM_SITE);
        when(mockedNodeService.getType(rmFilePlan)).thenReturn(RecordsManagementModel.TYPE_FILE_PLAN);
        when(mockedNodeService.getType(rmCategory)).thenReturn(RecordsManagementModel.TYPE_RECORD_CATEGORY);

        // type hierarchy
        when(mockedDictionaryService.isSubClass(RecordsManagementModel.TYPE_RM_SITE, SiteModel.TYPE_SITE)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(RecordsManagementModel.TYPE_FILE_PLAN, SiteModel.TYPE_SITE)).thenReturn(false);
        when(mockedDictionaryService.isSubClass(RecordsManagementModel.TYPE_RECORD_CATEGORY, SiteModel.TYPE_SITE)).thenReturn(false);
        
        // node associations
        ChildAssociationRef filePlanParentAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, rmSite, QName.createQName(GUID.generate()), rmFilePlan);
        when(mockedNodeService.getPrimaryParent(rmFilePlan)).thenReturn(filePlanParentAssoc);
        
        ChildAssociationRef categoryParentAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, rmFilePlan, QName.createQName(GUID.generate()), rmCategory);
        when(mockedNodeService.getPrimaryParent(rmCategory)).thenReturn(categoryParentAssoc);
    }

    /**
     * given the method is called on the rm site node 
     * check if the result is the site name
     */
    @Test
    public void testGetPathFromRMSite()
    {
        String rmSitePath = extendedImapServiceImpl.getPathFromSites(rmSite);
        Assert.assertEquals("Incorrect return value", RM_SITE_NAME.toLowerCase(), rmSitePath);
    }

    /**
     * given the method is called on a rm category
     * check if the result is the full path relative to the rm site
     */
    @Test
    public void testGetPathFromRMCategory()
    {
        String rmCategoryPath = extendedImapServiceImpl.getPathFromSites(rmCategory);
        Assert.assertEquals("Incorrect return value", (RM_SITE_NAME + "/" + RM_FILEPLAN_NAME + "/" + RM_CATEGORY_NAME).toLowerCase(), rmCategoryPath);
    }
}
