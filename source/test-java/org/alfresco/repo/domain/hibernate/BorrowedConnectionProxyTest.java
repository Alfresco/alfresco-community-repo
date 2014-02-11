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
package org.alfresco.repo.domain.hibernate;

import java.sql.Connection;

import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.hibernate.HibernateException;
import org.hibernate.jdbc.BorrowedConnectionProxy;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Tests the {@link BorrowedConnectionProxy}'s ability to detect post-close reuse.
 * 
 * @author Derek Hulley
 */
@Category(OwnJVMTestsCategory.class)
public class BorrowedConnectionProxyTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private DataSource dataSource;
    
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) ctx.getBean("transactionComponent");
        dataSource = (DataSource) ctx.getBean("dataSource");
    }
    
    public void testSimpleCommit() throws Throwable
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            txn.commit();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
    }
    
    public void testLoggingAutoActivate() throws Throwable
    {
        assertFalse("Auto-logging of misuse of the wrapper should be off", BorrowedConnectionProxy.isCallStackTraced());
        
        UserTransaction txn = transactionService.getUserTransaction();
        Connection connection;
        try
        {
            txn.begin();
            // Dig the proxy out of ... somewhere
            ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
            connection = conHolder.getConnection();
            txn.commit();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
        // Now mess with the connection, which is protected by the Hibernate wrapper
        try
        {
            connection.commit();
            fail("Use case should have generated a HibernateException");
        }
        catch (HibernateException e)
        {
            // Expected
        }
        assertTrue("Auto-logging of misuse of the wrapper should now be on", BorrowedConnectionProxy.isCallStackTraced());

        // Now start a new transaction and we should see logging
        txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            // Dig the proxy out of ... somewhere
            ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
            connection = conHolder.getConnection();
            txn.commit();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
        // Now mess with the connection, which is protected by the Hibernate wrapper
        try
        {
            connection.commit();
            fail("Use case should have generated a HibernateException");
        }
        catch (HibernateException e)
        {
            // Expected
        }
        // Check for error logs
    }
}
