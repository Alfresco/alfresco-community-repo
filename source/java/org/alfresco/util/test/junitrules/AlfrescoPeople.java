/*
 * Copyright (C) 2005-2012
 Alfresco Software Limited.
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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * This JUnit rule can be used to setup and teardown a set of Alfresco users for test purposes.
 * <p/>
 * Example usage:
 * <pre>
 * public class YourTestClass
 * {
 *     // Normally we would initialise the spring application context in another rule.
 *     &#64;ClassRule public static final ApplicationContextInit APP_CONTEXT_RULE = new ApplicationContextInit();
 *     
 *     // This rule will give us 8 GUID-named users.
 *     &#64;Rule public final AlfrescoPeople testPeople = new AlfrescoPeople(APP_CONTEXT_RULE, 8);
 *     
 *     &#64;Test public void aTestMethod()
 *     {
 *         Set&#60;String&#62; userNames = testPeople.getUsernames();
 *         // etc
 *     }
 * }
 * </pre>
 * 
 * @author Neil Mc Erlean
 * @since Odin
 */
public class AlfrescoPeople extends AbstractPersonRule
{
    private static final Log log = LogFactory.getLog(AlfrescoPeople.class);
    
    private final int personCount;
    
    /**
     * A Map (username: person nodeRef) of created users.
     */
    Map<String, NodeRef> usersPersons = new TreeMap<String, NodeRef>();
    
    /**
     * @param appContext the spring app context (needed to get at Alfresco services).
     * @param personCount the number of users to be created
     */
    public AlfrescoPeople(ApplicationContext appContext, int personCount)
    {
        super(appContext);
        this.personCount = personCount;
    }
    
    /**
     * @param appContextRule a rule which can be used to retrieve the spring app context.
     * @param personCount the number of users to be created
     */
    public AlfrescoPeople(ApplicationContextInit appContextRule, int personCount)
    {
        super(appContextRule);
        this.personCount = personCount;
    }
    
    @Override protected void before() throws Throwable
    {
        // Set up required services
        ApplicationContext ctxt = getApplicationContext();
        final RetryingTransactionHelper transactionHelper = (RetryingTransactionHelper) ctxt.getBean("retryingTransactionHelper");
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Creating " + personCount + " users for test purposes...");
                }
                
                for (int i = 0; i < personCount; i++)
                {
                    final String userName = GUID.generate();
                    NodeRef personNode = createPerson(userName);
                    usersPersons.put(userName, personNode);
                }
                
                return null;
            }
        });
    }
    
    @Override protected void after()
    {
        // Set up required services
        ApplicationContext ctxt = getApplicationContext();
        final RetryingTransactionHelper transactionHelper = (RetryingTransactionHelper) ctxt.getBean("retryingTransactionHelper");
        
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override public Void execute() throws Throwable
            {
                for (Map.Entry<String, NodeRef> entry : usersPersons.entrySet())
                {
                    deletePerson(entry.getKey());
                }
                
                return null;
            }
        });
    }
    
    /**
     * @return the usernames of the people created by this rule.
     */
    public Set<String> getUsernames()
    {
        return this.usersPersons.keySet();
    }
}
