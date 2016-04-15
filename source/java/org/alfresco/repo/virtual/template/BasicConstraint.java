
package org.alfresco.repo.virtual.template;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.SearchParameters;

public class BasicConstraint implements VirtualQueryConstraint
{
    public static final BasicConstraint INSTANCE = new BasicConstraint();

    private BasicConstraint()
    {

    }

    @Override
    public SearchParameters apply(ActualEnvironment environment, VirtualQuery query) throws VirtualizationException
    {
        SearchParameters searchParameters = new SearchParameters();

        String storeRefString = query.getStoreRef();
        if (storeRefString != null)
        {
            searchParameters.addStore(new StoreRef(storeRefString));
        }
        else
        {
            searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        }

        searchParameters.setLanguage(query.getLanguage());
        searchParameters.setQuery(query.getQueryString());
        searchParameters.setQueryConsistency(QueryConsistency.TRANSACTIONAL_IF_POSSIBLE);

        return searchParameters;
    }

}
