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

#include "stdafx.h"
#include "CAlfrescoApp.h"
#include "CAlfrescoAppDlg.h"
#include "FileStatusDialog.h"

#include <stdlib.h>

#include "util\String.h"
#include "util\DataBuffer.h"
#include "util\FileName.h"

#include <shellapi.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

using namespace std;
using namespace Alfresco;

// CCAlfrescoAppApp

BEGIN_MESSAGE_MAP(CCAlfrescoAppApp, CWinApp)
	ON_COMMAND(ID_HELP, CWinApp::OnHelp)
END_MESSAGE_MAP()


// CCAlfrescoAppApp construction

CCAlfrescoAppApp::CCAlfrescoAppApp()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance
}


// The one and only CCAlfrescoAppApp object

CCAlfrescoAppApp theApp;


// CCAlfrescoAppApp initialization

BOOL CCAlfrescoAppApp::InitInstance()
{
	// InitCommonControls() is required on Windows XP if an application
	// manifest specifies use of ComCtl32.dll version 6 or later to enable
	// visual styles.  Otherwise, any window creation will fail.

	InitCommonControls();
	CWinApp::InitInstance();
	AfxEnableControlContainer();

	// Get the application path

	String appPath = __wargv[0];

	int pos = appPath.lastIndexOf(PathSeperator);

	if ( pos < 0) {
		AfxMessageBox( L"Invalid application path", MB_OK | MB_ICONSTOP);
		return 1;
	}

	// Get the path to the folder containing the application

	String folderPath = appPath.substring(0, pos);

	// Create the Alfresco interface

	AlfrescoInterface alfresco(folderPath);
	if ( alfresco.isAlfrescoFolder()) {

		try {

			// If there are no file paths on the command line then display a status page for the files
			// in the Alfresco folder

			if ( __argc == 1) {

				// Display status for the files in the Alfresco folder

				doFolderStatus( alfresco);
			}
			else {

				// Build a list of the file names

				StringList fileList;

				for ( int i = 1; i < __argc; i++)
					fileList.addString( String(__wargv[i]));

				// Process the file list and check in or out each file

				doCheckInOut( alfresco, fileList);
			}
		}
		catch (Exception ex) {
			CString msg;
			msg.FormatMessage( L"Exception occurred\n\n%1", ex.getMessage().data());
			AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
		}
	}
	else {
		AfxMessageBox( L"Not a valid Alfresco CIFS folder", MB_OK | MB_ICONSTOP);
		return 1;
	}

	// Run the main dialog
/**
	CCAlfrescoAppDlg dlg;
	m_pMainWnd = &dlg;
	INT_PTR nResponse = dlg.DoModal();
	if (nResponse == IDOK)
	{
		// TODO: Place code here to handle when the dialog is
		//  dismissed with OK
	}
	else if (nResponse == IDCANCEL)
	{
		// TODO: Place code here to handle when the dialog is
		//  dismissed with Cancel
	}
**/

	// Since the dialog has been closed, return FALSE so that we exit the
	//  application, rather than start the application's message pump.
	return FALSE;
}

/**
 * Display file status of the files in the target Alfresco folder
 *
 * @param AlfrescoInterface& alfresco
 * @param const wchar_t* fileSpec
 * @return bool
 */
