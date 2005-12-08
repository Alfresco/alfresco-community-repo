/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;


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

    // Map of namespace to model name
    private Map<String,QName> namespaceToModel = new HashMap<String,QName>();
    
    // Map of model name to compiled model
    private Map<QName,CompiledModel> compiledModels = new HashMap<QName,CompiledModel>();


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
                namespaceToModel.remove(namespace.getUri());
            }
        }
        
        // Create namespace definitions for new model
        for (M2Namespace namespace : model.getNamespaces())
        {
            namespaceDAO.addURI(namespace.getUri());
            namespaceDAO.addPrefix(namespace.getPrefix(), namespace.getUri());
            namespaceToModel.put(namespace.getUri(), modelName);
        }
        
        // Publish new Model Definition
        compiledModels.put(modelName, compiledModel);
    }

    
    /**
     * @param uri  the namespace uri
     * @return the compiled model which defines the specified namespace
     */
    private CompiledModel getCompiledModelForNamespace(String uri)
    {
        QName modelName = namespaceToModel.get(uri);
        return (modelName == null) ? null : getCompiledModel(modelName); 
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
            throw new DictionaryException("Model " + modelName + " does not exist");
        }
        return model;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getPropertyType(org.alfresco.repo.ref.QName)
     */
    public DataTypeDefinition getDataType(QName typeName)
    {
        CompiledModel model = getCompiledModelForNamespace(typeName.getNamespaceURI());
        return (model == null) ? null : model.getDataType(typeName);
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
        CompiledModel model = getCompiledModelForNamespace(typeName.getNamespaceURI());
        return (model == null) ? null : model.getType(typeName);
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getAspect(org.alfresco.repo.ref.QName)
     */
    public AspectDefinition getAspect(QName aspectName)
    {
        CompiledModel model = getCompiledModelForNamespace(aspectName.getNamespaceURI());
        return (model == null) ? null : model.getAspect(aspectName);
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getClass(org.alfresco.repo.ref.QName)
     */
    public ClassDefinition getClass(QName className)
    {
        CompiledModel model = getCompiledModelForNamespace(className.getNamespaceURI());
        return (model == null) ? null : model.getClass(className);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getProperty(org.alfresco.repo.ref.QName)
     */
    public PropertyDefinition getProperty(QName propertyName)
    {
        CompiledModel model = getCompiledModelForNamespace(propertyName.getNamespaceURI());
        return (model == null) ? null : model.getProperty(propertyName);
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getAssociation(org.alfresco.repo.ref.QName)
     */
    public AssociationDefinition getAssociation(QName assocName)
    {
        CompiledModel model = getCompiledModelForNamespace(assocName.getNamespaceURI());
        return (model == null) ? null : model.getAssociation(assocName);
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
            throw new DictionaryException("Failed to create anonymous type as specified type " + type + " not found");
        }
        Collection<AspectDefinition> aspectDefs = new ArrayList<AspectDefinition>();
        if (aspects != null)
        {
            for (QName aspect : aspects)
            {
                AspectDefinition aspectDef = getAspect(aspect);
                if (typeDef == null)
                {
                    throw new DictionaryException("Failed to create anonymous type as specified aspect " + aspect + " not found");
                }
                aspectDefs.add(aspectDef);
            }
        }
        return new M2AnonymousTypeDefinition(typeDef, aspectDefs);
    }


}
