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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import junit.framework.TestCase;

import org.alfresco.repo.cache.MemoryCache;
import org.alfresco.repo.dictionary.CompiledModelsCache;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ThreadPoolExecutorFactoryBean;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;

/**
 * Policy Component Tests
 * 
 * Multi-tennant
 * 
 * @author mrogers
 */
public class MTPolicyComponentTest extends TestCase
{
    private static final String TEST_MODEL = "org/alfresco/repo/policy/policycomponenttest_model.xml";
    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/policycomponenttest/1.0";
    private static QName BASE_TYPE = QName.createQName(TEST_NAMESPACE, "base");
    private static QName BASE_PROP_A = QName.createQName(TEST_NAMESPACE, "base_a");
    private static QName BASE_ASSOC_A = QName.createQName(TEST_NAMESPACE, "base_assoc_a");
    private static QName FILE_TYPE = QName.createQName(TEST_NAMESPACE, "file");
    private static QName FILE_PROP_B = QName.createQName(TEST_NAMESPACE, "file_b");
    private static QName FOLDER_TYPE = QName.createQName(TEST_NAMESPACE, "folder");
    private static QName FOLDER_PROP_D = QName.createQName(TEST_NAMESPACE, "folder_d");
    private static QName TEST_ASPECT = QName.createQName(TEST_NAMESPACE, "aspect");
    private static QName ASPECT_PROP_A = QName.createQName(TEST_NAMESPACE, "aspect_a");
    private static QName INVALID_TYPE = QName.createQName(TEST_NAMESPACE, "classdoesnotexist");

    private PolicyComponent policyComponent = null;


    static final String BASE_PROTOCOL = "baseProtocol";
    static final String BASE_IDENTIFIER = "baseIdentifier";
    static final String BASE_ID = "baseId";
    	
    @Override
    protected void setUp() throws Exception
    {
    	TenantService mockTenantService = mock(TenantService.class);
    	when(mockTenantService.isEnabled()).thenReturn(true);
    	when(mockTenantService.getCurrentUserDomain()).thenReturn("test.com");
    	when(mockTenantService.getDomainUser(any(String.class), any(String.class))).thenReturn("System");
    	when(mockTenantService.getBaseName(any(NodeRef.class))).thenReturn(new NodeRef(BASE_PROTOCOL, BASE_IDENTIFIER, BASE_ID));
    	when(mockTenantService.getBaseName(any(StoreRef.class))).thenReturn(new StoreRef(BASE_PROTOCOL, BASE_IDENTIFIER));

    	
        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl();
        dictionaryDAO.setTenantService(mockTenantService);
        initDictionaryCaches(dictionaryDAO, mockTenantService);

        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");
        bootstrapModels.add("alfresco/model/systemModel.xml");
        bootstrapModels.add("org/alfresco/repo/policy/policycomponenttest_model.xml");
        bootstrapModels.add(TEST_MODEL);
        bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.setTenantService(mockTenantService);
        bootstrap.bootstrap();

        DictionaryComponent dictionary = new DictionaryComponent();
        dictionary.setDictionaryDAO(dictionaryDAO);

        // Instantiate Policy Component
        PolicyComponentImpl x = new PolicyComponentImpl(dictionary);
        x.setTenantService(mockTenantService);
        policyComponent = x;
    }

    @SuppressWarnings("unchecked")
    private void initDictionaryCaches(DictionaryDAOImpl dictionaryDAO, TenantService tenantService) throws Exception
    {
        CompiledModelsCache compiledModelsCache = new CompiledModelsCache();
        compiledModelsCache.setDictionaryDAO(dictionaryDAO);
        compiledModelsCache.setTenantService(tenantService);
        compiledModelsCache.setRegistry(new DefaultAsynchronouslyRefreshedCacheRegistry());
        ThreadPoolExecutorFactoryBean threadPoolfactory = new ThreadPoolExecutorFactoryBean();
        threadPoolfactory.afterPropertiesSet();
        compiledModelsCache.setThreadPoolExecutor((ThreadPoolExecutor) threadPoolfactory.getObject());
        dictionaryDAO.setDictionaryRegistryCache(compiledModelsCache);
        dictionaryDAO.init();
    }

