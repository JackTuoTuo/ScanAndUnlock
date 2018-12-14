package com.right.tuo.scanandunlock.bean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.right.tuo.scanandunlock.R;

import java.util.List;


public class LabelNumberAdapter extends BaseAdapter {

    private Context mContext;
    private List<LabelNumberBean> mLabelNumberBeans;

    public LabelNumberAdapter(Context context, List<LabelNumberBean> labelNumberBeans) {
        this.mContext = context;
        this.mLabelNumberBeans = labelNumberBeans;
    }

    @Override
    public int getCount() {
        return mLabelNumberBeans == null ? 0 : mLabelNumberBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return mLabelNumberBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_label_number, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        LabelNumberBean bean = mLabelNumberBeans.get(position);
        viewHolder.tvBicycleNo.setText(bean.getBicycleNo());
        viewHolder.tvSensorsNO.setText(bean.getSensorsNo());
        return convertView;
    }

    public static class ViewHolder {
        TextView tvBicycleNo;
        TextView tvSensorsNO;

        public ViewHolder(View view) {
            tvBicycleNo = (TextView) view.findViewById(R.id.tv_bicycle_no);
            tvSensorsNO = (TextView) view.findViewById(R.id.tv_sensors_no);
        }
    }
}
