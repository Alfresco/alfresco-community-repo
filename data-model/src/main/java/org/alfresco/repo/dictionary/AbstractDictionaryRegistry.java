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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 
 * @author sglover
 *
 */
public abstract class AbstractDictionaryRegistry implements DictionaryRegistry
{
    protected static final Log logger = LogFactory.getLog(AbstractDictionaryRegistry.class);

	protected DictionaryDAO dictionaryDAO;
    private Map<String, List<CompiledModel>> uriToModels = new ConcurrentHashMap<String, List<CompiledModel>>(0);
    private Map<QName,CompiledModel> compiledModels = new ConcurrentHashMap<QName,CompiledModel>(0);

    // namespaces
    private ReadWriteLock urisCacheRWLock = new ReentrantReadWriteLock(true);
    private List<String> urisCache = new ArrayList<String>(20);
    private Map<String, String> prefixesCache = new ConcurrentHashMap<String, String>(0);

    public AbstractDictionaryRegistry(DictionaryDAO dictionaryDAO)
    {
    	this.dictionaryDAO = dictionaryDAO;
	}

    @Override
    public void clear()
    {
    	setCompiledModels(new HashMap<QName,CompiledModel>());
    	setUriToModels(new HashMap<String, List<CompiledModel>>());
    }

    public Map<String, List<CompiledModel>> getUriToModels()
    {
    	Map<String, List<CompiledModel>> ret = new HashMap<>();
    	ret.putAll(uriToModels); // copy
        return ret;
    }

    private void setUriToModels(Map<String, List<CompiledModel>> uriToModels)
    {
        this.uriToModels = uriToModels;
    }

	@Override
	public Map<QName, CompiledModel> getCompiledModels(boolean includeInherited)
	{
		return compiledModels;
	}

    private void setCompiledModels(Map<QName, CompiledModel> compiledModels)
    {
        this.compiledModels = compiledModels;
    }

    public List<CompiledModel> getModelsForUri(String uri)
    {
    	return getModelsForUriImpl(uri);
    }

	@Override
	public QName putModel(CompiledModel model)
	{
		return putModelImpl(model);
	}

	@Override
	public CompiledModel getModel(QName modelName)
	{
		CompiledModel model = getModelImpl(modelName);
        if(model == null)
        {
            throw new DictionaryException("d_dictionary.model.err.no_model", modelName);
        }
        return model;
	}

	@Override
	public boolean modelExists(QName modelName)
	{
		CompiledModel model = getModelImpl(modelName);
        return model != null;
	}

	@Override
	public void removeModel(QName modelName)
	{
		removeModelImpl(modelName);
	}

    protected CompiledModel removeModelImpl(QName modelName)
    {
	    CompiledModel compiledModel = compiledModels.get(modelName);
	    if (compiledModel != null)
	    {
	        // Remove the namespaces from the namespace service
	        M2Model model = compiledModel.getM2Model();
	        for (M2Namespace namespace : model.getNamespaces())
	        {
	            prefixesCache.remove(namespace.getPrefix());
	            urisCacheRWLock.writeLock().lock();
	            try
	            {
	            	urisCache.remove(namespace.getUri());
	            }
	            finally
	            {
	            	urisCacheRWLock.writeLock().unlock();
	            }
	
	        	List<CompiledModel> models = uriToModels.get(namespace.getUri());
	        	if(models != null)
	        	{
	        		models.remove(compiledModel);
	        	}
	        }
	
			compiledModels.remove(modelName);
	    }

	    return compiledModel;
    }
    
    protected CompiledModel getModelImpl(QName modelName)
    {
    	CompiledModel model = compiledModels.get(modelName);
    	return model;
    }
    
    protected List<CompiledModel> getModelsForUriImpl(String uri)
    {
    	List<CompiledModel> models = uriToModels.get(uri);
    	if(models == null)
    	{
    		models = Collections.emptyList();
    	}
    	// defensive copy
        return new  ArrayList<CompiledModel>(models);
    }
    
