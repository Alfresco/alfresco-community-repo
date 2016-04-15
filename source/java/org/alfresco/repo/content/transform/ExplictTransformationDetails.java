package org.alfresco.repo.content.transform;

/**
 * Specifies transformations that are considered to be 'exceptional' so 
 * should be used in preference to other transformers that can perform
 * the same transformation.
 */
public class ExplictTransformationDetails extends SupportedTransformation
{
    public ExplictTransformationDetails()
    {
        super();
    }
    
    public ExplictTransformationDetails(String sourceMimetype, String targetMimetype)
    {
        super(sourceMimetype, targetMimetype);
    }
}