# SLedger

SLedger aka Simple Ledger is a decentralized credit lending network that facilitates micropayments across the FakeChain blockchain. 

### To run SLedger
Run "Main.java" in IDE with input arguments in the following format:
```sh
$ name RSApublicKey startingamount RSAprivateKey ipaddress
```
#### Note: 
 - 	keys should be a raw string excluding the <---> headers.
 
Example:
>MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3gonoq5MgzGUGZ07XO2
ln2yU8xaYu6CNdC8L14f4GJy8zXpTMtk/kqdLxQSnXKYI8nzrlon4rVQz1piuMwiZS
1fIz80JpVSDoCThzZ+UQbBy/pj+jXSYC1I1jRz1hFYIiXGSCYwahEqk6rzUKR+L8v6Z

SQ5y5Vj3eIGjP9D+AQIDAQAB

### Todos

 - 	Send transaction over socket : nested JSON unmarshalling from API issue
 - 	Receive transaction over socket 
 - 	Receive trustline over socket
 - 	Verify balance for every transaction
 - 	Settle payments over socket
 - 	Error handling/input verification
 - 	Concurrency verification


**Suprr's first delve into the blockchain space**
