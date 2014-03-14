package org.alfresco.filesys.auth.cifs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
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

public class CifsAuthenticatorKerberosTest
{
    public static final String[] CONFIG_LOCATIONS = new String[] { "classpath:alfresco/application-context.xml",
            "classpath:alfresco/filesys/auth/cifs/test-kerberos-context.xml"
        };
    private static ApplicationContext ctx = null;

    private PersonService personService;
    private TransactionService transactionService;
    private EnterpriseCifsAuthenticator cifsAuthenticator;

    private String userExistingLocal = "user1." + GUID.generate();
    private String userMissingLocal = "user2." + GUID.generate();

    @BeforeClass
    public static void init()
    {
       ApplicationContextHelper.setUseLazyLoading(false);
       ApplicationContextHelper.setNoAutoStart(true);
       ctx = ApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);
    }

    @Before
    public void before() throws Exception
    {
        this.personService = (PersonService)ctx.getBean("personService");
        this.transactionService = (TransactionService)ctx.getBean("transactionService");
        this.cifsAuthenticator = (EnterpriseCifsAuthenticator)ctx.getBean("cifsAuthenticator");

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
                personProps.put(ContentModel.PROP_EMAIL, userExistingLocal+"@email.com");
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

    @Test
    public void testExistingUserMappingWhenAutoCreateNotAllowed()
    {
        UserRegistrySynchronizer userRegistrySynchronizer = makeUserRegistrySynchronizerStub(false);
        ((AbstractAuthenticationComponent) cifsAuthenticator.getAuthenticationComponent()).setUserRegistrySynchronizer(userRegistrySynchronizer);
        String username = cifsAuthenticator.mapUserNameToPerson(userExistingLocal, false);
        assertEquals("Existing local user should be mapped to authenticated AD user", username, userExistingLocal);
    }

    @Test
    public void testExistingUserMappingWhenAutoCreateAllowed()
    {
        UserRegistrySynchronizer userRegistrySynchronizer = makeUserRegistrySynchronizerStub(true);
        ((AbstractAuthenticationComponent) cifsAuthenticator.getAuthenticationComponent()).setUserRegistrySynchronizer(userRegistrySynchronizer);
        String username = cifsAuthenticator.mapUserNameToPerson(userExistingLocal, false);
        assertEquals("Existing local user should be mapped to authenticated AD user", username, userExistingLocal);
    }

    @Test
    public void testMissingUserMappingWhenAutoCreateNotAllowed()
    {
        UserRegistrySynchronizer userRegistrySynchronizer = makeUserRegistrySynchronizerStub(false);
        ((AbstractAuthenticationComponent) cifsAuthenticator.getAuthenticationComponent()).setUserRegistrySynchronizer(userRegistrySynchronizer);
        try
        {
            cifsAuthenticator.mapUserNameToPerson(userMissingLocal, false);
            fail("User that does not exist in repository should not login when autoCreatePeopleOnLogin is not allowed");
        }
        catch (AuthenticationException expected)
        {
        }
    }

    @Test
    public void testMissingUserMappingWhenAutoCreateAllowed()
    {
        UserRegistrySynchronizer userRegistrySynchronizer = makeUserRegistrySynchronizerStub(true);
        ((AbstractAuthenticationComponent) cifsAuthenticator.getAuthenticationComponent()).setUserRegistrySynchronizer(userRegistrySynchronizer);
        String username = cifsAuthenticator.mapUserNameToPerson(userMissingLocal, false);
        assertEquals("User that does not exist in repository can login when autoCreatePeopleOnLogin is allowed", username, userMissingLocal);
        // personService.personExists requires RunAsUser to be set
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        assertTrue(personService.personExists(userMissingLocal));
    }

}
