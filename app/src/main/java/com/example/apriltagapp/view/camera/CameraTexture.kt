package com.example.apriltagapp.view.camera

import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class CameraTexture(var hTex: Int) {

    val VERTEX_SHADER_CODE = "" +
            "attribute vec2 vPosition;\n" +
            "attribute vec2 vTexCoord;\n" +
            "varying vec2 texCoord;\n" +
            "void main() {\n" +
            "  texCoord = vTexCoord;\n" +
            "  gl_Position = vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );\n" +
            "}"

    // gl_FragColor에 대입되는 값이 컬러값이 아닌 텍스쳐이고, texture2D함수의 인자로
    // texture와 uv 좌표값을 받고 있다.
    val FRAGMENT_SHADER_CODE = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "varying vec2 texCoord;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(sTexture,texCoord);\n" +
            "}"

    private var vtmp = arrayOf(
        -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f
    ).toFloatArray()

    private var ttmp = arrayOf(
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f
    ).toFloatArray()

    private var mProgram: Int = -1
    private var pVertex: FloatBuffer
    private var pTexCoord: FloatBuffer

    init {
        // VERTEX_SHADER_CODE에 저장된 소스코드를 vertextShader에 로드한 후, 컴파일
        val vertexShader = MyRenderer.loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        // FRAGMENT_SHADER_CODE에 저장된 소스코드를 fragmnetShader에 로드한 후, 컴파일
        val fragmentShader  = MyRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        // 1. ByteBuffer를 할당받는다.
        // 2. ByteBuffer를 FloatBuffer로 변환
        // 3. ByteBuffer에서 사용할 엔디안을 지정, 디바이스 하드웨어의 native byte order를 사용
        pVertex = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer()
        pTexCoord = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer()

        // 4. Float 배열에 정의된 자표들을 FloatBuffer에 저장
        pVertex.put(vtmp)
        pTexCoord.put(ttmp)

        // 5. 읽어올 버퍼의 위치를 0으로 설정, 즉 첫 번째 좌표부터 읽어옴
        pVertex.position(0)
        pTexCoord.position(0)

        // Program 객체 생성
        mProgram = GLES20.glCreateProgram()

        // vertextShader, fragmentShader를 mProgram객체에 추가
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)

        // program 객체를 OpenGL에 연결. mProgram에 추가된 shader들이 OpenGL에 연결됨
        GLES20.glLinkProgram(mProgram)
    }

    fun draw() {
        // 렌더링 상태(Rendering State)의 일부분으로 mProgram을 추가한다.
        GLES20.glUseProgram(mProgram)

        // mProgram 객체로부터 vertex shader의 'vPosition', 'vTexCoord' 멤버에 대한 핸들을 가져옴
        var ph = GLES20.glGetAttribLocation(mProgram, "vPosition")
        var tch = GLES20.glGetAttribLocation(mProgram, "vTexCoord")

        // vertex 속성을 pVertex, pTexCoord에 저장되어 있는 vertex 좌표들로 정의
        GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4 * 2, pVertex)
        GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4*2, pTexCoord)

        // vertex 속성을 활성화 시켜야 렌더링시 반영되서 그려짐
        GLES20.glEnableVertexAttribArray(ph)
        GLES20.glEnableVertexAttribArray(tch)

        // 첫 번째 Texture 활성화
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // texture 바인딩
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgram, "sTexture"), 0)

        // 렌더링
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glFlush()
    }
}