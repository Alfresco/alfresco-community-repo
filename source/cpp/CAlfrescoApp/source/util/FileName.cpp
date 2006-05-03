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

#include "util\FileName.h"

using namespace Alfresco;
using namespace std;

//	Declare the Dos separator and NTFS stream separator strings

String& Alfresco::FileName::DosSeperator = String("\\");
String& Alfresco::FileName::NTFSStreamSeperator = String(":");

wchar_t Alfresco::FileName::DOS_SEPERATOR = L'\\';

/**
 * Build a path using the specified components
 * 
 * @param dev const String&
 * @param path const String&
 * @param fileName const String&
 * @param sep wchar_t
 * @return const String
 */
const String FileName::buildPath( const String& dev, const String& path, const String& fileName, wchar_t sep) {

	//  Build the path string

	String fullPath;

	//  Check for a device name

	if ( dev.isNotEmpty()) {

		//  Add the device name

		fullPath.append( dev);

		//  Check if the device name has a file separator

		if ( dev.length() > 0 && dev.charAt( dev.length() - 1) != sep)
			fullPath.append( sep);
	}

	//  Check for a path

	if ( path.isNotEmpty()) {

		//  Add the path

		if (fullPath.length() > 0
			&& (path.charAt(0) == sep || path.charAt(0) == DOS_SEPERATOR))
			fullPath.append( path.substring(1));
		else
			fullPath.append( path);

		//  Add a trailing separator, if required

		if (path.length() > 0
			&& path.charAt(path.length() - 1) != sep
			&& fileName.isNotEmpty())
			fullPath.append(sep);
	}

	//  Check for a file name

	if (fileName.isNotEmpty()) {

		//  Add the file name

		if ( fullPath.length() > 0 && ( fileName.charAt(0) == sep || fileName.charAt(0) == DOS_SEPERATOR))
			fullPath.append( fileName.substring(1));
		else
			fullPath.append( fileName);
	}

	//  Debug

	//  Debug.println ( "BuildPath: " + fullPath.toString ());

	//  Convert the file separator characters in the path if we are not using the normal
	//  DOS file separator character.

	if (sep != DOS_SEPERATOR)
		return convertSeperators( fullPath, sep);
	return fullPath;
}

/**
 * Check if a file name contains a stream name
 * 
 * @param fileName const String&
 * @return bool
 */
bool FileName::containsStreamName( const String& fileName) {

	//	Check if the path contains the stream name separator character

	if ( fileName.indexOf( NTFSStreamSeperator) != -1)
		return true;
	return false;
}

/**
 * Convert path separator characters
 * 
 * @param path const String&
 * @param sep wchar_t
 * @return const String
 */
const String FileName::convertSeperators( const String& path, wchar_t sep) {

	//  Check if the path contains any DOS separators

	if ( path.indexOf( DOS_SEPERATOR) == -1)
		return path;

	//  Convert DOS path separators to the specified separator

	String newPath;
	unsigned int idx = 0;

	while ( idx < path.length()) {

		//  Get the current character from the path and check if it is a DOS path
		//  separator character.

		wchar_t ch = path.charAt(idx++);
		if (ch == DOS_SEPERATOR)
			newPath.append(sep);
		else
			newPath.append(ch);
	}

	//  Return the new path string

	return newPath;
}

/**
 * Make a relative path
 * 
 * @param basePath const String&
 * @param fullPath const String&
 * @return const String
 */
const String FileName::makeRelativePath( const String& basePath, const String& fullPath) {

	//	Check if the base path is the root path

	if ( basePath.length() == 0 || basePath.equals( DosSeperator)) {

		//	Return the full path, strip any leading separator

		if ( fullPath.length() > 0 && fullPath.charAt(0) == DOS_SEPERATOR)
			return fullPath.substring(1);
		return fullPath;
	}

	//	Split the base and full paths into separate components

	StringList baseNames = splitAllPaths(basePath);
	StringList fullNames = splitAllPaths(fullPath);

	//	Check that the full path is actually within the base path tree

	if ( baseNames.numberOfStrings() > 0 && fullNames.numberOfStrings() > 0 &&
		   baseNames.getStringAt(0).equalsIgnoreCase(fullNames.getStringAt(0)) == false)
		return String();

	//	Match the path names

	unsigned int idx = 0;

	while ( idx < baseNames.numberOfStrings() && idx < fullNames.numberOfStrings() &&
		      baseNames.getStringAt(idx).equalsIgnoreCase(fullNames.getStringAt(idx)))
		idx++;

	//	Build the relative path

	String relPath(128);

	while ( idx < fullNames.numberOfStrings()) {
		relPath.append(fullNames.getStringAt(idx++));
		if ( idx < fullNames.numberOfStrings())
			relPath.append(DOS_SEPERATOR);
	}

	//	Return the relative path

	return relPath;
}

