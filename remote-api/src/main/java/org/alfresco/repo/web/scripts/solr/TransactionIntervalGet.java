/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
import java.util.Map;

import org.alfresco.repo.domain.node.ibatis.NodeDAOImpl;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Returns the minimum and the maximum commit time for transactions in a node id range.
 * This webscript is used by Shards using DB_ID_RANGE in order to 
 * identify the transaction time window that needs to be indexed. 
 *  
 * @author aborroy
 *
 */
public class TransactionIntervalGet  extends DeclarativeWebScript
{
    
    private NodeDAOImpl nodeDAO;

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        Long fromNodeId = Long.parseLong(req.getParameter("fromNodeId"));
        Long toNodeId = Long.parseLong(req.getParameter("toNodeId"));
        
        Map<String, Object> model = new HashMap<>();
        model.put("minTransactionCommitTimeMs", nodeDAO.getMinTxInNodeIdRange(fromNodeId, toNodeId));
        model.put("maxTransactionCommitTimeMs", nodeDAO.getMaxTxInNodeIdRange(fromNodeId, toNodeId));
        return model;
    }

    public void setNodeDAO(NodeDAOImpl nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

}