    protected void unmapUriToModel(String uri, CompiledModel model)
    {
    	List<CompiledModel> models = uriToModels.get(uri);
    	if (models != null)
    	{
    		models.remove(model);
    	}
    }

    protected void mapUriToModel(String uri, CompiledModel model)
    {
    	List<CompiledModel> models = uriToModels.get(uri);
    	if (models == null)
    	{
    		models = new ArrayList<CompiledModel>();
    		uriToModels.put(uri, models);
    	}
    	if (!models.contains(model))
    	{
    		models.add(model);
    	}
    }

    protected QName putModelImpl(CompiledModel model)
    {
        QName modelName = model.getModelDefinition().getName();

        CompiledModel previousVersion = getModelImpl(modelName);
        if (previousVersion != null)
        {
        	for (M2Namespace namespace : previousVersion.getM2Model().getNamespaces())
        	{
        		prefixesCache.remove(namespace.getPrefix());

        		urisCacheRWLock.writeLock().lock();
        		try
        		{
        			urisCache.remove(namespace.getUri());
                }
                finally
                {
                	urisCacheRWLock.writeLock().unlock();
                }
        		unmapUriToModel(namespace.getUri(), previousVersion);
        	}

        	for (M2Namespace importNamespace : previousVersion.getM2Model().getImports())
        	{
        		unmapUriToModel(importNamespace.getUri(), previousVersion);
        	}
        }

        // Create namespace definitions for new model
        M2Model m2Model = model.getM2Model();
        for (M2Namespace namespace : m2Model.getNamespaces())
        {
        	addURI(namespace.getUri());
        	addPrefix(namespace.getPrefix(), namespace.getUri());
        	mapUriToModel(namespace.getUri(), model);
        }

        for (M2Namespace importNamespace : m2Model.getImports())
        {
        	mapUriToModel(importNamespace.getUri(), model);
        }

		compiledModels.put(modelName, model);

		return modelName;
    }

	@Override
    public AspectDefinition getAspect(QName aspectName)
    {
		return getAspectImpl(aspectName);
    }

    protected AspectDefinition getAspectImpl(QName aspectName)
    {
    	AspectDefinition aspect = null;

        if(aspectName != null)
        {
            List<CompiledModel> models = uriToModels.get(aspectName.getNamespaceURI());
            if(models != null && models.size() > 0)
            {
	            for (CompiledModel model : models)
	            {
	                aspect = model.getAspect(aspectName);
	                if(aspect != null)
	                {
	                	break;
	                }
	            }
            }
        }

        return aspect;
    }

    @Override
    public AssociationDefinition getAssociation(QName assocName)
    {
    	return getAssociationImpl(assocName);
    }

    protected AssociationDefinition getAssociationImpl(QName assocName)
    {
    	AssociationDefinition assocDef = null;

        List<CompiledModel> models = getModelsForUri(assocName.getNamespaceURI());
        if(models != null && models.size() > 0)
        {
	        for (CompiledModel model : models)
	        {
	        	assocDef = model.getAssociation(assocName);
	        	if(assocDef != null)
	        	{
	        		break;
	        	}
	        }
        }

        return assocDef;
    }

    @Override
    public ClassDefinition getClass(QName className)
    {
    	return getClassImpl(className);
    }

    protected ClassDefinition getClassImpl(QName className)
    {
    	ClassDefinition classDef = null;

        List<CompiledModel> models = getModelsForUri(className.getNamespaceURI());
        if(models != null && models.size() > 0)
        {
	        for (CompiledModel model : models)
	        {
	        	classDef = model.getClass(className);
	        	if (classDef != null)
	        	{
	        		break;
	        	}
	        }
        }

        return classDef;
    }

    @Override
    public PropertyDefinition getProperty(QName propertyName)
    {
    	return getPropertyImpl(propertyName);
    }

    protected PropertyDefinition getPropertyImpl(QName propertyName)
    {
    	PropertyDefinition propDef = null;

        List<CompiledModel> models = getModelsForUri(propertyName.getNamespaceURI());
        if(models != null && models.size() > 0)
        {
	        for (CompiledModel model : models)
	        {
	        	propDef = model.getProperty(propertyName);
	        	if(propDef != null)
	        	{
	        		break;
	        	}
	        }
        }

        return propDef;
    }
    
