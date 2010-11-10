/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
public abstract class CMISAbstractTypeDefinition implements CMISTypeDefinition, Serializable
{
    // Logger
    protected static final Log logger = LogFactory.getLog(CMISAbstractTypeDefinition.class);

    private static final long serialVersionUID = -3131505923356013430L;

    // Object type properties
    protected Boolean isPublic = null;
    protected ClassDefinition cmisClassDef = null;
    protected CMISTypeId objectTypeId = null;
    protected String objectTypeQueryName = null;
    protected String displayName = null;
    protected CMISTypeId parentTypeId = null;
    protected CMISTypeDefinition parentType = null;
    protected CMISAbstractTypeDefinition internalParentType = null;
    protected CMISTypeDefinition rootType = null;
    protected String description = null;
    protected Boolean creatable = null;
    protected Boolean queryable = null;
    protected Boolean fullTextIndexed = null;
    protected Boolean controllablePolicy = null;
    protected Boolean controllableACL = null;
    protected Boolean includedInSuperTypeQuery = null;
    protected Collection<CMISTypeId> subTypeIds = null;
    protected Collection<CMISTypeDefinition> subTypes = null;
    protected Map<String, CMISPropertyDefinition> properties = null;
    protected Map<String, CMISPropertyDefinition> inheritedProperties = null;
    protected Map<String, CMISPropertyDefinition> ownedProperties = null;
    protected Map<CMISAllowedActionEnum, CMISActionEvaluator<? extends Object>> actionEvaluators = null;
    

    /*package*/ void assertComplete()
    {
        if (objectTypeId == null) throw new IllegalStateException("objectTypeId not specified");
        if (isPublic == null) throw new IllegalStateException("isPublic not specified; objectTypeId=" + objectTypeId);
        //if (cmisClassDef == null) throw new IllegalStateException("cmisClassDef not specified; objectTypeId=" + objectTypeId);
        if (objectTypeQueryName == null) throw new IllegalStateException("objectTypeQueryName not specified; objectTypeId=" + objectTypeId);
        if (displayName == null) throw new IllegalStateException("displayName not specified; objectTypeId=" + objectTypeId);
        //if (parentTypeId == null) throw new IllegalStateException("parentTypeId not specified; objectTypeId=" + objectTypeId);
        if (parentTypeId != null && internalParentType == null) throw new IllegalStateException("parentType not specified; objectTypeId=" + objectTypeId + ",parentTypeId=" + parentTypeId);
        if (rootType == null) throw new IllegalStateException("rootType not specified; objectTypeId=" + objectTypeId);
        if (description == null) throw new IllegalStateException("description not specified; objectTypeId=" + objectTypeId);
        if (creatable == null) throw new IllegalStateException("creatable not specified; objectTypeId=" + objectTypeId);
        if (queryable == null) throw new IllegalStateException("queryable not specified; objectTypeId=" + objectTypeId);
        if (fullTextIndexed == null) throw new IllegalStateException("fullTextIndexed not specified; objectTypeId=" + objectTypeId);
        if (controllablePolicy == null) throw new IllegalStateException("controllablePolicy not specified; objectTypeId=" + objectTypeId);
        if (controllableACL == null) throw new IllegalStateException("controllablePolicy not specified; objectTypeId=" + objectTypeId);
        if (includedInSuperTypeQuery == null) throw new IllegalStateException("includedInSuperTypeQuery not specified; objectTypeId=" + objectTypeId);
        if (subTypeIds == null) throw new IllegalStateException("subTypeIds not specified; objectTypeId=" + objectTypeId);
        if (subTypes == null) throw new IllegalStateException("subTypes not specified; objectTypeId=" + objectTypeId);
        if (properties == null) throw new IllegalStateException("properties not specified; objectTypeId=" + objectTypeId);
        if (inheritedProperties == null) throw new IllegalStateException("inheritedProperties not specified; objectTypeId=" + objectTypeId);
        if (ownedProperties == null) throw new IllegalStateException("inheritedProperties not specified; objectTypeId=" + objectTypeId);
        if (actionEvaluators == null) throw new IllegalStateException("actionEvaluators not specified; objectTypeId=" + objectTypeId);
    }
    
    
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
        properties = new HashMap<String, CMISPropertyDefinition>();
        for (PropertyDefinition propDef : cmisClassDef.getProperties().values())
        {
            if (propDef.getContainerClass().equals(cmisClassDef) && !propDef.isOverride())
            {
                if (cmisMapping.getDataType(propDef.getDataType()) != null)
                {
                    CMISPropertyDefinition cmisPropDef = createProperty(cmisMapping, propDef);
                    properties.put(cmisPropDef.getPropertyId().getId(), cmisPropDef);
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
        String propertyId = cmisMapping.getCmisPropertyId(propertyQName);
        CMISPropertyId cmisPropertyId = new CMISPropertyId(propertyQName, propertyId);
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
        subTypeIds = new ArrayList<CMISTypeId>();
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
        
        CMISTypeId rootTypeId = objectTypeId.getBaseTypeId();
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
        
        subTypes = new ArrayList<CMISTypeDefinition>();
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
        inheritedProperties = new HashMap<String, CMISPropertyDefinition>();
        ownedProperties = new HashMap<String, CMISPropertyDefinition>();
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
    public CMISTypeDefinition getBaseType()
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
     * @see org.alfresco.cmis.CMISTypeDefinition#isFullTextIndexed()
     */
    public boolean isFullTextIndexed()
    {
        return fullTextIndexed;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#isControllable()
     */
    public boolean isControllablePolicy()
    {
        return controllablePolicy;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISTypeDefinition#isControllableACL()
     */
    public boolean isControllableACL()
    {
        return controllableACL;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISTypeDefinition#isIncludedInSuperTypeQuery()
     */
    public boolean isIncludedInSuperTypeQuery()
    {
        return includedInSuperTypeQuery;
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
    public Map<CMISAllowedActionEnum, CMISActionEvaluator<? extends Object>> getActionEvaluators()
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

    @Override
    public int hashCode()
    {
        return objectTypeId.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final CMISTypeDefinition other = (CMISTypeDefinition) obj;
        if (objectTypeId == null)
        {
            if (other.getTypeId() != null)
                return false;
        }
        else if (!objectTypeId.equals(other.getTypeId()))
            return false;
        return true;
    }
    
}
