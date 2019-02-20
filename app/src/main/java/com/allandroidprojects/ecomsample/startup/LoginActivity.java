package com.allandroidprojects.ecomsample.startup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.utility.PrefManager;
import com.allandroidprojects.ecomsample.utility.RetrofitClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private PrefManager prefManager;
    private EditText mEmail, mPassword;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefManager = new PrefManager(this);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        submit = (Button) findViewById(R.id.email_sign_in_button);

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View view) {
                String email = String.valueOf(mEmail.getText());
                String password = String.valueOf(mPassword.getText());

                Boolean validEmail = validate(email);
                if(!validEmail) {
                    Snackbar errorSnackbar = Snackbar.make(view, "Enter a valid email", 2000);
                    errorSnackbar.show();
                }
                else if(TextUtils.isEmpty(password)) {
                    Snackbar errorSnackbar = Snackbar.make(view, "Password field is empty", 2000);
                    errorSnackbar.show();
                }
                else {
                    //Call to login using retrofit
                    Call<ResponseBody> call = RetrofitClient
                            .getInstance()
                            .getApi()
                            .login(email, password);

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                                if(response.body() != null) {  //Succesful login
                                    prefManager.setIsLoggedIn(true);
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                }
                                else { //Invalid credentials
                                    Snackbar errorSnackbar = Snackbar.make(view, "Invalid credentials", 2000);
                                    errorSnackbar.show();
                                }

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Snackbar errorSnackbar = Snackbar.make(view, t.getMessage(), 2000);
                            errorSnackbar.show();
                        }
                    });
                }
            }
        });

    }

    Boolean validate(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

}

