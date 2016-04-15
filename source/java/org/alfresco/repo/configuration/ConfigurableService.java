package org.alfresco.repo.configuration;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Configurable service interface
 * 
 * @author Roy Wetherall
 */
public interface ConfigurableService 
{
    
    
	/**
	 * Indicates whether a node is configurable or not
	 * 
	 * @param nodeRef	the node reference
	 * @return			true if the node is configurable, false otherwise
	 */
	public boolean isConfigurable(NodeRef nodeRef);
	
	/**
     * Makes a specified node Configurable.
     * <p>
     * This will create the cofigurable folder, associate it as a child of the node and apply the 
     * configurable aspect to the node.
     * 
     * @param nodeRef the node reference
     */
    public void makeConfigurable(NodeRef nodeRef);
    
    /**
     * Get the configuration folder associated with a configuration node
     * 
     * @param nodeRef   the node reference
     * @return			the configuration folder
     */
    public NodeRef getConfigurationFolder(NodeRef nodeRef);

}
