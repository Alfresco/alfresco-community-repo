/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.test.util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordableversion.RecordableVersionConfigService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.report.ReportService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.impl.ExtendedPermissionService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.alfresco.util.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

/**
 * Base unit test.
 * <p>
 * Contains core and records management service mocks ready for injection.  Helper methods
 * provide an easy way to build RM or Alfresco constructs for use in tests.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class BaseUnitTest implements RecordsManagementModel, ContentModel
{
    protected NodeRef filePlanComponent;
    protected NodeRef filePlan;

    protected NodeRef recordFolder;
    protected NodeRef record;

    /** core service mocks */
    @Mock(name="nodeService")                    protected NodeService                  mockedNodeService;
    @Mock(name="dictionaryService")              protected DictionaryService            mockedDictionaryService;
    @Mock(name="namespaceService")               protected NamespaceService             mockedNamespaceService;
    @Mock(name="identifierService")              protected IdentifierService            mockedIdentifierService;
    @Mock(name="permissionService")              protected PermissionService            mockedPermissionService;
    @Mock(name="ownableService")                 protected OwnableService               mockedOwnableService;
    @Mock(name="searchService")                  protected SearchService                mockedSearchService;
    @Mock(name="retryingTransactionHelper")      protected RetryingTransactionHelper    mockedRetryingTransactionHelper;
    @Mock(name="authorityService")               protected AuthorityService             mockedAuthorityService;
    @Mock(name="policyComponent")                protected PolicyComponent              mockedPolicyComponent;
    @Mock(name="copyService")                    protected CopyService                  mockedCopyService;
    @Mock(name="fileFolderService")              protected FileFolderService            mockedFileFolderService;
    @Mock(name="modelSecurityService")           protected ModelSecurityService         mockedModelSecurityService;

    /** rm service mocks */
    @Mock(name="filePlanService")                protected FilePlanService              mockedFilePlanService;
    @Mock(name="recordFolderService")            protected RecordFolderService          mockedRecordFolderService;
    @Mock(name="recordService")                  protected RecordService                mockedRecordService;
    @Mock(name="holdService")                    protected HoldService                  mockedHoldService;
    @Mock(name="recordsManagementActionService") protected RecordsManagementActionService mockedRecordsManagementActionService;
    @Mock(name="reportService")                  protected ReportService                mockedReportService;
    @Mock(name="filePlanRoleService")            protected FilePlanRoleService          mockedFilePlanRoleService;
    @Mock(name="recordsManagementAuditService")  protected RecordsManagementAuditService mockedRecordsManagementAuditService;
    @Mock(name="policyBehaviourFilter")          protected BehaviourFilter              mockedBehaviourFilter;
    @Mock(name="authenticationUtil")             protected AuthenticationUtil           mockedAuthenticationUtil;
    @Mock(name="extendedPermissionService")      protected ExtendedPermissionService    mockedExtendedPermissionService;
    @Mock(name="extendedSecurityService")        protected ExtendedSecurityService      mockedExtendedSecurityService;
    @Mock(name="recordableVersionConfigService") protected RecordableVersionConfigService mockedRecordableVersionConfigService;

    /** application context mock */
    @Mock(name="applicationContext")             protected ApplicationContext           mockedApplicationContext;

    /** expected exception rule */
    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * Test method setup
     */
    @SuppressWarnings("unchecked")
    @Before
    public void before() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        // setup application context
        doReturn(mockedNodeService).when(mockedApplicationContext).getBean("dbNodeService");

        // setup retrying transaction helper
        Answer<Object> doInTransactionAnswer = new Answer<Object>()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                RetryingTransactionCallback callback = (RetryingTransactionCallback)invocation.getArguments()[0];
                return callback.execute();
            }
        };
        doAnswer(doInTransactionAnswer).when(mockedRetryingTransactionHelper).doInTransaction(any(RetryingTransactionCallback.class));

        // setup mocked authentication util
        setupAuthenticationUtilMock();

        // setup file plan
        filePlan = generateNodeRef(TYPE_FILE_PLAN);
        setupAsFilePlanComponent(filePlan);
        doReturn(true).when(mockedFilePlanService).isFilePlan(filePlan);

        // setup basic file plan component
        filePlanComponent = generateNodeRef();
        setupAsFilePlanComponent(filePlanComponent);

        // setup namespace service
        doReturn(RM_URI).when(mockedNamespaceService).getNamespaceURI(RM_PREFIX);
        doReturn(CollectionUtils.unmodifiableSet(RM_PREFIX)).when(mockedNamespaceService).getPrefixes(RM_URI);

        // setup record folder and record
        recordFolder = generateRecordFolder();
        record = generateRecord();

        // set record as child of record folder
        List<ChildAssociationRef> result = new ArrayList<ChildAssociationRef>(1);
        result.add(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, recordFolder, generateQName(), record, true, 1));
        doReturn(result).when(mockedNodeService).getChildAssocs(eq(recordFolder), eq(ContentModel.ASSOC_CONTAINS), any(QNamePattern.class));
        doReturn(result).when(mockedNodeService).getParentAssocs(record);
        doReturn(Collections.singletonList(recordFolder)).when(mockedRecordFolderService).getRecordFolders(record);
        doReturn(Collections.singletonList(record)).when(mockedRecordService).getRecords(recordFolder);
    }

    /**
     * Setup authentication util mock
     */
    @SuppressWarnings("unchecked")
    private void setupAuthenticationUtilMock()
    {
        // just do the work
        doAnswer(new Answer<Object>()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                RunAsWork work = (RunAsWork)invocation.getArguments()[0];
                return work.doWork();
            }

        }).when(mockedAuthenticationUtil).runAsSystem(any(RunAsWork.class));

        // just do the work
        doAnswer(new Answer<Object>()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                RunAsWork work = (RunAsWork)invocation.getArguments()[0];
                return work.doWork();
            }

        }).when(mockedAuthenticationUtil).runAs(any(RunAsWork.class), anyString());

        // assume admin
        doReturn("admin").when(mockedAuthenticationUtil).getAdminUserName();
        doReturn("admin").when(mockedAuthenticationUtil).getFullyAuthenticatedUser();
    }
    
    /**
     * Helper to generate random text value suitable for a property 
     * value or node name
     */
    protected String generateText()
    {
        return UUID.randomUUID().toString();
    }

    /**
     * Helper method to generate a qname.
     *
     * @return  QName   qualified name
     */
    protected static QName generateQName()
    {
        return QName.createQName(RM_URI, GUID.generate());
    }

    /**
     * Helper method to generate hold reference
     *
     * @param name                  hold name
     * @return {@link NodeRef}      node reference that will behave like a hold
     */
    protected NodeRef generateHoldNodeRef(String name)
    {
        NodeRef hold = generateNodeRef(TYPE_HOLD);
        doReturn(name).when(mockedNodeService).getProperty(hold, ContentModel.PROP_NAME);
        doReturn(true).when(mockedHoldService).isHold(hold);
        return hold;
    }

    /**
     * Helper method to generate record folder reference
     *
     * @return  {@link NodeRef} node reference that will behave like a record folder
     */
    protected NodeRef generateRecordFolder()
    {
        NodeRef recordFolder = generateNodeRef(TYPE_RECORD_FOLDER);
        setupAsFilePlanComponent(recordFolder);
        doReturn(true).when(mockedRecordFolderService).isRecordFolder(recordFolder);
        return recordFolder;
    }

    /**
     * Helper method to generate a record node reference.
     *
     * @return  {@link NodeRef} node reference that will behave like a record or type cm:content
     */
    protected NodeRef generateRecord()
    {
        NodeRef record = generateNodeRef(ContentModel.TYPE_CONTENT);
        setupAsFilePlanComponent(record);
        doReturn(true).when(mockedNodeService).hasAspect(record, ASPECT_RECORD);
        doReturn(true).when(mockedRecordService).isRecord(record);
        return record;
    }

    /**
     * Helper method to setup a node reference as a file plan component.
     *
     * @param nodeRef   {@link NodeRef} node reference that will now behave like a file plan component
     */
    protected void setupAsFilePlanComponent(NodeRef nodeRef)
    {
        doReturn(true).when(mockedNodeService).hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT);
        doReturn(true).when(mockedFilePlanService).isFilePlanComponent(nodeRef);
        doReturn(filePlan).when(mockedFilePlanService).getFilePlan(nodeRef);
        doReturn(filePlan).when(mockedNodeService).getProperty(nodeRef, PROP_ROOT_NODEREF);
    }

    /**
     * Helper method to generate a node reference.
     *
     * @return  {@link NodeRef} node reference that behaves like a node that exists in the spaces store
     */
    protected NodeRef generateNodeRef()
    {
        return generateNodeRef(null);
    }

    /**
     * Helper method to generate a node reference of a particular type.
     *
     * @param type  content type qualified name
     * @return {@link NodeRef}  node reference that behaves like a node that exists in the spaces store with
     *                          the content type provided
     */
    protected NodeRef generateNodeRef(QName type)
    {
        return generateNodeRef(type, true);
    }

    /**
     * Helper method to generate a cm:content node reference with a given name.
     *
     * @param name      content name
     * @return NodeRef  node reference
     */
    protected NodeRef generateCmContent(String name)
    {
        NodeRef nodeRef = generateNodeRef(ContentModel.TYPE_CONTENT, true);
        doReturn(name).when(mockedNodeService).getProperty(nodeRef, ContentModel.PROP_NAME);
        return nodeRef;
    }

    /**
     * Helper method to generate a node reference of a particular type with a given existence characteristic.
     *
     * @param type  content type qualified name
     * @param exists indicates whether this node should behave like a node that exists or not
     * @return {@link NodeRef}  node reference that behaves like a node that exists (or not) in the spaces store with
     *                          the content type provided
     */
    protected NodeRef generateNodeRef(QName type, boolean exists)
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        when(mockedNodeService.exists(eq(nodeRef))).thenReturn(exists);
        if (type != null)
        {
            when(mockedNodeService.getType(eq(nodeRef))).thenReturn(type);
        }
        return nodeRef;
    }

    /**
     * Helper method to generate a mocked child association reference.
     *
     * @param parent                        parent node (optional)
     * @param child                         child node (optional)
     * @return {@link ChildAssociationRef}  mocked to return the parent and child nodes
     */
    protected ChildAssociationRef generateChildAssociationRef(NodeRef parent, NodeRef child)
    {
        ChildAssociationRef mockedChildAssociationRef = mock(ChildAssociationRef.class);

        if (parent != null)
        {
            doReturn(parent).when(mockedChildAssociationRef).getParentRef();
        }

        if (child != null)
        {
            doReturn(child).when(mockedChildAssociationRef).getChildRef();
        }

        return mockedChildAssociationRef;
    }

    /**
     * Helper method to make one node the primary parent of the other.
     * <p>
     * Assumes the cm:contains assoc type.
     *
     * @param child
     * @param parent
     */
    protected void makePrimaryParentOf(NodeRef child, NodeRef parent)
    {
        doReturn(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parent, generateQName(), child))
            .when(mockedNodeService)
            .getPrimaryParent(child);
    }

    /**
     * Helper method to make a number of nodes children of another.
     * <p>
     * Assumes the cm:contains assoc type.
     *
     * @param parent
     * @param children
     */
    protected void makeChildrenOf(NodeRef parent, NodeRef ... children)
    {
        List<ChildAssociationRef> assocs = new ArrayList<ChildAssociationRef>(children.length);
        for (NodeRef child : children)
        {
            assocs.add(new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parent, generateQName(), child));
            doReturn(assocs).when(mockedNodeService).getParentAssocs(child);
        }
        doReturn(assocs).when(mockedNodeService).getChildAssocs(parent, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);        
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> buildList(T ... values)
    {
        List<T> result = new ArrayList<T>(values.length);
        for (T value : values)
        {
            result.add(value);
        }
        return result;
    }
}
