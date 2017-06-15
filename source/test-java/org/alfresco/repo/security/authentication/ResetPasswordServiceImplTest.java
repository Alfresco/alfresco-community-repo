/*-
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.repo.security.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.client.config.ClientAppNotFoundException;
import org.alfresco.repo.security.authentication.ResetPasswordServiceImpl.InvalidResetPasswordWorkflowException;
import org.alfresco.repo.security.authentication.ResetPasswordServiceImpl.ResetPasswordDetails;
import org.alfresco.repo.security.authentication.ResetPasswordServiceImpl.ResetPasswordWorkflowInvalidUserException;
import org.alfresco.repo.security.authentication.ResetPasswordServiceImpl.ResetPasswordWorkflowNotFoundException;
import org.alfresco.repo.security.authentication.activiti.SendResetPasswordConfirmationEmailDelegate;
import org.alfresco.repo.security.authentication.activiti.SendResetPasswordEmailDelegate;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.TestHelper;
import org.alfresco.util.email.EmailUtil;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.mail.internet.MimeMessage;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Tests for {@link ResetPasswordServiceImpl}
 *
 * @author Jamal Kaabi-Mofrad
 */
public class ResetPasswordServiceImplTest
{
    @ClassRule
    public static final ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();

    @Rule
    public final RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(AuthenticationUtil.getSystemUserName());

    private static final String DEFAULT_SENDER = "noreply@test-alfresco.test";

    private static ResetPasswordServiceImpl resetPasswordService;
    private static MutableAuthenticationService authenticationService;
    private static RetryingTransactionHelper transactionHelper;
    private static PersonService personService;
    private static Properties globalProperties;
    private static WorkflowService workflowService;

    private static TestPerson testPerson;
    private static EmailUtil emailUtil;

    @BeforeClass
    public static void initStaticData() throws Exception
    {
        resetPasswordService = APP_CONTEXT_INIT.getApplicationContext().getBean("resetPasswordService", ResetPasswordServiceImpl.class);
        resetPasswordService.setSendEmailAsynchronously(false);
        resetPasswordService.setDefaultEmailSender(DEFAULT_SENDER);
        authenticationService = APP_CONTEXT_INIT.getApplicationContext().getBean("authenticationService", MutableAuthenticationService.class);
        transactionHelper = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        personService = APP_CONTEXT_INIT.getApplicationContext().getBean("personService", PersonService.class);
        globalProperties = APP_CONTEXT_INIT.getApplicationContext().getBean("global-properties", Properties.class);
        workflowService = APP_CONTEXT_INIT.getApplicationContext().getBean("WorkflowService", WorkflowService.class);
        emailUtil = new EmailUtil(APP_CONTEXT_INIT.getApplicationContext());
        emailUtil.reset();

        String userName = "jane.doe" + System.currentTimeMillis();
        testPerson = new TestPerson()
                    .setUserName(userName)
                    .setFirstName("Jane")
                    .setLastName("doe")
                    .setPassword("password")
                    .setEmail(userName + "@example.com");

        transactionHelper.doInTransaction((RetryingTransactionCallback<Void>) () ->
        {
            createUser(testPerson);
            return null;
        });

    }

    @AfterClass
    public static void cleanUp()
    {
        resetPasswordService.setSendEmailAsynchronously(Boolean.valueOf(
                    globalProperties.getProperty("system.reset-password.sendEmailAsynchronously")));
        resetPasswordService.setDefaultEmailSender((String) globalProperties.get("system.email.sender.default"));

        AuthenticationUtil.setRunAsUserSystem();
        transactionHelper.doInTransaction(() ->
        {
            personService.deletePerson(testPerson.userName);
            return null;
        });

        // Restore authentication to pre-test state.
        try
        {
            AuthenticationUtil.popAuthentication();
        }
        catch(EmptyStackException e)
        {
            // Nothing to do.
        }
    }

    @After
    public void tearDown() throws Exception
    {
        emailUtil.reset();
    }

