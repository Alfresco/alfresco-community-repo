/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.avm.wf;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.springframework.extensions.surf.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;

/**
 * Releases the test deploy server used by the workflow (if any).
 * 
 * @author Gavin Cornwell
 */
public class AVMReleaseTestServerHandler extends JBPMSpringActionHandler 
{
    private SearchService searchService;
    private NodeService unprotectedNodeService;
    
    private static final String BEAN_NODE_SERVICE = "nodeService";
    private static final String BEAN_SEARCH_SERVICE = "searchService";
    
    private static final long serialVersionUID = -202652488887586866L;
    private static final Log logger = LogFactory.getLog(AVMReleaseTestServerHandler.class);
    
    /**
     * Initialize service references.
     * @param factory The BeanFactory to get references from.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory) 
    {
        this.searchService = (SearchService)factory.getBean(BEAN_SEARCH_SERVICE);
        this.unprotectedNodeService = (NodeService)factory.getBean(BEAN_NODE_SERVICE);
    }

    /**
     * Do the actual work.
     * @param executionContext The context to get stuff from.
     */
    public void execute(ExecutionContext executionContext) throws Exception 
    {
        // get the store name
        NodeRef pkg = ((JBPMNode)executionContext.getContextInstance().getVariable("bpm_package")).getNodeRef();
        Pair<Integer, String> pkgPath = AVMNodeConverter.ToAVMVersionPath(pkg);
        String [] workflowStorePath = pkgPath.getSecond().split(":");
        String workflowStoreName = workflowStorePath[0];
        
        // get the web project node for the submission
        JBPMNode webProjNode = (JBPMNode)executionContext.getContextInstance().getVariable("wcmwf_webproject");
        NodeRef webProjectRef = webProjNode.getNodeRef();
        
        if (logger.isDebugEnabled())
            logger.debug("Looking for test server to release for store: " + workflowStoreName);
            
        // query for the allocated test server (if one)
        NodeRef testServer = findAllocatedServer(webProjectRef, workflowStoreName);
        
        if (testServer != null)
        {
           // reset the allocatedto property on the test server node
           this.unprotectedNodeService.setProperty(testServer, WCMAppModel.PROP_DEPLOYSERVERALLOCATEDTO, null);
               
           if (logger.isDebugEnabled())
               logger.debug("Released test server '" + testServer + "' from store: " + workflowStoreName);
        }
        else if (logger.isDebugEnabled())
        {
           logger.debug("Store '" + workflowStoreName + "' didn't have an allocated test server to release");
        }
    }
    
    private NodeRef findAllocatedServer(NodeRef webProjectRef, String store)
    {        
        StringBuilder query = new StringBuilder("@");
        query.append(NamespaceService.WCMAPP_MODEL_PREFIX);
        query.append("\\:");
        query.append(WCMAppModel.PROP_DEPLOYSERVERALLOCATEDTO.getLocalName());
        query.append(":\"");
        query.append(store);
        query.append("\"");
      
        ResultSet results = null;
        NodeRef testServer = null;
        try
        {
            // execute the query
            results = this.searchService.query(webProjectRef.getStoreRef(), 
                     SearchService.LANGUAGE_LUCENE, query.toString());
         
            if (results.length() == 1)
            {
                testServer = results.getNodeRef(0);
            }
            else if (results.length() > 1)
            {
               // get the first one and warn that we found many!
               testServer = results.getNodeRef(0);
               
               if (logger.isWarnEnabled())
               logger.warn("More than one allocated test server for store '" +
                     store + "' was found, should only be one, first one found returned!");
            }
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }

        return testServer;
    }
}
