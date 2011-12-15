package org.alfresco.repo.model.filefolder;

import java.util.Set;

import org.alfresco.util.FileFilterMode.Client;

public interface HiddenFileInfo
{
    public Set<Client> getVisibility();
    public int getVisibilityMask();
    public String getFilter();
}