    public void testJavaBehaviour()
    {
        Behaviour validBehaviour = new JavaBehaviour(this, "validClassTest");
        TestClassPolicy policy = validBehaviour.getInterface(TestClassPolicy.class);
        assertNotNull(policy);
        NodeRef nodeRef = new NodeRef("workspace", "SpacesStore", "123");
        Date date = new Date();
        StoreRef storeRef = new StoreRef("workspace", "SpacesStore");
        TestClassPolicyResult result = policy.test("argument", nodeRef, date, storeRef);
        assertEquals("", "ValidTest: argument", result.getString());
        assertEquals("", nodeRef, result.getNodeRef());
    }
    
    
    @SuppressWarnings("unchecked")
    public void testRegisterDefinitions()
    {
        try
        {
            @SuppressWarnings("unused") ClassPolicyDelegate<InvalidMetaDataPolicy> delegate = policyComponent.registerClassPolicy(InvalidMetaDataPolicy.class);
            fail("Failed to catch hidden metadata");
        }
        catch(PolicyException e)
        {
        }
    
        try
        {
            @SuppressWarnings("unused") ClassPolicyDelegate<NoMethodPolicy> delegate = policyComponent.registerClassPolicy(NoMethodPolicy.class);
            fail("Failed to catch no methods defined in policy");
        }
        catch(PolicyException e)
        {
        }

        try
        {
            @SuppressWarnings("unused") ClassPolicyDelegate<MultiMethodPolicy> delegate = policyComponent.registerClassPolicy(MultiMethodPolicy.class);
            fail("Failed to catch multiple methods defined in policy");
        }
        catch(PolicyException e)
        {
        }
        
        QName policyName = QName.createQName(TEST_NAMESPACE, "test");
        boolean isRegistered = policyComponent.isRegisteredPolicy(PolicyType.Class, policyName);
        assertFalse(isRegistered);
        ClassPolicyDelegate<TestClassPolicy> delegate = policyComponent.registerClassPolicy(TestClassPolicy.class);
        assertNotNull(delegate);
        isRegistered = policyComponent.isRegisteredPolicy(PolicyType.Class, policyName);
        assertTrue(isRegistered);
        PolicyDefinition definition = policyComponent.getRegisteredPolicy(PolicyType.Class, policyName);
        assertNotNull(definition);
        assertEquals(policyName, definition.getName());
        assertEquals(PolicyType.Class, definition.getType());
        assertEquals(TestClassPolicy.class, definition.getPolicyInterface());
    }
    
    
    public void testBindBehaviour()
    {
        QName policyName = QName.createQName(TEST_NAMESPACE, "test");
        Behaviour validBehaviour = new JavaBehaviour(this, "validClassTest");
        
        // Test null policy
        try
        {
            policyComponent.bindClassBehaviour(null, FILE_TYPE, validBehaviour);
            fail("Failed to catch null policy whilst binding behaviour");
        }
        catch(IllegalArgumentException e) {}

        // Test null Class Reference
        try
        {
            policyComponent.bindClassBehaviour(policyName, null, validBehaviour);
            fail("Failed to catch null class reference whilst binding behaviour");
        }
        catch(IllegalArgumentException e) {}

        // Test invalid Class Reference
        try
        {
            policyComponent.bindClassBehaviour(policyName, INVALID_TYPE, validBehaviour);
            fail("Failed to catch invalid class reference whilst binding behaviour");
        }
        catch(IllegalArgumentException e) {}
        
        // Test null Behaviour
        try
        {
            policyComponent.bindClassBehaviour(policyName, FILE_TYPE, null);
            fail("Failed to catch null behaviour whilst binding behaviour");
        }
        catch(IllegalArgumentException e) {}

        // Test invalid behaviour (for registered policy)
        Behaviour invalidBehaviour = new JavaBehaviour(this, "methoddoesnotexist");
        policyComponent.registerClassPolicy(TestClassPolicy.class);
        try
        {
            policyComponent.bindClassBehaviour(policyName, FILE_TYPE, invalidBehaviour);
            fail("Failed to catch invalid behaviour whilst binding behaviour");
        }
        catch(PolicyException e) {}
        
        // Test valid behaviour (for registered policy)
        try
        {
            BehaviourDefinition<ClassBehaviourBinding> definition = policyComponent.bindClassBehaviour(policyName, FILE_TYPE, validBehaviour);
            assertNotNull(definition);
            assertEquals(policyName, definition.getPolicy());
            assertEquals(FILE_TYPE, definition.getBinding().getClassQName());
        }
        catch(PolicyException e)
        {
            fail("Policy exception thrown for valid behaviour" + e.toString());
        }
    }


