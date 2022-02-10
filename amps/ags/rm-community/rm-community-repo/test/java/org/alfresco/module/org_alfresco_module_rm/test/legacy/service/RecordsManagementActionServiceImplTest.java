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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.BeforeRMActionExecution;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnRMActionExecution;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestAction2;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Records management action service implementation test
 *
 * @author Roy Wetherall
 */
public class RecordsManagementActionServiceImplTest extends BaseRMTestCase
                                                    implements BeforeRMActionExecution,
                                                               OnRMActionExecution
{
    private RetryingTransactionHelper txnHelper;

    private NodeRef nodeRef;
    private List<NodeRef> nodeRefs;

    private boolean beforeMarker;
    private boolean onMarker;
    private boolean inTest;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.txnHelper = transactionService.getRetryingTransactionHelper();

        // Set the current security context as system
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

        RetryingTransactionCallback<Void> setUpCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Create a node we can use for the tests
                NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
                nodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "temp.txt"),
                        ContentModel.TYPE_CONTENT).getChildRef();

                // Create nodeRef list
                nodeRefs = new ArrayList<>(5);
                for (int i = 0; i < 5; i++)
                {
                    nodeRefs.add(
                            nodeService.createNode(
                                    rootNodeRef,
                                    ContentModel.ASSOC_CHILDREN,
                                    QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "temp.txt"),
                                    ContentModel.TYPE_CONTENT).getChildRef());
                }
                return null;
            }
        };
        txnHelper.doInTransaction(setUpCallback);

        beforeMarker = false;
        onMarker = false;
        inTest = false;
    }

    @Override
    protected void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    public void testGetActions()
    {
        RetryingTransactionCallback<Void> testCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                getActionsImpl();
                return null;
            }
        };
        txnHelper.doInTransaction(testCallback);
    }

    private void getActionsImpl()
    {
        List<RecordsManagementAction> result = this.rmActionService.getRecordsManagementActions();
        assertNotNull(result);
        Map<String, RecordsManagementAction> resultMap = new HashMap<>(8);
        for (RecordsManagementAction action : result)
        {
            resultMap.put(action.getName(), action);
        }

        assertTrue(resultMap.containsKey(TestAction.NAME));
        assertTrue(resultMap.containsKey(TestAction2.NAME));

        result = this.rmActionService.getDispositionActions();
        resultMap = new HashMap<>(8);
        for (RecordsManagementAction action : result)
        {
            resultMap.put(action.getName(), action);
        }
        assertTrue(resultMap.containsKey(TestAction.NAME));
        assertFalse(resultMap.containsKey(TestAction2.NAME));

        // get some specific actions and check the label
        RecordsManagementAction cutoff = this.rmActionService.getDispositionAction("cutoff");
        assertNotNull(cutoff);
        assertEquals("Cut Off", cutoff.getLabel());

        RecordsManagementAction freeze = this.rmActionService.getRecordsManagementAction("freeze");
        assertNotNull(freeze);
        assertEquals("Freeze", freeze.getLabel());
        assertEquals("Freeze", freeze.getLabel());

        // test non-existent actions
        assertNull(this.rmActionService.getDispositionAction("notThere"));
        assertNull(this.rmActionService.getRecordsManagementAction("notThere"));
    }

    public void testExecution()
    {
        RetryingTransactionCallback<Void> testCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                executionImpl();
                return null;
            }
        };
        txnHelper.doInTransaction(testCallback);
    }

    public void beforeRMActionExecution(NodeRef nodeRef, String name, Map<String, Serializable> parameters)
    {
        if (inTest)
        {
            assertEquals(this.nodeRef, nodeRef);
            assertEquals(TestAction.NAME, name);
            assertEquals(1, parameters.size());
            assertTrue(parameters.containsKey(TestAction.PARAM));
            assertEquals(TestAction.PARAM_VALUE, parameters.get(TestAction.PARAM));
            beforeMarker = true;
        }
    }

    public void onRMActionExecution(NodeRef nodeRef, String name, Map<String, Serializable> parameters)
    {
        if (inTest)
        {
            assertEquals(this.nodeRef, nodeRef);
            assertEquals(TestAction.NAME, name);
            assertEquals(1, parameters.size());
            assertTrue(parameters.containsKey(TestAction.PARAM));
            assertEquals(TestAction.PARAM_VALUE, parameters.get(TestAction.PARAM));
            onMarker = true;
        }
    }

    private void executionImpl()
    {
        inTest = true;
        try
        {
            policyComponent.bindClassBehaviour(
                    RecordsManagementPolicies.BEFORE_RM_ACTION_EXECUTION,
                    this,
                    new JavaBehaviour(this, "beforeRMActionExecution", NotificationFrequency.EVERY_EVENT));
            policyComponent.bindClassBehaviour(
                    RecordsManagementPolicies.ON_RM_ACTION_EXECUTION,
                    this,
                    new JavaBehaviour(this, "onRMActionExecution", NotificationFrequency.EVERY_EVENT));

            assertFalse(beforeMarker);
            assertFalse(onMarker);
            assertFalse(this.nodeService.hasAspect(this.nodeRef, ASPECT_RECORD));

            Map<String, Serializable> params = new HashMap<>(1);
            params.put(TestAction.PARAM, TestAction.PARAM_VALUE);
            this.rmActionService.executeRecordsManagementAction(this.nodeRef, TestAction.NAME, params);

            assertTrue(beforeMarker);
            assertTrue(onMarker);
            assertTrue(this.nodeService.hasAspect(this.nodeRef, ASPECT_RECORD));
        }
        finally
        {
            inTest = false;
        }
    }

    public void testBulkExecution()
    {
        RetryingTransactionCallback<Void> testCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                bulkExecutionImpl();
                return null;
            }
        };
        txnHelper.doInTransaction(testCallback);
    }

    private void bulkExecutionImpl()
    {
        for (NodeRef nodeRef : this.nodeRefs)
        {
            assertFalse(this.nodeService.hasAspect(nodeRef, ASPECT_RECORD));
        }

        Map<String, Serializable> params = new HashMap<>(1);
        params.put(TestAction.PARAM, TestAction.PARAM_VALUE);
        this.rmActionService.executeRecordsManagementAction(this.nodeRefs, TestAction.NAME, params);

        for (NodeRef nodeRef : this.nodeRefs)
        {
            assertTrue(this.nodeService.hasAspect(nodeRef, ASPECT_RECORD));
        }
    }
}
