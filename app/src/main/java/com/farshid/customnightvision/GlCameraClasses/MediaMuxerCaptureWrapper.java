/*MIT License

        Copyright (c) 2018 Masayuki Suda

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.*/
package com.farshid.customnightvision.GlCameraClasses;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by sudamasayuki on 2018/03/14.
 */

public class MediaMuxerCaptureWrapper {
    private static final String TAG = "MediaMuxerWrapper";

    private final MediaMuxer mediaMuxer;
    private int encoderCount, startedCount;
    private boolean isStarted;
    private MediaEncoder videoEncoder, audioEncoder;
    private long preventAudioPresentationTimeUs = -1;
    private int audioTrackIndex = -1;

    /**
     * Constructor
     */
    public MediaMuxerCaptureWrapper(final String filePath) throws IOException {
        mediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        encoderCount = startedCount = 0;
        isStarted = false;

    }

    public void prepare() throws IOException {
        if (videoEncoder != null) {
            videoEncoder.prepare();
        }
        if (audioEncoder != null) {
            audioEncoder.prepare();
        }
    }

    public void startRecording() {
        if (videoEncoder != null) {
            videoEncoder.startRecording();
        }
        if (audioEncoder != null) {
            audioEncoder.startRecording();
        }
    }

    public void stopRecording() {
        if (videoEncoder != null) {
            videoEncoder.stopRecording();
        }
        videoEncoder = null;
        if (audioEncoder != null) {
            audioEncoder.stopRecording();
        }
        audioEncoder = null;
    }

    public synchronized boolean isStarted() {
        return isStarted;
    }

//**********************************************************************
//**********************************************************************

    /**
     * assign encoder to this calss. this is called from encoder.
     *
     * @param encoder instance of MediaVideoEncoder or MediaAudioEncoder
     */
    void addEncoder(final MediaEncoder encoder) {
        if (encoder instanceof MediaVideoEncoder) {
            if (videoEncoder != null)
                throw new IllegalArgumentException("Video encoder already added.");
            videoEncoder = encoder;
        } else if (encoder instanceof MediaAudioEncoder) {
            if (audioEncoder != null)
                throw new IllegalArgumentException("Video encoder already added.");
            audioEncoder = encoder;
        } else
            throw new IllegalArgumentException("unsupported encoder");
        encoderCount = (videoEncoder != null ? 1 : 0) + (audioEncoder != null ? 1 : 0);
    }

    /**
     * request start recording from encoder
     *
     * @return true when muxer is ready to write
     */
    synchronized boolean start() {
        Log.v(TAG, "start:");
        startedCount++;
        if ((encoderCount > 0) && (startedCount == encoderCount)) {
            mediaMuxer.start();
            isStarted = true;
            notifyAll();
            Log.v(TAG, "MediaMuxer started:");
        }
        return isStarted;
    }

    /**
     * request stop recording from encoder when encoder received EOS
     */
    /*package*/
    synchronized void stop() {
        Log.v(TAG, "stop:startedCount=" + startedCount);
        startedCount--;
        if ((encoderCount > 0) && (startedCount <= 0)) {
            mediaMuxer.stop();
            mediaMuxer.release();
            isStarted = false;
            Log.v(TAG, "MediaMuxer stopped:");
        }
    }

    /**
     * assign encoder to muxer
     *
     * @param format
     * @return minus value indicate error
     */
    synchronized int addTrack(final MediaFormat format) {
        if (isStarted) {
            throw new IllegalStateException("muxer already started");
        }

        final int trackIx = mediaMuxer.addTrack(format);
        Log.i(TAG, "addTrack:trackNum=" + encoderCount + ",trackIx=" + trackIx + ",format=" + format);

        String mime = format.getString(MediaFormat.KEY_MIME);
        if (!mime.startsWith("video/")) {
            audioTrackIndex = trackIx;
        }
        return trackIx;
    }

    /**
     * write encoded data to muxer
     *
     * @param trackIndex
     * @param byteBuf
     * @param bufferInfo
     */
    /*package*/
    synchronized void writeSampleData(final int trackIndex, final ByteBuffer byteBuf, final MediaCodec.BufferInfo bufferInfo) {
        //bufferInfo.presentationTimeUs
        if (startedCount <= 0) return;

        if (audioTrackIndex == trackIndex) {
            if (preventAudioPresentationTimeUs < bufferInfo.presentationTimeUs) {
                mediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
                preventAudioPresentationTimeUs = bufferInfo.presentationTimeUs;
            }
        } else {
            mediaMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
        }
    }

}

