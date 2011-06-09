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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.solr.Acl;
import org.alfresco.repo.solr.AclChangeSet;
import org.alfresco.repo.solr.NodeParameters;
import org.alfresco.repo.solr.Transaction;
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
    
    public void testQueryChangeSets_NoLimit()
    {
        long startTime = System.currentTimeMillis() - (5 * 60000L);

        try
        {
            solrDAO.getAclChangeSets(null, startTime, 0);
            fail("Must have result limit");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }
    
    public void testQueryChangeSets_Time()
    {
        long startTime = System.currentTimeMillis() + (5 * 60000L);             // The future
        List<AclChangeSet> results = solrDAO.getAclChangeSets(null, startTime, 50);
        assertTrue("ChangeSet count not limited", results.size() == 0);
    }
    
    public void testQueryChangeSets_Limit()
    {
        List<AclChangeSet> results = solrDAO.getAclChangeSets(null, 0L, 50);
        assertTrue("Transaction count not limited", results.size() <= 50);
    }
    
    /**
     * Argument checks.
     */
    public void testQueryAcls_Arguments()
    {
        try
        {
            // No IDs
            solrDAO.getAcls(Collections.<Long>emptyList(), null, 50);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
        try
        {
            // No limit on results
            solrDAO.getAcls(Collections.singletonList(1L), null, 0);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }
    
    public void testQueryAcls_All()
    {
        // Do a query for some changesets
        List<AclChangeSet> aclChangeSets = solrDAO.getAclChangeSets(null, 0L, 50);
        
        // Choose some changesets with changes
        int aclTotal = 0;
        Iterator<AclChangeSet> aclChangeSetsIterator = aclChangeSets.iterator();
        while (aclChangeSetsIterator.hasNext())
        {
            AclChangeSet aclChangeSet = aclChangeSetsIterator.next();
            if (aclChangeSet.getAclCount() == 0)
            {
                aclChangeSetsIterator.remove();
            }
            else
            {
                aclTotal += aclChangeSet.getAclCount();
            }
        }
        // Stop if we don't have ACLs
        if (aclTotal == 0)
        {
            return;
        }
        
        List<Long> aclChangeSetIds = toIds(aclChangeSets);

        // Now use those to query for details
        List<Acl> acls = solrDAO.getAcls(aclChangeSetIds, null, 1000);

        // Check that the ACL ChangeSet IDs are correct
        Set<Long> aclChangeSetIdsSet = new HashSet<Long>(aclChangeSetIds);
        for (Acl acl : acls)
        {
            Long aclChangeSetId = acl.getAclChangeSetId();
            assertTrue("ACL ChangeSet ID not in original list", aclChangeSetIdsSet.contains(aclChangeSetId));
        }
    }
    
    public void testQueryAcls_Single()
    {
        List<AclChangeSet> aclChangeSets = solrDAO.getAclChangeSets(null, 0L, 1000);
        // Find one with multiple ALCs
        AclChangeSet aclChangeSet = null;
        for (AclChangeSet aclChangeSetLoop : aclChangeSets)
        {
            if (aclChangeSetLoop.getAclCount() > 1)
            {
                aclChangeSet = aclChangeSetLoop;
                break;
            }
        }
        if (aclChangeSet == null)
        {
            // Nothing to test: Very unlikely
            return;
        }
        
        // Loop a few times and check that the count is correct
        Long aclChangeSetId = aclChangeSet.getId();
        List<Long> aclChangeSetIds = Collections.singletonList(aclChangeSetId);
        int aclCount = aclChangeSet.getAclCount();
        int totalAclCount = 0;
        Long minAclId = null;
        while (true)
        {
            List<Acl> acls = solrDAO.getAcls(aclChangeSetIds, minAclId, 1);
            if (acls.size() == 0)
            {
                break;
            }
            assertEquals("Expected exactly one result", 1, acls.size());
            totalAclCount++;
            minAclId = acls.get(0).getId() + 1;
        }
        assertEquals("Expected to page to exact number of results", aclCount, totalAclCount);
    }
    
    private List<Long> toIds(List<AclChangeSet> aclChangeSets)
    {
        List<Long> ids = new ArrayList<Long>(aclChangeSets.size());
        for (AclChangeSet aclChangeSet : aclChangeSets)
        {
            ids.add(aclChangeSet.getId());
        }
        return ids;
    }
    
    public void testQueryTransactions_NoLimit()
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
    
    public void testQueryTransactions_Time()
    {
        long startTime = System.currentTimeMillis() + (5 * 60000L);             // The future
        List<Transaction> results = solrDAO.getTransactions(null, startTime, 50);
        assertTrue("Transaction count not limited", results.size() == 0);
    }
    
    public void testQueryTransactions_Limit()
    {
        List<Transaction> results = solrDAO.getTransactions(null, 0L, 50);
        assertTrue("Transaction count not limited", results.size() <= 50);
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
