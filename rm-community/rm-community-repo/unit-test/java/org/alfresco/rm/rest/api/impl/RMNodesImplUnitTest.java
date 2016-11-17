/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.rm.rest.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.model.Repository;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rm.rest.api.RMNodes;
import org.alfresco.rm.rest.api.model.FileplanComponentNode;
import org.alfresco.rm.rest.api.model.RecordCategoryNode;
import org.alfresco.rm.rest.api.model.RecordFolderNode;
import org.alfresco.rm.rest.api.model.RecordNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Unit Test class for RMNodesImpl.
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
public class RMNodesImplUnitTest extends BaseUnitTest
{
    private static final String UNFILED_ALIAS = "-unfiled-";

    private static final String HOLDS_ALIAS = "-holds-";

    private static final String TRANSFERS_ALIAS = "-transfers-";

    private static final String FILE_PLAN_ALIAS = "-filePlan-";

    private static final String RM_SITE_ID = "rm";

    @Mock
    private SiteService mockedSiteService;

    @Mock
    private Repository mockedRepositoryHelper;

    @Mock
    private PersonService mockedPersonService;

    @Mock
    private ServiceRegistry mockedServiceRegistry;
    
    @Mock
    private CapabilityService mockedCapabilityService;

    @InjectMocks
    private RMNodesImpl rmNodesImpl;

