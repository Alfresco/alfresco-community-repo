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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.AVMZipBootstrap;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.view.ImporterBinding.UUID_BINDING;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A Patch based importer which creates and populates
 *  a site based on the supplied data
 * 
 * @author Nick Burch
 */
public class SiteLoadPatch extends AbstractPatch
{
    public static final String PROPERTIES_USERS = "users";
    public static final String PROPERTIES_PEOPLE = "people";
    public static final String PROPERTIES_GROUPS = "groups";
    public static final String PROPERTIES_CONTENTS = "contents";
    public static final String PROPERTIES_AVM = "avm";
    
    private static final Map<String,String> DEFAULT_PATHS = new HashMap<String, String>();
    static {
        DEFAULT_PATHS.put(PROPERTIES_AVM, null);
        DEFAULT_PATHS.put(PROPERTIES_USERS, "/${alfresco_user_store.system_container.childname}/${alfresco_user_store.user_container.childname}"); 
        DEFAULT_PATHS.put(PROPERTIES_PEOPLE, "/${system.system_container.childname}/${system.people_container.childname}"); 
        DEFAULT_PATHS.put(PROPERTIES_GROUPS, null);
        DEFAULT_PATHS.put(PROPERTIES_CONTENTS, "/${spaces.company_home.childname}/${spaces.sites.childname}"); 
    }
    
    private static final String MSG_SITE_ALREADY_EXISTS = "patch.siteLoadPatch.exists";
    private static final String MSG_NO_BOOTSTRAP_VIEWS_GIVEN = "patch.siteLoadPatch.noBootstrapViews";
    private static final String MSG_SITE_CREATED = "patch.siteLoadPatch.result";
    
    // Logger
    private static final Log logger = LogFactory.getLog(SiteLoadPatch.class);

    private AuthorityService authorityService;
    private BehaviourFilter behaviourFilter;
    private SiteService siteService;
    
    private String siteName;
    
    private ImporterBootstrap spacesBootstrap;
    private ImporterBootstrap usersBootstrap;
    private AVMZipBootstrap avmBootstrap;
    
    private Map<String,Properties> bootstrapViews;
    
    public SiteLoadPatch()
    {
        // We do need to run in our own transaction
        setRequiresTransaction(true);
    }
        
