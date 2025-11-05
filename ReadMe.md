# 🏫 School Inventory Management System

A JavaFX desktop application for managing school inventory — including items, categories, borrowers, and scanned records.
The system provides a modern dashboard, data filtering, and real-time charts to help administrators track school equipment efficiently.

---

## 🚀 Features

* **📊 Dashboard Overview**

  * Displays key inventory metrics and a monthly scanned vs. unscanned report.
  * Dynamic bar chart visualization.

* **📦 Item Management**

  * Add, edit, and delete school items.
  * Track quantity, condition, and storage location.

* **📁 Category Management**

  * Organize items by categories (e.g., Computers, Books, Sports Equipment).

* **👥 Borrower Management**

  * Record and monitor borrowed items and their borrowers.

* **📅 Scanned Items**

  * View scan history with date range filters.
  * See which items are scanned or pending.

* **🔐 Secure Login System**

  * Separate login and signup features for user access control.

---

## 🧰 Tech Stack

| Component     | Technology                  |
| ------------- | --------------------------- |
| **Language**  | Java (Zulu Azul OpenJDK 25) |
| **Framework** | JavaFX 23                   |
| **Database**  | MySQL                       |
| **Layout**    | FXML (Scene Builder)        |
| **Styling**   | CSS                         |
| **IDE**       | NetBeans 27                 |

---

## ⚙️ Setup Guide

### 1️⃣ Clone the Project

```bash
git clone https://github.com/jerahmeelmitchao/school-inventory-system.git
cd school-inventory-system
```

### 2️⃣ Set Up the Database

1. Open **MySQL** and create a new database:

   ```sql
   CREATE DATABASE schoolinventory;
   ```
2. Import the provided SQL file:

   ```sql
   source schoolinventory.sql;
   ```
3. Update your database credentials inside:

   ```
   inventorysystem/utils/DatabaseConnection.java
   ```

### 3️⃣ Run the Application

* Open the project in **NetBeans 27** (or IntelliJ IDEA).
* Make sure JavaFX SDK is configured and added to your classpath.
* Run the app:

  ```bash
  Main.java
  ```

---

## 🖼️ UI Overview

| Section        | Description                       |
| -------------- | --------------------------------- |
| **Dashboard**  | Displays statistics and charts    |
| **Items**      | Manage all school inventory items |
| **Categories** | Create and organize categories    |
| **Borrowers**  | Track borrowed items              |
| **Scanned**    | Monitor scanning activities       |

---

## 🪄 Change App Icon

To change your app icon in **JavaFX**:

1. Place your `.png` icon in the `src/inventorysystem/assets/` directory.
2. In your `Main.java`, add:

   ```java
   stage.getIcons().add(new Image(getClass().getResourceAsStream("/inventorysystem/assets/inventory_icon.png")));
   ```
3. Rebuild and run your project — your custom icon will appear in the window and taskbar.

---

⭐ *Built with JavaFX and care using Zulu Azul OpenJDK 25.*
