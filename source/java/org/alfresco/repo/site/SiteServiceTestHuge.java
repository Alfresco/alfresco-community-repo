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
package org.alfresco.repo.site;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintStream;
import java.util.Set;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Site service unit test that targets HUGE numbers of sites
 * 
 * @author Alan Davis
 */
public class SiteServiceTestHuge
{
    private enum Allocation
    {
        /**
         * Allocate source (groups) to target (sites) on a round robin basis
         * until all targets (sites) have been allocated a single source (group).
         * 
         * Some source (groups) might have been allocated to more than one target
         * (site) or none at all.
         **/
        ROUND_ROBIN_TO_TARGET,
        
        /**
         * Allocate source (users) to target (groups) on a round robin basis
         * until all source (users) have been allocated a target (group).
         * 
         * Some target (groups) might have been allocated to more than one source
         * (user) or none at all.
         **/
        ROUND_ROBIN_FROM_SOURCE,
        
        /**
         * Allocate source (users) to target (groups) on a round robin basis
         * until all source (users) AND all target (groups) have been allocated
         * to at least one a target (group) or one source (user).
         * 
         * If there are more target (groups) than source (users)
         * then some source (users) will be in more than one target (group).
         * 
         * If there are more source (users) than target (groups) then some
         * target (groups) will contain more than one source (user).
         **/
        ROUND_ROBIN_BOTH,
        
        /**
         * Allocate all source (users) to each target (group).
         * OR
         * Allocate all source (users) to each target (site).
         * OR
         * Allocate all source (groups) to each target (site).
         **/
        ALL_TO_EACH,
        
        /** No allocation **/
        NONE
    }

    private enum OnFailure
    {
        GIVE_UP,
        KEEP_GOING
    }
    
    // Standard numbers of users, groups and sites
    private static final int NUM_USERS = 100;
    private static final int NUM_GROUPS = 60;
    private static int NUM_SITES = 60000;
    
    private static final String ADMIN_USER = "admin";
    
    // Max times in ms for various activities
    private static final long SECOND = 1000;
    private static final long FIVE_SECONDS = SECOND * 5;

    private static final long MAX_CREATE_USER_MS = FIVE_SECONDS;
    private static final long MAX_CREATE_GROUP_MS = FIVE_SECONDS;
    private static final long MAX_CREATE_SITE_MS = FIVE_SECONDS;

    private static final long MAX_DELETE_USER_MS = FIVE_SECONDS;
    private static final long MAX_DELETE_GROUP_MS = FIVE_SECONDS;
    private static final long MAX_DELETE_SITE_MS = FIVE_SECONDS;
    
    private static final long MAX_USER_TO_GROUP_MS = FIVE_SECONDS;
    private static final long MAX_USER_TO_SITE_MS = FIVE_SECONDS;
    private static final long MAX_GROUP_TO_SITE_MS = FIVE_SECONDS;

    // Used to save having to check if users, groups and sites exist if already created by this test. 
    private static int usersCreated = 0;
    private static int groupsCreated = 0;
    private static int sitesCreated = 0;

    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    private static TransactionService transactionService = (TransactionService)ctx.getBean("TransactionService");
    private static AuthenticationComponent authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
    private static MutableAuthenticationService authenticationService = (MutableAuthenticationService)ctx.getBean("authenticationService");
    private static PersonService personService = (PersonService)ctx.getBean("PersonService");
    private static AuthorityService authorityService = (AuthorityService)ctx.getBean("AuthorityService");
    private static SiteService siteService = (SiteService)ctx.getBean("SiteService"); // Big 'S'
    
