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
package org.alfresco.repo.dictionary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.AbstractConstraint;
import org.alfresco.repo.dictionary.constraint.ConstraintRegistry;
import org.alfresco.repo.dictionary.constraint.RegexConstraint;
import org.alfresco.repo.dictionary.constraint.RegisteredConstraint;
import org.alfresco.repo.dictionary.constraint.StringLengthConstraint;
import org.alfresco.repo.dictionary.constraint.UserNameConstraint;
import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ThreadPoolExecutorFactoryBean;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;
import org.alfresco.util.testing.category.DBTests;
import org.junit.experimental.categories.Category;
import org.springframework.extensions.surf.util.I18NUtil;

@Category(DBTests.class)
public class RepoDictionaryDAOTest extends TestCase
{
    public static final String TEST_RESOURCE_MESSAGES = "alfresco/messages/dictionary-messages";

    private static final String TEST_URL = "http://www.alfresco.org/test/dictionarydaotest/1.0";
    private static final String TEST_MODEL = "org/alfresco/repo/dictionary/dictionarydaotest_model.xml";
    private static final String TEST_BUNDLE = "org/alfresco/repo/dictionary/dictionarydaotest_model";
    private DictionaryService service;
    
    
    @Override
    public void setUp() throws Exception
    {
        // Registered the required constraints
        ConstraintRegistry constraintRegistry = ConstraintRegistry.getInstance();
        AbstractConstraint constraintReg1 = new UserNameConstraint();
        constraintReg1.setShortName("cm:reg1");
        constraintReg1.setRegistry(constraintRegistry);
        constraintReg1.initialize();
        AbstractConstraint constraintReg2 = new UserNameConstraint();
        constraintReg2.setShortName("cm:reg2");
        constraintReg2.setRegistry(constraintRegistry);
        constraintReg2.initialize();
        
        // register resource bundles for messages
        I18NUtil.registerResourceBundle(TEST_RESOURCE_MESSAGES);
        
        // Instantiate Dictionary Service
        TenantService tenantService = new SingleTServiceImpl();

        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl();
        dictionaryDAO.setTenantService(tenantService);
        initDictionaryCaches(dictionaryDAO, tenantService);

        // Populate with appropriate models
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");
        bootstrapModels.add(TEST_MODEL);
        List<String> labels = new ArrayList<String>();
        labels.add(TEST_BUNDLE);
        bootstrap.setModels(bootstrapModels);
        bootstrap.setLabels(labels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.setTenantService(tenantService);
        bootstrap.bootstrap();
        
        DictionaryComponent component = new DictionaryComponent();
        component.setDictionaryDAO(dictionaryDAO);
        component.setMessageLookup(new StaticMessageLookup());
        service = component;
    }
    
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

    public void testBootstrap() throws Exception 
    {
        TenantService tenantService = new SingleTServiceImpl();

        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl();
        dictionaryDAO.setTenantService(tenantService);
        initDictionaryCaches(dictionaryDAO, tenantService);
        
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");
        bootstrapModels.add("alfresco/model/systemModel.xml");
        bootstrapModels.add("alfresco/model/contentModel.xml");
        bootstrapModels.add("alfresco/model/applicationModel.xml");
        
        bootstrapModels.add("org/alfresco/repo/security/authentication/userModel.xml");
        bootstrapModels.add("org/alfresco/repo/action/actionModel.xml");
        bootstrapModels.add("org/alfresco/repo/rule/ruleModel.xml");
        bootstrapModels.add("org/alfresco/repo/version/version_model.xml");
        
        bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.setTenantService(tenantService);
        bootstrap.bootstrap();
    }


    public void testLabels()
    {
        QName model = QName.createQName(TEST_URL, "dictionarydaotest");
        ModelDefinition modelDef = service.getModel(model);
        assertEquals("Model Description", modelDef.getDescription(service));
        QName type = QName.createQName(TEST_URL, "base");
        TypeDefinition typeDef = service.getType(type);
        assertEquals("Base Title", typeDef.getTitle(service));
        assertEquals("Base Description", typeDef.getDescription(service));
        QName prop = QName.createQName(TEST_URL, "prop1");
        PropertyDefinition propDef = service.getProperty(prop);
        assertEquals("Prop1 Title", propDef.getTitle(service));
        assertEquals("Prop1 Description", propDef.getDescription(service));
        QName assoc = QName.createQName(TEST_URL, "assoc1");
        AssociationDefinition assocDef = service.getAssociation(assoc);
        assertEquals("Assoc1 Title", assocDef.getTitle(service));
        assertEquals("Assoc1 Description", assocDef.getDescription(service));
    }
    
    public void testConstraints()
    {
        // Check that the registered constraints are correct
        assertNotNull("Constraint reg1 not registered", ConstraintRegistry.getInstance().getConstraint("cm:reg1"));
        assertNotNull("Constraint reg2 not registered", ConstraintRegistry.getInstance().getConstraint("cm:reg2"));
        
        QName model = QName.createQName(TEST_URL, "dictionarydaotest");
        Collection<ConstraintDefinition> modelConstraints = service.getConstraints(model);
        assertEquals(23, modelConstraints.size()); // 10 + 7 + 6
        
        QName conRegExp1QName = QName.createQName(TEST_URL, "regex1");
        boolean found1 = false;
        
        QName conStrLen1QName = QName.createQName(TEST_URL, "stringLength1");
        boolean found2 = false;
        
        for (ConstraintDefinition constraintDef : modelConstraints)
        {
            if (constraintDef.getName().equals(conRegExp1QName))
            {
                assertEquals("Regex1 title", constraintDef.getTitle(service));
                assertEquals("Regex1 description", constraintDef.getDescription(service));
                found1 = true;
            }
            
            if (constraintDef.getName().equals(conStrLen1QName))
            {
                assertNull(constraintDef.getTitle(service));
                assertNull(constraintDef.getDescription(service));
                found2 = true;
            }
        }
        assertTrue(found1);
        assertTrue(found2);
        
        // get the constraints for a property without constraints
        QName propNoConstraintsQName = QName.createQName(TEST_URL, "fileprop");
        PropertyDefinition propNoConstraintsDef = service.getProperty(propNoConstraintsQName);
        assertNotNull("Property without constraints returned null list", propNoConstraintsDef.getConstraints());
        
        // get the constraints defined for the property
        QName prop1QName = QName.createQName(TEST_URL, "prop1");
        PropertyDefinition propDef = service.getProperty(prop1QName);
        List<ConstraintDefinition> constraints = propDef.getConstraints();
        assertNotNull("Null constraints list", constraints);
        assertEquals("Incorrect number of constraints", 3, constraints.size());
        assertTrue("Constraint instance incorrect", constraints.get(0).getConstraint() instanceof RegexConstraint);
        assertTrue("Constraint instance incorrect", constraints.get(1).getConstraint() instanceof StringLengthConstraint);
        assertTrue("Constraint instance incorrect", constraints.get(2).getConstraint() instanceof RegisteredConstraint);
        
        // check the individual constraints
        ConstraintDefinition constraintDef = constraints.get(0);
        assertTrue("Constraint anonymous name incorrect", constraintDef.getName().getLocalName().equals("dictionarydaotest_base_prop1_anon_0"));
        
        // inherit title / description for reference constraint
        assertTrue("Constraint title incorrect", constraintDef.getTitle(service).equals("Regex1 title"));
        assertTrue("Constraint description incorrect", constraintDef.getDescription(service).equals("Regex1 description"));
        
        constraintDef = constraints.get(1);
        assertTrue("Constraint anonymous name incorrect", constraintDef.getName().getLocalName().equals("dictionarydaotest_base_prop1_anon_1"));
        
        assertTrue("Constraint title incorrect", constraintDef.getTitle(service).equals("Prop1 Strlen1 title"));
        assertTrue("Constraint description incorrect", constraintDef.getDescription(service).equals("Prop1 Strlen1 description"));
        
        // check that the constraint implementation is valid (it used a reference)
        Constraint constraint = constraintDef.getConstraint();
        assertNotNull("Reference constraint has no implementation", constraint);
    }
    
    public void testConstraintsOverrideInheritance()
    {
        QName baseQName = QName.createQName(TEST_URL, "base");
        QName fileQName = QName.createQName(TEST_URL, "file");
        QName folderQName = QName.createQName(TEST_URL, "folder");
        QName prop1QName = QName.createQName(TEST_URL, "prop1");

        // get the base property
        PropertyDefinition prop1Def = service.getProperty(baseQName, prop1QName);
        assertNotNull(prop1Def);
        List<ConstraintDefinition> prop1Constraints = prop1Def.getConstraints();
        assertEquals("Incorrect number of constraints", 3, prop1Constraints.size());
        assertTrue("Constraint instance incorrect", prop1Constraints.get(0).getConstraint() instanceof RegexConstraint);
        assertTrue("Constraint instance incorrect", prop1Constraints.get(1).getConstraint() instanceof StringLengthConstraint);
        assertTrue("Constraint instance incorrect", prop1Constraints.get(2).getConstraint() instanceof RegisteredConstraint);

        // check the inherited property on folder (must be same as above)
        prop1Def = service.getProperty(folderQName, prop1QName);
        assertNotNull(prop1Def);
        prop1Constraints = prop1Def.getConstraints();
        assertEquals("Incorrect number of constraints", 3, prop1Constraints.size());
        assertTrue("Constraint instance incorrect", prop1Constraints.get(0).getConstraint() instanceof RegexConstraint);
        assertTrue("Constraint instance incorrect", prop1Constraints.get(1).getConstraint() instanceof StringLengthConstraint);
        assertTrue("Constraint instance incorrect", prop1Constraints.get(2).getConstraint() instanceof RegisteredConstraint);

        // check the overridden property on file (must be reverse of above)
        prop1Def = service.getProperty(fileQName, prop1QName);
        assertNotNull(prop1Def);
        prop1Constraints = prop1Def.getConstraints();
        assertEquals("Incorrect number of constraints", 3, prop1Constraints.size());
        assertTrue("Constraint instance incorrect", prop1Constraints.get(0).getConstraint() instanceof StringLengthConstraint);
        assertTrue("Constraint instance incorrect", prop1Constraints.get(1).getConstraint() instanceof RegexConstraint);
        assertTrue("Constraint instance incorrect", prop1Constraints.get(2).getConstraint() instanceof RegisteredConstraint);
    }

    public void testArchive()
    {
        QName testFileQName = QName.createQName(TEST_URL, "file");
        ClassDefinition fileClassDef = service.getClass(testFileQName);
        assertTrue("File type should have the archive flag", fileClassDef.getArchive());

        QName testFileDerivedQName = QName.createQName(TEST_URL, "file-derived");
        ClassDefinition fileDerivedClassDef = service.getClass(testFileDerivedQName);
        assertTrue("Direct derived File type should have the archive flag", fileDerivedClassDef.getArchive());

        QName testFileDerivedNoArchiveQName = QName.createQName(TEST_URL, "file-derived-no-archive");
        ClassDefinition fileDerivedNoArchiveClassDef = service.getClass(testFileDerivedNoArchiveQName);
        assertFalse("Derived File with archive override type should NOT have the archive flag",
                fileDerivedNoArchiveClassDef.getArchive());

        QName testFolderQName = QName.createQName(TEST_URL, "folder");
        ClassDefinition folderClassDef = service.getClass(testFolderQName);
        assertNull("Folder type should not have the archive flag", folderClassDef.getArchive());
    }
    
    public void testMandatoryEnforced()
    {
        // get the properties for the test type
        QName testEnforcedQName = QName.createQName(TEST_URL, "enforced");
        ClassDefinition testEnforcedClassDef = service.getClass(testEnforcedQName);
        Map<QName, PropertyDefinition> testEnforcedPropertyDefs = testEnforcedClassDef.getProperties();
        
        PropertyDefinition propertyDef = null;

        QName testMandatoryEnforcedQName = QName.createQName(TEST_URL, "mandatory-enforced");
        propertyDef = testEnforcedPropertyDefs.get(testMandatoryEnforcedQName);
        assertNotNull("Property not found: " + testMandatoryEnforcedQName,
                propertyDef);
        assertTrue("Expected property to be mandatory: " + testMandatoryEnforcedQName,
                propertyDef.isMandatory());
        assertTrue("Expected property to be mandatory-enforced: " + testMandatoryEnforcedQName,
                propertyDef.isMandatoryEnforced());

        QName testMandatoryNotEnforcedQName = QName.createQName(TEST_URL, "mandatory-not-enforced");
        propertyDef = testEnforcedPropertyDefs.get(testMandatoryNotEnforcedQName);
        assertNotNull("Property not found: " + testMandatoryNotEnforcedQName,
                propertyDef);
        assertTrue("Expected property to be mandatory: " + testMandatoryNotEnforcedQName,
                propertyDef.isMandatory());
        assertFalse("Expected property to be mandatory-not-enforced: " + testMandatoryNotEnforcedQName,
                propertyDef.isMandatoryEnforced());

        QName testMandatoryDefaultEnforcedQName = QName.createQName(TEST_URL, "mandatory-default-enforced");
        propertyDef = testEnforcedPropertyDefs.get(testMandatoryDefaultEnforcedQName);
        assertNotNull("Property not found: " + testMandatoryDefaultEnforcedQName,
                propertyDef);
        assertTrue("Expected property to be mandatory: " + testMandatoryDefaultEnforcedQName,
                propertyDef.isMandatory());
        assertFalse("Expected property to be mandatory-not-enforced: " + testMandatoryDefaultEnforcedQName,
                propertyDef.isMandatoryEnforced());
    }
    
    public void testSubClassOf()
    {
        QName invalid = QName.createQName(TEST_URL, "invalid");
        QName base = QName.createQName(TEST_URL, "base");
        QName file = QName.createQName(TEST_URL, "file");
        QName folder = QName.createQName(TEST_URL, "folder");
        QName referenceable = QName.createQName(TEST_URL, "referenceable");

        // Test invalid args
        boolean testI1 = service.isSubClass(invalid, referenceable);
        
        assertFalse(testI1);
        
        boolean testI2 = service.isSubClass(referenceable, invalid);
        assertFalse(testI2);
        
        boolean testI3 = service.isSubClass(invalid, invalid);
        assertFalse(testI3);

        // Test various flavours of subclassof
        boolean test1 = service.isSubClass(file, referenceable);  // type vs aspect
        assertFalse(test1);
        boolean test2 = service.isSubClass(file, folder);   // seperate hierarchies
        assertFalse(test2);
        boolean test3 = service.isSubClass(file, file);   // self
        assertTrue(test3);
        boolean test4 = service.isSubClass(folder, base);  // subclass
        assertTrue(test4);
        boolean test5 = service.isSubClass(base, folder);  // reversed test
        assertFalse(test5);
    }
    

    public void testPropertyOverride()
    {
        TypeDefinition type1 = service.getType(QName.createQName(TEST_URL, "overridetype1"));
        Map<QName, PropertyDefinition> props1 = type1.getProperties();
        PropertyDefinition prop1 = props1.get(QName.createQName(TEST_URL, "propoverride"));
        String def1 = prop1.getDefaultValue();
        assertEquals("one", def1);
        
        TypeDefinition type2 = service.getType(QName.createQName(TEST_URL, "overridetype2"));
        Map<QName, PropertyDefinition> props2 = type2.getProperties();
        PropertyDefinition prop2 = props2.get(QName.createQName(TEST_URL, "propoverride"));
        String def2 = prop2.getDefaultValue();
        assertEquals("two", def2);

        TypeDefinition type3 = service.getType(QName.createQName(TEST_URL, "overridetype3"));
        Map<QName, PropertyDefinition> props3 = type3.getProperties();
        PropertyDefinition prop3 = props3.get(QName.createQName(TEST_URL, "propoverride"));
        String def3 = prop3.getDefaultValue();
        assertEquals("three", def3);
    }

    public void testChildAssocPropagate()
    {
        // Check the default value
        AssociationDefinition assocDef = service.getAssociation(QName.createQName(TEST_URL, "childassoc1"));
        assertNotNull("No such child association found", assocDef);
        assertTrue("Expected a child association", assocDef instanceof ChildAssociationDefinition);
        ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
        assertFalse("Expected 'false' for default timestamp propagation", childAssocDef.getPropagateTimestamps());

        // Check the explicit value
        assocDef = service.getAssociation(QName.createQName(TEST_URL, "childassocPropagate"));
        assertNotNull("No such child association found", assocDef);
        assertTrue("Expected a child association", assocDef instanceof ChildAssociationDefinition);
        childAssocDef = (ChildAssociationDefinition) assocDef;
        assertTrue("Expected 'true' for timestamp propagation", childAssocDef.getPropagateTimestamps());
    }

    public void testADB159() throws Exception
    {
        // source dictionary
        TenantService tenantService = new SingleTServiceImpl();   
        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl();
        dictionaryDAO.setTenantService(tenantService);
        initDictionaryCaches(dictionaryDAO, tenantService);

        // destination dictionary
        DictionaryDAOImpl dictionaryDAO2 = new DictionaryDAOImpl();
        dictionaryDAO2.setTenantService(tenantService);
        initDictionaryCaches(dictionaryDAO2, tenantService);

        List<String> models = new ArrayList<String>();
        models.add("alfresco/model/dictionaryModel.xml");
        models.add("alfresco/model/systemModel.xml");
        models.add("alfresco/model/contentModel.xml");
        models.add("alfresco/model/applicationModel.xml");
        models.add("org/alfresco/repo/security/authentication/userModel.xml");
        models.add("org/alfresco/repo/action/actionModel.xml");
        models.add("org/alfresco/repo/rule/ruleModel.xml");
        models.add("org/alfresco/repo/version/version_model.xml");
        
        // round-trip default models
        for (String bootstrapModel : models)
        {
            InputStream modelStream = getClass().getClassLoader().getResourceAsStream(bootstrapModel);
            if (modelStream == null)
            {
                throw new DictionaryException("Could not find bootstrap model " + bootstrapModel);
            }
            try
            {
                // parse model from xml
                M2Model model = M2Model.createModel(modelStream);
                dictionaryDAO.putModel(model);
                
                // regenerate xml from model
                ByteArrayOutputStream xml1 = new ByteArrayOutputStream();
                model.toXML(xml1);
                
                // register regenerated xml with other dictionary
                M2Model model2 = M2Model.createModel(new ByteArrayInputStream(xml1.toByteArray()));
                dictionaryDAO2.putModel(model2);
            }
            catch(DictionaryException e)
            {
                throw new DictionaryException("Could not import bootstrap model " + bootstrapModel, e);
            }
        }
        
        // specific test case
        M2Model model = M2Model.createModel("test:adb25");
        model.createNamespace(TEST_URL, "test");
        model.createImport(NamespaceService.DICTIONARY_MODEL_1_0_URI, NamespaceService.DICTIONARY_MODEL_PREFIX);
        model.createImport(NamespaceService.SYSTEM_MODEL_1_0_URI, NamespaceService.SYSTEM_MODEL_PREFIX);
        model.createImport(NamespaceService.CONTENT_MODEL_1_0_URI, NamespaceService.CONTENT_MODEL_PREFIX);

        M2Type testType = model.createType("test:adb25" );
        testType.setParentName("cm:" + ContentModel.TYPE_CONTENT.getLocalName());

        M2Property prop1 = testType.createProperty("test:prop1");
        prop1.setMandatory(false);
        prop1.setType("d:" + DataTypeDefinition.TEXT.getLocalName());
        prop1.setMultiValued(false);

        ByteArrayOutputStream xml1 = new ByteArrayOutputStream();
        model.toXML(xml1); 
    }

}