    @Test
    public void testResetPassword() throws Exception
    {
        // Try the credential before change of password
        authenticateUser(testPerson.userName, testPerson.password);

        // Make sure to run as system
        AuthenticationUtil.clearCurrentSecurityContext();
        AuthenticationUtil.setRunAsUserSystem();

        // Request password reset
        resetPasswordService.requestReset(testPerson.userName, "share");
        assertEquals("A reset password email should have been sent.", 1, emailUtil.getSentCount());
        // Check the email
        MimeMessage msg = emailUtil.getLastEmail();
        assertNotNull("There should be an email.", msg);
        assertEquals("Should've been only one email recipient.", 1, msg.getAllRecipients().length);
        // Check the recipient is the person who requested the reset password
        assertEquals(testPerson.email, msg.getAllRecipients()[0].toString());
        //Check the sender is what we set as default
        assertEquals(DEFAULT_SENDER, msg.getFrom()[0].toString());
        // There should be a subject
        assertNotNull("There should be a subject.", msg.getSubject());
        // Check the default email subject - (check that we are sending the right email)
        String emailSubjectKey = getDeclaredField(SendResetPasswordEmailDelegate.class, "EMAIL_SUBJECT_KEY");
        assertNotNull(emailSubjectKey);
        assertEquals(msg.getSubject(), I18NUtil.getMessage(emailSubjectKey));

        // Check the reset password url.
        String resetPasswordUrl = (String) emailUtil.getLastEmailTemplateModelValue("reset_password_url");
        assertNotNull("Wrong email is sent.", resetPasswordUrl);
        // Get the workflow id and key
        Pair<String, String> pair = getWorkflowIdAndKeyFromUrl(resetPasswordUrl);
        assertNotNull("Workflow Id can't be null.", pair.getFirst());
        assertNotNull("Workflow Key can't be null.", pair.getSecond());

        emailUtil.reset();
        // Now that we have got the email, try to reset the password
        ResetPasswordDetails passwordDetails = new ResetPasswordDetails()
                    .setUserId(testPerson.userName)
                    .setPassword("newPassword")
                    .setWorkflowId(pair.getFirst())
                    .setWorkflowKey(pair.getSecond());

        resetPasswordService.initiateResetPassword(passwordDetails);
        assertEquals("A reset password confirmation email should have been sent.", 1, emailUtil.getSentCount());
        // Check the email
        msg = emailUtil.getLastEmail();
        assertNotNull("There should be an email.", msg);
        assertEquals("Should've been only one email recipient.", 1, msg.getAllRecipients().length);
        // Check the recipient is the person who requested the reset password
        assertEquals(testPerson.email, msg.getAllRecipients()[0].toString());
        // Check the sender is what we set as default
        assertEquals(DEFAULT_SENDER, msg.getFrom()[0].toString());
        // There should be a subject
        assertNotNull("There should be a subject.", msg.getSubject());
        // Check the default email subject - (check that we are sending the right email)
        emailSubjectKey = getDeclaredField(SendResetPasswordConfirmationEmailDelegate.class, "EMAIL_SUBJECT_KEY");
        assertNotNull(emailSubjectKey);
        assertEquals(msg.getSubject(), I18NUtil.getMessage(emailSubjectKey));

        // Try the old credential
        TestHelper.assertThrows(() -> authenticateUser(testPerson.userName, testPerson.password),
                    AuthenticationException.class,
                    "As the user changed her password, the authentication should have failed.");

        // Try the new credential
        authenticateUser(testPerson.userName, "newPassword");

        // Make sure to run as system
        AuthenticationUtil.clearCurrentSecurityContext();
        AuthenticationUtil.setRunAsUserSystem();
        emailUtil.reset();
        // Try reset again with the used workflow
        TestHelper.assertThrows(() -> resetPasswordService.initiateResetPassword(passwordDetails),
                    InvalidResetPasswordWorkflowException.class,
                    "The workflow instance is not active (it has already been used).");
        assertEquals("No email should have been sent.", 0, emailUtil.getSentCount());
    }

    @Test
    public void testRequestResetPasswordInvalid() throws Exception
    {
        // Request password reset
        TestHelper.assertThrows(() -> resetPasswordService.requestReset(testPerson.userName, null),
                    IllegalArgumentException.class,
                    "Client name is mandatory.");

        // Request password reset
        TestHelper.assertThrows(() -> resetPasswordService.requestReset(testPerson.userName, "TestClient" + System.currentTimeMillis()),
                    ClientAppNotFoundException.class,
                    "Client is not found.");
        assertEquals("No email should have been sent.", 0, emailUtil.getSentCount());

        // Request password reset
        TestHelper.assertThrows(() -> resetPasswordService.requestReset(null, "share"),
                    IllegalArgumentException.class,
                    "userId is mandatory.");
        assertEquals("No email should have been sent.", 0, emailUtil.getSentCount());

        // Request password reset
        TestHelper.assertThrows(() -> resetPasswordService.requestReset("NoUser" + System.currentTimeMillis(), "share"),
                    ResetPasswordWorkflowInvalidUserException.class,
                    "user does not exist.");
        assertEquals("No email should have been sent.", 0, emailUtil.getSentCount());

        // Disable the user
        enableUser(testPerson.userName, false);

        // Request password reset
        TestHelper.assertThrows(() -> resetPasswordService.requestReset(testPerson.userName, "share"),
                    ResetPasswordWorkflowInvalidUserException.class,
                    "user is disabled.");
        assertEquals("No email should have been sent.", 0, emailUtil.getSentCount());

        // Enable the user
        enableUser(testPerson.userName, true);
    }

