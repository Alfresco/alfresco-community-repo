package org.alfresco.filesys.config;

import java.util.List;

import org.alfresco.filesys.config.acl.AccessControlListBean;
import org.alfresco.jlan.server.core.ShareMapper;

// TODO: Auto-generated Javadoc
/**
 * The Class SecurityConfigBean.
 * 
 * @author dward
 */
public class SecurityConfigBean
{
    /** The global access control. */
    private AccessControlListBean globalAccessControl;

    /** The jce provider. */
    private String jceProvider;

    /** The share mapper. */
    private ShareMapper shareMapper;

    /** The domain mappings. */
    private List<DomainMappingConfigBean> domainMappings;

    /**
     * Gets the global access control.
     * 
     * @return the global access control
     */
    public AccessControlListBean getGlobalAccessControl()
    {
        return globalAccessControl;
    }

    /**
     * Sets the global access control.
     * 
     * @param globalAccessControl
     *            the new global access control
     */
    public void setGlobalAccessControl(AccessControlListBean globalAccessControl)
    {
        this.globalAccessControl = globalAccessControl;
    }

    /**
     * Gets the jCE provider.
     * 
     * @return the jCE provider
     */
    public String getJCEProvider()
    {
        return jceProvider;
    }

    /**
     * Sets the jCE provider.
     * 
     * @param provider
     *            the new jCE provider
     */
    public void setJCEProvider(String provider)
    {
        jceProvider = provider;
    }

    /**
     * Gets the share mapper.
     * 
     * @return the share mapper
     */
    public ShareMapper getShareMapper()
    {
        return shareMapper;
    }

    /**
     * Sets the share mapper.
     * 
     * @param shareMapper
     *            the new share mapper
     */
    public void setShareMapper(ShareMapper shareMapper)
    {
        this.shareMapper = shareMapper;
    }

    /**
     * Gets the domain mappings.
     * 
     * @return the domain mappings
     */
    public List<DomainMappingConfigBean> getDomainMappings()
    {
        return domainMappings;
    }

    /**
     * Sets the domain mappings.
     * 
     * @param domainMappings
     *            the new domain mappings
     */
    public void setDomainMappings(List<DomainMappingConfigBean> domainMappings)
    {
        this.domainMappings = domainMappings;
    }

}
