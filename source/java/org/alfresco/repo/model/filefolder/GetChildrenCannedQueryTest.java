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
package org.alfresco.repo.model.filefolder;

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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityInterceptor;
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
    
    private boolean setupOnce = false;
    
    private static final String TEST_USER = "GC-CQ-User-"+System.currentTimeMillis();
    
    private Set<NodeRef> hits = new HashSet<NodeRef>(100);
    private Set<NodeRef> misses = new HashSet<NodeRef>(100);
    
    @SuppressWarnings("unchecked")
    private NamedObjectRegistry<CannedQueryFactory> cannedQueryRegistry;
    
    @SuppressWarnings("unchecked")
    @Override
    public void setUp() throws Exception
    {
        if (! setupOnce)
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
            getChildrenCannedQueryFactory.setLocaleDAO((LocaleDAO)ctx.getBean("localeDAO"));
            getChildrenCannedQueryFactory.setNodeDAO((NodeDAO)ctx.getBean("nodeDAO"));
            getChildrenCannedQueryFactory.setQnameDAO((QNameDAO)ctx.getBean("qnameDAO"));
            
            getChildrenCannedQueryFactory.setMethodSecurityInterceptor((MethodSecurityInterceptor)ctx.getBean("FileFolderService_security"));
            getChildrenCannedQueryFactory.setMethodService((Object)ctx.getBean("fileFolderService"));
            getChildrenCannedQueryFactory.setMethodName("list");
            
            getChildrenCannedQueryFactory.afterPropertiesSet();
            
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            createUser(TEST_USER);
            
            boolean canRead = true;
            
            load(repositoryHelper.getCompanyHome(), "quick.jpg", "", "", canRead, hits);
            load(repositoryHelper.getCompanyHome(), "quick.txt", "ZZ title", "ZZ description 1", canRead, hits);
            load(repositoryHelper.getCompanyHome(), "quick.bmp", null, null, canRead, hits);
            load(repositoryHelper.getCompanyHome(), "quick.doc", "BB title", "BB description", canRead, hits);
            load(repositoryHelper.getCompanyHome(), "quick.pdf", "ZZ title", "ZZ description 2", canRead, hits);
            
            canRead = false;
            
            load(repositoryHelper.getCompanyHome(), "quick.ppt", "CC title", "CC description", canRead, misses);
            load(repositoryHelper.getCompanyHome(), "quick.xls", "AA title", "AA description", canRead, misses);
            load(repositoryHelper.getCompanyHome(), "quick.gif", "YY title", "BB description", canRead, misses);
            
            setupOnce = true;
            
            // double-check permissions - see testPermissions
            
            AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
            
            for (NodeRef nodeRef : hits)
            {
                assertTrue(permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
            }
            
            for (NodeRef nodeRef : misses)
            {
                // user CANNOT read
                assertFalse(permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
            }
            
            // belts-and-braces
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            for (NodeRef nodeRef : hits)
            {
                assertTrue(permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
            }
            
            for (NodeRef nodeRef : misses)
            {
                // admin CAN read
                assertTrue(permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
            }
        }
        
        AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
    }
    
    public void testSetup() throws Exception
    {
    }
    
    public void testMaxItems() throws Exception
    {
        NodeRef parentNodeRef = repositoryHelper.getCompanyHome();
        
        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0, null);
        assertFalse(results.hasMoreItems());
        
        int totalCnt = results.getPage().size();
        assertTrue(totalCnt > 2);
        
        for (int i = 1; i <= totalCnt; i++)
        {
            results = list(parentNodeRef, 0, i, 0, null);
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
        
        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0, null);
        assertFalse(results.hasMoreItems());
        
        int totalCnt = results.getPage().size();
        int pageSize = 3;
        assertTrue(totalCnt > pageSize);
        
        int pageCnt = new Double(totalCnt / pageSize).intValue(); // round-up
        assertTrue(pageCnt > 1);
        
        for (int i = 1; i <= pageCnt; i++)
        {
            int skipCount = ((i - 1)* pageSize);
            int maxItems = pageSize;
            
            results = list(parentNodeRef, skipCount, maxItems, 0, null);
            
            boolean hasMore = results.hasMoreItems();
            int itemsCnt = results.getPage().size();
            
            if (logger.isInfoEnabled())
            {
                logger.info("testSimplePaging: [pageNum="+i+",itemCnt="+itemsCnt+",hasMore="+hasMore+"]");
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
    
    public void testSorting() throws Exception
    {
        NodeRef parentNodeRef = repositoryHelper.getCompanyHome();
        
        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0, null);
        assertTrue(results.getPage().size() > 2);
        
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
        
        for (QName sortQName : sortQNames)
        {
            sortAndCheck(parentNodeRef, sortQName, false); // descending
            sortAndCheck(parentNodeRef, sortQName, true);  // ascending
        }
        
        // sort with two props
        List<Pair<QName, Boolean>> sortPairs = new ArrayList<Pair<QName, Boolean>>(3);
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_TITLE, false));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_DESCRIPTION, false));
        
        results = list(parentNodeRef, -1, -1, 0, sortPairs);
        assertEquals("quick.pdf", nodeService.getProperty(results.getPage().get(0), ContentModel.PROP_NAME)); // ZZ title + YY description
        assertEquals("quick.txt", nodeService.getProperty(results.getPage().get(1), ContentModel.PROP_NAME)); // ZZ title + XX description
        
        
        sortPairs = new ArrayList<Pair<QName, Boolean>>(3);
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_NAME, true));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_TITLE, true));
        sortPairs.add(new Pair<QName, Boolean>(ContentModel.PROP_DESCRIPTION, true));
        
        // try to sort with more than two props
        try
        {
            // -ve test
            results = list(parentNodeRef, -1, -1, 0, sortPairs);
            fail("Unexpected - cannot sort with more than two props");
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
        
        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0, null);
        assertFalse(results.hasMoreItems());
        assertTrue(results.permissionsApplied());
        
        List<NodeRef> nodeRefs = results.getPage();
        
        for (NodeRef nodeRef : hits)
        {
            assertTrue(nodeRefs.contains(nodeRef));
        }
        
        for (NodeRef nodeRef : misses)
        {
            assertFalse(nodeRefs.contains(nodeRef));
        }
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        results = list(parentNodeRef, -1, -1, 0, null);
        assertFalse(results.hasMoreItems());
        assertTrue(results.permissionsApplied());
        
        nodeRefs = results.getPage();
        
        for (NodeRef nodeRef : hits)
        {
            assertTrue(nodeRefs.contains(nodeRef));
        }
        
        for (NodeRef nodeRef : misses)
        {
            assertTrue(nodeRefs.contains(nodeRef));
        }
    }
    
    private void sortAndCheck(NodeRef parentNodeRef, QName sortPropQName, boolean sortAscending)
    {
        List<Pair<QName, Boolean>> sortPairs = new ArrayList<Pair<QName, Boolean>>(1);
        sortPairs.add(new Pair<QName, Boolean>(sortPropQName, sortAscending));
        
        PagingResults<NodeRef> results = list(parentNodeRef, -1, -1, 0, sortPairs);
        
        if (logger.isInfoEnabled())
        {
            logger.info("testSorting: "+sortPropQName+" - "+(sortAscending ? " ascending" : " descending"));
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
                    assertTrue("Not descending", result <= 0);
                }
                else
                {
                    assertTrue("Not ascending", result >= 0);
                }
            }
            prevVal = val;
        }
        
        assertFalse("All values were null", allValsNull);
    }
    
    // test helper method
    private PagingResults<NodeRef> list(NodeRef parentNodeRef, final int skipCount, final int maxItems, final int requestTotalCountMax, List<Pair<QName, Boolean>> sortProps)
    {
        PagingRequest pagingRequest = new PagingRequest()
            {
                public int getSkipCount()
                {
                    return skipCount;
                }
                
                public int getMaxItems()
                {
                    return maxItems;
                }
                
                public int getRequestTotalCountMax()
                {
                    return requestTotalCountMax;
                }
                
                public String getQueryExecutionId()
                {
                    return null;
                }
            };
        
        // get canned query
        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory)cannedQueryRegistry.getNamedObject("getChildrenCannedQueryFactory");
        GetChildrenCannedQuery cq = (GetChildrenCannedQuery)getChildrenCannedQueryFactory.getCannedQuery(parentNodeRef, null, pagingRequest, sortProps);
        
        // execute canned query
        CannedQueryResults<NodeRef> results = cq.execute();
        
        List<NodeRef> nodeRefs = results.getPages().get(0);
        
        Integer totalCount = null;
        if (requestTotalCountMax > 0)
        {
            totalCount = results.getTotalResultCount().getFirst();
        }
        
        return new PagingNodeRefResultsImpl(nodeRefs, results.hasMoreItems(), totalCount, false, true);
    }
    
    private class PagingNodeRefResultsImpl implements PagingResults<NodeRef>
    {
        private List<NodeRef> nodeRefs;
        
        private boolean hasMorePages; 
        private boolean permissionsApplied;
        
        private Integer totalResultCount; // null => not requested (or unknown)
        private Boolean isTotalResultCountCutoff; // null => unknown
        
        public PagingNodeRefResultsImpl(List<NodeRef> nodeRefs, boolean hasMorePages, Integer totalResultCount, Boolean isTotalResultCountCutoff, boolean permissionsApplied)
        {
            this.nodeRefs = nodeRefs;
            this.hasMorePages = hasMorePages;
            this.totalResultCount= totalResultCount;
            this.isTotalResultCountCutoff = isTotalResultCountCutoff;
            this.permissionsApplied = permissionsApplied;
        }
        
        public List<NodeRef> getPage()
        {
            return nodeRefs;
        }
        
        public boolean hasMoreItems()
        {
            return hasMorePages;
        }
        
        public boolean permissionsApplied()
        {
            return permissionsApplied;
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
    
    private void load(NodeRef parentNodeRef, String fileName, String title, String description, boolean readAllowed, Set<NodeRef> results) throws IOException
    {
        // Create the node
        Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
        properties.put(ContentModel.PROP_NAME, fileName);
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        
        NodeRef nodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, fileName);
        if (nodeRef != null)
        {
            nodeService.deleteNode(nodeRef);
        }
        
        nodeRef = nodeService.createNode(parentNodeRef,
                                         ContentModel.ASSOC_CONTAINS,
                                         QName.createQName(fileName),
                                         ContentModel.TYPE_CONTENT,
                                         properties).getChildRef();
        
        String classPath = "quick/" + fileName;
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
        writer.setMimetype(mimetypeService.guessMimetype(fileName));
        writer.putContent(file);
        
        if (! readAllowed)
        {
            // deny read (by explicitly breaking inheritance)
            permissionService.setInheritParentPermissions(nodeRef, false);
        }
        
        results.add(nodeRef);
    }
    
    private void createUser(String userName)
    {
        if (! authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }
        
        if (! personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }
    }
}
