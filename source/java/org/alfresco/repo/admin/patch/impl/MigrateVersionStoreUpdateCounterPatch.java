/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.admin.patch.impl;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.version.common.counter.VersionCounterService;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Update internal version2Store counter if needed (eg. affects upgrades from 2.x to 3.0.1, will not affect upgrades from 2.x to 3.1.0)
 */
public class MigrateVersionStoreUpdateCounterPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.migrateVersionStoreUpdateCounter.result";
    
    private VersionCounterService versionCounterService;
    
    public void setVersionCounterService(VersionCounterService versionCounterService)
    {
        this.versionCounterService = versionCounterService;
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        int oldV1count = versionCounterService.currentVersionNumber(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, VersionModel.STORE_ID));
        int oldV2count = versionCounterService.currentVersionNumber(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID));
        
        int newV2count = (oldV1count+oldV2count);
        versionCounterService.setVersionNumber(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID), newV2count);
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS, "oldV1count="+oldV1count+",oldV2count="+oldV2count+",newV2count="+newV2count);
        
        // done
        return msg;
    }
}
