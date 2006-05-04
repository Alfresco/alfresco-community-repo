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
