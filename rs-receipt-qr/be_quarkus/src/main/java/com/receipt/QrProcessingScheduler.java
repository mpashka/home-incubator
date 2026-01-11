package com.receipt;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class QrProcessingScheduler {

    private static final Logger LOGGER = Logger.getLogger(QrProcessingScheduler.class.getName());

    @Inject
    QrProcessingService qrProcessingService;

    @Inject
    ReceiptRawMapper receiptRawMapper;

//    @Scheduled(every = "30s")
    void processPendingQrCodes() {
        List<ReceiptRaw> pendingReceipts = receiptRawMapper.getByStatus("pending");

        LOGGER.info("Processing " + pendingReceipts.size() + " pending QR codes");

        for (ReceiptRaw receiptRaw : pendingReceipts) {
            try {
                qrProcessingService.processQrCode(receiptRaw.getId());
            } catch (Exception e) {
                LOGGER.severe("Failed to process QR code " + receiptRaw.getId() + ": " + e.getMessage());
            }
        }
    }
}
