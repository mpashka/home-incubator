В сербии используется система учёта покупок и генерации QR кодов TaxCore.
TaxCore - система, созданная компанией Data Tech International https://dti.rs/
В настоящее время используется версия TaxCore 3.5.16.0

## Описание TaxCore 
TaxCore описывает 
Документация TaxCore, а также веб приложение проверки и получения информации о QR
коде находится на сайте https://suf.purs.gov.rs/

Ссылки на веб документацию:
- на английском: https://tap.suf.purs.gov.rs/help/view/388044557/Readme/en-US
- на сербском: https://tap.suf.purs.gov.rs/help/view/585016726/Увод/sr-Cyrl-RS
- введение - https://tap.suf.purs.gov.rs/help/view/388044557/Readme/en-US @Tax_Eng_Readme.pdf @Tax_Srb_Readme.pdf
- описание - https://tap.suf.purs.gov.rs/help/view/388044557/Overview/en-US @Tax_Eng_Overview.pdf
- конфиденциальность - https://tap.suf.purs.gov.rs/help/view/388044557/Confidentiality/en-US - @Tax_Eng_Confidentiality.pdf
- концепции - https://tap.suf.purs.gov.rs/help/view/388044557/Concepts/en-US - @Tax_Eng_Concepts.pdf @Tax_Srb_Concepts.pdf
- приложение - https://tap.suf.purs.gov.rs/help/view/585016726/Апликације/sr-Cyrl-RS - @Tax_Srb_Application.pdf
- техническая документация - https://tap.suf.purs.gov.rs/help/view/388044557/Concepts/en-US - @Tax_Eng_TechDoc.pdf @Tax_Srb_TechDoc.pdf

Содержит
- Описание системы проверки чеков 
- Как проверить чек без наличия QR кода и URL

## Примеры
- @example - файлы примера QR кода:
- @example/qr-url.txt - URL из QR кода чека
- @example/qr.bin - декодированное бинарное поле vl из ссылки URL 
- @example/response.json - URL response при использовании http header "Accept: application/json" 
- @example/response-formatted.json - файл @example/response.json с перекодированными UTF-8 символами 
- @example/response-items.json - ответ http POST https://suf.purs.gov.rs/specifications с указанием
  invoiceNumber из чека и token из оригинальной веб страницы QR URL

