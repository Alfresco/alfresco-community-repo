package org.alfresco.repo.attributes;

import java.util.List;

import org.alfresco.repo.domain.hibernate.DirtySessionAnnotation;

/**
 * Interface for persistence of the top level attribute map.
 * @author britt
 */
public interface GlobalAttributeEntryDAO
{
    /**
     * Save an entry.
     * @param entry To save.
     */
    @DirtySessionAnnotation(markDirty=true)
    public void save(GlobalAttributeEntry entry);
    
    /**
     * Delete an entry.
     * @param entry To delete.
     */
    @DirtySessionAnnotation(markDirty=true)
    public void delete(GlobalAttributeEntry entry);
    
    /**
     * Delete an entry by name.
     * @param name The name of the entry.
     */
    @DirtySessionAnnotation(markDirty=true)
    public void delete(String name);
    
    /**
     * Get an attribute by name.
     * @param name The name of the attribute.
     * @return The entry or null.
     */
    @DirtySessionAnnotation(markDirty=false)
    public GlobalAttributeEntry get(String name);
    
    /**
     * Get all keys for global attributes.
     * @return A list of all top level keys.
     */
    @DirtySessionAnnotation(markDirty=false)
    public List<String> getKeys();
}
