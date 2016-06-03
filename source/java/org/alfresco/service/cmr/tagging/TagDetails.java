package org.alfresco.service.cmr.tagging;

/**
 * Tag details interface.
 * 
 * @author Roy Wetherall
 */
public interface TagDetails extends Comparable<TagDetails>
{
    /**
     * Get the name of the tag
     * 
     * @return  String  tag name
     */
    String getName();
    
    /**
     * Get the tag count
     * 
     * @return  int     tag count
     */
    int getCount();
}
