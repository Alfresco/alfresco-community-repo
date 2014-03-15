/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.repo.web.scripts.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterPropString;
import org.alfresco.repo.node.getchildren.FilterPropString.FilterTypeString;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ScriptPagingDetails;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the <i>site-admin-sites.get</i> web script.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class SiteAdminSitesGet extends DeclarativeWebScript
{
    private static final String NAME_FILTER = "nf";
    private static final String MAX_ITEMS = "maxItems";
    private static final String SKIP_COUNT = "skipCount";
    private static final int DEFAULT_MAX_ITEMS_PER_PAGE = 50;

    private SiteService siteService;
    private NodeService nodeService;
    private PersonService personService;

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        // check the current user access rights
        if (!siteService.isSiteAdmin(currentUser))
        {
            // Note: security, no message to indicate why
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Resource no found.");
        }

        // Create paging
        final ScriptPagingDetails paging = new ScriptPagingDetails(getIntParameter(req, MAX_ITEMS,
                    DEFAULT_MAX_ITEMS_PER_PAGE), getIntParameter(req, SKIP_COUNT, 0));

        // request a total count of found items
        paging.setRequestTotalCountMax(Integer.MAX_VALUE);

        final List<FilterProp> filterProp = getFilterProperties(req.getParameter(NAME_FILTER));

        final List<Pair<QName, Boolean>> sortProps = new ArrayList<Pair<QName, Boolean>>();
        sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_NAME, true));

        PagingResults<SiteInfo> pagingResults = AuthenticationUtil.runAs(
                    new AuthenticationUtil.RunAsWork<PagingResults<SiteInfo>>()
                    {
                        public PagingResults<SiteInfo> doWork() throws Exception
                        {
                            return siteService.listSites(filterProp, sortProps, paging);
                        }
                    }, AuthenticationUtil.getAdminUserName());

        List<SiteInfo> result = pagingResults.getPage();
        List<SiteState> sites = new ArrayList<SiteState>(result.size());
        for (SiteInfo info : result)
        {
            sites.add(SiteState.create(info,
                        siteService.listMembers(info.getShortName(), null, SiteModel.SITE_MANAGER, 0), currentUser,
                        nodeService, personService));
        }

        Map<String, Object> sitesData = new HashMap<String, Object>(6);

        // Site data
        sitesData.put("items", sites);
        // Paging data
        sitesData.put("count", result.size());
        sitesData.put("hasMoreItems", pagingResults.hasMoreItems());
        sitesData.put("totalItems", (pagingResults.getTotalResultCount() == null ? -1 : pagingResults.getTotalResultCount().getFirst()));
        sitesData.put("skipCount", paging.getSkipCount());
        sitesData.put("maxItems", paging.getMaxItems());
        
        // Create the model from the site and pagination data
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("data", sitesData);

        return model;
    }

    private int getIntParameter(WebScriptRequest req, String paramName, int defaultValue)
    {
        String paramString = req.getParameter(paramName);

        if (paramString != null)
        {
            try
            {
                int param = Integer.valueOf(paramString);

                if (param >= 0)
                {
                    return param;
                }
            }
            catch (NumberFormatException e)
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }

        return defaultValue;
    }

    private List<FilterProp> getFilterProperties(String filter)
    {
        if (filter == null || filter.isEmpty() || filter.equals("*"))
        {
            return null;
        }
        List<FilterProp> filterProps = new ArrayList<FilterProp>();
        filterProps.add(new FilterPropString(ContentModel.PROP_NAME, filter, FilterTypeString.STARTSWITH_IGNORECASE));
        filterProps.add(new FilterPropString(ContentModel.PROP_TITLE, filter, FilterTypeString.STARTSWITH_IGNORECASE));
        return filterProps;
    }
}
