/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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

#include <time.h>
#include "util\Debug.h"

using namespace std;
using namespace Alfresco;

// Global debug output stream

ofstream Debug::_debugOut;

/**
 * Open the debug output file
 *
 * @param logName const char*
 * @param append bool
 */
void Debug::openLog(const char *logName, bool append) {

	// Check if the log is already open

	if ( Debug::hasOutputStream())
		Debug::closeLog();

	// Open the debug log file

	unsigned int openMode = append ? ios::app : ios::out;
	_debugOut.open( logName, openMode);
}

/**
 * Close the debug output file
 */
void Debug::closeLog( void) {

	// Close the debug log

	if ( Debug::hasOutputStream()) {

		// Close the debug log file

		_debugOut.close();
	}
}

/**
 * Output the current date/time to the debug log
 */
void Debug::timeStamp( void) {

	if ( Debug::hasOutputStream()) {

		// Get the time in seconds and convert to a structure

		char timeBuf[32];
		__time32_t timeNow;
		struct tm timeTm;

		_time32( &timeNow);
		_localtime32_s( &timeTm, &timeNow);

		sprintf_s( timeBuf, sizeof( timeBuf), "%02d/%02d/%04d %02d:%02d:%02d ",
			timeTm.tm_mday, timeTm.tm_mon, timeTm.tm_year + 1900, timeTm.tm_hour, timeTm.tm_min, timeTm.tm_sec);
		Debug::getOutputStream() << timeBuf;
	}
}

