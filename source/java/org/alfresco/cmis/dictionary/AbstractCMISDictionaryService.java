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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryListener;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;


/**
 * Common CMIS Dictionary Support including registry of Types.
 * 
 * @author davidc
 */
public abstract class AbstractCMISDictionaryService extends AbstractLifecycleBean implements CMISDictionaryService, DictionaryListener
{
    // Logger
    protected static final Log logger = LogFactory.getLog(AbstractCMISDictionaryService.class);

    // service dependencies
    private DictionaryDAO dictionaryDAO;
    protected CMISMapping cmisMapping;
    protected DictionaryService dictionaryService;

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


    // TODO: Handle tenants
    // TODO: read / write locks
    private DictionaryRegistry registry;

    
    /**
     * CMIS Dictionary registry
     *
     * Index of CMIS Type Definitions
     */
    /*package*/ class DictionaryRegistry
    {
        // Type Definitions Index
        Map<QName, CMISObjectTypeDefinition> typeDefsByQName = new HashMap<QName, CMISObjectTypeDefinition>();
        Map<QName, CMISObjectTypeDefinition> assocDefsByQName = new HashMap<QName, CMISObjectTypeDefinition>();
        Map<CMISTypeId, CMISObjectTypeDefinition> objectDefsByTypeId = new HashMap<CMISTypeId, CMISObjectTypeDefinition>();
        Map<CMISTypeId, CMISTypeDefinition> typeDefsByTypeId = new HashMap<CMISTypeId, CMISTypeDefinition>();
        Map<String, CMISTypeDefinition> typeDefsByTable = new HashMap<String, CMISTypeDefinition>();

        // Property Definitions Index
        Map<String, CMISPropertyDefinition> propDefsByName = new HashMap<String, CMISPropertyDefinition>();
        Map<QName, CMISPropertyDefinition> propDefsByQName = new HashMap<QName, CMISPropertyDefinition>();
        Map<CMISPropertyId, CMISPropertyDefinition> propDefsByPropId = new HashMap<CMISPropertyId, CMISPropertyDefinition>();

        /**
         * Register Type Definition
         * 
         * @param typeDefinition
         */
        public void registerTypeDefinition(CMISObjectTypeDefinition typeDefinition)
        {
            QName typeQName = typeDefinition.getTypeId().getQName();
            if (typeQName != null)
            {
                if (typeDefinition instanceof CMISRelationshipTypeDefinition)
                {
                    assocDefsByQName.put(typeQName, typeDefinition);
                }
                else
                {
                    typeDefsByQName.put(typeQName, typeDefinition);
                }
            }
            objectDefsByTypeId.put(typeDefinition.getTypeId(), typeDefinition);
            typeDefsByTypeId.put(typeDefinition.getTypeId(), typeDefinition);
            typeDefsByTable.put(typeDefinition.getQueryName().toLowerCase(), typeDefinition);
        }

        /**
         * Registry Property Definition
         * 
         * @param propDef
         */
        public void registerPropertyDefinition(CMISPropertyDefinition propDef)
        {
            propDefsByPropId.put(propDef.getPropertyId(), propDef);
            propDefsByQName.put(propDef.getPropertyId().getQName(), propDef);
            propDefsByName.put(propDef.getPropertyId().getName().toLowerCase(), propDef);
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
            builder.append("Properties=").append(propDefsByPropId.size());
            builder.append("]");
            return builder.toString();
        }
    }


    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getTypeId(java.lang.String)
     */
    public CMISTypeId getTypeId(String typeId)
    {
        return cmisMapping.getCmisTypeId(typeId);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getTypeId(org.alfresco.service.namespace.QName, org.alfresco.cmis.dictionary.CMISScope)
     */
    public CMISTypeId getTypeId(QName clazz, CMISScope matchingScope)
    {
        CMISTypeDefinition typeDef = null;
        if (matchingScope != null && matchingScope == CMISScope.RELATIONSHIP)
        {
            typeDef = registry.assocDefsByQName.get(clazz);
        }
        else
        {
            typeDef = registry.typeDefsByQName.get(clazz);
        }

        CMISTypeDefinition matchingTypeDef = null;
        if (matchingScope == null)
        {
            matchingTypeDef = typeDef;
        }
        else
        {
            if (typeDef != null && typeDef.getTypeId().getScope() == matchingScope)
            {
                matchingTypeDef = typeDef;
            }
        }
        return matchingTypeDef == null ? null : matchingTypeDef.getTypeId();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getTypeIdFromTable(java.lang.String)
     */
    public CMISTypeId getTypeIdFromTable(String table)
    {
        CMISTypeDefinition typeDef = registry.typeDefsByTable.get(table.toLowerCase());
        return (typeDef == null) ? null : typeDef.getTypeId();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getType(org.alfresco.cmis.dictionary.CMISTypeId)
     */
    public CMISTypeDefinition getType(CMISTypeId typeId)
    {
        return registry.objectDefsByTypeId.get(typeId);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getAllTypes()
     */
    public Collection<CMISTypeDefinition> getAllTypes()
    {
        return Collections.unmodifiableCollection(registry.typeDefsByTypeId.values());
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getProperty(org.alfresco.cmis.dictionary.CMISPropertyId)
     */
    public CMISPropertyDefinition getProperty(CMISPropertyId propertyId)
    {
        return registry.propDefsByPropId.get(propertyId);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getPropertyId(java.lang.String)
     */
    public CMISPropertyId getPropertyId(String property)
    {
        CMISPropertyDefinition propDef = registry.propDefsByName.get(property.toLowerCase());
        return (propDef == null) ? null : propDef.getPropertyId();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getPropertyId(org.alfresco.service.namespace.QName)
     */
    public CMISPropertyId getPropertyId(QName property)
    {
        CMISPropertyDefinition propDef = registry.propDefsByQName.get(property);
        return (propDef == null) ? null : propDef.getPropertyId();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.dictionary.CMISDictionaryService#getDataType(org.alfresco.service.namespace.QName)
     */
    public CMISDataTypeEnum getDataType(QName dataType)
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

        // phase 1: construct type definitions
        createDefinitions(registry);
        for (CMISObjectTypeDefinition objectTypeDef : registry.objectDefsByTypeId.values())
        {
            Map<CMISPropertyId, CMISPropertyDefinition> propDefs = objectTypeDef.createProperties(cmisMapping, dictionaryService);
            for (CMISPropertyDefinition propDef : propDefs.values())
            {
                registry.registerPropertyDefinition(propDef);
            }
            objectTypeDef.createSubTypes(cmisMapping, dictionaryService);
        }

        // phase 2: link together
        for (CMISObjectTypeDefinition objectTypeDef : registry.objectDefsByTypeId.values())
        {
            objectTypeDef.resolveDependencies(registry);
        }
        
        // phase 3: resolve inheritance
        Map<Integer,List<CMISObjectTypeDefinition>> order = new TreeMap<Integer, List<CMISObjectTypeDefinition>>();
        for (CMISObjectTypeDefinition typeDef : registry.objectDefsByTypeId.values())
        {
            // calculate class depth in hierarchy
            int depth = 0;
            CMISTypeDefinition parent = typeDef.getParentType();
            while (parent != null)
            {
                depth = depth +1;
                parent = parent.getParentType();
            }

            // map class to depth
            List<CMISObjectTypeDefinition> classes = order.get(depth);
            if (classes == null)
            {
                classes = new ArrayList<CMISObjectTypeDefinition>();
                order.put(depth, classes);
            }
            classes.add(typeDef);
        }
        for (int depth = 0; depth < order.size(); depth++)
        {
            for (CMISObjectTypeDefinition typeDef : order.get(depth))
            {
                typeDef.resolveInheritance(registry);
            }
        }

        // publish new registry
        this.registry = registry;
        
        if (logger.isDebugEnabled())
            logger.debug("Initialized CMIS Dictionary. Types:" + registry.typeDefsByTypeId.size() + ", Properties:" + registry.propDefsByPropId.size());
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
     * @see org.alfresco.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    protected void onBootstrap(ApplicationEvent event)
    {
        afterDictionaryInit();
        dictionaryDAO.register(this);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    protected void onShutdown(ApplicationEvent event)
    {
    }

}
