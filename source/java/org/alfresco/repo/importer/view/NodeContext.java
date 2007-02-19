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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.importer.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.ImportNode;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;


/**
 * Maintains state about the currently imported node.
 * 
 * @author David Caruana
 *
 */
public class NodeContext extends ElementContext
    implements ImportNode
{
    private ParentContext parentContext;
    private boolean isReference = false;
    private NodeRef nodeRef;
    private String importId;        // unique identifier within import (optional)
    private String uuid;            // unique identifier within repository
    private TypeDefinition typeDef;
    private String childName;
    private Map<QName, AspectDefinition> nodeAspects = new HashMap<QName, AspectDefinition>();
    private Map<QName, ChildAssociationDefinition> nodeChildAssocs = new HashMap<QName, ChildAssociationDefinition>();
    private Map<QName, Serializable> nodeProperties = new HashMap<QName, Serializable>();
    private Map<QName, DataTypeDefinition> propertyDatatypes = new HashMap<QName, DataTypeDefinition>();

    // permissions
    private boolean inherit = true;
    private List<AccessPermission> accessControlEntries = new ArrayList<AccessPermission>();
    

    /**
     * Construct
     * 
     * @param elementName
     * @param parentContext
     * @param typeDef
     */
    public NodeContext(QName elementName, ParentContext parentContext, TypeDefinition typeDef)
    {
        super(elementName, parentContext.getDictionaryService(), parentContext.getImporter());
        this.parentContext = parentContext;
        this.typeDef = typeDef;
        this.uuid = null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportNode#getParentContext()
     */
    public ParentContext getParentContext()
    {
        return parentContext;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportNode#getTypeDefinition()
     */
    public TypeDefinition getTypeDefinition()
    {
        return typeDef;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportNode#isReference()
     */
    public boolean isReference()
    {
        return isReference;
    }
    
    /**
     * @param isReference  true => this is a node reference
     */
    public void setReference(boolean isReference)
    {
        this.isReference = isReference;
    }
    
    /**
     * Set Type Definition
     * 
     * @param typeDef
     */
    public void setTypeDefinition(TypeDefinition typeDef)
    {
        this.typeDef = typeDef;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportNode#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    
    /**
     * @param nodeRef  the node ref
     */
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportNode#getUUID()
     */
    public String getUUID()
    {
        return uuid;
    }
    
    /**
     * @param uuid  uuid
     */
    public void setUUID(String uuid)
    {
        this.uuid = uuid;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportNode#getImportId()
     */
    public String getImportId()
    {
        return importId;
    }
    
    /**
     * @param importId  import scoped id
     */
    public void setImportId(String importId)
    {
        this.importId = importId;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportNode#getChildName()
     */
    public String getChildName()
    {
        return childName;
    }
    
    /**
     * @param childName  the child name
     */
    public void setChildName(String childName)
    {
        this.childName = childName;
    }

    /*
     * @param  inherit  determines if node inherits permissions from parent
     */
    public void setInheritPermissions(boolean inherit)
    {
        this.inherit = inherit;
    }
    
    /**
     * @return  true => node inherits permissions from parent
     */
    public boolean getInheritPermissions()
    {
        return this.inherit;
    }
    
    /**
     * Adds a collection property to the node
     * 
     * @param property
     */
    public void addPropertyCollection(QName property)
    {
        // Do not import properties of sys:referenceable or cm:versionable or cm:copiedfrom
        // TODO: Make this configurable...
        PropertyDefinition propDef = getDictionaryService().getProperty(property);
        ClassDefinition classDef = (propDef == null) ? null : propDef.getContainerClass();
        if (classDef != null)
        {
            if (!isImportableClass(classDef.getName()))
            {
                return;
            }
        }
        
        // create collection and assign to property
        List<Serializable>values = new ArrayList<Serializable>();
        nodeProperties.put(property, (Serializable)values);
    }
    
    
    /**
     * Adds a property to the node
     * 
     * @param property  the property name
     * @param value  the property value
     */
    public void addProperty(QName property, String value)
    {
        // Process "special" properties
        // TODO: Make this configurable...
        PropertyDefinition propDef = getDictionaryService().getProperty(property);

        // Process Alfresco UUID
        if (uuid == null && propDef != null && propDef.getName().equals(ContentModel.PROP_NODE_UUID))
        {
            uuid = value;
        }

        // Do not import properties of sys:referenceable or cm:versionable
        ClassDefinition classDef = (propDef == null) ? null : propDef.getContainerClass();
        if (classDef != null)
        {
            if (!isImportableClass(classDef.getName()))
            {
                return;
            }
        }
        
        // Handle single / multi-valued cases
        Serializable newValue = value;
        Serializable existingValue = nodeProperties.get(property);
        if (existingValue != null)
        {
            if (existingValue instanceof Collection)
            {
                // add to existing collection of values
                ((Collection<Serializable>)existingValue).add(value);
                newValue = existingValue;
            }
            else
            {
                // convert single to multi-valued
                List<Serializable>values = new ArrayList<Serializable>();
                values.add((String)existingValue);
                values.add(value);
                newValue = (Serializable)values;
            }
        }
        nodeProperties.put(property, newValue);
    }
    
    /**
     * Adds a property datatype to the node
     * 
     * @param property  property name
     * @param datatype  property datatype
     */
    public void addDatatype(QName property, DataTypeDefinition datatype)
    {
        propertyDatatypes.put(property, datatype);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportNode#getPropertyDatatypes()
     */
    public Map<QName, DataTypeDefinition> getPropertyDatatypes()
    {
        return propertyDatatypes;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportNode#getProperties()
     */
    public Map<QName, Serializable> getProperties()
    {
        return nodeProperties;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportNode#getPropertyDataType(org.alfresco.service.namespace.QName)
     */
    public DataTypeDefinition getPropertyDataType(QName propertyName)
    {
        // get property datatype
        DataTypeDefinition valueDataType = propertyDatatypes.get(propertyName);
        if (valueDataType == null)
        {
            PropertyDefinition propDef = getDictionaryService().getProperty(propertyName);
            if (propDef != null)
            {
                valueDataType = propDef.getDataType();
            }
        }
        return valueDataType;
    }
        
    /**
     * Adds an aspect to the node
     * 
     * @param aspect  the aspect
     */
    public void addAspect(AspectDefinition aspect)
    {
        if (isImportableClass(aspect.getName()))
        {
            nodeAspects.put(aspect.getName(), aspect);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.ImportNode#getNodeAspects()
     */
    public Set<QName> getNodeAspects()
    {
        return nodeAspects.keySet();
    }

    /**
     * Adds an Access Control Entry
     * 
     * @param accessStatus
     * @param authority
     * @param permission
     */
    public void addAccessControlEntry(AccessStatus accessStatus, String authority, String permission)
    {
       // Note: Map guest permission to Consumer permission - this is to handle the case where 
       //       exports made against a pre 1.2 RC2 release
       if (permission.equalsIgnoreCase("guest"))
       {
           permission = PermissionService.CONSUMER;
       }
      
       ACE ace = new ACE();
       ace.accessStatus = accessStatus;
       ace.authority = authority;
       ace.permission = permission;
       accessControlEntries.add(ace);
    }

    /**
     * Gets the Access Control Entries
     * 
     * @return  access control entries
     */
    public List<AccessPermission> getAccessControlEntries()
    {
        return accessControlEntries;
    }
    
    /**
     * Determine the type of definition (aspect, property, association) from the
     * specified name
     * 
     * @param defName
     * @return the dictionary definition
     */
    public Object determineDefinition(QName defName)
    {
        Object def = determineAspect(defName);
        if (def == null)
        {
            def = determineProperty(defName);
            if (def == null)
            {
                def = determineAssociation(defName);
            }
        }
        return def;
    }
    
    /**
     * Determine if name referes to an aspect
     * 
     * @param defName
     * @return
     */
    public AspectDefinition determineAspect(QName defName)
    {
        AspectDefinition def = null;
        if (nodeAspects.containsKey(defName) == false)
        {
            def = getDictionaryService().getAspect(defName);
        }
        return def;
    }
    
    /**
     * Determine if name refers to a property
     * 
     * @param defName
     * @return
     */
    public PropertyDefinition determineProperty(QName defName)
    {
        PropertyDefinition def = null;
        if (nodeProperties.containsKey(defName) == false)
        {
            def = (typeDef == null) ? null : getDictionaryService().getProperty(typeDef.getName(), defName);
            if (def == null)
            {
                Set<AspectDefinition> allAspects = new HashSet<AspectDefinition>();
                if (typeDef != null)
                {
                    allAspects.addAll(typeDef.getDefaultAspects());
                }
                allAspects.addAll(nodeAspects.values());
                for (AspectDefinition aspectDef : allAspects)
                {
                    def = getDictionaryService().getProperty(aspectDef.getName(), defName);
                    if (def != null)
                    {
                        break;
                    }
                }
            }
        }
        return def;
    }
    
    /**
     * Determine if name referes to an association
     * 
     * @param defName
     * @return
     */
    public AssociationDefinition determineAssociation(QName defName)
    {
        AssociationDefinition def = null;
        if (nodeChildAssocs.containsKey(defName) == false)
        {
            def = getDictionaryService().getAssociation(defName);
        }
        return def;
    }
    
    /**
     * Determine if the provided class name is to be imported
     * 
     * @param className  class to check (type or aspect)
     * @return  true => import,  false => ignore on import
     */
    private boolean isImportableClass(QName className)
    {
        return !getImporter().isExcludedClass(className);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "NodeContext[childName=" + getChildName() + ",type=" + (typeDef == null ? "null" : typeDef.getName()) + ",nodeRef=" + nodeRef + 
            ",aspects=" + nodeAspects.values() + ",parentContext=" + parentContext.toString() + "]";
    }
 
    /**
     * Access Control Entry
     */
    private class ACE implements AccessPermission
    {
        private AccessStatus accessStatus;
        private String authority;
        private String permission;

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.security.AccessPermission#getPermission()
         */
        public String getPermission()
        {
            return permission;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.security.AccessPermission#getAccessStatus()
         */
        public AccessStatus getAccessStatus()
        {
            return accessStatus;
        }
        
        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.security.AccessPermission#getAuthority()
         */
        public String getAuthority()
        {
            return authority;
        }
        
        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.security.AccessPermission#getAuthorityType()
         */
        public AuthorityType getAuthorityType()
        {
            return null;
        }
    }

}
