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
package org.alfresco.repo.props;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * @see PropertyValueComponent
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyValueComponentTest extends TestCase
{
    private static final Log logger = LogFactory.getLog(PropertyValueComponentTest.class);
    
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private TransactionService transactionService;
    private PropertyValueComponent propertyValueComponent;
    
    @Override
    public void setUp()
    {
        transactionService = (TransactionService) ctx.getBean("transactionService");
        propertyValueComponent = (PropertyValueComponent) ctx.getBean("propertyValueComponent");
    }
    
    public void testSetUp()
    {
    }
    
    public void testUniqueContext_UpdateFromNothing()
    {
        // The non-existent source properties
        final String value1 = GUID.generate();
        final String value2 = GUID.generate();
        final String value3 = GUID.generate();
        // The target properties
        final String context = getName();
        final StoreRef store = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        final String uuid1 = GUID.generate();
        final String uuid2 = GUID.generate();
        
        RetryingTransactionCallback<Long> updateCallback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                return propertyValueComponent.updatePropertyUniqueContext(
                        value1, value2, value3,
                        context, store, uuid1);
            }
        };
        final Long id1 = transactionService.getRetryingTransactionHelper().doInTransaction(updateCallback);
        // Now attempt to create the context again and check for failure
        RetryingTransactionCallback<Long> updateAgainCallback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                return propertyValueComponent.updatePropertyUniqueContext(
                        context, store, uuid1,
                        context, store, uuid2);
            }
        };
        final Long id2 = transactionService.getRetryingTransactionHelper().doInTransaction(updateAgainCallback);
        assertEquals("The ID should be reused for the update", id1, id2);
        // Now create the uuid1 again (should work)
        final Long id3 = transactionService.getRetryingTransactionHelper().doInTransaction(updateCallback);
        assertNotSame("Should have created a new instance", id1, id3);
        
        // Now test failure
        RetryingTransactionCallback<Long> updateFailureCallback = new RetryingTransactionCallback<Long>()
        {
            public Long execute() throws Throwable
            {
                return propertyValueComponent.updatePropertyUniqueContext(
                        context, store, uuid1,
                        context, store, uuid2);
            }
        };
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(updateFailureCallback);
            fail("Expected to get a PropertyUniqueConstraintViolation");
        }
        catch (PropertyUniqueConstraintViolation e)
        {
            // Expected
            if (logger.isDebugEnabled())
            {
                logger.debug("Expected exception: " + e.getMessage());
            }
        }
        
        // Delete everything for the store and check that both are creatable again
        RetryingTransactionCallback<Void> deleteStoreCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                propertyValueComponent.deletePropertyUniqueContexts(context, store);
                propertyValueComponent.createPropertyUniqueContext(context, store, uuid1);
                propertyValueComponent.createPropertyUniqueContext(context, store, uuid2);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteStoreCallback);
        
        // Delete everything for the context and check that both are creatable again
        RetryingTransactionCallback<Void> deleteContextCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                propertyValueComponent.deletePropertyUniqueContexts(context);
                propertyValueComponent.createPropertyUniqueContext(context, store, uuid1);
                propertyValueComponent.createPropertyUniqueContext(context, store, uuid2);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(deleteContextCallback);
    }
}
