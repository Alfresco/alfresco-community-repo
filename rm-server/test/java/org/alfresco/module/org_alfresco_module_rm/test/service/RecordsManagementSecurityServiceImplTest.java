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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.Role;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;

/**
 * Event service implementation unit test
 * 
 * @author Roy Wetherall
 */
public class RecordsManagementSecurityServiceImplTest extends BaseSpringTest 
                                                      implements RecordsManagementModel
{    
    protected static StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    
	private NodeService nodeService;
	private MutableAuthenticationService authenticationService;
	private AuthorityService authorityService;
	private PermissionService permissionService;
	private PersonService personService;
	private RecordsManagementSecurityService rmSecurityService;
	private RecordsManagementActionService rmActionService;
	private RetryingTransactionHelper transactionHelper;
	private CapabilityService capabilityService;
	
	@Override
	protected void onSetUpInTransaction() throws Exception 
	{
		super.onSetUpInTransaction();

		// Get the service required in the tests
		this.nodeService = (NodeService)this.applicationContext.getBean("NodeService"); 
		this.authenticationService = (MutableAuthenticationService)this.applicationContext.getBean("AuthenticationService");
		this.personService = (PersonService)this.applicationContext.getBean("PersonService");
		this.authorityService = (AuthorityService)this.applicationContext.getBean("authorityService");
		this.rmSecurityService = (RecordsManagementSecurityService)this.applicationContext.getBean("RecordsManagementSecurityService");
		this.transactionHelper = (RetryingTransactionHelper)this.applicationContext.getBean("retryingTransactionHelper");
		this.permissionService = (PermissionService)this.applicationContext.getBean("PermissionService");
		this.rmActionService = (RecordsManagementActionService)this.applicationContext.getBean("RecordsManagementActionService");
		this.capabilityService = (CapabilityService)this.applicationContext.getBean("CapabilityService");
		
		// Set the current security context as admin
		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
	}
	
	public void testRoles()
	{
	    final NodeRef rmRootNode = createRMRootNodeRef();
	    
	    setComplete();
        endTransaction();
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {	    
        	    Set<Role> roles = rmSecurityService.getRoles(rmRootNode);
        	    assertNotNull(roles);
        	    assertEquals(5, roles.size());
        	    
        	    rmSecurityService.createRole(rmRootNode, "MyRole", "My Role", getListOfCapabilities(5));
        	    
        	    roles = rmSecurityService.getRoles(rmRootNode);
                assertNotNull(roles);
                assertEquals(6, roles.size());
                
                Role role = findRole(roles, "MyRole");
                assertNotNull(role);
                assertEquals("MyRole", role.getName());
                assertEquals("My Role", role.getDisplayLabel());
                assertNotNull(role.getCapabilities());
                assertEquals(5, role.getCapabilities().size());
                assertNotNull(role.getRoleGroupName());
                
                // Add a user to the role
                String userName = createAndAddUserToRole(role.getRoleGroupName());
                
                // Check that we can retrieve the users roles
                Set<Role> userRoles = rmSecurityService.getRolesByUser(rmRootNode, userName);
                assertNotNull(userRoles);
                assertEquals(1, userRoles.size());
                Role userRole  = userRoles.iterator().next();
                assertEquals("MyRole", userRole.getName());
                
                try
                {
                    rmSecurityService.createRole(rmRootNode, "MyRole", "My Role", getListOfCapabilities(5));
                    fail("Duplicate role id's not allowed for the same rm root node");
                }
                catch (AlfrescoRuntimeException e)
                {
                    // Expected
                }
                
                rmSecurityService.createRole(rmRootNode, "MyRole2", "My Role", getListOfCapabilities(5));
                
                roles = rmSecurityService.getRoles(rmRootNode);
                assertNotNull(roles);
                assertEquals(7, roles.size());    
                
                Set<Capability> list = getListOfCapabilities(3, 4);
                assertEquals(3, list.size());
                
                Role result = rmSecurityService.updateRole(rmRootNode, "MyRole", "SomethingDifferent", list);
                
                assertNotNull(result);
                assertEquals("MyRole", result.getName());
                assertEquals("SomethingDifferent", result.getDisplayLabel());
                assertNotNull(result.getCapabilities());
                assertEquals(3, result.getCapabilities().size());
                assertNotNull(result.getRoleGroupName());
        	 
                roles = rmSecurityService.getRoles(rmRootNode);
                assertNotNull(roles);
                assertEquals(7, roles.size());
                
                Role role2 = findRole(roles, "MyRole");
                assertNotNull(role2);
                assertEquals("MyRole", role2.getName());
                assertEquals("SomethingDifferent", role2.getDisplayLabel());
                assertNotNull(role2.getCapabilities());
                assertEquals(3, role2.getCapabilities().size());
                assertNotNull(role2.getRoleGroupName());
                
                rmSecurityService.deleteRole(rmRootNode, "MyRole2");
                
                roles = rmSecurityService.getRoles(rmRootNode);
                assertNotNull(roles);
                assertEquals(6, roles.size());
                
                return null;       
            }
        });
	}
	
	private Role findRole(Set<Role> roles, String name)
	{
	    Role result = null;
	    for (Role role : roles)
        {
            if (name.equals(role.getName()) == true)
            {
                result = role;
                break;
            }
        }    
	    
	    return result;
	}
	
	private Set<Capability> getListOfCapabilities(int size)
	{
	    return getListOfCapabilities(size, 0);
	}
	
	private Set<Capability> getListOfCapabilities(int size, int offset)
	{
	    Set<Capability> result = new HashSet<Capability>(size);
	    Set<Capability> caps = capabilityService.getCapabilities(false);
	    int count = 0;
	    for (Capability cap : caps)
        {
            if (count < size+offset)
            {
                if (count >= offset)
                {
                    result.add(cap);
                }
            }
            else
            {
                break;
            }
            count ++;
        }
	    return result;
	}
	
	private NodeRef createRMRootNodeRef()
	{
	    NodeRef root = this.nodeService.getRootNode(SPACES_STORE);
	    NodeRef filePlan = this.nodeService.createNode(root, ContentModel.ASSOC_CHILDREN, ContentModel.ASSOC_CHILDREN, TYPE_FILE_PLAN).getChildRef();	    
	    
	    return filePlan;
	}
	
	private NodeRef addFilePlanCompoent(NodeRef parent, QName type)
	{
	    String id = GUID.generate();
        String seriesName = "Series" + id;
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
        props.put(ContentModel.PROP_NAME, seriesName);
        props.put(PROP_IDENTIFIER, id);
        return nodeService.createNode(
                    parent, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, seriesName), 
                    type,
                    props).getChildRef();
	}
	
	private String createAndAddUserToRole(String role)
	{
	    // Create an athentication
	    String userName = GUID.generate();
	    authenticationService.createAuthentication(userName, "PWD".toCharArray());
	            
	    // Create a person
        PropertyMap ppOne = new PropertyMap(4);
        ppOne.put(ContentModel.PROP_USERNAME, userName);
        ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
        ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
        ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
        ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");        
        personService.createPerson(ppOne);
        
        // Assign the new user to the role passed
        authorityService.addAuthority(role, userName);
	    
        return userName;
	}
	
	private String createUser()
    {
        // Create an athentication
        String userName = GUID.generate();
        authenticationService.createAuthentication(userName, "PWD".toCharArray());
                
        // Create a person
        PropertyMap ppOne = new PropertyMap(4);
        ppOne.put(ContentModel.PROP_USERNAME, userName);
        ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
        ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
        ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
        ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");        
        personService.createPerson(ppOne);
           
        return userName;
    }
	
	public void testExecutionAsRMAdmin()
	{
        final NodeRef filePlan = createRMRootNodeRef();
        
        setComplete();
        endTransaction();
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                System.out.println("Groups:");
                Set<String> temp = authorityService.getAllRootAuthorities(AuthorityType.GROUP);
                for (String g : temp)
                {
                    System.out.println("   - " + g);
                }
                System.out.println("");
                
                assertTrue(permissionService.hasPermission(filePlan, RMPermissionModel.READ_RECORDS).equals(AccessStatus.ALLOWED));
                assertTrue(permissionService.hasPermission(filePlan, RMPermissionModel.FILE_RECORDS).equals(AccessStatus.ALLOWED));
                assertTrue(permissionService.hasPermission(filePlan, RMPermissionModel.FILING).equals(AccessStatus.ALLOWED));
                
                Role adminRole = rmSecurityService.getRole(filePlan, "Administrator");
                assertNotNull(adminRole);
                String adminUser = createAndAddUserToRole(adminRole.getRoleGroupName());
                AuthenticationUtil.setFullyAuthenticatedUser(adminUser);
                
                try
                {
                    assertTrue(permissionService.hasPermission(filePlan, RMPermissionModel.READ_RECORDS).equals(AccessStatus.ALLOWED));
                    assertTrue(permissionService.hasPermission(filePlan, RMPermissionModel.FILE_RECORDS).equals(AccessStatus.ALLOWED));
                    assertTrue(permissionService.hasPermission(filePlan, RMPermissionModel.FILING).equals(AccessStatus.ALLOWED));
                    
                    // Read the properties of the filePlan
                    nodeService.getProperties(filePlan);                    
                }
                finally
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                }
                
                return null;
            }
        });
	}
	
	public void testDefaultRolesBootstrap()
	{
	    NodeRef rootNode = nodeService.getRootNode(SPACES_STORE);
        final NodeRef filePlan = nodeService.createNode(rootNode, ContentModel.ASSOC_CHILDREN,
                TYPE_FILE_PLAN,
                TYPE_FILE_PLAN).getChildRef();
        
        setComplete();
        endTransaction();
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {

            public Object execute() throws Throwable
            {
                Set<Role> roles = rmSecurityService.getRoles(filePlan);
                assertNotNull(roles);
                assertEquals(5, roles.size());
                
                Role role = rmSecurityService.getRole(filePlan, "User");
                assertNotNull(role);
                assertEquals("User", role.getName());
                assertNotNull(role.getDisplayLabel());
                Set<String> caps = role.getCapabilities();
                assertNotNull(caps);
                System.out.println("\nUser capabilities: ");
                for (String cap : caps)
                {
                    assertNotNull(capabilityService.getCapability(cap));
                    System.out.println(cap);
                }                
                
                role = rmSecurityService.getRole(filePlan, "PowerUser");
                assertNotNull(role);
                assertEquals("PowerUser", role.getName());
                assertNotNull(role.getDisplayLabel());
                caps = role.getCapabilities();
                assertNotNull(caps);
                System.out.println("\nPowerUser capabilities: ");
                for (String cap : caps)
                {
                    assertNotNull(capabilityService.getCapability(cap));
                    System.out.println(cap);
                }
                
                role = rmSecurityService.getRole(filePlan, "SecurityOfficer");
                assertNotNull(role);
                assertEquals("SecurityOfficer", role.getName());
                assertNotNull(role.getDisplayLabel());
                caps = role.getCapabilities();
                assertNotNull(caps);
                System.out.println("\nSecurityOfficer capabilities: ");
                for (String cap : caps)
                {
                    assertNotNull(capabilityService.getCapability(cap));
                    System.out.println(cap);
                }
                
                role = rmSecurityService.getRole(filePlan, "RecordsManager");
                assertNotNull(role);
                assertEquals("RecordsManager", role.getName());
                assertNotNull(role.getDisplayLabel());
                caps = role.getCapabilities();
                assertNotNull(caps);
                System.out.println("\nRecordsManager capabilities: ");
                for (String cap : caps)
                {
                    assertNotNull(capabilityService.getCapability(cap));
                    System.out.println(cap);
                }
                
                role = rmSecurityService.getRole(filePlan, "Administrator");
                assertNotNull(role);
                assertEquals("Administrator", role.getName());
                assertNotNull(role.getDisplayLabel());
                caps = role.getCapabilities();
                assertNotNull(caps);
                System.out.println("\nAdministrator capabilities: ");
                for (String cap : caps)
                {
                    assertNotNull("No capability called " + cap, capabilityService.getCapability(cap));
                    System.out.println(cap);
                }
                
                return null;
            }
    
        });
	}
	
	public void xtestCreateNewRMUserAccessToFilePlan()
	{
	    final NodeRef rmRootNode = createRMRootNodeRef();        
	    
	    final NodeRef seriesOne = addFilePlanCompoent(rmRootNode, TYPE_RECORD_CATEGORY);
	    final NodeRef seriesTwo = addFilePlanCompoent(rmRootNode, TYPE_RECORD_CATEGORY);        
        final NodeRef seriesThree = addFilePlanCompoent(rmRootNode, TYPE_RECORD_CATEGORY);
        
	    final NodeRef catOne = addFilePlanCompoent(seriesOne, TYPE_RECORD_CATEGORY); 
	    final NodeRef catTwo = addFilePlanCompoent(seriesOne, TYPE_RECORD_CATEGORY);  
        final NodeRef catThree = addFilePlanCompoent(seriesOne, TYPE_RECORD_CATEGORY);         
	    
        final NodeRef folderOne = addFilePlanCompoent(catOne, TYPE_RECORD_FOLDER);
        final NodeRef folderTwo = addFilePlanCompoent(catOne, TYPE_RECORD_FOLDER);
        final NodeRef folderThree = addFilePlanCompoent(catOne, TYPE_RECORD_FOLDER);
        
        setComplete();
        endTransaction();
        
        final String user = transactionHelper.doInTransaction(new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {       
                // Create a new role
                Set<Capability> caps = new HashSet<Capability>(1);
                caps.add(capabilityService.getCapability(RMPermissionModel.VIEW_RECORDS));
                
                Role role = rmSecurityService.createRole(rmRootNode, "TestRole", "My Test Role", caps);
                String user = createUser();

                // Check the role group and allRole group are set up correctly
                Set<String> groups = authorityService.getContainingAuthorities(AuthorityType.GROUP, role.getRoleGroupName(), true);
                assertNotNull(groups);
                // expect allRole group and the capability group
                assertEquals(1, groups.size());
                List<String> tempList = new ArrayList<String>(groups);
                assertTrue(tempList.get(0).startsWith("GROUP_AllRoles"));
                
                // User shouldn't be able to see the file plan node                
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        // Check the permissions of the group on the root node
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(rmRootNode, RMPermissionModel.READ_RECORDS));
                        
                        try
                        {                            
                            nodeService.getChildAssocs(rmRootNode);
                            fail("The user shouldn't be able to read the children");
                        }
                        catch (AlfrescoRuntimeException e)
                        {
                            // expected
                        }
                        
                        return null;
                    }
                }, user);
                
                // Assign the new user to the role
                rmSecurityService.assignRoleToAuthority(rmRootNode, role.getName(), user);
                
                return user;
            }
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Prove that all the series are there
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(rmRootNode);
                assertNotNull(assocs);
                assertEquals(3, assocs.size());
                
                // User should be able to see the file plan node
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        // Check user has read on the root
                       // assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rmRootNode, RMPermissionModel.READ_RECORDS));
                        
                        // Check that the user can not see any of the series
                        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(rmRootNode);
                        assertNotNull(assocs);
                        assertEquals(0, assocs.size());
                        
                        return null;
                    }
                }, user);
                
                // Add read permissions to one of the series
                permissionService.setPermission(seriesOne, user, RMPermissionModel.READ_RECORDS, true);
                
                // Show that user can now see that series
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        // Check that the user can not see any of the series
                        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(rmRootNode);
                        assertNotNull(assocs);
                        assertEquals(1, assocs.size());
                        
                        return null;
                    }
                }, user);
                
                // Add the read permission and file permission to get to the folder
                permissionService.setPermission(catOne, user, RMPermissionModel.READ_RECORDS, true);
                permissionService.setPermission(folderOne, user, RMPermissionModel.FILING, true);
                
                // TODO check visibility of items as we add the permissions
                // TODO check that records inherit the permissions ok
                
                // Try and close the folder as the new user
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        try
                        {
                            rmActionService.executeRecordsManagementAction(folderOne, "closeRecordFolder");
                            fail("User does not have the capability for this");
                        }
                        catch (org.alfresco.repo.security.permissions.AccessDeniedException exception)
                        {
                            // expected
                        }
                        
                        return null;
                    }
                }, user);
                
                // Add the capability to the role
                Set<Capability> caps2 = new HashSet<Capability>(1);
                caps2.add(capabilityService.getCapability(RMPermissionModel.VIEW_RECORDS));
                caps2.add(capabilityService.getCapability(RMPermissionModel.CLOSE_FOLDERS));
                rmSecurityService.updateRole(rmRootNode, "TestRole", "My Test Role", caps2);
                
                Set<AccessPermission> aps = permissionService.getAllSetPermissions(rmRootNode);
                System.out.println("\nPermissions on new series node: ");
                for (AccessPermission ap : aps)
                {
                    System.out.println("   - " + ap.getAuthority() + " has " + ap.getPermission());
                }
                
                // Try and close the folder as the new user
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rmRootNode, RMPermissionModel.CLOSE_FOLDERS));
                        
                        rmActionService.executeRecordsManagementAction(folderOne, "closeRecordFolder");
                        
                        return null;
                    }
                }, user);
                
                return null;
            }
        });
	}
	
	public void testSetPermissions()
    {
        final NodeRef rmRootNode = createRMRootNodeRef();        
        
        final NodeRef seriesOne = addFilePlanCompoent(rmRootNode, TYPE_RECORD_CATEGORY);
        final NodeRef seriesTwo = addFilePlanCompoent(rmRootNode, TYPE_RECORD_CATEGORY);        
        final NodeRef seriesThree = addFilePlanCompoent(rmRootNode, TYPE_RECORD_CATEGORY);
        
        final NodeRef catOne = addFilePlanCompoent(seriesOne, TYPE_RECORD_CATEGORY); 
        final NodeRef catTwo = addFilePlanCompoent(seriesOne, TYPE_RECORD_CATEGORY);  
        final NodeRef catThree = addFilePlanCompoent(seriesOne, TYPE_RECORD_CATEGORY);         
        
        final NodeRef folderOne = addFilePlanCompoent(catOne, TYPE_RECORD_FOLDER);
        final NodeRef folderTwo = addFilePlanCompoent(catOne, TYPE_RECORD_FOLDER);
        final NodeRef folderThree = addFilePlanCompoent(catOne, TYPE_RECORD_FOLDER);
        
        setComplete();
        endTransaction();
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {       
                // Create a new role
                Set<Capability> caps = new HashSet<Capability>(1);
                caps.add(capabilityService.getCapability(RMPermissionModel.VIEW_RECORDS));
                
                Role role = rmSecurityService.createRole(rmRootNode, "TestRole", "My Test Role", caps);
                String user = createUser();
                
                rmSecurityService.assignRoleToAuthority(rmRootNode, role.getName(), user);
                
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rmRootNode, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(seriesOne, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(seriesTwo, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(seriesThree, RMPermissionModel.READ_RECORDS));                
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(catOne, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(catTwo, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(catThree, RMPermissionModel.READ_RECORDS));                
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folderOne, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folderTwo, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folderThree, RMPermissionModel.READ_RECORDS));                     

                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(rmRootNode, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(seriesOne, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(seriesTwo, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(seriesThree, RMPermissionModel.FILING));                
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(catOne, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(catTwo, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(catThree, RMPermissionModel.FILING));                
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folderOne, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folderTwo, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(folderThree, RMPermissionModel.FILING));
                        
                        return null;
                    }
                }, user);
                
                rmSecurityService.setPermission(catOne, user, RMPermissionModel.FILING);
                
                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(rmRootNode, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(seriesOne, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(seriesTwo, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(seriesThree, RMPermissionModel.READ_RECORDS));                
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(catOne, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(catTwo, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(catThree, RMPermissionModel.READ_RECORDS));                
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folderOne, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folderTwo, RMPermissionModel.READ_RECORDS));
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folderThree, RMPermissionModel.READ_RECORDS));                     

                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(rmRootNode, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(seriesOne, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(seriesTwo, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(seriesThree, RMPermissionModel.FILING));                
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(catOne, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(catTwo, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(catThree, RMPermissionModel.FILING));                
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folderOne, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folderTwo, RMPermissionModel.FILING));
                        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(folderThree, RMPermissionModel.FILING));
                        
                        return null;
                    }
                }, user);
                
                return null;
            }
        });
    }
}
