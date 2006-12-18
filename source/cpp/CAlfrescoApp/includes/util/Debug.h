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

#ifndef _Debug_H
#define _Debug_H

//	Includes

#include "util\String.h"
#include <ostream>
#include <fstream>

//	Classes defined in this header file

namespace Alfresco {
	class Debug;

	// Macro to access to the debug output stream

	#define HAS_DEBUG Debug::hasOutputStream() == true
	#define DBGOUT if ( Debug::hasOutputStream()) Debug::getOutputStream()
	#define TIMESTAMP Debug::timeStamp();
	#define DBGOUT_TS TIMESTAMP DBGOUT
}

/**
 * Debug Logging Class
 * 
 * Outputs debugging information to a file on the local filesystem.
 */
class Alfresco::Debug {
public:

	// Open/close the debug log

	static void openLog( const char* logName, bool append = true);
	static void closeLog( void);

	// Check if the output stream is valid, return the output stream

	static bool hasOutputStream( void) { return _debugOut.is_open() ? true : false; }
	static std::ofstream& getOutputStream( void) { return _debugOut; }

	// Output a timestamp to the debug log

	static void timeStamp( void);

private:
	// Debug output log file

	static std::ofstream _debugOut;

private:
	// Hide constructors, static only class

	Debug( void) {};
	Debug( const Debug& dbg) {};
};

#endif