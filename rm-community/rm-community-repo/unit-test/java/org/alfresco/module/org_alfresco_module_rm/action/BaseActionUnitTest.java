package org.alfresco.module.org_alfresco_module_rm.action;

import static org.mockito.Mockito.doReturn;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.action.Action;
import org.mockito.Mock;

/**
 * Declare as version record action unit test.
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public abstract class BaseActionUnitTest extends BaseUnitTest
{
    /** mocked action */
    private @Mock Action mockedAction;
    
    /**
     * @return  mocked action
     */
    protected Action getMockedAction()
    {
        return mockedAction;
    }
    
    /**
     * Helper to mock an action parameter value
     */
    protected void mockActionParameterValue(String name, Object value)
    {
        doReturn(value).when(mockedAction).getParameterValue(name);        
    }
     
}
