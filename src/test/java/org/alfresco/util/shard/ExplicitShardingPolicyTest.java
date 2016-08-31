/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.util.shard;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

/**
 * @author Andy
 */
public class ExplicitShardingPolicyTest
{

    @Test
    public void tenShards_noReplication_oneNodes()
    {
        ExplicitShardingPolicy policy = new ExplicitShardingPolicy(10, 1, 1);
        assertTrue(policy.configurationIsValid());
        List<Integer> shardIds = policy.getShardIdsForNode(1);
        assertEquals(10, shardIds.size());
        for (int i = 0; i < 10; i++)
        {
            assertTrue(shardIds.contains(i));
        }
        assertEquals(0, policy.getShardIdsForNode(2).size());
        
        for (int i = 0; i < 10; i++)
        {
            List<Integer> nodeInstances = policy.getNodeInstancesForShardId(i);
            assertEquals(1, nodeInstances.size());
        }
    }

    @Test
    public void tenShards_noReplication_tenNodes()
    {
        ExplicitShardingPolicy policy = new ExplicitShardingPolicy(10, 1, 10);
        assertTrue(policy.configurationIsValid());

        for (int i = 0; i < 10; i++)
        {
            List<Integer> shardIds = policy.getShardIdsForNode(i + 1);
            assertEquals(1, shardIds.size());
            assertTrue(shardIds.contains(i));
        }
        assertEquals(0, policy.getShardIdsForNode(11).size());
        
        for (int i = 0; i < 10; i++)
        {
            List<Integer> nodeInstances = policy.getNodeInstancesForShardId(i);
            assertEquals(1, nodeInstances.size());
            
        }
    }

    @Test
    public void tenShards_doubled_tenNodes()
    {
        ExplicitShardingPolicy policy = new ExplicitShardingPolicy(10, 2, 10);
        assertTrue(policy.configurationIsValid());

        for (int i = 0; i < 10; i++)
        {
            List<Integer> shardIds = policy.getShardIdsForNode(i + 1);
            assertEquals(2, shardIds.size());
            assertTrue(shardIds.contains(i));
            assertTrue(shardIds.contains((i + 1) % 10));
        }
        assertEquals(0, policy.getShardIdsForNode(11).size());
        
        for (int i = 0; i < 10; i++)
        {
            List<Integer> nodeInstances = policy.getNodeInstancesForShardId(i);
            assertEquals(2, nodeInstances.size());
            
        }
    }

    @Test
    public void check_24_3()
    {
        buildAndTest(24, 3, 72);
        buildAndTest(24, 3, 36);
        buildAndTest(24, 3, 24);
        buildAndTest(24, 3, 18);
        buildAndTest(24, 3, 12);
        buildAndTest(24, 3, 9);
        buildAndTest(24, 3, 8);
        buildAndTest(24, 3, 6);
        buildAndTest(24, 3, 4);
        buildAndTest(24, 3, 3);
    }
    
    @Test
    
    public void failing()
    {
        buildAndTest(10, 2, 4);
    }
    
    @Test
    public void check_10_2()
    {
        buildAndTest(10, 2, 20);
        buildAndTest(10, 2, 10);
        buildAndTest(10, 2, 5);
        buildAndTest(10, 2, 4);
        buildAndTest(10, 2, 2);
    }
    
    @Test
    public void check_12_2()
    {
        buildAndTest(12, 2, 24);
        buildAndTest(12, 2, 12);
        buildAndTest(12, 2, 8);
        buildAndTest(12, 2, 6);
        buildAndTest(12, 2, 4);
        buildAndTest(12, 2, 3);
        buildAndTest(12, 2, 2);
    }

    @Test
    public void invalidConfiguration_nodes()
    {
        ExplicitShardingPolicy policy = new ExplicitShardingPolicy(10, 2, 11);
        assertFalse(policy.configurationIsValid());

        policy = new ExplicitShardingPolicy(10, 0, 10);
        assertFalse(policy.configurationIsValid());

        policy = new ExplicitShardingPolicy(0, 2, 10);
        assertFalse(policy.configurationIsValid());

        policy = new ExplicitShardingPolicy(10, 11, 10);
        assertFalse(policy.configurationIsValid());
    }

    private void buildAndTest(int numShards, int replicationFactor, int numNodes)
    {
        ExplicitShardingPolicy policy = new ExplicitShardingPolicy(numShards, replicationFactor, numNodes);
        assertTrue(policy.configurationIsValid());

        int[] found = new int[numShards];
        for (int i = 0; i < numNodes; i++)
        {
            List<Integer> shardIds = policy.getShardIdsForNode(i + 1);
            assertEquals(numShards * replicationFactor / numNodes, shardIds.size());
            for (Integer shardId : shardIds)
            {
                found[shardId]++;
            }
        }
        check(found, replicationFactor);
        assertEquals(0, policy.getShardIdsForNode(numNodes + 1).size());
        
        
        int[] nodes  = new int[numNodes];
        for(int i = 0; i < numShards; i++)
        {
            List<Integer> nodeInstances = policy.getNodeInstancesForShardId(i);
            assertEquals(replicationFactor, nodeInstances.size());
            for (Integer nodeInstance : nodeInstances)
            {
                nodes[nodeInstance-1]++;
            }
        }
        check(nodes, numShards * replicationFactor / numNodes);
    }

    /**
     * @param found
     * @param i
     */
    private void check(int[] found, int count)
    {
        for (int i = 0; i < found.length; i++)
        {
            assertEquals(count, found[i]);
        }
    }
}
