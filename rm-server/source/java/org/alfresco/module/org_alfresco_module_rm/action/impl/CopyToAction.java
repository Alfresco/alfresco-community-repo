package org.alfresco.module.org_alfresco_module_rm.action.impl;

/**
 * File To action implementation.
 *
 * @author Mark Hibbins
 * @since 2.2
 */
public class CopyToAction extends CopyMoveLinkFileToBaseAction
{
    /** action name */
    public static final String NAME = "copyTo";

    @Override
    public void init()
    {
        super.init();
        setMode(CopyMoveLinkFileToActionMode.COPY);
    }
}