
package org.alfresco.repo.virtual.ref;

public class GetChildByIdMethod extends AbstractProtocolMethod<Reference>
{
    private String childId;

    public GetChildByIdMethod(String childId)
    {
        super();
        this.childId = childId;
    }

    /**
     * Provides a child {@link Reference} obtained from the parent
     * {@link Reference} and the childId. The inner template path is obtained
     * from the parent {@link Reference} and then the childId String is
     * concatenated to it. The child {@link Reference} is created by calling
     * Protocol#replaceTemplatePathMethod with the new id String as a parameter.
     * 
     * @param virtualProtocol
     * @param reference the parent {@link Reference}
     * @return the child {@link Reference}
     * @throws ProtocolMethodException
     */
    @Override
    public Reference execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        String path = reference.execute(new GetTemplatePathMethod()).trim();
        StringBuilder pathBuilder = new StringBuilder(path);
        if (!path.endsWith(PATH_SEPARATOR))
        {
            pathBuilder.append(PATH_SEPARATOR);
        }
        pathBuilder.append(childId);

        return virtualProtocol.replaceTemplatePath(reference,
                                                   pathBuilder.toString());
    }
}