/**
 * Map an input path to a real path
 * 
 * @param base const String&
 * @param path const String&
 * @return const String
 */
const String FileName::mapPath(const String& base, const String& path) {
	return String();
}

/**
 * Normalize a path converting all directories to uppercase and keeping the file name as is
 * 
 * @param path const String&
 * @return const String
 */
const String FileName::normalizePath(const String& path) {

	//	Split the path into directories and file name, only uppercase the directories to normalize
	//	the path.

	String normPath = path;

	if ( path.length() > 3) {

		//	Split the path to separate the folders/file name

		int pos = path.lastIndexOf( DOS_SEPERATOR);
		if ( pos != -1) {

			//	Get the path and file name parts, normalize the path

			String pathPart = path.substring(0, pos).toUpperCase();
			String namePart = path.substring(pos);

			//	Rebuild the path string

			normPath =  pathPart;
			normPath += namePart;
		}
	}

	//	Return the normalized path

	return normPath;
}

/**
 * Remove the file name from the path
 * 
 * @param path const String&
 * @return const String
 */
const String FileName::removeFileName(const String& path) {

	//  Find the last path separator

	int pos = path.lastIndexOf(DOS_SEPERATOR);
	if (pos != -1)
		return path.substring(0, pos);

	//  Return an empty string, no path separators

	return "";
}

/**
 * Split the path into all the component directories and filename
 * 
 * @param path const String&
 * @return StringList
 */
StringList FileName::splitAllPaths(const String& path) {

	//	Check if the path is valid

	StringList paths;

	if ( path.length() == 0) {
		paths.addString( path);
		return paths;
	}

	//	Split the path

	return path.tokenize( DosSeperator);
}

/**
 * Split the path into separate directory path and file name strings
 * 
 * @param path const String&
 * @param sep wchar_t
 * @return StringList
 */
StringList FileName::splitPath( const String& path, wchar_t sep) {

	//  Create an array of strings to hold the path and file name strings

	StringList pathList;
	String path0, path1;

	//  Check if the path is valid

	if ( path.length() > 0) {

		//  Check if the path has a trailing separator, if so then there is no
		//  file name.

		int pos = path.lastIndexOf(sep);

		if (pos == -1 || pos == (path.length() - 1)) {

			//  Set the path string in the returned string array

			path0 = path;
		}
		else {

			//  Split the path into directory list and file name strings

			path1 = path.substring(pos + 1);

			if (pos == 0)
				path0 = path.substring(0, pos + 1);
			else
				path0 = path.substring(0, pos);
		}
	}

	//	Set the path strings

	pathList.addString( path0);
	pathList.addString( path1);

	//  Return the path strings

	return pathList;
}

/**
 * Split a path string into directory path, file name and stream name components
 * 
 * @param path const String&
 * @return StringList
 */
StringList FileName::splitPathStream( const String& path) {

	//	Allocate the return list

	StringList pathList;

	//	Split the path into directory path and file/stream name

	pathList = FileName::splitPath(path, DOS_SEPERATOR);

	if ( pathList[1].length() == 0)
		return pathList;

	//	Split the file name into file and stream names

	int pos = pathList[1].indexOf( NTFSStreamSeperator);

	if ( pos != -1) {

		//	Split the file/stream name

		pathList[2] = pathList[1].substring(pos);
		pathList[1] = pathList[1].substring(0,pos);
	}

	//	Return the path components list

	return pathList;
}
