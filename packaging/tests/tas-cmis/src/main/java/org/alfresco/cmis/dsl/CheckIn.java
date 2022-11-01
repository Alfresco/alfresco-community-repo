package org.alfresco.cmis.dsl;

import org.alfresco.cmis.CmisWrapper;
import org.alfresco.utility.Utility;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;

import java.util.Map;

/**
 * DSL pertaining only to check in a {@link Document}
 */
public class CheckIn
{
    private CmisWrapper cmisWrapper;
    private boolean version;
    private Map<String, ?> properties;
    private String content;
    private String comment;

    public CheckIn(CmisWrapper cmisWrapper)
    {
        this.cmisWrapper = cmisWrapper;
    }

    public CheckIn withMajorVersion()
    {
        this.version = true;
        return this;
    }

    public CheckIn withMinorVersion()
    {
        this.version = false;
        return this;
    }

    public CheckIn withContent(String content)
    {
        this.content = content;
        return this;
    }

    public CheckIn withoutComment()
    {
        this.comment = null;
        return this;
    }

    public CheckIn withComment(String comment)
    {
        this.comment = comment;
        return this;
    }

    public CmisWrapper checkIn() throws Exception
    {
        return checkIn(properties);
    }
    
    public CmisWrapper checkIn(Map<String, ?> properties) throws Exception
    {
        ContentStream contentStream = cmisWrapper.withCMISUtil().getContentStream(content);
        try
        {
            Document pwc = cmisWrapper.withCMISUtil().getPWCDocument();
            pwc.refresh();
            Utility.waitToLoopTime(2);
            pwc.checkIn(version, properties, contentStream, comment);
        }
        catch(CmisStorageException st)
        {
            cmisWrapper.withCMISUtil().getPWCDocument().checkIn(version, properties, contentStream, comment);
        }
        return cmisWrapper;
    }
}
