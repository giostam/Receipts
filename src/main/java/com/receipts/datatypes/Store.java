/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.receipts.datatypes;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author StamaterisG
 */
public class Store {
    private Integer id;
    private String storeId;
    private Date storeDate;
    private List<Receipt> receipts;
    private boolean complete;
    private Integer firstReceiptId;
    private Integer lastReceiptId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Date getStoreDate() {
        return storeDate;
    }

    public void setStoreDate(Date storeDate) {
        this.storeDate = storeDate;
    }

    public List<Receipt> getReceipts() {
        Collections.sort(receipts, new ReceiptComparator());
        
        return receipts;
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
    }

    public boolean isComplete() {
        complete = true;
        for (Receipt receipt : getReceipts()) {
            if (!receipt.isComplete()) {
                complete = false;
            }
        }
        
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Integer getFirstReceiptId() {
        firstReceiptId = getReceipts().get(0).getId();
        
        return firstReceiptId;
    }

    public Integer getLastReceiptId() {
        lastReceiptId = getReceipts().get(getReceipts().size() - 1).getId();
        
        return lastReceiptId;
    }
}
