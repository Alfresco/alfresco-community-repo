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
package org.alfresco.repo.domain;

import java.util.Locale;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see PropertyValue
 * 
 * @author Derek Hulley
 */
public class PropertyValueTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    public void testMLText()
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        TransactionService txnService = serviceRegistry.getTransactionService();
        
        RetryingTransactionCallback<Object> doTestCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // single language
                MLText mlText = new MLText(Locale.FRENCH, "bonjour");
                PropertyValue propertyValue = new PropertyValue(DataTypeDefinition.MLTEXT, mlText);
                assertEquals("MLText not persisted as a string", "bonjour", propertyValue.getStringValue());
                
                try
                {
                    // multiple languages
                    mlText = new MLText(Locale.GERMAN, "hallo");
                    mlText.addValue(Locale.ITALIAN, "ciao");
                    propertyValue = new PropertyValue(DataTypeDefinition.MLTEXT, mlText);
                    
                    fail();
                }
                catch (UnsupportedOperationException uoe)
                {
                    // expected
                    // NOTE: since 2.2.1, PropertyValue is only used by AVM (which does not natively support MLText, other than single/default string)
                }
                
                // single language - empty string
                mlText = new MLText(Locale.FRENCH, "");
                propertyValue = new PropertyValue(DataTypeDefinition.MLTEXT, mlText);
                assertEquals("MLText not persisted as an empty string", "", propertyValue.getStringValue());
                
                // single language - null string
                mlText = new MLText(Locale.GERMAN, null);
                propertyValue = new PropertyValue(DataTypeDefinition.MLTEXT, mlText);
                assertNull("MLText not persisted as a null string", propertyValue.getStringValue());
                
                return null;
            }
        };
        txnService.getRetryingTransactionHelper().doInTransaction(doTestCallback, false);
    }
}
