# 🌐 ProcureFlow Server - LAN Setup Guide

This backend server hosts the core REST APIs and communicates with your MySQL database. It supports connections from frontend clients running on other PCs inside the Local Area Network (LAN).

---

## 🚀 Setting Up the Host Server

### Step 1: Find this PC's LAN IP Address
1. Open Command Prompt (`cmd`).
2. Run the command:
   ```cmd
   ipconfig
   ```
3. Locate your active network adapter (e.g., Wi-Fi or Ethernet) and note down the **IPv4 Address** (e.g., `192.168.1.100`).
4. **Share this IP Address with the Frontend Client users.** They will need it to link their desktop applications.

### Step 2: Database Configuration
* Ensure your MySQL server is running.
* Database configurations, table structures, and initial seeded data properties are located inside `src/main/resources/application.properties` and `src/main/resources/data.sql`.

### Step 3: Start the Backend
Start the server:
```cmd
gradlew bootRun
```
* **Verification**: Open a browser on any PC in the same network and navigate to:
  ```text
  http://<YOUR_LAN_IP>:8080/api/stats
  ```
  If it displays JSON statistics showing active request numbers, the server is successfully publishing APIs over the LAN!
