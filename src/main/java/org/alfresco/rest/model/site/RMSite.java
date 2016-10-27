/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.rest.model.site;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.com.site.RMSiteCompliance;
import org.alfresco.rest.model.RestSiteModel;

/**
 * POJO for RM Site component
 *
 * @author Rodica Sutu
 * @since 1.0
 */
public class RMSite extends RestSiteModel
{
    @JsonProperty (required = true)
    protected RMSiteCompliance compliance;

    /**
     * Helper method to set RM site compliance
     * @param compliance {@link RMSiteCompliance} the compliance to set
     */
    public void setCompliance(RMSiteCompliance compliance)
    {
        this.compliance = compliance;
    }

    /**
     * Helper method to get RM site compliance
     * @return compliance the RM Site compliance to get
     */
    public RMSiteCompliance getCompliance()
    {
        return compliance;
    }
}
