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
package org.alfresco.service.cmr.dictionary;

import java.util.Collection;

import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.namespace.QName;


/**
 * This interface represents the Repository Data Dictionary.  The
 * dictionary provides access to content meta-data such as Type
 * and Aspect descriptions.
 *
 * Content meta-data is organised into models where each model is
 * given a qualified name.  This means that it is safe to develop
 * independent models and bring them together into the same
 * Repository without name clashes (as long their namespace is
 * different). 
 * 
 * @author David Caruana
 */
@PublicService
public interface DictionaryService
{

    /**
     * @return the names of all models that have been registered with the Repository
     */
    @NotAuditable
    public Collection<QName> getAllModels();
    
    /**
     * @param model the model name to retrieve
     * @return the specified model (or null, if it doesn't exist)
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
    DataTypeDefinition getDataType(Class javaClass);

    /**
     * @return the names of all types that have been registered with the Repository
     */
    @NotAuditable
    Collection<QName> getAllTypes();
    
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
     * @return the names of all aspects that have been registered with the Repository
     */
    @NotAuditable
    Collection<QName> getAllAspects();
    
    /**
     * @param model the model to retrieve aspects for
     * @return the names of all aspects defined within the specified model
     */
    @NotAuditable
    Collection<QName> getAspects(QName model);

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
     * @param dataType
     * @return
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
     * @param dataType
     * @return
     */
    @NotAuditable
    Collection<QName> getProperties(QName model, QName dataType);
    
    /**
     * Get all poroperties for the specified model
     * 
     * @param model
     * @return
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
    
    // TODO: Behaviour definitions
    
}
