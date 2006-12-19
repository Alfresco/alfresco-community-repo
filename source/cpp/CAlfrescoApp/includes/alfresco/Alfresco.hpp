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

#ifndef _Alfresco_H
#define _Alfresco_H

//	Includes

#include <windows.h>
#include <WinIOCtl.h>

#include <vector>
#include <algorithm>

#include "util\Exception.h"
#include "util\String.h"
#include "util\DataBuffer.h"

#include "alfresco\Desktop.hpp"

//	Classes defined in this header file

namespace Alfresco {
	class AlfrescoInterface;
	class AlfrescoFileInfo;
	class AlfrescoFileInfoList;
	class AlfrescoActionInfo;
	typedef std::auto_ptr<AlfrescoFileInfo> PTR_AlfrescoFileInfo;
	typedef std::auto_ptr<AlfrescoActionInfo> PTR_AlfrescoActionInfo;
}

// Constants

namespace Alfresco {

	// Alfresco I/O control codes

	#define FSCTL_ALFRESCO_PROBE			CTL_CODE(FILE_DEVICE_FILE_SYSTEM, 0x800, METHOD_BUFFERED, FILE_ANY_ACCESS)
	#define FSCTL_ALFRESCO_FILESTS 			CTL_CODE(FILE_DEVICE_FILE_SYSTEM, 0x801, METHOD_BUFFERED, FILE_ANY_ACCESS)
	// Version 1 FSCTL_ALFRESCO_CHECKOUT - 0x802
	// Version 1 FSCTL_ALFRESCO_CHECKIN  - 0x803
	#define FSCTL_ALFRESCO_GETACTIONINFO	CTL_CODE(FILE_DEVICE_FILE_SYSTEM, 0x804, METHOD_BUFFERED, FILE_WRITE_DATA)
	#define FSCTL_ALFRESCO_RUNACTION		CTL_CODE(FILE_DEVICE_FILE_SYSTEM, 0x805, METHOD_BUFFERED, FILE_WRITE_DATA)

	// Request signature bytes

	#define IOSignature "ALFRESCO"
	#define IOSignatureLen 8

	// Path prefixes/components

	#define UNCPathPrefix L"\\\\"
	#define PathSeperator L"\\"

	// I/O control status codes

	#define StsSuccess          0

	#define StsError            1
	#define StsFileNotFound     2
	#define StsAccessDenied     3
	#define StsBadParameter		4
	#define StsNotWorkingCopy	5
	#define StsNoSuchAction		6
	#define StsLaunchURL		7
	#define StsCommandLine		8

	// Boolean field values

	#define True                1
	#define False               0

	// File status field values
	//
	// Node type

	#define TypeFile            0
	#define TypeFolder          1

	// Lock status

	#define LockNone            0
	#define LockRead            1
	#define LockWrite           2

	// Desktop action attributes

	#define AttrTargetFiles		0x0001
	#define AttrTargetFolders	0x0002
	#define AttrClientFiles		0x0004
	#define AttrClientFolders	0x0008
	#define AttrAlfrescoFiles	0x0010
	#define AttrAlfrescoFolders 0x0020
	#define	AttrMultiplePaths	0x0040
	#define AttrAllowNoParams   0x0080

	#define AttrAnyFiles		(AttrTargetFiles + AttrClientFiles + AttrAlfrescoFiles)
	#define AttrAnyFolders		(AttrTargetFolders + AttrClientFolders + AttrAlfrescoFolders)
	#define AttrAnyFilesFolders (AttrAnyFiles + AttrAnyFolders)

	// Desktop action pre-processing actions

	#define PreCopyToTarget			0x0001
	#define	PreConfirmAction		0x0002
	#define PreLocalToWorkingCopy	0x0004
}

// Define Alfresco interface exceptions

DEFINE_EXCEPTION(Alfresco, BadInterfaceException);

/**
* Alfresco API Class
* 
* Provides the interface to an Alfresco CIFS server to perform Alfresco specific functions
* not available via the normal file I/O functions.
*/
class Alfresco::AlfrescoInterface {
public:
	//	Class constructors

