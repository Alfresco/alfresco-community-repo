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
