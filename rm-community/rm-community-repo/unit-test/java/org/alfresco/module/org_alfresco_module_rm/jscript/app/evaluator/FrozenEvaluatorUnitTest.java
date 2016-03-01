 
package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * Freeze evaluator unit test.
 * 
 * @author Roy Wetherall
 */
public class FrozenEvaluatorUnitTest extends BaseUnitTest
{
    @Mock(name="kinds") Set<FilePlanComponentKind> mockedKinds;
    
    @Spy @InjectMocks FrozenEvaluator evaluator;
    
    @Before
    @Override
    public void before() throws Exception
    {
        super.before();
        
        // setup interactions
        doReturn(false).when(mockedKinds).contains(FilePlanComponentKind.RECORD_CATEGORY);
        doReturn(true).when(mockedKinds).contains(FilePlanComponentKind.RECORD_FOLDER);
        doReturn(true).when(mockedKinds).contains(FilePlanComponentKind.RECORD);
    }

    @Test
    public void isNotRecordOrRecordFolder()
    {
        // setup interactions
        NodeRef nodeRef = generateNodeRef(TYPE_RECORD_CATEGORY);
        doReturn(FilePlanComponentKind.RECORD_CATEGORY).when(mockedFilePlanService).getFilePlanComponentKind(nodeRef);
        
        // evaluate 
        boolean result = evaluator.evaluate(filePlanComponent);
        assertFalse(result);
        
        // verify interactions
        verify(mockedHoldService, never()).heldBy(filePlanComponent, true);
    }
    
    @Test
    public void isNotHeld()
    {
        // setup interactions
        doReturn(FilePlanComponentKind.RECORD).when(mockedFilePlanService).getFilePlanComponentKind(record);
        doReturn(Collections.EMPTY_LIST).when(mockedHoldService).heldBy(record, true);
        
        // evaluate
        boolean result = evaluator.evaluate(record);
        assertFalse(result);
        
        // verify interactions
        verify(mockedHoldService, times(1)).heldBy(record, true);
    }
    
    @Test
    public void isHeldByAtLeastOne()
    {
        // setup interactions
        doReturn(FilePlanComponentKind.RECORD).when(mockedFilePlanService).getFilePlanComponentKind(record);
        doReturn(Collections.singletonList(generateNodeRef(TYPE_HOLD))).when(mockedHoldService).heldBy(record, true);
        
        // evaluate
        boolean result = evaluator.evaluate(record);
        assertTrue(result);
        
        // verify interactions
        verify(mockedHoldService, times(1)).heldBy(record, true);
    }
}
