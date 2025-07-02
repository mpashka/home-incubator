Each receipt has QR code. Receipt QR is URL with the following parameters:
iic, tin, crtd, ord, bu, cr, sw, prc

QR code example: https://mapr.tax.gov.me/ic/#/verify?iic=bf672101f08f9c7cf65b94925e4554d2&tin=02440261&crtd=2025-07-01T13:26:59%2002:00&ord=yq104ib105%2F26461%2F2025%2Fyw481jg936&bu=yq104ib105&cr=yw481jg936&sw=jk606um818&prc=11.67


QR code parameters example:
iic=bf672101f08f9c7cf65b94925e4554d2
tin=02440261
crtd=2025-07-01T13:26:59%2002:00 (URL decoded: 2025-07-01T13:26:59 02:00)
ord=yq104ib105%2F26461%2F2025%2Fyw481jg936 (URL decoded: yq104ib105/26461/2025/yw481jg936)
bu=yq104ib105
cr=yw481jg936
sw=jk606um818
prc=11.67

Receipt request URL example: {
    method: POST
    URL: https://mapr.tax.gov.me/ic/api/verifyInvoice
    form data: {
        iic: bf672101f08f9c7cf65b94925e4554d2
        dateTimeCreated: 2025-07-01T13:26:59 02:00
        tin: 02440261
    }
}

Receipt reponse example is in file: qr-response-example.json
