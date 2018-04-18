package com.capstone.ako.addms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity {
    EditText email_input;
    TextView login_tv, signup_tv;
    ImageView background;
    EditText userName;
    EditText password;
    Verification v = new Verification();
    Button btn_Submit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // On screen elements
        login_tv = (TextView) findViewById(R.id.login);
        signup_tv = (TextView) findViewById(R.id.signup);
        email_input = (EditText) findViewById(R.id.email);
        background = (ImageView) findViewById(R.id.background);
        btn_Submit = (Button) findViewById(R.id.btn_Submit);
        // Set background scale and animation
        background.setScaleType(ImageView.ScaleType.FIT_XY);
        final Animation backgroundAnimation = AnimationUtils.loadAnimation(this, R.anim.background);
        background.setAnimation(backgroundAnimation);
        backgroundAnimation.start();

        /*
        This is Listener is for animation to help in looping
         */
        backgroundAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { /* Nothing */}

            // restart the animation (Loop)
            @Override
            public void onAnimationEnd(Animation animation) {
                backgroundAnimation.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { /* Nothing */}
        });


        /*
        This function is for login action
        When the user click on the login:
        the login underline should be visible and
        the email input filed should be invisible.
        Finally clean the email filed
         */
        login_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email_input.setVisibility(View.INVISIBLE);
                signup_tv.setBackgroundResource(0);
                login_tv.setBackgroundResource(R.drawable.bt_with_border);
                email_input.setText("");
            }
        });
        /*
        This function is for singup action
        When the user click on the singup:
        email input filed and the singup underline should be visible.
         */
        signup_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email_input.setVisibility(View.VISIBLE);
                login_tv.setBackgroundResource(0);
                signup_tv.setBackgroundResource(R.drawable.bt_with_border);
            }
        });
    }



    /** called when user trys to login into the application */
    public void nextScreen(View view) {
        userName = (EditText) findViewById(R.id.edt_User);
        password = (EditText) findViewById(R.id.edt_Pass);
       boolean check = v.verify(userName.getText().toString(),password.getText().toString());
        if(check == true) {
            startActivity(new Intent(Login.this, accountHomePage.class));
        }
        else{
            userName.setText("Wrong input");
        }
    }
}
