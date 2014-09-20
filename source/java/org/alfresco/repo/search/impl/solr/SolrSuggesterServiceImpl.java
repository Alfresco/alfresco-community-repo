/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.search.impl.solr;

import java.util.HashMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.impl.lucene.SolrSuggesterResult;
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

    public static final String SUGGESER_PATH = "/alfresco/suggest";

    private boolean enabled;
    private SolrAdminHTTPClient solrAdminHTTPClient;

    public void setEnabled(boolean isEnabled)
    {
        this.enabled = isEnabled;
    }

    public void setSolrAdminHTTPClient(SolrAdminHTTPClient solrAdminHTTPClient)
    {
        this.solrAdminHTTPClient = solrAdminHTTPClient;
    }

    @Override
    public boolean isEnabled()
    {
        return this.enabled;
    }

    @Override
    public SuggesterResult getSuggestions(String term, int limit)
    {
        // if it is not enabled, return an empty result set
        if (!enabled)
        {
            return new SolrSuggesterResult();
        }
        try
        {
            HashMap<String, String> params = new HashMap<>(3);
            params.put("q", term);
            if (limit > 0)
            {
                params.put("suggest.count", Integer.toString(limit));
            }
            params.put("wt", "json");

            JSONObject response = solrAdminHTTPClient.execute(SUGGESER_PATH, params);
            return new SolrSuggesterResult(response);
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("SolrSuggester failed.", e);
        }
    }

}
