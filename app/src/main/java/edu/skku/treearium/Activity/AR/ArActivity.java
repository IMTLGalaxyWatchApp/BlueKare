/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.skku.treearium.Activity.AR;

import android.content.Intent;
import android.content.res.Configuration;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.skku.treearium.R;
import edu.skku.treearium.Renderer.BackgroundRenderer;
import edu.skku.treearium.Renderer.PointCloudRenderer;
import edu.skku.treearium.Utils.PointCollector;
import edu.skku.treearium.Utils.PointUtil;
import edu.skku.treearium.helpers.CameraPermissionHelper;
import edu.skku.treearium.helpers.DisplayRotationHelper;
import edu.skku.treearium.helpers.FullScreenHelper;
import edu.skku.treearium.helpers.TrackingStateHelper;


import com.curvsurf.fsweb.FindSurfaceRequester;
import com.curvsurf.fsweb.RequestForm;
import com.curvsurf.fsweb.ResponseForm;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static java.lang.Integer.parseInt;

public class ArActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
  private static final String TAG = ArActivity.class.getSimpleName();

  // Rendering. The Renderers are created here, and initialized when the GL surface is created.
  private GLSurfaceView surfaceView;

  private boolean installRequested;

  private DisplayRotationHelper displayRotationHelper;
  private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);

  private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
  private Session session;
  private Frame frame;

  private PointCollector collector = null;
  private boolean isRecording = false;
  private Button recButton = null;
  private Button popup = null;


  private boolean isStaticView = false;
  private boolean drawSeedState = false;
  private float[] ray = null;
  private static final String REQUEST_URL = "https://developers.curvsurf.com/FindSurface/cylinder";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ar);
    recButton = (Button)findViewById(R.id.recButton);
    popup = (Button)findViewById(R.id.popup);
    surfaceView = (GLSurfaceView)findViewById(R.id.surfaceview);
    displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

    // Set up renderer.
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    surfaceView.setWillNotDraw(false);

    Intent intent = new Intent(ArActivity.this, PopupActivity.class);
    startActivityForResult(intent, 1);

    installRequested = false;

    recButton.setOnClickListener(v -> {
      isRecording = !isRecording;
      if(isRecording){
        collector = new PointCollector();
        recButton.setText("Stop");
        isStaticView = false;
      } else {
        (new Thread(new Runnable() {
          @Override
          public void run() {
            if (ArActivity.this.collector != null) {
              final FloatBuffer points = ArActivity.this.collector.doFilter();
              ArActivity.this.surfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                  pointCloudRenderer.update(points);
                  isStaticView = true;
                }
              });
              ArActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  recButton.setText("Recording");
                  recButton.setClickable(true);
                }
              });
            }
          }
        })).start();
      }
    });

    popup.setOnClickListener(v -> {
      Intent intent12 = new Intent(ArActivity.this, PopupActivity.class);
      startActivityForResult(intent12, 1);
    });

    surfaceView.setOnTouchListener((v, event) ->{
      if(collector != null && collector.filterPoints != null) {
        // ray 생성
        float tx = event.getX();
        float ty = event.getY();
        // ray 생성
        ray = screenPointToWorldRay(tx, ty, frame);
        float[] rayOrigin = new float[]{
                ray[0]+ray[3],
                ray[1]+ray[4],
                ray[2]+ray[5],
        };

        Camera camera = frame.getCamera();
//        ray = camera.getPose().getZAxis(); // by unit
//        ray[0] = -ray[0];
//        ray[1] = -ray[1];
//        ray[2] = -ray[2];
//
//        // camera location
//        float[] rayOrigin = camera.getPose().getTranslation();

        float[] projmtx = new float[16];
        camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
        float unitRadius = (float) (0.8 / Math.max(projmtx[0], projmtx[5]));

        //int pickIndex = PointUtil.pickPoint(collector.filterPoints, ray, rayOrigin);
        //float seedPos = collector.filterPoints.get(pickIndex);
        //drawSeedState = !drawSeedState;
        FloatBuffer targetPoints = collector.filterPoints;
        targetPoints.rewind();
        int pickIndex = PointUtil.pickPoint(targetPoints, ray, rayOrigin);
        if(pickIndex >= 0 && !Thread.currentThread().isInterrupted()) {
          (new Thread(() -> {
            RequestForm rf = new RequestForm();

            rf.setPointBufferDescription(collector.filterPoints.capacity() / 4, 16, 0); //pointcount, pointstride, pointoffset
            rf.setPointDataDescription(unitRadius * 0.2f, unitRadius * 0.4f); //accuracy, meanDistance
            rf.setTargetROI(pickIndex, unitRadius);//seedIndex,touchRadius
            rf.setAlgorithmParameter(RequestForm.SearchLevel.NORMAL, RequestForm.SearchLevel.NORMAL);//LatExt, RadExp
            FindSurfaceRequester fsr = new FindSurfaceRequester(REQUEST_URL, true);
            // Request Find Surface
            try {
              Log.d("CylinderFinder", "request");
              ResponseForm resp = fsr.request(rf, targetPoints);
              if (resp != null && resp.isSuccess()) {
                ResponseForm.CylinderParam param = resp.getParamAsCylider();
                // Normal Vector should be [0, 1, 0]
                float[] tmp = new float[]{param.b[0] - param.t[0], param.b[1] - param.t[1], param.b[2] - param.t[2]};
                float dist = (float)Math.sqrt( tmp[0] * tmp[0] + tmp[1] * tmp[1] + tmp[2] * tmp[2] );
                tmp[0] /= dist;
                tmp[1] /= dist;
                tmp[2] /= dist;
                Log.d("CylinderFinder", "request success code: "+parseInt(String.valueOf(resp.getResultCode()))+
                        ", Radius: "+param.r + ", Normal Vector: "+Arrays.toString(tmp));
              } else {
                Log.d("CylinderFinder", "request fail");
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          })).start();
          return false;
        }
      }
      return true;
    });
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (session == null) {
      Exception exception = null;
      String message = null;
      try {
        switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
          case INSTALL_REQUESTED:
            installRequested = true;
            return;
          case INSTALLED:
            break;
        }

        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
          CameraPermissionHelper.requestCameraPermission(this);
          return;
        }

        // Create the session.
        session = new Session(/* context= */ this);

        // ARCore 세부 설정
        Config config = new Config(session);
        config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);
        config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
        config.setFocusMode(Config.FocusMode.AUTO);

        session.configure(config); // Update Configuration

      } catch (UnavailableArcoreNotInstalledException
              | UnavailableUserDeclinedInstallationException e) {
        message = "Please install ARCore";
        exception = e;
      } catch (UnavailableApkTooOldException e) {
        message = "Please update ARCore";
        exception = e;
      } catch (UnavailableSdkTooOldException e) {
        message = "Please update this app";
        exception = e;
      } catch (UnavailableDeviceNotCompatibleException e) {
        message = "This device does not support AR";
        exception = e;
      } catch (Exception e) {
        message = "Failed to create AR session";
        exception = e;
      }

      if (message != null) {
        //messageSnackbarHelper.showError(this, message);
        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Exception creating session", exception);
        return;
      }
    }

    // Note that order matters - see the note in onPause(), the reverse applies here.
    try {
      session.resume();
    } catch (CameraNotAvailableException e) {
      //messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
      Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
      session = null;
      return;
    }

    surfaceView.onResume();
    displayRotationHelper.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (session != null) {
      // Note that the order matters - GLSurfaceView is paused first so that it does not try
      // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
      // still call session.update() and get a SessionPausedException.
      displayRotationHelper.onPause();
      surfaceView.onPause();
      session.pause();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
    super.onRequestPermissionsResult(requestCode, permissions, results);
    if (!CameraPermissionHelper.hasCameraPermission(this)) {
      Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
              .show();
      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        CameraPermissionHelper.launchPermissionSettings(this);
      }
      finish();
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
    try {
      // Create the texture and pass it to ARCore session to be filled during update().
      backgroundRenderer.createOnGlThread(/*context=*/ this);
      pointCloudRenderer.createOnGlThread(/*context=*/ this);

    } catch (IOException e) {
      Log.e(TAG, "Failed to read an asset file", e);
    }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    displayRotationHelper.onSurfaceChanged(width, height);
    GLES20.glViewport(0, 0, width, height);
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    // Clear screen to notify driver it should not load any pixels from previous frame.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (session == null) {
      return;
    }
    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session);

    try {
      session.setCameraTextureName(backgroundRenderer.getTextureId());

      // Obtain the current frame from ARSession. When the configuration is set to
      // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
      // camera framerate.
      frame = session.update();
      Camera camera = frame.getCamera();

      // If frame is ready, render camera preview image to the GL surface.
      backgroundRenderer.draw(frame);
      if(drawSeedState){
        //pointCloudRenderer.draw_seedPoint();
      }

      // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
      trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

      // If not tracking, don't draw 3D objects, show tracking failure reason instead.
      if (camera.getTrackingState() == TrackingState.PAUSED) {
        //messageSnackbarHelper.showMessage(
        //    this, TrackingStateHelper.getTrackingFailureReasonString(camera));
        return;
      }

      // Get projection matrix.
      float[] projmtx = new float[16];
      camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

      // Get camera matrix and draw.
      float[] viewmtx = new float[16];
      camera.getViewMatrix(viewmtx, 0);

      // Compute lighting from average intensity of the image.
      // The first three components are color scaling factors.
      // The last one is the average pixel intensity in gamma space.
      final float[] colorCorrectionRgba = new float[4];
      frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

      // Visualize tracked points.
      // Use try-with-resources to automatically release the point cloud.
      if(!isStaticView) {
        try (PointCloud pointCloud = frame.acquirePointCloud()) {
          if (isRecording && collector != null) {
            collector.doCollect(pointCloud);
          }
          pointCloudRenderer.update(pointCloud);
          pointCloudRenderer.draw(viewmtx, projmtx);
        }
      } else {
        pointCloudRenderer.draw(viewmtx, projmtx);
      }

    } catch (Throwable t) {
      // Avoid crashing the application due to unhandled exceptions.
      Log.e(TAG, "Exception on the OpenGL thread", t);
    }
  }

  float[] screenPointToWorldRay(float xPx, float yPx, Frame frame) {		// pointCloudActivity
    // ray[0~2] : camera pose
    // ray[3~5] : Unit vector of ray
    float[] ray_clip = new float[4];
    ray_clip[0] = 2.0f * xPx / surfaceView.getMeasuredWidth() - 1.0f;
    // +y is up (android UI Y is down):
    ray_clip[1] = 1.0f - 2.0f * yPx / surfaceView.getMeasuredHeight();
    ray_clip[2] = -1.0f; // +z is forwards (remember clip, not camera)
    ray_clip[3] = 1.0f; // w (homogenous coordinates)

    float[] ProMatrices = new float[32];  // {proj, inverse proj}
    frame.getCamera().getProjectionMatrix(ProMatrices, 0, 0.1f, 100.0f);
    Matrix.invertM(ProMatrices, 16, ProMatrices, 0);
    float[] ray_eye = new float[4];
    Matrix.multiplyMV(ray_eye, 0, ProMatrices, 16, ray_clip, 0);

    ray_eye[2] = -1.0f;
    ray_eye[3] = 0.0f;

    float[] out = new float[6];
    float[] ray_wor = new float[4];
    float[] ViewMatrices = new float[32];

    frame.getCamera().getViewMatrix(ViewMatrices, 0);
    Matrix.invertM(ViewMatrices, 16, ViewMatrices, 0);
    Matrix.multiplyMV(ray_wor, 0, ViewMatrices, 16, ray_eye, 0);

    float size = (float)Math.sqrt(ray_wor[0] * ray_wor[0] + ray_wor[1] * ray_wor[1] + ray_wor[2] * ray_wor[2]);

    out[3] = ray_wor[0] / size;
    out[4] = ray_wor[1] / size;
    out[5] = ray_wor[2] / size;

    out[0] = frame.getCamera().getPose().tx();
    out[1] = frame.getCamera().getPose().ty();
    out[2] = frame.getCamera().getPose().tz();

    return out;
  }
}
