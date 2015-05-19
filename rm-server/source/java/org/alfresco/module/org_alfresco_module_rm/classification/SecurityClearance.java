/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification;

import java.io.Serializable;
import java.util.Objects;

import org.alfresco.service.cmr.security.PersonService.PersonInfo;

/**
 * A simple data type for a single user's security clearance.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public final class SecurityClearance implements Serializable
{
    /** Serial version uid */
    private static final long serialVersionUID = 8410664575120817707L;

    private final PersonInfo     personInfo;
    private final ClearanceLevel clearanceLevel;

    public SecurityClearance(final PersonInfo personInfo, final ClearanceLevel clearanceLevel)
    {
        // Do not check the PersonInfo, as it may be null for the system user's SecurityClearance.
        Objects.requireNonNull(clearanceLevel);

        this.personInfo     = personInfo;
        this.clearanceLevel = clearanceLevel;
    }

    /** Returns the {@link PersonInfo} for this security clearance. */
    public PersonInfo getPersonInfo() { return this.personInfo; }

    /** Returns the {@link ClearanceLevel} for this security clearance. */
    public ClearanceLevel getClearanceLevel() { return this.clearanceLevel; }

    @Override public String toString()
    {
        StringBuilder msg = new StringBuilder();
        msg.append(SecurityClearance.class.getSimpleName())
           .append(':').append(personInfo.getUserName())
           .append(" [").append(clearanceLevel).append(']');

        return  msg.toString();
    }

    @Override public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SecurityClearance that = (SecurityClearance) o;

        return this.personInfo.equals(that.personInfo) &&
               this.clearanceLevel.equals(that.clearanceLevel);
    }

    @Override public int hashCode() { return Objects.hash(personInfo, clearanceLevel); }
}
