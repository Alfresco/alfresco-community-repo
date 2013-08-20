/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.util.test.junitrules;

import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.util.GUID;
import org.springframework.context.ApplicationContext;

/**
 * This JUnit rule can be used to setup and teardown a single Alfresco Tenant for test purposes.
 * <p/>
 * Example usage:
 * <pre>
 * public class YourTestClass
 * {
 *     // Normally we would initialise the spring application context in another rule.
 *     &#64;ClassRule public static final ApplicationContextInit APP_CONTEXT_RULE = new ApplicationContextInit();
 *     
 *     // We pass the rule that creates the spring application context.
 *     // This rule will give us a tenant with the domain 'testtenant'.
 *     &#64;Rule public final AlfrescoTenant tenant = new AlfrescoTenant(APP_CONTEXT_RULE, "testtenant");
 *     
 *     &#64;Test public void aTestMethod()
 *     {
    	    tenant.runAsSystem(new TenantRunAsWork<Void>() {
                &#64;Override
			    public Void doWork() throws Exception {
			       // Do something as the tenant system user.
			    }
			});
 *     }
 * }
 * </pre>
 * 
 * @author Alex Miller
 * @see AlfrescoPerson Consider using {@link AlfrescoPerson} instead, which will create tenants as needed when run in a Cloud build.
 */
public class AlfrescoTenant extends AbstractRule
{
	public static final String ADMIN_PASSWORD = "password"; 
	
    private final String tenantName;
    
    /**
     * Constructs the rule with a spring ApplicationContext.
     * A GUID-generated tenant name will be used for the test tenant.
     * 
     * @param appContext the spring app context (needed to get at Alfresco services).
     */
    public AlfrescoTenant(ApplicationContext appContext)
    {
        this(appContext, GUID.generate());
    }
    
    /**
     * Constructs the rule with a reference to a {@link ApplicationContextInit rule} which can be used to retrieve the ApplicationContext.
     * A GUID-generated  tenant name will be used for the test user.
     * 
     * @param appContextRule a rule which can be used to retrieve the spring app context.
     */
    public AlfrescoTenant(ApplicationContextInit appContextRule)
    {
        this(appContextRule, GUID.generate());
    }
    
    /**
     * Constructs the rule with a spring ApplicationContext.
     * 
     * @param appContext the spring app context (needed to get at Alfresco services).
     * @param userName   the username for the person to be created.
     */
    public AlfrescoTenant(ApplicationContext appContext, String tenantName)
    {
    	super(appContext);
    	this.tenantName = tenantName.toLowerCase();
    }
    
    /**
     * Constructs the rule with a reference to a {@link ApplicationContextInit rule} which can be used to retrieve the ApplicationContext.
     * 
     * @param appContextRule a rule which can be used to retrieve the spring app context.
     * @param tenantName   the name for the tenant to be created.
     */
    public AlfrescoTenant(ApplicationContextInit appContextRule, String tenantName)
    {
        super(appContextRule);
        this.tenantName = tenantName.toLowerCase();
    }
    
    /**
     * Create the tenant.
     */
    @Override protected void before() throws Throwable
    {
        final ApplicationContext appCtx = getApplicationContext();
        RetryingTransactionHelper transactionHelper = appCtx.getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
    	final TenantAdminService tenantAdminService = appCtx.getBean("tenantAdminService", TenantAdminService.class);
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
            	tenantAdminService.createTenant(tenantName, ADMIN_PASSWORD.toCharArray());
            	return null;
            }

        });
    }
    
    /**
     * Remove the tenant
     */
    @Override protected void after()
    {
        final ApplicationContext appCtx = getApplicationContext();
        RetryingTransactionHelper transactionHelper = appCtx.getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
    	final TenantAdminService tenantAdminService = appCtx.getBean("tenantAdminService", TenantAdminService.class);
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                tenantAdminService.deleteTenant(tenantName);
                return null;
            }
        });
    }

    /**
     * @return The tenant domain.
     */
	public String getTenantDomain() {
		return tenantName;
	}

	/**
	 * Do runAsWork as the system user for this tenant.
	 * 
	 * @param runAsWork The work to be done as the system user of this tenant.
	 * @return The result of the work
	 */
	public <T> T runAsSystem(TenantRunAsWork<T> runAsWork) {
		return TenantUtil.runAsSystemTenant(runAsWork, tenantName);
	}
}
