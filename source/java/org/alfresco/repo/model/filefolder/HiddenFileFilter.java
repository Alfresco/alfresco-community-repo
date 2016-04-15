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
package org.alfresco.repo.model.filefolder;

import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * Spring bean defining a hidden node filter.
 * 
 * @since 4.0
 *
 */
public class HiddenFileFilter implements InitializingBean
{
    private String filter;
    private String visibility;
    private String hiddenAttribute;
    private boolean cascadeHiddenAspect = true;
    private boolean cascadeIndexControlAspect = true;
    private boolean cmisDisableHideConfig;

    public HiddenFileFilter()
    {
    }

    public void setFilter(String filter)
    {
        this.filter = filter;
    }
    
    public void setVisibility(String visibility)
    {
        this.visibility = visibility;
    }
    
    public String getFilter()
    {
        return filter;
    }

    public String getVisibility()
    {
        return visibility;
    }
    
    public String getHiddenAttribute()
    {
        return hiddenAttribute;
    }

    public void setHiddenAttribute(String hiddenAttribute)
    {
        this.hiddenAttribute = hiddenAttribute;
    }
    
    public void setCascadeHiddenAspect(boolean cascadeHiddenAspect)
    {
		this.cascadeHiddenAspect = cascadeHiddenAspect;
	}

	public boolean cascadeHiddenAspect()
    {
		return cascadeHiddenAspect;
	}

    public void setCascadeIndexControlAspect(boolean cascadeIndexControlAspect)
    {
		this.cascadeIndexControlAspect = cascadeIndexControlAspect;
	}

	public boolean cascadeIndexControlAspect()
    {
		return cascadeIndexControlAspect;
	}
	
    public void setCmisDisableHideConfig(boolean cmisDisableHideConfig)
    {
        this.cmisDisableHideConfig = cmisDisableHideConfig;
    }

    public boolean isCmisDisableHideConfig()
    {
        return cmisDisableHideConfig;
    }
	/*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "filter", filter);
        if(visibility == null)
        {
            visibility = "";
        }
        if(hiddenAttribute == null)
        {
            hiddenAttribute = "";
        }
    }
}
