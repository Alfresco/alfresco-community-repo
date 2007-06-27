/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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

import java.util.Iterator;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.avm.AVMNodeProperty;
import org.alfresco.repo.avm.AVMNodePropertyDAO;

/**
 * Patch more remapping AVM properties.
 * @author britt
 */
public class AVMPropertiesPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.AVMProperties.result";

    private AVMNodePropertyDAO fAVMNodePropertyDAO;
    
    public void setAvmNodePropertyDAO(AVMNodePropertyDAO dao)
    {
        fAVMNodePropertyDAO = dao;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        Iterator<AVMNodeProperty> iter = fAVMNodePropertyDAO.iterate();
        while (iter.hasNext())
        {
            AVMNodeProperty prop = iter.next();
            prop.getNode().getProperties().put(prop.getName(), prop.getValue());
            fAVMNodePropertyDAO.delete(prop.getNode(), prop.getName());
        }
        return I18NUtil.getMessage(MSG_SUCCESS);
    }
}
