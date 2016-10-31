/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.opencmis.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.ExtendedDictionaryListener;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
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
        ExtendedDictionaryListener
{
    // Logger
    protected static final Log logger = LogFactory.getLog(CMISAbstractDictionaryService.class);

    // service dependencies
    protected DictionaryDAO dictionaryDAO;
    protected DictionaryService dictionaryService;
    protected CMISMapping cmisMapping;
    protected PropertyAccessorMapping accessorMapping;
    protected PropertyLuceneBuilderMapping luceneBuilderMapping;
    protected TenantService tenantService;

    private final ReentrantReadWriteLock registryLock = new ReentrantReadWriteLock();
    private final WriteLock registryWriteLock = registryLock.writeLock();
    private final ReadLock registryReadLock = registryLock.readLock();

    // note: cache is tenant-aware (if using TransctionalCache impl)
    private SimpleCache<String, CMISDictionaryRegistry> cmisRegistryCache;
    private final String KEY_OPENCMIS_DICTIONARY_REGISTRY = "key.openCmisDictionaryRegistry";

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Set the mapping service
     * 
     * @param cmisMapping
     *            CMISMapping
     */
    public void setCmisMapping(CMISMapping cmisMapping)
    {
        this.cmisMapping = cmisMapping;
    }

    /**
     * Set the property accessor mapping service
     * 
     * @param accessorMapping
     *            mapping
     */
    public void setPropertyAccessorMapping(PropertyAccessorMapping accessorMapping)
    {
        this.accessorMapping = accessorMapping;
    }

    /**
     * Set the property lucene mapping service
     * 
     * @param luceneBuilderMapping
     *            mapping
     */
    public void setPropertyLuceneBuilderMapping(PropertyLuceneBuilderMapping luceneBuilderMapping)
    {
        this.luceneBuilderMapping = luceneBuilderMapping;
    }

    /**
     * Set the dictionary Service
     * 
     * @param dictionaryService
     *            DictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the dictionary DAO
     * 
     * @param dictionaryDAO
     *            DictionaryDAO
     */
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }

    public void setSingletonCache(SimpleCache<String, CMISDictionaryRegistry> singletonCache)
    {
        this.cmisRegistryCache = singletonCache;
    }

    protected interface DictionaryInitializer
    {
        Collection<AbstractTypeDefinitionWrapper> createDefinitions(CMISDictionaryRegistry cmisRegistry);

        Collection<AbstractTypeDefinitionWrapper> createDefinitions(CMISDictionaryRegistry cmisRegistry,
                CompiledModel model);
    }

    protected abstract DictionaryInitializer getCoreDictionaryInitializer();

    protected abstract DictionaryInitializer getTenantDictionaryInitializer();

    protected CMISDictionaryRegistry getRegistry()
    {
        String tenant = TenantUtil.getCurrentDomain();
        return getRegistry(tenant);
    }

    CMISDictionaryRegistry getRegistry(String tenant)
    {
        CMISDictionaryRegistry cmisRegistry = null;

        String cacheKey = getCacheKey(tenant);

        registryReadLock.lock();
        try
        {
            cmisRegistry = cmisRegistryCache.get(cacheKey);
        }
        finally
        {
            registryReadLock.unlock();
        }

        if (cmisRegistry == null)
        {
            cmisRegistry = createDictionaryRegistry(tenant);

            registryWriteLock.lock();
            try
            {
                cmisRegistryCache.put(cacheKey, cmisRegistry);
            }
            finally
            {
                registryWriteLock.unlock();
            }
        }

        return cmisRegistry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#
     * onBootstrap (org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        afterDictionaryInit();

        // TODO revisit (for KS and/or 1.1)
        if (dictionaryDAO != null)
        {
            dictionaryDAO.registerListener(this);
        }
        else
        {
            logger.error("DictionaryDAO is null - hence CMIS Dictionary not registered for updates");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown
     * (org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }

    private String getCacheKey()
    {
        String tenant = tenantService.getCurrentUserDomain();
        return getCacheKey(tenant);
    }

    private String getCacheKey(String tenant)
    {
        String cacheKey = KEY_OPENCMIS_DICTIONARY_REGISTRY + "." + tenant + "."
                + cmisMapping.getCmisVersion().toString();
        return cacheKey;
    }

    protected CMISDictionaryRegistry createCoreDictionaryRegistry()
    {
        CMISDictionaryRegistryImpl cmisRegistry = new CMISDictionaryRegistryImpl(this, cmisMapping, dictionaryService,
                getCoreDictionaryInitializer());
        cmisRegistry.init();
        return cmisRegistry;
    }

    protected CMISDictionaryRegistry createTenantDictionaryRegistry(String tenant)
    {
        CMISDictionaryRegistryImpl cmisRegistry = new CMISDictionaryRegistryImpl(this, tenant, "", cmisMapping,
                dictionaryService, getTenantDictionaryInitializer());
        cmisRegistry.init();
        return cmisRegistry;
    }

    protected CMISDictionaryRegistry createDictionaryRegistryWithWriteLock()
    {
        String tenant = TenantUtil.getCurrentDomain();
        CMISDictionaryRegistry cmisRegistry = createDictionaryRegistry(tenant);
        String cacheKey = getCacheKey(tenant);

        registryWriteLock.lock();
        try
        {
            // publish new registry
            cmisRegistryCache.put(cacheKey, cmisRegistry);

        }
        finally
        {
            registryWriteLock.unlock();
        }

        return cmisRegistry;
    }

    protected CMISDictionaryRegistry createDictionaryRegistry(String tenant)
    {
        CMISDictionaryRegistry cmisRegistry = null;

        if (tenant.equals(TenantService.DEFAULT_DOMAIN))
        {
            cmisRegistry = createCoreDictionaryRegistry();
        }
        else
        {
            cmisRegistry = createTenantDictionaryRegistry(tenant);
        }

        return cmisRegistry;
    }

    @Override
    public TypeDefinitionWrapper findType(String typeId)
    {
        TypeDefinitionWrapper typeDef = getRegistry().getTypeDefByTypeId(typeId);
        if (typeDef != null && typeDef.getTypeDefinition(false).getDisplayName() == null)
        {
            typeDef.updateDefinition(dictionaryService);
        }
        return typeDef;
    }

    @Override
    public boolean isExcluded(QName qname)
    {
        return cmisMapping.isExcluded(qname);
    }

    @Override
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
            typeDef = getRegistry().getAssocDefByQName(clazz);
        }
        else
        {
            typeDef = getRegistry().getTypeDefByQName(clazz);
            if (typeDef == null)
            {
                typeDef = getRegistry().getAssocDefByQName(clazz);
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

    @Override
    public TypeDefinitionWrapper findNodeType(QName clazz)
    {
        return getRegistry().getTypeDefByQName(cmisMapping.getCmisType(clazz));
    }

    @Override
    public TypeDefinitionWrapper findAssocType(QName clazz)
    {
        return getRegistry().getAssocDefByQName(cmisMapping.getCmisType(clazz));
    }

    @Override
    public TypeDefinitionWrapper findTypeByQueryName(String queryName)
    {
        // ISO 9075 name look up should be lower case.
        return getRegistry().getTypeDefByQueryName(ISO9075.lowerCaseEncodedSQL(queryName));
    }

    @Override
    public PropertyDefinitionWrapper findProperty(String propId)
    {
        return getRegistry().getPropDefByPropId(propId);
    }

    @Override
    public PropertyDefinitionWrapper findPropertyByQueryName(String queryName)
    {
        return getRegistry().getPropDefByQueryName(ISO9075.lowerCaseEncodedSQL(queryName));
    }

    @Override
    public List<TypeDefinitionWrapper> getBaseTypes()
    {
        return getBaseTypes(true);
    }

    @Override
    public List<TypeDefinitionWrapper> getBaseTypes(boolean includeParent)
    {
        List<TypeDefinitionWrapper> types = getRegistry().getBaseTypes(includeParent);

        for (TypeDefinitionWrapper typeDef : types)
        {
            if (typeDef != null && typeDef.getTypeDefinition(false).getDisplayName() == null)
            {
                typeDef.updateDefinition(dictionaryService);
            }
        }

        return Collections.unmodifiableList(types);
    }

    @Override
    public List<TypeDefinitionWrapper> getAllTypes()
    {
        // TODO is there a way of not having to reconstruct this every time?
        return Collections.unmodifiableList(new ArrayList<TypeDefinitionWrapper>(getRegistry().getTypeDefs()));
    }

    @Override
    public List<TypeDefinitionWrapper> getAllTypes(boolean includeParent)
    {
        // TODO is there a way of not having to reconstruct this every time?
        return Collections
                .unmodifiableList(new ArrayList<TypeDefinitionWrapper>(getRegistry().getTypeDefs(includeParent)));
    }

    @Override
    public PropertyType findDataType(QName dataType)
    {
        return cmisMapping.getDataType(dataType);
    }

    @Override
    public QName findAlfrescoDataType(PropertyType propertyType)
    {
        return cmisMapping.getAlfrescoDataType(propertyType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.dictionary.DictionaryListener#onInit()
     */
    @Override
    public void onDictionaryInit()
    {
    }

    @Override
    public void modelAdded(CompiledModel model, String tenantDomain)
    {
        getRegistry(tenantDomain).addModel(model);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.dictionary.DictionaryListener#afterInit()
     */
    @Override
    public void afterDictionaryInit()
    {
        createDictionaryRegistryWithWriteLock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.dictionary.DictionaryListener#afterDictionaryDestroy()
     */
    @Override
    public void afterDictionaryDestroy()
    {
        registryWriteLock.lock();
        try
        {
            String cacheKey = getCacheKey();
            cmisRegistryCache.remove(cacheKey);
        }
        finally
        {
            registryWriteLock.unlock();
        }
    }

    @Override
    public List<TypeDefinitionWrapper> getChildren(String typeId)
    {
        List<TypeDefinitionWrapper> children = getRegistry().getChildren(typeId);

        for (TypeDefinitionWrapper child : children)
        {
            if (child != null && child.getTypeDefinition(false).getDisplayName() == null)
            {
                child.updateDefinition(dictionaryService);
            }
        }

        return children;
    }
}
