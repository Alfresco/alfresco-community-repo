/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Interceptor that enforces restrictions on updates of properties of type content (cm:content and alike) on NodeService.
 * <p/>
 * Can be configured by using global enabled flag - <code>contentPropertyRestrictions.enabled</code>,
 * or comma separated white list of callers <code>contentPropertyRestrictions.whitelist</code>,
 * which can be either packages or fully qualified class names.
 *
 * @author Alex Mukha
 * @author David Edwards
 */
public class ContentPropertyRestrictionInterceptor implements MethodInterceptor
{
    private DictionaryService dictionaryService;
    private NodeService internalNodeService;
    private Set<String> globalContentPropertyRestrictionWhiteList;
    private boolean globalContentPropertyRestrictions = true;

    @SuppressWarnings("deprecation")
    private Class[] defaultWhiteList = new Class[]
            {
                    org.alfresco.filesys.repo.CifsHelper.class,
                    org.alfresco.filesys.repo.ContentDiskDriver.class,
                    org.alfresco.filesys.repo.ContentDiskDriver2.class,
                    org.alfresco.filesys.repo.ContentNetworkFile.class,
                    org.alfresco.opencmis.AlfrescoCmisServiceImpl.class,
                    org.alfresco.repo.action.ActionServiceImpl.class,
                    org.alfresco.repo.action.executer.ContentMetadataExtracter.class,
                    org.alfresco.repo.coci.WorkingCopyAspect.class,
                    org.alfresco.repo.copy.CopyServiceImpl.class,
                    org.alfresco.repo.forms.processor.node.ContentModelFormProcessor.class,
                    org.alfresco.repo.forum.CommentServiceImpl.class,
                    org.alfresco.repo.importer.FileImporterImpl.class,
                    org.alfresco.repo.jscript.ScriptNode.ScriptContentData.class,
                    org.alfresco.repo.rendition.RenditionNodeManager.class,
                    org.alfresco.repo.transfer.RepoPrimaryManifestProcessorImpl.class,
                    org.alfresco.repo.version.Version2ServiceImpl.class,
                    org.alfresco.repo.workflow.WorkflowDeployer.class
            };

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setGlobalContentPropertyRestrictions(boolean globalContentPropertyRestrictions)
    {
        this.globalContentPropertyRestrictions = globalContentPropertyRestrictions;
    }

    public void setGlobalContentPropertyRestrictionWhiteList(String whitelist)
    {
        this.globalContentPropertyRestrictionWhiteList = getWhiteList(whitelist);
    }

