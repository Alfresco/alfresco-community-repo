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

import org.alfresco.repo.solr.AclChangeSet;
import org.alfresco.repo.solr.SOLRTrackingComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Support for SOLR: Track ACL Change Sets
 *
 * @since 4.0
 */
public class AclChangeSetsGet extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(AclChangeSetsGet.class);

    private SOLRTrackingComponent solrTrackingComponent;
    
    public void setSolrTrackingComponent(SOLRTrackingComponent solrTrackingComponent)
    {
        this.solrTrackingComponent = solrTrackingComponent;
    }

    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        String fromIdParam = req.getParameter("fromId");
        String fromTimeParam = req.getParameter("fromTime");
        String maxResultsParam = req.getParameter("maxResults");

        Long fromId = (fromIdParam == null ? null : Long.valueOf(fromIdParam));
        Long fromTime = (fromTimeParam == null ? null : Long.valueOf(fromTimeParam));
        int maxResults = (maxResultsParam == null ? 1024 : Integer.valueOf(maxResultsParam));
        
        List<AclChangeSet> changesets = solrTrackingComponent.getAclChangeSets(fromId, fromTime, maxResults);
        
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        model.put("aclChangeSets", changesets);

        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        
        return model;
    }
}