    @Test
    public void testResetPasswordInvalid() throws Exception
    {
        // Request password reset
        resetPasswordService.requestReset(testPerson.userName, "share");
        assertEquals("A reset password email should have been sent.", 1, emailUtil.getSentCount());
        // Check the email
        MimeMessage msg = emailUtil.getLastEmail();
        assertNotNull(msg);
        assertEquals("Should've been only one email recipient.", 1, msg.getAllRecipients().length);
        // Check the reset password url.
        String resetPasswordUrl = (String) emailUtil.getLastEmailTemplateModelValue("reset_password_url");
        assertNotNull("Wrong email is sent.", resetPasswordUrl);
        // Get the workflow id and key
        Pair<String, String> pair = getWorkflowIdAndKeyFromUrl(resetPasswordUrl);
        assertNotNull("Workflow Id can't be null.", pair.getFirst());
        assertNotNull("Workflow Key can't be null.", pair.getSecond());

        emailUtil.reset();

        TestHelper.assertThrows(() -> resetPasswordService.initiateResetPassword(null),
                    IllegalArgumentException.class,
                    "null parameter.");

        // Now that we have got the email, try to reset the password
        ResetPasswordDetails passwordDetails = new ResetPasswordDetails()
                    .setUserId(null)// user id is not provided
                    .setPassword("newPassword")
                    .setWorkflowId(pair.getFirst())
                    .setWorkflowKey(pair.getSecond());

        TestHelper.assertThrows(() -> resetPasswordService.initiateResetPassword(passwordDetails),
                    IllegalArgumentException.class,
                    "User id is mandatory.");

        passwordDetails.setUserId(testPerson.userName)
                    .setPassword(null); // Password is not provided
        TestHelper.assertThrows(() -> resetPasswordService.initiateResetPassword(passwordDetails),
                    IllegalArgumentException.class,
                    "Password is mandatory.");


        passwordDetails.setPassword(""); // Invalid password value
        TestHelper.assertThrows(() -> resetPasswordService.initiateResetPassword(passwordDetails),
                    IllegalArgumentException.class,
                    "Invalid password value.");

        passwordDetails.setPassword("newPassword")
                    .setWorkflowId(null); // Workflow id is not provided
        TestHelper.assertThrows(() -> resetPasswordService.initiateResetPassword(passwordDetails),
                    IllegalArgumentException.class,
                    "Workflow id is mandatory.");

        passwordDetails.setWorkflowId(pair.getFirst())
                    .setWorkflowKey(null); //Workflow key is not provided
        TestHelper.assertThrows(() -> resetPasswordService.initiateResetPassword(passwordDetails),
                    IllegalArgumentException.class,
                    "Workflow key is mandatory.");


        passwordDetails.setWorkflowId("activiti$" + System.currentTimeMillis()) // Invalid Id
                    .setWorkflowKey(pair.getSecond());
        TestHelper.assertThrows(() -> resetPasswordService.initiateResetPassword(passwordDetails),
                    ResetPasswordWorkflowNotFoundException.class,
                    "The workflow instance with the invalid id should not have been found.");

        passwordDetails.setWorkflowId(pair.getFirst())
                    .setWorkflowKey(GUID.generate()); // Invalid key
        TestHelper.assertThrows(() -> resetPasswordService.initiateResetPassword(passwordDetails),
                    InvalidResetPasswordWorkflowException.class,
                    "The recovered key does not match the given workflow key.");

        passwordDetails.setUserId("marco.polo")
                    .setWorkflowId(pair.getFirst())
                    .setWorkflowKey(pair.getSecond());
        TestHelper.assertThrows(() -> resetPasswordService.initiateResetPassword(passwordDetails),
                    InvalidResetPasswordWorkflowException.class,
                    "The given user id does not match the person's user id who requested the password reset.");

        assertEquals("No email should have been sent.", 0, emailUtil.getSentCount());
    }

