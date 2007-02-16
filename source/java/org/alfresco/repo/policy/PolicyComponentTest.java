/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.dictionary.NamespaceDAOImpl;
import org.alfresco.service.namespace.QName;


public class PolicyComponentTest extends TestCase
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


    @Override
    protected void setUp() throws Exception
    {
        // Instantiate Dictionary Service
        NamespaceDAO namespaceDAO = new NamespaceDAOImpl();
        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl(namespaceDAO);
        
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");
        bootstrapModels.add("org/alfresco/repo/policy/policycomponenttest_model.xml");
        bootstrapModels.add(TEST_MODEL);
        bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.bootstrap();

        DictionaryComponent dictionary = new DictionaryComponent();
        dictionary.setDictionaryDAO(dictionaryDAO);

        // Instantiate Policy Component
        policyComponent = new PolicyComponentImpl(dictionary); 
    }


    public void testJavaBehaviour()
    {
        Behaviour validBehaviour = new JavaBehaviour(this, "validTest");
        TestClassPolicy policy = validBehaviour.getInterface(TestClassPolicy.class);
        assertNotNull(policy);
        String result = policy.test("argument");
        assertEquals("ValidTest: argument", result);
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
        Behaviour validBehaviour = new JavaBehaviour(this, "validTest");
        
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
            fail("Policy exception thrown for valid behaviour");
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

        // Test NOOP Policy delegate
        Collection<TestClassPolicy> basePolicies = delegate.getList(BASE_TYPE);
        assertNotNull(basePolicies);
        assertTrue(basePolicies.size() == 0);
        TestClassPolicy basePolicy = delegate.get(BASE_TYPE);
        assertNotNull(basePolicy);
        
        // Test single Policy delegate
        Collection<TestClassPolicy> filePolicies = delegate.getList(FILE_TYPE);
        assertNotNull(filePolicies);
        assertTrue(filePolicies.size() == 1);
        TestClassPolicy filePolicy = delegate.get(FILE_TYPE);
        assertNotNull(filePolicy);
        assertEquals(filePolicies.iterator().next(), filePolicy);

        // Bind Service Behaviour
        Behaviour serviceBehaviour = new JavaBehaviour(this, "serviceTest");
        policyComponent.bindClassBehaviour(policyName, this, serviceBehaviour);

        // Test multi Policy delegate
        Collection<TestClassPolicy> file2Policies = delegate.getList(FILE_TYPE);
        assertNotNull(file2Policies);
        assertTrue(file2Policies.size() == 2);
        TestClassPolicy filePolicy2 = delegate.get(FILE_TYPE);
        assertNotNull(filePolicy2);
    }

    
    public void testClassOverride()
    {
        // Register Policy
        ClassPolicyDelegate<TestClassPolicy> delegate = policyComponent.registerClassPolicy(TestClassPolicy.class);
        
        // Bind Behaviour
        QName policyName = QName.createQName(TEST_NAMESPACE, "test");
        Behaviour baseBehaviour = new JavaBehaviour(this, "baseTest");
        policyComponent.bindClassBehaviour(policyName, BASE_TYPE, baseBehaviour);
        Behaviour folderBehaviour = new JavaBehaviour(this, "folderTest");
        policyComponent.bindClassBehaviour(policyName, FOLDER_TYPE, folderBehaviour);

        // Invoke Policies        
        TestClassPolicy basePolicy = delegate.get(BASE_TYPE);
        String baseResult = basePolicy.test("base");
        assertEquals("Base: base", baseResult);
        TestClassPolicy filePolicy = delegate.get(FILE_TYPE);
        String fileResult = filePolicy.test("file");
        assertEquals("Base: file", fileResult);
        TestClassPolicy folderPolicy = delegate.get(FOLDER_TYPE);
        String folderResult = folderPolicy.test("folder");
        assertEquals("Folder: folder", folderResult);
    }
    
    
    public void testClassCache()
    {
        // Register Policy
        ClassPolicyDelegate<TestClassPolicy> delegate = policyComponent.registerClassPolicy(TestClassPolicy.class);
        
        // Bind Behaviour
        QName policyName = QName.createQName(TEST_NAMESPACE, "test");
        Behaviour baseBehaviour = new JavaBehaviour(this, "baseTest");
        policyComponent.bindClassBehaviour(policyName, BASE_TYPE, baseBehaviour);
        Behaviour folderBehaviour = new JavaBehaviour(this, "folderTest");
        policyComponent.bindClassBehaviour(policyName, FOLDER_TYPE, folderBehaviour);

        // Invoke Policies        
        TestClassPolicy basePolicy = delegate.get(BASE_TYPE);
        String baseResult = basePolicy.test("base");
        assertEquals("Base: base", baseResult);
        TestClassPolicy filePolicy = delegate.get(FILE_TYPE);
        String fileResult = filePolicy.test("file");
        assertEquals("Base: file", fileResult);
        TestClassPolicy folderPolicy = delegate.get(FOLDER_TYPE);
        String folderResult = folderPolicy.test("folder");
        assertEquals("Folder: folder", folderResult);
        
        // Retrieve delegates again        
        TestClassPolicy basePolicy2 = delegate.get(BASE_TYPE);
        assertTrue(basePolicy == basePolicy2);
        TestClassPolicy filePolicy2 = delegate.get(FILE_TYPE);
        assertTrue(filePolicy == filePolicy2);
        TestClassPolicy folderPolicy2 = delegate.get(FOLDER_TYPE);
        assertTrue(folderPolicy == folderPolicy2);
        
        // Bind new behaviour (forcing base & file cache resets)
        Behaviour newBaseBehaviour = new JavaBehaviour(this, "newBaseTest");
        policyComponent.bindClassBehaviour(policyName, BASE_TYPE, newBaseBehaviour);

        // Invoke Policies        
        TestClassPolicy basePolicy3 = delegate.get(BASE_TYPE);
        assertTrue(basePolicy3 != basePolicy2);
        String baseResult3 = basePolicy3.test("base");
        assertEquals("NewBase: base", baseResult3);
        TestClassPolicy filePolicy3 = delegate.get(FILE_TYPE);
        assertTrue(filePolicy3 != filePolicy2);
        String fileResult3 = filePolicy3.test("file");
        assertEquals("NewBase: file", fileResult3);
        TestClassPolicy folderPolicy3 = delegate.get(FOLDER_TYPE);
        assertTrue(folderPolicy3 == folderPolicy2);
        String folderResult3 = folderPolicy3.test("folder");
        assertEquals("Folder: folder", folderResult3);
        
        // Bind new behaviour (forcing file cache reset)
        Behaviour fileBehaviour = new JavaBehaviour(this, "fileTest");
        policyComponent.bindClassBehaviour(policyName, FILE_TYPE, fileBehaviour);

        // Invoke Policies        
        TestClassPolicy basePolicy4 = delegate.get(BASE_TYPE);
        assertTrue(basePolicy4 == basePolicy3);
        String baseResult4 = basePolicy4.test("base");
        assertEquals("NewBase: base", baseResult4);
        TestClassPolicy filePolicy4 = delegate.get(FILE_TYPE);
        assertTrue(filePolicy4 != filePolicy3);
        String fileResult4 = filePolicy4.test("file");
        assertEquals("File: file", fileResult4);
        TestClassPolicy folderPolicy4 = delegate.get(FOLDER_TYPE);
        assertTrue(folderPolicy4 == folderPolicy4);
        String folderResult4 = folderPolicy4.test("folder");
        assertEquals("Folder: folder", folderResult4);
    }


    public void testPropertyDelegate()
    {
        // Register Policy
        PropertyPolicyDelegate<TestPropertyPolicy> delegate = policyComponent.registerPropertyPolicy(TestPropertyPolicy.class);
        
        // Bind Property Behaviour
        QName policyName = QName.createQName(TEST_NAMESPACE, "test");
        Behaviour fileBehaviour = new JavaBehaviour(this, "fileTest");
        policyComponent.bindPropertyBehaviour(policyName, FILE_TYPE, FILE_PROP_B, fileBehaviour);

        // Test NOOP Policy delegate
        Collection<TestPropertyPolicy> basePolicies = delegate.getList(BASE_TYPE, BASE_PROP_A);
        assertNotNull(basePolicies);
        assertTrue(basePolicies.size() == 0);
        TestPropertyPolicy basePolicy = delegate.get(BASE_TYPE, BASE_PROP_A);
        assertNotNull(basePolicy);
        
        // Test single Policy delegate
        Collection<TestPropertyPolicy> filePolicies = delegate.getList(FILE_TYPE, FILE_PROP_B);
        assertNotNull(filePolicies);
        assertTrue(filePolicies.size() == 1);
        TestPropertyPolicy filePolicy = delegate.get(FILE_TYPE, FILE_PROP_B);
        assertNotNull(filePolicy);
        assertEquals(filePolicies.iterator().next(), filePolicy);

        // Bind Service Behaviour
        Behaviour serviceBehaviour = new JavaBehaviour(this, "serviceTest");
        policyComponent.bindPropertyBehaviour(policyName, this, serviceBehaviour);

        // Test multi Policy delegate
        Collection<TestPropertyPolicy> file2Policies = delegate.getList(FILE_TYPE, FILE_PROP_B);
        assertNotNull(file2Policies);
        assertTrue(file2Policies.size() == 2);
        TestPropertyPolicy filePolicy2 = delegate.get(FILE_TYPE, FILE_PROP_B);
        assertNotNull(filePolicy2);
    }

    
    public void testPropertyOverride()
    {
        // Register Policy
        PropertyPolicyDelegate<TestPropertyPolicy> delegate = policyComponent.registerPropertyPolicy(TestPropertyPolicy.class);
        
        // Bind Behaviour
        QName policyName = QName.createQName(TEST_NAMESPACE, "test");
        Behaviour baseBehaviour = new JavaBehaviour(this, "baseTest");
        policyComponent.bindPropertyBehaviour(policyName, BASE_TYPE, BASE_PROP_A, baseBehaviour);
        Behaviour folderBehaviour = new JavaBehaviour(this, "folderTest");
        policyComponent.bindPropertyBehaviour(policyName, FOLDER_TYPE, BASE_PROP_A, folderBehaviour);
        Behaviour folderBehaviourD = new JavaBehaviour(this, "folderTest");
        policyComponent.bindPropertyBehaviour(policyName, FOLDER_TYPE, FOLDER_PROP_D, folderBehaviourD);

        // Invoke Policies        
        TestPropertyPolicy basePolicy = delegate.get(BASE_TYPE, BASE_PROP_A);
        String baseResult = basePolicy.test("base");
        assertEquals("Base: base", baseResult);
        TestPropertyPolicy filePolicy = delegate.get(FILE_TYPE, BASE_PROP_A);
        String fileResult = filePolicy.test("file");
        assertEquals("Base: file", fileResult);
        TestPropertyPolicy folderPolicy = delegate.get(FOLDER_TYPE, BASE_PROP_A);
        String folderResult = folderPolicy.test("folder");
        assertEquals("Folder: folder", folderResult);
        TestPropertyPolicy folderPolicy2 = delegate.get(FOLDER_TYPE, FOLDER_PROP_D);
        String folderResult2 = folderPolicy2.test("folder");
        assertEquals("Folder: folder", folderResult2);
    }

    
    public void testPropertyWildcard()
    {
        // Register Policy
        PropertyPolicyDelegate<TestPropertyPolicy> delegate = policyComponent.registerPropertyPolicy(TestPropertyPolicy.class);
        
        // Bind Behaviour
        QName policyName = QName.createQName(TEST_NAMESPACE, "test");
        Behaviour baseBehaviour = new JavaBehaviour(this, "baseTest");
        policyComponent.bindPropertyBehaviour(policyName, BASE_TYPE, baseBehaviour);
        Behaviour folderBehaviour = new JavaBehaviour(this, "folderTest");
        policyComponent.bindPropertyBehaviour(policyName, FOLDER_TYPE, folderBehaviour);
        Behaviour aspectBehaviour = new JavaBehaviour(this, "aspectTest");
        policyComponent.bindPropertyBehaviour(policyName, TEST_ASPECT, aspectBehaviour);
        
        // Invoke Policies        
        TestPropertyPolicy basePolicy = delegate.get(BASE_TYPE, BASE_PROP_A);
        String baseResult = basePolicy.test("base");
        assertEquals("Base: base", baseResult);
        TestPropertyPolicy filePolicy = delegate.get(FILE_TYPE, BASE_PROP_A);
        String fileResult = filePolicy.test("file");
        assertEquals("Base: file", fileResult);
        TestPropertyPolicy folderPolicy = delegate.get(FOLDER_TYPE, BASE_PROP_A);
        String folderResult = folderPolicy.test("folder");
        assertEquals("Folder: folder", folderResult);
        TestPropertyPolicy folderPolicy2 = delegate.get(FOLDER_TYPE, FOLDER_PROP_D);
        String folderResult2 = folderPolicy2.test("folder");
        assertEquals("Folder: folder", folderResult2);
        TestPropertyPolicy aspectPolicy = delegate.get(TEST_ASPECT, ASPECT_PROP_A);
        String aspectResult = aspectPolicy.test("aspect_prop_a");
        assertEquals("Aspect: aspect_prop_a", aspectResult);
        TestPropertyPolicy aspectPolicy2 = delegate.get(TEST_ASPECT, FOLDER_PROP_D);
        String aspectResult2 = aspectPolicy2.test("aspect_folder_d");
        assertEquals("Aspect: aspect_folder_d", aspectResult2);

        // Override wild-card with specific property binding
        Behaviour folderDBehaviour = new JavaBehaviour(this, "folderDTest");
        policyComponent.bindPropertyBehaviour(policyName, FOLDER_TYPE, FOLDER_PROP_D, folderDBehaviour);
        TestPropertyPolicy folderPolicy3 = delegate.get(FOLDER_TYPE, FOLDER_PROP_D);
        String folderResult3 = folderPolicy3.test("folder");
        assertEquals("FolderD: folder", folderResult3);
    }
    

    public void testPropertyCache()
    {
        // Register Policy
        PropertyPolicyDelegate<TestPropertyPolicy> delegate = policyComponent.registerPropertyPolicy(TestPropertyPolicy.class);
        
        // Bind Behaviour
        QName policyName = QName.createQName(TEST_NAMESPACE, "test");
        Behaviour baseBehaviour = new JavaBehaviour(this, "baseTest");
        policyComponent.bindPropertyBehaviour(policyName, BASE_TYPE, baseBehaviour);
        Behaviour folderBehaviour = new JavaBehaviour(this, "folderTest");
        policyComponent.bindPropertyBehaviour(policyName, FOLDER_TYPE, folderBehaviour);
        Behaviour folderDBehaviour = new JavaBehaviour(this, "folderDTest");
        policyComponent.bindPropertyBehaviour(policyName, FOLDER_TYPE, FOLDER_PROP_D, folderDBehaviour);
        Behaviour aspectBehaviour = new JavaBehaviour(this, "aspectTest");
        policyComponent.bindPropertyBehaviour(policyName, TEST_ASPECT, aspectBehaviour);
        
        // Invoke Policies        
        TestPropertyPolicy filePolicy = delegate.get(FILE_TYPE, BASE_PROP_A);
        String fileResult = filePolicy.test("file");
        assertEquals("Base: file", fileResult);
        TestPropertyPolicy folderPolicy = delegate.get(FOLDER_TYPE, FOLDER_PROP_D);
        String folderResult = folderPolicy.test("folder");
        assertEquals("FolderD: folder", folderResult);

        // Re-bind Behaviour
        Behaviour newBaseBehaviour = new JavaBehaviour(this, "newBaseTest");
        policyComponent.bindPropertyBehaviour(policyName, BASE_TYPE, newBaseBehaviour);

        // Re-invoke Policies
        TestPropertyPolicy filePolicy2 = delegate.get(FILE_TYPE, BASE_PROP_A);
        String fileResult2 = filePolicy2.test("file");
        assertEquals("NewBase: file", fileResult2);
        TestPropertyPolicy folderPolicy2 = delegate.get(FOLDER_TYPE, FOLDER_PROP_D);
        String folderResult2 = folderPolicy2.test("folder");
        assertEquals("FolderD: folder", folderResult2);
    }
    
    
    public void testAssociationDelegate()
    {
        // Register Policy
        AssociationPolicyDelegate<TestAssociationPolicy> delegate = policyComponent.registerAssociationPolicy(TestAssociationPolicy.class);
        
        // Bind Association Behaviour
        QName policyName = QName.createQName(TEST_NAMESPACE, "test");
        Behaviour baseBehaviour = new JavaBehaviour(this, "baseTest");
        policyComponent.bindAssociationBehaviour(policyName, BASE_TYPE, BASE_ASSOC_A, baseBehaviour);

        // Test single Policy delegate
        Collection<TestAssociationPolicy> filePolicies = delegate.getList(FILE_TYPE, BASE_ASSOC_A);
        assertNotNull(filePolicies);
        assertTrue(filePolicies.size() == 1);
        TestAssociationPolicy filePolicy = delegate.get(FILE_TYPE, BASE_ASSOC_A);
        assertNotNull(filePolicy);
        String fileResult = filePolicy.test("file");
        assertEquals("Base: file", fileResult);
        
        // Bind Service Behaviour
        Behaviour serviceBehaviour = new JavaBehaviour(this, "serviceTest");
        policyComponent.bindAssociationBehaviour(policyName, this, serviceBehaviour);

        // Test multi Policy delegate
        Collection<TestAssociationPolicy> file2Policies = delegate.getList(FILE_TYPE, BASE_ASSOC_A);
        assertNotNull(file2Policies);
        assertTrue(file2Policies.size() == 2);
        TestAssociationPolicy filePolicy2 = delegate.get(FILE_TYPE, BASE_ASSOC_A);
        assertNotNull(filePolicy2);
    }

    
    //
    // The following interfaces represents policies
    //
    
    public interface TestClassPolicy extends ClassPolicy
    {
        static String NAMESPACE = TEST_NAMESPACE;
        public String test(String argument);
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
    
    public String validTest(String argument)
    {
        return "ValidTest: " + argument;
    }
    
    public String baseTest(String argument)
    {
        return "Base: " + argument;
    }

    public String newBaseTest(String argument)
    {
        return "NewBase: " + argument;
    }
    
    public String fileTest(String argument)
    {
        return "File: " + argument;
    }
    
    public String folderTest(String argument)
    {
        return "Folder: " + argument;
    }

    public String aspectTest(String argument)
    {
        return "Aspect: " + argument;
    }
    
    public String folderDTest(String argument)
    {
        return "FolderD: " + argument;
    }

    public String serviceTest(String argument)
    {
        return "Service: " + argument;
    }
    
}
