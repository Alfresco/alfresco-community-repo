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
package org.alfresco.module.org_alfresco_module_rm.test.system;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.FreezeAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.TransferAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.TransferCompleteAction;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMEntryVoter;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.model.PermissionModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author andyh
 */
public class CapabilitiesSystemTest extends TestCase implements RecordsManagementModel
{

    private ApplicationContext ctx;

    private NodeRef rootNodeRef;

    private NodeService nodeService;

    private NodeService publicNodeService;

    private TransactionService transactionService;

    private UserTransaction testTX;

    private NodeRef filePlan;

    private PermissionService permissionService;

    private RecordsManagementService recordsManagementService;

    private RecordsManagementSecurityService recordsManagementSecurityService;

    private RecordsManagementActionService recordsManagementActionService;

    private RecordsManagementEventService recordsManagementEventService;
    
    private CapabilityService capabilityService;

    private PermissionModel permissionModel;

    private ContentService contentService;

    private NodeRef recordSeries;

    private NodeRef recordCategory_1;

    private NodeRef recordCategory_2;

    private NodeRef recordFolder_1;

    private NodeRef recordFolder_2;

    private NodeRef record_1;

    private NodeRef record_2;

    private RMEntryVoter rmEntryVoter;

    private AuthorityService authorityService;

    private String rmUsers;

    private String rmPowerUsers;

    private String rmSecurityOfficers;

    private String rmRecordsManagers;

    private String rmAdministrators;

    private PersonService personService;

    private String rm_user;

    private String rm_power_user;

    private String rm_security_officer;

    private String rm_records_manager;

    private String rm_administrator;

    private String test_user;

    private String testers;

    private NodeRef recordCategory_3;

    private NodeRef recordFolder_3;

    private NodeRef record_3;

    private ContentService publicContentService;

    /**
     * @param name
     */
    public CapabilitiesSystemTest(String name)
    {
        super(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext(); 
           
        super.setUp();
        
        nodeService = (NodeService) ctx.getBean("dbNodeService");
        publicNodeService = (NodeService) ctx.getBean("NodeService");
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        permissionService = (PermissionService) ctx.getBean("permissionService");
        permissionModel = (PermissionModel) ctx.getBean("permissionsModelDAO");
        contentService = (ContentService) ctx.getBean("contentService");
        publicContentService = (ContentService) ctx.getBean("ContentService");
        authorityService = (AuthorityService) ctx.getBean("authorityService");
        personService = (PersonService) ctx.getBean("personService");
        capabilityService = (CapabilityService) ctx.getBean("CapabilityService");

        recordsManagementService = (RecordsManagementService) ctx.getBean("RecordsManagementService");
        recordsManagementSecurityService = (RecordsManagementSecurityService) ctx.getBean("RecordsManagementSecurityService");
        recordsManagementActionService = (RecordsManagementActionService) ctx.getBean("RecordsManagementActionService");
        recordsManagementEventService = (RecordsManagementEventService) ctx.getBean("RecordsManagementEventService");
        rmEntryVoter = (RMEntryVoter) ctx.getBean("rmEntryVoter");

        testTX = transactionService.getUserTransaction();
        testTX.begin();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        recordsManagementEventService.getEvents();
        recordsManagementEventService.addEvent("rmEventType.simple", "event", "My Event");

        filePlan = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, TYPE_FILE_PLAN, TYPE_FILE_PLAN).getChildRef();
        recordSeries = createRecordSeries(filePlan, "RS", "RS-1", "Record Series", "My record series");
        recordCategory_1 = createRecordCategory(recordSeries, "Docs", "101-1", "Docs", "Docs", "week|1", true, false);
        recordCategory_2 = createRecordCategory(recordSeries, "More Docs", "101-2", "More Docs", "More Docs", "week|1", true, true);
        recordCategory_3 = createRecordCategory(recordSeries, "No disp schedule", "101-3", "No disp schedule", "No disp schedule", "week|1", true, null);
        
        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        recordFolder_1 = createRecordFolder(recordCategory_1, "F1", "101-3", "title", "description", "week|1", true);
        recordFolder_2 = createRecordFolder(recordCategory_2, "F2", "102-3", "title", "description", "week|1", true);
        recordFolder_3 = createRecordFolder(recordCategory_3, "F3", "103-3", "title", "description", "week|1", true);
        record_1 = createRecord(recordFolder_1);
        record_2 = createRecord(recordFolder_2);
        record_3 = createRecord(recordFolder_3);

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

        for (PermissionReference pr : permissionModel.getImmediateGranteePermissions(permissionModel.getPermissionReference(null, RMPermissionModel.ROLE_USER)))
        {
            setPermission(filePlan, rmUsers, pr.getName(), true);
        }
        authorityService.addAuthority(rmUsers, rm_user);
        setPermission(filePlan, rm_user, RMPermissionModel.FILING, true);

        for (PermissionReference pr : permissionModel.getImmediateGranteePermissions(permissionModel.getPermissionReference(null, RMPermissionModel.ROLE_POWER_USER)))
        {
            setPermission(filePlan, rmPowerUsers, pr.getName(), true);
        }
        authorityService.addAuthority(rmPowerUsers, rm_power_user);
        setPermission(filePlan, rm_power_user, RMPermissionModel.FILING, true);

        for (PermissionReference pr : permissionModel.getImmediateGranteePermissions(permissionModel.getPermissionReference(null, RMPermissionModel.ROLE_SECURITY_OFFICER)))
        {
            setPermission(filePlan, rmSecurityOfficers, pr.getName(), true);
        }
        authorityService.addAuthority(rmSecurityOfficers, rm_security_officer);
        setPermission(filePlan, rm_security_officer, RMPermissionModel.FILING, true);

        for (PermissionReference pr : permissionModel.getImmediateGranteePermissions(permissionModel.getPermissionReference(null, RMPermissionModel.ROLE_RECORDS_MANAGER)))
        {
            setPermission(filePlan, rmRecordsManagers, pr.getName(), true);
        }
        authorityService.addAuthority(rmRecordsManagers, rm_records_manager);
        setPermission(filePlan, rm_records_manager, RMPermissionModel.FILING, true);

        for (PermissionReference pr : permissionModel.getImmediateGranteePermissions(permissionModel.getPermissionReference(null, RMPermissionModel.ROLE_ADMINISTRATOR)))
        {
            setPermission(filePlan, rmAdministrators, pr.getName(), true);
        }
        authorityService.addAuthority(rmAdministrators, rm_administrator);
        setPermission(filePlan, rm_administrator, RMPermissionModel.FILING, true);

        testTX.commit();
        testTX = transactionService.getUserTransaction();
        testTX.begin();
    }

