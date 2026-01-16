# 🛒 POS TAHA - Point of Sale Management System

**POS TAHA** is a comprehensive desktop application developed in **Java Swing**, designed to manage sales operations, inventory tracking, and financial reporting for retail businesses.

## 🌟 Key Features

* **📊 Analytical Dashboard:** Real-time visualization of total revenue, customer counts, and low-stock alerts.
* **🛒 Sales Management:** Intuitive interface to scan products, calculate totals (inc. tax), and manage various payment methods.
* **📦 Inventory Control:** Full CRUD interface to add, update, and delete products with real-time quantity tracking.
* **📄 Report Exporting:** Automatically generate professional sales reports in **PDF** (via iText) and **Excel (CSV)** formats.
* **🔐 Security:** Robust authentication system with role-based access control (Admin / Cashier).
* **🎨 Modern UI:** Sleek and elegant interface powered by the **FlatLaf** LookAndFeel.

## 🛠️ Technologies Used

* **Language:** Java (JDK 17+)
* **GUI Framework:** Java Swing & FlatLaf
* **Database:** MySQL
* **External Libraries:**
    * `mysql-connector-java`: Database connectivity.
    * `itextpdf-5.5.13`: Professional PDF generation.
    * `flatlaf`: Modern Flat design theme.

## 🚀 Installation & Setup

### 1. Prerequisites
* **Java Runtime Environment** installed on your machine.
* An active **MySQL** server.

### 2. Database Configuration
1. Import the provided SQL schema (if available) or create the `Product`, `Sale`, and `User` tables.
2. Update your database credentials in: `src/com/pos/connection/DBConnection.java`.

### 3. Running the App (Ubuntu/Linux)
Make the script executable and run the application:
```bash
chmod +x run_pos.sh
./run_pos.sh
