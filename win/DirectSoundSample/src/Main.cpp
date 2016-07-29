#include <stdio.h>
#include "SoundCapture.h"
#include "SoundPlayback.h"

#define BUF_SIZE 10240
#define RECORD_SIZE 256 * 1024

int TestCapture();
int TestPlayback();

int main( int argc, char** argv )
{
	TestCapture();
	TestPlayback();
	return 0;
}

int TestCapture()
{
	HANDLE hNotificationEvent;
	DWORD dwSize;
	DWORD dwWriteBytesTotal = 0;
	byte buf[BUF_SIZE];

	SoundCapture* pCapture = new SoundCapture();
	pCapture->Open( 16000, 16, 1 );
	pCapture->Start( &hNotificationEvent );

	FILE* fp = fopen( "out.pcm", "wb" );
	while ( dwWriteBytesTotal <= RECORD_SIZE )
	{
		DWORD dwResult = MsgWaitForMultipleObjects( 1, &hNotificationEvent, FALSE, INFINITE, QS_ALLEVENTS );
		switch ( dwResult )
		{
		case WAIT_OBJECT_0:
			while ( ( dwSize = pCapture->GetPcmData( buf, BUF_SIZE ) ) > 0 )
			{
				fwrite( buf, 1, dwSize, fp );
				dwWriteBytesTotal += dwSize;
				printf( "record %d bytes\n", dwWriteBytesTotal );
				if ( dwWriteBytesTotal > RECORD_SIZE )
				{
					break;
				}
				Sleep( 200 );
			}
		}
	}
	pCapture->Stop();
	pCapture->Close();
	delete pCapture;
	fclose( fp );

	return 0;
}

int TestPlayback()
{
	byte buf[BUF_SIZE];
	FILE* fp = fopen( "out.pcm", "rb" );
	SoundPlayback* pPlayer = new SoundPlayback();
	pPlayer->Open();
	int readBytes;
	while ( ( readBytes = fread( buf, 1, BUF_SIZE, fp ) ) > 0 )
	{
		pPlayer->Play( buf, readBytes );
	}
	pPlayer->Close();
	delete pPlayer;

	return 0;
}
