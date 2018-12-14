package com.right.tuo.scanandunlock.zxing.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.right.tuo.scanandunlock.R;
import com.right.tuo.scanandunlock.ui.MainActivity;
import com.right.tuo.scanandunlock.zxing.ext.CallBack;

/**
 * 描述
 *
 * @author xuhj
 * @version 1.0.0
 * @since 2017/9/27
 */
public class QRCodeActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";

    TextView tvTitle;
    TextView tvInput;

    private CaptureFragment mCaptureFragment;

    private String mTitle;

    public static void newInstanceForResult(Activity activity, int requestCode) {
        newInstanceForResult(activity, requestCode, "");
    }

    public static void newInstanceForResult(Activity activity, int requestCode, String title) {
        Intent i = new Intent(activity, QRCodeActivity.class);
        i.putExtra(EXTRA_TITLE, title);
        activity.startActivityForResult(i, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        initFindViewId();
        initData();
        initView();
        initListener();

    }

    private void initFindViewId() {
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvInput = (TextView) findViewById(R.id.tv_input);
    }

    private void initData() {
        mTitle = getIntent().getStringExtra(EXTRA_TITLE);

        mCaptureFragment = (CaptureFragment) getSupportFragmentManager().findFragmentById(R.id.frag_capture);
        mCaptureFragment.setCallBack(new CallBack() {
            @Override
            public void success(Result result) {
                setQRResult(result);
            }
        });
    }

    private void initView() {
        if (mTitle != null) {
            tvTitle.setText(mTitle);
        }
    }

    private void initListener() {
        tvInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog(QRCodeActivity.this);
            }
        });
    }

    /**
     * 显示手动输入dialog
     */
    private void showInputDialog(final Context context) {
        final EditText editText = new EditText(context);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("请输入")
                .setView(editText)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .create();
        // 此处需要重写确定按钮的监听事件，屏蔽系统点击确定时会自动消失的逻辑
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 如果输入框内容为空时，显示提示信息
                        String text = editText.getText().toString().trim();
                        if (text.length() == 0) {
                            Toast.makeText(QRCodeActivity.this, "请输入", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        setQRResult(new Result(text, new byte[0], new ResultPoint[0], null));
                        dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }

    /**
     * 设置返回内容
     */
    private void setQRResult(Result result) {
//        Intent i = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putString("result", result.getText());
//        i.putExtras(bundle);
//        this.setResult(RESULT_OK, i);
//        this.finish();

        MainActivity.newInstance(this, result.getText());


    }

    /**
     * 获取扫描返回数据
     *
     * @param intent OnActivityResult中带的intent
     * @return 取出result
     */
    public static String getQRResultString(Intent intent) {


        return intent.getStringExtra("result");
    }


   /* public void unlock(String number) {

        if (number.equals("")) {
            Toast.makeText(this, "数据为空", Toast.LENGTH_LONG).show();
            return;
        }

        Call<BaseResponse<String>> call = HttpClient.getInstance().getRetrofit().create(HttpService.class).unlock(number);

        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                // codeId: 1成功 其他为失败
                int codeId = response.body().getCodeId();
                if (codeId == 1) {
                    Toast.makeText(QRCodeActivity.this, response.body().getCodeDes(), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(QRCodeActivity.this, response.body().getCodeDes(), Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                Toast.makeText(QRCodeActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }*/

}
