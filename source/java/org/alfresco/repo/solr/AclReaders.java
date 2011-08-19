/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.solr;

import java.util.Set;

/**
 * Bean for SOLR ACL readers.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class AclReaders
{
    private Long aclId;
    private Set<String> readers;
    private long aclChangeSetId;
    
    @Override
    public String toString()
    {
        return "AclReaders [aclId=" + aclId + ", readers=" + readers + ", aclChangeSetId=" + aclChangeSetId + "]";
    }
    public Long getAclId()
    {
        return aclId;
    }
    public void setAclId(Long aclId)
    {
        this.aclId = aclId;
    }
    public Set<String> getReaders()
    {
        return readers;
    }
    public void setReaders(Set<String> aclReaders)
    {
        this.readers = aclReaders;
    }
    public long getAclChangeSetId()
    {
        return aclChangeSetId;
    }
    public void setAclChangeSetId(long aclChangeSetId)
    {
        this.aclChangeSetId = aclChangeSetId;
    }
    
}
