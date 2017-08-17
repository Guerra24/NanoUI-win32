#include "stdafx.h"
#include <stdio.h>
#include "trayhook.h"

HWND applicationHandle = NULL;

#pragma data_seg()

static HHOOK hook = NULL;

static LRESULT CALLBACK HookCallback(int code, WPARAM wParam, LPARAM lParam);

extern "C" {
LIB_FUNC void Init()
{
}
LIB_FUNC BOOL RegisterSystemTrayHook(HWND hWnd, HINSTANCE hInstance)
{
	HWND hShell = FindWindow("Shell_TrayWnd", NULL);
	DWORD shellThread = GetWindowThreadProcessId(hShell, NULL);
	applicationHandle = hWnd;
	return (hook = SetWindowsHookEx(WH_CALLWNDPROCRET, (HOOKPROC)HookCallback, hInstance, shellThread)) != NULL;
}
LIB_FUNC BOOL UnregisterSystemTrayHook()
{
	return hook != NULL ? UnhookWindowsHookEx(hook) : TRUE;
}
}

#define SH_TRAY_DATA 1

typedef struct
{
	DWORD dwHz;
	DWORD dwMessage;
	NOTIFYICONDATA nid;
} SHELLTRAYDATA;

static LRESULT CALLBACK HookCallback(int code, WPARAM wParam, LPARAM lParam)
{
	if (code >= 0)
	{
		CWPRETSTRUCT *pInfo = (CWPRETSTRUCT *)lParam;

		if (pInfo->message == WM_COPYDATA)
		{
			COPYDATASTRUCT *copyDataStruct = (COPYDATASTRUCT *)pInfo->lParam;
			if (copyDataStruct->dwData == SH_TRAY_DATA &&
				((SHELLTRAYDATA *)copyDataStruct->lpData)->dwHz == 0x34753423)
			{
				SendMessageTimeout(applicationHandle, WM_COPYDATA, pInfo->wParam, pInfo->lParam,
								   SMTO_ABORTIFHUNG, 10000, NULL);
			}
		}
	}
	printf("Test");
	fflush(stdout);
	return CallNextHookEx(NULL, code, wParam, lParam);
}
