/*
 * Copyright (C) 2008 ZXing authors
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

package com.right.tuo.scanandunlock.zxing.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.right.tuo.scanandunlock.R;
import com.right.tuo.scanandunlock.zxing.BeepManager;
import com.right.tuo.scanandunlock.zxing.CaptureActivityHandler;
import com.right.tuo.scanandunlock.zxing.InactivityTimer;
import com.right.tuo.scanandunlock.zxing.ViewfinderView;
import com.right.tuo.scanandunlock.zxing.camera.CameraManager;
import com.right.tuo.scanandunlock.zxing.decode.DecodeFormatManager;
import com.right.tuo.scanandunlock.zxing.ext.CallBack;
import com.right.tuo.scanandunlock.zxing.util.Utils;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureFragment extends Fragment implements SurfaceHolder.Callback {

    private static final String TAG = CaptureFragment.class.getSimpleName();

    public static final String EXTRA_DECODE_FORMAT_SET = "EXTRA_DECODE_FORMAT_SET";

    //默认一次扫描成功后 3秒开始进行第二次扫描
    private static long RESCAN_TIME_MS = 1000;

    ViewfinderView viewfinderView;
    SurfaceView surfaceView;
    ImageView ivFlashLight;
    FrameLayout rootView;

    private Activity mActivity;
    private Collection<BarcodeFormat> mDecodeFormats;
    private Map<DecodeHintType, ?> mDecodeHints;

    private boolean hasSurface;
    private CameraManager mCameraManager;
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private CallBack mCallback;
    private Point mScreenResolution;

    /**
     * 创建实例
     *
     * @param decodeFormatValues 编码，例如: QR_CODE, CODE_39, CODE_128
     *                           这里约定传字符串数组就行了
     */
    public static CaptureFragment newInstance(String[] decodeFormatValues) {
        CaptureFragment fragment = new CaptureFragment();
        Bundle b = new Bundle();
        b.putStringArray(EXTRA_DECODE_FORMAT_SET, decodeFormatValues);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View view = inflater.inflate(R.layout.capture, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mActivity = getActivity();

        initFindViewId();
        initData();
        initDecodeFormats();
        initListener();

        hasSurface = false;
        inactivityTimer = new InactivityTimer(mActivity);
        beepManager = new BeepManager(mActivity);

        PreferenceManager.setDefaultValues(mActivity, R.xml.preferences, false);
    }

    private void initFindViewId() {
        viewfinderView = (ViewfinderView) mActivity.findViewById(R.id.viewfinder_view);
        surfaceView = (SurfaceView) mActivity.findViewById(R.id.preview_view);
        ivFlashLight = (ImageView) mActivity.findViewById(R.id.iv_flash_light);
        rootView = (FrameLayout) mActivity.findViewById(R.id.fragment_capture);
    }

    private void initData() {
        int width = getContext().getResources().getDisplayMetrics().widthPixels;
        int height = getContext().getResources().getDisplayMetrics().heightPixels;
        mScreenResolution = new Point(width, height);
        Log.i(TAG, "mScreenResolution: " + mScreenResolution);
    }

    /**
     * 初始化条码类型
     */
    private void initDecodeFormats() {
        // 初始化条码类型
        if (getArguments() != null) {
            String[] set = getArguments().getStringArray(EXTRA_DECODE_FORMAT_SET);
            if (set != null) {
                Set<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
                for (String s : set) {
                    try {
                        BarcodeFormat format = BarcodeFormat.valueOf(s);
                        decodeFormats.add(format);
                    } catch (Exception e) {
                        // 如果获取枚举异常，忽略，继续
                        e.printStackTrace();
                    }
                }
                mDecodeFormats = decodeFormats;
            }
        } else {
            mDecodeFormats = DecodeFormatManager.getDefaultSupportDecodeFormats();
        }
    }

    private void initListener() {
        // 闪光灯
        ivFlashLight.setTag(false);
        ivFlashLight.setImageLevel(0);
        ivFlashLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = (boolean) ivFlashLight.getTag();
                flag = !flag;
                ivFlashLight.setTag(flag);
                ivFlashLight.setImageLevel(flag ? 1 : 0);
                setTorch(flag);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        mCameraManager = new CameraManager(this);

        viewfinderView.setCameraManager(mCameraManager);

        handler = null;

        mActivity.setRequestedOrientation(Utils.getCurrentOrientation(mActivity));

        beepManager.updatePrefs();

        inactivityTimer.onResume();

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.

            //表面的活动暂停了，但没有停止，所以表面仍然存在。因此，不会调用表面ecreinit()，所以在这里init摄像头。
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    @Override
    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        mCameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged: ");
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult   The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode     A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();
        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            // Then not from history, so beep/vibrate and we have an image to draw on
            beepManager.playBeepSoundAndVibrate();
            drawResultPoints(barcode, scaleFactor, rawResult);
            // callback
            if (mCallback != null) {
                mCallback.success(rawResult);
            }

            // rescan for next
            inactivityTimer.onResume();
            restartPreviewAfterDelay(RESCAN_TIME_MS);
        }
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
     *
     * @param barcode     A bitmap of the captured image.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param rawResult   The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_points));
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
                 drawLine(canvas, paint, points[0], points[1], scaleFactor);
            } else if (points.length == 4 &&
                    (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                            rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and metadata
                drawLine(canvas, paint, points[0], points[1], scaleFactor);
                drawLine(canvas, paint, points[2], points[3], scaleFactor);
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    if (point != null) {
                        canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
                    }
                }
            }
        }
    }

    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
        if (a != null && b != null) {
            canvas.drawLine(scaleFactor * a.getX(),
                    scaleFactor * a.getY(),
                    scaleFactor * b.getX(),
                    scaleFactor * b.getY(),
                    paint);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (mCameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, mDecodeFormats, mDecodeHints,
                        null, mCameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        new AlertDialog.Builder(mActivity)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.msg_camera_framework_bug))
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mActivity.finish();
                    }
                })
                .show();
    }

    /**
     * 重新扫描
     *
     * @param delayMS
     */
    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    public void setTorch(boolean flag) {
        mCameraManager.setTorch(flag);
    }

    public Point getCaptureViewRect() {
        return new Point(viewfinderView.getWidth(), viewfinderView.getHeight());
    }

    public Point getScreenResolution() {
        return mScreenResolution;
    }

    public void setupSurfaceViewSize(int w, int h) {
        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        params.width = w;
        params.height = h;
        surfaceView.setLayoutParams(params);
        Log.i(TAG, "setupSurfaceViewSize: " + surfaceView.getLayoutParams().width
                + "x" + surfaceView.getLayoutParams().height);
    }

    public void setCallBack(CallBack callBack) {
        this.mCallback = callBack;
    }

    /**
     * 修改扫描间隔时间
     *
     * @param rescanTimeMs 间隔时间 ms
     */
    public static void setRescanTimeMs(long rescanTimeMs) {
        RESCAN_TIME_MS = rescanTimeMs;
    }
}
