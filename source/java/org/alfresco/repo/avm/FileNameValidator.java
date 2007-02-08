/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
