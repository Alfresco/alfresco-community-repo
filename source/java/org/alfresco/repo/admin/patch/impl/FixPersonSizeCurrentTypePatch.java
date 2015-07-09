/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * A patch to update the 'actual_type_n' and 'persisted_type_n' of {http://www.alfresco.org/model/content/1.0}sizeCurrent
 * property to be Long (3).
 * <p>
 * 
 * @author Viachaslau Tsikhanovich
 */
public class FixPersonSizeCurrentTypePatch extends AbstractPatch
{
    private static final String MSG_START = "patch.fixPersonSizeCurrentType.start";
    private static final String MSG_DONE = "patch.fixPersonSizeCurrentType.done";

    private PatchDAO patchDAO;

    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(patchDAO, "patchDAO");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        StringBuilder result = new StringBuilder(I18NUtil.getMessage(MSG_START));
        int updateCount = patchDAO.updatePersonSizeCurrentType();
        result.append(I18NUtil.getMessage(MSG_DONE, updateCount));
        return result.toString();
    }

}
