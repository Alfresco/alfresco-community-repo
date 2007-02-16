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
package org.alfresco.repo.dictionary;

import java.util.Map;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;


/**
 * Compiled Aspect Definition.
 * 
 * @author David Caruana
 */
/*package*/ class M2AspectDefinition extends M2ClassDefinition
    implements AspectDefinition
{

    /*package*/ M2AspectDefinition(ModelDefinition model, M2Aspect m2Aspect, NamespacePrefixResolver resolver, Map<QName, PropertyDefinition> modelProperties, Map<QName, AssociationDefinition> modelAssociations)
    {
        super(model, m2Aspect, resolver, modelProperties, modelAssociations);
    }

    @Override
    public String getDescription()
    {
        String value = M2Label.getLabel(model, "aspect", name, "description");
        
        // if we don't have a description call the super class
        if (value == null)
        {
           value = super.getDescription();
        }
        
        return value;
    }

    @Override
    public String getTitle()
    {
        String value = M2Label.getLabel(model, "aspect", name, "title");
        
        // if we don't have a title call the super class
        if (value == null)
        {
           value = super.getTitle();
        }
        
        return value;
   }
}
