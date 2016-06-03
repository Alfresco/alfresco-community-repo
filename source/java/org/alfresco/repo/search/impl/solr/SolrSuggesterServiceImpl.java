
package org.alfresco.repo.search.impl.solr;

import java.util.HashMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.impl.lucene.SolrSuggesterResult;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SuggesterParameters;
import org.alfresco.service.cmr.search.SuggesterResult;
import org.alfresco.service.cmr.search.SuggesterService;
import org.json.JSONObject;

/**
 * Solr Suggester Service Implementation.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SolrSuggesterServiceImpl implements SuggesterService
{

    public static final String SUGGEST_HANDLER = "/suggest";

    private boolean enabled;
    
    SolrQueryHTTPClient solrQueryHTTPClient;


    public void setEnabled(boolean isEnabled)
    {
        this.enabled = isEnabled;
    }

    @Override
    public boolean isEnabled()
    {
        return this.enabled;
    }


    /**
     * @param solrQueryHTTPClient the solrQueryHTTPClient to set
     */
    public void setSolrQueryHTTPClient(SolrQueryHTTPClient solrQueryHTTPClient)
    {
        this.solrQueryHTTPClient = solrQueryHTTPClient;
    }

    @Override
    public SuggesterResult getSuggestions(SuggesterParameters suggesterParameters)
    {
        // if it is not enabled, return an empty result set
        if (!enabled)
        {
            return new SolrSuggesterResult();
        }
        try
        {
            HashMap<String, String> params = new HashMap<>(3);
            String term = suggesterParameters.isTermIsCaseSensitive() ? suggesterParameters.getTerm() : suggesterParameters.getTerm().toLowerCase();
            int limit = suggesterParameters.getLimit();

            params.put("q", term);
            params.put("shards.qt", SUGGEST_HANDLER);
            if (limit > 0)
            {
                params.put("suggest.count", Integer.toString(limit));
            }
            params.put("wt", "json");

            JSONObject response = solrQueryHTTPClient.execute(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SUGGEST_HANDLER, params);
            return new SolrSuggesterResult(response);
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("SolrSuggester failed.", e);
        }
    }

}
