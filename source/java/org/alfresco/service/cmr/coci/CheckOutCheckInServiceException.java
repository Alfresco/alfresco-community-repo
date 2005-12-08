/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.coci;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Version opertaions service exception class
 * 
 * @author Roy Wetherall
 */
public class CheckOutCheckInServiceException extends AlfrescoRuntimeException 
{
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 3258410621186618417L;

	/**
	 * Constructor
	 * 
	 * @param message  the error message
	 */
	public CheckOutCheckInServiceException(String message) 
	{
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message		the error message	
	 * @param throwable		the cause of the exeption
	 */
	public CheckOutCheckInServiceException(String message, Throwable throwable)
	{
		super(message, throwable);
	}
}
