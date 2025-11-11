# BFH Java Fast 🚀

A simple Java app that:
1. Generates a webhook + JWT access token from BFH API  
2. Submits the SQL query for **Question 2 (Even registration number)**  
3. Confirms success with HTTP 200 responses  

### ✅ Output Proof
Got accessToken: true
Submission status: 200
Webhook post status: 200


### 🧰 Tech Stack
- Java 17  
- Maven  
- HttpClient + Jackson  

### 🏗️ Build & Run
mvn -q -DskipTests package
mvn -q exec:java


### SQL Query Submitted
```sql
SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME,
COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
FROM EMPLOYEE e1
JOIN DEPARTMENT d ON d.DEPARTMENT_ID = e1.DEPARTMENT
LEFT JOIN EMPLOYEE e2 ON e2.DEPARTMENT = e1.DEPARTMENT AND e2.DOB > e1.DOB
GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME
ORDER BY e1.EMP_ID DESC;