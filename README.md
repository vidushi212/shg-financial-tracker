# SHG Financial Tracker

A comprehensive Java console-based application for Self-Help Groups (SHGs) to track financial activities, receive advisory recommendations, and facilitate collective decision-making.

## 📋 Project Overview

The **SHG Financial Tracking & Advisory Platform** is designed specifically for SHGs (typically 10-20 members) to:
- Maintain transparent financial records (savings, loans, expenses)
- Receive authenticated advisory inputs from verified brokers and government representatives
- Access government schemes and investment opportunities
- Support collective, democratic decision-making

### Core Features:
✅ **Financial Tracking** - Record and aggregate transactions  
✅ **Monthly Reports** - Generate summaries and comparisons  
✅ **Advisory Module** - Access investment plans and government schemes  
✅ **Discussion Forum** - Group-based collaborative discussions  
✅ **Role-Based Access** - Member, Treasurer, Leader, Broker, Admin roles  
✅ **Console-Based UI** - Interactive menu-driven interface  

---

## 🏗️ Architecture

The project follows **MVC (Model-View-Controller)** architecture:

```
src/main/java/com/shg/
├── model/          # Entity classes (SHGGroup, Transaction, etc.)
├── view/           # Console UI components
├── controller/     # Business logic & API handlers
├── service/        # Business logic engines
└── repository/     # In-memory data storage
```

### Current Status:
- ✅ **UI Layer** - Complete (9 view classes)
- 🔄 **Model Layer** - In Progress
- ⏳ **Controller Layer** - Coming Soon
- ⏳ **Business Logic** - Coming Soon

---

## 🚀 Getting Started

### Prerequisites

Before running the application, ensure you have:
- **Java 11** or higher
- **Maven 3.6** or higher

#### Install Java:
```bash
# On macOS (using Homebrew)
brew install java11

# On Ubuntu/Debian
sudo apt-get install openjdk-11-jdk

# On Windows
# Download from https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
```

#### Install Maven:
```bash
# On macOS (using Homebrew)
brew install maven

# On Ubuntu/Debian
sudo apt-get install maven

# On Windows
# Download from https://maven.apache.org/download.cgi
```

#### Verify Installations:
```bash
java -version
mvn -version
```

---

## 📥 Setup Instructions

### Step 1: Clone the Repository
```bash
git clone https://github.com/vidushi212/shg-financial-tracker.git
cd shg-financial-tracker
```

### Step 2: Build the Project
```bash
mvn clean package
```

This command will:
- Clean any previous builds
- Compile all Java source files
- Run tests (if any)
- Package the application into a JAR file in the `target/` directory

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
[INFO] Finished at: YYYY-MM-DDTHH:MM:SS+05:30
```

### Step 3: Run the Application

#### **Option A: Run Using JAR**
```bash
java -jar target/shg-financial-tracker-1.0-SNAPSHOT.jar
```

#### **Option B: Run Using Maven**
```bash
mvn exec:java -Dexec.mainClass="com.shg.view.ApplicationNavigator"
```

#### **Option C: Run in IDE**
1. Open the project in IntelliJ IDEA or Eclipse
2. Navigate to `src/main/java/com/shg/view/ApplicationNavigator.java`
3. Right-click → **Run 'ApplicationNavigator.main()'**

---

## 🎯 Application Flow

Once the application starts, you'll see:

### 1. **Login Screen**
```
========== SHG FINANCIAL TRACKER ==========
1. Login
2. Register
3. Exit

Enter your choice (1-3):
```

**Available Roles During Registration:**
- **Member** - View reports and participate in discussions
- **President** - Manage group and members
- **Secretary/Treasurer** - Record transactions and generate reports
- **Broker** - Publish investment plans (must be verified by Admin)
- **Government Officer** - Publish government schemes
- **Admin** - Verify brokers and manage system

### 2. **Dashboard**
```
========== DASHBOARD - Shakti Mahila Sangha ==========
Group Balance: ₹45,000
Members: 15
Active Members: 14

