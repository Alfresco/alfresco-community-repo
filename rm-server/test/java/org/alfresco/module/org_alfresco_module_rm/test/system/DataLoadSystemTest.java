package org.alfresco.module.org_alfresco_module_rm.test.system;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
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
    
    /** config locations */
    protected String[] getConfigLocations()
    {
        return new String[]
        {
            "classpath:alfresco/application-context.xml",
            "classpath:test-context.xml"
        };
    }
    
    /** data sizing parameters */
    private static final int BATCH_SIZE = 100;
    private static final int ROOT_CATEGORY_COUNT = 1;   
    private static final int RECORD_FOLDER_COUNT = 1;
    private static final int RECORD_COUNT = 15000;
    
    /** application context */    
    private ApplicationContext applicationContext;
    
    private int totalCount;
    private List<NodeRef> recordCategories;
    private List<NodeRef> recordFolders;
    
    @Before
    public void before()
    {
        applicationContext = ApplicationContextHelper.getApplicationContext(getConfigLocations());
        filePlanService = (FilePlanService)applicationContext.getBean("FilePlanService");
        recordFolderService = (RecordFolderService)applicationContext.getBean("RecordFolderService");
        recordService = (RecordService)applicationContext.getBean("RecordService");
        transactionService = (TransactionService)applicationContext.getBean("transactionService");
    }
    
    @Test
    public void loadData()
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
    
    protected void repeatInTransactionBatches(final RunAsWork<Void> work, final int count) throws Exception
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
