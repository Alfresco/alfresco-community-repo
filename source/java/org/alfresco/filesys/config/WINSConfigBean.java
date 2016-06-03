package org.alfresco.filesys.config;

// TODO: Auto-generated Javadoc
/**
 * The Class WINSConfigBean.
 * 
 * @author dward
 */
public class WINSConfigBean
{

    /** The primary. */
    private String primary;

    /** The secondary. */
    private String secondary;

    /** The auto detect enabled. */
    private boolean autoDetectEnabled = true;

    /**
     * Checks if is auto detect enabled.
     * 
     * @return true, if is auto detect enabled
     */
    public boolean isAutoDetectEnabled()
    {
        return autoDetectEnabled;
    }

    /**
     * Sets the auto detect enabled.
     * 
     * @param autoDetectEnabled
     *            the new auto detect enabled
     */
    public void setAutoDetectEnabled(boolean autoDetectEnabled)
    {
        this.autoDetectEnabled = autoDetectEnabled;
    }

    /**
     * Gets the primary.
     * 
     * @return the primary
     */
    public String getPrimary()
    {
        return primary;
    }

    /**
     * Sets the primary.
     * 
     * @param primary
     *            the new primary
     */
    public void setPrimary(String primary)
    {
        this.primary = primary;
    }

    /**
     * Gets the secondary.
     * 
     * @return the secondary
     */
    public String getSecondary()
    {
        return secondary;
    }

    /**
     * Sets the secondary.
     * 
     * @param secondary
     *            the new secondary
     */
    public void setSecondary(String secondary)
    {
        this.secondary = secondary;
    }

}