1. View Transactions
2. Generate Report
3. View Advisory
4. Join Discussion
5. (Admin) Admin Panel
6. Logout
```

### 3. **Main Modules**

#### **Finance Module**
- Record new transactions (Savings, Loan, Expense)
- View transaction history with date/type filters
- Check group balance

#### **Reports Module**
- View monthly financial summaries
- Compare multi-month data
- ASCII bar charts
- Investment comparison tables

#### **Advisory Module**
- Browse bank investment plans
- Explore government schemes
- View AI-generated recommendations
- Discuss recommendations with members

#### **Discussion Module**
- Start new discussions
- Post and view comments
- Linked recommendation discussions

#### **Admin Module** (Admin Only)
- Verify pending brokers
- Approve/reject advisory submissions
- View platform statistics
- Manage system settings

---

## 🛠️ Troubleshooting

### Issue: `mvn: command not found`
**Solution:** Maven is not in your PATH. Reinstall Maven or add it to system PATH.

### Issue: `javac: command not found`
**Solution:** Java is not installed or not in PATH. Install Java 11+ and verify with `java -version`.

### Issue: `BUILD FAILURE`
**Try:**
```bash
mvn clean install
```

### Issue: `Exception in thread "main" java.lang.ClassNotFoundException`
**Solution:** The JAR wasn't built properly. Run:
```bash
mvn clean package -DskipTests
```

### Issue: Port Already in Use (if using network features)
**Solution:** Change the port in configuration or kill the process using the port.

---

## 📂 Project Structure

```
shg-financial-tracker/
├── src/
│   ├── main/
│   │   └── java/com/shg/
│   │       ├── view/                 # UI Layer (COMPLETE)
│   │       │   ├── LoginView.java
│   │       │   ├── DashboardView.java
│   │       │   ├── FinanceView.java
│   │       │   ├── ReportView.java
│   │       │   ├── AdvisoryView.java
│   │       │   ├── DiscussionView.java
│   │       │   ├── AdminView.java
│   │       │   ├── UIUtility.java
│   │       │   └── ApplicationNavigator.java
│   │       ├── model/                # Entity Classes (IN PROGRESS)
│   │       ├── controller/           # Controllers (COMING SOON)
│   │       ├── service/              # Business Logic (COMING SOON)
│   │       └── repository/           # Data Storage (COMING SOON)
│   └── test/                         # Unit Tests
├── pom.xml                           # Maven Configuration
├── .gitignore                        # Git Ignore Rules
└── README.md                         # This File
```

---

## 💻 Development

### Running with Debug Mode:
```bash
mvn clean compile exec:java@debug -Dexec.mainClass="com.shg.view.ApplicationNavigator"
```

### Running Tests:
```bash
mvn test
```

### Building Without Running Tests:
```bash
mvn clean package -DskipTests
```

### Creating an Executable JAR:
```bash
mvn clean package
java -jar target/shg-financial-tracker-1.0-SNAPSHOT.jar
```

---

## 📦 Dependencies

The project uses:
- **Java 11** - Core language
- **Maven** - Build automation
- **JUnit 4** (optional) - Unit testing

All dependencies are managed via `pom.xml`.

---

## 🔄 Development Roadmap

- [x] UI Layer - Console-based views
- [ ] Model Layer - Entity classes with relationships
- [ ] Controller Layer - Request handlers
- [ ] Business Logic - Recommendation engine, report generation
- [ ] Data Persistence - File-based or database storage
- [ ] API Integration - External broker/government APIs
- [ ] Web UI - Optional web interface

---

## 📝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -m 'Add your feature'`
4. Push to branch: `git push origin feature/your-feature`
5. Create a Pull Request

---

## 📄 License

This project is open-source and available under the MIT License.

---

## 📞 Support

For issues, questions, or suggestions:
- Create an [Issue](https://github.com/vidushi212/shg-financial-tracker/issues)
- Check existing [Documentation](https://github.com/vidushi212/shg-financial-tracker/wiki)

---

## ✨ Acknowledgments

This project implements a real-world solution for Self-Help Groups based on the SHG Financial Tracking & Advisory Platform specification, focusing on financial inclusion and collective decision-making in rural and semi-urban communities.

---

**Last Updated:** 2026-03-23 09:17:47  
**Project Status:** Active Development 🚀