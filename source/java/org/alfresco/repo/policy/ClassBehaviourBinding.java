/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.policy;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Behaviour binding to a Class (Type or Aspect) in the Content Model.
 * 
 * @author David Caruana
 *
 */
@AlfrescoPublicApi
public class ClassBehaviourBinding implements BehaviourBinding
{
    // The dictionary service
    private DictionaryService dictionary;
    
    // The class qualified name
    private QName classQName; 

    // Instance level node reference
    private NodeRef nodeRef;
    

    /**
     * Construct.
     * 
     * @param dictionary  the dictionary service
     * @param nodeRef  the instance level node reference
     * @param classQName  the Class qualified name
     */
    /*package*/ ClassBehaviourBinding(DictionaryService dictionary, NodeRef nodeRef, QName classQName)
    {
        this.dictionary = dictionary;
        this.nodeRef = nodeRef;
        this.classQName = classQName;
    }
    
    /**
     * Construct.
     * 
     * @param dictionary  the dictionary service
     * @param classQName  the Class qualified name
     */
    /*package*/ ClassBehaviourBinding(DictionaryService dictionary, QName classQName)
    {
        this(dictionary, null, classQName);
    }

    /*package*/ DictionaryService getDictionary()
    {
        return dictionary;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourBinding#generaliseBinding()
     */
    public BehaviourBinding generaliseBinding()
    {
        BehaviourBinding generalisedBinding = null;
        ClassDefinition classDefinition = dictionary.getClass(classQName);
        if (classDefinition == null)
        {
            // The class definition doesn't exist so there can be no behaviour bound
            return null;
        }
        
        QName parentClassName = classDefinition.getParentName();
        if (parentClassName != null)
        {
            generalisedBinding = new ClassBehaviourBinding(dictionary, parentClassName);
        }
        return generalisedBinding;
    }
    
    /**
     * Gets the instance level node reference
     * 
     * @return  the node reference
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    
    /**
     * Gets the class qualified name
     * 
     * @return  the class qualified name
     */
    public QName getClassQName()
    {
        return classQName;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof ClassBehaviourBinding))
        {
            return false;
        }
        return classQName.equals(((ClassBehaviourBinding)obj).classQName);
    }

    @Override
    public int hashCode()
    {
        return classQName.hashCode();
    }

    @Override
    public String toString()
    {
        return "ClassBinding[class=" + classQName + "]";
    }
    
}
