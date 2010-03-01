/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.admin.patch.impl;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.site.SiteAVMBootstrap;

/**
 * @author Kevin Roast
 */
public class SiteStorePatch extends AbstractPatch
{
    private static final String MSG_RESULT = "patch.createSiteStore.result";
    
    private SiteAVMBootstrap siteBootstrap;
    
    
    /**
     * @param siteBootstrap the SiteAVMBootstrap component to set
     */
    public void setSiteAVMBootstrap(SiteAVMBootstrap siteBootstrap)
    {
        this.siteBootstrap = siteBootstrap;
    }


    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#checkProperties()
     */
    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(this.siteBootstrap, "siteAVMBootstrap");
    }

    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        this.siteBootstrap.bootstrap();
        
        return I18NUtil.getMessage(MSG_RESULT);
    }
}
