package org.alfresco.filesys.config;

/**
 * Factory to provide community versions of key fileserver configuration beans.
 * 
 * @author Matt Ward
 */
public class FileServerConfigurationFactory
{
    public ServerConfigurationBean createFileServerConfiguration()
    {
        return new ServerConfigurationBean();
    }
    
    public ClusterConfigBean createClusterConfigBean()
    {
        return new ClusterConfigBean();
    }
}
