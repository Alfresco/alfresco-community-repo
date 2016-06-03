package org.alfresco.repo.blog;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Blog integration service.
 * 
 * @author Roy Wetherall
 *
 */
public interface BlogIntegrationService
{
    /**
     * Register a new blog integration implementation with the service
     * 
     * @param implementation    the implementation
     */
    void register(BlogIntegrationImplementation implementation);
    
    /**
     * Get the named blog integration implementation, null if name not recognised
     * 
     * @param implementationName                the implementation name
     * @return BlogIntegrationImplementation    the blog integration implementation
     */
    BlogIntegrationImplementation getBlogIntegrationImplementation(String implementationName);
    
    /**
     * Get a list of the registered integration implementations.
     * 
     * @return List<BlogIntegrationImplementaion>   list of registered blog integration implementations
     */
    List<BlogIntegrationImplementation> getBlogIntegrationImplementations();
    
    /**
     * Given a node reference, gets a list of 'in scope' BlogDetails. 
     * 
     * The node itself and then the primary parent hierarchy is searched and any blog details found returned in 
     * a list, with the 'nearest' first.
     * 
     * @param nodeRef               the node reference
     * @return List<BlogDetails>    list of the blog details found 'in scope' for the node, empty if none found
     */
    List<BlogDetails> getBlogDetails(NodeRef nodeRef);
    
    /**
     * Posts the content of a node to the blog specified
     * 
     * @param blogDetails BlogDetails
     * @param nodeRef NodeRef
     * @param contentProperty QName
     * @param publish boolean
     */
    void newPost(BlogDetails blogDetails, NodeRef nodeRef, QName contentProperty, boolean publish);
    
    /**
     * 
     * @param nodeRef NodeRef
     * @param contentProperty QName
     * @param publish boolean
     */
    void updatePost(NodeRef nodeRef, QName contentProperty, boolean publish);
    
    /**
     * 
     * @param nodeRef NodeRef
     */
    void deletePost(NodeRef nodeRef);
}
