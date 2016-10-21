/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteServiceException;
import org.alfresco.rest.api.impl.SiteImportPackageHandler;
import org.alfresco.rest.api.impl.SitesImpl;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.RMSites;
import org.alfresco.rm.rest.api.model.RMSite;
import org.alfresco.rm.rest.api.model.RMSiteCompliance;
import org.alfresco.rm.rest.api.model.SiteUpdate;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterContentCache;
import org.alfresco.service.cmr.view.ImporterProgress;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;

/**
 * Centralizes access to site services.
 *
 * @author Silviu Dinuta
 * @since 2.6
 *
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
        if(compliance == null)
        {
            compliance = RMSiteCompliance.STANDARD;
        }
        Site site = createSite(rmSite, parameters);
        return new RMSite(site, compliance);
    }

    @Override
    public Site createSite(Site site, Parameters parameters)
    {
        site = validateSite(site);

        SiteInfo siteInfo = null;
        try
        {
            siteInfo = siteService.createSite(RM_SITE_PRESET, RM_SITE_ID, site.getTitle(), site.getDescription(),
                        SiteVisibility.PUBLIC, getRMSiteType((RMSite) site));
        }
        catch (SiteServiceException sse)
        {
            if (sse.getMsgId().equals("site_service.unable_to_create"))
            {
                throw new ConstraintViolatedException(sse.getMessage());
            }
            else
            {
                throw sse;
            }
        }

        String siteId = siteInfo.getShortName();
        NodeRef siteNodeRef = siteInfo.getNodeRef();

        // import default/fixed preset Share surf config
        importSite(siteId, siteNodeRef);

        // pre-create doclib
        siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);

        // default false (if not provided)
        boolean skipAddToFavorites = Boolean.valueOf(parameters.getParameter(PARAM_SKIP_ADDTOFAVORITES));
        if (skipAddToFavorites == false)
        {
            String personId = AuthenticationUtil.getFullyAuthenticatedUser();
            favouritesService.addFavourite(personId, siteNodeRef); // ignore result
        }

        return getSite(siteInfo, true);
    }

    /**
     * Copied from SitesImpl since we didn't had access to it.
     *
     * @param siteInfo
     * @param includeRole
     * @return
     */
    private Site getSite(SiteInfo siteInfo, boolean includeRole)
    {
        // set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
        String siteId = siteInfo.getShortName();
        String role = null;
        if(includeRole)
        {
            role = getSiteRole(siteId);
        }
        return new Site(siteInfo, role);
    }

    /**
     * Copied from SitesImpl since we didn't had access to it
     *
     * @param siteId
     * @param siteNodeRef
     */
    private void importSite(final String siteId, final NodeRef siteNodeRef)
    {
        ImportPackageHandler acpHandler = new SiteImportPackageHandler(siteSurfConfig, siteId);
        Location location = new Location(siteNodeRef);
        ImporterBinding binding = new ImporterBinding()
        {
            @Override
            public String getValue(String key)
            {
                if (key.equals("siteId"))
                {
                    return siteId;
                }
                return null;
            }

            @Override
            public UUID_BINDING getUUIDBinding()
            {
                return UUID_BINDING.CREATE_NEW;
            }

            @Override
            public QName[] getExcludedClasses()
            {
                return null;
            }

            @Override
            public boolean allowReferenceWithinTransaction()
            {
                return false;
            }

            @Override
            public ImporterContentCache getImportConentCache()
            {
                return null;
            }
        };
        importerService.importView(acpHandler, location, binding, (ImporterProgress)null);
    }

    /**
     * This method is copied from SitesImpl since we could not access it since it is private.
     *
     * Even if the method it will be protected in core, we still need to override since we don't need to check if the visibility is set since for RM site it is always PUBLIC.
     * We also don't need to generate the id from title, or to check the id, since the id is always rm.
     * @param site
     * @return
     */
    private Site validateSite(Site site)
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
        if(RecordsManagementModel.TYPE_RM_SITE.equals(siteType))
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
     * TODO copied from core 5.2.N since we don't have it in 5.2.a-EA version. To be removed when upgrading.
     * @param siteId
     * @param update
     * @param parameters
     * @return
     */
    public Site updateSite(String siteId, SiteUpdate update, Parameters parameters)
    {
        // Get the site by ID (aka short name)
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new EntityNotFoundException(siteId);
        }

        // Bind any provided values to the site info, allowing for "partial" updates.
        if (update.getTitle() != null)
        {
            siteInfo.setTitle(update.getTitle());
        }
        if (update.getDescription() != null)
        {
            siteInfo.setDescription(update.getDescription());
        }
        if (update.getVisibility() != null)
        {
            siteInfo.setVisibility(update.getVisibility());
        }

        // Validate the new details
        validateSite(new Site(siteInfo, null));

        // Perform the actual update.
        siteService.updateSite(siteInfo);

        return getSite(siteId);
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
}
