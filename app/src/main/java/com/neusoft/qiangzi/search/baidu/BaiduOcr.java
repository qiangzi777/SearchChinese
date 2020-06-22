package com.neusoft.qiangzi.search.baidu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baidu.ocr.sdk.model.OcrRequestParams;
import com.baidu.ocr.sdk.model.OcrResponseResult;
import com.baidu.ocr.sdk.model.ResponseResult;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.baidu.ocr.ui.camera.CameraNativeHelper;
import com.baidu.ocr.ui.camera.CameraView;

import java.io.File;

import androidx.appcompat.app.AlertDialog;

public class BaiduOcr implements OnResultListener<GeneralResult>{
    private static final int REQUEST_CODE_GENERAL = 105;
    private static final int REQUEST_CODE_GENERAL_BASIC = 106;
    private static final int REQUEST_CODE_ACCURATE_BASIC = 107;
    private static final int REQUEST_CODE_ACCURATE = 108;
    private static final int REQUEST_CODE_GENERAL_ENHANCED = 109;
    private static final int REQUEST_CODE_GENERAL_WEBIMAGE = 110;
    private static final int REQUEST_CODE_BANKCARD = 111;
    private static final int REQUEST_CODE_VEHICLE_LICENSE = 120;
    private static final int REQUEST_CODE_DRIVING_LICENSE = 121;
    private static final int REQUEST_CODE_LICENSE_PLATE = 122;
    private static final int REQUEST_CODE_BUSINESS_LICENSE = 123;
    private static final int REQUEST_CODE_RECEIPT = 124;

    private static final int REQUEST_CODE_PASSPORT = 125;
    private static final int REQUEST_CODE_NUMBERS = 126;
    private static final int REQUEST_CODE_QRCODE = 127;
    private static final int REQUEST_CODE_BUSINESSCARD = 128;
    private static final int REQUEST_CODE_HANDWRITING = 129;
    private static final int REQUEST_CODE_LOTTERY = 130;
    private static final int REQUEST_CODE_VATINVOICE = 131;
    private static final int REQUEST_CODE_CUSTOM = 132;
    public static final int REQUEST_CODE_PICK_IMAGE_FRONT = 201;
    public static final int REQUEST_CODE_PICK_IMAGE_BACK = 202;
    public static final int REQUEST_CODE_CAMERA = 102;

    private static final String YOUR_BAIDU_API_KEY = "QMLIaNv0f2PMMSRwdzaPSFb9";
    private static final String YOUR_BAIDU_SECRET_KEY = "q4BxzrTjlylaeOIfoXrAS33a0oph1vyV";
    public static final int ID_CARD_FRONT = 0;
    public static final int ID_CARD_BACK = 1;
    private static final String TAG = "BaiduOcr";

    private boolean hasGotToken = false;
    private Activity context;
    static private BaiduOcr instance = null;
    private AlertDialog.Builder alertDialog;
    private OnShotImageListener onImageListener;
    private OnRecgResultListener onRecgResultListener;
    private String imgFilePath;

    public BaiduOcr(Activity context) {
        this.context = context;
        instance = this;
    }

    //    public void setContext(Activity context){
//        this.context = context;
//    }
    static public BaiduOcr getInstance(Activity context) {
        if (instance == null) {
            new BaiduOcr(context);
        } else {
            instance.context = context;
        }
        return instance;
    }

