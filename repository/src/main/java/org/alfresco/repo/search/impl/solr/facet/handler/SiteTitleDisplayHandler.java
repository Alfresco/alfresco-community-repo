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

package org.alfresco.repo.search.impl.solr.facet.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.surf.util.ParameterCheck;

import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ISO9075;

/**
 * A simple handler to get the site title from the site short name.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SiteTitleDisplayHandler extends AbstractFacetLabelDisplayHandler
{
    private final Map<String, String> nonSiteLocationsLabels;

    public SiteTitleDisplayHandler(Set<String> supportedFieldFacets)
    {
        this(supportedFieldFacets, Collections.<String, String> emptyMap());
    }

    public SiteTitleDisplayHandler(Set<String> supportedFieldFacets, Map<String, String> nonSiteLocationsLabels)
    {
        ParameterCheck.mandatory("supportedFieldFacets", supportedFieldFacets);

        this.supportedFieldFacets = Collections.unmodifiableSet(new HashSet<>(supportedFieldFacets));
        this.nonSiteLocationsLabels = nonSiteLocationsLabels == null ? Collections.<String, String> emptyMap() : nonSiteLocationsLabels;
    }

    @Override
    public FacetLabel getDisplayLabel(String value)
    {
        // Solr returns the site short name encoded
        value = ISO9075.decode(value);
        String title = null;

        if (nonSiteLocationsLabels.containsKey(value))
        {
            title = nonSiteLocationsLabels.get(value);
        }
        else
        {
            SiteService siteService = serviceRegistry.getSiteService();
            SiteInfo siteInfo = siteService.getSite(value);
            title = siteInfo != null ? siteInfo.getTitle() : value;
        }

        return new FacetLabel(value, title, -1);
    }
}
