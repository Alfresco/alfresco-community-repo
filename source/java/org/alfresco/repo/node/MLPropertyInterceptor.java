/*
 * Copyright (C) 2007 Alfresco, Inc.
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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceptor to filter out multilingual text properties from getter methods and
 * transform to multilingual text for setter methods.
 * <p>
 * This interceptor ensures that all multilingual (ML) text is transformed to the
 * locale chosen {@link org.alfresco.service.cmr.repository.MLText#getContextLocale() for the request}
 * for getters and transformed to the default locale type for setters.
 * <p>
 * Where {@link org.alfresco.service.cmr.repository.MLText ML text} has been passed in, this
 * will be allowed to pass.
 * 
 * @see org.alfresco.service.cmr.repository.MLText#getContextLocale()
 * @see org.alfresco.service.cmr.repository.NodeService#getProperty(NodeRef, QName)
 * @see org.alfresco.service.cmr.repository.NodeService#getProperties(NodeRef)
 * @see org.alfresco.service.cmr.repository.NodeService#setProperty(NodeRef, QName, Serializable)
 * @see org.alfresco.service.cmr.repository.NodeService#setProperties(NodeRef, Map)
 * 
 * @author Derek Hulley
 * @author Philippe Dubois
 */
public class MLPropertyInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(MLPropertyInterceptor.class);
    

    private static ThreadLocal<Boolean> mlAware = new ThreadLocal<Boolean>();
    
    /** Direct access to the NodeService */
    private NodeService directNodeService;
    /** Used to access property definitions */
    private DictionaryService dictionaryService;
    
    /**
     * Change the filtering behaviour of this interceptor on the curren thread.
     * Use this to switch off the filtering and just pass out properties as
     * handed out of the node service.
     * 
     * @param mlAwareVal <tt>true</tt> if the current thread is able to handle
     *      {@link MLText d:mltext} property types, otherwise <tt>false</tt>.
     */
    static public void setMLAware(boolean mlAwareVal)
    {
        mlAware.set(new Boolean(mlAwareVal));
    }
    
    /**
     * @return Returns <tt>true</tt> if the current thread has marked itself
     *      as being able to handle {@link MLText d:mltext} types properly.
     */
    static public boolean isMLAware()
    {
        if (mlAware.get() == null)
        {
            return false;
        }
        else
        {
            return mlAware.get();
        }
    }
    
    public void setDirectNodeService(NodeService bean)
    {
        this.directNodeService = bean;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    
    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        String methodName = invocation.getMethod().getName();
        Object[] args = invocation.getArguments();

        // What locale must be used for filtering
        Locale contentLocale = I18NUtil.getContentLocale();
        if (logger.isDebugEnabled())
        {
            logger.debug("Intercepting method " + methodName + " using content filter " + contentLocale);
        }
        
        Object ret = null;
        
        // If isMLAware then no treatment is done, just return
        if(isMLAware())
        {    
            // Don't interfere
            return invocation.proceed();
        }
        
        if (methodName.equals("getProperty"))
        {
            ret = invocation.proceed();
            // The return value might need to be converted to a String
            if (ret != null && ret instanceof MLText)
            {
                 MLText mlText = (MLText) ret;
                 ret = mlText.getClosestValue(contentLocale);
                // done
                if (logger.isDebugEnabled())
                {
                    logger.debug(
                            "Converted ML text: \n" +
                            "   initial: " + mlText + "\n" +
                            "   converted: " + ret);
                }
            }
        }
        else if (methodName.equals("getProperties"))
        {
            Map<QName, Serializable> properties = (Map<QName, Serializable>) invocation.proceed();
            Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(properties.size() * 2);
            // Check each return value type
            for (Map.Entry<QName, Serializable> entry : properties.entrySet())
            {
                QName key = entry.getKey();
                Serializable value = entry.getValue();
                if (value != null && value instanceof MLText)
                {
                    MLText mlText = (MLText) value;
                    value = mlText.getClosestValue(contentLocale);
                    // Store the converted value
                    convertedProperties.put(key, value);
                }
                else
                {
                    // The value goes straight back in
                    convertedProperties.put(key, value);
                }
            }
            ret = convertedProperties;
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Converted getProperties return value: \n" +
                        "   initial:   " + properties + "\n" +
                        "   converted: " + convertedProperties);
            }
        }
        else if (methodName.equals("setProperties"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            Map<QName, Serializable> newProperties =(Map<QName, Serializable>) args[1];
            // Get the current properties for the node
            Map<QName, Serializable> currentProperties = directNodeService.getProperties(nodeRef);
            // Convert all properties
            Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(newProperties.size() * 2);              
            for (Map.Entry<QName, Serializable> entry : newProperties.entrySet())
            {
                 QName propertyQName = entry.getKey();
                 Serializable inboundValue = entry.getValue();
                 // Get the current property value
                 Serializable currentValue = currentProperties.get(propertyQName);
                 // Convert the inbound property value
                 inboundValue = convertInboundProperty(contentLocale, nodeRef, propertyQName, inboundValue, currentValue);
                 // Put the value into the map
                 convertedProperties.put(propertyQName, inboundValue);
            }
            // Now complete the call by passing the converted properties
            directNodeService.setProperties(nodeRef, convertedProperties);
            // Done
        }
        else if (methodName.equals("setProperty"))
        {
            //check if the property is of type MLText
            NodeRef nodeRef = (NodeRef) args[0];
            QName propertyQName = (QName) args[1];
            Serializable inboundValue = (Serializable) args[2];
            // Convert the property
            inboundValue = convertInboundProperty(contentLocale, nodeRef, propertyQName, inboundValue, null);
            // Pass this through to the node service
            directNodeService.setProperty(nodeRef, propertyQName, inboundValue);
            // Done
        }
        else
        {
            ret = invocation.proceed();
        }
        // done
        return ret;
    }
    
    /**
     * 
     * @param inboundValue      The value that must be set
     * @param currentValue      The current value of the property or <tt>null</tt> if not known
     * @return                  Returns a potentially converted property that conforms to the model
     */
    private Serializable convertInboundProperty(
            Locale contentLocale,
            NodeRef nodeRef,
            QName propertyQName,
            Serializable inboundValue,
            Serializable currentValue)
    {
        Serializable ret = null;
        PropertyDefinition propertyDef = this.dictionaryService.getProperty(propertyQName);
        //if no type definition associated to the name then just proceed
        if (propertyDef != null && propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
        {
            // Don't mess with multivalued properties or instances already of type MLText
            if (propertyDef.isMultiValued() || (inboundValue instanceof MLText) )
            {
                ret = inboundValue;
            }
            else
            {
                // This is a multilingual single-valued property
                // Get the current value from the node service, if not provided
                if (currentValue == null)
                {
                    currentValue = directNodeService.getProperty(nodeRef, propertyQName);
                }
                MLText currentMLValue = null;
                if (currentValue == null)
                {
                    currentMLValue = new MLText();
                }
                else
                {
                    currentMLValue = DefaultTypeConverter.INSTANCE.convert(MLText.class, currentValue);
                }
                // Force the inbound value to be a String (it isn't MLText)
                String inboundValueStr = DefaultTypeConverter.INSTANCE.convert(String.class, inboundValue);
                // Add it to the current MLValue
                currentMLValue.put(contentLocale, inboundValueStr);
                // Done
                ret = currentMLValue;
            }
        }
        else            // It is not defined as d:mltext in the dictionary
        {
            ret = inboundValue;
        }
        // Done
        if (logger.isDebugEnabled() && ret != inboundValue)
        {
            logger.debug("Converted inbound property: \n" +
                    "   NodeRef:    " + nodeRef + "\n" +
                    "   Property:   " + propertyQName + "\n" +
                    "   Before:     " + inboundValue + "\n" +
                    "   After:      " + ret);
        }
        return ret;
    }
}
