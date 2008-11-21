/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * This class is a custom element reader to parse the config file for
 * &lt;form&gt; elements.
 * 
 * @author Neil McErlean.
 */
public class FormElementReader implements ConfigElementReader
{
    public static final String ATTR_APPEARANCE = "appearance";
    public static final String ATTR_FOR_MODE = "for-mode";
    public static final String ATTR_MESSAGE = "message";
    public static final String ATTR_MESSAGE_ID = "message-id";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_NAME_ID = "id";
    public static final String ATTR_PARENT = "parent";
    public static final String ATTR_REQUIRES_ROLE = "requires-role";
    public static final String ATTR_SUBMISSION_URL = "submission-url";
    public static final String ATTR_TEMPLATE = "template";
    public static final String ATTR_TYPE = "type";
    public static final String ELEMENT_FORM = "form";
    public static final String ELEMENT_HIDE = "hide";
    public static final String ELEMENT_SHOW = "show";

    /**
     * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
     */
    public ConfigElement parse(Element formElement)
    {
        FormConfigElement result = null;
        if (formElement == null)
        {
            return null;
        }

        String name = formElement.getName();
        if (!name.equals(ELEMENT_FORM))
        {
            throw new ConfigException(this.getClass().getName()
                    + " can only parse " + ELEMENT_FORM
                    + " elements, the element passed was '" + name + "'");
        }

        result = new FormConfigElement();
        
        parseSubmissionURL(formElement, result);
        
        // Using xpath expressions to select nodes under <form> and their attributes.
        parseForms(formElement, result);

        parseFieldVisibilities(formElement, result);

        parseSets(formElement, result);

        parseFields(formElement, result);

        parseModelOverrides(formElement, result);

        return result;
    }

    private void parseModelOverrides(Element formElement, FormConfigElement result)
    {
        for (Object propObj : formElement.selectNodes("./model-override/property")) {
            Element propertyElem = (Element)propObj;
            String propName = propertyElem.attributeValue(ATTR_NAME);
            String propValue = propertyElem.getTextTrim();
            result.addModelOverrides(propName, propValue);
        }
    }

    @SuppressWarnings("unchecked")
    private void parseFields(Element formElement, FormConfigElement result)
    {
        for (Object fieldObj : formElement.selectNodes("./appearance/field")) {
            Element fieldElem = (Element)fieldObj;
            List<Attribute> fieldAttributes = fieldElem.selectNodes("./@*");
            
            List<String> fieldAttributeNames = new ArrayList<String>();
            List<String> fieldAttributeValues = new ArrayList<String>();
            
            // With special handling for the mandatory "id" attribute.
            String fieldIdValue = null;
            for (Attribute nextAttr : fieldAttributes) {
                String nextAttributeName = nextAttr.getName();
                String nextAttributeValue = nextAttr.getValue();
                
                if (nextAttributeName.equals(ATTR_NAME_ID))
                {
                    fieldIdValue = nextAttributeValue;
                }
                else
                {
                    fieldAttributeNames.add(nextAttributeName);
                    fieldAttributeValues.add(nextAttributeValue);
                }
            }
            if (fieldIdValue == null)
            {
                throw new ConfigException("<field> node missing mandatory id attribute.");
            }
            result.addField(fieldIdValue, fieldAttributeNames, fieldAttributeValues);

            List<Element> controlObjs = fieldElem.selectNodes("./control");
            if (!controlObjs.isEmpty())
            {
                Element controlElem = controlObjs.get(0);
            
                String templateValue = controlElem.attributeValue(ATTR_TEMPLATE);
                List<String> controlParamNames = new ArrayList<String>();
                List<String> controlParamValues = new ArrayList<String>();
                for (Object paramObj : controlElem
                        .selectNodes("./control-param"))
                {
                    Element paramElem = (Element) paramObj;
                    controlParamNames.add(paramElem.attributeValue(ATTR_NAME));
                    controlParamValues.add(paramElem.getTextTrim());
                }
                result.addControlForField(fieldIdValue, templateValue, controlParamNames, controlParamValues);
            }
            
            for (Object constraintMessageObj : fieldElem.selectNodes("./constraint-message")) {
                Element constraintMessage = (Element)constraintMessageObj;
                String type = constraintMessage.attributeValue(ATTR_TYPE);
                String message = constraintMessage.attributeValue(ATTR_MESSAGE);
                String messageId = constraintMessage.attributeValue(ATTR_MESSAGE_ID);
                result.addConstraintForField(fieldIdValue, type, message, messageId);
            }
        }
    }

    private void parseSets(Element formElement, FormConfigElement result)
    {
        for (Object setObj : formElement.selectNodes("./appearance/set")) {
            Element setElem = (Element)setObj;
            String setId = setElem.attributeValue(ATTR_NAME_ID);
            String parentSetId = setElem.attributeValue(ATTR_PARENT);
            String appearance = setElem.attributeValue(ATTR_APPEARANCE);
            
            result.addSet(setId, parentSetId, appearance);
        }
    }

    private void parseFieldVisibilities(Element formElement,
            FormConfigElement result)
    {
        for (Object obj : formElement.selectNodes("./field-visibility/show|./field-visibility/hide")) {
            Element showOrHideElem = (Element)obj;
            String nodeName = showOrHideElem.getName();
            String fieldId = showOrHideElem.attributeValue(ATTR_NAME_ID);
            String mode = showOrHideElem.attributeValue(ATTR_FOR_MODE);
            
            result.addFieldVisibility(nodeName, fieldId, mode);
        }
    }

    private void parseForms(Element formElement, FormConfigElement result)
    {
        for (Object obj : formElement.selectNodes("./edit-form|./view-form|./create-form")) {
            Element editOrViewOrCreateFormElem = (Element)obj;
            String nodeName = editOrViewOrCreateFormElem.getName();
            String template = editOrViewOrCreateFormElem.attributeValue(ATTR_TEMPLATE);
            String requiresRole = editOrViewOrCreateFormElem.attributeValue(ATTR_REQUIRES_ROLE);
            
            result.addFormTemplate(nodeName, template, requiresRole);
        }
    }

    private void parseSubmissionURL(Element formElement,
            FormConfigElement result)
    {
        String submissionURL = formElement.attributeValue(ATTR_SUBMISSION_URL);
        result.setSubmissionURL(submissionURL);
    }
}
