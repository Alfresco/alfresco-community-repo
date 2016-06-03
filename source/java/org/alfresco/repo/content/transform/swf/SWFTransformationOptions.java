package org.alfresco.repo.content.transform.swf;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.TransformationOptions;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * SFW transformation options
 * 
 * @author Roy Wetherall
 */
public class SWFTransformationOptions extends TransformationOptions
{
    private static final String OPT_FLASH_VERSION = "flashVersion";
    
    /** The version of the flash to convert to */
    private String flashVersion = "9";
    
    public void setFlashVersion(String flashVersion)
    {
        ParameterCheck.mandatory("flashVersion", flashVersion);
        this.flashVersion = flashVersion;
    }
    
    public String getFlashVersion()
    {
        return flashVersion;
    }

    @Override
    public Map<String, Object> toMap()
    {
        Map<String, Object> baseProps = super.toMap();
        Map<String, Object> props = new HashMap<String, Object>(baseProps);
        props.put(OPT_FLASH_VERSION, flashVersion);
        return props;
    }
}
