/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.audit.generator;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.springframework.extensions.surf.util.PropertyCheck;

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
