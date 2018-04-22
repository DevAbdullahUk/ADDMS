package com.capstone.ako.addms;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Abdullah Janjua
 */

public class Verification extends AppCompatActivity {

    //  SQL query and user name
    static String executeQueryA = "", executeQueryB = "",executeQueryD = "",executeQueryE = "",userName_String = null;
    int operationStates = 0;
    static boolean addUser = false,updateUser = true, endDriving = false;
    static Context context;
    //  array of symbols that causes sql injection
    String[] sqlCheckList = {"--", ";--", ";", "/", "/", "@@", "@", "char", "nchar", "varchar", "nvarchar", "alter", "begin", "cast",
            "create", "cursor", "declare", "delete", "drop", "end", "exec", "execute", "fetch", "insert",
            "kill", "select", "sys", "sysobjects", "syscolumns", "table", "update", "=", "or", "where"};
    // A list of the parents
    List<String> parentsList = new ArrayList<>();
    Verification (Context context){
        this.context = context;
    }
    /*
    checks if two strings contains some symbols that might cause sql injection
    */
    public boolean verify(String userName, String pass) {
        for (int i = 0; i <= sqlCheckList.length - 1; i++) {
            if (userName.toLowerCase().contains(sqlCheckList[i].toLowerCase()) || pass.toLowerCase().contains(sqlCheckList[i].toLowerCase())) {
                userName_String = null;
                return false;
            }
        }
        userName_String = userName;
        return true;
    }

    /*
     SignUp an new user to the Database
     */
    public void userSignUp(String userName, String userPassword, String EMail) {
        if (verify(userName, userPassword)) {
            executeQueryB = "SELECT * FROM dbo.[User] WHERE UserName = '" + userName + "'";
            executeQueryA = "INSERT INTO dbo.[User] VALUES ('" + userName + "', '" + EMail + "','" + userPassword + "',1)";
            addUser = true;
            new QueryExecuting_loginAndSignUp().execute();
        }
         else{
            showMessage(1);
        }
    }
    /*
    This method will update the user information
     */
    public void updateUser(String userName, String userPasswordNew, String userPasswordNOld, String EMail) {
        if (verify(userPasswordNew, userPasswordNOld) && verify(userName, "empty")) {
            updateUser = true;
            if (EMail == null) { // Change the password
                executeQueryD = "UPDATE dbo.[User] SET Passowrd = '" + userPasswordNOld + "' WHERE Passowrd LIKE '" + userPasswordNew + "' AND UserName = '" + userName + "'";
            } else if (EMail == null && userPasswordNew == null && userPasswordNOld == null){ // Delete the account
                executeQueryD = "UPDATE dbo.[User] SET Status = '0' WHERE UserName = '" + userName + "'";
            }
            else { // Change email and password
                executeQueryD = "UPDATE dbo.[User] SET Email = '" + EMail + "', Passowrd = '" + userPasswordNOld + "' WHERE Passowrd LIKE '" + userPasswordNew + "' AND UserName = '" + userName + "'";
            }
            new QueryExecuting_account().execute();
        } else { showMessage(1); }
    }

    /*
    SignUp an new user to the Database.
    */
    public void userLogin(String userName, String userPassword) {
        if (verify(userName, userPassword)) {
            executeQueryB = "SELECT * FROM  dbo.[User] WHERE UserName = '" +userName+ "' AND Passowrd LIKE '" +userPassword+ "' And Status = 1";
            executeQueryA = "";
            new QueryExecuting_loginAndSignUp().execute();
        } else {
            showMessage(1);
        }
    }

    /*
    Show the parents name,delete the parents or add a new parent. this method will return a list
    In the parameters the Actions can be: GET_PARENTS = 0, ADD_PARENTS = 1, DELETE_PARENTS = 3
     */
    public List<String> parentsControl(int action, String parentName, String userName ){
        if (verify(parentName,userName)){
            if (action == 0){
                executeQueryD = "SELECT ParentID FROM dbo.Monitoring WHERE ChildID = '"+userName+"'" ;
                updateUser = false;
            } else {       // TODO:check if the parents exists or not when adding!
               if (action == 1) {executeQueryD = "DELETE FROM dbo.Monitoring WHERE ChildID = '"+userName+"' AND ParentID = '"+parentName+"'" ;}
                else if (action == 2){executeQueryD = "INSERT INTO dbo.Monitoring VALUES ( '"+parentName+"' ,'"+userName+"')";}
                updateUser = true;
            }
           new QueryExecuting_account().execute();
        }  else { showMessage(1); }
        return parentsList;
    }

