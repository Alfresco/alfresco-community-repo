/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.virtual;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.search.impl.SearchServiceSubSystemDelegator;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.virtual.model.SystemTemplateLocationsConstraint;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.traitextender.SpringExtensionBundle;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.testing.category.LuceneTests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

@Category(LuceneTests.class)
public abstract class VirtualizationIntegrationTest implements VirtualizationTest
{
    private static Log logger = LogFactory.getLog(VirtualizationIntegrationTest.class);

    private static final String PROP_VM_TEMPLATE_CLASSPATH = "prop_smf_system-template-location";

    protected static final String VIRTUAL_FOLDER_1_NAME = "VirtualFolder1";

    protected static final String VIRTUAL_FOLDER_2_NAME = "VirtualFolder2";
    
    protected static final String VIRTUAL_FOLDER_3_NAME = "VirtualFolder3";

    protected static final String TEST_ROOT_FOLDER_NAME = "TestFolder";

    protected static final String TEST_TEMPLATE_CLASSPATH = "/org/alfresco/repo/virtual/template/";

    protected static final String TEST_TEMPLATE_1_JS_NAME = "testTemplate1.js";

    protected static final String TEST_TEMPLATE_1_JSON_NAME = "testTemplate1.json";

    protected static final String TEST_TEMPLATE_2_JSON_NAME = "testTemplate2.json";

    protected static final String TEST_TEMPLATE_3_JSON_NAME = "testTemplate3.json";

    protected static final String TEST_TEMPLATE_4_JSON_NAME = "testTemplate4.json";

    protected static final String TEST_TEMPLATE_5_JSON_NAME = "testTemplate5.json";

    protected static final String TEST_TEMPLATE_6_JSON_NAME = "testTemplate6.json";

    protected static final String TEST_TEMPLATE_1_JS_CLASSPATH = TEST_TEMPLATE_CLASSPATH + TEST_TEMPLATE_1_JS_NAME;

    protected static final String TEST_TEMPLATE_1_JSON_CLASSPATH = TEST_TEMPLATE_CLASSPATH + TEST_TEMPLATE_1_JSON_NAME;

    protected static final String TEST_TEMPLATE_2_JSON_CLASSPATH = TEST_TEMPLATE_CLASSPATH + TEST_TEMPLATE_2_JSON_NAME;

    protected static final String TEST_TEMPLATE_3_JSON_CLASSPATH = TEST_TEMPLATE_CLASSPATH + TEST_TEMPLATE_3_JSON_NAME;

    protected static final String TEST_TEMPLATE_4_JSON_CLASSPATH = TEST_TEMPLATE_CLASSPATH + TEST_TEMPLATE_4_JSON_NAME;

    protected static final String TEST_TEMPLATE_5_JSON_CLASSPATH = TEST_TEMPLATE_CLASSPATH + TEST_TEMPLATE_5_JSON_NAME;

    protected static final String TEST_TEMPLATE_6_JSON_CLASSPATH = TEST_TEMPLATE_CLASSPATH + TEST_TEMPLATE_6_JSON_NAME;

    protected static final String TEST_TEMPLATE_1_JSON_SYS_PATH = "C" + TEST_TEMPLATE_1_JSON_CLASSPATH;

    protected static final String TEST_TEMPLATE_2_JSON_SYS_PATH = "C" + TEST_TEMPLATE_2_JSON_CLASSPATH;

    protected static final String TEST_TEMPLATE_3_JSON_SYS_PATH = "C" + TEST_TEMPLATE_3_JSON_CLASSPATH;

    protected static final String TEST_TEMPLATE_4_JSON_SYS_PATH = "C" + TEST_TEMPLATE_4_JSON_CLASSPATH;

    protected static final String TEST_TEMPLATE_5_JSON_SYS_PATH = "C" + TEST_TEMPLATE_5_JSON_CLASSPATH;

    protected static final String TEST_TEMPLATE_6_JSON_SYS_PATH = "C" + TEST_TEMPLATE_6_JSON_CLASSPATH;

    protected static final String TEST_TEMPLATE_1_JS_SYS_PATH = "C" + TEST_TEMPLATE_1_JS_CLASSPATH;

    public static final String VANILLA_PROCESSOR_JS_CLASSPATH = "/org/alfresco/repo/virtual/node/vanilla.js";

    protected static final String FORM_DATA_PROP_NAME = "prop_cm_name";

    protected static final String FORM_DATA_PROP_ALF_DEF = "alf_destination";

    protected ApplicationContext ctx;

