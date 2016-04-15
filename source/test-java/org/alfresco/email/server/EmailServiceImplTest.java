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
package org.alfresco.email.server;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import junit.framework.TestCase;

import org.alfresco.email.server.handler.FolderEmailMessageHandler;
import org.alfresco.email.server.impl.subetha.SubethaEmailMessage;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.email.EmailDelivery;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.email.EmailService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

import com.sun.mail.smtp.SMTPMessage;

/**
 * Unit test of EmailServiceImplTest
 * @author mrogers
 *
 */
@Category(OwnJVMTestsCategory.class)
public class EmailServiceImplTest extends TestCase 
{
    /**
     * Services used by the tests
     */
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private static Log logger = LogFactory.getLog(EmailServiceImplTest.class);
    
    private NodeService nodeService;
    private EmailService emailService;
    private PersonService personService;
    private AuthorityService authorityService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private FolderEmailMessageHandler folderEmailMessageHandler;
    private RetryingTransactionHelper transactionHelper;
    
    String TEST_USER="EmailServiceImplTestUser";
    
    @Override
    public void setUp() throws Exception
    {  
        AuthenticationUtil.setRunAsUserSystem();
        nodeService = (NodeService)ctx.getBean("NodeService");
        assertNotNull("nodeService", nodeService);
        authorityService = (AuthorityService)ctx.getBean("AuthorityService");
        assertNotNull("authorityService", authorityService);
        ChildApplicationContextFactory emailSubsystem = (ChildApplicationContextFactory) ctx.getBean("InboundSMTP");
        assertNotNull("emailSubsystem", emailSubsystem);
        ApplicationContext emailCtx = emailSubsystem.getApplicationContext();
        emailService = (EmailService)emailCtx.getBean("emailService");       
        assertNotNull("emailService", emailService);
        personService = (PersonService)emailCtx.getBean("PersonService");       
        assertNotNull("personService", personService);     
        namespaceService = (NamespaceService)emailCtx.getBean("NamespaceService");       
        assertNotNull("namespaceService", namespaceService);
        searchService = (SearchService)emailCtx.getBean("SearchService");  
        assertNotNull("searchService", searchService);
        folderEmailMessageHandler = (FolderEmailMessageHandler) emailCtx.getBean("folderEmailMessageHandler");  
        assertNotNull("folderEmailMessageHandler", folderEmailMessageHandler);
        transactionHelper = (RetryingTransactionHelper) emailCtx.getBean("retryingTransactionHelper");
        assertNotNull("transactionHelper", transactionHelper);
    }
    
    public void tearDown() throws Exception
    {
        AuthenticationUtil.setRunAsUserSystem();
        try 
        {
            personService.deletePerson(TEST_USER);
        } 
        catch (Exception e)
        {
        }
    }
    
