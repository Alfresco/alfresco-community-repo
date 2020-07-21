/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.forms.processor.node;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.ASSOC_DATA_PREFIX;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldGroup;
import org.alfresco.repo.forms.AssociationFieldDefinition.Direction;
import org.alfresco.repo.forms.processor.FieldProcessor;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link FieldProcessor} implementation that handles associations.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class AssociationFieldProcessor extends QNameFieldProcessor<AssociationDefinition>
{
    private static final Log logger = LogFactory.getLog(AssociationFieldProcessor.class);

    public AssociationFieldProcessor()
    {
        // Constructor for Spring.
    }

    public AssociationFieldProcessor(NamespaceService namespaceService, DictionaryService dictionaryService)
    {
        super(namespaceService, dictionaryService);
    }

    @Override
    protected Log getLogger() 
    {
        return logger;
    }

    @Override
    protected FieldGroup getGroup(AssociationDefinition assocDef)
    {
        // TODO Need to Implement this once Composite Content is implementd.
        return null;
    }

    @Override
    public Field makeField(AssociationDefinition assocDef, Object value, FieldGroup group)
    {
        AssociationFieldDefinition fieldDef = makeAssociationFieldDefinition(assocDef, group);
        return new ContentModelField(assocDef, fieldDef, value);
    }

    /**
     * Gets the associated value from the {@link ContentModelItemData}.
     * If the value is <code>null</code> the method returns an empty {@link List}.
     * If the value is a single Object (assumed to be a NodeRef) it returns a {@link List} containing a {@link String} representation of that object.
     * If the value is a {@link Collection} of Objects, returns a {@link List} containing {@link String} representations of all the objects.
     * @return An {@link ArrayList} of Strings or <code>null</code>.
     */
    @Override
    protected Object getValue(QName name, ContentModelItemData<?> data)
    {
        Serializable values = data.getAssociationValue(name);
        if (values == null)
        {
            return Collections.emptyList();
        }
        if (values instanceof Collection<?>)
        {
            return getValues((Collection<?>) values);
        }
        return Collections.singletonList(values.toString()); 
    }

    private List<String> getValues(Collection<?> collection)
    {
        List<String> results = new ArrayList<String>(collection.size());
        for (Object value : collection)
        {
            results.add(value.toString());
        }
        return results;
    }

    public AssociationFieldDefinition makeAssociationFieldDefinition(final AssociationDefinition assocDef, FieldGroup group)
    {
        String name = getPrefixedName(assocDef);
        String endpointType = assocDef.getTargetClass().getName().toPrefixString(namespaceService);
        AssociationFieldDefinition fieldDef = new AssociationFieldDefinition(name, 
                endpointType, 
                Direction.TARGET);
        
        populateFieldDefinition(assocDef, fieldDef, group, ASSOC_DATA_PREFIX);

        fieldDef.setEndpointMandatory(assocDef.isTargetMandatory());
        fieldDef.setEndpointMany(assocDef.isTargetMany());
        return fieldDef;
    }

    @Override
    protected String getRegistryKey() 
    {
        return FormFieldConstants.ASSOC;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.QNameFieldProcessor#getTypeDefinition(org.alfresco.service.namespace.QName, org.alfresco.repo.forms.processor.node.ItemData, boolean)
     */
    @Override
    protected AssociationDefinition getTypeDefinition(QName fullName, ContentModelItemData<?> itemData, boolean isForcedField)
    {
        AssociationDefinition assocDefinition = itemData.getAssociationDefinition(fullName);
        if (assocDefinition == null)
        {
            if (isForcedField)
            {
                assocDefinition = dictionaryService.getAssociation(fullName);
            }
        }
        return assocDefinition;
    }
}