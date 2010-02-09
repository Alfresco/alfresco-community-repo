/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.qname;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.springframework.extensions.surf.util.Pair;
import org.springframework.context.ApplicationContext;

/**
 * @see QNameDAO
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class QNameDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private QNameDAO qnameDAO;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        qnameDAO = (QNameDAO) ctx.getBean("qnameDAO");
    }
    
    private Pair<Long, String> getNamespace(final String uri, final boolean autoCreate, boolean expectSuccess)
    {
        RetryingTransactionCallback<Pair<Long, String>> callback = new RetryingTransactionCallback<Pair<Long, String>>()
        {
            public Pair<Long, String> execute() throws Throwable
            {
                Pair<Long, String> namespacePair = null;
                if (autoCreate)
                {
                    namespacePair = qnameDAO.getOrCreateNamespace(uri);
                }
                else
                {
                    namespacePair = qnameDAO.getNamespace(uri);
                }
                return namespacePair;
            }
        };
        try
        {
            return txnHelper.doInTransaction(callback, !autoCreate, false);
        }
        catch (Throwable e)
        {
            if (expectSuccess)
            {
                // oops
                throw new RuntimeException("Expected to get namespace '" + uri + "'.", e);
            }
            else
            {
                return null;
            }
        }
    }
    
    private Pair<Long, QName> getQName(final QName qname, final boolean autoCreate, boolean expectSuccess)
    {
        RetryingTransactionCallback<Pair<Long, QName>> callback = new RetryingTransactionCallback<Pair<Long, QName>>()
        {
            public Pair<Long, QName> execute() throws Throwable
            {
                Pair<Long, QName> qnamePair = null;
                if (autoCreate)
                {
                    qnamePair = qnameDAO.getOrCreateQName(qname);
                }
                else
                {
                    qnamePair = qnameDAO.getQName(qname);
                }
                return qnamePair;
            }
        };
        try
        {
            return txnHelper.doInTransaction(callback, !autoCreate, false);
        }
        catch (Throwable e)
        {
            if (expectSuccess)
            {
                // oops
                throw new RuntimeException("Expected to get qname '" + qname + "'.", e);
            }
            else
            {
                return null;
            }
        }
    }
    
    public void testCreateNamespace() throws Exception
    {
        // Create a namespace
        String uri = GUID.generate();
        Pair<Long, String> namespacePair = getNamespace(uri, true, true);
        // Check that it can be retrieved
        Pair<Long, String> namespacePairCheck = getNamespace(namespacePair.getSecond(), false, true);
        assertEquals("Namespace ID changed", namespacePair.getFirst(), namespacePairCheck.getFirst());
        // Check the duplicate checking
        getNamespace(uri, true, false);
    }
    
    public void testCreateNamespaceEmpty() throws Exception
    {
        // Create a namespace
        String uri = "";
        Pair<Long, String> namespacePair = getNamespace(uri, true, true);
        // Check that it can be retrieved
        Pair<Long, String> namespacePairCheck = getNamespace(namespacePair.getSecond(), false, true);
        assertEquals("Namespace ID changed", namespacePair.getFirst(), namespacePairCheck.getFirst());
    }
    
    public void testUpdateNamespace() throws Exception
    {
        // Create a namespace
        final String uri = GUID.generate();
        Pair<Long, String> namespacePair = getNamespace(uri, true, true);
        // Use namespace in a QName
        QName qnameOld = QName.createQName(uri, GUID.generate());
        Pair<Long, QName> qnameOldPair = getQName(qnameOld, true, true);
        // Now update it
        final String uri2 = GUID.generate();
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                qnameDAO.updateNamespace(uri, uri2);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        
        // Make sure that the old QName is gone (checks caching)
        getQName(qnameOld, false, false);
        
        // The new QName should be there
        QName qnameNew = QName.createQName(uri2, qnameOld.getLocalName());
        getQName(qnameNew, false, true);
        
        // Should be able to create the original namespace again
        Pair<Long, String> namespacePairAgain = getNamespace(uri, true, true);
        assertNotSame("Should have a new namespace ID", namespacePair.getFirst(), namespacePairAgain.getFirst());
    }

    public void testCreateQName() throws Exception
    {
        // Create a qname
        QName qname = QName.createQName(getName(), GUID.generate());
        Pair<Long, QName> qnamePair = getQName(qname, true, true);
        // Check that it can be retrieved
        Pair<Long, QName> qnamePairCheck = getQName(qnamePair.getSecond(), false, true);
        assertEquals("QName ID changed", qnamePair.getFirst(), qnamePairCheck.getFirst());
        // Check the duplicate checking
        getQName(qname, true, false);
    }
    
    public void testUpdateQName() throws Exception
    {
        // Create a qname
        final QName qnameOld = QName.createQName(GUID.generate(), GUID.generate());
        Pair<Long, QName> qnamePairOld = getQName(qnameOld, true, true);
        // Now update it
        final QName qnameNew = QName.createQName(GUID.generate(), GUID.generate());
        RetryingTransactionCallback<Pair<Long, QName>> callback = new RetryingTransactionCallback<Pair<Long, QName>>()
        {
            public Pair<Long, QName> execute() throws Throwable
            {
                return qnameDAO.updateQName(qnameOld, qnameNew);
            }
        };
        Pair<Long, QName> qnamePairNew = transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        // The ID must be the same
        assertEquals("New QName is incorrect", qnameNew, qnamePairNew.getSecond());
        assertEquals("ID did not remain the same", qnamePairOld.getFirst(), qnamePairNew.getFirst());
        // The old QName should not be there
        getQName(qnameOld, false, false);
        // The new QName should be there
        getQName(qnameNew, false, true);
    }
}
