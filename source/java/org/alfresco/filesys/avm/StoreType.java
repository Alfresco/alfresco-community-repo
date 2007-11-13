/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.filesys.avm;

/**
 * Store Types Class
 * 
 * @author gkspencer
 */
public class StoreType {

	// Store types
	
	public static final int Normal			= 0;
	
	// Web project store types
	
	public static final int WebAuthorMain		= 1;
	public static final int WebAuthorPreview	= 2;
	public static final int WebStagingPreview	= 3;
	public static final int WebStagingMain      = 4;
	
	// Store type strings
	
	private static final String[] _types = { "Normal", "AuthorMain", "AuthorPreview", "StagingPreview", "StagingMain" };
	
	/**
	 * Return a store type as a string
	 * 
	 * @param typ int
	 * @return String
	 */
	public final static String asString( int typ)
	{
		if ( typ < 0 || typ > _types.length)
			return "Invalid";
		return _types[ typ];
	}
}
