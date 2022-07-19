package org.alfresco.rest.requests;

import static io.restassured.RestAssured.given;
import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.core.RestProperties;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpStatus;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Declares all Rest API under the /alfresco/service/api/tenants path
 *
 */
public class Tenant extends ModelRequest<Tenant>
{
    private RestProperties restProperties;

    public Tenant(RestWrapper restWrapper, RestProperties restProperties)
    {
        super(restWrapper);
        this.restProperties = restProperties;
    }
    /**
     * Create tenant using POST call on "http://{server}:{port}/alfresco/service/api/tenants"
     * 
     * @param userModel
     * @return
     * @throws JsonToModelConversionException
     */
    public void createTenant(UserModel userModel)
    {
        STEP(String.format("DATAPREP: Create new tenant %s", userModel.getDomain()));
        String json = String.format("{\"tenantDomain\": \"%s\", \"tenantAdminPassword\": \"%s\"}", userModel.getDomain(), DataUser.PASSWORD);
        RequestSpecification request = given().auth().basic(restWrapper.getTestUser().getUsername(), restWrapper.getTestUser().getPassword())
                .contentType(ContentType.JSON);
        Response returnedResponse = request.contentType(ContentType.JSON).body(json)
                .post(String.format("%s/%s", restProperties.envProperty().getFullServerUrl(), "alfresco/service/api/tenants")).andReturn();
        if (!Integer.valueOf(returnedResponse.getStatusCode()).equals(HttpStatus.OK.value()))
        {
            throw new IllegalStateException(String.format("Tenant is not created: %s", returnedResponse.asString()));
        }
    }
}
