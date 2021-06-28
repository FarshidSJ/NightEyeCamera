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

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;

/**
 * Created by sudamasayuki on 2018/03/14.
 */

public class EncodeRenderHandler implements Runnable {
    private static final String TAG = "CameraRecorder";

    private final Object sync = new Object();
    private EGLContext sharedContext;
    private boolean isRecordable;
    private Object surface;
    private int texId = -1;

    private boolean requestSetEglContext;
    private boolean requestRelease;
    private int requestDraw;

    private float[] MVPMatrix = new float[16];
    private float[] STMatrix = new float[16];
    private float aspectRatio = 1f;

    private final float XMatrixScale;
    private final float YMatrixScale;
    private final float fileWidth;
    private final float fileHeight;
    private final boolean recordNoFilter;

    private GLES20FramebufferObject framebufferObject;
    private GLES20FramebufferObject filterFramebufferObject;
    private GlFilter normalFilter;
    private GlFilter glFilter;

    static EncodeRenderHandler createHandler(final String name,
                                             final boolean flipVertical,
                                             final boolean flipHorizontal,
                                             final float viewAspect,
                                             final float fileWidth,
                                             final float fileHeight,
                                             final boolean recordNoFilter,
                                             final GlFilter filter
    ) {
        Log.v(TAG, "createHandler:");
        Log.v(TAG, "fileAspect:" + (fileHeight / fileWidth) + " viewAcpect: " + viewAspect);

        final EncodeRenderHandler handler = new EncodeRenderHandler(
                flipVertical,
                flipHorizontal,
                fileHeight / fileWidth,
                viewAspect,
                fileWidth,
                fileHeight,
                recordNoFilter,
                filter
        );
        synchronized (handler.sync) {
            new Thread(handler, !TextUtils.isEmpty(name) ? name : TAG).start();
            try {
                handler.sync.wait();
            } catch (final InterruptedException e) {
            }
        }

        return handler;
    }

    private EncodeRenderHandler(final boolean flipVertical,
                                final boolean flipHorizontal,
                                final float fileAspect,
                                final float viewAspect,
                                final float fileWidth,
                                final float fileHeight,
                                final boolean recordNoFilter,
                                final GlFilter filter
    ) {


        this.fileWidth = fileWidth;
        this.fileHeight = fileHeight;
        this.recordNoFilter = recordNoFilter;
        this.glFilter = filter;

        if (fileAspect == viewAspect) {
            XMatrixScale = (flipHorizontal ? -1 : 1);
            YMatrixScale = flipVertical ? -1 : 1;
        } else {
            if (fileAspect < viewAspect) {
                XMatrixScale = (flipHorizontal ? -1 : 1);
                YMatrixScale = (flipVertical ? -1 : 1) * (viewAspect / fileAspect);
                Log.v(TAG, "cameraAspect: " + viewAspect + " YMatrixScale :" + YMatrixScale);
            } else {
                XMatrixScale = (flipHorizontal ? -1 : 1) * (fileAspect / viewAspect);
                YMatrixScale = (flipVertical ? -1 : 1);
                Log.v(TAG, "cameraAspect: " + viewAspect + " YMatrixScale :" + YMatrixScale + " XMatrixScale :" + XMatrixScale);
            }
        }

    }

