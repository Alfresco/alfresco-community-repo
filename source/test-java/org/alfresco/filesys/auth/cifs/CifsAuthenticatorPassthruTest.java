package org.alfresco.filesys.auth.cifs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.filesys.alfresco.AlfrescoClientInfo;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.ICifsAuthenticator;
import org.alfresco.jlan.server.auth.passthru.AuthenticateSession;
import org.alfresco.jlan.server.auth.passthru.PassthruDetails;
import org.alfresco.jlan.smb.server.SMBSrvSession;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;

public class CifsAuthenticatorPassthruTest
{
    private static ApplicationContext ctx = null;

    private PersonService personService;
    private TransactionService transactionService;
    private NodeService nodeService;
    private PassthruCifsAuthenticator cifsAuthenticator;
    private AuthenticationContext authenticationContext;

    private String userExistingLocal = "user1." + GUID.generate();
    private String userMissingLocal = "user2." + GUID.generate();

    @BeforeClass
    public static void init()
    {
       ApplicationContextHelper.setUseLazyLoading(false);
       ApplicationContextHelper.setNoAutoStart(true);
       ctx = ApplicationContextHelper.getApplicationContext();
    }

    @Before
    public void before() throws Exception
    {
        this.personService = (PersonService) ctx.getBean("personService");
        this.transactionService = (TransactionService) ctx.getBean("transactionService");
        this.nodeService = (NodeService) ctx.getBean("nodeService");
        this.authenticationContext = (AuthenticationContext) ctx.getBean("authenticationContext");

        // cannot get bean from context directly because of side affects from passthruServers.afterPropertiesSet
        this.cifsAuthenticator = new PassthruCifsAuthenticator();
        this.cifsAuthenticator.setTransactionService(transactionService);
        // passthru-authentication-context.xml : NTLMAuthenticationComponentImpl is used for passthru
        AbstractAuthenticationComponent ac = new org.alfresco.repo.security.authentication.ntlm.NTLMAuthenticationComponentImpl();
        ac.setPersonService(personService);
        ac.setTransactionService(transactionService);
        ac.setNodeService(nodeService);
        ac.setAuthenticationContext(authenticationContext);
        this.cifsAuthenticator.setAuthenticationComponent(ac);
        this.cifsAuthenticator.setAuthenticationService(mock(org.alfresco.repo.security.authentication.AuthenticationServiceImpl.class));

        // create only user1 in local repository
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void execute() throws Throwable
            {
                AuthenticationUtil.pushAuthentication();
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

                // create person properties
                PropertyMap personProps = new PropertyMap();
                personProps.put(ContentModel.PROP_USERNAME, userExistingLocal);
                personProps.put(ContentModel.PROP_FIRSTNAME, userExistingLocal);
                personProps.put(ContentModel.PROP_LASTNAME, userExistingLocal);
                personProps.put(ContentModel.PROP_EMAIL, userExistingLocal + "@email.com");
                personService.createPerson(personProps);

                AuthenticationUtil.popAuthentication();

                return null;
            }
        }, false, true);
    }

    private UserRegistrySynchronizer makeUserRegistrySynchronizerStub(final boolean autoCreatePeopleOnLogin)
    {
        UserRegistrySynchronizer stub = mock(UserRegistrySynchronizer.class);
        when(stub.createMissingPerson(anyString())).thenAnswer(new Answer<Boolean>()
        {
            public Boolean answer(InvocationOnMock invocation) throws Throwable
            {
                Object[] args = invocation.getArguments();
                String userName = (String) args[0];

                if (userName != null && !userName.equals(AuthenticationUtil.getSystemUserName()))
                {
                    PersonService personServiceStub = mock(PersonService.class);
                    when(personServiceStub.createMissingPeople()).thenReturn(true);
                    if (autoCreatePeopleOnLogin && personServiceStub.createMissingPeople())
                    {
                        AuthorityType authorityType = AuthorityType.getAuthorityType(userName);
                        if (authorityType == AuthorityType.USER)
                        {
                            personService.getPerson(userName);
                            return true;
                        }
                    }
                }
                return false;
            }
        });
        return stub;
    }

    private class TestContext
    {
        protected ClientInfo client;
        protected SrvSession sess;

        protected TestContext(ClientInfo client, SrvSession sess)
        {
            this.client = client;
            this.sess = sess;
        }
    }

    private TestContext prepareTestConditions(boolean autoCreatePeopleOnLogin, String userName) throws Exception
    {
        UserRegistrySynchronizer userRegistrySynchronizer = makeUserRegistrySynchronizerStub(autoCreatePeopleOnLogin);
        ((AbstractAuthenticationComponent) cifsAuthenticator.getAuthenticationComponent()).setUserRegistrySynchronizer(userRegistrySynchronizer);

        ClientInfo client = mock(AlfrescoClientInfo.class);
        client.setUserName(userName);
        SrvSession sess = mock(SMBSrvSession.class);
        sess.setUniqueId(userName);
        // add getter to the original class, otherwise reflection should be used to add session to private field
        AuthenticateSession as = mock(AuthenticateSession.class);
        // assume successful passthru authentication
        doNothing().when(as).doSessionSetup(anyString(), anyString(), anyString(), any(byte[].class), any(byte[].class), anyInt());
        PassthruDetails pd = new PassthruDetails(sess, as);
        cifsAuthenticator.getSessions().put(userName, pd);

        return new TestContext(client, sess);
    }

    @Test
    public void testExistingUserAuthenticationWhenAutoCreateNotAllowed() throws Exception
    {
        TestContext tc = prepareTestConditions(false, userExistingLocal);
        int status = cifsAuthenticator.authenticateUser(tc.client, tc.sess, 0);
        assertEquals("Access should be allowed if user exists in local repository", status, ICifsAuthenticator.AUTH_ALLOW);
    }

    @Test
    public void testExistingUserAuthenticationWhenAutoCreateAllowed() throws Exception
    {
        TestContext tc = prepareTestConditions(true, userExistingLocal);
        int status = cifsAuthenticator.authenticateUser(tc.client, tc.sess, 0);
        assertEquals("Access should be allowed if user exists in local repository", status, ICifsAuthenticator.AUTH_ALLOW);
    }

    @Test
    public void testMissingUserAuthenticationWhenAutoCreateNotAllowed() throws Exception
    {
        TestContext tc = prepareTestConditions(false, userMissingLocal);
        int status = cifsAuthenticator.authenticateUser(tc.client, tc.sess, 0);
        assertEquals("User that does not exist in repository should not login when autoCreatePeopleOnLogin is not allowed",
                status, ICifsAuthenticator.AUTH_DISALLOW);
    }

    @Test
    public void testMissingUserAuthenticationWhenAutoCreateAllowed() throws Exception
    {
        TestContext tc = prepareTestConditions(true, userMissingLocal);
        int status = cifsAuthenticator.authenticateUser(tc.client, tc.sess, 0);
        assertEquals("User that does not exist in repository can login when autoCreatePeopleOnLogin is allowed",
                status, ICifsAuthenticator.AUTH_ALLOW);
    }

}
