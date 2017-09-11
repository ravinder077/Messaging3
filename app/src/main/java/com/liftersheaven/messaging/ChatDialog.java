package com.liftersheaven.messaging;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.liftersheaven.messaging.Adapter.ChatDialogAdapters;
import com.liftersheaven.messaging.Common.Common;
import com.liftersheaven.messaging.Holder.QBChatDialogHolder;
import com.liftersheaven.messaging.Holder.QBUnreadMessageHolder;
import com.liftersheaven.messaging.Holder.QBUsersHolder;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static android.R.attr.id;

public class ChatDialog extends AppCompatActivity implements QBSystemMessageListener, QBChatDialogMessageListener{

    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    FloatingActionButton floatingActionButton;
    ListView ChatDialog;




    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_dialog_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch (item.getItemId()){
            case R.id.context_delete_dialog:
                deleteDialog(info.position);
                break;
        }

        return true;
    }

    private void deleteDialog(int index) {

        final QBChatDialog chatDialog = (QBChatDialog)ChatDialog.getAdapter().getItem(index);
        QBRestChatService.deleteDialog(chatDialog.getDialogId(),false)
                .performAsync(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        QBChatDialogHolder.getInstance().removeDialog(chatDialog.getDialogId());
                        ChatDialogAdapters adapter = new ChatDialogAdapters(getBaseContext(),QBChatDialogHolder.getInstance().getAllChatDialogs());
                        ChatDialog.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.chat_dialog_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.ads){
            Intent intent = new Intent(this, Ads.class);
            startActivity(intent);
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            return true;
        }
        if (id == R.id.keyboard){
            Intent intent = new Intent(this, Keyboard.class);
            startActivity(intent);
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            return true;
        }
        if (id == R.id.theme){
            Intent intent = new Intent(this, Theme.class);
            startActivity(intent);
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            return true;
        }
        if (id == R.id.more){
            Intent intent = new Intent(this, More.class);
            startActivity(intent);
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
            return true;
        }
        switch (item.getItemId()){
            case R.id.chat_dialog_user:
                showUserProfile();
                break;
            default:
                break;
        }
        return true;
    }

    private void showUserProfile() {
        Intent intent = new Intent(ChatDialog.this,UserProfile.class);
        startActivity(intent);
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadChatDialogs();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_dialog);

        MobileAds.initialize(this, "ca-app-pub-5227030558700264/1140117430");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5227030558700264/5266583171");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Toolbar toolbar = (Toolbar)findViewById(R.id.chatdialog_toolbar);
        toolbar.setTitle("DM");
        setSupportActionBar(toolbar);

        createSessionForChat();

        ChatDialog = (ListView) findViewById(R.id.ChatDialog);

        registerForContextMenu(ChatDialog);

        ChatDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog qbChatDialog = (QBChatDialog)ChatDialog.getAdapter().getItem(position);
                Intent intent = new Intent(ChatDialog.this, ChatMessage.class);
                intent.putExtra(Common.DIALOG_EXTRA, qbChatDialog);
                startActivity(intent);
            }
        });

        loadChatDialogs();



        floatingActionButton = (FloatingActionButton)findViewById(R.id.chatdialog_adduser);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDialog.this, ListUsers.class);
                startActivity(intent);
            }
        });

    }


    private void loadChatDialogs() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);

        QBRestChatService.getChatDialogs(null, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {

                QBChatDialogHolder.getInstance().putDialogs(qbChatDialogs);


                Set<String> setIds = new HashSet<String>();
                for (QBChatDialog chatDialog:qbChatDialogs)
                setIds.add(chatDialog.getDialogId());
                QBRestChatService.getTotalUnreadMessagesCount(setIds, QBUnreadMessageHolder.getInstance().getBundle())
                        .performAsync(new QBEntityCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer, Bundle bundle) {


                                QBUnreadMessageHolder.getInstance().setBundle(bundle);

                                ChatDialogAdapters adapter = new ChatDialogAdapters(getBaseContext(),QBChatDialogHolder.getInstance().getAllChatDialogs());
                                ChatDialog.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                Log.e("ERROR",e.getMessage());

                            }
                        });
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR",e.getMessage());
            }
        });
    }

    private void createSessionForChat() {
        final ProgressDialog mDialog = new ProgressDialog(ChatDialog.this);
        mDialog.setMessage("please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String user, password;
        user = getIntent().getStringExtra("user");
        password = getIntent().getStringExtra("password");
        QBSettings.getInstance().init(getApplicationContext(), "58687", "HXn8gHERu-MwDNp", "UH5CSgkAtVaDvq7");
       // QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
        //QBSettings.getInstance().fastConfigInit("58687", "HXn8gHERu-MwDNp", "UH5CSgkAtVaDvq7");
        QBSettings.getInstance().setAccountKey("HmMSqt16G2zeJvZsexfj");
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUsersHolder.getInstance().putUsers(qbUsers);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

        final QBUser qbUser = new QBUser(user,password);
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbUser.setId(qbSession.getUserId());
                try {
                    qbUser.setPassword(BaseService.getBaseService().getToken());
                }   catch (BaseServiceException e){
                    e.printStackTrace();
                }

                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        mDialog.dismiss();

                        QBSystemMessagesManager qbSystemMessageManager = QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessageManager.addSystemMessageListener(ChatDialog.this);

                        QBIncomingMessagesManager qbIncomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
                        qbIncomingMessagesManager.addDialogMessageListener(ChatDialog.this);

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR",""+e.getMessage());
                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

    }

    @Override
    public void processMessage(QBChatMessage qbChatMessage) {
        QBRestChatService.getChatDialogById(qbChatMessage.getBody()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {

                QBChatDialogHolder.getInstance().putDialog(qbChatDialog);
                ArrayList<QBChatDialog> adapterSource = QBChatDialogHolder.getInstance().getAllChatDialogs();
                ChatDialogAdapters adapters = new ChatDialogAdapters(getBaseContext(),adapterSource);
                ChatDialog.setAdapter(adapters);
                adapters.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Log.e("ERROR",""+e.getMessage());
    }

    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        loadChatDialogs();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

    }
}