    public void testClassDelegate()
    {
        // Register Policy
        ClassPolicyDelegate<TestClassPolicy> delegate = policyComponent.registerClassPolicy(TestClassPolicy.class);
        
        // Bind Class Behaviour
        QName policyName = QName.createQName(TEST_NAMESPACE, "test");
        Behaviour fileBehaviour = new JavaBehaviour(this, "fileTest");
        policyComponent.bindClassBehaviour(policyName, FILE_TYPE, fileBehaviour);
        
        NodeRef nodeRef = new NodeRef("workspace", "SpacesStore", "123");
        // base node ref gets set by mocked mt service
        NodeRef baseNodeRef = new NodeRef(BASE_PROTOCOL, BASE_IDENTIFIER, BASE_ID);
        
        Date date = new Date();
        StoreRef storeRef = new StoreRef("workspace", "SpacesStore");

        // Test NOOP Policy delegate
        Collection<TestClassPolicy> basePolicies = delegate.getList(BASE_TYPE);
        assertNotNull(basePolicies);
        assertEquals(0, basePolicies.size());
        TestClassPolicy basePolicy = delegate.get(BASE_TYPE);
        assertNotNull(basePolicy);
        TestClassPolicyResult baseResult = basePolicy.test("womble", nodeRef, date, storeRef);
        // we don't expect a result from the NO-OP handler
        assertNull("noop handler unexpectedly returned a result", baseResult);
        
        // Test single Policy delegate
        Collection<TestClassPolicy> filePolicies = delegate.getList(FILE_TYPE);
        assertNotNull(filePolicies);
        assertEquals(1, filePolicies.size());
        TestClassPolicy filePolicy = delegate.get(FILE_TYPE);
        assertNotNull(filePolicy);
        TestClassPolicyResult fileResult = filePolicy.test("womble", nodeRef, date, storeRef);
        assertEquals("argument type of NodeRef not replaced by base node ref", fileResult.getNodeRef(), baseNodeRef);
        
        
        // Bind Service Behaviour
        Behaviour serviceBehaviour = new JavaBehaviour(this, "serviceTest");
        policyComponent.bindClassBehaviour(policyName, this, serviceBehaviour);

        // Test multi Policy delegate
        Collection<TestClassPolicy> file2Policies = delegate.getList(FILE_TYPE);
        assertNotNull(file2Policies);
        assertEquals(2, file2Policies.size());
        TestClassPolicy filePolicy2 = delegate.get(FILE_TYPE);
        assertNotNull(filePolicy2);
        TestClassPolicyResult fileResult2 = filePolicy2.test("womble", nodeRef, date, storeRef);
        assertEquals("argument type of NodeRef not replaced by base node ref", fileResult2.getNodeRef(), baseNodeRef);
        
        // Test multiple class behaviours
        Behaviour file2Behaviour = new JavaBehaviour(this, "fileTest2");
        policyComponent.bindClassBehaviour(policyName, FILE_TYPE, file2Behaviour);
        Collection<TestClassPolicy> file3Policies = delegate.getList(FILE_TYPE);
        assertNotNull(file3Policies);
        assertEquals(3, file3Policies.size());
        TestClassPolicy filePolicy3 = delegate.get(FILE_TYPE);
        assertNotNull(filePolicy3);
        TestClassPolicyResult fileResult3 = filePolicy3.test("womble", nodeRef, date, storeRef);
        assertEquals("argument type of NodeRef not replaced by base node ref", fileResult3.getNodeRef(), baseNodeRef);
    }
    
    //
    // The following interfaces represents policies
    //
    
    public class TestClassPolicyResult
    {
    	String s;    // Not affected by MT
    	NodeRef nodeRef;
    	StoreRef storeRef;
    	ChildAssociationRef childAssociationRef;
    	Date date;   // Not affected by MT
    	String getString()
    	{
    		return s;
    	}
    	NodeRef getNodeRef()
    	{
    		return nodeRef;
    	}
    }
    
    public interface TestClassPolicy extends ClassPolicy
    {
        static String NAMESPACE = TEST_NAMESPACE;
        public TestClassPolicyResult test(String argument, NodeRef nodeRef, Date date, StoreRef storeRef);
    }

    public interface TestPropertyPolicy extends PropertyPolicy
    {
        static String NAMESPACE = TEST_NAMESPACE;
        public String test(String argument);
    }

    public interface TestAssociationPolicy extends AssociationPolicy
    {
        static String NAMESPACE = TEST_NAMESPACE;
        public String test(String argument);
    }

    public interface InvalidMetaDataPolicy extends ClassPolicy
    {
        static int NAMESPACE = 0;
        public String test(String nodeRef);
    }

    public interface NoMethodPolicy extends ClassPolicy
    {
    }
    
    public interface MultiMethodPolicy extends ClassPolicy
    {
        public void a();
        public void b();
    }
    
    
    //
    // The following methods represent Java Behaviours
    // 
    
    public TestClassPolicyResult validClassTest(String argument, NodeRef nodeRef, Date date, StoreRef storeRef)
    {
    	TestClassPolicyResult result = new TestClassPolicyResult();
    	result.s = "ValidTest: " + argument;
    	result.nodeRef = nodeRef;
    	result.date = date;
    	result.storeRef = storeRef;
        return result;
    }
        
    public TestClassPolicyResult fileTest(String argument, NodeRef nodeRef, Date date, StoreRef storeRef)
    {    	
    	TestClassPolicyResult result = new TestClassPolicyResult();
	    result.s = "ValidTest: " + argument;
	    result.nodeRef = nodeRef;
	    result.date = date;
	    result.storeRef = storeRef;
        return result;
    }

    public TestClassPolicyResult fileTest2(String argument, NodeRef nodeRef, Date date, StoreRef storeRef)
    {
    	TestClassPolicyResult result = new TestClassPolicyResult();
    	result.s = "ValidTest: " + argument;
    	result.nodeRef = nodeRef;
    	result.date = date;
    	result.storeRef = storeRef;
        return result;
    }


    public TestClassPolicyResult serviceTest(String argument, NodeRef nodeRef, Date date, StoreRef storeRef)
    {
    	TestClassPolicyResult result = new TestClassPolicyResult();
    	result.s = "ValidTest: " + argument;
    	result.nodeRef = nodeRef;
    	result.date = date;
    	result.storeRef = storeRef;
        return result;
    }
    
}
