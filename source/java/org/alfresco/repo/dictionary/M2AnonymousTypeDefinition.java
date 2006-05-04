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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Compiled anonymous type definition.
 * 
 * @author David Caruana
 *
 */
/*package*/ class M2AnonymousTypeDefinition implements TypeDefinition
{
    private TypeDefinition type;
    private Map<QName,PropertyDefinition> properties = new HashMap<QName,PropertyDefinition>();
    private Map<QName,AssociationDefinition> associations = new HashMap<QName,AssociationDefinition>();
    private Map<QName,ChildAssociationDefinition> childassociations = new HashMap<QName,ChildAssociationDefinition>();
    

    /**
     * Construct
     * 
     * @param type  the primary type
     * @param aspects  the aspects to combine with the type
     */
    /*package*/ M2AnonymousTypeDefinition(TypeDefinition type, Collection<AspectDefinition> aspects)
    {
        this.type = type;
        
        // Combine features of type and aspects
        properties.putAll(type.getProperties());
        associations.putAll(type.getAssociations());
        childassociations.putAll(type.getChildAssociations());
        for (AspectDefinition aspect : aspects)
        {
            properties.putAll(aspect.getProperties());
            associations.putAll(aspect.getAssociations());
            childassociations.putAll(aspect.getChildAssociations());
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.ClassDefinition#getModel()
     */
    public ModelDefinition getModel()
    {
        return type.getModel();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.TypeDefinition#getDefaultAspects()
     */
    public List<AspectDefinition> getDefaultAspects()
    {
        return type.getDefaultAspects();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ClassDefinition#getName()
     */
    public QName getName()
    {
        return QName.createQName(NamespaceService.DICTIONARY_MODEL_1_0_URI, "anonymous#" + type.getName().getLocalName());
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ClassDefinition#getTitle()
     */
    public String getTitle()
    {
        return type.getTitle();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ClassDefinition#getDescription()
     */
    public String getDescription()
    {
        return type.getDescription();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ClassDefinition#getParentName()
     */
    public QName getParentName()
    {
        return type.getParentName();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ClassDefinition#isAspect()
     */
    public boolean isAspect()
    {
        return type.isAspect();
    }

    public boolean isArchive()
    {
        return type.isArchive();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ClassDefinition#getProperties()
     */
    public Map<QName, PropertyDefinition> getProperties()
    {
        return Collections.unmodifiableMap(properties);
    }
    
    /**
     * @see org.alfresco.service.cmr.dictionary.ClassDefinition#getDefaultValues()
     */
    public Map<QName, Serializable> getDefaultValues()
    {
        Map<QName, Serializable> result = new HashMap<QName, Serializable>(5);
        
        for(Map.Entry<QName, PropertyDefinition> entry : properties.entrySet())
        {
            PropertyDefinition propertyDefinition = entry.getValue();
            String defaultValue = propertyDefinition.getDefaultValue();
            if (defaultValue != null)
            {
                result.put(entry.getKey(), defaultValue);
            }
        }
        
        return Collections.unmodifiableMap(result);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ClassDefinition#getAssociations()
     */
    public Map<QName, AssociationDefinition> getAssociations()
    {
        return Collections.unmodifiableMap(associations);
    }


    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.ClassDefinition#isContainer()
     */
    public boolean isContainer()
    {
        return !childassociations.isEmpty();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ClassDefinition#getChildAssociations()
     */
    public Map<QName, ChildAssociationDefinition> getChildAssociations()
    {
        return Collections.unmodifiableMap(childassociations);
    }

}
