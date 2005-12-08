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
package org.alfresco.service.cmr.rule;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Rule Service Exception Class
 * 
 * @author Roy Wetherall
 */
public class RuleServiceException extends AlfrescoRuntimeException
{
	/**
	 * Serial version UID 
	 */
	private static final long serialVersionUID = 3257571685241467958L;

	/**
	 * Construtor
	 * 
	 * @param message 	the message string
	 */
	public RuleServiceException(String message) 
	{
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message	the message string
	 * @param source	the source exception
	 */
	public RuleServiceException(String message, Throwable source) 
	{
		super(message, source);
	}
}
