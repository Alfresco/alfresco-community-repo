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

import junit.framework.TestCase;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;


public class DictionaryDAOTest extends TestCase
{
    
    private static final String TEST_MODEL = "org/alfresco/repo/dictionary/dictionarydaotest_model.xml";
    private static final String TEST_BUNDLE = "org/alfresco/repo/dictionary/dictionarydaotest_model";
    private DictionaryService service; 
    
    
    @Override
    public void setUp()
    {
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
        QName model = QName.createQName("http://www.alfresco.org/test/dictionarydaotest/1.0", "dictionarydaotest");
        ModelDefinition modelDef = service.getModel(model);
        assertEquals("Model Description", modelDef.getDescription());
        QName type = QName.createQName("http://www.alfresco.org/test/dictionarydaotest/1.0", "base");
        TypeDefinition typeDef = service.getType(type);
        assertEquals("Base Title", typeDef.getTitle());
        assertEquals("Base Description", typeDef.getDescription());
        QName prop = QName.createQName("http://www.alfresco.org/test/dictionarydaotest/1.0", "prop1");
        PropertyDefinition propDef = service.getProperty(prop);
        assertEquals("Prop1 Title", propDef.getTitle());
        assertEquals("Prop1 Description", propDef.getDescription());
        QName assoc = QName.createQName("http://www.alfresco.org/test/dictionarydaotest/1.0", "assoc1");
        AssociationDefinition assocDef = service.getAssociation(assoc);
        assertEquals("Assoc1 Title", assocDef.getTitle());
        assertEquals("Assoc1 Description", assocDef.getDescription());
        QName datatype = QName.createQName("http://www.alfresco.org/test/dictionarydaotest/1.0", "datatype");
        DataTypeDefinition datatypeDef = service.getDataType(datatype);
        assertEquals("Datatype Analyser", datatypeDef.getAnalyserClassName());
    }
    
    
    public void testSubClassOf()
    {
        QName invalid = QName.createQName("http://www.alfresco.org/test/dictionarydaotest/1.0", "invalid");
        QName base = QName.createQName("http://www.alfresco.org/test/dictionarydaotest/1.0", "base");
        QName file = QName.createQName("http://www.alfresco.org/test/dictionarydaotest/1.0", "file");
        QName folder = QName.createQName("http://www.alfresco.org/test/dictionarydaotest/1.0", "folder");
        QName referenceable = QName.createQName("http://www.alfresco.org/test/dictionarydaotest/1.0", "referenceable");

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
