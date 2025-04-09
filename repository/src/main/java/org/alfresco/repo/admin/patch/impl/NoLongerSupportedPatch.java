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
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.admin.PatchException;

/**
 * Notifies the user that the patch about to be run is no longer supported and an incremental upgrade path must be followed.
 * 
 * @author Derek Hulley
 * @since 2.1.5
 */
public class NoLongerSupportedPatch extends AbstractPatch
{
    private static final String ERR_USE_INCREMENTAL_UPGRADE = "patch.noLongerSupportedPatch.err.use_incremental_upgrade";

    private String lastSupportedVersion;

    public NoLongerSupportedPatch()
    {}

    public void setLastSupportedVersion(String lastSupportedVersion)
    {
        this.lastSupportedVersion = lastSupportedVersion;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(lastSupportedVersion, "lastSupportedVersion");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        throw new PatchException(
                ERR_USE_INCREMENTAL_UPGRADE,
                super.getId(),
                lastSupportedVersion,
                lastSupportedVersion);
    }
}
