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
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchParameters;
import org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.util.Pair;
import org.alfresco.util.TestWithUserUtils;

/**
 * Search service implementation unit test.
 * 
 * @author Roy Wetherall
 */
@SuppressWarnings("unused")
public class RecordsManagementSearchServiceImplTest extends BaseRMTestCase
{
    @Override
    protected boolean isMultiHierarchyTest()
    {
        return true;
    }
    
    private static final String SEARCH1 = "search1";
    private static final String SEARCH2 = "search2";
    private static final String SEARCH3 = "search3";
    private static final String SEARCH4 = "search4";
    
    private static final String USER1 = "user1";
    private static final String USER2 = "user2";
    
    private NodeRef folderLevelRecordFolder;
    private NodeRef recordLevelRecordFolder;
        
    private NodeRef recordOne;
    private NodeRef recordTwo;
    private NodeRef recordThree;
    private NodeRef recordFour;
    private NodeRef recordFive;
    private NodeRef recordSix;
    
    private MutableAuthenticationService authenticationService;
    
    private int numberOfReports;
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setupTestData()
     */
    @Override
    protected void setupTestData()
    {
        super.setupTestData();
        
        authenticationService = (MutableAuthenticationService)applicationContext.getBean("AuthenticationService");
        
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Create test users                
                TestWithUserUtils.createUser(USER1, USER1, rootNodeRef, nodeService, authenticationService);
                TestWithUserUtils.createUser(USER2, USER2, rootNodeRef, nodeService, authenticationService);
                
                // Count the number of pre-defined reports
                List<SavedSearchDetails> searches = rmSearchService.getSavedSearches(siteId);
                assertNotNull(searches);
                numberOfReports = searches.size();
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#setupMultiHierarchyTestData()
     */
    @Override
    protected void setupMultiHierarchyTestData()
    {
        super.setupMultiHierarchyTestData();
        
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                folderLevelRecordFolder = mhRecordFolder42;
                recordLevelRecordFolder = mhRecordFolder43;                
                
                recordOne = utils.createRecord(folderLevelRecordFolder, "recordOne.txt", null, "record one - folder level - elephant");
                recordTwo = utils.createRecord(folderLevelRecordFolder, "recordTwo.txt", null, "record two - folder level - snake");
                recordThree = utils.createRecord(folderLevelRecordFolder, "recordThree.txt", null, "record three - folder level - monkey");
                recordFour = utils.createRecord(recordLevelRecordFolder, "recordFour.txt", null, "record four - record level - elephant");
                recordFive = utils.createRecord(recordLevelRecordFolder, "recordFive.txt", null, "record five - record level - snake");
                recordSix = utils.createRecord(recordLevelRecordFolder, "recordSix.txt", null, "record six - record level - monkey");
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // Delete test users
                TestWithUserUtils.deleteUser(USER1, USER1, rootNodeRef, nodeService, authenticationService);
                TestWithUserUtils.deleteUser(USER2, USER2, rootNodeRef, nodeService, authenticationService);
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        super.tearDown();
    }
    
    public void testSearch()
    {
        // Full text search 
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                String query = "keywords:\"elephant\"";                
                RecordsManagementSearchParameters params = new RecordsManagementSearchParameters();
                params.setIncludeUndeclaredRecords(true);
                List<Pair<NodeRef, NodeRef>> results = rmSearchService.search(siteId, query, params);
                assertNotNull(results);
                assertEquals(2, results.size());
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        
        // Property search
        
        // 
    }
    
    public void testSaveSearch()
    {
        // Add some saved searches (as admin user)
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                SavedSearchDetails details1 = rmSearchService.saveSearch(siteId, SEARCH1, "description1", "query1", new RecordsManagementSearchParameters(), true);
                checkSearchDetails(details1, siteId, "search1", "description1", "query1", new RecordsManagementSearchParameters(), true);
                SavedSearchDetails details2 = rmSearchService.saveSearch(siteId, SEARCH2, "description2", "query2", new RecordsManagementSearchParameters(), false);
                checkSearchDetails(details2, siteId, "search2", "description2", "query2", new RecordsManagementSearchParameters(), false);
                
                return null;
            }

        }); 
        
        // Add some saved searches (as user1)
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                SavedSearchDetails details1 = rmSearchService.saveSearch(siteId, SEARCH3, "description3", "query3", new RecordsManagementSearchParameters(), false);
                checkSearchDetails(details1, siteId, SEARCH3, "description3", "query3", new RecordsManagementSearchParameters(), false);
                SavedSearchDetails details2 = rmSearchService.saveSearch(siteId, SEARCH4, "description4", "query4", new RecordsManagementSearchParameters(), false);
                checkSearchDetails(details2, siteId, SEARCH4, "description4", "query4", new RecordsManagementSearchParameters(), false);
                
                return null;
            }

        }, USER1); 
        
        // Get searches (as admin user)
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                List<SavedSearchDetails> searches = rmSearchService.getSavedSearches(siteId);
                assertNotNull(searches);
                assertEquals(numberOfReports + 2, searches.size());
                
                SavedSearchDetails search1 = rmSearchService.getSavedSearch(siteId, SEARCH1);
                assertNotNull(search1);
                checkSearchDetails(search1, siteId, "search1", "description1", "query1", new RecordsManagementSearchParameters(), true);
                
                SavedSearchDetails search2 = rmSearchService.getSavedSearch(siteId, SEARCH2);
                assertNotNull(search2);
                checkSearchDetails(search2, siteId, "search2", "description2", "query2", new RecordsManagementSearchParameters(), false);
                
                SavedSearchDetails search3 = rmSearchService.getSavedSearch(siteId, SEARCH3);
                assertNull(search3);
                
                SavedSearchDetails search4 = rmSearchService.getSavedSearch(siteId, SEARCH4);
                assertNull(search4);
                
                return null;
            }

        }); 
        
        // Get searches (as user1)
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                List<SavedSearchDetails> searches = rmSearchService.getSavedSearches(siteId);
                assertNotNull(searches);
                assertEquals(numberOfReports + 3, searches.size());
                
                SavedSearchDetails search1 = rmSearchService.getSavedSearch(siteId, SEARCH1);
                assertNotNull(search1);
                checkSearchDetails(search1, siteId, "search1", "description1", "query1", new RecordsManagementSearchParameters(), true);
                
                SavedSearchDetails search2 = rmSearchService.getSavedSearch(siteId, SEARCH2);
                assertNull(search2);
                
                SavedSearchDetails search3 = rmSearchService.getSavedSearch(siteId, SEARCH3);
                assertNotNull(search3);
                checkSearchDetails(search3, siteId, SEARCH3, "description3", "query3", new RecordsManagementSearchParameters(), false);
                
                SavedSearchDetails search4 = rmSearchService.getSavedSearch(siteId, SEARCH4);
                assertNotNull(search4);
                checkSearchDetails(search4, siteId, "search4", "description4", "query4", new RecordsManagementSearchParameters(), false);
                
                return null;
            }

        }, USER1);
        
        // Update search (as admin user)
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                SavedSearchDetails search1 = rmSearchService.getSavedSearch(siteId, SEARCH1);
                assertNotNull(search1);
                checkSearchDetails(search1, siteId, SEARCH1, "description1", "query1", new RecordsManagementSearchParameters(), true);
                
                rmSearchService.saveSearch(siteId, SEARCH1, "change", "change", new RecordsManagementSearchParameters(), true);
                
                search1 = rmSearchService.getSavedSearch(siteId, SEARCH1);
                assertNotNull(search1);
                checkSearchDetails(search1, siteId, SEARCH1, "change", "change", new RecordsManagementSearchParameters(), true);
                
                return null;                
            }
        });
        
        // Delete searches (as admin user)  
        // TODO
    }
    
    /**
     * Check the details of the saved search.
     */
    private void checkSearchDetails(
                    SavedSearchDetails details, 
                    String siteid, 
                    String name, 
                    String description, 
                    String query, 
                    RecordsManagementSearchParameters searchParameters, 
                    boolean isPublic)
    {
        assertNotNull(details);
        assertEquals(siteid, details.getSiteId());
        assertEquals(name, details.getName());
        assertEquals(description, details.getDescription());
        assertEquals(query, details.getSearch());
        assertEquals(isPublic, details.isPublic());
        
        assertEquals(searchParameters.getMaxItems(), details.getSearchParameters().getMaxItems());
        assertEquals(searchParameters.isIncludeRecords(), details.getSearchParameters().isIncludeRecords());
        assertEquals(searchParameters.isIncludeUndeclaredRecords(), details.getSearchParameters().isIncludeUndeclaredRecords());
        assertEquals(searchParameters.isIncludeVitalRecords(), details.getSearchParameters().isIncludeVitalRecords());
        assertEquals(searchParameters.isIncludeRecordFolders(), details.getSearchParameters().isIncludeRecordFolders());
        assertEquals(searchParameters.isIncludeFrozen(), details.getSearchParameters().isIncludeFrozen());
        assertEquals(searchParameters.isIncludeCutoff(), details.getSearchParameters().isIncludeCutoff());
        
        // Check the other stuff ....
    }
}
