package org.alfresco.repo.model.filefolder;

/**
 * Represents hidden file information, including the filter (regular expression) and the visibility mask.
 * 
 * @since 4.0
 *
 */
public interface HiddenFileInfo
{
    public int getVisibilityMask();
    public String getFilter();
}
