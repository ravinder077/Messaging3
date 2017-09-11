package com.liftersheaven.messaging.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.liftersheaven.messaging.Holder.QBUnreadMessageHolder;
import com.liftersheaven.messaging.R;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by cameron on 6/4/2017.
 */

public class ChatDialogAdapters extends BaseAdapter {

    private Context context;
    private ArrayList<QBChatDialog> qbChatDialogs;

    public ChatDialogAdapters(Context context, ArrayList<QBChatDialog> qbChatDialogs){
        this.context = context;
        this.qbChatDialogs = qbChatDialogs;
    }

    @Override
    public int getCount(){
        return qbChatDialogs.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatDialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
        {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_chat_dialog,null);

            TextView txtTitle,txtMessage;
            final ImageView imageView, image_unread;

            txtMessage = (TextView)view.findViewById(R.id.list_chat_dialog_message);
            txtTitle = (TextView)view.findViewById(R.id.list_chat_dialog_title);
            imageView = (ImageView)view.findViewById(R.id.image_chat_dialog);
            image_unread = (ImageView)view.findViewById(R.id.image_unread);

            txtMessage.setText(qbChatDialogs.get(position).getLastMessage());
            txtTitle.setText(qbChatDialogs.get(position).getName());

            ColorGenerator generator = ColorGenerator.MATERIAL;
            int randomColor = generator.getRandomColor();

            if (qbChatDialogs.get(position).getPhoto().equals("null")) {
                TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig()
                        .withBorder(4)
                        .endConfig()
                        .round();

                TextDrawable drawable = builder.build(txtTitle.getText().toString().substring(0, 1).toUpperCase(), randomColor);

                imageView.setImageDrawable(drawable);
            }
            else
            {
                QBContent.getFile(Integer.parseInt(qbChatDialogs.get(position).getPhoto()))
                        .performAsync(new QBEntityCallback<QBFile>(){
                            @Override
                            public void onSuccess(QBFile qbFile, Bundle bundle){
                                String fileURL = qbFile.getPublicUrl();
                                Picasso.with(context)
                                        .load(fileURL)
                                        .resize(50,50)
                                        .centerCrop()
                                        .into(imageView);
                            }

                            @Override
                            public void onError(QBResponseException e){
                                Log.e("ERROR_image",""+e.getMessage());
                            }
                        });
            }

            TextDrawable.IBuilder unreadBuilder = TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();
            int unread_count = QBUnreadMessageHolder.getInstance().getBundle().getInt(qbChatDialogs.get(position).getDialogId());
            if (unread_count > 0){
                TextDrawable unread_drawable = unreadBuilder.build(""+unread_count, Color.RED);
                image_unread.setImageDrawable(unread_drawable);
                    
                }
            }
        return view;
    }
}