    private static String logFilename;
    private static PrintStream log;
   
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        logFilename = "sites.log";
    }
    
    @AfterClass
    public static void tearDownClass()
    {
        if (log != null)
        {
            log.close();
        }
    }

    @Before
    public void setUp() throws Exception
    {
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    }

    @After
    public void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
    }
    
    private void log(String msg) throws Exception
    {
        System.out.println(msg);
        if (logFilename != null)
        {
            if (log == null)
            {
                log = new PrintStream(new File(logFilename));
            }
            log.println(msg);
        }
    }

    // ------------------ main test helper methods --------------------
    
    private void createAndAllocate(int userCount, int groupCount, int siteCount,
            Allocation usersToGroups, Allocation usersToSites, Allocation groupsToSites)
            throws Exception
    {
        createUsersGroupsAndSites(userCount, groupCount, siteCount);
        allocateUsersToGroupsAndSites(userCount, groupCount, siteCount, usersToGroups,
                usersToSites, groupsToSites);
    }
    
    private void createUsersGroupsAndSites(int userCount, int groupCount, int siteCount)
            throws Exception
    {
        createUsers(userCount);
        createGroups(groupCount);
        createSites(siteCount, userCount, 0, OnFailure.GIVE_UP);
    }

    private void allocateUsersToGroupsAndSites(int userCount, int groupCount, int siteCount,
            Allocation usersToGroups, Allocation usersToSites, Allocation groupsToSites)
            throws Exception
    {
        if ((usersToGroups == Allocation.ALL_TO_EACH &&
             usersToSites == Allocation.NONE &&
             groupsToSites == Allocation.ROUND_ROBIN_TO_TARGET)
            ||
            (usersToGroups == Allocation.NONE &&
             usersToSites == Allocation.ALL_TO_EACH &&
             groupsToSites == Allocation.NONE))
        {
            allocateUsersToGroups(userCount, groupCount, usersToGroups);
            allocateUsersToSites(userCount, siteCount, usersToSites);
            allocateGroupsToSites(groupCount, siteCount, groupsToSites, userCount, 0, OnFailure.GIVE_UP);
        }
        else
        {
            fail("Users are not able to see all sites or can see a site directly and via a group");
        }
    }

    private void addMoreSites(int sitesToAdd, OnFailure onFailureAction) throws Exception
    {
        log("\n\n ADD "+sitesToAdd+" MORE SITES AND ADD A GROUP TO EACH");
        Allocation groupsToSites = Allocation.ROUND_ROBIN_TO_TARGET;
        int sitesAlreadyCreated = sitesCreated;
        int siteCount = sitesAlreadyCreated + sitesToAdd;
        
        long ms = System.currentTimeMillis();
        createSites(siteCount, NUM_USERS, sitesAlreadyCreated, OnFailure.GIVE_UP);
        allocateGroupsToSites(NUM_GROUPS, siteCount, groupsToSites, NUM_USERS, sitesAlreadyCreated, OnFailure.KEEP_GOING);
        assertTime("Add more sites", sitesAlreadyCreated+1, sitesToAdd, ms, MAX_CREATE_SITE_MS+MAX_GROUP_TO_SITE_MS, onFailureAction, sitesToAdd);
    }
    
    private void assertTime(String what, int id1, int id2, long start, long max) throws Exception
    {
        assertTime(what, id1, id2, start, max, OnFailure.GIVE_UP, 1);
    }

    private void assertTime(String what, int id1, int id2, long start, long max, OnFailure onFailureAction, int blockSize) throws Exception
    {
        long ms = (System.currentTimeMillis() - start)/blockSize;
        
        String msg = what+","+id1+(id2 > 0 ? ","+id2 : "")+","+ms+",ms"+((blockSize == 1) ? "" : " average for,"+blockSize);
        log(msg);
        if (ms > max && onFailureAction == OnFailure.GIVE_UP)
        {
            fail(msg+" is longer than "+max);
        }
    }

    // ------------------ create N --------------------
    
    // Creates users and removes extra ones
    private void createUsers(int userCount) throws Exception
    {
        for (int userId=1; ; userId++)
        {
            String userName = getUserName(userId);
            boolean exists = (userId <= usersCreated) || personService.personExists(userName);
            
            if (userId <= userCount)
            {
                if (!exists)
                {
                    long ms = System.currentTimeMillis();
                    createUser(userName);
                    assertTime("Create user", userId, -1, ms, MAX_CREATE_USER_MS);
                }
            }
            else
            {
                if (!exists)
                {
                    break;
                }
                long ms = System.currentTimeMillis();
                deleteUser(userName);
                assertTime("Delete user", userId, -1, ms, MAX_DELETE_USER_MS);
            }
        }
        usersCreated = userCount;
    }
    
    // Creates groups and removes extra ones
    private void createGroups(int groupCount) throws Exception
    {
        for (int groupId=1; ; groupId++)
        {
            String groupName = getGroupName(groupId);
            String groupAuthorityName = authorityService.getName(AuthorityType.GROUP, groupName);
            boolean exists = (groupId <= groupsCreated) || authorityService.authorityExists(groupAuthorityName);
            
            if (groupId <= groupCount)
            {
                if (!exists)
                {
                    long ms = System.currentTimeMillis();
                    createGroup(groupName);
                    assertTime("Create group", groupId, -1, ms, MAX_CREATE_GROUP_MS);
                }
            }
            else
            {
                if (!exists)
                {
                    break;
                }
                long ms = System.currentTimeMillis();
                deleteGroup(groupName);
                assertTime("Delete group", groupId, -1, ms, MAX_DELETE_GROUP_MS);
            }
        }
        groupsCreated = groupCount;
    }
    
    // Creates sites and removes extra ones
    private void createSites(int siteCount, int userCount, int sitesAlreadyCreated, OnFailure onFailureAction) throws Exception
    {
        for (int siteId=sitesAlreadyCreated+1; ; siteId++)
        {
            String siteName = getSiteName(siteId);
            boolean exists = (siteId <= sitesCreated) || siteService.getSite(siteName) != null;
            
            if (siteId <= siteCount)
            {
                if (!exists)
                {
                    String siteOwnerUserName = getSiteOwnerUserName(siteId, userCount);
                    creatSite(siteId, siteOwnerUserName, onFailureAction);
               }
            }
            else
            {
                if (!exists)
                {
                    if (siteId >= siteCount + 1)
                        break;
                }
                else
                {
                    long ms = System.currentTimeMillis();
                    deleteSite(siteName, null);
                    assertTime("Delete site", siteId, -1, ms, MAX_DELETE_SITE_MS);
                }
            }
        }
        sitesCreated = siteCount;
    }

    private void deleteSites(int fromSiteId, int toSiteId, OnFailure onFailureAction) throws Exception
    {
        log("\n\n TIDY UP: DELETE SITES "+fromSiteId+" TO "+toSiteId);
        for (int siteId = fromSiteId; siteId <= toSiteId; siteId++)
        {
            try
            {
                deleteSite(siteId, null, onFailureAction);
            }
            catch (Exception e)
            {
                // move on
            }
        }
        sitesCreated = fromSiteId-1;
    }

    // ------------------ create 1 --------------------
    
    private void createUser(String userName) throws Exception
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();

            authenticationService.createAuthentication(userName, userName.toCharArray());

            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, userName.substring(0, userName.length()-4));
            ppOne.put(ContentModel.PROP_LASTNAME, "user");
            ppOne.put(ContentModel.PROP_EMAIL, userName + "@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            personService.createPerson(ppOne);

            txn.commit();
        }
        catch (Exception e)
        {
            txn.rollback();
            throw e;
        }
    }
    
    private void createGroup(String groupName) throws Exception
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();

            authorityService.createAuthority(AuthorityType.GROUP, groupName);

            txn.commit();
        }
        catch (Exception e)
        {
            txn.rollback();
            throw e;
        }
    }
    
    private void creatSite(int siteId, String doAsUser, OnFailure onFailureAction) throws Exception
    {
        String siteName = getSiteName(siteId);
        long ms = System.currentTimeMillis();
        creatSite(siteName, doAsUser);
        assertTime("Create site", siteId, -1, ms, MAX_CREATE_SITE_MS, onFailureAction, 1);
    }
    
    private void creatSite(String siteName, String doAsUser) throws Exception
    {
        String currentUser = authenticationComponent.getCurrentUserName();
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            if (doAsUser != null)
                authenticationComponent.setCurrentUser(doAsUser);
            txn.begin();

            if (siteService.getSite(siteName) == null)
            {
                String sitePreset = "site-dashboard";
                siteService.createSite(sitePreset, siteName, "Title for " + siteName, "Description for "
                        + siteName, SiteVisibility.PUBLIC);
                
                // TODO Should do the following rather than the createContainers - not sure how
//              Map<String, String> tokens = new HashMap<String, String>();
//              tokens.put("siteid", siteName);
//              presetsManager.constructPreset(tokens, tokens);
                siteService.createContainer(siteName, "documentLibrary", ContentModel.TYPE_FOLDER, null);
                siteService.createContainer(siteName, "links", ContentModel.TYPE_FOLDER, null);
            }

            txn.commit();
        }
        catch (Exception e)
        {
            try
            {
                txn.rollback();
            }
            catch (Exception e2)
            {
            }
            throw e;
        }
        finally
        {
            authenticationComponent.setCurrentUser(currentUser);
        }
    }
    
    // ------------------ delete 1 --------------------

    private void deleteUser(String userName) throws Exception
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();

            personService.deletePerson(userName);

            txn.commit();
        }
        catch (Exception e)
        {
            txn.rollback();
            throw e;
        }
    }
    
    private void deleteGroup(String groupName) throws Exception
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();

            String groupAuthorityName = authorityService.getName(AuthorityType.GROUP, groupName);
            authorityService.deleteAuthority(groupAuthorityName, true);

            txn.commit();
        }
        catch (Exception e)
        {
            txn.rollback();
            throw e;
        }
    }
    
    private void deleteSite(int siteId, String doAsUser, OnFailure onFailureAction) throws Exception
    {
        String siteName = getSiteName(siteId);
        long ms = System.currentTimeMillis();
        deleteSite(siteName, doAsUser);
        assertTime("Delete site", siteId, -1, ms, MAX_DELETE_SITE_MS, onFailureAction, 1);
    }
    
    private void deleteSite(String siteName, String doAsUser) throws Exception
    {
        String currentUser = authenticationComponent.getCurrentUserName();
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            if (doAsUser != null)
                authenticationComponent.setCurrentUser(doAsUser);
            txn.begin();

            siteService.deleteSite(siteName);

            txn.commit();
        }
        catch (Exception e)
        {
            try
            {
                txn.rollback();
            }
            catch (Exception e2)
            {
            }
            throw e;
        }
        finally
        {
            authenticationComponent.setCurrentUser(currentUser);
        }
    }   
    
    // ------------------ allocate N --------------------
    
    private void allocateUsersToGroups(int userCount, int groupCount, Allocation allocation)
            throws Exception
    {
        if (allocation == Allocation.ALL_TO_EACH)
        {
            for (int userId = 1; userId <= userCount; userId++)
            {
                UserTransaction txn = transactionService.getUserTransaction();
                try
                {
                    txn.begin();
                    Set<String> existingAuthorities = authorityService.getContainingAuthoritiesInZone(AuthorityType.GROUP,
					        getUserName(userId), AuthorityService.ZONE_APP_DEFAULT, null, -1);
                    for (int groupId = 1; groupId <= groupCount; groupId++)
                    {
                        if (!existingAuthorities.contains(authorityService.getName(AuthorityType.GROUP,
                                getGroupName(groupId))))
                        {
                            allocateUserToGroup(userId, groupId);
                        }
                    }
                    txn.commit();
                }
                catch (Exception e)
                {
                    txn.rollback();
                    throw e;
                }
            }
        }
        else
        {
            int iterations, groupIncrement;
            if (allocation == Allocation.ROUND_ROBIN_FROM_SOURCE)
            {
                iterations = userCount;
                groupIncrement = 1;
            }
            else
            {
                iterations = groupCount;
                groupIncrement = userCount;
            }
            int i=0;
            OUTER: while (i < iterations)
            {
                for (int userId = 1; userId <= userCount; userId++)
                {
                    UserTransaction txn = transactionService.getUserTransaction();
                    try
                    {
                        txn.begin();
                        Set<String> existingAuthorities = authorityService.getContainingAuthoritiesInZone(AuthorityType.GROUP,
                                getUserName(userId), AuthorityService.ZONE_APP_DEFAULT, null, -1);
                        for (int groupId = userId; groupId <= groupCount; groupId += groupIncrement)
                        {
                            if (!existingAuthorities.contains(authorityService.getName(AuthorityType.GROUP,
                                    getGroupName(groupId))))
                            {
                                allocateUserToGroup(userId, groupId);
                            }
                            if (++i >= iterations)
                            {
                                txn.commit();
                                break OUTER;
                            }
                        }
                        txn.commit();
                    }
                    catch (Exception e)
                    {
                        txn.rollback();
                        throw e;
                    }
                }
            }
        }
    }
    
    private void allocateUsersToSites(int userCount, int siteCount, Allocation allocation)
            throws Exception
    {
        if (allocation == Allocation.ALL_TO_EACH)
        {
            for (int userId = 1; userId <= userCount; userId++)
            {
                for (int siteId = 1; siteId <= siteCount; siteId++)
                {
                    allocateUserToSite(userId, siteId, userCount);
                }
            }
        }
        else if (allocation == Allocation.ROUND_ROBIN_TO_TARGET)
        {
            boolean sourceEnd = (allocation == Allocation.ROUND_ROBIN_TO_TARGET);
            boolean targetEnd = (allocation == Allocation.ROUND_ROBIN_FROM_SOURCE);
            for (int siteId = 1, userId = 1; ; siteId++, userId++)
            {
                if (userId > userCount)
                {
                    if (targetEnd)
                        break;
                    sourceEnd = true;
                    userId = 1;
                }
                if (siteId > siteCount)
                {
                    if (sourceEnd)
                        break;
                    targetEnd = true;
                    siteId = 1;
                }
                allocateUserToSite(userId, siteId, userCount);
            }
        }
    }
    
    private void allocateGroupsToSites(int groupCount, int siteCount, Allocation allocation, int userCount,
            int sitesAlreadyCreated, OnFailure onFailureAction) throws Exception
    {
        if (allocation == Allocation.ALL_TO_EACH)
        {
            for (int groupId = 1; groupId <= groupCount; groupId++)
            {
                for (int siteId = sitesAlreadyCreated+1; siteId <= siteCount; siteId++)
                {
                    String doAsUser = getSiteOwnerUserName(siteId, userCount);
                    allocateGroupToSite(groupId, siteId, userCount, doAsUser, onFailureAction);
                }
            }
        }
        else if (allocation == Allocation.ROUND_ROBIN_TO_TARGET)
        {
            boolean sourceEnd = (allocation == Allocation.ROUND_ROBIN_TO_TARGET);
            boolean targetEnd = (allocation == Allocation.ROUND_ROBIN_FROM_SOURCE);
            int startGroupId = sitesAlreadyCreated % groupCount + 1;
            for (int siteId = sitesAlreadyCreated+1, groupId = startGroupId; ; siteId++, groupId++)
            {
                if (groupId > groupCount)
                {
                    if (targetEnd)
                        break;
                    sourceEnd = true;
                    groupId = 1;
                }
                if (siteId > siteCount)
                {
                    if (sourceEnd)
                        break;
                    targetEnd = true;
                    siteId = 1;
                }
                String doAsUser = getSiteOwnerUserName(siteId, userCount);
                allocateGroupToSite(groupId, siteId, userCount, doAsUser, onFailureAction);
            }
        }
    }
    
    private void allocateUserToGroup(int userId, int groupId) throws Exception
    {
        String userName = getUserName(userId);
        String groupName = getGroupName(groupId);

        long ms = System.currentTimeMillis();
        allocateUserToGroup(userName, groupName);
        assertTime("Adding a user to a group", userId, groupId, ms, MAX_USER_TO_GROUP_MS);
    }

    private void allocateUserToSite(int userId, int siteId, int userCount) throws Exception
    {
        try
        {
            String userName = getUserName(userId);
            String siteName = getSiteName(siteId);
            String siteOwnerUserName = getSiteOwnerUserName(siteId, userCount);

            long ms = System.currentTimeMillis();
            allocateUserToSite(userName, siteName, siteOwnerUserName);
            assertTime("Adding a user to a site", userId, siteId, ms, MAX_USER_TO_SITE_MS);
        }
        catch (DuplicateChildNodeNameException e)
        {
            // Already allocated.
        }
    }
    
    private int getNextSiteToAddGroupTo(int firstSiteIdToCheck) throws Exception
    {
        String userName = getUserName(1);
        Set<String> existingAuthorities = authorityService.getAuthoritiesForUser(userName);
        int siteId = firstSiteIdToCheck;
        for (; siteId <= NUM_SITES; siteId++)
        {
            String siteName = getSiteName(siteId);
            String groupName = "GROUP_site_"+siteName;
            if (!existingAuthorities.contains(groupName))
            {
                break;
            }
        }
        log("Next site to add group to is "+siteId);
        return siteId;
    }
    
    private void allocateGroupToSite(int firstSiteId, int userCount, String doAsUser, OnFailure onFailureAction, int blockSize) throws Exception
    {
        String currentUser = authenticationComponent.getCurrentUserName();
        UserTransaction txn = transactionService.getUserTransaction();
        long ms1 = System.currentTimeMillis();
        
        try
        {
            if (doAsUser != null)
                authenticationComponent.setCurrentUser(doAsUser);
            txn.begin();
            for (int siteId = firstSiteId; siteId < firstSiteId+blockSize; siteId++)
            {
                try
                {
                    int groupId = (siteId - 1) % NUM_GROUPS + 1;
                    String groupName = getGroupName(groupId);
                    String siteName = getSiteName(siteId);

                    long ms2 = System.currentTimeMillis();
                    String groupAuthorityName = authorityService.getName(AuthorityType.GROUP,
                            groupName);
                    siteService.setMembership(siteName, groupAuthorityName, SiteModel.SITE_COLLABORATOR);
                    assertTime("Adding a group to a site", groupId, siteId, ms2, MAX_GROUP_TO_SITE_MS, onFailureAction, 1);
                }
                catch (DuplicateChildNodeNameException e)
                {
                    // Already allocated.
                }
            }
            txn.commit();
            assertTime("    Block Add", firstSiteId, 0, ms1, MAX_GROUP_TO_SITE_MS, onFailureAction, blockSize);
        }
        catch (Exception e)
        {
            txn.rollback();
            throw e;
        }
        finally
        {
            authenticationComponent.setCurrentUser(currentUser);
        }
    }
    
    private void allocateGroupToSite(int siteId, int userCount, String doAsUser, OnFailure onFailureAction) throws Exception
    {
        int groupId = (siteId-1) % NUM_GROUPS + 1;
        allocateGroupToSite(groupId, siteId, userCount, doAsUser, onFailureAction);
    }
    
    private void allocateGroupToSite(int groupId, int siteId, int userCount, String doAsUser, OnFailure onFailureAction) throws Exception
    {
        try
        {
            String groupName = getGroupName(groupId);
            String siteName = getSiteName(siteId);

            long ms = System.currentTimeMillis();
            allocateGroupToSite(groupName, siteName, doAsUser);
            assertTime("Adding a group to a site", groupId, siteId, ms, MAX_GROUP_TO_SITE_MS, onFailureAction, 1);
        }
        catch (DuplicateChildNodeNameException e)
        {
            // Already allocated.
        }
    }

    // ------------------ allocate 1 --------------------
    
    private void allocateUserToGroup(String userName, String groupName) throws Exception
    {
        String groupAuthorityName = authorityService.getName(AuthorityType.GROUP, groupName);
        authorityService.addAuthority(groupAuthorityName, userName);
    }
    
    private void allocateUserToSite(String userName, String siteName, String doAsUser)
            throws Exception
    {
        setSiteMembership(userName, siteName, doAsUser);
    }
    
    private void allocateGroupToSite(String groupName, String siteName, String doAsUser)
            throws Exception
    {
        String groupAuthorityName = authorityService.getName(AuthorityType.GROUP, groupName);
        setSiteMembership(groupAuthorityName, siteName, doAsUser);
    }

    private void setSiteMembership(String authority, String siteName, String doAsUser)
            throws SystemException, Exception
    {
        String currentUser = authenticationComponent.getCurrentUserName();
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            if (doAsUser != null)
                authenticationComponent.setCurrentUser(doAsUser);
            txn.begin();

            siteService.setMembership(siteName, authority, SiteModel.SITE_COLLABORATOR);

            txn.commit();
        }
        catch (Exception e)
        {
            txn.rollback();
            throw e;
        }
        finally
        {
            authenticationComponent.setCurrentUser(currentUser);
        }
    }

    // ------------------ names --------------------
    
    private String getUserName(int userId)
    {
        return getName("user", userId);
    }
    
    private String getGroupName(int groupId)
    {
        return getName("group", groupId);
    }
    
    private String getSiteName(int siteId)
    {
        return getName("site", siteId);
    }
    
    private String getName(String prefix, int id)
    {
        return new StringBuilder(prefix).append(id).toString();
    }
    
    private String getSiteOwnerUserName(int siteId, int userCount)
    {
        int ownerId = (siteId-1)%userCount+1;
        return getUserName(ownerId);
    }
        
    // ------------------ Original Tests --------------------
    
