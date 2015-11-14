/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.store;

import java.util.Set;
import java.util.regex.PatternSyntaxException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.config.NodeRefExpression;
import org.alfresco.repo.virtual.ref.Protocols;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Content type and aspect based virtualization strategy. <br>
 * Virtualizes nodes by associating their types or aspects with a template. <br>
 * A type or an aspect {@link QName} prefixed string form is associated with a
 * vanilla template that has the same name as the type or aspect prefixed name
 * with ':' replaced by '_' and it is <code>.json</code> postfixed. The template
 * is located at predefined templates repository path. <br>
 * Example: <br>
 * If the templates repository path is
 * <code>Data Dictionary/Virtual Folders</code> the <code>cm:author</code>
 * aspect will be associated with a vanilla template found by the
 * <code>Data Dictionary/Virtual Folders/cm_author.json</code> path. <br>
 * Considering all aspects for vitualization can degrade performance so the set
 * of aspects considered for virtualization can be limited to a predefined
 * accepted {@link QName} prefix set by setting a comma separated list of
 * accepted prefixes through {@link #setAspectPrefixFilter(String)}.
 * 
 * @author Bogdan Horje
 */
public class TypeVirtualizationMethod extends TemplateVirtualizationMethod
{
    private static Log logger = LogFactory.getLog(TypeVirtualizationMethod.class);

    private NamespacePrefixResolver namespacePrefixResolver;

    private NodeRefExpression templatesPath;

    private QNamePattern qnamePattern = RegexQNamePattern.MATCH_ALL;

    /**
     * Thread local solving template in process indicator.<br>
     * Used to prevent infinite recursion when solving templates within type
     * virtualized folders.
     */
    private ThreadLocal<Boolean> solvingTemplate = new ThreadLocal<Boolean>()
    {
        protected Boolean initialValue()
        {
            return false;
        };
    };

    public void init()
    {
        //void
    }
    
    public void setQnameFilterRegexp(String regexp)
    {
        this.qnamePattern = new RegexQNamePattern(regexp);
    }

    public void setTemplatesPath(NodeRefExpression templatesPath)
    {
        this.templatesPath = templatesPath;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver resolver)
    {
        this.namespacePrefixResolver = resolver;
    }

    @Override
    public boolean canVirtualize(ActualEnvironment env, NodeRef nodeRef) throws ActualEnvironmentException
    {
        if (solvingTemplate.get())
        {
            return false;
        }

        solvingTemplate.set(true);
        try
        {
            return templateNodeFor(env,
                                   nodeRef) != null;
        }
        finally
        {
            solvingTemplate.set(false);
        }

    }

    private String templateNodeNameForType(QName type)
    {
        String extension = ".json";
        String typePrefixString = type.toPrefixString(namespacePrefixResolver);
        String typeTemplateContentName = typePrefixString.replaceAll(":",
                                                                     "_");
        return typeTemplateContentName + extension;
    }

    private NodeRef templateNodeFor(ActualEnvironment env, NodeRef nodeRef)
    {
        try
        {
            NodeRef templatesContainerNode = templatesPath.resolve();
            if (templatesContainerNode == null)
            {
                return null;
            }
            NodeRef templateNode = null;
            QName nodeType = env.getType(nodeRef);
            if (qnamePattern.isMatch(nodeType))
            {
                String typeTemplateNodeName = templateNodeNameForType(nodeType);

                templateNode = env.getChildByName(templatesContainerNode,
                                                  ContentModel.ASSOC_CONTAINS,
                                                  typeTemplateNodeName);
            }

            if (templateNode == null)
            {
                Set<QName> aspects = env.getAspects(nodeRef);
                for (QName aspect : aspects)
                {

                    if (qnamePattern.isMatch(aspect))
                    {
                        String aspectTemplateNodeName = templateNodeNameForType(aspect);
                        templateNode = env.getChildByName(templatesContainerNode,
                                                          ContentModel.ASSOC_CONTAINS,
                                                          aspectTemplateNodeName);

                        if (templateNode != null)
                        {
                            break;
                        }
                    }

                }
            }

            return templateNode;
        }
        catch (PatternSyntaxException e)
        {
            logger.error("Invalid type methof type and aspect qName regexp pattern " + qnamePattern.toString());
            return null;
        }
        catch (Exception e)
        {
            logger.error("Type virtualization template search failed.",
                         e);
            return null;
        }
    }

    @Override
    public Reference virtualize(ActualEnvironment env, NodeRef nodeRef) throws VirtualizationException
    {
        if (solvingTemplate.get())
        {
            throw new VirtualizationException("Concurrent virtualization!");
        }

        try
        {
            solvingTemplate.set(true);

            NodeRef templateNode = templateNodeFor(env,
                                                   nodeRef);
            return newVirtualReference(Protocols.VANILLA.protocol,
                                       templateNode,
                                       nodeRef);
        }
        catch (VirtualizationException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new VirtualizationException(e);
        }
        finally
        {
            solvingTemplate.set(false);
        }
    }
}
