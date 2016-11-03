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
package org.alfresco.service.cmr.dictionary;

import java.util.Collection;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;


/**
 * This interface represents the Repository Data Dictionary.  The
 * dictionary provides access to content meta-data such as Type
 * and Aspect descriptions.
 * <p>
 * Content meta-data is organised into models where each model is
 * given a qualified name.  This means that it is safe to develop
 * independent models and bring them together into the same
 * Repository without name clashes (as long their namespace is
 * different). 
 * 
 * @author David Caruana
 */
@AlfrescoPublicApi
public interface DictionaryService extends MessageLookup
{

    /**
     * @return the names of all models that have been registered with the Repository
     */
    @NotAuditable
    public Collection<QName> getAllModels();

    /**
     * @param model the model name to retrieve
     * @return the specified model (never null)
     * @throws DictionaryException if the model could not be found
     */
    @NotAuditable
    public ModelDefinition getModel(QName model);

    /**
     * @return the names of all data types that have been registered with the Repository
     */
    @NotAuditable
    Collection<QName> getAllDataTypes();

    /**
     * @param model the model to retrieve data types for
     * @return the names of all data types defined within the specified model
     */
    @NotAuditable
    Collection<QName> getDataTypes(QName model);
    
    /**
     * @param name the name of the data type to retrieve
     * @return the data type definition (or null, if it doesn't exist)
     */
    @NotAuditable
    DataTypeDefinition getDataType(QName name);
    
    /**
     * @param javaClass  java class to find datatype for
     * @return  the data type definition (or null, if a mapping does not exist) 
     */
    @NotAuditable
    DataTypeDefinition getDataType(Class<?> javaClass);

    /**
     * @return the names of all types that have been registered with the Repository
     */
    @NotAuditable
    Collection<QName> getAllTypes();

    @NotAuditable
    Collection<QName> getAllTypes(boolean includeInherited);

    /**
     * Get the sub types of the type.   The returned list includes the base type which is passed in as a parameter.
     * 
     * @param type the qualified name of the type
     * @param follow  true => all sub-type descendants, false => immediate sub-type children
     * @return the names of the sub types of the specified type, including the value passed in.
     */
    @NotAuditable
    Collection<QName> getSubTypes(QName type, boolean follow);
   
    /**
     * @param model the model to retrieve types for
     * @return the names of all types defined within the specified model
     */
    @NotAuditable
    Collection<QName> getTypes(QName model);

    /**
     * @param name the name of the type to retrieve
     * @return the type definition (or null, if it doesn't exist)
     */
    @NotAuditable
    TypeDefinition getType(QName name);

    /**
     * Construct an anonymous type that combines the definitions of the specified
     * type and aspects.
     *
     * @param type the type to start with 
     * @param aspects the aspects to combine with the type
     * @return the anonymous type definition
     */
    @NotAuditable
    TypeDefinition getAnonymousType(QName type, Collection<QName> aspects);

    /**
     * Creates an anonymous {@link TypeDefinition} with all the mandatory {@link Aspect Aspects} applied.
     * This collapses all mandatory {@link Aspect Aspects} into a single {@link TypeDefinition}.
     * 
     * @param name  the name of the type definition.
     * @return  the anonymous type definition
     */
    TypeDefinition getAnonymousType(QName name);

    /**
     * @return the names of all aspects that have been registered with the Repository
     */
    @NotAuditable
    Collection<QName> getAllAspects();
    
    @NotAuditable
    Collection<QName> getAllAspects(boolean includeInherited);

    /**
     * @param aspect QName
     * @param follow  true => follow up the super-class hierarchy, false => immediate sub aspects only
     * @return the sub aspects of specified aspect
     */
    @NotAuditable
    Collection<QName> getSubAspects(QName aspect, boolean follow);
    
    /**
     * @param model the model to retrieve aspects for
     * @return the names of all aspects defined within the specified model
     */
    @NotAuditable
    Collection<QName> getAspects(QName model);

