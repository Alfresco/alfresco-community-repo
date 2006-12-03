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

import java.util.regex.Pattern;

/**
 * Static checker for valid file names.
 * @author britt
 */
public class FileNameValidator
{
    /**
     * The bad file name pattern.
     */
    private static String fgBadPattern = ".*[\"\\*\\\\><\\?/:\\|\\xA3\\xAC%&;]+.*";
    
    /**
     * The compiled regex.
     */
    private static Pattern fgPattern = Pattern.compile(fgBadPattern);
    
    public static boolean IsValid(String name)
    {
        return !fgPattern.matcher(name).matches();
    }
}
