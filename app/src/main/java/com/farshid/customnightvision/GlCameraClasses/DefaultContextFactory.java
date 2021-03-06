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

import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_NO_CONTEXT;

/**
 * Created by sudamasayuki on 2018/03/14.
 */

public class DefaultContextFactory implements GLSurfaceView.EGLContextFactory {

    private static final String TAG = "DefaultContextFactory";

    private int EGLContextClientVersion;

    public DefaultContextFactory(final int version) {
        EGLContextClientVersion = version;
    }

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    @Override
    public EGLContext createContext(final EGL10 egl, final EGLDisplay display, final EGLConfig config) {
        final int[] attrib_list;
        if (EGLContextClientVersion != 0) {
            attrib_list = new int[]{EGL_CONTEXT_CLIENT_VERSION, EGLContextClientVersion, EGL_NONE};
        } else {
            attrib_list = null;
        }
        return egl.eglCreateContext(display, config, EGL_NO_CONTEXT, attrib_list);
    }

    @Override
    public void destroyContext(final EGL10 egl, final EGLDisplay display, final EGLContext context) {
        if (!egl.eglDestroyContext(display, context)) {
            Log.e(TAG, "display:" + display + " context: " + context);
            throw new RuntimeException("eglDestroyContext" + egl.eglGetError());
        }
    }

}
