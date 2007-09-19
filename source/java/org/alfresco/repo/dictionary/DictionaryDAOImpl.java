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
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
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
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Default implementation of the Dictionary.
 *  
 * @author David Caruana
 *
 */
public class DictionaryDAOImpl implements DictionaryDAO
{
    // TODO: Allow for the dynamic creation of models.  Supporting
    //       this requires the ability to persistently store the
    //       registration of models, the ability to load models
    //       from a persistent store, the refresh of the cache
    //       and concurrent read/write of the models.
    //       (in progress)
    
    /**
     * Lock objects
     */
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();
    
    // Namespace Data Access
    private NamespaceDAO namespaceDAO;

    // Tenant Service
    private TenantService tenantService;

    // Map of Namespace URI usages to Models
    private SimpleCache<String, Map<String, List<CompiledModel>>> uriToModelsCache;

    // Map of model name to compiled model
    private SimpleCache<String, Map<QName,CompiledModel>> compiledModelsCache;

    // Static list of registered dictionary deployers
    private List<DictionaryDeployer> dictionaryDeployers = new ArrayList<DictionaryDeployer>();

    // Logger
    private static Log logger = LogFactory.getLog(DictionaryDAO.class);


	// inject dependencies
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setUriToModelsCache(SimpleCache<String, Map<String, List<CompiledModel>>> uriToModelsCache)
    {
        this.uriToModelsCache = uriToModelsCache;
    }
    
    public void setCompiledModelsCache(SimpleCache<String, Map<QName,CompiledModel>> compiledModelsCache)
    {
        this.compiledModelsCache = compiledModelsCache;
    }
    
    /**
     * Construct
     * 
     * @param namespaceDAO  namespace data access
     */
    public DictionaryDAOImpl(NamespaceDAO namespaceDAO)
    {
        this.namespaceDAO = namespaceDAO;
        this.namespaceDAO.registerDictionary(this);
        
    }
    
    /**
     * Register with the Dictionary
     */
    public void register(DictionaryDeployer dictionaryDeployer)
    {
        if (! dictionaryDeployers.contains(dictionaryDeployer))
        {
            dictionaryDeployers.add(dictionaryDeployer);
        }
    }
    
    /**
     * Initialise the Dictionary & Namespaces
     */
    public void init()
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        
        // initialise empty dictionary & namespaces
        putCompiledModels(tenantDomain, new HashMap<QName,CompiledModel>());
        putUriToModels(tenantDomain, new HashMap<String, List<CompiledModel>>());
                
        namespaceDAO.init();
        
        // populate the dictionary
        for (DictionaryDeployer dictionaryDeployer : dictionaryDeployers)
        {
        	dictionaryDeployer.initDictionary();
        }
        
