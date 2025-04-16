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
package org.alfresco.repo.node.getchildren;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.locale.LocaleDAO;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.model.filefolder.GetChildrenCannedQueryFactory;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.node.getchildren.FilterPropString.FilterTypeString;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.AlfrescoCollator;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.alfresco.util.testing.category.DBTests;

/**
 * GetChildren canned query - simple unit tests
 * 
 * @author janv
 * @since 4.0
 */
@Category({OwnJVMTestsCategory.class, DBTests.class})
public class GetChildrenCannedQueryTest extends TestCase
{
    private Log logger = LogFactory.getLog(getClass());

    private ApplicationContext ctx;

    private Repository repositoryHelper;
    private NodeService nodeService;
    private TransactionService transactionService;
    private DictionaryService dictionaryService;
    private ContentService contentService;
    private MimetypeService mimetypeService;

    private PersonService personService;
    private MutableAuthenticationService authenticationService;
    private PermissionService permissionService;
    private RatingService ratingService;
    private TenantService tenantService;
    private DictionaryDAO dictionaryDAO;

    private RatingScheme fiveStarRatingScheme;
    private RatingScheme likesRatingScheme;
    private Random random = new Random();

    private static boolean setupTestData = false;

    private static final String TEST_RUN = System.currentTimeMillis() + "";
    private static final String TEST_FILE_PREFIX = "GC-CQ-File-" + TEST_RUN + "-";
    private static final String TEST_USER_PREFIX = "GC-CQ-User-" + TEST_RUN + "-";

    private static final String TEST_USER = TEST_USER_PREFIX + "user";

