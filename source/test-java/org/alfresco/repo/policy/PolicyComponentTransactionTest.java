/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.Policy.Arg;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;


/**
 * Test Transaction-level Policies
 */
@Category(OwnJVMTestsCategory.class)
public class PolicyComponentTransactionTest extends TestCase
{
    private static final String TEST_MODEL = "org/alfresco/repo/policy/policycomponenttest_model.xml";
    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/policycomponenttest/1.0";
    private static QName BASE_TYPE = QName.createQName(TEST_NAMESPACE, "base");
    private static QName FILE_TYPE = QName.createQName(TEST_NAMESPACE, "file");

    private static QName A_TYPE = QName.createQName(TEST_NAMESPACE, "a_type");
    private static QName B_TYPE = QName.createQName(TEST_NAMESPACE, "b_type");
    private static QName C_TYPE = QName.createQName(TEST_NAMESPACE, "c_type");

    private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
    private static ClassPolicyDelegate<SideEffectTestPolicy> sideEffectDelegate = null;
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;
    private TransactionService trxService;
    private AuthenticationComponent authenticationComponent;
    private NodeService nodeService;
    private NodeLocatorService nodeLocatorService;
    private NodeRef companyHome;

    private TestOnCreateNodePolicy aTypeBehavior;
    private TestOnCreateNodePolicy bTypeBehavior;
    private TestOnCreateNodePolicy cTypeBehavior;

    
    @Override
    protected void setUp() throws Exception
    {
        // initialise policy test model
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add(TEST_MODEL);
        bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO((DictionaryDAO)applicationContext.getBean("dictionaryDAO"));
        bootstrap.setTenantService((TenantService)applicationContext.getBean("tenantService"));
        bootstrap.bootstrap();
        
        // retrieve policy component
        this.policyComponent = (PolicyComponent)applicationContext.getBean("policyComponent");
        this.behaviourFilter = (BehaviourFilter) applicationContext.getBean("policyBehaviourFilter");
        this.trxService = (TransactionService) applicationContext.getBean("transactionComponent");
        this.nodeService = (NodeService) applicationContext.getBean("nodeService");
        this.nodeLocatorService = (NodeLocatorService) applicationContext.getBean("nodeLocatorService");
        this.authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // Register Policy
        if (sideEffectDelegate == null)
        {
	        sideEffectDelegate = policyComponent.registerClassPolicy(SideEffectTestPolicy.class);
	
	        // Bind Behaviour to side effect policy
	        QName policyName = QName.createQName(TEST_NAMESPACE, "sideEffect");
	        Behaviour baseBehaviour = new JavaBehaviour(this, "sideEffectTest", NotificationFrequency.TRANSACTION_COMMIT);
	        this.policyComponent.bindClassBehaviour(policyName, BASE_TYPE, baseBehaviour);
        }

        this.companyHome = nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);
        createAndEnableBehaviours();
    }

    private void createAndEnableBehaviours()
    {
        aTypeBehavior = new TestOnCreateNodePolicy();
        bTypeBehavior = new TestOnCreateNodePolicy();
        cTypeBehavior = new TestOnCreateNodePolicy();

        // bind custom behavior for super type
        policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, A_TYPE, new JavaBehaviour(aTypeBehavior, "onCreateNode"));
        // bind custom behavior for "middle" type
        policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, B_TYPE, new JavaBehaviour(bTypeBehavior, "onCreateNode"));
        // bind custom behavior for sub type
        policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, C_TYPE, new JavaBehaviour(cTypeBehavior, "onCreateNode"));
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
    }
    
        
    public void testStartTransactionPolicy()
        throws Exception
    {
        ClassPolicyDelegate<StartTestPolicy> startDelegate = policyComponent.registerClassPolicy(StartTestPolicy.class);

        // Register Policy
        QName policyName = QName.createQName(TEST_NAMESPACE, "start");
        PolicyDefinition<Policy> definition = policyComponent.getRegisteredPolicy(PolicyType.Class, policyName);
        assertNotNull(definition);
        Arg arg0 = definition.getArgument(0);
        assertEquals(Arg.KEY, arg0);
        Arg arg1 = definition.getArgument(1);
        assertEquals(Arg.KEY, arg1);
        Arg arg2 = definition.getArgument(2);
        assertEquals(Arg.START_VALUE, arg2);
        Arg arg3 = definition.getArgument(3);
        assertEquals(Arg.END_VALUE, arg3);
        
        // Bind Behaviour
        Behaviour baseBehaviour = new JavaBehaviour(this, "startTransactionTest", NotificationFrequency.FIRST_EVENT);
        policyComponent.bindClassBehaviour(policyName, BASE_TYPE, baseBehaviour);
        
        // Invoke Behaviour
        UserTransaction userTransaction1 = trxService.getUserTransaction();
        try
        {
            userTransaction1.begin();
            
            List<TestResult> results = new ArrayList<TestResult>();
            
            StartTestPolicy basePolicy = startDelegate.get(BASE_TYPE);
            String baseResult1 = basePolicy.start("1", "2", "value1a", "value2a", false, results);
            TestResult result1 = new TestResult("startTransactionTest", "1", "2", "value1a", "value2a");
            assertEquals(result1.toString(), baseResult1);
            assertEquals(1, results.size());
            assertEquals(result1, results.get(0));
            String baseResult2 = basePolicy.start("2", "1", "value1b", "value2b", false, results);
            TestResult result2 = new TestResult("startTransactionTest", "2", "1", "value1b", "value2b");
            assertEquals(result2.toString(), baseResult2);
            assertEquals(2, results.size());
            assertEquals(result2, results.get(1));
            String baseResult3 = basePolicy.start("1", "2", "value1c", "value2c", false, results);
            assertEquals(result1.toString(), baseResult3);
            assertEquals(2, results.size());
            
            userTransaction1.commit();

            assertEquals(2, results.size());
            assertEquals(result1, results.get(0));
            assertEquals(result2, results.get(1));
        }
        catch(Exception e)
        {
            try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }        

        // Invoke Behaviour
        UserTransaction userTransaction2 = trxService.getUserTransaction();
        try
        {
            userTransaction2.begin();
            
            List<TestResult> results = new ArrayList<TestResult>();
            
            StartTestPolicy basePolicy = startDelegate.get(BASE_TYPE);
            String baseResult1 = basePolicy.start("1", "2", "value1a", "value2a", true, results);
            TestResult result1 = new TestResult("startTransactionTest", "1", "2", "value1a", "value2a");
            assertEquals(result1.toString(), baseResult1);
            assertEquals(1, results.size());
            assertEquals(result1, results.get(0));
            String baseResult2 = basePolicy.start("2", "1", "value1b", "value2b", true, results);
            TestResult result2 = new TestResult("startTransactionTest", "2", "1", "value1b", "value2b");
            assertEquals(result2.toString(), baseResult2);
            assertEquals(2, results.size());
            assertEquals(result2, results.get(1));
            String baseResult3 = basePolicy.start("1", "2", "value1c", "value2c", true, results);
            assertEquals(result1.toString(), baseResult3);
            assertEquals(2, results.size());
            
            TestResult result3 = new TestResult("sideEffectTest", "1", "2", "value1a", "value2a");
            TestResult result4 = new TestResult("sideEffectTest", "2", "1", "value1b", "value2b");

            userTransaction2.commit();

            assertEquals(4, results.size());
            assertEquals(result1, results.get(0));
            assertEquals(result2, results.get(1));
            assertEquals(result3, results.get(2));
            assertEquals(result4, results.get(3));
        }
        catch(Exception e)
        {
            try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }        
    }


    public void testEndTransactionPolicy()
        throws Exception
    {
        ClassPolicyDelegate<EndTestPolicy> endDelegate = policyComponent.registerClassPolicy(EndTestPolicy.class);

        QName policyName = QName.createQName(TEST_NAMESPACE, "end");
        PolicyDefinition<Policy> definition = policyComponent.getRegisteredPolicy(PolicyType.Class, policyName);
        assertNotNull(definition);
        Arg arg0 = definition.getArgument(0);
        assertEquals(Arg.KEY, arg0);
        Arg arg1 = definition.getArgument(1);
        assertEquals(Arg.KEY, arg1);
        Arg arg2 = definition.getArgument(2);
        assertEquals(Arg.START_VALUE, arg2);
        Arg arg3 = definition.getArgument(3);
        assertEquals(Arg.END_VALUE, arg3);
        
        // Bind Behaviour
        Behaviour baseBehaviour = new JavaBehaviour(this, "endTransactionTest", NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindClassBehaviour(policyName, BASE_TYPE, baseBehaviour);
        
        UserTransaction userTransaction1 = trxService.getUserTransaction();
        try
        {
            userTransaction1.begin();
            
            List<TestResult> results = new ArrayList<TestResult>();
                    
            // Invoke Behaviour
            EndTestPolicy basePolicy = endDelegate.get(BASE_TYPE);
            String baseResult1 = basePolicy.end("1", "2", "value1a", "value2a", false, results);
            assertEquals(null, baseResult1);
            assertEquals(0, results.size());
            String baseResult2 = basePolicy.end("2", "1", "value1b", "value2b", false, results);
            assertEquals(null, baseResult2);
            assertEquals(0, results.size());
            String baseResult3 = basePolicy.end("1", "2", "value1a", "value2c", false, results);
            assertEquals(null, baseResult3);
            assertEquals(0, results.size());
            
            TestResult result1 = new TestResult("endTransactionTest", "1", "2", "value1a", "value2c");
            TestResult result2 = new TestResult("endTransactionTest", "2", "1", "value1b", "value2b");
            
            userTransaction1.commit();

            assertEquals(2, results.size());
            assertEquals(result1, results.get(0));
            assertEquals(result2, results.get(1));
        }
        catch(Exception e)
        {
            try { userTransaction1.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }        

        UserTransaction userTransaction2 = trxService.getUserTransaction();
        try
        {
            userTransaction2.begin();
            
            List<TestResult> results = new ArrayList<TestResult>();
                    
            // Invoke Behaviour
            EndTestPolicy basePolicy = endDelegate.get(BASE_TYPE);
            String baseResult1 = basePolicy.end("1", "2", "value1a", "value2a", true, results);
            assertEquals(null, baseResult1);
            assertEquals(0, results.size());
            String baseResult2 = basePolicy.end("2", "1", "value1b", "value2b", true, results);
            assertEquals(null, baseResult2);
            assertEquals(0, results.size());
            String baseResult3 = basePolicy.end("1", "2", "value1a", "value2c", true, results);
            assertEquals(null, baseResult3);
            assertEquals(0, results.size());
            
            TestResult result1 = new TestResult("endTransactionTest", "1", "2", "value1a", "value2c");
            TestResult result2 = new TestResult("endTransactionTest", "2", "1", "value1b", "value2b");
            TestResult result3 = new TestResult("sideEffectTest", "1", "2", "value1a", "value2c");
            TestResult result4 = new TestResult("sideEffectTest", "2", "1", "value1b", "value2b");
            
            userTransaction2.commit();

            assertEquals(4, results.size());
            assertEquals(result1, results.get(0));
            assertEquals(result2, results.get(1));
            assertEquals(result3, results.get(2));
            assertEquals(result4, results.get(3));
            
        }
        catch(Exception e)
        {
            try { userTransaction2.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }
        
        NodeRef nodeRef = new NodeRef(TEST_NAMESPACE, "test", "123");
        // Check enabling and disabling of behaviours within the transaction
        // We disable multiple times and enable, including enabling when not disabled
        UserTransaction userTransaction3 = trxService.getUserTransaction();
        try
        {
            userTransaction3.begin();

            // Check all enabled to start
            checkBehaviour(BASE_TYPE, nodeRef, true, true, true, true);
            
            // Check instance with nesting
            behaviourFilter.enableBehaviour(nodeRef);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, true, true);
            behaviourFilter.disableBehaviour(nodeRef);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, false, false);
            behaviourFilter.disableBehaviour(nodeRef);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, false, false);
            behaviourFilter.enableBehaviour(nodeRef);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, false, false);
            behaviourFilter.enableBehaviour(nodeRef);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, true, true);
            
            // Check class and instance with nesting
            behaviourFilter.enableBehaviour(nodeRef, BASE_TYPE);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, true, true);
            behaviourFilter.disableBehaviour(nodeRef, BASE_TYPE);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, false, true);
            behaviourFilter.disableBehaviour(nodeRef, BASE_TYPE);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, false, true);
            behaviourFilter.enableBehaviour(nodeRef, BASE_TYPE);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, false, true);
            behaviourFilter.enableBehaviour(nodeRef, BASE_TYPE);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, true, true);
            
            // Check class with nesting
            behaviourFilter.enableBehaviour(BASE_TYPE);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, true, true);
            behaviourFilter.disableBehaviour(BASE_TYPE);
            checkBehaviour(BASE_TYPE, nodeRef, true, false, false, true);
            behaviourFilter.disableBehaviour(BASE_TYPE);
            checkBehaviour(BASE_TYPE, nodeRef, true, false, false, true);
            behaviourFilter.enableBehaviour(BASE_TYPE);
            checkBehaviour(BASE_TYPE, nodeRef, true, false, false, true);
            behaviourFilter.enableBehaviour(BASE_TYPE);
            checkBehaviour(BASE_TYPE, nodeRef, true, true, true, true);
            
            // Check global with nesting
            behaviourFilter.enableBehaviour();
            checkBehaviour(BASE_TYPE, nodeRef, true, true, true, true);
            behaviourFilter.disableBehaviour();
            checkBehaviour(BASE_TYPE, nodeRef, false, false, false, false);
            behaviourFilter.disableBehaviour();
            checkBehaviour(BASE_TYPE, nodeRef, false, false, false, false);
            behaviourFilter.enableBehaviour();
            checkBehaviour(BASE_TYPE, nodeRef, false, false, false, false);
            behaviourFilter.enableBehaviour();
            checkBehaviour(BASE_TYPE, nodeRef, true, true, true, true);
            
            userTransaction3.commit();
        }
        catch(Exception e)
        {
            try { userTransaction3.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }        
    }

    public void behaviourHierarchyTestWork(QName createDocType, ClassFilter... disableTypes) throws Exception
    {
        UserTransaction transaction = trxService.getUserTransaction();
        try
        {
            transaction.begin();
            disableBehaviours(disableTypes);
            try
            {
                createDocOfType(createDocType);
            }
            finally
            {
                enableBehaviours(disableTypes);
            }
            transaction.commit();
        }
        catch(Exception e)
        {
            try { transaction.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyEnableAll1() throws Exception
    {
        behaviourHierarchyTestWork(A_TYPE);
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyEnableAll2() throws Exception
    {
        behaviourHierarchyTestWork(B_TYPE);
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(1, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyEnableAll3() throws Exception
    {
        behaviourHierarchyTestWork(C_TYPE);
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(1, bTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(1, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableAllUpHierarchy1() throws Exception
    {
        behaviourHierarchyTestWork(
                A_TYPE,
                new ClassFilter(A_TYPE, false),
                new ClassFilter(B_TYPE, false),
                new ClassFilter(C_TYPE, false)
        );
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableAllUpHierarchy2() throws Exception
    {
        behaviourHierarchyTestWork(
                B_TYPE,
                new ClassFilter(A_TYPE, false),
                new ClassFilter(B_TYPE, false),
                new ClassFilter(C_TYPE, false)
        );
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableAllUpHierarchy3() throws Exception
    {
        behaviourHierarchyTestWork(
                C_TYPE,
                new ClassFilter(A_TYPE, false),
                new ClassFilter(B_TYPE, false),
                new ClassFilter(C_TYPE, false)
        );
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableAllDownHierarchy1() throws Exception
    {
        behaviourHierarchyTestWork(
                A_TYPE,
                new ClassFilter(A_TYPE, true),
                new ClassFilter(B_TYPE, true),
                new ClassFilter(C_TYPE, true)
        );
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableAllDownHierarchy2() throws Exception
    {
        behaviourHierarchyTestWork(
                B_TYPE,
                new ClassFilter(A_TYPE, true),
                new ClassFilter(B_TYPE, true),
                new ClassFilter(C_TYPE, true)
        );
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableAllDownHierarchy3() throws Exception
    {
        behaviourHierarchyTestWork(
                C_TYPE,
                new ClassFilter(A_TYPE, true),
                new ClassFilter(B_TYPE, true),
                new ClassFilter(C_TYPE, true)
        );
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSuper1() throws Exception
    {
        behaviourHierarchyTestWork(A_TYPE, new ClassFilter(A_TYPE, false));
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSuper2() throws Exception
    {
        behaviourHierarchyTestWork(B_TYPE, new ClassFilter(A_TYPE, false));
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(1, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSuper3() throws Exception
    {
        behaviourHierarchyTestWork(C_TYPE, new ClassFilter(A_TYPE, false));
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(1, bTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(1, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSuper4() throws Exception
    {
        behaviourHierarchyTestWork(A_TYPE, new ClassFilter(A_TYPE, true));
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSuper5() throws Exception
    {
        behaviourHierarchyTestWork(B_TYPE, new ClassFilter(A_TYPE, true));
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSuper6() throws Exception
    {
        behaviourHierarchyTestWork(C_TYPE, new ClassFilter(A_TYPE, true));
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableMiddle1() throws Exception
    {
        behaviourHierarchyTestWork(A_TYPE, new ClassFilter(B_TYPE, false));
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableMiddle2() throws Exception
    {
        behaviourHierarchyTestWork(B_TYPE, new ClassFilter(B_TYPE, false));
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableMiddle3() throws Exception
    {
        behaviourHierarchyTestWork(C_TYPE, new ClassFilter(B_TYPE, false));
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(1, bTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(1, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableMiddle4() throws Exception
    {
        behaviourHierarchyTestWork(A_TYPE, new ClassFilter(B_TYPE, true));
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableMiddle5() throws Exception
    {
        behaviourHierarchyTestWork(B_TYPE, new ClassFilter(B_TYPE, true));
        assertFalse("Behavior should notbe executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior not should be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableMiddle6() throws Exception
    {
        behaviourHierarchyTestWork(C_TYPE, new ClassFilter(B_TYPE, true));
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSub1() throws Exception
    {
        behaviourHierarchyTestWork(A_TYPE, new ClassFilter(C_TYPE, false));
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSub2() throws Exception
    {
        behaviourHierarchyTestWork(B_TYPE, new ClassFilter(C_TYPE, false));
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(1, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSub3() throws Exception
    {
        behaviourHierarchyTestWork(C_TYPE, new ClassFilter(C_TYPE, false));
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSub4() throws Exception
    {
        behaviourHierarchyTestWork(A_TYPE, new ClassFilter(C_TYPE, true));
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSub5() throws Exception
    {
        behaviourHierarchyTestWork(B_TYPE, new ClassFilter(C_TYPE, true));
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(1, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchyDisableSub6() throws Exception
    {
        behaviourHierarchyTestWork(C_TYPE, new ClassFilter(C_TYPE, true));
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testIsEnabled1() throws Exception
    {
        UserTransaction transaction = trxService.getUserTransaction();
        try
        {
            transaction.begin();
            disableBehaviours(new ClassFilter(B_TYPE, true));
            try
            {
                assertEquals("Incorrect behaviour state: global: ", true, behaviourFilter.isEnabled());
                // A_TYPE
                assertEquals("Incorrect behaviour state: class: ", true, behaviourFilter.isEnabled(A_TYPE));
                assertEquals("Incorrect behaviour state: classAndInstance", true, behaviourFilter.isEnabled(companyHome, A_TYPE));
                assertEquals("Incorrect behaviour state: instance", true, behaviourFilter.isEnabled(companyHome));
                // B_TYPE
                assertEquals("Incorrect behaviour state: class: ", false, behaviourFilter.isEnabled(B_TYPE));
                assertEquals("Incorrect behaviour state: classAndInstance", false, behaviourFilter.isEnabled(companyHome, B_TYPE));
                assertEquals("Incorrect behaviour state: instance", true, behaviourFilter.isEnabled(companyHome));
                // C_TYPE
                assertEquals("Incorrect behaviour state: class: ", false, behaviourFilter.isEnabled(C_TYPE));
                assertEquals("Incorrect behaviour state: classAndInstance", false, behaviourFilter.isEnabled(companyHome, C_TYPE));
                assertEquals("Incorrect behaviour state: instance", true, behaviourFilter.isEnabled(companyHome));
            }
            finally
            {
                behaviourFilter.enableBehaviour(B_TYPE);
            }
            transaction.commit();
        }
        catch(Exception e)
        {
            try { transaction.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }

    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testIsEnabled2() throws Exception
    {
        UserTransaction transaction = trxService.getUserTransaction();
        try
        {
            transaction.begin();
            disableBehaviours(new ClassFilter(B_TYPE, false));
            try
            {
                assertEquals("Incorrect behaviour state: global: ", true, behaviourFilter.isEnabled());
                // A_TYPE
                assertEquals("Incorrect behaviour state: class: ", true, behaviourFilter.isEnabled(A_TYPE));
                assertEquals("Incorrect behaviour state: classAndInstance", true, behaviourFilter.isEnabled(companyHome, A_TYPE));
                assertEquals("Incorrect behaviour state: instance", true, behaviourFilter.isEnabled(companyHome));
                // B_TYPE
                assertEquals("Incorrect behaviour state: class: ", false, behaviourFilter.isEnabled(B_TYPE));
                assertEquals("Incorrect behaviour state: classAndInstance", false, behaviourFilter.isEnabled(companyHome, B_TYPE));
                assertEquals("Incorrect behaviour state: instance", true, behaviourFilter.isEnabled(companyHome));
                // C_TYPE
                assertEquals("Incorrect behaviour state: class: ", true, behaviourFilter.isEnabled(C_TYPE));
                assertEquals("Incorrect behaviour state: classAndInstance", true, behaviourFilter.isEnabled(companyHome, C_TYPE));
                assertEquals("Incorrect behaviour state: instance", true, behaviourFilter.isEnabled(companyHome));
            }
            finally
            {
                behaviourFilter.enableBehaviour(B_TYPE);
            }
            transaction.commit();
        }
        catch(Exception e)
        {
            try { transaction.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }
    }


    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchySequence1() throws Exception
    {
        UserTransaction transaction = trxService.getUserTransaction();
        try
        {
            transaction.begin();
            disableBehaviours(new ClassFilter(A_TYPE, true), new ClassFilter(B_TYPE, true));

            behaviourFilter.enableBehaviour(B_TYPE);
            // Should be still disabled
            checkBehaviour(B_TYPE, companyHome, true, false, false, true);
            try
            {
                createDocOfType(C_TYPE);
            }
            finally
            {
                enableBehaviours(new ClassFilter(A_TYPE, true), new ClassFilter(B_TYPE, true));
            }
            transaction.commit();
        }
        catch(Exception e)
        {
            try { transaction.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }
        assertFalse("Behavior should not be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(0, aTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(0, bTypeBehavior.getExecutionCount());
        assertFalse("Behavior should not be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(0, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchySequence2() throws Exception
    {
        UserTransaction transaction = trxService.getUserTransaction();
        try
        {
            transaction.begin();
            disableBehaviours(new ClassFilter(A_TYPE, false), new ClassFilter(B_TYPE, true));

            behaviourFilter.enableBehaviour(B_TYPE);
            assertEquals("Incorrect behaviour state: class: ", true, behaviourFilter.isEnabled(B_TYPE));
            assertEquals("Incorrect behaviour state: classAndInstance", true, behaviourFilter.isEnabled(companyHome, B_TYPE));
            assertEquals("Incorrect behaviour state: instance", true, behaviourFilter.isEnabled(companyHome));
            try
            {
                createDocOfType(C_TYPE);
            }
            finally
            {
                enableBehaviours(new ClassFilter(A_TYPE, true), new ClassFilter(B_TYPE, true));
            }
            transaction.commit();
        }
        catch(Exception e)
        {
            try { transaction.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(1, bTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(1, cTypeBehavior.getExecutionCount());
    }

    /**
     * Test for MNT-13836 (new API)
     * @throws Exception
     */
    public void testBehaviourHierarchySequence3() throws Exception
    {
        UserTransaction transaction = trxService.getUserTransaction();
        try
        {
            transaction.begin();
            disableBehaviours(new ClassFilter(A_TYPE, false), new ClassFilter(B_TYPE, false));
            try
            {
                createDocOfType(C_TYPE);
            }
            finally
            {
                enableBehaviours(new ClassFilter(A_TYPE, true), new ClassFilter(B_TYPE, true));
            }
            transaction.commit();
        }
        catch(Exception e)
        {
            try { transaction.rollback(); } catch (IllegalStateException ee) {}
            throw e;
        }
        assertTrue("Behavior should be executed for a_type.", aTypeBehavior.isExecuted());
        assertEquals(1, aTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for b_type.", bTypeBehavior.isExecuted());
        assertEquals(1, bTypeBehavior.getExecutionCount());
        assertTrue("Behavior should be executed for c_type.", cTypeBehavior.isExecuted());
        assertEquals(1, cTypeBehavior.getExecutionCount());
    }

    private void disableBehaviours(ClassFilter... classFilters)
    {
        for(int i = 0; i < classFilters.length; i++)
        {
            behaviourFilter.disableBehaviour(
                    classFilters[i].getClassName(),
                    classFilters[i].isDisableSubClasses()
            );
            // check that behavior is disabled correctly
            checkBehaviour(classFilters[i].getClassName(), companyHome, true, false, false, true);
        }
    }

    private void enableBehaviours(ClassFilter... types)
    {
        for(int i = 0; i < types.length; i++)
        {
            behaviourFilter.enableBehaviour(types[i].getClassName());
        }
        for(int i = 0; i < types.length; i++)
        {
            checkBehaviour(types[i].getClassName(), companyHome, true, true, true, true);
        }
    }

    private void createDocOfType(QName type)
    {
        final String name = "Test (" + System.currentTimeMillis() + ").docx";
        final Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);

        // create node of child type
        nodeService.createNode(companyHome,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, name),
                type,
                contentProps);
    }

    /**
     * @param className                 the class to check
     * @param nodeRef                   the node instance to check
     * @param globalEnabled             <tt>true</tt> if the global filter should be enabled
     * @param classEnabled              <tt>true</tt> if the class should be enabled
     * @param classInstanceEnabled      <tt>true</tt> if the class and instance should be enabled
     * @param instanceEnabled           <tt>true</tt> if the instance should be enabled
     */
    private void checkBehaviour(
            QName className, NodeRef nodeRef,
            boolean globalEnabled,
            boolean classEnabled,
            boolean classInstanceEnabled,
            boolean instanceEnabled)
            
    {
        assertEquals("Incorrect behaviour state: global: ", globalEnabled, behaviourFilter.isEnabled());
        assertEquals("Incorrect behaviour state: class: ", classEnabled, behaviourFilter.isEnabled(className));
        assertEquals("Incorrect behaviour state: classAndInstance", classInstanceEnabled, behaviourFilter.isEnabled(nodeRef, className));
        assertEquals("Incorrect behaviour state: instance", instanceEnabled, behaviourFilter.isEnabled(nodeRef));
        assertEquals("'Active' flag incorrect: ",
                !(globalEnabled && classEnabled && classInstanceEnabled && instanceEnabled),
                behaviourFilter.isActivated());
    }

    //
    // Behaviour Implementations
    //
    
    public String startTransactionTest(String key1, String key2, String arg1, String arg2, boolean sideEffect, List<TestResult> results)
    {
        TestResult result = new TestResult("startTransactionTest", key1, key2, arg1, arg2);
        results.add(result);
        if (sideEffect)
        {
            SideEffectTestPolicy policy = sideEffectDelegate.get(BASE_TYPE);
            policy.sideEffect(key1, key2, arg1, arg2, results);
        }
        return result.toString();
    }

    public String endTransactionTest(String key1, String key2, String arg1, String arg2, boolean sideEffect, List<TestResult> results)
    {
        TestResult result = new TestResult("endTransactionTest", key1, key2, arg1, arg2);
        results.add(result);
        if (sideEffect)
        {
            SideEffectTestPolicy policy = sideEffectDelegate.get(BASE_TYPE);
            policy.sideEffect(key1, key2, arg1, arg2, results);
        }
        return result.toString();
    }
    
    public String sideEffectTest(String key1, String key2, String arg1, String arg2, List<TestResult> results)
    {
        TestResult result = new TestResult("sideEffectTest", key1, key2, arg1, arg2);
        results.add(result);
        return result.toString();
    }    

    
    //
    // Policy Definitions
    //
    
    public interface StartTestPolicy extends ClassPolicy
    {
        public String start(String key1, String key2, String arg1, String arg2, boolean sideEffect, List<TestResult> results);
        
        static String NAMESPACE = TEST_NAMESPACE;
        static Arg ARG_0 = Arg.KEY;
        static Arg ARG_1 = Arg.KEY;
        static Arg ARG_2 = Arg.START_VALUE;
        static Arg ARG_3 = Arg.END_VALUE;
    }
    
    public interface EndTestPolicy extends ClassPolicy
    {
        public String end(String key1, String key2, String arg1, String arg2, boolean sideEffect, List<TestResult> results);
        
        static String NAMESPACE = TEST_NAMESPACE;
        static Arg ARG_0 = Arg.KEY;
        static Arg ARG_1 = Arg.KEY;
        static Arg ARG_2 = Arg.START_VALUE;
        static Arg ARG_3 = Arg.END_VALUE;
    }
    
    public interface SideEffectTestPolicy extends ClassPolicy
    {
        public String sideEffect(String key1, String key2, String arg1, String arg2, List<TestResult> resultTest);
        
        static String NAMESPACE = TEST_NAMESPACE;
        static Arg ARG_0 = Arg.KEY;
        static Arg ARG_1 = Arg.KEY;
    }
    

    /**
     * Result of Policy Invocation
     */
    private class TestResult
    {
        private String trxId;
        private String behaviour;
        private String key1;
        private String key2;
        private String arg1;
        private String arg2;

        /**
         * Construct
         * 
         * @param behaviour String
         * @param key1 String
         * @param key2 String
         * @param arg1 String
         * @param arg2 String
         */
        public TestResult(String behaviour, String key1, String key2, String arg1, String arg2)
        {
            this.trxId = AlfrescoTransactionSupport.getTransactionId();
            this.behaviour = behaviour;
            this.key1 = key1;
            this.key2 = key2;
            this.arg1 = arg1;
            this.arg2 = arg2;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj instanceof TestResult)
            {
                TestResult that = (TestResult) obj;
                return (this.trxId.equals(that.trxId) &&
                        this.behaviour.equals(that.behaviour) &&
                        this.key1.equals(that.key1) &&
                        this.key2.equals(that.key2) &&
                        this.arg1.equals(that.arg1) &&
                        this.arg2.equals(that.arg2));
            }
            else
            {
                return false;
            }
        }

        @Override
        public String toString()
        {
            return "trxId=" + trxId + ", behaviour=" + behaviour + ", key1=" + key1 + ", key2=" + key2 + ", arg1=" + arg1 + ", arg2=" + arg2;
        }
    }
    
    private class TestOnCreateNodePolicy implements OnCreateNodePolicy
    {
        private boolean executed;
        private int executionCount;

        @Override
        public void onCreateNode(ChildAssociationRef childAssocRef)
        {
            executed = true;
            executionCount++;
        }
        
        public boolean isExecuted()
        {
            return executed;
        }
        
        public int getExecutionCount()
        {
            return executionCount;
        }
    }
    
}

