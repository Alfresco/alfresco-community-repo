/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rm.rest.api;

import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.SiteUpdate;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.model.RMSite;

/**
 * RM Sites API
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
public interface RMSites extends Sites
{
    /**
     * Creates RM site
     *
     * @param site
     * @param parameters
     * @return
     */
    RMSite createRMSite(RMSite site, Parameters parameters);

    /**
     * Gets RM site
     * @param siteId
     * @return
     */
    RMSite getRMSite(String siteId);

    /**
     * Updates RM site
     * @param siteId
     * @param site
     * @param parameters
     * @return
     */
    RMSite updateRMSite(String siteId, SiteUpdate site, Parameters parameters);

    /**
     * Deletes RM site
     *
     * @param siteId
     * @param parameters
     */
    void deleteRMSite(String siteId, Parameters parameters);
}
