/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
