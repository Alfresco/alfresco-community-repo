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
package org.alfresco.web.bean.search;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;

import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

public class SaveSearchDialog extends AdvancedSearchDialog
{

    private static final String MSG_ERROR_SAVE_SEARCH = "error_save_search";

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return saveNewSearchOK(context, outcome);
    }

    @Override
    public String getFinishButtonLabel()
    {
        return super.getFinishButtonLabel();
    }

    public String saveNewSearchOK(FacesContext newContext, String newOutcome)
    {
        String outcome = newOutcome;

        NodeRef searchesRef;
        if (properties.isSearchSaveGlobal())
        {
            searchesRef = getGlobalSearchesRef();
        }
        else
        {
            searchesRef = getUserSearchesRef();
        }

        final SearchContext search = this.navigator.getSearchContext();
        if (searchesRef != null && search != null)
        {
            try
            {
                final FacesContext context = newContext;// FacesContext.getCurrentInstance();
                final NodeRef searchesRefFinal = searchesRef;

                RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        // create new content node as the saved search object
                        Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
                        props.put(ContentModel.PROP_NAME, properties.getSearchName());
                        props.put(ContentModel.PROP_DESCRIPTION, properties.getSearchDescription());
                        ChildAssociationRef childRef = getNodeService().createNode(searchesRefFinal, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.ALFRESCO_URI, QName
                                .createValidLocalName(properties.getSearchName())), ContentModel.TYPE_CONTENT, props);

                        ContentService contentService = Repository.getServiceRegistry(context).getContentService();
                        ContentWriter writer = contentService.getWriter(childRef.getChildRef(), ContentModel.PROP_CONTENT, true);

                        // get a writer to our new node ready for XML content
                        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
                        writer.setEncoding("UTF-8");

                        // output an XML serialized version of the SearchContext
                        // object
                        writer.putContent(search.toXML());
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
            }
        }

        return outcome;
    }

}
