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

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.springframework.context.ApplicationContext;

/**
 * This JUnit rule can be used to setup and teardown a single Alfresco user for test purposes.
 * <p/>
 * Example usage:
 * <pre>
 * public class YourTestClass
 * {
 *     // Normally we would initialise the spring application context in another rule.
 *     &#64;ClassRule public static final ApplicationContextInit APP_CONTEXT_RULE = new ApplicationContextInit();
 *     
 *     // We pass the rule that creates the spring application context.
 *     // This rule will give us a user with username 'NeilM'.
 *     &#64;Rule public final AlfrescoPerson namedPerson = new AlfrescoPerson(APP_CONTEXT_RULE, "NeilM");
 *     // This rule with give us a user with a GUID-generated name.
 *     &#64;Rule public final AlfrescoPerson guidPerson = new AlfrescoPerson(APP_CONTEXT_RULE);
 *     
 *     &#64;Test public void aTestMethod()
 *     {
 *         AuthenticationUtil.setFullyAuthenticatedUser(namedPerson.getUsername());
 *         // etc
 *     }
 * }
 * </pre>
 * 
 * @author Neil Mc Erlean
 * @since Odin
 */
public class AlfrescoPerson extends AbstractPersonRule
{
    private final String userName;
    
    private NodeRef personNodeRef;
    
    /**
     * Constructs the rule with a spring ApplicationContext.
     * A GUID-generated username will be used for the test user.
     * 
     * @param appContext the spring app context (needed to get at Alfresco services).
     */
    public AlfrescoPerson(ApplicationContext appContext)
    {
        this(appContext, GUID.generate());
    }
    
    /**
     * Constructs the rule with a reference to a {@link ApplicationContextInit rule} which can be used to retrieve the ApplicationContext.
     * A GUID-generated username will be used for the test user.
     * 
     * @param appContextRule a rule which can be used to retrieve the spring app context.
     */
    public AlfrescoPerson(ApplicationContextInit appContextRule)
    {
        this(appContextRule, GUID.generate());
    }
    
    /**
     * Constructs the rule with a spring ApplicationContext.
     * 
     * @param appContext the spring app context (needed to get at Alfresco services).
     * @param userName   the username for the person to be created.
     */
    public AlfrescoPerson(ApplicationContext appContext, String userName)
    {
        super(appContext);
        this.userName = userName;
    }
    
    /**
     * Constructs the rule with a reference to a {@link ApplicationContextInit rule} which can be used to retrieve the ApplicationContext.
     * 
     * @param appContextRule a rule which can be used to retrieve the spring app context.
     * @param userName   the username for the person to be created.
     */
    public AlfrescoPerson(ApplicationContextInit appContextRule, String userName)
    {
        super(appContextRule);
        this.userName = userName;
    }
    
    @Override protected void before()
    {
        ApplicationContext ctxt = getApplicationContext();
        RetryingTransactionHelper transactionHelper = (RetryingTransactionHelper) ctxt.getBean("retryingTransactionHelper");
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                personNodeRef = createPerson(userName);
                
                return null;
            }
        });
    }
    
    @Override protected void after()
    {
        ApplicationContext ctxt = getApplicationContext();
        RetryingTransactionHelper transactionHelper = (RetryingTransactionHelper) ctxt.getBean("retryingTransactionHelper");
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                deletePerson(userName);
                
                return null;
            }
        });
    }
    
    /**
     * @return the username of the person created by this rule.
     */
    public String getUsername()
    {
        return this.userName;
    }
    
    /**
     * Gets the {@link NodeRef person node}.
     * @return the person node.
     */
    public NodeRef getPersonNode()
    {
        return this.personNodeRef;
    }
}
