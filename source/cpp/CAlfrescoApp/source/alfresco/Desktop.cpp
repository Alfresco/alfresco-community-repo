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

#include "alfresco\Desktop.hpp"
#include "util\Exception.h"
#include "util\Integer.h"

using namespace Alfresco;
using namespace std;

// Define exceptions

EXCEPTION_CLASS(Alfresco, DesktopActionException);

/**
 * Class constructor
 * 
 * @param typ	Target type
 * @param path	Target path/id
 */
DesktopTarget::DesktopTarget(int typ, String& path) {

	// Set the target type and path

	m_type = typ;
	m_target = path;
}

/**
 * Return the target type as a string
 *
 * @return const String
 */
const String DesktopTarget::getTypeAsString() const {

	String typStr;

	switch ( isType()) {
		case TargetFile:
			typStr = L"File";
			break;
		case TargetFolder:
			typStr = L"Folder";
			break;
		case TargetCopiedFile:
			typStr = L"File Copy";
			break;
		case TargetCopiedFolder:
			typStr = L"Folder Copy";
			break;
		case TargetNodeRef:
			typStr = L"NodeRef";
			break;
	}

	return typStr;
}

/**
 * Return the target details as a string
 *
 * @return const String
 */
const String DesktopTarget::toString( void) const {

	String str = L"[";

	str.append(getTypeAsString());
	str.append(L":");
	str.append(getTarget());
	str.append(L"]");

	return str;
}

/**
 * Equality operator
 *
 * @param target const DekstopTarget&
 * @return bool
 */
bool DesktopTarget::operator==( const DesktopTarget& target) {
	if ( isType() == target.isType() &&
		getTarget().equals(target.getTarget()))
		return true;
	return false;
}

/**
 * Less than operator
 *
 * @param target const DesktopTarget&
 * @return bool
 */
bool DesktopTarget::operator<( const DesktopTarget& target) {
	if ( isType() == target.isType())
		return getTarget() < target.getTarget();
	else
		return isType() < target.isType();
}

/**
 * Return the required desktop target
 *
 * @param idx const unsigned int
 * @return const DesktopTarget*
 */
const DesktopTarget* DesktopParams::getTarget(const unsigned int idx) const {

	// Range check the index

	if ( idx > m_list.size())
		return NULL;

	// Return the required target

	return m_list[idx];
}

/**
 * Return the desktop parameters as a string
 *
 * @return const String
 */
const String DesktopParams::toString(void) const {

	String str = L"[";

	str.append(L"Targets=");
	str.append((unsigned int)numberOfTargets());
	str.append(L"]");

	return str;
}

/**
 * Class constructor
 *
 * @param sts const unsigned int
 * @param msg const wchar_t*
 */
DesktopResponse::DesktopResponse(const unsigned int sts, const wchar_t* msg) {
	m_status = sts;
	if ( msg != NULL)
		m_statusMsg = msg;
}

/**
 * Assignment operator
 *
 * @param response const DesktopResponse&
 * @return DesktopResponse&
 */
DesktopResponse& DesktopResponse::operator=( const DesktopResponse& response) {
	m_status = response.getStatus();
	m_statusMsg = response.getStatusMessage();

	return *this;
}