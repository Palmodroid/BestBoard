package org.lattilad.bestboard.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * http://stephendnicholas.com/posts/android-handlerthread
 *
 * Thread: runs ONE runnable
 * HandlerThread: runs tasks defined by Messages, one after another
 */

public class SoundMaker implements Runnable
    {
    private static SoundMaker soundMaker = null;

    private static final int SAMPLE_RATE = 16000;

    private static final int RAMP_LENGTH = SAMPLE_RATE / 100;
    private static final int TAIL_LENGTH = SAMPLE_RATE * 3;

    // All SOUND_DOWN should be negative!
    private static final int SOUND_DOWN_NEXT_NEW = -1;
    private static final int SOUND_DOWN_NEXT_PAUSE = -2;
    private static final int SOUND_DOWN_NEXT_STOP = -3;
    private static final int SOUND_UP = 1;

    private final Object lock = new Object();

    private boolean running = false;

    private int mode = SOUND_DOWN_NEXT_PAUSE;
    private int frequency = 0;

    // This class cannot be instantiated from outside
    private SoundMaker() {}

    public static void start( int frequency )
        {
        if ( soundMaker != null )
            {
            synchronized ( soundMaker.lock )
                {
                if ( !soundMaker.running )
                    soundMaker = null;
                }
            }

        if ( soundMaker == null )
            {
            soundMaker = new SoundMaker();
            new Thread( soundMaker ).start();
            }

        synchronized ( soundMaker.lock )
            {
            soundMaker.frequency = frequency;
            soundMaker.mode = SOUND_DOWN_NEXT_NEW;
            }
        }

    public static void pause()
        {
        if ( soundMaker != null )
            {
            synchronized ( soundMaker.lock )
                {
                soundMaker.mode = SOUND_DOWN_NEXT_PAUSE;
                }
            }
        // else throw exception?
        }

    public static void stop()
        {
        if ( soundMaker != null )
            {
            synchronized ( soundMaker.lock )
                {
                soundMaker.mode = SOUND_DOWN_NEXT_STOP;
                }
            }
        // else throw exception?
        }


    /**
     * mode defines task
     * SOUND_DOWN...: ramps down sound
     * ...NEXT_NEW: ramps up a new frequency
     * ...NEXT_PAUSE: stays at no sound
     * ...NEXT_FINISH: finishes after a while
     * SOUND_UP: starts after NEXT_NEW within run()
     */
    @Override
    public void run()
        {
        // Log.d("MUSIC", "Thread start");

        AudioTrack audioTrack;
        int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
        audioTrack.play();

        short[] toneArray = new short[1];
        short tone;

        int tick = 0;
        int ramp = 0;
        int tail = TAIL_LENGTH; // Silent end before finishing this AudioTrack

        int modeTemp = SOUND_DOWN_NEXT_PAUSE;
        int frequencyTemp = 0;

        while ( true )
            {
            // One tone is always played at the beginning
            tone = (short) ((Math.sin( frequencyTemp * 2 * Math.PI * tick / SAMPLE_RATE) * 32767 * ramp/RAMP_LENGTH ));
            toneArray[0] = tone;
            audioTrack.write(toneArray, 0, 1);

            synchronized (lock)
                {
                modeTemp = mode;
                }

            // Sound can change only at ramp 0
            if ( ramp == 0 )
                {

                if ( modeTemp == SOUND_DOWN_NEXT_STOP)
                    {
                    tail--;
                    if ( tail == 1 )
                        {
                        // There is already one round left
                        synchronized ( soundMaker.lock )
                            {
                            running = false;
                            }
                        }
                    else if ( tail == 0 )
                        {
                        break;
                        }
                    }

                else // turn off every sign of stop
                    {
                    tick = 0;
                    tail = TAIL_LENGTH;
                    synchronized (lock)
                        {
                        running = true;
                        }

                    if (modeTemp == SOUND_DOWN_NEXT_NEW)
                        {
                        // Log.d("MUSIC", "Sound UP");

                        modeTemp = SOUND_UP;
                        synchronized (lock)
                            {
                            frequencyTemp = frequency;
                            mode = SOUND_UP;
                            }
                        }
                    }
                }

            // SOUND_UP - ramp sound up and hold
            if ( modeTemp == SOUND_UP && ramp < RAMP_LENGTH )
                ramp ++;

            // SOUND_DOWN - ramp sound down and NEXT_NEW: SOUND UP from 0; NEXT_PAUSE: hold
            else if ( modeTemp < 0 && ramp > 0 )
                ramp --;

            tick++;
            }

        audioTrack.stop();
        audioTrack.release();
        // Log.d("MUSIC", "Thread END");
        }
    }
