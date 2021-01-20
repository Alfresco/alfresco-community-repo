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
package org.alfresco.repo.i18n;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.FixMethodOrder;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Message Service unit tests
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(OwnJVMTestsCategory.class)
public class MessageServiceImplTest extends TestCase implements MessageDeployer
{
    private ApplicationContext applicationContext;
    
    private static final String BASE_BUNDLE_NAME = "testMessages";
    private static final String BASE_RESOURCE_CLASSPATH = "org/alfresco/repo/i18n/";
    
    private static final String PARAM_VALUE = "television";
    private static final String MSG_YES = "msg_yes";    
    private static final String MSG_NO = "msg_no";
    private static final String MSG_PARAMS = "msg_params";
    private static final String VALUE_YES = "Yes";
    private static final String VALUE_NO = "No";
    private static final String VALUE_PARAMS = "What no " + PARAM_VALUE + "?";
    private static final String VALUE_FR_YES = "Oui";
    private static final String VALUE_FR_NO = "Non";
    private static final String VALUE_FR_PARAMS = "Que non " + PARAM_VALUE + "?";
   
    private MessageService messageService;
    private NodeService nodeService;
    private MutableAuthenticationService authenticationService;
    private ContentService contentService;
    private DictionaryDAO dictionaryDAO;
    private TransactionService transactionService;
    private AuthenticationComponent authenticationComponent;
    
    /**
     * Test user details
     */
    private static final String PWD = "admin";
    
    /**
     * Test store ref
     */
    private StoreRef testStoreRef;

    private UserTransaction testTX;
    
    
    @Override
    protected void setUp() throws Exception
    {
        applicationContext = ApplicationContextHelper.getApplicationContext();
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            fail("Detected a leaked transaction from a previous test.");
        }
        
        // Get the services by name from the application context
        messageService = (MessageService)applicationContext.getBean("messageService");
        nodeService = (NodeService)applicationContext.getBean("NodeService");
        authenticationService = (MutableAuthenticationService)applicationContext.getBean("AuthenticationService");
        contentService = (ContentService) applicationContext.getBean("ContentService");
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        dictionaryDAO = (DictionaryDAO) applicationContext.getBean("dictionaryDAO");
        
        // Re-set the current locale to be the default
        Locale.setDefault(Locale.ENGLISH);
        messageService.setLocale(Locale.getDefault());
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        authenticationComponent.setSystemUserAsCurrentUser();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        if (testTX != null)
        {
            try
            {
                testTX.rollback();
            }
            catch (Throwable e) {} // Ignore
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    private void setupRepo() throws Exception
    {       
        AuthenticationUtil.clearCurrentSecurityContext();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        // Create a test workspace
        this.testStoreRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
             
        // Get a reference to the root node
        NodeRef rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);
        
        // Create and authenticate the user        
        if(!authenticationService.authenticationExists(AuthenticationUtil.getAdminUserName()))
        {
            authenticationService.createAuthentication(AuthenticationUtil.getAdminUserName(), PWD.toCharArray());
        }
             
        // Authenticate - as admin
        authenticationService.authenticate(AuthenticationUtil.getAdminUserName(), PWD.toCharArray());
        
        // Store test messages in repo
        String pattern = "classpath*:" + BASE_RESOURCE_CLASSPATH + BASE_BUNDLE_NAME + "*";
        
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
       
        Resource[] resources = resolver.getResources(pattern);

        if (resources != null)
        {
            for (int i = 0; i < resources.length; i++)
            {
                String filename = resources[i].getFilename();
                addMessageResource(rootNodeRef, filename, resources[i].getInputStream());                
            }
        }
    }
    
    private void addMessageResource(NodeRef rootNodeRef, String name, InputStream resourceStream) throws Exception
    {       
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);
        
        ChildAssociationRef association = nodeService.createNode(rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_CONTENT,
                contentProps);
        
