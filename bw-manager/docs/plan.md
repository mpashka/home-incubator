1. Парсить android URL
2. Надо создать некий site-id
3. Для android добавить маппинг
4. собрать все url для сайта
5. собрать все пароли для сайта


---
jq '.items[]|select(.login.totp != null)' bitwarden_export_20251101162039.json |less
jq '.items[].login.uris[].uri|select(.|startswith("android"))|.' bitwarden_export_20251101162039.json |less
