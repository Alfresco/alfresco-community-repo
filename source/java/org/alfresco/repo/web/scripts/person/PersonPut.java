/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.person;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Content;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.json.JSONObject;


/**
 * Web Script to update a person's details
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class PersonPut extends DeclarativeWebScript
{
    /**
     * Carries out the work of updating the given person node with the given
     * person properties if the current user has the appropriate permissions to do so
     */
    private class UpdatePersonPropertiesWorker implements RunAsWork<NodeRef>
    {
        // the person node whose properties to update
        private NodeRef person;

        // the properties to update the person with
        private JSONObject personProps;

        public UpdatePersonPropertiesWorker(NodeRef person, JSONObject personProps)
        {
            this.person = person;
            this.personProps = personProps;
        }

        public NodeRef doWork() throws Exception
        {
            // get references to services
            NodeService nodeService = PersonPut.this.serviceRegistry.getNodeService();
            
            // Update the given person node's properties
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(16);
            props.put(ContentModel.PROP_USERNAME, (Serializable) personProps.get("userName"));
            props.put(ContentModel.PROP_TITLE, (Serializable) personProps.get("title"));
            props.put(ContentModel.PROP_FIRSTNAME, (Serializable) personProps.get("firstName"));
            props.put(ContentModel.PROP_LASTNAME, (Serializable) personProps.get("lastName"));
            props.put(ContentModel.PROP_ORGANIZATION, (Serializable) personProps.get("organisation"));
            props.put(ContentModel.PROP_JOBTITLE, (Serializable) personProps.get("jobtitle"));
            props.put(ContentModel.PROP_EMAIL, (Serializable) personProps.get("email"));
            try
            {
                nodeService.setProperties(this.person, props);
            }
            catch (AccessDeniedException err)
            {
                // catch security exception if the user does not have permissions
                String currentUserName = AuthenticationUtil.getCurrentUserName();
                String personUserName = (String)nodeService.getProperty(person, ContentModel.PROP_USERNAME);
                throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Current user: "
                        + currentUserName + " does not have the appropriate permissons to update "
                        + " person node with userName: " + personUserName);
            }

            return this.person;
        }
    }

    // service and service component references
    private ServiceRegistry serviceRegistry;
    private AuthenticationComponent authenticationComponent;

    // model property keys
    private static final String MODEL_PROP_KEY_PERSON = "person";

    /**
     * Sets the serviceRegistry property
     * 
     * @param serviceRegistry the service registry to set 
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * Sets the authenticationComponent property
     * 
     * @param authenticationComponent
     *            the authentication component to set
     */
    public void setAuthenticationComponent(
            AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /*
     * This method contains the logic which gets executed when the Web
     * Script is run
     * 
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(
     *      org.alfresco.web.scripts.WebScriptRequest,
     *      org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status)
    {
        // initialise model to pass on for template to render
        Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
        
        // get references to services from service registry
        PersonService personService = this.serviceRegistry.getPersonService();
        
        // get request body content
        Content content = req.getContent();
        if (content == null)
        {
            throw new WebScriptException("Failed to convert request body content to JSON object");
        }

        // get the person properties passed in as a JSON object
        JSONObject personJsonProps = null;
        try
        {
            String jsonString = content.getContent();
            if (jsonString.startsWith("[") == true)
            {
                throw new WebScriptException(
                        Status.STATUS_INTERNAL_SERVER_ERROR,
                        "Properties for multiple people appear to have been passed in through in the request "
                                + "body as a JSON Array. Only one set of person properties must be passed "
                                + "through. Request body content is: " + jsonString);
            }
            else
            {
                personJsonProps = new JSONObject(jsonString);
            }
        }
        catch (Exception exception)
        {
            throw new WebScriptException("Failed to convert request body to JSON object", exception);
        }

        // Extract user name from the URL
        String userName = req.getExtensionPath();
        
        // Get person noderef associated with that user name
        NodeRef person = personService.getPerson(userName);

        // if person node has been found associated with the given user name
        // then update that person's details with the person properties
        // provided within the JSON object (in the request body)
        if (person != null)
        {
            RunAsWork<NodeRef> updatePersonPropertiesWorker = new UpdatePersonPropertiesWorker(
                    person, personJsonProps);
            NodeRef updatedPerson = AuthenticationUtil.runAs(
                    updatePersonPropertiesWorker, this.authenticationComponent.getSystemUserName());

            // Put the updated person on the model to pass to the template
            model.put(MODEL_PROP_KEY_PERSON, new ScriptNode(updatedPerson, this.serviceRegistry));
        }
        // else if no person was found matching the given user name,
        // then return HTTP error status "not found"
        else
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Person "
                    + userName + " does not exist and thus can't be updated");
        }

        return model;
    }
}
