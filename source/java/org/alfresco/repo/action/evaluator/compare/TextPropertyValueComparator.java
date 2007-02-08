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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.action.evaluator.compare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.service.cmr.action.ActionServiceException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

/**
 * Test property value comparator
 * 
 * @author Roy Wetherall
 */
public class TextPropertyValueComparator implements PropertyValueComparator
{
    /**
     * I18N message ids
     */
    private static final String MSGID_INVALID_OPERATION = "text_property_value_comparator.invalid_operation";
    
    /**
     * Special star string
     */
    private static final String STAR = "*";
    
    /**
     * @see org.alfresco.repo.action.evaluator.compare.PropertyValueComparator#compare(java.io.Serializable, java.io.Serializable, org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation)
     */
    public boolean compare(
            Serializable propertyValue,
            Serializable compareValue, 
            ComparePropertyValueOperation operation)
    {
        String compareText = (String)compareValue;
        
        boolean result = false;
        if (operation == null)
        {
            // Check for a trailing or leading star since it implies special behaviour when no default operation is specified
            if (compareText.startsWith(STAR) == true)
            {
                // Remove the star and set the operation to endsWith
                operation = ComparePropertyValueOperation.ENDS;
                compareText = compareText.substring(1);
            }
            else if (compareText.endsWith(STAR) == true)
            {
                // Remove the star and set the operation to startsWith
                operation = ComparePropertyValueOperation.BEGINS;
                compareText = compareText.substring(0, (compareText.length()-2));
            }
            else
            {
                operation = ComparePropertyValueOperation.CONTAINS;
            }
        }
            
        // Build the reg ex
        String regEx = buildRegEx(compareText, operation);
        
        // Do the match
        if (propertyValue != null)
        {
            result = ((String)propertyValue).toLowerCase().matches(regEx);
        }
        
        return result;
    }
    
    /**
     * Builds the regular expressin that it used to make the match
     * 
     * @param matchText     the raw text to be matched
     * @param operation     the operation
     * @return              the regular expression string
     */
    private String buildRegEx(String matchText, ComparePropertyValueOperation operation) 
    {
        String result = escapeText(matchText.toLowerCase());
        switch (operation) 
        {
            case CONTAINS:
                result = "^.*" + result + ".*$";
                break;
            case BEGINS:
                result = "^" + result + ".*$";
                break;
            case ENDS:
                result = "^.*" + result + "$";
                break;
            case EQUALS:
                break;
            default:
                // Raise an invalid operation exception
                throw new ActionServiceException(
                        MSGID_INVALID_OPERATION, 
                        new Object[]{operation.toString()});
        }
        return result;
    }

    /**
     * Escapes the text before it is turned into a regualr expression
     * 
     * @param matchText     the raw text
     * @return              the escaped text
     */
    private String escapeText(String matchText) 
    {
        StringBuilder builder = new StringBuilder(matchText.length());
        for (char charValue : matchText.toCharArray()) 
        {
            if (charValue == '*')
            {
                builder.append(".");
            }
            else if (getEscapeCharList().contains(charValue) == true)
            {
                builder.append("\\");
            }
            builder.append(charValue);
        }
        
        return builder.toString();
    }

    /**
     * List of escape characters
     */
    private static List<Character> ESCAPE_CHAR_LIST = null;
    
    /**
     * Get the list of escape chars
     * 
     * @return  list of excape chars
     */
    private List<Character> getEscapeCharList() 
    {
        if (ESCAPE_CHAR_LIST == null)
        {
            //([{\^$|)?*+.
            ESCAPE_CHAR_LIST = new ArrayList<Character>(4);
            ESCAPE_CHAR_LIST.add('.');
            ESCAPE_CHAR_LIST.add('^');
            ESCAPE_CHAR_LIST.add('$');
            ESCAPE_CHAR_LIST.add('(');
            ESCAPE_CHAR_LIST.add('[');
            ESCAPE_CHAR_LIST.add('{');
            ESCAPE_CHAR_LIST.add('\\');
            ESCAPE_CHAR_LIST.add('|');
            ESCAPE_CHAR_LIST.add(')');
            ESCAPE_CHAR_LIST.add('?');
            ESCAPE_CHAR_LIST.add('+');
        }
        return ESCAPE_CHAR_LIST;
    }

    /**
     * @see org.alfresco.repo.action.evaluator.compare.PropertyValueComparator#registerComparator(org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator)
     */
    public void registerComparator(ComparePropertyValueEvaluator evaluator)
    {
        evaluator.registerComparator(DataTypeDefinition.TEXT, this);        
    }
}
