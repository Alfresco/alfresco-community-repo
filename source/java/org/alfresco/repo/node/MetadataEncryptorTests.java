package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

public class MetadataEncryptorTests extends TestCase
{
    private static final String TEST_MODEL = "org/alfresco/repo/node/encrypted_prop_model.xml";
    private static QName ENCRYPTED_TYPE_QNAME = QName.createQName("http://www.alfresco.org/test/encryptedPropModel/1.0", "encrypted");
    private static QName ENCRYPTED_PROP_QNAME = QName.createQName("http://www.alfresco.org/test/encryptedPropModel/1.0", "prop1");

    private static final Log logger = LogFactory.getLog(MetadataEncryptorTests.class);
    
    private ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private NodeService nodeService;
    private TenantService tenantService;
    private DictionaryDAO dictionaryDAO;
    private MetadataEncryptor metadataEncryptor;
    
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
//        txnHelper = transactionService.getRetryingTransactionHelper();
        metadataEncryptor = (MetadataEncryptor)ctx.getBean("metadataEncryptor");
        nodeService = serviceRegistry.getNodeService();
        tenantService = (TenantService)ctx.getBean("tenantService");
        dictionaryDAO = (DictionaryDAO)ctx.getBean("dictionaryDAO");

        AuthenticationUtil.setRunAsUserSystem();
        
        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");
        bootstrapModels.add(TEST_MODEL);
//        List<String> labels = new ArrayList<String>();
//        labels.add(TEST_BUNDLE);
        bootstrap.setModels(bootstrapModels);
//        bootstrap.setLabels(labels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.setTenantService(tenantService);
        bootstrap.bootstrap();
        
        // create a first store directly
        RetryingTransactionCallback<NodeRef> createStoreWork = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute()
            {
                StoreRef storeRef = nodeService.createStore(
                        StoreRef.PROTOCOL_WORKSPACE,
                        "Test_" + System.nanoTime());
                return nodeService.getRootNode(storeRef);
            }
        };
        rootNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(createStoreWork);
    }
    
    /**
     * Loads the test model required for building the node graphs
     */
//    public static DictionaryService loadModel(ApplicationContext applicationContext)
//    {
//        DictionaryDAO dictionaryDao = (DictionaryDAO) applicationContext.getBean("dictionaryDAO");
//        // load the system model
//        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
//        InputStream modelStream = cl.getResourceAsStream("alfresco/model/contentModel.xml");
//        assertNotNull(modelStream);
//        M2Model model = M2Model.createModel(modelStream);
//        dictionaryDao.putModel(model);
//        // load the test model
//        modelStream = cl.getResourceAsStream("org/alfresco/repo/node/BaseNodeServiceTest_model.xml");
//        assertNotNull(modelStream);
//        model = M2Model.createModel(modelStream);
//        dictionaryDao.putModel(model);
//        
//        DictionaryComponent dictionary = new DictionaryComponent();
//        dictionary.setDictionaryDAO(dictionaryDao);
//        // done
//        return dictionary;
//    }
    
    public void testWithoutEncryption()
    {
        RetryingTransactionCallback<Void> encryptionWork = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                NodeRef nodeRef1 = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
                        ContentModel.TYPE_FOLDER, null).getChildRef();

                try
                {
	                // Create a node using the thread's locale
	                NodeRef nodeRef2 = nodeService.createNode(
	                		nodeRef1,
	                        ContentModel.ASSOC_CONTAINS,
	                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
	                        ENCRYPTED_TYPE_QNAME,
	                        Collections.singletonMap(ENCRYPTED_PROP_QNAME, (Serializable)"hello world")).getChildRef();
	                fail("Should have generated an IllegalArgumentException");
                }
                //catch(IntegrityException e)
                catch(IllegalArgumentException e)
                {
                	// expected
                }
                
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(encryptionWork);

    }

    public void testWithEncryption()
    {
        RetryingTransactionCallback<Void> encryptionWork = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                NodeRef nodeRef1 = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
                        ContentModel.TYPE_FOLDER, null).getChildRef();

                Map<QName, Serializable> allProperties = new PropertyMap();
                allProperties.put(ENCRYPTED_PROP_QNAME, "ABC");
                allProperties = metadataEncryptor.encrypt(allProperties);
                
                try
                {
	                NodeRef nodeRef2 = nodeService.createNode(
	                		nodeRef1,
	                        ContentModel.ASSOC_CONTAINS,
	                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
	                        ENCRYPTED_TYPE_QNAME, allProperties).getChildRef();
	                assertNotNull(nodeRef2);

	                Serializable encryptedPropertyValue = nodeService.getProperty(nodeRef2, ENCRYPTED_PROP_QNAME);
	                Serializable decryptedPropertyValue = metadataEncryptor.decrypt(ENCRYPTED_PROP_QNAME, encryptedPropertyValue);
	                assertEquals("ABC", decryptedPropertyValue);
                }
                //catch(IntegrityException e)
                catch(Throwable e)
                {
                	fail();
                }

                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(encryptionWork);
    }
    
    /**
     * Clean up the test thread
     */
    @Override
    protected void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        I18NUtil.setLocale(null);
    }
}