    @Test
    public void testResetPasswordEndTimer() throws Exception
    {
        String defaultTimer = globalProperties.getProperty("system.reset-password.endTimer");
        try
        {
            // Set the duration for 1 second
            resetPasswordService.setTimerEnd("PT1S");
            // Request password reset
            resetPasswordService.requestReset(testPerson.userName, "share");
            assertEquals("A reset password email should have been sent.", 1, emailUtil.getSentCount());

            // Check the reset password url.
            String resetPasswordUrl = (String) emailUtil.getLastEmailTemplateModelValue("reset_password_url");
            assertNotNull("Wrong email is sent.", resetPasswordUrl);
            // Get the workflow id and key
            Pair<String, String> pair = getWorkflowIdAndKeyFromUrl(resetPasswordUrl);
            assertNotNull("Workflow Id can't be null.", pair.getFirst());
            assertNotNull("Workflow Key can't be null.", pair.getSecond());

            emailUtil.reset();
            // Now that we have got the email, try to reset the password
            ResetPasswordDetails passwordDetails = new ResetPasswordDetails()
                        .setUserId(testPerson.userName)
                        .setPassword("newPassword")
                        .setWorkflowId(pair.getFirst())
                        .setWorkflowKey(pair.getSecond());
            // Wait for the maximum of 10 seconds, so the end timer expires!
            boolean active = TestHelper.waitBeforeRetry(() -> isActive(pair.getFirst()), false, 10, 1000);
            assertFalse("The workflow should have been inactive.", active);
            TestHelper.assertThrows(() -> resetPasswordService.initiateResetPassword(passwordDetails),
                        InvalidResetPasswordWorkflowException.class,
                        "The workflow instance is not active (expired).");

            assertEquals("No email should have been sent.", 0, emailUtil.getSentCount());
        }
        finally
        {
            resetPasswordService.setTimerEnd(defaultTimer);
        }
    }

    private boolean isActive(String workflowId)
    {
        WorkflowInstance workflowInstance = workflowService.getWorkflowById(workflowId);
        assertNotNull(workflowInstance);
        return workflowInstance.isActive();
    }

    public static Pair<String, String> getWorkflowIdAndKeyFromUrl(String url)
    {
        //url example: http://localhost:8081/share/page/reset-password?key=164e37bf-2590-414e-94db-8b8cfe5be790&id=activiti$156
        assertNotNull(url);

        String id = StringUtils.trimToNull(
                    StringUtils.substringAfter(url, "id="));
        String key = StringUtils.substringBetween(url, "key=", "&id=");

        Pair<String, String> pair = new Pair<>(id, key);
        return pair;
    }

    private static void createUser(TestPerson testPerson)
    {
        if (authenticationService.authenticationExists(testPerson.userName))
        {
            return;
        }
        authenticationService.createAuthentication(testPerson.userName, testPerson.password.toCharArray());

        Map<QName, Serializable> map = new HashMap<>(4);
        map.put(ContentModel.PROP_USERNAME, testPerson.userName);
        map.put(ContentModel.PROP_FIRSTNAME, testPerson.firstName);
        map.put(ContentModel.PROP_LASTNAME, testPerson.lastName);
        map.put(ContentModel.PROP_EMAIL, testPerson.email);

        personService.createPerson(map);
    }

    private void enableUser(final String userName, final boolean enable)
    {
        transactionHelper.doInTransaction(() ->
        {
            // disable the user
            authenticationService.setAuthenticationEnabled(userName, enable);
            return null;
        });
    }

    private static void authenticateUser(String userName, String password)
    {
        authenticationService.authenticate(userName, password.toCharArray());
    }

    private static String getDeclaredField(Class<?> clazz, String fieldName) throws NoSuchFieldException, IllegalAccessException
    {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(null);
    }

    private static class TestPerson
    {
        private String userName;
        private String firstName;
        private String lastName;
        private String password;
        private String email;

        private TestPerson setUserName(String userName)
        {
            this.userName = userName;
            return this;
        }

        private TestPerson setFirstName(String firstName)
        {
            this.firstName = firstName;
            return this;
        }

        private TestPerson setLastName(String lastName)
        {
            this.lastName = lastName;
            return this;
        }

        private TestPerson setPassword(String password)
        {
            this.password = password;
            return this;
        }

        private TestPerson setEmail(String email)
        {
            this.email = email;
            return this;
        }
    }

}
