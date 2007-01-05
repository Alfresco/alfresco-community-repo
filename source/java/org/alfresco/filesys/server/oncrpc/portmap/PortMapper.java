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
package org.alfresco.filesys.server.oncrpc.portmap;

/**
 * PortMapper RPC Service Constants Class
 * 
 * @author GKSpencer
 */
public class PortMapper {

	// Default port mapper port

	public static final int DefaultPort = 111;

	// Program and version id

	public static final int ProgramId 	= 100000;
	public static final int VersionId 	= 2;

	// RPC procedure ids

	public static final int ProcNull 	= 0;
	public static final int ProcSet 	= 1;
	public static final int ProcUnSet 	= 2;
	public static final int ProcGetPort = 3;
	public static final int ProcDump 	= 4;
	public static final int ProcMax 	= 4;

	// RPC procedure names

	private static final String[] _procNames = { "Null", "Set", "UnSet",
			"GetPort", "Dump" };

	/**
	 * Return a procedure id as a name
	 * 
	 * @param id
	 *            int
	 * @return String
	 */
	public final static String getProcedureName(int id) {
		if (id < 0 || id > ProcMax)
			return null;
		return _procNames[id];
	}
}
