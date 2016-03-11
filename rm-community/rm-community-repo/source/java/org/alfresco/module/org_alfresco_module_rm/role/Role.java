package org.alfresco.module.org_alfresco_module_rm.role;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;

/**
 * Records management role class
 *
 * @author Roy Wetherall
 */
public class Role
{
    /** Role name */
    private String name;

    /** Role label */
    private String displayLabel;

    /** Role capabilities */
    private Set<Capability> capabilities;

    /** Role group name */
    private String roleGroupName;

    /** Role group short name */
    private String groupShortName;

    /**
     * @param name
     * @param displayLabel
     * @param capabilities
     * @param roleGroupName
     */
    public Role(String name, String displayLabel, Set<Capability> capabilities, String roleGroupName)
    {
        this.name = name;
        this.displayLabel = displayLabel;
        this.capabilities = capabilities;
        this.roleGroupName = roleGroupName;
    }

    /**
     * @param name
     * @param displayLabel
     * @param capabilities
     * @param roleGroupName
     * @param groupShortName
     */
    public Role(String name, String displayLabel, Set<Capability> capabilities, String roleGroupName, String groupShortName)
    {
        this(name, displayLabel, capabilities, roleGroupName);
        this.groupShortName = groupShortName;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the displayLabel
     */
    public String getDisplayLabel()
    {
        return displayLabel;
    }

    /**
     * @return the capabilities
     */
    public Set<Capability> getCapabilities()
    {
        return capabilities;
    }

    /**
     * @return the roleGroupName
     */
    public String getRoleGroupName()
    {
        return roleGroupName;
    }

    /**
     * @return the groupShortName
     */
    public String getGroupShortName()
    {
        return this.groupShortName;
    }

}
