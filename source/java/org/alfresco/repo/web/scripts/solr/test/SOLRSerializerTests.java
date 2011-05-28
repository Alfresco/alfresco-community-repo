package org.alfresco.repo.web.scripts.solr.test;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.SOLRDeserializer;
import org.alfresco.util.SOLRSerializer;

public class SOLRSerializerTests extends BaseWebScriptTest
{
    static final String SOLR_TEST_MODEL_1_0_URI = "http://www.alfresco.org/model/solrtest/1.0";
    static final QName TYPE_TEST_OBJECT = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "testobject");
    static final QName PROP_MLTEXT = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "mlTextProp");
    static final QName PROP_BOOL = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "boolProp");
    static final QName PROP_LONG = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "longProp");
    static final QName PROP_FLOAT = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "floatProp");
    static final QName PROP_DOUBLE = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "doubleProp");
    static final QName PROP_DATE = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "dateProp");
    static final QName PROP_DATETIME = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "dateTimeProp");
    static final QName PROP_QNAME = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "qnameProp");
    static final QName PROP_NODEREF = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "nodeRefProp");
    static final QName PROP_CHILDASSOC = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "childAssocProp");
    static final QName PROP_ASSOC = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "assocProp");
    static final QName PROP_PATH = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "pathProp");
    static final QName PROP_CATEGORY = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "categoryProp");
    static final QName PROP_LOCALE = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "localeProp");
    static final QName PROP_PERIOD = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "periodProp"); 
    static final QName PROP_ANY = QName.createQName(SOLR_TEST_MODEL_1_0_URI, "anyProp");
    
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private ContentService contentService;
    private DictionaryService dictionaryService;
    private RepoAdminService repoAdminService;
    
    private SOLRSerializer solrSerializer;
    private SOLRDeserializer solrDeserializer;
    
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) getServer().getApplicationContext().getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        fileFolderService = serviceRegistry.getFileFolderService();
        contentService = serviceRegistry.getContentService();
        nodeService = serviceRegistry.getNodeService();
        dictionaryService = serviceRegistry.getDictionaryService();
        authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        repoAdminService = (RepoAdminService)getServer().getApplicationContext().getBean("repoAdminService");

        solrSerializer = (SOLRSerializer)getServer().getApplicationContext().getBean("solrSerializer");
        solrDeserializer = new SOLRDeserializer(dictionaryService);
        
        authenticationComponent.setSystemUserAsCurrentUser();

        InputStream modelStream = getClass().getClassLoader().getResourceAsStream("solr/solr-test-model.xml");
        repoAdminService.deployModel(modelStream, "solr-test-model");
        
        storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);
    }
    
    private NodeRef solrNode;
    private Date date;
    private MLText mlText;
    private ChildAssociationRef childAssoc;
    private AssociationRef assoc;
    private List<NodeRef> multiCategory;
    private NodeRef category;
    
    private static String[] mlOrderable_en = new String[] { "AAAA BBBB", "EEEE FFFF", "II", "KK", "MM", "OO", "QQ", "SS", "UU", "AA", "CC" };

    private static String[] mlOrderable_fr = new String[] { "CCCC DDDD", "GGGG HHHH", "JJ", "LL", "NN", "PP", "RR", "TT", "VV", "BB", "DD" };

    private MLText makeMLText()
    {
        return makeMLText(0);
    }

    private MLText makeMLText(int position)
    {
        MLText ml = new MLText();
        ml.addValue(Locale.ENGLISH, mlOrderable_en[position]);
        ml.addValue(Locale.FRENCH, mlOrderable_fr[position]);
        return ml;
    }

    private void buildTransaction()
    {
        PropertyMap props = new PropertyMap();
        props.put(ContentModel.PROP_NAME, "Container1");
        NodeRef container = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_FOLDER,
                props).getChildRef();

        FileInfo contentInfo = fileFolderService.create(container, "SolrNode", TYPE_TEST_OBJECT);
        solrNode = contentInfo.getNodeRef();
        ContentWriter writer = contentService.getWriter(solrNode, ContentModel.PROP_CONTENT, true);
        writer.putContent("Some Content");

        date = new Date();
        mlText = makeMLText();
        childAssoc = new ChildAssociationRef(ContentModel.ASSOC_CHILDREN,
                new NodeRef("testProtocol", "testID", "abcde"),
                QName.createQName("testProtocol", "testID"),
                new NodeRef("testProtocol", "testID", "xyz"));
        assoc = new AssociationRef(
                new NodeRef("testProtocol", "testID", "abcde"),
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "parts"),
                new NodeRef("testProtocol", "testID", "xyz"));

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(PROP_BOOL, Boolean.TRUE);
        properties.put(PROP_LONG, Long.valueOf(42));
        properties.put(PROP_FLOAT, Float.valueOf(42.0f));
        properties.put(PROP_DOUBLE, Double.valueOf(42.0));
        properties.put(PROP_DATE, date);
        properties.put(PROP_DATETIME, date);
        properties.put(PROP_NODEREF, container);
        properties.put(PROP_LOCALE, Locale.ITALY);
        properties.put(PROP_QNAME, PROP_QNAME);
        //properties.put(PROP_VERSION, new VersionNumber("1.0"));
        properties.put(PROP_PERIOD, new Period("period|12"));
        Path path = new Path();
        Path.Element element0 = new Path.ChildAssocElement(new ChildAssociationRef(null, null, null, new NodeRef("testProtocol", "testID", "abcde")));
        path.prepend(element0);
        properties.put(PROP_PATH, path);
        properties.put(PROP_ASSOC, assoc);
        category = new NodeRef("testProtocol", "testID", "cat1");
        properties.put(PROP_CATEGORY, (Serializable)category);
        properties.put(PROP_CHILDASSOC, childAssoc);
        properties.put(PROP_MLTEXT, mlText);

        nodeService.setProperties(solrNode, properties);
    }

    public void testAll()
    {
        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                buildTransaction();

                Serializable value = nodeService.getProperty(solrNode, ContentModel.PROP_NAME);
                Object serialized = solrSerializer.serialize(ContentModel.PROP_NAME, value);
                Serializable deserialized = solrDeserializer.deserialize(ContentModel.PROP_NAME, serialized);
                assertEquals(value, deserialized);

                value = nodeService.getProperty(solrNode, PROP_MLTEXT);
                serialized = solrSerializer.serialize(PROP_MLTEXT, value);
                deserialized = solrDeserializer.deserialize(PROP_MLTEXT, serialized);
                assertEquals(value, deserialized);

                value = nodeService.getProperty(solrNode, ContentModel.PROP_CONTENT);
                assertTrue("Expected ContentDataId, got " + value.getClass().getName(), value instanceof ContentDataWithId);
                serialized = solrSerializer.serialize(ContentModel.PROP_CONTENT, value);
                deserialized = solrDeserializer.deserialize(ContentModel.PROP_CONTENT, serialized);
                assertEquals(value, deserialized);
                
                value = nodeService.getProperty(solrNode, PROP_BOOL);
                serialized = solrSerializer.serialize(PROP_BOOL, value);
                deserialized = solrDeserializer.deserialize(PROP_BOOL, serialized);
                assertEquals(value, deserialized);
                
                value = nodeService.getProperty(solrNode, PROP_DATE);
                assertTrue("Expected Date object, got " + value.getClass().getName(), value instanceof Date);
                serialized = solrSerializer.serialize(PROP_DATE, value);
                deserialized = solrDeserializer.deserialize(PROP_DATE, serialized);
                assertEquals(value, deserialized);
                
                value = nodeService.getProperty(solrNode, PROP_DATETIME);
                assertTrue("Expected Date object, got " + value.getClass().getName(), value instanceof Date);
                serialized = solrSerializer.serialize(PROP_DATETIME, value);
                deserialized = solrDeserializer.deserialize(PROP_DATETIME, serialized);
                assertEquals(value, deserialized);
                
                value = nodeService.getProperty(solrNode, PROP_DOUBLE);
                assertTrue("Expected Double object, got " + value.getClass().getName(), value instanceof Double);
                serialized = solrSerializer.serialize(PROP_DATETIME, value);
                deserialized = solrDeserializer.deserialize(PROP_DATETIME, serialized);
                assertEquals(value, deserialized);
                
                value = nodeService.getProperty(solrNode, PROP_FLOAT);
                assertTrue("Expected Float object, got " + value.getClass().getName(), value instanceof Float);
                serialized = solrSerializer.serialize(PROP_FLOAT, value);
                deserialized = solrDeserializer.deserialize(PROP_FLOAT, serialized);
                assertEquals(value, deserialized);
                
                value = nodeService.getProperty(solrNode, PROP_LONG);
                assertTrue("Expected Long object, got " + value.getClass().getName(), value instanceof Long);
                serialized = solrSerializer.serialize(PROP_LONG, value);
                deserialized = solrDeserializer.deserialize(PROP_LONG, serialized);
                assertEquals(value, deserialized);
                
                value = nodeService.getProperty(solrNode, PROP_CHILDASSOC);
                serialized = solrSerializer.serialize(PROP_CHILDASSOC, value);
                deserialized = solrDeserializer.deserialize(PROP_CHILDASSOC, serialized);
                assertEquals(value, deserialized);
                
                value = nodeService.getProperty(solrNode, PROP_ASSOC);
                serialized = solrSerializer.serialize(PROP_ASSOC, value);
                deserialized = solrDeserializer.deserialize(PROP_ASSOC, serialized);
                assertEquals(value, deserialized);
                
                value = nodeService.getProperty(solrNode, PROP_CATEGORY);
                serialized = solrSerializer.serialize(PROP_ASSOC, value);
                deserialized = solrDeserializer.deserialize(PROP_ASSOC, serialized);
                assertEquals(value, deserialized);
                
                return null;
            }
        });
    }

}
