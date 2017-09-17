package com.liftersheaven.messaging;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.liftersheaven.messaging.Adapter.ListUsersAdapter;
import com.liftersheaven.messaging.Common.Common;
import com.liftersheaven.messaging.Holder.QBUsersHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.Consts;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.List;

public class ListUsers extends AppCompatActivity {

    ListView lstUsers;
    Button btnCreate;
    public Context context=null;

    String mode="";
    QBChatDialog qbChatDialog;
    List<QBUser> userAdd=new ArrayList<>();
    ArrayList<QBUser> qbUserSearch = new ArrayList<QBUser>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);
        context=getBaseContext();
        Toolbar toolbar = (Toolbar)findViewById(R.id.chatusers_toolbar);
        toolbar.setTitle("Users");
        setSupportActionBar(toolbar);

        mode = getIntent().getStringExtra(Common.UPDATE_MODE);
        qbChatDialog=(QBChatDialog)getIntent().getSerializableExtra(Common.UPDATE_DIALOG_EXTRA);

        lstUsers = (ListView)findViewById(R.id.lstUsers);
        lstUsers.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        btnCreate = (Button)findViewById(R.id.btn_create_chat);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mode == null) {


                    int countChoice = lstUsers.getCount();

                    if (lstUsers.getCheckedItemPositions().size() == 1)
                        createPrivateChat(lstUsers.getCheckedItemPositions());
                    else if (lstUsers.getCheckedItemPositions().size() > 1)
                        createGroupChat(lstUsers.getCheckedItemPositions());
                    else
                        Toast.makeText(ListUsers.this, "Select a friend to chat with", Toast.LENGTH_SHORT).show();

                } else if (mode.equals(Common.UPDATE_ADD_MODE) && qbChatDialog != null) {
                    if (userAdd.size() > 0) {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();

                        int cntChoice = lstUsers.getCount();
                        SparseBooleanArray checkItemPositions = lstUsers.getCheckedItemPositions();
                        for (int i = 0; i < cntChoice; i++) {
                            if (checkItemPositions.get(i)) {
                                QBUser user = (QBUser) lstUsers.getItemAtPosition(i);
                                requestBuilder.addUsers(user);
                            }
                        }
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(getBaseContext(), "Add user success", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {

                                    }
                                });
                    }
                } else if (mode.equals(Common.UPDATE_REMOVE_MODE) && qbChatDialog != null) {
                    if (userAdd.size() > 0) {
                        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                        int cntChoice = lstUsers.getCount();
                        SparseBooleanArray checkItemPositions = lstUsers.getCheckedItemPositions();
                        for (int i = 0; i < cntChoice; i++) {
                            if (checkItemPositions.get(i)) {
                                QBUser user = (QBUser) lstUsers.getItemAtPosition(i);
                                requestBuilder.removeUsers(user);
                            }
                        }
                        QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder)
                                .performAsync(new QBEntityCallback<QBChatDialog>() {
                                    @Override
                                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                        Toast.makeText(getBaseContext(), "Remove user success", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {

                                    }
                                });
                    }
                }
            }
        });

        if (mode == null && qbChatDialog == null)
            retrieveAllUser();
        else{
            if (mode.equals(Common.UPDATE_ADD_MODE))
                loadListAvailableUser();
            else if (mode.equals(Common.UPDATE_REMOVE_MODE))
                loadListUserInGroup();
        }
    }

    private void loadListUserInGroup() {

        btnCreate.setText("Remove User");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        List<Integer> occupantsId = qbChatDialog.getOccupants();
                        List<QBUser> listUserAlreadyInGroup = QBUsersHolder.getInstance().getUsersByIds(occupantsId);
                        ArrayList<QBUser> users = new ArrayList<QBUser>();
                        users.addAll(listUserAlreadyInGroup);

                        ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(),users);
                        lstUsers.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        userAdd = users;
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ListUsers.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadListAvailableUser() {
        btnCreate.setText("Add User");

        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId())
                .performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        ArrayList<QBUser> listUsers = QBUsersHolder.getInstance().getAllUsers();
                        List<Integer> occupantsId = qbChatDialog.getOccupants();
                        List<QBUser>listUserAlreadyInChatGroup = QBUsersHolder.getInstance().getUsersByIds(occupantsId);

                        for (QBUser user:listUserAlreadyInChatGroup)
                            listUsers.remove(user);
                        if (listUsers.size() > 0){
                            ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(),listUsers);
                            lstUsers.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            userAdd = listUsers;
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(ListUsers.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void createGroupChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog mDialog = new ProgressDialog(ListUsers.this);
        mDialog.setMessage("Waiting...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        int countChoice = lstUsers.getCount();
        ArrayList<Integer> occupantIdsList = new ArrayList<>();
        for (int i =0;i<countChoice;i++){
            if (checkedItemPositions.get(i)){
                QBUser user = (QBUser)lstUsers.getItemAtPosition(i);
                occupantIdsList.add(user.getId());
            }
        }

        QBChatDialog dialog = new QBChatDialog();
        dialog.setName(Common.createChatDialogName(occupantIdsList));
        dialog.setType(QBDialogType.GROUP);
        dialog.setOccupantsIds(occupantIdsList);

        QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                mDialog.dismiss();
                Toast.makeText(getBaseContext(), "Chat dialog successfully created", Toast.LENGTH_SHORT).show();
                finish();

                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());
                for (int i=0;i<qbChatDialog.getOccupants().size();i++) {
                    qbChatMessage.setRecipientId(qbChatDialog.getOccupants().get(i));
                    try {
                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }


                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR",e.getMessage());
            }
        });
    }

    private void createPrivateChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog mDialog = new ProgressDialog(ListUsers.this);
        mDialog.setMessage("Waiting...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        int countChoice = lstUsers.getCount();
        for(int i=0;i<countChoice;i++){
            if (checkedItemPositions.get(i)){
                final QBUser user = (QBUser)lstUsers.getItemAtPosition(i);
                QBChatDialog dialog = DialogUtils.buildPrivateDialog(user.getId());

                QBRestChatService.createChatDialog(dialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        mDialog.dismiss();
                        Toast.makeText(getBaseContext(), "Private chat dialog successfully created", Toast.LENGTH_SHORT).show();
                        finish();

                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        QBChatMessage qbChatMessage = new QBChatMessage();
                        qbChatMessage.setRecipientId(user.getId());
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        try {
                            qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }

                        finish();

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("ERROR",e.getMessage());

                    }
                });
            }
        }

    }



    private void retrieveAllUser() {


        if(QBChatService.getInstance().getUser()!=null)
        {

            System.err.println("user not set");
        }
        else
        {
            System.err.println("user is set");
        }
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {


                try {
                    QBUsersHolder.getInstance().putUsers(qbUsers);

                    ArrayList<QBUser> qbUserWithoutCurrent = new ArrayList<QBUser>();
                    System.err.println("qbUsers" + qbUsers);
                    for (QBUser user : qbUsers) {
                        if (!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin()))
                            qbUserWithoutCurrent.add(user);
                    }
                    qbUserSearch.addAll(qbUserWithoutCurrent);
                    ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(), qbUserWithoutCurrent);
                    lstUsers.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR", e.getMessage());
            }
        });
    }

    public void getUserById(String  ls)
    {

        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(10);

        Bundle params = new Bundle();

        ArrayList<QBUser> users = null;
        try {
            users = QBUsers.getUsersByFullName("ravi", pagedRequestBuilder, params).perform();
        } catch (QBResponseException e) {
            e.printStackTrace();
        }

        if(users != null){
            System.err.println(">>> Users: " + users.toString());
            System.err.println( "currentPage: " + params.getInt(Consts.CURR_PAGE));
            System.err.println( "perPage: " + params.getInt(Consts.PER_PAGE));
            System.err.println( "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
        }
    }



    private class FetchUserByName extends AsyncTask<String ,String,ArrayList<QBUser>>
    {

        @Override
        protected ArrayList<QBUser> doInBackground(String... params1) {

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByFullName(params1[0], pagedRequestBuilder, params).perform();
            } catch (QBResponseException e) {
                e.printStackTrace();
            }

            if(users != null){
                System.err.println(">>> Users: " + users.toString());
                System.err.println( "currentPage: " + params.getInt(Consts.CURR_PAGE));
                System.err.println( "perPage: " + params.getInt(Consts.PER_PAGE));
                System.err.println( "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }

            return users ;
        }

        @Override
        protected void onPostExecute(ArrayList<QBUser> result) {


            try {
                System.err.println("found userare");
                System.err.println("found userare" + result);
                if (result != null) {
                    ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(), result);
                    lstUsers.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                   // Toast.makeText(getBaseContext(), "No Users Found ", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e)
{

    e.printStackTrace();
}
        }





    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu, menu);
        getMenuInflater().inflate(R.menu.chat_users_menu, menu);


        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();

        final EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        searchEditText.setHint("Search");


        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                FetchUserByName ss=new FetchUserByName();
                ss.execute(query);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //CardFriendFragment cardFriendFragment=new CardFriendFragment();

                //  cardFriendFragment.myFilter(newText);
                // System.err.println("qbUserSearch"+qbUserSearch);




                //getUserById("helloxz");




                for (QBUser user : qbUserSearch){

                    System.err.println("searchuserid  "+user.getLogin());
                }
                ArrayList<QBUser> qbUserArrayListSearch=new ArrayList<QBUser>();
                for (QBUser user : qbUserSearch){

                    if(user.getLogin().contains(newText))
                    {
                        System.err.println("searchuseridusers  "+user.getLogin());
                        qbUserArrayListSearch.add(user);
                    }
                }

               ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(),qbUserArrayListSearch);
                lstUsers.setAdapter(adapter);
               adapter.notifyDataSetChanged();

                /*ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(),qbUserSearch);
                adapter.filter(newText,qbUserSearch);
                System.err.println("Searched Text "+newText);*/
                return false;

            }
        });


        return true;
    }



}
