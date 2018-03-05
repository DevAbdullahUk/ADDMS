package com.capstone.ako.addms;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class accountScreen extends AppCompatActivity {
    // On screen components activity_account_screen2.xml
    TextView parentsUserName ;
    LinearLayout accountLayout, selectedParentsNameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_screen);
    }

    /*
      This method is used to change this activity layout xml.
      The view Id of the caller element on the screen(TextView) will decide which
      layout should be replaced
       */
    public void moveScreen(View view) {
        if ( view.getId() == findViewById(R.id.parents).getId()) {
            setContentView(R.layout.activity_account_screen2);
        } else {
            setContentView(R.layout.activity_account_screen);
        }
    }

    /*
      This method is called when the add button is called
     */
    public void AddParents(View view) {
        parentsUserName = (TextView) findViewById(R.id.parentsUserName);
        AddParentsList (parentsUserName.getText().toString());
        parentsUserName.setText("");
    }

    /*
  This method is used to add name of the parents to the screen
  this method will take the name of the parents which will will
  create a new layout with a TextView before adding them to parentsUserName
  linear layout
   */
    public void AddParentsList (String Name ){
        accountLayout = (LinearLayout) findViewById(R.id.accountLayout);
        LinearLayout parentsNameLayout = new LinearLayout(this);
        parentsNameLayout.setId((int )(Math.random() * 10000 + 10));

        // Make the TextView dynamically
        TextView parentsName = new TextView(this);
        parentsName.setText(Name);
        parentsName.setTextColor(Color.rgb(36,123,160));
        parentsName.setTextSize(20);
        parentsName.setPadding(100,50,40,50);

        // Make the Delete TextView (act as a button)
        final TextView deleteParent = new TextView(this);
        deleteParent.setText("REMOVE");
        deleteParent.setTextSize(20);
        deleteParent.setPadding(30,50,40,50);
        deleteParent.setVisibility(View.INVISIBLE);
        deleteParent.setId(parentsNameLayout.getId()+2);
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
                    findViewById(selectedParentsNameLayout.getId()+2).setVisibility(View.INVISIBLE);
                }
              selectedParentsNameLayout = (LinearLayout) view;
              selectedParentsNameLayout.setBackgroundColor(Color.rgb(217,217,217));
              findViewById(selectedParentsNameLayout.getId()+2).setVisibility(View.VISIBLE);
            }
        });

        // deleteParent On ClickListener
        deleteParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountLayout.removeView(findViewById(view.getId()-2));
                selectedParentsNameLayout= null;
            }
        });

        // add the new elements to the screen (layouts)
        parentsNameLayout.addView(parentsName);
        parentsNameLayout.addView(deleteParent);
        accountLayout.addView(parentsNameLayout);
    }

}
