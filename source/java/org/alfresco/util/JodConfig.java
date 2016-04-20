package org.alfresco.util;

import java.util.Collection;

/**
 * Supplies OOoJodconverter subsystems config parameters that might clash with the OOoDirect subsystem.
 * 
 * @author Alan Davis
 */
public interface JodConfig
{
    public abstract Collection<String> getPortsCollection();
    
    public abstract String getPorts();

    public abstract void setPorts(String ports);

    public abstract boolean isEnabled();

    public abstract void setEnabled(boolean enabled);
}