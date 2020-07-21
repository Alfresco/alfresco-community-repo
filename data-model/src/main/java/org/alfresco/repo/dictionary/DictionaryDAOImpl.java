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
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Default implementation of the Dictionary.
 * 
 * @author David Caruana, janv, sglover
 * 
 */
// TODO deal with destroy of core dictionary registry i.e. do we remove all
// tenant dictionary registries too?
public class DictionaryDAOImpl implements DictionaryDAO, NamespaceDAO,
        ApplicationListener<ApplicationEvent>
{
    // Tenant Service
    private TenantService tenantService;

    // used to reset the cache
    private ThreadLocal<Map<String, DictionaryRegistry>> dictionaryRegistryThreadLocal = new ThreadLocal<Map<String, DictionaryRegistry>>();

    // Internal cache (clusterable)
    private CompiledModelsCache dictionaryRegistryCache;

    // Static list of registered dictionary listeners
    private List<DictionaryListener> dictionaryListeners = new ArrayList<DictionaryListener>();
    private ReadWriteLock dictionaryListenersLock = new ReentrantReadWriteLock();

    // Logger
    private static Log logger = LogFactory.getLog(DictionaryDAO.class);

    private String defaultAnalyserResourceBundleName;

    private ClassLoader resourceClassLoader;

    // inject dependencies

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setDictionaryRegistryCache(
            CompiledModelsCache dictionaryRegistryCache)
    {
        this.dictionaryRegistryCache = dictionaryRegistryCache;
    }

    @Override
    public String getDefaultAnalyserResourceBundleName()
    {
        return defaultAnalyserResourceBundleName;
    }

    public void setDefaultAnalyserResourceBundleName(
            String defaultAnalyserResourceBundleName)
    {
        this.defaultAnalyserResourceBundleName = defaultAnalyserResourceBundleName;
    }

    /**
     * Construct
     * 
     */
    public DictionaryDAOImpl()
    {
    }

    /**
     * Register listener with the Dictionary
     * <p>
     *     This method is <b>deprecated</b>, use {@link #registerListener(DictionaryListener dictionaryListener)} instead.
     * </p>
     * @param dictionaryListener
     */
    @Override
    @Deprecated
    public void register(DictionaryListener dictionaryListener)
    {
        registerListener(dictionaryListener);
    }

    /**
     * Register with the Dictionary
     */
    @Override
    public void registerListener(DictionaryListener dictionaryListener)
    {
        this.dictionaryListenersLock.writeLock().lock();
        try
        {
            if (!dictionaryListeners.contains(dictionaryListener))
            {
                dictionaryListeners.add(dictionaryListener);
            }
        }
        finally
        {
            this.dictionaryListenersLock.writeLock().unlock();
        }
    }

    @Override
    public List<DictionaryListener> getDictionaryListeners()
    {
        // need to hold read lock here
        this.dictionaryListenersLock.readLock().lock();
        try
        {
            return new ArrayList<DictionaryListener>(dictionaryListeners);
        }
        finally
        {
            this.dictionaryListenersLock.readLock().unlock();
        }
    }

    private Map<String, DictionaryRegistry> getThreadLocal()
    {
        Map<String, DictionaryRegistry> map = dictionaryRegistryThreadLocal
                .get();
        if (map == null)
        {
            map = new HashMap<String, DictionaryRegistry>();
            dictionaryRegistryThreadLocal.set(map);
        }
        return map;
    }

    private DictionaryRegistry createCoreDictionaryRegistry()
    {
        DictionaryRegistry dictionaryRegistry = new CoreDictionaryRegistryImpl(
                this);
        return dictionaryRegistry;
    }

    private DictionaryRegistry createTenantDictionaryRegistry(
            final String tenant)
    {
        DictionaryRegistry result = AuthenticationUtil.runAs(
                new RunAsWork<DictionaryRegistry>()
                {
                    public DictionaryRegistry doWork()
                    {
                        DictionaryRegistry dictionaryRegistry = new TenantDictionaryRegistryImpl(
                                DictionaryDAOImpl.this, tenant);
                        return dictionaryRegistry;
                    }
                }, tenantService.getDomainUser(
                        AuthenticationUtil.getSystemUserName(), tenant));

        return result;
    }

    @Override
    public void init()
    {
        String tenant = tenantService.getCurrentUserDomain();

        dictionaryRegistryCache.forceInChangesForThisUncommittedTransaction(tenant);

        if (logger.isDebugEnabled())
        {
            logger.debug("Triggered immediate reload of dictionary for tenant " + tenant);
        }
    }

    @Override
    public void destroy()
    {
        String tenant = tenantService.getCurrentUserDomain();

        // TODO Should be reworked when ACE-2001 will be implemented
        dictionaryRegistryCache.remove(tenant);

        if (logger.isDebugEnabled())
        {
            logger.debug("Dictionary destroyed for tenant " + tenant);
        }
    }

    @Override
    public void reset()
    {
        String tenant = tenantService.getCurrentUserDomain();
        if (logger.isDebugEnabled())
        {
            logger.debug("Resetting dictionary for tenant " + tenant);
        }

        destroy();
        // Ensure that we have a dictionary available right now
        getDictionaryRegistry(tenant);

        if (logger.isDebugEnabled())
        {
            logger.debug("Dictionary reset complete for tenant " + tenant);
        }
    }

    @Override
    public QName putModel(M2Model model)
    {
        // the core registry is not yet initialised so put it in the core registry
        QName ret = putModelImpl(model, true);
        return ret;
    }

    @Override
    public QName putModelIgnoringConstraints(M2Model model)
    {
        return putModelImpl(model, false);
    }

    private QName putModelImpl(M2Model model, boolean enableConstraintClassLoading)
    {
        // Compile model definition
        CompiledModel compiledModel = model.compile(this, this,
                enableConstraintClassLoading);
        QName modelName = compiledModel.getModelDefinition().getName();

        getTenantDictionaryRegistry().putModel(compiledModel);

        if (logger.isTraceEnabled())
        {
            logger.trace("Registered core model: "
                    + modelName.toPrefixString(this));
            for (M2Namespace namespace : model.getNamespaces())
            {
                logger.trace("Registered core namespace: '"
                        + namespace.getUri() + "' (prefix '"
                        + namespace.getPrefix() + "')");
            }
        }

        return modelName;
    }

    /**
     * @see org.alfresco.repo.dictionary.DictionaryDAO#removeModel(org.alfresco.service.namespace.QName)
     */
    public void removeModel(QName modelName)
    {
        getTenantDictionaryRegistry().removeModel(modelName);
    }

    private DictionaryRegistry getTenantDictionaryRegistry()
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        return getDictionaryRegistry(tenantDomain);
    }

    /**
     * @param modelName
     *            the model name
     * @return the compiled model of the given name
     */
    public CompiledModel getCompiledModel(QName modelName)
    {
        return getTenantDictionaryRegistry().getModel(modelName);
    }

    @Override
    public DataTypeDefinition getDataType(QName typeName)
    {
        DataTypeDefinition dataTypeDef = null;

        if (typeName != null)
        {
            dataTypeDef = getTenantDictionaryRegistry().getDataType(typeName);
        }

        return dataTypeDef;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DataTypeDefinition getDataType(Class javaClass)
    {
        return getTenantDictionaryRegistry().getDataType(javaClass);
    }

    @Override
    public Collection<DataTypeDefinition> getDataTypes(QName modelName)
    {
        CompiledModel model = getCompiledModel(modelName);
        return model.getDataTypes();
    }

    @Override
    public TypeDefinition getType(QName typeName)
    {
        TypeDefinition typeDef = null;

        if (typeName != null)
        {
            typeDef = getTenantDictionaryRegistry().getType(typeName);
        }

        return typeDef;
    }

    @Override
    public Collection<QName> getSubTypes(QName superType, boolean follow)
    {
        // note: could be optimised further, if compiled into the model

        // Get all types (with parent type) for all models
        Map<QName, QName> allTypesAndParents = new HashMap<QName, QName>(); // name,
                                                                            // parent

        for (CompiledModel model : getCompiledModels(true).values())
        {
            for (TypeDefinition type : model.getTypes())
            {
                allTypesAndParents.put(type.getName(), type.getParentName());
            }
        }

        // Get sub types
        HashSet<QName> subTypes = new HashSet<QName>();
        for (QName type : allTypesAndParents.keySet())
        {
            if (follow)
            {
                // all sub types
                QName current = type;
                while ((current != null) && !current.equals(superType))
                {
                    current = allTypesAndParents.get(current); // get parent
                }
                if (current != null)
                {
                    subTypes.add(type);
                }
            }
            else
            {
                // immediate sub types only
                QName typesSuperType = allTypesAndParents.get(type);
                if (typesSuperType != null && typesSuperType.equals(superType))
                {
                    subTypes.add(type);
                }
            }

        }
        return subTypes;
    }

    @Override
    public AspectDefinition getAspect(QName aspectName)
    {
        AspectDefinition aspectDef = null;

        if (aspectName != null)
        {
            aspectDef = getTenantDictionaryRegistry().getAspect(aspectName);
        }

        return aspectDef;
    }

    @Override
    public Collection<QName> getSubAspects(QName superAspect, boolean follow)
    {
        // note: could be optimised further, if compiled into the model

        // Get all aspects (with parent aspect) for all models
        Map<QName, QName> allAspectsAndParents = new HashMap<QName, QName>(); // name,
                                                                              // parent

        for (CompiledModel model : getCompiledModels(true).values())
        {
            for (AspectDefinition aspect : model.getAspects())
            {
                allAspectsAndParents.put(aspect.getName(),
                        aspect.getParentName());
            }
        }

        // Get sub aspects
        HashSet<QName> subAspects = new HashSet<QName>();
        for (QName aspect : allAspectsAndParents.keySet())
        {
            if (follow)
            {
                // all sub aspects
                QName current = aspect;
                while ((current != null) && !current.equals(superAspect))
                {
                    current = allAspectsAndParents.get(current); // get parent
                }
                if (current != null)
                {
                    subAspects.add(aspect);
                }
            }
            else
            {
                // immediate sub aspects only
                QName typesSuperAspect = allAspectsAndParents.get(aspect);
                if (typesSuperAspect != null
                        && typesSuperAspect.equals(superAspect))
                {
                    subAspects.add(aspect);
                }
            }
        }
        return subAspects;
    }

    @Override
    public ClassDefinition getClass(QName className)
    {
        ClassDefinition classDef = null;

        if (className != null)
        {
            classDef = getTenantDictionaryRegistry().getClass(className);
        }

        return classDef;
    }

    @Override
    public PropertyDefinition getProperty(QName propertyName)
    {
        PropertyDefinition propertyDef = null;

        if (propertyName != null)
        {
            propertyDef = getTenantDictionaryRegistry().getProperty(
                    propertyName);
        }

        return propertyDef;
    }

    @Override
    public ConstraintDefinition getConstraint(QName constraintQName)
    {
        ConstraintDefinition constraintDef = null;

        if (constraintQName != null)
        {
            constraintDef = getTenantDictionaryRegistry().getConstraint(
                    constraintQName);
        }

        return constraintDef;
    }

    @Override
    public AssociationDefinition getAssociation(QName assocName)
    {
        return getTenantDictionaryRegistry().getAssociation(assocName);
    }

    public Collection<AssociationDefinition> getAssociations(QName modelName)
    {
        CompiledModel model = getCompiledModel(modelName);
        return model.getAssociations();
    }

    public Collection<QName> getModels(boolean includeInherited)
    {
        // get all models - including inherited models, if applicable
        return getCompiledModels(includeInherited).keySet();
    }

    @Override
    public Collection<QName> getModels()
    {
        // get all models - including inherited models, if applicable
        return getModels(true);
    }

    public Collection<QName> getTypes(boolean includeInherited)
    {
        return getTenantDictionaryRegistry().getTypes(includeInherited);
    }

    public Collection<QName> getAssociations(boolean includeInherited)
    {
        return getTenantDictionaryRegistry().getAssociations(includeInherited);
    }

    public Collection<QName> getAspects(boolean includeInherited)
    {
        return getTenantDictionaryRegistry().getAspects(includeInherited);
    }

    // MT-specific
    public boolean isModelInherited(QName modelName)
    {
        return getTenantDictionaryRegistry().isModelInherited(modelName);
    }

    private Map<QName, CompiledModel> getCompiledModels(boolean includeInherited)
    {
        return getTenantDictionaryRegistry().getCompiledModels(includeInherited);
    }

    @Override
    public ModelDefinition getModel(QName name)
    {
        CompiledModel model = getCompiledModel(name);
        return model.getModelDefinition();
    }

    @Override
    public Collection<TypeDefinition> getTypes(QName modelName)
    {
        CompiledModel model = getCompiledModel(modelName);
        return model.getTypes();
    }

    @Override
    public Collection<AspectDefinition> getAspects(QName modelName)
    {
        CompiledModel model = getCompiledModel(modelName);
        return model.getAspects();
    }

    @Override
    public TypeDefinition getAnonymousType(QName type, Collection<QName> aspects)
    {
        TypeDefinition typeDef = getType(type);
        if (typeDef == null)
        {
            throw new DictionaryException(
                    "d_dictionary.model.err.type_not_found", type);
        }
        Collection<AspectDefinition> aspectDefs = new ArrayList<AspectDefinition>();
        if (aspects != null)
        {
            for (QName aspect : aspects)
            {
                AspectDefinition aspectDef = getAspect(aspect);
                if (aspectDef == null)
                {
                    throw new DictionaryException(
                            "d_dictionary.model.err.aspect_not_found", aspect);
                }
                aspectDefs.add(aspectDef);
            }
        }
        return new M2AnonymousTypeDefinition(typeDef, aspectDefs);
    }

    @Override
    public Collection<PropertyDefinition> getProperties(QName modelName)
    {
        CompiledModel model = getCompiledModel(modelName);
        return model.getProperties();
    }

    @Override
    public Collection<PropertyDefinition> getProperties(QName modelName,
            QName dataType)
    {
        HashSet<PropertyDefinition> properties = new HashSet<PropertyDefinition>();

        Collection<PropertyDefinition> props = getProperties(modelName);
        for (PropertyDefinition prop : props)
        {
            if ((dataType == null)
                    || prop.getDataType().getName().equals(dataType))
            {
                properties.add(prop);
            }
        }
        return properties;
    }

    @Override
    public Collection<PropertyDefinition> getPropertiesOfDataType(QName dataType)
    {
        Collection<PropertyDefinition> properties = new HashSet<PropertyDefinition>();

        Collection<QName> modelNames = getModels();
        for (QName modelName : modelNames)
        {
            properties.addAll(getProperties(modelName, dataType));
        }

        return properties;
    }

    @Override
    public Collection<NamespaceDefinition> getNamespaces(QName modelName)
    {
        CompiledModel model = getCompiledModel(modelName);
        ModelDefinition modelDef = model.getModelDefinition();
        return modelDef.getNamespaces();
    }

    @Override
    public Collection<ConstraintDefinition> getConstraints(QName modelName)
    {
        return getConstraints(modelName, false);
    }

    public Collection<ConstraintDefinition> getConstraints(QName modelName,
            boolean referenceableDefsOnly)
    {
        CompiledModel model = getCompiledModel(modelName);
        if (referenceableDefsOnly)
        {
            return getReferenceableConstraintDefs(model);
        }
        else
        {
            return model.getConstraints();
        }
    }

    private Collection<ConstraintDefinition> getReferenceableConstraintDefs(
            CompiledModel model)
    {
        Collection<ConstraintDefinition> conDefs = model.getConstraints();
        Collection<PropertyDefinition> propDefs = model.getProperties();
        for (PropertyDefinition propDef : propDefs)
        {
            for (ConstraintDefinition conDef : propDef.getConstraints())
            {
                conDefs.remove(conDef);
            }
        }

        return conDefs;
    }

    // re-entrant (eg. via reset)
    @Override
    public DictionaryRegistry getDictionaryRegistry(String tenantDomain)
    {
        DictionaryRegistry dictionaryRegistry = null;

        if (tenantDomain == null)
        {
            throw new AlfrescoRuntimeException("Tenant must be set");
        }

        // check threadlocal first - return if set
        dictionaryRegistry = getThreadLocal().get(tenantDomain);
        if (dictionaryRegistry == null)
        {
            dictionaryRegistry = dictionaryRegistryCache.get(tenantDomain);
        }

        return dictionaryRegistry;
    }

    /**
     * For cache use only.
     * 
     * @param tenantDomain String
     * @return constructed DictionaryRegistry
     */
    public DictionaryRegistry initDictionaryRegistry(final String tenantDomain)
    {
        return AuthenticationUtil.runAs(
                new RunAsWork<DictionaryRegistry>()
                {
                    public DictionaryRegistry doWork()
                    {
                        DictionaryRegistry dictionaryRegistry = null;
                        if (tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
                        {
                            dictionaryRegistry = createCoreDictionaryRegistry();
                        }
                        else
                        {
                            dictionaryRegistry = createTenantDictionaryRegistry(tenantDomain);
                        }

                        getThreadLocal().put(tenantDomain, dictionaryRegistry);
                        dictionaryRegistry.init();
                        getThreadLocal().remove(tenantDomain);

                        return dictionaryRegistry;
                    }
                },
                tenantService.getDomainUser(
                        AuthenticationUtil.getSystemUserName(), tenantDomain));
    }

    /**
     * Return diffs between input model and model in the Dictionary.
     * 
     * If the input model does not exist in the Dictionary then no diffs will be
     * returned.
     * 
     * @param model M2Model
     * @return model diffs (if any)
     */
    public List<M2ModelDiff> diffModel(M2Model model)
    {
        return diffModel(model, true);
    }

    public List<M2ModelDiff> diffModelIgnoringConstraints(M2Model model)
    {
        return diffModel(model, false);
    }

    /**
     * Return diffs between input model and model in the Dictionary.
     * 
     * If the input model does not exist in the Dictionary then no diffs will be
     * returned.
     * 
     * @param model M2Model
     * @param enableConstraintClassLoading boolean
     * @return model diffs (if any)
     */
    public List<M2ModelDiff> diffModel(M2Model model,
            boolean enableConstraintClassLoading)
    {
        // Compile model definition
        CompiledModel compiledModel = model.compile(this, this,
                enableConstraintClassLoading);
        QName modelName = compiledModel.getModelDefinition().getName();

        CompiledModel previousVersion = null;
        try
        {
            previousVersion = getCompiledModel(modelName);
        }
        catch (DictionaryException e)
        {
             // ignore missing model, there's no need to warn about this.
            logger.debug(e);
        }

        if (previousVersion == null)
        {
            return new ArrayList<M2ModelDiff>(0);
        }
        else
        {
            return diffModel(previousVersion, compiledModel);
        }
    }

    /**
     * Return diffs between two compiled models.
     * 
     * note: - checks classes (types & aspects) for incremental updates - checks
     * properties for incremental updates, but does not include the diffs -
     * checks assocs & child assocs for incremental updates, but does not
     * include the diffs - incremental updates include changes in
     * title/description, property default value, etc - ignores changes in model
     * definition except name (ie. title, description, author, published date,
     * version are treated as an incremental update)
     * 
     * TODO - imports - namespace - datatypes - constraints (including property
     * constraints - references and inline)
     * 
     * @param previousVersion CompiledModel
     * @param model CompiledModel
     * @return model diffs (if any)
     */
    /* package */List<M2ModelDiff> diffModel(CompiledModel previousVersion,
            CompiledModel model)
    {
        List<M2ModelDiff> M2ModelDiffs = new ArrayList<M2ModelDiff>();

        if (previousVersion != null)
        {
            Collection<TypeDefinition> previousTypes = previousVersion
                    .getTypes();
            Collection<AspectDefinition> previousAspects = previousVersion
                    .getAspects();
            Collection<ConstraintDefinition> previousConDefs = getReferenceableConstraintDefs(previousVersion);
            Collection<NamespaceDefinition> previousImportedNamespaces = previousVersion.getModelDefinition().getImportedNamespaces();

            if (model == null)
            {
                // delete model
                for (TypeDefinition previousType : previousTypes)
                {
                    M2ModelDiffs.add(new M2ModelDiff(previousType.getName(),
                            M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_DELETED));
                }
                for (AspectDefinition previousAspect : previousAspects)
                {
                    M2ModelDiffs.add(new M2ModelDiff(previousAspect.getName(),
                            M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_DELETED));
                }
                for (ConstraintDefinition previousConDef : previousConDefs)
                {
                    M2ModelDiffs.add(new M2ModelDiff(previousConDef.getName(),
                            M2ModelDiff.TYPE_CONSTRAINT,
                            M2ModelDiff.DIFF_DELETED));
                }
            }
            else
            {
                // update model
                Collection<TypeDefinition> types = model.getTypes();
                Collection<AspectDefinition> aspects = model.getAspects();
                Collection<ConstraintDefinition> conDefs = getReferenceableConstraintDefs(model);
                Collection<NamespaceDefinition> importedNamespaces = model.getModelDefinition().getImportedNamespaces();

                if (previousTypes.size() != 0)
                {
                    M2ModelDiffs.addAll(M2ClassDefinition.diffClassLists(
                            new ArrayList<ClassDefinition>(previousTypes),
                            new ArrayList<ClassDefinition>(types),
                            M2ModelDiff.TYPE_TYPE));
                }
                else
                {
                    for (TypeDefinition type : types)
                    {
                        M2ModelDiffs
                                .add(new M2ModelDiff(type.getName(),
                                        M2ModelDiff.TYPE_TYPE,
                                        M2ModelDiff.DIFF_CREATED));
                    }
                }

                if (previousAspects.size() != 0)
                {
                    M2ModelDiffs.addAll(M2ClassDefinition.diffClassLists(
                            new ArrayList<ClassDefinition>(previousAspects),
                            new ArrayList<ClassDefinition>(aspects),
                            M2ModelDiff.TYPE_ASPECT));
                }
                else
                {
                    for (AspectDefinition aspect : aspects)
                    {
                        M2ModelDiffs.add(new M2ModelDiff(aspect.getName(),
                                M2ModelDiff.TYPE_ASPECT,
                                M2ModelDiff.DIFF_CREATED));
                    }
                }

                if (previousConDefs.size() != 0)
                {
                    M2ModelDiffs
                            .addAll(M2ConstraintDefinition
                                    .diffConstraintLists(
                                            new ArrayList<ConstraintDefinition>(
                                                    previousConDefs),
                                            new ArrayList<ConstraintDefinition>(
                                                    conDefs)));
                }
                else
                {
                    for (ConstraintDefinition conDef : conDefs)
                    {
                        M2ModelDiffs.add(new M2ModelDiff(conDef.getName(),
                                M2ModelDiff.TYPE_CONSTRAINT,
                                M2ModelDiff.DIFF_CREATED));
                    }
                }

                if (previousImportedNamespaces.size() != 0)
                {
                    M2ModelDiffs
                            .addAll(M2NamespaceDefinition
                                    .diffNamespaceDefinitionLists(
                                            new ArrayList<NamespaceDefinition>(
                                                    previousImportedNamespaces),
                                            new ArrayList<NamespaceDefinition>(
                                                    importedNamespaces)));
                }
                else
                {
                    for(NamespaceDefinition namespaceDefinition: importedNamespaces) 
                    {
                        M2ModelDiffs.add(new M2ModelDiff(namespaceDefinition.getModel().getName(),
                                namespaceDefinition,
                                M2ModelDiff.TYPE_NAMESPACE,
                                M2ModelDiff.DIFF_CREATED));
                    }
                }
            }
        }
        else
        {
            if (model != null)
            {
                // new model
                Collection<TypeDefinition> types = model.getTypes();
                Collection<AspectDefinition> aspects = model.getAspects();

                for (TypeDefinition type : types)
                {
                    M2ModelDiffs.add(new M2ModelDiff(type.getName(),
                            M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_CREATED));
                }

                for (AspectDefinition aspect : aspects)
                {
                    M2ModelDiffs.add(new M2ModelDiff(aspect.getName(),
                            M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_CREATED));
                }
            }
            else
            {
                // nothing to diff
            }
        }

        return M2ModelDiffs;
    }

    @Override
    public ClassLoader getResourceClassLoader()
    {
        return resourceClassLoader;
    }

    @Override
    public void setResourceClassLoader(ClassLoader resourceClassLoader)
    {
        this.resourceClassLoader = resourceClassLoader;
    }

    @Override
    public String getNamespaceURI(String prefix)
    {
        return getTenantDictionaryRegistry().getNamespaceURI(prefix);
    }

    @Override
    public Collection<String> getPrefixes(String URI)
    {
        return getTenantDictionaryRegistry().getPrefixes(URI);
    }

    @Override
    public void addURI(String uri)
    {
        getTenantDictionaryRegistry().addURI(uri);
    }

    @Override
    public Collection<String> getPrefixes()
    {
        return Collections.unmodifiableCollection(getTenantDictionaryRegistry()
                .getPrefixesCache().keySet());
    }

    @Override
    public Collection<String> getURIs()
    {
        return Collections.unmodifiableCollection(getTenantDictionaryRegistry()
                .getUrisCache());
    }

    @Override
    public void removeURI(String uri)
    {
        getTenantDictionaryRegistry().removeURI(uri);
    }

    @Override
    public void addPrefix(String prefix, String uri)
    {
        getTenantDictionaryRegistry().addPrefix(prefix, uri);
    }

    @Override
    public void removePrefix(String prefix)
    {
        getTenantDictionaryRegistry().removePrefix(prefix);
    }

    private AtomicBoolean contextRefreshed = new AtomicBoolean(false);

    @Override
    public boolean isContextRefreshed()
    {
        return contextRefreshed.get();
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof ContextRefreshedEvent)
        {
            contextRefreshed.set(true);
        }
    }
}
