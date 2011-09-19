/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.notification;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.notification.NotificationProvider;
import org.alfresco.service.cmr.notification.NotificationService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Notification service implementation test.
 * 
 * @author Roy Wetherall
 * @since 4.0
 */
public class NotificationServiceImplSystemTest extends BaseAlfrescoTestCase
{
    private static final String FROM_USER = "fromUser" + GUID.generate();
    private static final String FROM_EMAIL = "test@alfresco.com";
    private static final String FROM_FIRST_NAME = "Grace";
    private static final String FROM_LAST_NAME = "Wetherall";
    
    private static final String TO_USER1 = "userOne" + GUID.generate();
    private static final String TO_USER2 = "userTwo" + GUID.generate();
    private static final String TO_USER3 = "userThree" + GUID.generate();
    
    private static final String EMAIL = "rwetherall@alfresco.com";
    private static final String PASSWORD = "password";
    private static final String FIRST_NAME = "Peter";
    private static final String LAST_NAME = "Wetherall";
    
    private static final String SUBJECT = "Notification Test";
    private static final String BODY = "This is a test notification from org.alfresco.repo.notification.NotificationServiceImplSystemTest.  Please do not respond!";
    
    private static final String TEMPLATE = 
     "<html>" +
     "   <body bgcolour='#dddddd'>" +
     "      <p>This is a test notification from org.alfresco.repo.notification.NotificationServiceImplSystemTest.  Please do not respond!</p>" +
     "      <br>" +
     "      Template context:<br><br>" +
     "      userhome: ${userhome}<br>" +
     "      companyhome: ${companyhome}<br>" +
     "      productname: ${productName}<br>" +
     "";
    
    private NotificationService notificationService;
    private MutableAuthenticationService authenticationService;
    private PersonService personService;
    private Repository repository;
    private FileFolderService fileFolderService;
    
    private NodeRef fromPerson;
    private NodeRef toPerson1;
    private NodeRef toPerson2;
    private NodeRef toPerson3;    
    
    private NodeRef template;
    
    /**
     * Storage for a {@link NotificationContext} sent from a test method.
     */
    private NotificationContext contextSentFromTest = null;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        contextSentFromTest = null;
        
        // Get the notification service
        notificationService = (NotificationService)ctx.getBean("NotificationService");
        authenticationService = (MutableAuthenticationService)ctx.getBean("AuthenticationService");   
        personService = (PersonService)ctx.getBean("PersonService");
        repository = (Repository)ctx.getBean("repositoryHelper");
        fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
        
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
                // Create people and users
                fromPerson = createPerson(FROM_USER, PASSWORD, FROM_FIRST_NAME, FROM_LAST_NAME, FROM_EMAIL);
                toPerson1 = createPerson(TO_USER1, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL);
                toPerson2 = createPerson(TO_USER2, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL);
                toPerson3 = createPerson(TO_USER3, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL);
                
                // Create a test template
                NodeRef companyHome = repository.getCompanyHome();
                template = fileFolderService.create(companyHome, "testTemplate" + GUID.generate() + ".ftl", ContentModel.TYPE_CONTENT).getNodeRef();
                
                ContentWriter writer = contentService.getWriter(template, ContentModel.PROP_CONTENT, true);
                writer.setEncoding("UTF-8");
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.putContent(TEMPLATE);
                
