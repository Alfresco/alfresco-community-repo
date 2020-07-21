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

#include "util\System.h"

#include <sys\timeb.h>

using namespace Alfresco;

/**
 * Return the current system time in milliseconds since Jan 1 1970
 * 
 * @return DATETIME
 */
DATETIME System::currentTimeMillis( void) {

	//	Get the current system time

	struct __timeb64 timeNow;
	_ftime64( &timeNow);

	//	Build the milliseconds time

	DATETIME timeNowMillis = ( timeNow.time * 1000L) + (DATETIME) timeNow.millitm;
	return timeNowMillis;
}