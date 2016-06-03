package org.alfresco.filesys.config;

// TODO: Auto-generated Javadoc
/**
 * The Class TcpipSMBConfigBean.
 * 
 * @author dward
 */
public class TcpipSMBConfigBean
{

    /** The platforms. */
    private String platforms;

    /** The port. */
    private Integer port;

    /** The ipv6 enabled. */
    private boolean ipv6Enabled;

    /**
     * Gets the platforms.
     * 
     * @return the platforms
     */
    public String getPlatforms()
    {
        return platforms;
    }

    /**
     * Sets the platforms.
     * 
     * @param platforms
     *            the new platforms
     */
    public void setPlatforms(String platforms)
    {
        this.platforms = platforms;
    }

    /**
     * Gets the port.
     * 
     * @return the port
     */
    public Integer getPort()
    {
        return port;
    }

    /**
     * Sets the port.
     * 
     * @param port
     *            the new port
     */
    public void setPort(Integer port)
    {
        this.port = port;
    }

    /**
     * Checks if is ipv6 enabled.
     * 
     * @return true, if is ipv6 enabled
     */
    public boolean getIpv6Enabled()
    {
        return ipv6Enabled;
    }

    /**
     * Sets the ipv6 enabled.
     * 
     * @param ipv6Enabled
     *            the new ipv6 enabled
     */
    public void setIpv6Enabled(boolean ipv6Enabled)
    {
        this.ipv6Enabled = ipv6Enabled;
    }

}
