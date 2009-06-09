/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.forms.processor.node;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FormProcessor implementation that can generate and persist Form objects
 * for types in the Alfresco content model.
 *
 * @author Gavin Cornwell
 */
public class TypeFormProcessor extends NodeFormProcessor
{
    /** Logger */
    private static Log logger = LogFactory.getLog(TypeFormProcessor.class);
    
    @Override
    protected Object getTypedItem(Item item)
    {
        TypeDefinition typeDef = null;
        
        try
        {
            // convert the prefix type into full QName representation
            // TODO: Also look for and deal with full QName as itemId
            QName type = QName.createQName(item.getId(), this.namespaceService);
        
            // retrieve the type from the dictionary
            typeDef = this.dictionaryService.getType(type);
            
            if (typeDef == null)
            {
                throw new FormNotFoundException(item, 
                            new IllegalArgumentException("Type does not exist: " + item.getId()));
            }
        }
        catch (InvalidQNameException iqne)
        {
            throw new FormNotFoundException(item, iqne);
        }
        
        // return the type definition object for the requested type
        return typeDef;
    }

    @Override
    protected void internalGenerate(Object item, List<String> fields, List<String> forcedFields, Form form)
    {
        if (logger.isDebugEnabled())
            logger.debug("Generating form for item: " + item);
        
        // cast to the expected NodeRef representation
        TypeDefinition typeDef = (TypeDefinition)item;
        
        // generate the form for the node
        generateType(typeDef, fields, forcedFields, form);
        
        if (logger.isDebugEnabled())
            logger.debug("Generating form: " + form);
    }
    
    /**
     * Sets up the Form object for the given NodeRef
     * 
     * @param nodeRef The NodeRef to generate a Form for
     * @param fields Restricted list of fields to include
     * @param forcedFields List of fields to forcibly include
     * @param form The Form instance to populate
     */
    protected void generateType(TypeDefinition typeDef, List<String> fields, List<String> forcedFields, Form form)
    {
        // set the type and URL of the item
        form.getItem().setType(typeDef.getName().toPrefixString(this.namespaceService));
        form.getItem().setUrl("/api/classes/" + typeDef.getName().toPrefixString(this.namespaceService).replace(":", "_"));
        
        if (fields != null && fields.size() > 0)
        {
            generateSelectedFields(null, typeDef, fields, forcedFields, form);
        }
        else
        {
            // setup field definitions and data
            generateAllPropertyFields(typeDef, form);
            generateAllAssociationFields(typeDef, form);
        }
    }
    
    /**
     * Sets up the field definitions for all the type's properties.
     * 
     * @param typeDef The type being setup
     * @param form The Form instance to populate
     */
    protected void generateAllPropertyFields(TypeDefinition typeDef, Form form)
    {
        // iterate round the property defintions and setup field definition
        Map<QName, PropertyDefinition> propDefs = typeDef.getProperties();
        for (PropertyDefinition propDef : propDefs.values())
        {
            generatePropertyField(propDef, null, form);
        }
        
        // get all default aspects for the type and iterate round their 
        // property definitions too
        List<AspectDefinition> aspects = typeDef.getDefaultAspects(true);
        for (AspectDefinition aspect : aspects)
        {
            propDefs = aspect.getProperties();
            for (PropertyDefinition propDef : propDefs.values())
            {
                generatePropertyField(propDef, null, form);
            }
        }
    }
    
    /**
     * Sets up the field definitions for all the type's associations.
     * 
     * @param typeDef The type being setup
     * @param form The Form instance to populate
     */
    protected void generateAllAssociationFields(TypeDefinition typeDef, Form form)
    {
        // iterate round the association defintions and setup field definition
        Map<QName, AssociationDefinition> assocDefs = typeDef.getAssociations();
        for (AssociationDefinition assocDef : assocDefs.values())
        {
            this.generateAssociationField(assocDef, null, form);
        }
        
        // get all default aspects for the type and iterate round their 
        // association definitions too
        List<AspectDefinition> aspects = typeDef.getDefaultAspects(true);
        for (AspectDefinition aspect : aspects)
        {
            assocDefs = aspect.getAssociations();
            for (AssociationDefinition assocDef : assocDefs.values())
            {
                this.generateAssociationField(assocDef, null, form);
            }
        }
    }

    @Override
    protected Object internalPersist(Object item, FormData data)
    {
        if (logger.isDebugEnabled())
            logger.debug("Persisting form for: " + item);
        
        // cast to the expected NodeRef representation
        TypeDefinition typeDef = (TypeDefinition)item;
        
        if (logger.isWarnEnabled())
            logger.warn("Persisting of 'type' form items has not been implemented yet!");
        
        return item;
    }

}
