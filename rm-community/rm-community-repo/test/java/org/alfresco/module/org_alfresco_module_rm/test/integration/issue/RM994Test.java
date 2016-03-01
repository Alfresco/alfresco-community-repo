 
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;


/**
 * System test for RM-994
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RM994Test extends BaseRMTestCase 
{    
    @Override
    protected void initServices()
    {
        super.initServices();
    }
    
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }
    
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }
   
    public void testRM944() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                checkVitalRecordNotSet(rmContainer);
                checkVitalRecordNotSet(rmFolder);
                checkVitalRecordNotSet(recordOne);  
                assertNull(nodeService.getProperty(recordOne, PROP_REVIEW_AS_OF));
                
                vitalRecordService.setVitalRecordDefintion(rmContainer, true, new Period("month|1"));        
                
                return null;
            }
        });
        
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {                
                checkVitalRecordSet(rmContainer);
                checkVitalRecordSet(rmFolder);
                checkVitalRecordSet(recordOne);                
                assertNotNull(nodeService.getProperty(recordOne, PROP_REVIEW_AS_OF));
                
                recordService.createRecord(filePlan, dmDocument, true);
                
                assertTrue(recordService.isRecord(dmDocument));
                checkVitalRecordNotSet(dmDocument);
                
                fileFolderService.move(dmDocument, rmFolder, null);
                
                checkVitalRecordSet(dmDocument); 
                
                return null;
            }
        }, "admin");
        
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {    
                checkVitalRecordSet(dmDocument); 
                
                return null;
            }
        });

    }
    
    private void checkVitalRecordSet(NodeRef nodeRef)
    {
        VitalRecordDefinition def = vitalRecordService.getVitalRecordDefinition(nodeRef);
        assertNotNull(def);
        assertTrue(def.isEnabled());
        assertEquals("month", def.getReviewPeriod().getPeriodType());
        assertEquals("1", def.getReviewPeriod().getExpression());
    }
    
    private void checkVitalRecordNotSet(NodeRef nodeRef)
    {
        VitalRecordDefinition recordDef = vitalRecordService.getVitalRecordDefinition(nodeRef);
        if (recordDef != null)
        {
            assertFalse(recordDef.isEnabled());
            assertEquals("none", recordDef.getReviewPeriod().getPeriodType());              
            assertNull(recordDef.getNextReviewDate());
        }
    }
}
