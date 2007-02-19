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

import java.util.Collection;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;


/**
 * Dictionary Data Access
 * 
 * @author David Caruana
 */
public interface DictionaryDAO extends ModelQuery
{
 
    /**
     * @return the models known by the dictionary
     */
    public Collection<QName> getModels();
    
    /**
     * @param name the model to retrieve
     * @return the named model definition
     */
    public ModelDefinition getModel(QName name);
    
    /**
     * @param model the model to retrieve property types for
     * @return the property types of the model
     */
    public Collection<DataTypeDefinition> getDataTypes(QName model);
    
    /**
     * @param model the model to retrieve types for
     * @return the types of the model
     */
    public Collection<TypeDefinition> getTypes(QName model);

    /**
     * @param model the model to retrieve aspects for
     * @return the aspects of the model
     */
    public Collection<AspectDefinition> getAspects(QName model);
    
    
    /**
     * 
     * @param model the model for which to get properties
     * @return
     */
    public Collection<PropertyDefinition> getProperties(QName model);

    /**
     * Construct an anonymous type that combines a primary type definition and
     * and one or more aspects
     * 
     * @param type the primary type
     * @param aspects  the aspects to combine
     * @return the anonymous type definition
     */
    public TypeDefinition getAnonymousType(QName type, Collection<QName> aspects);
    
    /**
     * Adds a model to the dictionary.  The model is compiled and validated.
     * 
     * @param model the model to add
     */
    public void putModel(M2Model model);
    
    /**
     * Removes a model from the dictionary.  The types and aspect in the model will no longer be 
     * available.
     * 
     * @param model     the qname of the model to remove
     */
    public void removeModel(QName model);
    
    /**
     * Get all properties for the model and that are of the given data type.
     * If dataType is null then the all properties will be returned. 
     * 
     * @param modelName
     * @param dataType
     * @return
     */
    public Collection<PropertyDefinition> getProperties(QName modelName, QName dataType);
    
}
