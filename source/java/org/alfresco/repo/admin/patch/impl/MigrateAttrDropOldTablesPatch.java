/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrate attributes (drop tables 'alf_*attribute*')
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class MigrateAttrDropOldTablesPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.migrateAttrDropOldTables.result";
    
    private PatchDAO patchDAO;
    
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        patchDAO.migrateOldAttrDropTables();
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS);
        // done
        return msg;
    }
}
