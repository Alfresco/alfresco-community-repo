/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

package org.alfresco.repo.invitation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * Test class which ensures that workflows created by {@link InvitationService} are correctly cleaned up when no longer needed.
 * 
 * @author Neil Mc Erlean
 * @since 4.2
 */
public class InvitationCleanupTest
{
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    // Rules to create 2 test users.
    public static AlfrescoPerson TEST_USER1 = new AlfrescoPerson(APP_CONTEXT_INIT);
    public static AlfrescoPerson TEST_USER2 = new AlfrescoPerson(APP_CONTEXT_INIT);
    
    // Rule to manage test sites
    public static TemporarySites TEST_SITES = new TemporarySites(APP_CONTEXT_INIT);
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain ruleChain = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                            .around(TEST_USER1)
                                                            .around(TEST_USER2)
                                                            .around(TEST_SITES);
    
    // Various services
    private static InvitationService         INVITATION_SERVICE;
    private static SiteService               SITE_SERVICE;
    private static RetryingTransactionHelper TRANSACTION_HELPER;
    private static WorkflowService           WORKFLOW_SERVICE;
    private static NodeArchiveService        NODE_ARCHIVE_SERVICE;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        INVITATION_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("InvitationService", InvitationService.class);
        SITE_SERVICE       = APP_CONTEXT_INIT.getApplicationContext().getBean("SiteService", SiteService.class);
        TRANSACTION_HELPER = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        WORKFLOW_SERVICE   = APP_CONTEXT_INIT.getApplicationContext().getBean("WorkflowService", WorkflowService.class);
        NODE_ARCHIVE_SERVICE = APP_CONTEXT_INIT.getApplicationContext().getBean("nodeArchiveService", NodeArchiveService.class);
    }
    
    /** See CLOUD-1824 for details on bug &amp; ALF-11872 for details on a related, older bug. */
    @Test public void pendingJoinRequestsToModeratedSitesShouldDisappearOnSiteDeletion() throws Exception
    {
        // First, UserOne creates a site - we'll name it after this test method.
        final String siteShortName = InvitationCleanupTest.class.getSimpleName() + "-test-site";
        
        TEST_SITES.createSite("sitePreset", siteShortName, "title", "description", SiteVisibility.MODERATED, TEST_USER1.getUsername());
        
        // Now UserTwo makes a request to join the site
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                return TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        INVITATION_SERVICE.inviteModerated("Let me in!",
                                                           TEST_USER2.getUsername(),
                                                           ResourceType.WEB_SITE,
                                                           siteShortName,
                                                           SiteModel.SITE_CONTRIBUTOR);
                        return null;
                    }
                });
            }
        }, TEST_USER2.getUsername());
        
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override public Void doWork() throws Exception
            {
                TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // Sanity check: UserOne should now have a task assigned to them
                        assertUserHasTasks(TEST_USER1.getUsername(), 1);
                        
                        return null;
                    }
                });
                
                NodeRef archivedNodeRef = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        SiteInfo siteInfo = SITE_SERVICE.getSite(siteShortName);
                        // UserOne ignores the task and instead deletes the site.
                        SITE_SERVICE.deleteSite(siteShortName);
                        
                        return NODE_ARCHIVE_SERVICE.getArchivedNode(siteInfo.getNodeRef());
                    }
                });
                
                // Purge deleted site from trashcan so that sitename can be reused
                NODE_ARCHIVE_SERVICE.purgeArchivedNode(archivedNodeRef);
                
                // The pending inviations are deleted asynchronously and so we must wait for that deletion to complete.
                // TODO Obviously Thread.sleep is not the best way to do this.
                Thread.sleep(1000);
                
                TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // UserOne now creates a new site WITH THE SAME NAME.
                        TEST_SITES.createSite("sitePreset", siteShortName, "title", "description", SiteVisibility.MODERATED, TEST_USER1.getUsername());
                        
                        return null;
                    }
                });
                
                TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // Now the task should remain unseen/deleted.
                        assertUserHasTasks(TEST_USER1.getUsername(), 0);
                        
                        return null;
                    }
                });
                
                return null;
            }
        }, TEST_USER1.getUsername());
    }
    
    private List<WorkflowTask> assertUserHasTasks(final String username, final int number)
    {
        List<WorkflowTask> tasks = WORKFLOW_SERVICE.getAssignedTasks(username, WorkflowTaskState.IN_PROGRESS);
        
        // Need mutable collection.
        List<WorkflowTask> allTasks = new ArrayList<WorkflowTask>();
        allTasks.addAll(tasks);
        allTasks.addAll(WORKFLOW_SERVICE.getPooledTasks(username));
        
        assertEquals("Wrong number of tasks assigned to user", number, allTasks.size());
        return tasks;
    }
}
