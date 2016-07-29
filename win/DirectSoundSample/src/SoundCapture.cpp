#include <assert.h>

#include "SoundCapture.h"

#define NUM_REC_NOTIFICATIONS  16
#define SafeRelease(p) { if(p) { (p)->Release(); (p)=NULL; } }

SoundCapture::SoundCapture( IUnknown* pOuter )
{
}

SoundCapture::~SoundCapture()
{
}

HRESULT SoundCapture::Open( DWORD dwSampleRate, WORD wBitPerSample, WORD wChannels )
{
	GUID deviceGuid = GUID_NULL;
	HRESULT hr = DirectSoundCaptureCreate( &deviceGuid, &m_pDSCapture, NULL );
	m_dwNextCaptureOffset = 0;

	WAVEFORMATEX wfxInput;
	ZeroMemory ( &wfxInput, sizeof(wfxInput) );
	wfxInput.wFormatTag = WAVE_FORMAT_PCM;
	wfxInput.nSamplesPerSec = dwSampleRate;
	wfxInput.wBitsPerSample = wBitPerSample;
	wfxInput.nChannels = wChannels;
	wfxInput.nBlockAlign = wfxInput.nChannels * ( wfxInput.wBitsPerSample / 8 );
	wfxInput.nAvgBytesPerSec = wfxInput.nBlockAlign * wfxInput.nSamplesPerSec;

	DWORD dwNotifySize = max( 1024, wfxInput.nAvgBytesPerSec / 8 );
	dwNotifySize -= dwNotifySize % wfxInput.nBlockAlign;
	m_dwCaptureBufferSize = dwNotifySize * NUM_REC_NOTIFICATIONS;

	DSCBUFFERDESC dscbd;
	ZeroMemory( &dscbd, sizeof(dscbd) );
	dscbd.dwSize = sizeof(dscbd);
	dscbd.dwBufferBytes = m_dwCaptureBufferSize;
	dscbd.lpwfxFormat = &wfxInput; // Set the format during creatation
	hr = m_pDSCapture->CreateCaptureBuffer( &dscbd, &m_pDSBCapture, NULL );

	DSBPOSITIONNOTIFY arrPosNotify[NUM_REC_NOTIFICATIONS + 1];
	ZeroMemory( &arrPosNotify, sizeof( DSBPOSITIONNOTIFY ) * ( NUM_REC_NOTIFICATIONS + 1 ) );
	m_hNotificationEvent = CreateEvent( NULL, FALSE, FALSE, NULL );

	hr = m_pDSBCapture->QueryInterface( IID_IDirectSoundNotify, (VOID**)&m_pDSNotify );
	for ( int i = 0; i < NUM_REC_NOTIFICATIONS; i++ )
	{
		arrPosNotify[i].dwOffset = (dwNotifySize * i) + dwNotifySize - 1;
		arrPosNotify[i].hEventNotify = m_hNotificationEvent;
	}
	hr = m_pDSNotify->SetNotificationPositions( NUM_REC_NOTIFICATIONS, arrPosNotify );
	return S_OK;
}

HRESULT SoundCapture::Close()
{
	SafeRelease( m_pDSNotify );
	SafeRelease( m_pDSBCapture );
	SafeRelease( m_pDSCapture );
	CloseHandle( m_hNotificationEvent );

	return S_OK;
}

HRESULT SoundCapture::Start( HANDLE* pHandle )
{
	*pHandle = m_hNotificationEvent;
	return m_pDSBCapture->Start( DSCBSTART_LOOPING );
}

HRESULT SoundCapture::Stop()
{
	if ( NULL == m_pDSBCapture )
	{
		return E_FAIL;
	}

	return m_pDSBCapture->Stop();
}

DWORD SoundCapture::GetPcmData( byte* pbData, DWORD dwSize )
{
	VOID* pbCaptureData = NULL;
	VOID* pbCaptureData2 = NULL;
	DWORD dwCapturePos;
	DWORD dwReadPos;
	DWORD dwCaptureLength;
	DWORD dwCaptureLength2 = 0;
	LONG lLockSize;

	HRESULT hr = m_pDSBCapture->GetCurrentPosition( &dwCapturePos, &dwReadPos );

	lLockSize = dwReadPos - m_dwNextCaptureOffset;
	if ( lLockSize < 0 )
	{
		lLockSize += m_dwCaptureBufferSize;
	}
	if ( lLockSize > (LONG)dwSize )
	{
		lLockSize = dwSize;
	}
	if ( lLockSize == 0 )
	{
		return 0;
	}
	hr = m_pDSBCapture->Lock( m_dwNextCaptureOffset, lLockSize,
		&pbCaptureData, &dwCaptureLength,
		&pbCaptureData2, &dwCaptureLength2, 0L );

	assert( ( "Data size exceeds buffer size!!!", dwCaptureLength + dwCaptureLength2 <= dwSize ) );

	memcpy( pbData, pbCaptureData, dwCaptureLength );
	m_dwNextCaptureOffset += dwCaptureLength;
	m_dwNextCaptureOffset %= m_dwCaptureBufferSize; // Circular buffer

	if (pbCaptureData2 != NULL)
	{
		memcpy( pbData + dwCaptureLength, pbCaptureData2, dwCaptureLength2 );

		// Move the capture offset along
		m_dwNextCaptureOffset += dwCaptureLength2;
		m_dwNextCaptureOffset %= m_dwCaptureBufferSize; // Circular buffer
	}

	m_pDSBCapture->Unlock( pbCaptureData, dwCaptureLength, pbCaptureData2, dwCaptureLength2 );

	return dwCaptureLength + dwCaptureLength2;
}

