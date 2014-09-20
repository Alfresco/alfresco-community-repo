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
