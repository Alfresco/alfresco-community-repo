/**
 * 
 */
package org.alfresco.repo.googledocs;

import java.text.MessageFormat;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roy Wetherall
 */
public class GoolgeDocsUnsupportedMimetypeException extends AlfrescoRuntimeException 
{
	private static final long serialVersionUID = 7505645425492536907L;
	
	private static Log logger = LogFactory.getLog(GoolgeDocsUnsupportedMimetypeException.class);

	/**
	 * @param msgId
	 */
	public GoolgeDocsUnsupportedMimetypeException(String msgId) 
	{
		super(msgId);
	}

	/**
	 * @param msgId
	 * @param msgParams
	 */
	public GoolgeDocsUnsupportedMimetypeException(String msgId, Object[] msgParams) 
	{
		super(msgId, msgParams);
	}

	/**
	 * @param msgId
	 * @param cause
	 */
	public GoolgeDocsUnsupportedMimetypeException(String msgId, Throwable cause) 
	{
		super(msgId, cause);
	}

	/**
	 * @param msgId
	 * @param msgParams
	 * @param cause
	 */
	public GoolgeDocsUnsupportedMimetypeException(String msgId, Object[] msgParams, Throwable cause) 
	{
		super(msgId, msgParams, cause);
	}
	
	/**
	 * @param nodeRef	node reference
	 * @param contentProp	content property
	 * @param mimetype	mimetype
	 */
	public GoolgeDocsUnsupportedMimetypeException(NodeRef nodeRef, QName contentProp, String mimetype)
	{
		// TODO I18N
		this(MessageFormat.format("The mimetype {0} is not supported by Google Docs", mimetype));
		
		if (logger.isDebugEnabled() == true)
		{
			logger.debug(MessageFormat.format("The mimetype {0} is not supported by Google Docs. (nodeRef = {1}, contentProp = {2})", mimetype, nodeRef.toString(), contentProp.toString()));
		}
	}

}
