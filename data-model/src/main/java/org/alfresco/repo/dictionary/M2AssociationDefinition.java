/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
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
    private transient MessageLookup staticMessageLookup = new StaticMessageLookup();
    
    
    /**
     * Construct
     * 
     * @param classDef  ClassDefinition
     * @param assoc  M2ClassAssociation
     * @param resolver  NamespacePrefixResolver
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
            throw new DictionaryException("d_dictionary.association.target_class_not_specified", name.toPrefixString());
        }
        targetClass = query.getClass(targetClassName);
        if (targetClass == null)
        {
            throw new DictionaryException("d_dictionary.association.target_class_not_found", targetClassName.toPrefixString(), name.toPrefixString());
        }
    }
    

    @Override
    public ModelDefinition getModel()
    {
        return classDef.getModel();
    }


    @Override
    public QName getName()
    {
        return name;
    }

    
    @Override
    public boolean isChild()
    {
        return (assoc instanceof M2ChildAssociation);
    }

    @Override
    public String getTitle()
    {
        return getTitle(staticMessageLookup);
    }

    @Override
    public String getDescription()
    {
        return getDescription(staticMessageLookup);
    }

    @Override
    public String getTitle(MessageLookup messageLookup)
    {
        String value = M2Label.getLabel(classDef.getModel(), messageLookup, "association", name, "title"); 
        if (value == null)
        {
            value = assoc.getTitle();
        }
        return value;
    }


    @Override
    public String getDescription(MessageLookup messageLookup)
    {
        String value = M2Label.getLabel(classDef.getModel(), messageLookup, "association", name, "description"); 
        if (value == null)
        {
            value = assoc.getDescription();
        }
        return value;
    }


    @Override
    public boolean isProtected()
    {
        return assoc.isProtected();
    }


    @Override
    public ClassDefinition getSourceClass()
    {
        return classDef;
    }


    @Override
    public QName getSourceRoleName()
    {
        return sourceRoleName;
    }


    @Override
    public boolean isSourceMandatory()
    {
        return assoc.isSourceMandatory();
    }


    @Override
    public boolean isSourceMany()
    {
        return assoc.isSourceMany();
    }


    @Override
    public ClassDefinition getTargetClass()
    {
        return targetClass;
    }


    @Override
    public QName getTargetRoleName()
    {
        return targetRoleName;
    }


    @Override
    public boolean isTargetMandatory()
    {
        return assoc.isTargetMandatory();
    }


    @Override
    public boolean isTargetMandatoryEnforced()
    {
        return assoc.isTargetMandatoryEnforced();
    }


    @Override
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
        if (! EqualsHelper.nullSafeEquals(getTitle(null), assocDef.getTitle(null), false))
        { 
            isUpdatedIncrementally = true;
        }
        
        // check description
        if (! EqualsHelper.nullSafeEquals(getDescription(null), assocDef.getDescription(null), false))
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
