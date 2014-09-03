/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.admin.patch.PatchExecuter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Patch to assign users and groups to default zones
 * 
 * @author andyh
 */
public class AuthorityDefaultZonesPatch extends AbstractPatch
{
    /** Success message. */
    private static final String MSG_SUCCESS = "patch.authorityDefaultZonesPatch.result";
    private static final String MSG_UPDATE_USERS = "patch.authorityDefaultZonesPatch.users";
    private static final String MSG_UPDATE_GROUPS = "patch.authorityDefaultZonesPatch.groups";
    private static Log progress_logger = LogFactory.getLog(PatchExecuter.class);

    /** The authority service. */
    private AuthorityService authorityService;
    private SiteService siteService;

    /**
     * Sets the authority service.
     * 
     * @param authorityService
     *            the authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Set the site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        int count = 0;
        int total = authorityService.getAllAuthorities(AuthorityType.USER).size() + authorityService.getAllAuthorities(AuthorityType.GROUP).size();
        reportProgress(total, count);
        String msg = I18NUtil.getMessage(MSG_UPDATE_USERS);
        progress_logger.info(msg);
        count = setZonesForPeople(total, count);
        msg = I18NUtil.getMessage(MSG_UPDATE_GROUPS);
        progress_logger.info(msg);
        setZonesForGroups(total, count);

        return MSG_SUCCESS;

    }

    private int setZonesForPeople(int total, int start)
    {
        Set<String> defaultZones = new HashSet<String>(2, 1.0f);
        defaultZones.add(AuthorityService.ZONE_APP_DEFAULT);
        defaultZones.add(AuthorityService.ZONE_AUTH_ALFRESCO);

        List<Action> personActions = new ArrayList<Action>(1);
        personActions.add(new Action(null, defaultZones, ActionType.SET));

        return setZones(AuthorityType.USER, personActions, total, start);

    }

    private int setZonesForGroups(int total, int start)
    {
        Set<String> defaultZones = new HashSet<String>(2, 1.0f);
        defaultZones.add(AuthorityService.ZONE_APP_DEFAULT);
        defaultZones.add(AuthorityService.ZONE_AUTH_ALFRESCO);

        Set<String> shareZones = new HashSet<String>(2, 1.0f);
        shareZones.add(AuthorityService.ZONE_APP_SHARE);
        shareZones.add(AuthorityService.ZONE_AUTH_ALFRESCO);

        List<SiteInfo> sites = siteService.listSites(null, null);

        List<Action> groupActions = new ArrayList<Action>(sites.size() * 5 + 1);
        for (SiteInfo site : sites)
        {
            groupActions.add(new Action("GROUP_site_" + site.getShortName(), shareZones, ActionType.SET));
            groupActions.add(new Action("GROUP_site_" + site.getShortName()+"_SiteManager", shareZones, ActionType.SET));
            groupActions.add(new Action("GROUP_site_" + site.getShortName()+"_SiteCollaborator", shareZones, ActionType.SET));
            groupActions.add(new Action("GROUP_site_" + site.getShortName()+"_SiteContributor", shareZones, ActionType.SET));
            groupActions.add(new Action("GROUP_site_" + site.getShortName()+"_SiteConsumer", shareZones, ActionType.SET));
        }
        groupActions.add(new Action(null, defaultZones, ActionType.SET));

        return setZones(AuthorityType.GROUP, groupActions, total, start);

    }

    private int setZones(AuthorityType authorityType, List<Action> actions, int total, int start)
    {
        int count = start;
        Set<String> authorities = authorityService.getAllAuthorities(authorityType);
        for (String authority : authorities)
        {
            for (Action action : actions)
            {
                if (action.name != null)
                {
                    if (action.name.equals(authority))
                    {
                        fixAuthority(action.actionType, action.zones, authority);
                        break;
                    }
                }
                else
                {
                    fixAuthority(action.actionType, action.zones, authority);
                    break;
                }
            }
            count++;
            reportProgress(total, count);
        }
        return count;
    }
    

    private void fixAuthority(ActionType actionType, Set<String> zones, String authority)
    {
        Set<String> current;
        switch (actionType)
        {
        case ADD:
            authorityService.addAuthorityToZones(authority, zones);
            break;
        case SET:
            current = authorityService.getAuthorityZones(authority);
            authorityService.removeAuthorityFromZones(authority, current);
            authorityService.addAuthorityToZones(authority, zones);
            break;
        case SET_IF_UNSET:
            current = authorityService.getAuthorityZones(authority);
            if (current.size() == 0)
            {
                authorityService.addAuthorityToZones(authority, zones);

            }
            break;
        }
    }

    private enum ActionType
    {
        ADD, SET, SET_IF_UNSET;
    }

    private static class Action
    {
        String name;

        Set<String> zones;

        ActionType actionType;

        Action(String name, Set<String> zones, ActionType actionType)
        {
            this.name = name;
            this.zones = zones;
            this.actionType = actionType;
        }
    }

}
