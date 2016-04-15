
package org.alfresco.repo.search.impl;

import org.alfresco.repo.search.impl.lucene.SolrSuggesterResult;
import org.alfresco.service.cmr.search.SuggesterParameters;
import org.alfresco.service.cmr.search.SuggesterResult;
import org.alfresco.service.cmr.search.SuggesterService;

/**
 * Dummy Suggester
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class DummySuggesterServiceImpl implements SuggesterService
{

    @Override
    public boolean isEnabled()
    {
        return false;
    }

    @Override
    public SuggesterResult getSuggestions(SuggesterParameters suggesterParameters)
    {
        return new SolrSuggesterResult();
    }

}