    public void setNodeService(NodeService nodeService)
    {
        this.internalNodeService = nodeService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        String methodName = invocation.getMethod().getName();
        Object[] args = invocation.getArguments();

        if (globalContentPropertyRestrictions && !isCallerWhiteListed())
        {
            if (methodName.equals("setProperties"))
            {
                Map<QName, Serializable> properties = args[1] != null ?
                        Collections.unmodifiableMap((Map<QName, Serializable>) args[1]) : Collections.emptyMap();
                NodeRef nodeRef = (NodeRef) args[0];
                if (nodeRef != null)
                {
                    for (QName propQname : properties.keySet())
                    {
                        if (isContentProperty(propQname, properties.get(propQname)) &&
                                isContentNotNullOrEmpty(properties.get(propQname)) &&
                                isContentChanged(nodeRef, propQname, properties.get(propQname)))
                        {
                            throw new InvalidTypeException("The node's content can't be updated via NodeService#setProperties directly: \n" +
                                    "   node: " + args[0] + "\n" +
                                    "   property name: " + propQname.getLocalName(), propQname);
                        }
                    }
                }
            }
            else if (methodName.equals("addProperties"))
            {
                Map<QName, Serializable> properties = args[1] != null ?
                        Collections.unmodifiableMap((Map<QName, Serializable>) args[1]) : Collections.emptyMap();
                NodeRef nodeRef = (NodeRef) args[0];
                if (nodeRef != null)
                {
                    for (QName propQname : properties.keySet())
                    {
                        if (isContentProperty(propQname, properties.get(propQname)) &&
                                isContentNotNullOrEmpty(properties.get(propQname)) &&
                                isContentChanged(nodeRef, propQname, properties.get(propQname)))
                        {
                            throw new InvalidTypeException("The node's content can't be updated via NodeService#addProperties directly: \n" +
                                    "   node: " + args[0] + "\n" +
                                    "   property name: " + propQname.getLocalName(), propQname);
                        }
                    }
                }
            }
            else if (methodName.equals("createNode") && args.length == 5)
            {
                Map<QName, Serializable> properties = args[4] != null ?
                        Collections.unmodifiableMap((Map<QName, Serializable>) args[4]) : Collections.emptyMap();
                for (QName propQname : properties.keySet())
                {
                    if (isContentProperty(propQname, properties.get(propQname)) && isContentNotNullOrEmpty(properties.get(propQname)))
                    {
                        throw new InvalidTypeException("The node's content can't be updated via NodeService#createNode directly: \n" +
                                "   node: " + args[0] + "\n" +
                                "   property name: " + propQname.getLocalName(), propQname);
                    }
                }
            }
            else if (methodName.equals("setProperty"))
            {
                QName propQname = (QName) args[1];
                Serializable value = (Serializable) args[2];
                NodeRef nodeRef = (NodeRef) args[0];
                if (nodeRef != null)
                {
                    if (isContentProperty(propQname, value) &&
                            isContentNotNullOrEmpty(value) &&
                            isContentChanged(nodeRef, propQname, value))
                    {
                        throw new InvalidTypeException("The node's content can't be updated via NodeService#setProperty directly: \n" +
                                "   node: " + args[0] + "\n" +
                                "   property name: " + propQname.getLocalName(), propQname);
                    }
                }
            }
            else if (methodName.equals("addAspect"))
            {
                Map<QName, Serializable> properties = args[2] != null ?
                        Collections.unmodifiableMap((Map<QName, Serializable>) args[2]) : Collections.emptyMap();
                NodeRef nodeRef = (NodeRef) args[0];
                if (nodeRef != null)
                {
                    for (QName propQname : properties.keySet())
                    {
                        if (isContentProperty(propQname, properties.get(propQname)) &&
                                isContentNotNullOrEmpty(properties.get(propQname)) &&
                                isContentChanged(nodeRef, propQname, properties.get(propQname)))
                        {
                            throw new InvalidTypeException("The node's content can't be updated via NodeService#addAspect directly: \n" +
                                    "   node: " + args[0] + "\n" +
                                    "   property name: " + propQname.getLocalName(), propQname);
                        }
                    }
                }
            }
        }

        return invocation.proceed();
    }

    private boolean isContentChanged(NodeRef nodeRef, QName qname, Serializable newValue)
    {
        Serializable existingValue = internalNodeService.getProperty(nodeRef, qname);
        if (existingValue == null)
        {
            return newValue != null;
        }
        else
        {
            if (newValue instanceof ContentData && existingValue instanceof ContentData)
            {
                // ContentData may be changed in mimetype, locale, size or encoding
                String existingUrl = ((ContentData) existingValue).getContentUrl();
                String newUrl = ((ContentData) newValue).getContentUrl();
                return !Objects.equals(existingUrl, newUrl);
            }
            else
            {
                return !existingValue.equals(newValue);
            }
        }
    }

    private boolean isContentNotNullOrEmpty(Serializable propValue)
    {
        if (propValue == null)
        {
            return false;
        }
        if (propValue instanceof ContentData)
        {
            String contentUrl = ((ContentData) propValue).getContentUrl();
            return contentUrl != null && !contentUrl.isEmpty();
        }
        return true;
    }

    private boolean isContentProperty(QName propertyQName, Serializable propValue)
    {
        if (propValue instanceof ContentData)
        {
            return true;
        }

        PropertyDefinition contentPropDef = dictionaryService.getProperty(propertyQName);
        return contentPropDef != null && contentPropDef.getDataType().getName().equals(DataTypeDefinition.CONTENT);
    }

    private boolean isCallerWhiteListed()
    {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        Optional<String> callerClass = walker.walk(s ->
                s.map(StackWalker.StackFrame::getDeclaringClass)
                        .map(Class::getName)
                        .filter(globalContentPropertyRestrictionWhiteList::contains)
                        .findFirst());

        return callerClass.isPresent();
    }

    private Set<String> getWhiteList(String whiteList)
    {
        Set<String> whiteListSet = new HashSet<>();

        whiteList = whiteList == null ? "" : whiteList.trim();
        if(whiteList.length() > 0)
        {
            String[] classes = whiteList.split(",");
            for (String className : classes)
            {
                className = className.trim();

                whiteListSet.add(className);
            }
        }

        // Combine default list with property values
        Set.of(defaultWhiteList).forEach(item -> whiteListSet.add(item.getName()));

        return whiteListSet;
    }
}
