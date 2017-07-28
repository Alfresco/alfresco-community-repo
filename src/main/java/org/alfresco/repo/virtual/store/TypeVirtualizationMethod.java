/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.virtual.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.config.NodeRefExpression;
import org.alfresco.repo.virtual.ref.Protocols;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
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

    private String filters;

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
        // resetFilters();
    }

    private QNamePattern[] createFilters()
    {
        QNamePattern[] qnamePatternFilters = new QNamePattern[] {};

        if (namespacePrefixResolver != null && filters != null)
        {
            qnamePatternFilters = asRegExpQNamePatternFilters(filters);
        }
        else
        {
            logger.debug("Could not reset qName filters with NameSpacePrefixResolver=" + namespacePrefixResolver
                        + " and filters=" + filters);
        }

        return qnamePatternFilters;
    }

    private QNamePattern[] asRegExpQNamePatternFilters(String filtersString)
    {
        String[] filters = filtersString.split(",");
        List<QNamePattern> patterns = new ArrayList<>(3);
        for (int i = 0; i < filters.length; i++)
        {
            String trimmedFilters = filters[i].trim();
            if (!trimmedFilters.isEmpty())
            {
                if ("*".equals(trimmedFilters))
                {
                    patterns.clear();
                    patterns.add(RegexQNamePattern.MATCH_ALL);
                    break;
                }

                if ("none".equals(trimmedFilters))
                {
                    patterns.clear();
                    break;
                }

                String[] components = filters[i].split(":");
                if (components == null || components.length != 2 || components[0].trim().isEmpty()
                            || components[1].trim().isEmpty())
                {
                    throw new IllegalArgumentException("Illegal filters string " + filtersString
                                + ". Expected <prefix>:<name> | <prefix>:'*' instead of " + filters[i]);
                }
                try
                {
                    String uri = namespacePrefixResolver.getNamespaceURI(components[0]);

                    if (uri == null)
                    {
                        // replicate expected resolver behavior
                        throw new NamespaceException("Unregistrered prefix " + components[0]);
                    }

                    String localName = components[1];

                    if ("*".equals(localName.trim()))
                    {
                        localName = ".*";
                    }

                    Pattern.compile(uri);
                    Pattern.compile(localName);

                    RegexQNamePattern qNamePattern = new RegexQNamePattern(uri,
                                                                           localName);

                    if (!qNamePattern.isMatch(QName.createQName(uri,
                                                                components[1])))
                    {
                        throw new IllegalArgumentException("Illegal filters string " + filtersString
                                    + " due to invalid regexp translatrion in  " + filters[i] + " as " + qNamePattern);

                    }
                    patterns.add(qNamePattern);
                }
                catch (NamespaceException e)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Illegal filters string " + filtersString + " due to unregistered name space in  "
                                                 + filters[i],
                                     e);
                    }
                }
                catch (PatternSyntaxException e)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Illegal filters string " + filtersString
                                                 + " due to invalid regexp translation in  " + filters[i],
                                     e);
                    }
                }
            }

        }

        return patterns.toArray(new QNamePattern[] {});
    }

    public void setQnameFilters(String filters)
    {
        ParameterCheck.mandatoryString("filters",
                                       filters);
        this.filters = filters;
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

    private boolean isAnyFilterMatch(QName qname)
    {
        QNamePattern[] syncFilters = createFilters();
        for (int i = 0; i < syncFilters.length; i++)
        {
            if (syncFilters[i].isMatch(qname))
            {
                return true;
            }
        }

        return false;
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
            if (isAnyFilterMatch(nodeType))
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

                    if (isAnyFilterMatch(aspect))
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
            logger.error("Invalid type method type and aspect in qName filter ",
                         e);
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
