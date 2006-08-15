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