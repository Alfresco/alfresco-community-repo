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

package org.alfresco.module.org_alfresco_module_rm.test.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.GUID;

public class DataLoadSystemTest
{
    /** services */
    protected FilePlanService filePlanService;
    protected RecordFolderService recordFolderService;
    protected RecordService recordService;
    protected TransactionService transactionService;
    protected AuthorityService authorityService;
    protected MutableAuthenticationService authenticationService;
    protected PersonService personService;
    protected FilePlanRoleService filePlanRoleService;
    protected SiteService siteService;
    protected FileFolderService fileFolderService;

    /** config locations */
    protected String[] getConfigLocations()
    {
        return new String[]
        {
            "classpath:alfresco/application-context.xml",
            "classpath:test-context.xml"
        };
    }

    /** transaction batch size */
    private static final int BATCH_SIZE = 100;

    /** file plan sizing */
    private static final int ROOT_CATEGORY_COUNT    = 0;
    private static final int RECORD_FOLDER_COUNT    = 0;
    private static final int RECORD_COUNT           = 0;

    /** rm user sizing */
    private static final int RM_GROUP_COUNT         = 0;
    private static final int RM_USER_COUNT          = 0;

    /** inplace sizing */
    private static final int USER_COUNT             = 0;
    private static final int INPLACE_RECORD_COUNT   = 5000;

    /** application context */
    private ApplicationContext applicationContext;
    CommonRMTestUtils utils;

    private int totalCount;
    private List<NodeRef> recordCategories;
    private List<NodeRef> recordFolders;
    private List<String> groups;

    @Before
    public void before()
    {
        applicationContext = ApplicationContextHelper.getApplicationContext(getConfigLocations());
        utils = new CommonRMTestUtils(applicationContext);

        filePlanService = (FilePlanService)applicationContext.getBean("FilePlanService");
        recordFolderService = (RecordFolderService)applicationContext.getBean("RecordFolderService");
        recordService = (RecordService)applicationContext.getBean("RecordService");
        transactionService = (TransactionService)applicationContext.getBean("transactionService");
        authorityService = (AuthorityService)applicationContext.getBean("authorityService");
        authenticationService = (MutableAuthenticationService)applicationContext.getBean("AuthenticationService");
        personService = (PersonService)applicationContext.getBean("personService");
        filePlanRoleService = (FilePlanRoleService)applicationContext.getBean("filePlanRoleService");
        siteService = (SiteService)applicationContext.getBean("siteService");
        fileFolderService = (FileFolderService)applicationContext.getBean("fileFolderService");
    }

    @Test
    public void loadAllData()
    {
       loadFilePlanData();
       loadRMUsersAndGroups();
       loadInPlace();
    }

    private void loadInPlace()
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
           public Void doWork() throws Exception
           {
               final SiteInfo site = siteService.getSite("test");
               if (site == null)
               {
                   throw new AlfrescoRuntimeException("The collab site test is not present.");
               }

               final NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
               if (filePlan == null)
               {
                   Assert.fail("The default RM site is not present.");
               }

               // create users and add to site
               repeatInTransactionBatches(new RunAsWork<Void>()
               {
                  public Void doWork() throws Exception
                  {
                      // create user
                      String userName = GUID.generate();
                      System.out.println("Creating user " + userName);
                      createPerson(userName, true);

                      // add to collab site
                      siteService.setMembership("test", userName, SiteRole.SiteCollaborator.toString());

                      return null;
                  }
               }, USER_COUNT);

               // create content and declare as record
               repeatInTransactionBatches(new RunAsWork<Void>()
               {
                  public Void doWork() throws Exception
                  {
                      // create document
                      NodeRef docLib = siteService.getContainer(site.getShortName(), SiteService.DOCUMENT_LIBRARY);
                      NodeRef document = fileFolderService.create(docLib, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();

                      recordService.createRecord(filePlan, document);

                      return null;
                  }
               }, INPLACE_RECORD_COUNT);

               return null;
           }
        }, AuthenticationUtil.getAdminUserName());
    }


    private void loadRMUsersAndGroups()
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
           public Void doWork() throws Exception
           {
               final NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
               if (filePlan == null)
               {
                   Assert.fail("The default RM site is not present.");
               }

               groups = new ArrayList<>();

               repeatInTransactionBatches(new RunAsWork<Void>()
               {
                  public Void doWork() throws Exception
                  {
                      String groupName = GUID.generate();
                      System.out.println("Creating group " + groupName);
                      groups.add("GROUP_" + authorityService.createAuthority(AuthorityType.GROUP, groupName));
                      filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_RECORDS_MANAGER, groupName);
                      return null;
                  }
               }, RM_GROUP_COUNT);

               for (final String group : groups)
               {
                   repeatInTransactionBatches(new RunAsWork<Void>()
                   {
                      public Void doWork() throws Exception
                      {
                          String userName = GUID.generate();
                          System.out.println("Creating user " + userName + " and adding to group " + group);
                          createPerson(userName, true);
                          authorityService.addAuthority(group, userName);
                          return null;
                      }
                   }, RM_USER_COUNT);
               }

               return null;
           }
        });
    }

    private void loadFilePlanData()
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
           public Void doWork() throws Exception
           {
              final NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
              if (filePlan == null)
              {
                  Assert.fail("The default RM site is not present.");
              }

              // create root categories
              recordCategories = new ArrayList<>(ROOT_CATEGORY_COUNT);
              repeatInTransactionBatches(new RunAsWork<Void>()
              {
                 public Void doWork() throws Exception
                 {
                    recordCategories.add(filePlanService.createRecordCategory(filePlan, GUID.generate()));
                    return null;
                 }
              }, ROOT_CATEGORY_COUNT);

              // create record folders
              recordFolders = new ArrayList<>(RECORD_FOLDER_COUNT);
              for (final NodeRef recordCategory : recordCategories)
              {
                  repeatInTransactionBatches(new RunAsWork<Void>()
                  {
                     public Void doWork() throws Exception
                     {
                        recordFolders.add(recordFolderService.createRecordFolder(recordCategory, GUID.generate()));
                        return null;
                     }
                  }, RECORD_FOLDER_COUNT);
              }

              // create records
              for (final NodeRef recordFolder : recordFolders)
              {
                  repeatInTransactionBatches(new RunAsWork<Void>()
                  {
                     public Void doWork() throws Exception
                     {
                        recordService.createRecordFromContent(recordFolder, GUID.generate(), ContentModel.TYPE_CONTENT, null, null);
                        return null;
                     }
                  }, RECORD_COUNT);
              }


              return null;
           }
        });
    }

    private NodeRef createPerson(String userName, boolean createAuth)
    {
        if (createAuth)
        {
            authenticationService.createAuthentication(userName, "password".toCharArray());
        }
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        return personService.createPerson(properties);
    }

    private void repeatInTransactionBatches(final RunAsWork<Void> work, final int count) throws Exception
    {
        totalCount = 0;
        while (totalCount < count)
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    int batchSize = count - totalCount;
                    if (batchSize >= BATCH_SIZE)
                    {
                        batchSize = BATCH_SIZE;
                    }

                    for (int i = 0; i < batchSize; i++)
                    {
                        // do it
                        work.doWork();
                        totalCount++;
                    }

                    System.out.println("Created " + totalCount + " of " + count);

                    return null;
                }
            },
            false, true);
        }
    }
}
