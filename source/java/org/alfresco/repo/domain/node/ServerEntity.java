package org.alfresco.repo.domain.node;

import java.io.Serializable;

/**
 * Bean to represent <tt>alf_server</tt> data.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class ServerEntity implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long version;
    private String ipAddress;
    
    /**
     * Required default constructor
     */
    public ServerEntity()
    {
    }
        
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("ServerEntity")
          .append("[ ID=").append(id)
          .append(", ipAddress=").append(ipAddress)
          .append("]");
        return sb.toString();
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

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }
}
