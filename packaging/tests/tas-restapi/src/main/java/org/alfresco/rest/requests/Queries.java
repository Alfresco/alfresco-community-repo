package org.alfresco.rest.requests;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestNodeModelsCollection;
import org.alfresco.rest.model.RestPersonModelsCollection;
import org.alfresco.rest.model.RestSiteModelsCollection;
import org.springframework.http.HttpMethod;

public class Queries extends ModelRequest<Queries>
{

    public Queries(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    /**
     * GET on queries/nodes
     * 
     * @return
     */
    public RestNodeModelsCollection findNodes()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "queries/nodes?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestNodeModelsCollection.class, request);
    }
    
    /**
     * GET on queries/people
     * 
     * @return
     */
    public RestPersonModelsCollection findPeople()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "queries/people?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestPersonModelsCollection.class, request);
    }
    
    /**
     * GET on queries/people
     * 
     * @return
     */
    public RestSiteModelsCollection findSites()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "queries/sites?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestSiteModelsCollection.class, request);
    }
    
    
    
    
}
