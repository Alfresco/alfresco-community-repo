/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GUID;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This helper class is used to manage a workflow package. The manager is a
 * stateful object which accumulates all the changes to be made to the package
 * (such as adding and removing package items). These changes are then applied
 * to the package when either the create() or update() method is called.
 * 
 * @author Nick Smith
 * @since 3.4
 */
public class PackageManager
{
    
    private static final QName PCKG_CONTAINS = WorkflowModel.ASSOC_PACKAGE_CONTAINS;
    private static final QName PCKG_ASPECT= WorkflowModel.ASPECT_WORKFLOW_PACKAGE;
    private static final String CM_URL = NamespaceService.CONTENT_MODEL_1_0_URI;

    /** Default Log */
    private final static Log LOGGER = LogFactory.getLog(PackageManager.class);
    
    private final WorkflowService workflowService;
    private final NodeService nodeService;
    private final Log logger;
    private final BehaviourFilter behaviourFilter;
    
    private final Set<NodeRef> addItems = new HashSet<NodeRef>();
    private final Set<NodeRef> removeItems = new HashSet<NodeRef>();

    public PackageManager(WorkflowService workflowService,
                NodeService nodeService,
                BehaviourFilter behaviourFilter,
                Log logger)
    {
        this.workflowService = workflowService;
        this.nodeService = nodeService;
        this.behaviourFilter =behaviourFilter;
        this.logger = logger ==null ? LOGGER : logger;
    }
    
    public void addItems(List<NodeRef> items)
    {
        addItems.addAll(items);
    }

    /**
     * Takes a comma-separated list of {@link NodeRef} ids and adds the
     * specified NodeRefs to the package.
     * 
     * @param items
     */
    public void addItems(String items)
    {
        List<NodeRef> nodes = NodeRef.getNodeRefs(items);
        addItems(nodes);
    }
    
    public void addItemsAsStrings(List<String> itemStrs)
    {
        for (String itemStr : itemStrs)
        {
            addItem(itemStr);
        }
    }

    public void addItem(NodeRef item)
    {
        addItems.add(item);
    }
    
    public void addItem(String itemStr)
    {
        addItem(new NodeRef(itemStr));
    }

    public void removeItems(List<NodeRef> items)
    {
        removeItems.addAll(items);
    }

    /**
     * Takes a comma-separated list of {@link NodeRef} ids and adds the
     * specified NodeRefs to the package.
     * 
     * @param items
     */
    public void removeItems(String items)
    {
        List<NodeRef> nodes = NodeRef.getNodeRefs(items);
        removeItems(nodes);
    }
    
    public void removeItemsAsStrings(List<String> itemStrs)
    {
        for (String itemStr : itemStrs)
        {
            removeItem(itemStr);
        }
    }

    public void removeItem(NodeRef item)
    {
        removeItems.add(item);
    }
    
    public void removeItem(String itemStr)
    {
        removeItem(new NodeRef(itemStr));
    }

    /**
     * Creates a new Workflow package using the specified <code>container</code>.
     * If the <code>container</code> is null then a new container node is created.
     * Applies the specified updates to the package after it is created.
     * @param container
     * @return the package {@link NodeRef}.
     * @throws WorkflowException if the specified container is already package.
     */
    public NodeRef create(NodeRef container) throws WorkflowException
    {
        NodeRef packageRef = workflowService.createPackage(container);
        update(packageRef);
        return packageRef;
    }
    
    /**
     * Applies the specified modifications to the package.
     * @param packageRef
     */
    public void update(final NodeRef packageRef)
    {
        if (addItems.isEmpty() && removeItems.isEmpty())
            return;

        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                checkPackage(packageRef);
                checkPackageItems(packageRef);
                addPackageItems(packageRef);
                removePackageItems(packageRef);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
        addItems.clear();
        removeItems.clear();
    }

    private void checkPackage(NodeRef packageRef)
    {
        if (packageRef == null || nodeService.hasAspect(packageRef, PCKG_ASPECT) == false)
        {
            String msg = "The package NodeRef must implement the aspect: " + PCKG_ASPECT;
            throw new WorkflowException(msg);
        }
    }

    private void removePackageItems(NodeRef packageRef)
    {
        for (NodeRef item : removeItems)
        {
            nodeService.removeChild(packageRef, item);
        }
    }

    private void addPackageItems(final NodeRef packageRef)
    {
        for (NodeRef item : addItems)
        {
            String name = (String) nodeService.getProperty(item, ContentModel.PROP_NAME);
            if (name == null)
            {
                name = GUID.generate();
            }
            String localName = QName.createValidLocalName(name);
            QName qName = QName.createQName(CM_URL, localName);

            behaviourFilter.disableBehaviour(item, ContentModel.ASPECT_AUDITABLE);
            try
            {
                nodeService.addChild(packageRef, item, PCKG_CONTAINS, qName);
            }
            finally
            {
                behaviourFilter.enableBehaviour(item, ContentModel.ASPECT_AUDITABLE);
            }
        }
    }

    private List<NodeRef> getCurrentItems(NodeRef packageRef)
    {
        List<ChildAssociationRef> children = nodeService.getChildAssocs(
                    packageRef, PCKG_CONTAINS, RegexQNamePattern.MATCH_ALL);
        ArrayList<NodeRef> results = new ArrayList<NodeRef>(children.size());
        for (ChildAssociationRef child : children)
        {
            results.add(child.getChildRef());
        }
        return results;
    }
    
    @SuppressWarnings("unchecked")
    private void checkPackageItems(NodeRef packageRef)
    {
        List<NodeRef> currentitems = getCurrentItems(packageRef);
        Collection<NodeRef> intersection = CollectionUtils.intersection(addItems, removeItems);
        addItems.removeAll(intersection);
        removeItems.removeAll(intersection);
        for (NodeRef node : intersection)
        {
            if (logger.isDebugEnabled())
                logger.debug("Item was added and removed from package! Ignoring item: "+ node);
        }
        checkAddedItems(currentitems);
        checkRemovedItems(currentitems);
    }

    private void checkRemovedItems(List<NodeRef> currentitems)
    {
        for (Iterator<NodeRef> iter = removeItems.iterator(); iter.hasNext();)
        {
            NodeRef removeItem= iter.next();
            if (currentitems.contains(removeItem) == false)
            {
                iter.remove();
                if (logger.isDebugEnabled())
                    logger.debug("Ignoring item to remove, item not in package: " + removeItem);
            }
        }
    }

    private void checkAddedItems(List<NodeRef> currentitems)
    {
        for (Iterator<NodeRef> iter = addItems.iterator(); iter.hasNext();)
        {
            NodeRef addItem= iter.next();
            if (currentitems.contains(addItem))
            {
                iter.remove();
                if (logger.isDebugEnabled())
                    logger.debug("Ignoring item to add, item already in package: " + addItem);
            }
        }
    }

}
