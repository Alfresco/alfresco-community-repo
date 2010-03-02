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
package org.alfresco.web.bean.search;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class EditSearchDialog extends AdvancedSearchDialog
{
    private static final long serialVersionUID = -8914819218709478527L;
    
    private static final String MSG_ERROR_SAVE_SEARCH = "error_save_search";

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return saveEditSearchOK(context, outcome);
    }

    @Override
    public String getFinishButtonLabel()
    {
        return super.getFinishButtonLabel();
    }

    public String saveEditSearchOK(FacesContext newContext, String newOutcome)
    {
        String outcome = newOutcome;

        final SearchContext search = this.navigator.getSearchContext();
        if (search != null)
        {
            try
            {
                final FacesContext context = newContext;

                RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        // handle Edit e.g. Overwrite of existing search
                        // detect if was previously selected saved search (e.g.
                        // NodeRef not null)
                        NodeRef searchRef = new NodeRef(Repository.getStoreRef(), properties.getSavedSearch());
                        if (getNodeService().exists(searchRef))
                        {
                            Map<QName, Serializable> props = getNodeService().getProperties(searchRef);
                            props.put(ContentModel.PROP_NAME, properties.getSearchName());
                            props.put(ContentModel.PROP_DESCRIPTION, properties.getSearchDescription());
                            getNodeService().setProperties(searchRef, props);

                            ContentService contentService = Repository.getServiceRegistry(context).getContentService();
                            ContentWriter writer = contentService.getWriter(searchRef, ContentModel.PROP_CONTENT, true);

                            // get a writer to our new node ready for XML
                            // content
                            writer.setMimetype(MimetypeMap.MIMETYPE_XML);
                            writer.setEncoding("UTF-8");

                            // output an XML serialized version of the
                            // SearchContext object
                            writer.putContent(search.toXML());
                        }
                        return null;
                    }
                };
                callback.execute();

                properties.getCachedSavedSearches().clear();
                properties.setSavedSearch(null);
            }
            catch (Throwable e)
            {
                Utils.addErrorMessage(MessageFormat.format(Application.getMessage(newContext, MSG_ERROR_SAVE_SEARCH), e.getMessage()), e);
                outcome = null;
                this.isFinished = false;
                ReportedException.throwIfNecessary(e);
            }
        }

        return outcome;
    }

}
