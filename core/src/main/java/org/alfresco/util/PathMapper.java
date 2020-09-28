/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A component that maps source data paths to target data paths.
 * <p>
 * This class caches results and is thread-safe. 
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PathMapper
{
    private static final Log logger = LogFactory.getLog(PathMapper.class);
    
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;
    
    private boolean locked;
    /**
     * Used to lookup path translations
     */
    private final Map<String, Set<String>> pathMaps;
    /**
     * Cached fine-grained path translations (derived data) 
     */
    private final Map<String, Set<String>> derivedPathMaps;
    private final Map<String, Set<String>> derivedPathMapsPartial;
    
    /**
     * Default constructor
     */
    public PathMapper()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        
        pathMaps = new HashMap<String, Set<String>>(37);
        derivedPathMaps = new HashMap<String, Set<String>>(127);
        derivedPathMapsPartial = new HashMap<String, Set<String>>(127);
    }
    
    /**
     * Locks the instance against further modifications.
     */
    public void lock()
    {
        writeLock.lock();
        try
        {
            locked = true;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public void clear()
    {
        writeLock.lock();
        try
        {
            if (locked)
            {
                throw new IllegalStateException("The PathMapper has been locked against further changes");
            }
            pathMaps.clear();
            derivedPathMaps.clear();
            derivedPathMapsPartial.clear();
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Add a path mapping.
     * 
     * @param sourcePath            the source path
     * @param targetPath            the target path
     */
    public void addPathMap(String sourcePath, String targetPath)
    {
        writeLock.lock();
        try
        {
            if (locked)
            {
                throw new IllegalStateException("The PathMapper has been locked against further changes");
            }
            derivedPathMaps.clear();
            derivedPathMapsPartial.clear();
            Set<String> targetPaths = pathMaps.get(sourcePath);
            if (targetPaths == null)
            {
                targetPaths = new HashSet<String>(5);
                pathMaps.put(sourcePath, targetPaths);
            }
            targetPaths.add(targetPath);
        }
        finally
        {
            writeLock.unlock();
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Added path map: " + sourcePath + " --> " + targetPath);
        }
    }
    
    /**
     * Gets the remapped paths for the given source path, excluding any derivative
     * paths i.e. does exact path matching only.
     * 
     * @param sourcePath            the source path
     * @return                      Returns the target paths (never <tt>null</tt>)
     */
    public Set<String> getMappedPaths(String sourcePath)
    {
        readLock.lock();
        try
        {
            Set<String> targetPaths = derivedPathMaps.get(sourcePath);
            if (targetPaths != null)
            {
                return targetPaths;
            }
        }
        finally
        {
            readLock.unlock();
        }
        // We didn't find anything, so update the cache
        writeLock.lock();
        try
        {
            return updateMappedPaths(sourcePath);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    /**
     * Gets the remapped paths for the given source path, including any derivative
     * paths i.e. does partial path matching.
     * 
     * @param sourcePath            the source path
     * @return                      Returns the target paths (never <tt>null</tt>)
     */
    public Set<String> getMappedPathsWithPartialMatch(String sourcePath)
    {
        readLock.lock();
        try
        {
            Set<String> targetPaths = derivedPathMapsPartial.get(sourcePath);
            if (targetPaths != null)
            {
                return targetPaths;
            }
        }
        finally
        {
            readLock.unlock();
        }
        // We didn't find anything, so update the cache
        writeLock.lock();
        try
        {
            return updateMappedPathsPartial(sourcePath);
        }
        finally
        {
            writeLock.unlock();
        }
    }
    
    public boolean isEmpty()
    {
        readLock.lock();
        try
        {
            return pathMaps.isEmpty();
        }
        finally
        {
            readLock.unlock();
        }
    }
    
    private Set<String> updateMappedPaths(String sourcePath)
    {
        // Do a double-check
        Set<String> targetPaths = derivedPathMaps.get(sourcePath);
        if (targetPaths != null)
        {
            return targetPaths;
        }
        targetPaths = new HashSet<String>(17);
        derivedPathMaps.put(sourcePath, targetPaths);
        // Now remap it and build the target values
        for (Map.Entry<String, Set<String>> entry : pathMaps.entrySet())
        {
            String mapSourcePath = entry.getKey();
            Set<String> mapTargetPaths = entry.getValue();
            // If the map source matches the source, then it's simple
            if (mapSourcePath.equals(sourcePath))
            {
                targetPaths.addAll(mapTargetPaths);
                continue;
            }
            // It is not an exact match, so check if it starts with the source
            int index = sourcePath.indexOf(mapSourcePath);
            if (index != 0)
            {
                // It doesn't match the start, so ignore it
                continue;
            }
            // Replace the beginning with the mapped targets
            for (String mapTargetPath : mapTargetPaths)
            {
                if (mapTargetPath.equals(mapSourcePath))
                {
                    // Direct mapping, so shortcut
                    targetPaths.add(sourcePath);
                }
                else
                {
                    String newPath = (mapTargetPath + sourcePath.substring(mapSourcePath.length()));
                    targetPaths.add(newPath);
                }
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Cached path mapping: \n" +
                    "   Source:  " + sourcePath + "\n" +
                    "   Targets: " + targetPaths);
        }
        return targetPaths;
    }
    
    private Set<String> updateMappedPathsPartial(String sourcePath)
    {
        // Do a double-check
        Set<String> targetPaths = derivedPathMapsPartial.get(sourcePath);
        if (targetPaths != null)
        {
            return targetPaths;
        }
        targetPaths = new HashSet<String>(17);
        derivedPathMapsPartial.put(sourcePath, targetPaths);
        // Now remap it and build the target values
        for (Map.Entry<String, Set<String>> entry : pathMaps.entrySet())
        {
            String mapSourcePath = entry.getKey();
            Set<String> mapTargetPaths = entry.getValue();
            // It is not an exact match, so check if it starts with the source
            int index = mapSourcePath.indexOf(sourcePath);
            if (index != 0)
            {
                // It doesn't match the start, so ignore it
                continue;
            }
            // Record the partial matches
            targetPaths.addAll(mapTargetPaths);
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Cached path mapping (partial): \n" +
                    "   Source:  " + sourcePath + "\n" +
                    "   Targets: " + targetPaths);
        }
        return targetPaths;
    }
    
    public <V> Map<String, V> convertMap(Map<String, V> valueMap)
    {
        Map<String, V> resultMap = new HashMap<String, V>(valueMap.size() * 2 + 1);
        for (Map.Entry<String, V> entry : valueMap.entrySet())
        {
            String path = entry.getKey();
            V value = entry.getValue();
            Set<String> mappedPaths = getMappedPaths(path);
            for (String mappedPath : mappedPaths)
            {
                resultMap.put(mappedPath, value);
            }
        }
        return resultMap;
    }
}
