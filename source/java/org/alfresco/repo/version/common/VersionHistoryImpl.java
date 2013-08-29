/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.version.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionDoesNotExistException;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionServiceException;
import org.alfresco.util.EqualsHelper;

/**
 * Version History implementation. 
 * 
 * @author Roy Wetherall
 */
public class VersionHistoryImpl implements VersionHistory
{
    private static final long serialVersionUID = 3257001051558326840L;

    /*
     * Error message(s)
     */
    private static final String ERR_MSG = "The root version must be specified when creating a version history object.";
    
    /*
     * Field is left here to aid in detection of old serialized versions
     */
    @SuppressWarnings("unused")
    private transient List<Version> versions;
    
    /*
     * Version history tree structure map
     */
    private HashMap<String, String> versionHistory = null;
    
    /*
     * Label to version object map - Iterators must be in the order entries were addded.
     */
    private Map<String, Version> versionsByLabel = null;
    
    /*
     * Versions ordered by creation date (descending).
     */
    private Comparator<Version> versionComparatorDesc;

    /**
     * Constructor, ensures the root version is set.
     * 
     * @param rootVersion  the root version, can not be null.
     * @param versionComparatorDesc optional comparator of versions.
     */
    public VersionHistoryImpl(Version rootVersion, Comparator<Version> versionComparatorDesc)
    {
        if (rootVersion == null)
        {
            // Exception - a version history can not be created unless
            //             a root version is specified
            throw new VersionServiceException(VersionHistoryImpl.ERR_MSG);
        }
        
        this.versionHistory = new HashMap<String, String>();
        this.versionsByLabel = new LinkedHashMap<String, Version>();
        this.versionComparatorDesc = versionComparatorDesc;
        
        addVersion(rootVersion, null);
    }    
    
    /**
     * Gets the root (initial / least recent) version of the version history.
     * 
     * @return  the root version
     */
    public Version getRootVersion()
    {
        return versionsByLabel.values().iterator().next();
    }
    
    /**
     * Gets the head (current / most recent) version of the version history.
     * 
     * @return  the head version
     */
    public Version getHeadVersion()
    {
        return getAllVersions().iterator().next();
    }
    
    /**
     * Gets a collection containing all the versions within the
     * version history.
     * <p>
     * Versions are returned in descending create date order (most recent first).
     * 
     * @return  collection containing all the versions
     */
    public Collection<Version> getAllVersions()
    {
        return sortDescending(versionsByLabel.values());
    }

    /**
     * Sorts Versions into descending create date order (most recent first).
     * @param versions <b>Must be in order addVersion was called</b>.
     * @return
     */
    private Collection<Version> sortDescending(Collection<Version> versions)
    {
        List<Version> sortedVersions = new ArrayList<Version>(versions);

        if (versionComparatorDesc == null)
        {
            Collections.reverse(sortedVersions);
        }
        else
        {
            Collections.sort(sortedVersions, versionComparatorDesc);
        }
        return sortedVersions;
    }
    
    /**
     * Gets the predecessor of a specified version
     * 
     * @param version  the version object
     * @return         the predeceeding version, null if root version
     */
    public Version getPredecessor(Version version)
    {
        Version result = null;
        if (version != null)
        {
            result = getVersion(this.versionHistory.get(version.getVersionLabel()));
        }
        return result;
    }

    /**
     * Gets the succeeding versions of a specified version. If there are multiple
     * Versions they are sorted into descending create date order (most recent first).
     * 
     * @param version  the version object
     * @return         a collection containing the succeeding version, empty is none
     */
    public Collection<Version> getSuccessors(Version version)
    {
        ArrayList<Version> result = new ArrayList<Version>();
        
        if (version != null)
        {
            String versionLabel = version.getVersionLabel();
            
            if (this.versionHistory.containsValue(versionLabel) == true)
            {
                for (Entry<String, Version> entry: versionsByLabel.entrySet())
                {
                    String key = entry.getKey();
                    if (EqualsHelper.nullSafeEquals(this.versionHistory.get(key), versionLabel))
                    {
                        result.add(entry.getValue());
                    }
                }
            }
        }

        return sortDescending(result);
    }
    
