/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.action.evaluator;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.compare.ContentPropertyName;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Compare mime type evaluator
 * 
 * @author Roy Wetherall
 */
public class CompareMimeTypeEvaluator extends ComparePropertyValueEvaluator
{
	/**
	 * Evaluator constants
	 */
	public static final String NAME = "compare-mime-type";
    
    /**
     * 
     */
    private static final String ERRID_NOT_A_CONTENT_TYPE = "compare_mime_type_evaluator.not_a_content_type";
    private static final String ERRID_NO_PROPERTY_DEFINTION_FOUND = "compare_mime_type_evaluator.no_property_definition_found"; 
    
    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef)
    {
        QName propertyQName = (QName)actionCondition.getParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY);
        if (propertyQName == null)
        {
            // Default to the standard content property
            actionCondition.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_CONTENT);
        }
        else
        {
            // Ensure that we are dealing with a content property
            QName propertyTypeQName = null;
            PropertyDefinition propertyDefintion = this.dictionaryService.getProperty(propertyQName);
            if (propertyDefintion != null)
            {
                propertyTypeQName = propertyDefintion.getDataType().getName();
                if (DataTypeDefinition.CONTENT.equals(propertyTypeQName) == false)
                {
                    throw new ActionServiceException(ERRID_NOT_A_CONTENT_TYPE);
                }
            }
            else
            {
                throw new ActionServiceException(ERRID_NO_PROPERTY_DEFINTION_FOUND);
            }
        }
        
        // Set the content property to be MIMETYPE
        actionCondition.setParameterValue(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, ContentPropertyName.MIME_TYPE.toString());

        return super.evaluateImpl(actionCondition, actionedUponNodeRef);
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
        super.addParameterDefinitions(paramList);
	}
}