        NodeRef content = association.getChildRef();
        
        ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);

        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
    
        writer.putContent(resourceStream);
        resourceStream.close();
    }
    
    /**
     * Test the set and get methods
     */
    public void test1SetAndGet()
    {
        // Check that the default locale is returned 
        assertEquals(Locale.getDefault(), messageService.getLocale());
        
        // Set the locals
        messageService.setLocale(Locale.CANADA_FRENCH);
        assertEquals(Locale.CANADA_FRENCH, messageService.getLocale());
        
        // Reset the locale
        messageService.setLocale(null);
        assertEquals(Locale.getDefault(), messageService.getLocale());
    }
    
    /**
     * Test get message from repository
     */
    public void test2GetMessagesLoadedFromRepo() throws Exception
    {
        setupRepo();
        
        // Check with no bundles loaded
        assertNull(messageService.getMessage(MSG_NO));        
        
        // Register the bundle
        messageService.registerResourceBundle(testStoreRef + "/cm:" + BASE_BUNDLE_NAME);
        
        getMessages();
        
        messageService.unregisterResourceBundle(testStoreRef + "/cm:" + BASE_BUNDLE_NAME);
    }
       
    /**
     * Test getting a parameterised message from repository
     */
    public void test3GetMessagesWithParamsLoadedFromRepo() throws Exception
    {
        setupRepo();
        
        // Check with no bundles loaded
        assertNull(messageService.getMessage(MSG_PARAMS, new Object[]{PARAM_VALUE}));
        
        // Register the bundle
        messageService.registerResourceBundle(testStoreRef + "/cm:" + BASE_BUNDLE_NAME);
        
        getMessagesWithParams();
        
        messageService.unregisterResourceBundle(testStoreRef + "/cm:" + BASE_BUNDLE_NAME);
    }
 
    
    /**
     * Test get message from classpath
     */
    public void test4GetMessagesLoadedFromClasspath() throws Exception
    {
        // Check with no bundles loaded
        assertNull(messageService.getMessage(MSG_NO));        
        
        // Register the bundle
        messageService.registerResourceBundle(BASE_RESOURCE_CLASSPATH + BASE_BUNDLE_NAME);
        
        getMessages();
        
        messageService.unregisterResourceBundle(BASE_RESOURCE_CLASSPATH + BASE_BUNDLE_NAME);
    }
    
    /**
     * Test getting a parameterised message from classpath
     */
    public void test5GetMessagesWithParamsLoadedFromClasspath() throws Exception
    {       
        // Check with no bundles loaded
        assertNull(messageService.getMessage(MSG_PARAMS, new Object[]{PARAM_VALUE}));
        
        // Register the bundle
        messageService.registerResourceBundle(BASE_RESOURCE_CLASSPATH + BASE_BUNDLE_NAME);
        
        getMessagesWithParams();
        
        messageService.unregisterResourceBundle(BASE_RESOURCE_CLASSPATH + BASE_BUNDLE_NAME);
    }
    
    /**
     * Test register bundle (using a repository location) with uninitialised cache
     */
    public void test6RegisterBundleFromRepo() throws Exception
    {  
        setupRepo();
        
        // Register the bundle
        messageService.registerResourceBundle(testStoreRef + "/cm:" + BASE_BUNDLE_NAME);
       
        // Test getting a message
        assertEquals(VALUE_YES, messageService.getMessage(MSG_YES));
        
        messageService.unregisterResourceBundle(testStoreRef + "/cm:" + BASE_BUNDLE_NAME);
    }
    
    /**
     * Test register bundle (using a classpath location) with uninitialised cache
     */
    public void test7RegisterBundleFromClasspath() throws Exception
    {  
        // Register the bundle
        messageService.registerResourceBundle(BASE_RESOURCE_CLASSPATH + BASE_BUNDLE_NAME);
       
        // Test getting a message
        assertEquals(VALUE_YES, messageService.getMessage(MSG_YES));
        
        messageService.unregisterResourceBundle(BASE_RESOURCE_CLASSPATH + BASE_BUNDLE_NAME);
    }
    
    /**
     * Test forced reset
     */
    public void test8Reset() throws Exception
    {  
        // Check with no bundles loaded
        assertNull(messageService.getMessage(MSG_YES)); 
        
        messageService.register(this); // register with message service to allow reset (via initMessages callback)

        initMessages();
       
        // Test getting a message
        assertEquals(VALUE_YES, messageService.getMessage(MSG_YES));
        
        // Force a reset
        ((MessageServiceImpl)messageService).reset();
        
        // Test getting a message
        assertEquals(VALUE_YES, messageService.getMessage(MSG_YES));
    }
    
    public void initMessages()
    {
        // Register the bundle
        messageService.registerResourceBundle(BASE_RESOURCE_CLASSPATH + BASE_BUNDLE_NAME);
    }
    
    public void test9LocaleMatching()
    {
        Set<Locale> options = new HashSet<Locale>(13);
        options.add(Locale.FRENCH);                 // fr
        options.add(Locale.FRANCE);                 // fr_FR
        options.add(Locale.CANADA);                 // en_CA
        options.add(Locale.CANADA_FRENCH);          // fr_CA
        options.add(Locale.CHINESE);                // zh
        options.add(Locale.TRADITIONAL_CHINESE);    // zh_TW
        options.add(Locale.SIMPLIFIED_CHINESE);     // zh_CN
        options.add(Locale.GERMAN);                 // de
        // add some variants
        Locale fr_FR_1 = new Locale("fr", "FR", "1");
        Locale zh_CN_1 = new Locale("zh", "CN", "1");
        Locale zh_CN_2 = new Locale("zh", "CN", "2");
        Locale zh_CN_3 = new Locale("zh", "CN", "3");
        options.add(zh_CN_1);                       // zh_CN_1
        options.add(zh_CN_2);                       // zh_CN_2
        
        Set<Locale> chineseMatches = new HashSet<Locale>(3);
        chineseMatches.add(Locale.SIMPLIFIED_CHINESE);
        chineseMatches.add(zh_CN_1);                      
        chineseMatches.add(zh_CN_2);   
        
        Set<Locale> frenchMatches = new HashSet<Locale>(3);
        frenchMatches.add(Locale.FRANCE);
        
        // check
        assertEquals(Locale.CHINA, messageService.getNearestLocale(Locale.CHINA, options));
        assertEquals(Locale.CHINESE, messageService.getNearestLocale(Locale.CHINESE, options));
        assertEquals(zh_CN_1, messageService.getNearestLocale(zh_CN_1, options));
        assertEquals(zh_CN_2, messageService.getNearestLocale(zh_CN_2, options));
        assertTrue(chineseMatches.contains(messageService.getNearestLocale(zh_CN_3, options)));         // must match the last variant - but set can have any order an IBM JDK differs!
        assertEquals(Locale.FRANCE, messageService.getNearestLocale(fr_FR_1, options)); // same here
        
        // fallback to language if the country isn't defined
        assertFalse(options.contains(Locale.GERMANY)); // test pre-condition
        assertEquals(Locale.GERMAN, messageService.getNearestLocale(Locale.GERMANY, options));
        
        // now test the match for just anything
        Locale na_na_na = new Locale("", "", "");
        Locale check = messageService.getNearestLocale(na_na_na, options);
        assertNotNull("Expected some kind of value back", check);
    }
    
    public void testLocaleParsing()
    {
        assertEquals(Locale.FRANCE, messageService.parseLocale("fr_FR"));
        assertEquals(new Locale("en", "GB", "cockney"), messageService.parseLocale("en_GB_cockney"));
        assertEquals(new Locale("en", "GB", ""), messageService.parseLocale("en_GB"));
        assertEquals(new Locale("en", "", ""), messageService.parseLocale("en"));
        assertEquals(Locale.getDefault(), messageService.parseLocale(""));
    }
    
    public void testRegisteredBundlesSetDirectModification()
    {
        String bad_key = "BAD_KEY" + System.currentTimeMillis();
        
        Set<String> bundles = messageService.getRegisteredBundles();
        
        assertNotNull(bundles);
        assertTrue(!bundles.contains(bad_key));
        
        try
        {
            // put entry directly
            bundles.add(bad_key);
            fail("Shouldn't be modified");
        }
        catch (UnsupportedOperationException e)
        {
            // it's ok
        }
        
        Set<String> anotherTryBundles = messageService.getRegisteredBundles();
        
        assertNotNull(anotherTryBundles);
        assertTrue(!bundles.contains(bad_key));
    }
    
    private void getMessages()
    {
        // Check default values
        assertEquals(VALUE_YES, messageService.getMessage(MSG_YES));
        assertEquals(VALUE_NO, messageService.getMessage(MSG_NO));
        
        // Check not existant value
        assertNull(messageService.getMessage("bad_key"));        
        
        // Change the locale and re-test
        messageService.setLocale(new Locale("fr", "FR"));
        
        // Check values
        assertEquals(VALUE_FR_YES, messageService.getMessage(MSG_YES));
        assertEquals(VALUE_FR_NO, messageService.getMessage(MSG_NO));
        
        // Check values when overriding the locale
        assertEquals(VALUE_YES, messageService.getMessage(MSG_YES, Locale.getDefault()));
        assertEquals(VALUE_NO, messageService.getMessage(MSG_NO, Locale.getDefault()));
    }
  
    private void getMessagesWithParams()
    {
         // Check the default value
         assertEquals(VALUE_PARAMS, messageService.getMessage(MSG_PARAMS, new Object[]{PARAM_VALUE}));
             
         // Change the locale and re-test
         messageService.setLocale(new Locale("fr", "FR"));
         
         // Check the default value
         assertEquals(VALUE_FR_PARAMS, messageService.getMessage(MSG_PARAMS, new Object[]{PARAM_VALUE}));       
         
         // Check values when overriding the locale
         assertEquals(VALUE_PARAMS, messageService.getMessage(MSG_PARAMS, Locale.getDefault(), new Object[]{PARAM_VALUE}));
     }  

    /**
     * See MNT-9462
     */
    public void testDictionaryDAOLock()
    {
        class DictionaryDAOThread extends Thread
        {
            private volatile boolean success = false;
            @Override
            public void run()
            {
                success = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>()
                {
                    @Override
                    public Boolean execute() throws Throwable
                    {
                        dictionaryDAO.destroy();
                        dictionaryDAO.init();
                        return Boolean.TRUE;
                    }
                });
            }
        }
        class MessageServiceThread extends Thread
        {
            private volatile boolean success = false;
            @Override
            public void run()
            {
                success = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>()
                {
                    @Override
                    public Boolean execute()
                    {
                        messageService.destroy();
                        messageService.getMessage(MSG_YES);
                        return Boolean.TRUE;
                    }
                });
            }
        }
        // Create the threads so that they die if the VM exits
        DictionaryDAOThread ddt = new DictionaryDAOThread();
        ddt.setDaemon(true);
        MessageServiceThread mst = new MessageServiceThread();
        mst.setDaemon(true);
        
        ddt.start();
        mst.start();
        // Wait for the first thread to 
        try
        {
            ddt.join(60000);
            mst.join(60000);
        }
        catch (InterruptedException e)
        {
            // Interrupt to terminate any lock trying
            ddt.interrupt();
            mst.interrupt();
            // Something kicked us out before we could join and before the time expired ... unlikely
            fail("Unexpected interrupt while joining to deadlocking threads.");
        }
        
        try
        {
            if (ddt.isAlive() && mst.isAlive())
            {
                fail("Deadlock: DictionaryDAOThread and MessageServiceThread are both still alive.");
            }
            else if (ddt.isAlive())
            {
                fail("Possible deadlock with a background process: DictionaryDAOThread is still alive.");
            }
            else if (mst.isAlive())
            {
                fail("Possible deadlock with a background process: MessageServiceThread is still alive.");
            }
            else if (!ddt.success)
            {
                fail("DictionaryDAOThread failed to execute successfully.");
            }
            else if (!mst.success)
            {
                fail("MessageServiceThread failed to execute successfully.");
            }
        }
        finally
        {
            // Interrupt to terminate any lock trying
            ddt.interrupt();
            mst.interrupt();
        }
    }
    
    public void testMNT13575()
    {
        Locale de = new Locale("de");
        assertTrue(messageService.getLocale().equals(new Locale("en")));
        assertFalse(messageService.getLocale().equals(de));
        String key = "cm_contentmodel.property.cm_description.title";
        String value_en = "Description";
        String value_de = "Beschreibung";
        assertEquals(value_en, messageService.getMessage(key));
        assertEquals(value_de, messageService.getMessage(key, de));
    }
}
