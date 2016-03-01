 
package org.alfresco.module.org_alfresco_module_rm.jscript;

/**
 * @author Roy Wetherall
 */
public class ScriptCapability
{
    private String name;
    private String displayLabel;
    private String[] actions;

    /**
     * @param name
     * @param displayLabel
     * @param actions
     */
    protected ScriptCapability(String name, String displayLabel, String[] actions)
    {
        this.name = name;
        this.displayLabel = displayLabel;
        this.actions = actions.clone();
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
     * @return the actions
     */
    public String[] getActions()
    {
        return actions;
    }
}
