 
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestService;


/**
 * System test for RM-452
 * 
 * See alfresco.extension.rm-method-security.properties
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RM452Test extends BaseRMTestCase 
{
    private TestService testService;
    
    @Override
    protected void initServices()
    {
        super.initServices();
        
        testService = (TestService)applicationContext.getBean("TestService");
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
   
    public void testRM452() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertNotNull(folder);
                assertNotNull(recordOne);
                assertFalse(filePlanService.isFilePlanComponent(folder));
                assertTrue(filePlanService.isFilePlanComponent(recordOne));
                
                // call methodOne with non-RM artifact .. expect success
                testService.testMethodOne(folder);
                        
                // call methodTwo with non-RM artifact .. expect success
                testService.testMethodTwo(folder);
                
                // call methodOne with an RM artifact .. expect success
                testService.testMethodOne(recordOne);                
                
                return null;
            }
        });
        
        doTestInTransaction(new FailureTest
        (
                "Shouldn't be able to call testMethodTwo on TestService, because override RM security for method is not configred.", 
                AlfrescoRuntimeException.class
        )
        {
            
            @Override
            public void run() throws Exception
            {
                // call methodTwo with an RM artifact .. expect failure
                testService.testMethodTwo(recordOne);
            }
        });
    }
}
