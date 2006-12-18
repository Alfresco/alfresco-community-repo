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

