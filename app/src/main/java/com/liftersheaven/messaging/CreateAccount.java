package com.liftersheaven.messaging;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class CreateAccount extends Activity {

    protected SQLiteDatabase database;
    Button btnSignUp;
    EditText edtUser, edtPassword, edtFullName;
    TextView btnCancel, btnCance, btnCanc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_account);

        registerSession();

        btnSignUp = (Button)findViewById(R.id.main_btnCreateA);
        btnCancel  = (TextView)findViewById(R.id.main_btnCancel);
        btnCance = (TextView)findViewById(R.id.textView5);
        btnCanc  = (TextView)findViewById(R.id.policy);

        edtPassword  = (EditText) findViewById(R.id.sign_editPassword);
        edtUser  = (EditText) findViewById(R.id.sign_editLogin);
        edtFullName = (EditText) findViewById(R.id.main_editFullName);

        btnCancel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(CreateAccount.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btnCance.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(CreateAccount.this, Terms.class);
                startActivity(intent);
            }
        });

        btnCanc.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(CreateAccount.this, Privacy.class);
                startActivity(intent);
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = edtUser.getText().toString();
                String password = edtPassword.getText().toString();




                QBUser qbUser = new QBUser(user, password);
                 final  String  user1=user;
                final String  password1=password;
                qbUser.setFullName(edtFullName.getText().toString());

                QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(), "Account Created", Toast.LENGTH_SHORT).show();
                        database = openOrCreateDatabase("dm", MODE_PRIVATE, null);
                        String TableName="users";
                  /* Create a Table in the Database. */
                        database.execSQL("CREATE TABLE IF NOT EXISTS "
                                + TableName
                                + " (user VARCHAR, password VARCHAR);");

   /* Insert data to a Table*/
                        database.execSQL("INSERT INTO "
                                + TableName
                                + " (user, password)"
                                + " VALUES ('"+user1+"','"+password1+"');");

   /*retrieve data from database */

                        Cursor c = database.rawQuery("SELECT * FROM " + TableName , null);

                        int Column1 = c.getColumnIndex("user");

                        int Column2 = c.getColumnIndex("password");
                        c.moveToFirst();
                        String  Column11=c.getString(Column1);
                        String   Column22=c.getString(Column1);
                        System.out.println("Column11   "+Column11);
                        System.out.println("Column22   "+Column22);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void registerSession() {
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("ERROR",e.getMessage());
            }
        });
    }
}
