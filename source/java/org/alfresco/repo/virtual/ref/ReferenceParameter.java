
package org.alfresco.repo.virtual.ref;

public class ReferenceParameter extends ValueParameter<Reference>
{

    public ReferenceParameter(Reference reference)
    {
        super(reference);
    }

    @Override
    public String stringify(Stringifier stringifier) throws ReferenceEncodingException
    {
        return stringifier.stringifyParameter(this);
    }

}
