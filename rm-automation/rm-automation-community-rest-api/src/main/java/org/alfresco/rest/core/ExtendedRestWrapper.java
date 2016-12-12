/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.core;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.rm.community.requests.igCoreAPI.RestIGCoreAPI;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FIXME!!!
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class ExtendedRestWrapper extends RestWrapper
{
    @Autowired
    private ExtendedRestProperties extendedRestProperties;

    public RestIGCoreAPI withIGCoreAPI()
    {
        return new RestIGCoreAPI(this, extendedRestProperties);
    }
}
