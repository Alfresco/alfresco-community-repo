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
package org.alfresco.service.cmr.dictionary;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;

/**
 * Read-only definition of a Class.
 * 
 * @author David Caruana
 */
@AlfrescoPublicApi
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
     * @deprecated The problem identified in MNT-413 will still exist
     */
    public String getTitle();

    /**
     * @deprecated The problem identified in MNT-413 will still exist
     */
    public String getDescription();
    
    /**
     * @return the human-readable class title 
     */
    public String getTitle(MessageLookup messageLookup);
    
    /**
     * @return the human-readable class description 
     */
    public String getDescription(MessageLookup messageLookup);
    
    /**
     * @return  the super class (or null, if this is the root)
     */
    public QName getParentName();
    
    /**
     * @return true => aspect, false => type
     */
    public boolean isAspect();
    
    /**
     * @return Return <code>true</code> if the type should be archived on delete, <code>false</code> if it should be
     *         deleted or <code>null</code> if not defined.
     */
    public Boolean getArchive();
    
    /**
     * 
     * @return Return <code>true</code> if the type should be included in supertypes queries, <code>false</code> if not
     *         deleted or <code>null</code> if not defined.
     */
    public Boolean getIncludedInSuperTypeQuery();

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
    
    /**
     * @return  the set of default aspects names associated with this type
     */
    public Set<QName> getDefaultAspectNames();
    
    /**
     * @param inherited include default aspects inherited from super types
     * @return  the default aspects
     */
    public List<AspectDefinition> getDefaultAspects(boolean inherited);
    
    /**
     * Get the name of the property bundle that defines analyser mappings for this class (keyed by the type of the property) 
     * @return the resource or null if not set.
     */
    public String getAnalyserResourceBundleName();
    
    /**
     * Get the parent class definition
     * 
     * @return ClassDefinition
     */
    public ClassDefinition getParentClassDefinition();
    
}
