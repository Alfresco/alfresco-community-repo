
package org.alfresco.repo.search.impl.solr.facet.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ISO9075;
import org.springframework.extensions.surf.util.ParameterCheck;

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
