/*
* Copyright (C) 2005 Alfresco, Inc.
*
* Licensed under the Alfresco Network License. You may obtain a
* copy of the License at
*
*   http://www.alfrescosoftware.com/legal/
*
* Please view the license relevant to your network subscription.
*
* BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
* READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
* YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
* ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
* THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
* AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
* TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
* BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
* HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
* SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
* TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
* CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
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
