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