package com.right.tuo.scanandunlock.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.right.tuo.scanandunlock.R;
import com.right.tuo.scanandunlock.bean.BaseResponse;
import com.right.tuo.scanandunlock.bean.LabelNumberAdapter;
import com.right.tuo.scanandunlock.bean.LabelNumberBean;
import com.right.tuo.scanandunlock.http.HttpClient;
import com.right.tuo.scanandunlock.http.HttpService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanBindingActivity extends AppCompatActivity implements View.OnClickListener {


    private Button btnHand, btnInput, btnUpload, btnClear;
    private EditText etAware;
    private EditText etScan;
    private CheckBox cbCanInput;

    private TextView tvHasInput, tvBicycleNo, tvSensorsNo;

    private ListView mLv;
    private LabelNumberAdapter mAdapter;
    private List<LabelNumberBean> labelNumberBeans = new ArrayList<>();


    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_binding);

        tvHasInput = (TextView) findViewById(R.id.tv_has_upload);
        tvHasInput.setText("已上传" + count + "条数据");


        btnHand = (Button) findViewById(R.id.btn_hand);
        btnHand.setOnClickListener(this);
        btnInput = (Button) findViewById(R.id.btn_input);
        btnInput.setOnClickListener(this);
        btnUpload = (Button) findViewById(R.id.btn_upload);
        btnUpload.setOnClickListener(this);
        btnClear = (Button) findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_hand:
                break;
            case R.id.btn_input:
                judgeNull();
                break;
            case R.id.btn_upload:
                upLoad();
                break;
            case R.id.btn_clear:
                clear();
                break;
        }
    }


    private void input() {


    }


    private void upLoad() {

        if (labelNumberBeans.size() == 0) {
            Toast.makeText(this, "你还未录入数据", Toast.LENGTH_SHORT).show();
            return;
        }

        String data = new Gson().toJson(labelNumberBeans);

        Call<BaseResponse<String>> call = HttpClient.getInstance().getRetrofit().create(HttpService.class).batchBindSensorsNo("token", data);

        call.enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(Call<BaseResponse<String>> call, Response<BaseResponse<String>> response) {
                // codeId: 1成功 其他为失败
                int codeId = response.body().getCodeId();
                if (codeId == 1) {
                    count += labelNumberBeans.size();
                    tvHasInput.setText("已上传" + count + "条数据");
                    Toast.makeText(ScanBindingActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                    labelNumberBeans.clear();
                    mAdapter.notifyDataSetChanged();
                } else {

                    Toast.makeText(ScanBindingActivity.this, response.body().getCodeDes(), Toast.LENGTH_SHORT).show();

                    showDialog();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<String>> call, Throwable t) {

            }
        });
    }

    private void showDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("是否删除录入的错误数据")
                .setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        labelNumberBeans.clear();
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .setPositiveButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();

    }


    private void clear() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("是否清空录入的数据")
                .setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        labelNumberBeans.clear();
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .setPositiveButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }


    private void judgeNull() {
        String bicycleNo = tvBicycleNo.getText().toString().trim();

        if (TextUtils.isEmpty(bicycleNo)) {
            Toast.makeText(this, "车辆编号为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String sensorsNo = tvSensorsNo.getText().toString().trim();

        if (TextUtils.isEmpty(sensorsNo)) {
            Toast.makeText(this, "标签编号为空", Toast.LENGTH_SHORT).show();
            return;
        }

        add(bicycleNo, sensorsNo);
    }

    // 检查数据是否已被录入
    private boolean check(String sensorsNo, String bicycleNo) {
        boolean flag = false;
        for (LabelNumberBean labelNumberBean : labelNumberBeans) {
            if (labelNumberBean.getSensorsNo().equals(sensorsNo)) {
                Toast.makeText(this, "该电子标签已被录入,请检查", Toast.LENGTH_SHORT).show();
                flag = true;
                break;
            }
        }

        for (LabelNumberBean labelNumberBean : labelNumberBeans) {
            if (labelNumberBean.getBicycleNo().equals(bicycleNo)) {
                Toast.makeText(this, "该二维码已被录入,请检查", Toast.LENGTH_SHORT).show();
                flag = true;
                break;
            }
        }

        return flag;
    }


    public void add(String sensorsNo, String bicycleNo) {

        // 检查电子标签和二维码是否录入
        boolean flag = check(sensorsNo, bicycleNo);

        if (flag) {
            return;
        }

        LabelNumberBean bean = new LabelNumberBean();

        bean.setBicycleNo(bicycleNo);
        bean.setSensorsNo(sensorsNo);


        labelNumberBeans.add(bean);
        mAdapter.notifyDataSetChanged();
    }
}
