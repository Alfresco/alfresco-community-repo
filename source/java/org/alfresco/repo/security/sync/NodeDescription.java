package org.alfresco.repo.security.sync;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.util.PropertyMap;

/**
 * An 'off-line' description of an Alfresco node.
 * 
 * @author dward
 */
public class NodeDescription
{
    /**
     * An identifier for the node for monitoring purposes. Should help trace where the node originated from.
     */
    private String sourceId;

    /** The properties. */
    private final PropertyMap properties = new PropertyMap(19);

    /** The child associations. */
    private final Set<String> childAssociations = new TreeSet<String>();

    /** The last modification date. */
    private Date lastModified;

    /**
     * Instantiates a new node description.
     * 
     * @param sourceId
     *            An identifier for the node for monitoring purposes. Should help trace where the node originated from.
     */
    public NodeDescription(String sourceId)
    {
        this.sourceId = sourceId;
    }        

    /**
     * Gets an identifier for the node for monitoring purposes. Should help trace where the node originated from.
     * 
     * @return an identifier for the node for monitoring purposes
     */
    public String getSourceId()
    {
        return sourceId;
    }

    /**
     * Gets the last modification date.
     * 
     * @return the last modification date
     */
    public Date getLastModified()
    {
        return lastModified;
    }

    /**
     * Sets the last modification date.
     * 
     * @param lastModified
     *            the last modification date
     */
    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }

    /**
     * Gets the properties.
     * 
     * @return the properties
     */
    public PropertyMap getProperties()
    {
        return properties;
    }

    /**
     * Gets the child associations.
     * 
     * @return the child associations
     */
    public Set<String> getChildAssociations()
    {
        return childAssociations;
    }
}
