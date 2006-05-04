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
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.dictionary.constraint.RegexConstraint;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;


public class DictionaryDAOTest extends TestCase
{
    public static final String TEST_RESOURCE_MESSAGES = "alfresco/messages/dictionary-messages";

    private static final String TEST_URL = "http://www.alfresco.org/test/dictionarydaotest/1.0";
    private static final String TEST_MODEL = "org/alfresco/repo/dictionary/dictionarydaotest_model.xml";
    private static final String TEST_BUNDLE = "org/alfresco/repo/dictionary/dictionarydaotest_model";
    private DictionaryService service;
    
    
    @Override
    public void setUp()
    {
        // register resource bundles for messages
        I18NUtil.registerResourceBundle(TEST_RESOURCE_MESSAGES);
        
        // Instantiate Dictionary Service
        NamespaceDAO namespaceDAO = new NamespaceDAOImpl();
        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl(namespaceDAO);

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
        bootstrap.bootstrap();
        
        DictionaryComponent component = new DictionaryComponent();
        component.setDictionaryDAO(dictionaryDAO);
        service = component;
    }
    

    public void testBootstrap()
    {
        NamespaceDAO namespaceDAO = new NamespaceDAOImpl();
        DictionaryDAOImpl dictionaryDAO = new DictionaryDAOImpl(namespaceDAO);
        
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
        bootstrap.bootstrap();        
    }


    public void testLabels()
    {
        QName model = QName.createQName(TEST_URL, "dictionarydaotest");
        ModelDefinition modelDef = service.getModel(model);
        assertEquals("Model Description", modelDef.getDescription());
        QName type = QName.createQName(TEST_URL, "base");
        TypeDefinition typeDef = service.getType(type);
        assertEquals("Base Title", typeDef.getTitle());
        assertEquals("Base Description", typeDef.getDescription());
        QName prop = QName.createQName(TEST_URL, "prop1");
        PropertyDefinition propDef = service.getProperty(prop);
        assertEquals("Prop1 Title", propDef.getTitle());
        assertEquals("Prop1 Description", propDef.getDescription());
        QName assoc = QName.createQName(TEST_URL, "assoc1");
        AssociationDefinition assocDef = service.getAssociation(assoc);
        assertEquals("Assoc1 Title", assocDef.getTitle());
        assertEquals("Assoc1 Description", assocDef.getDescription());
        QName datatype = QName.createQName(TEST_URL, "datatype");
        DataTypeDefinition datatypeDef = service.getDataType(datatype);
        assertEquals("Datatype Analyser", datatypeDef.getAnalyserClassName());
    }
    
    public void testConstraints()
    {
        // get the constraints for a property without constraints
        QName propNoConstraintsQName = QName.createQName(TEST_URL, "fileprop");
        PropertyDefinition propNoConstraintsDef = service.getProperty(propNoConstraintsQName);
        assertNotNull("Property without constraints returned null list", propNoConstraintsDef.getConstraints());
        
        // get the constraints defined for the property
        QName prop1QName = QName.createQName(TEST_URL, "prop1");
        PropertyDefinition propDef = service.getProperty(prop1QName);
        List<ConstraintDefinition> constraints = propDef.getConstraints();
        assertNotNull("Null constraints list", constraints);
        assertEquals("Incorrect number of constraints", 2, constraints.size());
        
        // check the individual constraints
        ConstraintDefinition constraintDef = constraints.get(0);
        assertTrue("Constraint anonymous name incorrect", constraintDef.getName().getLocalName().startsWith("prop1_anon"));
        // check that the constraint implementation is valid (it used a reference)
        Constraint constraint = constraintDef.getConstraint();
        assertNotNull("Reference constraint has no implementation", constraint);
        
        // make sure it is the correct type of constraint
        assertTrue("Expected type REGEX constraint", constraint instanceof RegexConstraint);
    }

    public void testArchive()
    {
        QName testFileQName = QName.createQName(TEST_URL, "file");
        ClassDefinition fileClassDef = service.getClass(testFileQName);
        assertTrue("File type should have the archive flag", fileClassDef.isArchive());

        QName testFolderQName = QName.createQName(TEST_URL, "folder");
        ClassDefinition folderClassDef = service.getClass(testFolderQName);
        assertFalse("Folder type should not have the archive flag", folderClassDef.isArchive());
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
        try
        {
            service.isSubClass(invalid, referenceable);
            fail("Failed to catch invalid class parameter");
        }
        catch(InvalidTypeException e) {}

        try
        {
            service.isSubClass(referenceable, invalid);
            fail("Failed to catch invalid class parameter");
        }
        catch(InvalidTypeException e) {}

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
    
    
}
