package org.alfresco.web.bean.coci;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;

/**
 * Dialog supporting the Upload New Version action on a working copy node.
 */
public class UploadNewVersionDialog extends DoneEditingDialog
{
    private final static String MSG_UPLOAD_NEW_VERSION = "upload_new_version";
    private final static String MSG_LEFT_QUOTE = "left_qoute";
    private final static String MSG_RIGHT_QUOTE = "right_quote";
    private final static String MSG_OF = "of";

    private boolean finishedEditing = false;

    public void setFinishedEditing(boolean finished)
    {
        this.finishedEditing = finished;
    }

    public boolean isFinishedEditing()
    {
        return finishedEditing;
    }

    @Override
    public boolean getFinishButtonDisabled()
    {
        return property.getFile() == null;
    }

    @Override
    public String getContainerTitle()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return Application.getMessage(fc, MSG_UPLOAD_NEW_VERSION) + " " + Application.getMessage(fc, MSG_OF) + " " +
                Application.getMessage(fc, MSG_LEFT_QUOTE)
                + property.getDocument().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        if (finishedEditing)
        {
            property.setKeepCheckedOut(false);
            return checkinFileOK(context, outcome);
        }
        else
        {
            return updateFileOK(context, outcome);
        }
    }
    
    @Override
    public void resetState()
    {
        super.resetState();
        finishedEditing = false;
    }
}
