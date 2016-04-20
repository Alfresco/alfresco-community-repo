package org.alfresco.filesys;

import org.alfresco.jlan.server.config.ServerConfigurationAccessor;

/**
 * An interface exposing some extended capabilities of the AbstractServerConfigurationBean.
 * 
 * @author dward
 */
public interface ExtendedServerConfigurationAccessor extends ServerConfigurationAccessor
{

    /**
     * Get the local server name and optionally trim the domain name
     * 
     * @param trimDomain
     *            boolean
     * @return String
     */
    public String getLocalServerName(boolean trimDomain);

    /**
     * Get the local domain/workgroup name
     * 
     * @return String
     */
    public String getLocalDomainName();
}
