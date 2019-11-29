package com.example.facebooklogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private TextView displayName, emailID;
    private ImageView displayImage;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

   //     printKeyHash();
        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);

        displayName = findViewById(R.id.display_name);
        emailID = findViewById(R.id.email);
        displayImage = findViewById(R.id.image_view);
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("email","public_profile"));

        // Creating CallbackManager
        callbackManager = CallbackManager.Factory.create();
        // Registering CallbackManager with the LoginButton
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Retrieving access token using the LoginResult
                AccessToken accessToken = loginResult.getAccessToken();
            //    Toast.makeText(getApplicationContext(),""+accessToken.toString(),Toast.LENGTH_LONG).show();
                useLoginInformation(accessToken);

            }
            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(),"onCancel",Toast.LENGTH_LONG).show();
            }
            @Override
            public void onError(FacebookException error) {
                Log.e("TAG",error.toString());
                Toast.makeText(getApplicationContext(),"onError"+error.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            }

        });

    }
    private void printKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo("Your Package Name", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash: AA", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("KeyHash:", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("KeyHash:", e.toString());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
  //      accessTokenTracker.startTracking();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            useLoginInformation(accessToken);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // We stop the tracking before destroying the activity
    //    accessTokenTracker.stopTracking();
    }

    @Override
    public void onActivityResult(int requestCode, int resulrCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resulrCode, data);
        super.onActivityResult(requestCode, resulrCode, data);
    }

    private void useLoginInformation(AccessToken accessToken) {
        /**
         Creating the GraphRequest to fetch user details
         1st Param - AccessToken
         2nd Param - Callback (which will be invoked once the request is successful)
         **/
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            //OnCompleted is invoked once the GraphRequest is successful
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String name = object.getString("name");
                    String email = object.getString("email");
                    String image = object.getJSONObject("picture").getJSONObject("data").getString("url");
                //    Toast.makeText(getApplicationContext(),image,Toast.LENGTH_LONG).show();
                    Log.i("image",image);
                    displayName.setText(name);
                    Glide.with(getApplicationContext()).load(image).into(displayImage);
                    emailID.setText(email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        // We set parameters to the GraphRequest using a Bundle.
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);
        // Initiate the GraphRequest
        request.executeAsync();
    }

}
