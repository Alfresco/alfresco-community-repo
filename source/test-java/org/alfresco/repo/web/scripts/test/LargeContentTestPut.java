package org.alfresco.repo.web.scripts.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Test webscript class that simply read request content. Used in unit test for MNT-11237 issue.
 * 
 * @author pavel.yurkevich
 */
public class LargeContentTestPut extends DeclarativeWebScript
{
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        try
        {
            req.getContent().getInputStream().close();
            model.put("result", "success");
        }
        catch (IOException e)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Fail to read request content.");
        }
        return model;
    }

}