//    @Test
//    public void testSingleGroup() throws Exception
//    {
//        Allocation usersToGroups = Allocation.ALL_TO_EACH;
//        Allocation usersToSites = Allocation.NONE;
//        Allocation groupsToSites = Allocation.ROUND_ROBIN_TO_TARGET;
//
//        createAndAllocate(NUM_USERS, NUM_GROUPS, NUM_SITES, usersToGroups, usersToSites, groupsToSites);
//    }

//  @Test
//  public void testMultipleGroups() throws Exception
//  {
//      Allocation usersToGroups = Allocation.ALL_TO_EACH;
//      Allocation usersToSites = Allocation.NONE;
//      Allocation groupsToSites = Allocation.ROUND_ROBIN_TO_TARGET;
//      
//      createAndAllocate(NUM_USERS, NUM_GROUPS, NUM_SITES, usersToGroups, usersToSites, groupsToSites);
//  }

//  @Test
//  public void testNoGroups() throws Exception
//  {
//      Allocation usersToGroups = Allocation.NONE;
//      Allocation usersToSites = Allocation.ALL_TO_EACH;
//      Allocation groupsToSites = Allocation.NONE;
//      
//      createAndAllocate(NUM_USERS, NUM_GROUPS, NUM_SITES, usersToGroups, usersToSites, groupsToSites);
//  }

    // ------------------ Initial Data Load Tests --------------------

