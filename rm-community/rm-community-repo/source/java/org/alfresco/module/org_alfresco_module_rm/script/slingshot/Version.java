package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import static org.alfresco.util.ParameterCheck.mandatory;
import static org.alfresco.util.ParameterCheck.mandatoryString;

/**
 * Recordable version class
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class Version
{
    /** The version policy */
    private String policy;

    /** Is the version selected */
    private boolean selected;

    /**
     * Constructor
     *
     * @param policy The version policy
     * @param selected Is the version selected
     */
    public Version(String policy, boolean selected)
    {
        mandatoryString("policy", policy);
        mandatory("selected", selected);

        setPolicy(policy);
        setSelected(selected);
    }

    /**
     * Gets the version policy
     *
     * @return The version policy
     */
    public String getPolicy()
    {
        return this.policy;
    }

    /**
     * Sets the version policy
     *
     * @param policy The version policy
     */
    private void setPolicy(String policy)
    {
        this.policy = policy;
    }

    /**
     * Is the version selected
     *
     * @return <code>true</code> if the version is selected, <code>false</code> otherwise
     */
    public boolean isSelected()
    {
        return this.selected;
    }

    /**
     * Sets the version as selected
     *
     * @param selected <code>true</code> if the version should be selected, <code>false</code> otherwise
     */
    private void setSelected(boolean selected)
    {
        this.selected = selected;
    }
}
