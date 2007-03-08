package org.alfresco.web.api;

import org.alfresco.service.cmr.repository.ScriptLocation;

public interface ScriptLoader
{

    ScriptLocation getScriptLocation(String path);

}
