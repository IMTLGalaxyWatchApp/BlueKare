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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import edu.skku.treearium.R;
import edu.skku.treearium.Renderer.BackgroundRenderer;
import edu.skku.treearium.Renderer.ObjectRenderer;
import edu.skku.treearium.Renderer.PointCloudRenderer;
import edu.skku.treearium.Utils.PointCollector;
import edu.skku.treearium.Utils.PointUtil;
import edu.skku.treearium.helpers.CameraPermissionHelper;
import edu.skku.treearium.helpers.DisplayRotationHelper;
import edu.skku.treearium.helpers.FullScreenHelper;

import com.curvsurf.fsweb.FindSurfaceRequester;
import com.curvsurf.fsweb.RequestForm;
import com.curvsurf.fsweb.ResponseForm;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.hluhovskyi.camerabutton.CameraButton;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static java.lang.Integer.parseInt;

public class ArActivity extends AppCompatActivity implements GLSurfaceView.Renderer{
  private static final String TAG = ArActivity.class.getSimpleName();

  // Rendering. The Renderers are created here, and initialized when the GL surface is created.
  private GLSurfaceView surfaceView;

  private boolean installRequested;
  private String[] REQUIRED_PERMISSSIONS = {Manifest.permission.CAMERA};
  private final int PERMISSION_REQUEST_CODE = 0; // PROTECTION_NORMAL

  private DisplayRotationHelper displayRotationHelper;

  private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
  private final ObjectRenderer virtualObject = new ObjectRenderer();

  private View arLayout;
  private Session session;
  private Thread httpTh;
  private Frame frame;

  private PointCollector collector = null;
  private CylinderVars cylinderVars = null;

  private Button popup = null;
  private Button resetBtn = null;
  private CameraButton recBtn = null;
  private Pose anchorPoints = null;

  private boolean isFound = false;
  private boolean isRecording = false;
  private boolean isStaticView = false;
  private boolean drawSeedState = false;
  private float[] ray = null;
  private static final String REQUEST_URL = "https://developers.curvsurf.com/FindSurface/cylinder";

  //firebase
  FirebaseAuth mFirebaseAuth;
  FirebaseFirestore fstore;
  String userID;
  LocationManager locationManager;
  String latitude, longitude;
  GeoPoint location;

  private static final int REQUEST_LOCATION = 1;


  // Temporary matrix allocated here to reduce number of allocations for each frame.
  private final float[] anchorMatrix = new float[16];

  @SuppressLint("ClickableViewAccessibility")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ar);

    arLayout = findViewById(R.id.arLayout);
    popup = (Button)findViewById(R.id.popup);
    resetBtn = (Button)findViewById(R.id.resetBtn);
    resetBtn.setEnabled(false);
    recBtn = (CameraButton)findViewById(R.id.recBtn);
    surfaceView = (GLSurfaceView)findViewById(R.id.surfaceview);
    displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

    // Set up renderer.
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    surfaceView.setWillNotDraw(false);

    //firebase
    mFirebaseAuth = FirebaseAuth.getInstance();
    fstore = FirebaseFirestore.getInstance();
    userID= mFirebaseAuth.getCurrentUser().getUid();

    Intent intent = new Intent(ArActivity.this, PopupActivity.class);
    startActivityForResult(intent, 1);

    installRequested = false;

