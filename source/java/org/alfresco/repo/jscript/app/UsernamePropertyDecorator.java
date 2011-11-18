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
package org.alfresco.repo.jscript.app;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;

/**
 * Username property decorator class.
 *
 * @author Mike Hatfield
 */
public class UsernamePropertyDecorator implements PropertyDecorator
{
    private ServiceRegistry services;
    private NodeService nodeService = null;
    private PersonService personService = null;

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.services = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.personService = serviceRegistry.getPersonService();
    }

    public Serializable decorate(NodeRef nodeRef, String propertyName, Serializable value)
    {
        String username = value.toString();
        String firstName = null;
        String lastName = null;
        Map<String, Serializable> map = new LinkedHashMap<String, Serializable>(4);
        map.put("userName", username);

        if (this.personService.personExists(username))
        {
            NodeRef personRef = this.personService.getPerson(username);
            Map<QName, Serializable> properties = this.nodeService.getProperties(personRef);
            firstName = (String)properties.get(ContentModel.PROP_FIRSTNAME);
            lastName = (String)properties.get(ContentModel.PROP_LASTNAME);
        }
        else if (username.equals("System") || username.startsWith("System@"))
        {
            firstName = "System";
            lastName = "User";
        }
        else
        {
            map.put("isDeleted", true);
            return (Serializable)map;
        }

        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("displayName", (firstName != null ? firstName + " " : "" + lastName != null ? lastName : "").replaceAll("^\\s+|\\s+$", ""));
        return (Serializable)map;
    }
}
