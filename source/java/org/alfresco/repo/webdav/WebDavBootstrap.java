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
