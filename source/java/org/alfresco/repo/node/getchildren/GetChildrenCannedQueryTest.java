/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.node.getchildren;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.locale.LocaleDAO;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.getchildren.FilterPropString.FilterTypeString;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * GetChildren canned query - simple unit tests
 * 
 * @author janv
 * @since 4.0
 */
public class GetChildrenCannedQueryTest extends TestCase
{
    private Log logger = LogFactory.getLog(getClass());
    
    private static final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private Repository repositoryHelper;
    private NodeService nodeService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    
    private PersonService personService;
    private MutableAuthenticationService authenticationService;
    private PermissionService permissionService;
    
    private static boolean setupTestData = false;
    
    private static final String TEST_RUN = System.currentTimeMillis()+"";
    private static final String TEST_FILE_PREFIX = "GC-CQ-File-"+TEST_RUN+"-";
    private static final String TEST_USER_PREFIX = "GC-CQ-User-"+TEST_RUN+"-";
    
    private static final String TEST_USER = TEST_USER_PREFIX+"user";
    
    private static QName TEST_CONTENT_SUBTYPE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "savedquery");
    private static QName TEST_FOLDER_SUBTYPE = ContentModel.TYPE_SYSTEM_FOLDER;
    
    private Set<NodeRef> permHits = new HashSet<NodeRef>(100);
    private Set<NodeRef> permMisses = new HashSet<NodeRef>(100);
    
    @SuppressWarnings({ "rawtypes" })
    private NamedObjectRegistry<CannedQueryFactory> cannedQueryRegistry;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void setUp() throws Exception
    {
        repositoryHelper = (Repository)ctx.getBean("repositoryHelper");
        
        nodeService = (NodeService)ctx.getBean("NodeService");
        contentService = (ContentService)ctx.getBean("ContentService");
        mimetypeService = (MimetypeService)ctx.getBean("MimetypeService");
        
        personService = (PersonService)ctx.getBean("PersonService");
        authenticationService = (MutableAuthenticationService)ctx.getBean("AuthenticationService");
        permissionService = (PermissionService)ctx.getBean("PermissionService");
        
        cannedQueryRegistry = new NamedObjectRegistry<CannedQueryFactory>();
        cannedQueryRegistry.setStorageType(CannedQueryFactory.class);
        
        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = new GetChildrenCannedQueryFactory();
        
        getChildrenCannedQueryFactory.setBeanName("getChildrenCannedQueryFactory");
        getChildrenCannedQueryFactory.setRegistry(cannedQueryRegistry);
        
        getChildrenCannedQueryFactory.setCannedQueryDAO((CannedQueryDAO)ctx.getBean("cannedQueryDAO"));
        getChildrenCannedQueryFactory.setContentDataDAO((ContentDataDAO)ctx.getBean("contentDataDAO"));
        getChildrenCannedQueryFactory.setDictionaryService((DictionaryService)ctx.getBean("dictionaryService"));
        getChildrenCannedQueryFactory.setTenantService((TenantService)ctx.getBean("tenantService"));
        getChildrenCannedQueryFactory.setLocaleDAO((LocaleDAO)ctx.getBean("localeDAO"));
        getChildrenCannedQueryFactory.setNodeDAO((NodeDAO)ctx.getBean("nodeDAO"));
        getChildrenCannedQueryFactory.setQnameDAO((QNameDAO)ctx.getBean("qnameDAO"));
        
        getChildrenCannedQueryFactory.setMethodSecurity((MethodSecurityBean<NodeRef>)ctx.getBean("FileFolderService_security_list"));
        
        getChildrenCannedQueryFactory.afterPropertiesSet();

        if (! setupTestData)
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            createUser(TEST_USER_PREFIX, TEST_USER, TEST_USER);
            
            createUser(TEST_USER_PREFIX+"aaaa", TEST_USER_PREFIX+"bbbb", TEST_USER_PREFIX+"cccc");
            createUser(TEST_USER_PREFIX+"cccc", TEST_USER_PREFIX+"dddd", TEST_USER_PREFIX+"eeee");
            createUser(TEST_USER_PREFIX+"dddd", TEST_USER_PREFIX+"ffff", TEST_USER_PREFIX+"gggg");
            createUser(TEST_USER_PREFIX+"hhhh", TEST_USER_PREFIX+"cccc", TEST_USER_PREFIX+"jjjj");
            
            NodeRef testParentFolder = repositoryHelper.getCompanyHome();
            
            // create folder subtype
            createFolder(testParentFolder, "emptySystemFolder", ContentModel.TYPE_SYSTEM_FOLDER);
            
            // create content subtype (note: no pun intended ... "cm:savedquery" already exists in content model ... but is NOT related to canned queries !)
            createContent(testParentFolder, "textContent", QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "savedquery"));
            
            boolean canRead = true;
            
            loadContent(testParentFolder, "quick.jpg", "", "", canRead, permHits);
            loadContent(testParentFolder, "quick.txt", "ZZ title "+TEST_RUN, "ZZ description 1", canRead, permHits);
            loadContent(testParentFolder, "quick.bmp", null, null, canRead, permHits);
            loadContent(testParentFolder, "quick.doc", "BB title "+TEST_RUN, "BB description", canRead, permHits);
            loadContent(testParentFolder, "quick.pdf", "ZZ title "+TEST_RUN, "ZZ description 2", canRead, permHits);
            
            canRead = false;
            
            loadContent(testParentFolder, "quick.ppt", "CC title "+TEST_RUN, "CC description", canRead, permMisses);
            loadContent(testParentFolder, "quick.xls", "AA title "+TEST_RUN, "AA description", canRead, permMisses);
            loadContent(testParentFolder, "quick.gif", "YY title "+TEST_RUN, "BB description", canRead, permMisses);
            loadContent(testParentFolder, "quick.xml", "ZZ title" +TEST_RUN, "BB description", canRead, permMisses);
            
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
        
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
    }
    
    public void testSetup() throws Exception
    {
        NodeRef parentNodeRef = repositoryHelper.getCompanyHome();
        
        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0);
        assertTrue(results.getPage().size() > 3);
    }
    
    public void testMaxItems() throws Exception
    {
        NodeRef parentNodeRef = repositoryHelper.getCompanyHome();
        
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
                logger.info("testSimpleMaxItems: [itemCnt="+i+",hasMore="+hasMore+"]");
            }
        }
    }
    
    public void testPaging() throws Exception
    {
        NodeRef parentNodeRef = repositoryHelper.getCompanyHome();
        
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
            logger.info("testSimplePaging: [totalCount="+totalCnt+",pageSize="+pageSize+",pageCount="+pageCnt+"]");
        }
        
        for (int i = 1; i <= pageCnt; i++)
        {
            int skipCount = ((i - 1)* pageSize);
            int maxItems = pageSize;
            
            results = list(parentNodeRef, skipCount, maxItems, 0);
            
            boolean hasMore = results.hasMoreItems();
            int itemsCnt = results.getPage().size();
            
            if (logger.isInfoEnabled())
            {
                logger.info("testSimplePaging:     [pageNum="+i+",itemCnt="+itemsCnt+",hasMore="+hasMore+"]");
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
        NodeRef parentNodeRef = repositoryHelper.getCompanyHome();
        
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
    }
    
    public void testPropertyStringFiltering() throws Exception
    {
        NodeRef parentNodeRef = repositoryHelper.getCompanyHome();
        
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_NAME, "GC-CQ-File-"+TEST_RUN+"-", FilterTypeString.STARTSWITH, 5);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_NAME, "gc-CQ-File-"+TEST_RUN+"-", FilterTypeString.STARTSWITH, 0);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_NAME, "gC-CQ-File-"+TEST_RUN+"-", FilterTypeString.STARTSWITH_IGNORECASE, 5);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_NAME, "CQ-CQ-File-"+TEST_RUN+"-", FilterTypeString.STARTSWITH_IGNORECASE, 0);
        
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_TITLE, "ZZ title "+TEST_RUN, FilterTypeString.EQUALS, 2);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_TITLE, "zz title "+TEST_RUN, FilterTypeString.EQUALS, 0);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_TITLE, "zZ tItLe "+TEST_RUN, FilterTypeString.EQUALS_IGNORECASE, 2);
        filterByPropAndCheck(parentNodeRef, ContentModel.PROP_TITLE, "title "+TEST_RUN, FilterTypeString.EQUALS, 0);
        
        // filter with two props
        List<FilterProp> filterProps = new ArrayList<FilterProp>(4);
        filterProps.add( new FilterPropString(ContentModel.PROP_USERNAME, TEST_USER_PREFIX+"dddd", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_FIRSTNAME, TEST_USER_PREFIX+"dddd", FilterTypeString.STARTSWITH_IGNORECASE));
        
        NodeRef peopleContainerRef = personService.getPeopleContainer();
        PagingResults<NodeRef> results = list(peopleContainerRef, -1, -1, 0, null, filterProps, null);
        assertEquals(2, results.getPage().size());
        
        // filter with three props
        filterProps.clear();
        filterProps.add( new FilterPropString(ContentModel.PROP_USERNAME, TEST_USER_PREFIX+"cccc", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_FIRSTNAME, TEST_USER_PREFIX+"cccc", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_LASTNAME, TEST_USER_PREFIX+"cccc", FilterTypeString.STARTSWITH_IGNORECASE));
        
        results = list(peopleContainerRef, -1, -1, 0, null, filterProps, null);
        assertEquals(3, results.getPage().size());
        
        filterProps.clear();
        filterProps.add( new FilterPropString(ContentModel.PROP_USERNAME, TEST_USER_PREFIX+"aaaa", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_FIRSTNAME, TEST_USER_PREFIX+"aaaa", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_LASTNAME, TEST_USER_PREFIX+"aaaa", FilterTypeString.STARTSWITH_IGNORECASE));
        
        results = list(peopleContainerRef, -1, -1, 0, null, filterProps, null);
        assertEquals(1, results.getPage().size());
        
        filterProps.clear();
        filterProps.add( new FilterPropString(ContentModel.PROP_USERNAME, TEST_USER_PREFIX+"ffff", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_FIRSTNAME, TEST_USER_PREFIX+"ffff", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_LASTNAME, TEST_USER_PREFIX+"ffff", FilterTypeString.STARTSWITH_IGNORECASE));
        
        results = list(peopleContainerRef, -1, -1, 0, null, filterProps, null);
        assertEquals(1, results.getPage().size());
        
        filterProps.clear();
        filterProps.add( new FilterPropString(ContentModel.PROP_USERNAME, TEST_USER_PREFIX+"jjjj", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_FIRSTNAME, TEST_USER_PREFIX+"jjjj", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_LASTNAME, TEST_USER_PREFIX+"jjjj", FilterTypeString.STARTSWITH_IGNORECASE));
        
        results = list(peopleContainerRef, -1, -1, 0, null, filterProps, null);
        assertEquals(1, results.getPage().size());
        
        
        // try to filter with more than three props
        filterProps.clear();
        filterProps.add( new FilterPropString(ContentModel.PROP_NAME, "a", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_TITLE, "a", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_DESCRIPTION, "a", FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add( new FilterPropString(ContentModel.PROP_CREATOR, "a", FilterTypeString.STARTSWITH_IGNORECASE));
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
    
    public void DISABLED_testPropertySorting() throws Exception
    {
        NodeRef parentNodeRef = repositoryHelper.getCompanyHome();
        
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
        sortQNames.add(GetChildrenCannedQuery.SORT_QNAME_CONTENT_SIZE);
        sortQNames.add(GetChildrenCannedQuery.SORT_QNAME_CONTENT_MIMETYPE);
        sortQNames.add(GetChildrenCannedQuery.SORT_QNAME_NODE_TYPE);
        
        // TODO pending merge to HEAD: sortQNames.add(ContentModel... cm:likesRatingSchemeCount ...);
        
        // sort with one prop
        for (QName sortQName : sortQNames)
        {
            sortAndCheck(parentNodeRef, sortQName, false); // descending
            sortAndCheck(parentNodeRef, sortQName, true);  // ascending
        }
        
        // sort with two props
        List<Pair<QName, Boolean>> sortPairs = new ArrayList<Pair<QName, Boolean>>(3);
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_TITLE, false));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_DESCRIPTION, false));
        
        results = list(parentNodeRef, -1, -1, 0, null, null, sortPairs);
        assertEquals(TEST_FILE_PREFIX+"quick.pdf", nodeService.getProperty(results.getPage().get(0), ContentModel.PROP_NAME)); // ZZ title + YY description
        assertEquals(TEST_FILE_PREFIX+"quick.txt", nodeService.getProperty(results.getPage().get(1), ContentModel.PROP_NAME)); // ZZ title + XX description
        
        
        sortPairs = new ArrayList<Pair<QName, Boolean>>(3);
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_NAME, true));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_TITLE, true));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_DESCRIPTION, true));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_MODIFIED, true));
        
        // TODO - sort with three props
        
        // try to sort with more than three props
        try
        {
            // -ve test
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
        
        NodeRef parentNodeRef = repositoryHelper.getCompanyHome();
        
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
        assertEquals(nodeRef1, results.getPage().get(0));
        assertEquals(nodeRef2, results.getPage().get(1));
        
        pattern = "*";
        results = list(parentNodeRef, -1, -1, 0, pattern, null);
        assertFalse(results.hasMoreItems());
        totalCnt = results.getPage().size();
        assertTrue(totalCnt == 5);
        assertEquals(nodeRef1, results.getPage().get(0));
        assertEquals(nodeRef2, results.getPage().get(1));
        assertEquals(nodeRef3, results.getPage().get(2));
        assertEquals(nodeRef4, results.getPage().get(3));
        assertEquals(nodeRef5, results.getPage().get(4));
        
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
    
    // test helper method - optional filtering/sorting
    private PagingResults<NodeRef> list(NodeRef parentNodeRef, final int skipCount, final int maxItems, final int requestTotalCountMax, String pattern, List<Pair<QName, Boolean>> sortProps)
    {
        PagingRequest pagingRequest = new PagingRequest(skipCount, maxItems, null);
        pagingRequest.setRequestTotalCountMax(requestTotalCountMax);
        
        // get canned query
        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory)cannedQueryRegistry.getNamedObject("getChildrenCannedQueryFactory");
        GetChildrenCannedQuery cq = (GetChildrenCannedQuery)getChildrenCannedQueryFactory.getCannedQuery(parentNodeRef, pattern, null, null, sortProps, pagingRequest);
        
        // execute canned query
        CannedQueryResults<NodeRef> results = cq.execute();
        
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
            logger.info("testFiltering: "+count+" items ["+filterPropQName+","+filterVal+","+filterType+"]");
        }
        
        for (NodeRef nodeRef : results.getPage())
        {
            Serializable propVal = nodeService.getProperty(nodeRef, filterPropQName);
            
            if (logger.isInfoEnabled())
            {
                logger.info("testFiltering:     ["+nodeRef+","+propVal+"]");
            }
            
            if (propVal instanceof String)
            {
                String val = (String)propVal;
                switch (filterType)
                {
                    case STARTSWITH:
                        if (! val.startsWith(filterVal))
                        {
                            fail("Unexpected val: "+val+" (does not 'startWith': "+filterVal+")");
                        }
                    break;
                    case STARTSWITH_IGNORECASE:
                        if (! val.toLowerCase().startsWith(filterVal.toLowerCase()))
                        {
                            fail("Unexpected val: "+val+" (does not 'startWithIgnoreCase': "+filterVal+")");
                        }
                        break;
                    case EQUALS:
                        if (! val.equals(filterVal))
                        {
                            fail("Unexpected val: "+val+" (does not 'equal': "+filterVal+")");
                        }
                    break;
                    case EQUALS_IGNORECASE:
                        if (! val.equalsIgnoreCase(filterVal))
                        {
                            fail("Unexpected val: "+val+" (does not 'equalIgnoreCase': "+filterVal+")");
                        }
                        break;
                    default:
                }
            }
            else
            {
                fail("Unsupported filter type: "+propVal.getClass().getName());
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
            logger.info("testSorting: "+count+" items ["+sortPropQName+","+(sortAscending ? " ascending" : " descending")+"]");
        }
        
        Collator collator = Collator.getInstance();
        
        // check order
        Serializable prevVal = null;
        
        boolean allValsNull = true;
        
        for (NodeRef nodeRef : results.getPage())
        {
            Serializable val = null;
            
            if (sortPropQName.equals(GetChildrenCannedQuery.SORT_QNAME_CONTENT_SIZE) || sortPropQName.equals(GetChildrenCannedQuery.SORT_QNAME_CONTENT_MIMETYPE))
            {
                // content data properties (size or mimetype)
                ContentData cd = (ContentData)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
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
            else
            {
                val = nodeService.getProperty(nodeRef, sortPropQName);
            }
            
            if (logger.isInfoEnabled())
            {
                logger.info("testSorting:     ["+nodeRef+", "+val+"]");
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
                    result = ((Date)val).compareTo((Date)prevVal);
                }
                else if (val instanceof String)
                {
                    result = collator.compare((String)val, (String)prevVal);
                }
                else if (val instanceof Long)
                {
                    result = ((Long)val).compareTo((Long)prevVal);
                }
                else if (val instanceof QName)
                {
                    result = ((QName)val).compareTo((QName)prevVal);
                }
                else
                {
                    fail("Unsupported sort type: "+val.getClass().getName());
                }
                
                if (! sortAscending)
                {
                    assertTrue("Not descending: ["+sortPropQName+","+val+","+prevVal+"]", result <= 0);
                }
                else
                {
                    assertTrue("Not ascending: ["+sortPropQName+","+val+","+prevVal+"]", result >= 0);
                }
            }
            prevVal = val;
        }
        
        assertFalse("All values were null", allValsNull);
    }
    
    // test helper method - no filtering/sorting
    private PagingResults<NodeRef> list(NodeRef parentNodeRef, final int skipCount, final int maxItems, final int requestTotalCountMax)
    {
        return list(parentNodeRef, skipCount, maxItems, requestTotalCountMax, null, null, null);
    }
    
    // test helper method - optional filtering/sorting
    private PagingResults<NodeRef> list(NodeRef parentNodeRef, final int skipCount, final int maxItems, final int requestTotalCountMax, Set<QName> childTypeQNames, List<FilterProp> filterProps, List<Pair<QName, Boolean>> sortProps)
    {
        PagingRequest pagingRequest = new PagingRequest(skipCount, maxItems, null);
        pagingRequest.setRequestTotalCountMax(requestTotalCountMax);
        
        // get canned query
        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory)cannedQueryRegistry.getNamedObject("getChildrenCannedQueryFactory");
        GetChildrenCannedQuery cq = (GetChildrenCannedQuery)getChildrenCannedQueryFactory.getCannedQuery(parentNodeRef, null, childTypeQNames, filterProps, sortProps, pagingRequest);
        
        // execute canned query
        CannedQueryResults<NodeRef> results = cq.execute();
        
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
            this.totalResultCount= totalResultCount;
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
    
    private void createFolder(NodeRef parentNodeRef, String folderName, QName folderType) throws IOException
    {
        Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
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
    }
    
    private NodeRef createContent(NodeRef parentNodeRef, String fileName, QName contentType) throws IOException
    {
        Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
        properties.put(ContentModel.PROP_NAME, fileName);
        properties.put(ContentModel.PROP_TITLE, fileName+" my title");
        properties.put(ContentModel.PROP_DESCRIPTION, fileName+" my description");
        
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
    
    private void loadContent(NodeRef parentNodeRef, String inFileName, String title, String description, boolean readAllowed, Set<NodeRef> results) throws IOException
    {
        String newFileName = TEST_FILE_PREFIX + inFileName;
        
        Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
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
        
        if (! readAllowed)
        {
            // deny read (by explicitly breaking inheritance)
            permissionService.setInheritParentPermissions(nodeRef, false);
        }
        
        results.add(nodeRef);
    }
    
    private void createUser(String userName, String firstName, String lastName)
    {
        if (! authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }
        
        if (! personService.personExists(userName))
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
}
