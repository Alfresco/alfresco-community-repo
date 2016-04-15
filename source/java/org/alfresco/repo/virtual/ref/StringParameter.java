
package org.alfresco.repo.virtual.ref;

/**
 * Helper class that has one {@link String} as a value attribute, value that can
 * be retrieved and used in virtualization process.
 * <p>
 * It also provides the possibility of converting the value attribute into a
 * string representation according to {@link Encodings} definition, using
 * provided {@link Stringifier} objects.
 */
public class StringParameter extends ValueParameter<String>
{
    public StringParameter(String value)
    {
        super(value);
    }

    @Override
    public String stringify(Stringifier stringifier) throws ReferenceEncodingException
    {
        return stringifier.stringifyParameter(this);
    }
}
