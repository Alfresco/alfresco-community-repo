package org.alfresco.web.bean.spaces;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.TemplateSupportBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

public class ApplyDocTemplateDialog extends BaseDialogBean
{
    private static final long serialVersionUID = -9098279672434597354L;
    
    private static final String DIALOG_CLOSE = "dialog:close";
    private static final String MSG_APPLY_TEMPLATE = "apply_template";
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";
    
    protected String template;
    
    
    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        if (this.template != null && this.template.equals(TemplateSupportBean.NO_SELECTION) == false)
        {
            try
            {
                // apply the templatable aspect if required
                if (getNode().hasAspect(ContentModel.ASPECT_TEMPLATABLE) == false)
                {
                    this.getNodeService().addAspect(getNode().getNodeRef(), ContentModel.ASPECT_TEMPLATABLE, null);
                }

                // get the selected template from the Template Picker
                NodeRef templateRef = new NodeRef(Repository.getStoreRef(), this.template);

                // set the template NodeRef into the templatable aspect property
                this.getNodeService().setProperty(getNode().getNodeRef(), ContentModel.PROP_TEMPLATE, templateRef);

                // reset node details for next refresh of details page
                getNode().reset();
            }
            catch (Exception e)
            {
                Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
                ReportedException.throwIfNecessary(e);
            }
        }
        return DIALOG_CLOSE;
    }

    public String getContainerTitle()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Application.getMessage(fc, MSG_APPLY_TEMPLATE) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
    }

    public String getTemplate()
    {
        // return current template if it exists
        NodeRef ref = (NodeRef) getNode().getProperties().get(ContentModel.PROP_TEMPLATE);
        return ref != null ? ref.getId() : this.template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public Node getNode()
    {
        return this.browseBean.getDocument();
    }

    public String getName()
    {
        return getNode().getName();
    }
}