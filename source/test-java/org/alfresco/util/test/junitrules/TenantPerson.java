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
package org.alfresco.util.test.junitrules;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.util.ParameterCheck;


/**
 * This JUnit rule can be used to setup and teardown a single Alfresco user, within a tenant, for test purposes.
 * <p/>
 * Example usage:
 * <pre>
 * public class YourTestClass
 * {
 *     // Initialise the spring application context and tenant in a rule chain
 *     public static final ApplicationContextInit APP_CONTEXT_RULE = new ApplicationContextInit();
 *     public static final AlfrescoTenant TENANT_RULE = new AlfrescoTenant(APP_CONTEXT_RULE, "testtenant");
 *     
 *     &#64;ClassRule public static RuleChain ruleChain = RuleChain.outerRule(APP_CONTEXT_INIT)
 *         		                                                .around(TENANT);
 *     
 *     // We pass the rule that creates the spring application context.
 *     // This rule will give us a user with username 'AlexM@testtenant'.
 *     &#64;Rule public final TenantPerson namedPerson = new AlfrescoPerson(APP_CONTEXT_RULE, "AlexM@testTenant", TENANT);
 *     
 *     &#64;Test public void aTestMethod()
 *     {
 *         	AUSTRALIAN_USER.runAsFullyAuthenticated(new TenantRunAsWork<Void>()
 *          {
 *          	@Override
 *          	public Void doWork() throws Exception
 *          	{
 *          		// Do something as the tenant user
 *          	}
 *          });
 *      }
 * }
 * </pre>
 * 
 * @author Alex Miller
 */
public class TenantPerson extends AlfrescoPerson 
{

	private AlfrescoTenant tenant;

	/**
	 * Constructs the rule with a reference to a {@link ApplicationContextInit rule} which can be used to retrieve the ApplicationContext.
	 * 
	 * @param appContextInit a rule which can be used to retrieve the spring app context.
	 * @param userName   the username for the person to be created.
	 * @param tenant    the tenant the person should be created under.
	 */
	public TenantPerson(ApplicationContextInit appContextInit, String userName, AlfrescoTenant tenant) {
		super(appContextInit, userName + "@" + tenant.getTenantDomain());
		ParameterCheck.mandatory("tenant", tenant);
		this.tenant = tenant;
	}

	/**
	 * Create the user, in the given tenant, using the tenant system user.
	 */
    @Override protected void before()
    {
		tenant.runAsSystem(new TenantRunAsWork<Void>() {

			@Override
			public Void doWork() throws Exception {
				TenantPerson.super.before();
				return null;
			}
		});
    }

    /**
     * Remove the user, using the system user for the tenant.
     */
    @Override protected void after()
    {
		tenant.runAsSystem(new TenantRunAsWork<Void>() {

			@Override
			public Void doWork() throws Exception {
				TenantPerson.super.after();
				return null;
			}
		});
    }
    
    /**
     * Do runAsWork as the fully authenticated user managed by this class,
     * 
     * @param runAsWork
     * @return The result of runAsWork
     */
	public <T> T runAsFullyAuthenticated(TenantRunAsWork<T> runAsWork) 
	{
		AuthenticationUtil.pushAuthentication();
		AuthenticationUtil.setFullyAuthenticatedUser(getUsername());
		try 
		{
			return TenantUtil.runAsUserTenant(runAsWork, getUsername(), tenant.getTenantDomain());
		}
		finally 
		{
			AuthenticationUtil.popAuthentication();
		}
	}

	/**
	 * Get the tenant name of the users tenant.
	 */
	public String getTenantName() {
		return tenant.getTenantDomain();
	}
}
