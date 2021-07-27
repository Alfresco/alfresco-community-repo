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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * System-wide direct access URL configuration settings.
 *
 * @author Sara Aspery
 */
public class SystemWideDirectUrlConfig implements DirectUrlConfig
{
    private static final Log logger = LogFactory.getLog(SystemWideDirectUrlConfig.class);

    /** Direct access url configuration settings */
    private Boolean isEnabled;
    private Long defaultExpiryTimeInSec;
    private Long maxExpiryTimeInSec;

    public void setIsEnabled(Boolean enabled)
    {
        isEnabled = enabled;
    }

    public void setDefaultExpiryTimeInSec(Long defaultExpiryTimeInSec)
    {
        this.defaultExpiryTimeInSec = defaultExpiryTimeInSec;
    }

    public void setMaxExpiryTimeInSec(Long maxExpiryTimeInSec)
    {
        this.maxExpiryTimeInSec = maxExpiryTimeInSec;
    }

    public Boolean isEnabled()
    {
        return isEnabled;
    }

    public Long getDefaultExpiryTimeInSec()
    {
        return defaultExpiryTimeInSec;
    }

    public Long getMaxExpiryTimeInSec()
    {
        return maxExpiryTimeInSec;
    }

    /**
     * Configuration initialise
     */
    public void init()
    {
        validate();
    }

    public void validate()
    {
        // Disable direct access URLs system-wide if any error found in the system-wide direct access URL config
        try
        {
            validateSystemDirectAccessUrlConfig();
        }
        catch (InvalidDirectAccessUrlConfigException ex)
        {
            logger.error("Disabling system-wide direct access URLs due to configuration error: " + ex.getMessage());
            setIsEnabled(false);
        }
    }

    /* Helper method to validate the system-wide direct access url configuration settings */
    private void validateSystemDirectAccessUrlConfig() throws InvalidDirectAccessUrlConfigException
    {
        if (isEnabled())
        {
            if (getDefaultExpiryTimeInSec() == null || getDefaultExpiryTimeInSec() < 1)
            {
                throw new InvalidDirectAccessUrlConfigException("System-wide direct access URL default expiry time is missing or invalid.");
            }

            if (getMaxExpiryTimeInSec() == null || getMaxExpiryTimeInSec() < 1)
            {
                throw new InvalidDirectAccessUrlConfigException("System-wide direct access URL maximum expiry time is missing or invalid.");
            }

            if (getDefaultExpiryTimeInSec() > getMaxExpiryTimeInSec())
            {
                throw new InvalidDirectAccessUrlConfigException("System-wide direct access URL default expiry time exceeds maximum expiry time.");
            }
        }
    }
}
