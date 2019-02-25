package com.allandroidprojects.ecomsample.user;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.startup.MainActivity;
import com.allandroidprojects.ecomsample.utility.PrefManager;
import com.allandroidprojects.ecomsample.utility.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.LENGTH_LONG;

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

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String email = String.valueOf(mEmail.getText());
                String password = String.valueOf(mPassword.getText());

                Boolean validEmail = validate(email);
                if (!validEmail) {
                    Snackbar errorSnackbar = Snackbar.make(view, "Enter a valid email", 2000);
                    errorSnackbar.show();
                } else if (TextUtils.isEmpty(password)) {
                    Snackbar errorSnackbar = Snackbar.make(view, "Password field is empty", 2000);
                    errorSnackbar.show();
                } else {
                    //Call to login using retrofit
                    Call<ResponseBody> call = RetrofitClient
                            .getInstance()
                            .getApi()
                            .login(email, password);

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                            System.out.println("\n\n"+response.toString()+"\n\n");
                            if (response.body() != null) {  //Succesful login
                                prefManager.setIsLoggedIn(true);
                                prefManager.setUserName(email);
                                Toast.makeText(LoginActivity.this, "Signin Successful", LENGTH_LONG).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();

                            } else { //Invalid credentials
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

        TextView register = (TextView) findViewById(R.id.register_user);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    Boolean validate(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

}

