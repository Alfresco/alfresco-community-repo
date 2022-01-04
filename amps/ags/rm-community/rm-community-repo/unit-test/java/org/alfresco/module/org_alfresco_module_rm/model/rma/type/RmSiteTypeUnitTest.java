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

package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import com.google.common.collect.Sets;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.module.org_alfresco_module_rm.test.util.MockAuthenticationUtilHelper;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit test for RmSiteType
 *
 * @author Silviu Dinuta
 * @since 2.6
 *
 */
public class RmSiteTypeUnitTest extends BaseUnitTest implements DOD5015Model
{
    @Mock
    private AuthenticationUtil mockAuthenticationUtil;

    @Mock
    private SiteService mockedSiteService;

    private @InjectMocks RmSiteType rmSiteType;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        MockAuthenticationUtilHelper.setup(mockAuthenticationUtil);
    }

    /**
     * Given that we try to add non allowed type to rm site,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testAddNonAcceptedTypeToRmSite()
    {
        NodeRef rmSiteNodeRef = generateNodeRef(TYPE_RM_SITE, true);

        QName type = AlfMock.generateQName();
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, type);

        ChildAssociationRef mockedChildAssoc = mock(ChildAssociationRef.class);
        when(mockedChildAssoc.getChildRef()).thenReturn(nodeRef);
        when(mockedChildAssoc.getParentRef()).thenReturn(rmSiteNodeRef);
        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        when(mockedSiteService.getSite(rmSiteNodeRef)).thenReturn(mockedSiteInfo);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add one cm:folder to rm site,
     * Then operation is successful.
     */
    @Test
    public void testAddOneFolderTypeToRmSite()
    {
        NodeRef rmSiteNodeRef = generateNodeRef(TYPE_RM_SITE, true);
        ArrayList<ChildAssociationRef> assocs = new ArrayList<>();

        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        when(mockedSiteService.getSite(rmSiteNodeRef)).thenReturn(mockedSiteInfo);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);

        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_FOLDER);
        ChildAssociationRef mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_FOLDER))).thenReturn(assocs);

        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add two cm:folder to rm site,
     * Then operation is successful.
     */
    @Test
    public void testAddTwoFolderTypeToRmSite()
    {
        NodeRef rmSiteNodeRef = generateNodeRef(TYPE_RM_SITE, true);
        ArrayList<ChildAssociationRef> assocs = new ArrayList<>();

        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        when(mockedSiteService.getSite(rmSiteNodeRef)).thenReturn(mockedSiteInfo);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);

        //create first folder
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_FOLDER);
        ChildAssociationRef mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_FOLDER))).thenReturn(assocs);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);

        //create second cm:folder
        nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_FOLDER);
        mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_FOLDER))).thenReturn(assocs);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add more than two cm:folder to rm site,
     * Then IntegrityException is thrown.
     */
    @Test
    public void testAddMoreThanTwhoFolderTypeToRmSite()
    {
        NodeRef rmSiteNodeRef = generateNodeRef(TYPE_RM_SITE, true);
        ArrayList<ChildAssociationRef> assocs = new ArrayList<>();

        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        when(mockedSiteService.getSite(rmSiteNodeRef)).thenReturn(mockedSiteInfo);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_FOLDER))).thenReturn(new ArrayList<>());

        //create first folder
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_FOLDER);
        ChildAssociationRef mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_FOLDER))).thenReturn(assocs);

        //create second cm:folder
        nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_FOLDER);
        mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_FOLDER))).thenReturn(assocs);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);

        //create third cm:folder
        nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_FOLDER);
        mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_FOLDER))).thenReturn(assocs);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add one rma:filePlan to rm site,
     * Then operation is successful.
     */
    @Test
    public void testAddOneFilePlanTypeToRmSite()
    {
        NodeRef rmSiteNodeRef = generateNodeRef(TYPE_RM_SITE, true);
        ArrayList<ChildAssociationRef> assocs = new ArrayList<>();

        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        when(mockedSiteService.getSite(rmSiteNodeRef)).thenReturn(mockedSiteInfo);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);

        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_FILE_PLAN);
        ChildAssociationRef mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_FILE_PLAN))).thenReturn(assocs);

        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add one dod:filePlan to standard rm site,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testAddDODFilePlanTypeToStandardRmSite()
    {
        NodeRef rmSiteNodeRef = generateNodeRef(TYPE_RM_SITE, true);
        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        when(mockedSiteService.getSite(rmSiteNodeRef)).thenReturn(mockedSiteInfo);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);

        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_DOD_5015_FILE_PLAN);
        ChildAssociationRef mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add more than one rma:filePlan to rm site,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testAddMoreThanOneFilePlanTypeToRmSite()
    {
        NodeRef rmSiteNodeRef = generateNodeRef(TYPE_RM_SITE, true);
        ArrayList<ChildAssociationRef> assocs = new ArrayList<>();

        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        when(mockedSiteService.getSite(rmSiteNodeRef)).thenReturn(mockedSiteInfo);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);

        //first file plan creation
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_FILE_PLAN);
        ChildAssociationRef mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_FILE_PLAN))).thenReturn(assocs);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);

        //second filePlan creation
        nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_FILE_PLAN);
        mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_FILE_PLAN))).thenReturn(assocs);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add one dod:filePlan to rm site,
     * Then operation is successful.
     */
    @Test
    public void testAddOneDODFilePlanTypeToRmSite()
    {
        NodeRef rmSiteNodeRef = generateNodeRef(TYPE_DOD_5015_SITE, true);
        ArrayList<ChildAssociationRef> assocs = new ArrayList<>();

        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        when(mockedSiteInfo.getNodeRef()).thenReturn(rmSiteNodeRef);
        when(mockedSiteService.getSite(rmSiteNodeRef)).thenReturn(mockedSiteInfo);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);

        when(mockedDictionaryService.isSubClass(TYPE_DOD_5015_SITE, TYPE_RM_SITE)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(TYPE_DOD_5015_FILE_PLAN, TYPE_FILE_PLAN)).thenReturn(true);
        rmSiteType.registerFilePlanType(TYPE_DOD_5015_SITE, TYPE_DOD_5015_FILE_PLAN);

        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_DOD_5015_FILE_PLAN);
        ChildAssociationRef mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_DOD_5015_FILE_PLAN))).thenReturn(assocs);

        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add more than one dod:filePlan to rm site,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testAddMoreThanOneDODFilePlanTypeToRmSite()
    {
        NodeRef rmSiteNodeRef = generateNodeRef(TYPE_DOD_5015_SITE, true);
        ArrayList<ChildAssociationRef> assocs = new ArrayList<>();

        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        when(mockedSiteInfo.getNodeRef()).thenReturn(rmSiteNodeRef);
        when(mockedSiteService.getSite(rmSiteNodeRef)).thenReturn(mockedSiteInfo);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);

        when(mockedDictionaryService.isSubClass(TYPE_DOD_5015_SITE, TYPE_RM_SITE)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(TYPE_DOD_5015_FILE_PLAN, TYPE_FILE_PLAN)).thenReturn(true);
        rmSiteType.registerFilePlanType(TYPE_DOD_5015_SITE, TYPE_DOD_5015_FILE_PLAN);

        //first dod:filePlan creation
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_DOD_5015_FILE_PLAN);
        ChildAssociationRef mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_DOD_5015_FILE_PLAN))).thenReturn(assocs);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);

        //second dod:filePlan creation
        nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_DOD_5015_FILE_PLAN);
        mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        assocs.add(mockedChildAssoc);
        when(mockedNodeService.getChildAssocs(rmSiteNodeRef, Sets.newHashSet(TYPE_DOD_5015_FILE_PLAN))).thenReturn(assocs);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);
    }

    /**
     * Given that we try to add one rma:filePlan to DOD rm site,
     * Then IntegrityException is thrown.
     */
    @Test(expected = IntegrityException.class)
    public void testAddStandardFilePlanTypeToDODRmSite()
    {
        NodeRef rmSiteNodeRef = generateNodeRef(TYPE_DOD_5015_SITE, true);

        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        when(mockedSiteInfo.getNodeRef()).thenReturn(rmSiteNodeRef);
        when(mockedSiteService.getSite(rmSiteNodeRef)).thenReturn(mockedSiteInfo);
        when(mockedApplicationContext.getBean("dbNodeService")).thenReturn(mockedNodeService);

        when(mockedDictionaryService.isSubClass(TYPE_DOD_5015_SITE, TYPE_RM_SITE)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(TYPE_DOD_5015_FILE_PLAN, TYPE_FILE_PLAN)).thenReturn(true);
        rmSiteType.registerFilePlanType(TYPE_DOD_5015_SITE, TYPE_DOD_5015_FILE_PLAN);

        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService, TYPE_FILE_PLAN);
        ChildAssociationRef mockedChildAssoc = generateChildAssociationRef(rmSiteNodeRef, nodeRef);
        rmSiteType.onCreateChildAssociation(mockedChildAssoc, true);
    }
}
