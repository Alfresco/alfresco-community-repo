package org.alfresco.filesys.repo;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.util.FileFilterMode.Client;

public class ClientHelper
{
    public static Client getClient(SrvSession srvSession)
    {
        String clientStr = srvSession.getServer().getProtocolName().toLowerCase();
        if(clientStr.equals("cifs"))
        {
            return Client.cifs;
        }
        else if(clientStr.equals("nfs"))
        {
            return Client.nfs;
        }
        else if(clientStr.equals("ftp"))
        {
            return Client.ftp;
        }
        else
        {
            return null;
        }
    }
}
