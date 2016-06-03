package org.alfresco.repo.index.shard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.Pair;

/**
 * @author Andy
 *
 */
public interface ShardRegistry
{
    public void registerShardState(ShardState shardState);
    
    public List<ShardInstance> getIndexSlice(SearchParameters searchParameters);
    
    public void purge();
    
    public HashMap<Floc, HashMap<Shard, HashSet<ShardState>>> getFlocs();

    public void purgeAgedOutShards();
}
