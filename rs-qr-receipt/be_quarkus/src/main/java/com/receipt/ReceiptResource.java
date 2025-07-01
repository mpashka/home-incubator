package com.receipt;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/receipts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReceiptResource {

    @Inject
    ReceiptMapper receiptMapper;

    @GET
    public List<Receipt> getAll() {
        return receiptMapper.getAll();
    }

    @GET
    @Path("/{id}")
    public Receipt getById(@PathParam("id") Integer id) {
        return receiptMapper.getById(id);
    }

    @POST
    public void create(Receipt receipt) {
        receiptMapper.insert(receipt);
    }

    @POST
    @Path("/with-items")
    public void createWithItems(ReceiptWithItems data, @Inject PurchaseItemMapper purchaseItemMapper) {
        receiptMapper.insert(data.receipt);
        if (data.items != null) {
            for (PurchaseItem item : data.items) {
                item.setReceiptId(data.receipt.getId());
                purchaseItemMapper.insert(item);
            }
        }
    }

    @PUT
    @Path("/{id}")
    public void update(@PathParam("id") Integer id, Receipt receipt) {
        receipt.setId(id);
        receiptMapper.update(receipt);
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Integer id) {
        receiptMapper.delete(id);
    }

    @PUT
    @Path("/{id}/tags")
    public void updateTags(@PathParam("id") Integer id, java.util.Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<String> tags = (java.util.List<String>) body.get("tags");
        Receipt receipt = receiptMapper.getById(id);
        receipt.setTags(tags);
        receiptMapper.update(receipt);
    }
}

class ReceiptWithItems {
    public Receipt receipt;
    public List<PurchaseItem> items;
}
