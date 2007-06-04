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

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
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
    
    // Namespace Data Access
    private NamespaceDAO namespaceDAO;

    // Map of Namespace URI usages to Models
    private Map<String, List<CompiledModel>> uriToModels = new HashMap<String, List<CompiledModel>>();
    
    // Map of model name to compiled model
    private Map<QName,CompiledModel> compiledModels = new HashMap<QName,CompiledModel>();

    // Logger
    private static Log logger = LogFactory.getLog(DictionaryDAO.class);


    /**
     * Construct
     * 
     * @param namespaceDAO  namespace data access
     */
    public DictionaryDAOImpl(NamespaceDAO namespaceDAO)
    {
        this.namespaceDAO = namespaceDAO;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.DictionaryDAO#putModel(org.alfresco.repo.dictionary.impl.M2Model)
     */
    public void putModel(M2Model model)
    {
        // Compile model definition
        CompiledModel compiledModel = model.compile(this, namespaceDAO);
        QName modelName = compiledModel.getModelDefinition().getName();

        // Remove namespace definitions for previous model, if it exists
        CompiledModel previousVersion = compiledModels.get(modelName);
        if (previousVersion != null)
        {
            for (M2Namespace namespace : previousVersion.getM2Model().getNamespaces())
            {
                namespaceDAO.removePrefix(namespace.getPrefix());
                namespaceDAO.removeURI(namespace.getUri());
                unmapUriToModel(namespace.getUri(), previousVersion);
            }
            for (M2Namespace importNamespace : previousVersion.getM2Model().getImports())
            {
            	unmapUriToModel(importNamespace.getUri(), previousVersion);
            }
        }
        
        // Create namespace definitions for new model
        for (M2Namespace namespace : model.getNamespaces())
        {
            namespaceDAO.addURI(namespace.getUri());
            namespaceDAO.addPrefix(namespace.getPrefix(), namespace.getUri());
            mapUriToModel(namespace.getUri(), compiledModel);
        }
        for (M2Namespace importNamespace : model.getImports())
        {
        	mapUriToModel(importNamespace.getUri(), compiledModel);
        }
        
        // Publish new Model Definition
        compiledModels.put(modelName, compiledModel);

        if (logger.isInfoEnabled())
        {
            logger.info("Registered model " + modelName.toPrefixString(namespaceDAO));
            for (M2Namespace namespace : model.getNamespaces())
            {
                logger.info("Registered namespace '" + namespace.getUri() + "' (prefix '" + namespace.getPrefix() + "')");
            }
        }
    }
    

    /**
     * @see org.alfresco.repo.dictionary.DictionaryDAO#removeModel(org.alfresco.service.namespace.QName)
     */
    public void removeModel(QName modelName)
    {
        CompiledModel compiledModel = this.compiledModels.get(modelName);
        if (compiledModel != null)
        {
            // Remove the namespaces from the namespace service
            M2Model model = compiledModel.getM2Model();            
            for (M2Namespace namespace : model.getNamespaces())
            {
                namespaceDAO.removePrefix(namespace.getPrefix());
                namespaceDAO.removeURI(namespace.getUri());
                unmapUriToModel(namespace.getUri(), compiledModel);
            }
            
            // Remove the model from the list
            this.compiledModels.remove(modelName);
        }
    }


    /**
     * Map Namespace URI to Model
     * 
     * @param uri   namespace uri
     * @param model   model
     */
    private void mapUriToModel(String uri, CompiledModel model)
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

    
    /**
     * Unmap Namespace URI from Model
     * 
     * @param uri  namespace uri
     * @param model   model
     */
    private void unmapUriToModel(String uri, CompiledModel model)
    {
    	List<CompiledModel> models = uriToModels.get(uri);
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
    	List<CompiledModel> models = uriToModels.get(uri);
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
    private CompiledModel getCompiledModel(QName modelName)
    {
        CompiledModel model = compiledModels.get(modelName);
        if (model == null)
        {
            // TODO: Load model from persistent store 
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
    public DataTypeDefinition getDataType(Class javaClass)
    {
        for (CompiledModel model : compiledModels.values())
        {
            DataTypeDefinition dataTypeDef = model.getDataType(javaClass);
            if (dataTypeDef != null)
            {
                return dataTypeDef;
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
        return compiledModels.keySet();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.DictionaryDAO#getModel(org.alfresco.repo.ref.QName)
     */
    public ModelDefinition getModel(QName name)
    {
        CompiledModel model = getCompiledModel(name);
        if (model != null)
        {
            return model.getModelDefinition();
        }
        return null;
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

}
