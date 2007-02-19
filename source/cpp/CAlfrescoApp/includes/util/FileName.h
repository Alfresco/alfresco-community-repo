/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

#ifndef _FileName_H
#define _FileName_H

//	Includes

#include "util\String.h"

//	Classes defined in this header file

namespace Alfresco {
	class FileName;
}

/**
 * File Naming Utility Class
 * 
 * Contains various utility methods for building and splitting file paths.
 */
class Alfresco::FileName {
public:
	//	Build a path using the specified components

	static const String buildPath( const String& dev, const String& path, const String& fileName, wchar_t sep = L'\\');

	//	Check if a file name contains a stream name

	static bool containsStreamName( const String& fileName);

	//	Convert path separator characters

	static const String convertSeperators( const String& path, wchar_t sep);

	//	Make a relative path

	static const String makeRelativePath( const String& basePath, const String& fullPath);

	//	Map an input path to a real path

	static const String mapPath(const String& base, const String& path);

	//	Normalize a path converting all directories to uppercase and keeping the file name as is

	static const String normalizePath(const String& path);

	//	Remove the file name from the path

	static const String removeFileName(const String& path);

	//	Split the path into all the component directories and filename

	static StringList splitAllPaths(const String& path);

	//	Split the path into separate directory path and file name strings

	static StringList splitPath( const String& path, wchar_t sep = L'\\');

	//	Split a path string into directory path, file name and stream name components

	static StringList splitPathStream( const String& path);

public:
	//	Constant values

	static String& DosSeperator;
	static String& NTFSStreamSeperator;

	static wchar_t DOS_SEPERATOR;

private:
	//	Hide constructors, static only class

	FileName( void) {};
	FileName( const FileName& fname) {};
};

#endif
