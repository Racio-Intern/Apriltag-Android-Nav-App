package com.example.apriltagapp

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.apriltagapp.databinding.ActivityGlBinding
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GlActivity : AppCompatActivity() {
    lateinit var binding: ActivityGlBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGlBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.viewGlSurface.setEGLContextClientVersion(2)
        binding.viewGlSurface.setRenderer(ExampleRenderer())

    }
}

class ExampleRenderer: GLSurfaceView.Renderer {
    private lateinit var triangle: CameraTexture
    companion object {
        fun loadShader(type: Int, shaderCode: String): Int{
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            return shader
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        triangle = CameraTexture(0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        triangle.draw()
    }


}