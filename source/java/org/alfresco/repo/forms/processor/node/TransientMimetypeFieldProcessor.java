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

package org.alfresco.repo.forms.processor.node;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP_DATA_PREFIX;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.TRANSIENT_MIMETYPE;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

public class TransientMimetypeFieldProcessor extends TransientFieldProcessor
{
	private static final Log logger = LogFactory.getLog(TransientMimetypeFieldProcessor.class);
    private static final String MSG_MIMETYPE_LABEL = "form_service.mimetype.label";
    private static final String MSG_MIMETYPE_DESC = "form_service.mimetype.description";

	@Override
	protected Log getLogger() {
		return logger;
	}
	
	@Override
	protected FieldDefinition makeTransientFieldDefinition() {
        String dataKeyName = PROP_DATA_PREFIX + TRANSIENT_MIMETYPE;
        PropertyFieldDefinition mimetypeField = new PropertyFieldDefinition(TRANSIENT_MIMETYPE, DataTypeDefinition.TEXT
                    .getLocalName());
        mimetypeField.setLabel(I18NUtil.getMessage(MSG_MIMETYPE_LABEL));
        mimetypeField.setDescription(I18NUtil.getMessage(MSG_MIMETYPE_DESC));
        mimetypeField.setDataKeyName(dataKeyName);
        return mimetypeField;
    }

	@Override
    protected String getRegistryKey() 
	{
		 return FormFieldConstants.TRANSIENT_MIMETYPE;
	}
}
