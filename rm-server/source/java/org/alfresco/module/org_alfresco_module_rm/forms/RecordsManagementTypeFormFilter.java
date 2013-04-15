/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.forms;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldGroup;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.node.FieldUtils;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Implementation of a form processor Filter.
 * <p>
 * The filter implements the <code>afterGenerate</code> method to ensure a
 * default unique identifier is provided for the <code>rma:identifier</code>
 * property.
 * </p>
 * <p>
 * The filter also ensures that any custom properties defined for the records
 * management type are provided as part of the Form.
 * </p>
 *
 * @author Gavin Cornwell
 */
public class RecordsManagementTypeFormFilter extends RecordsManagementFormFilter<TypeDefinition> implements RecordsManagementModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RecordsManagementTypeFormFilter.class);

    protected static final String NAME_FIELD_GROUP_ID = "name";
    protected static final String TITLE_FIELD_GROUP_ID = "title";
    protected static final String DESC_FIELD_GROUP_ID = "description";
    protected static final String OTHER_FIELD_GROUP_ID = "other";

    protected static final FieldGroup NAME_FIELD_GROUP = new FieldGroup(NAME_FIELD_GROUP_ID, null, false, false, null);
    protected static final FieldGroup TITLE_FIELD_GROUP = new FieldGroup(TITLE_FIELD_GROUP_ID, null, false, false, null);
    protected static final FieldGroup DESC_FIELD_GROUP = new FieldGroup(DESC_FIELD_GROUP_ID, null, false, false, null);
    protected static final FieldGroup OTHER_FIELD_GROUP = new FieldGroup(OTHER_FIELD_GROUP_ID, null, false, false, null);

    /** Identifier service */
    protected IdentifierService identifierService;

    /**
     * @param identifierService identifier service
     */
    public void setIdentifierService(IdentifierService identifierService)
    {
        this.identifierService = identifierService;
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.Filter#afterGenerate(java.lang.Object,
     * java.util.List, java.util.List, org.alfresco.repo.forms.Form,
     * java.util.Map)
     */
    public void afterGenerate(TypeDefinition type, List<String> fields, List<String> forcedFields, Form form,
                Map<String, Object> context)
    {
        QName typeName = type.getName();
        if (rmAdminService.isCustomisable(typeName) == true)
        {
        	addCustomRMProperties(typeName, form);
        }

        // What about any mandatory aspects?
        Set<QName> aspects = type.getDefaultAspectNames();
        for (QName aspect : aspects)
        {
            if (rmAdminService.isCustomisable(aspect) == true)
            {
                addCustomRMProperties(aspect, form);
            }
        }
    }

    /**
     * Adds a property definition for each of the custom properties for the
     * given RM type to the given form.
     *
     * @param rmTypeCustomAspect Enum representing the RM type to add custom
     *            properties for
     * @param form The form to add the properties to
     */
    protected void addCustomRMProperties(QName customisableType, Form form)
    {
        ParameterCheck.mandatory("customisableType", customisableType);
        ParameterCheck.mandatory("form", form);

        Map<QName, PropertyDefinition> customProps = rmAdminService.getCustomPropertyDefinitions(customisableType);

        if (customProps != null)
        {
	        if (logger.isDebugEnabled() == true)
	        {
	            logger.debug("Found " + customProps.size() + " custom properties for customisable type " + customisableType);
	        }

	        // setup field definition for each custom property
	        Collection<PropertyDefinition> properties = customProps.values();
	        FieldGroup group = new FieldGroup(CUSTOM_RM_FIELD_GROUP_ID, null, false, false, null);
	        List<Field> fields = FieldUtils.makePropertyFields(properties, group, namespaceService, dictionaryService);
	        form.addFields(fields);
        }
    }

    /*
     * @see org.alfresco.repo.forms.processor.Filter#afterPersist(java.lang.Object, org.alfresco.repo.forms.FormData, java.lang.Object)
     */
    public void afterPersist(TypeDefinition item, FormData data, final NodeRef nodeRef)
    {
    }

}
