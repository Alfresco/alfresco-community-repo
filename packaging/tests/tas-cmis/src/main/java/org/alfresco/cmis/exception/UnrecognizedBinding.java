package org.alfresco.cmis.exception;

public class UnrecognizedBinding extends Exception
{
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_MESSAGE = "Unrecognized CMIS Binding [%s]. Available binding options: BROWSER or ATOM";
    
    public UnrecognizedBinding(String binding)
    {
        super(String.format(DEFAULT_MESSAGE, binding));
    }
}
