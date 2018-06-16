package capstone.gonggancapsule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.time.LocalDate;
import java.util.ArrayList;

import capstone.gonggancapsule.database.DatabaseHelper;
import cn.refactor.lib.colordialog.ColorDialog;

public class TitleAdapter extends BaseAdapter {

    LayoutInflater inflater = null;
    private ArrayList<Capsule> titleList;
    private int titleCount;
    Context mContext;

    public TitleAdapter(Context context, ArrayList<Capsule> titleList) {
        this.mContext = context;
        this.titleList = titleList;
    }

    @Override
    public int getCount() {
        return titleList.size();
    }

    @Override
    public Object getItem(int position) {
        return titleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            final Context context = parent.getContext();
            if (inflater == null) {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.title_listview_item, parent, false);
        }

        TextView title_tv = (TextView) convertView.findViewById(R.id.title_tv);
        TextView create_date_tv = (TextView) convertView.findViewById(R.id.createdate_tv);

        Capsule capsuleData = this.titleList.get(position);

        title_tv.setText(capsuleData.getTitle());
        create_date_tv.setText(capsuleData.getCreate_date());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorDialog dialog = new ColorDialog(mContext);
                dialog.setColor("#d6697c"); //색깔 변경 가능
                dialog.setAnimationEnable(true);
                dialog.setTitle("DELETE");
                    dialog.setContentText(" '" + capsuleData.getTitle() + "' 의 일기를 삭제하시겠습니까?");
                    dialog.setPositiveListener("YES", new ColorDialog.OnPositiveListener() {
                        @Override
                        public void onClick(ColorDialog dialog) {
                            Toast.makeText(mContext, "삭제!", Toast.LENGTH_SHORT).show();
                            removeData(capsuleData);
                            dialog.dismiss();
                        }
                    });
                    dialog.setNegativeListener("CANCEL", new ColorDialog.OnNegativeListener() {
                        @Override
                        public void onClick(ColorDialog dialog) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
            }
        });

        return convertView;
    }

    public void removeData(Capsule capsule) {
        DatabaseHelper dbHelper = new DatabaseHelper(mContext, "capsule", null, 3);
        dbHelper.delete(capsule.getCapsule_id());
        --titleCount;
        notifyDataSetChanged();

        titleList.clear();
        titleList = dbHelper.getAllDiary();

        ((MainActivity)mContext).setTotalCount();
        notifyDataSetChanged();
        //((MainActivity)mContext).capsuleList.clear();
        //((MainActivity)mContext).capsuleList = dbHelper.getAllDiary();
    }
}
