package org.alfresco.module.org_alfresco_module_rm.script.hold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseWebScriptUnitTest;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Base hold web script unit test.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class BaseHoldWebScriptUnitTest extends BaseWebScriptUnitTest
{
    /** test holds */
    protected NodeRef hold1NodeRef;
    protected NodeRef hold2NodeRef;
    protected List<NodeRef> holds;
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest#before()
     */
    @Override
    public void before()
    {
        super.before();
        
        // generate test holds
        hold1NodeRef = generateHoldNodeRef("hold1");
        hold2NodeRef = generateHoldNodeRef("hold2");
        
        // list of holds
        holds = new ArrayList<NodeRef>(2);
        Collections.addAll(holds, hold1NodeRef, hold2NodeRef);
    }
}
