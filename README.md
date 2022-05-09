![`market_maker_scheme`](https://github.com/bloxmove-com/market-maker/blob/main/src/main/resources/images/market_maker_scheme.PNG)

Each actor is working on separate thread and communication between each other using events
### Guardian
Main actor responsible for whole actor system
### REST API
Endpoints to control market maker working, sending requests to Exchange Manager Actor using wrapper
### Exchange Manager Actor
Main functional actor responsible for controlling specific exchanges actors
### Exchange Actor
Actor represents each exchange and responsible for controlling market maker actors
### Market Maker Actor
Actor represents each currency pair and is responsible for managing working cycles. Each cycle actor is creating 
Place Orders Actor and send request for Check Balance Actor
### Place Orders Actor 
Actor is responsible for placing orders and after finishing its work is getting stopped
### Check Balance Actor 
Actor is responsible for balance check. If balance is getting too low actor sends email and creates notification

#REST API

# Market maker
## Create market maker
#### ***Request format*** 
POST .../market-maker
#### ***Request Parameter***
| Field             | Type          | Required      | Description                                                               |
| ----------------- | ------------- | ------------- | ------------------------------------------------------------------------- |
| exchangeName      | String        | yes           | Exchange Name (e.g., bitmart, binance)                                    |
| currencyPair      | String        | yes           | Currency Pair (e.g., BLXM_USDT, BTC_USDT)                                 |
| amount            | Number        | yes           | Total amount of first currency equivalent per cycle                       |
| minPrice          | Number        | yes           | Bottom value of price range                                               |
| targetPrice       | Number        | yes           | Target value of price range                                               |
| maxPrice          | Number        | yes           | Top value of price range                                                  |
| delay             | Number        | yes           | Interval between cycles in seconds                                        |
| gridCount         | Number        | yes           | Amount of orders needs to be placed per cycle on each side (sell and buy) |
| minFirstCurrency  | Number        | yes           | Minimal amount of first currency that should trigger notification         |
| minSecondCurrency | Number        | yes           | Minimal amount of second currency that should trigger notification        |
#### ***JSON request example***
```json
{
  "exchangeName": "bitmart",
  "currencyPair": "BLXM_USDT",
  "amount": 70.0,
  "minPrice": 0.46,
  "targetPrice": 0.56,
  "maxPrice": 0.66,
  "delay": 60,
  "gridCount": 1,
  "minFirstCurrency": 100.0,
  "minSecondCurrency": 100.0
}
```
#### ***Response Data***
Status code 200

*Request for same exchange name and currency pair would update existing market maker.* 

## Stop market maker
#### ***Request format***
PUT .../market-maker
#### ***Request Parameter***
| Field             | Type          | Required      | Description                               |
| ----------------- | ------------- | ------------- | ----------------------------------------- |
| exchangeName      | String        | yes           | Exchange Name (e.g., bitmart, binance)    |
| currencyPair      | String        | yes           | Currency Pair (e.g., BLXM_USDT, BTC_USDT) |

#### ***JSON request example***
```json
{
  "exchangeName": "bitmart",
  "currencyPair": "BLXM_USDT"
}
```
#### ***Response Data***
Status code 200

*Status of market maker would be changed to **STOPPED* in database.** 

## Get market makers
#### ***Request format***
GET .../market-maker?status=WORKING
#### ***Request Parameter***
| Field             | Type          | Required      | Description                                  |
| ----------------- | ------------- | ------------- | -------------------------------------------- |
| status            | String        | yes           | Market maker status (e.g., WORKING, STOPPED) |

#### ***Response Data***
| Field             | Type          | Description                                                               |
| ----------------- | ------------- | ------------------------------------------------------------------------- |
| id                | String        | Unique id from database                                                   |
| exchangeName      | String        | Exchange Name (e.g., bitmart, binance)                                    |
| currencyPair      | String        | Currency Pair (e.g., BLXM_USDT, BTC_USDT)                                 |
| amount            | Number        | Total amount of first currency equivalent per cycle                       |
| minPrice          | Number        | Bottom value of price range                                               |
| targetPrice       | Number        | Target value of price range                                               |
| maxPrice          | Number        | Top value of price range                                                  |
| delay             | Number        | Interval between cycles in seconds                                        |
| gridCount         | Number        | Amount of orders needs to be placed per cycle on each side (sell and buy) |
| minFirstCurrency  | Number        | Minimal amount of first currency that should trigger notification         |
| minSecondCurrency | Number        | Minimal amount of second currency that should trigger notification        |
| status            | String        | Market maker status (e.g., WORKING, STOPPED)                              |
| created           | String        | Date of market maker creation                                             |
| updated           | String        | Date of market maker last update                                          |

#### ***JSON response example***
```json
[
  {
    "id": "62754358e87a8d49170f54e3",
    "exchangeName": "bitmart",
    "currencyPair": "BLXM_USDT",
    "amount": 70.0,
    "minPrice": 0.46,
    "targetPrice": 0.56,
    "maxPrice": 0.66,
    "delay": 60,
    "gridCount": 1,
    "minFirstCurrency": 100.0,
    "minSecondCurrency": 100.0,
    "status": "WORKING",
    "created": "2022-01-01T20:48:18.234Z",
    "updated": "2022-01-01T20:48:18.234Z"
  }
]
```

# Notification
## Get notifications
#### ***Request format***
GET .../notification?status=OPEN
#### ***Request Parameter***
| Field             | Type          | Required      | Description                              |
| ----------------- | ------------- | ------------- | ---------------------------------------- |
| status            | String        | yes           | Notification status (e.g., OPEN, CLOSED) |

#### ***Response Data***
| Field             | Type          | Description                                             |
| ----------------- | ------------- | ------------------------------------------------------- |
| id                | String        | Unique id from database                                 |
| type              | String        | Notification type (e.g., LOW_BALANCE, PLACE_ORDER_FAIL) |
| status            | String        | Notification status (e.g., OPEN, CLOSED)                |
| message           | String        | Detailed information about notification                 |
| created           | String        | Date of notification creation                           |
| updated           | String        | Date of notification last update                        |

#### ***JSON response example***
```json
[
  {
    "id": "62754358e87a8d49170f54e3",
    "type": "LOW_BALANCE",
    "status": "CLOSED",
    "message": "Balance of USDT is too low",
    "created": "2022-05-06T15:48:40.412Z",
    "updated": "2022-05-06T15:51:15.641Z"
  }
]
```

## Close notification
#### ***Request format***
PUT .../notification/{notificationId}
#### ***Request Parameter***
| Field             | Type          | Required      | Description             |
| ----------------- | ------------- | ------------- | ----------------------  |
| notificationId    | String        | yes           | Unique id from database |

#### ***Response Data***
Status code 200

*Status of notification would be changed to **CLOSED* in database.** 
