package org.alfresco.repo.web.scripts.solr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.solr.NodeParameters;
import org.alfresco.repo.domain.solr.SOLRDAO;
import org.alfresco.repo.domain.solr.SOLRDAO.NodeQueryCallback;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Support for SOLR. Get a list of nodes in the given transactions.
 * 
 * Supports fromNodeId, toNodeId, count (all optional) to control the number of nodes returned
 * e.g. (null, null, 1000) will return at most 1000 nodes starting from the first node in the first transaction.
 * e.g. (1234, null, 1000) will return at most 1000 nodes starting from the node id 1234.
 * 
 * @since 4.0
 */
public class GetNodes extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(GetNodes.class);

    private SOLRDAO solrDAO;
    
    /**
     * @param solrDAO          the solrDAO to set
     */
    public void setSolrDAO(SOLRDAO solrDAO)
    {
        this.solrDAO = solrDAO;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        String txnIdsString = req.getParameter("txnIds");
        if(txnIdsString == null)
        {
            throw new WebScriptException("txnIds parameter is required for GetNodes");
        }
        
        String param = req.getParameter("fromNodeId");
        Long fromNodeId = (param == null ? null : Long.valueOf(param));
        
        param = req.getParameter("toNodeId");
        Long toNodeId = (param == null ? null : Long.valueOf(param));
        
        param = req.getParameter("excludeAspects");
        Set<QName> excludeAspects = null;
        if(param != null)
        {
            String[] excludeAspectsStrings = param.split(",");
            excludeAspects = new HashSet<QName>(excludeAspectsStrings.length);
            for(String excludeAspect : excludeAspectsStrings)
            {
                excludeAspects.add(QName.createQName(excludeAspect.trim()));
            }
        }
        
        param = req.getParameter("includeAspects");
        Set<QName> includeAspects = null;
        if(param != null)
        {
            String[] includeAspectsStrings = param.split(",");
            includeAspects = new HashSet<QName>(includeAspectsStrings.length);
            for(String includeAspect : includeAspectsStrings)
            {
                includeAspects.add(QName.createQName(includeAspect.trim()));
            }
        }
        
        param = req.getParameter("maxResults");
        int maxResults = (param == null ? 0 : Integer.valueOf(param));
        
        param = req.getParameter("storeProtocol");
        String storeProtocol = param;
        
        param = req.getParameter("storeIdentifier");
        String storeIdentifier = param;
        
        String[] txnIdStrings = txnIdsString.split(",");
    	List<Long> txnIds = new ArrayList<Long>(txnIdStrings.length);
        for(String txnIdString : txnIdStrings)
        {
        	txnIds.add(Long.valueOf(txnIdString.trim()));
        }

        WebNodeQueryCallback nodeQueryCallback = new WebNodeQueryCallback(maxResults);
        NodeParameters nodeParameters = new NodeParameters();
        nodeParameters.setTransactionIds(txnIds);
        nodeParameters.setFromNodeId(fromNodeId);
        nodeParameters.setToNodeId(toNodeId);
        nodeParameters.setExcludeAspects(excludeAspects);
        nodeParameters.setIncludeAspects(includeAspects);
        nodeParameters.setStoreProtocol(storeProtocol);
        nodeParameters.setStoreIdentifier(storeIdentifier);
        solrDAO.getNodes(nodeParameters, maxResults, nodeQueryCallback);

        Map<String, Object> model = new HashMap<String, Object>(2, 1.0f);
        List<Node> nodes = nodeQueryCallback.getNodes();
        model.put("nodes", nodes);
        model.put("count", nodes.size());

        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        
        return model;
    }

    /**
     * 
     *
     */
    private static class WebNodeQueryCallback implements NodeQueryCallback
    {
    	private ArrayList<Node> nodes;

		public WebNodeQueryCallback(int count) {
			super();
			nodes = new ArrayList<Node>(count == 0 || count == Integer.MAX_VALUE ? 100 : count);
		}

		@Override
		public boolean handleNode(Node node) {
    		nodes.add(node);

    		// continue - get next node
    		return true;
		}
		
		public List<Node> getNodes()
		{
		    return nodes;
		}
    }

}