    /**
     * Gets a version with a specified version label.  The version label is guaranteed 
     * unique within the version history.
     * 
     * @param versionLabel                   the version label
     * @return                               the version object
     * @throws VersionDoesNotExistException  indicates requested version does not exist
     */
    public Version getVersion(String versionLabel)
    {
        Version result = null;
        if (versionLabel != null)
        {
            result = this.versionsByLabel.get(versionLabel);
            
            if (result == null)
            {
                // Throw exception indicating that the version does not exit
                throw new VersionDoesNotExistException(versionLabel);
            }
        }
        return result;
    }
    
    /**
     * Add a version to the version history, <b>in the order they were
     * created</b>.
     * <p>
     * Used internally to build the version history tree.
     * 
     * @param version       the version object
     * @param predecessor   the preceeding version
     */
    public void addVersion(Version version, Version predecessor)
    {
        // TODO cope with exception case where duplicate version labels have been specified
        
        this.versionsByLabel.put(version.getVersionLabel(), version);
        
        if (predecessor != null)
        {
            this.versionHistory.put(version.getVersionLabel(), predecessor.getVersionLabel());
        }
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException
    {
        GetField fields = is.readFields();
        if (fields.defaulted("versionsByLabel"))
        {
            // This is a V2.2 class
            // The old 'rootVersion' maps to the current 'rootVersion'
            this.versionsByLabel = (HashMap<String, Version>) fields.get("versions", new HashMap<String, Version>());
            // The old 'versionHistory' maps to the current 'versionHistory'
            this.versionHistory = (HashMap<String, String>) fields.get("versionHistory", new HashMap<String, String>());
            // Need this comparator as versionsByLabel is not a LinkedHashMap in this version 
            this.versionComparatorDesc = new VersionLabelComparator(); 
        }
        else if (fields.defaulted("versionComparatorDesc"))
        {
            // This is a V3.1.0 class
            // The old 'rootVersion' maps to the current 'rootVersion'
            this.versionsByLabel = (HashMap<String, Version>) fields.get("versionsByLabel", new HashMap<String, Version>());
            // The old 'versionHistory' maps to the current 'versionHistory'
            this.versionHistory = (HashMap<String, String>) fields.get("versionHistory", new HashMap<String, String>());
            // Need this comparator as versionsByLabel is not a LinkedHashMap in this version 
            this.versionComparatorDesc = new VersionLabelComparator(); 
        }
        else
        {
            // This is a V4.1.3 (and 4.0.2 HF) class
            // The old 'rootVersion' maps to the current 'rootVersion'
            this.versionsByLabel = (Map<String, Version>) fields.get("versionsByLabel", new LinkedHashMap<String, Version>());
            // The old 'versionHistory' maps to the current 'versionHistory'
            this.versionHistory = (HashMap<String, String>) fields.get("versionHistory", new HashMap<String, String>());
            this.versionComparatorDesc = (Comparator<Version>) fields.get("versionComparatorDesc", null); 
        }
    }
    
    /**
     * @since 4.1.7
     */
    @Override public String toString()
    {
        // The toString result looks something like: "VersionHistoryImpl [1] , { '0.1', tail... }"
        final Collection<Version> allVersions = getAllVersions();
        
        final StringBuilder msg = new StringBuilder();
        
        msg.append(VersionHistoryImpl.class.getSimpleName())
           .append(" [") .append(allVersions.size()) .append("] ");
        if ( !allVersions.isEmpty())
        {
            msg.append(", { '") .append(getHeadVersion().getVersionLabel()) .append("', tail... }");
        }
        
        return msg.toString();
    }
}
