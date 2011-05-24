package org.alfresco.repo.domain.solr.ibatis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.solr.NodeParameters;
import org.alfresco.repo.domain.solr.SOLRDAO;
import org.alfresco.repo.domain.solr.SOLRTransactionParameters;
import org.alfresco.repo.domain.solr.Transaction;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * DAO support for SOLR web scripts.
 * 
 * @since 4.0
 */
public class SOLRDAOImpl implements SOLRDAO
{
    private static final String SELECT_TRANSACTIONS = "alfresco.solr.select_Txns";
    private static final String SELECT_NODES = "alfresco.solr.select_Txn_Nodes";
    
    private QNameDAO qnameDAO;
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    public SqlMapClientTemplate getSqlMapClientTemplate()
    {
        return this.template;
    }
    
    public void setQNameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    /*
     * Initialize
     */    
    public void init()
    {
    }
    
	@SuppressWarnings("unchecked")
	public List<Transaction> getTransactions(Long minTxnId, Long fromCommitTime, int maxResults)
	{
	    if(minTxnId == null && fromCommitTime == null && (maxResults == 0 || maxResults == Integer.MAX_VALUE))
	    {
	        throw new IllegalArgumentException("Must specify at least one parameter");
	    }

	    List<Transaction> txns = null;
	    SOLRTransactionParameters params = new SOLRTransactionParameters();
	    params.setMinTxnId(minTxnId);
	    params.setTxnFromCommitTime(fromCommitTime);

	    if(maxResults != 0 && maxResults != Integer.MAX_VALUE)
	    {
	        txns = (List<Transaction>)template.queryForList(SELECT_TRANSACTIONS, params, 0, maxResults);
	    }
	    else
	    {
            txns = (List<Transaction>)template.queryForList(SELECT_TRANSACTIONS, params);
	    }
	    
	    return txns;
	}

	@SuppressWarnings("unchecked")
	// TODO should create qnames if don't exist?
    public void getNodes(NodeParameters nodeParameters, int maxResults, NodeQueryCallback callback)
	{
	    List<NodeEntity> nodes = null;
        NodeQueryRowHandler rowHandler = new NodeQueryRowHandler(callback);

        if(nodeParameters.getIncludeTypeIds() == null && nodeParameters.getIncludeNodeTypes() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(nodeParameters.getIncludeNodeTypes(), false);
            nodeParameters.setIncludeTypeIds(new ArrayList<Long>(qnamesIds));
        }

        if(nodeParameters.getExcludeTypeIds() == null && nodeParameters.getExcludeNodeTypes() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(nodeParameters.getExcludeNodeTypes(), false);
            nodeParameters.setExcludeTypeIds(new ArrayList<Long>(qnamesIds));
        }
        
        if(nodeParameters.getExcludeAspectIds() == null && nodeParameters.getExcludeAspects() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(nodeParameters.getExcludeAspects(), false);
            nodeParameters.setExcludeAspectIds(new ArrayList<Long>(qnamesIds));
        }

        if(nodeParameters.getIncludeAspectIds() == null && nodeParameters.getIncludeAspects() != null)
        {
            Set<Long> qnamesIds = qnameDAO.convertQNamesToIds(nodeParameters.getIncludeAspects(), false);
            nodeParameters.setIncludeAspectIds(new ArrayList<Long>(qnamesIds));
        }

	    if(maxResults != 0 && maxResults != Integer.MAX_VALUE)
	    {
	        nodes = (List<NodeEntity>)template.queryForList(SELECT_NODES, nodeParameters, 0, maxResults);
	    }
	    else
	    {
	        nodes = (List<NodeEntity>)template.queryForList(SELECT_NODES, nodeParameters);
	    }
	    
	    for(NodeEntity node : nodes)
	    {
	    	rowHandler.processResult(node);
	    }
	}

    /**
     * Class that passes results from a result entity into the client callback
     */
    protected class NodeQueryRowHandler
    {
        private final NodeQueryCallback callback;
        private boolean more;

        private NodeQueryRowHandler(NodeQueryCallback callback)
        {
            this.callback = callback;
            this.more = true;
        }
        
        public void processResult(Node row)
        {
            if (!more)
            {
                // No more results required
                return;
            }
            
            more = callback.handleNode(row);
        }
    }
}
