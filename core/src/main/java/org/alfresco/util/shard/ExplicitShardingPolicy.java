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

import java.util.LinkedList;
import java.util.List;

/**
 * Common ACL based index sharding behaviour for SOLR and the repository
 * 
 * @author Andy
 */
public class ExplicitShardingPolicy
{
    private int numShards;

    private int replicationFactor;

    private int numNodes;

    public ExplicitShardingPolicy(int numShards, int replicationFactor, int numNodes)
    {
        this.numShards = numShards;
        this.replicationFactor = replicationFactor;
        this.numNodes = numNodes;
    }

    public boolean configurationIsValid()
    {
        if ((numShards * replicationFactor) % numNodes != 0)
        {
            return false;
        }

        int shardsPerNode = numShards * replicationFactor / numNodes;
        if ((shardsPerNode > numShards) || (shardsPerNode < 1))
        {
            return false;
        }

        return true;
    }

    public List<Integer> getShardIdsForNode(int nodeInstance)
    {
        LinkedList<Integer> shardIds = new LinkedList<Integer>();
        int test = 0;
        for (int replica = 0; replica < replicationFactor; replica++)
        {
            for (int shard = replica; shard < numShards + replica; shard++)
            {
                if (test % numNodes == nodeInstance - 1)
                {
                    shardIds.add(shard % numShards);
                }
                test++;
            }

        }
        return shardIds;
    }

    public List<Integer> getNodeInstancesForShardId(int shardId)
    {
        LinkedList<Integer> nodeInstances = new LinkedList<Integer>();
        for (int nodeInstance = 1; nodeInstance <= numNodes; nodeInstance++)
        {
            int test = 0;
            for (int replica = 0; replica < replicationFactor; replica++)
            {
                for (int shard = replica; shard < numShards + replica; shard++)
                {
                    if (test % numNodes == nodeInstance - 1)
                    {
                        if(shard % numShards == shardId)
                        {
                            nodeInstances.add(nodeInstance);
                        }
                    }
                    test++;
                }

            }
        }
        return nodeInstances;
    }

}
