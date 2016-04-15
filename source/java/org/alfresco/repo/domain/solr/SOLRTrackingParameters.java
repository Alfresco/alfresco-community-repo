/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.domain.solr;

import java.util.List;

import org.alfresco.util.EqualsHelper;

/**
 * Holds parameters for SOLR DAO calls against <b>alf_transaction</b> and <b>alf_change_set</b>.
 * 
 * @since 4.0
 */
public class SOLRTrackingParameters
{
    private Long fromIdInclusive;
    private Long fromCommitTimeInclusive;
    private List<Long> ids;
    private Long toIdExclusive;
    private Long toCommitTimeExclusive;
    private final Long deletedTypeQNameId;

    /**
     * Construct the parameters
     * 
     * @param deletedTypeQNameId            the QName ID representing deleted nodes
     */
    public SOLRTrackingParameters(Long deletedTypeQNameId)
    {
        this.deletedTypeQNameId = deletedTypeQNameId;
    }
    
    public Long getFromIdInclusive()
    {
        return fromIdInclusive;
    }

    public void setFromIdInclusive(Long fromIdInclusive)
    {
        this.fromIdInclusive = fromIdInclusive;
    }

    public Long getFromCommitTimeInclusive()
    {
        return fromCommitTimeInclusive;
    }

    public void setFromCommitTimeInclusive(Long fromCommitTimeInclusive)
    {
        this.fromCommitTimeInclusive = fromCommitTimeInclusive;
    }

    public List<Long> getIds()
    {
        return ids;
    }

    public void setIds(List<Long> ids)
    {
        this.ids = ids;
    }

    /**
	 * Helper for cross-DB boolean support
	 * 
	 * @return             <tt>true</tt> always
	 */
	public boolean getTrue()
	{
	    return true;
	}
	
    /**
     * Helper for cross-DB boolean support
     * 
     * @return             <tt>false</tt> always
     */
	public boolean getFalse()
	{
	    return false;
	}

    public Long getDeletedTypeQNameId()
    {
        return deletedTypeQNameId;
    }

    public Long getToIdExclusive()
    {
        return toIdExclusive;
    }

    public void setToIdExclusive(Long toIdExclusive)
    {
        this.toIdExclusive = toIdExclusive;
    }

    public Long getToCommitTimeExclusive()
    {
        return toCommitTimeExclusive;
    }

    public void setToCommitTimeExclusive(Long toCommitTimeExclusive)
    {
        this.toCommitTimeExclusive = toCommitTimeExclusive;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fromCommitTimeInclusive == null) ? 0 : fromCommitTimeInclusive.hashCode());
        result = prime * result + ((fromIdInclusive == null) ? 0 : fromIdInclusive.hashCode());
        result = prime * result + ((ids == null) ? 0 : ids.hashCode());
        result = prime * result + ((toCommitTimeExclusive == null) ? 0 : toCommitTimeExclusive.hashCode());
        result = prime * result + ((toIdExclusive == null) ? 0 : toIdExclusive.hashCode());
        result = prime * result + ((deletedTypeQNameId == null) ? 0 : deletedTypeQNameId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SOLRTrackingParameters other = (SOLRTrackingParameters) obj;
        return
                EqualsHelper.nullSafeEquals(this.fromCommitTimeInclusive, other.fromCommitTimeInclusive) &&
                EqualsHelper.nullSafeEquals(this.fromIdInclusive, other.fromIdInclusive) &&
                EqualsHelper.nullSafeEquals(this.ids, other.ids) &&
                EqualsHelper.nullSafeEquals(this.toIdExclusive, other.toIdExclusive) &&
                EqualsHelper.nullSafeEquals(this.toCommitTimeExclusive, other.toCommitTimeExclusive) &&
                EqualsHelper.nullSafeEquals(this.deletedTypeQNameId, other.deletedTypeQNameId);
    }

    @Override
    public String toString()
    {
        return "SOLRTrackingParameters [fromIdInclusive=" + fromIdInclusive
                + ", fromCommitTimeInclusive=" + fromCommitTimeInclusive + ", ids=" + ids
                + ", toIdExclusive=" + toIdExclusive + ", toCommitTimeExclusive="
                + toCommitTimeExclusive + ", typeQNameId=" + deletedTypeQNameId + "]";
    }
}
