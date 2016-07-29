#ifndef __SOUND_PLAYBACK_HEADER_INCLUDED__
#define __SOUND_PLAYBACK_HEADER_INCLUDED__
#pragma once

#include <dsound.h>

class SoundPlayback
{
public:
	SoundPlayback( IUnknown* pOuter = NULL );
	~SoundPlayback();

	int Open();
	int Play( byte* pcmData, int size );
	int Close();

private:
	IDirectSound8* m_directSound;
	IDirectSoundBuffer* m_primaryBuffer;
	IDirectSoundBuffer8* m_secondaryBuffer;
};

#endif//__SOUND_PLAYBACK_HEADER_INCLUDED__