bool CCAlfrescoAppApp::doFolderStatus( AlfrescoInterface& alfresco, const wchar_t* fileSpec) {

	// Get the base UNC path

	String uncPath = alfresco.getUNCPath();
	uncPath.append(PathSeperator);

	// Search the Alfresco folder

	WIN32_FIND_DATA findData;
	String searchPath = uncPath;
	searchPath.append( fileSpec);

	bool sts = false;
	HANDLE fHandle = FindFirstFile( searchPath, &findData);
	AlfrescoFileInfoList fileList;

	if ( fHandle != INVALID_HANDLE_VALUE) {

		// Loop until all files have been returned

		PTR_AlfrescoFileInfo pFileInfo;
		sts = true;

		while ( fHandle != INVALID_HANDLE_VALUE) {

			// Get the file name, ignore the '.' and '..' files

			String fName = findData.cFileName;

			if ( fName.equals(L".") || fName.equals(L"..")) {

				// Get the next file/folder name in the search

				if ( FindNextFile( fHandle, &findData) == 0)
					fHandle = INVALID_HANDLE_VALUE;
				continue;
			}

			// Get the file information for the current file folder

			pFileInfo = alfresco.getFileInformation( findData.cFileName);

			if ( pFileInfo.get() != NULL) {

				// Add the file to the list

				fileList.addInfo( pFileInfo);
			}

			// Get the next file/folder name in the search

			if ( FindNextFile( fHandle, &findData) == 0)
				fHandle = INVALID_HANDLE_VALUE;
		}
	}

	// Display the file status dialog if there are files to display

	if ( fileList.size() > 0) {

		// Display the file status dialog

		CFileStatusDialog dlg( fileList);
		dlg.DoModal();
	}
	else {
		CString msg;
		msg.FormatMessage( L"No files found in %1", uncPath.data());
		AfxMessageBox( msg, MB_OK | MB_ICONINFORMATION);
	}

	// Return status

	return sts;
}

/**
 * Process the list of files and check in or out each file
 *
 * @param alfresco AlfrescoInterface&
 * @param files StringList&
 */
bool CCAlfrescoAppApp::doCheckInOut( AlfrescoInterface& alfresco, StringList& files) {

	// Process the list of files and either check in the file if it is a working copy or check out
	// the file

	for ( unsigned int i = 0; i < files.numberOfStrings(); i++) {

		// Get the current file name

		String curFile = files.getStringAt( i);

		// Check if the path is on an Alfresco mapped drive

		if ( alfresco.isMappedDrive() && curFile.startsWithIgnoreCase( alfresco.getDrivePath())) {

			// Convert the path to a UNC path

			String uncPath = alfresco.getRootPath();
			uncPath.append( curFile.substring(2));

			curFile = uncPath;
		}

		// Check that the path is to a file

		bool copyFile = false;

		DWORD attr = GetFileAttributes( curFile);
		if ( attr != INVALID_FILE_ATTRIBUTES && (attr & FILE_ATTRIBUTE_DIRECTORY) == 0) {

			// Get the file name from the path

			StringList nameParts = FileName::splitPath( curFile);
			String curName = nameParts.getStringAt( 1);

			// Get the Alfresco file status information

			PTR_AlfrescoFileInfo pFileInfo = alfresco.getFileInformation( curName);

			// If the path is to a file that is not on the Alfresco share the file will need to be copied,
			// after checking the status of a matching file in the Alfresco folder

			if ( curFile.length() >= 3 && curFile.substring(1,3).equals( L":\\")) {

				// Check if there is an existing file with the same name

				if ( pFileInfo.get() != NULL) {

					// Check if the file is a working copy

					if ( pFileInfo->isWorkingCopy()) {

						// Local file matches a working copy file in the Alfresco folder

						CString msg;
						msg.FormatMessage( L"Found matching working copy for local file %1", curName.data());
						AfxMessageBox( msg, MB_OK | MB_ICONINFORMATION);
					}
					else if ( pFileInfo->getLockType() != LockNone) {

						// File is locked, may be the original document

						CString msg;
						msg.FormatMessage( L"Destination file %1 is locked", curName.data());
						AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
						return false;
					}
					else {

						// Indicate that we have copied a new file to the Alfresco share, do not check in/out

						copyFile = true;
					}
				}
				else {

					// Indicate that we have copied a new file to the Alfresco share, do not check in/out

					copyFile = true;
				}

				// Build the from/to paths, must be double null terminated

				wchar_t fromPath[MAX_PATH + 1];
				wchar_t toPath[MAX_PATH + 1];

				memset( fromPath, 0, sizeof( fromPath));
				memset( toPath, 0, sizeof( toPath));

				wcscpy( fromPath, curFile.data());
				wcscpy( toPath, alfresco.getUNCPath());

				// Copy the local file to the Alfresco folder

				SHFILEOPSTRUCT fileOpStruct;
				memset( &fileOpStruct, 0, sizeof(SHFILEOPSTRUCT));

				fileOpStruct.hwnd  = HWND_DESKTOP;
				fileOpStruct.wFunc = FO_COPY;
				fileOpStruct.pFrom = fromPath;
				fileOpStruct.pTo   = toPath;
				fileOpStruct.fFlags= 0;
				fileOpStruct.fAnyOperationsAborted =false;

				// Copy the file to the Alfresco folder

				if ( SHFileOperation( &fileOpStruct) != 0) {

					// File copy failed

					CString msg;
					msg.FormatMessage( L"Failed to copy file %1", curFile.data());
					AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
					return false;
				}
				else if ( fileOpStruct.fAnyOperationsAborted) {

					// User aborted the file copy

					CString msg;
					msg.FormatMessage( L"Copy aborted for %1", curFile.data());
					AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
					return false;
				}

				// Get the file information for the copied file

				pFileInfo = alfresco.getFileInformation( curName);
			}

			// Check in or check out the file

			if ( pFileInfo.get() != NULL) {

				// Check if the file should be checked in/out

				if ( copyFile == false) {

					// Check if the file is a working copy, if so then check it in

					if ( pFileInfo->isWorkingCopy()) {

						// Check in the file

						doCheckIn( alfresco, pFileInfo);
					}
					else if ( pFileInfo->getLockType() == LockNone) {

						// Check out the file

						doCheckOut( alfresco, pFileInfo);
					}
					else {

						// File is locked, may already be checked out

						CString msg;
						msg.FormatMessage( L"File %1 is locked", curFile.data());
						AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
					}
				}
				else {

					// No existing file to link the copied file to

					CString msg;
					msg.FormatMessage( L"Copied file %1 to Alfresco folder", curFile.data());
					AfxMessageBox( msg, MB_OK | MB_ICONINFORMATION);
				}

			}
			else {
				CString msg;
				msg.FormatMessage( L"Failed to get file status for %1", curFile.data());
				AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
			}
		}
		else {

			// Check the error status

			CString msg;

			if ( attr != INVALID_FILE_ATTRIBUTES)
				msg.FormatMessage( L"Path %1 is a folder, ignored", curFile.data());
			else
				msg.FormatMessage( L"File %1 does not exist", curFile.data());
			AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
		}
	}

	// Return status

	return true;
}

