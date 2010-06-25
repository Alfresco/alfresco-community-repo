/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrate attributes - check no custom attributes and then delete from 'alf_*attribute*' tables
 * 
 * @author janv
 * @since 3.4
 */
public class MigrateAttrDeletePatch extends AbstractPatch
{
    private Log logger = LogFactory.getLog(this.getClass());
    
    private static final String MSG_SUCCESS = "patch.migrateAttrDelete.result";
    
    private PatchDAO patchDAO;
    
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        List<String> results = patchDAO.getOldAttrCustomNames();
        
        if (results.size() > 0)
        {
            for (String custom : results)
            {
                logger.warn("Custom global attribute found: "+custom);
            }
            throw new AlfrescoRuntimeException("Custom attributes found - will require custom migration patch: "+results);
        }
        
        patchDAO.deleteAllOldAttrs();
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS);
        // done
        return msg;
    }
}