    protected FileFolderService fileAndFolderService;

    protected ContentService contentService;

    protected TransactionService transactionService;

    protected NodeService nodeService;

    protected PermissionService permissionService;

    protected RetryingTransactionHelper retryingTransactionHelper;

    protected FileInfo testRootFolder;

    protected NodeRef virtualFolder1NodeRef;

    protected NodeRef rootNodeRef;

    protected NodeRef companyHomeNodeRef;

    protected ActualEnvironment environment;

    protected TypeAndAspectsFormProcessor typeAndAspectsFormProcessor;

    protected String txnTamperHint;

    protected UserTransaction txn;

    protected AuthenticationComponent authenticationComponent;

    protected VirtualizationConfigTestBootstrap virtualizationConfigTestBootstrap;

    protected SystemTemplateLocationsConstraint constraints;

    /** when set to a not-null value will be restored up[on {@link #tearDown()} */
    protected String configuredTemplatesClassPath = null;

    private @Mock ResultSet dbResults;
    private @Mock ResultSetRow resultSetRow;
    private SearchServiceSubSystemDelegator searchServiceDelegatorSpy;
    
    private String queryMatcher;

    @Before
    public void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);;
        
        virtualizationConfigTestBootstrap = ctx.getBean(VIRTUALIZATION_CONFIG_TEST_BOOTSTRAP_BEAN_ID,
                                                        VirtualizationConfigTestBootstrap.class);

        // Get the required services
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");

        transactionService = serviceRegistry.getTransactionService();
        retryingTransactionHelper = serviceRegistry.getRetryingTransactionHelper();
        nodeService = serviceRegistry.getNodeService();
        contentService = serviceRegistry.getContentService();
        fileAndFolderService = serviceRegistry.getFileFolderService();
        permissionService = serviceRegistry.getPermissionService();

        authenticationComponent = ctx.getBean("authenticationComponent",
                                              AuthenticationComponent.class);

        environment = ctx.getBean("actualEnvironment",
                                  ActualEnvironment.class);

        typeAndAspectsFormProcessor = ctx.getBean("typeAndAspectsFormProcessor",
                                                  TypeAndAspectsFormProcessor.class);

        constraints = ctx.getBean("systemTemplateLocations",
                                  SystemTemplateLocationsConstraint.class);

        Repository repository = ctx.getBean("repositoryHelper",
                                            Repository.class);

        if (!virtualizationConfigTestBootstrap.areVirtualFoldersEnabled())
        {
            // "use the force" and enable virtual folders
            SpringExtensionBundle vfBundle = ctx.getBean("smartFoldersBundle",
                                                         SpringExtensionBundle.class);
            vfBundle.start();
        }
        else
        {
            logger.info("Virtual folders are spring-enabled.");
        }

        this.authenticationComponent.setSystemUserAsCurrentUser();

        // start the transaction
        txn = transactionService.getUserTransaction();
        txn.begin();

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        companyHomeNodeRef = repository.getCompanyHome();
        testRootFolder = fileAndFolderService.create(companyHomeNodeRef,
                                                     TEST_ROOT_FOLDER_NAME,
                                                     ContentModel.TYPE_FOLDER);

        virtualFolder1NodeRef = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                        VIRTUAL_FOLDER_1_NAME,
                                                        TEST_TEMPLATE_1_JSON_SYS_PATH);
        rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        setupMocks();
    }

    @After
    public void tearDown() throws Exception
    {
        if (!virtualizationConfigTestBootstrap.areVirtualFoldersEnabled())
        {
            // "use the force" and disable virtual folders
            SpringExtensionBundle vfBundle = ctx.getBean("smartFoldersBundle",
                                                         SpringExtensionBundle.class);
            vfBundle.stop();
        }

        if (configuredTemplatesClassPath != null)
        {
            constraints.setTemplatesParentClasspath(configuredTemplatesClassPath);
            configuredTemplatesClassPath = null;
        }
        authenticationComponent.clearCurrentSecurityContext();
        try
        {
            txn.rollback();
        }
        catch (Exception e)
        {
            logger.error("Test tear down failed. Has the test setup transaction been tempered with ? Hint : "
                                     + txnTamperHint,
                         e);
        }

       // super.tearDown();
    }

    /**
     * @param parent
     * @param name
     * @param templateSysPath system path of the template to be applied. If
     *            <code>null</code> the property is not set - this should
     *            trigger default predefined settings behavior.
     * @return the {@link NodeRef} of a newly created folder that has the
     *         predefined virtualization method aspect applied
     */
    protected NodeRef createVirtualizedFolder(NodeRef parent, String name, String templateSysPath)
    {
        Item testItem = new Item("typeAndAspects",
                                 "cm:folder,smf:systemConfigSmartFolder");
        FormData testFormData = new FormData();

        testFormData.addFieldData(FORM_DATA_PROP_NAME,
                                  name);
        if (templateSysPath != null)
        {
            testFormData.addFieldData(PROP_VM_TEMPLATE_CLASSPATH,
                                      templateSysPath);
        }
        // alf_destination is mandatory
        testFormData.addFieldData(FORM_DATA_PROP_ALF_DEF,
                                  parent.toString());

        return (NodeRef) typeAndAspectsFormProcessor.persist(testItem,
                                                             testFormData);

    }

    protected void assertVirtualNode(NodeRef nodeRef)
    {
        assertVirtualNode(nodeRef,
                          Collections.<QName, Serializable> emptyMap());
    }

    protected void assertVirtualNode(NodeRef nodeRef, Map<QName, Serializable> expectedProperties)
    {
        assertNotNull(Reference.fromNodeRef(nodeRef));

        assertTrue(nodeService.hasAspect(nodeRef,
                                         VirtualContentModel.ASPECT_VIRTUAL));
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        assertTrue("Smart virtual node missing virtual aspect",aspects.contains(VirtualContentModel.ASPECT_VIRTUAL));
        //ACE-5303 injected properties title and description  require the titled aspect 
        assertTrue("Smaft virtual node missing titled aspect",aspects.contains(ContentModel.ASPECT_TITLED));

        Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);

        List<QName> mandatoryProperties = Arrays.asList(ContentModel.PROP_STORE_IDENTIFIER,
                                                        ContentModel.PROP_STORE_PROTOCOL,
                                                        ContentModel.PROP_LOCALE,
                                                        ContentModel.PROP_MODIFIED,
                                                        ContentModel.PROP_MODIFIER,
                                                        ContentModel.PROP_CREATED,
                                                        ContentModel.PROP_CREATOR,
                                                        ContentModel.PROP_NODE_DBID,
                                                        ContentModel.PROP_DESCRIPTION);

        Set<QName> missingPropreties = new HashSet<>(mandatoryProperties);
        missingPropreties.removeAll(nodeProperties.keySet());

        assertTrue("Mandatory properties are missing" + missingPropreties,
                   missingPropreties.isEmpty());

        assertFalse("ACE-5303 : ContentModel.PROP_TITLE should remain unset",nodeProperties.containsKey(ContentModel.PROP_TITLE));
        
        Set<Entry<QName, Serializable>> epEntries = expectedProperties.entrySet();
        StringBuilder unexpectedBuilder = new StringBuilder();
        for (Entry<QName, Serializable> entry : epEntries)
        {
            Serializable actualValue = nodeProperties.get(entry.getKey());
            Serializable expectedValue = expectedProperties.get(entry.getKey());
            boolean fail = false;
            String expectedValueStr = null;

            if (expectedValue != null)
            {
                expectedValueStr = expectedValue.toString();

                if (!expectedValue.equals(actualValue))
                {
                    fail = true;
                }

            }
            else if (actualValue != null)
            {
                fail = true;
                expectedValueStr = "<null>";
            }

            if (fail)
            {
                unexpectedBuilder.append("\n");
                unexpectedBuilder.append(entry.getKey());
                unexpectedBuilder.append(" expected[");
                unexpectedBuilder.append(expectedValueStr);
                unexpectedBuilder.append("] != actua[");
                unexpectedBuilder.append(actualValue);
                unexpectedBuilder.append("]");
            }
        }
        String unexpectedStr = unexpectedBuilder.toString();
        assertTrue("Unexpected property values : " + unexpectedStr,
                   unexpectedStr.isEmpty());
    }

    protected ChildAssociationRef createTypedNode(NodeRef parent, final String name, QName type)
    {
        return createTypedNode(parent,
                               name,
                               type,
                               new HashMap<QName, Serializable>());
    }

    protected ChildAssociationRef createTypedNode(NodeRef parent, final String name, QName type,
                HashMap<QName, Serializable> properties)
    {
        final HashMap<QName, Serializable> newProperties = new HashMap<QName, Serializable>(properties);
        newProperties.put(ContentModel.PROP_NAME,
                          name);
        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                             QName.createValidLocalName(name));
        return nodeService.createNode(parent,
                                      ContentModel.ASSOC_CONTAINS,
                                      assocQName,
                                      type,
                                      newProperties);
    }

    protected ChildAssociationRef createFolder(NodeRef parent, final String name)
    {
        return createTypedNode(parent,
                               name,
                               ContentModel.TYPE_FOLDER);
    }

    protected ChildAssociationRef createFolder(NodeRef parent, final String name,
                HashMap<QName, Serializable> properties)
    {
        return createTypedNode(parent,
                               name,
                               ContentModel.TYPE_FOLDER,
                               properties);
    }

    protected ChildAssociationRef createContent(NodeRef parent, final String name)
    {
        return createTypedNode(parent,
                               name,
                               ContentModel.TYPE_CONTENT);
    }

    protected ChildAssociationRef createContent(NodeRef parent, final String name, InputStream stream, String mimeType,
                String encoding)
    {
        return createContent(parent,
                             name,
                             stream,
                             mimeType,
                             encoding,
                             ContentModel.TYPE_CONTENT);
    }

    protected ChildAssociationRef createContent(NodeRef parent, final String name, InputStream stream, String mimeType,
                String encoding, QName nodeType)
    {
        ChildAssociationRef nodeAssoc = createTypedNode(parent,
                                                        name,
                                                        nodeType);
        NodeRef child = nodeAssoc.getChildRef();
        ContentWriter writer = contentService.getWriter(child,
                                                        ContentModel.PROP_CONTENT,
                                                        true);

        writer.setMimetype(mimeType);
        writer.setEncoding(encoding);
        writer.putContent(stream);
        return nodeAssoc;
    }

    protected ChildAssociationRef createContent(NodeRef parent, final String name, String contentString,
                String mimeType, String encoding)
    {
        ChildAssociationRef nodeAssoc = createTypedNode(parent,
                                                        name,
                                                        ContentModel.TYPE_CONTENT);
        NodeRef child = nodeAssoc.getChildRef();
        ContentWriter writer = contentService.getWriter(child,
                                                        ContentModel.PROP_CONTENT,
                                                        true);

        writer.setMimetype(mimeType);
        writer.setEncoding(encoding);
        writer.putContent(contentString);
        return nodeAssoc;
    }

    protected void setupMocks()
    {
        SearchServiceSubSystemDelegator searchServiceDelegator = (SearchServiceSubSystemDelegator) ctx.getBean("searchService");
        searchServiceDelegatorSpy = spy(searchServiceDelegator);
        searchServiceDelegator.setSubSystem(searchServiceDelegatorSpy);

        doReturn(dbResults).when(searchServiceDelegatorSpy).query(argThat(getArgMatcher()));
    }

    protected void prepareMocks(String queryMatcher, NodeRef realNodeToReturn)
    {
        this.queryMatcher = queryMatcher;
        List<ResultSetRow> dbRows = new ArrayList<ResultSetRow>();
        dbRows.add(resultSetRow);
        when(resultSetRow.getNodeRef()).thenReturn(realNodeToReturn);
        prepareMocksCommon(dbRows);
    }

    protected void prepareMocks(String queryMatcher, List<NodeRef> realNodesToReturn)
    {
        this.queryMatcher = queryMatcher;
        List<ResultSetRow> dbRows = new ArrayList<ResultSetRow>();
        for (NodeRef node : realNodesToReturn)
        {
            ResultSetRow mock = mock(ResultSetRow.class);
            dbRows.add(mock);
            when(mock.getNodeRef()).thenReturn(node);
        }
        prepareMocksCommon(dbRows);
    }

    protected void prepareMocksCommon(List<ResultSetRow> dbRows)
    {
        // make sure we return a new iterator each time
        when(dbResults.iterator()).thenAnswer(new Answer<Iterator<ResultSetRow>>()
        {
            public Iterator<ResultSetRow> answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable
            {
                return dbRows.iterator();
            };
        });
        when(dbResults.hasMore()).thenReturn(false);
        when(dbResults.getNumberFound()).thenReturn((long) dbRows.size());
        when(dbResults.getStart()).thenReturn(0);
    }

    protected void resetMocks()
    {
        // this will invalidate any match on the parameter for the query function,
        // so the queries will go to the real object
        this.queryMatcher = null;
    }

    protected ArgumentMatcher<SearchParameters> getArgMatcher()
    {
        return argument -> {
            if (argument!=null)
            {
                String matchingString = getMatchingString();
                if (matchingString != null && argument.toString().contains(matchingString))
                {
                    return true;
                }
            }
            return false;
        };
    }

    protected String getMatchingString()
    {
        return queryMatcher;
    }
}
