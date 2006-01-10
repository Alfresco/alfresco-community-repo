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

import java.util.Collection;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
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
    
}