    final void setEglContext(final EGLContext shared_context, final int tex_id, final Object surface) {
        Log.i(TAG, "setEglContext:");
        if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture) && !(surface instanceof SurfaceHolder)) {
            throw new RuntimeException("unsupported window type:" + surface);
        }
        synchronized (sync) {
            if (requestRelease) return;
            sharedContext = shared_context;
            texId = tex_id;
            this.surface = surface;
            this.isRecordable = true;
            requestSetEglContext = true;
            sync.notifyAll();
            try {
                sync.wait();
            } catch (final InterruptedException e) {
            }
        }
    }


    final void prepareDraw() {
        synchronized (sync) {
            if (requestRelease) return;
            requestDraw++;
            sync.notifyAll();
        }
    }


    public final void draw(final int tex_id, final float[] texMatrix, final float[] mvpMatrix, final float aspectRatio) {
        synchronized (sync) {
            if (requestRelease) return;
            texId = tex_id;
            System.arraycopy(texMatrix, 0, STMatrix, 0, 16);
            System.arraycopy(mvpMatrix, 0, MVPMatrix, 0, 16);
            // square対策
            Matrix.scaleM(MVPMatrix,
                    0,
                    XMatrixScale, // ここをマイナスの値にするとflipする
                    YMatrixScale, // 見た目との歪みもここで調整すればいけると思う。
                    1);
            this.aspectRatio = aspectRatio;
            requestDraw++;
            sync.notifyAll();
        }
    }


    public final void release() {
        Log.i(TAG, "release:");
        synchronized (sync) {
            if (requestRelease) return;
            requestRelease = true;
            sync.notifyAll();
            try {
                sync.wait();
            } catch (final InterruptedException e) {
            }
        }
    }

    //********************************************************************************
//********************************************************************************
    private EGLBase egl;
    private EglSurface inputSurface;

    private GlPreview previewShader;

    @Override
    public final void run() {
        Log.i(TAG, "EncodeRenderHandler thread started:");
        synchronized (sync) {
            requestSetEglContext = requestRelease = false;
            requestDraw = 0;
            sync.notifyAll();
        }
        boolean localRequestDraw;
        for (; ; ) {
            synchronized (sync) {
                if (requestRelease) break;
                if (requestSetEglContext) {
                    requestSetEglContext = false;
                    internalPrepare();
                }
                localRequestDraw = requestDraw > 0;
                if (localRequestDraw) {
                    requestDraw--;

                }
            }
            if (localRequestDraw) {
                if ((egl != null) && texId >= 0) {
                    inputSurface.makeCurrent();

                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

                    if (isRecordFilter()) {
                        framebufferObject.enable();
                        filterFramebufferObject.enable();
                    }

                    previewShader.draw(texId, MVPMatrix, STMatrix, aspectRatio);

                    if (isRecordFilter()) {
                        framebufferObject.enable();
                        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                        glFilter.draw(filterFramebufferObject.getTexName(), framebufferObject);

                        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

                        GLES20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                        normalFilter.draw(framebufferObject.getTexName(), null);
                    }


                    inputSurface.swap();
                }
            } else {
                synchronized (sync) {
                    try {
                        sync.wait();
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
            }
        }
        synchronized (sync) {
            requestRelease = true;
            internalRelease();
            sync.notifyAll();
        }
        Log.i(TAG, "EncodeRenderHandler thread finished:");
    }

    private void internalPrepare() {
        Log.i(TAG, "internalPrepare:");
        internalRelease();
        egl = new EGLBase(sharedContext, false, isRecordable);

        inputSurface = egl.createFromSurface(surface);

        inputSurface.makeCurrent();

        previewShader = new GlPreview(GlPreview.GL_TEXTURE_EXTERNAL_OES);
        previewShader.setup();

        if (isRecordFilter()) {
            framebufferObject = new GLES20FramebufferObject();
            framebufferObject.setup((int) fileWidth, (int) fileHeight);

            filterFramebufferObject = new GLES20FramebufferObject();
            filterFramebufferObject.setup((int) fileWidth, (int) fileHeight);

            normalFilter = new GlFilter();
            normalFilter.setup();
        }

        surface = null;
        sync.notifyAll();
    }

    private void internalRelease() {
        Log.i(TAG, "internalRelease:");
        if (inputSurface != null) {
            inputSurface.release();
            inputSurface = null;
        }
        if (egl != null) {
            egl.release();
            egl = null;
        }
        if (normalFilter != null) {
            normalFilter.release();
            normalFilter = null;
        }
        if (filterFramebufferObject != null) {
            filterFramebufferObject.release();
            filterFramebufferObject = null;
        }
        if (framebufferObject != null) {
            framebufferObject.release();
            framebufferObject = null;
        }
    }

    private boolean isRecordFilter() {
        return (glFilter != null && !recordNoFilter);
    }

}

