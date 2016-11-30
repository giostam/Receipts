/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.receipts.services;

import com.receipts.datatypes.Product;
import com.receipts.datatypes.Receipt;
import com.receipts.utils.DbUtils;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import org.joda.time.DateTime;

/**
 *
 * @author StamaterisG
 */
@ManagedBean(name = "receiptsService")
@ApplicationScoped
public class ReceiptsService {

    /**
     * Creates a new instance of ReceiptsService
     */
    public ReceiptsService() {
    }

    public Receipt getReceiptById(int receiptId) {
        Receipt receipt = null;

        String receiptsSql = "SELECT RECEIPT_TIME, COMPLETE, IMG_NAME, COMPLETE_DATE FROM RECEIPTS WHERE ID = ?";
        String productsSql = "SELECT ID, PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_QUANTITY FROM PRODUCTS WHERE RECEIPT_FK = ?";
        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement receiptsPs = conn.prepareStatement(receiptsSql);
                PreparedStatement productsPs = conn.prepareStatement(productsSql)) {

            receiptsPs.setInt(1, receiptId);
            productsPs.setInt(1, receiptId);

            try (ResultSet receiptsRs = receiptsPs.executeQuery()) {
                while (receiptsRs.next()) {
                    receipt = new Receipt();
                    String receiptTime = receiptsRs.getString("RECEIPT_TIME");
                    int complete = receiptsRs.getInt("COMPLETE");
                    String imgName = receiptsRs.getString("IMG_NAME");
                    Timestamp completeDate = receiptsRs.getTimestamp("COMPLETE_DATE");

                    receipt.setId(receiptId);
                    receipt.setReceiptTime(receiptTime);
                    receipt.setComplete(complete == 1);
                    receipt.setImgName(imgName);
                    if (completeDate != null) {
                        receipt.setCompleteDate(new Date(completeDate.getTime()));
                    }

                    BigDecimal priceTotal = BigDecimal.ZERO;
                    List<Product> products = new ArrayList<>();
                    try (ResultSet productsRs = productsPs.executeQuery()) {
                        while (productsRs.next()) {
                            Product product = new Product();

                            Integer id = productsRs.getInt("ID");
                            String productName = productsRs.getString("PRODUCT_NAME");
                            BigDecimal productPrice = productsRs.getBigDecimal("PRODUCT_PRICE");
                            BigDecimal productQuantity = productsRs.getBigDecimal("PRODUCT_QUANTITY");

                            product.setId(id);
                            product.setProductName(productName);
                            product.setProductPrice(productPrice);
                            product.setProductQuantity(productQuantity);

                            products.add(product);

                            priceTotal = priceTotal.add(productPrice);
                        }
                    }
                    receipt.setPriceTotal(priceTotal);

                    receipt.setProducts(products);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return receipt;
    }

    public void setReceiptCompleteStatus(int receiptId, boolean complete, String time) {
        StringBuilder sqlSb = new StringBuilder();

        sqlSb.append("UPDATE RECEIPTS SET COMPLETE = ?, COMPLETE_DATE = ?");
        if (time != null) {
            sqlSb.append(", RECEIPT_TIME = ?");
        }
        sqlSb.append(" WHERE ID = ?");

        String sql = sqlSb.toString();
        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, complete ? 1 : 0);
            if (complete) {
                ps.setTimestamp(2, new Timestamp(DateTime.now().getMillis()));
            } else {
                ps.setNull(2, Types.NULL);
            }
            if (time != null) {
                ps.setString(3, time);
                ps.setInt(4, receiptId);
            } else {
                ps.setInt(3, receiptId);
            }
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
