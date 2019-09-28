package com.example.artechtoprotect

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat.postDelayed
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var ENABLE_CAMERA: Int = 111;
    var andyRenderable: ModelRenderable? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions();
        maybeEnableArButton();
        checkARInstall();
        var node = Node();
        var arFragment = getSupportFragmentManager()
            .findFragmentById(R.id.ux_fragment) as ArFragment
        var future = ModelRenderable.builder()
            .setSource(this, R.drawable.ic_launcher_foreground)
            .build()
            .thenAccept { modelRenderable -> andyRenderable = modelRenderable  }
            .exceptionally { throwable ->
                val toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                null
            };
        node.setParent(arFragment.getArSceneView().getScene());
        node.renderable = andyRenderable;

        takePhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        quit.setOnClickListener {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ENABLE_CAMERA -> {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    Log.i("dhl", "Camera permission granted.");
                } else {
                    Log.i("dhl", "Camera permission rejected.");
                }
                return
            }

            else -> {
                // fall through.
            }
        }
    }

    fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                ENABLE_CAMERA)
        } else {
            // Permission has already been granted.
        }
    }


    fun maybeEnableArButton() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            // Re-query at 5Hz while compatibility is checked in the background.
            Handler().postDelayed(Runnable { maybeEnableArButton() }, 200)
        }
        if (availability.isSupported) {
            Log.i("dhl", "AR supported.");
        } else { // Unsupported or unknown.
            Log.i("dhl", "AR not supported for the phone.");
        }
    }

    fun checkARInstall() {
        try {
            when (ArCoreApk.getInstance().requestInstall(this, true)) {
                ArCoreApk.InstallStatus.INSTALLED -> {

                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            Toast.makeText(this, "TODO: handle exception " + e, Toast.LENGTH_LONG)
                .show();
            return;
        }
    }

    private val requestImageCapture = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, requestImageCapture)
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == requestImageCapture && resultCode == RESULT_OK) {
            Toast.makeText(this, "Photo was Saved", Toast.LENGTH_LONG).show()
        }
    }
}
