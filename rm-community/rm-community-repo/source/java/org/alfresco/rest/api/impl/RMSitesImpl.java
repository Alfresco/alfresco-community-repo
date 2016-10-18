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

package org.alfresco.rest.api.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteServiceException;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
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
 *
 * @author Silviu Dinuta
 * @since 2.6
 *
 */
public class RMSitesImpl extends SitesImpl implements RMSites
{
    private static final int SITE_MAXLEN_ID = 72;
    private static final int SITE_MAXLEN_TITLE = 256;
    private static final int SITE_MAXLEN_DESCRIPTION = 512;
    private static final String SITE_ID_VALID_CHARS_PARTIAL_REGEX = "A-Za-z0-9\\-";

    @Override
    public Site createSite(Site site, Parameters parameters) {
        // note: if site id is null then will be generated from the site title
        site = validateSite(site);

        SiteInfo siteInfo = null;
        try
        {
            siteInfo = siteService.createSite("rm-site-dashboard", site.getId(), site.getTitle(), site.getDescription(), site.getVisibility(), RecordsManagementModel.TYPE_RM_SITE);
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

        String personId = AuthenticationUtil.getFullyAuthenticatedUser();
        favouritesService.addFavourite(personId, siteNodeRef); // ignore result

        return getSite(siteInfo, true);
    }

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

        SiteVisibility siteVisibility = site.getVisibility();
        if (siteVisibility == null)
        {
            throw new InvalidArgumentException("Site visibility is expected: "+siteTitle+" (eg. PUBLIC, PRIVATE, MODERATED)");
        }

        String siteId = site.getId();
        if (siteId == null)
        {
            // generate a site id from title (similar to Share create site dialog)
            siteId = siteTitle.
                    trim(). // trim leading & trailing whitespace
                    replaceAll("[^"+SITE_ID_VALID_CHARS_PARTIAL_REGEX+" ]",""). // remove special characters (except spaces)
                    replaceAll(" +", " "). // collapse multiple spaces to single space
                    replace(" ","-"). // replaces spaces with dashs
                    toLowerCase(); // lowercase :-)
        }
        else
        {
            if (! siteId.matches("^["+SITE_ID_VALID_CHARS_PARTIAL_REGEX+"]+"))
            {
                throw new InvalidArgumentException("Invalid site id - should consist of alphanumeric/dash characters");
            }
        }

        if (siteId.length() > SITE_MAXLEN_ID)
        {
            throw new InvalidArgumentException("Site id exceeds max length of "+SITE_MAXLEN_ID+ "characters");
        }

        site.setId(siteId);

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
}