    @Override
    public TypeDefinition getType(QName typeName)
    {
    	return getTypeImpl(typeName);
    }

    protected TypeDefinition getTypeImpl(QName typeName)
    {
    	TypeDefinition typeDef = null;

        if (typeName != null)
        {
            List<CompiledModel> models = getModelsForUri(typeName.getNamespaceURI());
            if(models != null && models.size() > 0)
            {
	            for (CompiledModel model : models)
	            {
	            	typeDef = model.getType(typeName);
	                if(typeDef != null)
	                {
	                    break;
	                }
	            }
            }
        }

        return typeDef;
    }
    
    @Override
    public ConstraintDefinition getConstraint(QName constraintQName)
    {
    	return getConstraintImpl(constraintQName);
    }

    protected ConstraintDefinition getConstraintImpl(QName constraintQName)
    {
    	ConstraintDefinition constraintDef = null;

        List<CompiledModel> models = getModelsForUri(constraintQName.getNamespaceURI());
        if(models != null && models.size() > 0)
        {
	        for (CompiledModel model : models)
	        {
	        	constraintDef = model.getConstraint(constraintQName);
	        	if(constraintDef != null)
	        	{
	        		break;
	        	}
	        }
        }

        return constraintDef;
    }

    @Override
    public DataTypeDefinition getDataType(QName typeName)
    {
    	return getDataTypeImp(typeName);
    }

    protected DataTypeDefinition getDataTypeImp(QName typeName)
    {
    	DataTypeDefinition dataType = null;

    	if (typeName != null)
    	{
	        List<CompiledModel> models = getModelsForUri(typeName.getNamespaceURI());
	        if(models != null && models.size() > 0)
	        {
		        for (CompiledModel model : models)
		        {
		        	dataType = model.getDataType(typeName);
		        	if(dataType != null)
		        	{
		        		break;
		        	}
		        }
	        }
    	}

        return dataType;
    }

    @SuppressWarnings("rawtypes")
	@Override
    public DataTypeDefinition getDataType(Class javaClass)
    {
    	return getDataTypeImpl(javaClass);
    }

    @SuppressWarnings("rawtypes")
	protected DataTypeDefinition getDataTypeImpl(Class javaClass)
    {
    	DataTypeDefinition dataTypeDef = null;

    	if (javaClass != null)
    	{
            for (CompiledModel model : getCompiledModels(true).values())
            { 
                dataTypeDef = model.getDataType(javaClass);
                if(dataTypeDef != null)
                {
                    break;
                }
            }
    	}

        return dataTypeDef;
    }

    protected Map<String, String> getPrefixesCacheImpl()
    {
        return prefixesCache;
    }
    
    @Override
    public Map<String, String> getPrefixesCache()
    {
        return getPrefixesCacheImpl();
    }

    @Override
    public List<String> getUrisCache()
    {
        return getUrisCacheImpl();
    }
    
    protected List<String> getUrisCacheImpl()
    {
    	urisCacheRWLock.readLock().lock();
    	try
    	{
    		return new ArrayList<String>(urisCache);
        }
        finally
        {
        	urisCacheRWLock.readLock().unlock();
        }
    }

    @Override
    public Collection<String> getPrefixes(String URI)
    {
    	return getPrefixesImpl(URI);
    }

    protected Collection<String> getPrefixesImpl(String URI)
    {
        Collection<String> prefixes = new ArrayList<String>();

        for (String key : prefixesCache.keySet())
        {
            String uri = prefixesCache.get(key);
            if ((uri != null) && (uri.equals(URI)))
            {
            	prefixes.add(key);
            }
        }

        return prefixes;
    }

    @Override
    public void addURI(String uri)
    {
    	addURIImpl(uri);
    }

    protected void addURIImpl(String uri)
    {
        if(hasURI(uri))
        {
            throw new NamespaceException("URI " + uri + " has already been defined");
        }

        urisCacheRWLock.writeLock().lock();
        try
        {
        	urisCache.add(uri);
        }
        finally
        {
        	urisCacheRWLock.writeLock().unlock();
        }
    }