        logger.info("Dictionary initialised");
    }
    
    /**
     * Destroy the Dictionary & Namespaces
     */
    public void destroy()
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        
        removeCompiledModels(tenantDomain);
        removeUriToModels(tenantDomain);                 
        
        namespaceDAO.destroy();
        
        logger.info("Dictionary destroyed");
    }
    
    /**
     * Reset the Dictionary & Namespaces
     */      
    public void reset()
    {
    	reset(tenantService.getCurrentUserDomain());
    }
    
    private void reset(String tenantDomain)
    {
		if (logger.isDebugEnabled()) 
		{
			logger.debug("Resetting dictionary ...");
		}
		    
		String userName;
		if (tenantDomain == "")
		{
			userName = AuthenticationUtil.getSystemUserName();
		}
		else
		{
			userName = tenantService.getDomainUser(TenantService.ADMIN_BASENAME, tenantDomain);
		}
		   
		AuthenticationUtil.runAs(new RunAsWork<Object>()
		{
			public Object doWork()
			{  
		       destroy();
		       init();
		       
		       return null;
			}                               
		}, userName);
		
		if (logger.isDebugEnabled()) 
		{
			logger.debug("... resetting dictionary completed");
		}
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.DictionaryDAO#putModel(org.alfresco.repo.dictionary.impl.M2Model)
     */
    public QName putModel(M2Model model)
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        
        // Compile model definition
        CompiledModel compiledModel = model.compile(this, namespaceDAO);
        QName modelName = compiledModel.getModelDefinition().getName();

        // Remove namespace definitions for previous model, if it exists
        CompiledModel previousVersion = getCompiledModels(tenantDomain).get(modelName);
        if (previousVersion != null)
        {
            for (M2Namespace namespace : previousVersion.getM2Model().getNamespaces())
            {
                namespaceDAO.removePrefix(namespace.getPrefix());
                namespaceDAO.removeURI(namespace.getUri());
                unmapUriToModel(namespace.getUri(), previousVersion, tenantDomain);
            }
            for (M2Namespace importNamespace : previousVersion.getM2Model().getImports())
            {
            	unmapUriToModel(importNamespace.getUri(), previousVersion, tenantDomain);
            }
        }
        
        // Create namespace definitions for new model
        for (M2Namespace namespace : model.getNamespaces())
        {
            namespaceDAO.addURI(namespace.getUri());
            namespaceDAO.addPrefix(namespace.getPrefix(), namespace.getUri());
            mapUriToModel(namespace.getUri(), compiledModel, tenantDomain);
        }
        for (M2Namespace importNamespace : model.getImports())
        {
        	mapUriToModel(importNamespace.getUri(), compiledModel, tenantDomain);
        }
        
        // Publish new Model Definition
        getCompiledModels(tenantDomain).put(modelName, compiledModel);

        if (logger.isInfoEnabled())
        {
            logger.info("Registered model " + modelName.toPrefixString(namespaceDAO));
            for (M2Namespace namespace : model.getNamespaces())
            {
                logger.info("Registered namespace '" + namespace.getUri() + "' (prefix '" + namespace.getPrefix() + "')");
            }
        }

        return modelName;
    }
    

    /**
     * @see org.alfresco.repo.dictionary.DictionaryDAO#removeModel(org.alfresco.service.namespace.QName)
     */
    public void removeModel(QName modelName)
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        
        CompiledModel compiledModel = getCompiledModels(tenantDomain).get(modelName);
        if (compiledModel != null)
        {
            // Remove the namespaces from the namespace service
            M2Model model = compiledModel.getM2Model();            
            for (M2Namespace namespace : model.getNamespaces())
            {
                namespaceDAO.removePrefix(namespace.getPrefix());
                namespaceDAO.removeURI(namespace.getUri());
                unmapUriToModel(namespace.getUri(), compiledModel, tenantDomain);
            }
            
            // Remove the model from the list
            getCompiledModels(tenantDomain).remove(modelName);
        }
    }


    /**
     * Map Namespace URI to Model
     * 
     * @param uri   namespace uri
     * @param model   model
     * @param tenantDomain
     */
    private void mapUriToModel(String uri, CompiledModel model, String tenantDomain)
    {
    	List<CompiledModel> models = getUriToModels(tenantDomain).get(uri);
    	if (models == null)
    	{
    		models = new ArrayList<CompiledModel>();
    		getUriToModels(tenantDomain).put(uri, models);
    	}
    	if (!models.contains(model))
    	{
    		models.add(model);
    	}
    }

    
    /**
     * Unmap Namespace URI from Model
     * 
     * @param uri  namespace uri
     * @param model   model
     * @param tenantDomain
     */
    private void unmapUriToModel(String uri, CompiledModel model, String tenantDomain)
    {
    	List<CompiledModel> models = getUriToModels(tenantDomain).get(uri);
    	if (models != null)
    	{
    		models.remove(model);
    	}
    }

    
    /**
     * Get Models mapped to Namespace Uri
     * 
     * @param uri   namespace uri
     * @return   mapped models 
     */
    private List<CompiledModel> getModelsForUri(String uri)
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        if (tenantDomain != "")
        {
            // get non-tenant models (if any)
            List<CompiledModel> models = getUriToModels("").get(uri);
            
            List<CompiledModel> filteredModels = new ArrayList<CompiledModel>();
            if (models != null)
            {
                filteredModels.addAll(models);
            }
    
            // get tenant models (if any)
            List<CompiledModel> tenantModels = getUriToModels(tenantDomain).get(uri);
            if (tenantModels != null)
            {
                if (models != null)
                {
                    // check to see if tenant model overrides a non-tenant model
                    for (CompiledModel tenantModel : tenantModels)
                    {
                        for (CompiledModel model : models)
                        {
                            if (tenantModel.getM2Model().getName().equals(model.getM2Model().getName()))
                            {
                                filteredModels.remove(model);
                            }
                        }
                    }
                }
                filteredModels.addAll(tenantModels);
                models = filteredModels;
            }
    
            if (models == null)
            {
                models = Collections.emptyList();
            }
            return models;
        }

        List<CompiledModel> models = getUriToModels("").get(uri);
        if (models == null)
        {
            models = Collections.emptyList(); 
        }
        return models;
    }


    /**
     * @param modelName  the model name
     * @return the compiled model of the given name
     */
    /* package */ CompiledModel getCompiledModel(QName modelName)
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        if (tenantDomain != "")
        {
            // get tenant-specific model (if any)
            CompiledModel model = getCompiledModels(tenantDomain).get(modelName);
            if (model != null)
            {
                return model;
            }
            // else drop down to check for shared (core/system) models ...
        }

        // get non-tenant model (if any)
        CompiledModel model = getCompiledModels("").get(modelName);
        if (model == null)
        {
            throw new DictionaryException("d_dictionary.model.err.no_model", modelName);
        }
        return model;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getPropertyType(org.alfresco.repo.ref.QName)
     */
    public DataTypeDefinition getDataType(QName typeName)
    {
        List<CompiledModel> models = getModelsForUri(typeName.getNamespaceURI());
        for (CompiledModel model : models)
        {
        	DataTypeDefinition dataType = model.getDataType(typeName);
        	if (dataType != null)
        	{
        		return dataType;
        	}
        }
        return null;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelQuery#getDataType(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public DataTypeDefinition getDataType(Class javaClass)
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        if (tenantDomain != "")
        {
            // get tenant models (if any)                
            for (CompiledModel model : getCompiledModels(tenantDomain).values())
            { 
                DataTypeDefinition dataTypeDef = model.getDataType(javaClass);
                if (dataTypeDef != null)
                {
                    return dataTypeDef;
                }
            }          
        
            // get non-tenant models (if any)
            for (CompiledModel model : getCompiledModels("").values())
            {    
                DataTypeDefinition dataTypeDef = model.getDataType(javaClass);
                if (dataTypeDef != null)
                {
                    return dataTypeDef;
                }
            }
        
            return null;
        }
        else
        {
            for (CompiledModel model : getCompiledModels("").values())
            {    
                DataTypeDefinition dataTypeDef = model.getDataType(javaClass);
                if (dataTypeDef != null)
                {
                    return dataTypeDef;
                }
            }
        }
        return null;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.DictionaryDAO#getPropertyTypes(org.alfresco.repo.ref.QName)
     */
    public Collection<DataTypeDefinition> getDataTypes(QName modelName)
    {
        CompiledModel model = getCompiledModel(modelName);
        return model.getDataTypes();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getType(org.alfresco.repo.ref.QName)
     */
    public TypeDefinition getType(QName typeName)
    {
        List<CompiledModel> models = getModelsForUri(typeName.getNamespaceURI());
        for (CompiledModel model : models)
        {
        	TypeDefinition type = model.getType(typeName);
        	if (type != null)
        	{
        		return type;
        	}
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryDAO#getSubTypes(org.alfresco.service.namespace.QName, boolean)
     */
    public Collection<QName> getSubTypes(QName superType, boolean follow)
    {
    	// note: could be optimised further, if compiled into the model
    	
        // Get all types (with parent type) for all models
        Map<QName, QName> allTypesAndParents = new HashMap<QName, QName>(); // name, parent
        
        for (CompiledModel model : getCompiledModels().values())
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
        		if (allTypesAndParents.get(type).equals(superType))
        		{
        			subTypes.add(type);
        		}
        	}

        }
        return subTypes;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getAspect(org.alfresco.repo.ref.QName)
     */
    public AspectDefinition getAspect(QName aspectName)
    {
        List<CompiledModel> models = getModelsForUri(aspectName.getNamespaceURI());
        for (CompiledModel model : models)
        {
        	AspectDefinition aspect = model.getAspect(aspectName);
        	if (aspect != null)
        	{
        		return aspect;
        	}
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryDAO#getSubAspects(org.alfresco.service.namespace.QName, boolean)
     */
    public Collection<QName> getSubAspects(QName superAspect, boolean follow)
    {
    	// note: could be optimised further, if compiled into the model
    	
        // Get all aspects (with parent aspect) for all models   
        Map<QName, QName> allAspectsAndParents = new HashMap<QName, QName>(); // name, parent
        
        for (CompiledModel model : getCompiledModels().values())
        {
        	for (AspectDefinition aspect : model.getAspects())
        	{
        		allAspectsAndParents.put(aspect.getName(), aspect.getParentName());
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
	    		if (allAspectsAndParents.get(aspect).equals(superAspect))
	    		{
	    			subAspects.add(aspect);
	    		}
	    	}        	
        }
        return subAspects;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getClass(org.alfresco.repo.ref.QName)
     */
    public ClassDefinition getClass(QName className)
    {
        List<CompiledModel> models = getModelsForUri(className.getNamespaceURI());

        for (CompiledModel model : models)
        {
        	ClassDefinition classDef = model.getClass(className);
        	if (classDef != null)
        	{
        		return classDef;
        	}
        }
        return null;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getProperty(org.alfresco.repo.ref.QName)
     */
    public PropertyDefinition getProperty(QName propertyName)
    {
        List<CompiledModel> models = getModelsForUri(propertyName.getNamespaceURI());
        for (CompiledModel model : models)
        {
        	PropertyDefinition propDef = model.getProperty(propertyName);
        	if (propDef != null)
        	{
        		return propDef;
        	}
        }
        return null;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelQuery#getConstraint(org.alfresco.service.namespace.QName)
     */
    public ConstraintDefinition getConstraint(QName constraintQName)
    {
        List<CompiledModel> models = getModelsForUri(constraintQName.getNamespaceURI());
        for (CompiledModel model : models)
        {
        	ConstraintDefinition constraintDef = model.getConstraint(constraintQName);
        	if (constraintDef != null)
        	{
        		return constraintDef;
        	}
        }
        return null;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getAssociation(org.alfresco.repo.ref.QName)
     */
    public AssociationDefinition getAssociation(QName assocName)
    {
        List<CompiledModel> models = getModelsForUri(assocName.getNamespaceURI());
        for (CompiledModel model : models)
        {
        	AssociationDefinition assocDef = model.getAssociation(assocName);
        	if (assocDef != null)
        	{
        		return assocDef;
        	}
        }
        return null;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.DictionaryDAO#getModels()
     */
    public Collection<QName> getModels()
    {
    	return getCompiledModels().keySet();
    }
    
    // return all tenant-specific models and all shared (non-overridden) models
    private Map<QName,CompiledModel> getCompiledModels() 
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        if (tenantDomain != "")
        {
            // return all tenant-specific models and all shared (non-overridden) models
            Map<QName, CompiledModel> filteredModels = new HashMap<QName, CompiledModel>();
            
            // get tenant models (if any)
            Map<QName,CompiledModel> tenantModels = getCompiledModels(tenantDomain);
            
            // get non-tenant models (if any)
            // note: these will be shared, if not overridden - could be core/system model or additional custom model shared between tenants
            Map<QName,CompiledModel> nontenantModels = getCompiledModels("");

            // check for overrides
            filteredModels.putAll(nontenantModels);
     
            for (QName tenantModel : tenantModels.keySet())
            {
                for (QName nontenantModel : nontenantModels.keySet())
                {
                    if (tenantModel.equals(nontenantModel))
                    {
                        // override
                        filteredModels.remove(nontenantModel);
                        break;
                    }
                }
            }

            filteredModels.putAll(tenantModels);
            return filteredModels;
        }
        else
        {
            return getCompiledModels("");
        } 
    }
    
    // used for clean-up, e.g. when deleting a tenant
    protected Collection<QName> getNonSharedModels()
    {            
        return getCompiledModels(tenantService.getCurrentUserDomain()).keySet();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.DictionaryDAO#getModel(org.alfresco.repo.ref.QName)
     */
    public ModelDefinition getModel(QName name)
    {
        CompiledModel model = getCompiledModel(name);
        return model.getModelDefinition();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.DictionaryDAO#getTypes(org.alfresco.repo.ref.QName)
     */
    public Collection<TypeDefinition> getTypes(QName modelName)
    {
        CompiledModel model = getCompiledModel(modelName);
        return model.getTypes();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.DictionaryDAO#getAspects(org.alfresco.repo.ref.QName)
     */
    public Collection<AspectDefinition> getAspects(QName modelName)
    {
        CompiledModel model = getCompiledModel(modelName);
        return model.getAspects();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.DictionaryDAO#getAnonymousType(org.alfresco.repo.ref.QName, java.util.Collection)
     */
    public TypeDefinition getAnonymousType(QName type, Collection<QName> aspects)
    {
        TypeDefinition typeDef = getType(type);
        if (typeDef == null)
        {
            throw new DictionaryException("d_dictionary.model.err.type_not_found", type);
        }
        Collection<AspectDefinition> aspectDefs = new ArrayList<AspectDefinition>();
        if (aspects != null)
        {
            for (QName aspect : aspects)
            {
                AspectDefinition aspectDef = getAspect(aspect);
                if (aspectDef == null)
                {
                    throw new DictionaryException("d_dictionary.model.err.aspect_not_found", aspect);
                }
                aspectDefs.add(aspectDef);
            }
        }
        return new M2AnonymousTypeDefinition(typeDef, aspectDefs);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryDAO#getProperties(org.alfresco.service.namespace.QName)
     */
    public Collection<PropertyDefinition> getProperties(QName modelName)
    {
        CompiledModel model = getCompiledModel(modelName);
        return model.getProperties();
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryDAO#getProperties(org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
     */
    public Collection<PropertyDefinition> getProperties(QName modelName, QName dataType)
    {
        HashSet<PropertyDefinition> properties = new HashSet<PropertyDefinition>();

        Collection<PropertyDefinition> props = getProperties(modelName);
        for(PropertyDefinition prop : props)
        {
            if((dataType == null) ||   prop.getDataType().getName().equals(dataType))
            {
                properties.add(prop);
            }
        }
        return properties;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.dictionary.DictionaryDAO#getNamespaces(org.alfresco.service.namespace.QName)
     */
    public Collection<NamespaceDefinition> getNamespaces(QName modelName)
    {
        CompiledModel model = getCompiledModel(modelName);
        ModelDefinition modelDef = model.getModelDefinition();
        
        List<NamespaceDefinition> namespaces = new ArrayList<NamespaceDefinition>();
        for (M2Namespace namespace : model.getM2Model().getNamespaces())
        {
            namespaces.add(new M2NamespaceDefinition(modelDef, namespace.getUri(), namespace.getPrefix()));
        }
        
        return namespaces;
    }
    
    /**
     * Get compiledModels from the cache (in the context of the given tenant domain)
     * 
     * @param tenantDomain
     */
    private Map<QName,CompiledModel> getCompiledModels(String tenantDomain)
    {
        Map<QName,CompiledModel> compiledModels = null;
        try 
        {
            readLock.lock();
            compiledModels = compiledModelsCache.get(tenantDomain);
        }
        finally
        {
            readLock.unlock();
        }
        
        
        if (compiledModels == null)
        {          
            reset(tenantDomain); // reset caches - may have been invalidated (e.g. in a cluster)

            try 
            {
                readLock.lock();
                compiledModels = compiledModelsCache.get(tenantDomain);
            }
            finally
            {
                readLock.unlock();
            }                
                
            if (compiledModels == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to re-initialise compiledModelsCache " + tenantDomain);
            }
        }
            
        return compiledModels;
    }  

    /**
     * Put compiledModels into the cache (in the context of the given tenant domain)
     * 
     * @param tenantDomain
     */
    private void putCompiledModels(String tenantDomain, Map<QName, CompiledModel> compiledModels)
    {      
        try 
        {
            writeLock.lock();
            compiledModelsCache.put(tenantDomain, compiledModels);
        }
        finally
        {
            writeLock.unlock();
        }       
    } 
    
    /**
     * Remove compiledModels from the cache (in the context of the given tenant domain)
     * 
     * @param tenantDomain
     */
    private void removeCompiledModels(String tenantDomain)
    {
        try
        {
            writeLock.lock();
            if (compiledModelsCache.get(tenantDomain) != null)
            {
                compiledModelsCache.get(tenantDomain).clear();
                compiledModelsCache.remove(tenantDomain);
            }
        }
        finally
        {
            writeLock.unlock();
        }        
    }

    /**
     * Get uriToModels from the cache (in the context of the given tenant domain)
     * 
     * @param tenantDomain
     */
    private Map<String, List<CompiledModel>> getUriToModels(String tenantDomain)
    {
        Map<String, List<CompiledModel>> uriToModels = null;
        try
        {
            readLock.lock();
            uriToModels = uriToModelsCache.get(tenantDomain);
        }
        finally
        {
            readLock.unlock();
        }
        
        if (uriToModels == null)
        {
            reset(tenantDomain); // reset caches - may have been invalidated (e.g. in a cluster)
            
            try
            {
                readLock.lock();
                uriToModels = uriToModelsCache.get(tenantDomain);
            }
            finally
            {
                readLock.unlock();
            }
            
            if (uriToModels == null)
            {     
                // unexpected
                throw new AlfrescoRuntimeException("Failed to re-initialise uriToModelsCache " + tenantDomain);
            }
        }
            
        return uriToModels;
    }  
    
    /**
     * Put uriToModels into the cache (in the context of the given tenant domain)
     * 
     * @param tenantDomain
     */
    private void putUriToModels(String tenantDomain, Map<String, List<CompiledModel>> uriToModels)
    {
        try 
        {
            writeLock.lock();
            uriToModelsCache.put(tenantDomain, uriToModels);
        }
        finally
        {
            writeLock.unlock();
        }
    } 
    
    /**
     * Remove uriToModels from the cache (in the context of the given tenant domain)
     * 
     * @param tenantDomain
     */
    private void removeUriToModels(String tenantDomain)
    {
        try 
        {
            writeLock.lock();
            if (uriToModelsCache.get(tenantDomain) != null)
            {
                uriToModelsCache.get(tenantDomain).clear();
                uriToModelsCache.remove(tenantDomain);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    } 
    
    /**
     * Return diffs between input model and model in the Dictionary.
     * 
     * If the input model does not exist in the Dictionary or is equivalent to the one in the Dictionary
     * then no diffs will be returned.
     * 
     * @param model
     * @return model diffs (if any)
     */
    private List<M2ModelDiff> diffModel(M2Model model)
    {
        // Compile model definition
        CompiledModel compiledModel = model.compile(this, namespaceDAO);
        QName modelName = compiledModel.getModelDefinition().getName();
        
        CompiledModel previousVersion = null;
        try { previousVersion = getCompiledModel(modelName); } catch (DictionaryException e) {} // ignore missing model

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
     * 
     * @param model
     * @return model diffs (if any)
     */
    /* package */ List<M2ModelDiff> diffModel(CompiledModel previousVersion, CompiledModel model)
    {
        List<M2ModelDiff> M2ModelDiffs = new ArrayList<M2ModelDiff>();
        
        if (previousVersion != null)
        { 
            Collection<TypeDefinition> previousTypes = previousVersion.getTypes();
            Collection<AspectDefinition> previousAspects = previousVersion.getAspects();
           
            if (model == null)
            {
                // delete model
                for (TypeDefinition previousType : previousTypes)
                {
                    M2ModelDiffs.add(new M2ModelDiff(previousType.getName(), M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_DELETED));
                }
                for (AspectDefinition previousAspect : previousAspects)
                {
                    M2ModelDiffs.add(new M2ModelDiff(previousAspect.getName(), M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_DELETED));
                }              
            }
            else
            {
                // update model
                Collection<TypeDefinition> types = model.getTypes();
                Collection<AspectDefinition> aspects = model.getAspects();
                
                if (previousTypes.size() != 0)
                {
                    M2ModelDiffs.addAll(M2ClassDefinition.diffClassLists(new ArrayList<ClassDefinition>(previousTypes), new ArrayList<ClassDefinition>(types), M2ModelDiff.TYPE_TYPE));
                }
                else
                {
                    for (TypeDefinition type : types)
                    {
                        M2ModelDiffs.add(new M2ModelDiff(type.getName(), M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_CREATED));
                    }
                }
                
                if (previousAspects.size() != 0)
                {
                    M2ModelDiffs.addAll(M2ClassDefinition.diffClassLists(new ArrayList<ClassDefinition>(previousAspects), new ArrayList<ClassDefinition>(aspects), M2ModelDiff.TYPE_ASPECT));
                }
                else
                {
                    for (AspectDefinition aspect : aspects)
                    {
                        M2ModelDiffs.add(new M2ModelDiff(aspect.getName(), M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_CREATED));
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
                    M2ModelDiffs.add(new M2ModelDiff(type.getName(), M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_CREATED));
                }
                           
                for (AspectDefinition aspect : aspects)
                {
                    M2ModelDiffs.add(new M2ModelDiff(aspect.getName(), M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_CREATED));
                }  
            }
            else 
            {
                // nothing to diff
            }
        }
        
        return M2ModelDiffs;
    }
    
    /**
     * validate against dictionary
     * 
     * if new model 
     * then nothing to validate
     * 
     * else if an existing model 
     * then could be updated (or unchanged) so validate to currently only allow incremental updates
     *   - addition of new types, aspects (except default aspects), properties, associations
     *   - no deletion of types, aspects or properties or associations
     *   - no addition, update or deletion of default/mandatory aspects
     * 
     * @param newOrUpdatedModel
     */
    public void validateModel(M2Model newOrUpdatedModel)
    {
        // Check that all the passed values are not null        
        ParameterCheck.mandatory("newOrUpdatedModel", newOrUpdatedModel);
        
        List<M2ModelDiff> modelDiffs = diffModel(newOrUpdatedModel);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            if (modelDiff.getDiffType().equals(M2ModelDiff.DIFF_DELETED))
            {
                throw new AlfrescoRuntimeException("Failed to validate model update - found deleted " + modelDiff.getElementType() + " '" + modelDiff.getElementName() + "'");
            }
            
            if (modelDiff.getDiffType().equals(M2ModelDiff.DIFF_UPDATED))
            {
                throw new AlfrescoRuntimeException("Failed to validate model update - found non-incrementally updated " + modelDiff.getElementType() + " '" + modelDiff.getElementName() + "'");
            }
        } 
    }
}
