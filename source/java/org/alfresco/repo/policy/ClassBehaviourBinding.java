/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.policy;

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
/*package*/ class ClassBehaviourBinding implements BehaviourBinding
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
        	throw new PolicyException("Class definition " + classQName.toPrefixString() + " does not exist.");
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
