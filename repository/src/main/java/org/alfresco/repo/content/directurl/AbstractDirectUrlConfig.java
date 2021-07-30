/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.content.directurl;

/**
 * Direct Access Url configuration settings.
 *
 * @author Sara Aspery
 */
public abstract class AbstractDirectUrlConfig implements DirectUrlConfig
{
    /** System-wide direct access URL configuration */
    private SystemWideDirectUrlConfig systemWideDirectUrlConfig;

    /** Direct access URL configuration settings */
    private Boolean isEnabled;
    private Long defaultExpiryTimeInSec;

    public void setSystemWideDirectUrlConfig(SystemWideDirectUrlConfig systemWideDirectUrlConfig)
    {
        this.systemWideDirectUrlConfig = systemWideDirectUrlConfig;
    }

    public void setIsEnabled(Boolean enabled)
    {
        isEnabled = enabled;
    }

    public void setDefaultExpiryTimeInSec(Long defaultExpiryTimeInSec)
    {
        this.defaultExpiryTimeInSec = defaultExpiryTimeInSec;
    }

    protected Boolean isSysWideEnabled()
    {
        return systemWideDirectUrlConfig.isEnabled();
    }

    public Long getSysWideDefaultExpiryTimeInSec()
    {
        return systemWideDirectUrlConfig.getDefaultExpiryTimeInSec();
    }

    public Long getSysWideMaxExpiryTimeInSec()
    {
        return systemWideDirectUrlConfig.getMaxExpiryTimeInSec();
    }

    public Boolean isEnabled()
    {
        return isEnabled;
    }

    public Long getDefaultExpiryTimeInSec()
    {
        return defaultExpiryTimeInSec;
    }
}