    /*
    This method is used only to show a toast message
    */
    public void showMessage (int messageNumber){
        String message = "";
        switch (messageNumber) {
            case 1 : message = "NOT ALLOWED CHARACTER";
            break;
            case 2 : message = "SORRY! USER NAME IS ALREADY EXISTS";
            break;
            case 3 : message = "PLEASE CHECK YOUR INTERNET CONNECTION";
            break;
            case 4 : message = "SORRY! USERNAME OR PASSWORD IS'T CORRECT";
            break;
            default: message = "UMMM! SOMETHING WENT WRONG";
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /*
    This is method is used when user end driving mode, to in order to save the
    last trip details to the user history
     */
    public void saveTripHistoy (int numberOfAlerts, double coveredDistance, double elapsedTime, double averageSpeed, String userName){
        DateFormat theDate = new SimpleDateFormat("MMM d, yyyy");
        executeQueryE = "INSERT INTO dbo.TripDetails VALUES ("+numberOfAlerts+","+coveredDistance+", "+elapsedTime+","+averageSpeed+", '"+userName+"', '"+theDate.format(new Date())+"')";
//        executeQueryE = "INSERT INTO dbo.TripDetails VALUES ("+numberOfAlerts+","+coveredDistance+", "+elapsedTime+","+averageSpeed+", '"+userName+"', 'March 11 2018'";
        endDriving = true;
        new QueryExecuting_account().execute();
    }

    /*
    This method will add the user name to the preferences so it can be used to skip the loginScreen,
    Firebase, and to show the name of the user in other activates
     */
    public void userSharedPreferences(){
        // Save the login data
        final SharedPreferences login_Data = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor userName_Pref = login_Data.edit();
        userName_Pref.putString("userName", userName_String);
        userName_Pref.commit(); // close editor

    }


    /*
    This class is for executing the sql query for login and sig-up
     */
    class QueryExecuting_loginAndSignUp extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            try {
                // set the connection to SQL
                Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
                Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://10.20.1.153:49170/;user=sa;password=140096;integratedSecurity=true;");
                Statement stmt = DbConn.createStatement();
                // execute the SQL query B (Check if the user exists in the DB then query A
                ResultSet reset = stmt.executeQuery(executeQueryB);
                if (reset.next()) { // To check if the userName
                    DbConn.close();
                    if (addUser==false){ // Only if it is login
                        DbConn.close();
                        userSharedPreferences(); // save the userName to SharedPreferences
                        operationStates = 0; // Move to the next screen
                        context.startActivity(new Intent(context, accountHomePage.class));
                    } else {operationStates = 2;}
                } else {operationStates = 4;}
                if (addUser && operationStates != 2 ) { // Only if it is a sign-up and the user is not available
                    stmt.executeUpdate(executeQueryA);
                    DbConn.close();
                    addUser = false;
                    operationStates = 0;
                    userSharedPreferences(); // save the userName to SharedPreferences
                    context.startActivity(new Intent(context, accountHomePage.class));
                }
            } catch (Exception e) { operationStates = 3; }
            Handler handler =  new Handler(context.getMainLooper());
            handler.post( new Runnable(){ // This thraed is for updating the UI
                public void run(){
                    if (operationStates != 0)
                    showMessage(operationStates);
                }
            });
            return "";
        }
    }

    /*
    This class is for executing the sql query for login and sig-up
     */
    class QueryExecuting_account extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            try {
                // set the connection to SQL
                Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
                Connection DbConn = DriverManager.getConnection("jdbc:jtds:sqlserver://10.20.1.153:49170/;user=sa;password=140096;integratedSecurity=true;");
                Statement stmt = DbConn.createStatement();
                // execute the SQL query B (Check if the user exists in the DB then query A
                if (endDriving) {
                    stmt.executeUpdate(executeQueryE);
                    endDriving = false;
                }
               else if (updateUser){
                   stmt.executeUpdate(executeQueryD); //TODO: Tell the user if the update is not successful
               }else { // Get the list of the parents
                   ResultSet reset = stmt.executeQuery(executeQueryD);
                   while (reset.next()) { // To check if the userName
                       parentsList.add(reset.getString(1));
                   }
               }
            } catch (Exception e) {  operationStates = 3; }
            Handler handler =  new Handler(context.getMainLooper());
            handler.post( new Runnable(){ // This thraed is for updating the UI
                public void run(){
                    if (operationStates != 0)
                    showMessage(operationStates);
                }
            });
            return "";
        }

    }
}
