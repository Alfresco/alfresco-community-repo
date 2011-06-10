package org.alfresco.repo.node.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;

public class EncryptedPropertiesTest extends BaseSpringTest
{
    private static String NAMESPACE = "http://www.alfresco.org/test/encryptiontest/1.0";
    private static QName PROP1_TYPE = QName.createQName(NAMESPACE, "prop1");
    private static QName PROP2_TYPE = QName.createQName(NAMESPACE, "prop2");
    private static QName ASPECT_PROP1_TYPE = QName.createQName(NAMESPACE, "aspectprop1");
    private static QName ASPECT_PROP2_TYPE = QName.createQName(NAMESPACE, "aspectprop2");
    private static QName ENCRYPTIONTEST_TYPE = QName.createQName(NAMESPACE, "testtype");

    private DictionaryDAO dictionaryDAO;
    private NodeService nodeService;
    
    private NodeRef rootNodeRef;

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();

        dictionaryDAO = (DictionaryDAO) applicationContext.getBean("dictionaryDAO");

        nodeService = getNodeService();

        // Create the test model
        createTestModel();
        
        // create a first store directly
        StoreRef storeRef = nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE,
                "Test_" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
    }
    
    protected NodeService getNodeService()
    {
        // Force cascading
        DbNodeServiceImpl dbNodeServiceImpl = (DbNodeServiceImpl) applicationContext.getBean("dbNodeServiceImpl");
        
        return (NodeService) applicationContext.getBean("dbNodeService");
    }
    
    private void createTestModel()
    {
        // register the test model
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("org/alfresco/repo/node/db/encrypted_properties_test_model.xml");
        
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        
        bootstrap.bootstrap();
    }
    
    public void testEncryptedProperties()
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(PROP1_TYPE, "test string value");

        MLText mlTextProperty = new MLText();
        mlTextProperty.addValue(Locale.ENGLISH, "Very good!");
        mlTextProperty.addValue(Locale.FRENCH, "Très bon!");
        mlTextProperty.addValue(Locale.GERMAN, "Sehr gut!");
        properties.put(PROP2_TYPE, mlTextProperty);

        NodeRef test1 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NAMESPACE, "n1"),
                ENCRYPTIONTEST_TYPE,
                properties).getChildRef();
        
        NodeRef test2 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NAMESPACE, "n2"),
                ContentModel.TYPE_CONTENT,
                null).getChildRef();

        properties = new HashMap<QName, Serializable>();
        properties.put(ASPECT_PROP1_TYPE, "test string value");

        mlTextProperty = new MLText();
        mlTextProperty.addValue(Locale.ENGLISH, "Very good!");
        mlTextProperty.addValue(Locale.FRENCH, "Très bon!");
        mlTextProperty.addValue(Locale.GERMAN, "Sehr gut!");
        properties.put(ASPECT_PROP2_TYPE, mlTextProperty);
        nodeService.addAspect(test2, QName.createQName(NAMESPACE, "testaspect"), properties);

        String prop1 = (String)nodeService.getProperty(test1, PROP1_TYPE);
        assertEquals("test string value", prop1);

        MLText prop2 = (MLText)nodeService.getProperty(test1, PROP2_TYPE);
        assertEquals("Very good!", prop2.getValue(Locale.ENGLISH));
        assertEquals("Très bon!", prop2.getValue(Locale.FRENCH));
        assertEquals("Sehr gut!", prop2.getValue(Locale.GERMAN));

        String aspectprop1 = (String)nodeService.getProperty(test2, ASPECT_PROP1_TYPE);
        assertEquals("test string value", aspectprop1);
        
        MLText aspectprop2 = (MLText)nodeService.getProperty(test2, ASPECT_PROP2_TYPE);
        assertEquals("Very good!", aspectprop2.getValue(Locale.ENGLISH));
        assertEquals("Très bon!", aspectprop2.getValue(Locale.FRENCH));
        assertEquals("Sehr gut!", aspectprop2.getValue(Locale.GERMAN));
    }
    
//    protected void createNodes()
//    {
//        // create 1000 nodes with a single (non-encrypted) string property
//        for(int i = 0; i < 2000; i++)
//        {
//            Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
//            properties.put(ContentModel.PROP_NAME, "encryption test name");
//            NodeRef test1 = nodeService.createNode(
//                    rootNodeRef,
//                    ContentModel.ASSOC_CHILDREN,
//                    QName.createQName(NAMESPACE, "n" + i),
//                    ContentModel.TYPE_CONTENT,
//                    properties).getChildRef();
//        
//        }
//    }
    
//    protected void createEnryptedPropertyNodes()
//    {
//        // create 1000 nodes with a single encrypted string attribute
//        for(int i = 0; i < 2000; i++)
//        {
//            Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
//            properties.put(PROP1_TYPE, "test string value");
//            NodeRef test1 = nodeService.createNode(
//                    rootNodeRef,
//                    ContentModel.ASSOC_CHILDREN,
//                    QName.createQName(NAMESPACE, "n" + i),
//                    ENCRYPTIONTEST_TYPE,
//                    properties).getChildRef();
//        
//        }
//    }
    
//    public void testEncryptedPropertiesSpeed()
//    {
//        // warm up
//        createNodes();
//        createEnryptedPropertyNodes();
//
//        // time
//        long start = System.currentTimeMillis();
//        createEnryptedPropertyNodes();
//        long end = System.currentTimeMillis();
//        System.out.println("Encrypted property 1000 nodes in " + (end - start) + "ms");
//        
//        start = System.currentTimeMillis();
//        createNodes();
//        end = System.currentTimeMillis();
//        System.out.println("Non-encrypted property 1000 nodes in " + (end - start) + "ms");
//    }
}
