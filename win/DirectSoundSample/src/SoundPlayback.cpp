#include "SoundPlayback.h"

#define SafeRelease(p) { if(p) { (p)->Release(); (p)=NULL; } }

SoundPlayback::SoundPlayback( IUnknown* pOuter )
{
}

SoundPlayback::~SoundPlayback()
{
}

int SoundPlayback::Open()
{
	HRESULT result;
	DSBUFFERDESC bufferDesc;
	WAVEFORMATEX waveFormat;

	result = DirectSoundCreate8( NULL, &m_directSound, NULL );

	HWND hwnd = GetConsoleWindow();
	result = m_directSound->SetCooperativeLevel( hwnd, DSSCL_PRIORITY );

	// Setup the primary buffer description.
	bufferDesc.dwSize = sizeof( DSBUFFERDESC );
	bufferDesc.dwFlags = DSBCAPS_PRIMARYBUFFER | DSBCAPS_CTRLVOLUME;
	bufferDesc.dwBufferBytes = 0;
	bufferDesc.dwReserved = 0;
	bufferDesc.lpwfxFormat = NULL;
	bufferDesc.guid3DAlgorithm = GUID_NULL;

	// Get control of the primary sound buffer on the default sound device.
	result = m_directSound->CreateSoundBuffer( &bufferDesc, &m_primaryBuffer, NULL );

	// Setup the format of the primary sound bufffer.
	// In this case it is a .WAV file recorded at 44,100 samples per second in 16-bit stereo (cd audio format).
	waveFormat.wFormatTag = WAVE_FORMAT_PCM;
	waveFormat.nSamplesPerSec = 16000;
	waveFormat.wBitsPerSample = 16;
	waveFormat.nChannels = 1;
	waveFormat.nBlockAlign = ( waveFormat.wBitsPerSample / 8 ) * waveFormat.nChannels;
	waveFormat.nAvgBytesPerSec = waveFormat.nSamplesPerSec * waveFormat.nBlockAlign;
	waveFormat.cbSize = 0;

	// Set the primary buffer to be the wave format specified.
	result = m_primaryBuffer->SetFormat( &waveFormat );

	return 0;
}

int SoundPlayback::Play( byte* pcmData, int size )
{
	WAVEFORMATEX waveFormat;
	DSBUFFERDESC bufferDesc;
	HRESULT result;
	IDirectSoundBuffer* tempBuffer;
	unsigned char* bufferPtr;
	unsigned long bufferSize;

	// Set the wave format of secondary buffer that this wave file will be loaded onto.
	waveFormat.wFormatTag = WAVE_FORMAT_PCM;
	waveFormat.nSamplesPerSec = 16000;
	waveFormat.wBitsPerSample = 16;
	waveFormat.nChannels = 1;
	waveFormat.nBlockAlign = ( waveFormat.wBitsPerSample / 8 ) * waveFormat.nChannels;
	waveFormat.nAvgBytesPerSec = waveFormat.nSamplesPerSec * waveFormat.nBlockAlign;
	waveFormat.cbSize = 0;

	// Set the buffer description of the secondary sound buffer that the wave file will be loaded onto.
	bufferDesc.dwSize = sizeof( DSBUFFERDESC );
	bufferDesc.dwFlags = DSBCAPS_CTRLVOLUME;
	bufferDesc.dwBufferBytes = size;
	bufferDesc.dwReserved = 0;
	bufferDesc.lpwfxFormat = &waveFormat;
	bufferDesc.guid3DAlgorithm = GUID_NULL;

	result = m_directSound->CreateSoundBuffer( &bufferDesc, &tempBuffer, NULL );

	result = tempBuffer->QueryInterface( IID_IDirectSoundBuffer8, ( void** )&m_secondaryBuffer );
	tempBuffer->Release();
	tempBuffer = 0;

	result = m_secondaryBuffer->Lock( 0, size, (void**)&bufferPtr, (DWORD*)&bufferSize, NULL, 0, 0 );
	memcpy( bufferPtr, pcmData, size );
	result = m_secondaryBuffer->Unlock( (void*)bufferPtr, bufferSize, NULL, 0 );


	result = m_secondaryBuffer->SetCurrentPosition( 0 );
	result = m_secondaryBuffer->SetVolume( DSBVOLUME_MAX );
	result = m_secondaryBuffer->Play( 0, 0, 0 );

	DWORD dwStatus;
	do
	{
		Sleep( 200 );
		m_secondaryBuffer->GetStatus( &dwStatus );
	} while ( dwStatus & DSBSTATUS_PLAYING );

	return 0;
}

int SoundPlayback::Close()
{
	SafeRelease( m_secondaryBuffer );
	SafeRelease( m_primaryBuffer );
	SafeRelease( m_directSound );
	return 0;
}
