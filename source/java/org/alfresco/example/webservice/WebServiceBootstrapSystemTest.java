package org.alfresco.example.webservice;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sun.corba.se.impl.orb.ParserTable.TestContactInfoListFactory;

public class WebServiceBootstrapSystemTest extends TestCase
{   
    /**
     * NOTE:  You need to set the location of the indexes and content store to be a non-realtive
     *        location in the repository.properties file otherwise running this test here will not
     *        populate the correct index and content store and the test won't work when running against
     *        the repository
     */
    
    public static final String FOLDER_NAME = "test folder";
    public static final String CONTENT_NAME = "test content";
    
    public static final String PROP_STORE_REF = "storeRef";
    public static final String PROP_ROOT_NODE_REF = "rootNodeRef";
    public static final String PROP_FOLDER_NODE_REF = "folderNodeRef";
    public static final String PROP_CONTENT_NODE_REF = "contentNodeRef";
    
    private static final String TEMP_BOOTSTRAP_PROPERTIES = "./WebServiceTestBootstrap.properties";    
    
    private static final String TEST_CONTENT = "This is some test content.  This is some test content.";    
    
    
    /**
     * Runs the bootstrap and populates the property file with the infomration required for the tests
     */
    public void testBootstrap()
    {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:alfresco/application-context.xml");
        
        // Get the services
        TransactionService transactionService = (TransactionService)applicationContext.getBean("transactionComponent");
        AuthenticationComponent authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        NodeService nodeService = (NodeService)applicationContext.getBean("NodeService");
        ContentService contentService = (ContentService)applicationContext.getBean("contentService");       
        ImporterService importerService = (ImporterService)applicationContext.getBean("importerComponent");
        
        UserTransaction userTransaction = transactionService.getUserTransaction();
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        try
        {

        StoreRef storeRef = null;
        NodeRef rootNodeRef = null;
        NodeRef folderNodeRef = null;
        NodeRef testContent = null;
        
        try
        {
            userTransaction.begin();
            
            // Create the store
            storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
            rootNodeRef = rootNodeRef = nodeService.getRootNode(storeRef);
            
            // Import the categories
            InputStream viewStream = getClass().getClassLoader().getResourceAsStream("alfresco/bootstrap/categories.xml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(viewStream));
            Location importLocation = new Location(storeRef);
            importLocation.setPath("/");
            importerService.importView(reader, importLocation, null, null);
            
            // Folder properties
            Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
            folderProps.put(ContentModel.PROP_NAME, FOLDER_NAME);
            
            // Create a folder
            folderNodeRef = nodeService.createNode(
                                            rootNodeRef, 
                                            ContentModel.ASSOC_CHILDREN,
                                            ContentModel.ASSOC_CHILDREN,
                                            ContentModel.TYPE_FOLDER,
                                            folderProps).getChildRef();
            
            Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>(3);
            contentProps.put(ContentModel.PROP_NAME, CONTENT_NAME);
            
            // Create some test content        
            testContent = nodeService.createNode(
                    rootNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.TYPE_CONTENT,
                    contentProps).getChildRef();
            ContentWriter writer = contentService.getWriter(testContent, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(TEST_CONTENT);
            
            // Add the translatable aspect to the test content
            nodeService.addAspect(testContent, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "translatable"), null);
            
            // Create content to be the translation of the translatable content
            NodeRef testContent2 = nodeService.createNode(
                    rootNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.TYPE_CONTENT,
                    contentProps).getChildRef();
            ContentWriter writer2 = contentService.getWriter(testContent2, ContentModel.PROP_CONTENT, true);
            writer2.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer2.setEncoding("UTF-8");
            writer2.putContent(TEST_CONTENT);
            
            // Add the association from the master content to the translated content
            nodeService.createAssociation(testContent, testContent2, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "translations"));
            
            userTransaction.commit();
        }
        catch(Throwable e)
        {
            // rollback the transaction
            try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
            try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
            throw new AlfrescoRuntimeException("Bootstrap failed", e);
        }
        
        Properties properties = new Properties();
        properties.put(PROP_STORE_REF, storeRef.toString());
        properties.put(PROP_ROOT_NODE_REF, rootNodeRef.toString());
        properties.put(PROP_FOLDER_NODE_REF, folderNodeRef.toString());
        properties.put(PROP_CONTENT_NODE_REF, testContent.toString());
        
        try
        {
            OutputStream outputStream = new FileOutputStream(TEMP_BOOTSTRAP_PROPERTIES);
            properties.store(outputStream, "Web service node store details");
            outputStream.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unable to store bootstrap details.");
        }
        
        //System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
    }
    
    public static Properties getBootstrapProperties()
    {
        Properties properties = new Properties();
        try
        {
            InputStream inputStream = new FileInputStream(TEMP_BOOTSTRAP_PROPERTIES);
            properties.load(inputStream);            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Unable to load test bootstrap details.  Try running WebServiceBootstrapSystem test, then re-start container and try again.");
        }
        return properties;
    }
}