    /**
     * Sets the name of the site to be bootstrapped.
     * 
     * @param siteName The short name of the site
     */
    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }

    public void setSpacesBootstrap(ImporterBootstrap spacesBootstrap)
    {
        this.spacesBootstrap = spacesBootstrap;
    }
    public void setUsersBootstrap(ImporterBootstrap usersBootstrap)
    {
        this.usersBootstrap = usersBootstrap;
    }
    public void setAvmBootstrap(AVMZipBootstrap avmBootstrap)
    {
        this.avmBootstrap = avmBootstrap;
    }

    /**
     * Sets the details of the bootstraps to perform
     */
    public void setBootstrapViews(Map<String,Properties> bootstrapViews)
    {
        this.bootstrapViews = bootstrapViews;
    }

    /**
     * Sets the Site Service to be used for importing into
     * 
     * @param siteService The Site Service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    /**
     * Sets the Authority Service to be used for groups and people
     * 
     * @param authorityService The Authority Service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        PropertyCheck.mandatory(this, "siteService", siteService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "siteName", siteName);
    }

    @Override
    protected String applyInternal() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        try
        {
            // The site service is funny about permissions,
            //  so even though we're running as the system we
            //  still need to identify us as the admin user
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
            
            return applyInternalImpl();
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * Load the site in.<br/>
     * Site will be loaded as admin user.
     */
    private String applyInternalImpl() throws Exception
    {
        if (bootstrapViews == null || bootstrapViews.size() == 0)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No Bootstraps given to import from - bootstrap import ignored");
            }
            return I18NUtil.getMessage(MSG_NO_BOOTSTRAP_VIEWS_GIVEN, siteName);
        }
        
        // Is the site already there?
        // (Run now as we need DB + Security Context)
        if (siteService.getSite(siteName) != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Site " + siteName + " already exists - bootstrap import ignored");
            }
            return I18NUtil.getMessage(MSG_SITE_ALREADY_EXISTS, siteName);
        }
        
        // If we get here, we're good to go!
        if(logger.isDebugEnabled())
        {
            logger.debug("Performing bootstrap of site " + siteName);
        }

        
        // Create the site as the admin user
        SiteInfo site = siteService.createSite(
                siteName, siteName, siteName, 
                null, SiteVisibility.PUBLIC
        );
        
        // At this point we can go back to being the system
        AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
        
        // Setup the Importer Bootstrap Beans
        for(ImporterBootstrap bootstrap : new ImporterBootstrap[] { spacesBootstrap, usersBootstrap })
        {
            bootstrap.setAllowWrite(true);
            bootstrap.setUseExistingStore(true);
            bootstrap.setUuidBinding(UUID_BINDING.REPLACE_EXISTING);
        }
        
        // Normally paths aren't given for the views, so supply
        //  the defaults where they weren't
        for(String type : DEFAULT_PATHS.keySet())
        {
            Properties props = bootstrapViews.get(type);
            if(props != null && DEFAULT_PATHS.get(type) != null)
            {
                if(! props.containsKey("path"))
                {
                    props.setProperty("path", DEFAULT_PATHS.get(type));
                }
            }
        }
        
        // Load our various bootstraps, in the required order
        //  for things to come in correctly
        
        // Load any users requested
        if(bootstrapViews.containsKey(PROPERTIES_USERS))
        {
            List<Properties> views = new ArrayList<Properties>(1);
            views.add(bootstrapViews.get(PROPERTIES_USERS));
            usersBootstrap.setBootstrapViews(views);
            usersBootstrap.bootstrap();
        }
        
        // Load any people requested
        if(bootstrapViews.containsKey(PROPERTIES_PEOPLE))
        {
            List<Properties> views = new ArrayList<Properties>(1);
            views.add(bootstrapViews.get(PROPERTIES_PEOPLE));
            spacesBootstrap.setBootstrapViews(views);
            spacesBootstrap.bootstrap();
        }
        
        // Put people into groups
        if(bootstrapViews.containsKey(PROPERTIES_GROUPS))
        {
            try
            {
                doGroupImport(
                      bootstrapViews.get(PROPERTIES_GROUPS).getProperty("location")
                );
            } 
            catch(Throwable t)
            {
                throw new AlfrescoRuntimeException("Bootstrap failed", t);
            }
        }
        
        // Load the AVM contents
        if(bootstrapViews.containsKey(PROPERTIES_AVM))
        {
            avmBootstrap.setLocation(
                  bootstrapViews.get(PROPERTIES_AVM).getProperty("location")
            );
            avmBootstrap.bootstrap();
        }
        
        // Load the Main (ACP) Contents
        if(bootstrapViews.containsKey(PROPERTIES_CONTENTS))
        {
            // Disable the behaviour which prevents site deletion.
            behaviourFilter.disableBehaviour(site.getNodeRef(), ContentModel.ASPECT_UNDELETABLE);
            try
            {
                // Clear up the stub content that createSite gave us, first
                // apply the temporary aspect though to prevent the node from
                // being archived
                nodeService.addAspect(site.getNodeRef(), ContentModel.ASPECT_TEMPORARY, null);
                nodeService.deleteNode(site.getNodeRef());
            }
            finally
            {
                behaviourFilter.enableBehaviour(site.getNodeRef(), ContentModel.ASPECT_UNDELETABLE);
            }
            
            
            // Now load in the real content from the ACP
            List<Properties> views = new ArrayList<Properties>(1);
            views.add(bootstrapViews.get(PROPERTIES_CONTENTS));
            spacesBootstrap.setBootstrapViews(views);
            spacesBootstrap.bootstrap();
        }
        
        return I18NUtil.getMessage(MSG_SITE_CREATED, siteName);
    }
    
    /**
     * Note - This Could potentially be split out into another Bootstrap class,
     *  but for now is inline to keep things simple
     */
    private void doGroupImport(String location) throws Throwable
    {
        File groupFile = ImporterBootstrap.getFile(location);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(groupFile), "UTF-8")
        );
        
        String line;
        while( (line = reader.readLine()) != null )
        {
            int splitAt = line.indexOf('=');
            if(splitAt == -1)
            {
                logger.warn("Invalid group line " + line);
                continue;
            }
            
            String user = line.substring(0, splitAt);
            Set<String> currentGroups = authorityService.getAuthoritiesForUser(user); 
            
            StringTokenizer groups = new StringTokenizer(line.substring(splitAt+1), ",");
            while(groups.hasMoreTokens())
            {
                String group = groups.nextToken();
                if(! currentGroups.contains(group))
                {
                    authorityService.addAuthority(group, user);
                    
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Added user " + user + " to group " + group);
                    }
                }
            }
        }
        reader.close();
    }
}
