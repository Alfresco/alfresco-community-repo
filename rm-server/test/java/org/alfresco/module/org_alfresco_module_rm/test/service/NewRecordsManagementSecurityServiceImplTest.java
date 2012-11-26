
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Records management security service test.
 * 
 * @author Roy Wetherall
 */
public class NewRecordsManagementSecurityServiceImplTest extends BaseRMTestCase
{
    private NodeRef record;
    private NodeRef recordToo;
    
    @Override
    protected boolean isUserTest()
    {
        return true;
    }
    
    @Override
    protected void setupTestDataImpl()
    {
        super.setupTestDataImpl();
        
        record = utils.createRecord(rmFolder, "record.txt");
        recordToo = utils.createRecord(rmFolder, "recordToo.txt");    
    }

    
    // TODO testGetProtectedAspects
    
    // TODO getProtectedProperties
    
    // TODO bootstrapDefaultRoles
    
    // TODO getRoles
    
    // TODO getRolesByUser
    
    // TODO getRole    
    
    // TODO existsRole
    
    // TODO hasRMAdminRole
    
    // TODO createRole
    
    // TODO updateRole
    
    // TODO deleteRole
    
    // TODO assignRoleToAuthority
    
    // TODO setPermission
    
    // TODO deletePermission
    
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
                
                assertNull(securityService.getExtendedReaders(record));
                
                Set<String> extendedReaders = new HashSet<String>(2);
                extendedReaders.add("monkey");
                extendedReaders.add("elephant");
                
                securityService.setExtendedReaders(record, extendedReaders);
                
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
                
                securityService.setExtendedReaders(recordToo, extendedReadersToo);
                
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
                
                return null;
            }
            
            private boolean hasExtendedReadersAspect(NodeRef nodeRef)
            {
                return nodeService.hasAspect(nodeRef, ASPECT_EXTENDED_READERS);
            }
            
            private void test(NodeRef nodeRef, Map<String, Integer> testMap)
            {
                assertTrue(hasExtendedReadersAspect(nodeRef));
                
                Map<String, Integer> readersMap = (Map<String,Integer>)nodeService.getProperty(nodeRef, PROP_READERS);
                assertNotNull(readersMap);
                assertEquals(testMap.size(), readersMap.size());
                
                for (Map.Entry<String, Integer> entry: testMap.entrySet())
                {
                    assertTrue(readersMap.containsKey(entry.getKey()));
                    assertEquals(entry.getValue(), readersMap.get(entry.getKey()));
                    
                }
                
                Set<String> readers = securityService.getExtendedReaders(nodeRef);
                assertNotNull(readers);
                assertEquals(testMap.size(), readers.size());
            }
        });
    }
    
    // TODO getExtendedReaders
    
    // TODO setExtendedReaders
    
    // TODO removeExtendedReaders
    
    // TODO removeAllExtendedReaders        
}