    private static QName TEST_CONTENT_SUBTYPE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "savedquery");
    private static QName TEST_FOLDER_SUBTYPE = ContentModel.TYPE_SYSTEM_FOLDER;

    private static final String TEST_RUN_ID = "" + System.currentTimeMillis();

    private static final String FOLDER_1 = "aaa-folder";
    private static final String FOLDER_2 = "bbb-folder";
    private static final String FOLDER_3 = "emptySystemFolder"; // note: using FileFolderService directly will auto-hide this folder
    private static final String FOLDER_4 = "yyy-folder";
    private static final String FOLDER_5 = "zzz-folder";

    private Set<NodeRef> permHits = new HashSet<NodeRef>(100);
    private Set<NodeRef> permMisses = new HashSet<NodeRef>(100);

    @SuppressWarnings({"rawtypes"})
    private NamedObjectRegistry<CannedQueryFactory> cannedQueryRegistry;
    private static final String CQ_FACTORY_NAME = "fileFolderGetChildrenCannedQueryFactory";

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        repositoryHelper = (Repository) ctx.getBean("repositoryHelper");

        nodeService = (NodeService) ctx.getBean("NodeService");
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        contentService = (ContentService) ctx.getBean("ContentService");
        mimetypeService = (MimetypeService) ctx.getBean("MimetypeService");
        dictionaryService = (DictionaryService) ctx.getBean("DictionaryService");

        personService = (PersonService) ctx.getBean("PersonService");
        authenticationService = (MutableAuthenticationService) ctx.getBean("AuthenticationService");
        permissionService = (PermissionService) ctx.getBean("PermissionService");
        ratingService = (RatingService) ctx.getBean("RatingService");

        dictionaryDAO = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        tenantService = (TenantService) ctx.getBean("tenantService");

        cannedQueryRegistry = new NamedObjectRegistry<CannedQueryFactory>();
        cannedQueryRegistry.setStorageType(CannedQueryFactory.class);

        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = new GetChildrenCannedQueryFactory();

        getChildrenCannedQueryFactory.setBeanName("fileFolderGetChildrenCannedQueryFactory");
        getChildrenCannedQueryFactory.setRegistry(cannedQueryRegistry);

        getChildrenCannedQueryFactory.setCannedQueryDAO((CannedQueryDAO) ctx.getBean("cannedQueryDAO"));
        getChildrenCannedQueryFactory.setContentDataDAO((ContentDataDAO) ctx.getBean("contentDataDAO"));
        getChildrenCannedQueryFactory.setDictionaryService((DictionaryService) ctx.getBean("dictionaryService"));
        getChildrenCannedQueryFactory.setTenantService((TenantService) ctx.getBean("tenantService"));
        getChildrenCannedQueryFactory.setLocaleDAO((LocaleDAO) ctx.getBean("localeDAO"));
        getChildrenCannedQueryFactory.setNodeDAO((NodeDAO) ctx.getBean("nodeDAO"));
        getChildrenCannedQueryFactory.setNodeService(nodeService);
        getChildrenCannedQueryFactory.setQnameDAO((QNameDAO) ctx.getBean("qnameDAO"));
        getChildrenCannedQueryFactory.setHiddenAspect((HiddenAspect) ctx.getBean("hiddenAspect"));

        getChildrenCannedQueryFactory.setMethodSecurity((MethodSecurityBean<NodeRef>) ctx.getBean("FileFolderService_security_list"));

        getChildrenCannedQueryFactory.afterPropertiesSet();

        fiveStarRatingScheme = ratingService.getRatingScheme("fiveStarRatingScheme");
        assertNotNull(fiveStarRatingScheme);
        likesRatingScheme = ratingService.getRatingScheme("likesRatingScheme");
        assertNotNull(likesRatingScheme);

        if (!setupTestData)
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

            // Load test model
            DictionaryBootstrap bootstrap = new DictionaryBootstrap();
            List<String> bootstrapModels = new ArrayList<String>();
            bootstrapModels.add("org/alfresco/repo/node/getchildren/testModel.xml");
            List<String> labels = new ArrayList<String>();
            bootstrap.setModels(bootstrapModels);
            bootstrap.setLabels(labels);
            bootstrap.setDictionaryDAO(dictionaryDAO);
            bootstrap.setTenantService(tenantService);
            bootstrap.bootstrap();

            createUser(TEST_USER, TEST_USER, TEST_USER);

            createUser(TEST_USER_PREFIX + "aaaa", TEST_USER_PREFIX + "bbbb", TEST_USER_PREFIX + "cccc");
            createUser(TEST_USER_PREFIX + "cccc", TEST_USER_PREFIX + "dddd", TEST_USER_PREFIX + "eeee");
            createUser(TEST_USER_PREFIX + "dddd", TEST_USER_PREFIX + "ffff", TEST_USER_PREFIX + "gggg");
            createUser(TEST_USER_PREFIX + "hhhh", TEST_USER_PREFIX + "cccc", TEST_USER_PREFIX + "jjjj");

            NodeRef testParentFolder = getOrCreateParentTestFolder("GetChildrenCannedQueryTest-" + TEST_RUN_ID);

            // create folder subtype (note: system folder here)
            createFolder(testParentFolder, FOLDER_3, TEST_FOLDER_SUBTYPE);

            // create content subtype (note: no pun intended ... "cm:savedquery" already exists in content model ... but is NOT related to canned queries !)
            createContent(testParentFolder, "textContent", TEST_CONTENT_SUBTYPE);

            createFolder(testParentFolder, FOLDER_5, ContentModel.TYPE_FOLDER);
            createFolder(testParentFolder, FOLDER_4, ContentModel.TYPE_FOLDER);

            boolean canRead = true;

            loadContent(testParentFolder, "quick.jpg", "", "", canRead, permHits);
            loadContent(testParentFolder, "quick.txt", "ZZ title " + TEST_RUN, "ZZ description 1", canRead, permHits);
            loadContent(testParentFolder, "quick.bmp", null, null, canRead, permHits);
            loadContent(testParentFolder, "quick.doc", "BB title " + TEST_RUN, "BB description", canRead, permHits);
            loadContent(testParentFolder, "quick.pdf", "ZZ title " + TEST_RUN, "ZZ description 2", canRead, permHits);

            canRead = false;

            loadContent(testParentFolder, "quick.ppt", "CC title " + TEST_RUN, "CC description", canRead, permMisses);
            loadContent(testParentFolder, "quick.xls", "AA title " + TEST_RUN, "AA description", canRead, permMisses);
            loadContent(testParentFolder, "quick.gif", "YY title " + TEST_RUN, "BB description", canRead, permMisses);
            loadContent(testParentFolder, "quick.xml", "ZZ title" + TEST_RUN, "BB description", canRead, permMisses);

            createFolder(testParentFolder, FOLDER_2, ContentModel.TYPE_FOLDER);
            createFolder(testParentFolder, FOLDER_1, ContentModel.TYPE_FOLDER);

            setupTestData = true;

            // double-check permissions - see testPermissions

            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);

            for (NodeRef nodeRef : permHits)
            {
                assertTrue(permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
            }

            for (NodeRef nodeRef : permMisses)
            {
                // user CANNOT read
                assertFalse(permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
            }

            // belts-and-braces
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

            for (NodeRef nodeRef : permHits)
            {
                assertTrue(permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
            }

            for (NodeRef nodeRef : permMisses)
            {
                // admin CAN read
                assertTrue(permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
            }
        }

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        NodeRef testFolder = createFolder(repositoryHelper.getCompanyHome(), "GetChildrenCannedQueryTest-testFolder-" + TEST_RUN_ID, QName.createQName("http://www.alfresco.org/test/getchildrentest/1.0", "folder"));
        createContent(testFolder, "textContent1", ContentModel.TYPE_CONTENT);
        createContent(testFolder, QName.createQName("http://www.alfresco.org/test/getchildrentest/1.0", "contains1"), "textContent2", ContentModel.TYPE_CONTENT);

        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
    }

    private NodeRef getOrCreateParentTestFolder(String name) throws Exception
    {
        NodeRef testFolder = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, name);
        if (testFolder == null)
        {
            testFolder = createFolder(repositoryHelper.getCompanyHome(), name, ContentModel.TYPE_FOLDER);
        }
        return testFolder;
    }

    public void testSetup() throws Exception
    {
        NodeRef parentNodeRef = getOrCreateParentTestFolder("GetChildrenCannedQueryTest-" + TEST_RUN_ID);

        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0);
        assertTrue(results.getPage().size() > 3);
    }

    public void testMaxItems() throws Exception
    {
        NodeRef parentNodeRef = getOrCreateParentTestFolder("GetChildrenCannedQueryTest-" + TEST_RUN_ID);

        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0);
        assertFalse(results.hasMoreItems());

        int totalCnt = results.getPage().size();
        assertTrue(totalCnt > 3);

        for (int i = 1; i <= totalCnt; i++)
        {
            results = list(parentNodeRef, 0, i, 0);
            assertEquals(results.getPage().size(), i);

            boolean hasMore = results.hasMoreItems();
            assertTrue(hasMore == (i != totalCnt));

            if (logger.isInfoEnabled())
            {
                logger.info("testSimpleMaxItems: [itemCnt=" + i + ",hasMore=" + hasMore + "]");
            }
        }
    }

    public void testPaging() throws Exception
    {
        NodeRef parentNodeRef = getOrCreateParentTestFolder("GetChildrenCannedQueryTest-" + TEST_RUN_ID);

        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0);
        assertFalse(results.hasMoreItems());

        int totalCnt = results.getPage().size();
        int pageSize = 3;
        assertTrue(totalCnt > pageSize);

        int pageCnt = totalCnt / pageSize;
        if ((totalCnt % pageSize) != 0)
        {
            // round-up
            pageCnt++;
        }
        assertTrue(pageCnt > 1);

        if (logger.isInfoEnabled())
        {
            logger.info("testSimplePaging: [totalCount=" + totalCnt + ",pageSize=" + pageSize + ",pageCount=" + pageCnt + "]");
        }

        for (int i = 1; i <= pageCnt; i++)
        {
            int skipCount = ((i - 1) * pageSize);
            int maxItems = pageSize;

            results = list(parentNodeRef, skipCount, maxItems, 0);

            boolean hasMore = results.hasMoreItems();
            int itemsCnt = results.getPage().size();

            if (logger.isInfoEnabled())
            {
                logger.info("testSimplePaging:     [pageNum=" + i + ",itemCnt=" + itemsCnt + ",hasMore=" + hasMore + "]");
            }

            if (i != pageCnt)
            {
                // not last page
                assertEquals(itemsCnt, maxItems);
                assertTrue(hasMore);
            }
            else
            {
                // last page
                assertTrue(itemsCnt <= maxItems);
                assertFalse(hasMore);
            }
        }
    }

    public void testTypeFiltering() throws Exception
    {
        NodeRef parentNodeRef = getOrCreateParentTestFolder("GetChildrenCannedQueryTest-" + TEST_RUN_ID);

        // note: parent should contain test example(s) of each type

        Set<QName> childTypeQNames = new HashSet<QName>(3);
        Set<QName> antiChildTypeQNames = new HashSet<QName>(3);

        // note: subtype != supertype

        // folders

        childTypeQNames.clear();
        childTypeQNames.add(ContentModel.TYPE_FOLDER);

        antiChildTypeQNames.clear();
        antiChildTypeQNames.add(TEST_FOLDER_SUBTYPE);

        filterByTypeAndCheck(parentNodeRef, childTypeQNames, antiChildTypeQNames);

        // files (content)

        childTypeQNames.clear();
        childTypeQNames.add(ContentModel.TYPE_CONTENT);

        antiChildTypeQNames.clear();
        antiChildTypeQNames.add(TEST_CONTENT_SUBTYPE);

        filterByTypeAndCheck(parentNodeRef, childTypeQNames, antiChildTypeQNames);

        // folders and files (base types)

        childTypeQNames.clear();
        childTypeQNames.add(ContentModel.TYPE_CONTENT);
        childTypeQNames.add(ContentModel.TYPE_FOLDER);

        antiChildTypeQNames.clear();
        antiChildTypeQNames.add(TEST_CONTENT_SUBTYPE);
        antiChildTypeQNames.add(TEST_FOLDER_SUBTYPE);

        filterByTypeAndCheck(parentNodeRef, childTypeQNames, antiChildTypeQNames);

        // folders and files (specific subtypes)

        childTypeQNames.clear();
        childTypeQNames.add(TEST_CONTENT_SUBTYPE);
        childTypeQNames.add(TEST_FOLDER_SUBTYPE);

        antiChildTypeQNames.clear();
        antiChildTypeQNames.add(ContentModel.TYPE_CONTENT);
        antiChildTypeQNames.add(ContentModel.TYPE_FOLDER);

        filterByTypeAndCheck(parentNodeRef, childTypeQNames, antiChildTypeQNames);

        // Specific super-type (that likely does not exist in DB, at least yet - see ACE-5114 - alternatively could create custom type to ensure this)
        // note: results should return 0

        childTypeQNames.clear();
        childTypeQNames.add(ContentModel.TYPE_LINK);

        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0, childTypeQNames, null, null);
        assertEquals(0, results.getPage().size());

        childTypeQNames.clear();
        childTypeQNames.add(ContentModel.TYPE_CMOBJECT);

        results = list(parentNodeRef, -1, -1, 0, childTypeQNames, null, null);
        assertEquals(0, results.getPage().size());
    }

    public void testPrimaryVsSecondary() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        NodeRef userHomeRef = repositoryHelper.getCompanyHome();

        NodeRef parentNodeRef1 = createFolder(userHomeRef, "GetChildrenCannedQueryTest-PrimaryVsSecondary-" + TEST_RUN_ID, ContentModel.TYPE_FOLDER);

        PagingResults<NodeRef> results = list(parentNodeRef1, -1, -1, 0, null, null, null);
        assertEquals(0, results.getPage().size());

        List<FilterProp> filterPropsPrimary = new ArrayList<>(1);
        filterPropsPrimary.add(new FilterPropBoolean(GetChildrenCannedQuery.FILTER_QNAME_NODE_IS_PRIMARY, true));

        List<FilterProp> filterPropsSecondary = new ArrayList<>(1);
        filterPropsSecondary.add(new FilterPropBoolean(GetChildrenCannedQuery.FILTER_QNAME_NODE_IS_PRIMARY, false));

        results = list(parentNodeRef1, -1, -1, 0, null, filterPropsPrimary, null);
        assertEquals(0, results.getPage().size());

        results = list(parentNodeRef1, -1, -1, 0, null, filterPropsSecondary, null);
        assertEquals(0, results.getPage().size());

        NodeRef folder1Ref = createFolder(parentNodeRef1, FOLDER_1, ContentModel.TYPE_FOLDER);
        NodeRef folder2Ref = createFolder(parentNodeRef1, FOLDER_2, ContentModel.TYPE_FOLDER);

        results = list(parentNodeRef1, -1, -1, 0, null, null, null);
        assertEquals(2, results.getPage().size());
        assertTrue(results.getPage().contains(folder1Ref));
        assertTrue(results.getPage().contains(folder2Ref));

        results = list(parentNodeRef1, -1, -1, 0, null, filterPropsPrimary, null);
        assertEquals(2, results.getPage().size());
        assertTrue(results.getPage().contains(folder1Ref));
        assertTrue(results.getPage().contains(folder2Ref));

        results = list(parentNodeRef1, -1, -1, 0, null, filterPropsSecondary, null);
        assertEquals(0, results.getPage().size());

        NodeRef parentNodeRef2 = getOrCreateParentTestFolder("GetChildrenCannedQueryTest-2-" + TEST_RUN_ID);

        NodeRef folder3Ref = createFolder(parentNodeRef2, FOLDER_3, ContentModel.TYPE_FOLDER);

        nodeService.addChild(parentNodeRef1, folder3Ref, ContentModel.ASSOC_CONTAINS, QName.createQName("cm:2nd"));

        results = list(parentNodeRef1, -1, -1, 0, null, null, null);
        assertEquals(3, results.getPage().size());
        assertTrue(results.getPage().contains(folder1Ref));
        assertTrue(results.getPage().contains(folder2Ref));
        assertTrue(results.getPage().contains(folder3Ref));

        results = list(parentNodeRef1, -1, -1, 0, null, filterPropsPrimary, null);
        assertEquals(2, results.getPage().size());
        assertTrue(results.getPage().contains(folder1Ref));
        assertTrue(results.getPage().contains(folder2Ref));

        results = list(parentNodeRef1, -1, -1, 0, null, filterPropsSecondary, null);
        assertEquals(1, results.getPage().size());
        assertTrue(results.getPage().contains(folder3Ref));
    }

    public void testPropertyStringFiltering() throws Exception
    {
        NodeRef parentNodeRef = getOrCreateParentTestFolder("GetChildrenCannedQueryTest-" + TEST_RUN_ID);

        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_NAME, "GC-CQ-File-" + TEST_RUN + "-", FilterTypeString.STARTSWITH, 5);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_NAME, "gc-CQ-File-" + TEST_RUN + "-", FilterTypeString.STARTSWITH, 0);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_NAME, "gC-CQ-File-" + TEST_RUN + "-", FilterTypeString.STARTSWITH_IGNORECASE, 5);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_NAME, "CQ-CQ-File-" + TEST_RUN + "-", FilterTypeString.STARTSWITH_IGNORECASE, 0);

        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_TITLE, "ZZ title " + TEST_RUN, FilterTypeString.EQUALS, 2);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_TITLE, "zz title " + TEST_RUN, FilterTypeString.EQUALS, 0);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_TITLE, "zZ tItLe " + TEST_RUN, FilterTypeString.EQUALS_IGNORECASE, 2);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_TITLE, "title " + TEST_RUN, FilterTypeString.EQUALS, 0);

        // filter with two props
        List<FilterProp> filterProps = new ArrayList<FilterProp>(4);
        filterProps.add(new FilterPropString(ContentModel.PROP_USERNAME, TEST_USER_PREFIX + "dddd", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_FIRSTNAME, TEST_USER_PREFIX + "dddd", FilterTypeString.STARTSWITH_IGNORECASE));

        NodeRef peopleContainerRef = personService.getPeopleContainer();
        PagingResults<NodeRef> results = list(peopleContainerRef, -1, -1, 0, null, filterProps, null);
        assertEquals(2, results.getPage().size());

        // filter with three props
        filterProps.clear();
        filterProps.add(new FilterPropString(ContentModel.PROP_USERNAME, TEST_USER_PREFIX + "cccc", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_FIRSTNAME, TEST_USER_PREFIX + "cccc", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_LASTNAME, TEST_USER_PREFIX + "cccc", FilterTypeString.STARTSWITH_IGNORECASE));

        results = list(peopleContainerRef, -1, -1, 0, null, filterProps, null);
        assertEquals(3, results.getPage().size());

        filterProps.clear();
        filterProps.add(new FilterPropString(ContentModel.PROP_USERNAME, TEST_USER_PREFIX + "aaaa", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_FIRSTNAME, TEST_USER_PREFIX + "aaaa", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_LASTNAME, TEST_USER_PREFIX + "aaaa", FilterTypeString.STARTSWITH_IGNORECASE));

        results = list(peopleContainerRef, -1, -1, 0, null, filterProps, null);
        assertEquals(1, results.getPage().size());

        filterProps.clear();
        filterProps.add(new FilterPropString(ContentModel.PROP_USERNAME, TEST_USER_PREFIX + "ffff", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_FIRSTNAME, TEST_USER_PREFIX + "ffff", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_LASTNAME, TEST_USER_PREFIX + "ffff", FilterTypeString.STARTSWITH_IGNORECASE));

        results = list(peopleContainerRef, -1, -1, 0, null, filterProps, null);
        assertEquals(1, results.getPage().size());

        filterProps.clear();
        filterProps.add(new FilterPropString(ContentModel.PROP_USERNAME, TEST_USER_PREFIX + "jjjj", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_FIRSTNAME, TEST_USER_PREFIX + "jjjj", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_LASTNAME, TEST_USER_PREFIX + "jjjj", FilterTypeString.STARTSWITH_IGNORECASE));

        results = list(peopleContainerRef, -1, -1, 0, null, filterProps, null);
        assertEquals(1, results.getPage().size());

        // try to filter with more than three props
        filterProps.clear();
        filterProps.add(new FilterPropString(ContentModel.PROP_NAME, "a", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_TITLE, "a", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_DESCRIPTION, "a", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_CREATOR, "a", FilterTypeString.STARTSWITH_IGNORECASE));
        try
        {
            // -ve test
            results = list(parentNodeRef, -1, -1, 0, null, filterProps, null);
            fail("Unexpected - cannot filter with more than three props");
        }
        catch (AlfrescoRuntimeException are)
        {
            // expected
        }
    }

    private float getFiveStarRating()
    {
        float rating = random.nextFloat() * (fiveStarRatingScheme.getMaxRating() - fiveStarRatingScheme.getMinRating()) + fiveStarRatingScheme.getMinRating();
        assertTrue("Five star rating is out of range: " + rating, (rating >= fiveStarRatingScheme.getMinRating() && rating <= fiveStarRatingScheme.getMaxRating()));
        return rating;
    }

    public void testPropertySorting() throws Exception
    {
        NodeRef parentNodeRef = getOrCreateParentTestFolder("GetChildrenCannedQueryTest-" + TEST_RUN_ID);

        NodeRef nodeRef1 = null;
        NodeRef nodeRef2 = null;
        NodeRef nodeRef3 = null;
        NodeRef nodeRef4 = null;
        NodeRef nodeRef5 = null;
        NodeRef nodeRef6 = null;
        NodeRef nodeRef7 = null;
        NodeRef nodeRef8 = null;
        NodeRef nodeRef9 = null;
        NodeRef nodeRef10 = null;

        // Create some nodes with integer properties on which to sort, in this case cm:fiveStarRatingScheme and cm:likesRatingScheme
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        try
        {
            nodeRef1 = createContent(parentNodeRef, "rating1", ContentModel.TYPE_CONTENT);
            nodeRef2 = createContent(parentNodeRef, "rating2", ContentModel.TYPE_CONTENT);
            nodeRef3 = createContent(parentNodeRef, "rating3", ContentModel.TYPE_CONTENT);
            nodeRef4 = createContent(parentNodeRef, "rating4", ContentModel.TYPE_CONTENT);
            nodeRef5 = createContent(parentNodeRef, "rating5", ContentModel.TYPE_CONTENT);

            nodeRef6 = createContent(parentNodeRef, "rating6", ContentModel.TYPE_CONTENT);
            nodeRef7 = createContent(parentNodeRef, "rating7", ContentModel.TYPE_CONTENT);
            nodeRef8 = createContent(parentNodeRef, "rating8", ContentModel.TYPE_CONTENT);
            nodeRef9 = createContent(parentNodeRef, "rating9", ContentModel.TYPE_CONTENT);
            nodeRef10 = createContent(parentNodeRef, "rating10", ContentModel.TYPE_CONTENT);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }

        // apply some ratings to these nodes
        ratingService.applyRating(nodeRef1, getFiveStarRating(), "fiveStarRatingScheme");
        ratingService.applyRating(nodeRef2, getFiveStarRating(), "fiveStarRatingScheme");
        ratingService.applyRating(nodeRef3, getFiveStarRating(), "fiveStarRatingScheme");
        ratingService.applyRating(nodeRef4, getFiveStarRating(), "fiveStarRatingScheme");
        ratingService.applyRating(nodeRef5, getFiveStarRating(), "fiveStarRatingScheme");

        ratingService.applyRating(nodeRef6, 1.0f, "likesRatingScheme");
        ratingService.applyRating(nodeRef7, 1.0f, "likesRatingScheme");
        ratingService.applyRating(nodeRef8, 1.0f, "likesRatingScheme");
        ratingService.applyRating(nodeRef9, 1.0f, "likesRatingScheme");
        ratingService.applyRating(nodeRef10, 1.0f, "likesRatingScheme");

        // do children canned query
        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0);

        List<QName> sortQNames = new ArrayList<QName>(3);

        // note: initial test list derived from default Share DocLib ("share-documentlibrary-config.xml")

        sortQNames.add(ContentModel.PROP_NAME);
        sortQNames.add(ContentModel.PROP_TITLE);
        sortQNames.add(ContentModel.PROP_DESCRIPTION);
        sortQNames.add(ContentModel.PROP_CREATED);
        sortQNames.add(ContentModel.PROP_CREATOR);
        sortQNames.add(ContentModel.PROP_MODIFIED);
        sortQNames.add(ContentModel.PROP_MODIFIER);

        // special (pseudo) content/node props
        sortQNames.add(GetChildrenCannedQuery.SORT_QNAME_CONTENT_SIZE);
        sortQNames.add(GetChildrenCannedQuery.SORT_QNAME_CONTENT_MIMETYPE);
        sortQNames.add(GetChildrenCannedQuery.SORT_QNAME_NODE_TYPE);
        sortQNames.add(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER);

        // Add in the ratings properties on which to sort
        sortQNames.add(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "fiveStarRatingSchemeCount"));
        sortQNames.add(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "likesRatingSchemeCount"));

        // sort with one prop
        for (QName sortQName : sortQNames)
        {
            sortAndCheck(parentNodeRef, sortQName, false); // descending
            sortAndCheck(parentNodeRef, sortQName, true); // ascending
        }

        // sort with two props - title first, then description
        List<Pair<QName, Boolean>> sortPairs = new ArrayList<Pair<QName, Boolean>>(3);
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_TITLE, false));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_DESCRIPTION, false));

        results = list(parentNodeRef, -1, -1, 0, null, null, sortPairs);
        assertEquals(TEST_FILE_PREFIX + "quick.pdf", nodeService.getProperty(results.getPage().get(0), ContentModel.PROP_NAME)); // ZZ title + YY description
        assertEquals(TEST_FILE_PREFIX + "quick.txt", nodeService.getProperty(results.getPage().get(1), ContentModel.PROP_NAME)); // ZZ title + XX description

        // sort with two props - folders first (ALF-13968) then by name
        sortPairs = new ArrayList<Pair<QName, Boolean>>(3);
        sortPairs.add(new Pair<QName, Boolean>(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, false));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_NAME, true));

        results = list(parentNodeRef, -1, -1, 0, null, null, sortPairs);

        assertEquals(FOLDER_1, nodeService.getProperty(results.getPage().get(0), ContentModel.PROP_NAME));
        assertEquals(FOLDER_2, nodeService.getProperty(results.getPage().get(1), ContentModel.PROP_NAME));
        assertEquals(FOLDER_3, nodeService.getProperty(results.getPage().get(2), ContentModel.PROP_NAME));
        assertEquals(FOLDER_4, nodeService.getProperty(results.getPage().get(3), ContentModel.PROP_NAME));
        assertEquals(FOLDER_5, nodeService.getProperty(results.getPage().get(4), ContentModel.PROP_NAME));

        assertEquals(TEST_FILE_PREFIX + "quick.bmp", nodeService.getProperty(results.getPage().get(5), ContentModel.PROP_NAME));
        assertEquals(TEST_FILE_PREFIX + "quick.doc", nodeService.getProperty(results.getPage().get(6), ContentModel.PROP_NAME));

        sortPairs = new ArrayList<Pair<QName, Boolean>>(3);
        sortPairs.add(new Pair<QName, Boolean>(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, true));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_NAME, false));

        results = list(parentNodeRef, -1, -1, 0, null, null, sortPairs);

        // note: this test assumes that children fit on one page
        int len = results.getPage().size();

        assertEquals("textContent", nodeService.getProperty(results.getPage().get(0), ContentModel.PROP_NAME));
        assertEquals("rating9", nodeService.getProperty(results.getPage().get(1), ContentModel.PROP_NAME));

        assertEquals(FOLDER_5, nodeService.getProperty(results.getPage().get(len - 5), ContentModel.PROP_NAME));
        assertEquals(FOLDER_4, nodeService.getProperty(results.getPage().get(len - 4), ContentModel.PROP_NAME));
        assertEquals(FOLDER_3, nodeService.getProperty(results.getPage().get(len - 3), ContentModel.PROP_NAME));
        assertEquals(FOLDER_2, nodeService.getProperty(results.getPage().get(len - 2), ContentModel.PROP_NAME));
        assertEquals(FOLDER_1, nodeService.getProperty(results.getPage().get(len - 1), ContentModel.PROP_NAME));

        // TODO - sort with three props

        // try to sort with more than three props
        try
        {
            // -ve test
            sortPairs = new ArrayList<Pair<QName, Boolean>>(3);
            sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_NAME, true));
            sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_TITLE, true));
            sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_DESCRIPTION, true));
            sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_MODIFIED, true));

            results = list(parentNodeRef, -1, -1, 0, null, null, sortPairs);
            fail("Unexpected - cannot sort with more than three props");
        }
        catch (AlfrescoRuntimeException are)
        {
            // expected
        }
    }

    public void testPermissions() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);

        NodeRef parentNodeRef = getOrCreateParentTestFolder("GetChildrenCannedQueryTest-" + TEST_RUN_ID);

        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0);
        assertFalse(results.hasMoreItems());

        List<NodeRef> nodeRefs = results.getPage();

        for (NodeRef nodeRef : permHits)
        {
            assertTrue(nodeRefs.contains(nodeRef));
        }

        for (NodeRef nodeRef : permMisses)
        {
            assertFalse(nodeRefs.contains(nodeRef));
        }

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        results = list(parentNodeRef, -1, -1, 0);
        assertFalse(results.hasMoreItems());

        nodeRefs = results.getPage();

        for (NodeRef nodeRef : permHits)
        {
            assertTrue(nodeRefs.contains(nodeRef));
        }

        for (NodeRef nodeRef : permMisses)
        {
            assertTrue(nodeRefs.contains(nodeRef));
        }
    }

    public void testPatterns() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        NodeRef parentNodeRef = nodeService.createNode(
                repositoryHelper.getCompanyHome(),
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
                ContentModel.TYPE_FOLDER, null).getChildRef();

        // set up some nodes to test patterns
        NodeRef nodeRef1 = createContent(parentNodeRef, "page.component-1-2.user~admin~dashboard.xml", ContentModel.TYPE_CONTENT);
        NodeRef nodeRef2 = createContent(parentNodeRef, "page.component-1-4.user~admin~dashboard.xml", ContentModel.TYPE_CONTENT);
        NodeRef nodeRef3 = createContent(parentNodeRef, "page.xml", ContentModel.TYPE_CONTENT);
        NodeRef nodeRef4 = createContent(parentNodeRef, "page.component-1-4.user~admin~panel.xml", ContentModel.TYPE_CONTENT);
        NodeRef nodeRef5 = createContent(parentNodeRef, "page.component-1-4.user~admin~%dashboard.xml", ContentModel.TYPE_CONTENT);

        AuthenticationUtil.popAuthentication();

        String pattern = "page.*.user~admin~%dashboard.xml";
        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0, pattern, null);
        assertFalse(results.hasMoreItems());

        int totalCnt = results.getPage().size();
        assertTrue(totalCnt == 1);
        assertEquals(nodeRef5, results.getPage().get(0));

        pattern = "page.*.user~admin~dashboard.xml";
        results = list(parentNodeRef, -1, -1, 0, pattern, null);
        assertFalse(results.hasMoreItems());

        totalCnt = results.getPage().size();
        assertTrue(totalCnt == 2);
        assertTrue(results.getPage().contains(nodeRef1));
        assertTrue(results.getPage().contains(nodeRef2));

        pattern = "*";
        results = list(parentNodeRef, -1, -1, 0, pattern, null);
        assertFalse(results.hasMoreItems());
        totalCnt = results.getPage().size();
        assertTrue(totalCnt == 5);
        assertTrue(results.getPage().contains(nodeRef1));
        assertTrue(results.getPage().contains(nodeRef2));
        assertTrue(results.getPage().contains(nodeRef3));
        assertTrue(results.getPage().contains(nodeRef4));
        assertTrue(results.getPage().contains(nodeRef5));

        pattern = "foo*bar";
        results = list(parentNodeRef, -1, -1, 0, pattern, null);
        assertFalse(results.hasMoreItems());
        totalCnt = results.getPage().size();
        assertEquals("", 0, totalCnt);

        pattern = "page.*.admin~dashboard.xml";
        results = list(parentNodeRef, -1, -1, 0, pattern, null);
        assertFalse(results.hasMoreItems());
        totalCnt = results.getPage().size();
        assertEquals("", 0, totalCnt);

        pattern = "page.*.user~admin~*.xml";
        results = list(parentNodeRef, -1, -1, 0, pattern, null);
        assertFalse(results.hasMoreItems());
        totalCnt = results.getPage().size();
        assertEquals("", 4, totalCnt);

        pattern = "page.*.user~admin~%*.xml";
        results = list(parentNodeRef, -1, -1, 0, pattern, null);
        assertFalse(results.hasMoreItems());
        totalCnt = results.getPage().size();
        assertEquals("", 1, totalCnt);

        pattern = "page.*.user~admin~%dashboard.xml";
        results = list(parentNodeRef, -1, -1, 0, pattern, null);
        assertFalse(results.hasMoreItems());
        totalCnt = results.getPage().size();
        assertEquals("", 1, totalCnt);

        pattern = "page.component-1-4.user~admin~%dashboard.xml";
        results = list(parentNodeRef, -1, -1, 0, pattern, null);
        assertFalse(results.hasMoreItems());
        totalCnt = results.getPage().size();
        assertEquals("", 1, totalCnt);

        pattern = "page.%.user~admin~%.xml";
        results = list(parentNodeRef, -1, -1, 0, pattern, null);
        assertFalse(results.hasMoreItems());
        totalCnt = results.getPage().size();
        assertEquals("", 0, totalCnt);
    }

    public void testRestrictByAssocType() throws Exception
    {
        NodeRef parentNodeRef = getOrCreateParentTestFolder("GetChildrenCannedQueryTest-testFolder-" + TEST_RUN_ID);

        Set<QName> assocTypeQNames = new HashSet<QName>(3);
        Set<QName> childTypeQNames = new HashSet<QName>(3);

        assocTypeQNames.clear();
        assocTypeQNames.add(ContentModel.ASSOC_CONTAINS);
        childTypeQNames.clear();
        childTypeQNames.add(ContentModel.TYPE_CONTENT);
        List<NodeRef> children = filterByAssocTypeAndCheck(parentNodeRef, assocTypeQNames, childTypeQNames);
        assertEquals(1, children.size());

        assocTypeQNames.clear();
        assocTypeQNames.add(QName.createQName("http://www.alfresco.org/test/getchildrentest/1.0", "contains1"));
        childTypeQNames.clear();
        childTypeQNames.add(ContentModel.TYPE_CONTENT);
        children = filterByAssocTypeAndCheck(parentNodeRef, assocTypeQNames, childTypeQNames);
        assertEquals(1, children.size());

        assocTypeQNames.clear();
        assocTypeQNames.add(QName.createQName("http://www.alfresco.org/test/getchildrentest/1.0", "contains1"));
        assocTypeQNames.add(ContentModel.ASSOC_CONTAINS);
        childTypeQNames.clear();
        childTypeQNames.add(ContentModel.TYPE_CONTENT);
        children = filterByAssocTypeAndCheck(parentNodeRef, assocTypeQNames, childTypeQNames);
        assertEquals(2, children.size());
    }

    @SuppressWarnings("unused")
    public void testAspectFiltering() throws Exception
    {
        NodeRef parentNodeRef = repositoryHelper.getCompanyHome();
        NodeRef nodeRef1 = null;
        NodeRef nodeRef2 = null;
        NodeRef nodeRef3 = null;
        NodeRef nodeRef4 = null;
        NodeRef nodeRef5 = null;
        NodeRef nodeRef6 = null;
        NodeRef nodeRef7 = null;
        NodeRef nodeRef8 = null;
        NodeRef nodeRef9 = null;
        NodeRef nodeRef10 = null;

        // Create some nodes with integer properties on which to sort, in this case cm:fiveStarRatingScheme and cm:likesRatingScheme
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        try
        {
            nodeRef1 = createContent(parentNodeRef, "node1", ContentModel.TYPE_CONTENT);
            nodeRef2 = createContent(parentNodeRef, "node2", ContentModel.TYPE_CONTENT);
            nodeRef3 = createContent(parentNodeRef, "node3", ContentModel.TYPE_CONTENT);
            nodeRef4 = createContent(parentNodeRef, "node4", ContentModel.TYPE_CONTENT);
            nodeRef5 = createContent(parentNodeRef, "node5", ContentModel.TYPE_CONTENT);

            nodeRef6 = createContent(parentNodeRef, "node6", ContentModel.TYPE_CONTENT);
            nodeRef7 = createContent(parentNodeRef, "node7", ContentModel.TYPE_CONTENT);
            nodeRef8 = createContent(parentNodeRef, "node8", ContentModel.TYPE_CONTENT);
            nodeRef9 = createContent(parentNodeRef, "node9", ContentModel.TYPE_CONTENT);
            nodeRef10 = createContent(parentNodeRef, "node10", ContentModel.TYPE_CONTENT);

            Map<QName, Serializable> props = Collections.emptyMap();

            nodeService.addAspect(nodeRef2, ContentModel.ASPECT_CLASSIFIABLE, props);
            nodeService.addAspect(nodeRef4, ContentModel.ASPECT_CLASSIFIABLE, props);
            nodeService.addAspect(nodeRef4, RenditionModel.ASPECT_RENDITION, props);
            nodeService.addAspect(nodeRef8, ContentModel.ASPECT_CLASSIFIABLE, props);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }

        // do children canned query
        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0);
        List<NodeRef> nodeRefs = results.getPage();
        for (NodeRef nodeRef : nodeRefs)
        {
            System.out.print(nodeRef);
            Set<QName> aspects = nodeService.getAspects(nodeRef);
            for (QName aspect : aspects)
            {
                System.out.print(" " + aspect);
            }
            System.out.println();
        }
        Set<QName> includedAspects = new HashSet<QName>(Arrays.asList(new QName[]{ContentModel.ASPECT_CLASSIFIABLE}));
        results = list(parentNodeRef, includedAspects, null, -1, -1, 0);
        assertEquals(3, results.getPage().size());

        Set<QName> excludedAspects = new HashSet<QName>(Arrays.asList(new QName[]{RenditionModel.ASPECT_RENDITION}));
        results = list(parentNodeRef, null, excludedAspects, -1, -1, 0);
        for (NodeRef result : results.getPage())
        {
            assertFalse(nodeService.getAspects(result).contains(RenditionModel.ASPECT_RENDITION));
        }

        results = list(parentNodeRef, includedAspects, excludedAspects, -1, -1, 0);
        assertEquals(2, results.getPage().size());
    }

    // test helper method - optional filtering/sorting
    private PagingResults<NodeRef> list(NodeRef parentNodeRef, final int skipCount, final int maxItems, final int requestTotalCountMax, String pattern, List<Pair<QName, Boolean>> sortProps)
    {
        PagingRequest pagingRequest = new PagingRequest(skipCount, maxItems, null);
        pagingRequest.setRequestTotalCountMax(requestTotalCountMax);

        // get canned query
        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory) cannedQueryRegistry.getNamedObject(CQ_FACTORY_NAME);
        final GetChildrenCannedQuery cq = (GetChildrenCannedQuery) getChildrenCannedQueryFactory.getCannedQuery(parentNodeRef, pattern, null, null, null, null, null, sortProps, pagingRequest);

        // execute canned query
        RetryingTransactionCallback<CannedQueryResults<NodeRef>> callback = new RetryingTransactionCallback<CannedQueryResults<NodeRef>>() {
            @Override
            public CannedQueryResults<NodeRef> execute() throws Throwable
            {
                return cq.execute();
            }
        };
        CannedQueryResults<NodeRef> results = transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);

        List<NodeRef> nodeRefs = results.getPages().get(0);

        Integer totalCount = null;
        if (requestTotalCountMax > 0)
        {
            totalCount = results.getTotalResultCount().getFirst();
        }

        return new PagingNodeRefResultsImpl(nodeRefs, results.hasMoreItems(), totalCount, false);
    }

    private void filterByTypeAndCheck(NodeRef parentNodeRef, Set<QName> childTypeQNames, Set<QName> antiChildTypeQNames)
    {
        // belts-and-braces
        for (QName childTypeQName : childTypeQNames)
        {
            assertFalse(antiChildTypeQNames.contains(childTypeQName));
        }

        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0, childTypeQNames, null, null);
        assertTrue(results.getPage().size() > 0);

        PagingResults<NodeRef> antiResults = list(parentNodeRef, -1, -1, 0, antiChildTypeQNames, null, null);
        assertTrue(antiResults.getPage().size() > 0);

        List<NodeRef> childNodeRefs = results.getPage();
        List<NodeRef> antiChildNodeRefs = antiResults.getPage();

        for (NodeRef childNodeRef : childNodeRefs)
        {
            assertFalse(antiChildNodeRefs.contains(childNodeRef));
        }

        for (NodeRef childNodeRef : childNodeRefs)
        {
            QName childNodeTypeQName = nodeService.getType(childNodeRef);
            assertTrue(childTypeQNames.contains(childNodeTypeQName));
        }

        for (NodeRef childNodeRef : antiChildNodeRefs)
        {
            QName childNodeTypeQName = nodeService.getType(childNodeRef);
            assertTrue(antiChildTypeQNames.contains(childNodeTypeQName));
        }
    }

    private List<NodeRef> filterByAssocTypeAndCheck(NodeRef parentNodeRef, Set<QName> assocTypeQNames, Set<QName> childTypeQNames)
    {
        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0, assocTypeQNames, childTypeQNames, null, null, null, null);
        assertTrue(results.getPage().size() > 0);

        List<NodeRef> childNodeRefs = results.getPage();
        return childNodeRefs;
    }

    private void filterByPropAndCheck(NodeRef parentNodeRef, QName filterPropQName, String filterVal, FilterTypeString filterType, int expectedCount)
    {
        FilterProp filter = new FilterPropString(filterPropQName, filterVal, filterType);
        List<FilterProp> filterProps = new ArrayList<FilterProp>(1);
        filterProps.add(filter);

        // note: currently inclusive filter
        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0, null, filterProps, null);

        int count = results.getPage().size();
        assertEquals(expectedCount, count);

        if (logger.isInfoEnabled())
        {
            logger.info("testFiltering: " + count + " items [" + filterPropQName + "," + filterVal + "," + filterType + "]");
        }

        for (NodeRef nodeRef : results.getPage())
        {
            Serializable propVal = nodeService.getProperty(nodeRef, filterPropQName);

            if (logger.isInfoEnabled())
            {
                logger.info("testFiltering:     [" + nodeRef + "," + propVal + "]");
            }

            if (propVal instanceof String)
            {
                String val = (String) propVal;
                switch (filterType)
                {
                case STARTSWITH:
                    if (!val.startsWith(filterVal))
                    {
                        fail("Unexpected val: " + val + " (does not 'startWith': " + filterVal + ")");
                    }
                    break;
                case STARTSWITH_IGNORECASE:
                    if (!val.toLowerCase().startsWith(filterVal.toLowerCase()))
                    {
                        fail("Unexpected val: " + val + " (does not 'startWithIgnoreCase': " + filterVal + ")");
                    }
                    break;
                case EQUALS:
                    if (!val.equals(filterVal))
                    {
                        fail("Unexpected val: " + val + " (does not 'equal': " + filterVal + ")");
                    }
                    break;
                case EQUALS_IGNORECASE:
                    if (!val.equalsIgnoreCase(filterVal))
                    {
                        fail("Unexpected val: " + val + " (does not 'equalIgnoreCase': " + filterVal + ")");
                    }
                    break;
                default:
                }
            }
            else
            {
                fail("Unsupported filter type: " + propVal.getClass().getName());
            }
        }
    }

    private void sortAndCheck(NodeRef parentNodeRef, QName sortPropQName, boolean sortAscending)
    {
        List<Pair<QName, Boolean>> sortPairs = new ArrayList<Pair<QName, Boolean>>(1);
        sortPairs.add(new Pair<QName, Boolean>(sortPropQName, sortAscending));

        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0, null, null, sortPairs);

        int count = results.getPage().size();
        assertTrue(count > 3);

        if (logger.isInfoEnabled())
        {
            logger.info("testSorting: " + count + " items [" + sortPropQName + "," + (sortAscending ? " ascending" : " descending") + "]");
        }

        Collator collator = AlfrescoCollator.getInstance(I18NUtil.getContentLocale());

        // check order
        Serializable prevVal = null;
        NodeRef prevNodeRef = null;
        int currentIteration = 0;

        boolean allValsNull = true;

        for (NodeRef nodeRef : results.getPage())
        {
            currentIteration++;

            Serializable val = null;

            if (sortPropQName.equals(GetChildrenCannedQuery.SORT_QNAME_CONTENT_SIZE) || sortPropQName.equals(GetChildrenCannedQuery.SORT_QNAME_CONTENT_MIMETYPE))
            {
                // content data properties (size or mimetype)
                ContentData cd = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
                if (cd != null)
                {
                    if (sortPropQName.equals(GetChildrenCannedQuery.SORT_QNAME_CONTENT_SIZE))
                    {
                        val = cd.getSize();
                    }
                    else if (sortPropQName.equals(GetChildrenCannedQuery.SORT_QNAME_CONTENT_MIMETYPE))
                    {
                        val = cd.getMimetype();
                    }
                }
            }
            else if (sortPropQName.equals(GetChildrenCannedQuery.SORT_QNAME_NODE_TYPE))
            {
                val = nodeService.getType(nodeRef);
            }
            else if (sortPropQName.equals(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER))
            {
                val = dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_FOLDER);
            }
            else
            {
                val = nodeService.getProperty(nodeRef, sortPropQName);
            }

            if (logger.isInfoEnabled())
            {
                logger.info("testSorting:     [" + nodeRef + ", " + val + "]");
            }

            int result = 0;

            if (val != null)
            {
                allValsNull = false;
            }

            if (prevVal == null)
            {
                result = (val == null ? 0 : 1);
            }
            else if (val == null)
            {
                result = -1;
            }
            else
            {
                if (val instanceof Date)
                {
                    result = ((Date) val).compareTo((Date) prevVal);
                }
                else if (val instanceof String)
                {
                    result = collator.compare((String) val, (String) prevVal);
                }
                else if (val instanceof Long)
                {
                    result = ((Long) val).compareTo((Long) prevVal);
                }
                else if (val instanceof Integer)
                {
                    result = ((Integer) val).compareTo((Integer) prevVal);
                }
                else if (val instanceof QName)
                {
                    result = ((QName) val).compareTo((QName) prevVal);
                }
                else if (val instanceof Boolean)
                {
                    result = ((Boolean) val).compareTo((Boolean) prevVal);
                }
                else
                {
                    fail("Unsupported sort type (" + nodeRef + "): " + val.getClass().getName());
                }

                String prevName = (String) nodeService.getProperty(prevNodeRef, ContentModel.PROP_NAME);
                String currName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

                if (!sortAscending)
                {
                    assertTrue(
                            "Not descending: " + result + "\n" +
                                    "   Iteration: " + currentIteration + " out of " + count + "\n" +
                                    "   Previous:  " + prevNodeRef + " had " + prevVal + " (" + prevName + ")\n" +
                                    "   Current :  " + nodeRef + " had " + val + " (" + currName + ")",
                            result <= 0);
                }
                else
                {
                    assertTrue(
                            "Not ascending: " + result + "\n" +
                                    "   Iteration: " + currentIteration + " out of " + count + "\n" +
                                    "   Previous:  " + prevNodeRef + " had " + prevVal + " (" + prevName + ")\n" +
                                    "   Current :  " + nodeRef + " had " + val + " (" + currName + ")",
                            result >= 0);
                }
            }
            prevVal = val;
            prevNodeRef = nodeRef;
        }

        assertFalse("All values were null", allValsNull);
    }

    // test helper method - no filtering/sorting
    private PagingResults<NodeRef> list(NodeRef parentNodeRef, Set<QName> inclusiveAspects, Set<QName> exclusiveAscpects, final int skipCount, final int maxItems, final int requestTotalCountMax)
    {
        return list(parentNodeRef, skipCount, maxItems, requestTotalCountMax, null, null, null, null, inclusiveAspects, exclusiveAscpects);
    }

    // test helper method - no filtering/sorting
    private PagingResults<NodeRef> list(NodeRef parentNodeRef, final int skipCount, final int maxItems, final int requestTotalCountMax)
    {
        return list(parentNodeRef, skipCount, maxItems, requestTotalCountMax, null, null, null, null, null, null);
    }

    // test helper method - optional filtering/sorting
    private PagingResults<NodeRef> list(NodeRef parentNodeRef, final int skipCount, final int maxItems, final int requestTotalCountMax, Set<QName> childTypeQNames, List<FilterProp> filterProps, List<Pair<QName, Boolean>> sortProps)
    {
        PagingRequest pagingRequest = new PagingRequest(skipCount, maxItems, null);
        pagingRequest.setRequestTotalCountMax(requestTotalCountMax);

        // get canned query (note: test the fileFolder extension - including support for sorting folders first)
        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory) cannedQueryRegistry.getNamedObject(CQ_FACTORY_NAME);
        final GetChildrenCannedQuery cq = (GetChildrenCannedQuery) getChildrenCannedQueryFactory.getCannedQuery(parentNodeRef, null, null, childTypeQNames, null, null, filterProps, sortProps, pagingRequest);

        // execute canned query
        RetryingTransactionCallback<CannedQueryResults<NodeRef>> callback = new RetryingTransactionCallback<CannedQueryResults<NodeRef>>() {
            @Override
            public CannedQueryResults<NodeRef> execute() throws Throwable
            {
                return cq.execute();
            }
        };
        CannedQueryResults<NodeRef> results = transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);

        List<NodeRef> nodeRefs = results.getPages().get(0);

        Integer totalCount = null;
        if (requestTotalCountMax > 0)
        {
            totalCount = results.getTotalResultCount().getFirst();
        }

        return new PagingNodeRefResultsImpl(nodeRefs, results.hasMoreItems(), totalCount, false);
    }

    // test helper method - optional filtering/sorting
    private PagingResults<NodeRef> list(NodeRef parentNodeRef, final int skipCount, final int maxItems, final int requestTotalCountMax, Set<QName> assocTypeQNames, Set<QName> childTypeQNames, List<FilterProp> filterProps, List<Pair<QName, Boolean>> sortProps, Set<QName> inclusiveAspects, Set<QName> exclusiveAspects)
    {
        PagingRequest pagingRequest = new PagingRequest(skipCount, maxItems, null);
        pagingRequest.setRequestTotalCountMax(requestTotalCountMax);

        // get canned query
        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory) cannedQueryRegistry.getNamedObject(CQ_FACTORY_NAME);
        final GetChildrenCannedQuery cq = (GetChildrenCannedQuery) getChildrenCannedQueryFactory.getCannedQuery(parentNodeRef, null, assocTypeQNames, childTypeQNames, inclusiveAspects, exclusiveAspects, filterProps, sortProps, pagingRequest);

        // execute canned query
        RetryingTransactionCallback<CannedQueryResults<NodeRef>> callback = new RetryingTransactionCallback<CannedQueryResults<NodeRef>>() {
            @Override
            public CannedQueryResults<NodeRef> execute() throws Throwable
            {
                return cq.execute();
            }
        };
        CannedQueryResults<NodeRef> results = transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);

        List<NodeRef> nodeRefs = results.getPages().get(0);

        Integer totalCount = null;
        if (requestTotalCountMax > 0)
        {
            totalCount = results.getTotalResultCount().getFirst();
        }

        return new PagingNodeRefResultsImpl(nodeRefs, results.hasMoreItems(), totalCount, false);
    }

    private class PagingNodeRefResultsImpl implements PagingResults<NodeRef>
    {
        private List<NodeRef> nodeRefs;

        private boolean hasMorePages;

        private Integer totalResultCount; // null => not requested (or unknown)
        private Boolean isTotalResultCountCutoff; // null => unknown

        public PagingNodeRefResultsImpl(List<NodeRef> nodeRefs, boolean hasMorePages, Integer totalResultCount, Boolean isTotalResultCountCutoff)
        {
            this.nodeRefs = nodeRefs;
            this.hasMorePages = hasMorePages;
            this.totalResultCount = totalResultCount;
            this.isTotalResultCountCutoff = isTotalResultCountCutoff;
        }

        public List<NodeRef> getPage()
        {
            return nodeRefs;
        }

        public boolean hasMoreItems()
        {
            return hasMorePages;
        }

        public Pair<Integer, Integer> getTotalResultCount()
        {
            return new Pair<Integer, Integer>(totalResultCount, (isTotalResultCountCutoff ? null : totalResultCount));
        }

        public String getQueryExecutionId()
        {
            return "";
        }
    }

    private NodeRef createFolder(NodeRef parentNodeRef, String folderName, QName folderType) throws IOException
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, folderName);

        NodeRef nodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, folderName);
        if (nodeRef != null)
        {
            nodeService.deleteNode(nodeRef);
        }

        nodeRef = nodeService.createNode(parentNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(folderName),
                folderType,
                properties).getChildRef();
        return nodeRef;
    }

    private NodeRef createContent(NodeRef parentNodeRef, String fileName, QName contentType) throws IOException
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, fileName);
        properties.put(ContentModel.PROP_TITLE, fileName + " my title");
        properties.put(ContentModel.PROP_DESCRIPTION, fileName + " my description");

        NodeRef nodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, fileName);
        if (nodeRef != null)
        {
            nodeService.deleteNode(nodeRef);
        }

        nodeRef = nodeService.createNode(parentNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(fileName),
                contentType,
                properties).getChildRef();

        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetypeService.guessMimetype(fileName));
        writer.putContent("my text content");

        return nodeRef;
    }

    private NodeRef createContent(NodeRef parentNodeRef, QName childAssocType, String fileName, QName contentType) throws IOException
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, fileName);
        properties.put(ContentModel.PROP_TITLE, fileName + " my title");
        properties.put(ContentModel.PROP_DESCRIPTION, fileName + " my description");

        NodeRef nodeRef = nodeService.getChildByName(parentNodeRef, childAssocType, fileName);
        if (nodeRef != null)
        {
            nodeService.deleteNode(nodeRef);
        }

        nodeRef = nodeService.createNode(parentNodeRef,
                childAssocType,
                QName.createQName(fileName),
                contentType,
                properties).getChildRef();

        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetypeService.guessMimetype(fileName));
        writer.putContent("my text content");

        return nodeRef;
    }

    private void loadContent(NodeRef parentNodeRef, String inFileName, String title, String description, boolean readAllowed, Set<NodeRef> results) throws IOException
    {
        String newFileName = TEST_FILE_PREFIX + inFileName;

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, newFileName);
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);

        NodeRef nodeRef = nodeService.createNode(parentNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(newFileName),
                ContentModel.TYPE_CONTENT,
                properties).getChildRef();

        String classPath = "quick/" + inFileName;
        File file = null;
        URL url = getClass().getClassLoader().getResource(classPath);
        if (url != null)
        {
            file = new File(url.getFile());
            if (!file.exists())
            {
                file = null;
            }
        }

        if (file == null)
        {
            fail("Unable to find test file: " + classPath);
        }

        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetypeService.guessMimetype(inFileName));
        writer.putContent(file);

        if (!readAllowed)
        {
            // deny read (by explicitly breaking inheritance)
            permissionService.setInheritParentPermissions(nodeRef, false);
        }

        results.add(nodeRef);
    }

    private void createUser(String userName, String firstName, String lastName)
    {
        if (!authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }

        if (!personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap(5);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, firstName);
            ppOne.put(ContentModel.PROP_LASTNAME, lastName);
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            personService.createPerson(ppOne);
        }
    }

    // REPO-1204 / MNT-16742 (fallout from MNT-12894)
    public void testPagingGetChildrenCannedQueryWithoutProps() throws Exception
    {
        try
        {
            long startTime = System.currentTimeMillis();

            int itemCount = 1500;
            int repeatListCount = 5;

            Set<QName> assocTypeQNames = new HashSet<>(1);
            assocTypeQNames.add(ContentModel.ASSOC_CONTAINS);

            Set<QName> childTypeQNames = new HashSet<>(1);
            childTypeQNames.add(ContentModel.TYPE_FOLDER);

            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

            NodeRef testFolder = repositoryHelper.getCompanyHome();

            NodeRef parentFolder = createFolder(testFolder, "testCreateList-" + GUID.generate(), ContentModel.TYPE_FOLDER);

            for (int i = 1; i <= itemCount; i++)
            {
                String folderName = "folder_" + GUID.generate();
                createFolder(parentFolder, folderName, ContentModel.TYPE_FOLDER);
            }

            for (int j = 1; j <= repeatListCount; j++)
            {
                // page/iterate through the children
                boolean hasMore = true;
                int skipCount = 0;
                int maxItems = 100;

                int count = 0;
                Set<String> docIds = new HashSet<>(itemCount);

                while (hasMore)
                {
                    // note: mimic similar to AlfrescoServiceCmisServiceImpl
                    PagingResults<NodeRef> results = list(parentFolder, skipCount, maxItems, skipCount + 10000, assocTypeQNames, childTypeQNames, null, null, null, null);
                    hasMore = results.hasMoreItems();
                    skipCount = skipCount + maxItems;

                    for (NodeRef nodeRef : results.getPage())
                    {
                        docIds.add(nodeRef.getId());
                        count++;
                    }
                }

                assertEquals(itemCount, count);
                assertEquals(itemCount, docIds.size());
            }

            System.out.println("Test time: " + (System.currentTimeMillis() - startTime) + " ms");
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }
}
