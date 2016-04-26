package org.alfresco.repo.forms;

/**
 * Represents a field group
 *
 * @author Gavin Cornwell
 */
public class FieldGroup
{
    protected String id;
    protected String label;
    protected FieldGroup parent;
    protected boolean repeats;
    protected boolean mandatory;
    
    /**
     * Constructs a FieldGroup
     * 
     * @param id        The id of the group
     * @param label     The display label of the group
     * @param mandatory Whether the group is mandatory
     * @param repeats   Whether the group of fields can repeat
     * @param parent    The group's parent group or null if it 
     *                  doesn't have a parent
     */
    public FieldGroup(String id, String label, boolean mandatory, 
                      boolean repeats, FieldGroup parent)
    {
        this.id = id;
        this.label = label;
        this.mandatory = mandatory;
        this.parent = parent;
        this.repeats = repeats;
    }

    /**
     * Returns the id of the group 
     * 
     * @return The id of the group
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Returns the display label of the group
     * 
     * @return The display label of the group
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * Returns the parent group
     * 
     * @return The parent group or null if there isn't a parent
     */
    public FieldGroup getParent()
    {
        return this.parent;
    }

    /**
     * Determines whether the fields inside this group can 
     * repeat multiple times
     * 
     * @return true if the group repeats
     */
    public boolean isRepeating()
    {
        return this.repeats;
    }

    /**
     * Determines if the group is mandatory
     * 
     * @return true if the group is mandatory
     */
    public boolean isMandatory()
    {
        return this.mandatory;
    }
}
