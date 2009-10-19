/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.admin.patch.PatchExecuter;
import org.alfresco.repo.domain.hibernate.HibernateSessionHelper;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private AVMService avmService;

    private SiteService siteService;

    private HibernateSessionHelper hibernateSessionHelper;

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
     * Set the avm service
     * @param avmService
     */
    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    /**
     * Set the site service
     * @param siteService
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * @param hibernateSessionHelper
     */
    public void setHibernateSessionHelper(HibernateSessionHelper hibernateSessionHelper)
    {
        this.hibernateSessionHelper = hibernateSessionHelper;
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

        Set<String> wcmZones = new HashSet<String>(2, 1.0f);
        wcmZones.add(AuthorityService.ZONE_APP_WCM);
        wcmZones.add(AuthorityService.ZONE_AUTH_ALFRESCO);

        Set<String> shareZones = new HashSet<String>(2, 1.0f);
        shareZones.add(AuthorityService.ZONE_APP_SHARE);
        shareZones.add(AuthorityService.ZONE_AUTH_ALFRESCO);

        List<AVMStoreDescriptor> stores = avmService.getStores();
        List<SiteInfo> sites = siteService.listSites(null, null);

        List<Action> groupActions = new ArrayList<Action>(stores.size() * 4 + sites.size() * 5 + 1);
        for (AVMStoreDescriptor store : stores)
        {
            groupActions.add(new Action("GROUP_"+store.getName()+"-ContentManager", wcmZones, ActionType.SET));
            groupActions.add(new Action("GROUP_"+store.getName()+"-ContentPublisher", wcmZones, ActionType.SET));
            groupActions.add(new Action("GROUP_"+store.getName()+"-ContentContributor", wcmZones, ActionType.SET));
            groupActions.add(new Action("GROUP_"+store.getName()+"-ContentReviewer", wcmZones, ActionType.SET));
        }
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
        hibernateSessionHelper.mark();
        Set<String> authorities = authorityService.getAllAuthorities(authorityType);
        hibernateSessionHelper.reset();
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
            hibernateSessionHelper.reset();
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
