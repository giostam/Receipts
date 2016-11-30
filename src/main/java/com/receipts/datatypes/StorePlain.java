/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.receipts.datatypes;

import java.util.Date;
import javax.persistence.Transient;

/**
 *
 * @author StamaterisG
 */
public class StorePlain {

    private Integer id;
    private String storeId;
    private Date storeDate;
    private Date importDate;
    private boolean complete;
    private Integer firstReceiptId;
    private Integer lastReceiptId;
    private Integer receiptsCnt = 0;
    private Integer completedCnt = 0;
    private Integer productsCnt = 0;

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

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Integer getFirstReceiptId() {
        return firstReceiptId;
    }

    public void setFirstReceiptId(Integer firstReceiptId) {
        this.firstReceiptId = firstReceiptId;
    }

    public Integer getLastReceiptId() {
        return lastReceiptId;
    }

    public void setLastReceiptId(Integer lastReceiptId) {
        this.lastReceiptId = lastReceiptId;
    }

    public Integer getReceiptsCnt() {
        return receiptsCnt;
    }

    public void setReceiptsCnt(Integer receiptsCnt) {
        this.receiptsCnt = receiptsCnt;
    }

    public Integer getCompletedCnt() {
        return completedCnt;
    }

    public void setCompletedCnt(Integer completedCnt) {
        this.completedCnt = completedCnt;
    }

    public Integer getProductsCnt() {
        return productsCnt;
    }

    public void setProductsCnt(Integer productsCnt) {
        this.productsCnt = productsCnt;
    }

    @Transient
    private String filterForStoreDate;

    public String getFilterForStoreDate() {
        return filterForStoreDate;
    }

    public void setFilterForStoreDate(String filterForStoreDate) {
        this.filterForStoreDate = filterForStoreDate;
    }
}
