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
 * FIXME: Document me :)
 *
 * @author Rodica Sutu
 * @since 1.0
 */
public class RMSite extends RestSiteModel
{
    @JsonProperty (required = true)
    protected RMSiteCompliance compliance;

    /**
     * FIXME: Document me :)
     *
     * @param compliance the compliance to set
     */
    public void setCompliance(RMSiteCompliance compliance)
    {
        this.compliance = compliance;
    }

    /**
     * FIXME: Document me :)
     *
     * @return FIXME: Document me :)
     */
    public RMSiteCompliance getCompliance()
    {
        return compliance;
    }
}
