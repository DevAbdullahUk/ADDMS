package com.capstone.ako.addms;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class accountScreen extends AppCompatActivity {
    // On screen components activity_account_screen2.xml
    TextView parentsUserName;
    LinearLayout accountLayout, selectedParentsNameLayout;
    // On screen elements
    TextView userName_tv,userPasswordOLD_tv,userPasswordNew_tv,userEmail_tv;
    // create Verification objects
    Verification verification = new Verification(this);
    // set the id
    final static int VIEW_ID = 2;
    // Parents List
    List<String> parentsList = new ArrayList<>();
    // user name
    static String userNmae;
    // Alert the user when adding a new parent
    AlertDialog dialog;
    // check to see it is the first time adding a new parent
    boolean firstTimeAddParent = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_screen);

        // initialize the elements
        userName_tv = (TextView) findViewById(R.id.userNameSetting);
        userPasswordOLD_tv = (TextView) findViewById(R.id.userPasswordOldSetting);
        userPasswordNew_tv = (TextView) findViewById(R.id.userPasswordNewSetting);
        userEmail_tv = (TextView) findViewById(R.id.userEmailSetting);

        // Set the user name from Preferences
        final SharedPreferences login_Data = PreferenceManager.getDefaultSharedPreferences(this);
        userNmae = login_Data.getString("userName","0");
        userName_tv.setText(userNmae);

        // get the list of the parents in the background
        parentsList = verification.parentsControl(0,"",userNmae);

        // Create a dialog box to be used when first time adding a new parent
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_parent).setTitle("Adding a new parent");
        dialog = builder.create();
    }

         /*
          This method is used to change this activity layout xml.
          The view Id of the caller element on the screen(TextView) will decide which
          layout should be replaced
           */
    public void moveScreen(View view) {
        if ( view.getId() == findViewById(R.id.parents).getId()) {
            setContentView(R.layout.activity_account_screen2);

            for (int i = 0; i<parentsList.size(); i++){
                AddParentsList(parentsList.get(i));
            }
            } else {
            userName_tv.setText(userNmae);
            setContentView(R.layout.activity_account_screen);
            userName_tv.setText(userNmae);
        }
    }

    /*
      This method is called when the add button is called
     */
    public void AddParents(View view) {
            parentsUserName = (TextView) findViewById(R.id.parentsUserName);
            AddParentsList(parentsUserName.getText().toString());
            parentsList.add(parentsUserName.getText().toString());
            verification.parentsControl(2, parentsUserName.getText().toString(), userNmae);
            parentsUserName.setText("");
            if (firstTimeAddParent) {
                dialog.show();
                firstTimeAddParent = false;
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
    }

        /*
      This method is used to add name of the parents to the screen
      this method will take the name of the parents which will will
      create a new layout with a TextView before adding them to parentsUserName
      linear layout
       */
    public void AddParentsList (String Name){
        accountLayout = (LinearLayout) findViewById(R.id.accountLayout);
        LinearLayout parentsNameLayout = new LinearLayout(this);
        parentsNameLayout.setId((int )(Math.random() * 10000 + 10));

        // Make the TextView dynamically
        TextView parentsName = new TextView(this);
        parentsName.setText(Name);
        parentsName.setTextColor(Color.rgb(36,123,160));
        parentsName.setTextSize(20);
        parentsName.setPadding(100,50,40,50);
        parentsName.setId(parentsNameLayout.getId()-VIEW_ID);

        // Make the Delete TextView (act as a button)
        final TextView deleteParent = new TextView(this);
        deleteParent.setText("REMOVE");
        deleteParent.setTextSize(20);
        deleteParent.setPadding(30,50,40,50);
        deleteParent.setVisibility(View.INVISIBLE);
        deleteParent.setId(parentsNameLayout.getId()+VIEW_ID);
        deleteParent.setTextColor(Color.rgb(242,95,92));
        deleteParent.setGravity(Gravity.RIGHT);
        deleteParent.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayoutCompat.LayoutParams.FILL_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT));

        // parentsNameLayout On ClickListener
        parentsNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (selectedParentsNameLayout != null) {
                    selectedParentsNameLayout.setBackgroundColor(Color.WHITE);
                    findViewById(selectedParentsNameLayout.getId()+VIEW_ID).setVisibility(View.INVISIBLE);
                }
              selectedParentsNameLayout = (LinearLayout) view;
              selectedParentsNameLayout.setBackgroundColor(Color.rgb(217,217,217));
              findViewById(selectedParentsNameLayout.getId()+VIEW_ID).setVisibility(View.VISIBLE);
            }
        });

        // deleteParent On ClickListener
        deleteParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView theParent = findViewById(findViewById(view.getId()-VIEW_ID).getId()-VIEW_ID);
                parentsList.remove(theParent.getText().toString());
                verification.parentsControl(1,theParent.getText().toString(),userNmae);
                accountLayout.removeView(findViewById(view.getId()-VIEW_ID));
                selectedParentsNameLayout= null;
            }
        });

        // add the new elements to the screen (layouts)
        parentsNameLayout.addView(parentsName);
        parentsNameLayout.addView(deleteParent);
        accountLayout.addView(parentsNameLayout);
    }

    /*
    This method is for updating the account information form activity_account_screen
     */
    public void updateAccount(View view) {
        verification.updateUser(
                userName_tv.getText().toString(),
                userPasswordNew_tv.getText().toString(),
                userPasswordOLD_tv.getText().toString(),
                userEmail_tv.getText().toString()
        );
        startActivity(new Intent(accountScreen.this, accountHomePage.class));
        finish();
    }

    /*
    This method is for deleting the account information form activity_account_screen
    */
    public void deleteAccount(View view) {
        verification.updateUser(
               userNmae,
               null,
                null,
                null
        );
        // Save the login data
        final SharedPreferences login_Data = PreferenceManager.getDefaultSharedPreferences(accountScreen.this);
        final SharedPreferences.Editor userName_Pref = login_Data.edit();
        userName_Pref.putString("userName", null);
        userName_Pref.commit(); // close editor

        startActivity(new Intent(accountScreen.this,Login.class));
        finish();

    }
    /*
    This method is for switching the screen form th current acclivity to accountScreen acclivity
     */
    public void goBack(View view) {
        startActivity(new Intent(accountScreen.this, accountHomePage.class));
        finish();
    }
}
