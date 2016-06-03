package org.alfresco.repo.content.transform;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * 
 * Wraps an exception that could be thrown in any transformer to
 * propagate it up to <code>NodeInfoBean.sendNodeInfo</code> method.
 * <code>NodeInfoBean</code> can handle this exception to display it in NodeInfo frame
 * to avoid error message box with "Exception in Transaction" message.
 * 
 * See {@link org.alfresco.repo.content.transform.PoiHssfContentTransformer} for pattern.
 * 
 * @author Arseny Kovalchuk
 * 
 */
public class TransformerInfoException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -4343331677825559617L;

    public TransformerInfoException(String msg)
    {
        super(msg);
    }

    public TransformerInfoException(String msg, Throwable err)
    {
        super(msg, err);
    } 
}
