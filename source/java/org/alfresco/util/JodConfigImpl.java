package org.alfresco.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Implementation that supplies OOoJodconverter subsystems config parameters that might clash with the OOoDirect subsystem.
 * 
 * @author Alan Davis
 */
public class JodConfigImpl implements JodConfig
{
    private String ports;
    private boolean enabled;
    
    @Override
    public Collection<String> getPortsCollection()
    {
        return Arrays.asList(ports.trim().split("[, ][, ]*"));
    }
    
    @Override
    public String getPorts()
    {
        return getPortsCollection().toString().replaceAll("[ \\[\\]]", "");
    }

    @Override
    public void setPorts(String ports)
    {
        this.ports = ports;
    }
    
    @Override
    public boolean isEnabled()
    {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
