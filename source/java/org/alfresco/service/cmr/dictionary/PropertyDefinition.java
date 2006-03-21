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
package org.alfresco.service.cmr.dictionary;

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * Read-only definition of a Property.
 * 
 * @author David Caruana
 */
public interface PropertyDefinition
{
    /**
     * @return defining model 
     */
    public ModelDefinition getModel();
    
    /**
     * @return the qualified name of the property
     */
    public QName getName();

    /**
     * @return the human-readable class title 
     */
    public String getTitle();
    
    /**
     * @return the human-readable class description 
     */
    public String getDescription();
    
    /**
     * @return the default value 
     */
    public String getDefaultValue();
    
    /**
     * @return the qualified name of the property type
     */
    public DataTypeDefinition getDataType();

    /**
     * @return Returns the owning class's defintion
     */    
    public ClassDefinition getContainerClass();
    
    /**
     * @return  true => multi-valued, false => single-valued  
     */
    public boolean isMultiValued();

    /**
     * @return  true => mandatory, false => optional
     */
    public boolean isMandatory();
    
    /**
     * @return Returns true if the system enforces the presence of
     *      {@link #isMandatory() mandatory} properties, or false if the system
     *      just marks objects that don't have all mandatory properties present.  
     */
    public boolean isMandatoryEnforced();
    
    /**
     * @return  true => system maintained, false => client may maintain 
     */
    public boolean isProtected();

    /**
     * @return  true => indexed, false => not indexed
     */
    public boolean isIndexed();
    
    /**
     * @return  true => stored in index
     */
    public boolean isStoredInIndex();

    /**
     * @return true => tokenised when it is indexed (the stored value will not be tokenised)
     */
    public boolean isTokenisedInIndex();
    
    /**
     * All non atomic properties will be indexed at the same time.
     *
     * @return true => The attribute must be indexed in the commit of the transaction. 
     * false => the indexing will be done in the background and may be out of date.
     */
    public boolean isIndexedAtomically();
    
    /**
     * Get all constraints that apply to the property value
     * 
     * @return Returns a list of property constraint definitions
     */
    public List<ConstraintDefinition> getConstraints();
}
