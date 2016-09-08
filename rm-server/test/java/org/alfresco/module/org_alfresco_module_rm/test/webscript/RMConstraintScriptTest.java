/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.test.webscript;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMCaveatConfigService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Test of GET RM Constraint  (User facing scripts)
 *
 * @author Mark Rogers
 */
public class RMConstraintScriptTest extends BaseRMWebScriptTestCase
{
    private MutableAuthenticationService authenticationService;
    private RMCaveatConfigService caveatConfigService;
    private PersonService personService;
    
    protected final static String RM_LIST          = "rmc:smListTest";
    protected final static String RM_LIST_URI_ELEM = "rmc_smListTest";
    
    private static final String URL_RM_CONSTRAINTS = "/api/rma/rmconstraints";
  
    @Override
    protected void initServices() 
    {
    	super.initServices();
    	
        this.caveatConfigService = (RMCaveatConfigService)getServer().getApplicationContext().getBean("CaveatConfigService");
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
    }
    
    /**
     * 
     * @throws Exception
     */
    public void testGetRMConstraint() throws Exception
    {
        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        /**
         * Delete the list to remove any junk then recreate it.
         */
        if (caveatConfigService.getRMConstraint(RM_LIST) != null)
        {
            caveatConfigService.deleteRMConstraint(RM_LIST);
        }
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);
        
        
        createUser("fbloggs");
        createUser("jrogers");
        createUser("jdoe");
      
        
        List<String> values = new ArrayList<String>();
        values.add("NOFORN");
        values.add("FGI");
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "fbloggs", values);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jrogers", values);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jdoe", values);
        
        AuthenticationUtil.setFullyAuthenticatedUser("jdoe");
        /**
         * Positive test Get the constraint 
         */
        {
            String url = URL_RM_CONSTRAINTS + "/" + RM_LIST_URI_ELEM;
            Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());
            
            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());
            
            data.getJSONArray("allowedValuesForCurrentUser");

        }
        

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        personService.deletePerson("fbloggs");
        personService.deletePerson("jrogers");
        personService.deletePerson("jdoe");
                
    }
    
    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, "title" + userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            this.personService.createPerson(ppOne);
        }        
    }
}
    
     
 