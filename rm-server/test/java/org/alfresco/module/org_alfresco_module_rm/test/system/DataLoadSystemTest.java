package org.alfresco.module.org_alfresco_module_rm.test.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
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
    }
    
    @Test
    private void loadAllData()
    {
       loadFilePlanData(); 
       loadRMUsersAndGroups();
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
               
               groups = new ArrayList<String>();
               
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
              recordCategories = new ArrayList<NodeRef>(ROOT_CATEGORY_COUNT);
              repeatInTransactionBatches(new RunAsWork<Void>()
              {
                 public Void doWork() throws Exception
                 {
                    recordCategories.add(filePlanService.createRecordCategory(filePlan, GUID.generate()));
                    return null;
                 }
              }, ROOT_CATEGORY_COUNT);
              
              // create record folders
              recordFolders = new ArrayList<NodeRef>(RECORD_FOLDER_COUNT);
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
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
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
