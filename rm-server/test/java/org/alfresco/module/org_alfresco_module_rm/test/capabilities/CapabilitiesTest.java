/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.model.PermissionModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Test the RM permissions model
 * 
 * @author Andy Hind
 * @author Roy Wetherall
 */
public class CapabilitiesTest extends BaseRMTestCase implements
        RMPermissionModel, RecordsManagementModel
{
    private NodeRef record;
    
    private PermissionModel permissionModel;
    private PermissionService permissionService;

    @Override
    protected void initServices()
    {
        super.initServices();

        permissionModel = (PermissionModel) applicationContext.getBean("permissionsModelDAO");
        permissionService = (PermissionService) applicationContext.getBean("PermissionService");
    }

    @Override
    protected boolean isUserTest()
    {
        return true;
    }
    
    @Override
    protected void setupTestDataImpl()
    {
        super.setupTestDataImpl();
        
        record = utils.createRecord(rmFolder, "CapabilitiesTest.txt");
    }
    
    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        super.setupTestUsersImpl(filePlan);
        
        // Give all the users file permission objects
        for (String user : testUsers)
        {
            securityService.setPermission(filePlan, user, FILING);
            securityService.setPermission(rmContainer, user, FILING);
        }                
    }

    protected void check(Map<Capability, AccessStatus> access, String name, AccessStatus accessStatus)
    {
        Capability capability = capabilityService.getCapability(name);
        assertNotNull(capability);
        assertEquals(accessStatus, access.get(capability));
    }

    /**
     * Check the RM permission model
     */
    public void testPermissionsModel()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        // As system user
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(AuthenticationUtil
                                        .getSystemUserName());

                        Set<PermissionReference> exposed = permissionModel
                                .getExposedPermissions(ASPECT_FILE_PLAN_COMPONENT);
                        assertEquals(6, exposed.size());
                        assertTrue(exposed.contains(permissionModel
                                .getPermissionReference(
                                        ASPECT_FILE_PLAN_COMPONENT,
                                        ROLE_ADMINISTRATOR)));

                        // Check all the permission are there
                        Set<PermissionReference> all = permissionModel
                                .getAllPermissions(ASPECT_FILE_PLAN_COMPONENT);
                        assertEquals(58 /* capbilities */* 2 + 5 /* roles */
                                + (2 /* Read+File */* 2) + 1 /* Filing */, all
                                .size());

                        /*
                         * Check the granting for each permission. It is assumed
                         * that the ROLE_ADMINISTRATOR always has grant
                         * permission so is automatically checked.
                         */
                        checkGranting(ACCESS_AUDIT, ROLE_RECORDS_MANAGER);
                        checkGranting(ADD_MODIFY_EVENT_DATES,
                                ROLE_RECORDS_MANAGER, ROLE_SECURITY_OFFICER,
                                ROLE_POWER_USER);
                        checkGranting(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(ATTACH_RULES_TO_METADATA_PROPERTIES,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(AUTHORIZE_ALL_TRANSFERS,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(AUTHORIZE_NOMINATED_TRANSFERS,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(CHANGE_OR_DELETE_REFERENCES,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(CLOSE_FOLDERS, ROLE_RECORDS_MANAGER,
                                ROLE_SECURITY_OFFICER, ROLE_POWER_USER);
                        checkGranting(CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                ROLE_RECORDS_MANAGER, ROLE_SECURITY_OFFICER);
                        checkGranting(CREATE_MODIFY_DESTROY_EVENTS,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(CREATE_MODIFY_DESTROY_FOLDERS,
                                ROLE_RECORDS_MANAGER, ROLE_SECURITY_OFFICER,
                                ROLE_POWER_USER);
                        checkGranting(CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(CREATE_MODIFY_DESTROY_ROLES,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(CYCLE_VITAL_RECORDS,
                                ROLE_RECORDS_MANAGER, ROLE_SECURITY_OFFICER,
                                ROLE_POWER_USER);
                        checkGranting(DECLARE_AUDIT_AS_RECORD,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(DECLARE_RECORDS, ROLE_RECORDS_MANAGER,
                                ROLE_SECURITY_OFFICER, ROLE_POWER_USER,
                                ROLE_USER);
                        checkGranting(DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                ROLE_RECORDS_MANAGER, ROLE_SECURITY_OFFICER,
                                ROLE_POWER_USER);
                        checkGranting(DELETE_AUDIT, ROLE_RECORDS_MANAGER);
                        checkGranting(DELETE_LINKS, ROLE_RECORDS_MANAGER);
                        checkGranting(DELETE_RECORDS, ROLE_RECORDS_MANAGER);
                        checkGranting(DESTROY_RECORDS, ROLE_RECORDS_MANAGER);
                        checkGranting(
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(DISPLAY_RIGHTS_REPORT,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(EDIT_DECLARED_RECORD_METADATA,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(EDIT_NON_RECORD_METADATA,
                                ROLE_RECORDS_MANAGER, ROLE_SECURITY_OFFICER,
                                ROLE_POWER_USER);
                        checkGranting(EDIT_RECORD_METADATA,
                                ROLE_RECORDS_MANAGER, ROLE_SECURITY_OFFICER,
                                ROLE_POWER_USER);
                        checkGranting(EDIT_SELECTION_LISTS,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(ENABLE_DISABLE_AUDIT_BY_TYPES,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(EXPORT_AUDIT, ROLE_RECORDS_MANAGER);
                        checkGranting(EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(MANAGE_ACCESS_CONTROLS);
                        checkGranting(MANAGE_ACCESS_RIGHTS,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(MANUALLY_CHANGE_DISPOSITION_DATES,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(MAP_CLASSIFICATION_GUIDE_METADATA,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(MAP_EMAIL_METADATA, ROLE_RECORDS_MANAGER);
                        checkGranting(MOVE_RECORDS, ROLE_RECORDS_MANAGER);
                        checkGranting(PASSWORD_CONTROL, ROLE_RECORDS_MANAGER);
                        checkGranting(PLANNING_REVIEW_CYCLES,
                                ROLE_RECORDS_MANAGER, ROLE_SECURITY_OFFICER,
                                ROLE_POWER_USER);
                        checkGranting(RE_OPEN_FOLDERS, ROLE_RECORDS_MANAGER,
                                ROLE_SECURITY_OFFICER, ROLE_POWER_USER);
                        checkGranting(SELECT_AUDIT_METADATA,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(TRIGGER_AN_EVENT, ROLE_RECORDS_MANAGER);
                        checkGranting(UNDECLARE_RECORDS, ROLE_RECORDS_MANAGER);
                        checkGranting(UNFREEZE, ROLE_RECORDS_MANAGER);
                        checkGranting(UPDATE_CLASSIFICATION_DATES,
                                ROLE_RECORDS_MANAGER, ROLE_SECURITY_OFFICER);
                        checkGranting(UPDATE_EXEMPTION_CATEGORIES,
                                ROLE_RECORDS_MANAGER, ROLE_SECURITY_OFFICER);
                        checkGranting(UPDATE_TRIGGER_DATES,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                ROLE_RECORDS_MANAGER);
                        checkGranting(UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                ROLE_RECORDS_MANAGER, ROLE_SECURITY_OFFICER);
                        checkGranting(VIEW_RECORDS, ROLE_RECORDS_MANAGER,
                                ROLE_SECURITY_OFFICER, ROLE_POWER_USER,
                                ROLE_USER);
                        checkGranting(VIEW_UPDATE_REASONS_FOR_FREEZE,
                                ROLE_RECORDS_MANAGER);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Check that the roles passed have grant on the permission passed.
     * 
     * @param permission
     *            permission
     * @param roles
     *            grant roles
     */
    private void checkGranting(String permission, String... roles)
    {
        Set<PermissionReference> granting = permissionModel
                .getGrantingPermissions(permissionModel.getPermissionReference(
                        RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT,
                        permission));
        Set<PermissionReference> test = new HashSet<PermissionReference>();
        test.addAll(granting);
        Set<PermissionReference> nonRM = new HashSet<PermissionReference>();
        for (PermissionReference pr : granting)
        {
            if (!pr.getQName().equals(
                    RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
            {
                nonRM.add(pr);
            }
        }
        test.removeAll(nonRM);
        assertEquals(roles.length + 2, test.size());

        assertTrue(test.contains(permissionModel.getPermissionReference(
                RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT,
                ROLE_ADMINISTRATOR)));
        for (String role : roles)
        {
            assertTrue(test.contains(permissionModel.getPermissionReference(
                    RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT, role)));
        }

    }

    /**
     * Test the capability configuration
     */
    public void testConfig()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        // As system user
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(AuthenticationUtil
                                        .getSystemUserName());

                        assertEquals(6, securityService.getProtectedAspects()
                                .size());
                        assertEquals(13, securityService
                                .getProtectedProperties().size());

                        // Test action wire up
                        testCapabilityActions(0, ACCESS_AUDIT);
                        testCapabilityActions(2, ADD_MODIFY_EVENT_DATES);
                        testCapabilityActions(2,
                                APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF);
                        testCapabilityActions(0,
                                ATTACH_RULES_TO_METADATA_PROPERTIES);
                        testCapabilityActions(2, AUTHORIZE_ALL_TRANSFERS);
                        testCapabilityActions(2, AUTHORIZE_NOMINATED_TRANSFERS);
                        testCapabilityActions(0, CHANGE_OR_DELETE_REFERENCES);
                        testCapabilityActions(1, CLOSE_FOLDERS);
                        testCapabilityActions(0,
                                CREATE_AND_ASSOCIATE_SELECTION_LISTS);
                        testCapabilityActions(0,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES);
                        testCapabilityActions(0, CREATE_MODIFY_DESTROY_EVENTS);
                        testCapabilityActions(0,
                                CREATE_MODIFY_DESTROY_FILEPLAN_METADATA);
                        testCapabilityActions(0,
                                CREATE_MODIFY_DESTROY_FILEPLAN_TYPES);
                        testCapabilityActions(0, CREATE_MODIFY_DESTROY_FOLDERS);
                        testCapabilityActions(0,
                                CREATE_MODIFY_DESTROY_RECORD_TYPES);
                        testCapabilityActions(0,
                                CREATE_MODIFY_DESTROY_REFERENCE_TYPES);
                        testCapabilityActions(0, CREATE_MODIFY_DESTROY_ROLES);
                        testCapabilityActions(0,
                                CREATE_MODIFY_DESTROY_TIMEFRAMES);
                        testCapabilityActions(0,
                                CREATE_MODIFY_DESTROY_USERS_AND_GROUPS);
                        testCapabilityActions(0,
                                CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS);
                        testCapabilityActions(1, CYCLE_VITAL_RECORDS);
                        testCapabilityActions(0, DECLARE_AUDIT_AS_RECORD);
                        testCapabilityActions(2, DECLARE_RECORDS);
                        testCapabilityActions(1,
                                DECLARE_RECORDS_IN_CLOSED_FOLDERS);
                        testCapabilityActions(0, DELETE_AUDIT);
                        testCapabilityActions(0, DELETE_LINKS);
                        testCapabilityActions(0, DELETE_RECORDS);
                        testCapabilityActions(0, DESTROY_RECORDS);
                        testCapabilityActions(1,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION);
                        testCapabilityActions(0, DISPLAY_RIGHTS_REPORT);
                        testCapabilityActions(0, EDIT_DECLARED_RECORD_METADATA);
                        testCapabilityActions(0, EDIT_NON_RECORD_METADATA);
                        testCapabilityActions(0, EDIT_RECORD_METADATA);
                        testCapabilityActions(0, EDIT_SELECTION_LISTS);
                        testCapabilityActions(0, ENABLE_DISABLE_AUDIT_BY_TYPES);
                        testCapabilityActions(0, EXPORT_AUDIT);
                        testCapabilityActions(1,
                                EXTEND_RETENTION_PERIOD_OR_FREEZE);
                        testCapabilityActions(1, FILE_RECORDS);
                        testCapabilityActions(0,
                                MAKE_OPTIONAL_PARAMETERS_MANDATORY);
                        testCapabilityActions(0, MANAGE_ACCESS_CONTROLS);
                        testCapabilityActions(0, MANAGE_ACCESS_RIGHTS);
                        testCapabilityActions(1,
                                MANUALLY_CHANGE_DISPOSITION_DATES);
                        testCapabilityActions(0,
                                MAP_CLASSIFICATION_GUIDE_METADATA);
                        testCapabilityActions(0, MAP_EMAIL_METADATA);
                        testCapabilityActions(0, MOVE_RECORDS);
                        testCapabilityActions(0, PASSWORD_CONTROL);
                        testCapabilityActions(1, PLANNING_REVIEW_CYCLES);
                        testCapabilityActions(1, RE_OPEN_FOLDERS);
                        testCapabilityActions(0, SELECT_AUDIT_METADATA);
                        testCapabilityActions(0, TRIGGER_AN_EVENT);
                        testCapabilityActions(1, UNDECLARE_RECORDS);
                        testCapabilityActions(2, UNFREEZE);
                        testCapabilityActions(0, UPDATE_CLASSIFICATION_DATES);
                        testCapabilityActions(0, UPDATE_EXEMPTION_CATEGORIES);
                        testCapabilityActions(0, UPDATE_TRIGGER_DATES);
                        testCapabilityActions(0,
                                UPDATE_VITAL_RECORD_CYCLE_INFORMATION);
                        testCapabilityActions(0,
                                UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS);
                        testCapabilityActions(0, VIEW_RECORDS);
                        testCapabilityActions(1, VIEW_UPDATE_REASONS_FOR_FREEZE);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test the capability actions
     * 
     * @param count
     * @param capability
     */
    private void testCapabilityActions(int count, String capability)
    {
        assertEquals(count, capabilityService.getCapability(capability)
                .getActionNames().size());
    }

    /**
     * Test file plan as system
     */
    public void testFilePlanAsSystem()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        // As system user
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(AuthenticationUtil
                                        .getSystemUserName());

                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(filePlan);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    public void testFilePlanAsAdmin()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(AuthenticationUtil
                                        .getAdminUserName());
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(filePlan);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test file plan as administrator
     */
    public void testFilePlanAsAdministrator()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(rmAdminName);
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(filePlan);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    public void testFilePlanAsRecordsManager()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        Set<AccessPermission> permissions = permissionService
                                .getAllSetPermissions(filePlan);
                        for (AccessPermission ap : permissions)
                        {
                            System.out.println(ap.getAuthority() + " -> "
                                    + ap.getPermission() + " ("
                                    + ap.getPosition() + ")");
                        }

                        AuthenticationUtil
                                .setFullyAuthenticatedUser(recordsManagerName);
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(filePlan);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    public void testFilePlanAsSecurityOfficer()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        List<String> temp = new ArrayList<String>();
                        temp.add("ACCESS_AUDIT");
                        capabilityService.getCapabilitiesAccessState(filePlan, temp);
                        
                        
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(securityOfficerName);
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(filePlan);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test file plan as power user
     */
    public void testFilePlanAsPowerUser()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(powerUserName);
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(filePlan);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.DENIED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test file plan as user
     */
    public void testFilePlanAsUser()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(rmUserName);
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(filePlan);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.DENIED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record category as system
     */
    public void testRecordCategoryAsSystem()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);

                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(rmContainer);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record category as admin
     */
    public void testRecordCategoryAsAdmin()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(AuthenticationUtil
                                        .getAdminUserName());
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(rmContainer);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record category as administrator
     */
    public void testRecordCategoryAsAdministrator()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(rmAdminName);
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(rmContainer);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record category as records manager
     */
    public void testRecordCategoryAsRecordsManager()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(recordsManagerName);
                        // permissionService.setPermission(recordCategory_1,
                        // rm_records_manager, FILING, true);
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(rmContainer);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record category as security officer
     */
    public void testRecordCategoryAsSecurityOfficer()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(securityOfficerName);
                        // permissionService.setPermission(recordCategory_1,
                        // securityOfficerName, FILING, true);
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(rmContainer);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record category as power user
     */
    public void testRecordCategoryAsPowerUser()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(powerUserName);
                        // permissionService.setPermission(rmContainer,
                        // powerUserName, FILING, true);
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(rmContainer);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.DENIED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record category as user
     */
    public void testRecordCategoryAsUser()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(rmUserName);
                        // permissionService.setPermission(rmContainer,
                        // rmUserName, FILING, true);
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(rmContainer);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.DENIED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record folder as system
     */
    public void testRecordFolderAsSystem()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);

                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(rmFolder);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.ALLOWED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.ALLOWED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED); // rmFolder
                                                                                 // is
                                                                                 // not
                                                                                 // a
                                                                                 // vital
                                                                                 // record
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.ALLOWED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.ALLOWED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);

    }

    /**
     * Test record folder as admin
     */
    public void testRecordFolderAsAdmin()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(AuthenticationUtil
                                        .getAdminUserName());
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(rmFolder);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.ALLOWED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.ALLOWED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.ALLOWED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.ALLOWED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);

    }

    /**
     * Test record folder as administrator
     */
    public void testRecordFolderAsAdministrator()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(rmAdminName);
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(rmFolder);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.ALLOWED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.ALLOWED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.ALLOWED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.ALLOWED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record folder as records manager
     */
    public void testRecordFolderAsRecordsManager()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil.setFullyAuthenticatedUser(recordsManagerName);
                        //setFilingOnRecordFolder(rmFolder, recordsManagerName);
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(rmFolder);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.ALLOWED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.ALLOWED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.ALLOWED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.ALLOWED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.ALLOWED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record folder as security officer
     */
    public void testRecordFolderAsSecurityOfficer()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil.setFullyAuthenticatedUser(securityOfficerName);
                        //setFilingOnRecordFolder(rmFolder, securityOfficerName);
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(rmFolder);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.ALLOWED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.ALLOWED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record folder as power user
     */
    public void testRecordFolderAsPowerUser()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil.setFullyAuthenticatedUser(powerUserName);
                        //setFilingOnRecordFolder(rmFolder, powerUserName);
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(rmFolder);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.ALLOWED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.ALLOWED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.DENIED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record folder as user
     */
    public void testRecordFolderAsUser()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(rmUserName);
                        //setFilingOnRecordFolder(rmFolder, rmUserName);
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(rmFolder);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.DENIED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record as system
     */
    public void testRecordAsSystem()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);
                        Map<Capability, AccessStatus> access = capabilityService.getCapabilitiesAccessState(record);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.ALLOWED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.ALLOWED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.UNDETERMINED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record as admin
     */
    public void testRecordAsAdmin()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(AuthenticationUtil
                                        .getAdminUserName());
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(record);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.ALLOWED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.ALLOWED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.UNDETERMINED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record as administrator
     */
    public void testRecordAsAdministrator()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(rmAdminName);
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(record);
                        assertEquals(59, access.size());
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.ALLOWED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.ALLOWED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.UNDETERMINED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record as records manager
     */
    public void testRecordAsRecordsManager()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(recordsManagerName);
                      //  setFilingOnRecord(record, recordsManagerName);
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(record);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.ALLOWED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.ALLOWED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.UNDETERMINED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.ALLOWED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.ALLOWED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.ALLOWED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_SELECTION_LISTS,
                                AccessStatus.ALLOWED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.ALLOWED);
                        check(access, EXPORT_AUDIT, AccessStatus.ALLOWED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.ALLOWED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.ALLOWED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS,
                                AccessStatus.ALLOWED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.ALLOWED);
                        check(access, MOVE_RECORDS, AccessStatus.UNDETERMINED);
                        check(access, PASSWORD_CONTROL, AccessStatus.ALLOWED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.ALLOWED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record as security officer
     */
    public void testRecordAsSecurityOfficer()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(securityOfficerName);
                   //     setFilingOnRecord(record, securityOfficerName);
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(record);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.ALLOWED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.ALLOWED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.ALLOWED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test records as power user
     */
    public void testRecordAsPowerUser()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {

                        AuthenticationUtil
                                .setFullyAuthenticatedUser(powerUserName);
                 //       setFilingOnRecord(record, powerUserName);
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(record);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_RECORD_METADATA,
                                AccessStatus.ALLOWED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.DENIED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    /**
     * Test record as user
     */
    public void testRecordAsUser()
    {
        retryingTransactionHelper.doInTransaction(
                new RetryingTransactionCallback<Object>()
                {
                    @Override
                    public Object execute() throws Throwable
                    {
                        AuthenticationUtil
                                .setFullyAuthenticatedUser(rmUserName);
                        // setFilingOnRecord(record, rmUserName);
                        Map<Capability, AccessStatus> access = capabilityService
                                .getCapabilitiesAccessState(record);
                        assertEquals(59, access.size()); // 58 + File
                        check(access, ACCESS_AUDIT, AccessStatus.DENIED);
                        check(access, ADD_MODIFY_EVENT_DATES,
                                AccessStatus.DENIED);
                        check(access, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF,
                                AccessStatus.DENIED);
                        check(access, ATTACH_RULES_TO_METADATA_PROPERTIES,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_ALL_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, AUTHORIZE_NOMINATED_TRANSFERS,
                                AccessStatus.DENIED);
                        check(access, CHANGE_OR_DELETE_REFERENCES,
                                AccessStatus.DENIED);
                        check(access, CLOSE_FOLDERS, AccessStatus.DENIED);
                        check(access, CREATE_AND_ASSOCIATE_SELECTION_LISTS,
                                AccessStatus.DENIED);
                        check(access,
                                CREATE_MODIFY_DESTROY_CLASSIFICATION_GUIDES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_EVENTS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_METADATA,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FILEPLAN_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_RECORD_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_REFERENCE_TYPES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_ROLES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_TIMEFRAMES,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_DESTROY_USERS_AND_GROUPS,
                                AccessStatus.DENIED);
                        check(access, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, CYCLE_VITAL_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_AUDIT_AS_RECORD,
                                AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, DECLARE_RECORDS_IN_CLOSED_FOLDERS,
                                AccessStatus.DENIED);
                        check(access, DELETE_AUDIT, AccessStatus.DENIED);
                        check(access, DELETE_LINKS, AccessStatus.UNDETERMINED);
                        check(access, DELETE_RECORDS, AccessStatus.DENIED);
                        check(access, DESTROY_RECORDS, AccessStatus.DENIED);
                        check(access,
                                DESTROY_RECORDS_SCHEDULED_FOR_DESTRUCTION,
                                AccessStatus.DENIED);
                        check(access, DISPLAY_RIGHTS_REPORT,
                                AccessStatus.DENIED);
                        check(access, EDIT_DECLARED_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_NON_RECORD_METADATA,
                                AccessStatus.DENIED);
                        check(access, EDIT_RECORD_METADATA, AccessStatus.DENIED);
                        check(access, EDIT_SELECTION_LISTS, AccessStatus.DENIED);
                        check(access, ENABLE_DISABLE_AUDIT_BY_TYPES,
                                AccessStatus.DENIED);
                        check(access, EXPORT_AUDIT, AccessStatus.DENIED);
                        check(access, EXTEND_RETENTION_PERIOD_OR_FREEZE,
                                AccessStatus.DENIED);
                        check(access, MAKE_OPTIONAL_PARAMETERS_MANDATORY,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_CONTROLS,
                                AccessStatus.DENIED);
                        check(access, MANAGE_ACCESS_RIGHTS, AccessStatus.DENIED);
                        check(access, MANUALLY_CHANGE_DISPOSITION_DATES,
                                AccessStatus.DENIED);
                        check(access, MAP_CLASSIFICATION_GUIDE_METADATA,
                                AccessStatus.DENIED);
                        check(access, MAP_EMAIL_METADATA, AccessStatus.DENIED);
                        check(access, MOVE_RECORDS, AccessStatus.DENIED);
                        check(access, PASSWORD_CONTROL, AccessStatus.DENIED);
                        check(access, PLANNING_REVIEW_CYCLES,
                                AccessStatus.DENIED);
                        check(access, RE_OPEN_FOLDERS, AccessStatus.DENIED);
                        check(access, SELECT_AUDIT_METADATA,
                                AccessStatus.DENIED);
                        check(access, TRIGGER_AN_EVENT, AccessStatus.DENIED);
                        check(access, UNDECLARE_RECORDS, AccessStatus.DENIED);
                        check(access, UNFREEZE, AccessStatus.DENIED);
                        check(access, UPDATE_CLASSIFICATION_DATES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_EXEMPTION_CATEGORIES,
                                AccessStatus.DENIED);
                        check(access, UPDATE_TRIGGER_DATES, AccessStatus.DENIED);
                        check(access, UPDATE_VITAL_RECORD_CYCLE_INFORMATION,
                                AccessStatus.DENIED);
                        check(access, UPGRADE_DOWNGRADE_AND_DECLASSIFY_RECORDS,
                                AccessStatus.DENIED);
                        check(access, VIEW_RECORDS, AccessStatus.ALLOWED);
                        check(access, VIEW_UPDATE_REASONS_FOR_FREEZE,
                                AccessStatus.DENIED);

                        return null;
                    }
                }, false, true);
    }

    // private void setFilingOnRecord(NodeRef record, String authority)
    // {
    // NodeRef recordFolder =
    // nodeService.getPrimaryParent(record).getParentRef();
    // permissionService.setPermission(recordFolder, authority, FILING, true);
    // permissionService.setPermission(nodeService.getPrimaryParent(recordFolder).getParentRef(),
    // authority, READ_RECORDS, true);
    // }
    //    
    // private void setFilingOnRecordFolder(NodeRef recordFolder, String
    // authority)
    // {
    // permissionService.setPermission(recordFolder, authority, FILING, true);
    // permissionService.setPermission(nodeService.getPrimaryParent(recordFolder).getParentRef(),
    // authority, READ_RECORDS, true);
    // }
}
