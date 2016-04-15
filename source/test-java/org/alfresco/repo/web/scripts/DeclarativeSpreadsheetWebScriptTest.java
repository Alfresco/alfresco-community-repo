package org.alfresco.repo.web.scripts;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Test for DeclarativeSpreadsheetWebScript class
 * 
 * @author alex.mukha
 * @since 4.2.4
 */
public class DeclarativeSpreadsheetWebScriptTest extends BaseWebScriptTest
{
    private String admin;
    private static String URL = "/api/test/getcsv";
    protected static final QName[] COLUMNS = new QName[]
            {
                ContentModel.PROP_USERNAME,
                ContentModel.PROP_FIRSTNAME, 
                ContentModel.PROP_LASTNAME
            };
    
    /** The context locations, in reverse priority order. */
    private static final String CONFIG_LOCATION = "classpath:alfresco/declarative-spreadsheet-webscript-application-context.xml";
    
    @Override
    protected void setUp() throws Exception
    {
        super.setCustomContext(CONFIG_LOCATION);
        super.setUp();
        getServer().getApplicationContext();
        admin = AuthenticationUtil.getAdminUserName();
        AuthenticationUtil.setFullyAuthenticatedUser(admin);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    };
    
    public void testCSVStrategy() throws Exception
    {
        TestWebScriptServer.GetRequest req = new TestWebScriptServer.GetRequest(URL);
        Response response = sendRequest(req, Status.STATUS_OK, admin);
        // default excel, delimiter is a comma ","
        assertEquals("The response CSV body was not correct.", "User Name,First Name,Last Name\n", response.getContentAsString());
        
        req = new TestWebScriptServer.GetRequest(URL + "?" + DeclarativeSpreadsheetWebScript.PARAM_REQ_DELIMITER + "=%2C");
        response = sendRequest(req, Status.STATUS_OK, admin);
        // delimiter is a comma ","
        assertEquals("The response CSV body was not correct.", "User Name,First Name,Last Name\n", response.getContentAsString());
        
        req = new TestWebScriptServer.GetRequest(URL + "?" + DeclarativeSpreadsheetWebScript.PARAM_REQ_DELIMITER + "=%09");
        response = sendRequest(req, Status.STATUS_OK, admin);
        // delimiter is a tab space "\t"
        assertEquals("The response CSV body was not correct.", "User Name\tFirst Name\tLast Name\n", response.getContentAsString());
        
        req = new TestWebScriptServer.GetRequest(URL + "?" + DeclarativeSpreadsheetWebScript.PARAM_REQ_DELIMITER + "=%3B");
        response = sendRequest(req, Status.STATUS_OK, admin);
        // delimiter is a semicolon ";"
        assertEquals("The response CSV body was not correct.", "User Name;First Name;Last Name\n", response.getContentAsString());
    }
}
