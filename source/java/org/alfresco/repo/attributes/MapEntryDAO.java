package org.alfresco.repo.attributes;

import java.util.List;

/**
 * Interface for MapEntry persistence. 
 * @author britt
 */
public interface MapEntryDAO
{
    /**
     * Save a MapEntry.
     * @param entry To save.
     */
    public void save(MapEntry entry);
    
    /**
     * Delete a MapEntry.
     * @param entry
     */
    public void delete(MapEntry entry);
    
    /**
     * Delete all entries for a map.
     * @param mapAttr The map to purge.
     */
    public void delete(MapAttribute mapAttr);
    
    /**
     * Get an entry by name.
     * @param mapAttr The map to get the entry from.
     * @param key The key of the entry.
     * @return A MapEntry or null.
     */
    public MapEntry get(MapAttribute mapAttr, String key);
    
    /**
     * Retrieve all the entries in a map.
     * @param mapAttr
     * @return A List of all entries in the given map.
     */
    public List<MapEntry> get(MapAttribute mapAttr);
    
    /**
     * Get the number of entries in a MapAttribute.
     * @param mapAttr The MapAttribute/
     * @return The number of entries.
     */
    public int size(MapAttribute mapAttr);
}
