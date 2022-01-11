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

package org.alfresco.rm.rest.api.impl;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.rest.api.impl.SitesImpl;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.model.SiteUpdate;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.RMSites;
import org.alfresco.rm.rest.api.model.RMSite;
import org.alfresco.rm.rest.api.model.RMSiteCompliance;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;

/**
 * Centralizes access to site services.
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
public class RMSitesImpl extends SitesImpl implements RMSites
{
    private static final String RM_SITE_PRESET = "rm-site-dashboard";
    private static final String RM_SITE_ID = "rm";
    private static final int SITE_MAXLEN_TITLE = 256;
    private static final int SITE_MAXLEN_DESCRIPTION = 512;

    @Override
    public RMSite createRMSite(RMSite rmSite, Parameters parameters)
    {
        RMSiteCompliance compliance = rmSite.getCompliance();
        if (compliance == null)
        {
            compliance = RMSiteCompliance.STANDARD;
        }
        Site site = createSite(rmSite, parameters);
        return new RMSite(site, compliance);
    }

    @Override
    protected SiteInfo createSite(Site site)
    {
        return siteService.createSite(RM_SITE_PRESET, RM_SITE_ID, site.getTitle(), site.getDescription(), SiteVisibility.PUBLIC, getRMSiteType((RMSite) site));
    }

    /**
     * Even if the method it will be protected in core, we still need to override since we don't need to check if the visibility is set since for RM site it is always PUBLIC.
     * We also don't need to generate the id from title, or to check the id, since the id is always rm.
     * @param site
     * @return
     */
    @Override
    protected Site validateSite(Site site)
    {
        // site title - mandatory
        String siteTitle = site.getTitle();
        if ((siteTitle == null) || siteTitle.isEmpty())
        {
            throw new InvalidArgumentException("Site title is expected: "+siteTitle);
        }
        else if (siteTitle.length() > SITE_MAXLEN_TITLE)
        {
            throw new InvalidArgumentException("Site title exceeds max length of "+SITE_MAXLEN_TITLE+" characters");
        }

        String siteDescription = site.getDescription();

        if (siteDescription == null)
        {
            // workaround: to avoid Share error (eg. in My Sites dashlet / freemarker template)
            site.setDescription("");
        }

        if ((siteDescription != null) && (siteDescription.length() > SITE_MAXLEN_DESCRIPTION))
        {
            throw new InvalidArgumentException("Site description exceeds max length of "+SITE_MAXLEN_DESCRIPTION+" characters");
        }

        return site;
    }

    /**
     * Updates the RM site
     */
    public RMSite updateRMSite(String siteId, SiteUpdate update, Parameters parameters)
    {
        Site updatedSite = updateSite(siteId, update, parameters);
        SiteInfo siteInfo = siteService.getSite(siteId);
        RMSiteCompliance compliance = getCompliance(siteInfo);
        return new RMSite(updatedSite, compliance);
    }

    /**
     * Obtain compliance from site info
     *
     * @param siteInfo
     * @return
     */
    private RMSiteCompliance getCompliance(SiteInfo siteInfo)
    {
        NodeRef nodeRef = siteInfo.getNodeRef();
        QName siteType = nodeService.getType(nodeRef);
        RMSiteCompliance compliance;
        if (RecordsManagementModel.TYPE_RM_SITE.equals(siteType))
        {
            compliance = RMSiteCompliance.STANDARD;
        }
        else
        {
            compliance = RMSiteCompliance.DOD5015;
        }
        return compliance;
    }

    /**
     * Gets RM site type based on compliance.
     *
     * @param rmSite
     * @return
     */
    private QName getRMSiteType(RMSite rmSite)
    {
        RMSiteCompliance compliance = rmSite.getCompliance();
        if (compliance == null || compliance.equals(RMSiteCompliance.STANDARD))
        {
            return RecordsManagementModel.TYPE_RM_SITE;
        }
        else
        {
            return DOD5015Model.TYPE_DOD_5015_SITE;
        }
    }

    @Override
    public RMSite getRMSite(String siteId)
    {
        Site site = getSite(siteId);
        SiteInfo siteInfo = siteService.getSite(siteId);
        RMSiteCompliance compliance = getCompliance(siteInfo);
        return new RMSite(site, compliance);
    }

    @Override
    public void deleteRMSite(String siteId, Parameters parameters)
    {
        deleteSite(siteId, parameters);
        solveRMSiteNodeRefCaching();
    }

    /**
     * Method used for solving rm site nodeRef caching problem that affected rm site update and get from rest api, after site deletion from rest api.
     * See RM-4289 issue for details.
     *
     */
    private void solveRMSiteNodeRefCaching()
    {
        //since we do not have access to SiteServiceImpl.getSiteNodeRef(String shortName, boolean enforcePermissions) method we can use hasSite method to solve caching problem
        siteService.hasSite(RM_SITE_ID);
    }
}
