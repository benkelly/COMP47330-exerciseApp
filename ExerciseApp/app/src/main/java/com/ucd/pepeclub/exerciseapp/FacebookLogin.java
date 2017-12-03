package com.ucd.pepeclub.exerciseapp;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class FacebookLogin extends AppCompatActivity {

    BackgroundDataBaseTasks backgroundTask = new BackgroundDataBaseTasks(this);


    CallbackManager callbackManager;
    LoginButton loginButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean loggedIn = AccessToken.getCurrentAccessToken() != null;
        if (loggedIn) {
            Intent intent = new Intent(FacebookLogin.this, MainMenu.class);
            startActivity(intent);
        }
        else {
            setContentView(R.layout.activity_facebook_login);
            loginButton = (LoginButton) findViewById(R.id.login_button);
            loginButton.setReadPermissions(Arrays.asList("public_profile", "user_friends"));
            callbackManager = CallbackManager.Factory.create();
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    loginButton.setVisibility(View.GONE);

                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject jsonObject,
                                                        GraphResponse response) {
                                    try {
                                        String id = jsonObject.getString("id");
                                        String name = jsonObject.getString("name");

                                        SharedPreferences userInfo =  getSharedPreferences("user_info",
                                                Context.MODE_PRIVATE);

                                        SharedPreferences.Editor editor = userInfo.edit();
                                        editor.putString("id", id);
                                        editor.putString("name", name);
                                        editor.commit();

                                        //send id and name to php for new user storage
                                        String method = "register";
                                        backgroundTask.execute(method,name,id);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,friends");
                    request.setParameters(parameters);
                    request.executeAsync();

                    Intent intent  = new Intent(FacebookLogin.this, MainMenu.class);
                    startActivity(intent);
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException error) {

                }
            });
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if you don't add following block,
        // your registered `FacebookCallback` won't be called
        if (callbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }
}
