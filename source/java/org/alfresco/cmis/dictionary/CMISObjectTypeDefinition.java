/*
 * Copyright (C) 2005-20079 Alfresco Software Limited.
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
package org.alfresco.cmis.dictionary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.dictionary.AbstractCMISDictionaryService.DictionaryRegistry;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;


/**
 * CMIS Object Type Definition
 * 
 * @author davidc
 */
public class CMISObjectTypeDefinition implements CMISTypeDefinition, Serializable
{
    private static final long serialVersionUID = -3131505923356013430L;

    // Object type properties
    protected ClassDefinition cmisClassDef;
    protected CMISTypeId objectTypeId;
    protected String objectTypeQueryName;
    protected String displayName;
    protected CMISTypeId parentTypeId;
    protected CMISTypeDefinition parentType;
    protected CMISTypeDefinition rootType;
    protected String description;
    protected boolean creatable;
    protected boolean queryable;
    protected boolean controllable;
    protected boolean includeInSuperTypeQuery;
    protected Collection<CMISTypeId> subTypeIds = new ArrayList<CMISTypeId>();
    protected Collection<CMISTypeDefinition> subTypes = new ArrayList<CMISTypeDefinition>();
    protected Map<CMISPropertyId, CMISPropertyDefinition> properties = new HashMap<CMISPropertyId, CMISPropertyDefinition>();
    protected Map<CMISPropertyId, CMISPropertyDefinition> inheritedProperties = new HashMap<CMISPropertyId, CMISPropertyDefinition>();
    

    /**
     * Construct
     * 
     * @param cmisMapping
     * @param dictionaryService
     * @return
     */
    /*package*/ Map<CMISPropertyId, CMISPropertyDefinition> createProperties(CMISMapping cmisMapping, DictionaryService dictionaryService)
    {
        // map properties directly defined on this type
        for (PropertyDefinition propDef : cmisClassDef.getProperties().values())
        {
            if (propDef.getContainerClass().equals(cmisClassDef))
            {
                if (cmisMapping.getDataType(propDef.getDataType()) != null)
                {
                    CMISPropertyDefinition cmisPropDef = createProperty(cmisMapping, propDef);
                    properties.put(cmisPropDef.getPropertyId(), cmisPropDef);
                }
            }
        }
        
        // map properties directly defined on default aspects
        for (AspectDefinition aspectDef : cmisClassDef.getDefaultAspects(false))
        {
            for (PropertyDefinition propDef : aspectDef.getProperties().values())
            {
                if (cmisMapping.getDataType(propDef.getDataType()) != null)
                {
                    CMISPropertyDefinition cmisPropDef = createProperty(cmisMapping, propDef);
                    properties.put(cmisPropDef.getPropertyId(), cmisPropDef);
                }
            }
        }
        
        return properties;
    }

    /**
     * Create Property Definition
     * 
     * @param cmisMapping
     * @param propDef
     * @return
     */
    private CMISPropertyDefinition createProperty(CMISMapping cmisMapping, PropertyDefinition propDef)
    {
        QName propertyQName = propDef.getName(); 
        String propertyName = cmisMapping.getCmisPropertyName(propertyQName);
        String propertyId = cmisMapping.getCmisPropertyId(propertyQName);
        CMISPropertyId cmisPropertyId = new CMISPropertyId(propertyName, propertyId, propertyQName);
        return new CMISPropertyDefinition(cmisMapping, cmisPropertyId, propDef, this);
    }

    /**
     * Create Sub Types
     * 
     * @param cmisMapping
     * @param dictionaryService
     */
    /*package*/ void createSubTypes(CMISMapping cmisMapping, DictionaryService dictionaryService)
    {
        Collection<QName> subTypes = dictionaryService.getSubTypes(objectTypeId.getQName(), false);
        for (QName subType : subTypes)
        {
            if (cmisMapping.isValidCmisDocumentOrFolder(subType))
            {
                CMISTypeId subTypeId = cmisMapping.getCmisTypeId(subType);
                if (subTypeId != null)
                {
                    subTypeIds.add(subTypeId);
                }
            }
        }
    }

