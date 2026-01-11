package com.receipt;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/api/receipt-raw")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReceiptRawResource {

    @Inject
    ReceiptRawMapper receiptRawMapper;

    @Inject
    ReceiptMapper receiptMapper;

    @Inject
    ReceiptItemMapper receiptItemMapper;

    @POST
    public Map<String, Object> create(Map<String, Object> request) {
        ReceiptRaw receiptRaw = new ReceiptRaw();
        receiptRaw.setUserId((Integer) request.get("userId"));
        receiptRaw.setUrl((String) request.get("url"));
        receiptRaw.setStatus("pending");
        receiptRaw.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        receiptRawMapper.insert(receiptRaw);

        Map<String, Object> response = new HashMap<>();
        response.put("receiptRawId", receiptRaw.getId());
        response.put("status", receiptRaw.getStatus());
        return response;
    }

    @GET
    @Path("/{id}")
    public Map<String, Object> getStatus(@PathParam("id") Integer id) {
        ReceiptRaw receiptRaw = receiptRawMapper.getById(id);

        Map<String, Object> response = new HashMap<>();
        if (receiptRaw != null) {
            response.put("receiptRawId", receiptRaw.getId());
            response.put("status", receiptRaw.getStatus());
            response.put("receiptId", receiptRaw.getReceiptId());
        }
        return response;
    }

    @GET
    @Path("/{id}/receipt")
    public Map<String, Object> getReceiptWithItems(@PathParam("id") Integer id) {
        ReceiptRaw receiptRaw = receiptRawMapper.getById(id);

        Map<String, Object> response = new HashMap<>();
        if (receiptRaw != null && receiptRaw.getReceiptId() != null) {
            Receipt receipt = receiptMapper.getById(receiptRaw.getReceiptId());
            List<ReceiptItem> items = receiptItemMapper.getByReceiptId(receiptRaw.getReceiptId());

            response.put("receipt", receipt);
            response.put("items", items);
        }
        return response;
    }

    @GET
    @Path("/pending")
    public List<ReceiptRaw> getPending() {
        return receiptRawMapper.getByStatus("pending");
    }

    @GET
    @Path("/user/{userId}")
    public List<ReceiptRaw> getByUserId(@PathParam("userId") Integer userId) {
        return receiptRawMapper.getByUserId(userId);
    }
}
