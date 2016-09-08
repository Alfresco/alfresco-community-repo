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
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.CompositeCapability;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Declarative capability unit test
 *
 * @author Roy Wetherall
 */
public class DeclarativeCapabilityTest extends BaseRMTestCase
{
    private NodeRef record;
    private NodeRef declaredRecord;
    private NodeRef undeclaredRecord;

    private NodeRef recordFolderContainsFrozen;
    private NodeRef frozenRecord;
    private NodeRef frozenRecord2;
    private NodeRef frozenRecordFolder;

    private NodeRef closedFolder;

    private NodeRef moveToFolder;
    private NodeRef moveToCategory;

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected void setupTestDataImpl()
    {
        super.setupTestDataImpl();

        // Pre-filed content
        record = utils.createRecord(rmFolder, "record.txt");
        declaredRecord = utils.createRecord(rmFolder, "declaredRecord.txt");
        undeclaredRecord = utils.createRecord(rmFolder, "undeclaredRecord.txt");

        // Closed folder
        closedFolder = rmService.createRecordFolder(rmContainer, "closedFolder");
        utils.closeFolder(closedFolder);

        // Frozen artifacts
        recordFolderContainsFrozen = rmService.createRecordFolder(rmContainer, "containsFrozen");
        frozenRecord = utils.createRecord(rmFolder, "frozenRecord.txt");
        frozenRecord2 = utils.createRecord(recordFolderContainsFrozen, "frozen2.txt");
        frozenRecordFolder = rmService.createRecordFolder(rmContainer, "frozenRecordFolder");

        // MoveTo artifacts
        moveToFolder = rmService.createRecordFolder(rmContainer, "moveToFolder");
        moveToCategory = filePlanService.createRecordCategory(rmContainer, "moveToCategory");
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
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

                utils.declareRecord(declaredRecord);
                utils.declareRecord(frozenRecord);
                utils.declareRecord(frozenRecord2);
                utils.freeze(frozenRecord);
                utils.freeze(frozenRecordFolder);
                utils.freeze(frozenRecord2);

                return null;
            }
        });
    }

    @Override
    protected void tearDownImpl()
    {
        // Unfreeze stuff so it can be deleted
    	utils.unfreeze(frozenRecord);
    	utils.unfreeze(frozenRecordFolder);
    	utils.unfreeze(frozenRecord2);

        super.tearDownImpl();
    }

    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        super.setupTestUsersImpl(filePlan);

        // Give all the users file permission objects
        for (String user : testUsers)
        {
            filePlanPermissionService.setPermission(rmFolder, user, RMPermissionModel.FILING);
            filePlanPermissionService.setPermission(rmContainer, user, RMPermissionModel.READ_RECORDS);
            filePlanPermissionService.setPermission(moveToFolder, user, RMPermissionModel.READ_RECORDS);
            filePlanPermissionService.setPermission(moveToCategory, user, RMPermissionModel.READ_RECORDS);
        }
    }

    public void testDeclarativeCapabilities()
    {
        Set<Capability> capabilities = capabilityService.getCapabilities();
        for (Capability capability : capabilities)
        {
            if (capability instanceof DeclarativeCapability &&
                capability instanceof CompositeCapability == false &&
                capability.isPrivate() == false &&
                capability.getName().equals("MoveRecords") == false &&
                capability.getName().equals("DeleteLinks") == false &&
                capability.getName().equals("ChangeOrDeleteReferences") == false)
            {
                testDeclarativeCapability((DeclarativeCapability)capability);
            }
        }
    }

    private void testDeclarativeCapability(final DeclarativeCapability capability)
    {
        for (String user : testUsers)
        {
            testDeclarativeCapability(capability, user, filePlan);
            testDeclarativeCapability(capability, user, rmContainer);
            testDeclarativeCapability(capability, user, rmFolder);
            testDeclarativeCapability(capability, user, record);
        }
    }

    private void testDeclarativeCapability(final DeclarativeCapability capability, final String userName, final NodeRef filePlanComponent)
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                AccessStatus accessStatus = capability.hasPermission(filePlanComponent);

                Set<Role> roles = filePlanRoleService.getRolesByUser(filePlan, userName);
                if (roles.isEmpty() == true)
                {
                    assertEquals("User " + userName + " has no RM role so we expect access to be denied for capability " + capability.getName(),
                                 AccessStatus.DENIED,
                                 accessStatus);
                }
                else
                {
                    // Do the kind check here ...
                    FilePlanComponentKind actualKind = filePlanService.getFilePlanComponentKind(filePlanComponent);
                    List<String> kinds = capability.getKinds();

                    if (kinds == null ||
                        kinds.contains(actualKind.toString()) == true)
                    {
                        Map<String, Boolean> conditions = capability.getConditions();
                        boolean conditionResult = getConditionResult(filePlanComponent, conditions);

                        assertEquals("User is expected to only have one role.", 1, roles.size());
                        Role role = new ArrayList<Role>(roles).get(0);
                        assertNotNull(role);

                        Set<Capability> roleCapabilities = role.getCapabilities();
                        if (roleCapabilities.contains(capability) == true && conditionResult == true)
                        {
                            assertEquals("User " + userName + " has the role " + role.getDisplayLabel() +
                                         " so we expect access to be allowed for capability " + capability.getName() + " on the object " +
                                         (String)nodeService.getProperty(filePlanComponent, ContentModel.PROP_NAME),
                                         AccessStatus.ALLOWED,
                                         accessStatus);
                        }
                        else
                        {
                            assertEquals("User " + userName + " has the role " + role.getDisplayLabel() + " so we expect access to be denied for capability " + capability.getName(),
                                         AccessStatus.DENIED,
                                         accessStatus);
                        }
                    }
                    else
                    {
                        // Expect fail since the kind is not expected by the capability
                        assertEquals("NodeRef is of kind" + actualKind + " so we expect access to be denied for capability " + capability.getName(),
                                AccessStatus.DENIED,
                                accessStatus);
                    }
                }

                return null;
            }
        }, userName);
    }

    private boolean getConditionResult(NodeRef nodeRef, Map<String, Boolean> conditions)
    {
        boolean result = true;

        if (conditions != null && conditions.size() != 0)
        {
            for (Map.Entry<String, Boolean> entry : conditions.entrySet())
            {
                // Get the condition bean
                CapabilityCondition condition = (CapabilityCondition)applicationContext.getBean(entry.getKey());
                assertNotNull("Invalid condition name.", condition);

                boolean actual = condition.evaluate(nodeRef);
                if (actual != entry.getValue().booleanValue())
                {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /** Specific declarative capability tests */

    public void testCreateRecordCapability()
    {
        final Capability capability = capabilityService.getCapability("CreateRecords");
        assertNotNull(capability);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.ALLOWED, capability.hasPermission(record));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(declaredRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecordFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(recordFolderContainsFrozen));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(closedFolder));

                return null;
            }
        }, recordsManagerName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(record));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(declaredRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecordFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(recordFolderContainsFrozen));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(closedFolder));

                return null;
            }
        }, rmUserName);
    }

    public void testMoveRecordCapability()
    {
        // grab the move record capability
        final Capability capability = capabilityService.getCapability("MoveRecords");
        assertNotNull(capability);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // first take a look at just the record
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.UNDETERMINED, capability.hasPermission(record));
                assertEquals(AccessStatus.UNDETERMINED, capability.hasPermission(declaredRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecordFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(recordFolderContainsFrozen));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(closedFolder));
                assertEquals(AccessStatus.UNDETERMINED, capability.hasPermission(undeclaredRecord));

                // now lets take a look when we know what the destination is
                // NOTE:  should be denied since we do not have file permission on the destination folder
                //        despite having the capability!
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, capability.evaluate(record, moveToFolder));
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, capability.evaluate(declaredRecord, moveToFolder));
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, capability.evaluate(undeclaredRecord, moveToFolder));
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, capability.evaluate(frozenRecord, moveToFolder));

                return null;
            }
        }, recordsManagerName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                for (String user : testUsers)
                {
                    filePlanPermissionService.setPermission(moveToFolder, user, RMPermissionModel.FILING);
                }
                return null;
            }
        }, rmAdminName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // first take a look at just the record
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.UNDETERMINED, capability.hasPermission(record));
                assertEquals(AccessStatus.UNDETERMINED, capability.hasPermission(declaredRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecordFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(recordFolderContainsFrozen));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(closedFolder));
                assertEquals(AccessStatus.UNDETERMINED, capability.hasPermission(undeclaredRecord));

                // now lets take a look when we know what the destination is
                // NOTE:  should be allowed now since we have filling permission on the destination folder
                assertEquals(AccessDecisionVoter.ACCESS_GRANTED, capability.evaluate(record, moveToFolder));
                assertEquals(AccessDecisionVoter.ACCESS_GRANTED, capability.evaluate(declaredRecord, moveToFolder));
                assertEquals(AccessDecisionVoter.ACCESS_GRANTED, capability.evaluate(undeclaredRecord, moveToFolder));
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, capability.evaluate(frozenRecord, moveToFolder));

                return null;
            }
        }, recordsManagerName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // first take a look at just the record
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(record));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(declaredRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecordFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(recordFolderContainsFrozen));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(closedFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(undeclaredRecord));

                // now lets take a look when we know what the destination is
                // NOTE:  should be allowed now since we have filling permission on the destination folder
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, capability.evaluate(record, moveToFolder));
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, capability.evaluate(declaredRecord, moveToFolder));
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, capability.evaluate(undeclaredRecord, moveToFolder));
                assertEquals(AccessDecisionVoter.ACCESS_DENIED, capability.evaluate(frozenRecord, moveToFolder));

                return null;
            }
        }, rmUserName);
    }

    public void testMoveRecordFolderCapability()
    {
        // grab the move record capability
        final Capability capability = capabilityService.getCapability("MoveRecordFolder");
        assertNotNull(capability);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                // first take a look at just the record
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.UNDETERMINED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(record));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(declaredRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecordFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(recordFolderContainsFrozen));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(closedFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(undeclaredRecord));

                assertEquals(AccessDecisionVoter.ACCESS_DENIED, capability.evaluate(rmFolder, moveToCategory));

                return null;
            }
        }, recordsManagerName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                for (String user : testUsers)
                {
                    filePlanPermissionService.setPermission(moveToCategory, user, RMPermissionModel.FILING);
                }
                return null;
            }
        }, rmAdminName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.UNDETERMINED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(record));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(declaredRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecordFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(recordFolderContainsFrozen));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(closedFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(undeclaredRecord));

                assertEquals(AccessDecisionVoter.ACCESS_GRANTED, capability.evaluate(rmFolder, moveToCategory));

                return null;
            }
        }, recordsManagerName);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmContainer));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(rmFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(record));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(declaredRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecordFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(recordFolderContainsFrozen));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(frozenRecord));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(closedFolder));
                assertEquals(AccessStatus.DENIED, capability.hasPermission(undeclaredRecord));

                assertEquals(AccessDecisionVoter.ACCESS_DENIED, capability.evaluate(rmFolder, moveToCategory));

                return null;
            }
        }, rmUserName);
    }
}