    /**
     * @param model the model to retrieve associations for
     * @return the names of all associations defined within the specified model
     */
    @NotAuditable
    public Collection<QName> getAssociations(QName model);
    
    /**
     * @param name the name of the aspect to retrieve
     * @return the aspect definition (or null, if it doesn't exist)
     */
    @NotAuditable
    AspectDefinition getAspect(QName name);

    /**
     * @param name the name of the class (type or aspect) to retrieve
     * @return the class definition (or null, if it doesn't exist)
     */
    @NotAuditable
    ClassDefinition getClass(QName name);
    
    /**
     * Determines whether a class is a sub-class of another class
     * 
     * @param className the sub-class to test
     * @param ofClassName the class to test against
     * @return true => the class is a sub-class (or itself)
     */
    @NotAuditable
    boolean isSubClass(QName className, QName ofClassName);

    /**
     * Gets the definition of the property as defined by the specified Class.
     * 
     * Note: A sub-class may override the definition of a property that's 
     *       defined in a super-class.
     * 
     * @param className the class name
     * @param propertyName the property name
     * @return the property definition (or null, if it doesn't exist)
     */
    @NotAuditable
    PropertyDefinition getProperty(QName className, QName propertyName);
    
    /**
     * Gets the definitions of the properties defined by the specified Class.
     * 
     * @param className the class name
     * @return the property definitions
     */
    @NotAuditable
    Map<QName,PropertyDefinition> getPropertyDefs(QName className);

    /**
     * Gets the definition of the property as defined by its owning Class.
     * 
     * @param propertyName the property name
     * @return the property definition (or null, if it doesn't exist)
     */
    @NotAuditable
    PropertyDefinition getProperty(QName propertyName);

    /**
     * Get all properties defined across all models with the given data type.
     * 
     * Note that DataTypeDefinition.ANY will only match this type and can not be used as get all properties.
     * 
     * If dataType is null then this method will return *ALL* properties regardless of data type.
     * 
     * @param dataType QName
     */
    @NotAuditable
    Collection<QName> getAllProperties(QName dataType);
    
    /**
     * Get all properties defined for the given model with the given data type.
     * 
     * Note that DataTypeDefinition.ANY will only match this type and can not be used as get all properties.
     * 
     * If dataType is null then this method will return *ALL* properties regardless of data type.
     * 
     * @param model QName
     * @param dataType QName
     */
    @NotAuditable
    Collection<QName> getProperties(QName model, QName dataType);
    
    /**
     * Get all properties for the specified model
     * 
     * @param model QName
     */
    Collection<QName> getProperties(QName model);
    
    /**
     * Gets the definition of the association as defined by its owning Class.
     * 
     * @param associationName the property name
     * @return the association definition (or null, if it doesn't exist)
     */
    @NotAuditable
    AssociationDefinition getAssociation(QName associationName);
    
    /**
     * Get all the association definitions
    
     * @return all the association qnames
     */
    @NotAuditable
    Collection<QName> getAllAssociations();
    
    @NotAuditable
    Collection<QName> getAllAssociations(boolean includeInherited);

    /**
     * Gets the definition of the constraint
     * 
     * @param constraintQName the constraint name
     * @return the constraint definition (or null, if it doesn't exist)
     * 
     * @since 3.2.1
     */
    @NotAuditable
    public ConstraintDefinition getConstraint(QName constraintQName);
    
    /**
     * Get constraints for the specified model
     * 
     * @param model QName
     */
    public Collection<ConstraintDefinition> getConstraints(QName model);
    
    /**
     * Get constraints for the specified model
     
     * Optionally return referenceable (ie. non-property specific) constraints only
     * 
     * @param model QName
     * @param referenceableDefsOnly boolean
     *
     * @since 3.2R
     */
    Collection<ConstraintDefinition> getConstraints(QName model, boolean referenceableDefsOnly);
    
    /**
     * @param uri the namespace uri for search for
     * @return the named model definition
     */
    ModelDefinition getModelByNamespaceUri(String uri);
}
