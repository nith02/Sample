#ifndef __SOUND_CAPTURE_HEADER_INCLUDED__
#define __SOUND_CAPTURE_HEADER_INCLUDED__
#pragma once

#include <dsound.h>

class SoundCapture
{
public:
	SoundCapture( IUnknown* pOuter = NULL );
	~SoundCapture();

	HRESULT Open( DWORD dwSampleRate = 48000, WORD wBitPerSample = 8, WORD wChannels = 1 );
	HRESULT Close();
	HRESULT Start( HANDLE* pHandle );
	HRESULT Stop();
	DWORD GetPcmData( byte* pbData, DWORD dwSize );

private:
	LPDIRECTSOUNDCAPTURE m_pDSCapture;
	LPDIRECTSOUNDCAPTUREBUFFER m_pDSBCapture;
	LPDIRECTSOUNDNOTIFY m_pDSNotify;
	DWORD m_dwCaptureBufferSize;
	DWORD m_dwNextCaptureOffset;
	HANDLE m_hNotificationEvent;
};

#endif//__SOUND_CAPTURE_HEADER_INCLUDED__
