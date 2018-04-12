package com.capstone.ako.addms;

/**
 * Created by khalil on 4/10/2018.
 */

public class Verification {

    /** array of symbols that causes sql injection*/

    String[] sqlCheckList = {"--", ";--", ";", "/", "/", "@@", "@", "char", "nchar", "varchar", "nvarchar", "alter", "begin", "cast",
            "create", "cursor", "declare", "delete", "drop", "end", "exec", "execute", "fetch", "insert",
            "kill", "select", "sys", "sysobjects", "syscolumns", "table", "update", "=", "or", "where"};

/** checks if two strings contains some symbols that might cause sql injection */
    public boolean verify(String userName, String pass) {
        for (int i = 0; i <= sqlCheckList.length - 1; i++) {
            if (userName.toLowerCase().contains(sqlCheckList[i].toLowerCase()) || pass.toLowerCase().contains(sqlCheckList[i].toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}
