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
package org.alfresco.web.bean.categories;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

public class CreateCategoryDialog extends BaseDialogBean
{
    private static final String DEFAULT_OUTCOME = "finish";
    
    protected CategoryService categoryService;
    
    /** Current category ref */
    private NodeRef categoryRef = null;
    
    /** Dialog properties */
    private String name = null;
    private String description = null;
    
    @Override
    public void init(Map<String, String> parameters)
    {
       this.isFinished = false;
       
       // retrieve parameters
       String ref = parameters.get(CategoriesDialog.PARAM_CATEGORY_REF);
       
       this.name = null;
       this.description = null;
       
       if (ref != null)
       {
          categoryRef = new NodeRef(ref);
       } 
    }
    
    public CategoryService getCategoryService()
    {
        return categoryService;
    }

    public void setCategoryService(CategoryService categoryService)
    {
        this.categoryService = categoryService;
    }
    
    public NodeRef getCategoryRef()
    {
        return categoryRef;
    }

    public void setCategoryRef(NodeRef categoryRef)
    {
        this.categoryRef = categoryRef;
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    
    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        finishCreate();
        return outcome;
    }

    @Override
    public String getFinishButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), "new_category");
    }
    
    public String finishCreate()
    {
        String outcome = DEFAULT_OUTCOME;

        try
        {
            FacesContext context = FacesContext.getCurrentInstance();
            RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    // create category using categoryservice
                    NodeRef ref;
                    if (getCategoryRef() == null || getCategoryRef().getId().equals("null"))
                    {
                        ref = getCategoryService().createRootCategory(Repository.getStoreRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, getName());
                    }
                    else
                    {
                        ref = getCategoryService().createCategory(getCategoryRef(), getName());
                    }

                    // apply the titled aspect - for description
                    Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
                    titledProps.put(ContentModel.PROP_DESCRIPTION, getDescription());
                    getNodeService().addAspect(ref, ContentModel.ASPECT_TITLED, titledProps);
                    return null;
                }
            };
            txnHelper.doInTransaction(callback);
        }
        catch (Throwable err)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
            outcome = null;
        }

        return outcome;
    }
}