    private Capability deleteCapability;
    private Capability createCapability;
    private Capability updateCapability;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);

        List<String> prefixes = new ArrayList<String>();
        prefixes.add(NamespaceService.DEFAULT_PREFIX);
        when(mockedNamespaceService.getPrefixes(any(String.class))).thenReturn(prefixes);
        when(mockedNamespaceService.getNamespaceURI(any(String.class))).thenReturn(RM_URI);

        deleteCapability = mock(Capability.class);
        when(mockedCapabilityService.getCapability("Delete")).thenReturn(deleteCapability);
        createCapability = mock(Capability.class);
        when(mockedCapabilityService.getCapability("FillingPermissionOnly")).thenReturn(createCapability);
        updateCapability = mock(Capability.class);
        when(mockedCapabilityService.getCapability("Update")).thenReturn(updateCapability);
    }

    @Test
    public void testGetFileplanComponent() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        QName mockedType = AlfMock.generateQName();
        when(mockedNodeService.getType(nodeRef)).thenReturn(mockedType);
        when(mockedDictionaryService.isSubClass(mockedType, ContentModel.TYPE_CMOBJECT)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(mockedType, ContentModel.TYPE_SYSTEM_FOLDER)).thenReturn(false);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        when(mockedFilePlanService.isFilePlanComponent(nodeRef)).thenReturn(true);
        List<String> includeParamList = new ArrayList<String>();
        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        assertNotNull(folderOrDocument);
        assertTrue(FileplanComponentNode.class.isInstance(folderOrDocument));

        FileplanComponentNode resultNode = (FileplanComponentNode) folderOrDocument;
        assertEquals(false, resultNode.getIsRecordFolder());
        assertEquals(false, resultNode.getIsFile());
        assertEquals(false, resultNode.getIsCategory());
    }

    @Test
    public void testGetFilePlanAllowableOperations() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        QName mockedType = AlfMock.generateQName();
        when(mockedNodeService.getType(nodeRef)).thenReturn(mockedType);
        when(mockedDictionaryService.isSubClass(mockedType, ContentModel.TYPE_CMOBJECT)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(mockedType, ContentModel.TYPE_SYSTEM_FOLDER)).thenReturn(false);
        when(mockedDictionaryService.isSubClass(mockedType, ContentModel.TYPE_FOLDER)).thenReturn(true);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        when(mockedFilePlanService.isFilePlanComponent(nodeRef)).thenReturn(true);
        List<String> includeParamList = new ArrayList<String>();
        includeParamList.add(RMNodes.PARAM_INCLUDE_ALLOWABLEOPERATIONS);

        setPermissions(nodeRef, AccessStatus.ALLOWED);

        when(deleteCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);
        when(createCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);
        when(updateCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);

        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(nodeRef);
        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        checksAllowedOperations(folderOrDocument, true, true, false);
    }

    @Test
    public void testGetFilePlanAllowableOperationsWithoutPermissions() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        QName mockedType = AlfMock.generateQName();
        when(mockedNodeService.getType(nodeRef)).thenReturn(mockedType);
        when(mockedDictionaryService.isSubClass(mockedType, ContentModel.TYPE_CMOBJECT)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(mockedType, ContentModel.TYPE_SYSTEM_FOLDER)).thenReturn(false);
        when(mockedDictionaryService.isSubClass(mockedType, ContentModel.TYPE_FOLDER)).thenReturn(true);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        when(mockedFilePlanService.isFilePlanComponent(nodeRef)).thenReturn(true);
        List<String> includeParamList = new ArrayList<String>();
        includeParamList.add(RMNodes.PARAM_INCLUDE_ALLOWABLEOPERATIONS);

        setPermissions(nodeRef, AccessStatus.DENIED);

        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(nodeRef);
        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        assertNotNull(folderOrDocument);
        assertTrue(FileplanComponentNode.class.isInstance(folderOrDocument));

        FileplanComponentNode resultNode = (FileplanComponentNode) folderOrDocument;
        assertEquals(false, resultNode.getIsRecordFolder());
        assertEquals(false, resultNode.getIsFile());
        assertEquals(false, resultNode.getIsCategory());
        List<String> allowableOperations = resultNode.getAllowableOperations();
        assertEquals(0, allowableOperations.size());
    }

    @Test
    public void testGetTransferContainerAllowableOperations() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getType(nodeRef)).thenReturn(RecordsManagementModel.TYPE_TRANSFER_CONTAINER);
        when(mockedDictionaryService.isSubClass(RecordsManagementModel.TYPE_TRANSFER_CONTAINER, ContentModel.TYPE_FOLDER)).thenReturn(true);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        List<String> includeParamList = new ArrayList<String>();
        includeParamList.add(RMNodes.PARAM_INCLUDE_ALLOWABLEOPERATIONS);

        setPermissions(nodeRef, AccessStatus.ALLOWED);

        NodeRef filePlanNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(filePlanNodeRef);
        when(mockedFilePlanService.getTransferContainer(filePlanNodeRef)).thenReturn(nodeRef);

        when(deleteCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);
        when(createCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);
        when(updateCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);

        when(mockedFilePlanService.isFilePlanComponent(nodeRef)).thenReturn(true);

        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        checksAllowedOperations(folderOrDocument, false, true, false);
    }

    @Test
    public void testGetHoldContainerAllowableOperations() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getType(nodeRef)).thenReturn(RecordsManagementModel.TYPE_HOLD_CONTAINER);
        when(mockedDictionaryService.isSubClass(RecordsManagementModel.TYPE_HOLD_CONTAINER, ContentModel.TYPE_FOLDER)).thenReturn(true);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        List<String> includeParamList = new ArrayList<String>();
        includeParamList.add(RMNodes.PARAM_INCLUDE_ALLOWABLEOPERATIONS);

        setPermissions(nodeRef, AccessStatus.ALLOWED);

        NodeRef filePlanNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(filePlanNodeRef);

        NodeRef transferContainerNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getTransferContainer(filePlanNodeRef)).thenReturn(transferContainerNodeRef);

        when(mockedFilePlanService.getHoldContainer(filePlanNodeRef)).thenReturn(nodeRef);

        when(deleteCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);
        when(createCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);
        when(updateCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);

        when(mockedFilePlanService.isFilePlanComponent(nodeRef)).thenReturn(true);

        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        checksAllowedOperations(folderOrDocument, true, true, false);
    }

    @Test
    public void testGetUnfiledContainerAllowableOperations() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getType(nodeRef)).thenReturn(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);
        when(mockedDictionaryService.isSubClass(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER, ContentModel.TYPE_FOLDER)).thenReturn(true);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        List<String> includeParamList = new ArrayList<String>();
        includeParamList.add(RMNodes.PARAM_INCLUDE_ALLOWABLEOPERATIONS);

        setPermissions(nodeRef, AccessStatus.ALLOWED);

        NodeRef filePlanNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(filePlanNodeRef);

        NodeRef transferContainerNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getTransferContainer(filePlanNodeRef)).thenReturn(transferContainerNodeRef);

        NodeRef holdContainerNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getHoldContainer(filePlanNodeRef)).thenReturn(holdContainerNodeRef);

        when(mockedFilePlanService.getUnfiledContainer(filePlanNodeRef)).thenReturn(nodeRef);

        when(deleteCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);
        when(createCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);
        when(updateCapability.evaluate(nodeRef)).thenReturn(AccessDecisionVoter.ACCESS_GRANTED);

        when(mockedFilePlanService.isFilePlanComponent(nodeRef)).thenReturn(true);

        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        checksAllowedOperations(folderOrDocument, true, true, false);
    }

    @Test
    public void testGetNonFileplanComponent() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        QName mockedType = AlfMock.generateQName();
        when(mockedNodeService.getType(nodeRef)).thenReturn(mockedType);
        when(mockedDictionaryService.isSubClass(mockedType, ContentModel.TYPE_CMOBJECT)).thenReturn(true);
        when(mockedDictionaryService.isSubClass(mockedType, ContentModel.TYPE_SYSTEM_FOLDER)).thenReturn(false);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        when(mockedFilePlanService.isFilePlanComponent(nodeRef)).thenReturn(false);
        List<String> includeParamList = new ArrayList<String>();
        try
        {
            rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
            fail("Expected exception since the requested node is not a fileplan component.");
        }
        catch(InvalidParameterException ex)
        {
            assertEquals("The provided node is not a fileplan component", ex.getMessage());
        }

    }

    @Test
    public void testGetRecordCategory() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getType(nodeRef)).thenReturn(RecordsManagementModel.TYPE_RECORD_CATEGORY);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        List<String> includeParamList = new ArrayList<String>();
        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        assertNotNull(folderOrDocument);
        assertTrue(RecordCategoryNode.class.isInstance(folderOrDocument));

        RecordCategoryNode resultNode = (RecordCategoryNode) folderOrDocument;
        assertEquals(false, resultNode.getIsRecordFolder());
        assertEquals(false, resultNode.getIsFile());
        assertEquals(true, resultNode.getIsCategory());
    }

    @Test
    public void testGetRecordCategoryWithHasRetentionScheduleParam() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getType(nodeRef)).thenReturn(RecordsManagementModel.TYPE_RECORD_CATEGORY);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        List<String> includeParamList = new ArrayList<String>();
        includeParamList.add(RMNodes.PARAM_INCLUDE_HAS_RETENTION_SCHEDULE);

        //test has retention schedule true
        DispositionSchedule mockedDispositionSchedule = mock(DispositionSchedule.class);
        when(mockedDispositionService.getDispositionSchedule(nodeRef)).thenReturn(mockedDispositionSchedule);
        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        assertNotNull(folderOrDocument);
        assertTrue(RecordCategoryNode.class.isInstance(folderOrDocument));

        RecordCategoryNode resultNode = (RecordCategoryNode) folderOrDocument;
        assertEquals(false, resultNode.getIsRecordFolder());
        assertEquals(false, resultNode.getIsFile());
        assertEquals(true, resultNode.getIsCategory());
        assertEquals(true, resultNode.getHasRetentionSchedule());

        //test has retention schedule false
        when(mockedDispositionService.getDispositionSchedule(nodeRef)).thenReturn(null);
        folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        assertNotNull(folderOrDocument);
        assertTrue(RecordCategoryNode.class.isInstance(folderOrDocument));

        resultNode = (RecordCategoryNode) folderOrDocument;
        assertEquals(false, resultNode.getIsRecordFolder());
        assertEquals(false, resultNode.getIsFile());
        assertEquals(true, resultNode.getIsCategory());
        assertEquals(false, resultNode.getHasRetentionSchedule());
    }

    @Test
    public void testGetRecordFolder() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getType(nodeRef)).thenReturn(RecordsManagementModel.TYPE_RECORD_FOLDER);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        List<String> includeParamList = new ArrayList<String>();
        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        assertNotNull(folderOrDocument);
        assertTrue(RecordFolderNode.class.isInstance(folderOrDocument));

        RecordFolderNode resultNode = (RecordFolderNode) folderOrDocument;
        assertEquals(true, resultNode.getIsRecordFolder());
        assertEquals(false, resultNode.getIsFile());
        assertEquals(false, resultNode.getIsCategory());
    }

    @Test
    public void testGetRecordFolderWithIsClosedParam() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getType(nodeRef)).thenReturn(RecordsManagementModel.TYPE_RECORD_FOLDER);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        List<String> includeParamList = new ArrayList<String>();
        includeParamList.add(RMNodes.PARAM_INCLUDE_IS_CLOSED);

        //check closed record folder
        when(mockedNodeService.getProperty(nodeRef, RecordsManagementModel.PROP_IS_CLOSED)).thenReturn(true);
        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        assertNotNull(folderOrDocument);
        assertTrue(RecordFolderNode.class.isInstance(folderOrDocument));

        RecordFolderNode resultNode = (RecordFolderNode) folderOrDocument;
        assertEquals(true, resultNode.getIsRecordFolder());
        assertEquals(false, resultNode.getIsFile());
        assertEquals(false, resultNode.getIsCategory());
        assertEquals(true, resultNode.getIsClosed());

        //check opened record folder
        when(mockedNodeService.getProperty(nodeRef, RecordsManagementModel.PROP_IS_CLOSED)).thenReturn(false);
        folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        assertNotNull(folderOrDocument);
        assertTrue(RecordFolderNode.class.isInstance(folderOrDocument));

        resultNode = (RecordFolderNode) folderOrDocument;
        assertEquals(true, resultNode.getIsRecordFolder());
        assertEquals(false, resultNode.getIsFile());
        assertEquals(false, resultNode.getIsCategory());
        assertEquals(false, resultNode.getIsClosed());
    }

    @Test
    public void testGetRecord() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        List<String> includeParamList = new ArrayList<String>();
        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        assertNotNull(folderOrDocument);
        assertTrue(RecordNode.class.isInstance(folderOrDocument));

        RecordNode resultNode = (RecordNode) folderOrDocument;
        assertEquals(false, resultNode.getIsRecordFolder());
        assertEquals(true, resultNode.getIsFile());
        assertEquals(false, resultNode.getIsCategory());
    }

    @Test
    public void testGetRecordWithIsCompletedParam() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedNodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);

        setupCompanyHomeAndPrimaryParent(nodeRef);

        List<String> includeParamList = new ArrayList<String>();

        includeParamList.add(RMNodes.PARAM_INCLUDE_IS_COMPLETED);

        //test completed record
        when(mockedNodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_DECLARED_RECORD)).thenReturn(true);
        Node folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        assertNotNull(folderOrDocument);
        assertTrue(RecordNode.class.isInstance(folderOrDocument));

        RecordNode resultNode = (RecordNode) folderOrDocument;
        assertEquals(false, resultNode.getIsRecordFolder());
        assertEquals(true, resultNode.getIsFile());
        assertEquals(false, resultNode.getIsCategory());
        assertEquals(true, resultNode.getIsCompleted());

        //test incomplete record
        when(mockedNodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_DECLARED_RECORD)).thenReturn(false);
        folderOrDocument = rmNodesImpl.getFolderOrDocument(nodeRef, null, null, includeParamList, null);
        assertNotNull(folderOrDocument);
        assertTrue(RecordNode.class.isInstance(folderOrDocument));

        resultNode = (RecordNode) folderOrDocument;
        assertEquals(false, resultNode.getIsRecordFolder());
        assertEquals(true, resultNode.getIsFile());
        assertEquals(false, resultNode.getIsCategory());
        assertEquals(false, resultNode.getIsCompleted());
    }

    @Test
    public void testValidateNodeWithFilePlanAlias() throws Exception
    {
        NodeRef filePlanNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(filePlanNodeRef);
        NodeRef validateOrLookupNode = rmNodesImpl.validateNode(FILE_PLAN_ALIAS);
        assertEquals(filePlanNodeRef, validateOrLookupNode);
    }

    @Test
    public void testValidateNodeWithTransfersAlias() throws Exception
    {
        NodeRef filePlanNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(filePlanNodeRef);

        NodeRef transferContainerNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getTransferContainer(filePlanNodeRef)).thenReturn(transferContainerNodeRef);

        NodeRef validateOrLookupNode = rmNodesImpl.validateNode(TRANSFERS_ALIAS);
        assertEquals(transferContainerNodeRef, validateOrLookupNode);
    }

    @Test
    public void testValidateNodeWithHoldsAlias() throws Exception
    {
        NodeRef filePlanNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(filePlanNodeRef);

        NodeRef holdsContainerNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getHoldContainer(filePlanNodeRef)).thenReturn(holdsContainerNodeRef);

        NodeRef validateOrLookupNode = rmNodesImpl.validateNode(HOLDS_ALIAS);
        assertEquals(holdsContainerNodeRef, validateOrLookupNode);
    }

    @Test
    public void testValidateNodeWithUnfiledAlias() throws Exception
    {
        NodeRef filePlanNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(filePlanNodeRef);

        NodeRef unfiledContainerNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedFilePlanService.getUnfiledContainer(filePlanNodeRef)).thenReturn(unfiledContainerNodeRef);

        NodeRef validateOrLookupNode = rmNodesImpl.validateNode(UNFILED_ALIAS);
        assertEquals(unfiledContainerNodeRef, validateOrLookupNode);
    }

    @Test
    public void testValidateNodeWithFilePlanAliasRMSiteNotCreated() throws Exception
    {
        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(null);

        try
        {
            rmNodesImpl.validateNode(FILE_PLAN_ALIAS);
            fail("Expected exception as RM site is not created.");
        }
        catch(EntityNotFoundException ex)
        {
            //it is ok since exception is thrown
        }
    }

    @Test
    public void testValidateNodeWithTransferAliasRMSiteNotCreated() throws Exception
    {
        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(null);

        try
        {
            rmNodesImpl.validateNode(TRANSFERS_ALIAS);
            fail("Expected exception as RM site is not created.");
        }
        catch(EntityNotFoundException ex)
        {
            //it is ok since exception is thrown
        }
    }

    @Test
    public void testValidateNodeWithHoldsAliasRMSiteNotCreated() throws Exception
    {
        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(null);

        try
        {
            rmNodesImpl.validateNode(HOLDS_ALIAS);
            fail("Expected exception as RM site is not created.");
        }
        catch(EntityNotFoundException ex)
        {
            //it is ok since exception is thrown
        }
    }

    @Test
    public void testValidateNodeWithUnfiledAliasRMSiteNotCreated() throws Exception
    {
        when(mockedFilePlanService.getFilePlanBySiteId(RM_SITE_ID)).thenReturn(null);

        try
        {
            rmNodesImpl.validateNode(UNFILED_ALIAS);
            fail("Expected exception as RM site is not created.");
        }
        catch(EntityNotFoundException ex)
        {
            //it is ok since exception is thrown
        }
    }

    @Test
    public void testValidateNodeNullNodeRef() throws Exception
    {
        try
        {
            rmNodesImpl.validateNode((String)null);
            fail("Expected exception as nodId should not be null or empty.");
        }
        catch(IllegalArgumentException ex)
        {
            assertEquals("nodeId is a mandatory parameter", ex.getMessage());
        }
    }

    @Test
    public void testValidateNode() throws Exception
    {
        NodeRef nodeRef = AlfMock.generateNodeRef(mockedNodeService);
        NodeRef validateOrLookupNode = rmNodesImpl.validateNode(nodeRef.getId());
        assertEquals(nodeRef, validateOrLookupNode);
    }

    private void setupCompanyHomeAndPrimaryParent(NodeRef nodeRef)
    {
        NodeRef companyHomeNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedRepositoryHelper.getCompanyHome()).thenReturn(companyHomeNodeRef);
        NodeRef parentNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        ChildAssociationRef mockedChildAssoc = mock(ChildAssociationRef.class);
        when(mockedChildAssoc.getParentRef()).thenReturn(parentNodeRef);
        when(mockedNodeService.getPrimaryParent(nodeRef)).thenReturn(mockedChildAssoc);
    }

    private void setPermissions(NodeRef nodeRef, AccessStatus permissionToSet)
    {
        when(mockedPermissionService.hasPermission(nodeRef, PermissionService.WRITE)).thenReturn(permissionToSet);
        when(mockedPermissionService.hasPermission(nodeRef, PermissionService.DELETE)).thenReturn(permissionToSet);
        when(mockedPermissionService.hasPermission(nodeRef, PermissionService.ADD_CHILDREN)).thenReturn(permissionToSet);
    }

    private void checksAllowedOperations(Node containerNode, boolean allowCreate, boolean allowUpdate, boolean allowDelete)
    {
        assertNotNull(containerNode);
        assertTrue(FileplanComponentNode.class.isInstance(containerNode));
        FileplanComponentNode resultNode = (FileplanComponentNode) containerNode;
        List<String> allowableOperations = resultNode.getAllowableOperations();

        assertEquals("Create operation should " + (allowCreate?"":"not ") + "be available for provided container.", 
                allowCreate, 
                allowableOperations.contains(RMNodes.OP_CREATE));
  
        assertEquals("Update operation should " + (allowCreate?"":"not ") + "be available for provided container.", 
                allowUpdate, 
                allowableOperations.contains(RMNodes.OP_UPDATE));

        assertEquals("Delete operation should " + (allowCreate?"":"not ") + "be available for provided container.", 
                allowDelete, 
                allowableOperations.contains(RMNodes.OP_DELETE));
    }
}
