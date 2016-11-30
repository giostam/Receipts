/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.receipts.services;


import com.receipts.datatypes.AutocompleteProduct;
import com.receipts.utils.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author StamaterisG
 */
@ManagedBean(name = "autocompleteService")
@ApplicationScoped
public class AutocompleteService {

    /**
     * Creates a new instance of AutocompleteService
     */
    public AutocompleteService() {
    }
    
    public List<AutocompleteProduct> listAutocompleteProducts() {
        List<AutocompleteProduct> list = new ArrayList<>();
        
        String sql = "SELECT ID, PRODUCT FROM AUTOCOMPLETE_PRODUCTS";
        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AutocompleteProduct autocompleteProduct = new AutocompleteProduct();
                    
                    Integer id = rs.getInt("ID");   
                    String product = rs.getString("PRODUCT");
                    
                    autocompleteProduct.setId(id);
                    autocompleteProduct.setProductName(product);
                    
                    list.add(autocompleteProduct);
                }
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        
        return list;
    }
    
    public void insertAutocompleteProduct(String product) {
        String sql = "INSERT INTO AUTOCOMPLETE_PRODUCTS (PRODUCT) VALUES (?)";
        
        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product);
            
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void deleteAutocompleteProduct(int id) {
        String sql = "DELETE FROM AUTOCOMPLETE_PRODUCTS WHERE ID = ?";
        
        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
}
