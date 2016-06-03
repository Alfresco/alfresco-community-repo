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
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class SaveSearchDialog extends AdvancedSearchDialog
{

    private static final long serialVersionUID = 237262751601280456L;
    
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
                ReportedException.throwIfNecessary(e);
            }
        }

        return outcome;
    }

}
