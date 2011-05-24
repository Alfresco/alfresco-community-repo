package org.alfresco.repo.web.scripts.solr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.solr.SOLRDAO;
import org.alfresco.repo.domain.solr.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Support for SOLR. Get a list of transactions with a commit time greater than or equal to the given parameter.
 *
 * @since 4.0
 */
public class GetTransactions extends DeclarativeWebScript
{
    protected static final Log logger = LogFactory.getLog(GetTransactions.class);

    private SOLRDAO solrDAO;
    
    /**
     * @param solrDAO          the SOLDAO to set
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
        String minTxnIdParam = req.getParameter("minTxnId");
        String fromCommitTimeParam = req.getParameter("fromCommitTime");
        String maxResultsParam = req.getParameter("maxResults");

        Long minTxnId = (minTxnIdParam == null ? null : Long.valueOf(minTxnIdParam));
        Long fromCommitTime = (fromCommitTimeParam == null ? null : Long.valueOf(fromCommitTimeParam));
        int maxResults = (maxResultsParam == null ? 0 : Integer.valueOf(maxResultsParam));
        
        List<Transaction> transactions = solrDAO.getTransactions(minTxnId, fromCommitTime, maxResults);
        
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        model.put("transactions", transactions);

        if (logger.isDebugEnabled())
        {
            logger.debug("Result: \n\tRequest: " + req + "\n\tModel: " + model);
        }
        
        return model;
    }

}
