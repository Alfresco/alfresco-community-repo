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
package org.alfresco.service.cmr.audit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.util.Pair;

/**
 * Parameters controlling audit queries.
 * 
 * @author Derek Hulley
 * @since 3.3
 */
public class AuditQueryParameters
{
    private boolean forward;
    private String applicationName;
    private String user;
    private Long fromId;
    private Long toId;
    private Long fromTime;
    private Long toTime;
    private List<Pair<String, Serializable>> searchKeyValues;
    
    /**
     * Defaults:<br/>
     * &nbsp;<code>forward = true;</code><br/>
     * &nbsp;<code>searchKeyValues = emptylist</code><br/>
     * &nbsp:<code>others = null</code>
     */
    public AuditQueryParameters()
    {
        forward = true;
        searchKeyValues = new ArrayList<Pair<String,Serializable>>();
    }
    
    /**
     * @return                  Returns <tt>true</tt> if any query using these parameters will
     *                          necessarily yield no results.
     */
    public boolean isZeroResultQuery()
    {
        if (fromId != null && toId != null && fromId.compareTo(toId) > 0)
        {
            // Inverted IDs
            return true;
        }
        if (fromTime != null && toTime != null && fromTime.compareTo(toTime) > 0)
        {
            // Inverted IDs
            return true;
        }
        return false;
    }

    /**
     * @return                  Returns <tt>true</tt> if the results are ordered by increasing ID
     */
    public boolean isForward()
    {
        return forward;
    }

    /**
     * @param forward           <tt>true</tt> for results to ordered from first to last,
     *                          or <tt>false</tt> to order from last to first
     */
    public void setForward(boolean forward)
    {
        this.forward = forward;
    }

    /**
     * @return                  Returns if not <tt>null</tt>, find entries logged against this application
     */
    public String getApplicationName()
    {
        return applicationName;
    }

    /**
     * @param applicationName   if not <tt>null</tt>, find entries logged against this application
     */
    public void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }

    /**
     * @return                  Returns if not <tt>null</tt>, find entries logged against this user
     */
    public String getUser()
    {
        return user;
    }

    /**
     * @param user              if not <tt>null</tt>, find entries logged against this user
     */
    public void setUser(String user)
    {
        this.user = user;
    }

    /**
     * @return                  Returns the ID to search from (<tt>null</tt> to start at the beginning)
     */
    public Long getFromId()
    {
        return fromId;
    }

    /**
     * @param fromId            the ID to search from (<tt>null</tt> to start at the beginning)
     */
    public void setFromId(Long fromId)
    {
        this.fromId = fromId;
    }

    /**
     * @return                  Returns the ID to search to (<tt>null</tt> for no limit)
     */
    public Long getToId()
    {
        return toId;
    }

    /**
     * @param toId              the start ID to search to (<tt>null</tt> for no limit)
     */
    public void setToId(Long toId)
    {
        this.toId = toId;
    }

    /**
     * @return                  Returns the start search time (<tt>null</tt> to start at the beginning)
     */
    public Long getFromTime()
    {
        return fromTime;
    }

    /**
     * @param fromTime          the start search time (<tt>null</tt> to start at the beginning)
     */
    public void setFromTime(Long fromTime)
    {
        this.fromTime = fromTime;
    }

    /**
     * @return                  Returns the end search time (<tt>null</tt> for no limit)
     */
    public Long getToTime()
    {
        return toTime;
    }

    /**
     * @param toTime            the end search time (<tt>null</tt> for no limit) 
     */
    public void setToTime(Long toTime)
    {
        this.toTime = toTime;
    }

    /**
     * 
     * @return                  Returns the search keys for the query
     */
    public List<Pair<String, Serializable>> getSearchKeyValues()
    {
        return Collections.unmodifiableList(searchKeyValues);
    }

    /**
     * Add a search key pair.
     * 
     * @param searchKey         the path-value pair.  Either the path ({@link Pair#getFirst() first} value)
     *                          or the search value ({@link Pair#getSecond() second} value) may be <tt>null</tt>,
     *                          but not both.
     */
    public void addSearchKey(String searchKey, Serializable searchValue)
    {
        if (searchKey == null && searchValue == null)
        {
            throw new IllegalArgumentException("A search key must have a 'searchKey' and/or a 'searchValue'.");
        }
        if (searchKeyValues.size() > 0)
        {
            throw new UnsupportedOperationException("Only one search key-value pair is currently supported.");
        }
            
        this.searchKeyValues.add(new Pair<String, Serializable>(searchKey, searchValue));
    }
}
