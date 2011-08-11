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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISPropertyId;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISTypeId;
import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryListener;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;


/**
 * Common CMIS Dictionary Support including registry of Types.
 * 
 * @author davidc
 */
public abstract class CMISAbstractDictionaryService extends AbstractLifecycleBean implements CMISDictionaryService, DictionaryListener
{
    // Logger
    protected static final Log logger = LogFactory.getLog(CMISAbstractDictionaryService.class);

    // service dependencies
    private DictionaryDAO dictionaryDAO;
    protected CMISMapping cmisMapping;
    protected DictionaryService dictionaryService;
    protected TenantService tenantService;

    /**
     * Set the mapping service
     * 
     * @param cmisMapping
     */
    public void setCMISMapping(CMISMapping cmisMapping)
    {
        this.cmisMapping = cmisMapping;
    }

    /**
     * Set the dictionary Service
     * 
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the dictionary DAO
     * 
     * @param dictionaryDAO
     */
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }
    
    /**
     * Set the tenant Service
     * 
     * @param tenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }


    /** CMIS Dictionary Registry (tenant-aware) */
    private Map<String, DictionaryRegistry> registryMap = new ConcurrentHashMap<String, DictionaryRegistry>(4);

    
    /**
     * CMIS Dictionary registry
     *
     * Index of CMIS Type Definitions
     */
    /*package*/ class DictionaryRegistry
    {
        // Type Definitions Index
        Map<QName, CMISAbstractTypeDefinition> typeDefsByQName = new HashMap<QName, CMISAbstractTypeDefinition>();
        Map<QName, CMISAbstractTypeDefinition> assocDefsByQName = new HashMap<QName, CMISAbstractTypeDefinition>();
        Map<CMISTypeId, CMISAbstractTypeDefinition> objectDefsByTypeId = new HashMap<CMISTypeId, CMISAbstractTypeDefinition>();
        Map<CMISTypeId, CMISTypeDefinition> typeDefsByTypeId = new HashMap<CMISTypeId, CMISTypeDefinition>();
        Map<String, CMISTypeDefinition> typeDefsByQueryName = new HashMap<String, CMISTypeDefinition>();
        List<CMISTypeDefinition> baseTypes = new ArrayList<CMISTypeDefinition>();

        // Property Definitions Index
        Map<String, CMISPropertyDefinition> propDefsById = new HashMap<String, CMISPropertyDefinition>();
        Map<QName, CMISPropertyDefinition> propDefsByQName = new HashMap<QName, CMISPropertyDefinition>();
        Map<CMISPropertyId, CMISPropertyDefinition> propDefsByPropId = new HashMap<CMISPropertyId, CMISPropertyDefinition>();
        Map<String, CMISPropertyDefinition> propDefsByQueryName = new HashMap<String, CMISPropertyDefinition>();

        /**
         * Register Type Definition
         * 
         * @param typeDef
         */
        public void registerTypeDefinition(CMISAbstractTypeDefinition typeDef)
        {
            CMISTypeDefinition existingTypeDef = objectDefsByTypeId.get(typeDef.getTypeId());
            if (existingTypeDef != null)
            {
                throw new AlfrescoRuntimeException("Type " + typeDef.getTypeId() + " already registered");
            }
            
            objectDefsByTypeId.put(typeDef.getTypeId(), typeDef);
            if (typeDef.isPublic())
            {
                QName typeQName = typeDef.getTypeId().getQName();
                if (typeQName != null)
                {
                    if (typeDef instanceof CMISRelationshipTypeDefinition)
                    {
                        assocDefsByQName.put(typeQName, typeDef);
                    }
                    else
                    {
                        typeDefsByQName.put(typeQName, typeDef);
                    }
                }
                typeDefsByTypeId.put(typeDef.getTypeId(), typeDef);
                typeDefsByQueryName.put(typeDef.getQueryName().toLowerCase(), typeDef);
            }
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Registered type " + typeDef.getTypeId() + " (scope=" + typeDef.getTypeId().getScope() + ", public=" + typeDef.isPublic() + ")");
                logger.debug(" QName: " + typeDef.getTypeId().getQName());
                logger.debug(" Table: " + typeDef.getQueryName());
                logger.debug(" Action Evaluators: " + typeDef.getActionEvaluators().size());
            }
        }

        /**
         * Registry Property Definition
         * 
         * @param propDef
         */
        public void registerPropertyDefinition(CMISPropertyDefinition propDef)
        {
            CMISPropertyDefinition existingPropDef = propDefsByPropId.get(propDef.getPropertyId());
            if (existingPropDef != null)
            {
                throw new AlfrescoRuntimeException("Property " + propDef.getPropertyId() + " of " + propDef.getOwningType().getTypeId() + " already registered by type " + existingPropDef.getOwningType().getTypeId());
            }
            
            propDefsByPropId.put(propDef.getPropertyId(), propDef);
            propDefsByQName.put(propDef.getPropertyId().getQName(), propDef);
            propDefsById.put(propDef.getPropertyId().getId().toLowerCase(), propDef);
            propDefsByQueryName.put(propDef.getQueryName().toLowerCase(), propDef);
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Registered property " + propDef.getPropertyId().getId());
                logger.debug(" QName: " + propDef.getPropertyId().getQName());
                logger.debug(" Id: " + propDef.getPropertyId().getId());
                logger.debug(" Owning Type: " + propDef.getOwningType().getTypeId());
                logger.debug(" Property Accessor: " + propDef.getPropertyAccessor() + " , mappedProperty=" + propDef.getPropertyAccessor().getMappedProperty());
            }
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("DictionaryRegistry[");
            builder.append("Types=").append(typeDefsByTypeId.size()).append(", ");
            builder.append("Base Types=").append(baseTypes.size()).append(", ");
            builder.append("Properties=").append(propDefsByPropId.size());
            builder.append("]");
            return builder.toString();
        }
    }
    
    private DictionaryRegistry getRegistry()
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        DictionaryRegistry registry = registryMap.get(tenantDomain);
        if (registry == null)
        {
            init();
            registry = registryMap.get(tenantDomain);
        }
        return registry;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#findType(org.alfresco.cmis.dictionary.CMISTypeId)
     */
    public CMISTypeDefinition findType(CMISTypeId typeId)
    {
        return getRegistry().objectDefsByTypeId.get(typeId);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#findType(java.lang.String)
     */
    public CMISTypeDefinition findType(String typeId)
    {
        CMISTypeId cmisTypeId = cmisMapping.getCmisTypeId(typeId);
        return findType(cmisTypeId);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#findTypeForClass(org.alfresco.service.namespace.QName, org.alfresco.cmis.dictionary.CMISScope[])
     */
    public CMISTypeDefinition findTypeForClass(QName clazz, CMISScope... matchingScopes)
    {
        // searching for relationship
        boolean scopeByRelationship = false;
        for (CMISScope scope : matchingScopes)
        {
            if (scope == CMISScope.RELATIONSHIP)
            {
                scopeByRelationship = true;
                break;
            }
        }
        
        // locate type in registry
        CMISTypeDefinition typeDef = null;
        if (scopeByRelationship)
        {
            typeDef = getRegistry().assocDefsByQName.get(clazz);
        }
        else
        {
            typeDef = getRegistry().typeDefsByQName.get(clazz);
            if (typeDef == null)
            {
                typeDef = getRegistry().assocDefsByQName.get(clazz);
            }
        }

        // ensure matches one of provided matching scopes
        CMISTypeDefinition matchingTypeDef = (matchingScopes.length == 0) ? typeDef : null;
        if (typeDef != null)
        {
            for (CMISScope scope : matchingScopes)
            {
                if (typeDef.getTypeId().getScope() == scope)
                {
                    matchingTypeDef = typeDef;
                    break;
                }
            }
        }
        
        return matchingTypeDef;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#findTypeForTable(java.lang.String)
     */
    public CMISTypeDefinition findTypeByQueryName(String queryName)
    {
        CMISTypeDefinition typeDef = getRegistry().typeDefsByQueryName.get(ISO9075.encodeSQL(queryName.toLowerCase()));
        return typeDef;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISDictionaryService#findPropertyByQueryName(java.lang.String)
     */
    public CMISPropertyDefinition findPropertyByQueryName(String queryName)
    {
       CMISPropertyDefinition propertyDef = getRegistry().propDefsByQueryName.get(ISO9075.encodeSQL(queryName.toLowerCase()));
        return propertyDef;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISDictionaryService#getBaseTypes()
     */
    public Collection<CMISTypeDefinition> getBaseTypes()
    {
        return Collections.unmodifiableCollection(getRegistry().baseTypes);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getAllTypes()
     */
    public Collection<CMISTypeDefinition> getAllTypes()
    {
        return Collections.unmodifiableCollection(getRegistry().typeDefsByTypeId.values());
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getProperty(org.alfresco.service.namespace.QName, org.alfresco.cmis.dictionary.CMISTypeDefinition)
     */
    public CMISPropertyDefinition findProperty(QName property, CMISTypeDefinition matchingType)
    {
        CMISPropertyDefinition propDef = getRegistry().propDefsByQName.get(property);
        return getProperty(propDef, matchingType);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getProperty(java.lang.String, org.alfresco.cmis.dictionary.CMISTypeDefinition)
     */
    public CMISPropertyDefinition findProperty(String property, CMISTypeDefinition matchingType)
    {
        CMISPropertyDefinition propDef = getRegistry().propDefsById.get(property.toLowerCase());
        return getProperty(propDef, matchingType);
    }
    
    /**
     * Return property definition if part of specified type definition
     * 
     * @param property
     * @param matchingType
     * @return  property definition (if matches), or null (if not matches)
     */
    private CMISPropertyDefinition getProperty(CMISPropertyDefinition property, CMISTypeDefinition matchingType)
    {
        boolean isMatchingType = (matchingType == null);
        if (property != null && matchingType != null)
        {
            Map<String, CMISPropertyDefinition> props = matchingType.getPropertyDefinitions();
            if (props.containsKey(property.getPropertyId().getId()))
            {
                isMatchingType = true;
            }
        }
        return isMatchingType ? property : null;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getDataType(org.alfresco.service.namespace.QName)
     */
    public CMISDataTypeEnum findDataType(QName dataType)
    {
        return cmisMapping.getDataType(dataType);
    }


    /**
     * Factory for creating CMIS Definitions
     * 
     * @param registry
     */
    abstract protected void createDefinitions(DictionaryRegistry registry);
    

    /**
     * Dictionary Initialisation - creates a new registry
     */
    private void init()
    {
        DictionaryRegistry registry = new DictionaryRegistry();

        if (logger.isDebugEnabled())
            logger.debug("Creating type definitions...");
        
        // phase 1: construct type definitions
        createDefinitions(registry);
        for (CMISAbstractTypeDefinition objectTypeDef : registry.objectDefsByTypeId.values())
        {
            Map<String, CMISPropertyDefinition> propDefs = objectTypeDef.createProperties(cmisMapping, dictionaryService);
            for (CMISPropertyDefinition propDef : propDefs.values())
            {
                registry.registerPropertyDefinition(propDef);
            }
            objectTypeDef.createSubTypes(cmisMapping, dictionaryService);
        }

        if (logger.isDebugEnabled())
            logger.debug("Linking type definitions...");

        // phase 2: link together
        for (CMISAbstractTypeDefinition objectTypeDef : registry.objectDefsByTypeId.values())
        {
            objectTypeDef.resolveDependencies(registry);
        }

        if (logger.isDebugEnabled())
            logger.debug("Resolving type inheritance...");

        // phase 3: resolve inheritance
        Map<Integer,List<CMISAbstractTypeDefinition>> order = new TreeMap<Integer, List<CMISAbstractTypeDefinition>>();
        for (CMISAbstractTypeDefinition typeDef : registry.objectDefsByTypeId.values())
        {
            // calculate class depth in hierarchy
            int depth = 0;
            CMISAbstractTypeDefinition parent = typeDef.getInternalParentType();
            while (parent != null)
            {
                depth = depth +1;
                parent = parent.getInternalParentType();
            }

            // map class to depth
            List<CMISAbstractTypeDefinition> classes = order.get(depth);
            if (classes == null)
            {
                classes = new ArrayList<CMISAbstractTypeDefinition>();
                order.put(depth, classes);
            }
            classes.add(typeDef);
        }
        for (int depth = 0; depth < order.size(); depth++)
        {
            for (CMISAbstractTypeDefinition typeDef : order.get(depth))
            {
                typeDef.resolveInheritance(registry);
            }
        }

        // phase 4: assert valid
        for (CMISAbstractTypeDefinition typeDef : registry.objectDefsByTypeId.values())
        {
            typeDef.assertComplete();
        }

        // phase 5: register base types
        for (CMISAbstractTypeDefinition typeDef : registry.objectDefsByTypeId.values())
        {
            if (typeDef.isPublic() && typeDef.getParentType() == null)
            {
                registry.baseTypes.add(typeDef);
            }
        }
        
        // publish new registry
        registryMap.put(tenantService.getCurrentUserDomain(), registry);
        
        if (logger.isInfoEnabled())
            logger.info("Initialized CMIS Dictionary. Types:" + registry.typeDefsByTypeId.size() + ", Base Types:" + registry.baseTypes.size() + ", Properties:" + registry.propDefsByPropId.size());
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryListener#onInit()
     */
    public void onDictionaryInit()
    {
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryListener#afterInit()
     */
    public void afterDictionaryInit()
    {
        init();
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryListener#afterDictionaryDestroy()
     */
    public void afterDictionaryDestroy()
    {
        registryMap.remove(tenantService.getCurrentUserDomain());
    }
    
    /*
     * (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    protected void onBootstrap(ApplicationEvent event)
    {
        afterDictionaryInit();
        dictionaryDAO.register(this);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    protected void onShutdown(ApplicationEvent event)
    {
    }

}
