/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;

/**
 * Records management method security post processor.
 * <p>
 * Combines RM method security configuration with that of the core server before the security 
 * bean is instantiated.
 * 
 * @author Roy Wetherall
 */
public class RMMethodSecurityPostProcessor implements BeanFactoryPostProcessor
{
    private static Log logger = LogFactory.getLog(RMMethodSecurityPostProcessor.class);
    
    public static final String PROP_OBJECT_DEFINITION_SOURCE = "objectDefinitionSource";
    public static final String PROPERTY_PREFIX = "rm.methodsecurity.";
    public static final String SECURITY_BEAN_POSTFIX = "_security";
    
    /** Security bean names */
    private Set<String> securityBeanNames;    
    private Set<String> securityBeanNameCache;
    
    /** Configuration properties */
    private Properties properties;
    
    /**
     * Set of security beans to apply RM configuration to.
     * <p>
     * Used in the case where the security bean does not follow the standard naming convention.
     * 
     * @param securityBeanNames security bean names
     */
    public void setSecurityBeanNames(Set<String> securityBeanNames)
    {
        this.securityBeanNames = securityBeanNames;
    }    
    
    /**
     * @param properties    configuration properties
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }
    
    /**
     * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
        for (String bean : getSecurityBeanNames(beanFactory))
        {            
            if (beanFactory.containsBeanDefinition(bean) == true) 
            {
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Adding RM method security definitions for " + bean);
                }
                
                BeanDefinition beanDef = beanFactory.getBeanDefinition(bean);                        
                PropertyValue beanValue = beanDef.getPropertyValues().getPropertyValue(PROP_OBJECT_DEFINITION_SOURCE);          
                if (beanValue != null)
                {
                    String beanStringValue = (String)((TypedStringValue)beanValue.getValue()).getValue();
                    String mergedStringValue = merge(beanStringValue);
                    beanDef.getPropertyValues().addPropertyValue(PROP_OBJECT_DEFINITION_SOURCE, new TypedStringValue(mergedStringValue));
                }
            }
        }
    }
    
    /**
     * Get all the security bean names by looking at the property values set.
     * 
     * @param beanFactory
     * @return
     */
    private Set<String> getSecurityBeanNames(ConfigurableListableBeanFactory beanFactory)
    {
        if (securityBeanNameCache == null)
        {
            securityBeanNameCache = new HashSet<String>(21);
            if (securityBeanNames != null)
            {
                securityBeanNameCache.addAll(securityBeanNames);
            }
            
            for (Object key : properties.keySet())
            {        
                String[] split = ((String)key).split("\\.");
                int index = split.length - 2;
                String securityBeanName = split[index] + SECURITY_BEAN_POSTFIX;
                if (securityBeanNameCache.contains(securityBeanName) == false && beanFactory.containsBean(securityBeanName) == true)
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("Adding " + securityBeanName + " to list from properties.");
                    }
                    
                    securityBeanNameCache.add(securityBeanName);
                }            
            }
        }
        
        return securityBeanNameCache;
    }
    
    /**
     * @param beanStringValue
     * @param rmBeanStringValue
     * @return
     */
    private String merge(String beanStringValue) 
    {
        Map<String, String> map = convertToMap(beanStringValue);
        
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            String key = entry.getKey();
            String propKey = PROPERTY_PREFIX + key;
            if (properties.containsKey(propKey) == true)
            {
                map.put(key, entry.getValue() + "," + properties.getProperty(propKey));
            }
            else
            {
                if (logger.isWarnEnabled() == true)
                {                 
                    logger.warn("Missing RM security definition for method " + key);                    
                }
            }
        }
        
        return convertToString(map);
    }
    
    /**
     * @param stringValue
     * @return
     */
    private Map<String, String> convertToMap(String stringValue)
    {
        String[] values = stringValue.trim().split("\n");
        Map<String, String> map = new HashMap<String, String>(values.length);
        for (String value : values)
        {            
            String[] pair = value.trim().split("=");
            map.put(pair[0], pair[1]);
        }
        return map;
    }
    
    /**
     * @param map
     * @return
     */
    private String convertToString(Map<String, String> map)
    {
        StringBuffer buffer = new StringBuffer(256);
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            buffer.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");            
        }    
        
        return buffer.toString();
    }
}
