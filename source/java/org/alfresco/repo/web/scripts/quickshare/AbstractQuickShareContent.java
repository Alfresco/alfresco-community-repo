/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.web.scripts.quickshare;

import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;


/**
 * QuickShare/PublicView
 * 
 * @author janv
 * @since Cloud/4.2
 */
public abstract class AbstractQuickShareContent extends DeclarativeWebScript
{
    protected QuickShareService quickShareService;
    
    private boolean enabled = true;
    
    public void setQuickShareService(QuickShareService quickShareService)
    {
        this.quickShareService = quickShareService;
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    protected boolean isEnabled()
    {
        return this.enabled;
    }
}