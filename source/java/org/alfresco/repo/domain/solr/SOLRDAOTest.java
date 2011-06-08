/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.domain.solr;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.solr.NodeParameters;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Tests for the SOLR DAO
 *
 * @since 4.0
 */
public class SOLRDAOTest extends TestCase
{
    private ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextHelper.getApplicationContext();

    private AuthenticationComponent authenticationComponent;
    private SOLRDAO solrDAO;
    
    @Override
    public void setUp() throws Exception
    {
        solrDAO = (SOLRDAO)ctx.getBean("solrDAO");
        authenticationComponent = (AuthenticationComponent)ctx.getBean("authenticationComponent");
        
        authenticationComponent.setSystemUserAsCurrentUser();
    }
    
    public void testQueryTransactionsNoLimit()
    {
        long startTime = System.currentTimeMillis() - (5 * 60000L);

        try
        {
            solrDAO.getTransactions(null, startTime, 0);
            fail("Must have result limit");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }
    
    public void testQueryTransactionsTime()
    {
        long startTime = System.currentTimeMillis() + (5 * 60000L);             // The future
        List<Transaction> txns = solrDAO.getTransactions(null, startTime, 50);
        assertTrue("Transaction count not limited", txns.size() == 0);
    }
    
    public void testQueryTransactionsLimit()
    {
        List<Transaction> txns = solrDAO.getTransactions(null, 0L, 50);
        assertTrue("Transaction count not limited", txns.size() <= 50);
    }
    
    public void testGetNodesSimple()
    {
        long startTime = 0L;

        List<Transaction> txns = solrDAO.getTransactions(null, startTime, 500);

        List<Long> txnIds = toTxnIds(txns);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(txnIds);
        nodeParameters.setStoreProtocol(StoreRef.PROTOCOL_WORKSPACE);
        nodeParameters.setStoreIdentifier(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier());
        
        List<Node> nodes = solrDAO.getNodes(nodeParameters);
        assertTrue("Expect 'some' nodes associated with txns", nodes.size() > 0);
    }
    
    public void testGetNodesForStore()
    {
        List<Transaction> txns = solrDAO.getTransactions(null, null, 500);

        List<Long> txnIds = toTxnIds(txns);

        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(txnIds);
        
        List<Node> nodes = solrDAO.getNodes(nodeParameters);
        assertTrue("Expect 'some' nodes associated with txns", nodes.size() > 0);
    }
    
    public void testGetNodesForTxnRange()
    {
        List<Transaction> txns = solrDAO.getTransactions(null, null, 500);

        List<Long> txnIds = toTxnIds(txns);
        
        // Only works if there are transactions
        if (txnIds.size() < 2)
        {
            // Nothing to test
            return;
        }
        
        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setFromTxnId(txnIds.get(0));
        nodeParameters.setToTxnId(txnIds.get(1));
        
        List<Node> nodes = solrDAO.getNodes(nodeParameters);
        assertTrue("Expect 'some' nodes associated with txns", nodes.size() > 0);
    }
    
    private List<Long> toTxnIds(List<Transaction> txns)
    {
        List<Long> txnIds = new ArrayList<Long>(txns.size());
        for(Transaction txn : txns)
        {
            txnIds.add(txn.getId());
        }
        
        return txnIds;
    }
}
