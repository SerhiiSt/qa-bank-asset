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
---
# Manual Test Cases
## TC-001 Buy Shares
| Field        | Value                            |
| ------------ | -------------------------------- |
| Test Case ID | TC-001                           |
| Scenario     | Security under target allocation |
| Input        | IBM (-10%, $150)                 |
| Expected     | BUY 67 shares                    |

### _Steps_
```text
1) Enter total asset = 100000
2) Enter IBM variance = -10
3) Enter unit price = 150
4) Click Rebalance
```
### _Expected Results:_
```text
IBM → BUY 67 shares
```

## TC-002 Sell Shares
| Field        | Value                            |
| ------------ | -------------------------------- |
| Test Case ID | TC-002                           |
| Scenario     | Security above target allocation |
| Input        | ORCL (+10%, $220)                |
| Expected     | SELL 45 shares                   |

### _Steps_
```text
1) Enter total asset = 100000
2) Enter IBM variance = +10
3) Enter unit price = 220
4) Click Rebalance
```
### _Expected Results:_
```text
ORCL -> SELL 45 shares
```

## TC-003 No Action Required
| Field        | Value        |
| ------------ | ------------ |
| Test Case ID | TC-003       |
| Input        | Variance = 0 |
| Expected     | Shares = 0   |

Applicable for:
* MSFT
* AAPL
* HD

## TC-004 Multiple Securities
### _Input_
```text
Entire portfolio
```
### _Expected_
```text
* IBM  → BUY 67
* MSFT → 0
* ORCL → SELL 45
* AAPL → 0
* HD   → 0
```

## TC-005 Decimal Share Calculation
### _Input_
```text
Total Asset = 100000
Variance = -3%
Price = 37
```
### _Expected_
```text
3000/37 = 81.08
Rounded = 81
```

## TC-006 Zero Price
### _Input_
```text
Price = 0
```

### _Expected_
```text
Validation Error
"Price must be greater than zero"
```


## TC-007 Negative Price

### _Expected:_
```text
Validation Error
```

## TC-008 Empty Inputs

### _Expected:_
```text
Required field validation
```


## TC-009 Large Portfolio
### _Input_
```text
Total Asset = 1,000,000,000
```
### _Expected_
```text
Calculation completes correctly
No overflow
```

## TC-010 Sum of Buy Equals Sum of Sell
### _Input_
```text
Original portfolio
```
### _Expected_
```text
Buy Amount = $10,000
Sell Amount = $10,000

Portfolio remains balanced.
```