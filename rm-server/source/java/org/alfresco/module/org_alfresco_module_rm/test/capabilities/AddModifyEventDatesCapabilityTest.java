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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.FreezeAction;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * @author Roy Wetherall
 */
public class AddModifyEventDatesCapabilityTest extends BaseTestCapabilities
{
    /**
     * 
     * @throws Exception
     */
    public void testAddModifyEventDatesCapability() throws Exception
    {        
        // Check file plan permissions
        checkPermissions(
                filePlan, 
                ADD_MODIFY_EVENT_DATES, 
                stdUsers, 
                new AccessStatus[]
                {
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.DENIED
                });
        
        checkCapabilities(
                recordFolder_1,
                ADD_MODIFY_EVENT_DATES,
                stdUsers, 
                new AccessStatus[]
                {
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.DENIED
                });
        
        checkCapabilities(
                record_1,
                ADD_MODIFY_EVENT_DATES,
                stdUsers, 
                new AccessStatus[]
                {
                        AccessStatus.DENIED,
                        AccessStatus.DENIED,
                        AccessStatus.DENIED,
                        AccessStatus.DENIED,
                        AccessStatus.DENIED,
                        AccessStatus.DENIED
                });
        
        checkCapabilities(
                recordFolder_2,
                ADD_MODIFY_EVENT_DATES,
                stdUsers, 
                new AccessStatus[]
                {
                        AccessStatus.DENIED,
                        AccessStatus.DENIED,
                        AccessStatus.DENIED,
                        AccessStatus.DENIED,
                        AccessStatus.DENIED,
                        AccessStatus.DENIED
                });

        checkCapabilities(
                record_2,
                ADD_MODIFY_EVENT_DATES,
                stdUsers, 
                new AccessStatus[]
                {
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.ALLOWED,
                        AccessStatus.DENIED
                });

        /** Test user has no capabilities */
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                                  AccessStatus.DENIED,  // recordFolder_1 
                                  AccessStatus.DENIED,  // record_1
                                  AccessStatus.DENIED,  // recordFolder_2
                                  AccessStatus.DENIED); // record_2
        
        /** Add filing to both record folders */
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

        /** Check capabilities */
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                  AccessStatus.DENIED,  // recordFolder_1 
                  AccessStatus.DENIED,  // record_1
                  AccessStatus.DENIED,  // recordFolder_2
                  AccessStatus.DENIED); // record_2

        /** Add declare record capability */
        addCapability(DECLARE_RECORDS, testers, filePlan);
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.DENIED,  // recordFolder_1 
                AccessStatus.DENIED,  // record_1
                AccessStatus.DENIED,  // recordFolder_2
                AccessStatus.DENIED); // record_2

        /** Add modify event date capability */
        addCapability(ADD_MODIFY_EVENT_DATES, testers, filePlan);        
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.ALLOWED); // record_2

        /** Remove declare capability */
        removeCapability(DECLARE_RECORDS, testers, filePlan);         
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.ALLOWED); // record_2
        
        /** Add declare capability */
        addCapability(DECLARE_RECORDS, testers, filePlan);        
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.ALLOWED); // record_2
        
        /** Remove view records capability */
        removeCapability(VIEW_RECORDS, testers, filePlan);         
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.DENIED,   // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.DENIED);  // record_2
           
        /** Add view records capability */
        addCapability(VIEW_RECORDS, testers, filePlan);         
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.ALLOWED); // record_2
        
        /** Remove filing from record folders */
        removeCapability(FILING, testers, recordFolder_1, recordFolder_2);        
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.DENIED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.DENIED); // record_2        

        /** Set filing permission on records folders */
        addCapability(FILING, testers, recordFolder_1, recordFolder_2);        
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.ALLOWED); // record_2    
        
        /** Freeze folder 1 */
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "one");
        executeAction("freeze", params, recordFolder_1);        
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.DENIED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.ALLOWED); // record_2            
        
        /** Freeze record_2 */
        params = new HashMap<String, Serializable>(1);
        params.put(FreezeAction.PARAM_REASON, "Two");
        executeAction("freeze", params, record_2);        
        
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.DENIED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.DENIED); // record_2    
        
        /** Unfreeze */
        executeAction("unfreeze", recordFolder_1, record_2);        
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.ALLOWED); // record_2    

        /** Close record folders */
        executeAction("closeRecordFolder", recordFolder_1, recordFolder_2);        
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.ALLOWED); // record_2
        
        /** Open record folders */
        executeAction("openRecordFolder", recordFolder_1, recordFolder_2);       
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.ALLOWED); // record_2    
        
        /** Try and complete events*/        
        Map<String, Serializable> eventDetails = new HashMap<String, Serializable>(3);
        eventDetails.put(CompleteEventAction.PARAM_EVENT_NAME, "event");
        eventDetails.put(CompleteEventAction.PARAM_EVENT_COMPLETED_AT, new Date());
        eventDetails.put(CompleteEventAction.PARAM_EVENT_COMPLETED_BY, test_user);        
        executeAction("completeEvent", eventDetails, test_user, recordFolder_1);
        checkExecuteActionFail("completeEvent", eventDetails, test_user, recordFolder_2);
        checkExecuteActionFail("completeEvent", eventDetails, test_user, record_1);
        executeAction("completeEvent", eventDetails, test_user, record_2);

        /** Check properties can not be set */
        checkSetPropertyFail(record_1, RecordsManagementModel.PROP_EVENT_EXECUTION_COMPLETE, test_user, true);
        checkSetPropertyFail(record_1, RecordsManagementModel.PROP_EVENT_EXECUTION_COMPLETED_AT, test_user, new Date());
        checkSetPropertyFail(record_1, RecordsManagementModel.PROP_EVENT_EXECUTION_COMPLETED_AT, test_user, "me");

        /** Declare and cutoff */
        declare(record_1, record_2);
        cutoff(recordFolder_1, record_2);
        checkTestUserCapabilities(ADD_MODIFY_EVENT_DATES, 
                AccessStatus.ALLOWED,  // recordFolder_1 
                AccessStatus.DENIED,   // record_1
                AccessStatus.DENIED,   // recordFolder_2
                AccessStatus.ALLOWED); // record_2 
    }
}
