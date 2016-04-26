package org.alfresco.repo.audit.generator;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyCheck;

/**
 * Gives back the full name (person details) of the currently-authenticated user.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuthenticatedPersonDataGenerator extends AbstractDataGenerator
{
    private PersonService personService;
    private NodeService nodeService;
    
    /**
     * Set the service used to discover the user's person node
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * Set the service to retrieve the user's full name
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
    }

    /**
     * @return              Returns the full name of the currently-authenticated user
     */
    public Serializable getData() throws Throwable
    {
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        NodeRef personNodeRef = personService.getPerson(user);
        String fullName = null;
        if (personNodeRef != null && nodeService.exists(personNodeRef))
        {
            String firstName = (String)nodeService.getProperty(personNodeRef, ContentModel.PROP_FIRSTNAME);
            String lastName = (String)nodeService.getProperty(personNodeRef, ContentModel.PROP_LASTNAME);
            
            fullName = ((firstName != null && firstName.length() > 0) ? firstName : "");
            if (lastName != null && lastName.length() > 0)
            {
                fullName += (fullName.length() > 0 ? " " : "");
                fullName += lastName;
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Generated name '" + fullName + "' for user '" + user + "'.");
        }
        return fullName;
    }
}
