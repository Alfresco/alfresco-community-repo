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

import org.alfresco.cmis.CMISActionEvaluator;
import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISPropertyId;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISTypeId;
import org.alfresco.cmis.dictionary.CMISAbstractDictionaryService.DictionaryRegistry;
import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * CMIS Object Type Definition
 * 
 * @author davidc
 */
public class CMISAbstractTypeDefinition implements CMISTypeDefinition, Serializable
{
    // Logger
    protected static final Log logger = LogFactory.getLog(CMISAbstractTypeDefinition.class);

    private static final long serialVersionUID = -3131505923356013430L;

    // Object type properties
    protected boolean isPublic;
    protected ClassDefinition cmisClassDef;
    protected CMISTypeId objectTypeId;
    protected String objectTypeQueryName;
    protected String displayName;
    protected CMISTypeId parentTypeId;
    protected CMISTypeDefinition parentType;
    protected CMISAbstractTypeDefinition internalParentType;
    protected CMISTypeDefinition rootType;
    protected String description;
    protected boolean creatable;
    protected boolean queryable;
    protected boolean controllable;
    protected boolean includeInSuperTypeQuery;
    protected Collection<CMISTypeId> subTypeIds = new ArrayList<CMISTypeId>();
    protected Collection<CMISTypeDefinition> subTypes = new ArrayList<CMISTypeDefinition>();
    protected Map<String, CMISPropertyDefinition> properties = new HashMap<String, CMISPropertyDefinition>();
    protected Map<String, CMISPropertyDefinition> inheritedProperties = new HashMap<String, CMISPropertyDefinition>();
    protected Map<String, CMISPropertyDefinition> ownedProperties = new HashMap<String, CMISPropertyDefinition>();
    protected Map<CMISAllowedActionEnum, CMISActionEvaluator> actionEvaluators;
    

    /**
     * Construct
     * 
     * @param cmisMapping
     * @param dictionaryService
     * @return
     */
    /*package*/ Map<String, CMISPropertyDefinition> createProperties(CMISMapping cmisMapping, DictionaryService dictionaryService)
    {
        // map properties directly defined on this type
        for (PropertyDefinition propDef : cmisClassDef.getProperties().values())
        {
            if (propDef.getContainerClass().equals(cmisClassDef) && !propDef.isOverride())
            {
                if (cmisMapping.getDataType(propDef.getDataType()) != null)
                {
                    CMISPropertyDefinition cmisPropDef = createProperty(cmisMapping, propDef);
                    properties.put(cmisPropDef.getPropertyId().getName(), cmisPropDef);
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
        return new CMISBasePropertyDefinition(cmisMapping, cmisPropertyId, propDef, this);
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
            internalParentType = registry.objectDefsByTypeId.get(parentTypeId);
            if (internalParentType == null)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve parent type for type id " + parentTypeId);
            }
            if (internalParentType.isPublic() == isPublic)
            {
                parentType = internalParentType;
            }
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Type " + objectTypeId + ": parent=" + (parentType == null ? "<none>" : parentType.getTypeId()) +
                    ", internal parent=" + (internalParentType == null ? "<none>" : internalParentType.getTypeId()));
        
        CMISTypeId rootTypeId = objectTypeId.getRootTypeId();
        if (rootTypeId != null)
        {
            rootType = registry.objectDefsByTypeId.get(rootTypeId);
            if (rootType == null)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve root type for type id " + rootTypeId);
            }
        }

        if (logger.isDebugEnabled())
            logger.debug("Type " + objectTypeId + ": root=" + rootType.getTypeId());
        
        for (CMISTypeId subTypeId : subTypeIds)
        {
            CMISTypeDefinition subType = registry.objectDefsByTypeId.get(subTypeId);
            if (subType == null)
            {
                throw new AlfrescoRuntimeException("Failed to retrieve sub type for type id " + subTypeId + " for parent type " + objectTypeId);
            }
            if (subType.isPublic() == isPublic)
            {
                subTypes.add(subType);

                if (logger.isDebugEnabled())
                    logger.debug("Type " + objectTypeId + ": subtype=" + subType.getTypeId());
            }
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
        ownedProperties.putAll(properties);
        if (internalParentType != null)
        {
            inheritedProperties.putAll(internalParentType.getPropertyDefinitions());
            
            // collapse all internal inherited properties into owned properties
            if (internalParentType.isPublic != isPublic)
            {
                ownedProperties.putAll(internalParentType.getPropertyDefinitions());
            }

            if (logger.isDebugEnabled())
                logger.debug("Type " + objectTypeId + " inheriting properties: " + internalParentType.getPropertyDefinitions().size() + " from " + internalParentType.getTypeId());
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Type " + objectTypeId + " properties: " + inheritedProperties.size() + ", owned: " + ownedProperties.size());
    }

    /**
     * Get internal parent type
     * 
     * @return
     */
    public CMISAbstractTypeDefinition getInternalParentType()
    {
        return internalParentType;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#isPublic()
     */
    public boolean isPublic()
    {
        return isPublic;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getTypeId()
     */
    public CMISTypeId getTypeId()
    {
        return objectTypeId;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getQueryName()
     */
    public String getQueryName()
    {
        return objectTypeQueryName;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getDisplayName()
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getParentType()
     */
    public CMISTypeDefinition getParentType()
    {
        return parentType;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getRootType()
     */
    public CMISTypeDefinition getRootType()
    {
        return rootType;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getSubTypes(boolean)
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getDescription()
     */
    public String getDescription()
    {
        return description;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#isCreatable()
     */
    public boolean isCreatable()
    {
        return creatable;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#isQueryable()
     */
    public boolean isQueryable()
    {
        return queryable;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#isControllable()
     */
    public boolean isControllable()
    {
        return controllable;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#isIncludeInSuperTypeQuery()
     */
    public boolean isIncludeInSuperTypeQuery()
    {
        return includeInSuperTypeQuery;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getPropertyDefinitions()
     */
    public Map<String, CMISPropertyDefinition> getPropertyDefinitions()
    {
        return inheritedProperties;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getOwnedPropertyDefinitions()
     */
    public Map<String, CMISPropertyDefinition> getOwnedPropertyDefinitions()
    {
        return ownedProperties;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISTypeDefinition#getActionEvaluators()
     */
    public Map<CMISAllowedActionEnum, CMISActionEvaluator> getActionEvaluators()
    {
        return actionEvaluators;
    }
    
    //
    // Document Type specific
    //
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#isFileable()
     */
    public boolean isFileable()
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#isVersionable()
     */
    public boolean isVersionable()
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getContentStreamAllowed()
     */
    public CMISContentStreamAllowedEnum getContentStreamAllowed()
    {
        return CMISContentStreamAllowedEnum.NOT_ALLOWED;
    }

    //
    // Relationship Type specific
    //
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getAllowedSourceTypes()
     */
    public Collection<CMISTypeDefinition> getAllowedSourceTypes()
    {
        return Collections.emptyList();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#getAllowedTargetTypes()
     */
    public Collection<CMISTypeDefinition> getAllowedTargetTypes()
    {
        return Collections.emptyList();
    }

}