//  @Test
//  public void testInitClearDownAll() throws Exception
//  {
//      createUsersGroupsAndSites(0, 0, 0);
//  }

//    @Test
//    public void testInitCreateUsersAndGroups() throws Exception
//    {
//        createUsers(NUM_USERS);
//        createGroups(NUM_GROUPS);
//        allocateUsersToGroups(NUM_USERS, NUM_GROUPS, Allocation.ALL_TO_EACH);
//    }

//    @Test
//    public void testInit() throws Exception
//    {
//        createUsers(NUM_USERS);
//        createGroups(NUM_GROUPS);
//        allocateUsersToGroups(NUM_USERS, NUM_GROUPS, Allocation.ALL_TO_EACH);
//
//        createSites(NUM_SITES, NUM_USERS, 0, OnFailure.KEEP_GOING);
//
//        int blockSize = 10;
//        for (int siteId = getNextSiteToAddGroupTo(1); siteId <= NUM_SITES; siteId += blockSize)
//        {
//            allocateGroupToSite(siteId, NUM_USERS, ADMIN_USER, OnFailure.KEEP_GOING, blockSize);
//        }
//    }

    // ------------------ Test to load data from cmd line --------------------

    @Test
    public void commandLine() throws Exception
    {
        String from = System.getProperty("from");
        String to = System.getProperty("to");
        String restart = System.getProperty("restart");
        String action = System.getProperty("action");
        logFilename = System.getProperty("log", "sites.log");
        
        boolean usersOnly = "usersOnly".equalsIgnoreCase(action);
        boolean sites = "sites".equalsIgnoreCase(action);
        boolean groups = "groups".equalsIgnoreCase(action);
        boolean test = "test".equalsIgnoreCase(action);

        if ((usersOnly && (from != null || to != null || restart != null)) ||
            (!usersOnly && (from == null || to == null || (action != null && !sites && !groups && !test))))
        {
            System.err.println(
                    "Usage: -Dfrom=<fromSiteId> -Dto=<toSiteId>                     [ -Dlog=<logFilename> ]\n" +
                    "       -Daction=usersOnly                                      [ -Dlog=<logFilename> ]\n" +
                    "       -Daction=sites      -Dfrom=<fromSiteId> -Dto=<toSiteId> [ -Dlog=<logFilename> ] [ -Drestart=<restartAtSiteId> ]\n" +
                    "       -Daction=groups     -Dfrom=<fromSiteId> -Dto=<toSiteId> [ -Dlog=<logFilename> ] [ -Drestart=<restartAtSiteId> ]" +
                    "       -Daction=test       -Dfrom=<fromSiteId> -Dto=<toSiteId> [ -Dlog=<logFilename> ] ");
        }
        else
        {
            try
            {
                int fromId = (from == null) ? 0 : Integer.parseInt(from);
                NUM_SITES = (to == null) ? 0 : Integer.parseInt(to);
                int restartFromId = (restart == null) ? fromId : Integer.parseInt(restart);

                if (test)
                {
                    testAddingSitesAndDelete(fromId, NUM_SITES);
                }
                else
                {
                    if (action == null || usersOnly)
                    {
                        createUsers(NUM_USERS);
                        createGroups(NUM_GROUPS);
                        allocateUsersToGroups(NUM_USERS, NUM_GROUPS, Allocation.ALL_TO_EACH);
                        if (action == null)
                        {
                            sites = true;
                        }
                    }

                    if (sites)
                    {
                        createSites(NUM_SITES, NUM_USERS, restartFromId - 1, OnFailure.KEEP_GOING);
                        restartFromId = fromId;
                        groups = true;
                    }

                    if (groups)
                    {
                        int blockSize = 10;
                        for (int siteId = getNextSiteToAddGroupTo(restartFromId); siteId <= NUM_SITES; siteId += blockSize)
                        {
                            int size = Math.min(blockSize, NUM_SITES - siteId + 1);
                            allocateGroupToSite(siteId, NUM_USERS, ADMIN_USER, OnFailure.KEEP_GOING, size);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                log("\n\n DONE");
            }
        }
    }
    
    /**
     * Simplify running unit test from the command line.
     * 
     * set SITE_CPATH=%TOMCAT_HOME%/lib/*;%TOMCAT_HOME%/endorsed/*;%TOMCAT_HOME%/webapps/alfresco/WEB-INF/lib/*;%TOMCAT_HOME%/webapps/alfresco/WEB-INF/classes;%TOMCAT_HOME%/shared/classes;
     * java -Xmx2048m -XX:MaxPermSize=512M -classpath %SITE_CPATH% org.alfresco.repo.site.SiteServiceTestHuge ...
     */
    public static void main(String args[])
    {
        org.junit.runner.JUnitCore.main(SiteServiceTestHuge.class.getName());
    }
    
    // ------------------ Tests Once Data Is Loaded --------------------
    
//    @Test
//    public void testAdding1000SitesInBlocksOf100() throws Exception
//    {
//        usersCreated = NUM_USERS;
//        groupsCreated = NUM_GROUPS;
//        sitesCreated = NUM_SITES;
//
//        deleteSites(NUM_SITES+1, NUM_SITES+1000, OnFailure.KEEP_GOING);
//        
//        for (int i=1; i<=10; i++)
//        {
//            addMoreSites(100, OnFailure.GIVE_UP);
//        }
//        
//        deleteSites(NUM_SITES+1, NUM_SITES+1000, OnFailure.KEEP_GOING);
//    }
    
//    @Test
//    public void testAdding4SitesAndDelete() throws Exception
//    {
//        testAddingSitesAndDelete(NUM_SITES + 1, NUM_SITES + 100);
//    }
    
    public void testAddingSitesAndDelete(int fromSiteId, int toSiteId) throws Exception
    {
        usersCreated = NUM_USERS;
        groupsCreated = NUM_GROUPS;
        sitesCreated = fromSiteId - 1;

        deleteSites(fromSiteId, toSiteId, OnFailure.KEEP_GOING);

        log("\n\n CREATE SITES");
        for (int siteId = fromSiteId; siteId <= toSiteId; siteId++)
        {
            String siteCreatorUser = getSiteOwnerUserName(siteId, NUM_USERS);
            creatSite(siteId, siteCreatorUser, OnFailure.KEEP_GOING);
        }

        log("\n\n ADD GROUPS");
        for (int siteId = fromSiteId; siteId <= toSiteId; siteId++)
        {
            String siteCreatorUser = getSiteOwnerUserName(siteId, NUM_USERS);
            allocateGroupToSite(siteId, NUM_USERS, siteCreatorUser, OnFailure.KEEP_GOING);
        }

        log("\n\n DELETE");
        for (int siteId = fromSiteId; siteId <= toSiteId; siteId++)
        {
            String siteCreatorUser = getSiteOwnerUserName(siteId, NUM_USERS);
            deleteSite(siteId, siteCreatorUser, OnFailure.KEEP_GOING);
        }
    }
}
