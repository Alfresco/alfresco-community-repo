/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api;

import org.alfresco.rest.api.impl.directurl.RestApiDirectUrlConfig;
import org.alfresco.rest.api.model.DirectAccessUrlRequest;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.apache.commons.lang3.BooleanUtils;

/**
 * Helper class for retrieving direct access URLs options.
 *
 * @author Sara Aspery
 */
public class DirectAccessUrlHelper
{
    private RestApiDirectUrlConfig restApiDirectUrlConfig;

    public void setRestApiDirectUrlConfig(RestApiDirectUrlConfig restApiDirectUrlConfig)
    {
        this.restApiDirectUrlConfig = restApiDirectUrlConfig;
    }

    public Long getDefaultExpiryTimeInSec()
    {
        if (restApiDirectUrlConfig ==null || !restApiDirectUrlConfig.isEnabled())
        {
            throw new DisabledServiceException("Direct access url isn't available.");
        }

        return restApiDirectUrlConfig.getDefaultExpiryTimeInSec();
    }

    public boolean getAttachment(DirectAccessUrlRequest directAccessUrlRequest)
    {
        boolean attachment = true;
        if (directAccessUrlRequest != null )
        {
            attachment = BooleanUtils.toBooleanDefaultIfNull(directAccessUrlRequest.isAttachment(), true);
        }
        return attachment;
    }

    
    public String getFileName(DirectAccessUrlRequest directAccessUrlRequest)
    {
        return directAccessUrlRequest != null ? directAccessUrlRequest.getFileName() : null;
    }
}
