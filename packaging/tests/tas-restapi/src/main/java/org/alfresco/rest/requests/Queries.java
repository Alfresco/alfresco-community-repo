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
     * @throws Exception
     */
    public RestNodeModelsCollection findNodes() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "queries/nodes?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestNodeModelsCollection.class, request);
    }
    
    /**
     * GET on queries/people
     * 
     * @return
     * @throws Exception
     */
    public RestPersonModelsCollection findPeople() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "queries/people?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestPersonModelsCollection.class, request);
    }
    
    /**
     * GET on queries/people
     * 
     * @return
     * @throws Exception
     */
    public RestSiteModelsCollection findSites() throws Exception
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "queries/sites?{parameters}", restWrapper.getParameters());
        return restWrapper.processModels(RestSiteModelsCollection.class, request);
    }
    
    
    
    
}
