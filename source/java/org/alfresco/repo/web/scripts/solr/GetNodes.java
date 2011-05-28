package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
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
        try
        {
            Content content = req.getContent();
            if(content == null)
            {
                throw new WebScriptException("Failed to convert request to String");
            }
            JSONObject o = new JSONObject(content.getContent());

            JSONArray aTxnIds = o.has("txnIds") ? o.getJSONArray("txnIds") : null;
            Long fromTxnId = o.has("fromTxnId") ? o.getLong("fromTxnId") : null;
            Long toTxnId = o.has("toTxnId") ? o.getLong("toTxnId") : null;

            Long fromNodeId = o.has("fromNodeId") ? o.getLong("fromNodeId") : null;
            Long toNodeId = o.has("toNodeId") ? o.getLong("toNodeId") : null;
            
            Set<QName> excludeAspects = null;
            if(o.has("excludeAspects"))
            {
                JSONArray aExcludeAspects = o.getJSONArray("excludeAspects");
                excludeAspects = new HashSet<QName>(aExcludeAspects.length());
                for(int i = 0; i < aExcludeAspects.length(); i++)
                {
                    excludeAspects.add(QName.createQName(aExcludeAspects.getString(i).trim()));
                }
            }

            Set<QName> includeAspects = null;
            if(o.has("includeAspects"))
            {
                JSONArray aIncludeAspects = o.getJSONArray("includeAspects");
                includeAspects = new HashSet<QName>(aIncludeAspects.length());
                for(int i = 0; i < aIncludeAspects.length(); i++)
                {
                    includeAspects.add(QName.createQName(aIncludeAspects.getString(i).trim()));
                }
            }

            // 0 or Integer.MAX_VALUE => ignore
            int maxResults = o.has("maxResults") ? o.getInt("maxResults") : 0;

            String storeProtocol = o.has("storeProtocol") ? o.getString("storeProtocol") : null;
            String storeIdentifier = o.has("storeIdentifier") ? o.getString("storeIdentifier") : null;

            List<Long> txnIds = null;
            if(aTxnIds != null)
            {
                txnIds = new ArrayList<Long>(aTxnIds.length());
                for(int i = 0; i < aTxnIds.length(); i++)
                {
                    txnIds.add(aTxnIds.getLong(i));
                }
            }

            WebNodeQueryCallback nodeQueryCallback = new WebNodeQueryCallback(maxResults);
            NodeParameters nodeParameters = new NodeParameters();
            nodeParameters.setTransactionIds(txnIds);
            nodeParameters.setFromTxnId(fromTxnId);
            nodeParameters.setToTxnId(toTxnId);
            nodeParameters.setFromNodeId(fromNodeId);
            nodeParameters.setToNodeId(toNodeId);
            nodeParameters.setExcludeAspects(excludeAspects);
            nodeParameters.setIncludeAspects(includeAspects);
            nodeParameters.setStoreProtocol(storeProtocol);
            nodeParameters.setStoreIdentifier(storeIdentifier);
            nodeParameters.setMaxResults(maxResults);
            solrDAO.getNodes(nodeParameters, nodeQueryCallback);

            Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
            List<Node> nodes = nodeQueryCallback.getNodes();
            model.put("nodes", nodes);

            if (logger.isDebugEnabled())
            {
                logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
            }

            return model;
        }
        catch(IOException e)
        {
            throw new WebScriptException("IO exception parsing request", e);
        }
        catch(JSONException e)
        {
            throw new WebScriptException("Invalid JSON", e);
        }
    }

    /**
     * Callback for DAO get nodes query
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