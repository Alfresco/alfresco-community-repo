
package org.alfresco.web.bean.coci;

import java.io.Serializable;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;

/**
 * This bean class handle done-editing(commit) dialog.
 *
 */
public class CCDoneEditingDialog extends CheckinCheckoutDialog
{

    private final static String MSG_DONE = "done";
    private final static String MSG_TITLE = "done_editing_title";

    /**
     * @return Returns label for new version with major changes
     */
    public String getMajorNewVersionLabel()
    {
        String label = getCurrentVersionLabel();
        StringTokenizer st = new StringTokenizer(label, ".");
        return (Integer.valueOf(st.nextToken()) + 1) + ".0";
    }

    /**
     * @return Returns label for new version with minor changes
     */
    public String getMinorNewVersionLabel()
    {
        String label = getCurrentVersionLabel();
        StringTokenizer st = new StringTokenizer(label, ".");
        return st.nextToken() + "." + (Integer.valueOf(st.nextToken()) + 1);
    }

    @Override
    public String getFinishButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_DONE);
    }

    @Override
    public boolean getFinishButtonDisabled()
    {
       return false;
    }

    @Override
    public String getContainerTitle()
    {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_TITLE) + " '" + property.getDocument().getName() + "'";
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return checkinFileOK(context, outcome);
    }

    /**
     * @return version label for source node for working copy.
     */
    private String getCurrentVersionLabel()
    {
        NodeRef workingCopyNodeRef = property.getDocument().getNodeRef();
        if (this.nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_COPIEDFROM) == true)
        {
            Map<QName, Serializable> workingCopyProperties = nodeService.getProperties(workingCopyNodeRef);
            NodeRef nodeRef = (NodeRef) workingCopyProperties.get(ContentModel.PROP_COPY_REFERENCE);

            Version curVersion = property.getVersionQueryService().getCurrentVersion(nodeRef);
            return curVersion.getVersionLabel();
        }

        return null;
    }

}
