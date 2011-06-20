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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Filter using property file values to accept or reject audit map values.<p>
 * 
 * The last component in the {@code rootPath} is considered to be the event
 * action. The keys in an audit map identify each audit value. Properties may be
 * defined to accept or reject each value. If any value in an audit map is
 * rejected, the whole map is rejected. So that one does not have to define
 * too many properties, a 'default' event action property may be defined. This
 * will be inherited by all actions unless a property is defined for a particular
 * event action. For example:
 * <pre>
 *   audit.filter.alfresco-access.default.enabled=true
 *   audit.filter.alfresco-access.default.user=~System;.*
 *   audit.filter.alfresco-access.default.type=cm:folder;cm:content;st:site
 *   audit.filter.alfresco-access.default.path=/app:company_home/.*
 *   audit.filter.alfresco-access.transaction.user=
 *   audit.filter.alfresco-access.login.user=jblogs
 *   ...
 * </pre>
 * 
 * Each property value defines a list of regular expressions that will be used
 * to match the actual audit map values. In the above example, events created
 * by any user except for the internal user 'System' will be recorded by default
 * for all event actions. However the property for the 'transaction' event action
 * overrides this to record even 'System' events.<p>
 * 
 * For any filters to be applied to an event action, that action's filters must be
 * enabled with an 'enabled' property set to {@code "true"}. However this may
 * also be done by using the 'default' event action, as shown above.<p> 
 * 
 * Note: Property names have a {@code "audit.filter."} prefix and use {@code '.'}
 * as a separator where as components of rootPath and keys in the audit map use
 * {@code '/'}. The following is an example rootPath and audit map which could be
 * used with the corresponding property names shown above:
 * 
 * <pre>
 *     rootPath                       auditMap
 *     "/alfresco-access/transaction" "user" => "System"
 *                                    "path" => "/app:company_home/st:sites/cm:mysite/cm:documentLibrary/cm:folder1"
 *                                    "type" => "cm:folder"
 *                                    "node" => ...
 * </pre> 
 * 
 * Lists are evaluated from left to right allowing one flexibility to accept or
 * reject different combinations of values. If no match is made by the end of the
 * list the value is rejected. If there is not a property for a given value or
 * an empty list is defined (as above for the user value on a transaction action)
 * any value is accepted.<p>
 * 
 * Each regular expression in the list is separated by a {@code ';'}. Expressions
 * that include a {@code ';'} may be escaped using a {@code '\'}. An expression
 * that starts with a {@code '~'} indicates that any matching value should be
 * rejected. If the first character of an expression needs to be a {@code '~'} it
 * too may be escaped with a {@code '\'}.<p>
 * 
 * A property value may be a reference to another property, which saves having
 * multiple copies. This is indicated by a {@code '$' as the first character of the 
 * property value. If the first character of an expression needs to be a
 * {@code '$'} it too may be escaped with a {@code '\'}. For example:
 * <pre>
 *   audit.filter.alfresco-access.default.type=cm:folder;cm:content
 *   audit.filter.alfresco-access.moveNode.from.type=$audit.filter.alfresco-access.default.type
 * </pre> 
 * 
 * @author Alan Davis
 */
public class PropertyAuditFilter implements AuditFilter
{
    private static Log logger = LogFactory.getLog(PropertyAuditFilter.class);

    private static final char NOT = '~';
    private static final char REDIRECT = '$';
    private static final String REG_EXP_SEPARATOR = ";";
    private static final char PROPERTY_SEPARATOR = '.';
    private static final String PROPERY_NAME_PREFIX = "audit.filter";
    private static final char ESCAPE = '\\';
    
    private static final String ESCAPED_REDIRECT = ""+ESCAPE+REDIRECT;
    private static final String ESCAPED_REG_EXP_SEPARATOR = ""+ESCAPE+REG_EXP_SEPARATOR;
    private static final String ESCAPED_NOT = ""+ESCAPE+NOT;
    
    private static final String ENABLED = "enabled";
    private static final String DEFAULT = "default";

    /**
     * Cache of {@code Patterns} for performance.
     */
    static Map<String, Pattern> patternCache =
        Collections.synchronizedMap(new WeakHashMap<String, Pattern>());
    
    /**
     * Properties to drive the filter.
     */
    Properties properties;
    
    /**
     * Set the properties object holding filter configuration
     * @since 3.2
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * @inheritDoc
     * @param @inheritDoc
     * @param @inheritDoc
     * @return @inheritDoc
     */
    @Override
    public boolean accept(String rootPath, Map<String, Serializable> auditMap)
    {
        String[] root = splitPath(rootPath);
        String rootProperty = getPropertyName(PROPERY_NAME_PREFIX, getPropertyName(root));
        String defaultRootProperty = getDefaultRootProperty(root);
      
        if ("true".equalsIgnoreCase(getProperty(rootProperty, defaultRootProperty, ENABLED)))
        {
            for (Map.Entry<String, Serializable> entry : auditMap.entrySet())
            {
                Serializable value = entry.getValue();
                if (value == null)
                {
                    value = "null";
                }
                String stringValue = (value instanceof String) ? (String)value : value.toString();
                String[] key = splitPath(entry.getKey());
                String propertyValue = getProperty(rootProperty, defaultRootProperty, key);
                if (!acceptValue(stringValue, propertyValue, rootProperty, key))
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Rejected \n\t            "+rootPath+'/'+entry.getKey()+"="+stringValue+
                                "\n\t"+getPropertyName(rootProperty, getPropertyName(key))+"="+propertyValue);                
                    }
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Checks a single value against a list of regular expressions.
     */
    private boolean acceptValue(String value, String regExpValue, String rootProperty, String... key)
    {
        // If no property or zero length it matches.
        if (regExpValue == null || regExpValue.length() == 0)
        {
            return true;
        }
        
        for (String regExp: getRegExpList(regExpValue, rootProperty, key))
        {
            boolean includeExp = regExp.charAt(0) != NOT;
            if (!includeExp || regExp.startsWith(ESCAPED_NOT))
            {
                regExp = regExp.substring(1);
            }
            if (getPattern(regExp).matcher(value).matches())
            {
                return includeExp;
            }
        }        
        
        return false;
    }
    
    private Pattern getPattern(String regExp)
    {
        Pattern pattern = patternCache.get(regExp);
        if (pattern == null)
        {
            pattern = Pattern.compile(regExp);
            patternCache.put(regExp, pattern);
        }
        return pattern;
    }

    /**
     * @return the root property name for the default event action.
     */
    private String getDefaultRootProperty(String[] root)
    {
        String action = root[root.length-1];
        root[root.length-1] = DEFAULT;
        String defaultRootProperty = getPropertyName(PROPERY_NAME_PREFIX, getPropertyName(root));
        root[root.length-1] = action;
        return defaultRootProperty;
    }

    /**
     * @return the value of the property {@code rootProperty+'.'+getPropertyName(keyComponents)}
     * defaulting to {@code defaultRootProperty+'.'+getPropertyName(keyComponents)}.
     */
    private String getProperty(String rootProperty, String defaultRootProperty, String... keyComponents)
    {
        String keyName = getPropertyName(keyComponents);
        String propertyName = getPropertyName(rootProperty, keyName);
        String value = getProperty(null, propertyName);
        if (value == null)
        {
            value = getProperty(null, getPropertyName(defaultRootProperty, keyName));
        }
        return value;
    }
    
    /**
     * @return a property value, including redirected values (where the value
     * of a property starts with a {@code '$'} indicating it is another property
     * name).
     * @throws IllegalArgumentException if redirecting properties reference themselves.
     */
    private String getProperty(List<String> loopCheck, String propertyName)
    {
        String value = properties.getProperty(propertyName);

        // Handle redirection of properties.
        if (value != null && value.length() > 0 && value.charAt(0) == REDIRECT)
        {
            String newPropertyName = value.substring(1);
            if (loopCheck == null)
            {
                loopCheck = new ArrayList<String>();
            }
            if (loopCheck.contains(newPropertyName))
            {
                RuntimeException e = new IllegalArgumentException("Redirected property "+
                        newPropertyName+" referes back to itself.");
                logger.error("Error found in properties for audit filter.", e);
                throw e;
            }
            loopCheck.add(propertyName);
            value = getProperty(loopCheck, newPropertyName);
        }
        else if (value == null && loopCheck != null && !loopCheck.isEmpty())
        {
            RuntimeException e = new IllegalArgumentException("Redirected property "+
                    loopCheck.get(loopCheck.size()-1)+
                    " points to "+propertyName+" but it does not exist.");
            logger.error("Error found in properties for audit filter.", e);
            throw e;
        }
        
        return value;
    }

    /**
     * Returns a List of regular expressions from a property's String value.
     * A leading {@code '~'} indicating the regular expression should be used
     * to reject values. This may be escaped with a leading back slash
     * ({@code "\\~"}) if the first character must be a semicolon. Other
     * escape characters are removed. A check is made that no expression is
     * zero length. 
     * @return a List of regular expressions.
     * @throws IllegalArgumentException if there are any zero length expressions.
     */
    private List<String> getRegExpList(String value, String rootProperty, String... key)
    {
        // Split the value into substrings separated by ';'. This may be escaped using "\;".
        List<String> regExpList = new ArrayList<String>();
        {
            int j = 0;
            int i = j - 1;
            do
            {
                i = value.indexOf(';', i+1);
                if (i != -1)
                {
                    if (i == 0 || value.charAt(i-1) != '\\')
                    {
                        regExpList.add(value.substring(j, i));
                        j = i + 1;
                    }               
                }
            }
            while (i != -1);
            if (j < value.length()-1)
            {
                regExpList.add(value.substring(j));
            }
        }
        
        // Remove escape characters other than the NOT (\~)
        // \$ at the start becomes "$"
        // \; anywhere becomes ";"
        for (int i=regExpList.size()-1; i >= 0; i--)
        {
            String regExp = regExpList.get(i);
            if (regExp.startsWith(ESCAPED_REDIRECT))
            {
                regExp = regExp.substring(1);
            }
            regExp = regExp.replaceAll(ESCAPED_REG_EXP_SEPARATOR, REG_EXP_SEPARATOR);
            
            if (regExp.length() == 0 || (regExp.charAt(0) == NOT && regExp.length() == 1))
            {
                throw new IllegalArgumentException(getPropertyName(rootProperty, getPropertyName(key))+"="+value+
                        "includes an empty regular expression.");
            }       
            regExpList.set(i, regExp);
        }
        return regExpList;
    }

    /**
     * @return a property name from the supplied components. Each component is
     * separated by a {@code '.'}.
     */
    private String getPropertyName(String... components)
    {
        StringBuilder sb = new StringBuilder();
        for (String component: components)
        {
            if (sb.length() > 0)
            {
                sb.append(PROPERTY_SEPARATOR);
            }
            sb.append(component);
        }
        return sb.toString();
    }

    /**
     * @return a list of components separated by '/' characters.
     */
    private String[] splitPath(String path)
    {
        if (path.length() > 0 && path.charAt(0) == '/')
        {
            path = path.substring(1);
        }
        return path.split("/");
    }
}