//    LocationManager nManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//    if (ActivityCompat.checkSelfPermission(
//            ArActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//            ArActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
//    }
//    else {
//      Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//      if (locationGPS != null) {
//        double lat = locationGPS.getLatitude();
//        double longi = locationGPS.getLongitude();
//        location = new GeoPoint(lat, longi);
//      } else {
//        Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
//      }
//    }

    popup.setOnClickListener(v -> {
      Intent intent12 = new Intent(ArActivity.this, PopupActivity.class);
      startActivityForResult(intent12, 1);
    });


    resetBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        collector = new PointCollector();
        Snackbar.make(arLayout, "Reset", Snackbar.LENGTH_LONG).show();
        isStaticView = false;
      }
    });

    recBtn.setOnClickListener(v -> {
      isRecording = !isRecording;
      if(isRecording){
        collector = new PointCollector();
        Snackbar.make(arLayout, "Collecting", Snackbar.LENGTH_LONG).show();
        isStaticView = false;
        resetBtn.setEnabled(true);
        resetBtn.setForeground(getApplicationContext().getDrawable(R.drawable.ic_sharp_sync_25));
      } else {
        resetBtn.setEnabled(false);
        resetBtn.setForeground(getApplicationContext().getDrawable(R.drawable.ic_sharp_sync_24));
        (new Thread(() -> {
          if (ArActivity.this.collector != null) {
            ArActivity.this.collector.filterPoints = ArActivity.this.collector.doFilter();
            ArActivity.this.surfaceView.queueEvent(() -> {
              pointCloudRenderer.update(ArActivity.this.collector.filterPoints);
              isStaticView = true;
            });
            ArActivity.this.runOnUiThread(() -> {
              Snackbar.make(arLayout, "Collecting Finished", Snackbar.LENGTH_LONG).show();
            });
          }
        })).start();
      }
    });

    surfaceView.setOnTouchListener((v, event) ->{
      if(collector != null && collector.filterPoints != null) {

        float tx = event.getX();
        float ty = event.getY();
        // ray 생성
        ray = screenPointToWorldRay(tx, ty, frame);
        float[] rayOrigin = new float[] {ray[3],ray[4],ray[5]};

        Camera camera = frame.getCamera();

        float[] projmtx = new float[16];
        camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
        final float unitRadius = (float) (0.8 / Math.max(projmtx[0], projmtx[5]));

        drawSeedState = true;

        Log.d("UnitRadius__", Float.toString(unitRadius));

        FloatBuffer targetPoints = collector.filterPoints;
        targetPoints.rewind();
        for(int i=0; i<targetPoints.capacity()/4; i++){
          Log.d("PointsBuffer", collector.filterPoints.get(4 * i)
                  +" "+ collector.filterPoints.get(4 * i + 1)
                  +" "+ collector.filterPoints.get(4 * i + 2));
        }

        int pickIndex = PointUtil.pickPoint(targetPoints, ray, rayOrigin);
        float[] seedPoint = PointUtil.getSeedPoint();

        float seedZLength = ray[0] * (seedPoint[0] - rayOrigin[0]) + ray[1] * (seedPoint[1] - rayOrigin[1]) + ray[2] * (seedPoint[2] - rayOrigin[2]);
        seedZLength = Math.abs(seedZLength);

        Log.d("seedZLength__", Float.toString(seedZLength));
        float roiRadius = unitRadius * seedZLength / 2;
        Log.d("UnitRadius", roiRadius +" "+ /*RMS*/roiRadius * 0.2f +" "+ roiRadius * 0.4f);

        if(pickIndex >= 0 && !Thread.currentThread().isInterrupted()) {
          httpTh = new Thread(() -> {
            isFound = false;
            RequestForm rf = new RequestForm();

            rf.setPointBufferDescription(targetPoints.capacity() / 4, 16, 0); //pointcount, pointstride, pointoffset
            rf.setPointDataDescription(roiRadius * 0.2f, roiRadius * 0.4f); //accuracy, meanDistance
            rf.setTargetROI(pickIndex, roiRadius);//seedIndex,touchRadius
            rf.setAlgorithmParameter(RequestForm.SearchLevel.NORMAL, RequestForm.SearchLevel.NORMAL);//LatExt, RadExp
            FindSurfaceRequester fsr = new FindSurfaceRequester(REQUEST_URL, true);
            // Request Find Surface
            try {
              Log.d("CylinderFinder", "request");
              targetPoints.rewind();
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
                        ", Radius: "+param.r + ", Normal Vector: "+Arrays.toString(tmp)+
                        ", RMS: "+resp.getRMS());

                float[] centerPose = new float[]{(param.b[0]+param.t[0])/2, (param.b[1]+param.t[1])/2, (param.b[2]+param.t[2])/2};
                anchorPoints = Pose.makeTranslation(centerPose);

                cylinderVars = new CylinderVars(param.r, tmp, centerPose);
                isFound = true;
              } else {
                Log.d("CylinderFinder", "request fail");
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
          httpTh.start();

          ArActivity.this.runOnUiThread(() -> {
            Snackbar.make(arLayout, "Please Wait...", Snackbar.LENGTH_LONG).show();
            try {
              httpTh.join();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            if(isFound && ArActivity.this.cylinderVars.getDbh() > 0.0f){
              Snackbar.make(arLayout, "Cylinder Found", Snackbar.LENGTH_LONG).show();

              final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                      ArActivity.this, R.style.BottomSheetDialogTheme
              );
              View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(
                              R.layout.layout_bottom_sheet,
                              (LinearLayout)findViewById(R.id.bottomSheetContainer)
                      );
              EditText mbottomdbh=bottomSheetView.findViewById(R.id.bottomdbh);
              Spinner dropdown = bottomSheetView.findViewById(R.id.bottomspecies);
              String[] items = new String[]{"은행","이팝", "배롱", "배롱","무궁화", "느티", "벚", "단풍", "백합", "메타","단풍","기타"};
              ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
              adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
              dropdown.setAdapter(adapter);

              mbottomdbh.setText(String.valueOf(ArActivity.this.cylinderVars.getDbh()*200));
              bottomSheetView.findViewById(R.id.confirmBtn).setOnClickListener(v1 -> {
                Map<String,Map<String,Object>> user = new HashMap<>();
                Map<String,Object> tree = new HashMap<>();
                EditText edit1 = bottomSheetView.findViewById(R.id.bottomname);
                EditText edit3 = bottomSheetView.findViewById(R.id.bottomdbh);
                EditText edit4 = bottomSheetView.findViewById(R.id.bottomheight);
                tree.put("treeName", edit1.getText().toString());
                tree.put("treeSpecies",dropdown.getSelectedItem().toString());
                tree.put("treeDBH", edit3.getText().toString());
                tree.put("treeHeight", edit4.getText().toString());
                //tree.put("treeLocation",location);
                Long tsLong = System.currentTimeMillis()/1000;
                String ts = (tsLong).toString();
                user.put(ts,tree);
                fstore.collection("tree").document(userID).set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void aVoid) {
                  }
                });
                bottomSheetDialog.dismiss();
              });
              bottomSheetDialog.setContentView(bottomSheetView);
              bottomSheetDialog.show();
            }
            else{
              Snackbar.make(arLayout, "PickSeed Again", Snackbar.LENGTH_LONG).show();
            }
          });
        }
      }
      return false;
    });

    for(String permission : REQUIRED_PERMISSSIONS){
      if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSSIONS, PERMISSION_REQUEST_CODE);
      }
    }
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
      virtualObject.createOnGlThread(/*context=*/ this, "models/andy.obj", "models/andy.png");
      virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);

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

    try {// Obtain the current frame from ARSession. When the configuration is set to
      // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
      // camera framerate.
      session.setCameraTextureName(backgroundRenderer.getTextureId());
      frame = session.update();
      Camera camera = frame.getCamera();

      // If frame is ready, render camera preview image to the GL surface.
      backgroundRenderer.draw(frame);

      // Get projection matrix.
      // Get camera matrix and draw.
      // Get multiple of proj matrix and view matrix
      float[] projmtx = new float[16];
      float[] viewmtx = new float[16];
      float[] vpMatrix = new float[16];
      camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
      camera.getViewMatrix(viewmtx, 0);

      Matrix.multiplyMM(vpMatrix,0, projmtx, 0, viewmtx,0);

      // Compute lighting from average intensity of the image.
      // The first three components are color scaling factors.
      // The last one is the average pixel intensity in gamma space.
      final float[] colorCorrectionRgba = new float[4];
      frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

      // Visualize tracked points.
      // Use try-with-resources to automatically release the point cloud.
      if(!isStaticView) {
        PointUtil.resetSeedPoint();
        try (PointCloud pointCloud = frame.acquirePointCloud()) {
          if (isRecording && collector != null) {
            collector.doCollect(pointCloud);
          }
          pointCloudRenderer.update(pointCloud);
          pointCloudRenderer.draw(viewmtx, projmtx);
        }
      } else {
        pointCloudRenderer.draw(viewmtx, projmtx);

        if(drawSeedState && PointUtil.getSeedPoint() != null){
          float[] seedPoint = PointUtil.getSeedPoint();

          pointCloudRenderer.draw_seedPoint(vpMatrix, seedPoint);
        }
      }
      float scaleFactor = 1.0f;
      // 평균낸 거 넣기만 하면 되나?
      if(isFound && ArActivity.this.cylinderVars.getDbh() > 0.0f){
        anchorPoints.toMatrix(anchorMatrix, 0);
        virtualObject.updateModelMatrix(anchorMatrix, scaleFactor);
        virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba);
      }

    } catch (Throwable t) {
      // Avoid crashing the application due to unhandled exceptions.
      Log.e(TAG, "Exception on the OpenGL thread", t);
    }
  }

  float[] screenPointToWorldRay(float xPx, float yPx, Frame frame) {
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
