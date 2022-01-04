/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.system;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.notification.RecordsManagementNotificationHelper;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;

/**
 * Notification helper (system) test
 *
 * @author Roy Wetherall
 */
public class NotificationServiceHelperSystemTest extends BaseRMTestCase
{
    private static final String NOTIFICATION_ROLE = "RecordsManager";
    private static final String EMAIL_ADDRESS = "roy.wetherall@alfreso.com";

    /** Services */
    private RecordsManagementNotificationHelper notificationHelper;

    /** Test data */
    private NodeRef record;
    private List<NodeRef> records;
    private String userName;
    private NodeRef person;

    @Override
    protected void initServices()
    {
        super.initServices();

        // Get the notification helper
        notificationHelper = (RecordsManagementNotificationHelper)applicationContext.getBean("recordsManagementNotificationHelper");
    }

    @Override
    protected void setupTestData()
    {
        super.setupTestData();

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

                // Create a user
                userName = GUID.generate();
                authenticationService.createAuthentication(userName, "".toCharArray());
                PropertyMap props = new PropertyMap();
                props.put(PROP_USERNAME, userName);
                props.put(PROP_FIRSTNAME, "Test");
                props.put(PROP_LASTNAME, "User");
                props.put(PROP_EMAIL, EMAIL_ADDRESS);
                person = personService.createPerson(props);

                // Find the authority for the given role
                Role role = filePlanRoleService.getRole(filePlan, NOTIFICATION_ROLE);
                assertNotNull("Notification role could not be retrieved", role);
                String roleGroup = role.getRoleGroupName();
                assertNotNull("Notification role group can not be null.", roleGroup);

                // Add user to notification role group
                authorityService.addAuthority(roleGroup, userName);

                return null;
            }
        });
    }

    @Override
    protected void setupTestDataImpl()
    {
        super.setupTestDataImpl();

        // Create a few test records
        record = utils.createRecord(rmFolder, "recordOne");
        NodeRef record2 = utils.createRecord(rmFolder, "recordTwo");
        NodeRef record3 = utils.createRecord(rmFolder, "recordThree");

        records = new ArrayList<>(3);
        records.add(record);
        records.add(record2);
        records.add(record3);
    }

    @Override
    protected void tearDownImpl()
    {
        super.tearDownImpl();

        // Delete the person and user
        personService.deletePerson(person);
    }

    public void testSendDueForReviewNotification()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                notificationHelper.recordsDueForReviewEmailNotification(records);
                return null;
            }
        });
    }

    public void testSendSupersededNotification()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                notificationHelper.recordSupersededEmailNotification(record);
                return null;
            }
        });
    }
}
