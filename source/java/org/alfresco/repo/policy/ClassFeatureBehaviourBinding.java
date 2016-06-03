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
public class ClassFeatureBehaviourBinding extends ClassBehaviourBinding
{
    // The feature qualified name (property or association)
    private QName featureQName;
    private QName activeFeatureQName;

    // Wild Card feature match (match all features)
    private static final QName ALL_FEATURES = QName.createQName("", "*");


    /**
     * Construct.
     * 
     * @param dictionary  the dictionary service
     * @param nodeRef  the node reference
     * @param classQName  the Class qualified name
     * @param featureQName  the Class feature (property or association) qualifed name
     */
    /*package*/ ClassFeatureBehaviourBinding(DictionaryService dictionary, NodeRef nodeRef, QName classQName, QName featureQName)
    {
        this(dictionary, nodeRef, classQName, featureQName, featureQName);
    }

    
    /**
     * Construct.
     * 
     * @param dictionary  the dictionary service
     * @param classQName  the Class qualified name
     * @param featureQName  the Class feature (property or association) qualifed name
     */
    /*package*/ ClassFeatureBehaviourBinding(DictionaryService dictionary, QName classQName, QName featureQName)
    {
        this(dictionary, null, classQName, featureQName, featureQName);
    }

    
    /**
     * Construct.
     * 
     * @param dictionary  the dictionary service
     * @param nodeRef  the node reference
     * @param classQName  the Class qualified name
     */
    /*package*/ ClassFeatureBehaviourBinding(DictionaryService dictionary, NodeRef nodeRef, QName classQName)
    {
        this(dictionary, nodeRef, classQName, ALL_FEATURES);
    }

    
    /**
     * Construct.
     * 
     * @param dictionary  the dictionary service
     * @param classQName  the Class qualified name
     */
    /*package*/ ClassFeatureBehaviourBinding(DictionaryService dictionary, QName classQName)
    {
        this(dictionary, null, classQName, ALL_FEATURES);
    }
        
    
    /**
     * Construct.
     * 
     * @param dictionary  the dictionary service
     * @param nodeRef  the node reference
     * @param classQName  the Class qualified name
     * @param featureQName  the Class feature (property or association) qualifed name
     * @param activeFeatureQName  the currently active feature QName
     */
    private ClassFeatureBehaviourBinding(DictionaryService dictionary, NodeRef nodeRef, QName classQName, QName featureQName, QName activeFeatureQName)
    {
        super(dictionary, nodeRef, classQName);
        this.featureQName = featureQName;
        this.activeFeatureQName = activeFeatureQName;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourBinding#generaliseBinding()
     */
    public BehaviourBinding generaliseBinding()
    {
        BehaviourBinding generalisedBinding = null;
        ClassDefinition classDefinition = getDictionary().getClass(getClassQName());
        
        if (classDefinition == null)
        {
            // The class definition doesn't exist so there can be no behaviour bound
            return null;
        }
        
        if (activeFeatureQName.equals(ALL_FEATURES))
        {
            QName parentClassName = classDefinition.getParentName();
            if (parentClassName != null)
            {
                generalisedBinding = new ClassFeatureBehaviourBinding(getDictionary(), getNodeRef(), parentClassName, featureQName, featureQName);
            }
        }
        else
        {
            generalisedBinding = new ClassFeatureBehaviourBinding(getDictionary(), getNodeRef(), getClassQName(), featureQName, ALL_FEATURES);
        }
        
        return generalisedBinding;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof ClassFeatureBehaviourBinding))
        {
            return false;
        }
        return getClassQName().equals(((ClassFeatureBehaviourBinding)obj).getClassQName()) &&
               activeFeatureQName.equals(((ClassFeatureBehaviourBinding)obj).activeFeatureQName);
    }

    @Override
    public int hashCode()
    {
        return 37 * getClassQName().hashCode() + activeFeatureQName.hashCode();
    }

    @Override
    public String toString()
    {
        return "ClassFeatureBinding[class=" + getClassQName() + ";feature=" + activeFeatureQName + "]";
    }
    
}
