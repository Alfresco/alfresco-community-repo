
package org.alfresco.repo.model.filefolder;

/**
 * Represents configurable hidden file information, based on which some filename
 * patterns will be hidden or not depending the client
 * 
 * @author Andreea Dragoi
 * @since 4.2.5
 */

public interface ConfigurableHiddenFileInfo extends HiddenFileInfo
{
    public boolean isCmisDisableHideConfig();
    public void setCmisDisableHideConfig(boolean cmisDisableHideConfig);

}
