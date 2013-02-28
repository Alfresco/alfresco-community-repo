/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.action.parameter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.action.ParameterizedItem;
import org.alfresco.service.cmr.action.ParameterizedItemDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * 
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ParameterProcessorComponent
{
    private static final String REG_EX = "\\$\\{([^\\$\\{]+)\\}";
    
    private Map<String, ParameterProcessor> processors = new HashMap<String, ParameterProcessor>(5);
    
    /**
     * 
     * @param processor
     */
    public void register(ParameterProcessor processor)
    {
        this.processors.put(processor.getName(), processor);
    }
    
    /**
     * 
     * @param ruleItem
     * @param ruleItemDefinition
     * @param actionedUponNodeRef
     */
    public void process(ParameterizedItem ruleItem, ParameterizedItemDefinition ruleItemDefinition, NodeRef actionedUponNodeRef)
    {
        for (Map.Entry<String, Serializable> entry : ruleItem.getParameterValues().entrySet())
        {
            String parameterName = entry.getKey();            
            
            // get the parameter definition
            ParameterDefinition def = ruleItemDefinition.getParameterDefintion(parameterName);
            if (def != null)
            {
                if (DataTypeDefinition.TEXT.equals(def.getType()) == true)
                {
                    String parameterValue = (String)entry.getValue();
                    
                    // match the substitution pattern
                    Pattern patt = Pattern.compile(REG_EX);
                    Matcher m = patt.matcher(parameterValue);
                    StringBuffer sb = new StringBuffer(parameterValue.length());
                    
                    while (m.find()) 
                    {
                      String text = m.group(1);              
                      
                      // lookup parameter processor to use
                      ParameterProcessor processor = lookupProcessor(text);
                      if (processor == null)
                      {
                          throw new AlfrescoRuntimeException("A parameter processor has not been found for the substitution string " + text);
                      }
                      else
                      {                  
                          // process each substitution value
                          text = processor.process(text, actionedUponNodeRef);
                      }
                      
                      // append new value
                      m.appendReplacement(sb, Matcher.quoteReplacement(text));
                    }            
                    m.appendTail(sb);              
                    
                    // set the updated parameter value
                    ruleItem.setParameterValue(parameterName, sb.toString());
                }
            }            
        }        
    }
    
    private ParameterProcessor lookupProcessor(String value)
    {
        ParameterProcessor result = null;
        
        if (value != null && value.isEmpty() == false)
        {
            String[] values = value.split("\\.", 2);
            if (values.length != 0)
            {
                // get the processor from the registered map
                result = processors.get(values[0]);
            }
        }
        
        return result;
    }
}
