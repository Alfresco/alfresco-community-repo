/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.testing.category.LuceneTests;
import org.junit.experimental.categories.Category;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Test for DeclarativeSpreadsheetWebScript class
 * 
 * @author alex.mukha
 * @since 4.2.4
 */
@Category(LuceneTests.class)
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
    
    public void testCSVFormat() throws Exception
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
