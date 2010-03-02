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
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class CreateCategoryDialog extends BaseDialogBean
{
    private static final long serialVersionUID = 1905920932992642119L;

    private static final String DEFAULT_OUTCOME = "finish";
    
    transient protected CategoryService categoryService;
    
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
       else
       {
          categoryRef = null;
       }
    }
    
    /**
     * @return the categoryService
     */
    private CategoryService getCategoryService()
    {
       //check for null in cluster environment
       if(categoryService == null)
       {
          categoryService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getCategoryService();
       }
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
            ReportedException.throwIfNecessary(err);
        }

        return outcome;
    }
}