/**
 * Check in the specified file
 *
 * @param alfresco AlfrescoInterface&
 * @param pFileInfo PTR_AlfrescoFileInfo&
 * @return bool
 */
bool CCAlfrescoAppApp::doCheckIn( AlfrescoInterface& alfresco, PTR_AlfrescoFileInfo& pFileInfo) {

	bool checkedIn = false;

	try {

		// Check in the specified file

		alfresco.checkIn( pFileInfo->getName());

		CString msg;
		msg.FormatMessage( L"Checked in file %1", pFileInfo->getName().data());
		AfxMessageBox( msg, MB_OK | MB_ICONINFORMATION);

		// Indicate that the check in was successful

		checkedIn = true;
	}
	catch (Exception ex) {
		CString msg;
		msg.FormatMessage( L"Error checking in file %1\n\n%2", pFileInfo->getName().data(), ex.getMessage().data());
		AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
	}

	// Return the check in status

	return checkedIn;
}

/**
 * Check out the specified file
 *
 * @param alfresco AlfrescoInterface&
 * @param pFileInfo PTR_AlfrescoFileInfo&
 * @return bool
 */
bool CCAlfrescoAppApp::doCheckOut( AlfrescoInterface& alfresco, PTR_AlfrescoFileInfo& pFileInfo) {

	bool checkedOut = false;

	try {

		// Check out the specified file

		String workingCopy;
		alfresco.checkOut( pFileInfo->getName(), workingCopy);

		CString msg;
		msg.FormatMessage( L"Checked out file %1 to %2", pFileInfo->getName().data(), workingCopy.data());
		AfxMessageBox( msg, MB_OK | MB_ICONINFORMATION);

		// Indicate that the check out was successful

		checkedOut = true;
	}
	catch (Exception ex) {
		CString msg;
		msg.FormatMessage( L"Error checking out file %1\n\n%2", pFileInfo->getName().data(), ex.getMessage().data());
		AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
	}

	// Return the check out status

	return checkedOut;
}
