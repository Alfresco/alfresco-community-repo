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
package org.alfresco.module.org_alfresco_module_rm.test.capabilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMEntryVoter;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.model.PermissionModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Roy Wetherall
 */
public abstract class BaseCapabilitiesTest extends TestCase 
                                           implements RMPermissionModel, RecordsManagementModel
{
    /* Application context */
    protected ApplicationContext ctx;
 
    /* Root node reference */
    protected StoreRef storeRef;
    protected NodeRef rootNodeRef;
    
    /* Services */
    protected NodeService nodeService;
    protected NodeService publicNodeService;
    protected TransactionService transactionService;
    protected PermissionService permissionService;
    protected RecordsManagementService recordsManagementService;
    protected RecordsManagementSecurityService recordsManagementSecurityService;
    protected RecordsManagementActionService recordsManagementActionService;
    protected RecordsManagementEventService recordsManagementEventService;
    protected PermissionModel permissionModel;
    protected ContentService contentService;
    protected AuthorityService authorityService;
    protected PersonService personService;
    protected ContentService publicContentService;
    protected RetryingTransactionHelper retryingTransactionHelper;
    protected CapabilityService capabilityService;
    
    protected RMEntryVoter rmEntryVoter;
    
    protected UserTransaction testTX;
    
    protected NodeRef filePlan;
    protected NodeRef recordSeries;
    protected NodeRef recordCategory_1;
    protected NodeRef recordCategory_2;
    protected NodeRef recordFolder_1;
    protected NodeRef recordFolder_2;
    protected NodeRef record_1;
    protected NodeRef record_2;
    protected NodeRef recordCategory_3;
    protected NodeRef recordFolder_3;
    protected NodeRef record_3;    

    protected String rmUsers;
    protected String rmPowerUsers;
    protected String rmSecurityOfficers;
    protected String rmRecordsManagers;
    protected String rmAdministrators;

    protected String rm_user;
    protected String rm_power_user;
    protected String rm_security_officer;
    protected String rm_records_manager;
    protected String rm_administrator;
    protected String test_user;

    protected String testers;    
        
    protected String[] stdUsers;
    protected NodeRef[] stdNodeRefs;;
    
    /**
     * Test setup
     * @throws Exception
     */
    protected void setUp() throws Exception
    {
        // Get the application context
        ctx = ApplicationContextHelper.getApplicationContext();            
        
        // Get beans
        nodeService = (NodeService) ctx.getBean("dbNodeService");
        publicNodeService = (NodeService) ctx.getBean("NodeService");
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        permissionService = (PermissionService) ctx.getBean("permissionService");
        permissionModel = (PermissionModel) ctx.getBean("permissionsModelDAO");
        contentService = (ContentService) ctx.getBean("contentService");
        publicContentService = (ContentService) ctx.getBean("ContentService");
        authorityService = (AuthorityService) ctx.getBean("authorityService");
        personService = (PersonService) ctx.getBean("personService");
        recordsManagementService = (RecordsManagementService) ctx.getBean("RecordsManagementService");
        recordsManagementSecurityService = (RecordsManagementSecurityService) ctx.getBean("RecordsManagementSecurityService");
        recordsManagementActionService = (RecordsManagementActionService) ctx.getBean("RecordsManagementActionService");
        recordsManagementEventService = (RecordsManagementEventService) ctx.getBean("RecordsManagementEventService");
        rmEntryVoter = (RMEntryVoter) ctx.getBean("rmEntryVoter");
        retryingTransactionHelper = (RetryingTransactionHelper)ctx.getBean("retryingTransactionHelper");
        capabilityService = (CapabilityService)ctx.getBean("capabilityService");
        
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                
                // Create store and get the root node reference
                storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
                rootNodeRef = nodeService.getRootNode(storeRef);                
          
                // As admin user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                
                // Create test events
                recordsManagementEventService.getEvents();
                recordsManagementEventService.addEvent("rmEventType.simple", "event", "My Event");
                
                // Create file plan node
                filePlan = nodeService.createNode(
                        rootNodeRef, 
                        ContentModel.ASSOC_CHILDREN, 
                        TYPE_FILE_PLAN, 
                        TYPE_FILE_PLAN).getChildRef();
                
                return null;
            }
        }, false, true);
        
                
        // Load in the plan data required for the test
        loadFilePlanData();
        
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                
               // create people ...                
                rm_user = "rm_user_" + storeRef.getIdentifier();
                rm_power_user = "rm_power_user_" + storeRef.getIdentifier();
                rm_security_officer = "rm_security_officer_" + storeRef.getIdentifier();
                rm_records_manager = "rm_records_manager_" + storeRef.getIdentifier();
                rm_administrator = "rm_administrator_" + storeRef.getIdentifier();                
                test_user = "test_user_" + storeRef.getIdentifier();
                
                personService.createPerson(createDefaultProperties(rm_user));
                personService.createPerson(createDefaultProperties(rm_power_user));
                personService.createPerson(createDefaultProperties(rm_security_officer));
                personService.createPerson(createDefaultProperties(rm_records_manager));
                personService.createPerson(createDefaultProperties(rm_administrator));
                personService.createPerson(createDefaultProperties(test_user));
                
                // create roles as groups
                rmUsers = authorityService.createAuthority(AuthorityType.GROUP, "RM_USER_" + storeRef.getIdentifier());
                rmPowerUsers = authorityService.createAuthority(AuthorityType.GROUP, "RM_POWER_USER_" + storeRef.getIdentifier());
                rmSecurityOfficers = authorityService.createAuthority(AuthorityType.GROUP, "RM_SECURITY_OFFICER_" + storeRef.getIdentifier());
                rmRecordsManagers = authorityService.createAuthority(AuthorityType.GROUP, "RM_RECORDS_MANAGER_" + storeRef.getIdentifier());
                rmAdministrators = authorityService.createAuthority(AuthorityType.GROUP, "RM_ADMINISTRATOR_" + storeRef.getIdentifier());
                testers = authorityService.createAuthority(AuthorityType.GROUP, "RM_TESTOR_" + storeRef.getIdentifier());
                
                authorityService.addAuthority(testers, test_user);
                
                setPermissions(rmUsers, rm_user, ROLE_USER);
                setPermissions(rmPowerUsers, rm_power_user, ROLE_POWER_USER);
                setPermissions(rmSecurityOfficers, rm_security_officer, ROLE_SECURITY_OFFICER);
                setPermissions(rmRecordsManagers, rm_records_manager, ROLE_RECORDS_MANAGER);
                setPermissions(rmAdministrators, rm_administrator, ROLE_ADMINISTRATOR);
                
                stdUsers = new String[]
                {
                   AuthenticationUtil.getSystemUserName(),
                   rm_administrator,
                   rm_records_manager,
                   rm_security_officer,
                   rm_power_user,
                   rm_user
                };
                 
                stdNodeRefs = new NodeRef[]
                {
                   recordFolder_1,
                   record_1,
                   recordFolder_2,
                   record_2
                };
                
                return null;
            }
        }, false, true);        
    }
    
    /**
     * Test tear down
     * @throws Exception
     */
    @Override
    protected void tearDown() throws Exception
    {
        // TODO we should clean up as much as we can ....
    }
    
    /**
     * Set the permissions for a group, user and role
     * @param group
     * @param user
     * @param role
     */
    private void setPermissions(String group, String user, String role)
    {
        for (PermissionReference pr : permissionModel.getImmediateGranteePermissions(permissionModel.getPermissionReference(null, role)))
        {
            setPermission(filePlan, group, pr.getName(), true);
        }
        authorityService.addAuthority(group, user);
        setPermission(filePlan, user, FILING, true);
    }
    
    /**
     * Loads the file plan date required for the tests
     */
    protected void loadFilePlanData()
    {
        recordSeries = createRecordSeries(filePlan, "RS", "RS-1", "Record Series", "My record series");
        
        recordCategory_1 = createRecordCategory(recordSeries, "Docs", "101-1", "Docs", "Docs", "week|1", true, false);
        recordCategory_2 = createRecordCategory(recordSeries, "More Docs", "101-2", "More Docs", "More Docs", "week|1", true, true);
        recordCategory_3 = createRecordCategory(recordSeries, "No disp schedule", "101-3", "No disp schedule", "No disp schedule", "week|1", true, null);
        
        recordFolder_1 = createRecordFolder(recordCategory_1, "F1", "101-3", "title", "description", "week|1", true);
        recordFolder_2 = createRecordFolder(recordCategory_2, "F2", "102-3", "title", "description", "week|1", true);
        recordFolder_3 = createRecordFolder(recordCategory_3, "F3", "103-3", "title", "description", "week|1", true);

        record_1 = createRecord(recordFolder_1);
        record_2 = createRecord(recordFolder_2);
        record_3 = createRecord(recordFolder_3);
    }
        
    /**
     * Set permission for authority on node reference.
     * @param nodeRef
     * @param authority
     * @param permission
     * @param allow
     */
    private void setPermission(NodeRef nodeRef, String authority, String permission, boolean allow)
    {
        permissionService.setPermission(nodeRef, authority, permission, allow);
        if (permission.equals(FILING))
        {
            if (recordsManagementService.isRecordCategory(nodeRef) == true)
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef child = assoc.getChildRef();
                    if (recordsManagementService.isRecordFolder(child) == true || 
                        recordsManagementService.isRecordCategory(child) == true)
                    {
                        setPermission(child, authority, permission, allow);
                    }
                }
            }
        }
    }

    /**
     * Create the default person properties
     * @param userName
     * @return
     */
    private Map<QName, Serializable> createDefaultProperties(String userName)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_HOMEFOLDER, null);
        properties.put(ContentModel.PROP_FIRSTNAME, userName);
        properties.put(ContentModel.PROP_LASTNAME, userName);
        properties.put(ContentModel.PROP_EMAIL, userName);
        properties.put(ContentModel.PROP_ORGID, "");
        return properties;
    }

    /**
     * Create a new record.  Executed in a new transaction.
     */
    private NodeRef createRecord(final NodeRef recordFolder)
    {
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                // As admin
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                                       
                // Create the record
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, "MyRecord.txt");
                NodeRef recordOne = nodeService.createNode(recordFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "MyRecord.txt"),
                        ContentModel.TYPE_CONTENT, props).getChildRef();
        
                // Set the content
                ContentWriter writer = contentService.getWriter(recordOne, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.setEncoding("UTF-8");
                writer.putContent("There is some content in this record");
                return recordOne;
            }
        }, false, true);
    }

    /**
     * Create a test record series.  Executed in a new transaction.
     */
    private NodeRef createRecordSeries(final NodeRef filePlan, final String name, final String identifier, final String title, final String description)
    {
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                // As admin
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                
                HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(ContentModel.PROP_NAME, name);
                properties.put(PROP_IDENTIFIER, identifier);
                properties.put(ContentModel.PROP_TITLE, title);
                properties.put(ContentModel.PROP_DESCRIPTION, description);
                
                NodeRef recordSeried = nodeService.createNode(filePlan, ContentModel.ASSOC_CONTAINS, TYPE_RECORD_CATEGORY, TYPE_RECORD_CATEGORY, properties).getChildRef();
                permissionService.setInheritParentPermissions(recordSeried, false);
                
                return recordSeried;
            }
        }, false, true);
    }

    /**
     * Create a test record category in a new transaction.
     */
    private NodeRef createRecordCategory(
                        final NodeRef recordSeries, 
                        final String name, 
                        final String identifier, 
                        final String title, 
                        final String description, 
                        final String review, 
                        final boolean vital,
                        final Boolean recordLevelDisposition)
    {
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                // As admin
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                
                HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(ContentModel.PROP_NAME, name);
                properties.put(PROP_IDENTIFIER, identifier);
                properties.put(ContentModel.PROP_TITLE, title);
                properties.put(ContentModel.PROP_DESCRIPTION, description);
                properties.put(PROP_REVIEW_PERIOD, review);
                properties.put(PROP_VITAL_RECORD_INDICATOR, vital);
                
                NodeRef answer = nodeService.createNode(recordSeries, ContentModel.ASSOC_CONTAINS, TYPE_RECORD_CATEGORY, TYPE_RECORD_CATEGORY, properties)
                        .getChildRef();
        
                if (recordLevelDisposition != null)
                {
                    properties = new HashMap<QName, Serializable>();
                    properties.put(PROP_DISPOSITION_AUTHORITY, "N1-218-00-4 item 023");
                    properties.put(PROP_DISPOSITION_INSTRUCTIONS, "Cut off monthly, hold 1 month, then destroy.");
                    properties.put(PROP_RECORD_LEVEL_DISPOSITION, recordLevelDisposition);
                    NodeRef ds = nodeService.createNode(answer, ASSOC_DISPOSITION_SCHEDULE, TYPE_DISPOSITION_SCHEDULE, TYPE_DISPOSITION_SCHEDULE,
                            properties).getChildRef();
        
                    createDispoistionAction(ds, "cutoff", "monthend|1", null, "event");
                    createDispoistionAction(ds, "transfer", "month|1", null, null);
                    createDispoistionAction(ds, "accession", "month|1", null, null);
                    createDispoistionAction(ds, "destroy", "month|1", "{http://www.alfresco.org/model/recordsmanagement/1.0}cutOffDate", null);
                }
                
                permissionService.setInheritParentPermissions(answer, false);
                
                return answer;
            }
        }, false, true);
    }

    /**
     * Create disposition action.
     * @param disposition
     * @param actionName
     * @param period
     * @param periodProperty
     * @param event
     * @return
     */
    private NodeRef createDispoistionAction(NodeRef disposition, String actionName, String period, String periodProperty, String event)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(PROP_DISPOSITION_ACTION_NAME, actionName);
        properties.put(PROP_DISPOSITION_PERIOD, period);
        if (periodProperty != null)
        {
            properties.put(PROP_DISPOSITION_PERIOD_PROPERTY, periodProperty);
        }
        if (event != null)
        {
            properties.put(PROP_DISPOSITION_EVENT, event);
        }
        NodeRef answer = nodeService.createNode(disposition, ASSOC_DISPOSITION_ACTION_DEFINITIONS, TYPE_DISPOSITION_ACTION_DEFINITION,
                TYPE_DISPOSITION_ACTION_DEFINITION, properties).getChildRef();
        return answer;
    }

    /**
     * Create record folder.  Executed in a new transaction.
     * @param recordCategory
     * @param name
     * @param identifier
     * @param title
     * @param description
     * @param review
     * @param vital
     * @return
     */
    private NodeRef createRecordFolder(
                        final NodeRef recordCategory, 
                        final String name, 
                        final String identifier, 
                        final String title, 
                        final String description, 
                        final String review, 
                        final boolean vital)
    {
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                // As admin
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                     
                HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
                properties.put(ContentModel.PROP_NAME, name);
                properties.put(PROP_IDENTIFIER, identifier);
                properties.put(ContentModel.PROP_TITLE, title);
                properties.put(ContentModel.PROP_DESCRIPTION, description);
                properties.put(PROP_REVIEW_PERIOD, review);
                properties.put(PROP_VITAL_RECORD_INDICATOR, vital);
                NodeRef answer = nodeService.createNode(recordCategory, ContentModel.ASSOC_CONTAINS, TYPE_RECORD_FOLDER, TYPE_RECORD_FOLDER, properties)
                        .getChildRef();
                permissionService.setInheritParentPermissions(answer, false);
                return answer;
            }
        }, false, true);
    }
    
    /**
     * 
     * @param user
     * @param nodeRef
     * @param capabilityName
     * @param accessStstus
     */
    protected void checkCapability(final String user, final NodeRef nodeRef, final String capabilityName, final AccessStatus expected)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                Capability capability = recordsManagementSecurityService.getCapability(capabilityName);
                assertNotNull(capability);
                
                List<String> capabilities = new ArrayList<String>(1);
                capabilities.add(capabilityName);                
                Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(nodeRef, capabilities);
                
                AccessStatus actual = access.get(capability);
                
                assertEquals(
                        "for user: " + user,
                        expected, 
                        actual);
                  
                return null;
            }
        }, user);        
    }
    
    /**
     * 
     * @param access
     * @param name
     * @param accessStatus
     */
    protected void check(Map<Capability, AccessStatus> access, String name, AccessStatus accessStatus)
    {
        Capability capability = recordsManagementSecurityService.getCapability(name);
        assertNotNull(capability);
        assertEquals(accessStatus, access.get(capability));
    }

    /**
     * 
     * @param user
     * @param nodeRef
     * @param permission
     * @param accessStstus
     */
    protected void checkPermission(final String user, final NodeRef nodeRef, final String permission, final AccessStatus accessStstus)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                AccessStatus actualAccessStatus = permissionService.hasPermission(nodeRef, permission);         
                assertTrue(actualAccessStatus == accessStstus);
                return null;
            }
        }, user);               
    }
    
    /**
     * 
     * @param nodeRef
     * @param permission
     * @param users
     * @param expectedAccessStatus
     */
    protected void checkPermissions(            
            final NodeRef nodeRef, 
            final String permission, 
            final String[] users,
            final AccessStatus ... expectedAccessStatus)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                assertEquals(
                        "The number of users should match the number of expected access status", 
                        users.length, 
                        expectedAccessStatus.length);
                
                for (int i = 0; i < users.length; i++)
                {
                    checkPermission(users[i], nodeRef, permission, expectedAccessStatus[i]);
                }
                
                return null;
            }
        }, true, true);
    }
    
    /**
     * 
     * @param nodeRef
     * @param capability
     * @param users
     * @param expectedAccessStatus
     */
    protected void checkCapabilities(
            final NodeRef nodeRef, 
            final String capability, 
            final String[] users, 
            final AccessStatus ... expectedAccessStatus)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                assertEquals(
                        "The number of users should match the number of expected access status", 
                        users.length, 
                        expectedAccessStatus.length);
                
                for (int i = 0; i < users.length; i++)
                {
                    checkCapability(users[i], nodeRef, capability, expectedAccessStatus[i]);
                }
                
                return null;
            }
        }, true, true);
    }
    
    /**
     * 
     * @param user
     * @param capability
     * @param nodeRefs
     * @param expectedAccessStatus
     */
    protected void checkCapabilities(
            final String user, 
            final String capability,
            final NodeRef[] nodeRefs,
            final AccessStatus ... expectedAccessStatus)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                assertEquals(
                        "The number of node references should match the number of expected access status", 
                        nodeRefs.length, 
                        expectedAccessStatus.length);
                
                for (int i = 0; i < nodeRefs.length; i++)
                {
                    checkCapability(user, nodeRefs[i], capability, expectedAccessStatus[i]);
                }
                
                return null;
            }
        }, true, true);
    }
    
    /**
     * 
     * @param capability
     * @param accessStatus
     */
    protected void checkTestUserCapabilities(String capability, AccessStatus ... accessStatus)
    {
        checkCapabilities(
                test_user, 
                capability, 
                stdNodeRefs, 
                accessStatus);
    }
    
    /**
     * Execute RM action
     * @param action
     * @param params
     * @param nodeRefs
     */
    protected void executeAction(final String action, final Map<String, Serializable> params, final String user, final NodeRef ... nodeRefs)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(user);

                for (NodeRef nodeRef : nodeRefs)
                {                         
                    recordsManagementActionService.executeRecordsManagementAction(nodeRef, action, params);
                }

                return null;
            }
        }, false, true);
    }
    
    /**
     * 
     * @param action
     * @param nodeRefs
     */
    protected void executeAction(final String action, final NodeRef ... nodeRefs)
    {
        executeAction(action, null, AuthenticationUtil.SYSTEM_USER_NAME, nodeRefs);   
    }
    
    /**
     * 
     * @param action
     * @param params
     * @param nodeRefs
     */
    protected void executeAction(final String action, final Map<String, Serializable> params,  final NodeRef ... nodeRefs)
    {
        executeAction(action, params, AuthenticationUtil.SYSTEM_USER_NAME, nodeRefs);   
    }
    
    /**
     * 
     * @param action
     * @param params
     * @param user
     * @param nodeRefs
     */
    protected void checkExecuteActionFail(final String action, final Map<String, Serializable> params, final String user, final NodeRef ... nodeRefs)
    {
        try 
        { 
            executeAction(action, params, user, nodeRefs); 
            fail("Action " + action + " has succeded and was expected to fail"); 
        } 
        catch (AccessDeniedException ade)
        {}
    }
    
    /**
     * 
     * @param nodeRef
     * @param property
     * @param user
     */
    protected void checkSetPropertyFail(final NodeRef nodeRef, final QName property, final String user, final Serializable value)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(user);

                try
                {
                    publicNodeService.setProperty(nodeRef, property, value);
                    fail("Expected failure when setting property");
                } 
                catch (AccessDeniedException ade)
                {}

                return null;
            }
        }, false, true);
    }
    
    /**
     * Add a capability
     * @param capability
     * @param authority
     * @param nodeRefs
     */
    protected void addCapability(final String capability, final String authority, final NodeRef ... nodeRefs)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);    
                for (NodeRef nodeRef : nodeRefs)
                {                    
                    permissionService.setPermission(nodeRef, authority, capability, true);
                }
                return null;
            }
        }, false, true); 
    }
    
    /**
     * Remove capability
     * @param capability
     * @param authority
     * @param nodeRef
     */
    protected void removeCapability(final String capability, final String authority, final NodeRef ... nodeRefs)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);
                for (NodeRef nodeRef : nodeRefs)
                {
                    permissionService.deletePermission(nodeRef, authority, capability);
                }                               
                return null;
            }
        }, false, true); 
    }
    
    /**
     * 
     * @param nodeRefs
     */
    protected void declare(final NodeRef ... nodeRefs)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);

                for (NodeRef nodeRef : nodeRefs)
                {                 
                    nodeService.setProperty(nodeRef, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
                    nodeService.setProperty(nodeRef, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
                    nodeService.setProperty(nodeRef, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
                    nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, "titleValue");
                    recordsManagementActionService.executeRecordsManagementAction(nodeRef, "declareRecord");
                }

                return null;
            }
        }, false, true);
    }
    
    protected void cutoff(final NodeRef ... nodeRefs)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                
                for (NodeRef nodeRef : nodeRefs)
                {                    
                    NodeRef ndNodeRef = nodeService.getChildAssocs(nodeRef, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                    nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
                    recordsManagementActionService.executeRecordsManagementAction(nodeRef, "cutoff", null);
                }

                return null;
            }
        }, false, true);
    }
    
    protected void makeEligible(final NodeRef ... nodeRefs)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                
                for (NodeRef nodeRef : nodeRefs)
                {                    
                    NodeRef ndNodeRef = nodeService.getChildAssocs(nodeRef, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                    nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
                }

                return null;
            }
        }, false, true);
    }
}
