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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * {@link Repository} based {@link NodeRefResolver} implementation.<br>
 */
public class RepositoryNodeRefResolver implements NodeRefResolver
{
    public static final String PATH_REF_EXPRESSION = "path";

    public static final String NODE_REF_EXPRESSION = "node";

    public static final String QNAME_REF_EXPRESSION = "qname";

    private Repository repository;

    private NodeService nodeService;

    private NamespacePrefixResolver namespacePrefixResolver;

    public RepositoryNodeRefResolver()
    {

    }

    public RepositoryNodeRefResolver(Repository repository)
    {
        super();
        this.repository = repository;
    }

    @Override
    public NodeRef resolveNodeReference(String[] reference)
    {
        return repository.findNodeRef(NODE_REF_EXPRESSION,
                                      reference);
    }

    @Override
    public NodeRef resolvePathReference(String[] reference)
    {
        return repository.findNodeRef(PATH_REF_EXPRESSION,
                                      reference);
    }

    @Override
    public NodeRef resolveQNameReference(String[] reference)
    {
        NodeRef theNodeRef = null;

        if (reference.length > 0)
        {

            theNodeRef = repository.getRootHome();

            List<ChildAssociationRef> rootChildren = nodeService
                        .getChildAssocs(theNodeRef,
                                        ContentModel.ASSOC_CHILDREN,
                                        QName.createQName(reference[0],
                                                          namespacePrefixResolver),
                                        false);
            if (rootChildren == null || rootChildren.isEmpty())
            {
                // one more attempt : might be a contains assoc 

                rootChildren = nodeService.getChildAssocs(theNodeRef,
                                                          ContentModel.ASSOC_CONTAINS,
                                                          QName.createQName(reference[0],
                                                                            namespacePrefixResolver),
                                                          false);
            }
            
            if (rootChildren == null || rootChildren.isEmpty())
            {
                // one more attempt : might be a contains assoc though
                return null;
            }
            else
            {
                theNodeRef = rootChildren.get(0).getChildRef();
            }

            for (int i = 1; i < reference.length; i++)
            {

                List<ChildAssociationRef> children = nodeService
                            .getChildAssocs(theNodeRef,
                                            ContentModel.ASSOC_CONTAINS,
                                            QName.createQName(reference[i],
                                                              namespacePrefixResolver),
                                            false);
                if (children == null || children.isEmpty())
                {
                    theNodeRef = null;
                    break;
                }
                else
                {
                    theNodeRef = children.get(0).getChildRef();
                }
            }
        }
        return theNodeRef;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    public NodeRef getCompanyHome()
    {
        return repository.getCompanyHome();
    }

    @Override
    public NodeRef getRootHome()
    {
        return repository.getRootHome();
    }

    @Override
    public NodeRef getSharedHome()
    {
        return repository.getSharedHome();
    }

    @Override
    public NodeRef getUserHome(NodeRef person)
    {
        return repository.getUserHome(person);
    }

    @Override
    public NodeRef createNamePath(String[] reference)
    {
        Stack<String> notFoundStack = new Stack<>();
        NodeRef found;
        if (reference == null || reference.length == 0)
        {
            found = getRootHome();
        }
        else
        {
            NodeRef parentNodeRef = null;
            for (int i = reference.length; i > 0; i--)
            {
                String[] parent = new String[i];
                System.arraycopy(reference,
                                 0,
                                 parent,
                                 0,
                                 i);
                parentNodeRef = resolvePathReference(parent);
                if (parentNodeRef != null)
                {
                    break;
                }
                else
                {
                    notFoundStack.push(reference[i - 1]);
                }
            }
            while (!notFoundStack.isEmpty())
            {
                String toCreate = notFoundStack.pop();
                final HashMap<QName, Serializable> newProperties = new HashMap<QName, Serializable>();
                newProperties.put(ContentModel.PROP_NAME,
                                  toCreate);
                ChildAssociationRef newAssoc = nodeService
                            .createNode(parentNodeRef,
                                        ContentModel.ASSOC_CONTAINS,
                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                          QName.createValidLocalName(toCreate)),
                                        ContentModel.TYPE_FOLDER,
                                        newProperties);
                parentNodeRef = newAssoc.getChildRef();
            }

            found = parentNodeRef;
        }
        return found;
    }

    @Override
    public NodeRef createQNamePath(String[] reference, String[] names)
    {
        Stack<String> notFoundStack = new Stack<>();
        Stack<String> notFoundNameStack = new Stack<>();
        NodeRef found;
        if (reference == null || reference.length == 0)
        {
            found = getRootHome();
        }
        else
        {
            NodeRef parentNodeRef = null;
            for (int i = reference.length; i > 0; i--)
            {
                String[] parent = new String[i];
                System.arraycopy(reference,
                                 0,
                                 parent,
                                 0,
                                 i);
                parentNodeRef = resolveQNameReference(parent);
                if (parentNodeRef != null)
                {
                    break;
                }
                else
                {
                    if (names != null)
                    {
                        int offset = reference.length - i;
                        if (offset < names.length)
                        {
                            notFoundNameStack.push(names[names.length - 1 - offset]);
                        }
                    }
                    notFoundStack.push(reference[i - 1]);
                }
            }
            while (!notFoundStack.isEmpty())
            {
                String stringQNameToCreate = notFoundStack.pop();
                QName qNameToCreate = QName.createQName(stringQNameToCreate,
                                                        namespacePrefixResolver);
                String nameToCreate;
                if (!notFoundNameStack.isEmpty())
                {
                    nameToCreate = notFoundNameStack.pop();
                }
                else
                {
                    nameToCreate = qNameToCreate.getLocalName();
                }
                final HashMap<QName, Serializable> newProperties = new HashMap<QName, Serializable>();
                newProperties.put(ContentModel.PROP_NAME,
                                  nameToCreate);
                ChildAssociationRef newAssoc = nodeService.createNode(parentNodeRef,
                                                                      ContentModel.ASSOC_CONTAINS,
                                                                      qNameToCreate,
                                                                      ContentModel.TYPE_FOLDER,
                                                                      newProperties);
                parentNodeRef = newAssoc.getChildRef();
            }

            found = parentNodeRef;
        }

        return found;

    }

}
