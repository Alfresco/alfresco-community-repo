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
package org.alfresco.repo.web.scripts.archive;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PathUtil;

/**
 * A simple POJO class for the state of an archived node. For easier passing to the FTL model.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
public class ArchivedNodeState
{
    private NodeRef archivedNodeRef;
    private String archivedBy;
    private Date archivedDate;
    private String name;
    private String title;
    private String description;
    private String displayPath;
    private String firstName;
    private String lastName;
    private String nodeType;
    
    /**
     * To prevent unauthorised construction.
     */
    private ArchivedNodeState() { /* Intentionally empty*/ }
    
    public static ArchivedNodeState create(NodeRef archivedNode, ServiceRegistry serviceRegistry)
    {
        ArchivedNodeState result = new ArchivedNodeState();
        
        NodeService nodeService = serviceRegistry.getNodeService();
        Map<QName, Serializable> properties = nodeService.getProperties(archivedNode);

        result.archivedNodeRef = archivedNode;
        result.archivedBy = (String) properties.get(ContentModel.PROP_ARCHIVED_BY);
        result.archivedDate = (Date) properties.get(ContentModel.PROP_ARCHIVED_DATE);
        result.name = (String) properties.get(ContentModel.PROP_NAME);
        result.title = (String) properties.get(ContentModel.PROP_TITLE);
        result.description = (String) properties.get(ContentModel.PROP_DESCRIPTION);
        result.nodeType = nodeService.getType(archivedNode).toPrefixString(serviceRegistry.getNamespaceService());

        PersonService personService = serviceRegistry.getPersonService();
        if (result.archivedBy != null && personService.personExists(result.archivedBy))
        {
            NodeRef personNodeRef = personService.getPerson(result.archivedBy, false);
            Map<QName, Serializable> personProps = nodeService.getProperties(personNodeRef);
            
            result.firstName = (String) personProps.get(ContentModel.PROP_FIRSTNAME);
            result.lastName = (String) personProps.get(ContentModel.PROP_LASTNAME);
        }
        
        ChildAssociationRef originalParentAssoc = (ChildAssociationRef) properties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
        
        if (serviceRegistry.getPermissionService().hasPermission(originalParentAssoc.getParentRef(), PermissionService.READ).equals(AccessStatus.ALLOWED)
                && nodeService.exists(originalParentAssoc.getParentRef()))
        {
           result.displayPath = PathUtil.getDisplayPath(nodeService.getPath(originalParentAssoc.getParentRef()), true);
        }
        else
        {
           result.displayPath = "";
        }
        
        return result;
    }
    
    public NodeRef getNodeRef()
    {
        return this.archivedNodeRef;
    }
    
    public String getArchivedBy()
    {
        return this.archivedBy;
    }
    
    public Date getArchivedDate()
    {
        return this.archivedDate;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getTitle()
    {
        return this.title;
    }
    
    public String getDescription()
    {
        return this.description;
    }
    
    public String getDisplayPath()
    {
        return this.displayPath;
    }
    
    public String getFirstName()
    {
        return this.firstName;
    }
    
    public String getLastName()
    {
        return this.lastName;
    }
    
    public String getNodeType()
    {
        return this.nodeType;
    }
}
