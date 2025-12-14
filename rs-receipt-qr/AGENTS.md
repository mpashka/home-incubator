This is "rs-receipt" application. 

This application allows you to save, manage and access receipts.
Receipts which are issued in Serbia upon purchase have QR code with purchase information.
This application allows you to scan QR code, save purchase information into the database and
allows you to analyze expenses and find receipts for warranty service.

The Application consists of the following modules:
- *`/be_quarkus/`* backend. Written in java, uses quarkus framework, uses PostgreSQL to store data, myBatis as persistence framework
- *`/fe_mobile_flutter/`* mobile frontend. Written in flutter. Allows user to scan receipts QR codes, sends information to backend. Allows purchase browse, filter, search, tagging. Stores all purchase information locally, synchronizes information when online. Uses drift to store local data.
- *`/fe_web_vue/`* web frontend. Written in TypeScript, uses vue web framework, uses vue material components. Allows user to browse purchases, receipts. Allows to filter, search, tag purchases and receipts. Provides analytics information about expenses - expenses per month, expenses for specific type - e.g. food, amount of specific product or product type - e.g. milk, vegetables, entertaiment, e.t.c.

- *`@/docs/readme.md`* - modules description 
- *`@/docs/`* project folder contains project documentation, entity and behavior diagrams, er database diagram.
- *`@/docs/er_diagram.md`* er database diagram.

Use and modify documentation if needed.
