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
package org.alfresco.web.action.evaluator;

import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Evaluates whether the view permissions action should be visible.
 * 
 * @author Sergey Gavrusev
 */
public class ViewPermissionEvaluator extends BaseActionEvaluator
{

    private static final long serialVersionUID = 1340473144312214960L;

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.action.evaluator.BaseActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
     */
    @Override
    public boolean evaluate(final Node node)
    {
        boolean result = true;
//        // WCM
//        final String path = AVMNodeConverter.ToAVMVersionPath(node.getNodeRef()).getSecond();
//        if (!AVMUtil.isMainStore(AVMUtil.getStoreName(path)))
//        {
//            result = false;
//        }
        return result;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.action.evaluator.BaseActionEvaluator#evaluate(java.lang.Object)
     */
    @Override
    public boolean evaluate(Object obj)
    {
//        // WCM
//        if (obj instanceof ManagePermissionsDialog)
//        {
//            return !((ManagePermissionsDialog) obj).isRendered();
//        }
        return false;
    }

}
