package org.alfresco.repo.domain.node;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Bean to capture <tt>StoreRef</tt> results.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class StoreEntity implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long version;
    private String protocol;
    private String identifier;
    private NodeEntity rootNode;
    
    /**
     * Required default constructor
     */
    public StoreEntity()
    {
    }
        
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("StoreEntity")
          .append("[ ID=").append(id)
          .append(", protocol=").append(protocol)
          .append(", identifier=").append(identifier)
          .append(", rootNode=").append(rootNode)
          .append("]");
        return sb.toString();
    }
    
    public StoreRef getStoreRef()
    {
        return new StoreRef(protocol, identifier);
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public NodeEntity getRootNode()
    {
        return rootNode;
    }

    public void setRootNode(NodeEntity rootNode)
    {
        this.rootNode = rootNode;
    }
}