	AlfrescoInterface(String& path);

	// Class destructor

	~AlfrescoInterface();

	// Return the UNC path and root path

	inline const String& getUNCPath( void) const { return m_uncPath; }
	inline const String& getRootPath( void) const { return m_rootPath; }

	// Check if the application is running from a mapped drive, return the drive path

	inline bool isMappedDrive( void) const { return m_mappedDrive.length() > 0 ? true : false; }
	inline const String& getDrivePath( void) const { return m_mappedDrive; }

	// Check if the path is on an Alfresco CIFS server

	bool isAlfrescoFolder( void);

	// Return the protocol version of the server

	inline const unsigned int isProtocolVersion( void) const { return m_protocolVersion; }

	// Return the Alfresco file information for a file/folder within the current folder

	PTR_AlfrescoFileInfo getFileInformation(const wchar_t* fileName);

	// Get action information, map the executable name to a server action

	AlfrescoActionInfo getActionInformation(const wchar_t* exeName);

	// Run a desktop action and return the server response

	DesktopResponse runAction(AlfrescoActionInfo& action, DesktopParams& params);

	// Set the root path to be used as the working directory

	bool setRootPath( const wchar_t* rootPath);

private:
	// Send an I/O control request, receive and validate the response

	void sendIOControl( const unsigned int ctlCode, DataBuffer& reqbuf, DataBuffer& respbuf);

private:
	// Hide the copy constructor

	AlfrescoInterface(const AlfrescoInterface& alfresco) {};

private:
	// Instance variables
	//
	// UNC path and root path

	String m_uncPath;
	String m_rootPath;

	// Local path letter if running from a mapped drive

	String m_mappedDrive;

	// Handle to folder

	HANDLE m_handle;

	// Protocol version

	unsigned int m_protocolVersion;
};

/**
 * Alfresco File Information Class
 *
 * Contains Alfresco specific file information for a file/folder on an Alfresco CIFS server.
 */
class Alfresco::AlfrescoFileInfo {
public:
	// Class constructor

	AlfrescoFileInfo( const wchar_t* fName);

	// Return the file/folder name

	inline const String& getName( void) const { return m_name; }

	// Determine if the file is a file or folder

	inline unsigned int isType( void) const { return m_type; }

	// Return the working copy status, owner, copied from

	inline bool isWorkingCopy( void) const { return m_workingCopy; }
	inline const String& getCopyOwner( void) const { return m_workOwner; }
	inline const String& getCopiedFrom( void) const { return m_copiedFrom; }

	// Return the lock status

	inline unsigned int getLockType( void) const { return m_lockType; }
	inline const String& getLockOwner( void) const { return m_lockOwner; }

	// Return the content details

	inline bool hasContent( void) const { return m_hasContent; }
	inline LONG64 getContentLength( void) const { return m_contentLen; }
	inline const String& getContentType( void) const { return m_contentMimeType; }

	// Set the file/folder type

	inline void setType( unsigned int typ) { m_type = typ; }

	// Set the working copy owner and copied from

	void setWorkingCopy( const wchar_t* owner, const wchar_t* copiedFrom);

	// Set the lock type and owner

	void setLockType( unsigned int typ, const wchar_t* owner = L"");

	// Set the content length and type

	void setContent( LONG64 siz, const wchar_t* mimeType);

	// Operators

	bool operator==( const AlfrescoFileInfo& finfo);
	bool operator<( const AlfrescoFileInfo& finfo);

private:
	// Hide the copy constructor

	AlfrescoFileInfo(const AlfrescoFileInfo& aInfo) {};

private:
	// Instance variables
	//
	// File/folder name

	String m_name;
	unsigned int m_type;

	// Working copy flag, owner and copied from

	bool m_workingCopy;
	String m_workOwner;
	String m_copiedFrom;

	// Lock type and owner

	unsigned int m_lockType;
	String m_lockOwner;

	// Content mime-type and length

