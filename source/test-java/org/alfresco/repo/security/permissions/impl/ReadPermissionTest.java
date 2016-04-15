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
package org.alfresco.repo.security.permissions.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.experimental.categories.Category;

// Unit tests for ALF-3952 "Search/Read Permissions Evaluation Performance"
@Category(OwnJVMTestsCategory.class)
public class ReadPermissionTest extends AbstractReadPermissionTest
{    
//    public void testDynamicAuthority() throws Exception
//    {
//        SearchParameters sp;
//        ResultSet results;
//
//        buildNodes("1001", null, 10, false);
//
//        runAs("1001");
//
//        sp = new SearchParameters();
//        sp.addStore(rootNodeRef.getStoreRef());
//        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
//        sp.setQuery("TYPE:\"cm:content\"");
//        sp.setMaxItems(Integer.MAX_VALUE);
//        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
//        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
//        results = serviceRegistry.getSearchService().query(sp);
//        int length = results.length();
//        results.close();
//
//        assertEquals(10, length);        
//    }
    
    public void testAdminCanRead()
    {
        runAs("Web1");
        
        buildNodes("1001", "Read", 10, true);
        
        SearchParameters sp;
        ResultSet results;
        
        runAs("admin");

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(10, results.length());
        results.close();        
    }

    public void testReadDeny()
    {
        SearchParameters sp;
        ResultSet results;

    	build1000NodesReadDenied("1001");

        runAs("1001");

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        int length = results.length();
        results.close();

        assertEquals(0, length);
    }

    public void testNoRead()
    {
        SearchParameters sp;
        ResultSet results;

    	build1000Nodes("1001", PermissionService.WRITE, true);

        runAs("1001");

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        int length = results.length();
        results.close();

        assertEquals(0, length);
    }

	protected void buildContainers(final String username, final String permission)
	{
		runAs("admin");
		
		RetryingTransactionCallback<Void> cb = new RetryingTransactionCallback<Void>()
		{
			public Void execute() throws Throwable
			{
				int i = 0;
				String namePrefix = "simple" + System.currentTimeMillis();

		        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}01"),
		                ContentModel.TYPE_CONTAINER).getChildRef();
		        permissionService.setPermission(n1, username, permission, true);

				return null;
			}
		};
		retryingTransactionHelper.doInTransaction(cb, false, false);
	}
	
    public void testNodeOwner()
    {
        SearchParameters sp;
        ResultSet results;

        buildOwnedNodes("1001", 0);

        runAs(AuthenticationUtil.getAdminUserName());
    	runAs("1001");

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("PATH:\"//*\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        int length = results.length();
        results.close();

        assertEquals(1001, length); // folder + children
    }

    public void testChangePermissions()
    {
        SearchParameters sp;
        ResultSet results;

    	NodeRef[] nodes = build1000Nodes("1001", 4, false);

        runAs("1001");

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        int length = results.length();
        results.close();

        assertEquals(1000, length);
        
        for(int i = 0; i < 4; i++)
        {
            permissionService.deletePermission(nodes[i], "1001", PermissionService.READ);        	
        }

        //setPermission(nodes[0], "10", permission, allow)
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        length = results.length();
        results.close();

        assertEquals(1000-4, length);
    }
    
    public void testQueryReadPermission()
    {
    	buildNodes();
        
        SearchParameters sp;
        ResultSet results;
        
        runAs("1000");

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(1000*COUNT, results.length());
        results.close();

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(1000*COUNT, results.length());
        results.close();
        
        runAs("100");
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(100*COUNT, results.length());
        results.close();
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(100*COUNT, results.length());
        results.close();
        
        runAs("10");
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(10*COUNT, results.length());
        results.close();
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(10*COUNT, results.length());
        results.close();

        // test user member of group with read permission can read
        runAs("10_1");

        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(10*COUNT, results.length());
        results.close();
        
        runAs("1");
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(COUNT, results.length());
        results.close();
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(COUNT, results.length());
        results.close();
        
        runAs("01");
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(c01.count(), results.length());
        results.close();
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(c01.count(), results.length());
        results.close();
        
        runAs("001");
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(c001.count(), results.length());
        results.close();
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(c001.count(), results.length());
        results.close();
        
        runAs("0001");
       
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(c0001.count(), results.length());
        results.close();
        
        sp = new SearchParameters();
        sp.addStore(rootNodeRef.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("TYPE:\"cm:content\"");
        sp.setMaxItems(Integer.MAX_VALUE);
        sp.setMaxPermissionChecks(Integer.MAX_VALUE);
        sp.setMaxPermissionCheckTimeMillis(Integer.MAX_VALUE);
        results = serviceRegistry.getSearchService().query(sp);
        results.setBulkFetch(false);
        assertEquals(c0001.count(), results.length());
        results.close();
    }
}
