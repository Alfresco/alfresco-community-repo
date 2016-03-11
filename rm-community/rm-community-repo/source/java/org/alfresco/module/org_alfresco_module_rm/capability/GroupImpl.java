package org.alfresco.module.org_alfresco_module_rm.capability;

import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Group implementation
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class GroupImpl implements Group
{
    /** The group id */
    private String id;

    /** The group title */
    private String title;

    /** The group index */
    private int index;

    /** Capability service */
    private CapabilityService capabilityService;

    /**
     * Sets the capability service
     *
     * @param capabilityService the capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    public void init()
    {
        this.capabilityService.addGroup(this);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Group#getId()
     */
    @Override
    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Group#getTitle()
     */
    @Override
    public String getTitle()
    {
        String title = this.title;
        if (StringUtils.isBlank(title))
        {
            title = I18NUtil.getMessage("capability.group." + getId() + ".title");
        }
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Group#getIndex()
     */
    @Override
    public int getIndex()
    {
        return this.index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }
}
