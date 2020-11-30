/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.module;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.util.VersionNumber;
import org.apache.maven.artifact.versioning.ComparableVersion;

/**
 * The exising Alfresco VersionNumber can only be numeric.
 * ModuleVersionNumber allows string literals in the version number.
 * 
 * It follows maven conventions and actually uses the ComparableVersion class
 * from the maven code base.
 *
 * @author Gethin James
 */

public class ModuleVersionNumber implements Externalizable
{
    private static final long serialVersionUID = 8594906471270433420L;
    
    public static final ModuleVersionNumber VERSION_ZERO = new ModuleVersionNumber("0");;
    public static final ModuleVersionNumber VERSION_BIG  = new ModuleVersionNumber("999.999.999.99");

    // Matches versions with 3 or 4 parts to their basic number such as 1.2.3 or 1.2.3.4
    // that also optionally have a -A9 -M9 or -RC9 suffix whe 9 is an integer.
    private static Pattern A_M_RC_VERSION_PATTERN = Pattern.compile("((\\d+\\.){2,3}\\d+)(-(A|M|RC)\\d+)*");

    protected ComparableVersion delegate;
    
    public ModuleVersionNumber()
    {
        super();
    }
    
    public ModuleVersionNumber(String versionString)
    {
        delegate = new ComparableVersion(versionString);
    }

    public ModuleVersionNumber(VersionNumber versionCurrent)
    {
        this(versionCurrent.toString());
    }

    /**
     * Now that we create internal releases of the form {@code M.m.r-M9} (milestone), {@code M.m.r-A9} (alpha) and
     * {@code M.m.r-RC9} (release candidate) during development and testing, we need to ensure we can upgrade form any
     * of these to any other, as they may occur in any order and also to the final external release {@code M.m.r}. We
     * also will allow a change from the final release back to an internal one for regression testing.
     *
     * The code within {@link ModuleComponentHelper} which calls this method, checks if it is the same version
     * ({@code 0}) and then if it is downgrading ({@code > 0}), so if they share the same {@code M.m.r} part matches
     * AND is one of these formats, we return {@code <0}.
     *
     * @param installingVersion the new version
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object.
     */
    public int compareTo(ModuleVersionNumber installingVersion)
    {
        String thisVersion = toString();
        String thatVersion = installingVersion.toString();
        if (thisVersion.equals(thatVersion))
        {
            return 0;
        }

        String thisVersionWithoutSuffix = getVersionWithoutSuffix();
        if (thisVersionWithoutSuffix != null)
        {
            String thatVersionWithoutSuffix = installingVersion.getVersionWithoutSuffix();
            if (thisVersionWithoutSuffix.equals(thatVersionWithoutSuffix))
            {
                return -1;
            }
        }

        return delegate.compareTo(installingVersion.delegate);
    }

    String getVersionWithoutSuffix()
    {
        String versionWithoutAMOrRc = null;
        String fullVersion = toString();
        Matcher matcher = A_M_RC_VERSION_PATTERN.matcher(fullVersion);
        if (matcher.matches())
        {
            versionWithoutAMOrRc = matcher.group(1);
            // matcher.group(3) would be the suffix, such as "-M4"
            // matcher.group(4) would be the type of release: "RC", "A" or "M"
        }
        return versionWithoutAMOrRc;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.delegate == null) ? 0 : this.delegate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ModuleVersionNumber other = (ModuleVersionNumber) obj;
        if (this.delegate == null)
        {
            if (other.delegate != null) return false;
        }
        else if (!this.delegate.equals(other.delegate)) return false;
        return true;
    }

    @Override
    public String toString()
    {
    	return this.delegate.toString();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeUTF(delegate.toString());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        String versionString = in.readUTF();
        delegate = new ComparableVersion(versionString);
    }
}
