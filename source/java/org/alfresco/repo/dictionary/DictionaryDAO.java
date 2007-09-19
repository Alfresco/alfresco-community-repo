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
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
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
     * @param superType
     * @param follow  true => follow up the super-class hierarchy, false => immediate sub types only
     * @return
     */
    public Collection<QName> getSubTypes(QName superType, boolean follow);

    /**
     * @param model the model to retrieve aspects for
     * @return the aspects of the model
     */
    public Collection<AspectDefinition> getAspects(QName model);
    
    /**
     * @param superAspect
     * @param follow  true => follow up the super-class hierarchy, false => immediate sub aspects only
     * @return
     */
    public Collection<QName> getSubAspects(QName superAspect, boolean follow);
       
    /**
     * @param model the model for which to get properties for
     * @return the properties of the model
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
     * @return QName name of model
     */
    public QName putModel(M2Model model);
    
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
     
    /**
     * @param model the model to retrieve namespaces for
     * @return the namespaces of the model
     */
    public Collection<NamespaceDefinition> getNamespaces(QName modelName);
    
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
    public void validateModel(M2Model newOrUpdatedModel);
    
    /**
     *
     * Register with the Dictionary
     * 
     * @param dictionaryDeployer
     */
    public void register(DictionaryDeployer dictionaryDeployer);
    
    /**
     * Reset the Dictionary - destroy & re-initialise
     */
    public void reset();
    
    /**
     * Initialise the Dictionary
     */
    public void init();
    
    /**
     * Destroy the Dictionary
     */
    public void destroy();
}
