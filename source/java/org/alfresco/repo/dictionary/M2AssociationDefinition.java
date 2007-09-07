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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    	// note: currently used for model 'diffs'
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
    
    /*package*/ static Collection<M2ModelDiff> diffAssocLists(Collection<AssociationDefinition> previousAssocs, Collection<AssociationDefinition> newAssocs)
    {
        List<M2ModelDiff> M2ModelDiffs = new ArrayList<M2ModelDiff>();
        
        for (AssociationDefinition previousAssoc : previousAssocs)
        {
            boolean found = false;
            for (AssociationDefinition newAssoc : newAssocs)
            {
                if (newAssoc.getName().equals(previousAssoc.getName()))
                {
                    // TODO currently uses toString() to check whether changed - could override equals()
                    if ((((M2AssociationDefinition)previousAssoc).toString()).equals(((M2AssociationDefinition)newAssoc).toString()))
                    {
                        M2ModelDiffs.add(new M2ModelDiff(newAssoc.getName(), M2ModelDiff.TYPE_ASSOCIATION, M2ModelDiff.DIFF_UNCHANGED));
                    }
                    else
                    {
                        M2ModelDiffs.add(new M2ModelDiff(newAssoc.getName(), M2ModelDiff.TYPE_ASSOCIATION, M2ModelDiff.DIFF_UPDATED));
                    }
                    found = true;
                    break;
                }
            }
            
            if (! found)
            {
                M2ModelDiffs.add(new M2ModelDiff(previousAssoc.getName(), M2ModelDiff.TYPE_ASSOCIATION, M2ModelDiff.DIFF_DELETED));
            }
        }
        
        for (AssociationDefinition newAssoc : newAssocs)
        {
            boolean found = false;
            for (AssociationDefinition previousAssoc : previousAssocs)
            {
                if (newAssoc.getName().equals(previousAssoc.getName()))
                {
                    found = true;
                    break;
                }
            }
            
            if (! found)
            {
                M2ModelDiffs.add(new M2ModelDiff(newAssoc.getName(), M2ModelDiff.TYPE_ASSOCIATION, M2ModelDiff.DIFF_CREATED));
            }                        
        }
        
        return M2ModelDiffs;
    }

}
