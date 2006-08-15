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

#ifndef _Desktop_H
#define _Desktop_H

//	Includes

#include <windows.h>

#include <vector>
#include <algorithm>
#include "util\Exception.h"
#include "util\String.h"
#include "util\DataBuffer.h"

//	Classes defined in this header file

namespace Alfresco {
	class DesktopTarget;
	class DesktopParams;
	class DesktopResponse;

	typedef std::auto_ptr<DesktopTarget> PTR_DesktopTarget;
	typedef std::auto_ptr<DesktopParams> PTR_DesktopParams;
	typedef std::auto_ptr<DesktopResponse> PTR_DesktopResponse;
}

// Constants

namespace Alfresco {

	// Desktop target types

	#define TargetFile			0
	#define TargetFolder		1
	#define TargetCopiedFile	2
	#define TargetCopiedFolder	3
	#define TargetNodeRef		4
}

// Define desktop action exceptions

DEFINE_EXCEPTION(Alfresco, DesktopActionException);

/**
 * Desktop Target Class
 * 
 * Contains the details of a target for a desktop action.
 */
class Alfresco::DesktopTarget {
public:
	//	Class constructors

	DesktopTarget(int typ, String& path);

	// Class destructor

	~DesktopTarget();

	// Return the target type, target path/id

	inline unsigned int isType(void) const { return m_type; }
	inline const String& getTarget(void) const { return m_target; }

	// Return the target type as a string

	const String getTypeAsString( void) const;

	// Return the target details as a string

	const String toString( void) const;

	// Operators

	bool operator==( const DesktopTarget& target);
	bool operator<( const DesktopTarget& target);

private:
	// Hide the copy constructor

	DesktopTarget(const DesktopTarget& target) {};

private:
	// Instance variables
	//
	// Target type and path/id

	unsigned int m_type;
	String m_target;
};

/**
 * Desktop Params Class
 * 
 * Contains the parameters for a desktop action request.
 */
class Alfresco::DesktopParams {
public:
	//	Class constructors

	DesktopParams(void) {}

	// Return the number of targets

	inline size_t numberOfTargets(void) const { return m_list.size(); }

	// Return a target from the list

	const DesktopTarget* getTarget(const unsigned int idx) const;

	// Add a desktop target

	inline void addTarget(DesktopTarget* pTarget) { m_list.push_back(pTarget); }

	// Clear the target list

	inline void clearTargets( void) { m_list.clear(); }

	// Return the desktop parameters as a string

	const String toString(void) const;

private:
	// Instance variables
	//
	// List of file/folder/node targets for the action

	std::vector<DesktopTarget*>	m_list;
};

/**
 * Desktop Response Class
 *
 * Contains the result of calling a server side desktop action.
 */
class Alfresco::DesktopResponse {
public:
	// class constructors

	DesktopResponse( const unsigned int sts, const wchar_t* msg = NULL);

	// Return the status code

	inline unsigned int getStatus( void) const { return m_status; }

	// Check if there is a status message, return the status message

	inline bool hasStatusMessage(void) const { return m_statusMsg.length() > 0; }
	inline const String& getStatusMessage(void) const { return m_statusMsg; }

	// Assignment operator

	DesktopResponse& operator=( const DesktopResponse& response);

private:
	// Instance variables
	//
	// Status code and message

	unsigned int m_status;
	String m_statusMsg;
};

#endif
