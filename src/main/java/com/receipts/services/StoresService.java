package com.receipts.services;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.receipts.datatypes.Product;
import com.receipts.datatypes.Receipt;
import com.receipts.datatypes.ReceiptComparator;
import com.receipts.datatypes.Store;
import com.receipts.datatypes.StorePlain;
import com.receipts.utils.DbUtils;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author StamaterisG
 */
@ManagedBean(name = "storesService")
@ApplicationScoped
public class StoresService implements Serializable {

    /**
     * Creates a new instance of StoresService
     */
    public StoresService() {
    }

    public List<StorePlain> loadAllStores() {
        List<StorePlain> allStores = new ArrayList<>();

        String storesSql = "SELECT ID, STORE_ID, STORE_DATE, IMPORT_DATE FROM STORES ORDER BY IMPORT_DATE DESC";
        String receiptsSql = "SELECT ID, RECEIPT_TIME, IMG_NAME, COMPLETE, COMPLETE_DATE FROM RECEIPTS WHERE STORE_FK = ?";
        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement storesPs = conn.prepareStatement(storesSql);
                PreparedStatement receiptsPs = conn.prepareStatement(receiptsSql)) {

            try (ResultSet storeRs = storesPs.executeQuery()) {
                while (storeRs.next()) {
                    StorePlain store = new StorePlain();
                    store.setId(storeRs.getInt("ID"));
                    store.setStoreId(storeRs.getString("STORE_ID"));
                    store.setStoreDate(storeRs.getDate("STORE_DATE"));
                    store.setImportDate(storeRs.getDate("IMPORT_DATE"));

                    boolean complete = true;

                    int completeReceiptsCnt = 0;
                    int productsCnt = 0;
                    receiptsPs.setInt(1, storeRs.getInt("ID"));
                    List<Receipt> receipts = new ArrayList<>();
                    try (ResultSet receiptsRs = receiptsPs.executeQuery()) {
                        while (receiptsRs.next()) {
                            Receipt receipt = new Receipt();
                            receipt.setId(receiptsRs.getInt("ID"));
                            receipt.setReceiptTime(receiptsRs.getString("RECEIPT_TIME"));
                            receipt.setImgName(receiptsRs.getString("IMG_NAME"));
                            receipt.setComplete(receiptsRs.getInt("COMPLETE") == 1);
                            if (receiptsRs.getTimestamp("COMPLETE_DATE") != null) {
                                receipt.setCompleteDate(new Date(receiptsRs.getTimestamp("COMPLETE_DATE").getTime()));
                            }

                            if (receiptsRs.getInt("COMPLETE") == 0) {
                                complete = false;
                            } else {
                                completeReceiptsCnt++;
                                String productsSql = "SELECT COUNT(*) FROM PRODUCTS WHERE RECEIPT_FK = ?";
                                try (PreparedStatement productsPs = conn.prepareStatement(productsSql)) {
                                    productsPs.setInt(1, receipt.getId());
                                    try (ResultSet productsRs = productsPs.executeQuery()) {
                                        while (productsRs.next()) {
                                            int products = productsRs.getInt(1);
                                            productsCnt += products;
                                        }
                                    }
                                }
                            }

                            receipts.add(receipt);
                        }
                    }

                    store.setProductsCnt(productsCnt);

                    if (!receipts.isEmpty()) {
                        store.setCompletedCnt(completeReceiptsCnt);
                        store.setReceiptsCnt(receipts.size());

                        Collections.sort(receipts, new ReceiptComparator());
                        store.setFirstReceiptId(receipts.get(0).getId());
                        store.setLastReceiptId(receipts.get(receipts.size() - 1).getId());
                    }
                    store.setComplete(complete);
                    allStores.add(store);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return allStores;
    }

    public void deleteStore(int storeId) {
        String sql = "DELETE FROM STORES WHERE ID = ?";

        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, storeId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Store getStoreById(int storeId) {
        Store store = null;

        String storesSql = "SELECT ID, STORE_ID, STORE_DATE FROM STORES WHERE ID = ?";
        String receiptsSql = "SELECT ID, RECEIPT_TIME, IMG_NAME, COMPLETE, COMPLETE_DATE FROM RECEIPTS WHERE STORE_FK = ?";
        String productsSql = "SELECT ID, PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_QUANTITY FROM PRODUCTS WHERE RECEIPT_FK = ?";
        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement storesPs = conn.prepareStatement(storesSql);
                PreparedStatement receiptsPs = conn.prepareStatement(receiptsSql);
                PreparedStatement productsPs = conn.prepareStatement(productsSql)) {

            storesPs.setInt(1, storeId);
            try (ResultSet storeRs = storesPs.executeQuery()) {
                while (storeRs.next()) {
                    store = new Store();
                    store.setId(storeRs.getInt("ID"));
                    store.setStoreId(storeRs.getString("STORE_ID"));
                    store.setStoreDate(storeRs.getDate("STORE_DATE"));

                    receiptsPs.setInt(1, storeRs.getInt("ID"));
                    List<Receipt> receipts = new ArrayList<>();
                    try (ResultSet receiptsRs = receiptsPs.executeQuery()) {
                        while (receiptsRs.next()) {
                            Receipt receipt = new Receipt();
                            receipt.setId(receiptsRs.getInt("ID"));
                            receipt.setReceiptTime(receiptsRs.getString("RECEIPT_TIME"));
                            receipt.setImgName(receiptsRs.getString("IMG_NAME"));
                            receipt.setComplete(receiptsRs.getInt("COMPLETE") == 1);
                            if (receiptsRs.getTimestamp("COMPLETE_DATE") != null) {
                                receipt.setCompleteDate(new Date(receiptsRs.getTimestamp("COMPLETE_DATE").getTime()));
                            }

                            BigDecimal priceTotal = BigDecimal.ZERO;
                            productsPs.setInt(1, receiptsRs.getInt("ID"));
                            List<Product> products = new ArrayList<>();
                            try (ResultSet productsRs = productsPs.executeQuery()) {
                                while (productsRs.next()) {
                                    Product product = new Product();
                                    product.setId(productsRs.getInt("ID"));
                                    product.setProductName(productsRs.getString("PRODUCT_NAME"));
                                    product.setProductPrice(productsRs.getBigDecimal("PRODUCT_PRICE"));
                                    product.setProductQuantity(productsRs.getBigDecimal("PRODUCT_QUANTITY"));

                                    products.add(product);

                                    priceTotal = priceTotal.add(productsRs.getBigDecimal("PRODUCT_PRICE"));
                                }
                            }
                            receipt.setPriceTotal(priceTotal);

                            receipt.setProducts(products);
                            receipts.add(receipt);
                        }
                    }

                    store.setReceipts(receipts);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return store;
    }
}
