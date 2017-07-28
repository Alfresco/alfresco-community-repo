/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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