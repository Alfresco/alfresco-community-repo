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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.impl.FreezeAction;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * @author Roy Wetherall
 */
public class ApproveRecordsScheduledForCutoffCapability extends BaseTestCapabilities
{
    public void testApproveRecordsScheduledForCutoffCapability()
    {
        // File plan permissions
        checkPermissions(filePlan, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, stdUsers, 
                AccessStatus.ALLOWED,
                AccessStatus.ALLOWED,
                AccessStatus.ALLOWED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED);
        
        // Not yet eligible
        checkCapabilities(recordFolder_1, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, stdUsers, 
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED);   
        checkCapabilities(record_1, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, stdUsers, 
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED);   
        checkCapabilities(recordFolder_2, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, stdUsers, 
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED);   
        checkCapabilities(record_2, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, stdUsers, 
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED); 

        // Set appropriate state - declare records and make eligible
        declare(record_1, record_2);
        makeEligible(recordFolder_1, record_2);

        checkCapabilities(recordFolder_1, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, stdUsers, 
                AccessStatus.ALLOWED,
                AccessStatus.ALLOWED,
                AccessStatus.ALLOWED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED);      
        checkCapabilities(record_1, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, stdUsers, 
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED);     
        checkCapabilities(recordFolder_2, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, stdUsers, 
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED);
        checkCapabilities(record_2, APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, stdUsers, 
                AccessStatus.ALLOWED,
                AccessStatus.ALLOWED,
                AccessStatus.ALLOWED,
                AccessStatus.DENIED,
                AccessStatus.DENIED,
                AccessStatus.DENIED); 
        
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.DENIED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.DENIED); // record_2
       
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);

                permissionService.setPermission(filePlan, testers, VIEW_RECORDS, true);
                permissionService.setInheritParentPermissions(recordCategory_1, false);
                permissionService.setInheritParentPermissions(recordCategory_2, false);
                permissionService.setPermission(recordCategory_1, testers, READ_RECORDS, true);
                permissionService.setPermission(recordCategory_2, testers, READ_RECORDS, true);
                permissionService.setPermission(recordFolder_1, testers, FILING, true);
                permissionService.setPermission(recordFolder_2, testers, FILING, true);

                return null;
            }
        }, false, true);

        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.DENIED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.DENIED); // record_2

        addCapability(DECLARE_RECORDS, testers, filePlan);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.DENIED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.DENIED); // record_2

        addCapability(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, testers, filePlan);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.ALLOWED); // record_2
        
        removeCapability(DECLARE_RECORDS, testers, filePlan);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.ALLOWED); // record_2
        
        addCapability(DECLARE_RECORDS, testers, filePlan);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.ALLOWED); // record_2

        removeCapability(VIEW_RECORDS, testers, filePlan);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.DENIED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.DENIED); // record_2
        
        addCapability(VIEW_RECORDS, testers, filePlan);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.ALLOWED); // record_2

        removeCapability(FILING, testers, recordFolder_1, recordFolder_2);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.DENIED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.DENIED); // record_2
        
        addCapability(FILING, testers, recordFolder_1, recordFolder_2);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.ALLOWED); // record_2

        // Freeze record folder
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        executeAction("freeze", params, recordFolder_1);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.DENIED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.ALLOWED); // record_2
        
        // Freeze record 
        executeAction("freeze", params, record_2);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.DENIED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.DENIED); // record_2
        
        // Unfreeze
        executeAction("unfreeze", recordFolder_1, record_2);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.ALLOWED); // record_2
        
        // Close folders
        executeAction("closeRecordFolder", recordFolder_1, recordFolder_2);
        checkTestUserCapabilities(APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.ALLOWED); // record_2

//
//        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
//        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "openRecordFolder");
//        recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "openRecordFolder");
//
//        checkCapability(test_user, recordFolder_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
//        checkCapability(test_user, record_1, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
//        checkCapability(test_user, recordFolder_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.DENIED);
//        checkCapability(test_user, record_2, RMPermissionModel.APPROVE_RECORDS_SCHEDULED_FOR_CUTOFF, AccessStatus.ALLOWED);
//
//        // try and cut off
//
//        AuthenticationUtil.setFullyAuthenticatedUser(test_user);
//        recordsManagementActionService.executeRecordsManagementAction(recordFolder_1, "cutoff", null);
//        try
//        {
//            recordsManagementActionService.executeRecordsManagementAction(recordFolder_2, "cutoff", null);
//            fail();
//        }
//        catch (AccessDeniedException ade)
//        {
//
//        }
//        try
//        {
//            recordsManagementActionService.executeRecordsManagementAction(record_1, "cutoff", null);
//            fail();
//        }
//        catch (AccessDeniedException ade)
//        {
//
//        }
//        recordsManagementActionService.executeRecordsManagementAction(record_2, "cutoff", null);
//
//        // check protected properties
//
//        try
//        {
//            publicNodeService.setProperty(record_1, RecordsManagementModel.PROP_CUT_OFF_DATE, new Date());
//            fail();
//        }
//        catch (AccessDeniedException ade)
//        {
//
//        }

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

}
