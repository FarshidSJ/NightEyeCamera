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
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLSurface;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by sudamasayuki on 2018/03/14.
 */

public class EglSurface {
    private static final boolean DEBUG = false;    // TODO set false on release
    private static final String TAG = "EGLBase";


    private final EGLBase egl;
    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private final int width, height;

    EglSurface(final EGLBase egl, final Object surface) {
        if (DEBUG) Log.v(TAG, "EglSurface:");
        if (!(surface instanceof SurfaceView)
                && !(surface instanceof Surface)
                && !(surface instanceof SurfaceHolder)
                && !(surface instanceof SurfaceTexture))
            throw new IllegalArgumentException("unsupported surface");
        this.egl = egl;
        eglSurface = this.egl.createWindowSurface(surface);
        width = this.egl.querySurface(eglSurface, EGL14.EGL_WIDTH);
        height = this.egl.querySurface(eglSurface, EGL14.EGL_HEIGHT);
        if (DEBUG) Log.v(TAG, String.format("EglSurface:size(%d,%d)", width, height));
    }

    public void makeCurrent() {
        egl.makeCurrent(eglSurface);
    }

    public void swap() {
        egl.swap(eglSurface);
    }

    public EGLContext getContext() {
        return egl.getContext();
    }

    public void release() {
        if (DEBUG) Log.v(TAG, "EglSurface:release:");
        egl.makeDefault();
        egl.destroyWindowSurface(eglSurface);
        eglSurface = EGL14.EGL_NO_SURFACE;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
