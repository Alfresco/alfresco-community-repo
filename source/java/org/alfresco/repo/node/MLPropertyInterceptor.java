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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

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
    private NodeService nodeService;
    
    /** Direct access to the MultilingualContentService */
    private MultilingualContentService multilingualContentService;
    
    /** Used to access property definitions */
    private DictionaryService dictionaryService;
    
    /**
     * Change the filtering behaviour of this interceptor on the curren thread.
     * Use this to switch off the filtering and just pass out properties as
     * handed out of the node service.
     * 
     * @param mlAwareVal <tt>true</tt> if the current thread is able to handle
     *      {@link MLText d:mltext} property types, otherwise <tt>false</tt>.
     * @return
     *      <tt>true</tt> if the current transaction is ML aware
     */
    static public boolean setMLAware(boolean mlAwareVal)
    {
        boolean wasMLAware = isMLAware();
        mlAware.set(Boolean.valueOf(mlAwareVal));
        return wasMLAware;
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

    public void setNodeService(NodeService bean)
    {
        this.nodeService = bean;
    }

    public void setMultilingualContentService(MultilingualContentService multilingualContentService)
    {
        this.multilingualContentService = multilingualContentService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    @SuppressWarnings("unchecked")
    public Object invoke(final MethodInvocation invocation) throws Throwable
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Intercepting method " + invocation.getMethod().getName() + " using content filter " + I18NUtil.getContentLocale());
        }
        
        // If isMLAware then no treatment is done, just return
        if (isMLAware())
        {    
            // Don't interfere
            return invocation.proceed();
        }
        
        Locale contentLangLocale = I18NUtil.getContentLocaleLang();
        
        Object ret = null;
        
        final String methodName = invocation.getMethod().getName();
        final Object[] args = invocation.getArguments();
        
        if (methodName.equals("getProperty"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            QName propertyQName = (QName) args[1];
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);
            
            // What locale must be used for filtering - ALF-3756 fix, ignore the country and variant
            Serializable value = (Serializable) invocation.proceed();
            ret = convertOutboundProperty(contentLangLocale, nodeRef, pivotNodeRef, propertyQName, value);
        }
        else if (methodName.equals("getProperties"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);
            
            Map<QName, Serializable> properties = (Map<QName, Serializable>) invocation.proceed();
            Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(properties.size() * 2);
            // Check each return value type
            for (Map.Entry<QName, Serializable> entry : properties.entrySet())
            {
                QName propertyQName = entry.getKey();
                Serializable value = entry.getValue();
                Serializable convertedValue = convertOutboundProperty(contentLangLocale, nodeRef, pivotNodeRef, propertyQName, value);
                // Add it to the return map
                convertedProperties.put(propertyQName, convertedValue);
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
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);

            // Get the current properties for the node
            Map<QName, Serializable> currentProperties = nodeService.getProperties(nodeRef);
            // Convert all properties
            Map<QName, Serializable> convertedProperties = convertInboundProperties(
                    currentProperties,
                    newProperties,
                    contentLangLocale,
                    nodeRef,
                    pivotNodeRef);
            // Now complete the call by passing the converted properties
            nodeService.setProperties(nodeRef, convertedProperties);
            // Done
        }
        else if (methodName.equals("addProperties"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            Map<QName, Serializable> newProperties =(Map<QName, Serializable>) args[1];
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);

            // Get the current properties for the node
            Map<QName, Serializable> currentProperties = nodeService.getProperties(nodeRef);
            // Convert all properties
            Map<QName, Serializable> convertedProperties = convertInboundProperties(
                    currentProperties,
                    newProperties,
                    contentLangLocale,
                    nodeRef,
                    pivotNodeRef);
            // Now complete the call by passing the converted properties
            nodeService.addProperties(nodeRef, convertedProperties);
            // Done
        }
        else if (methodName.equals("setProperty"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            QName propertyQName = (QName) args[1];
            Serializable inboundValue = (Serializable) args[2];
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);
            
            // Convert the property
            inboundValue = convertInboundProperty(contentLangLocale, nodeRef, pivotNodeRef, propertyQName, inboundValue, null);
            
            // Pass this through to the node service
            nodeService.setProperty(nodeRef, propertyQName, inboundValue);
            // Done
        }
        else if (methodName.equals("createNode") && args.length > 4)
        {
            NodeRef parentNodeRef = (NodeRef) args[0];
            QName assocTypeQName = (QName) args[1];
            QName assocQName = (QName) args[2];
            QName nodeTypeQName = (QName) args[3];
            Map<QName, Serializable> newProperties =(Map<QName, Serializable>) args[4];
            if (newProperties == null)
            {
                newProperties = Collections.emptyMap();
            }
            NodeRef nodeRef = null;                 // Not created yet
            
            // No pivot
            NodeRef pivotNodeRef = null;

            // Convert all properties
            Map<QName, Serializable> convertedProperties = convertInboundProperties(
                    null,
                    newProperties,
                    contentLangLocale,
                    nodeRef,
                    pivotNodeRef);
            // Now complete the call by passing the converted properties
            ret = nodeService.createNode(parentNodeRef, assocTypeQName, assocQName, nodeTypeQName, convertedProperties);
            // Done
        }
        else if (methodName.equals("addAspect") && args[2] != null)
        {
            NodeRef nodeRef = (NodeRef) args[0];
            QName aspectTypeQName = (QName) args[1];
            
            // Get the pivot translation, if appropriate
            NodeRef pivotNodeRef = getPivotNodeRef(nodeRef);

            Map<QName, Serializable> newProperties =(Map<QName, Serializable>) args[2];
            // Get the current properties for the node
            Map<QName, Serializable> currentProperties = nodeService.getProperties(nodeRef);
            // Convert all properties
            Map<QName, Serializable> convertedProperties = convertInboundProperties(
                    currentProperties,
                    newProperties,
                    contentLangLocale,
                    nodeRef,
                    pivotNodeRef);
            // Now complete the call by passing the converted properties
            nodeService.addAspect(nodeRef, aspectTypeQName, convertedProperties);
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
     * @param nodeRef
     *      a potential empty translation
     * @return
     *      the pivot translation node or <tt>null</tt>
     */
    private NodeRef getPivotNodeRef(NodeRef nodeRef)
    {
        if (nodeRef == null)
        {
            throw new IllegalArgumentException("NodeRef may not be null for calls to NodeService.  Check client code.");
        }
        if (!nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM) && nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
        {
            return multilingualContentService.getPivotTranslation(nodeRef);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Ensure that content is spoofed for empty translations.
     */
    private Serializable convertOutboundProperty(
            Locale contentLocale,
            NodeRef nodeRef,
            NodeRef pivotNodeRef,
            QName propertyQName,
            Serializable outboundValue)
    {
        Serializable ret = null;
        if (outboundValue == null)
        {
           ret = null;
        }
        if (outboundValue instanceof MLText)
        {
            // It is MLText
            MLText mlText = (MLText) outboundValue;
            ret = mlText.getClosestValue(contentLocale);
        }
        else if(isCollectionOfMLText(outboundValue))
        {
            Collection<?> col = (Collection<?>)outboundValue; 
            ArrayList<String> answer = new ArrayList<String>(col.size());
            Locale closestLocale = getClosestLocale(col, contentLocale);
            for(Object o : col)
            {
                MLText mlText = (MLText) o;
                String value = mlText.get(closestLocale);
                if(value != null)
                {
                    answer.add(value);
                }
            }
            ret = answer;
        }
        else if (pivotNodeRef != null)       // It is an empty translation
        {
           if (propertyQName.equals(ContentModel.PROP_MODIFIED))
           {
              // An empty translation's modified date must be the later of its own
              // modified date and the pivot translation's modified date
              Date emptyLastModified = (Date) outboundValue;
              Date pivotLastModified = (Date) nodeService.getProperty(pivotNodeRef, ContentModel.PROP_MODIFIED);
              if (emptyLastModified.compareTo(pivotLastModified) < 0)
              {
                 ret = pivotLastModified;
              }
              else
              {
                 ret = emptyLastModified;
              }
           }
           else if (propertyQName.equals(ContentModel.PROP_CONTENT))
           {
              // An empty translation's cm:content must track the cm:content of the
              // pivot translation.
              ret = nodeService.getProperty(pivotNodeRef, ContentModel.PROP_CONTENT);
           }
           else
           {
              ret = outboundValue;
           }
        }
        else
        {
            ret = outboundValue;
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Converted outbound property: \n" +
                    "   NodeRef:        " + nodeRef + "\n" +
                    "   Property:       " + propertyQName + "\n" +
                    "   Before:         " + outboundValue + "\n" +
                    "   After:          " + ret);
        }
        return ret;
    }
    
    public Locale getClosestLocale(Collection<?> collection, Locale locale)
    {
        if (collection.size() == 0)
        {
            return null;
        }
        // Use the available keys as options
        HashSet<Locale> locales = new HashSet<Locale>();
        for(Object o : collection)
        {
            MLText mlText = (MLText)o;
            locales.addAll(mlText.keySet());
        }
        // Get a match
        Locale match = I18NUtil.getNearestLocale(locale, locales);
        if (match == null)
        {
            // No close matches for the locale - go for the default locale
            locale = I18NUtil.getLocale();
            match = I18NUtil.getNearestLocale(locale, locales);
            if (match == null)
            {
                // just get any locale
                match = I18NUtil.getNearestLocale(null, locales);
            }
        }
        return match;
    }
    
    /**
     * @param outboundValue
     * @return
     */
    private boolean isCollectionOfMLText(Serializable outboundValue)
    {
        if(outboundValue instanceof Collection<?>)
        {
            for(Object o : (Collection<?>)outboundValue)
            {
                if(!(o instanceof MLText))
                {
                    return false;
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    private Map<QName, Serializable> convertInboundProperties(
            Map<QName, Serializable> currentProperties,
            Map<QName, Serializable> newProperties,
            Locale contentLocale,
            NodeRef nodeRef,
            NodeRef pivotNodeRef)
    {
        Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(newProperties.size() * 2);
        for (Map.Entry<QName, Serializable> entry : newProperties.entrySet())
        {
             QName propertyQName = entry.getKey();
             Serializable inboundValue = entry.getValue();
             // Get the current property value
             Serializable currentValue = currentProperties == null ? null : currentProperties.get(propertyQName);
             // Convert the inbound property value
             inboundValue = convertInboundProperty(contentLocale, nodeRef, pivotNodeRef, propertyQName, inboundValue, currentValue);
             // Put the value into the map
             convertedProperties.put(propertyQName, inboundValue);
        }
        return convertedProperties;
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
            NodeRef pivotNodeRef,
            QName propertyQName,
            Serializable inboundValue,
            Serializable currentValue)
    {
        Serializable ret = null;
        PropertyDefinition propertyDef = this.dictionaryService.getProperty(propertyQName);
        //if no type definition associated to the name then just proceed
        if (propertyDef == null)
        {
           ret = inboundValue;
        }
        else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
        {
            // Don't mess with multivalued properties or instances already of type MLText
            if (inboundValue instanceof MLText)
            {
                ret = inboundValue;
            }
            else if(propertyDef.isMultiValued())
            {
                // leave collectios of ML text alone
                if(isCollectionOfMLText(inboundValue))
                {
                    ret = inboundValue;
                }
                else
                {
                    // Anything else we assume is localised
                    if (currentValue == null && nodeRef != null)
                    {
                        currentValue = nodeService.getProperty(nodeRef, propertyQName);
                    }
                    ArrayList<MLText> returnMLList = new ArrayList<MLText>();
                    if (currentValue != null)
                    {
                        Collection<MLText> currentCollection = DefaultTypeConverter.INSTANCE.getCollection(MLText.class, currentValue);  
                        returnMLList.addAll(currentCollection);
                    }
                    Collection<String> inboundCollection = DefaultTypeConverter.INSTANCE.getCollection(String.class, inboundValue);
                    int count = 0;
                    for(String current : inboundCollection)
                    {
                        MLText newMLValue;
                        if(count < returnMLList.size())
                        { 
                            MLText currentMLValue = returnMLList.get(count);
                            newMLValue = new MLText();
                            if (currentMLValue != null)
                            {
                                newMLValue.putAll(currentMLValue);
                            }                
                        }
                        else
                        {
                            newMLValue = new MLText();
                        }
                        newMLValue.addValue(contentLocale, current);
                        if(count < returnMLList.size())
                        {
                            returnMLList.set(count, newMLValue);
                        }
                        else
                        {
                            returnMLList.add(newMLValue);
                        }
                        count++;
                    }
                    // remove locale settings for anything after
                    for(int i = count; i < returnMLList.size(); i++)
                    {
                        MLText currentMLValue = returnMLList.get(i);
                        MLText newMLValue = new MLText();
                        if (currentMLValue != null)
                        {
                            newMLValue.putAll(currentMLValue);
                        }
                        newMLValue.remove(contentLocale);
                        returnMLList.set(i, newMLValue);
                    }
                    // tidy up empty locales
                    ArrayList<MLText> tidy = new ArrayList<MLText>();
                    for(MLText mlText : returnMLList)
                    {
                        if(mlText.keySet().size() > 0)
                        {
                            tidy.add(mlText);
                        }
                    }
                    ret = tidy;
                }
            }
            else
            {
                // This is a multilingual single-valued property
                // Get the current value from the node service, if not provided
                if (currentValue == null && nodeRef != null)
                {
                    currentValue = nodeService.getProperty(nodeRef, propertyQName);
                }
                MLText returnMLValue = new MLText();
                if (currentValue != null)
                {
                    MLText currentMLValue = DefaultTypeConverter.INSTANCE.convert(MLText.class, currentValue);
                    returnMLValue.putAll(currentMLValue);                   
                }
                // Force the inbound value to be a String (it isn't MLText)
                String inboundValueStr = DefaultTypeConverter.INSTANCE.convert(String.class, inboundValue);
                // Add it to the current MLValue
                returnMLValue.put(contentLocale, inboundValueStr);
                // Done
                ret = returnMLValue;
            }
        }
        else if (pivotNodeRef != null && propertyQName.equals(ContentModel.PROP_CONTENT))
        {
           // It is an empty translation.  The content must not change if it matches
           // the content of the pivot translation
           ContentData pivotContentData = (ContentData) nodeService.getProperty(pivotNodeRef, ContentModel.PROP_CONTENT);
           ContentData emptyContentData = (ContentData) inboundValue;
           String pivotContentUrl = pivotContentData == null ? null : pivotContentData.getContentUrl();
           String emptyContentUrl = emptyContentData == null ? null : emptyContentData.getContentUrl();
           if (EqualsHelper.nullSafeEquals(pivotContentUrl, emptyContentUrl))
           {
              // They are a match.  So the empty translation must be reset to it's original value
              ret = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
           }
           else
           {
              ret = inboundValue;
           }
        }
        else
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
