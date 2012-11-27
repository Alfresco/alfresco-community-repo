
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Records management security service test.
 * 
 * @author Roy Wetherall
 */
public class ExtendedSecurityServiceImplTest extends BaseRMTestCase
{
    private ExtendedSecurityService extendedSecurityService;
    
    private NodeRef record;
    private NodeRef recordToo;
    
    @Override
    protected boolean isUserTest()
    {
        return true;
    }
    
    @Override
    protected void initServices()
    {
        super.initServices();
        
        extendedSecurityService = (ExtendedSecurityService)applicationContext.getBean("ExtendedSecurityService");
    }
    
    @Override
    protected void setupTestDataImpl()
    {
        super.setupTestDataImpl();
        
        record = utils.createRecord(rmFolder, "record.txt");
        recordToo = utils.createRecord(rmFolder, "recordToo.txt");    
    }
    
    public void testExtendedReaders()
    {
        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                assertFalse(hasExtendedReadersAspect(filePlan));
                assertFalse(hasExtendedReadersAspect(rmContainer));
                assertFalse(hasExtendedReadersAspect(rmFolder));
                assertFalse(hasExtendedReadersAspect(record));
                
                assertNull(extendedSecurityService.getExtendedReaders(record));
                
                Set<String> extendedReaders = new HashSet<String>(2);
                extendedReaders.add("monkey");
                extendedReaders.add("elephant");
                
                extendedSecurityService.setExtendedReaders(record, extendedReaders);
                
                Map<String, Integer> testMap = new HashMap<String, Integer>(2);
                testMap.put("monkey", Integer.valueOf(1));
                testMap.put("elephant", Integer.valueOf(1));
                
                test(filePlan, testMap);
                test(rmContainer, testMap);
                test(rmFolder, testMap);
                test(record, testMap);
                
                Set<String> extendedReadersToo = new HashSet<String>(2);
                extendedReadersToo.add("monkey");
                extendedReadersToo.add("snake");
                
                extendedSecurityService.setExtendedReaders(recordToo, extendedReadersToo);
                
                Map<String, Integer> testMapToo = new HashMap<String, Integer>(2);
                testMapToo.put("monkey", Integer.valueOf(1));
                testMapToo.put("snake", Integer.valueOf(1));
                
                Map<String, Integer> testMapThree = new HashMap<String, Integer>(3);
                testMapThree.put("monkey", Integer.valueOf(2));
                testMapThree.put("elephant", Integer.valueOf(1));
                testMapThree.put("snake", Integer.valueOf(1));
                
                test(filePlan, testMapThree);
                test(rmContainer, testMapThree);
                test(rmFolder, testMapThree);
                test(recordToo, testMapToo);
                
                // test remove (with no parent inheritance)
                
                Set<String> removeMap1 = new HashSet<String>(2);
                removeMap1.add("elephant");
                removeMap1.add("monkey");
                
                extendedSecurityService.removeExtendedReaders(rmFolder, removeMap1, false);
                
                Map<String, Integer> testMapFour = new HashMap<String, Integer>(2);
                testMapFour.put("monkey", Integer.valueOf(1));
                testMapFour.put("snake", Integer.valueOf(1));
                
                test(filePlan, testMapThree);
                test(rmContainer, testMapThree);
                test(rmFolder, testMapFour);
                test(recordToo, testMapToo);
                
                // test remove (apply to parents)
                
                Set<String> removeMap2 = new HashSet<String>(1);
                removeMap2.add("snake");
                
                extendedSecurityService.removeExtendedReaders(recordToo, removeMap2, true);
                
                testMapThree.remove("snake");
                testMapFour.remove("snake");
                testMapToo.remove("snake");
                
                test(filePlan, testMapThree);
                test(rmContainer, testMapThree);
                test(rmFolder, testMapFour);
                test(recordToo, testMapToo);
                
                return null;
            }
            
            private boolean hasExtendedReadersAspect(NodeRef nodeRef)
            {
                return nodeService.hasAspect(nodeRef, ASPECT_EXTENDED_READERS);
            }
            
            @SuppressWarnings("unchecked")
            private void test(NodeRef nodeRef, Map<String, Integer> testMap)
            {
                assertTrue(hasExtendedReadersAspect(nodeRef));
                
                Map<String, Integer> readersMap = (Map<String,Integer>)nodeService.getProperty(nodeRef, PROP_READERS);
                assertNotNull(readersMap);
                assertEquals(testMap.size(), readersMap.size());
                
                for (Map.Entry<String, Integer> entry: testMap.entrySet())
                {
                    assertTrue(readersMap.containsKey(entry.getKey()));
                    assertEquals(entry.getKey(), entry.getValue(), readersMap.get(entry.getKey()));
                    
                }
                
                Set<String> readers = extendedSecurityService.getExtendedReaders(nodeRef);
                assertNotNull(readers);
                assertEquals(testMap.size(), readers.size());
            }
        });
    }     
}
