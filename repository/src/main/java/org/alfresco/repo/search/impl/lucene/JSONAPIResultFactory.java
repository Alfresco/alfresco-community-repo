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
package org.alfresco.repo.search.impl.lucene;

import java.util.Arrays;

import org.alfresco.error.AlfrescoRuntimeException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSON API Results factory
 * SOLR JSON responses are parsed according to required Action, Command or Handler 
 *
 * @author aborroy
 * @since 6.2
 */
public class JSONAPIResultFactory
{
    
    /**
     * SOLR CoreAdmin API Actions (partial list)
     */
    public static enum ACTION 
    {
        STATUS,
        REPORT,
        TXREPORT,
        ACLTXREPORT,
        NODEREPORT,
        ACLREPORT,
        FIX,
        CHECK
    }

    /**
     * SOLR API Commands (partial list)
     */
    public static enum COMMAND
    {
        BACKUP
    }

    /**
     * SOLR API Handlers (partial list)
     */
    public static enum HANDLER
    {
        REPLICATION
    }

    /**
     * Build a JSON Parser Result object according to required SOLR Action
     * @param action SOLR Action invoked
     * @param json Result in JSON of the SOLR Action invoked
     * @return
     * @throws JSONException
     */
    public static JSONAPIResult buildActionResult(ACTION action, JSONObject json)
    {
        switch (action)
        {
        case STATUS:
        {
            return new SolrActionStatusResult(json);
        }
        case REPORT:
        {
            return new SolrActionReportResult(json);
        }
        case TXREPORT:
        {
            return new SolrActionTxReportResult(json);
        }
        case ACLTXREPORT:
        {
            return new SolrActionAclTxReportResult(json);
        }
        case ACLREPORT:
        {
            return new SolrActionAclReportResult(json);
        }
        case NODEREPORT:
        {
            return new SolrActionNodeReportResult(json);
        }
        case FIX:
        {
            return new SolrActionFixResult(json);
        }
        case CHECK:
        {
            return new SolrActionCheckResult(json);
        }
        default:
        {
            throw new AlfrescoRuntimeException("Action " + action + " is not supported when invoking to SOLR REST API, available actions: " + Arrays.asList(ACTION.values()));
        }
        }
    }
    
}
