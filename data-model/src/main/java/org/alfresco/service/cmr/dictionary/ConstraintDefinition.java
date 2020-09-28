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
 * Property constraint definition
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface ConstraintDefinition
{
    /**
     * @return defining model 
     */
    public ModelDefinition getModel();
    
    /**
     * @return Returns the qualified name of the constraint
     */
    public QName getName();
    
    /**
     * @deprecated The problem identified in MNT-413 will still exist
     * @see org.alfresco.service.cmr.dictionary.ConstraintDefinition#getTitle(org.alfresco.service.cmr.i18n.MessageLookup)
     */
    public String getTitle();

    /**
     * @deprecated The problem identified in MNT-413 will still exist
     * @see org.alfresco.service.cmr.dictionary.ConstraintDefinition#getDescription(org.alfresco.service.cmr.i18n.MessageLookup)
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
     * @return Returns the constraint implementation
     */
    public Constraint getConstraint();
    
    /**
     * @return Returns the referenced constraint definition, if any (null for explicit or inline constraint def)
     * 
     * @since 3.2R
     */
    public QName getRef();
}
