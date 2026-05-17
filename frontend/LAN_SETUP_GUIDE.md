# 🌐 ProcureFlow Client - LAN Setup Guide

This client desktop application can be run locally on any PC and connect to a backend server running on another PC in the Local Area Network (LAN).

---

## 🚀 Connecting to the Host Server

### Option A: Automatic LAN Link (Recommended)
1. Run the **`configure_lan.bat`** script located directly in this `frontend` folder.
2. Enter the **LAN IP Address** of the machine running the backend server (e.g., `192.168.1.100`) when prompted.
3. The script will automatically link this client to the server.

### Option B: Manual UI Setup
1. Launch the application:
   ```cmd
   gradlew run
   ```
2. Log in with any credential.
3. Click on the **⚙️ Settings** tab on the navigation sidebar.
4. Replace `http://localhost:8080` in the **REST API Endpoint Base URL** field with:
   ```text
   http://<HOST_SERVER_IP>:8080
   ```
5. Click **💾 Save Preferences**.
6. The app is now connected over LAN! The setting is remembered permanently across restarts.

---

## 🔑 Valid Role Login Accounts

| Account | Email Address | Access Level |
| :--- | :--- | :--- |
| **Faculty (Requester)** | `faculty@college.edu` | Dashboard, Requests (Form Enabled), Sign Out |
| **Store Manager** | `manager@college.edu` | Dashboard, Requests, Inventory, Vendors, Sign Out |
| **Chief Accountant** | `accountant@college.edu` | Dashboard, Requests, Vendors, Finance, Sign Out |
| **College Director** | `director@college.edu` | Dashboard, Requests, Finance, Sign Out |
| **Super Admin** | `admin@college.edu` | **ALL PANELS** + User Access Control Panel in Settings |
