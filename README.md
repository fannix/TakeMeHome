# TakeMeHome

## Introduction

This is the Andorid app for for the project [TakeMeHome](https://fannix.github.io/blockchain/2018/03/04/TakeMeHome.html)

To run this app, you need to create two smart contracts. For testing purpose, I already deploy two contracts in testnet,

please take a look at the the contract project page for the codes and the addresses at  <https://github.com/fannix/TakeMeHomeContract>.


## compilation and testing

To compile the project, you will need to install AndroidStudio.

To test it, you need an android device, either with a real one or a simulated one. I already tested it with a real Samgsung Galaxy tab s3 (API 24).
You also need a real iBeacon or a Beacon simulator. I used the "Locate" ios app which is developed by Radius Networks,
and used the Null iBeacon (id1: 00000000-0000-0000-0000-000000000000 id2: 0 id3: 0) to trigger beacon events.

To verify the app actually submit location and get reward, you can look into its storage, which saves the state of the contract.
For example, we can open the TakeMeHome app and then simulate an beacon. 
This will trigger a location submission event. We can then verify that 
the contract send reward to `0000000000000000000000000000000000000001` (our example wallet address used in the app):

```bash

curl -X POST -i 'http://seed2.neo.org:20332' --data '{
  "jsonrpc": "2.0",
  "method": "getstorage",
  "params": ["011ce07245481a06042f039407f6b7737e443e47", "0000000000000000000000000000000000000001"],
  "id": 15
}'
```

It should return non null result like:

`{"jsonrpc":"2.0","id":15,"result":"01"}`

This means that we did get 1 token reward by submitting the location.

If you want to see the app in action but don't want to install and compile the codes, you can take a look at <https://youtu.be/l5xETZ_naJw>