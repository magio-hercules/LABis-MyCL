package com.labis.mycl.login;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.labis.mycl.R;

import java.util.ArrayList;

public class UrlListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<UrlListItem> listViewItemList = new ArrayList<UrlListItem>() ;
    private Activity mActivity;
    private String mCurrentId;

    // ListViewAdapter의 생성자
    public UrlListViewAdapter(Activity activity) {
        mActivity = activity;
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_s3_url, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView textViewId = (TextView) convertView.findViewById(R.id.url_text_id) ;
        TextView textViewTitle = (TextView) convertView.findViewById(R.id.url_text_title) ;
        TextView textViewUrl = (TextView) convertView.findViewById(R.id.url_text_s3url) ;

        final UrlListItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        textViewId.setText(listViewItem.getTextId());
        textViewTitle.setText(listViewItem.getTextTitle());
        textViewUrl.setText(listViewItem.getTextUrl());


        //리스트뷰 클릭 이벤트
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, (pos+1)+"번째 리스트가 클릭되었습니다.", Toast.LENGTH_SHORT).show();

                mCurrentId =  listViewItemList.get(pos).getTextId();

                String url = listViewItemList.get(pos).getTextUrl();
                EditText editUrl = mActivity.findViewById(R.id.edit_url);
                editUrl.setText(url);

//                String url = mUrl.getText().toString();
//                Log.d("[TEST] ", "URL : " + url);
//                mActivity.loadImage(url);
//                UrlActivity.loadImage(url);
//                new UrlActivity.LoadImage().execute(url);
            }
        });

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String id, String title, String url) {
        UrlListItem item = new UrlListItem();
        item.setTextId(id);
        item.setTextTitle(title);
        item.setTextUrl(url);

        Log.e("[TEST]", "add item : " + id + ", " + title + ", " + url);

        listViewItemList.add(item);
    }

    public String getCurrentId() {
        return mCurrentId;
    }
}
