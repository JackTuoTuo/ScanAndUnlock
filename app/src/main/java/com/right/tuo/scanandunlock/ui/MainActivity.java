package com.right.tuo.scanandunlock.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.right.tuo.scanandunlock.R;
import com.right.tuo.scanandunlock.bean.BaseResponse;
import com.right.tuo.scanandunlock.http.HttpClient;
import com.right.tuo.scanandunlock.http.HttpService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {


    private TextView tvNum;

    private ProgressDialog progressDialog;


    public static void newInstance(Context context, String result) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("result", result);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvNum = (TextView) findViewById(R.id.tv_number);

        String result = getIntent().getExtras().getString("result");
        String number = result.substring(result.indexOf("=") + 1);

        tvNum.setText(number);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("请稍后");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }


    public void openLock(View view) {

        progressDialog.show();

        String number = tvNum.getText().toString().trim();

        Call<BaseResponse<String>> call = HttpClient.getInstance().getRetrofit().create(HttpService.class).openLock(number);

        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    // codeId: 1成功 其他为失败
                    int codeId = response.body().getCodeId();
                    if (codeId == 1) {
                        Toast.makeText(MainActivity.this, response.body().getCodeDes(), Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(MainActivity.this, response.body().getCodeDes(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void closeLock(View view) {

        progressDialog.show();

        String number = tvNum.getText().toString().trim();

        Call<BaseResponse<String>> call = HttpClient.getInstance().getRetrofit().create(HttpService.class).closeLock(number);

        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    // codeId: 1成功 其他为失败
                    int codeId = response.body().getCodeId();
                    if (codeId == 1) {
                        Toast.makeText(MainActivity.this, response.body().getCodeDes(), Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(MainActivity.this, response.body().getCodeDes(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
                }



            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
