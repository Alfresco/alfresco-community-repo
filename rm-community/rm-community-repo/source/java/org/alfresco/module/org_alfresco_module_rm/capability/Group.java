 
package org.alfresco.module.org_alfresco_module_rm.capability;

/**
 * Group interface
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public interface Group
{
    /**
     * Gets the id of a group (Get the id of the group)
     *
     * @return String the group id
     */
    String getId();

    /**
     * Gets the title of a group
     *
     * @return String the group title
     */
    String getTitle();

    /**
     * Gets the index of a group
     *
     * @return int the group index
     */
    int getIndex();
}
