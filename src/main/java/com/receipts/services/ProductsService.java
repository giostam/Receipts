/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.receipts.services;

import com.receipts.utils.DbUtils;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author StamaterisG
 */
@ManagedBean(name = "productsService")
@ApplicationScoped
public class ProductsService implements Serializable {

    public ProductsService() {
    }
    
    public void insertProduct(String productDescription, BigDecimal price, BigDecimal quantity, int receiptFk, int userId) {
        String sql = "INSERT INTO PRODUCTS (PRODUCT_NAME, PRODUCT_PRICE, PRODUCT_QUANTITY, RECEIPT_FK, USER_ID) VALUES (?, ?, ?, ? ,?)";
        
        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, productDescription);
            ps.setBigDecimal(2, price);
            ps.setBigDecimal(3, quantity);
            ps.setInt(4, receiptFk);
            ps.setInt(5, userId);
            
            ps.executeUpdate();
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        } 
    }
    
    public void deleteProductById(int productId) {
        String sql = "DELETE FROM PRODUCTS WHERE ID = ?";
        
        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
}
