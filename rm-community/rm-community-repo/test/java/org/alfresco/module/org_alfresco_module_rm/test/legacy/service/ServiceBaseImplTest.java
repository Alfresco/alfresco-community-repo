 
package org.alfresco.module.org_alfresco_module_rm.test.legacy.service;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestServiceImpl;
import org.alfresco.service.namespace.QName;

/**
 * Unit test for service base implementation.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class ServiceBaseImplTest extends BaseRMTestCase
{
    /** test service */
    private TestServiceImpl testService;
    
    /**
     * Init services
     */
    @Override
    protected void initServices()
    {
        super.initServices();
        
        testService = (TestServiceImpl)applicationContext.getBean("testService");
    }

    /**
     * test instanceOf()
     */
    public void testInstanceOf()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                assertTrue(testService.doInstanceOf(rmFolder, ContentModel.TYPE_FOLDER));
                assertTrue(testService.doInstanceOf(rmFolder, TYPE_RECORD_FOLDER));
                assertFalse(testService.doInstanceOf(rmFolder, TYPE_RECORD_CATEGORY));
                
                return null;
            }
        });
        
    }
    
    /**
     * test getNextCounter()
     */
    public void testGetNextCounter()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                assertNull(nodeService.getProperty(rmFolder, PROP_COUNT));
                assertEquals(1, testService.doGetNextCount(rmFolder));
                assertEquals(2, testService.doGetNextCount(rmFolder));
                assertEquals(3, testService.doGetNextCount(rmFolder));
                
                return null;
            }
        });
        
    }
    
    /**
     * test getTypeAndAspects()
     */
    public void testGetTypeAndAspects()
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                Set<QName> result = testService.doGetTypeAndApsects(rmFolder);
                assertTrue(result.contains(TYPE_RECORD_FOLDER));
        
                return null;
            }
        });        
    }

}
