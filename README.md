# SLedger

SLedger aka Simple Ledger is a decentralized credit lending network that facilitates micropayments across the FakeChain blockchain. 

### To run SLedger
Download the jar file SLedger.jar and navigate to its directory and complete the following command:
```sh
$ java -jar SLedger.jar Name RSApublicKey RSAprivateKey Startingamount
```
#### Note: 
 - 	keys should be a raw string excluding the <---> headers OR anything you choose.
 
Example:
>MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3gonoq5MgzGUGZ07XO2
ln2yU8xaYu6CNdC8L14f4GJy8zXpTMtk/kqdLxQSnXKYI8nzrlon4rVQz1piuMwiZS
1fIz80JpVSDoCThzZ+UQbBy/pj+jXSYC1I1jRz1hFYIiXGSCYwahEqk6rzUKR+L8v6Z
SQ5y5Vj3eIGjP9D+AQIDAQAB

##Operation Notes:
- Settles when Trustline balance > 100… if not enough money to pay out then Trustline gets reset


### Todos

 - 	Scanner input from server and main thread – gets hung up when prompting for incoming Trustline
 - Registers wrong ip address if local ip address resolver does not return wireless LAN adapter
 - Remove user balances … can settle using just Trustline balances

 - 	Concurrency verification
 -  Encrypting Trustline traffic


**Suprr's first delve into the blockchain space**
