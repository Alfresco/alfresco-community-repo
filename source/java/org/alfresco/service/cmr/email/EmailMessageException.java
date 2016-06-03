package org.alfresco.service.cmr.email;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A checked and handled exception indicating a specific and well-known
 * email error condition.
 * 
 * @since 2.2
 */
public class EmailMessageException extends RuntimeException
{
    private static final long serialVersionUID = 5039365329619219256L;

    /**
     * @param message       exception message.
     * @param params        message arguments for I18N
     * 
     * @see I18NUtil#getMessage(String, Object[])
     */
    public EmailMessageException(String message, Object ... params)
    {
        super(I18NUtil.getMessage(message, params));
    }
}
