/*
 * Copyright (C) 2007 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
        Locale contentLocale = I18NUtil.getContentLocale();
        Object ret = null;
        
        // If isMLAware then no treatment is done, just return
        if(isMLAware())
        {    
            // Don't interfere
            return invocation.proceed();
        }
        
        String methodName = invocation.getMethod().getName();
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
            // done
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
            //get the raw properties and for every multi lingual property, 
            //take the value transmited as parameter and if type string put the value in
            //the multilingual property.  If not just transfer property value
            NodeRef node = (NodeRef)invocation.getArguments()[0];
            //new properties
            Map<QName, Serializable> newProperties =(Map<QName, Serializable>)(invocation.getArguments()[1]);
            //get the raw property values
            Map<QName, Serializable> previousProperties = directNodeService.getProperties(node);
            //merge with previous properties only for MLText ones
            Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(newProperties.size() * 2);              
            for (Map.Entry<QName, Serializable> entry : newProperties.entrySet())
            {
                 QName key = entry.getKey();
                 PropertyDefinition propertyDef = this.dictionaryService.getProperty(key);
                 if (propertyDef == null)
                 {
                     //no type found in the model, just transfer
                     convertedProperties.put(key,newProperties.get(key));
                     continue;
                 }
                 //if incoming new property is MLText(based on the model) then 
                 //transfer or merge depending on the incoming concrete type.
                 //if incoming type is String and model indicates MLText then merge if something else just transfer
                 if(propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT) && 
                         newProperties.get(key) instanceof String )
                 {
                     //get back previous value it should a MLText or null
                     
                     Serializable preVal = previousProperties.get(key);
                     MLText newVal = new MLText();
                     if (preVal == null || !(preVal instanceof MLText))
                     {
                         //second test is in case of the value was set before in the system without MLText
                         //create a new MLText and transfer
                         //if prevval wasn't null save value with as LOCAL.ENGLISH (purely arbitrary) to avoid any 
                         //information loss
                         if(preVal!= null)
                             newVal.addValue(Locale.ENGLISH,(String)preVal);
                     }
                     else
                     {
                        newVal = (MLText)preVal; 
                     }
                     //use alternate locale
                      newVal.addValue(
                              contentLocale,
                              (String)newProperties.get(key));
                     //transfer
                     convertedProperties.put(key,newVal);
                     continue;
                 }
                 //normal process, just transfer
                 convertedProperties.put(key,newProperties.get(key));
            }//for
            directNodeService.setProperties(node, convertedProperties);
        }
        else if (methodName.equals("setProperty"))
        {
            //check if the property is of type MLText
            QName qPropName = (QName)invocation.getArguments()[1];
            PropertyDefinition propertyDef = this.dictionaryService.getProperty(qPropName);
            //if no type definition associated to the name then just proceed
            if (propertyDef != null && propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
            {
                
                //check if property is multi valued or actual set value is MLText
                // in that case delegate deletate
                if (propertyDef.isMultiValued() || (invocation.getArguments()[2] instanceof MLText) )
                {
                    ret = invocation.proceed();
                }
                else
                {
                    //this is a multilingual mono valued property
                    //then get the MLText value and set the linguistic value conrresponding 
                    //of the preferate language
                    NodeRef node = (NodeRef)invocation.getArguments()[0];
                    MLText mlPropertyValue = (MLText)directNodeService.getProperty(node, qPropName);
                    if (mlPropertyValue == null)
                    {
                        mlPropertyValue = new MLText();
                    }
                    mlPropertyValue.addValue(
                            contentLocale,
                            (String) invocation.getArguments()[2]);
                    //set the value following according to the current locale
                    directNodeService.setProperty(node, qPropName, mlPropertyValue);
                }
            }
            else
            {
                ret = invocation.proceed();
            }
        }
        else
        {
            ret = invocation.proceed();
        }
        // done
        return ret;
    }
}
