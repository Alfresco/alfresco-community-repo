/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

/**
 * Utility class with static methods to convert between
 * a NodeRef style id string and an ordinary AVM path.
 * @author britt
 */
public class AVMPathConverter
{
    /**
     * Converts to a string to stuff into a NodeRef. Version = 3 
     * of main:/snot/efluvium/biggle would be converted into 
     * 3/main/snot/efluvium/biggle
     * @param version The version id.
     * @param avmPath The full AVM path.
     * @return A mangled string appropriate for stuffing into a NodeRef.
     */
    public static String toNodeRefStyle(int version, String avmPath)
    {
        String [] pathParts = avmPath.split(":");
        if (pathParts.length != 2)
        {
            throw new AVMException("Malformed path.");
        }
        return version + "/" + pathParts[0] + pathParts[1];
    }
    
    /**
     * Convert from a NodeRef packed form to a version number
     * and standard AVM path.
     * @param nrID The NodeRef packed for of an AVM path.
     * @return The version number and the standard AVM path.
     */
    public static Object[] toAVMStyle(String nrID)
    {
        Object [] result = new Object[2];
        String [] pathParts = nrID.split("/");
        result[0] = new Integer(pathParts[0]);
        StringBuilder builder = new StringBuilder();
        builder.append(pathParts[1]);
        builder.append(':');
        for (int i = 2; i < pathParts.length; i++)
        {
            builder.append('/');
            builder.append(pathParts[i]);
        }
        if (pathParts.length == 2)
        {
            builder.append('/');
        }
        result[1] = builder.toString();
        return result;
    }
}
