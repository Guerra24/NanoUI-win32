#pragma once

#if defined(DLL_EXPORT)
#define LIB_FUNC __declspec(dllexport)
#else
#define LIB_FUNC
#endif

extern "C" {
LIB_FUNC void Init();
LIB_FUNC BOOL RegisterSystemTrayHook(HWND hWnd, HINSTANCE hInstance);
LIB_FUNC BOOL UnregisterSystemTrayHook();
}
