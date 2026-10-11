# 🎓 College Attendance Management System

A Java-based desktop application for managing college attendance efficiently. The system allows users to manage students, subjects, faculty members, record daily attendance, and generate attendance reports through a graphical user interface built with Java Swing.

## ✨ Features

- **Student Management** — Add, update, delete, and search student records.
- **Subject Management** — Manage subject codes and subject names.
- **Faculty Management** — Add, update, and delete faculty records.
- **Attendance Tracking** — Record and update student attendance as Present or Absent.
- **Date Validation** — Record attendance using the `YYYY-MM-DD` format and prevent future dates.
- **Attendance Reports** — Calculate attendance percentages for individual students and subjects.
- **Low Attendance Detection** — Identify students whose attendance falls below 75%.
- **Sorting & Search** — Search students by ID or name and sort reports by column.
- **Data Persistence** — Save records to a local text file and load them when the application restarts.
- **Input Validation** — Prevent empty fields and duplicate student IDs, subject codes, and faculty IDs.

## 🛠️ Technologies Used

- **Language:** Java
- **GUI:** Java Swing
- **Collections:** ArrayList, LinkedList, HashMap, TreeMap
- **Date Handling:** Java Time API (`LocalDate`)
- **Data Storage:** File handling using text files
- **IDE:** Visual Studio Code, IntelliJ IDEA, or Eclipse

## 📋 Prerequisites

Before running the project, make sure you have:

- Java Development Kit (JDK) 8 or later
- A Java-compatible IDE or terminal
- A desktop environment that supports Java Swing

## 🚀 How to Run

### 1. Clone the repository

```bash
git clone https://github.com/YOUR-USERNAME/Attendance-Management-System.git
```

### 2. Navigate to the project folder

```bash
cd Attendance-Management-System
```

### 3. Compile the Java program

```bash
javac AttendanceManagementSystem.java
```

### 4. Run the application

```bash
java AttendanceManagementSystem
```

The College Attendance Management System window will open.

## 📂 Project Structure

```text
Attendance-Management-System/
│
├── AttendanceManagementSystem.java
├── attendance-data.txt       # Created when data is saved
└── README.md
```

**Note:** The `attendance-data.txt` file is generated in the application's working directory when records are saved. Keep this file to retain your data between sessions.

## 📊 Attendance Calculation

The system calculates attendance percentage using the following formula:

```text
Attendance Percentage =
(Present Classes / Total Recorded Classes) × 100
```

For example, if a student attends 18 out of 20 recorded classes:

```text
Attendance Percentage = (18 / 20) × 100 = 90%
```

The reports classify students with attendance below 75% as **LOW** and students at or above 75% as **OK**. Students without attendance records are displayed as **NO RECORDS**.

## 🧠 Data Structures Used

| Data Structure | Purpose |
|---|---|
| `ArrayList` | Stores student, subject, and faculty records |
| `LinkedList` | Maintains attendance history |
| `HashMap` | Provides quick student lookup using student ID |
| `TreeMap` | Groups and sorts students by attendance percentage |
| `Array` | Stores the available attendance statuses |

## 🎯 Learning Objectives

This project demonstrates practical applications of:

- Object-Oriented Programming (OOP)
- Classes, objects, constructors, and encapsulation
- Java Swing GUI development
- Event handling and exception handling
- Java Collections Framework
- File handling and data persistence
- Date validation and business logic
- Searching, sorting, and report generation

## 🔮 Future Improvements

- Integrate a database such as MySQL or PostgreSQL.
- Add login and role-based access for administrators, faculty, and students.
- Export attendance reports to PDF or Excel.
- Add charts and visual attendance analytics.
- Support bulk attendance entry and attendance history filtering.
- Introduce automated backups and stronger data validation.

## 📄 License

This project is available for educational and learning purposes. Add a formal open-source license if you want others to reuse, modify, or distribute it under defined terms.
