package org.alfresco.module.org_alfresco_module_rm.record;

import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Unit test for RecordMetadataBootstrap
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class RecordMetadataBootstrapUnitTest extends BaseUnitTest
{
    @InjectMocks private RecordMetadataBootstrap bootstrap;
    
    /**
     * Test init method to ensure set map will register correctly with record service.
     */
    @Test
    public void testInit()
    {
        // create and set map
        Map<String, String> map = new HashMap<String, String>(2);
        map.put("rma:test1", "rma:filePlan");
        map.put("rma:test2", "rma:filePlan");
        bootstrap.setRecordMetadataAspects(map);
        
        // call init
        bootstrap.init();
        
        // verify that the metedata aspects where registered
        QName test1 = QName.createQName(RM_URI, "test1");
        QName test2 = QName.createQName(RM_URI, "test2");
        verify(mockedRecordService).registerRecordMetadataAspect(test1, TYPE_FILE_PLAN);
        verify(mockedRecordService).registerRecordMetadataAspect(test2, TYPE_FILE_PLAN);
    }
}
