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

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;


/**
 * Compiled Association Definition.
 * 
 * @author David Caruana
 */
/*package*/ class M2AssociationDefinition implements AssociationDefinition
{

    private ClassDefinition classDef;
    private M2ClassAssociation assoc;
    private QName name;
    private QName targetClassName;
    private ClassDefinition targetClass;
    private QName sourceRoleName;
    private QName targetRoleName;
    
    
    /**
     * Construct
     * 
     * @param m2Association  association definition
     * @return  the definition
     */
    /*package*/ M2AssociationDefinition(ClassDefinition classDef, M2ClassAssociation assoc, NamespacePrefixResolver resolver)
    {
        this.classDef = classDef;
        this.assoc = assoc;
        
        // Resolve names
        this.name = QName.createQName(assoc.getName(), resolver);
        this.targetClassName = QName.createQName(assoc.getTargetClassName(), resolver);
        this.sourceRoleName = QName.createQName(assoc.getSourceRoleName(), resolver);
        this.targetRoleName = QName.createQName(assoc.getTargetRoleName(), resolver);
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(56);
        sb.append("Association")
          .append("[ class=").append(classDef)
          .append(", name=").append(name)
          .append(", target class=").append(targetClassName)
          .append(", source role=").append(sourceRoleName)
          .append(", target role=").append(targetRoleName)
          .append("]");
        return sb.toString();
    }

    
    /*package*/ M2ClassAssociation getM2Association()
    {
        return assoc;
    }


    /*package*/ void resolveDependencies(ModelQuery query)
    {
        if (targetClassName == null)
        {
            throw new DictionaryException("Target class of association " + name.toPrefixString() + " must be specified");
        }
        targetClass = query.getClass(targetClassName);
        if (targetClass == null)
        {
            throw new DictionaryException("Target class " + targetClassName.toPrefixString() + " of association " + name.toPrefixString() + " is not found");
        }
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.AssociationDefinition#getModel()
     */
    public ModelDefinition getModel()
    {
        return classDef.getModel();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#getName()
     */
    public QName getName()
    {
        return name;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#isChild()
     */
    public boolean isChild()
    {
        return (assoc instanceof M2ChildAssociation);
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#getTitle()
     */
    public String getTitle()
    {
        String value = M2Label.getLabel(classDef.getModel(), "association", name, "title"); 
        if (value == null)
        {
            value = assoc.getTitle();
        }
        return value;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#getDescription()
     */
    public String getDescription()
    {
        String value = M2Label.getLabel(classDef.getModel(), "association", name, "description"); 
        if (value == null)
        {
            value = assoc.getDescription();
        }
        return value;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#isProtected()
     */
    public boolean isProtected()
    {
        return assoc.isProtected();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#getSourceClass()
     */
    public ClassDefinition getSourceClass()
    {
        return classDef;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#getSourceRoleName()
     */
    public QName getSourceRoleName()
    {
        return sourceRoleName;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#isSourceMandatory()
     */
    public boolean isSourceMandatory()
    {
        return assoc.isSourceMandatory();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#isSourceMany()
     */
    public boolean isSourceMany()
    {
        return assoc.isSourceMany();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#getTargetClass()
     */
    public ClassDefinition getTargetClass()
    {
        return targetClass;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#getTargetRoleName()
     */
    public QName getTargetRoleName()
    {
        return targetRoleName;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#isTargetMandatory()
     */
    public boolean isTargetMandatory()
    {
        return assoc.isTargetMandatory();
    }


    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.AssociationDefinition#isTargetMandatoryEnforced()
     */
    public boolean isTargetMandatoryEnforced()
    {
        return assoc.isTargetMandatoryEnforced();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.AssociationDefinition#isTargetMany()
     */
    public boolean isTargetMany()
    {
        return assoc.isTargetMany();
    }

}
