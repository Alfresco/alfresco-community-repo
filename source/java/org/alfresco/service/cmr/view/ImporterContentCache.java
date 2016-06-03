package org.alfresco.service.cmr.view;

import org.alfresco.service.cmr.repository.ContentData;

public interface ImporterContentCache
{
    public ContentData getContent(ImportPackageHandler handler, ContentData sourceContent);
}
