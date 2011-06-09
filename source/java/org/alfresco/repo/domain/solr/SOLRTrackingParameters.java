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
package org.alfresco.repo.domain.solr;

import java.util.Date;
import java.util.List;

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
    private Long fromRelatedIdInclusive;
    private Long toRelatedIdExclusive;
    private boolean trueOrFalse;

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

    public Long getFromRelatedIdInclusive()
    {
        return fromRelatedIdInclusive;
    }

    public void setFromRelatedIdInclusive(Long fromRelatedIdInclusive)
    {
        this.fromRelatedIdInclusive = fromRelatedIdInclusive;
    }

    public Long getToRelatedIdExclusive()
    {
        return toRelatedIdExclusive;
    }

    public void setToRelatedIdExclusive(Long toRelatedIdExclusive)
    {
        this.toRelatedIdExclusive = toRelatedIdExclusive;
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

	/**
	 * Simple mutalbe cross-DB boolean value
	 */
	public boolean isTrueOrFalse()
    {
        return trueOrFalse;
    }

	/**
     * Simple mutalbe cross-DB boolean value
	 */
    public void setTrueOrFalse(boolean trueOrFalse)
    {
        this.trueOrFalse = trueOrFalse;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("SOLRTrackingParameters")
          .append(", fromIdInclusive").append(fromIdInclusive)
          .append(", ids").append(ids == null ? null : ids.size())
          .append(", fromCommitTimeInclusive").append(fromCommitTimeInclusive == null ? null : new Date(fromCommitTimeInclusive))
          .append(", fromRelatedIdInclusive=").append(fromRelatedIdInclusive)
          .append(", toRelatedIdExclusive").append(toRelatedIdExclusive)
          .append("]");
        return sb.toString();
    }
}
