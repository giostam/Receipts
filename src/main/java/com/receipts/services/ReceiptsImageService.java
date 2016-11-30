/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.receipts.services;

import com.receipts.utils.DbUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author StamaterisG
 */
@ManagedBean(name = "receiptsImageService")
@ApplicationScoped
public class ReceiptsImageService {

    /**
     * Creates a new instance of ReceiptsImageService
     */
    public ReceiptsImageService() {
    }

    public ByteArrayInputStream getReceiptsImage(int id) {
        ByteArrayInputStream bais = null;

        String sql = "SELECT RECEIPT_IMG FROM RECEIPTS WHERE ID = ?";
        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Blob imgBlob = rs.getBlob("RECEIPT_IMG");
                    InputStream is = imgBlob.getBinaryStream();
                    
                    bais = new ByteArrayInputStream(IOUtils.toByteArray(is));
                }
            }
        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
        }

        return bais;
    }

}
