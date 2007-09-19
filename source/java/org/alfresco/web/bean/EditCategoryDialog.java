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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.repo.component.IRepoBreadcrumbHandler;

public class EditCategoryDialog extends CategoriesDialog
{
    private static final String DEFAULT_OUTCOME = "finish";
    private final static String MSG_EDIT_CATEGORY = "edit_category";
    private final static String MSG_FINISH = "finish_button";

    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        finishEdit();
        return outcome;
    }

    @Override
    public String getContainerTitle()
    {

        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_EDIT_CATEGORY) + " '" + properties.getActionCategory().getName() + "'";
    }

    @Override
    public String getFinishButtonLabel()
    {

        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_FINISH);
    }

    public String finishEdit()
    {
        String outcome = DEFAULT_OUTCOME;

        try
        {
            FacesContext context = FacesContext.getCurrentInstance();
            RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
            RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                    // update the category node
                    NodeRef nodeRef = properties.getActionCategory().getNodeRef();
                    nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, properties.getName());

                    // apply the titled aspect - for description
                    if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED) == false)
                    {
                        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
                        titledProps.put(ContentModel.PROP_DESCRIPTION, properties.getDescription());
                        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, titledProps);
                    }
                    else
                    {
                        nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, properties.getDescription());
                    }
                    return nodeRef;
                }
            };
            NodeRef nodeRef = txnHelper.doInTransaction(callback);

            // edit the node in the breadcrumb if required
            List<IBreadcrumbHandler> location = getLocation();
            IBreadcrumbHandler handler = location.get(location.size() - 1);

            // see if the current breadcrumb location is our node
            if (nodeRef.equals(((IRepoBreadcrumbHandler) handler).getNodeRef()))
            {
                // and update with the modified node details
                IBreadcrumbHandler newHandler = new CategoryBreadcrumbHandler(nodeRef, Repository.getNameForNode(nodeService, nodeRef));
                location.set(location.size() - 1, newHandler);
            }
        }
        catch (Throwable err)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
            outcome = null;
        }

        return outcome;
    }
}
