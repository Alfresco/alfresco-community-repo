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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A method interceptor to clean up node ref properties as they are passed in and out of the node service. For
 * getProperty and getProperies calls invalid node refs are removed from the returned set (they appear to have be
 * cleaned up). For setProperty and setProperties calls invalid node refs are removed and thus not set. It only
 * considers properties of type d:noderef.
 * 
 * @author andyh
 */
public class NodeRefPropertyMethodInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(NodeRefPropertyMethodInterceptor.class);

    private boolean filterOnGet = true;

    private boolean filterOnSet = true;

    private DictionaryService dictionaryService;

    private NodeService nodeService;

    public boolean isFilterOnGet()
    {
        return filterOnGet;
    }

    public void setFilterOnGet(boolean filterOnGet)
    {
        this.filterOnGet = filterOnGet;
    }

    public boolean isFilterOnSet()
    {
        return filterOnSet;
    }

    public void setFilterOnSet(boolean filterOnSet)
    {
        this.filterOnSet = filterOnSet;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        String methodName = invocation.getMethod().getName();

        // We are going to change the method arguments as we proceed - so we keep them to set the references back at the
        // end
        // Not sure if there would be any side effect but we guard against it in any case.
        // Audit for example will see the correct values on exit
        // org.springframework.aop.framework.ReflectiveMethodInvocation does not do any special wrapping and this is
        // fine

        Object[] args = invocation.getArguments();
        Object[] in = new Object[args.length];
        System.arraycopy(args, 0, in, 0, args.length);
        invocation.getStaticPart();

        try
        {
            if (methodName.equals("addAspect"))
            {
                if (filterOnSet)
                {
                    NodeRef nodeRef = (NodeRef) args[0];
                    QName aspectType = (QName) args[1];
                    Map<QName, Serializable> newProperties = (Map<QName, Serializable>) args[2];

                    if (newProperties == null)
                    {
                        args[2] = newProperties;
                        return invocation.proceed();
                    }
                    else
                    {

                        Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(newProperties.size() * 2);
                        for (Map.Entry<QName, Serializable> entry : newProperties.entrySet())
                        {
                            QName propertyQName = entry.getKey();
                            Serializable value = entry.getValue();
                            value = getValue(propertyQName, value);
                            convertedProperties.put(propertyQName, value);
                        }
                        args[2] = convertedProperties;
                        return invocation.proceed();
                    }
                }
                else
                {
                    return invocation.proceed();
                }
            }
            else if (methodName.equals("createNode") & (args.length == 5))
            {
                if (filterOnSet)
                {
                    NodeRef parentRef = (NodeRef) args[0];
                    QName assocTypeQName = (QName) args[1];
                    QName assocQName = (QName) args[2];
                    QName nodeTypeQName = (QName) args[3];
                    Map<QName, Serializable> newProperties = (Map<QName, Serializable>) args[4];
                    if (newProperties == null)
                    {
                        args[4] = newProperties;
                        return invocation.proceed();
                    }
                    else
                    {

                        Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(newProperties.size() * 2);
                        for (Map.Entry<QName, Serializable> entry : newProperties.entrySet())
                        {
                            QName propertyQName = entry.getKey();
                            Serializable value = entry.getValue();
                            value = getValue(propertyQName, value);
                            convertedProperties.put(propertyQName, value);
                        }
                        args[4] = newProperties;
                        return invocation.proceed();
                    }
                }
                else
                {
                    return invocation.proceed();
                }
            }
            else if (methodName.equals("getProperty"))
            {
                if (filterOnGet)
                {
                    NodeRef nodeRef = (NodeRef) args[0];
                    QName propertyQName = (QName) args[1];

                    Serializable value = (Serializable) invocation.proceed();
                    return getValue(propertyQName, value);
                }
                else
                {
                    return invocation.proceed();
                }
            }
            else if (methodName.equals("getProperties"))
            {
                if (filterOnGet)
                {
                    NodeRef nodeRef = (NodeRef) args[0];

                    Map<QName, Serializable> properties = (Map<QName, Serializable>) invocation.proceed();
                    Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(properties.size() * 2);
                    for (Map.Entry<QName, Serializable> entry : properties.entrySet())
                    {
                        QName propertyQName = entry.getKey();
                        Serializable value = entry.getValue();
                        Serializable convertedValue = getValue(propertyQName, value);
                        convertedProperties.put(propertyQName, convertedValue);
                    }
                    return convertedProperties;
                }
                else
                {
                    return invocation.proceed();
                }
            }
            else if (methodName.equals("setProperties"))
            {
                if (filterOnSet)
                {
                    NodeRef nodeRef = (NodeRef) args[0];

                    Map<QName, Serializable> newProperties = (Map<QName, Serializable>) args[1];
                    Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(newProperties.size() * 2);
                    for (Map.Entry<QName, Serializable> entry : newProperties.entrySet())
                    {
                        QName propertyQName = entry.getKey();
                        Serializable value = entry.getValue();
                        value = getValue(propertyQName, value);
                        convertedProperties.put(propertyQName, value);
                    }
                    args[1] = convertedProperties;
                    return invocation.proceed();
                }
                else
                {
                    return invocation.proceed();
                }
            }
            else if (methodName.equals("setProperty"))
            {
                if (filterOnSet)
                {
                    NodeRef nodeRef = (NodeRef) args[0];
                    QName propertyQName = (QName) args[1];
                    Serializable value = (Serializable) args[2];
                    value = getValue(propertyQName, value);
                    args[2] = value;
                    return invocation.proceed();
                }
                else
                {
                    return invocation.proceed();
                }
            }
            else
            {
                return invocation.proceed();
            }

        }
        finally
        {
            System.arraycopy(in, 0, args, 0, in.length);
        }
    }

    /**
     * Remove unknown node ref values Remove unknowen categories - the node will be removed if it does exist and it is
     * not a category
     * 
     * @param propertyQName
     * @param inboundValue
     * @return
     */
    private Serializable getValue(QName propertyQName, Serializable inboundValue)
    {
        PropertyDefinition propertyDef = this.dictionaryService.getProperty(propertyQName);
        if (propertyDef == null)
        {
            return inboundValue;
        }
        else
        {
            if ((propertyDef.getDataType().getName().equals(DataTypeDefinition.NODE_REF)) || (propertyDef.getDataType().getName().equals(DataTypeDefinition.CATEGORY)))
            {
                if (inboundValue instanceof Collection)
                {
                    Collection in = (Collection) inboundValue;
                    ArrayList<NodeRef> out = new ArrayList<NodeRef>(in.size());
                    for (Object o : in)
                    {
                        Serializable value = (Serializable) o;
                        if (value == null)
                        {
                            out.add(null);
                        }
                        else
                        {
                            try
                            {
                                NodeRef test = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, value);
                                if (nodeService.exists(test))
                                {
                                    if (propertyDef.getDataType().getName().equals(DataTypeDefinition.CATEGORY))
                                    {
                                        QName type = nodeService.getType(test);
                                        if (dictionaryService.isSubClass(type, ContentModel.TYPE_CATEGORY))
                                        {
                                            out.add(test);
                                        }
                                    }
                                    else
                                    {
                                        out.add(test);
                                    }
                                }
                            }
                            catch (TypeConversionException e)
                            {
                                // Catch and continue
                            }
                        }
                    }
                    return out;
                }
                else
                {
                    if (inboundValue == null)
                    {
                        return inboundValue;
                    }
                    else
                    {
                        try
                        {
                            NodeRef test = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, inboundValue);
                            if (nodeService.exists(test))
                            {
                                if (propertyDef.getDataType().getName().equals(DataTypeDefinition.CATEGORY))
                                {
                                    QName type = nodeService.getType(test);
                                    if (dictionaryService.isSubClass(type, ContentModel.TYPE_CATEGORY))
                                    {
                                        return inboundValue;
                                    }
                                    else
                                    {
                                        return null;
                                    }
                                }
                                else
                                {
                                    return inboundValue;
                                }
                            }
                            else
                            {
                                return null;
                            }
                        }

                        catch (TypeConversionException e)
                        {
                            return null;
                        }
                    }
                }
            }
            else
            {
                return inboundValue;
            }
        }
    }
}
