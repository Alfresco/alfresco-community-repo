package org.alfresco.rest.api.tests;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.TestWebScriptServer;

/**
 * Helper for Cloud Tests
 */
public class PublicApiTestContext
{
    private ApplicationContext applicationContext;
    private BaseWebScriptTest test;
    
    private List<String> usersToBeTidied = new ArrayList<String>();
    private List<String> invalidDomainsToBeTidied = new ArrayList<String>();
    
    private MutableAuthenticationService authenticationService;
    private RetryingTransactionHelper transactionHelper;

    public PublicApiTestContext()
    {
        init(ApplicationContextHelper.getApplicationContext());
    }
    
    public PublicApiTestContext(ApplicationContext context)
    {
        init(context);
    }
    
    public PublicApiTestContext(BaseWebScriptTest test)
    {
        this.test = test;
        this.test.setCustomContext("cloud-test-context.xml");
        init(test.getServer().getApplicationContext());
    }
    
    private void init(ApplicationContext context)
    {
        applicationContext = context;
        transactionHelper = (RetryingTransactionHelper)applicationContext.getBean("retryingTransactionHelper");
        authenticationService = (MutableAuthenticationService)applicationContext.getBean("authenticationService");
    }
    
    public ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }
    
    public TestWebScriptServer getTestServer()
    {
        return test.getServer();
    }
    
    public String createUserName(String alias, String tenant)
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append(alias);
    	if(tenant != null && !tenant.equals(TenantService.DEFAULT_DOMAIN))
    	{
    		sb.append("@");
    		sb.append(tenant);
    	}
    	return sb.toString();
    }

    public void addUser(String user)
    {
        usersToBeTidied.add(user);
    }
    
    public void addInvalidDomain(String domain)
    {
        invalidDomainsToBeTidied.add(domain);
    }
    
    public void removeInvalidDomain(String domain)
    {
        invalidDomainsToBeTidied.remove(domain);
    }

    public void cleanup()
    {
        RunAsWork<Void> work = new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    @SuppressWarnings("synthetic-access")
                    public Void execute() throws Throwable
                    {
                        return null;
                    }
                });
                return null;
            }
        };
        AuthenticationUtil.runAs(work, AuthenticationUtil.getSystemUserName());
        
        for (final String user : this.usersToBeTidied)
        {
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    if (authenticationService.authenticationExists(user))
                    {
                        authenticationService.deleteAuthentication(user);
                    }
                    return null;
                }
            });
        }
        usersToBeTidied.clear();
        
        // finally clear authentication
        AuthenticationUtil.clearCurrentSecurityContext();
    }
}
