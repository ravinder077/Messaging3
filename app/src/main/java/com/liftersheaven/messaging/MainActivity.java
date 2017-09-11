package com.liftersheaven.messaging;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettingsSaver;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.model.QBEntity;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import android.Manifest;

import static com.liftersheaven.messaging.R.id.textView;

public class MainActivity extends Activity {
    protected SQLiteDatabase database;
    static final String APP_ID = "58687";
    static final String AUTH_KEY = "HXn8gHERu-MwDNp";
    static final String AUTH_SECRET = "UH5CSgkAtVaDvq7";
    static final String ACCOUNT_KEY = "HmMSqt16G2zeJvZsexfj";

    static final int REQUEST_CODE = 1000;

    Button btnLogin;
    EditText edtUser, edtPassword;
    TextView btnCreate, btnCance, btnCanc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        requestRuntimePermission();

        initializeFramework();

        btnLogin = (Button) findViewById(R.id.main_btnLogin);
        btnCreate = (TextView) findViewById(R.id.main_btnCreate);
        btnCance = (TextView)findViewById(R.id.term1);
        btnCanc  = (TextView)findViewById(R.id.policy1);

        edtPassword = (EditText) findViewById(R.id.main_editPassword);
        edtUser = (EditText) findViewById(R.id.main_editLogin);

        btnCance.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Terms.class);
                startActivity(intent);
            }
        });

        btnCanc.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Privacy.class);
                startActivity(intent);
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, CreateAccount.class);
                startActivity(intent);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = edtUser.getText().toString();
                final String password = edtPassword.getText().toString();

                QBUser qbUser = new QBUser(user, password);

                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(getBaseContext(), "Login successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent (MainActivity.this, ChatDialog.class);
                        intent.putExtra("user", user);
                        intent.putExtra("password",password);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });


try {
    database = openOrCreateDatabase("dm", MODE_PRIVATE, null);
    String TableName = "users";
    Cursor c = database.rawQuery("SELECT * FROM " + TableName, null);

    int Column1 = c.getColumnIndex("user");

    int Column2 = c.getColumnIndex("password");
    c.moveToFirst();


    if (c != null) {
        String Column11 = c.getString(Column1);
        String Column22 = c.getString(Column1);
        System.out.println("Column11   " + Column11);
        System.out.println("Column22   " + Column22);
        Toast.makeText(getBaseContext(), "Signing as "+Column11, Toast.LENGTH_SHORT).show();

        QBUser qbUser = new QBUser(Column11, Column22);

        QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
               // Toast.makeText(getBaseContext(), "Login successful", Toast.LENGTH_SHORT).show();

              //  Intent intent = new Intent(MainActivity.this, ChatDialog.class);
             //   intent.putExtra("user", qbUser.getLogin());
             //   intent.putExtra("password", qbUser.getPassword());
             //   startActivity(intent);
              //  finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(getBaseContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
catch (Exception e)
{
    e.printStackTrace();
}
    }

    private void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CODE);

            }
        }
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case REQUEST_CODE:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getBaseContext(),"Permission Granted",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getBaseContext(),"Permission Denied",Toast.LENGTH_SHORT).show();
            }
                break;
        }
    }

    private void initializeFramework() {
        QBSettings.getInstance().init(getApplicationContext(),APP_ID,AUTH_KEY,AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);

    }

}