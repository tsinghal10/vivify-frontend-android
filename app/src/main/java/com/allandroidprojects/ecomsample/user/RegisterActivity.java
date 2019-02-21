package com.allandroidprojects.ecomsample.user;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.allandroidprojects.ecomsample.R;
import com.allandroidprojects.ecomsample.utility.RetrofitClient;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.LENGTH_LONG;

public class RegisterActivity extends AppCompatActivity {
    private EditText username, email, password, confirm_password;
    private Button register_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = (EditText) findViewById(R.id.user_name);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        confirm_password = (EditText) findViewById(R.id.confirm_password);
        register_button = (Button) findViewById(R.id.register_button);

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String user_name = username.getText().toString();
                final String email_id = email.getText().toString();
                final String pass_word = password.getText().toString();
                final String confirm_pass_word = confirm_password.getText().toString();

                Boolean validEmail = validate(email_id);
                if (TextUtils.isEmpty(user_name)) {
                    Snackbar errorSnackbar = Snackbar.make(view, "User Name field is empty", 2000);
                    errorSnackbar.show();
                } else if (!validEmail) {
                    Snackbar errorSnackbar = Snackbar.make(view, "Enter a valid email", 2000);
                    errorSnackbar.show();
                } else if (TextUtils.isEmpty(pass_word) || TextUtils.isEmpty(confirm_pass_word)) {
                    Snackbar errorSnackbar = Snackbar.make(view, "Password field is empty", 2000);
                    errorSnackbar.show();
                } else if (!TextUtils.equals(pass_word, confirm_pass_word)) {
                    Snackbar errorSnackbar = Snackbar.make(view, "Passwords do not match", 2000);
                    errorSnackbar.show();
                } else {
                    Call<ResponseBody> call = RetrofitClient
                            .getInstance()
                            .getApi()
                            .register(email_id, user_name, pass_word);

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            if (response.body() != null) {

                                Toast.makeText(RegisterActivity.this, "Registered Successfully", LENGTH_LONG).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                Snackbar errorSnackbar = Snackbar.make(view, "Email Id already Exists", 2000);
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
