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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Compiled representation of a model definition.
 * 
 * In this case, compiled means that
 * a) all references between model items have been resolved
 * b) inheritence of class features have been flattened
 * c) overridden class features have been resolved
 * 
 * A compiled model also represents a valid model.
 * 
 * @author David Caruana
 *
 */
/*package*/ class CompiledModel implements ModelQuery
{
    
    // Logger
    private static final Log logger = LogFactory.getLog(DictionaryDAOImpl.class);
    
    private M2Model model;
    private ModelDefinition modelDefinition;
    private Map<QName, DataTypeDefinition> dataTypes = new HashMap<QName, DataTypeDefinition>();
    private Map<QName, ClassDefinition> classes = new HashMap<QName, ClassDefinition>();
    private Map<QName, TypeDefinition> types = new HashMap<QName, TypeDefinition>();
    private Map<QName, AspectDefinition> aspects = new HashMap<QName, AspectDefinition>();
    private Map<QName, PropertyDefinition> properties = new HashMap<QName, PropertyDefinition>();
    private Map<QName, AssociationDefinition> associations = new HashMap<QName, AssociationDefinition>();
    private Map<QName, ConstraintDefinition> constraints = new HashMap<QName, ConstraintDefinition>();
    
    /**
     * Construct
     * 
     * @param model model definition 
     * @param dictionaryDAO dictionary DAO
     * @param namespaceDAO namespace DAO
     */
    /*package*/ CompiledModel(M2Model model, DictionaryDAO dictionaryDAO, NamespaceDAO namespaceDAO)
    {
        try
        {
            // Phase 1: Construct model definitions from model entries
            //          resolving qualified names
            this.model = model;
            constructDefinitions(model, namespaceDAO);
    
            // Phase 2: Resolve dependencies between model definitions
            ModelQuery query = new DelegateModelQuery(this, dictionaryDAO);
            resolveDependencies(query, namespaceDAO);
            
            // Phase 3: Resolve inheritance of values within class hierachy
            NamespacePrefixResolver localPrefixes = createLocalPrefixResolver(model, namespaceDAO);
            resolveInheritance(query, localPrefixes, constraints);
            
            // Phase 4: Resolve constraint dependencies
            for (ConstraintDefinition def : constraints.values())
            {
                ((M2ConstraintDefinition)def).resolveDependencies(query);
            }
        }
        catch(Exception e)
        {
            throw new DictionaryException("Failed to compile model " + model.getName(), e);
        }
    }

    
    /**
     * @return the model definition
     */
    /*package*/ M2Model getM2Model()
    {
        return model;
    }
    
    
    /**
     * Construct compiled definitions
     * 
     * @param model model definition
     * @param namespaceDAO namespace DAO
     */
    private void constructDefinitions(M2Model model, NamespaceDAO namespaceDAO)
    {
        NamespacePrefixResolver localPrefixes = createLocalPrefixResolver(model, namespaceDAO);
    
        // Construct Model Definition
        modelDefinition = new M2ModelDefinition(model, localPrefixes);
        
        // Construct Property Types
        for (M2DataType propType : model.getPropertyTypes())
        {
            M2DataTypeDefinition def = new M2DataTypeDefinition(modelDefinition, propType, localPrefixes);
            if (dataTypes.containsKey(def.getName()))
            {
                throw new DictionaryException("Found duplicate property type definition " + propType.getName());
            }
            dataTypes.put(def.getName(), def);
        }
        
        // Construct Type Definitions
        for (M2Type type : model.getTypes())
        {
            M2TypeDefinition def = new M2TypeDefinition(modelDefinition, type, localPrefixes, properties, associations);
            if (classes.containsKey(def.getName()))
            {
                throw new DictionaryException("Found duplicate class definition " + type.getName() + " (a type)");
            }
            classes.put(def.getName(), def);
            types.put(def.getName(), def);
        }
        
        // Construct Aspect Definitions
        for (M2Aspect aspect : model.getAspects())
        {
            M2AspectDefinition def = new M2AspectDefinition(modelDefinition, aspect, localPrefixes, properties, associations);
            if (classes.containsKey(def.getName()))
            {
                throw new DictionaryException("Found duplicate class definition " + aspect.getName() + " (an aspect)");
            }
            classes.put(def.getName(), def);
            aspects.put(def.getName(), def);
        }
        
        // Construct Constraint Definitions
        for (M2Constraint constraint : model.getConstraints())
        {
            M2ConstraintDefinition def = new M2ConstraintDefinition(modelDefinition, null, constraint, localPrefixes);
            QName qname = def.getName();
            if (constraints.containsKey(qname))
            {
                throw new DictionaryException("Found duplicate constraint definition " + constraint.getName() + " (an aspect)");
            }
            constraints.put(qname, def);
        }
    }    
    
    
    /**
     * Create a local namespace prefix resolver containing the namespaces defined and imported
     * in the model
     * 
     * @param model model definition
     * @param namespaceDAO  namespace DAO
     * @return the local namespace prefix resolver
     */
    private NamespacePrefixResolver createLocalPrefixResolver(M2Model model, NamespaceDAO namespaceDAO)
    {
        // Retrieve set of existing URIs for validation purposes
        Collection<String> uris = namespaceDAO.getURIs();
        
        // Create a namespace prefix resolver based on imported and defined
        // namespaces within the model
        DynamicNamespacePrefixResolver prefixResolver = new DynamicNamespacePrefixResolver(null);
        for (M2Namespace imported : model.getImports())
        {
            String uri = imported.getUri();
            if (!uris.contains(uri))
            {
                throw new NamespaceException("URI " + uri + " cannot be imported as it is not defined (with prefix " + imported.getPrefix());
            }
            prefixResolver.registerNamespace(imported.getPrefix(), uri);
        }
        for (M2Namespace defined : model.getNamespaces())
        {
            prefixResolver.registerNamespace(defined.getPrefix(), defined.getUri());
        }
        return prefixResolver;
    }
    

    /**
     * Resolve dependencies between model items
     * 
     * @param query support for querying other items in model
     */
    private void resolveDependencies(ModelQuery query, NamespaceDAO namespaceDAO)
    {
        NamespacePrefixResolver prefixResolver = createLocalPrefixResolver(model, namespaceDAO);
        
        for (DataTypeDefinition def : dataTypes.values())
        {
            ((M2DataTypeDefinition)def).resolveDependencies(query);
        }
        for (ClassDefinition def : classes.values())
        {
            ((M2ClassDefinition)def).resolveDependencies(query, prefixResolver, constraints);
        }
    }
        

    /**
     * Resolve class feature inheritence
     * 
     * @param query support for querying other items in model
     */
    private void resolveInheritance(
            ModelQuery query,
            NamespacePrefixResolver prefixResolver,
            Map<QName, ConstraintDefinition> modelConstraints)
    {
        // Calculate order of class processing (root to leaf)
        Map<Integer,List<ClassDefinition>> order = new TreeMap<Integer,List<ClassDefinition>>();
        for (ClassDefinition def : classes.values())
        {
            // Calculate class depth in hierarchy
            int depth = 0;
            QName parentName = def.getParentName();
            while (parentName != null)
            {
                ClassDefinition parentClass = getClass(parentName);
                if (parentClass == null)
                {
                    break;
                }
                depth = depth +1;
                parentName = parentClass.getParentName();
            }

            // Map class to depth
            List<ClassDefinition> classes = order.get(depth);
            if (classes == null)
            {
                classes = new ArrayList<ClassDefinition>();
                order.put(depth, classes);
            }
            classes.add(def);
            
            if (logger.isDebugEnabled())
                logger.debug("Resolving inheritance: class " + def.getName() + " found at depth " + depth);
        }
        
        // Resolve inheritance of each class
        for (int depth = 0; depth < order.size(); depth++)
        {
            for (ClassDefinition def : order.get(depth))
            {
                ((M2ClassDefinition)def).resolveInheritance(query, prefixResolver, modelConstraints);
            }
        }
    }
        
    
    /**
     * @return  the compiled model definition
     */
    public ModelDefinition getModelDefinition()
    {
        return modelDefinition;
    }
    
    
    /**
     * @return  the compiled property types
     */
    public Collection<DataTypeDefinition> getDataTypes()
    {
        return dataTypes.values();
    }


    /**
     * @return the compiled types
     */
    public Collection<TypeDefinition> getTypes()
    {
        return types.values();
    }
    

    /**
     * @return the compiled aspects
     */
    public Collection<AspectDefinition> getAspects()
    {
        return aspects.values();
    }
    
    /**
     * 
     * @return the compiled properties
     */
    public Collection<PropertyDefinition> getProperties()
    {
        return properties.values();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getPropertyType(org.alfresco.repo.ref.QName)
     */
    public DataTypeDefinition getDataType(QName name)
    {
        return dataTypes.get(name);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelQuery#getDataType(java.lang.Class)
     */
    public DataTypeDefinition getDataType(Class javaClass)
    {
        for (DataTypeDefinition dataTypeDef : dataTypes.values())
        {
            if (dataTypeDef.getJavaClassName().equals(javaClass.getName()))
            {
                return dataTypeDef;
            }
        }
        return null;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getType(org.alfresco.repo.ref.QName)
     */
    public TypeDefinition getType(QName name)
    {
        return types.get(name);
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getAspect(org.alfresco.repo.ref.QName)
     */
    public AspectDefinition getAspect(QName name)
    {
        return aspects.get(name);
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getClass(org.alfresco.repo.ref.QName)
     */
    public ClassDefinition getClass(QName name)
    {
        return classes.get(name);
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getProperty(org.alfresco.repo.ref.QName)
     */
    public PropertyDefinition getProperty(QName name)
    {
        return properties.get(name);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getAssociation(org.alfresco.repo.ref.QName)
     */
    public AssociationDefinition getAssociation(QName name)
    {
        return associations.get(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getConstraint(QName)
     */
    public ConstraintDefinition getConstraint(QName name)
    {
        return constraints.get(name);
    }
}
