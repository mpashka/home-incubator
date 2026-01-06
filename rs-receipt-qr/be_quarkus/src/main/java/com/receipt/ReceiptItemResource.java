package com.receipt;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/receipt-items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReceiptItemResource {

    @Inject
    ReceiptItemMapper receiptItemMapper;

    @GET
    @Path("/by-receipt/{receiptId}")
    public List<ReceiptItem> getByReceiptId(@PathParam("receiptId") Integer receiptId) {
        return receiptItemMapper.getByReceiptId(receiptId);
    }

    @GET
    @Path("/by-user/{userId}")
    public List<ReceiptItem> getByUserId(@PathParam("userId") Integer userId) {
        return receiptItemMapper.getByUserId(userId);
    }

    @GET
    @Path("/{id}")
    public ReceiptItem getById(@PathParam("id") Integer id) {
        return receiptItemMapper.getById(id);
    }

    @POST
    public void create(ReceiptItem item) {
        receiptItemMapper.insert(item);
    }

    @PUT
    @Path("/{id}")
    public void update(@PathParam("id") Integer id, ReceiptItem item) {
        item.setId(id);
        receiptItemMapper.update(item);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Integer id) {
        receiptItemMapper.delete(id);
    }

    @PUT
    @Path("/{id}/tags")
    public void updateTags(@PathParam("id") Integer id, java.util.Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<String> tags = (java.util.List<String>) body.get("tags");
        ReceiptItem item = receiptItemMapper.getById(id);
        item.setTags(tags);
        receiptItemMapper.update(item);
    }
}
