/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import junit.framework.TestCase;

import org.alfresco.email.server.impl.subetha.SubethaEmailMessage;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.email.EmailMessageException;
import org.alfresco.service.cmr.email.EmailService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.filters.StringInputStream;
import org.springframework.context.ApplicationContext;

import com.sun.mail.smtp.SMTPMessage;

/**
 * Unit test of EmailServiceImplTest
 * @author mrogers
 *
 */
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
        searchService = (SearchService)emailCtx.getBean("SearchService");       
        assertNotNull("searchService", searchService);
        namespaceService = (NamespaceService)emailCtx.getBean("NamespaceService");       
        assertNotNull("namespaceService", namespaceService);
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
           InputStream is = new StringInputStream(bos.toString());
           assertNotNull("is is null", is);
       
           SubethaEmailMessage m = new SubethaEmailMessage(is);   

           emailService.importMessage(m);
           fail("anonymous user not rejected");
       } 
       catch (EmailMessageException e)
       {
           // Check the exception is for the anonymous user.
           assertTrue(e.getMessage().contains("anonymous"));
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
       InputStream is = new StringInputStream(bos.toString());
       assertNotNull("is is null", is);
   
           SubethaEmailMessage m = new SubethaEmailMessage(is);   

       emailService.importMessage(m);
   }
       
       /**
        * Step 3
        * 
        * From with < name@ domain > format
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
           InputStream is = new StringInputStream(bos.toString());
           assertNotNull("is is null", is);
   
           SubethaEmailMessage m = new SubethaEmailMessage(is);   

           emailService.importMessage(m);
       }
       
//       /**
//        * Step 4
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
//           InputStream is = new StringInputStream(bos.toString());
//           assertNotNull("is is null", is);
//   
//           SubethaEmailMessage m = new SubethaEmailMessage(is);   
//
//           emailService.importMessage(m);
//       }
    }


    /**
      * ALF-9544
      *  
      * Inbound email to a folder restricts file name to 86 characters or less.
      */
    public void testFolderSubject() throws Exception
    {
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
        msg.setSubject("This is a very very long name in particular it is greater than eitghty six characters which was a problem explored in ALF-9544");
        msg.setContent(content, "text/plain");
            
        StringBuffer sb = new StringBuffer();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        msg.writeTo(bos);
        InputStream is = new StringInputStream(bos.toString());
        assertNotNull("is is null", is);
    
        SubethaEmailMessage m = new SubethaEmailMessage(is);   

        emailService.importMessage(m);
           
    }
    


}
