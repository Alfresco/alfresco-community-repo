/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.propval;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.springframework.context.ApplicationContext;

/**
 * @see PropertyValueDAO
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class PropertyValueDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private PropertyValueDAO propertyValueDAO;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        propertyValueDAO = (PropertyValueDAO) ctx.getBean("propertyValueDAO");
    }
    
    public void testPropertyClass() throws Exception
    {
        final Class<?> clazz = this.getClass();
        RetryingTransactionCallback<Pair<Long, Class<?>>> createClassCallback = new RetryingTransactionCallback<Pair<Long, Class<?>>>()
        {
            public Pair<Long, Class<?>> execute() throws Throwable
            {
                // Get the classes
                return propertyValueDAO.getOrCreatePropertyClass(clazz);
            }
        };
        final Pair<Long, Class<?>> clazzEntityPair = txnHelper.doInTransaction(createClassCallback, false);
        assertNotNull(clazzEntityPair);
        assertNotNull(clazzEntityPair.getFirst());
        assertEquals(clazz, clazzEntityPair.getSecond());
        // Now retrieve it
        RetryingTransactionCallback<Void> getClassCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                Pair<Long, Class<?>> checkPair1 = propertyValueDAO.getPropertyClass(clazzEntityPair.getFirst());
                assertEquals(clazzEntityPair, checkPair1);
                Pair<Long, Class<?>> checkPair2 = propertyValueDAO.getPropertyClass(clazzEntityPair.getSecond());
                assertEquals(clazzEntityPair, checkPair2);
                return null;
            }
        };
        txnHelper.doInTransaction(getClassCallback, true);
        
        // Test failure when requesting invalid ID
        RetryingTransactionCallback<Void> badGetCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                propertyValueDAO.getPropertyClass(Long.MIN_VALUE);
                return null;
            }
        };
        try
        {
            txnHelper.doInTransaction(badGetCallback, false);
            fail("Expected exception when using invalid ID.");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
        
        // Test null caching
        RetryingTransactionCallback<Void> noHitCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                propertyValueDAO.getPropertyClass(this.getClass());
                propertyValueDAO.getPropertyClass(this.getClass());
                return null;
            }
        };
        txnHelper.doInTransaction(noHitCallback, false);
    }
}
