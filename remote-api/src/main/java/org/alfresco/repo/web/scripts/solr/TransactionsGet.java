/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.solr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.index.shard.ShardMethodEnum;
import org.alfresco.repo.index.shard.ShardState;
import org.alfresco.repo.index.shard.ShardStateBuilder;
import org.alfresco.repo.search.SearchTrackingComponent;
import org.alfresco.repo.solr.Transaction;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Support for SOLR: Get a list of transactions with a commit time greater than or equal to the given parameter.
 *
 * @since 4.0
 */
public class TransactionsGet extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(TransactionsGet.class);

    private SearchTrackingComponent searchTrackingComponent;
    
    public void setSearchTrackingComponent(SearchTrackingComponent searchTrackingComponent)
    {
        this.searchTrackingComponent = searchTrackingComponent;
    }

    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        String minTxnIdParam = req.getParameter("minTxnId");
        String fromCommitTimeParam = req.getParameter("fromCommitTime");
        String maxTxnIdParam = req.getParameter("maxTxnId");
        String toCommitTimeParam = req.getParameter("toCommitTime");
        String maxResultsParam = req.getParameter("maxResults");
        
        String baseUrl = req.getParameter("baseUrl");
        String hostName = req.getParameter("hostName");
        String template = req.getParameter("template");
        String instance = req.getParameter("instance");
        String numberOfShards = req.getParameter("numberOfShards");
        String port = req.getParameter("port");
        String stores = req.getParameter("stores");
        String isMaster = req.getParameter("isMaster");
        String hasContent = req.getParameter("hasContent");
        String shardMethod = req.getParameter("shardMethod");
        
        String lastUpdated =  req.getParameter("lastUpdated");
        String lastIndexedChangeSetCommitTime =  req.getParameter("lastIndexedChangeSetCommitTime");
        String lastIndexedChangeSetId =  req.getParameter("lastIndexedChangeSetId");
        String lastIndexedTxCommitTime =  req.getParameter("lastIndexedTxCommitTime");
        String lastIndexedTxId =  req.getParameter("lastIndexedTxId");
        
        if(baseUrl != null)
        {
            ShardState shardState =  ShardStateBuilder.shardState()
                    .withMaster(Boolean.valueOf(isMaster))
                    .withLastUpdated(Long.valueOf(lastUpdated))
                    .withLastIndexedChangeSetCommitTime(Long.valueOf(lastIndexedChangeSetCommitTime))
                    .withLastIndexedChangeSetId(Long.valueOf(lastIndexedChangeSetId))
                    .withLastIndexedTxCommitTime(Long.valueOf(lastIndexedTxCommitTime))
                    .withLastIndexedTxId(Long.valueOf(lastIndexedTxId))
                    .withShardInstance()
                        .withBaseUrl(baseUrl)
                        .withPort(Integer.valueOf(port))
                        .withHostName(hostName)
                        .withShard()
                            .withInstance(Integer.valueOf(instance))
                            .withFloc()
                                .withNumberOfShards(Integer.valueOf(numberOfShards))
                                .withTemplate(template)
                                .withHasContent(Boolean.valueOf(hasContent))
                                .withShardMethod(ShardMethodEnum.getShardMethod(shardMethod))
                                .endFloc()
                            .endShard()
                         .endShardInstance()
                    .build();
            
            for(String store : stores.split(","))
            {
                shardState.getShardInstance().getShard().getFloc().getStoreRefs().add(new StoreRef(store));
            }
            
            for(String pName : req.getParameterNames())
            {
                if(pName.startsWith("floc.property."))
                {
                    String key = pName.substring("floc.property.".length());
                    String value = req.getParameter(pName);
                    shardState.getShardInstance().getShard().getFloc().getPropertyBag().put(key, value);
                }
                else  if(pName.startsWith("state.property."))
                {
                    String key = pName.substring("state.property.".length());
                    String value = req.getParameter(pName);
                    shardState.getPropertyBag().put(key, value);
                }
            }
            
            searchTrackingComponent.registerShardState(shardState);
   
        }
        

        Long minTxnId = (minTxnIdParam == null ? null : Long.valueOf(minTxnIdParam));
        Long fromCommitTime = (fromCommitTimeParam == null ? null : Long.valueOf(fromCommitTimeParam));
        Long maxTxnId = (maxTxnIdParam == null ? null : Long.valueOf(maxTxnIdParam));
        Long toCommitTime = (toCommitTimeParam == null ? null : Long.valueOf(toCommitTimeParam));
        int maxResults = (maxResultsParam == null ? 1024 : Integer.valueOf(maxResultsParam));
        
        List<Transaction> transactions = searchTrackingComponent.getTransactions(minTxnId, fromCommitTime, maxTxnId, toCommitTime, maxResults);
        
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        model.put("transactions", transactions);
        
        Long maxTxnCommitTime = searchTrackingComponent.getMaxTxnCommitTime();
        if(maxTxnCommitTime != null)
        {
            model.put("maxTxnCommitTime", maxTxnCommitTime);
        }
        
        Long maxTxnIdOnServer = searchTrackingComponent.getMaxTxnId();
        if(maxTxnIdOnServer != null)
        {
            model.put("maxTxnId", maxTxnIdOnServer);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        
        return model;
    }
}
