package com.receipt;

import com.receipt.dto.InvoiceItem;
import com.receipt.dto.InvoiceResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class QrProcessingService {

    private static final Logger LOGGER = Logger.getLogger(QrProcessingService.class.getName());

    @Inject
    ReceiptRawMapper receiptRawMapper;

    @Inject
    ReceiptMapper receiptMapper;

    @Inject
    ReceiptItemMapper receiptItemMapper;

    @Inject
    ShopPointMapper shopPointMapper;

    public void processQrCode(Integer receiptRawId) {
        ReceiptRaw receiptRaw = receiptRawMapper.getById(receiptRawId);
        if (receiptRaw == null || !"pending".equals(receiptRaw.getStatus())) {
            return;
        }

        try {
            receiptRaw.setStatus("processing");
            receiptRawMapper.update(receiptRaw);

            // Parse QR URL parameters
            URI uri = new URI(receiptRaw.getUrl());
            String query = uri.getQuery();
            String iic = getParameter(query, "iic");
            String crtd = getParameter(query, "crtd");
            String tin = getParameter(query, "tin");

            if (iic == null || crtd == null || tin == null) {
                throw new Exception("Missing required QR parameters");
            }

            // Call tax.gov.me API
            InvoiceResource invoice = fetchInvoiceFromTaxApi(iic, crtd, tin);

            // Find or create shop_point by tax_id
            Integer taxId = Integer.parseInt(tin);
            ShopPoint shopPoint = shopPointMapper.getByTaxId(taxId);
            if (shopPoint == null) {
                shopPoint = new ShopPoint();
                shopPoint.setTaxId(taxId);
                shopPoint.setName("Shop-" + taxId);
                shopPointMapper.insert(shopPoint);
            }

            // Create receipt
            Receipt receipt = new Receipt();
            receipt.setUserId(receiptRaw.getUserId());
            receipt.setShopPointId(shopPoint.getId());
            receipt.setPosTime(parseDateTimeCreated(invoice.getDateTimeCreated()));
            receipt.setTotal(BigDecimal.valueOf(invoice.getTotalPrice()));
            receipt.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            receiptMapper.insert(receipt);

            // Create receipt items
            for (InvoiceItem invoiceItem : invoice.getItems()) {
                ReceiptItem receiptItem = new ReceiptItem();
                receiptItem.setUserId(receiptRaw.getUserId());
                receiptItem.setReceiptId(receipt.getId());
                receiptItem.setName(invoiceItem.getName());
                receiptItem.setPrice(BigDecimal.valueOf(invoiceItem.getPriceAfterVat()));
                receiptItem.setQuantity(invoiceItem.getQuantity().intValue());
                receiptItemMapper.insert(receiptItem);
            }

            // Update receipt_raw with receipt_id and status
            receiptRaw.setReceiptId(receipt.getId());
            receiptRaw.setStatus("completed");
            receiptRawMapper.update(receiptRaw);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to process QR code: " + receiptRawId, e);
            receiptRaw.setStatus("failed");
            receiptRawMapper.update(receiptRaw);
        }
    }

    private InvoiceResource fetchInvoiceFromTaxApi(String iic, String crtd, String tin) throws Exception {
        Client client = ClientBuilder.newClient();
        try {
            Form form = new Form();
            form.param("iic", iic);
            form.param("dateTimeCreated", crtd);
            form.param("tin", tin);

            InvoiceResource response = client.target("https://mapr.tax.gov.me/ic/api/verifyInvoice")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.form(form), InvoiceResource.class);

            return response;
        } finally {
            client.close();
        }
    }

    private String getParameter(String query, String name) {
        if (query == null) return null;
        String[] params = query.split("&");
        for (String param : params) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(name)) {
                return pair[1];
            }
        }
        return null;
    }

    private Timestamp parseDateTimeCreated(String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
            return Timestamp.valueOf(localDateTime);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse date: " + dateTimeStr, e);
            return new Timestamp(System.currentTimeMillis());
        }
    }
}
