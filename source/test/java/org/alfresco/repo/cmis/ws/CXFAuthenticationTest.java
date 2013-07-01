package org.alfresco.repo.cmis.ws;

import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;

public class CXFAuthenticationTest extends BaseWebScriptTest
{

    // RepositoryServicePort used in AbstractServiceTest substitutes #PasswordDigest
    // if Type is not set explicitly, so sending POST requests directly to remote Alfresco to test the case
    private static RemoteServer remoteServer = new RemoteServer();

    protected void setUp() throws Exception
    {
        super.setRemoteServer(remoteServer);
        super.setUp();
    }

    public void testValidPasswordTypeCXFLogin() throws Exception
    {
        String cxfEndpoint = CmisServiceTestHelper.ALFRESCO_URL + "/cmis/RepositoryService";
        String postBody = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<S:Header>" +
                "<Security xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
                "<Timestamp xmlns=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"></Timestamp>" +
                "<UsernameToken><Username>" + CmisServiceTestHelper.USERNAME_ADMIN +"</Username>" +
                "<Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">" + CmisServiceTestHelper.PASSWORD_ADMIN + "</Password></UsernameToken>" +
                "</Security>" +
            "</S:Header>" +
            "<S:Body><ns2:getRepositories xmlns=\"http://docs.oasis-open.org/ns/cmis/core/200908/\" xmlns:ns2=\"http://docs.oasis-open.org/ns/cmis/messaging/200908/\"/></S:Body>" +
            "</S:Envelope>";
        sendRequest(new PostRequest(cxfEndpoint, postBody, "text/xml"), Status.STATUS_OK);
    }

    // Post request without PasswordType
    public void testInvalidPasswordTypeCXFLogin() throws Exception
    {
        String cxfEndpoint = CmisServiceTestHelper.ALFRESCO_URL + "/cmis/RepositoryService";
        String postBody = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<S:Header>" +
            "<wsse:Security S:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
                "<wsu:Timestamp wsu:Id=\"Timestamp-7485188\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"></wsu:Timestamp>" +
                "<wsse:UsernameToken wsu:Id=\"UsernameToken-20851530\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">" +
                "<wsse:Username>"+ CmisServiceTestHelper.USERNAME_ADMIN +"</wsse:Username>" +
                "<wsse:Password>"+ CmisServiceTestHelper.PASSWORD_ADMIN +"</wsse:Password>" +
                "</wsse:UsernameToken></wsse:Security>" +
            "</S:Header>" +
            "<S:Body><ns2:getRepositories xmlns=\"http://docs.oasis-open.org/ns/cmis/core/200908/\" xmlns:ns2=\"http://docs.oasis-open.org/ns/cmis/messaging/200908/\"/></S:Body>" +
            "</S:Envelope>";
        sendRequest(new PostRequest(cxfEndpoint, postBody, "text/xml"), Status.STATUS_OK);
    }

}
