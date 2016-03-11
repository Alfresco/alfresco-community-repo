package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Filling on hold container capability condition unit test
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class FillingOnHoldContainerCapabilityConditionUnitTest extends BaseUnitTest
{
    /** evaluator */
    private @InjectMocks FillingOnHoldContainerCapabilityCondition condition;
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Before
    @Override
    public void before() throws Exception
    {
        super.before();
    }
    
    /**
     * Given hold container node
     * And no filling permission
     * When evaluate
     * Then false
     */
    @Test
    public void noFillingOnHoldContainer()
    {
        NodeRef holdContainer = generateNodeRef(TYPE_HOLD_CONTAINER);
        when(mockedFilePlanService.isFilePlan(holdContainer))
            .thenReturn(false);
        when(mockedPermissionService.hasPermission(holdContainer, RMPermissionModel.FILE_RECORDS))
            .thenReturn(AccessStatus.DENIED);
        
        assertFalse(condition.evaluateImpl(holdContainer));
    }
    
    /**
     * Given hold container node
     * And filling permission
     * When evaluate
     * Then true
     */
    @Test
    public void fillingOnHoldContainer()
    {
        NodeRef holdContainer = generateNodeRef(TYPE_HOLD_CONTAINER);
        when(mockedFilePlanService.isFilePlan(holdContainer))
            .thenReturn(false);
        when(mockedPermissionService.hasPermission(holdContainer, RMPermissionModel.FILE_RECORDS))
            .thenReturn(AccessStatus.ALLOWED);
        
        assertTrue(condition.evaluateImpl(holdContainer));
    }
    
    /**
     * Given file-plan node
     * And no filling permission on hold container
     * When evaluate
     * Then false
     */
    @Test
    public void filePlanNoFilling()
    {
        NodeRef holdContainer = generateNodeRef(TYPE_HOLD_CONTAINER);
        when(mockedFilePlanService.getHoldContainer(filePlan))
            .thenReturn(holdContainer);
        when(mockedPermissionService.hasPermission(holdContainer, RMPermissionModel.FILE_RECORDS))
            .thenReturn(AccessStatus.DENIED);
        
        assertFalse(condition.evaluateImpl(holdContainer));
    }
    
    /**
     * Given file-plan node
     * And filling permission on hold container
     * When evaluate
     * Then true
     */
    @Test
    public void filePlanFilling()
    {
        NodeRef holdContainer = generateNodeRef(TYPE_HOLD_CONTAINER);
        when(mockedFilePlanService.getHoldContainer(filePlan))
            .thenReturn(holdContainer);
        when(mockedPermissionService.hasPermission(holdContainer, RMPermissionModel.FILE_RECORDS))
            .thenReturn(AccessStatus.ALLOWED);
        
        assertTrue(condition.evaluateImpl(holdContainer));
    }
    
    /**
     * Given unexpected node type
     * When evaluate
     * Then false
     */
    @Test
    public void unexpectedNode()
    {
        NodeRef unexpectedNode = generateNodeRef();
        when(mockedFilePlanService.isFilePlan(unexpectedNode))
            .thenReturn(false);
        when(mockedPermissionService.hasPermission(unexpectedNode, RMPermissionModel.FILE_RECORDS))
            .thenReturn(AccessStatus.ALLOWED);
        
        assertFalse(condition.evaluateImpl(unexpectedNode));
    }    
}
