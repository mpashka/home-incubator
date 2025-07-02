package com.receipt;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/purchase-items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PurchaseItemResource {

    @Inject
    PurchaseItemMapper purchaseItemMapper;

    @GET
    @Path("/by-receipt/{receiptId}")
    public List<PurchaseItem> getByReceiptId(@PathParam("receiptId") Integer receiptId) {
        return purchaseItemMapper.getByReceiptId(receiptId);
    }

    @GET
    @Path("/{id}")
    public PurchaseItem getById(@PathParam("id") Integer id) {
        return purchaseItemMapper.getById(id);
    }

    @POST
    public void create(PurchaseItem item) {
        purchaseItemMapper.insert(item);
    }

    @PUT
    @Path("/{id}")
    public void update(@PathParam("id") Integer id, PurchaseItem item) {
        item.setId(id);
        purchaseItemMapper.update(item);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Integer id) {
        purchaseItemMapper.delete(id);
    }

    @PUT
    @Path("/{id}/tags")
    public void updateTags(@PathParam("id") Integer id, java.util.Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<String> tags = (java.util.List<String>) body.get("tags");
        PurchaseItem item = purchaseItemMapper.getById(id);
        item.setTags(tags);
        purchaseItemMapper.update(item);
    }
} 