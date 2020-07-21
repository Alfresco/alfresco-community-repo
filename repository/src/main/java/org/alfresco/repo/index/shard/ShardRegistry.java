/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.index.shard;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.QName;

/**
 * A registry which collects all the active shard subscriptions.
 *
 * @author Andy
 * @author agazzarini
 * @author eporciani
 */
public interface ShardRegistry
{
    /**
     * Registers (or updates the existing subscription) of a shard.
     *
     * @param shardState the shard state, which contains the information about the shard that wants to subscribe/register.
     */
    void registerShardState(ShardState shardState);
    
    List<ShardInstance> getIndexSlice(SearchParameters searchParameters);
    
    void purge();
    
    Map<Floc, Map<Shard, Set<ShardState>>> getFlocs();

    void purgeAgedOutShards();

    /**
     * Returns the shard instance (i.e. shard number) which owns (or should own) the transaction associated with the given timestamp.
     *
     * @param coreId an identifier (e.g. core name, base url) of the core / collection whose requested shard belongs to.
     * @param txnTimestamp the transaction timestamp used as search criteria.
     * @return the shard instance (i.e. shard number) which owns (or should own) the transaction associated with the given timestamp.
     */
    OptionalInt getShardInstanceByTransactionTimestamp(String coreId, long txnTimestamp);
    
    /**
     * Returns the property used for EXPLICIT_ID Sharding methods if exists. Null otherwise.
     * 
     * @param coreName is the name of the SOLR core: alfresco, archive
     * @return QName of the property used for EXPLICIT_ID Sharding methods or null.
     */
    QName getExplicitIdProperty(String coreName);
    
    /**
     * Returns the list with the numbers of the registered Shard Instances.
     * 
     * @param coreName is the name of the SOLR core: alfresco, archive
     * @return Ordered list of numbers of the registered Shard Instances
     */
    Set<Integer> getShardInstanceList(String coreName);
    
}
