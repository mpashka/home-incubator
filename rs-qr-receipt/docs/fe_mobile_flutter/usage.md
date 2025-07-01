# fe_mobile_flutter Usage Instructions

## 1. QR Code Scanning and Invoice Import
- Tap the "Scan QR Code" button on the home page.
- Scan a Montenegrin receipt QR code.
- The app will:
  1. Parse the QR code URL and extract `iic`, `crtd`, and `tin` parameters.
  2. Fetch invoice data from https://mapr.tax.gov.me/ic/api/verifyInvoice using these parameters.
  3. Parse the returned JSON into Dart objects.
  4. Save the receipt and its items to the local Drift database.
- Success or error messages are shown at the bottom of the scanner screen.

## 2. Syncing Receipts and Purchases to Backend
- Tap the "Sync Receipts to Backend" button on the home page.
- The app will:
  1. Read all receipts and their purchase items from the local database.
  2. Serialize them to JSON.
  3. Send them to the backend Quarkus server at `/api/receipts/with-items`.
  4. Show a progress indicator and display success or error messages.

## 3. Requirements
- Make sure to run `flutter pub get` to install dependencies.
- Run `flutter pub run build_runner build --delete-conflicting-outputs` to generate Drift database code.
- Update the backend URL in `main.dart` and `sync_service.dart` as needed.

## 4. Notes
- The app uses Drift for local storage, HTTP for backend and invoice API communication, and `qr_code_scanner` for QR scanning.
- All data is stored locally and can be synced to the backend when online. 