                return null;
            }
        });
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                
                // Delete the template
                nodeService.deleteNode(template);
                
                return null;
            }
        });
        
        super.tearDown();
    }
    
    private NodeRef createPerson(String userName, String password, String firstName, String lastName, String email)
    {
        NodeRef person = null;
        
        if (authenticationService.authenticationExists(userName) == false)
        {
            authenticationService.createAuthentication(userName, password.toCharArray());
            
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
            properties.put(ContentModel.PROP_USERNAME, userName);
            properties.put(ContentModel.PROP_FIRSTNAME, firstName);
            properties.put(ContentModel.PROP_LASTNAME, lastName);
            properties.put(ContentModel.PROP_EMAIL, email);
            
            person = personService.createPerson(properties);
        }
        else
        {
            person = personService.getPerson(userName);
        }
        return person;
    }
    
    @Override
    protected boolean useSpacesStore()
    {
        return true;
    }
    
    public void testSimpleEmailNotification()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                NotificationContext context = new NotificationContext();
                
                context.setFrom(FROM_EMAIL);
                context.addTo(TO_USER1);
                context.setSubject(SUBJECT);
                context.setBody(BODY);
                
                notificationService.sendNotification(EMailNotificationProvider.NAME, context);
                
                return null;
            }
        });
    }
    
    public void testTemplateEmailNotification()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                NotificationContext context = new NotificationContext();
                
                context.setFrom(FROM_EMAIL);
                context.addTo(TO_USER1);
                context.setSubject(SUBJECT);
                context.setBodyTemplate(template);
                
                Map<String, Serializable> templateArgs = new HashMap<String, Serializable>(1);
                templateArgs.put("template", template);
                context.setTemplateArgs(templateArgs);
                
                notificationService.sendNotification(EMailNotificationProvider.NAME, context);
                
                return null;
            }
        });
    }
    
    /**
     * This method tests the {@link TestNotificationProvider}.
     */
    public void testTestNotificationProvider() throws Exception
    {
        // Create a class to receive the notification.
        NotificationReceiver receiver = new NotificationReceiver()
        {
            @Override
            public void receiveNotification(NotificationContext notificationContext)
            {
                contextSentFromTest = notificationContext;
            }
        };
        
        // Set up the notification provider.
        TestNotificationProvider testNP = new TestNotificationProvider();
        testNP.setNotificationService(notificationService);
        testNP.setNotificationReceiver(receiver);
        testNP.init();
        
        // send the notification
        assertTrue(notificationService.getNotificationProviders().contains(TestNotificationProvider.NAME));
        NotificationContext context = new NotificationContext();
        final String to = "FAO: test object";
        context.addTo(to);
        notificationService.sendNotification(TestNotificationProvider.NAME, context);
        
        assertNotNull("notification context was null.", contextSentFromTest);
        assertTrue("notification context did not contain correct 'to' entry", contextSentFromTest.getTo().contains(to));
    }
    
    /**
     * This {@link NotificationProvider} is intended for use in test code in order to validate the
     * content/state of notifications. It could, for example, be used in a test context as a drop-in
     * replacement for the {@link EMailNotificationProvider} for validation and/or to prevent emails being
     * sent from test code.
     * 
     * @author Neil Mc Erlean
     * @since 4.0
     */
    public static class TestNotificationProvider implements NotificationProvider
    {
        public static final String NAME = TestNotificationProvider.class.getSimpleName();
        
        private static Log log = LogFactory.getLog(TestNotificationProvider.class); 
        
        private NotificationService notificationService;
        private NotificationReceiver notificationReceiver;
        
        public void setNotificationService(NotificationService notificationService)
        {
            this.notificationService = notificationService;
        }
        
        public void setNotificationReceiver(NotificationReceiver notificationReceiver)
        {
            this.notificationReceiver = notificationReceiver;
        }
        
        /**
         * Init method registers provider with notification service.
         */
        public void init()
        {
            notificationService.register(TestNotificationProvider.this);
        }
        
        @Override
        public String getName()
        {
            return NAME;
        }
        
        @Override
        public void sendNotification(NotificationContext notificationContext)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Sending notification.");
            }
            this.notificationReceiver.receiveNotification(notificationContext);
        }
    }
    
    /**
     * Implementations of this interface can be injected into the {@link TestNotificationProvider} in order to receive notifications.
     * <p/>
     * Only intended for test code at this stage.
     * 
     * @author Neil Mc Erlean
     * @since 4.0
     */
    public static interface NotificationReceiver
    {
        public void receiveNotification(NotificationContext notificationContext);
    }
}
