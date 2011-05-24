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
package org.alfresco.opencmis.dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryListener;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Common CMIS Dictionary Support including registry of Types.
 * 
 * @author davidc
 * @author florian.mueller
 */
public abstract class CMISAbstractDictionaryService extends AbstractLifecycleBean implements CMISDictionaryService,
        DictionaryListener
{
    // Logger
    protected static final Log logger = LogFactory.getLog(CMISAbstractDictionaryService.class);

    // service dependencies
    private DictionaryDAO dictionaryDAO;
    protected CMISMapping cmisMapping;
    protected DictionaryService dictionaryService;
    protected TenantService tenantService;
    protected ServiceRegistry serviceRegistry;

    /**
     * Set the mapping service
     * 
     * @param cmisMapping
     */
    public void setOpenCMISMapping(CMISMapping cmisMapping)
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

    /**
     * Set the service registry
     * 
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /** CMIS Dictionary Registry (tenant-aware) */
    private Map<String, DictionaryRegistry> registryMap = new ConcurrentHashMap<String, DictionaryRegistry>(4);

    /**
     * CMIS Dictionary registry
     * 
     * Index of CMIS Type Definitions
     */
    /* package */class DictionaryRegistry
    {
        // Type Definitions Index
        Map<QName, TypeDefinitionWrapper> typeDefsByQName = new HashMap<QName, TypeDefinitionWrapper>();
        Map<QName, TypeDefinitionWrapper> assocDefsByQName = new HashMap<QName, TypeDefinitionWrapper>();

        Map<String, AbstractTypeDefinitionWrapper> typeDefsByTypeId = new HashMap<String, AbstractTypeDefinitionWrapper>();
        Map<String, TypeDefinitionWrapper> typeDefsByQueryName = new HashMap<String, TypeDefinitionWrapper>();
        List<TypeDefinitionWrapper> baseTypes = new ArrayList<TypeDefinitionWrapper>();

        Map<String, PropertyDefintionWrapper> propDefbyPropId = new HashMap<String, PropertyDefintionWrapper>();
        Map<String, PropertyDefintionWrapper> propDefbyQueryName = new HashMap<String, PropertyDefintionWrapper>();

        /**
         * Register type definition.
         * 
         * @param typeDef
         */
        public void registerTypeDefinition(AbstractTypeDefinitionWrapper typeDef)
        {
            AbstractTypeDefinitionWrapper existingTypeDef = typeDefsByTypeId.get(typeDef.getTypeId());
            if (existingTypeDef != null)
            {
                throw new AlfrescoRuntimeException("Type " + typeDef.getTypeId() + " already registered");
            }

            typeDefsByTypeId.put(typeDef.getTypeId(), typeDef);
            QName typeQName = typeDef.getAlfrescoName();
            if (typeQName != null)
            {
                if ((typeDef instanceof RelationshipTypeDefintionWrapper) && !typeDef.isBaseType())
                {
                    assocDefsByQName.put(typeQName, typeDef);
                } else
                {
                    typeDefsByQName.put(typeQName, typeDef);
                }
            }

            typeDefsByQueryName.put(typeDef.getTypeDefinition(false).getQueryName(), typeDef);

            if (logger.isDebugEnabled())
            {
                logger.debug("Registered type " + typeDef.getTypeId() + " (scope=" + typeDef.getBaseTypeId() + ")");
                logger.debug(" QName: " + typeDef.getAlfrescoName());
                logger.debug(" Table: " + typeDef.getTypeDefinition(false).getQueryName());
                logger.debug(" Action Evaluators: " + typeDef.getActionEvaluators().size());
            }
        }

        /**
         * Register property definitions.
         * 
         * @param typeDef
         */
        public void registerPropertyDefinitions(AbstractTypeDefinitionWrapper typeDef)
        {
            for (PropertyDefintionWrapper propDef : typeDef.getProperties())
            {
                if (propDef.getPropertyDefinition().isInherited())
                {
                    continue;
                }

                propDefbyPropId.put(propDef.getPropertyId(), propDef);
                propDefbyQueryName.put(propDef.getPropertyDefinition().getQueryName(), propDef);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("DictionaryRegistry[");
            builder.append("Types=").append(typeDefsByTypeId.size()).append(", ");
            builder.append("Base Types=").append(baseTypes.size()).append(", ");
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

    public TypeDefinitionWrapper findType(String typeId)
    {
        return getRegistry().typeDefsByTypeId.get(typeId);
    }

    public TypeDefinitionWrapper findTypeForClass(QName clazz, BaseTypeId... matchingScopes)
    {
        // searching for relationship
        boolean scopeByRelationship = false;
        for (BaseTypeId scope : matchingScopes)
        {
            if (scope == BaseTypeId.CMIS_RELATIONSHIP)
            {
                scopeByRelationship = true;
                break;
            }
        }

        // locate type in registry
        clazz = cmisMapping.getCmisType(clazz);
        TypeDefinitionWrapper typeDef = null;
        if (scopeByRelationship)
        {
            typeDef = getRegistry().assocDefsByQName.get(clazz);
        } else
        {
            typeDef = getRegistry().typeDefsByQName.get(clazz);
            if (typeDef == null)
            {
                typeDef = getRegistry().assocDefsByQName.get(clazz);
            }
        }

        // ensure matches one of provided matching scopes
        TypeDefinitionWrapper matchingTypeDef = (matchingScopes.length == 0) ? typeDef : null;
        if (typeDef != null)
        {
            for (BaseTypeId scope : matchingScopes)
            {
                if (typeDef.getBaseTypeId() == scope)
                {
                    matchingTypeDef = typeDef;
                    break;
                }
            }
        }

        return matchingTypeDef;
    }

    public TypeDefinitionWrapper findNodeType(QName clazz)
    {
        return getRegistry().typeDefsByQName.get(cmisMapping.getCmisType(clazz));
    }

    public TypeDefinitionWrapper findAssocType(QName clazz)
    {
        return getRegistry().assocDefsByQName.get(cmisMapping.getCmisType(clazz));
    }

    public TypeDefinitionWrapper findTypeByQueryName(String queryName)
    {
        return getRegistry().typeDefsByQueryName.get(queryName);
    }

    public QName getAlfrescoClass(QName name)
    {
        return cmisMapping.getAlfrescoClass(name);
    }

    public PropertyDefintionWrapper findProperty(String propId)
    {
        return getRegistry().propDefbyPropId.get(propId);
    }

    @Override
    public PropertyDefintionWrapper findPropertyByQueryName(String queryName)
    {
        return getRegistry().propDefbyQueryName.get(queryName);
    }

    public List<TypeDefinitionWrapper> getBaseTypes()
    {
        return Collections.unmodifiableList(getRegistry().baseTypes);
    }

    public List<TypeDefinitionWrapper> getAllTypes()
    {
        return Collections.unmodifiableList(new ArrayList<TypeDefinitionWrapper>(getRegistry().typeDefsByTypeId
                .values()));
    }

    public PropertyType findDataType(QName dataType)
    {
        return cmisMapping.getDataType(dataType);
    }

    public QName findAlfrescoDataType(PropertyType propertyType)
    {
        return cmisMapping.getAlfrescoDataType(propertyType);
    }

    /**
     * Factory for creating CMIS Definitions
     * 
     * @param registry
     */
    abstract protected void createDefinitions(DictionaryRegistry registry);

    /**
     * Dictionary Initialization - creates a new registry
     */
    private void init()
    {
        DictionaryRegistry registry = new DictionaryRegistry();

        if (logger.isDebugEnabled())
            logger.debug("Creating type definitions...");

        // phase 1: construct type definitions and link them together
        createDefinitions(registry);
        for (AbstractTypeDefinitionWrapper objectTypeDef : registry.typeDefsByTypeId.values())
        {
            objectTypeDef.connectParentAndSubTypes(cmisMapping, registry, dictionaryService);
        }

        // phase 2: register base types and inherit property definitions
        for (AbstractTypeDefinitionWrapper typeDef : registry.typeDefsByTypeId.values())
        {
            if (typeDef.getTypeDefinition(false).getParentTypeId() == null)
            {
                registry.baseTypes.add(typeDef);
                typeDef.resolveInheritance(cmisMapping, serviceRegistry, registry, dictionaryService);
            }
        }

        // phase 3: register properties
        for (AbstractTypeDefinitionWrapper typeDef : registry.typeDefsByTypeId.values())
        {
            registry.registerPropertyDefinitions(typeDef);
        }

        // phase 4: assert valid
        for (AbstractTypeDefinitionWrapper typeDef : registry.typeDefsByTypeId.values())
        {
            typeDef.assertComplete();
        }

        // publish new registry
        registryMap.put(tenantService.getCurrentUserDomain(), registry);

        if (logger.isInfoEnabled())
            logger.info("Initialized CMIS Dictionary. Types:" + registry.typeDefsByTypeId.size() + ", Base Types:"
                    + registry.baseTypes.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.dictionary.DictionaryListener#onInit()
     */
    public void onDictionaryInit()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.dictionary.DictionaryListener#afterInit()
     */
    public void afterDictionaryInit()
    {
        init();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.dictionary.DictionaryListener#afterDictionaryDestroy()
     */
    public void afterDictionaryDestroy()
    {
        registryMap.remove(tenantService.getCurrentUserDomain());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap
     * (org.springframework.context.ApplicationEvent)
     */
    protected void onBootstrap(ApplicationEvent event)
    {
        afterDictionaryInit();
        dictionaryDAO.register(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown
     * (org.springframework.context.ApplicationEvent)
     */
    protected void onShutdown(ApplicationEvent event)
    {
    }

}
