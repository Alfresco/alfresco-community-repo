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

package org.alfresco.repo.site;

import java.io.Serializable;

import org.alfresco.service.cmr.site.SiteMemberInfo;

/**
 * Site member's information class
 * 
 * @author Jamal Kaabi-Mofrad
 * @since Odin
 */
public class SiteMemberInfoImpl implements SiteMemberInfo, Serializable
{
    private static final long serialVersionUID = -5902865692214513762L;

    private String memberName;

    private String memberRole;

    private boolean memberOfGroup;

    /**
     * Constructor
     * 
     * @param memberName The name of an individual or a group
     * @param memberRole The role of the individual or group
     * @param isMemberOfGroup Whether a member belongs to a group with access
     *            rights to the site or not
     */
    public SiteMemberInfoImpl(String memberName, String memberRole, boolean isMemberOfGroup)
    {
        this.memberName = memberName;
        this.memberRole = memberRole;
        this.memberOfGroup = isMemberOfGroup;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteMemberInfo#getMemberName()
     */
    @Override
    public String getMemberName()
    {
        return this.memberName;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteMemberInfo#getMemberRole()
     */
    @Override
    public String getMemberRole()
    {
        return memberRole;
    }

    /**
     * @see org.alfresco.service.cmr.site.SiteMemberInfo#isMemberOfGroup()
     */
    @Override
    public boolean isMemberOfGroup()
    {
        return memberOfGroup;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.memberName == null) ? 0 : this.memberName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof SiteMemberInfoImpl)) { return false; }
        SiteMemberInfoImpl other = (SiteMemberInfoImpl) obj;
        if (this.memberName == null)
        {
            if (other.memberName != null) { return false; }
        }
        else if (!this.memberName.equals(other.memberName)) { return false; }
        return true;
    }
}
