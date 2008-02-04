
package org.alfresco.web.bean.coci;

import java.util.StringTokenizer;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.version.Version;
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
        Version curVersion = property.getVersionQueryService().getCurrentVersion(property.getDocument().getNodeRef());
        StringTokenizer st = new StringTokenizer(curVersion.getVersionLabel(), ".");
        return (Integer.valueOf(st.nextToken()) + 1) + ".0";
    }

    /**
     * @return Returns label for new version with minor changes
     */
    public String getMinorNewVersionLabel()
    {
        Version curVersion = property.getVersionQueryService().getCurrentVersion(property.getDocument().getNodeRef());
        StringTokenizer st = new StringTokenizer(curVersion.getVersionLabel(), ".");
        return st.nextToken() + "." + (Integer.valueOf(st.nextToken()) + 1);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return checkinFileOK(context, outcome);
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

}
