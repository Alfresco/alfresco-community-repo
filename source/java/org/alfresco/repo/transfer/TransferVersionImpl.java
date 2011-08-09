/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.transfer.TransferVersion;
import org.alfresco.service.descriptor.Descriptor;

public class TransferVersionImpl implements TransferVersion
{
    private String versionMajor;
    private String versionMinor;
    private String versionRevision;
    private String edition;

    /**
     *
     * @param versionMajor
     * @param versionMinor
     * @param versionRevision
     * @param edition
     */
    public TransferVersionImpl(String versionMajor, String versionMinor, String versionRevision, String edition)
    {
        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
        this.versionRevision = versionRevision;
        this.edition = edition;
    }

    /**
     * Construct a transferVersion from a system descriptor
     * @param d the system descriptor
     */
    public TransferVersionImpl(Descriptor d)
    {
        this.versionMajor = d.getVersionMajor();
        this.versionMinor = d.getVersionMinor();
        this.versionRevision = d.getVersionRevision();
        this.edition = d.getEdition();
    }

    @Override
    public String getVersionMajor()
    {
        return versionMajor;
    }

    @Override
    public String getVersionMinor()
    {
        return versionMinor;
    }

    @Override
    public String getVersionRevision()
    {
        return versionRevision;
    }

    @Override
    public String getEdition()
    {
        return edition;
    }

    public String toString()
    {
        StringBuilder version = new StringBuilder();
        version.append(getEdition());
        version.append(".");
        version.append(getVersionMajor());
        version.append(".");
        version.append(getVersionMinor());
        version.append(".");
        version.append(getVersionRevision());

        return version.toString();
    }

    public int hashCode()
    {
        if(edition != null && versionMinor != null)
        {
            return edition.hashCode() + versionMinor.hashCode() * 37;
        }
        else
        {
            return 1;
        }
    }

    public boolean equals(Object other)
    {
        if(other == null)
        {
            return false;
        }

        if (other instanceof TransferVersion)
        {
            TransferVersion v = (TransferVersion)other;

            if(!edition.equalsIgnoreCase(v.getEdition()))
            {
                return false;
            }
            if(!versionMajor.equalsIgnoreCase(v.getVersionMajor()))
            {
                return false;
            }
            if(!versionMinor.equalsIgnoreCase(v.getVersionMinor()))
            {
                return false;
            }
            if(!versionRevision.equalsIgnoreCase(v.getVersionRevision()))
            {
                return false;
            }
        }
        return true;
    }
}