    /**
     * Resolve Dependencies
     * 
     * @param registry
     */
    /*package*/ void resolveDependencies(DictionaryRegistry registry)
    {
        if (parentTypeId != null)
        {
            parentType = registry.typeDefsByTypeId.get(parentTypeId);
            if (parentType == null)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve parent type for type id " + parentTypeId);
            }
        }
        CMISTypeId rootTypeId = objectTypeId.getRootTypeId();
        if (rootTypeId != null)
        {
            rootType = registry.typeDefsByTypeId.get(rootTypeId);
            if (rootType == null)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve root type for type id " + rootTypeId);
            }
        }
        for (CMISTypeId subTypeId : subTypeIds)
        {
            CMISTypeDefinition subType = registry.typeDefsByTypeId.get(subTypeId);
            if (subType == null)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve sub type for type id " + subTypeId + " for parent type " + objectTypeId);
            }
            subTypes.add(subType);
        }
    }

    /**
     * Resolve Inheritance
     * 
     * @param registry
     */
    /*package*/ void resolveInheritance(DictionaryRegistry registry)
    {
        inheritedProperties.putAll(properties);
        if (parentType != null)
        {
            inheritedProperties.putAll(parentType.getPropertyDefinitions());
        }
    }
    

    
    /**
     * Get the unique identifier for the type
     * 
     * @return - the type id
     */
    public CMISTypeId getTypeId()
    {
        return objectTypeId;
    }

    /**
     * Get the table name used for queries against the type. This is also a unique identifier for the type. The string
     * conforms to SQL table naming conventions. TODO: Should we impose a maximum length and if so how do we avoid
     * collisions from truncations?
     * 
     * @return the sql table name
     */
    public String getQueryName()
    {
        return objectTypeQueryName;
    }

    /**
     * Get the display name for the type.
     * 
     * @return - the display name
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Get parent type
     * 
     * @return
     */
    public CMISTypeDefinition getParentType()
    {
        return parentType;
    }

    /**
     * Get the root type
     * 
     * @return
     */
    public CMISTypeDefinition getRootType()
    {
        return rootType;
    }

    /**
     * Get sub types
     * 
     * @return
     */
    public Collection<CMISTypeDefinition> getSubTypes(boolean includeDescendants)
    {
        if (!includeDescendants)
        {
            return subTypes;
        }

        // recurse sub-type hierarchy
        Collection<CMISTypeDefinition> descendants = new ArrayList<CMISTypeDefinition>();
        LinkedList<CMISTypeDefinition> stack = new LinkedList<CMISTypeDefinition>();
        stack.addLast(this);
        while (stack.size() > 0)
        {
            CMISTypeDefinition current = stack.removeLast();
            if (!current.equals(this))  // do not add this type
            {
                descendants.add(current);
            }
            
            // descend...
            for (CMISTypeDefinition type : current.getSubTypes(false))
            {
                stack.addLast(type);
            }
        }
        return descendants;
    }
    
    /**
     * Get the description for the type
     * 
     * @return - the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Can objects of this type be created?
     * 
     * @return
     */
    public boolean isCreatable()
    {
        return creatable;
    }

    /**
     * Is this type queryable? If not, the type may not appear in the FROM clause of a query. This property of the type
     * is not inherited in the type hierarchy. It is set on each type.
     * 
     * @return true if queryable
     */
    public boolean isQueryable()
    {
        return queryable;
    }

    /**
     * Are objects of this type controllable.
     * 
     * @return
     */
    public boolean isControllable()
    {
        return controllable;
    }
    
    /**
     * Are objects of this type included in super type queries
     * 
     * @return
     */
    public boolean isIncludeInSuperTypeQuery()
    {
        return includeInSuperTypeQuery;
    }

    /**
     * Gets the property definitions for this type
     * 
     * @return  property definitions
     */
    public Map<CMISPropertyId, CMISPropertyDefinition> getPropertyDefinitions()
    {
        return inheritedProperties;
    }


    //
    // Document Type specific
    //

    /**
     * Is Fileable?
     * 
     * @return
     */
    public boolean isFileable()
    {
        return false;
    }

    /**
     * Is Versionable?
     * 
     * @return
     */
    public boolean isVersionable()
    {
        return false;
    }
    
    /**
     * Is Content Stream Allowed?
     * 
     * @return
     */
    public CMISContentStreamAllowedEnum getContentStreamAllowed()
    {
        return CMISContentStreamAllowedEnum.NOT_ALLOWED;
    }
    
    //
    // Relationship Type specific
    //
    
    /**
     * Get allowed source types
     * 
     * @return
     */
    public Collection<CMISTypeDefinition> getAllowedSourceTypes()
    {
        return Collections.emptyList();
    }

    /**
     * Get allowed target types
     * 
     * @return
     */
    public Collection<CMISTypeDefinition> getAllowedTargetTypes()
    {
        return Collections.emptyList();
    }

}
