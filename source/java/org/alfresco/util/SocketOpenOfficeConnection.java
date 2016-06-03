package org.alfresco.util;

import org.alfresco.util.bean.BooleanBean;

import net.sf.jooreports.openoffice.converter.AbstractOpenOfficeDocumentConverter;
import net.sf.jooreports.openoffice.converter.OpenOfficeDocumentConverter;
import net.sf.jooreports.openoffice.converter.StreamOpenOfficeDocumentConverter;

public class SocketOpenOfficeConnection extends net.sf.jooreports.openoffice.connection.SocketOpenOfficeConnection
{
    private boolean defaultHost = true;
    private boolean enabled = true;
    
    public SocketOpenOfficeConnection() {
        super();
    }

    public SocketOpenOfficeConnection(int port) {
        super(port);
    }

    public SocketOpenOfficeConnection(String host, int port) {
        super(host, port);
        defaultHost = DEFAULT_HOST.equals(host);
    }
    
    public AbstractOpenOfficeDocumentConverter getDefaultConverter()
    {
        return defaultHost
            ? new OpenOfficeDocumentConverter(this)
            : new StreamOpenOfficeDocumentConverter(this);
    }

    public void setEnabledFromBean(BooleanBean enabled)
    {
        this.enabled = enabled.isTrue();
    }

    public boolean isConnected() {
        return enabled && super.isConnected();
    }
    
    public boolean isEnabled() {
        return enabled;
    }
}
