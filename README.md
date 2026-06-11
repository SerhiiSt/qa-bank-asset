# Portfolio Rebalancing Engine - QA Automation Project



---

## Project Overview

This project validates a **Portfolio Rebalancing Application**.

The application calculates the number of shares that must be **bought** or **sold** to achieve the target asset allocation for each security in an investment portfolio.

The primary output of the system is:

> **Number of shares to buy or sell for each security to reach zero target variance.**

---

#  Business Scenario

### Account ABC

**Total Assets = $100,000**

| Security | Target % | Current % | Variance % | Unit Price ($) |
| -------- | -------- | --------- | ---------- | -------------- |
| IBM      | 20       | 10        | -10        | 150            |
| MSFT     | 20       | 20        | 0          | 90             |
| ORCL     | 20       | 30        | +10        | 220            |
| AAPL     | 20       | 20        | 0          | 450            |
| HD       | 20       | 20        | 0          | 70             |

---

## Business Rules

### Target Variance

```text
Variance = Current % - Target %
```

Interpretation:

| Variance | Action    |
| -------- | --------- |
| Negative | BUY       |
| Positive | SELL      |
| Zero     | No Action |

---

##  Rebalancing Calculation

### Step 1: Calculate Dollar Variance

Formula:

```text
Variance Amount =
(Total Assets × |Variance %|) / 100
```

### IBM

```text
$100,000 × 10% = $10,000
```

Action:

```text
BUY $10,000 worth of IBM
```

---

### ORCL

```text
$100,000 × 10% = $10,000
```

Action:

```text
SELL $10,000 worth of ORCL
```

---

## Step 2: Convert Dollars to Shares

Formula:

```text
Shares = Variance Amount / Unit Price
```

### IBM

```text
10000 / 150 = 66.67
Rounded = 67 shares
```

### ORCL

```text
10000 / 220 = 45.45
Rounded = 45 shares
```

---

#  Expected Output

| Security | Action | Shares |
| -------- | ------ | ------ |
| IBM      | BUY    | 67     |
| MSFT     | NONE   | 0      |
| ORCL     | SELL   | 45     |
| AAPL     | NONE   | 0      |
| HD       | NONE   | 0      |

