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
package org.alfresco.util.test.junitrules;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.GUID;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;

/**
 * A JUnit rule designed to help with the automatic cleanup of temporary st:site nodes.
 * 
 * @author Neil Mc Erlean
 * @since 4.0.3
 */
public class TemporarySites extends AbstractPersonRule
{
    private static final Log log = LogFactory.getLog(TemporarySites.class);

    private List<SiteInfo> temporarySites = new ArrayList<SiteInfo>();
    private List<String> temporarySiteUsers = new ArrayList<String>();

    /**
     * Constructs the rule with a reference to a {@link ApplicationContextInit rule} which can be used to retrieve the ApplicationContext.
     * 
     * @param appContextRule
     *            a rule which can be used to retrieve the spring app context.
     */
    public TemporarySites(ApplicationContextInit appContextRule)
    {
        super(appContextRule);
    }

    @Override
    protected void before() throws Throwable
    {
        // Intentionally empty
    }

    @Override
    protected void after()
    {
        final RetryingTransactionHelper transactionHelper = (RetryingTransactionHelper) appContextRule.getApplicationContext().getBean("retryingTransactionHelper");
        final SiteService siteService = appContextRule.getApplicationContext().getBean("siteService", SiteService.class);
        final ActivityPostDAO postDAO = appContextRule.getApplicationContext().getBean("postDAO", ActivityPostDAO.class);
        final NodeArchiveService nodeArchiveService = (NodeArchiveService) appContextRule.getApplicationContext().getBean("nodeArchiveService");

        // Run as admin to ensure all sites can be deleted irrespective of which user created them.
        AuthenticationUtil.runAs(new RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception
            {
                transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                    @Override
                    public Void execute() throws Throwable
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

                        for (String username : temporarySiteUsers)
                        {
                            log.debug("Deleting temporary site user " + username);
                            deletePerson(username);
                        }

                        // Clean all the post feeds
                        int deletedCnt = 0;
                        Date keepDate = new Date(System.currentTimeMillis() + (120 * 1000L));
                        for (ActivityPostEntity.STATUS status : ActivityPostEntity.STATUS.values())
                        {
                            deletedCnt += postDAO.deletePosts(keepDate, status);
                        }
                        log.debug("Deleted " + deletedCnt + " post feeds.");

                        return null;
                    }
                });
                transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
                    @Override
                    public Void execute() throws Throwable
                    {
                        for (SiteInfo site : temporarySites)
                        {
                            log.debug("Purging temporary site from trashcan: " + site.getShortName());
                            nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(site.getNodeRef()));
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
     * @param temporarySite
     *            a SiteInfo
     */
    public void addSite(SiteInfo temporarySite)
    {
        this.temporarySites.add(temporarySite);
    }

    /**
     * This method creates a Share Site and adds it to the internal list of NodeRefs to be tidied up by the rule. This method will be run in its own transaction and will be run with the specified user as the fully authenticated user, thus ensuring the named user is the creator of the new site.
     * 
     * @param sitePreset
     *            the site preset
     * @param siteShortName
     *            the short name of the new site
     * @param siteTitle
     *            the title of the new site
     * @param siteDescription
     *            the description of the new site
     * @param visibility
     *            the visibility
     * @param siteCreator
     *            the username of the person who will create the site
     * @return the newly created SiteInfo (will be of type st:site).
     */
    public SiteInfo createSite(final String sitePreset, final String siteShortName, final String siteTitle, final String siteDescription,
            final SiteVisibility visibility, final String siteCreator)
    {
        return this.createSite(sitePreset, siteShortName, siteTitle, siteDescription, visibility, SiteModel.TYPE_SITE, siteCreator);
    }

    /**
     * This method creates a Share Site (<b>or subtype</b>) and adds it to the internal list of NodeRefs to be tidied up by the rule. This method will be run in its own transaction and will be run with the specified user as the fully authenticated user, thus ensuring the named user is the creator of the new site.
     * 
     * @param sitePreset
     *            the site preset
     * @param siteShortName
     *            the short name of the new site
     * @param siteTitle
     *            the title of the new site
     * @param siteDescription
     *            the description of the new site
     * @param visibility
     *            the visibility
     * @param siteType
     *            the site type (must be st:site or subtype)
     * @param siteCreator
     *            the username of the person who will create the site
     * @return the newly created SiteInfo.
     */
    public SiteInfo createSite(final String sitePreset, final String siteShortName, final String siteTitle, final String siteDescription,
            final SiteVisibility visibility, final QName siteType, final String siteCreator)
    {
        final RetryingTransactionHelper transactionHelper = appContextRule.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(siteCreator);

        SiteInfo newSite = transactionHelper.doInTransaction(new RetryingTransactionCallback<SiteInfo>() {
            public SiteInfo execute() throws Throwable
            {
                final SiteService siteService = appContextRule.getApplicationContext().getBean("siteService", SiteService.class);

                SiteInfo newSite = siteService.createSite(sitePreset, siteShortName, siteTitle, siteDescription, visibility, siteType);

                // ensure that the Document Library folder is pre-created so that test code can start creating content straight away.
                // At the time of writing HEAD does not create this folder automatically, but Thor does.
                // So to be safe, I'll pre-check if the node is there.
                NodeRef docLibFolder = siteService.getContainer(siteShortName, SiteService.DOCUMENT_LIBRARY);
                if (docLibFolder == null)
                {
                    docLibFolder = siteService.createContainer(siteShortName, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
                }

                return newSite;
            }
        });

        AuthenticationUtil.popAuthentication();

        this.temporarySites.add(newSite);
        return newSite;
    }

    /**
     * This method creates a test site (of Alfresco type <code>st:site</code>) and one user for each of the Share Site Roles. This method will be run in its own transaction and will be run with the specified user as the fully authenticated user, thus ensuring the named user is the creator of the new site. The site and its users will be deleted automatically by the rule.
     * 
     * @param sitePreset
     *            the site preset.
     * @param visibility
     *            the Site visibility.
     * @param siteCreator
     *            the username of a user who will be used to create the site (user must exist of course).
     * @return the {@link SiteInfo} object for the newly created site.
     */
    public TestSiteAndMemberInfo createTestSiteWithUserPerRole(final String siteShortName, String sitePreset, SiteVisibility visibility, String siteCreator)
    {
        // create the site
        SiteInfo result = this.createSite(sitePreset, siteShortName, null, null, visibility, siteCreator);

        // create the users
        final RetryingTransactionHelper transactionHelper = appContextRule.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        final SiteService siteService = appContextRule.getApplicationContext().getBean("siteService", SiteService.class);

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(siteCreator);

        // Create users for this test site that cover the various roles.
        List<String> userNames = transactionHelper.doInTransaction(new RetryingTransactionCallback<List<String>>() {
            public List<String> execute() throws Throwable
            {
                List<String> users = new ArrayList<String>(4);

                for (String shareRole : SiteModel.STANDARD_PERMISSIONS)
                {
                    final String userName = siteShortName + "_" + shareRole + "_" + GUID.generate();

                    log.debug("Creating temporary site user " + userName);

                    createPerson(userName);
                    siteService.setMembership(siteShortName, userName, shareRole);
                    users.add(userName);

                    temporarySiteUsers.add(userName);
                }

                return users;
            }
        });

        NodeRef doclibFolder = transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>() {
            public NodeRef execute() throws Throwable
            {
                return siteService.getContainer(siteShortName, SiteService.DOCUMENT_LIBRARY);
            }
        });

        AuthenticationUtil.popAuthentication();

        return new TestSiteAndMemberInfo(result, doclibFolder, userNames.get(0),
                userNames.get(1),
                userNames.get(2),
                userNames.get(3));
    }

    /* This method creates a test site with n groups. */
    public List<String> createTestSiteWithGroups(final String siteShortName, String sitePreset, SiteVisibility visibility, String siteCreator, int noOfGroups)
    {
        // create the site
        this.createSite(sitePreset, siteShortName, null, null, visibility, siteCreator);

        // create the users
        final RetryingTransactionHelper transactionHelper = appContextRule.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        final SiteService siteService = appContextRule.getApplicationContext().getBean("siteService", SiteService.class);
        final AuthorityService authorityService = appContextRule.getApplicationContext().getBean("authorityService", AuthorityService.class);
        final PersonService personService = appContextRule.getApplicationContext().getBean("personService", PersonService.class);

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(siteCreator);

        // Create group with one user and add it to the site
        List<String> groupIds = transactionHelper.doInTransaction(new RetryingTransactionCallback<List<String>>() {
            public List<String> execute() throws Throwable
            {
                List<String> groups = new ArrayList<String>(noOfGroups);

                for (int i = 0; i < noOfGroups; i++)
                {
                    String shareRole = SiteModel.STANDARD_PERMISSIONS.get(i % 4);
                    final String groupName = "Test_Group_" + GUID.generate();
                    final String userName = "Test_User_" + GUID.generate();

                    log.debug("Creating temporary site user " + userName);

                    final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
                    properties.put(ContentModel.PROP_USERNAME, userName);
                    properties.put(ContentModel.PROP_FIRSTNAME, GUID.generate());
                    properties.put(ContentModel.PROP_LASTNAME, GUID.generate());
                    properties.put(ContentModel.PROP_EMAIL, GUID.generate() + "@test.com");
                    personService.createPerson(properties);

                    log.debug("Creating temporary site group " + groupName);
                    final String groupId = authorityService.createAuthority(AuthorityType.GROUP, groupName);
                    authorityService.setAuthorityDisplayName(groupId, GUID.generate());
                    authorityService.addAuthority(groupId, userName);
                    siteService.setMembership(siteShortName, groupId, shareRole);
                    groups.add(userName);
                }

                return groups;
            }
        });

        return groupIds;
    }

    /**
     * A simple POJO class to store the {@link SiteInfo} for this site and its initial, automatically created members' usernames.
     * 
     * @author Neil Mc Erlean
     */
    public static class TestSiteAndMemberInfo
    {
        public final SiteInfo siteInfo;
        public final NodeRef doclib;
        public final String siteManager;
        public final String siteCollaborator;
        public final String siteContributor;
        public final String siteConsumer;

        public TestSiteAndMemberInfo(SiteInfo siteInfo, NodeRef siteDocLib, String siteManager, String siteCollaborator, String siteContributor, String siteConsumer)
        {
            this.siteInfo = siteInfo;
            this.doclib = siteDocLib;
            this.siteManager = siteManager;
            this.siteCollaborator = siteCollaborator;
            this.siteContributor = siteContributor;
            this.siteConsumer = siteConsumer;
        }
    }
}
