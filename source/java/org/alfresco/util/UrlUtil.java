
package org.alfresco.util;

import org.alfresco.repo.admin.SysAdminParams;


/**
 * Alfresco URL related utility functions.
 * 
 * @since 3.5
 */
public class UrlUtil
{
    /**
     * Builds up the Url to Alfresco based on the settings in the 
     *  {@link SysAdminParams}. 
     * @return Alfresco Url such as https://col.ab.or.ate/alfresco/
     *  or http://localhost:8080/alfresco/
     */
    public static String getAlfrescoUrl(SysAdminParams sysAdminParams)
    {
        return buildUrl(
                sysAdminParams.getAlfrescoProtocol(),
                sysAdminParams.getAlfrescoHost(),
                sysAdminParams.getAlfrescoPort(),
                sysAdminParams.getAlfrescoContext());
    }
    
    /**
     * Builds up the Url to Share based on the settings in the 
     *  {@link SysAdminParams}. 
     * @return Alfresco Url such as https://col.ab.or.ate/share/
     *  or http://localhost:8081/share/
     */
    public static String getShareUrl(SysAdminParams sysAdminParams)
    {
        return buildUrl(
                sysAdminParams.getShareProtocol(),
                sysAdminParams.getShareHost(),
                sysAdminParams.getSharePort(),
                sysAdminParams.getShareContext());
    }
    
    protected static String buildUrl(String protocol, String host, int port, String context)
    {
        StringBuilder url = new StringBuilder();
        url.append(protocol);
        url.append("://");
        url.append(host);
        if ("http".equals(protocol) && port == 80)
        {
            // Not needed
        }
        else if ("https".equals(protocol) && port == 443)
        {
            // Not needed
        }
        else
        {
            url.append(':');
            url.append(port);
        }
        url.append('/');
        url.append(context);
        return url.toString();
    }
}