    /**
     * Test the from name.
     * 
     * Step 1:
     * User admin will map to the "unknownUser"  which out of the box is "anonymous"
     * Sending email From "admin" will fail. 
     * 
     * Step 2:
     * Send from the test user to the test' user's home folder.
     */
   public void testFromName() throws Exception
   {
     
       folderEmailMessageHandler.setOverwriteDuplicates(true);
       
       logger.debug("Start testFromName");
       
       String TEST_EMAIL="buffy@sunnydale.high";
       
       // TODO Investigate why setting PROP_EMAIL on createPerson does not work.
       NodeRef person = personService.getPerson(TEST_USER);
       if(person == null)
       {
           logger.debug("new person created");
           Map<QName, Serializable> props = new HashMap<QName, Serializable>();
           props.put(ContentModel.PROP_USERNAME, TEST_USER);
           props.put(ContentModel.PROP_EMAIL, TEST_EMAIL);
           person = personService.createPerson(props);
       }
       nodeService.setProperty(person, ContentModel.PROP_EMAIL, TEST_EMAIL);

       Set<String> auths = authorityService.getContainedAuthorities(null, "GROUP_EMAIL_CONTRIBUTORS", true);
       if(!auths.contains(TEST_USER))
       {
           authorityService.addAuthority("GROUP_EMAIL_CONTRIBUTORS", TEST_USER);
       }
       
       String companyHomePathInStore = "/app:company_home"; 
       String storePath = "workspace://SpacesStore";
       StoreRef storeRef = new StoreRef(storePath);

       NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
       List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore, null, namespaceService, false);
       NodeRef companyHomeNodeRef = nodeRefs.get(0);
       assertNotNull("company home is null", companyHomeNodeRef);
       String companyHomeDBID = ((Long)nodeService.getProperty(companyHomeNodeRef, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
       String testUserDBID = ((Long)nodeService.getProperty(person, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
       NodeRef testUserHomeFolder = (NodeRef)nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
       assertNotNull("testUserHomeFolder is null", testUserHomeFolder);
       String testUserHomeDBID = ((Long)nodeService.getProperty(testUserHomeFolder, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
       
      /**
       * Step 1
        * Negative test - send from "Bert" who does not exist.
        * User will be mapped to anonymous who is not an email contributor.
        */
       try
       {
           String from = "admin";
           String to = "bertie";
           String content = "hello world";
       
           Session sess = Session.getDefaultInstance(new Properties());
           assertNotNull("sess is null", sess);
           SMTPMessage msg = new SMTPMessage(sess);
           InternetAddress[] toa =  { new InternetAddress(to) };
       
           msg.setFrom(new InternetAddress("Bert"));
           msg.setRecipients(Message.RecipientType.TO, toa);
           msg.setSubject("JavaMail APIs transport.java Test");
           msg.setContent(content, "text/plain");
               
           StringBuffer sb = new StringBuffer();
           ByteArrayOutputStream bos = new ByteArrayOutputStream();
           msg.writeTo(bos);
           InputStream is = IOUtils.toInputStream(bos.toString());
           assertNotNull("is is null", is);
       
           SubethaEmailMessage m = new SubethaEmailMessage(is); 
           
           EmailDelivery delivery = new EmailDelivery(to, from, null);

           emailService.importMessage(delivery, m);
           fail("anonymous user not rejected");
       } 
       catch (EmailMessageException e)
       {
           // Check the exception is for the anonymous user.
           assertTrue("Message is not for anonymous", e.getMessage().contains("anonymous"));
       }
       
       /**
        * Step 2
        * 
        * Send From the test user TEST_EMAIL to the test user's home
        */
       {
           logger.debug("Step 2");
           
       String from = TEST_EMAIL;
       String to = testUserHomeDBID;
       String content = "hello world";
   
       Session sess = Session.getDefaultInstance(new Properties());
       assertNotNull("sess is null", sess);
       SMTPMessage msg = new SMTPMessage(sess);
       InternetAddress[] toa =  { new InternetAddress(to) };
   
       msg.setFrom(new InternetAddress(TEST_EMAIL));
       msg.setRecipients(Message.RecipientType.TO, toa);
       msg.setSubject("JavaMail APIs transport.java Test");
       msg.setContent(content, "text/plain");
           
       StringBuffer sb = new StringBuffer();
       ByteArrayOutputStream bos = new ByteArrayOutputStream();
       msg.writeTo(bos);
       InputStream is = IOUtils.toInputStream(bos.toString());
       assertNotNull("is is null", is);
   
       SubethaEmailMessage m = new SubethaEmailMessage(is);  
           
       EmailDelivery delivery = new EmailDelivery(to, from, null);

       emailService.importMessage(delivery, m);
   }
       
       /**
        * Step 3
        * 
        * message.from From with "name" < name@ domain > format
        * SMTP.FROM="dummy"
        * 
        * Send From the test user <TEST_EMAIL> to the test user's home
        */
       {
           logger.debug("Step 3");
       
           String from = " \"Joe Bloggs\" <" + TEST_EMAIL + ">";
           String to = testUserHomeDBID;
           String content = "hello world";
   
           Session sess = Session.getDefaultInstance(new Properties());
           assertNotNull("sess is null", sess);
           SMTPMessage msg = new SMTPMessage(sess);
           InternetAddress[] toa =  { new InternetAddress(to) };
   
           msg.setFrom(new InternetAddress(from));
           msg.setRecipients(Message.RecipientType.TO, toa);
           msg.setSubject("JavaMail APIs transport.java Test");
           msg.setContent(content, "text/plain");
           
           StringBuffer sb = new StringBuffer();
           ByteArrayOutputStream bos = new ByteArrayOutputStream();
           msg.writeTo(System.out);
           msg.writeTo(bos);
           InputStream is = IOUtils.toInputStream(bos.toString());
           assertNotNull("is is null", is);
   
           SubethaEmailMessage m = new SubethaEmailMessage(is);
           
           EmailDelivery delivery = new EmailDelivery(to, "dummy", null);

           emailService.importMessage(delivery,m);
       }
       
       /**
        * Step 4
        * 
        * From with "name" < name@ domain > format
        * 
        * Send From the test user <TEST_EMAIL> to the test user's home
        */
       {
           logger.debug("Step 4");
       
           String from = " \"Joe Bloggs\" <" + TEST_EMAIL + ">";
           String to = testUserHomeDBID;
           String content = "hello world";
   
           Session sess = Session.getDefaultInstance(new Properties());
           assertNotNull("sess is null", sess);
           SMTPMessage msg = new SMTPMessage(sess);
           InternetAddress[] toa =  { new InternetAddress(to) };
   
           msg.setFrom(new InternetAddress(from));
           msg.setRecipients(Message.RecipientType.TO, toa);
           msg.setSubject("JavaMail APIs transport.java Test");
           msg.setContent(content, "text/plain");
           
           StringBuffer sb = new StringBuffer();
           ByteArrayOutputStream bos = new ByteArrayOutputStream();
           msg.writeTo(System.out);
           msg.writeTo(bos);
           InputStream is = IOUtils.toInputStream(bos.toString());
           assertNotNull("is is null", is);
   
           SubethaEmailMessage m = new SubethaEmailMessage(is);
           
           InternetAddress a = new InternetAddress(from);
           String x = a.getAddress();
           
           EmailDelivery delivery = new EmailDelivery(to, x, null);

           emailService.importMessage(delivery,m);
       }
       
//       /**
//        * Step 5
//        * 
//        * From with <e=name@domain> format
//        * 
//        * RFC3696
//        * 
//        * Send From the test user <TEST_EMAIL> to the test user's home
//        */
//       {
//           logger.debug("Step 4 <local tag=name@ domain > format");
//       
//           String from = "\"Joe Bloggs\" <e=" + TEST_EMAIL + ">";
//           String to = testUserHomeDBID;
//           String content = "hello world";
//   
//           Session sess = Session.getDefaultInstance(new Properties());
//           assertNotNull("sess is null", sess);
//           SMTPMessage msg = new SMTPMessage(sess);
//           InternetAddress[] toa =  { new InternetAddress(to) };
//   
//           msg.setFrom(new InternetAddress(from));
//           msg.setRecipients(Message.RecipientType.TO, toa);
//           msg.setSubject("JavaMail APIs transport.java Test");
//           msg.setContent(content, "text/plain");
//           
//           StringBuffer sb = new StringBuffer();
//           ByteArrayOutputStream bos = new ByteArrayOutputStream();
//           msg.writeTo(System.out);
//           msg.writeTo(bos);
//           InputStream is = IOUtils.toInputStream(bos.toString());
//           assertNotNull("is is null", is);
//   
//           SubethaEmailMessage m = new SubethaEmailMessage(is);   
//
//           emailService.importMessage(m);
//       }
    }


    /**
      * ALF-9544
      * ALF-751 
      *  
      * Inbound email to a folder restricts file name to 86 characters or less.
      * 
      * Also has tests for other variations of subject
      */
    public void testFolderSubject() throws Exception
    {
        logger.debug("Start testFromName");
        
        String TEST_EMAIL="buffy@sunnydale.high";
        
        folderEmailMessageHandler.setOverwriteDuplicates(true);
        
        // TODO Investigate why setting PROP_EMAIL on createPerson does not work.
        NodeRef person = personService.getPerson(TEST_USER);
        if(person == null)
        {
            logger.debug("new person created");
            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ContentModel.PROP_USERNAME, TEST_USER);
            props.put(ContentModel.PROP_EMAIL, TEST_EMAIL);
            person = personService.createPerson(props);
        }
        nodeService.setProperty(person, ContentModel.PROP_EMAIL, TEST_EMAIL);

        Set<String> auths = authorityService.getContainedAuthorities(null, "GROUP_EMAIL_CONTRIBUTORS", true);
        if(!auths.contains(TEST_USER))
        {
            authorityService.addAuthority("GROUP_EMAIL_CONTRIBUTORS", TEST_USER);
        }
        
        String companyHomePathInStore = "/app:company_home"; 
        String storePath = "workspace://SpacesStore";
        StoreRef storeRef = new StoreRef(storePath);

        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore, null, namespaceService, false);
        NodeRef companyHomeNodeRef = nodeRefs.get(0);
        assertNotNull("company home is null", companyHomeNodeRef);
        String companyHomeDBID = ((Long)nodeService.getProperty(companyHomeNodeRef, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
        String testUserDBID = ((Long)nodeService.getProperty(person, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
        NodeRef testUserHomeFolder = (NodeRef)nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
        assertNotNull("testUserHomeFolder is null", testUserHomeFolder);
        String testUserHomeDBID = ((Long)nodeService.getProperty(testUserHomeFolder, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
                
        /**
         * Send From the test user TEST_EMAIL to the test user's home
         */
        String from = TEST_EMAIL;
        String to = testUserHomeDBID;
        String content = "hello world";
    
        {
        Session sess = Session.getDefaultInstance(new Properties());
        assertNotNull("sess is null", sess);
        SMTPMessage msg = new SMTPMessage(sess);
        InternetAddress[] toa =  { new InternetAddress(to) };
    
        msg.setFrom(new InternetAddress(TEST_EMAIL));
        msg.setRecipients(Message.RecipientType.TO, toa);
        msg.setSubject("This is a very very long name in particular it is greater than eitghty six characters which was a problem explored in ALF-9544");
        msg.setContent(content, "text/plain");
            
        StringBuffer sb = new StringBuffer();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        msg.writeTo(bos);
        InputStream is = IOUtils.toInputStream(bos.toString());
        assertNotNull("is is null", is);
    
        SubethaEmailMessage m = new SubethaEmailMessage(is);   
        EmailDelivery delivery = new EmailDelivery(to, from, null);

        emailService.importMessage(delivery, m);
        }
        
        // Check import with subject containing some "illegal chars"
        {
            Session sess = Session.getDefaultInstance(new Properties());
            assertNotNull("sess is null", sess);
            SMTPMessage msg = new SMTPMessage(sess);
            InternetAddress[] toa =  { new InternetAddress(to) };
        
            msg.setFrom(new InternetAddress(TEST_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, toa);
            msg.setSubject("Illegal<>!*/\\.txt");
            msg.setContent(content, "text/plain");
                
            StringBuffer sb = new StringBuffer();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            msg.writeTo(bos);
            InputStream is = IOUtils.toInputStream(bos.toString());
            assertNotNull("is is null", is);
        
            SubethaEmailMessage m = new SubethaEmailMessage(is);   
            EmailDelivery delivery = new EmailDelivery(to, from, null);

            emailService.importMessage(delivery, m);
         }
        
        // Check with null subject
        {
            Session sess = Session.getDefaultInstance(new Properties());
            assertNotNull("sess is null", sess);
            SMTPMessage msg = new SMTPMessage(sess);
            InternetAddress[] toa =  { new InternetAddress(to) };
        
            msg.setFrom(new InternetAddress(TEST_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, toa);
            //msg.setSubject();
            msg.setContent(content, "text/plain");
                
            StringBuffer sb = new StringBuffer();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            msg.writeTo(bos);
            InputStream is = IOUtils.toInputStream(bos.toString());
            assertNotNull("is is null", is);
        
            SubethaEmailMessage m = new SubethaEmailMessage(is);   
            EmailDelivery delivery = new EmailDelivery(to, from, null);

            emailService.importMessage(delivery, m);
         }
        
        
        // ALF-751 Email ends with period
        {
            Session sess = Session.getDefaultInstance(new Properties());
            assertNotNull("sess is null", sess);
            SMTPMessage msg = new SMTPMessage(sess);
            InternetAddress[] toa =  { new InternetAddress(to) };
        
            msg.setFrom(new InternetAddress(TEST_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, toa);
            msg.setSubject("Foobar.");
            msg.setContent(content, "text/plain");
                
            StringBuffer sb = new StringBuffer();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            msg.writeTo(bos);
            InputStream is = IOUtils.toInputStream(bos.toString());
            assertNotNull("is is null", is);
        
            SubethaEmailMessage m = new SubethaEmailMessage(is);   
            EmailDelivery delivery = new EmailDelivery(to, from, null);

            emailService.importMessage(delivery, m);
         }
        
        // ALF-751 Email ends with ...
        {
            Session sess = Session.getDefaultInstance(new Properties());
            assertNotNull("sess is null", sess);
            SMTPMessage msg = new SMTPMessage(sess);
            InternetAddress[] toa =  { new InternetAddress(to) };
        
            msg.setFrom(new InternetAddress(TEST_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, toa);
            msg.setSubject("Foobar...");
            msg.setContent(content, "text/plain");
                
            StringBuffer sb = new StringBuffer();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            msg.writeTo(bos);
            InputStream is = IOUtils.toInputStream(bos.toString());
            assertNotNull("is is null", is);
        
            SubethaEmailMessage m = new SubethaEmailMessage(is);   
            EmailDelivery delivery = new EmailDelivery(to, from, null);

            emailService.importMessage(delivery, m);
         }
        
        // ALF-751 Email subject is blank " ... "
        {
            Session sess = Session.getDefaultInstance(new Properties());
            assertNotNull("sess is null", sess);
            SMTPMessage msg = new SMTPMessage(sess);
            InternetAddress[] toa =  { new InternetAddress(to) };
        
            msg.setFrom(new InternetAddress(TEST_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, toa);
            msg.setSubject(" ... ");
            msg.setContent(content, "text/plain");
                
            StringBuffer sb = new StringBuffer();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            msg.writeTo(bos);
            InputStream is = IOUtils.toInputStream(bos.toString());
            assertNotNull("is is null", is);
        
            SubethaEmailMessage m = new SubethaEmailMessage(is);   
            EmailDelivery delivery = new EmailDelivery(to, from, null);

            emailService.importMessage(delivery, m);
         }
        
        // ALF-751 Email subject is a single .
        {
            Session sess = Session.getDefaultInstance(new Properties());
            assertNotNull("sess is null", sess);
            SMTPMessage msg = new SMTPMessage(sess);
            InternetAddress[] toa =  { new InternetAddress(to) };
        
            msg.setFrom(new InternetAddress(TEST_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, toa);
            msg.setSubject(".");
            msg.setContent(content, "text/plain");
                
            StringBuffer sb = new StringBuffer();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            msg.writeTo(bos);
            InputStream is = IOUtils.toInputStream(bos.toString());
            assertNotNull("is is null", is);
        
            SubethaEmailMessage m = new SubethaEmailMessage(is);   
            EmailDelivery delivery = new EmailDelivery(to, from, null);

            emailService.importMessage(delivery, m);
         }
           
    }
    
    /**
     * ALF-1878
     * 
     * Duplicate incoming email Subjects over-write each other
     */
   public void testMultipleMessagesToFolder() throws Exception
   {
       logger.debug("Start testFromName");
       
       String TEST_EMAIL="buffy@sunnydale.high";
       
       String TEST_SUBJECT="Practical Bee Keeping";
       
       String TEST_LONG_SUBJECT = "This is a very very long name in particular it is greater than eitghty six characters which was a problem explored in ALF-9544";
       
       
       // TODO Investigate why setting PROP_EMAIL on createPerson does not work.
       NodeRef person = personService.getPerson(TEST_USER);
       if(person == null)
       {
           logger.debug("new person created");
           Map<QName, Serializable> props = new HashMap<QName, Serializable>();
           props.put(ContentModel.PROP_USERNAME, TEST_USER);
           props.put(ContentModel.PROP_EMAIL, TEST_EMAIL);
           person = personService.createPerson(props);
       }
       nodeService.setProperty(person, ContentModel.PROP_EMAIL, TEST_EMAIL);

       Set<String> auths = authorityService.getContainedAuthorities(null, "GROUP_EMAIL_CONTRIBUTORS", true);
       if(!auths.contains(TEST_USER))
       {
           authorityService.addAuthority("GROUP_EMAIL_CONTRIBUTORS", TEST_USER);
       }
       
       String companyHomePathInStore = "/app:company_home"; 
       String storePath = "workspace://SpacesStore";
       StoreRef storeRef = new StoreRef(storePath);

       NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
       List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore, null, namespaceService, false);
       NodeRef companyHomeNodeRef = nodeRefs.get(0);
       assertNotNull("company home is null", companyHomeNodeRef);
       String companyHomeDBID = ((Long)nodeService.getProperty(companyHomeNodeRef, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
       String testUserDBID = ((Long)nodeService.getProperty(person, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
       NodeRef testUserHomeFolder = (NodeRef)nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
       assertNotNull("testUserHomeFolder is null", testUserHomeFolder);
       String testUserHomeDBID = ((Long)nodeService.getProperty(testUserHomeFolder, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
       
       // Clean up old messages in test folder
       List<ChildAssociationRef> assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
       for(ChildAssociationRef assoc : assocs)
       {
           nodeService.deleteNode(assoc.getChildRef());
       }
               
       /**
        * Send From the test user TEST_EMAIL to the test user's home
        */
       String from = TEST_EMAIL;
       String to = testUserHomeDBID;
       String content = "hello world";
   
       Session sess = Session.getDefaultInstance(new Properties());
       assertNotNull("sess is null", sess);
       SMTPMessage msg = new SMTPMessage(sess);
       InternetAddress[] toa =  { new InternetAddress(to) };
   
       msg.setFrom(new InternetAddress(TEST_EMAIL));
       msg.setRecipients(Message.RecipientType.TO, toa);
       msg.setSubject(TEST_SUBJECT);
       msg.setContent(content, "text/plain");
           
       ByteArrayOutputStream bos = new ByteArrayOutputStream();
       msg.writeTo(bos);
       InputStream is = IOUtils.toInputStream(bos.toString());
       assertNotNull("is is null", is);
   
       SubethaEmailMessage m = new SubethaEmailMessage(is);   
       
       /**
        * Turn on overwriteDuplicates
        */
       logger.debug("Step 1: turn on Overwite Duplicates");
       folderEmailMessageHandler.setOverwriteDuplicates(true);
       
       EmailDelivery delivery = new EmailDelivery(to, from, null);

       emailService.importMessage(delivery, m);
       assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
       assertEquals("assocs not 1", 1, assocs.size());
       assertEquals("name of link not as expected", assocs.get(0).getQName(), QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, TEST_SUBJECT));
       emailService.importMessage(delivery, m);
       assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
       assertEquals("assocs not 1", 1, assocs.size());   
       
       /**
        * Turn off overwrite Duplicates
        */
       logger.debug("Step 2: turn off Overwite Duplicates");
       folderEmailMessageHandler.setOverwriteDuplicates(false);
       emailService.importMessage(delivery, m);
       assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
       assertEquals("assocs not 2", 2, assocs.size());
       emailService.importMessage(delivery, m);
       assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
       assertEquals("assocs not 3", 3, assocs.size());   

       /**
        * Check assoc rename with long names.   So truncation and rename need to work together.
        */
       logger.debug("Step 3: turn off Overwite Duplicates with long subject name");
       msg.setSubject(TEST_LONG_SUBJECT);
       ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
       msg.writeTo(bos2);
       is = IOUtils.toInputStream(bos2.toString());
       assertNotNull("is is null", is);
       m = new SubethaEmailMessage(is);   
       
       folderEmailMessageHandler.setOverwriteDuplicates(false);
       emailService.importMessage(delivery, m);
       assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
       assertEquals("assocs not 4", 4, assocs.size());
       emailService.importMessage(delivery, m);
       assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
       assertEquals("assocs not 5", 5, assocs.size());
       
       /**
        * Check assoc rename with long names and an extension. So truncation and rename need to 
        * work together and not muck up a .extension.
        */
       logger.debug("Step 4: turn off Overwite Duplicates with long subject name with extension");
       String EXT_NAME = "Blob.xls";
       
       msg.setSubject(EXT_NAME);
       ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
       msg.writeTo(bos3);
       is = IOUtils.toInputStream(bos3.toString());
       assertNotNull("is is null", is);
       m = new SubethaEmailMessage(is);  
       folderEmailMessageHandler.setOverwriteDuplicates(false);
       emailService.importMessage(delivery, m);
       emailService.importMessage(delivery, m);
       emailService.importMessage(delivery, m);
       assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);

       List<QName> assocNames = new Vector<QName>(); 
       for(ChildAssociationRef assoc : assocs)
       {
           logger.debug("assocName: " + assoc.getQName());
           System.out.println(assoc.getQName());
           assocNames.add(assoc.getQName());
       }
       assertTrue(EXT_NAME + "not found", assocNames.contains(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Blob.xls")));
       assertTrue("Blob(1).xls not found", assocNames.contains(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Blob(1).xls")));   
       assertTrue("Blob(2).xls not found", assocNames.contains(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Blob(2).xls")));      
       assertTrue(TEST_SUBJECT + "not found", assocNames.contains(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, TEST_SUBJECT)));   
       assertTrue(TEST_SUBJECT+"(1) not found", assocNames.contains(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Practical Bee Keeping(1)")));      

       /**
        * Check concurrent deliver of the same message. Reuse message from the previous test.
        */
       logger.debug("Step 5: turn off Overwite Duplicates and check concurrent deliver of the same message");
       folderEmailMessageHandler.setOverwriteDuplicates(false);
       assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
       int numBeforeConcurrentDeliver = assocs.size();
       deliverConcurrently(delivery, m);
       assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
       int numAfterConcurrentDeliver = assocs.size();
       assertEquals("Two messages must be added", numBeforeConcurrentDeliver + 2, numAfterConcurrentDeliver);
   }

   private void deliverConcurrently(final EmailDelivery delivery, final SubethaEmailMessage m) throws Exception
   {
       final CountDownLatch cdl = new CountDownLatch(1);
       class ConcurrentMessageImporter implements Runnable
       {
           private Throwable throwable;
           @Override
           public void run()
           {
                try
                {
                    transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            cdl.countDown();
                            emailService.importMessage(delivery, m);
                            return null;
                        }
                    }, false, true);
                }
                catch (Throwable t)
                {
                    throwable = t;
                }
           }
       }
       ConcurrentMessageImporter messageImporter = new ConcurrentMessageImporter();
       final Thread messageImporterThread = new Thread(messageImporter);
       transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
               {
                   public Void execute() throws Throwable
                   {
                       emailService.importMessage(delivery, m);
                       messageImporterThread.start();
                       // wait until concurrent transaction has started
                       cdl.await();
                       return null;
                   }
               }, false, true);
       messageImporterThread.join();
       if (null != messageImporter.throwable)
       {
           fail(messageImporter.throwable.getMessage());
       }
   }
   
   /**
    * MNT-9289
    * 
    * Change in case in email Subject causes DuplicateChildNodeNameException
    */
   public void testCaseSensitivity() throws Exception
   {
	   NodeRef person = personService.getPerson(TEST_USER);
	   String TEST_EMAIL="buffy@sunnydale.high";
	   NodeRef testUserHomeFolder = (NodeRef)nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
       if(person == null)
       {
           logger.debug("new person created");
           Map<QName, Serializable> props = new HashMap<QName, Serializable>();
           props.put(ContentModel.PROP_USERNAME, TEST_USER);
           props.put(ContentModel.PROP_EMAIL, TEST_EMAIL);
           person = personService.createPerson(props);
       }
       
       nodeService.setProperty(person, ContentModel.PROP_EMAIL, TEST_EMAIL);

       Set<String> auths = authorityService.getContainedAuthorities(null, "GROUP_EMAIL_CONTRIBUTORS", true);
       if(!auths.contains(TEST_USER))
       {
           authorityService.addAuthority("GROUP_EMAIL_CONTRIBUTORS", TEST_USER);
       }
       
       String companyHomePathInStore = "/app:company_home"; 
       String storePath = "workspace://SpacesStore";
       StoreRef storeRef = new StoreRef(storePath);

       NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
       List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore, null, namespaceService, false);
       NodeRef companyHomeNodeRef = nodeRefs.get(0);
       assertNotNull("company home is null", companyHomeNodeRef);
	   
       String TEST_CASE_SENSITIVITY_SUBJECT = "Test (Mail)";
       String testUserHomeDBID = ((Long)nodeService.getProperty(testUserHomeFolder, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
	   
       String from = TEST_EMAIL;
       String to = testUserHomeDBID;
       String content = "hello world";
   
       Session sess = Session.getDefaultInstance(new Properties());
       assertNotNull("sess is null", sess);
       SMTPMessage msg = new SMTPMessage(sess);
       InternetAddress[] toa =  { new InternetAddress(to) };
       
       EmailDelivery delivery = new EmailDelivery(to, from, null);
   
       msg.setFrom(new InternetAddress(TEST_EMAIL));
       msg.setRecipients(Message.RecipientType.TO, toa);
       msg.setContent(content, "text/plain");
	   
       msg.setSubject(TEST_CASE_SENSITIVITY_SUBJECT);
       ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
       msg.writeTo(bos1);
       InputStream is = IOUtils.toInputStream(bos1.toString());
       assertNotNull("is is null", is);
       SubethaEmailMessage m = new SubethaEmailMessage(is);  
       folderEmailMessageHandler.setOverwriteDuplicates(false);
       emailService.importMessage(delivery, m);
       
       QName safeQName = QName.createQNameWithValidLocalName(NamespaceService.CONTENT_MODEL_1_0_URI, TEST_CASE_SENSITIVITY_SUBJECT);
       List<ChildAssociationRef> assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, safeQName);
       assertEquals(1, assocs.size());
       
       msg.setSubject(TEST_CASE_SENSITIVITY_SUBJECT.toUpperCase());
       ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
       msg.writeTo(bos2);
       is = IOUtils.toInputStream(bos2.toString());
       assertNotNull("is is null", is);
       m = new SubethaEmailMessage(is);  
       folderEmailMessageHandler.setOverwriteDuplicates(false);
       emailService.importMessage(delivery, m);
       
       safeQName = QName.createQNameWithValidLocalName(NamespaceService.CONTENT_MODEL_1_0_URI, TEST_CASE_SENSITIVITY_SUBJECT.toUpperCase() +  "(1)");
       assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, safeQName);
       assertEquals(1, assocs.size());
   }
   
   
   /**
    * ALF-12297
    * 
    * Test messages being sent to a cm:content node
    */
  public void testMessagesToDocument() throws Exception
  {
      logger.debug("Start testMessagesToDocument");
      
      String TEST_EMAIL="buffy@sunnydale.high";
      
      String TEST_SUBJECT="Practical Bee Keeping";
      
      String TEST_LONG_SUBJECT = "This is a very very long name in particular it is greater than eitghty six characters which was a problem explored in ALF-9544";
      
      
      // TODO Investigate why setting PROP_EMAIL on createPerson does not work.
      NodeRef person = personService.getPerson(TEST_USER);
      if(person == null)
      {
          logger.debug("new person created");
          Map<QName, Serializable> props = new HashMap<QName, Serializable>();
          props.put(ContentModel.PROP_USERNAME, TEST_USER);
          props.put(ContentModel.PROP_EMAIL, TEST_EMAIL);
          person = personService.createPerson(props);
      }
      nodeService.setProperty(person, ContentModel.PROP_EMAIL, TEST_EMAIL);

      Set<String> auths = authorityService.getContainedAuthorities(null, "GROUP_EMAIL_CONTRIBUTORS", true);
      if(!auths.contains(TEST_USER))
      {
          authorityService.addAuthority("GROUP_EMAIL_CONTRIBUTORS", TEST_USER);
      }
      
      String companyHomePathInStore = "/app:company_home"; 
      String storePath = "workspace://SpacesStore";
      StoreRef storeRef = new StoreRef(storePath);

      NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
      List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore, null, namespaceService, false);
      NodeRef companyHomeNodeRef = nodeRefs.get(0);
      assertNotNull("company home is null", companyHomeNodeRef);
      String companyHomeDBID = ((Long)nodeService.getProperty(companyHomeNodeRef, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
 //     String testUserDBID = ((Long)nodeService.getProperty(person, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
      NodeRef testUserHomeFolder = (NodeRef)nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
      assertNotNull("testUserHomeFolder is null", testUserHomeFolder);
//      String testUserHomeDBID = ((Long)nodeService.getProperty(testUserHomeFolder, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
      
      // Clean up old messages in test folder
      List<ChildAssociationRef> assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
      for(ChildAssociationRef assoc : assocs)
      {
          nodeService.deleteNode(assoc.getChildRef());
      }
      
      
      Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
      properties.put(ContentModel.PROP_NAME, "bees");
      properties.put(ContentModel.PROP_DESCRIPTION, "bees - test doc for email tests");
      ChildAssociationRef testDoc = nodeService.createNode(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "bees"), ContentModel.TYPE_CONTENT, properties);
      NodeRef testDocNodeRef = testDoc.getChildRef();
      
      String testDocDBID = ((Long)nodeService.getProperty(testDocNodeRef, ContentModel.PROP_NODE_DBID)).toString();
      
      /**
       * Send From the test user TEST_EMAIL to the test user's home
       */
      String from = TEST_EMAIL;
      String to = testDocDBID + "@alfresco.com";
      String content = "hello world";
  
      Session sess = Session.getDefaultInstance(new Properties());
      assertNotNull("sess is null", sess);
      SMTPMessage msg = new SMTPMessage(sess);
      InternetAddress[] toa =  { new InternetAddress(to) };
  
      msg.setFrom(new InternetAddress(TEST_EMAIL));
      msg.setRecipients(Message.RecipientType.TO, toa);
      msg.setSubject(TEST_SUBJECT);
      msg.setContent(content, "text/plain");
          
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      msg.writeTo(bos);
      InputStream is = IOUtils.toInputStream(bos.toString());
      assertNotNull("is is null", is);
  
      SubethaEmailMessage m = new SubethaEmailMessage(is);   
      
      /**
       * Turn on overwriteDuplicates
       */
      logger.debug("Step 1: send an email to a doc");
            
      EmailDelivery delivery = new EmailDelivery(to, from, null);

      emailService.importMessage(delivery, m);
      
      assertTrue(nodeService.hasAspect(testDocNodeRef, ForumModel.ASPECT_DISCUSSABLE));
      
 
   
  } // end of test sending to cm:content node
  
  
  /**
   * ENH-560 - Inbound email server not working with custom types
   */
 public void testMessagesToSubTypeOfDocument() throws Exception
 {
     logger.debug("Start testMessagesToSubTypesOfDocument");
     
     String TEST_EMAIL="buffy@sunnydale.high";
     
     String TEST_SUBJECT="Practical Bee Keeping";
     
     String TEST_LONG_SUBJECT = "This is a very very long name in particular it is greater than eitghty six characters which was a problem explored in ALF-9544";
     
     
     // TODO Investigate why setting PROP_EMAIL on createPerson does not work.
     NodeRef person = personService.getPerson(TEST_USER);
     if(person == null)
     {
         logger.debug("new person created");
         Map<QName, Serializable> props = new HashMap<QName, Serializable>();
         props.put(ContentModel.PROP_USERNAME, TEST_USER);
         props.put(ContentModel.PROP_EMAIL, TEST_EMAIL);
         person = personService.createPerson(props);
     }
     nodeService.setProperty(person, ContentModel.PROP_EMAIL, TEST_EMAIL);

     Set<String> auths = authorityService.getContainedAuthorities(null, "GROUP_EMAIL_CONTRIBUTORS", true);
     if(!auths.contains(TEST_USER))
     {
         authorityService.addAuthority("GROUP_EMAIL_CONTRIBUTORS", TEST_USER);
     }
     
     String companyHomePathInStore = "/app:company_home"; 
     String storePath = "workspace://SpacesStore";
     StoreRef storeRef = new StoreRef(storePath);

     NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
     List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore, null, namespaceService, false);
     NodeRef companyHomeNodeRef = nodeRefs.get(0);
     assertNotNull("company home is null", companyHomeNodeRef);
     String companyHomeDBID = ((Long)nodeService.getProperty(companyHomeNodeRef, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
//     String testUserDBID = ((Long)nodeService.getProperty(person, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
     NodeRef testUserHomeFolder = (NodeRef)nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
     assertNotNull("testUserHomeFolder is null", testUserHomeFolder);
//     String testUserHomeDBID = ((Long)nodeService.getProperty(testUserHomeFolder, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
     
     // Clean up old messages in test folder
     List<ChildAssociationRef> assocs = nodeService.getChildAssocs(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
     for(ChildAssociationRef assoc : assocs)
     {
         nodeService.deleteNode(assoc.getChildRef());
     }
     
     
     Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
     properties.put(ContentModel.PROP_NAME, "hamster");
     properties.put(ContentModel.PROP_DESCRIPTION, "syrian hamsters - test doc for email tests, sending to a subtype of cm:content");
     
     // Transfer report is a subtype of cm:content
     ChildAssociationRef testDoc = nodeService.createNode(testUserHomeFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "bees"), TransferModel.TYPE_TRANSFER_REPORT, properties);
     NodeRef testDocNodeRef = testDoc.getChildRef();
     
     String testDocDBID = ((Long)nodeService.getProperty(testDocNodeRef, ContentModel.PROP_NODE_DBID)).toString();
     
     /**
      * Send From the test user TEST_EMAIL to the test user's home
      */
     String from = TEST_EMAIL;
     String to = testDocDBID + "@alfresco.com";
     String content = "hello world";
 
     Session sess = Session.getDefaultInstance(new Properties());
     assertNotNull("sess is null", sess);
     SMTPMessage msg = new SMTPMessage(sess);
     InternetAddress[] toa =  { new InternetAddress(to) };
 
     msg.setFrom(new InternetAddress(TEST_EMAIL));
     msg.setRecipients(Message.RecipientType.TO, toa);
     msg.setSubject(TEST_SUBJECT);
     msg.setContent(content, "text/plain");
         
     ByteArrayOutputStream bos = new ByteArrayOutputStream();
     msg.writeTo(bos);
     InputStream is = IOUtils.toInputStream(bos.toString());
     assertNotNull("is is null", is);
 
     SubethaEmailMessage m = new SubethaEmailMessage(is);   
     
     /**
      * Turn on overwriteDuplicates
      */
     logger.debug("Step 1: send an email to a transfer report");
           
     EmailDelivery delivery = new EmailDelivery(to, from, null);

     emailService.importMessage(delivery, m);
     
 } // end of test sending to trx:transferReport


   
   
   
   /**
    * The Email contributors authority controls who can add email.
    * 
    * This test switches between the EMAIL_CONTRIBUTORS group and EVERYONE
    */
   public void testEmailContributorsAuthority() throws Exception
   {
       EmailServiceImpl emailServiceImpl = (EmailServiceImpl)emailService;
       
       folderEmailMessageHandler.setOverwriteDuplicates(true);
       
       logger.debug("Start testEmailContributorsAuthority");
       
       String TEST_EMAIL="buffy@sunnydale.high";
       
       // TODO Investigate why setting PROP_EMAIL on createPerson does not work.
       NodeRef person = personService.getPerson(TEST_USER);
       if(person == null)
       {
           logger.debug("new person created");
           Map<QName, Serializable> props = new HashMap<QName, Serializable>();
           props.put(ContentModel.PROP_USERNAME, TEST_USER);
           props.put(ContentModel.PROP_EMAIL, TEST_EMAIL);
           person = personService.createPerson(props);
       }
       nodeService.setProperty(person, ContentModel.PROP_EMAIL, TEST_EMAIL);

       Set<String> auths = authorityService.getContainedAuthorities(null, "GROUP_EMAIL_CONTRIBUTORS", true);
       if(auths.contains(TEST_USER))
       {
           authorityService.removeAuthority("GROUP_EMAIL_CONTRIBUTORS", TEST_USER);
       }
       
       String companyHomePathInStore = "/app:company_home"; 
       String storePath = "workspace://SpacesStore";
       StoreRef storeRef = new StoreRef(storePath);

       NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
       List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, companyHomePathInStore, null, namespaceService, false);
       NodeRef companyHomeNodeRef = nodeRefs.get(0);
       assertNotNull("company home is null", companyHomeNodeRef);
       String companyHomeDBID = ((Long)nodeService.getProperty(companyHomeNodeRef, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
       String testUserDBID = ((Long)nodeService.getProperty(person, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
       NodeRef testUserHomeFolder = (NodeRef)nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
       assertNotNull("testUserHomeFolder is null", testUserHomeFolder);
       String testUserHomeDBID = ((Long)nodeService.getProperty(testUserHomeFolder, ContentModel.PROP_NODE_DBID)).toString() + "@Alfresco.com";
       
      /**
       * Step 1
       * Set the email contributors authority to EVERYONE 
       * 
       * Test that TEST_USER is allowed to send email - so even though TEST_USER is not 
       * a contributor
       */
       emailServiceImpl.setEmailContributorsAuthority("EVERYONE");
     
       String from = "admin";
       String to =  testUserHomeDBID;
       String content = "hello world";
       
       Session sess = Session.getDefaultInstance(new Properties());
       assertNotNull("sess is null", sess);
       SMTPMessage msg = new SMTPMessage(sess);
       InternetAddress[] toa =  { new InternetAddress(to) };
       
       msg.setFrom(new InternetAddress(TEST_EMAIL));
       msg.setRecipients(Message.RecipientType.TO, toa);
       msg.setSubject("JavaMail APIs transport.java Test");
       msg.setContent(content, "text/plain");
               
       StringBuffer sb = new StringBuffer();
       ByteArrayOutputStream bos = new ByteArrayOutputStream();
       msg.writeTo(bos);
       InputStream is = IOUtils.toInputStream(bos.toString());
       assertNotNull("is is null", is);
       
       SubethaEmailMessage m = new SubethaEmailMessage(is);  
       
       EmailDelivery delivery = new EmailDelivery(to, from, null);

       emailService.importMessage(delivery, m);
       
       /**
        * Step 2
        * Negative test
        * 
        * Send From the test user TEST_EMAIL to the test user's home
        */
       try 
       {
           logger.debug("Step 2");
           emailServiceImpl.setEmailContributorsAuthority("EMAIL_CONTRIBUTORS");
           emailService.importMessage(delivery, m);
           fail("not thrown out");
       }
       catch (EmailMessageException e)
       {
         // Check the exception is for the anonymous user.
         // assertTrue(e.getMessage().contains("anonymous"));
       }
   }
 
} // end of EmailServiceImplTest
