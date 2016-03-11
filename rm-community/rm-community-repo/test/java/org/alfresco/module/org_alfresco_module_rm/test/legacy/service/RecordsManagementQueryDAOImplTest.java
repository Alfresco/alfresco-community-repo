package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * Records Management Query DAO
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordsManagementQueryDAOImplTest extends BaseRMTestCase implements RecordsManagementModel
{
    protected RecordsManagementQueryDAO queryDAO;
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
     */
    @Override
    protected void initServices()
    {
        super.initServices();

        queryDAO = (RecordsManagementQueryDAO)applicationContext.getBean("recordsManagementQueryDAO");
    }

    /**
     * This is a record test
     * 
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isRecordTest()
     */
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    /**
     * @see RecordService#getRecordMetaDataAspects()
     */
    public void testGetRecordMetaDataAspects() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                int count = queryDAO.getCountRmaIdentifier("abc-123");
                assertEquals(0, count);
                
                String existingID = (String)nodeService.getProperty(recordOne, PROP_IDENTIFIER);
                count = queryDAO.getCountRmaIdentifier(existingID);
                assertEquals(1, count);    
                
                return null;
            }
        });
    }

  
}
