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

/**
 * Created by sudamasayuki on 2017/05/18.
 */

public class GlCGAColorspaceFilter extends GlFilter {

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +

                    "varying vec2 vTextureCoord;" +
                    "uniform lowp sampler2D sTexture;" +

                    "void main() {" +
                    "highp vec2 sampleDivisor = vec2(1.0 / 200.0, 1.0 / 320.0);" +

                    "highp vec2 samplePos = vTextureCoord - mod(vTextureCoord, sampleDivisor);" +
                    "highp vec4 color = texture2D(sTexture, samplePos);" +

                    "mediump vec4 colorCyan = vec4(85.0 / 255.0, 1.0, 1.0, 1.0);" +
                    "mediump vec4 colorMagenta = vec4(1.0, 85.0 / 255.0, 1.0, 1.0);" +
                    "mediump vec4 colorWhite = vec4(1.0, 1.0, 1.0, 1.0);" +
                    "mediump vec4 colorBlack = vec4(0.0, 0.0, 0.0, 1.0);" +

                    "mediump vec4 endColor;" +
                    "highp float blackDistance = distance(color, colorBlack);" +
                    "highp float whiteDistance = distance(color, colorWhite);" +
                    "highp float magentaDistance = distance(color, colorMagenta);" +
                    "highp float cyanDistance = distance(color, colorCyan);" +

                    "mediump vec4 finalColor;" +

                    "highp float colorDistance = min(magentaDistance, cyanDistance);" +
                    "colorDistance = min(colorDistance, whiteDistance);" +
                    "colorDistance = min(colorDistance, blackDistance);" +

                    "if (colorDistance == blackDistance) {" +
                    "finalColor = colorBlack;" +
                    "} else if (colorDistance == whiteDistance) {" +
                    "finalColor = colorWhite;" +
                    "} else if (colorDistance == cyanDistance) {" +
                    "finalColor = colorCyan;" +
                    "} else {" +
                    "finalColor = colorMagenta;" +
                    "}" +

                    "gl_FragColor = finalColor;" +
                    "}";


    public GlCGAColorspaceFilter() {
        super(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER);
    }

}
