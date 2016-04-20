package org.alfresco.repo.content.transform;

/**
 * Represents a supported transformation. Normally used in a spring bean that limits
 * the number of supported configures.
 */
public class SupportedTransformation
{
    private String sourceMimetype;
    private String targetMimetype;
    
    public SupportedTransformation()
    {   
    }
    
    public SupportedTransformation(String sourceMimetype, String targetMimetype)
    {
        this.sourceMimetype = sourceMimetype;
        this.targetMimetype = targetMimetype;
    }
    
    public void setSourceMimetype(String sourceMimetype)
    {
        this.sourceMimetype = sourceMimetype;
    }
    
    public String getSourceMimetype()
    {
        return sourceMimetype;
    }
    
    public void setTargetMimetype(String targetMimetype)
    {
        this.targetMimetype = targetMimetype;
    }
    
    public String getTargetMimetype()
    {
        return targetMimetype;
    }
}