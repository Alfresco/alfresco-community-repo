package org.alfresco.repo.jscript.app;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * Username property decorator class.
 *
 * @author Mike Hatfield
 */
public class UsernamePropertyDecorator extends BasePropertyDecorator
{
    /** Person service */
    private PersonService personService = null;
    
    /**
     * @param personService person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * @see org.alfresco.repo.jscript.app.PropertyDecorator#decorate(QName, org.alfresco.service.cmr.repository.NodeRef, java.io.Serializable)
     */
    @SuppressWarnings("unchecked")
    public JSONAware decorate(QName propertyName, NodeRef nodeRef, Serializable value)
    {
        String username = value.toString();
        String firstName = null;
        String lastName = null;
        JSONObject map = new JSONObject();
        map.put("userName", username);
        
        // DO NOT change this to just use getPersonOrNullImpl
        //  - there is Cloud THOR prod hack see personServiceImpl.personExists
        //  - and THOR-293 
        if (username.isEmpty())
        {
            firstName = "";
            lastName = "";
        }
        else if (this.personService.personExists(username))
        {
            NodeRef personRef = this.personService.getPerson(username, false);
            firstName = (String)this.nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
            lastName = (String)this.nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
        }
        else if (username.equals("System") || username.startsWith("System@"))
        {
            firstName = "System";
            lastName = "User";
        }
        else
        {
            map.put("isDeleted", true);
            return map;
        }
        
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("displayName", ((firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "")).replaceAll("^\\s+|\\s+$", ""));
        return map;
    }
}
