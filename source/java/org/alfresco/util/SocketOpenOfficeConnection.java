/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