    /**
     * 用明文ak，sk初始化
     */
    public void init() {
        OCR.getInstance(context).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                hasGotToken = true;
                //  初始化本地质量控制模型,释放代码在onDestory中
                //  调用身份证扫描必须加上 intent.putExtra(CameraActivity.KEY_NATIVE_MANUAL, true); 关闭自动初始化和释放本地模型
                CameraNativeHelper.init(context, OCR.getInstance(context).getLicense(),
                        new CameraNativeHelper.CameraNativeInitCallback() {
                            @Override
                            public void onError(int errorCode, Throwable e) {
                                String msg;
                                switch (errorCode) {
                                    case CameraView.NATIVE_SOLOAD_FAIL:
                                        msg = "加载so失败，请确保apk中存在ui部分的so";
                                        break;
                                    case CameraView.NATIVE_AUTH_FAIL:
                                        msg = "授权本地质量控制token获取失败";
                                        break;
                                    case CameraView.NATIVE_INIT_FAIL:
                                        msg = "本地质量控制";
                                        break;
                                    default:
                                        msg = String.valueOf(errorCode);
                                }
                                alertText("初始化失败", "本地质量控制初始化错误，错误原因： " + msg);
                            }
                        });
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                alertText("AK，SK方式获取token失败", error.getMessage());
            }
        }, context, YOUR_BAIDU_API_KEY, YOUR_BAIDU_SECRET_KEY);
    }

    public void release(){
        CameraNativeHelper.release();
        OCR.getInstance(context).release();
    }

    // 通用文字识别
    public void startCommonCharActivityForResult() {
        if (!hasGotToken) {
            Toast.makeText(context, "token还未成功获取", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(context).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        context.startActivityForResult(intent, REQUEST_CODE_GENERAL_BASIC);
    }

    // 通用文字识别（含位置信息版）
    public void startCommonCharWithPosActivityForResult() {
        if (!hasGotToken) {
            Toast.makeText(context, "token还未成功获取", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(context).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        context.startActivityForResult(intent, REQUEST_CODE_GENERAL);
    }

    // 通用文字识别(高精度版)
    public void startHighAccCharActivityForResult() {
        if (!hasGotToken) {
            Toast.makeText(context, "token还未成功获取", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(context).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        context.startActivityForResult(intent, REQUEST_CODE_ACCURATE_BASIC);
    }

    // 通用文字识别（含位置信息高精度版）
    public void startHighAccCharWithPosActivityForResult() {
        if (!hasGotToken) {
            Toast.makeText(context, "token还未成功获取", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(context).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        context.startActivityForResult(intent, REQUEST_CODE_ACCURATE);
    }

    // 通用文字识别（含生僻字版）
    public void startCommonCharEnhanceActivityForResult() {
        if (!hasGotToken) {
            Toast.makeText(context, "token还未成功获取", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(context).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        context.startActivityForResult(intent, REQUEST_CODE_GENERAL_ENHANCED);
    }

    // 网络图片识别
    public void startWebImageActivityForResult() {
        if (!hasGotToken) {
            Toast.makeText(context, "token还未成功获取", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(context).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        context.startActivityForResult(intent, REQUEST_CODE_GENERAL_WEBIMAGE);
    }

    // 数字识别
    public void startNumberActivityForResult() {
        if (!hasGotToken) {
            Toast.makeText(context, "token还未成功获取", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(context).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        context.startActivityForResult(intent, REQUEST_CODE_NUMBERS);
    }

    // 手写识别
    public void startHandWrittingActivityForResult() {
        if (!hasGotToken) {
            Toast.makeText(context, "token还未成功获取", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(context).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        context.startActivityForResult(intent, REQUEST_CODE_HANDWRITING);
    }

    // 身份证识别
    public void startIdCardActivityForResult(boolean isFront, boolean isAuto) {
        if (!hasGotToken) {
            Toast.makeText(context, "token还未成功获取", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(context).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
        if (isAuto) {
            intent.putExtra(CameraActivity.KEY_NATIVE_ENABLE, true);
            // KEY_NATIVE_MANUAL设置了之后CameraActivity中不再自动初始化和释放模型
            // 请手动使用CameraNativeHelper初始化和释放模型
            // 推荐这样做，可以避免一些activity切换导致的不必要的异常
            intent.putExtra(CameraActivity.KEY_NATIVE_MANUAL, true);
        }
        if (isFront)
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
        else
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_BACK);

        context.startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    private void alertText(final String title, final String message) {
        alertDialog = new AlertDialog.Builder(context);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }

    public boolean isHasGotToken() {
        return hasGotToken;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private int requestCode;
    public void setImageResult(Intent data) {

        requestCode = data.getIntExtra("requestCode", 0);
        int resultCode = data.getIntExtra("resultCode", 0);

        imgFilePath = FileUtil.getSaveFile(context).getAbsolutePath();
        onImageListener.imageResult(imgFilePath);

        if(resultCode != Activity.RESULT_OK){
            Log.e(TAG, "setImageResult: resultCode is not ok.");
            return;
        }
        switch (requestCode){
            case REQUEST_CODE_GENERAL:// 识别成功回调，通用文字识别（含位置信息）
                recGeneral(context, imgFilePath);
                break;
            case REQUEST_CODE_ACCURATE:// 识别成功回调，通用文字识别（含位置信息高精度版）:
                recAccurate(context, imgFilePath);
                break;
            case REQUEST_CODE_GENERAL_BASIC: // 识别成功回调，通用文字识别
                recGeneralBasic(context, imgFilePath);
                break;
            case REQUEST_CODE_ACCURATE_BASIC: // 识别成功回调，通用文字识别（高精度版）
                recAccurateBasic(context, imgFilePath);
                break;
            case REQUEST_CODE_GENERAL_ENHANCED: // 识别成功回调，通用文字识别（含生僻字版）
                recGeneralEnhanced(context, imgFilePath);
                break;
            case REQUEST_CODE_GENERAL_WEBIMAGE: // 识别成功回调，网络图片文字识别
                recWebimage(context, imgFilePath);
                break;
            case REQUEST_CODE_NUMBERS:// 识别成功回调，数字
                recNumbers(context, imgFilePath);
                break;
            case REQUEST_CODE_HANDWRITING:// 识别成功回调，手写
                recHandwriting(context, imgFilePath);
                break;
            case REQUEST_CODE_CAMERA://身份证识别，手动
                if (data != null) {
                    String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                    if (!TextUtils.isEmpty(contentType)) {
                        if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                            recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, imgFilePath);
                        } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
                            recIDCard(IDCardParams.ID_CARD_SIDE_BACK, imgFilePath);
                        }
                    }
                }
                break;
            case REQUEST_CODE_PICK_IMAGE_FRONT://身份证识别，自动:
                Uri uri = data.getData();
                imgFilePath = getRealPathFromURI(uri);
                recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, imgFilePath);
                break;
            case REQUEST_CODE_PICK_IMAGE_BACK: //身份证识别，自动
                Uri uri1 = data.getData();
                imgFilePath = getRealPathFromURI(uri1);
                recIDCard(IDCardParams.ID_CARD_SIDE_BACK, imgFilePath);
                break;
        }
    }


    private void recGeneral(Context ctx, String filePath) {
        GeneralParams param = new GeneralParams();
        param.setDetectDirection(true);
        param.setVertexesLocation(true);
        param.setRecognizeGranularity(GeneralParams.GRANULARITY_SMALL);
        param.setImageFile(new File(filePath));
        OCR.getInstance(ctx).recognizeGeneral(param, this);
    }

    //通用文字识别（含位置信息高精度版）
    private void recAccurate(Context ctx, String filePath) {
        GeneralParams param = new GeneralParams();
        param.setDetectDirection(true);
        param.setVertexesLocation(true);
        param.setRecognizeGranularity(GeneralParams.GRANULARITY_SMALL);
        param.setImageFile(new File(filePath));
        OCR.getInstance(ctx).recognizeAccurate(param, this);
    }

    private void recAccurateBasic(Context ctx, final String filePath) {
        GeneralParams param = new GeneralParams();
        param.setDetectDirection(true);
        param.setVertexesLocation(true);
        param.setRecognizeGranularity(GeneralParams.GRANULARITY_SMALL);
        param.setImageFile(new File(filePath));
        OCR.getInstance(ctx).recognizeAccurateBasic(param, this);
    }


    private void recGeneralBasic(Context ctx, final String filePath) {
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(filePath));
        OCR.getInstance(ctx).recognizeGeneralBasic(param, this);
    }

    private void recWebimage(Context ctx, String filePath) {
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(filePath));
        OCR.getInstance(ctx).recognizeWebimage(param, this);
    }

    private void recNumbers(Context ctx, String filePath) {
        OcrRequestParams param = new OcrRequestParams();
        param.setImageFile(new File(filePath));
        OCR.getInstance(ctx).recognizeNumbers(param, new OnResultListener<OcrResponseResult>() {
            @Override
            public void onResult(OcrResponseResult result) {
                onRecgResultListener.onResult(result);
                alertText("识别结果", result.getJsonRes());
            }

            @Override
            public void onError(OCRError error) {
                alertText("识别失败", error.getMessage());
            }
        });
    }

    private void recHandwriting(Context ctx, String filePath) {
        OcrRequestParams param = new OcrRequestParams();
        param.setImageFile(new File(filePath));
        OCR.getInstance(ctx).recognizeHandwriting(param, new OnResultListener<OcrResponseResult>() {
            @Override
            public void onResult(OcrResponseResult result) {
                onRecgResultListener.onResult(result);
                alertText("识别结果", result.getJsonRes());
            }

            @Override
            public void onError(OCRError error) {
                alertText("识别失败", error.getMessage());
            }
        });
    }

    private void recGeneralEnhanced(Context ctx, String filePath) {
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(filePath));
        OCR.getInstance(ctx).recognizeGeneralEnhanced(param, this);
    }

    private void recLottery(Context ctx, String filePath) {
        OcrRequestParams param = new OcrRequestParams();
        param.setImageFile(new File(filePath));
        OCR.getInstance(ctx).recognizeLottery(param, new OnResultListener<OcrResponseResult>() {
            @Override
            public void onResult(OcrResponseResult result) {
                onRecgResultListener.onResult(result);
            }

            @Override
            public void onError(OCRError error) {
                alertText("识别失败", error.getMessage());
            }
        });
    }

    private void recIDCard(String idCardSide, String filePath) {

        IDCardParams param = new IDCardParams();
        param.setImageFile(new File(filePath));
        // 设置身份证正反面
        param.setIdCardSide(idCardSide);
        // 设置方向检测
        param.setDetectDirection(true);
        // 设置图像参数压缩质量0-100, 越大图像质量越好但是请求时间越长。 不设置则默认值为20
        param.setImageQuality(20);

        OCR.getInstance(context).recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(IDCardResult result) {
                onRecgResultListener.onResult(result);
            }

            @Override
            public void onError(OCRError error) {
                alertText("识别失败", error.getMessage());
            }
        });
    }

    @Override
    public void onResult(GeneralResult result) {
        onRecgResultListener.onResult(result);
        //alertText("识别结果", result.getJsonRes());
        switch (requestCode){
            case REQUEST_CODE_ACCURATE_BASIC: // 识别成功回调，通用文字识别（高精度版）
                Toast.makeText(context,"高精度文字识别成功！",Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CODE_GENERAL_BASIC: // 识别成功回调，通用文字识别
                Toast.makeText(context,"通用文字识别成功！",Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CODE_GENERAL:// 识别成功回调，通用文字识别（含位置信息）
                Toast.makeText(context,"通用+文字识别成功！",Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CODE_ACCURATE:// 识别成功回调，通用文字识别（含位置信息高精度版）:
                Toast.makeText(context,"高精度+文字识别成功！",Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CODE_GENERAL_ENHANCED: // 识别成功回调，通用文字识别（含生僻字版）
                break;
            case REQUEST_CODE_GENERAL_WEBIMAGE: // 识别成功回调，网络图片文字识别
                break;
            case REQUEST_CODE_NUMBERS:// 识别成功回调，数字
                break;
            case REQUEST_CODE_HANDWRITING:// 识别成功回调，手写
                break;
            default:
                break;
        }
    }

    @Override
    public void onError(OCRError error) {
        //免费限额已满
        if (error.getErrorCode() == 4 ||
                error.getErrorCode() == 17 ||
                error.getErrorCode() == 18 ||
                error.getErrorCode() == 19) {
            switch (requestCode){
                case REQUEST_CODE_ACCURATE_BASIC: // 识别成功回调，通用文字识别（高精度版） 500
//                    alertText("识别失败", "高精度，今天免费限额已满！");
//                    Toast.makeText(context,"高精度限额已满，换通用文字识别",Toast.LENGTH_SHORT).show();
                    requestCode = REQUEST_CODE_GENERAL_BASIC;
                    recGeneralBasic(context, imgFilePath);//尝试调用更低级别接口
                    break;
                case REQUEST_CODE_GENERAL_BASIC: // 识别成功回调，通用文字识别 50000
//                    alertText("识别失败", "今天所有免费限额已满，明天再用吧！");
                    requestCode = REQUEST_CODE_GENERAL;
                    recGeneral(context, imgFilePath);
                    break;
                case REQUEST_CODE_GENERAL:// 识别成功回调，通用文字识别（含位置信息） 500
//                    alertText("识别失败", "通用带位置识别，今天免费限额已满！");
                    requestCode = REQUEST_CODE_ACCURATE;
                    recAccurate(context, imgFilePath);//尝试调用更低级别接口
                    break;
                case REQUEST_CODE_ACCURATE:// 识别成功回调，通用文字识别（含位置信息高精度版） 50
//                    alertText("识别失败", "高精度带位置，今天免费限额已满！");
                    alertText("识别失败", "今天免费限额已满，明天再用吧！");
                    break;
                case REQUEST_CODE_GENERAL_ENHANCED: // 识别成功回调，通用文字识别（含生僻字版）
                    break;
                case REQUEST_CODE_GENERAL_WEBIMAGE: // 识别成功回调，网络图片文字识别
                    break;
                case REQUEST_CODE_NUMBERS:// 识别成功回调，数字
                    break;
                case REQUEST_CODE_HANDWRITING:// 识别成功回调，手写
                    break;
            }
        }else {
            alertText("识别失败", error.getMessage());
        }
    }
    public void setOnImageListener(OnShotImageListener listener) {
        onImageListener = listener;
    }

    public void setOnRecgResultListener(OnRecgResultListener listener) {
        onRecgResultListener = listener;
    }

    public interface OnShotImageListener {
        void imageResult(String fpath);
    }

    public interface OnRecgResultListener {
//        void onIdCardResult(IDCardResult result);

//        void onResult(String result);

        void onResult(ResponseResult result);

//        void onError(OCRError error);
    }

    public void destroy() {
        // 释放本地质量控制模型
        CameraNativeHelper.release();
    }
}
