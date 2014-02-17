package com.aleks.letstrip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: Aleks
 * Date: 16/02/14
 * Time: 19:35
 * To change this template use File | Settings | File Templates.
 */
public class BtListAdapter extends ArrayAdapter <String> {


    public BtListAdapter(Context context) {
        super(context, 0);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String name = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.component_list_item, null);
        }

        TextView nameView = (TextView) convertView.findViewById(R.id.device_name);
        if(name != null) {
            nameView.setText(name);
        }
        return convertView;
    }
}
