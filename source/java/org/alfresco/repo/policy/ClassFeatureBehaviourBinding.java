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
/*package*/ class ClassFeatureBehaviourBinding extends ClassBehaviourBinding
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
