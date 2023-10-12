package com.example.myfinances;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class ViewHolder {
    ImageView mFlag;
}
public class SpinnerAdapter extends ArrayAdapter<String> {
    int[] spinnerImages;
    Context mContext;

    public SpinnerAdapter(@NonNull Context context, int[] images) {
        super(context, R.layout.spinner_row);
        this.spinnerImages = images;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return spinnerImages.length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder mViewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.spinner_row, parent, false);
            mViewHolder.mFlag = (ImageView) convertView.findViewById(R.id.iv_icon);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.mFlag.setImageResource(spinnerImages[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position,convertView,parent);
    }
}
