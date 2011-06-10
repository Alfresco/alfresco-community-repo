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
package org.alfresco.repo.domain.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.locale.LocaleDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.security.encryption.EncryptionEngine;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.EqualsHelper;
import org.springframework.context.ApplicationContext;

/**
 * Test low-level marshalling and unmarshalling of node properties
 * 
 * @see NodePropertyHelper 
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodePropertyHelperTest extends TestCase
{
    private static final QName QN_BOOLEAN = createQName("boolean");
    private static final QName QN_INTEGER = createQName("integer");
    private static final QName QN_LONG = createQName("long");
    private static final QName QN_FLOAT = createQName("float");
    private static final QName QN_TEXT = createQName("text");
    private static final QName QN_MLTEXT = createQName("mltext");
    private static final QName QN_REF = createQName("ref");
    private static final QName QN_ANY = createQName("any");
    
    /**
     * @return              Returns a QName that uses the localname
     */
    private static QName createQName(String localName)
    {
        return QName.createQName("test", "local-" + localName);
    }

    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private NodePropertyHelper helper;
    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;

    @Override
    public void setUp()
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
        QNameDAO qnameDAO = (QNameDAO) ctx.getBean("qnameDAO");
        LocaleDAO localeDAO = (LocaleDAO) ctx.getBean("localeDAO");
        ContentDataDAO contentDataDAO = (ContentDataDAO) ctx.getBean("contentDataDAO");
        EncryptionEngine encryptionEngine = (EncryptionEngine) ctx.getBean("encryptionEngine");

        helper = new NodePropertyHelper(dictionaryService, qnameDAO, localeDAO, contentDataDAO, encryptionEngine);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.setMinRetryWaitMs(10);
        txnHelper.setRetryWaitIncrementMs(10);
        txnHelper.setMaxRetryWaitMs(50);
    }

    /**
     * Converts properties back and forth and ensures that the result is unchanged or
     * at least doesn't show up in a deep equals check.
     */
    private void marshallAndUnmarshall(final Map<QName, Serializable> in, final boolean exact) throws Throwable
    {
        RetryingTransactionCallback<Void> txnCallback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                String diffReport;
                // Convert to raw and back
                Map<NodePropertyKey, NodePropertyValue> rawProps1 = helper.convertToPersistentProperties(in);
                Map<QName, Serializable> props1 = helper.convertToPublicProperties(rawProps1);
                // We can't be sure that we have what we started with, because there may have been
                // some mandatory conversions to the types defined by the model i.e. the values
                // will get converted to the dictionary type rather than the incoming type
                if (exact)
                {
                    diffReport = EqualsHelper.getMapDifferenceReport(props1, in);
                    assertNull(diffReport, diffReport);
                }
                // Convert back to raw again
                Map<NodePropertyKey, NodePropertyValue> rawProps2 = helper.convertToPersistentProperties(in);
                diffReport = EqualsHelper.getMapDifferenceReport(rawProps2, rawProps1);
                assertNull(diffReport, diffReport);
                // But now, on the second time out, we expect to get exactly what we got before
                Map<QName, Serializable> props2 = helper.convertToPublicProperties(rawProps2);
                diffReport = EqualsHelper.getMapDifferenceReport(props2, props1);
                assertNull(diffReport, diffReport);

                return null;
            }
        };
        txnHelper.doInTransaction(txnCallback);
    }
    
    /**
     * Tests simple, well-typed nulls
     */
    public void testNullKnownValues() throws Throwable
    {
        Map<QName, Serializable> in = new HashMap<QName, Serializable>(17);
        in.put(ContentModel.PROP_AUTO_VERSION, null);           // d:boolean
        in.put(ContentModel.PROP_HITS, null);                   // d:int
        in.put(ContentModel.PROP_SIZE_CURRENT, null);           // d:long
        in.put(ContentModel.PROP_RATING_SCORE, null);           // d:float
        in.put(ContentModel.PROP_NAME, null);                   // d:text
        in.put(ContentModel.PROP_TITLE, null);                  // d:mltext
        in.put(ContentModel.PROP_REFERENCE, null);              // d:noderef
        in.put(VersionModel.PROP_QNAME_VALUE, null);            // d:any
        
        marshallAndUnmarshall(in, true);
    }
    
    /**
     * Tests simple, well-typed values
     */
    public void testSimpleKnownValues() throws Throwable
    {
        Map<QName, Serializable> in = new HashMap<QName, Serializable>(17);
        in.put(ContentModel.PROP_AUTO_VERSION, Boolean.TRUE);
        in.put(ContentModel.PROP_HITS, new Integer(1));
        in.put(ContentModel.PROP_SIZE_CURRENT, new Long(2L));
        in.put(ContentModel.PROP_RATING_SCORE, new Float(3.0));
        in.put(ContentModel.PROP_NAME, "four");
        in.put(ContentModel.PROP_TITLE, new MLText("five"));
        in.put(ContentModel.PROP_REFERENCE, new NodeRef("protocol://identifier/six"));
        in.put(VersionModel.PROP_QNAME_VALUE, Locale.CANADA);
        
        marshallAndUnmarshall(in, true);
    }
    
    /**
     * Tests simple, well-typed values that need conversion
     */
    public void testConvertableKnownValues() throws Throwable
    {
        Map<QName, Serializable> in = new HashMap<QName, Serializable>(17);
        in.put(ContentModel.PROP_AUTO_VERSION, "TRUE");
        in.put(ContentModel.PROP_HITS, "1");
        in.put(ContentModel.PROP_SIZE_CURRENT, "2");
        in.put(ContentModel.PROP_RATING_SCORE, "3.0");
        in.put(ContentModel.PROP_NAME, new MLText("four"));
        in.put(ContentModel.PROP_TITLE, "five");
        in.put(ContentModel.PROP_REFERENCE, "protocol://identifier/six");
        in.put(VersionModel.PROP_QNAME_VALUE, "en_CA_");
        
        marshallAndUnmarshall(in, false);
    }
    
    /**
     * Tests simple, residual nulls
     */
    public void testNullResidualValues() throws Throwable
    {
        Map<QName, Serializable> in = new HashMap<QName, Serializable>(17);
        in.put(QN_TEXT, null);
        
        marshallAndUnmarshall(in, true);
    }
    
    /**
     * Tests simple, residual values
     */
    public void testSimpleResidualValues() throws Throwable
    {
        Map<QName, Serializable> in = new HashMap<QName, Serializable>(17);
        in.put(QN_BOOLEAN, Boolean.TRUE);
        in.put(QN_INTEGER, new Integer(1));
        in.put(QN_LONG, new Long(2L));
        in.put(QN_FLOAT, new Float(3.0));
        in.put(QN_TEXT, "four");
        in.put(QN_MLTEXT, new MLText("five"));
        in.put(QN_REF, new NodeRef("protocol://identifier/six"));
        in.put(QN_ANY, Locale.CANADA);
        
        marshallAndUnmarshall(in, true);
    }
    
    /**
     * Tests simple multi-value type
     */
    public void testSimpleMultiValue() throws Throwable
    {
        Map<QName, Serializable> in = new HashMap<QName, Serializable>(17);

        in.put(ContentModel.PROP_ADDRESSEES, null);
        marshallAndUnmarshall(in, true);
        
        in.put(ContentModel.PROP_ADDRESSEES, (Serializable) Arrays.<String>asList());
        marshallAndUnmarshall(in, true);
        
        in.put(ContentModel.PROP_ADDRESSEES, (Serializable) Arrays.<String>asList("A"));
        marshallAndUnmarshall(in, true);
        
        in.put(ContentModel.PROP_ADDRESSEES, (Serializable) Arrays.<String>asList("A", "B"));
        marshallAndUnmarshall(in, true);
    }
    
    /**
     * Tests d:any multi-value type
     */
    public void testAnyMultiValue() throws Throwable
    {
        Map<QName, Serializable> in = new HashMap<QName, Serializable>(17);

        in.put(VersionModel.PROP_QNAME_VALUE, null);
        marshallAndUnmarshall(in, true);
        
        in.put(VersionModel.PROP_QNAME_VALUE, (Serializable) Arrays.<String>asList());
        marshallAndUnmarshall(in, true);
        
        in.put(VersionModel.PROP_QNAME_VALUE, (Serializable) Arrays.<String>asList("A"));
        marshallAndUnmarshall(in, true);
        
        in.put(VersionModel.PROP_QNAME_VALUE, (Serializable) Arrays.<String>asList("A", "B"));
        marshallAndUnmarshall(in, true);
    }
    
    /**
     * Tests residual multi-value type
     */
    public void testResidualMultiValue() throws Throwable
    {
        Map<QName, Serializable> in = new HashMap<QName, Serializable>(17);

        in.put(QN_ANY, null);
        marshallAndUnmarshall(in, true);
        
        in.put(QN_ANY, (Serializable) Arrays.<String>asList());
        marshallAndUnmarshall(in, true);
        
        in.put(QN_ANY, (Serializable) Arrays.<String>asList("A"));
        marshallAndUnmarshall(in, true);
        
        in.put(QN_ANY, (Serializable) Arrays.<String>asList("A", "B"));
        marshallAndUnmarshall(in, true);

        // Collection of collections
        ArrayList<Serializable> arrayListVal = new ArrayList<Serializable>(2);
        HashSet<Serializable> hashSetVal = new HashSet<Serializable>(2);
        in.put(QN_ANY, (Serializable) Arrays.<Serializable>asList(arrayListVal, hashSetVal));
        marshallAndUnmarshall(in, true);
    }
}