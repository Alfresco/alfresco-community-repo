/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.issue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;


/**
 * System test for RM-1008
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class RM1008Test extends BaseRMTestCase
{
    private String myUser;

    @Override
    protected void initServices()
    {
        super.initServices();
    }

    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        super.setupTestUsersImpl(filePlan);

        myUser = GUID.generate();
        createPerson(myUser);
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_USER, myUser);
    }

    public void testContainers() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                NodeRef holdContainer = filePlanService.getHoldContainer(filePlan);
                assertNotNull(holdContainer);
                NodeRef transferContainer = filePlanService.getTransferContainer(filePlan);
                assertNotNull(transferContainer);

                Capability viewRecords = capabilityService.getCapability("ViewRecords");
                assertNotNull(viewRecords);

                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(holdContainer));
                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(transferContainer));

                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(holdContainer, RMPermissionModel.FILING));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(transferContainer, RMPermissionModel.FILING));

                return null;
            }
        }, rmAdminName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                NodeRef holdContainer = filePlanService.getHoldContainer(filePlan);
                assertNotNull(holdContainer);
                NodeRef transferContainer = filePlanService.getTransferContainer(filePlan);
                assertNotNull(transferContainer);

                Capability viewRecords = capabilityService.getCapability("ViewRecords");
                assertNotNull(viewRecords);

                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(holdContainer));
                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(transferContainer));

                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(holdContainer, RMPermissionModel.FILING));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(transferContainer, RMPermissionModel.FILING));

                return null;
            }
        }, myUser);
    }

    public void testHold()
    {
        final NodeRef hold = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                // create hold object
                freezeService.freeze("test", rmFolder);
                Set<NodeRef> holds = freezeService.getHolds(filePlan);
                return holds.iterator().next();
            }
        }, rmAdminName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Capability viewRecords = capabilityService.getCapability("ViewRecords");
                assertNotNull(viewRecords);

                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(hold));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(hold, RMPermissionModel.FILING));

                return null;
            }
        }, rmAdminName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Capability viewRecords = capabilityService.getCapability("ViewRecords");
                assertNotNull(viewRecords);

                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(hold));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(hold, RMPermissionModel.FILING));

                return null;
            }
        }, myUser);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                filePlanPermissionService.setPermission(filePlan, myUser, FILING);

                return null;
            }
        }, rmAdminName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Capability viewRecords = capabilityService.getCapability("ViewRecords");
                assertNotNull(viewRecords);

                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(hold));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(hold, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(hold, RMPermissionModel.FILING));

                return null;
            }
        }, myUser);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                filePlanPermissionService.deletePermission(filePlan, myUser, FILING);

                return null;
            }
        }, rmAdminName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Capability viewRecords = capabilityService.getCapability("ViewRecords");
                assertNotNull(viewRecords);

                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(hold));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(hold, RMPermissionModel.FILING));

                return null;
            }
        }, myUser);
    }

    public void testTransfer()
    {
        final NodeRef transferFolder = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                NodeRef transferCat = filePlanService.createRecordCategory(filePlan, "transferCat");

                Map<QName, Serializable> dsProps = new HashMap<QName, Serializable>(3);
                dsProps.put(PROP_DISPOSITION_AUTHORITY, "test");
                dsProps.put(PROP_DISPOSITION_INSTRUCTIONS, "test");
                dsProps.put(PROP_RECORD_LEVEL_DISPOSITION, false);
                DispositionSchedule dispositionSchedule = dispositionService.createDispositionSchedule(transferCat, dsProps);

                Map<QName, Serializable> adParams = new HashMap<QName, Serializable>(3);
                adParams.put(PROP_DISPOSITION_ACTION_NAME, "cutoff");
                adParams.put(PROP_DISPOSITION_DESCRIPTION, "test");
                adParams.put(PROP_DISPOSITION_PERIOD, "immediately|0");

                dispositionService.addDispositionActionDefinition(dispositionSchedule, adParams);

                adParams = new HashMap<QName, Serializable>(3);
                adParams.put(PROP_DISPOSITION_ACTION_NAME, "transfer");
                adParams.put(PROP_DISPOSITION_DESCRIPTION, "test");
                adParams.put(PROP_DISPOSITION_PERIOD, "immediately|0");

                dispositionService.addDispositionActionDefinition(dispositionSchedule, adParams);

                return rmService.createRecordFolder(transferCat, "transferFolder");
            }
        });

        final NodeRef transfer = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {
                actionService.executeRecordsManagementAction(transferFolder, "cutoff");
                actionService.executeRecordsManagementAction(transferFolder, "transfer");

                NodeRef transferContainer = filePlanService.getTransferContainer(filePlan);
                List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(transferContainer, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
                return childAssocs.get(0).getChildRef();
            }

            @Override
            public void test(NodeRef result) throws Exception
            {
                assertNotNull(result);
                assertEquals(TYPE_TRANSFER, nodeService.getType(result));
            }
        });

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Capability viewRecords = capabilityService.getCapability("ViewRecords");
                assertNotNull(viewRecords);

                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(transfer));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(transfer, RMPermissionModel.FILING));

                return null;
            }
        }, rmAdminName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Capability viewRecords = capabilityService.getCapability("ViewRecords");
                assertNotNull(viewRecords);

                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(transfer));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(transfer, RMPermissionModel.FILING));

                return null;
            }
        }, myUser);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                filePlanPermissionService.setPermission(filePlan, myUser, FILING);

                return null;
            }
        }, rmAdminName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Capability viewRecords = capabilityService.getCapability("ViewRecords");
                assertNotNull(viewRecords);

                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(transfer));
                assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(transfer, RMPermissionModel.READ_RECORDS));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(transfer, RMPermissionModel.FILING));

                return null;
            }
        }, myUser);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                filePlanPermissionService.deletePermission(filePlan, myUser, FILING);

                return null;
            }
        }, rmAdminName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Capability viewRecords = capabilityService.getCapability("ViewRecords");
                assertNotNull(viewRecords);

                assertEquals(AccessStatus.ALLOWED, viewRecords.hasPermission(transfer));
                assertEquals(AccessStatus.DENIED, permissionService.hasPermission(transfer, RMPermissionModel.FILING));

                return null;
            }
        }, myUser);


    }

}
