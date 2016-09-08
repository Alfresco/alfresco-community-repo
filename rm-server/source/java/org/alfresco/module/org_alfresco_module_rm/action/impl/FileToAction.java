package org.alfresco.module.org_alfresco_module_rm.action.impl;

/**
 * File To action implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class FileToAction extends CopyMoveLinkFileToBaseAction
{
    /** action name */
    public static final String NAME = "fileTo";

    @Override
    public void init()
    {
        super.init();
        setMode(CopyMoveLinkFileToActionMode.MOVE);
    }
}
