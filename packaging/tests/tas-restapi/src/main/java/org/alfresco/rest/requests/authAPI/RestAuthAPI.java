package org.alfresco.rest.requests.authAPI;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestTicketBodyModel;
import org.alfresco.rest.model.RestTicketModel;
import org.alfresco.rest.requests.ModelRequest;
import org.springframework.http.HttpMethod;

import io.restassured.RestAssured;

public class RestAuthAPI extends ModelRequest<RestAuthAPI>
{
    public RestAuthAPI(RestWrapper restWrapper)
    {
        super(restWrapper);
        RestAssured.basePath = "alfresco/api/-default-/public/authentication/versions/1";
        restWrapper.configureRequestSpec().setBasePath(RestAssured.basePath);
    }

    public RestTicketModel createTicket(RestTicketBodyModel ticketBody)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, ticketBody.toJson(), "tickets");
        return restWrapper.processModel(RestTicketModel.class, request);
    }

    public RestTicketModel getTicket()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tickets/-me-");
        return restWrapper.processModel(RestTicketModel.class, request);
    }

    public void removeTicket()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.DELETE, "tickets/-me-");
        restWrapper.processEmptyModel(request);
    }

}
