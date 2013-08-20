/*
 * Copyright (C) 2005-2012
 Alfresco Software Limited.
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
package org.alfresco.util.test.junitrules;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ExternalResource;

/**
 * A JUnit rule designed to help with finding well known nodes
 *  in the system. It provides functionality similar to
 *  {@link Repository}, but with extra nodes needed for testing
 * 
 * @author Nick Burch
 * @since Odin
 */
public class WellKnownNodes extends ExternalResource
{
    private static final Log log = LogFactory.getLog(WellKnownNodes.class);
    
    private final ApplicationContextInit appContextRule;
    private final Repository repositoryHelper;
    private final NodeService nodeService;
    
    private static QName SYSTEM_FOLDER_QNAME = 
        QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "system");
    
    /**
     * Constructs the rule with a reference to a {@link ApplicationContextInit rule} which can be used to retrieve the ApplicationContext.
     * 
     * @param appContextRule a rule which can be used to retrieve the spring app context.
     */
    public WellKnownNodes(ApplicationContextInit appContextRule)
    {
        this.appContextRule = appContextRule;
        this.repositoryHelper = (Repository)appContextRule.getApplicationContext().getBean("repositoryHelper");
        this.nodeService = (NodeService)appContextRule.getApplicationContext().getBean("NodeService");
    }
    
    
    @Override protected void before() throws Throwable
    {
        // Intentionally empty
    }
    
    @Override protected void after()
    {
        // Intentionally empty
    }
    
    /**
     * Returns the root of the workspace store
     */
    public NodeRef getWorkspaceRoot()
    {
        return repositoryHelper.getRootHome();
    }
    /**
     * Returns company home
     */
    public NodeRef getCompanyHome()
    {
        return repositoryHelper.getCompanyHome();
    }
    /**
     * Returns the system root
     */
    public NodeRef getSystemRoot()
    {
        NodeRef root = getWorkspaceRoot();
        List<ChildAssociationRef> sysRefs = nodeService.getChildAssocs(
                root, ContentModel.ASSOC_CHILDREN, SYSTEM_FOLDER_QNAME);
        if (sysRefs.size() != 1)
        {
            throw new IllegalStateException("System folder missing / duplicated! Found " + sysRefs);
        }
        final NodeRef system = sysRefs.get(0).getChildRef();
        return system;
    }
    /**
     * Returns the given System Container
     */
    public NodeRef getSystemContainer(QName containerName)
    {
        NodeRef system = getSystemRoot();
        List<ChildAssociationRef> containerRefs = nodeService.getChildAssocs(
                system, ContentModel.ASSOC_CHILDREN, containerName);
        if (containerRefs.size() != 1)
        {
            throw new IllegalStateException("System Container " + containerName + " missing / duplicated! Found " + containerRefs);
        }
        final NodeRef container = containerRefs.get(0).getChildRef();
        return container;
    }
}
