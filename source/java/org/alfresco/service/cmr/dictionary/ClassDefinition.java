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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * Read-only definition of a Class.
 * 
 * @author David Caruana
 */
public interface ClassDefinition
{
    /**
     * @return  defining model
     */
    public ModelDefinition getModel();

    /**
     * @return the qualified name of the class
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
     * @return  the super class (or null, if this is the root)
     */
    public QName getParentName();
    
    /**
     * @return true => aspect, false => type
     */
    public boolean isAspect();

    /**
     * @return the properties of the class, including inherited properties
     */
    public Map<QName, PropertyDefinition> getProperties();
    
    /**
     * @return a map containing the default property values, including inherited properties
     */
    public Map<QName, Serializable> getDefaultValues();
    
    /**
     * Fetch all associations for which this is a source type, including child associations.
     * 
     * @return the associations including inherited ones
     * @see ChildAssociationDefinition
     */
    public Map<QName, AssociationDefinition> getAssociations();
    
    /**
     * @return true => this class supports child associations
     */
    public boolean isContainer();
    
    /**
     * Fetch only child associations for which this is a source type.
     *
     * @return all child associations applicable to this type, including those
     *         inherited from super types
     */
    public Map<QName, ChildAssociationDefinition> getChildAssociations();

    /**
     * Fetch all associations for which this is a target type, including child associations.
     * 
     * @return the associations including inherited ones
     */
    // TODO: public Map<QName, AssociationDefinition> getTargetAssociations();
    
    /**
     * @return  the default aspects associated with this type
     */
    public List<AspectDefinition> getDefaultAspects();
    
}
