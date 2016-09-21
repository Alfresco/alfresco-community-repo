package org.alfresco.module.org_alfresco_module_rm.action.impl;

/**
 * Link To action implementation.
 *
 * @author Mark Hibbins
 * @since 2.2
 */
public class LinkToAction extends CopyMoveLinkFileToBaseAction
{
    /** action name */
    public static final String NAME = "linkTo";

    @Override
    public void init()
    {
        super.init();
        setMode(CopyMoveLinkFileToActionMode.LINK);
    }
}