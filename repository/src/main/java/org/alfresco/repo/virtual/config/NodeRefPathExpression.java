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

package org.alfresco.repo.virtual.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A String name or qname path expression that resolves to a {@link NodeRef}.<br>
 * The given name or qname path is relative to a {@link NodeRefContext}
 * repository location. The default context is set to
 * {@link CompanyHomeContext#COMPANY_HOME_CONTEXT_NAME}. Other contexts can be
 * set using their name with {@link #setContext(String)}.<br>
 * The set path is automatically detected and checked for consistency.
 */
public class NodeRefPathExpression implements NodeRefExpression
{
    private static final String NAMESPACE_DELIMITER = ":";

    private static final String PATH_DELIMITER = "/";

    private Map<String, NodeRefContext> contexts = new HashMap<String, NodeRefContext>();

    private NodeRefResolver resolver;

    private String context = CompanyHomeContext.COMPANY_HOME_CONTEXT_NAME;

    private String[] createNamePath;

    private String[] namePath;

    private String[] qNamePath;

    public NodeRefPathExpression(NodeRefResolver resolver, Map<String, NodeRefContext> contexts)
    {
        this(resolver,
             contexts,
             CompanyHomeContext.COMPANY_HOME_CONTEXT_NAME,
             null);
    }

    public NodeRefPathExpression(NodeRefResolver resolver, Map<String, NodeRefContext> contexts, String context,
                String path)
    {
        super();
        this.resolver = resolver;
        this.contexts = contexts;
        this.context = context;
        setPath(path);
    }

    public void setContext(String context)
    {
        this.context = context;
    }

    public void setCreatedPathName(String createNamePath)
    {
        this.createNamePath = createNamePath.split(",");
    }

    /**
     * Path setter.<br>
     * The type of path is automatically detected and checked for consistency.
     * 
     * @param path the string path to be resolved later
     * @throws AlfrescoRuntimeException if the given path is inconsistent (i.e.
     *             a combination of qnames and names)
     */
    public void setPath(String path) throws AlfrescoRuntimeException
    {
        String[] pathElements = splitAndNormalizePath(path);
        if (isQNamePath(pathElements))
        {
            this.qNamePath = pathElements;
        }
        else if (isNamePath(pathElements))
        {
            this.namePath = pathElements;
        }
        else
        {
            throw new AlfrescoRuntimeException("Invalid path format : " + path);
        }

    }

    private boolean isQNamePath(String[] pathElements)
    {
        for (int i = 0; i < pathElements.length; i++)
        {
            if (!pathElements[i].contains(NAMESPACE_DELIMITER))
            {
                return false;
            }
        }

        return true;
    }

    private boolean isNamePath(String[] pathElements)
    {
        for (int i = 0; i < pathElements.length; i++)
        {
            if (pathElements[i].contains(NAMESPACE_DELIMITER))
            {
                return false;
            }
        }

        return true;
    }

    public static String[] splitAndNormalizePath(String path)
    {
        if (path == null || path.trim().length() == 0)
        {
            return new String[] {};
        }

        String[] splitPath = path.split(PATH_DELIMITER);

        // remove blank entries resulted from misplaced delimiters
        int shift = 0;
        for (int i = 0; i < splitPath.length; i++)
        {
            if (splitPath[i] == null || splitPath[i].trim().isEmpty())
            {
                shift++;
            }
            else if (shift > 0)
            {
                splitPath[i - shift] = splitPath[i];
            }
        }

        if (shift > 0)
        {
            String[] noBlanksSplitPath = new String[splitPath.length - shift];
            if (noBlanksSplitPath.length > 0)
            {
                System.arraycopy(splitPath,
                                 0,
                                 noBlanksSplitPath,
                                 0,
                                 noBlanksSplitPath.length);
            }

            splitPath = noBlanksSplitPath;
        }

        return splitPath;
    }

    @Override
    public NodeRef resolve()
    {
        NodeRefContext theContext = contexts.get(context);
        if (this.namePath != null)
        {
            return theContext.resolveNamePath(this.namePath,
                                              resolver);
        }
        else
        {
            return theContext.resolveQNamePath(this.qNamePath,
                                               resolver);
        }
    }

    @Override
    public NodeRef resolve(boolean createIfNotFound)
    {
        NodeRef nodeRef = resolve();
        if (nodeRef == null && createIfNotFound)
        {
            NodeRefContext theContext = contexts.get(context);

            if (this.namePath != null)
            {
                return theContext.createNamePath(this.namePath,
                                                 resolver);
            }
            else
            {
                return theContext.createQNamePath(this.qNamePath,
                                                  this.createNamePath,
                                                  resolver);
            }

        }
        return nodeRef;
    }

    @Override
    public String toString()
    {
        StringBuilder pathString = new StringBuilder();
        pathString.append("<");
        pathString.append(this.context);
        pathString.append(">/");

        if (this.namePath != null)
        {
            pathString.append(Arrays.toString(this.namePath));
        }

        if (this.qNamePath != null)
        {
            pathString.append(Arrays.toString(this.qNamePath));
        }

        return "<null path expression>";
    }

}
