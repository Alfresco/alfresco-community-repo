/*
 * Copyright (C) 2005-2012
 Alfresco Software Limited.
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
package org.alfresco.util.test.junitrules;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.rules.ExternalResource;

/**
 * A JUnit rule designed to help with the automatic cleanup of temporary st:site nodes.
 * 
 * @author Neil Mc Erlean
 * @since 4.0.3
 */
public class TemporarySites extends ExternalResource
{
    private static final Log log = LogFactory.getLog(TemporarySites.class);
    
    private final ApplicationContextInit appContextRule;
    private List<SiteInfo> temporarySites = new ArrayList<SiteInfo>();
    
    /**
     * Constructs the rule with a reference to a {@link ApplicationContextInit rule} which can be used to retrieve the ApplicationContext.
     * 
     * @param appContextRule a rule which can be used to retrieve the spring app context.
     */
    public TemporarySites(ApplicationContextInit appContextRule)
    {
        this.appContextRule = appContextRule;
    }
    
    
    @Override protected void before() throws Throwable
    {
        // Intentionally empty
    }
    
    @Override protected void after()
    {
        final RetryingTransactionHelper transactionHelper = (RetryingTransactionHelper) appContextRule.getApplicationContext().getBean("retryingTransactionHelper");
        final SiteService siteService = appContextRule.getApplicationContext().getBean("siteService", SiteService.class);
        
        // Run as admin to ensure all sites can be deleted irrespective of which user created them.
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    @Override public Void execute() throws Throwable
                    {
                        for (SiteInfo site : temporarySites)
                        {
                            final String shortName = site.getShortName();
                            if (siteService.getSite(shortName) != null)
                            {
                                log.debug("Deleting temporary site " + shortName);
                                siteService.deleteSite(shortName);
                            }
                        }
                        
                        return null;
                    }
                });
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }
    
    /**
     * Add a specified site to the list of SiteInfos to be deleted by this rule.
     * 
     * @param temporarySite a SiteInfo
     */
    public void addSite(SiteInfo temporarySite)
    {
        this.temporarySites.add(temporarySite);
    }
    
    /**
     * This method creates a Share Site and adds it to the internal list of NodeRefs to be tidied up by the rule.
     * This method will be run in its own transaction and will be run with the specified user as the fully authenticated user,
     * thus ensuring the named user is the creator of the new site.
     * 
     * @param sitePreset      the site preset
     * @param siteShortName   the short name of the new site
     * @param siteTitle       the title of the new site
     * @param siteDescription the description of the new site
     * @param visibility      the visibility
     * @param siteCreator     the username of the person who will create the site
     * @return the newly created SiteInfo (will be of type st:site).
     */
    public SiteInfo createSite(final String sitePreset, final String siteShortName, final String siteTitle, final String siteDescription,
                                             final SiteVisibility visibility, final String siteCreator)
    {
        return this.createSite(sitePreset, siteShortName, siteTitle, siteDescription, visibility, SiteModel.TYPE_SITE, siteCreator);
    }
    
    /**
     * This method creates a Share Site (<b>or subtype</b>) and adds it to the internal list of NodeRefs to be tidied up by the rule.
     * This method will be run in its own transaction and will be run with the specified user as the fully authenticated user,
     * thus ensuring the named user is the creator of the new site.
     * 
     * @param sitePreset      the site preset
     * @param siteShortName   the short name of the new site
     * @param siteTitle       the title of the new site
     * @param siteDescription the description of the new site
     * @param visibility      the visibility
     * @param node type       the node type of the site (must be st:site or subtype)
     * @param siteCreator     the username of the person who will create the site
     * @return the newly created SiteInfo.
     */
    public SiteInfo createSite(final String sitePreset, final String siteShortName, final String siteTitle, final String siteDescription,
                                             final SiteVisibility visibility, final QName siteType, final String siteCreator)
    {
        final RetryingTransactionHelper transactionHelper = appContextRule.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(siteCreator);
        
        SiteInfo newSite = transactionHelper.doInTransaction(new RetryingTransactionCallback<SiteInfo>()
        {
            public SiteInfo execute() throws Throwable
            {
                final SiteService siteService = appContextRule.getApplicationContext().getBean("siteService", SiteService.class);
                
                return siteService.createSite(sitePreset, siteShortName, siteTitle, siteDescription, visibility, siteType);
            }
        });
        
        AuthenticationUtil.popAuthentication();
        
        this.temporarySites.add(newSite);
        return newSite;
    }
}