    @Override
    public boolean hasURI(String uri)
    {
    	urisCacheRWLock.readLock().lock();
    	try
    	{
    		return urisCache.contains(uri);
        }
        finally
        {
        	urisCacheRWLock.readLock().unlock();
        }
    }

    @Override
    public void addPrefix(String prefix, String uri)
    {
    	addPrefixImpl(prefix, uri);
    }

    @Override
    public boolean hasPrefix(String prefix)
    {
    	return prefixesCache.containsKey(prefix);
    }

    protected void addPrefixImpl(String prefix, String uri)
    {
    	urisCacheRWLock.readLock().lock();
    	try
    	{
		    if(!urisCache.contains(uri))
		    {
		        throw new NamespaceException("Namespace URI " + uri + " does not exist");
		    }
		    if (prefixesCache.containsKey(prefix))
	        {
	            throw new NamespaceException(
	                    String.format("Namespace prefix '%s' is already in use for URI '%s' so cannot be registered for URI '%s'",
	                            prefix,
	                            prefixesCache.get(prefix),
	                            uri));
	        }
		    prefixesCache.put(prefix,  uri);
        }
        finally
        {
        	urisCacheRWLock.readLock().unlock();
        }
    }

    @Override
    public void removeURI(String uri)
    {
    	removeURIImpl(uri);
    }

    @Override
    public void removePrefix(String prefix)
    {
        removePrefixImpl(prefix);
    }

    protected boolean removeURIImpl(String uri)
    {
    	urisCacheRWLock.writeLock().lock();
    	try
    	{
    		return urisCache.remove(uri);
        }
        finally
        {
        	urisCacheRWLock.writeLock().unlock();
        }
    }

    protected boolean removePrefixImpl(String prefix)
    {
        return (prefixesCache.remove(prefix) != null);
    }

    @Override
    public Collection<QName> getTypes(boolean includeInherited)
    {
	    Collection<QName> types = new ArrayList<QName>(100);
	    for (QName model : getCompiledModels(includeInherited).keySet())
	    {
	    	for(TypeDefinition typeDef : getModel(model).getTypes())
	    	{
	    		types.add(typeDef.getName());
	    	}
	    }
	    return types;
    }
    
	@Override
	public Collection<QName> getAssociations(boolean includeInherited)
	{
	    Collection<QName> types = new ArrayList<QName>(100);
	    for (QName model : getCompiledModels(includeInherited).keySet())
	    {
	    	for(AssociationDefinition assocDef : getModel(model).getAssociations())
	    	{
	    		types.add(assocDef.getName());
	    	}
	    }
	    return types;
	}

	@Override
	public Collection<QName> getAspects(boolean includeInherited)
	{
	    Collection<QName> types = new ArrayList<QName>(100);
	    for (QName model : getCompiledModels(includeInherited).keySet())
	    {
	    	for(AspectDefinition aspectDef : getModel(model).getAspects())
	    	{
	    		types.add(aspectDef.getName());
	    	}
	    }
	    return types;
	}

    protected abstract void initImpl();

    @Override
    public void init()
    {
    	initImpl();
    }

    protected abstract void removeImpl();

	@Override
	public void remove()
	{
	    uriToModels.clear();
	    compiledModels.clear();
	    urisCacheRWLock.writeLock().lock();
	    try
	    {
	    	urisCache.clear();
        }
        finally
        {
        	urisCacheRWLock.writeLock().unlock();
        }
	    prefixesCache.clear();

		removeImpl();
	}

    @Override
    public boolean isModelInherited(QName modelName)
    {
    	CompiledModel model = compiledModels.get(modelName);
    	return (model != null);
    }

    @Override
    public String getNamespaceURI(String prefix)
    {
    	String namespaceURI = null;

    	if(prefix != null)
    	{
    		namespaceURI = getPrefixesCache().get(prefix);
    	}

    	return namespaceURI;
    }
}
