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
package org.alfresco.repo.web.scripts.solr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.alfresco.repo.solr.Transaction;
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

    private SOLRTrackingComponent solrTrackingComponent;
    
    public void setSolrTrackingComponent(SOLRTrackingComponent solrTrackingComponent)
    {
        this.solrTrackingComponent = solrTrackingComponent;
    }

    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        String minTxnIdParam = req.getParameter("minTxnId");
        String fromCommitTimeParam = req.getParameter("fromCommitTime");
        String maxResultsParam = req.getParameter("maxResults");

        Long minTxnId = (minTxnIdParam == null ? null : Long.valueOf(minTxnIdParam));
        Long fromCommitTime = (fromCommitTimeParam == null ? null : Long.valueOf(fromCommitTimeParam));
        int maxResults = (maxResultsParam == null ? 1024 : Integer.valueOf(maxResultsParam));
        
        List<Transaction> transactions = solrTrackingComponent.getTransactions(minTxnId, fromCommitTime, maxResults);
        
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        model.put("transactions", transactions);
        
        Long maxTxnCommitTime = solrTrackingComponent.getMaxTxnCommitTime();
        if(maxTxnCommitTime != null)
        {
            model.put("maxTxnCommitTime", maxTxnCommitTime);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        
        return model;
    }

}
