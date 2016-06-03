package org.alfresco.repo.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.util.FileCopyUtils;

/**
 * {@link ContentServiceHelper} implementation which uses the local ContentService to update the content.
 * 
 * @author Alex Miller
 */
public class LocalContentServiceHelper implements ContentServiceHelper
{

    private ContentService contentService;
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    @Override
    public void updateContent(final NodeRef downloadNode, final File archiveFile) throws ContentIOException, FileNotFoundException, IOException
    {
        //RunAsSystem to mimic clustered behavior, and bypass quotas when using S3 storage.
        AuthenticationUtil.runAsSystem(new RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception
            {
                ContentWriter writer = contentService.getWriter(downloadNode, ContentModel.PROP_CONTENT, true);
                FileCopyUtils.copy(new FileInputStream(archiveFile), writer.getContentOutputStream());
                return null;
            }
        });
    }
}
