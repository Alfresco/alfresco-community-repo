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
package org.alfresco.repo.index.shard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.attributes.AttributeService.AttributeQueryCallback;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.GUID;

import com.hazelcast.util.ConcurrentHashSet;

/**
 * @author Andy
 */
public class ShardRegistryImpl implements ShardRegistry
{
    private static String SHARD_STATE_KEY = ".SHARD_STATE";

    private AttributeService attributeService;

    private SimpleCache<ShardInstance, ShardState> shardStateCache;

    private SimpleCache<ShardInstance, String> shardToGuidCache;

    private ConcurrentHashSet<Floc> knownFlocks = new ConcurrentHashSet<Floc>();

    private Random random = new Random(123);

    public ShardRegistryImpl()
    {
    }

    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    public void setShardStateCache(SimpleCache<ShardInstance, ShardState> shardStateCache)
    {
        this.shardStateCache = shardStateCache;
    }

    public void setShardToGuidCache(SimpleCache<ShardInstance, String> shardToGuidCache)
    {
        this.shardToGuidCache = shardToGuidCache;
    }

    /**
     * @param shardState
     */
    public void registerShardState(ShardState shardState)
    {

        String guid = getPersistedShardStatusGuid(shardState.getShardInstance());
        attributeService.setAttribute(shardState, SHARD_STATE_KEY, guid);
        shardStateCache.put(shardState.getShardInstance(), shardState);
        knownFlocks.add(shardState.getShardInstance().getShard().getFloc());
    }

    public List<ShardInstance> getIndexSlice(SearchParameters searchParameters)
    {
        Floc floc = findFlocFromKnown(searchParameters);
        if (floc == null)
        {
            updateKnownFlocs();
            floc = findFlocFromKnown(searchParameters);
        }
        return selectShardInstancesForFlock(floc);

    }

    private List<ShardInstance> selectShardInstancesForFlock(Floc floc)
    {
        HashMap<Shard, HashSet<ShardInstance>> index = new HashMap<Shard, HashSet<ShardInstance>>();

        getShardInstancesFromCache(floc, index);
        if (index.size() < floc.getNumberOfShards())
        {
            updateShardStateCache(floc);
        }
        getShardInstancesFromCache(floc, index);

        ArrayList<ShardInstance> slice = new ArrayList<ShardInstance>(floc.getNumberOfShards());
        for (Shard shard : index.keySet())
        {
            int position = random.nextInt(index.get(shard).size());
            ShardInstance instance = index.get(shard).toArray(new ShardInstance[] {})[position];
            slice.add(instance);
        }
        return slice;
    }

    /**
     * @param floc
     */
    private void updateShardStateCache(Floc floc)
    {
        ShardStateCollector shardStates = getPersistedShardStates();
        HashMap<Shard, HashMap<ShardInstance, ShardState>> shards = shardStates.getIndexes().get(floc);
        for (HashMap<ShardInstance, ShardState> map : shards.values())
        {
            for (ShardInstance instance : map.keySet())
            {
                shardStateCache.put(instance, map.get(instance));
            }
        }
    }

    /**
     * @param floc
     * @param index
     */
    private void getShardInstancesFromCache(Floc floc, HashMap<Shard, HashSet<ShardInstance>> index)
    {
        for (ShardInstance instance : shardStateCache.getKeys())
        {
            if (instance.getShard().getFloc().equals(floc))
            {
                HashSet<ShardInstance> replicas = index.get(instance.getShard());
                if (replicas == null)
                {
                    replicas = new HashSet<ShardInstance>();
                    index.put(instance.getShard(), replicas);
                }
                replicas.add(instance);
            }
        }
    }

    private void updateKnownFlocs()
    {
        ShardStateCollector shardStates = getPersistedShardStates();
        knownFlocks.addAll(shardStates.getIndexes().keySet());
    }

    private Floc findFlocFromKnown(SearchParameters searchParameters)
    {
        Floc best = null;
        for (Floc floc : knownFlocks)
        {
            if (floc.getStoreRefs().containsAll(searchParameters.getStores()))
            {
                best = getBestFloc(best, floc);
            }
        }
        return best;
    }

    private Floc getBestFloc(Floc best, Floc floc)
    {
        if (best == null)
        {
            return floc;
        }
        if (best.getNumberOfShards() >= floc.getNumberOfShards())
        {
            return best;
        }
        else
        {
            return floc;
        }
    }

    private String getPersistedShardStatusGuid(ShardInstance shardInstance)
    {
        String guid = shardToGuidCache.get(shardInstance);
        if (guid == null)
        {
            ShardStateCollector shardStates = getPersistedShardStates();
            for (ShardInstance instance : shardStates.getShardGuids().keySet())
            {
                if (!shardToGuidCache.contains(instance))
                {
                    shardToGuidCache.put(instance, shardStates.getShardGuids().get(instance));
                }
            }
            guid = shardToGuidCache.get(shardInstance);
        }
        if(guid == null)
        {
            guid =  GUID.generate();
            shardToGuidCache.put(shardInstance, guid);
        }
        return guid;
    }

    private ShardStateCollector getPersistedShardStates()
    {
        ShardStateCollector shardStateCollector = new ShardStateCollector();
        attributeService.getAttributes(shardStateCollector, SHARD_STATE_KEY);
        knownFlocks.addAll(shardStateCollector.getIndexes().keySet());
        return shardStateCollector;
    }

    protected static class ShardStateCollector implements AttributeQueryCallback
    {
        HashMap<ShardInstance, String> shardGuids = new HashMap<ShardInstance, String>();

        HashMap<Floc, HashMap<Shard, HashMap<ShardInstance, ShardState>>> indexes = new HashMap<Floc, HashMap<Shard, HashMap<ShardInstance, ShardState>>>();

        public ShardStateCollector()
        {
        }

        @Override
        public boolean handleAttribute(Long id, Serializable value, Serializable[] keys)
        {
            
            String shardInstanceGuid = (String) keys[1];

            ShardState shardState = (ShardState) value;

            shardGuids.put(shardState.getShardInstance(), shardInstanceGuid);

            HashMap<Shard, HashMap<ShardInstance, ShardState>> shards = indexes.get(shardState.getShardInstance().getShard().getFloc());
            if (shards == null)
            {
                shards = new HashMap<Shard, HashMap<ShardInstance, ShardState>>();
                indexes.put(shardState.getShardInstance().getShard().getFloc(), shards);
            }
            HashMap<ShardInstance, ShardState> shardInstances = shards.get(shardState.getShardInstance().getShard());
            if (shardInstances == null)
            {
                shardInstances = new HashMap<ShardInstance, ShardState>();
                shards.put(shardState.getShardInstance().getShard(), shardInstances);
            }
            ShardState currentState = shardInstances.get(shardState.getShardInstance());

            shardInstances.put(shardState.getShardInstance(), shardState);

            return true;
        }

        /**
         * @return the shardGuids
         */
        public HashMap<ShardInstance, String> getShardGuids()
        {
            return shardGuids;
        }

        /**
         * @return the indexes
         */
        public HashMap<Floc, HashMap<Shard, HashMap<ShardInstance, ShardState>>> getIndexes()
        {
            return indexes;
        }

    }
}
