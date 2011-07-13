/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.security.authority.script;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ScriptPagingDetails;
import org.springframework.context.ApplicationContext;

/**
 * Tests for the Script wrapper for the Authority Service,
 *  ScriptAuthorityService
 */
public class ScriptAuthorityServiceTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private static final String GROUP_A = "testGroupA";
    private static final String GROUP_A_FULL = "GROUP_testGroupA";
    private static final String GROUP_B = "testGroupB";
    private static final String GROUP_B_FULL = "GROUP_testGroupB";
    private static final String GROUP_C = "testGroupC";
    private static final String GROUP_C_FULL = "GROUP_testGroupC";
    private static final String USER_A = "testUserA";
    private static final String USER_B = "testUserB";
    private static final String USER_C = "testUserC";

    private AuthenticationComponent authenticationComponentImpl;

    private MutableAuthenticationService authenticationService;

    private MutableAuthenticationDao authenticationDAO;

    private AuthorityService authorityService;

    private AuthorityService pubAuthorityService;

    private PersonService personService;

    private UserTransaction tx;

    private AclDAO aclDaoComponent;

    private NodeService nodeService;
    
    private ScriptAuthorityService service;

    public ScriptAuthorityServiceTest()
    {
        super();

    }

    public void setUp() throws Exception
    {
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new AlfrescoRuntimeException(
                    "A previous tests did not clean up transaction: " +
                    AlfrescoTransactionSupport.getTransactionId());
        }
        
        authenticationComponentImpl = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        authenticationService = (MutableAuthenticationService) ctx.getBean("authenticationService");
        authorityService = (AuthorityService) ctx.getBean("authorityService");
        pubAuthorityService = (AuthorityService) ctx.getBean("AuthorityService");
        personService = (PersonService) ctx.getBean("personService");
        authenticationDAO = (MutableAuthenticationDao) ctx.getBean("authenticationDao");
        aclDaoComponent = (AclDAO) ctx.getBean("aclDAO");
        nodeService = (NodeService) ctx.getBean("nodeService");
        service = (ScriptAuthorityService) ctx.getBean("authorityServiceScript");

        authenticationComponentImpl.setSystemUserAsCurrentUser();

        // Clean up the users if they're already there
        TransactionService transactionService = (TransactionService) ctx.getBean(ServiceRegistry.TRANSACTION_SERVICE.getLocalName());
        tx = transactionService.getUserTransaction();
        tx.begin();
        for (String user : new String[] { USER_A, USER_B, USER_C })
        {
             if (personService.personExists(user))
             {
                 NodeRef person = personService.getPerson(user);
                 NodeRef hf = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
                 if (hf != null)
                 {
                     nodeService.deleteNode(hf);
                 }
                 aclDaoComponent.deleteAccessControlEntries(user);
                 personService.deletePerson(user);
             }
             if (authenticationDAO.userExists(user))
             {
                 authenticationDAO.deleteUser(user);
             }
        }
        
        // And the group
        if (authorityService.authorityExists(GROUP_A_FULL))
        {
           authorityService.deleteAuthority(GROUP_A_FULL);
        }
        if (authorityService.authorityExists(GROUP_B_FULL))
        {
           authorityService.deleteAuthority(GROUP_B_FULL);
        }
        if (authorityService.authorityExists(GROUP_C_FULL))
        {
           authorityService.deleteAuthority(GROUP_C_FULL);
        }
        tx.commit();

        // Now re-create them
        tx = transactionService.getUserTransaction();
        tx.begin();

        authorityService.createAuthority(AuthorityType.GROUP, GROUP_A);
        authorityService.createAuthority(AuthorityType.GROUP, GROUP_B);
        authorityService.createAuthority(AuthorityType.GROUP, GROUP_C);
        
        for (String user : new String[] { USER_A, USER_B, USER_C })
        {
           if (!authenticationDAO.userExists(user))
           {
               authenticationService.createAuthentication(user, user.toCharArray());
           }
           
           Map<QName,Serializable> props = new HashMap<QName, Serializable>();
           props.put(ContentModel.PROP_USERNAME, user);
           props.put(ContentModel.PROP_FIRSTNAME, user);
           props.put(ContentModel.PROP_LASTNAME, "Last_" + user);
           personService.createPerson(props);
        }
    }

    @Override
    protected void tearDown() throws Exception
    {
        if ((tx.getStatus() == Status.STATUS_ACTIVE) || (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK))
        {
            tx.rollback();
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    public void testBasics()
    {
       // Should return the same count for the root groups
       int count = pubAuthorityService.getAllRootAuthorities(AuthorityType.GROUP).size();
       ScriptGroup[] groups = service.getAllRootGroups();
       assertEquals(count, groups.length);
       
       // And our test ones are in there
       ScriptGroup groupA = null;
       ScriptGroup groupB = null;
       ScriptGroup groupC = null;
       for(ScriptGroup group : groups)
       {
          if (group.getShortName().equals(GROUP_A)) groupA = group;
          if (group.getShortName().equals(GROUP_B)) groupB = group;
          if (group.getShortName().equals(GROUP_C)) groupC = group;
       }
       assertNotNull(GROUP_A + " not found in " + groups, groupA);
       assertNotNull(GROUP_B + " not found in " + groups, groupB);
       assertNotNull(GROUP_C + " not found in " + groups, groupC);
       
       // Check group A in more detail
       assertEquals(GROUP_A, groupA.getShortName());
       assertEquals(GROUP_A, groupA.getDisplayName());
       assertEquals(GROUP_A_FULL, groupA.getFullName());
       
       // return the NodeRef for the group and check
       ScriptNode groupANode = groupA.getGroupNode();
       assertEquals(groupA.getDisplayName(), groupANode.getProperties().get("authorityDisplayName"));
       assertEquals(groupA.getFullName(), groupANode.getProperties().get("authorityName"));
    }

    public void testZones()
    {
    }
    
    public void testGetGroups()
    {
        // find all the groups that start with "test"
        ScriptGroup[] groups = service.getGroupsInZone("test", AuthorityService.ZONE_APP_DEFAULT, new ScriptPagingDetails(10,0), null);
        assertEquals(3, groups.length);
        
        // find a certain group
        groups = service.getGroupsInZone(GROUP_A, AuthorityService.ZONE_APP_DEFAULT, new ScriptPagingDetails(10,0), null);
        assertEquals(1, groups.length);
        
        // make sure a contains query falls back to lucene
        groups = service.getGroupsInZone("*Group", AuthorityService.ZONE_APP_DEFAULT, new ScriptPagingDetails(10,0), null);
        assertEquals(3, groups.length);
        
        // make sure a ? wildcard query falls back to lucene
        groups = service.getGroupsInZone("t?st", AuthorityService.ZONE_APP_DEFAULT, new ScriptPagingDetails(10,0), null);
        assertEquals(3, groups.length);
        
        // make sure we support getting all results
        groups = service.getGroupsInZone("*", AuthorityService.ZONE_APP_DEFAULT, new ScriptPagingDetails(10,0), null);
        assertEquals(5, groups.length);
        
        // ensure paging works, query for all results but just return 1 per page
        groups = service.getGroupsInZone("test", AuthorityService.ZONE_APP_DEFAULT, new ScriptPagingDetails(2,2), "displayName");
        assertEquals(1, groups.length);
        assertEquals(GROUP_C, groups[0].getShortName());
    }
    
    public void testFindGroups()
    {
       // Put one group inside another
       pubAuthorityService.addAuthority(GROUP_A_FULL, GROUP_B_FULL);

       // Check groups
       ScriptGroup[] groups = service.searchGroups(
             GROUP_A, new ScriptPagingDetails(10,0), "default");
       
       assertEquals(1, groups.length);
       assertEquals(GROUP_A, groups[0].getShortName());
       
       groups = service.searchGroups(
             GROUP_A.substring(0, GROUP_A.length()-1), new ScriptPagingDetails(10,0), "default");
       
       assertEquals(3, groups.length);
       assertEquals(GROUP_A, groups[0].getShortName());
       assertEquals(GROUP_B, groups[1].getShortName());
       assertEquals(GROUP_C, groups[2].getShortName());
       
       // Check groups with paging
       groups = service.searchGroups(
             GROUP_A.substring(0, GROUP_A.length()-1), new ScriptPagingDetails(2,0), "default");
       
       assertEquals(2, groups.length);
       assertEquals(GROUP_A, groups[0].getShortName());
       assertEquals(GROUP_B, groups[1].getShortName());
       
       groups = service.searchGroups(
             GROUP_A.substring(0, GROUP_A.length()-1), new ScriptPagingDetails(2, 2), "default");
       
       assertEquals(1, groups.length);
       assertEquals(GROUP_C, groups[0].getShortName());
       
       
       // Check root groups
       groups = service.searchRootGroups(
             GROUP_A, new ScriptPagingDetails(10,0), "default");
       
       assertEquals(1, groups.length);
       assertEquals(GROUP_A, groups[0].getShortName());
       
       groups = service.searchRootGroups(
             GROUP_A.substring(0, GROUP_A.length()-1)+"*", new ScriptPagingDetails(10,0), "default");
       
       assertEquals(2, groups.length);
       assertEquals(GROUP_A, groups[0].getShortName());
       assertEquals(GROUP_C, groups[1].getShortName());
       
       
       // Now with zones
       Set<String> zones = new HashSet<String>();
       zones.add(AuthorityService.ZONE_APP_SHARE);
       pubAuthorityService.addAuthorityToZones(GROUP_A_FULL, zones);
       zones.add(AuthorityService.ZONE_APP_WCM);
       pubAuthorityService.addAuthorityToZones(GROUP_B_FULL, zones);
       
       groups = service.searchGroupsInZone(
             GROUP_A.substring(0, GROUP_A.length()-1), AuthorityService.ZONE_APP_SHARE,  
             new ScriptPagingDetails(10,0), "default");
       
       assertEquals(2, groups.length);
       assertEquals(GROUP_A, groups[0].getShortName());
       assertEquals(GROUP_B, groups[1].getShortName());
       
       groups = service.searchGroupsInZone(
             GROUP_A.substring(0, GROUP_A.length()-1), AuthorityService.ZONE_APP_WCM,  
             new ScriptPagingDetails(10,0), "default");
       
       assertEquals(1, groups.length);
       assertEquals(GROUP_B, groups[0].getShortName());

       
       // And root groups in zones 
       groups = service.searchRootGroupsInZone(
             GROUP_A.substring(0, GROUP_A.length()-1)+"*", AuthorityService.ZONE_APP_SHARE,  
             new ScriptPagingDetails(10,0), "default");
       
       assertEquals(1, groups.length);
       assertEquals(GROUP_A, groups[0].getShortName());
       
       groups = service.searchRootGroupsInZone(
             GROUP_A.substring(0, GROUP_A.length()-1)+"*", AuthorityService.ZONE_APP_WCM,  
             new ScriptPagingDetails(10,0), "default");
       
       // B apparently counts as a root group in the WCM zone as it's
       //  parent group A isn't in that zone too
       assertEquals(1, groups.length);
       assertEquals(GROUP_B, groups[0].getShortName());
    }
    
    public void testGroupUsers()
    {
       ScriptGroup groupA = service.getGroup(GROUP_A);
       
       // Check on a group with no users
       assertEquals(0, groupA.getUserCount());
       ScriptUser[] users = groupA.getAllUsers();
       assertEquals(0, users.length);
       
       // Add some users to the group
       authorityService.addAuthority(GROUP_A_FULL, USER_A);
       authorityService.addAuthority(GROUP_A_FULL, USER_B);
       
       // Now look for the users on that group
       groupA = service.getGroup(GROUP_A);
       assertEquals(2, groupA.getUserCount());
       
       users = groupA.getAllUsers();
       assertEquals(2, users.length);
       
       ScriptUser userA = null;
       ScriptUser userB = null;
       for(ScriptUser user : users)
       {
          if (user.getFullName().equals(USER_A)) userA = user;
          if (user.getFullName().equals(USER_B)) userB = user;
       }
       assertNotNull(userA);
       assertNotNull(userB);
       
       assertEquals(USER_A, userA.getPerson().getProperties().get("userName"));
       assertEquals(USER_B, userB.getPerson().getProperties().get("userName"));
    }
    
    public void testUsers()
    {
       // Getting by username
       ScriptUser userA = service.getUser(USER_A);
       ScriptUser userB = service.getUser(USER_B);
       ScriptUser userC = service.getUser(USER_C);
       ScriptUser userNA = service.getUser("DOESnotEXISTinTHEsystem");
       assertNotNull(userA);
       assertNotNull(userB);
       assertNotNull(userC);
       assertNull(userNA);
       
       // Check the details on one user
       assertEquals(USER_A, userA.getFullName());
       assertEquals(USER_A, userA.getShortName());
       assertEquals(USER_A, userA.getDisplayName());
       
       NodeRef nodeA = personService.getPerson(USER_A, false);
       assertNotNull(nodeA);
       
       // Check the person
       assertEquals(nodeA, userA.getPersonNodeRef());
       assertEquals(nodeA, userA.getPerson().getNodeRef());
       assertEquals(USER_A, userA.getPerson().getProperties().get("userName"));
       assertEquals(USER_A, userA.getPerson().getProperties().get("firstName"));
    }
    
    public void testFindUsers()
    {
       // Try to find admin
       ScriptUser[] users = service.searchUsers(
             AuthenticationUtil.getAdminUserName(),
             new ScriptPagingDetails(10, 0),
             "userName");
       
       assertTrue("Admin not found", users.length > 0);
       
       // Try to find our test users
       users = service.searchUsers(
             USER_A.substring(0, USER_A.length()-1),
             new ScriptPagingDetails(10, 0),
             "userName");
       
       assertEquals("Users count wrong " + users, 3, users.length);
       
       // Check on the username sorting
       assertEquals(USER_A, users[0].getPerson().getProperties().get("userName"));
       assertEquals(USER_B, users[1].getPerson().getProperties().get("userName"));
       assertEquals(USER_C, users[2].getPerson().getProperties().get("userName"));
       
       // Tweak names and re-check 
       ScriptUser userA = users[0];
       ScriptUser userB = users[1];
       ScriptUser userC = users[2];
       nodeService.setProperty(userB.getPersonNodeRef(), ContentModel.PROP_FIRSTNAME, "bbbbFIRST");
       nodeService.setProperty(userC.getPersonNodeRef(), ContentModel.PROP_LASTNAME, "ccccLAST");
       
       users = service.searchUsers(
             USER_A.substring(0, USER_A.length()-1),
             new ScriptPagingDetails(10, 0),
             "userName");
       
       assertEquals("Users count wrong " + users, 3, users.length);
       assertEquals(USER_A, users[0].getPerson().getProperties().get("userName"));
       assertEquals(USER_B, users[1].getPerson().getProperties().get("userName"));
       assertEquals(USER_C, users[2].getPerson().getProperties().get("userName"));
       
       // Check sorting on firstname
       users = service.searchUsers(
             USER_A.substring(0, USER_A.length()-1),
             new ScriptPagingDetails(10, 0),
             "firstName");
       
       assertEquals("Users count wrong " + users, 3, users.length);
       assertEquals(USER_B, users[0].getPerson().getProperties().get("userName"));
       assertEquals(USER_A, users[1].getPerson().getProperties().get("userName"));
       assertEquals(USER_C, users[2].getPerson().getProperties().get("userName"));
       
       // And lastname
       users = service.searchUsers(
             USER_A.substring(0, USER_A.length()-1),
             new ScriptPagingDetails(10, 0),
             "lastName");
       
       assertEquals("Users count wrong " + users, 3, users.length);
       assertEquals(USER_C, users[0].getPerson().getProperties().get("userName"));
       assertEquals(USER_A, users[1].getPerson().getProperties().get("userName"));
       assertEquals(USER_B, users[2].getPerson().getProperties().get("userName"));
    }
}
