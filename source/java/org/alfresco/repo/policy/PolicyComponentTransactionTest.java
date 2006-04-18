/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.policy;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.Policy.Arg;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;


/**
 * Test Transaction-level Policies
 */
public class PolicyComponentTransactionTest extends TestCase
{
    private static final String TEST_MODEL = "org/alfresco/repo/policy/policycomponenttest_model.xml";
    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/policycomponenttest/1.0";
    private static QName BASE_TYPE = QName.createQName(TEST_NAMESPACE, "base");
    
    private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
    private PolicyComponent policyComponent;
    private TransactionService trxService;
    private AuthenticationComponent authenticationComponent;
    private ClassPolicyDelegate<SideEffectTestPolicy> sideEffectDelegate;

    
    @Override
    protected void setUp() throws Exception
    {
        // initialise policy test model
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add(TEST_MODEL);
        bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO((DictionaryDAO)applicationContext.getBean("dictionaryDAO"));
        bootstrap.bootstrap();
        
        // retrieve policy component
        this.policyComponent = (PolicyComponent)applicationContext.getBean("policyComponent");
        this.trxService = (TransactionService) applicationContext.getBean("transactionComponent");
        this.authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // Register Policy
        sideEffectDelegate = policyComponent.registerClassPolicy(SideEffectTestPolicy.class);

        // Bind Behaviour to side effect policy
        QName policyName = QName.createQName(TEST_NAMESPACE, "sideEffect");
        Behaviour baseBehaviour = new JavaBehaviour(this, "sideEffectTest", NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindClassBehaviour(policyName, BASE_TYPE, baseBehaviour);
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
         * @param behaviour
         * @param key1
         * @param key2
         * @param arg1
         * @param arg2
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
    
}

