/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.receipts.services;

import com.receipts.datatypes.Product;
import com.receipts.datatypes.Receipt;
import com.receipts.datatypes.Store;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author StamaterisG
 */
@ManagedBean(name = "exportService")
@ApplicationScoped
public class ExportService {

    /**
     * Creates a new instance of ExportService
     */
    public ExportService() {
    }

    public InputStream createExportFile(Store store) {
        InputStream is = null;

        String storeId = store.getStoreId();
        String storeIdStr = StringUtils.leftPad(storeId, 3, "0");

        Date storeDate = store.getStoreDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String storeDateStr = sdf.format(storeDate);

        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');

        StringBuilder sb = new StringBuilder();
        for (Receipt receipt : store.getReceipts()) {
            if (receipt.isComplete()) {
                String receiptId = receipt.getId().toString();
                String receiptTime = receipt.getReceiptTime();

                for (Product product : receipt.getProducts()) {
                    // Store Id: 3 chars
                    storeIdStr = StringUtils.substring(storeIdStr, 0, 3);
                    sb.append(storeIdStr);

                    // Product Name: 32 chars
                    String productName = StringUtils.substring(product.getProductName(), 0, 32);
                    String productNameStr = StringUtils.rightPad(productName, 32);
                    sb.append(productNameStr);
                    
                    String productPrice = new DecimalFormat("0000.00", dfs).format(product.getProductPrice());
                    String productPriceStr = StringUtils.leftPad(productPrice, 7, "0");
                    sb.append(productPriceStr);

                    // Receipt Id: 10 chars
                    String receiptIdStr = StringUtils.leftPad(StringUtils.substring(receiptId, 0, 10), 10);
                    sb.append(receiptIdStr);

                    sb.append(storeDateStr);

                    sb.append(receiptTime);

                    String productQuantity = new DecimalFormat("#0.000", dfs).format(product.getProductQuantity());
                    String productQuantityStr = StringUtils.leftPad(productQuantity, 6, "0");
                    sb.append(productQuantityStr);

                    sb.append(System.getProperty("line.separator"));
                }
            }
        }

        is = IOUtils.toInputStream(sb.toString());

        return is;
    }

}
