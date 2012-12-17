package org.alfresco.util.remote.server;

import java.rmi.RemoteException;

import org.springframework.remoting.rmi.RmiServiceExporter;

public class AlfrescoRMIServiceExporter extends RmiServiceExporter
{
    private boolean enabled = true;

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }
    
    public void prepare() throws RemoteException 
    {
        if(enabled)
        {
            super.prepare();
        }
    }
    
    public void destroy() throws RemoteException 
    {
        if(enabled)
        {
            super.destroy();
        }
    }
    
}
