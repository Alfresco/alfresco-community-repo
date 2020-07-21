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

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;


/**
 * Read-only definition of an Association.
 *  
 * @author David Caruana
 *
 */
@AlfrescoPublicApi
public interface AssociationDefinition extends ClassAttributeDefinition
{
    
    /**
     * @return  defining model
     */
    public ModelDefinition getModel();
    
    /**
     * @return  the qualified name
     */
    public QName getName();

    /**
     * @deprecated The problem identified in MNT-413 will still exist
     * @see org.alfresco.service.cmr.dictionary.AssociationDefinition#getTitle(org.alfresco.service.cmr.i18n.MessageLookup)
     */
    public String getTitle();

    /**
     * @deprecated The problem identified in MNT-413 will still exist
     * @see org.alfresco.service.cmr.dictionary.AssociationDefinition#getDescription(org.alfresco.service.cmr.i18n.MessageLookup)
     */
    public String getDescription();

    /**
     * @return the human-readable title 
     */
    public String getTitle(MessageLookup messageLookup);
    
    /**
     * @return the human-readable description 
     */
    public String getDescription(MessageLookup messageLookup);
    
    /**
     * Is this a child association?
     * 
     * @return true => child,  false => general relationship
     */
    public boolean isChild();
    
    /**
     * Is this association maintained by the Repository?
     * 
     * @return true => system maintained, false => client may maintain 
     */
    public boolean isProtected();

    /**
     * @return the source class
     */
    public ClassDefinition getSourceClass();

    /**
     * @return the role of the source class in this association? 
     */
    public QName getSourceRoleName();
    
    /**
     * Is the source class optional in this association?
     *  
     * @return true => cardinality > 0
     */
    public boolean isSourceMandatory();

    /**
     * Can there be many source class instances in this association? 
     * 
     * @return true => cardinality > 1, false => cardinality of 0 or 1
     */
    public boolean isSourceMany();

    /**
     * @return the target class  
     */
    public ClassDefinition getTargetClass();
    
    /**
     * @return the role of the target class in this association? 
     */
    public QName getTargetRoleName();
    
    /**
     * Is the target class optional in this association?
     *  
     * @return true => cardinality > 0
     */
    public boolean isTargetMandatory();
    
    /**
     * Is the target class is mandatory, it is enforced?
     *  
     * @return true => enforced
     */
    public boolean isTargetMandatoryEnforced();

    /**
     * Can there be many target class instances in this association? 
     * 
     * @return true => cardinality > 1, false => cardinality of 0 or 1
     */
    public boolean isTargetMany();

}
