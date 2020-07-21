package org.alfresco.rest.auth;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestTicketBodyModel;
import org.alfresco.rest.model.RestTicketModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AuthTests extends RestTest
{

    private RestWrapper addRestRequestAuthorization(RestTicketModel ticketModel)
    {
        restClient.configureRequestSpec().addHeader("Authorization", "Basic " + encodeB64(ticketModel.getId()));
        return restClient;
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.AUTH }, executionType = ExecutionType.SANITY, description = "Verify HttpMethod.POST tickets")
    @Test(groups = { TestGroup.REST_API, TestGroup.SANITY, TestGroup.AUTH })
    public void adminShouldGetTicketBody() throws JsonToModelConversionException, Exception
    {
        RestTicketBodyModel ticketBody = new RestTicketBodyModel();
        ticketBody.setUserId("admin");
        ticketBody.setPassword("admin");

        RestTicketModel ticketReturned = restClient.authenticateUser(dataContent.getAdminUser()).withAuthAPI().createTicket(ticketBody);

        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        ticketReturned.assertThat().field("id").contains("TICKET_");
    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.AUTH }, executionType = ExecutionType.SANITY, description = "Verify HttpMethod.GET tickets/-me-")
    @Test(groups = { TestGroup.REST_API, TestGroup.SANITY, TestGroup.AUTH })
    public void randomUserGetTicket() throws Exception
    {
        UserModel userModel = dataUser.createRandomTestUser();
        RestTicketBodyModel ticketBody = new RestTicketBodyModel();
        ticketBody.setUserId(userModel.getUsername());
        ticketBody.setPassword(userModel.getPassword());

        RestTicketModel ticketCreated = restClient.authenticateUser(userModel).withAuthAPI().createTicket(ticketBody);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        addRestRequestAuthorization(ticketCreated);
        RestTicketModel ticketReturned = restClient.withAuthAPI().getTicket();
        Assert.assertEquals(ticketCreated.getId(), ticketReturned.getId());

    }

    @TestRail(section = { TestGroup.REST_API,
            TestGroup.AUTH }, executionType = ExecutionType.SANITY, description = "Verify HttpMethod.REMOVE tickets/-me-")
    @Test(groups = { TestGroup.REST_API, TestGroup.SANITY, TestGroup.AUTH })
    public void randomUserRemoveTicket() throws Exception
    {
        UserModel userModel = dataUser.createRandomTestUser();
        RestTicketBodyModel ticketBody = new RestTicketBodyModel();
        ticketBody.setUserId(userModel.getUsername());
        ticketBody.setPassword(userModel.getPassword());

        RestTicketModel ticketCreated = restClient.authenticateUser(userModel).withAuthAPI().createTicket(ticketBody);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        addRestRequestAuthorization(ticketCreated);
        restClient.withAuthAPI().removeTicket();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.withAuthAPI().getTicket();
        restClient.assertLastError().containsErrorKey("Ticket base authentication required.");

    }

    private String encodeB64(String str)
    {
        return Base64.encodeBase64String(str.getBytes());
    }

}