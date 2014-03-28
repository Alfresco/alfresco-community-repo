package org.alfresco.module.org_alfresco_module_rm.script.hold;

import static org.alfresco.module.org_alfresco_module_rm.test.util.WebScriptExceptionMatcher.badRequest;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Base hold web script with content unit test.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class BaseHoldWebScriptWithContentUnitTest extends BaseHoldWebScriptUnitTest
{   
    /**
     * Helper method to build JSON content to send to hold webscripts.
     */
    protected String buildContent(NodeRef nodeRef, List<NodeRef> holds)
    {
        StringBuilder builder = new StringBuilder(255);
        builder.append("{");
       
        if (nodeRef != null)
        {
            builder.append("'nodeRef':'").append(nodeRef.toString()).append("'");
        }
        
        if (nodeRef != null && holds != null)
        {
            builder.append(",");
        }
        
        if (holds != null)
        {
            builder.append("'holds':[");
            
            boolean bFirst = true;
            for (NodeRef hold : holds)
            {
                if (bFirst == false)
                {
                    builder.append(",");
                }
                else
                {
                    bFirst = false;
                }
                
                builder.append("'" + hold.toString() + "'");
            }
            
            builder.append("]");
        }
        
        builder.append("}");
        
        return builder.toString();
    }
    
    /**
     * Test for expected exception when invalid JSON sent
     */
    @SuppressWarnings("unchecked")
    @Test
    public void sendInvalidJSON() throws Exception
    {
        // invalid JSON
        String content = "invalid JSON";       
        
        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());
        
        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);   
    }
    
    /**
     * Test for expected exception when one of the holds doesn't exist.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void holdDoesNotExist() throws Exception
    {
        // setup interactions
        when(mockedNodeService.exists(eq(hold1NodeRef))).thenReturn(false);
        
        // build content   
        String content = buildContent(record, holds);
        
        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());
        
        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);         
    }
    
    /**
     * Test for expected excpetion when the item being added to the hold
     * does not exist.
     */
    @SuppressWarnings("unchecked")
    @Test    
    public void nodeRefDoesNotExist() throws Exception
    {
        // setup interactions
        when(mockedNodeService.exists(eq(record))).thenReturn(false);
        
        // build content   
        String content = buildContent(record, holds);
        
        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());
        
        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);          
    }
    
    /**
     * Test for expected exception when hold information is missing from
     * sent JSON.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void holdMissingFromContent() throws Exception
    {
        // build content
        String content = buildContent(record, null);
        
        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());
        
        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content); 
    }
    
    /**
     * Test for expected exception when noderef information is missing
     * from send JSON.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void nodeRefMissingFromContent() throws Exception
    {
        // build content
        String content = buildContent(null, holds);
        
        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());
        
        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);  
    }
    
    /**
     * Test for expected exception when adding an item to something
     * that isn't a hold.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void holdIsNotAHold() throws Exception
    {
        // build json content to send to server
        List<NodeRef> notAHold = Collections.singletonList(recordFolder);
        String content = buildContent(record, notAHold);    
        
        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());    
        
        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);            
    }
    
    /**
     * Test for expected exception when adding an item to a hold
     * that isn't a record or record folder.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void nodeRefIsNotARecordOrRecordFolder() throws Exception
    {
        // build json content to send to server
        List<NodeRef> notAHold = Collections.singletonList(recordFolder);
        String content = buildContent(filePlanComponent, notAHold);    
        
        // expected exception
        exception.expect(WebScriptException.class);
        exception.expect(badRequest());    
        
        // execute web script
        executeWebScript(Collections.EMPTY_MAP, content);         
    }
}
