package com.procurement.frontend.controller;

import org.json.JSONObject;

public class BaseController {
    private static JSONObject currentUser;

    public static void setCurrentUser(JSONObject user) {
        currentUser = user;
    }

    public static JSONObject getCurrentUser() {
        return currentUser;
    }

    public static String getUserRole() {
        return currentUser != null ? currentUser.optString("role", "faculty") : "faculty";
    }

    public static String getUserName() {
        return currentUser != null ? currentUser.optString("name", "Staff Member") : "Staff Member";
    }

    public static String getUserId() {
        return currentUser != null ? currentUser.optString("id", "unknown") : "unknown";
    }

    public static String getUserEmail() {
        return currentUser != null ? currentUser.optString("email", "unknown@college.edu") : "unknown@college.edu";
    }
}
