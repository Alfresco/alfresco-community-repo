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
package org.alfresco.repo.webdav;

import org.alfresco.util.PropertyCheck;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Bootstrap WebDav
 * 
 * Not much to do (yet) but simply looks up root node
 *
 * @author mrogers
 */
public class WebDavBootstrap extends AbstractLifecycleBean
{
    private MTNodesCache2 rootNode;
    
    public void init()
    {
        PropertyCheck.mandatory(this, "rootNode", getRootNode());   
    }
    
    public void setRootNode(MTNodesCache2 rootNode)
    {
        this.rootNode = rootNode;
    }
    public MTNodesCache2 getRootNode()
    {
        return rootNode;
    }  
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // Bootstrap the rootNode
        rootNode.onBootstrap();
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // TODO Auto-generated method stub
        
    }
 

}