	bool m_hasContent;
	LONG64 m_contentLen;
	String m_contentMimeType;
};

/**
 * Alfresco File Info List Class
 */
class Alfresco::AlfrescoFileInfoList {
public:
	//	Class constructor

	AlfrescoFileInfoList( void) {};

	//	Add a file information object to the list

	inline void addInfo( AlfrescoFileInfo* pInfo) { m_list.push_back( pInfo); }
	inline void addInfo( PTR_AlfrescoFileInfo pInfo) { if ( pInfo.get() != NULL) m_list.push_back( pInfo.release()); }

	//	Return the number of objects in the list

	inline size_t size( void) const { return m_list.size(); }

	//	Return the specified file information

	inline const AlfrescoFileInfo* getInfoAt( unsigned int idx) const { return m_list[idx]; }

	//	Assignment operator

	inline AlfrescoFileInfo*& operator[] ( const unsigned int idx) { return m_list[idx]; }

	//	Remove all objects from the list

	inline void clear( void) { for ( unsigned int i = 0; i < m_list.size(); delete m_list[i++]); m_list.clear(); }

	// Return the vector

	std::vector<AlfrescoFileInfo*> getList( void) { return m_list; }

private:
	//	Instance variables
	//
	//	List of file information objects

	std::vector<AlfrescoFileInfo*>	m_list;
};

/**
 * Alfresco Action Info Class
 */
class Alfresco::AlfrescoActionInfo {
public:
	//  Default constructor

	AlfrescoActionInfo(void);

	//	Class constructor

	AlfrescoActionInfo( const String& name, const unsigned int attr, const unsigned int preActions);

	// Return the action name, pseudo file name

	inline const String& getName(void) const { return m_name; }
	inline const String& getPseudoName(void) const { return m_pseudoName; }

	// Return the action attributes, action pre-processing flags

	inline unsigned int getAttributes(void) const { return m_attributes; }
	inline unsigned int getPreProcessActions(void) const { return m_clientPreActions; }

	// Check if the action has the specifed attribute/pre-processing action

	inline bool hasAttribute(const unsigned int attr) const { return (m_attributes & attr) != 0 ? true : false; }
	inline bool hasPreProcessAction(const unsigned int pre) const { return (m_clientPreActions & pre) != 0 ? true : false; }

	// Check if the confirmation message is valid, return the confirmation message

	inline bool hasConfirmationMessage(void) const { return m_confirmMsg.length() > 0 ? true : false; }
	inline const String& getConfirmationMessage(void) const { return m_confirmMsg; }

	// Check if the action supports file or folder paths

	inline bool supportsFiles(void) const { return hasAttribute(AttrTargetFiles+AttrClientFiles+AttrAlfrescoFiles); }
	inline bool supportsFolders(void) const { return hasAttribute(AttrTargetFolders+AttrClientFolders+AttrAlfrescoFolders); }

	// Check if the action allows no parameters

	inline bool allowsNoParameters(void) const { return hasAttribute(AttrAllowNoParams) || hasAttribute(AttrAnyFilesFolders) == false; }

	// Set the action name, pseudo name, set the confirmation message

	inline void setName(const String& name) { m_name = name; }
	inline void setPseudoName(const String& pseudo) { m_pseudoName = pseudo; }
	inline void setConfirmationMessage(const String& msg) { m_confirmMsg = msg; }

	// Set the action attributes and pre-processing actions

	inline void setAttributes(const unsigned int attr) { m_attributes = attr; }
	inline void setPreProcessActions(const unsigned int pre) { m_clientPreActions = pre; }

	// Return the action information as a string

	const String toString(void) const;

	// Assignment operator

	AlfrescoActionInfo& operator=( const AlfrescoActionInfo& actionInfo);

private:
	// Instance variables
	//
	// Action name

	String m_name;

	// Pseudo file name

	String m_pseudoName;

	// Action attributes and pre-processing flags

	unsigned int m_attributes;
	unsigned int m_clientPreActions;

	// Action confirmation message

	String m_confirmMsg;
};

#endif
