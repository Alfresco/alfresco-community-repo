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
 * Content store direct access URL configuration settings.
 *
 * @author Sara Aspery
 */
public class ContentStoreDirectUrlConfig extends AbstractDirectUrlConfig {
    private static final Log logger = LogFactory.getLog(ContentStoreDirectUrlConfig.class);

    private Long maxExpiryTimeInSec;

    public void setMaxExpiryTimeInSec(Long maxExpiryTimeInSec) {
        this.maxExpiryTimeInSec = maxExpiryTimeInSec;
    }

    public Long getMaxExpiryTimeInSec() {
        return maxExpiryTimeInSec;
    }

    /** Configuration initialise */
    public void init() {
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void validate() {
        // Disable direct access URLs for the content store if any error found in the content store
        // direct access URL config
        try {
            validateDirectAccessUrlConfig();
        } catch (InvalidDirectAccessUrlConfigException ex) {
            logger.error(
                    "Disabling content store direct access URLs due to configuration error: "
                            + ex.getMessage());
            setEnabled(false);
        }
        logger.info(
                "Content store direct access URLs are " + (isEnabled() ? "enabled" : "disabled"));
    }

    /* Helper method to validate the content direct access url configuration settings */
    private void validateDirectAccessUrlConfig() throws InvalidDirectAccessUrlConfigException {
        if (isEnabled()) {
            if (getMaxExpiryTimeInSec() == null) {
                logger.warn(
                        String.format(
                                "Maximum expiry time property is missing: setting to system-wide"
                                        + " maximum [%s].",
                                getSysWideMaxExpiryTimeInSec()));
                setMaxExpiryTimeInSec(getSysWideMaxExpiryTimeInSec());
            } else if (getMaxExpiryTimeInSec() > getSysWideMaxExpiryTimeInSec()) {
                String errorMsg =
                        String.format(
                                "Content store direct access URL maximum expiry time [%s] exceeds"
                                        + " system-wide maximum expiry time [%s].",
                                getMaxExpiryTimeInSec(), getSysWideMaxExpiryTimeInSec());
                throw new InvalidDirectAccessUrlConfigException(errorMsg);
            }

            if (getDefaultExpiryTimeInSec() == null) {
                logger.warn(
                        String.format(
                                "Default expiry time property is missing: setting to system-wide"
                                        + " default [%s].",
                                getSysWideDefaultExpiryTimeInSec()));
                setDefaultExpiryTimeInSec(getSysWideDefaultExpiryTimeInSec());
            } else if (getDefaultExpiryTimeInSec() > getMaxExpiryTimeInSec()) {
                logger.warn(
                        String.format(
                                "Default expiry time property [%s] exceeds maximum expiry time for"
                                    + " content store [%s]: setting to system-wide default [%s].",
                                getDefaultExpiryTimeInSec(),
                                getMaxExpiryTimeInSec(),
                                getSysWideDefaultExpiryTimeInSec()));
                setDefaultExpiryTimeInSec(getSysWideDefaultExpiryTimeInSec());
            } else if (getDefaultExpiryTimeInSec() > getSysWideDefaultExpiryTimeInSec()) {
                logger.warn(
                        String.format(
                                "Default expiry time property [%s] exceeds system-wide default"
                                        + " expiry time [%s]: setting to system-wide default.",
                                getDefaultExpiryTimeInSec(), getSysWideDefaultExpiryTimeInSec()));
                setDefaultExpiryTimeInSec(getSysWideDefaultExpiryTimeInSec());
            }

            if (getDefaultExpiryTimeInSec() < 1) {
                String errorMsg =
                        String.format(
                                "Content store direct access URL default expiry time [%s] is"
                                        + " invalid.",
                                getDefaultExpiryTimeInSec());
                throw new InvalidDirectAccessUrlConfigException(errorMsg);
            }

            if (getDefaultExpiryTimeInSec() > getSysWideMaxExpiryTimeInSec()) {
                String errorMsg =
                        String.format(
                                "Content store direct access URL default expiry time [%s] exceeds"
                                        + " system-wide maximum expiry time [%s].",
                                getDefaultExpiryTimeInSec(), getSysWideMaxExpiryTimeInSec());
                throw new InvalidDirectAccessUrlConfigException(errorMsg);
            }

            if (getDefaultExpiryTimeInSec() > getMaxExpiryTimeInSec()) {
                String errorMsg =
                        String.format(
                                "Content store direct access URL default expiry time [%s] exceeds"
                                        + " content store maximum expiry time [%s].",
                                getDefaultExpiryTimeInSec(), getMaxExpiryTimeInSec());
                throw new InvalidDirectAccessUrlConfigException(errorMsg);
            }
        }
    }
}
