/*
 * Copyright (C) 2005-2008 Alfresco Software Limited. This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301, USA. As a special exception to the terms and conditions of version 2.0 of the GPL, you may redistribute this Program in connection with Free/Libre and
 * Open Source Software ("FLOSS") applications as described in Alfresco's FLOSS exception. You should have recieved a copy of the text describing the FLOSS exception, and it is
 * also available here: http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.web.action.evaluator;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.ManagePermissionsDialog;

/**
 * UI Action Evaluator - Evaluates whether the change and remove permissions action should be visible.
 * 
 * @author Sergey Gavrusev
 */
public class ManagePermissionIsMainStoreEvaluator extends BaseActionEvaluator
{

    private static final long serialVersionUID = 4221869509273412546L;

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.action.evaluator.BaseActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
     */
    public boolean evaluate(final Node node)
    {
        boolean result = false;
        final String path = AVMNodeConverter.ToAVMVersionPath(node.getNodeRef()).getSecond();
        if (!AVMUtil.isMainStore(AVMUtil.getStoreName(path)))
        {
            result = true;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.action.evaluator.BaseActionEvaluator#evaluate(java.lang.Object)
     */
    @Override
    public boolean evaluate(final Object obj)
    {
        if (obj instanceof ManagePermissionsDialog)
        {
            return ((ManagePermissionsDialog) obj).isRendered();
        }
        return false;
    }

}
