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
package org.alfresco.repo.module;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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

    public int compareTo(ModuleVersionNumber installingVersion)
    {
        return delegate.compareTo(installingVersion.delegate);
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
