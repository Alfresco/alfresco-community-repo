/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
import org.alfresco.util.EqualsHelper;


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
    
    /* package */ M2ModelDiff diffAssoc(AssociationDefinition assocDef)
    {
        M2ModelDiff modelDiff = null;
        boolean isUpdated = false;
        boolean isUpdatedIncrementally = false;
        
        if (this == assocDef)
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_ASSOCIATION, M2ModelDiff.DIFF_UNCHANGED);
            return modelDiff;
        }
        
        // check name - cannot be null
        if (! name.equals(assocDef.getName()))
        { 
            isUpdated = true;
        }
        
        // check title
        if (! EqualsHelper.nullSafeEquals(getTitle(), assocDef.getTitle(), false))
        { 
            isUpdatedIncrementally = true;
        }
        
        // check description
        if (! EqualsHelper.nullSafeEquals(getDescription(), assocDef.getDescription(), false))
        { 
            isUpdatedIncrementally = true;
        }
        
        // check source class qname
        if (! EqualsHelper.nullSafeEquals(getSourceClass().getName(), assocDef.getSourceClass().getName()))
        { 
            isUpdated = true;
        }
        
        // check source role qname
        if (! EqualsHelper.nullSafeEquals(getSourceRoleName(), assocDef.getSourceRoleName()))
        { 
            isUpdated = true;
        }
        
        // check target class qname
        if (! EqualsHelper.nullSafeEquals(getTargetClass().getName(), assocDef.getTargetClass().getName()))
        { 
            isUpdated = true;
        }
        
        // check target role qname
        if (! EqualsHelper.nullSafeEquals(getTargetRoleName(), assocDef.getTargetRoleName()))
        { 
            isUpdated = true;
        }
        
        // TODO - additional checks - is... (x7)
        
        if (isUpdated)
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_ASSOCIATION, M2ModelDiff.DIFF_UPDATED);
        }
        else if (isUpdatedIncrementally)
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_ASSOCIATION, M2ModelDiff.DIFF_UPDATED_INC);
        }
        else
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_ASSOCIATION, M2ModelDiff.DIFF_UNCHANGED);
        }
        
        return modelDiff;
    }
    
    /*package*/ static Collection<M2ModelDiff> diffAssocLists(Collection<AssociationDefinition> previousAssocs, Collection<AssociationDefinition> newAssocs)
    {
        List<M2ModelDiff> modelDiffs = new ArrayList<M2ModelDiff>();
        
        for (AssociationDefinition previousAssoc : previousAssocs)
        {
            boolean found = false;
            for (AssociationDefinition newAssoc : newAssocs)
            {
                if (newAssoc.getName().equals(previousAssoc.getName()))
                {
                    modelDiffs.add(((M2AssociationDefinition)previousAssoc).diffAssoc(newAssoc));
                    found = true;
                    break;
                }
            }
            
            if (! found)
            {
                modelDiffs.add(new M2ModelDiff(previousAssoc.getName(), M2ModelDiff.TYPE_ASSOCIATION, M2ModelDiff.DIFF_DELETED));
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
                modelDiffs.add(new M2ModelDiff(newAssoc.getName(), M2ModelDiff.TYPE_ASSOCIATION, M2ModelDiff.DIFF_CREATED));
            }
        }
        
        return modelDiffs;
    }

}
