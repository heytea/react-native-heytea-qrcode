package com.heyteago.qrcode.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.heyteago.qrcode.Constants;
import com.heyteago.qrcode.R;
import com.heyteago.qrcode.RNHeyteaQRCodeModule;
import com.heyteago.qrcode.Utils;
import com.heyteago.qrcode.camera.CameraManager;
import com.heyteago.qrcode.decoding.CaptureActivityHandler;
import com.heyteago.qrcode.decoding.InactivityTimer;
import com.heyteago.qrcode.decoding.RGBLuminanceSource;
import com.heyteago.qrcode.view.ViewfinderView;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pub.devrel.easypermissions.EasyPermissions;


/**
 * Initial the camera
 **/
public class CaptureActivity extends AppCompatActivity implements Callback, View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_SCAN_GALLERY = 100;

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private ImageButton back;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private ProgressDialog mProgress;
    private String photo_path;
    private Bitmap scanBitmap;
    private LinearLayoutCompat flashLightLayout;   // 打开闪光灯
    private LinearLayoutCompat albumLayout; // 打开相册
    private AppCompatImageView flashLightIv; // 闪光灯图片
    private TextView flashLightTv;// 闪光灯文字

    private boolean isFlashOn = false;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scanner);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_content);
        flashLightLayout = (LinearLayoutCompat) findViewById(R.id.flashLightLayout);
        albumLayout = (LinearLayoutCompat) findViewById(R.id.albumLayout);
        back = (ImageButton) findViewById(R.id.btn_back);
        flashLightIv = findViewById(R.id.flashLightIv);
        flashLightTv = findViewById(R.id.flashLightTv);
        flashLightLayout.setOnClickListener(this);
        albumLayout.setOnClickListener(this);
        back.setOnClickListener(this);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);

        //添加toolbar
        addToolbar();
    }

    private void addToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }


    /**
     * 手动选择照片
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SCAN_GALLERY:
                    //获取选中图片的路径
                    photo_path = Utils.getRealPathFromUri(this, data.getData());
                    mProgress = new ProgressDialog(CaptureActivity.this);
                    mProgress.setMessage("正在扫描...");
                    mProgress.setCancelable(false);
                    mProgress.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Result result = scanningImage(photo_path);
                            if (result != null) {
                                Intent resultIntent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putString(Constants.INTENT_EXTRA_KEY_QR_SCAN, result.getText());
                                resultIntent.putExtras(bundle);
                                CaptureActivity.this.setResult(RESULT_OK, resultIntent);
                                mProgress.dismiss();
                                RNHeyteaQRCodeModule.setQRCodeResult(result);
                                finish();
                            } else {
                                mProgress.dismiss();
                                Message m = handler.obtainMessage();
                                m.what = R.id.decode_failed;
                                m.obj = "Scan failed!";
                                handler.sendMessage(m);
                            }
                        }
                    }).start();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        RNHeyteaQRCodeModule.setQRCodeResult(null);
    }

    /**
     * 扫描二维码图片的方法
     *
     * @param path
     * @return
     */
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        String reg = ".+(.jpg|.bmp|.jpeg|.png|.gif|.JPG|.BMP|.JPEG|.PNG|.GIF)$";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(path);
        if (!matcher.find()) {
            return null;
        }
        if (!new File(path).exists()) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.scanner_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
        super.onDestroy();
    }

    /**
     * Handler scan result
     * 扫码后得到的结果
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        if (TextUtils.isEmpty(resultString)) {
            Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.INTENT_EXTRA_KEY_QR_SCAN, resultString);
        // 不能使用Intent传递大于40kb的bitmap，可以使用一个单例对象存储这个bitmap
//            bundle.putParcelable("bitmap", barcode);
//            Logger.d("saomiao",resultString);
        resultIntent.putExtras(bundle);
        this.setResult(RESULT_OK, resultIntent);
        RNHeyteaQRCodeModule.setQRCodeResult(result);
        CaptureActivity.this.finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_back) {
            finish();
        } else if (id == R.id.flashLightLayout) {
            try {
                boolean isSuccess = CameraManager.get().setFlashLight(!isFlashOn);
                if (!isSuccess) {
                    Toast.makeText(CaptureActivity.this, "暂时无法开启闪光灯", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isFlashOn) {
                    // 关闭闪光灯
                    isFlashOn = false;

                } else {
                    // 开启闪光灯
                    isFlashOn = true;
                }
                switchFlashImg(isFlashOn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.albumLayout) {
            this.selectPhoto();
        }
    }

    /**
     * @param flashState 切换闪光灯图片
     */
    public void switchFlashImg(Boolean flashState) {

        if (flashState) {
            flashLightIv.setImageResource(R.drawable.ic_open);
            flashLightTv.setText("关闭闪光灯");
        } else {
            flashLightIv.setImageResource(R.drawable.ic_close);
            flashLightTv.setText("打开闪光灯");
        }

    }


    /**
     * 相册选择图片
     */
    private void selectPhoto() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            navigatePhoto();
        } else {
            EasyPermissions.requestPermissions(this, "Select QRCode pic need storage permission", 0x666, perms);
        }
    }

    private void navigatePhoto() {
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); // "android.intent.action.GET_CONTENT"
        innerIntent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, "Select QRCode pic");
        startActivityForResult(wrapperIntent, REQUEST_CODE_SCAN_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == 0x666) {
            navigatePhoto();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}