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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.repo.component.IRepoBreadcrumbHandler;

public class DeleteCategoryDialog extends CategoriesDialog
{
    private static final String DEFAULT_OUTCOME = "finish";
    private final static String MSG_DELETE_CATEGORY = "delete_category";
    private final static String MSG_DELETE = "delete";

    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        finishDelete();
        return outcome;
    }

    @Override
    public String getContainerTitle()
    {

        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_DELETE_CATEGORY) + " '" + properties.getActionCategory().getName() + "'";
    }

    @Override
    public String getFinishButtonLabel()
    {

        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_DELETE);
    }

    public String finishDelete()
    {
        String outcome = DEFAULT_OUTCOME;

        if (properties.getActionCategory() != null)
        {
            try
            {
                FacesContext context = FacesContext.getCurrentInstance();
                RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
                RetryingTransactionCallback<NodeRef> callback = new RetryingTransactionCallback<NodeRef>()
                {
                    @SuppressWarnings("unchecked")
                    public NodeRef execute() throws Throwable
                    {
                        // delete the category node using the nodeservice
                        NodeRef categoryNodeRef = properties.getActionCategory().getNodeRef();
                        properties.getCategoryService().deleteCategory(categoryNodeRef);

                        // if there are other items in the repository using this category
                        // all the associations to the category should be removed too
                        if (properties.getMembers() != null && properties.getMembers().size() > 0)
                        {
                            for (ChildAssociationRef childRef : properties.getMembers())
                            {
                                List<NodeRef> list = new ArrayList<NodeRef>(properties.getMembers().size());

                                NodeRef member = childRef.getChildRef();
                                Collection<NodeRef> categories = (Collection<NodeRef>) nodeService.getProperty(member, ContentModel.PROP_CATEGORIES);

                                for (NodeRef category : categories)
                                {
                                    if (category.equals(categoryNodeRef) == false)
                                    {
                                        list.add(category);
                                    }
                                }

                                // persist the list back to the repository
                                nodeService.setProperty(member, ContentModel.PROP_CATEGORIES, (Serializable) list);
                            }
                        }
                        return categoryNodeRef;
                    }
                };
                NodeRef categoryNodeRef = txnHelper.doInTransaction(callback);

                // remove this node from the breadcrumb if required
                List<IBreadcrumbHandler> location = getLocation();
                IBreadcrumbHandler handler = location.get(location.size() - 1);

                // see if the current breadcrumb location is our node
                if (categoryNodeRef.equals(((IRepoBreadcrumbHandler) handler).getNodeRef()))
                {
                    location.remove(location.size() - 1);

                    // now work out which node to set the list to refresh against
                    if (location.size() != 0)
                    {
                        handler = location.get(location.size() - 1);
                        this.setCurrentCategory(((IRepoBreadcrumbHandler) handler).getNodeRef());
                    }
                }

                // clear action context
                setActionCategory(null);
            }
            catch (Throwable err)
            {
                Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
                outcome = null;
            }
        }

        return outcome;
    }

}
