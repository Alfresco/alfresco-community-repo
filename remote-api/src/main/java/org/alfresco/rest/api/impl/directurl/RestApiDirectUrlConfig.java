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
package org.alfresco.rest.api.impl.directurl;

import org.alfresco.repo.content.directurl.AbstractDirectUrlConfig;
import org.alfresco.repo.content.directurl.InvalidDirectAccessUrlConfigException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * REST API direct access URL configuration settings.
 *
 * @author Sara Aspery
 */
public class RestApiDirectUrlConfig extends AbstractDirectUrlConfig
{
    private static final Log logger = LogFactory.getLog(RestApiDirectUrlConfig.class);

    /**
     * Configuration initialise
     */
    public void init()
    {
        validate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate()
    {
        // Disable direct access URLs for the REST API if any error found in the REST API direct access URL config
        try
        {
            validateDirectAccessUrlConfig();
        }
        catch (InvalidDirectAccessUrlConfigException ex)
        {
            logger.error("Disabling REST API direct access URLs due to configuration error: " + ex.getMessage());
            setEnabled(false);
        }
    }

    /* Helper method to validate the REST API direct access url configuration settings */
    private void validateDirectAccessUrlConfig() throws InvalidDirectAccessUrlConfigException
    {
        if (isEnabled())
        {
            if (getDefaultExpiryTimeInSec() == null)
            {
                logger.warn(String.format("Default expiry time property is missing: setting to system-wide default [%s].", getSysWideDefaultExpiryTimeInSec()));
                setDefaultExpiryTimeInSec(getSysWideDefaultExpiryTimeInSec());
            }

            if (getDefaultExpiryTimeInSec() < 1)
            {
                String errorMsg = String.format("REST API direct access URL default expiry time [%s] is invalid.", getDefaultExpiryTimeInSec());
                throw new InvalidDirectAccessUrlConfigException(errorMsg);
            }

            if (getDefaultExpiryTimeInSec() > getSysWideMaxExpiryTimeInSec())
            {
                String errorMsg = String.format("REST API direct access URL default expiry time [%s] exceeds system-wide maximum expiry time [%s].",
                        getDefaultExpiryTimeInSec(), getSysWideMaxExpiryTimeInSec());
                throw new InvalidDirectAccessUrlConfigException(errorMsg);
            }
        }
    }
}