    private void setPermission(NodeRef nodeRef, String authority, String permission, boolean allow)
    {
        permissionService.setPermission(nodeRef, authority, permission, allow);
        if (permission.equals(RMPermissionModel.FILING))
        {
            if (recordsManagementService.isRecordCategory(nodeRef) == true)
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef child = assoc.getChildRef();
                    if (recordsManagementService.isRecordFolder(child) == true || recordsManagementService.isRecordCategory(child) == true)
                    {
                        setPermission(child, authority, permission, allow);
                    }
                }
            }
        }
    }

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

    private NodeRef createRecord(NodeRef recordFolder)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "MyRecord.txt");
        NodeRef recordOne = this.nodeService.createNode(recordFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "MyRecord.txt"),
                ContentModel.TYPE_CONTENT, props).getChildRef();

        // Set the content
        ContentWriter writer = this.contentService.getWriter(recordOne, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("There is some content in this record");
        return recordOne;
    }

    private NodeRef createRecordSeries(NodeRef filePlan, String name, String identifier, String title, String description)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, name);
        properties.put(PROP_IDENTIFIER, identifier);
        properties.put(ContentModel.PROP_TITLE, title);
        properties.put(ContentModel.PROP_DESCRIPTION, description);
        NodeRef answer = nodeService.createNode(filePlan, ContentModel.ASSOC_CONTAINS, TYPE_RECORD_CATEGORY, TYPE_RECORD_CATEGORY, properties).getChildRef();
        permissionService.setInheritParentPermissions(answer, false);
        return answer;
    }

    private NodeRef createRecordCategory(NodeRef recordSeries, String name, String identifier, String title, String description, String review, boolean vital,
            Boolean recordLevelDisposition)
    {
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

    private NodeRef createRecordFolder(NodeRef recordCategory, String name, String identifier, String title, String description, String review, boolean vital)
    {
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

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        if (testTX.getStatus() == Status.STATUS_ACTIVE)
        {
            testTX.rollback();
        }
        else if (testTX.getStatus() == Status.STATUS_MARKED_ROLLBACK)
        {
            testTX.rollback();
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }

    public void testPermissionsModel()
    {
        Set<PermissionReference> exposed = permissionModel.getExposedPermissions(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT);
        assertEquals(6, exposed.size());
        assertTrue(exposed.contains(permissionModel.getPermissionReference(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT, RMPermissionModel.ROLE_ADMINISTRATOR)));

        Set<PermissionReference> all = permissionModel.getAllPermissions(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT);
        assertEquals(58 /* capbilities */* 2 + 5 /* roles */+ (2 /* Read+File */* 2) + 1 /* Filing */, all.size());

        checkGranting(RMPermissionModel.ACCESS_AUDIT, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.ADD_MODIFY_EVENT_DATES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER,
                RMPermissionModel.ROLE_SECURITY_OFFICER, RMPermissionModel.ROLE_POWER_USER);
        checkGranting(RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CLOSE_FOLDERS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER, RMPermissionModel.ROLE_SECURITY_OFFICER,
                RMPermissionModel.ROLE_POWER_USER);
        checkGranting(RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER,
                RMPermissionModel.ROLE_SECURITY_OFFICER);
        checkGranting(RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER,
                RMPermissionModel.ROLE_SECURITY_OFFICER, RMPermissionModel.ROLE_POWER_USER);
        checkGranting(RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.CYCLE_VITAL_RECORDS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER, RMPermissionModel.ROLE_SECURITY_OFFICER,
                RMPermissionModel.ROLE_POWER_USER);
        checkGranting(RMPermissionModel.DECLARE_AUDIT_AS_RECORD, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.DECLARE_RECORDS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER, RMPermissionModel.ROLE_SECURITY_OFFICER,
                RMPermissionModel.ROLE_POWER_USER, RMPermissionModel.ROLE_USER);
        checkGranting(RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER,
                RMPermissionModel.ROLE_SECURITY_OFFICER, RMPermissionModel.ROLE_POWER_USER);
        checkGranting(RMPermissionModel.DELETE_AUDIT, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.DELETE_LINKS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.DELETE_RECORDS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.DESTROY_RECORDS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.DISPLAY_RIGHTS_REPORT, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.EDIT_NON_RECORD_METADATA, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER,
                RMPermissionModel.ROLE_SECURITY_OFFICER, RMPermissionModel.ROLE_POWER_USER);
        checkGranting(RMPermissionModel.EDIT_RECORD_METADATA, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER,
                RMPermissionModel.ROLE_SECURITY_OFFICER, RMPermissionModel.ROLE_POWER_USER);
        checkGranting(RMPermissionModel.EDIT_SELECTION_LISTS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.EXPORT_AUDIT, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        // File does not exists
        // checkGranting(RMPermissionModel.FILE_RECORDS, RMPermissionModel.ROLE_ADMINISTRATOR,
        // RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.MANAGE_ACCESS_CONTROLS, RMPermissionModel.ROLE_ADMINISTRATOR);
        checkGranting(RMPermissionModel.MANAGE_ACCESS_RIGHTS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.MAP_EMAIL_METADATA, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.MOVE_RECORDS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.PASSWORD_CONTROL, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.PLANNING_REVIEW_CYCLES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER,
                RMPermissionModel.ROLE_SECURITY_OFFICER, RMPermissionModel.ROLE_POWER_USER);
        checkGranting(RMPermissionModel.RE_OPEN_FOLDERS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER, RMPermissionModel.ROLE_SECURITY_OFFICER,
                RMPermissionModel.ROLE_POWER_USER);
        checkGranting(RMPermissionModel.SELECT_AUDIT_METADATA, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.TRIGGER_AN_EVENT, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.UNDECLARE_RECORDS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.UNFREEZE, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.UPDATE_CLASSIFICATION_DATES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER,
                RMPermissionModel.ROLE_SECURITY_OFFICER);
        checkGranting(RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER,
                RMPermissionModel.ROLE_SECURITY_OFFICER);
        checkGranting(RMPermissionModel.UPDATE_TRIGGER_DATES, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);
        checkGranting(RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER,
                RMPermissionModel.ROLE_SECURITY_OFFICER);
        checkGranting(RMPermissionModel.VIEW_RECORDS, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER, RMPermissionModel.ROLE_SECURITY_OFFICER,
                RMPermissionModel.ROLE_POWER_USER, RMPermissionModel.ROLE_USER);
        checkGranting(RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, RMPermissionModel.ROLE_ADMINISTRATOR, RMPermissionModel.ROLE_RECORDS_MANAGER);

    }

    private void checkGranting(String permission, String... roles)
    {
        Set<PermissionReference> granting = permissionModel.getGrantingPermissions(permissionModel.getPermissionReference(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT,
                permission));
        Set<PermissionReference> test = new HashSet<PermissionReference>();
        test.addAll(granting);
        Set<PermissionReference> nonRM = new HashSet<PermissionReference>();
        for (PermissionReference pr : granting)
        {
            if (!pr.getQName().equals(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
            {
                nonRM.add(pr);
            }
        }
        test.removeAll(nonRM);
        assertEquals(roles.length + 1, test.size());
        for (String role : roles)
        {
            assertTrue(test.contains(permissionModel.getPermissionReference(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT, role)));
        }

    }

    public void testConfig()
    {
        assertEquals(6, recordsManagementSecurityService.getProtectedAspects().size());
        assertEquals(13, recordsManagementSecurityService.getProtectedProperties().size());

        // Test action wire up
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.ACCESS_AUDIT).getActionNames().size());
        assertEquals(2, recordsManagementSecurityService.getCapability(RMPermissionModel.ADD_MODIFY_EVENT_DATES).getActionNames().size());
        assertEquals(2, recordsManagementSecurityService.getCapability(RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES).getActionNames().size());
        assertEquals(2, recordsManagementSecurityService.getCapability(RMPermissionModel.AUTHORIZE_ALL_TRANSFERS).getActionNames().size());
        assertEquals(2, recordsManagementSecurityService.getCapability(RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CHANGE_OR_DELETE_REFERENCES).getActionNames().size());
        assertEquals(1, recordsManagementSecurityService.getCapability(RMPermissionModel.CLOSE_FOLDERS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS).getActionNames().size());
        assertEquals(1, recordsManagementSecurityService.getCapability(RMPermissionModel.CYCLE_VITAL_RECORDS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.DECLARE_AUDIT_AS_RECORD).getActionNames().size());
        assertEquals(2, recordsManagementSecurityService.getCapability(RMPermissionModel.DECLARE_RECORDS).getActionNames().size());
        assertEquals(1, recordsManagementSecurityService.getCapability(RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.DELETE_AUDIT).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.DELETE_LINKS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.DELETE_RECORDS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.DESTROY_RECORDS).getActionNames().size());
        assertEquals(1, recordsManagementSecurityService.getCapability(RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.DISPLAY_RIGHTS_REPORT).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.EDIT_DECLARED_RECORD_METADATA).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.EDIT_NON_RECORD_METADATA).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.EDIT_RECORD_METADATA).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.EDIT_SELECTION_LISTS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.EXPORT_AUDIT).getActionNames().size());
        assertEquals(1, recordsManagementSecurityService.getCapability(RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE).getActionNames().size());
        assertEquals(1, recordsManagementSecurityService.getCapability(RMPermissionModel.FILE_RECORDS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.MANAGE_ACCESS_CONTROLS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.MANAGE_ACCESS_RIGHTS).getActionNames().size());
        assertEquals(1, recordsManagementSecurityService.getCapability(RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.MAP_EMAIL_METADATA).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.MOVE_RECORDS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.PASSWORD_CONTROL).getActionNames().size());
        assertEquals(1, recordsManagementSecurityService.getCapability(RMPermissionModel.PLANNING_REVIEW_CYCLES).getActionNames().size());
        assertEquals(1, recordsManagementSecurityService.getCapability(RMPermissionModel.RE_OPEN_FOLDERS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.SELECT_AUDIT_METADATA).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.TRIGGER_AN_EVENT).getActionNames().size());
        assertEquals(1, recordsManagementSecurityService.getCapability(RMPermissionModel.UNDECLARE_RECORDS).getActionNames().size());
        assertEquals(2, recordsManagementSecurityService.getCapability(RMPermissionModel.UNFREEZE).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.UPDATE_CLASSIFICATION_DATES).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.UPDATE_TRIGGER_DATES).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS).getActionNames().size());
        assertEquals(0, recordsManagementSecurityService.getCapability(RMPermissionModel.VIEW_RECORDS).getActionNames().size());
        assertEquals(1, recordsManagementSecurityService.getCapability(RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE).getActionNames().size());

    }

    public void testFilePlanAsSystem()
    {
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(filePlan);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testFilePlanAsAdmin()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(filePlan);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testFilePlanAsAdministrator()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_administrator);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(filePlan);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testFilePlanAsRecordsManager()
    {
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(filePlan);
        for (AccessPermission ap : permissions)
        {
            System.out.println(ap.getAuthority() + " -> " + ap.getPermission() + " (" + ap.getPosition() + ")");
        }

        AuthenticationUtil.setFullyAuthenticatedUser(rm_records_manager);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(filePlan);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testFilePlanAsSecurityOfficer()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_security_officer);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(filePlan);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testFilePlanAsPowerUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_power_user);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(filePlan);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testFilePlanAsUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_user);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(filePlan);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordSeriesAsSystem()
    {
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordSeries);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordSeriesAsAdmin()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordSeries);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordSeriesAsAdministrator()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_administrator);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordSeries);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordSeriesAsRecordsManager()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_records_manager);
        permissionService.setPermission(recordSeries, rm_records_manager, RMPermissionModel.FILING, true);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordSeries);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordSeriesAsSecurityOfficer()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_security_officer);
        permissionService.setPermission(recordSeries, rm_security_officer, RMPermissionModel.FILING, true);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordSeries);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordSeriesAsPowerUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_power_user);
        permissionService.setPermission(recordSeries, rm_power_user, RMPermissionModel.FILING, true);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordSeries);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordSeriesAsUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_user);
        permissionService.setPermission(recordSeries, rm_user, RMPermissionModel.FILING, true);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordSeries);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordCategoryAsSystem()
    {
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordCategory_1);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordCategoryAsAdmin()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordCategory_1);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordCategoryAsAdministrator()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_administrator);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordCategory_1);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordCategoryAsRecordsManager()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_records_manager);
        permissionService.setPermission(recordCategory_1, rm_records_manager, RMPermissionModel.FILING, true);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordCategory_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordCategoryAsSecurityOfficer()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_security_officer);
        permissionService.setPermission(recordCategory_1, rm_security_officer, RMPermissionModel.FILING, true);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordCategory_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordCategoryAsPowerUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_power_user);
        permissionService.setPermission(recordCategory_1, rm_power_user, RMPermissionModel.FILING, true);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordCategory_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordCategoryAsUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_user);
        permissionService.setPermission(recordCategory_1, rm_user, RMPermissionModel.FILING, true);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordCategory_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordFolderAsSystem()
    {
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordFolder_1);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordFolderAsAdmin()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordFolder_1);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordFolderAsAdministrator()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_administrator);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordFolder_1);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    private void setFilingOnRecordFolder(NodeRef recordFolder, String authority)
    {
        permissionService.setPermission(recordFolder, authority, RMPermissionModel.FILING, true);
        permissionService.setPermission(nodeService.getPrimaryParent(recordFolder).getParentRef(), authority, RMPermissionModel.READ_RECORDS, true);
    }

    public void testRecordFolderAsRecordsManager()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_records_manager);
        setFilingOnRecordFolder(recordFolder_1, rm_records_manager);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordFolder_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordFolderAsSecurityOfficer()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_security_officer);
        permissionService.setPermission(recordFolder_1, rm_security_officer, RMPermissionModel.FILING, true);
        permissionService.setPermission(nodeService.getPrimaryParent(recordFolder_1).getParentRef(), rm_security_officer, RMPermissionModel.READ_RECORDS, true);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordFolder_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordFolderAsPowerUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_power_user);
        permissionService.setPermission(recordFolder_1, rm_power_user, RMPermissionModel.FILING, true);
        permissionService.setPermission(nodeService.getPrimaryParent(recordFolder_1).getParentRef(), rm_power_user, RMPermissionModel.READ_RECORDS, true);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordFolder_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordFolderAsUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_user);
        setFilingOnRecordFolder(recordFolder_1, rm_user);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(recordFolder_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordAsSystem()
    {
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(record_1);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordAsAdmin()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(record_1);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordAsAdministrator()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_administrator);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(record_1);
        assertEquals(65, access.size());
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordAsRecordsManager()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_records_manager);
        setFilingOnRecord(record_1, rm_records_manager);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(record_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

    }

    public void testRecordAsSecurityOfficer()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_security_officer);
        setFilingOnRecord(record_1, rm_security_officer);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(record_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    private void setFilingOnRecord(NodeRef record, String authority)
    {
        NodeRef recordFolder = nodeService.getPrimaryParent(record).getParentRef();
        permissionService.setPermission(recordFolder, authority, RMPermissionModel.FILING, true);
        permissionService.setPermission(nodeService.getPrimaryParent(recordFolder).getParentRef(), authority, RMPermissionModel.READ_RECORDS, true);
    }

    public void testRecordAsPowerUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_power_user);
        setFilingOnRecord(record_1, rm_power_user);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(record_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    public void testRecordAsUser()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(rm_user);
        Map<Capability, AccessStatus> access = recordsManagementSecurityService.getCapabilities(record_1);
        assertEquals(65, access.size()); // 58 + File
        check(access, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        check(access, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.DELETE_LINKS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        check(access, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        check(access, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        check(access, RMPermissionModel.MANUALLY_CHANGE_DISPOSITION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.MOVE_RECORDS, AccessStatus.UNDETERMINED);
        check(access, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        check(access, RMPermissionModel.PLANNING_REVIEW_CYCLES, AccessStatus.DENIED);
        check(access, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        check(access, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        check(access, RMPermissionModel.TRIGGER_AN_EVENT, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPDATE_VITAL_RECORD_CYCLE_INFORMATION, AccessStatus.DENIED);
        check(access, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
        check(access, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        check(access, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
    }

    private void checkCapability(String user, NodeRef nodeRef, String permission, AccessStatus accessStstus)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user);
        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(nodeRef);
        check(access, permission, accessStstus);
    }

    private void checkPermission(String user, NodeRef nodeRef, String permission, AccessStatus accessStstus)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user);
        assertTrue(permissionService.hasPermission(nodeRef, permission) == accessStstus);
    }

    public void testAccessAuditCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.ACCESS_AUDIT, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.ACCESS_AUDIT, AccessStatus.DENIED);
    }

    public void testAddModifyEventDatesCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkPermission(rm_user, filePlan, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(rm_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.ADD_MODIFY_EVENT_DATES, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);

        // Check closed
        // should make no difference
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);

        // try and complete some events

        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        Map<String, Serializable> eventDetails = new HashMap<String, Serializable>(3);
        eventDetails.put(CompleteEventAction.PARAM_EVENT_NAME, "event");
        eventDetails.put(CompleteEventAction.PARAM_EVENT_COMPLETED_AT, new Date());
        eventDetails.put(CompleteEventAction.PARAM_EVENT_COMPLETED_BY, test_user);
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "completeEvent", eventDetails);
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "completeEvent", eventDetails);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_1, "completeEvent", eventDetails);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        recordsManagementActionService.executeRecordsManagementAction(record_2, "completeEvent", eventDetails);

        // check protected properties

        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_EVENT_EXECUTION_COMPLETE, true);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_EVENT_EXECUTION_COMPLETED_AT, new Date());
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_EVENT_EXECUTION_COMPLETED_BY, "me");
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        // check cutoff

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        NodeRef ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "cutoff", null);
        recordsManagementActionService.executeRecordsManagementAction(record_2, "cutoff", null);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.ADD_MODIFY_EVENT_DATES, AccessStatus.ALLOWED);
    }
                            
    public void testApproveRecordsScheduledForCutoffCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        // folder level - not eligible all deny

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        // record level - not eligible all deny

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        // Set appropriate state - declare records and make eligible

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        NodeRef ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);

        // Check closed
        // should make no difference
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);

        // try and cut off

        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "cutoff", null);
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "cutoff", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_1, "cutoff", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        recordsManagementActionService.executeRecordsManagementAction(record_2, "cutoff", null);

        // check protected properties

        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_CUT_OFF_DATE, new Date());
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        // check cutoff again (it is already cut off)

        // try
        // {
        // recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "cutoff", null);
        // fail();
        // }
        // catch (AccessDeniedException ade)
        // {
        //
        // }
        // try
        // {
        // recordsManagementActionService.executeRecordsManagementAction(record_2, "cutoff", null);
        // fail();
        // }
        // catch (AccessDeniedException ade)
        // {
        //
        // }

        // checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
        // AccessStatus.DENIED);
        // checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
        // AccessStatus.DENIED);
        // checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
        // AccessStatus.DENIED);
        // checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
        // AccessStatus.DENIED);
    }

    public void testAttachRulesToMetadataPropertiesCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.ATTACH_RULES_TO_METADATA_PROPERTIES, AccessStatus.DENIED);
    }
    
    private void setupForTransfer()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        // folder level - not eligible all deny

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        // record level - not eligible all deny

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        // Set appropriate state - declare records and make eligible

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        NodeRef ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "cutoff", null);
        recordsManagementActionService.executeRecordsManagementAction(record_2, "cutoff", null);

        ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);

        // Check closed
        // should make no difference
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
    }
    
    private void setupForTransferComplete()
    {
        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_ALL_TRANSFERS, AccessStatus.ALLOWED);
        
        // check each action
        
        TransferAction transfer = (TransferAction) ctx.getBean("transfer");
        assertFalse(transfer.isExecutable(recordFolder_1, null));
        assertFalse(transfer.isExecutable(record_1, null));
        assertFalse(transfer.isExecutable(recordFolder_2, null));
        assertFalse(transfer.isExecutable(record_2, null));
        
        TransferCompleteAction transferComplete = (TransferCompleteAction) ctx.getBean("transferComplete");
        assertTrue(transferComplete.isExecutable(recordFolder_1, null));
        assertFalse(transferComplete.isExecutable(record_1, null));
        assertFalse(transferComplete.isExecutable(recordFolder_2, null));
        assertTrue(transferComplete.isExecutable(record_2, null));
    }
    
    public void testAuthorizeAllTransfersCapability()
    {
        setupForTransfer();
        
        // try and transfer
        
        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "transfer", null);
        
        recordsManagementActionService.executeRecordsManagementAction(record_2, "transfer", null);
        
        setupForTransferComplete();
        
        // try and complete the transfer
        
        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        
        recordsManagementActionService.executeRecordsManagementAction(getTransferObject(recordFolder_1), "transferComplete", null);
    }
    
    public void testAuthorizeAllTransfersCapability_TransferNegative()
    {
        setupForTransfer();
        
        // try and transfer
        
        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "transfer", null);
        
        recordsManagementActionService.executeRecordsManagementAction(record_2, "transfer", null);
        
        // -ve checks (ALF-2749)
        // note: ideally, each -ve test should be run independently (if we want outer/setup txn to rollback)
        
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "transfer", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_1, "transfer", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        
        // check protected properties

        // PROP_DISPOSITION_ACTION_STARTED_AT
        // PROP_DISPOSITION_ACTION_STARTED_BY
        // PROP_DISPOSITION_ACTION_COMPLETED_AT
        // PROP_DISPOSITION_ACTION_COMPLETED_BY

        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_DISPOSITION_ACTION_STARTED_AT, true);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_DISPOSITION_ACTION_STARTED_BY, true);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_DISPOSITION_ACTION_COMPLETED_AT, true);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_DISPOSITION_ACTION_COMPLETED_BY, true);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        // check cutoff again (it is already cut off)

        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "transfer", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        catch (AlfrescoRuntimeException are)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_2, "transfer", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        catch (AlfrescoRuntimeException are)
        {

        }
    }
    
    public void testAuthorizeAllTransfersCapability_TransferCompleteNegative()
    {
        setupForTransfer();
        
        // try and transfer
        
        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "transfer", null);
        
        recordsManagementActionService.executeRecordsManagementAction(record_2, "transfer", null);
        
        setupForTransferComplete();
        
        // try and complete the transfer
        
        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        
        recordsManagementActionService.executeRecordsManagementAction(getTransferObject(recordFolder_1), "transferComplete", null);
        
        // -ve checks (ALF-2749)
        // note: ideally, each -ve test should be run independently (if we want outer/setup txn to rollback)
        
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "transferComplete", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_1, "transferComplete", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            // will fail as this is in the same transafer which is now done.
            recordsManagementActionService.executeRecordsManagementAction(getTransferObject(record_2), "transferComplete", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        // try again - should fail

        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "transferComplete", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_2, "transferComplete", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
    }
    
    
    private NodeRef getTransferObject(NodeRef fp)
    {
        List<ChildAssociationRef> assocs = this.nodeService.getParentAssocs(fp, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        if (assocs.size() > 0)
        {
            return assocs.get(0).getParentRef();
        }
        else
        {
            return fp;
        }
    }
    
    private void setupForAccession()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        // folder level - not eligible all deny

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        // record level - not eligible all deny

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        // Set appropriate state - declare records and make eligible

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        NodeRef ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "cutoff", null);
        recordsManagementActionService.executeRecordsManagementAction(record_2, "cutoff", null);

        ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "transfer", null);
        recordsManagementActionService.executeRecordsManagementAction(record_2, "transfer", null);
        recordsManagementActionService.executeRecordsManagementAction(getTransferObject(recordFolder_1), "transferComplete", null);

        assertTrue(this.nodeService.exists(recordFolder_1));
        ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        assertTrue(this.nodeService.exists(recordFolder_1));
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        // folder level

        assertTrue(this.nodeService.exists(recordFolder_1));
        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);

        // Check closed
        // should make no difference
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
    }
    
    private void setupForAccessionComplete()
    {
        checkCapability(test_user, recordFolder_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.AUTHORIZE_NOMINATED_TRANSFERS, AccessStatus.ALLOWED);
        
        // check each action
        
        TransferAction transfer = (TransferAction) ctx.getBean("accession");
        assertFalse(transfer.isExecutable(recordFolder_1, null));
        assertFalse(transfer.isExecutable(record_1, null));
        assertFalse(transfer.isExecutable(recordFolder_2, null));
        assertFalse(transfer.isExecutable(record_2, null));
        
        TransferCompleteAction transferComplete = (TransferCompleteAction) ctx.getBean("accessionComplete");
        assertTrue(transferComplete.isExecutable(recordFolder_1, null));
        assertFalse(transferComplete.isExecutable(record_1, null));
        assertFalse(transferComplete.isExecutable(recordFolder_2, null));
        assertTrue(transferComplete.isExecutable(record_2, null));
    }
    
    public void testAuthorizeNominatedTransfersCapability()
    {
        setupForAccession();
        
        // try accession
        
        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "accession", null);
        
        recordsManagementActionService.executeRecordsManagementAction(record_2, "accession", null);
        
        setupForAccessionComplete();
        
        // try and complete the transfer
        
        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        recordsManagementActionService.executeRecordsManagementAction(getTransferObject(recordFolder_1), "accessionComplete", null);
    }
    
    public void testAuthorizeNominatedTransfersCapability_AccessionNegative()
    {
        setupForAccession();
        
        // try accession
        
        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "accession", null);
        
        recordsManagementActionService.executeRecordsManagementAction(record_2, "accession", null);
        
        // -ve checks (ALF-2749)
        // note: ideally, each -ve test should be run independently (if we want outer/setup txn to rollback)
        
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "accession", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_1, "accession", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        
        // check protected properties

        // PROP_DISPOSITION_ACTION_STARTED_AT
        // PROP_DISPOSITION_ACTION_STARTED_BY
        // PROP_DISPOSITION_ACTION_COMPLETED_AT
        // PROP_DISPOSITION_ACTION_COMPLETED_BY

        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_DISPOSITION_ACTION_STARTED_AT, true);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_DISPOSITION_ACTION_STARTED_BY, true);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_DISPOSITION_ACTION_COMPLETED_AT, true);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_DISPOSITION_ACTION_COMPLETED_BY, true);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        
        // check cutoff again (it is already cut off)

        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "accession", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        catch (AlfrescoRuntimeException are)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_2, "accession", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        catch (AlfrescoRuntimeException are)
        {

        }
    }
    
    public void testAuthorizeNominatedTransfersCapability_AccessionCompleteNegative()
    {
        setupForAccession();
        
        // try accession
        
        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "accession", null);
        
        recordsManagementActionService.executeRecordsManagementAction(record_2, "accession", null);
        
        setupForAccessionComplete();
        
        // try and complete the transfer
        
        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        recordsManagementActionService.executeRecordsManagementAction(getTransferObject(recordFolder_1), "accessionComplete", null);
        
        // -ve checks (ALF-2749)
        // note: ideally, each -ve test should be run independently (if we want outer/setup txn to rollback)
        
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "accessionComplete", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {
            
        }
        catch (AlfrescoRuntimeException are)
        {
            
        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_1, "accessionComplete", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {
            
        }
        catch (AlfrescoRuntimeException are)
        {
            
        }
        try
        {
            // will fail as this is in the same transfer which is now done.
            recordsManagementActionService.executeRecordsManagementAction(getTransferObject(record_2), "accessionComplete", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {
            
        }
        catch (AlfrescoRuntimeException are)
        {
            
        }
        
        // try again - should fail
        
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(getTransferObject(recordFolder_1), "accessionComplete", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {
            
        }
        catch (AlfrescoRuntimeException are)
        {
            
        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(getTransferObject(record_2), "accessionComplete", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {
            
        }
        catch (AlfrescoRuntimeException are)
        {
            
        }
    }
    
    public void testChangeOrDeleteReferencesCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CHANGE_OR_DELETE_REFERENCES, AccessStatus.DENIED);
    }

    public void testCloseFoldersCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        // folder level - no preconditions

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        // record level - record denies - folder allows

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        // Set appropriate state - declare records and make eligible for cut off

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        NodeRef ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        // folder level

        assertTrue(this.nodeService.exists(recordFolder_1));
        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.CLOSE_FOLDERS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        // Check closed
        // should make no difference
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CLOSE_FOLDERS, AccessStatus.DENIED);

        // try to close

        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder", null);
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder", null);

        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_1, "closeRecordFolder", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_2, "closeRecordFolder", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        // check protected properties

        // PROP_IS_CLOSED

        try
        {
            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_IS_CLOSED, true);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        // check close again (it is already closed)

        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_1, "closeRecordFolder", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(record_2, "closeRecordFolder", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
    }

    public void testCreateAndAssociateSelectionListsCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_AND_ASSOCIATE_SELECTION_LISTS, AccessStatus.DENIED);
    }

    public void testCreateModifyDestroyClassificationGuidesCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES, AccessStatus.DENIED);
    }

    public void testCreateModifyDestroyEventsCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_EVENTS, AccessStatus.DENIED);
    }

    public void testCreateModifyDestroyFileplanMetadataCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA, AccessStatus.DENIED);
    }

    public void testCreateModifyDestroyFileplanTypesCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_TYPES, AccessStatus.DENIED);
    }

    public void testCreateModifyDestroyFoldersCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        // folder level - no preconditions

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        // Check closed
        // should make no difference
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        // series level capabilities

        // fails as no filling rights ...

        checkCapability(test_user, recordCategory_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordCategory_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordCategory_1, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordCategory_2, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS, AccessStatus.ALLOWED);

        // create

        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, "name");
        properties.put(PROP_IDENTIFIER, "identifier");
        properties.put(ContentModel.PROP_TITLE, "title");
        properties.put(ContentModel.PROP_DESCRIPTION, "description");
        properties.put(PROP_REVIEW_PERIOD, "week|1");
        properties.put(PROP_VITAL_RECORD_INDICATOR, true);
        NodeRef newFolder = publicNodeService.createNode(recordCategory_1, ContentModel.ASSOC_CONTAINS, TYPE_RECORD_FOLDER, TYPE_RECORD_FOLDER,
                properties).getChildRef();

        // modify

        publicNodeService.addAspect(newFolder, ContentModel.ASPECT_OWNABLE, null);
        properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_OWNER, "me");
        publicNodeService.addProperties(newFolder, properties);
        // move should fail ...
        try
        {
            publicNodeService.moveNode(newFolder, recordCategory_2, ContentModel.ASSOC_CONTAINS, TYPE_RECORD_FOLDER);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        publicNodeService.removeProperty(newFolder, ContentModel.PROP_TITLE);
        publicNodeService.setProperty(newFolder, ContentModel.PROP_TITLE, "title");
        publicNodeService.addAspect(newFolder, ContentModel.ASPECT_TEMPORARY, null);
        publicNodeService.removeAspect(newFolder, ContentModel.ASPECT_TEMPORARY);
        publicNodeService.setProperties(newFolder, publicNodeService.getProperties(newFolder));
        try
        {
            // abstains
            publicNodeService.setType(newFolder, TYPE_RECORD_FOLDER);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        // try move

        permissionService.setPermission(filePlan, testers, RMPermissionModel.MOVE_RECORDS, true);
        publicNodeService.moveNode(newFolder, recordCategory_2, ContentModel.ASSOC_CONTAINS, TYPE_RECORD_FOLDER);

        // delete

        publicNodeService.deleteNode(newFolder);
        publicNodeService.deleteNode(recordFolder_1);
        publicNodeService.deleteNode(recordFolder_2);

    }

    public void testCreateModifyDestroyRecordTypesCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_RECORD_TYPES, AccessStatus.DENIED);
    }

    public void testCreateModifyDestroyReferenceTypesCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_REFERENCE_TYPES, AccessStatus.DENIED);
    }

    public void testCreateModifyDestroyRolesCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_ROLES, AccessStatus.DENIED);
    }

    public void testCreateModifyDestroyTimeframesCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_TIMEFRAMES, AccessStatus.DENIED);
    }

    public void testCreateModifyDestroyUsersAndGroupsCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_USERS_AND_GROUPS, AccessStatus.DENIED);
    }

    public void testCreateModifyRecordsInCuttoffFoldersCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        // folder level - no preconditions

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);

        // Check closed
        // should make no difference
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);

        // Check cutoff
        // should make no difference
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        NodeRef ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "cutoff");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "cutoff");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS, AccessStatus.ALLOWED);

        // create

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, "MyRecordCreate.txt");
        NodeRef newRecord = this.publicNodeService.createNode(recordFolder_1, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "MyRecord.txt"), ContentModel.TYPE_CONTENT, properties).getChildRef();

        // Set the content
        ContentWriter writer = this.publicContentService.getWriter(newRecord, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("There is some content in this record");

        recordsManagementActionService.executeRecordsManagementAction(newRecord, "file");
        // modify

        publicNodeService.addAspect(newRecord, ContentModel.ASPECT_OWNABLE, null);
        properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_OWNER, "me");
        publicNodeService.addProperties(newRecord, properties);
        // move should fail ...
        try
        {
            publicNodeService.moveNode(newRecord, recordCategory_2, ContentModel.ASSOC_CONTAINS, TYPE_RECORD_FOLDER);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        publicNodeService.removeProperty(newRecord, ContentModel.PROP_TITLE);
        publicNodeService.setProperty(newRecord, ContentModel.PROP_TITLE, "title");
        publicNodeService.addAspect(newRecord, ContentModel.ASPECT_TEMPORARY, null);
        publicNodeService.removeAspect(newRecord, ContentModel.ASPECT_TEMPORARY);
        publicNodeService.setProperties(newRecord, publicNodeService.getProperties(newRecord));
        try
        {
            // abstains
            publicNodeService.setType(newRecord, TYPE_RECORD_FOLDER);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

    }

    public void testCycleVitalRecordsCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_user, filePlan, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.CYCLE_VITAL_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);

        // Check closed
        // should make no difference
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);

        // try and cycle

        recordsManagementActionService.executeRecordsManagementAction(record_1, "reviewed");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "reviewed");

        recordsManagementActionService.executeRecordsManagementAction(record_1, "reviewed");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "reviewed");

        // check cutoff

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        NodeRef ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "cutoff", null);
        recordsManagementActionService.executeRecordsManagementAction(record_2, "cutoff", null);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.CYCLE_VITAL_RECORDS, AccessStatus.ALLOWED);
    }

    public void testDeclareAuditAsRecordCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.DECLARE_AUDIT_AS_RECORD, AccessStatus.DENIED);
    }

    public void testDeclareRecordsCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_user, filePlan, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        // Set appropriate state - declare records and make eligible

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        // recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        // recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);

        // Check closed
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.ALLOWED);

        // try declare

        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "declareRecord", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "declareRecord", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS, AccessStatus.DENIED);
    }

    public void testDeclareRecordsInClosedFoldersCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_user, filePlan, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        // Set appropriate state - declare records and make eligible

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        // recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        // recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);

        // Check closed
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.ALLOWED);

        // try declare in closed

        // Close
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "declareRecord", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "declareRecord", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DECLARE_RECORDS_IN_CLOSED_FOLDERS, AccessStatus.DENIED);
    }

    public void testDeleteAuditCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.DELETE_AUDIT, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.DELETE_AUDIT, AccessStatus.DENIED);
    }

    public void testDeleteLinksCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.DELETE_LINKS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.DELETE_LINKS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.DELETE_LINKS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.DELETE_LINKS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.DELETE_LINKS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.DELETE_LINKS, AccessStatus.DENIED);
    }

    public void testDeleteRecordsCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.DELETE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.DELETE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.DELETE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.DELETE_RECORDS, AccessStatus.DENIED);
    }

    public void testDestroyRecordsCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        // Set appropriate state - declare records and make eligible

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        NodeRef ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DESTROY_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);

        // Check closed
        // should make no difference
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS, AccessStatus.ALLOWED);

        // cut off

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "cutoff", null);
        recordsManagementActionService.executeRecordsManagementAction(record_2, "cutoff", null);

        // fix disposition

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        // should delete even though transfer is next ..,.

        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        nodeService.deleteNode(recordFolder_1);
        nodeService.deleteNode(record_2);

    }

    public void testDestroyRecordsScheduledForDestructionCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        // folder level - not eligible all deny

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        // record level - not eligible all deny

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        // Set appropriate state - declare records and make eligible

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        NodeRef ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "cutoff", null);
        recordsManagementActionService.executeRecordsManagementAction(record_2, "cutoff", null);

        ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "transfer", null);
        recordsManagementActionService.executeRecordsManagementAction(record_2, "transfer", null);
        // this completes both transfers :-)
        recordsManagementActionService.executeRecordsManagementAction(getTransferObject(recordFolder_1), "transferComplete", null);

        ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "accession", null);
        recordsManagementActionService.executeRecordsManagementAction(record_2, "accession", null);

        ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        // this completes both transfers :-)
        recordsManagementActionService.executeRecordsManagementAction(getTransferObject(recordFolder_1), "transferComplete", null);

        ndNodeRef = this.nodeService.getChildAssocs(recordFolder_1, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());
        ndNodeRef = this.nodeService.getChildAssocs(record_2, RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
        this.nodeService.setProperty(ndNodeRef, RecordsManagementModel.PROP_DISPOSITION_AS_OF, calendar.getTime());

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.DECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);

        // Check closed
        // should make no difference
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION, AccessStatus.ALLOWED);

        // scheduled destroy

        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "destroy", null);
        recordsManagementActionService.executeRecordsManagementAction(record_2, "destroy", null);

    }

    public void testDisplayRightsReportCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.DISPLAY_RIGHTS_REPORT, AccessStatus.DENIED);
    }

    public void testEditDeclaredRecordMetadataCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        // Set appropriate state - declare records and make eligible

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);

        // Check closed
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA, AccessStatus.ALLOWED);

        // try to modify

        publicNodeService.addAspect(record_1, ContentModel.ASPECT_OWNABLE, null);
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_OWNER, "me");
        publicNodeService.addProperties(record_1, properties);
        // move should fail ...
        try
        {
            publicNodeService.moveNode(record_1, recordCategory_2, ContentModel.ASSOC_CONTAINS, TYPE_RECORD_FOLDER);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        publicNodeService.removeProperty(record_1, ContentModel.PROP_TITLE);
        publicNodeService.setProperty(record_1, ContentModel.PROP_TITLE, "title");
        publicNodeService.addAspect(record_1, ContentModel.ASPECT_TEMPORARY, null);
        publicNodeService.removeAspect(record_1, ContentModel.ASPECT_TEMPORARY);
        publicNodeService.setProperties(record_1, publicNodeService.getProperties(record_1));
        try
        {
            // abstains
            publicNodeService.setType(record_1, TYPE_RECORD_FOLDER);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
    }

    public void testEditNonRecordMetadataCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_user, filePlan, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.EDIT_NON_RECORD_METADATA, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.EDIT_NON_RECORD_METADATA);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.EDIT_NON_RECORD_METADATA, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        // Check closed
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_NON_RECORD_METADATA, AccessStatus.DENIED);

        // try to modify

        publicNodeService.addAspect(recordFolder_1, ContentModel.ASPECT_OWNABLE, null);
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_OWNER, "me");
        publicNodeService.addProperties(recordFolder_1, properties);
        // move should fail ...
        try
        {
            publicNodeService.moveNode(recordFolder_1, recordCategory_2, ContentModel.ASSOC_CONTAINS, TYPE_RECORD_FOLDER);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        publicNodeService.removeProperty(recordFolder_1, ContentModel.PROP_TITLE);
        publicNodeService.setProperty(recordFolder_1, ContentModel.PROP_TITLE, "title");
        publicNodeService.addAspect(recordFolder_1, ContentModel.ASPECT_TEMPORARY, null);
        publicNodeService.removeAspect(recordFolder_1, ContentModel.ASPECT_TEMPORARY);
        publicNodeService.setProperties(recordFolder_1, publicNodeService.getProperties(recordFolder_1));
        try
        {
            // abstains
            publicNodeService.setType(recordFolder_1, TYPE_RECORD_FOLDER);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
    }

    public void testEditRecordMetadataCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_user, filePlan, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(rm_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.EDIT_RECORD_METADATA, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.EDIT_RECORD_METADATA);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.EDIT_RECORD_METADATA, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);

        // Check closed
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EDIT_RECORD_METADATA, AccessStatus.ALLOWED);

        // try to modify

        publicNodeService.addAspect(record_1, ContentModel.ASPECT_OWNABLE, null);
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_OWNER, "me");
        publicNodeService.addProperties(record_1, properties);
        // move should fail ...
        try
        {
            publicNodeService.moveNode(record_1, recordCategory_2, ContentModel.ASSOC_CONTAINS, TYPE_RECORD_FOLDER);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        publicNodeService.removeProperty(record_1, ContentModel.PROP_TITLE);
        publicNodeService.setProperty(record_1, ContentModel.PROP_TITLE, "title");
        publicNodeService.addAspect(record_1, ContentModel.ASPECT_TEMPORARY, null);
        publicNodeService.removeAspect(record_1, ContentModel.ASPECT_TEMPORARY);
        publicNodeService.setProperties(record_1, publicNodeService.getProperties(record_1));
        try
        {
            // abstains
            publicNodeService.setType(record_1, TYPE_RECORD_FOLDER);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
    }

    public void testEditSelectionListsCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.EDIT_SELECTION_LISTS, AccessStatus.DENIED);
    }

    public void testEnableDisableAuditByTypesCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.ENABLE_DISABLE_AUDIT_BY_TYPES, AccessStatus.DENIED);
    }

    public void testExportAuditCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.EXPORT_AUDIT, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.EXPORT_AUDIT, AccessStatus.DENIED);
    }

    public void testExtendRetentionPeriodOrFreezeCapability()
    {
        // freeze and unfreeze is part of most other tests - this jusr duplicates the basics ...

        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);

        // check frozen - can be in mutiple holds/freezes ..

        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);

        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);

        // Check closed
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.EXTEND_RETENTION_PERIOD_OR_FREEZE, AccessStatus.ALLOWED);

    }

    public void testFileRecordsCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        // Record
        checkPermission(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.FILE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.FILE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.FILE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        // Check closed
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.FILE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.FILE_RECORDS, AccessStatus.ALLOWED);

        // Do some filing ...

        // create

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, "MyRecordCreate.txt");
        NodeRef newRecord_1 = this.publicNodeService.createNode(recordFolder_1, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "MyRecord.txt"), ContentModel.TYPE_CONTENT, properties).getChildRef();

        // Set the content (relies on owner in the DM side until it becode RM ified ...)
        ContentWriter writer = this.publicContentService.getWriter(newRecord_1, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("There is some content in this record");

        assertFalse(recordsManagementService.isFilePlanComponent(newRecord_1));
        recordsManagementActionService.executeRecordsManagementAction(newRecord_1, "file");
        assertTrue(recordsManagementService.isFilePlanComponent(newRecord_1));

        properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, "MyRecordCreate.txt");
        NodeRef newRecord_2 = this.publicNodeService.createNode(recordFolder_2, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "MyRecord.txt"), ContentModel.TYPE_CONTENT, properties).getChildRef();

        // Set the content
        writer = this.publicContentService.getWriter(newRecord_2, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("There is some content in this record");

        recordsManagementActionService.executeRecordsManagementAction(newRecord_2, "file");

        // update with permissions in place ...

        writer = this.publicContentService.getWriter(newRecord_1, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("There is some updated content in this record");

        writer = this.publicContentService.getWriter(newRecord_2, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("There is some content in this record");
    }

    public void testMakeOptionalPropertiesMandatoryCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.MAKE_OPTIONAL_PARAMETERS_MANDATORY, AccessStatus.DENIED);
    }

    public void testManageAccessControlsCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.MANAGE_ACCESS_CONTROLS, AccessStatus.DENIED);
    }

    public void testManageAccessRightsCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
    }

    public void testManuallyChangeDispositionDatesCapability()
    {
        // TODO: The action is not yet done
    }

    public void testMapClassificationGuideMetadataCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.MAP_CLASSIFICATION_GUIDE_METADATA, AccessStatus.DENIED);
    }

    public void testMapEmailMetadataCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.MAP_EMAIL_METADATA, AccessStatus.DENIED);
    }

    public void testMoveRecordsCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.MOVE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.MOVE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.MOVE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.MOVE_RECORDS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.MOVE_RECORDS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.MOVE_RECORDS, AccessStatus.DENIED);
    }

    public void testPasswordControlCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.PASSWORD_CONTROL, AccessStatus.DENIED);
    }

    public void testPlanningReviewCyclesCapability()
    {
        // TODO: Waiting for the appropriate action
    }

    public void testReOpenFoldersCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkPermission(rm_user, filePlan, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        // Check closed
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.RE_OPEN_FOLDERS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.RE_OPEN_FOLDERS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.RE_OPEN_FOLDERS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.ALLOWED);
        checkCapability(test_user, record_2, RMPermissionModel.RE_OPEN_FOLDERS, AccessStatus.DENIED);

    }

    public void testSelectAuditMetadataCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.SELECT_AUDIT_METADATA, AccessStatus.DENIED);
    }

    public void testTriggerAnEventCapability()
    {
        // TODO: Waiting for action
    }

    public void testUndeclareRecordsCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        // Set appropriate state - declare records and make eligible

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_1, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_1, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_1, "declareRecord");

        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        nodeService.setProperty(record_2, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
        nodeService.setProperty(record_2, ContentModel.PROP_TITLE, "titleValue");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "declareRecord");

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.UNDECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.UNDECLARE_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.UNDECLARE_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);

        // check frozen

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);

        // Check closed
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "closeRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "closeRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.ALLOWED);

        // try undeclare

        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "undeclareRecord", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        recordsManagementActionService.executeRecordsManagementAction(record_1, "undeclareRecord");
        try
        {
            recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "undeclareRecord", null);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        recordsManagementActionService.executeRecordsManagementAction(record_2, "undeclareRecord");

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNDECLARE_RECORDS, AccessStatus.DENIED);
    }

    public void testUnfreezeCapability()
    {
        // freeze and unfreeze is part of most other tests - this jusr duplicates the basics ...

        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.UNFREEZE, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.UNFREEZE);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.UNFREEZE, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);

        // check frozen - can be in mutiple holds/freezes ..

        checkCapability(test_user, recordFolder_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, record_1, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, recordFolder_2, RMPermissionModel.UNFREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.UNFREEZE, AccessStatus.ALLOWED);

        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "unfreeze");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "unfreeze");

    }

    public void testUpdateClassificationDatesCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.UPDATE_CLASSIFICATION_DATES, AccessStatus.DENIED);
    }

    public void testUpdateExemptionCategoriesCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.UPDATE_EXEMPTION_CATEGORIES, AccessStatus.DENIED);
    }

    public void testUpdateTriggerDatesCapability()
    {
        // TODO: waiting for action
    }

    public void testUpdateVitalRecordCycleInformationCapability()
    {
        // TODO: ?
    }

    public void testUpgradeDowngradeAndDeclassifyRecordsCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS, AccessStatus.DENIED);
    }

    public void testViewRecordsCapability()
    {
        // capability is checked above - just check permission assignments
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        checkPermission(rm_user, filePlan, RMPermissionModel.VIEW_RECORDS, AccessStatus.ALLOWED);
        // already tested in many places above
    }

    public void testViewUpdateReasonsForFreezeCapability()
    {
        // Folder
        checkPermission(AuthenticationUtil.getSystemUserName(), filePlan, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkPermission(rm_administrator, filePlan, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkPermission(rm_records_manager, filePlan, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkPermission(rm_security_officer, filePlan, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkPermission(rm_power_user, filePlan, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkPermission(rm_user, filePlan, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, record_1, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), recordFolder_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_administrator, recordFolder_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_records_manager, recordFolder_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_security_officer, recordFolder_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, recordFolder_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, recordFolder_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), record_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_administrator, record_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_records_manager, record_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_security_officer, record_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, record_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, record_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "freeze", params);
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        recordsManagementActionService.executeRecordsManagementAction(record_2, "freeze", params);

        // folder level

        checkCapability(AuthenticationUtil.getSystemUserName(), getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        // record level

        checkCapability(AuthenticationUtil.getSystemUserName(), getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_administrator, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_records_manager, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_security_officer, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        checkCapability(AuthenticationUtil.getSystemUserName(), getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_administrator, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_records_manager, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(rm_security_officer, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_power_user, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(rm_user, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        // check person with no access and add read and write
        // Filing

        checkCapability(test_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, record_2, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);
        permissionService.setPermission(filePlan, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setInheritParentPermissions(recordCategory_1, false);
        permissionService.setInheritParentPermissions(recordCategory_2, false);
        permissionService.setPermission(recordCategory_1, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordCategory_2, testers, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, true);

        checkCapability(test_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE);

        checkCapability(test_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, true);

        checkCapability(test_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS);

        checkCapability(test_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);

        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_RECORDS, true);

        checkCapability(test_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);

        permissionService.deletePermission(recordFolder_1, testers, RMPermissionModel.FILING);
        permissionService.deletePermission(recordFolder_2, testers, RMPermissionModel.FILING);

        checkCapability(test_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);

        permissionService.setPermission(recordFolder_1, testers, RMPermissionModel.FILING, true);
        permissionService.setPermission(recordFolder_2, testers, RMPermissionModel.FILING, true);

        checkCapability(test_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);

        // check frozen - can be in multiple holds/freezes ..

        checkCapability(test_user, getHold(recordFolder_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(record_1), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);
        checkCapability(test_user, getHold(recordFolder_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.DENIED);
        checkCapability(test_user, getHold(record_2), RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, AccessStatus.ALLOWED);

        // TODO: property is not yet duplicated, waiting for action.

        // test filter - from the freeze object

        Map<QName, Serializable> returned = publicNodeService.getProperties(getHold(recordFolder_1));
        assertTrue(returned.containsKey(RecordsManagementModel.PROP_HOLD_REASON));
        assertNotNull(publicNodeService.getProperty(getHold(recordFolder_1), RecordsManagementModel.PROP_HOLD_REASON));

        permissionService.deletePermission(filePlan, testers, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE);

        returned = publicNodeService.getProperties(getHold(recordFolder_1));
        assertFalse(returned.containsKey(RecordsManagementModel.PROP_HOLD_REASON));
        try
        {
            publicNodeService.getProperty(getHold(recordFolder_1), RecordsManagementModel.PROP_HOLD_REASON);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        // test query

        // update

        permissionService.setPermission(filePlan, testers, RMPermissionModel.FILING, true);
        try
        {
            publicNodeService.setProperty(getHold(recordFolder_1), RecordsManagementModel.PROP_HOLD_REASON, "meep");
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }
        permissionService.setPermission(filePlan, testers, RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE, true);
        // TODO: fix reject by updateProperties - no capabilty lets it through even though not protected
        // publicNodeService.setProperty(getHold(recordFolder_1), RecordsManagementModel.PROP_HOLD_REASON, "meep");

        // update by action

        // 
    }

    private NodeRef getHold(NodeRef held)
    {
        List<ChildAssociationRef> holdAssocs = nodeService.getChildAssocs(filePlan, RecordsManagementModel.ASSOC_HOLDS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef holdAssoc : holdAssocs)
        {
            List<ChildAssociationRef> freezeAssocs = nodeService.getChildAssocs(holdAssoc.getChildRef());
            for (ChildAssociationRef inHold : freezeAssocs)
            {
                if (inHold.getChildRef().equals(held))
                {
                    return holdAssoc.getChildRef();
                }
                List<ChildAssociationRef> heldFolderChildren = nodeService.getChildAssocs(inHold.getChildRef());
                for (ChildAssociationRef car : heldFolderChildren)
                {
                    if (car.getChildRef().equals(held))
                    {
                        return holdAssoc.getChildRef();
                    }
                }
            }
        }
        return held;
    }

    private void check(Map<Capability, AccessStatus> access, String name, AccessStatus accessStatus)
    {
        Capability capability = recordsManagementSecurityService.getCapability(name);
        assertNotNull(capability);
        assertEquals(accessStatus, access.get(capability));
    }

    private static ImporterBinding REPLACE_BINDING = new ImporterBinding()
    {

        public UUID_BINDING getUUIDBinding()
        {
            return UUID_BINDING.UPDATE_EXISTING;
        }

        public String getValue(String key)
        {
            return null;
        }

        public boolean allowReferenceWithinTransaction()
        {
            return false;
        }

        public QName[] getExcludedClasses()
        {
            return null;
        }

    };

}
