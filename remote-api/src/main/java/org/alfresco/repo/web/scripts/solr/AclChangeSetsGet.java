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

import org.alfresco.repo.search.SearchTrackingComponent;
import org.alfresco.repo.solr.AclChangeSet;
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

    private SearchTrackingComponent searchTrackingComponent;
    
    public void setSearchTrackingComponent(SearchTrackingComponent searchTrackingComponent)
    {
        this.searchTrackingComponent = searchTrackingComponent;
    }

    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        String fromIdParam = req.getParameter("fromId");
        String fromTimeParam = req.getParameter("fromTime");
        String toIdParam = req.getParameter("toId");
        String toTimeParam = req.getParameter("toTime");
        String maxResultsParam = req.getParameter("maxResults");

        Long fromId = (fromIdParam == null ? null : Long.valueOf(fromIdParam));
        Long fromTime = (fromTimeParam == null ? null : Long.valueOf(fromTimeParam));
        Long toId = (toIdParam == null ? null : Long.valueOf(toIdParam));
        Long toTime = (toTimeParam == null ? null : Long.valueOf(toTimeParam));
        int maxResults = (maxResultsParam == null ? 1024 : Integer.valueOf(maxResultsParam));
        
        List<AclChangeSet> changesets = searchTrackingComponent.getAclChangeSets(fromId, fromTime, toId, toTime, maxResults);
        
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        model.put("aclChangeSets", changesets);

        Long maxChangeSetCommitTime = searchTrackingComponent.getMaxChangeSetCommitTime();
        if(maxChangeSetCommitTime != null)
        {
            model.put("maxChangeSetCommitTime", maxChangeSetCommitTime);
        }
        
        Long maxChangeSetId = searchTrackingComponent.getMaxChangeSetId();
        if(maxChangeSetId != null)
        {
            model.put("maxChangeSetId", maxChangeSetId);
        }

        
        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        
        return model;
    }
}
