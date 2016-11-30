package com.receipts.backingbeans;

import com.receipts.datatypes.AutocompleteProduct;
import com.receipts.datatypes.Receipt;
import com.receipts.datatypes.Store;
import com.receipts.datatypes.StorePlain;
import com.receipts.services.AutocompleteService;
import com.receipts.services.ExportService;
import com.receipts.services.ProductsService;
import com.receipts.services.ReceiptsService;
import com.receipts.services.StoresService;
import com.receipts.utils.DbUtils;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.json.JSONArray;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author StamaterisG
 */
@ManagedBean(name = "storeView")
@ViewScoped
public class StoreView implements Serializable {

    private static boolean warningAlreadyShown;

    private List<StorePlain> allStores;
    private List<StorePlain> filteredStores;
    private Store selectedStore;
    private Receipt selectedReceipt;

    private String goToReceiptInput;
    private String goToReceiptType;

    private String receiptTime;
    private String receiptTotalToCompare;
    private String productDescription;
    private BigDecimal productPrice;
    private BigDecimal productQuantity;

    private List<AutocompleteProduct> autocompleteProducts;
    private String autocompleteProduct;

    private StreamedContent exportFile;

    @ManagedProperty("#{storesService}")
    private StoresService storesService;

    @ManagedProperty("#{receiptsService}")
    private ReceiptsService receiptsService;

    @ManagedProperty("#{productsService}")
    private ProductsService productsService;

    @ManagedProperty("#{exportService}")
    private ExportService exportService;

    @ManagedProperty("#{autocompleteService}")
    private AutocompleteService autocompleteService;

    @PostConstruct
    public void init() {
        allStores = storesService.loadAllStores();
        autocompleteProducts = autocompleteService.listAutocompleteProducts();
    }

    public void importStores(FileUploadEvent event) {
        int userId = (int) event.getComponent().getAttributes().get("userId");
        UploadedFile zipFile = event.getFile();

        String insertStoresSql = "insert into stores (store_id, store_date, user_id) values (?, ?, ?)";
        String selectStoresSql = "select id from stores where store_id = ? and store_date = ?";
        String insertReceiptsSql = "insert into receipts (store_fk, user_id, img_name) values (?, ?, ?)";
        try (Connection conn = DbUtils.getDbConnection()) {
            try (ZipInputStream zin = new ZipInputStream(new BufferedInputStream(zipFile.getInputstream()));
                    PreparedStatement insertStoresPs = conn.prepareStatement(insertStoresSql);
                    PreparedStatement selectStoresPs = conn.prepareStatement(selectStoresSql);
                    PreparedStatement insertReceiptsPs = conn.prepareStatement(insertReceiptsSql)) {
                ZipEntry entry;

                conn.setAutoCommit(false);

                while ((entry = zin.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    boolean isDirectory = entry.isDirectory();

                    if (isDirectory) {
                        String storeId = entryName.split("_")[0];
                        String storeDay = entryName.split("_")[1];
                        String storeMonth = entryName.split("_")[2];
                        String storeYear = entryName.split("_")[3].substring(0, entryName.split("_")[3].length() - 1);

                        LocalDate storeDate = new LocalDate(Integer.parseInt(storeYear), Integer.parseInt(storeMonth), Integer.parseInt(storeDay));

                        insertStoresPs.setString(1, storeId);
                        insertStoresPs.setDate(2, new java.sql.Date(storeDate.toDateTimeAtStartOfDay().getMillis()));
                        insertStoresPs.setInt(3, userId);

                        insertStoresPs.executeUpdate();
                    } else {
                        String storeData = entryName.split("/")[0];
                        String fileName = entryName.split("/")[1];

                        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")
                                || fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".gif")
                                || fileName.endsWith(".tiff")) {
                            String storeId = storeData.split("_")[0];
                            String storeDay = storeData.split("_")[1];
                            String storeMonth = storeData.split("_")[2];
                            String storeYear = storeData.split("_")[3];

                            LocalDate storeDate = new LocalDate(Integer.parseInt(storeYear), Integer.parseInt(storeMonth), Integer.parseInt(storeDay));

                            selectStoresPs.setInt(1, Integer.parseInt(storeId));
                            selectStoresPs.setDate(2, new java.sql.Date(storeDate.toDateTimeAtStartOfDay().getMillis()));

                            int storePK = -1;
                            try (ResultSet rs = selectStoresPs.executeQuery()) {
                                while (rs.next()) {
                                    storePK = rs.getInt("id");
                                }
                            }

//                            insertReceiptsPs.setBlob(1, zin, entry.getSize());
                            insertReceiptsPs.setInt(1, storePK);
                            insertReceiptsPs.setInt(2, userId);
                            insertReceiptsPs.setString(3, fileName);

                            insertReceiptsPs.executeUpdate();
                        }
                    }
                }

                conn.commit();

                allStores = storesService.loadAllStores();
                conn.setAutoCommit(true);
            } catch (Exception ex) {
                conn.rollback();
                conn.setAutoCommit(true);

                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteStore(ActionEvent event) {
        Integer storeId = (Integer) event.getComponent().getAttributes().get("storeId");
        storesService.deleteStore(storeId);

        allStores = storesService.loadAllStores();
    }

    public String openStoreReceipts(Integer storeId) {
        selectedStore = storesService.getStoreById(storeId);

        Receipt firstReceipt = selectedStore.getReceipts().get(0);
        selectedReceipt = firstReceipt;
        
        warningAlreadyShown = false;

        return null;
    }

    public String goToReceipt() {
        int selectedStoreId = selectedStore.getId();
        Store refreshStore = storesService.getStoreById(selectedStoreId);
        selectedStore = refreshStore;

        if (goToReceiptType != null && goToReceiptInput != null) {
            boolean found = false;
            for (Receipt receipt : selectedStore.getReceipts()) {
                switch (goToReceiptType) {
                    case "img":
                        if (receipt.getImgName().contains(goToReceiptInput)) {
                            selectedReceipt = receiptsService.getReceiptById(receipt.getId());
                            found = true;
                        }
                        break;
                    case "id":
                        if (StringUtils.isNumeric(goToReceiptInput)) {
                            Integer goToReceiptId = Integer.parseInt(goToReceiptInput);
                            if (receipt.getId().equals(goToReceiptId)) {
                                selectedReceipt = receiptsService.getReceiptById(goToReceiptId);
                                found = true;
                            }
                        }
                        break;
                }

                if (found) {
                    break;
                }
            }
        }

        goToReceiptInput = null;
        warningAlreadyShown = false;
        receiptTotalToCompare = null;

        return null;
    }

    public String previousReceipt() {
        int selectedStoreId = selectedStore.getId();
        Store refreshStore = storesService.getStoreById(selectedStoreId);
        selectedStore = refreshStore;

        int selectedReceiptId = selectedReceipt.getId();

        for (Receipt receipt : selectedStore.getReceipts()) {
            if (receipt.getId().equals(selectedReceiptId - 1)) {
                selectedReceipt = receiptsService.getReceiptById(receipt.getId());
            }
        }
        
        warningAlreadyShown = false;
        receiptTotalToCompare = null;

        return null;
    }

    public String nextReceipt() {
        int selectedStoreId = selectedStore.getId();
        Store refreshStore = storesService.getStoreById(selectedStoreId);
        selectedStore = refreshStore;

        int selectedReceiptId = selectedReceipt.getId();

        boolean hasNextReceipt = false;
        for (Receipt receipt : selectedStore.getReceipts()) {
            if (receipt.getId().equals(selectedReceiptId + 1)) {
                selectedReceipt = receiptsService.getReceiptById(receipt.getId());
                hasNextReceipt = true;
            }
        }

        if (!hasNextReceipt) {
            selectedReceipt = receiptsService.getReceiptById(selectedReceiptId);
        }
        
        warningAlreadyShown = false;
        receiptTotalToCompare = null;

        return null;
    }

    public String saveAndNextReceipt(int selectedReceiptId) {
        if (receiptTime == null) {
            return null;
        } else {
            receiptsService.setReceiptCompleteStatus(selectedReceiptId, true, receiptTime);
            allStores = storesService.loadAllStores();

            return nextReceipt();
        }
    }

    public String unsaveReceipt(int selectedReceiptId) {
        receiptsService.setReceiptCompleteStatus(selectedReceiptId, false, null);
        selectedReceipt = receiptsService.getReceiptById(selectedReceiptId);
        allStores = storesService.loadAllStores();

        return null;
    }

    public String insertProduct(int userId, int selectedReceiptId) {
        productsService.insertProduct(productDescription.trim(), productPrice, productQuantity, selectedReceiptId, userId);

        selectedReceipt = receiptsService.getReceiptById(selectedReceiptId);

        productDescription = null;
        productPrice = null;
        productQuantity = null;
        
        warningAlreadyShown = false;

        return null;
    }

    public void deleteProduct(ActionEvent event) {
        Integer productId = (Integer) event.getComponent().getAttributes().get("productId");

        productsService.deleteProductById(productId);

        selectedReceipt = receiptsService.getReceiptById(selectedReceipt.getId());
        
        warningAlreadyShown = false;
    }

    public void prepExport(ActionEvent event) {
        Integer id = (Integer) event.getComponent().getAttributes().get("storeId");

        selectedStore = storesService.getStoreById(id);
    }

    public void insertAutocompleteProduct(ActionEvent event) {
        autocompleteService.insertAutocompleteProduct(autocompleteProduct);

        autocompleteProduct = null;

        autocompleteProducts = autocompleteService.listAutocompleteProducts();
    }

    public void deleteAutocompleteProduct(ActionEvent event) {
        Integer wordId = (Integer) event.getComponent().getAttributes().get("wordId");

        autocompleteService.deleteAutocompleteProduct(wordId);

        autocompleteProducts = autocompleteService.listAutocompleteProducts();
    }

    public List<String> completeText(String query) {
        List<String> results = new ArrayList<>();
        for (AutocompleteProduct auto : autocompleteProducts) {
            if (auto.getProductName().toUpperCase().replaceAll("\\s+", "").startsWith(query.toUpperCase().replaceAll("\\s+", ""))) {
//            if (auto.getProductName().startsWith(query)) {
                results.add(auto.getProductName());
            }
        }

        return results;
    }

    public List<StorePlain> getAllStores() {
        return allStores;
    }

    public void setAllStores(List<StorePlain> allStores) {
        this.allStores = allStores;
    }

    public List<StorePlain> getFilteredStores() {
        return filteredStores;
    }

    public void setFilteredStores(List<StorePlain> filteredStores) {
        this.filteredStores = filteredStores;
    }

    public Store getSelectedStore() {
        return selectedStore;
    }

    public void setSelectedStore(Store selectedStore) {
        this.selectedStore = selectedStore;
    }

    public Receipt getSelectedReceipt() {
        return selectedReceipt;
    }

    public void setSelectedReceipt(Receipt selectedReceipt) {
        this.selectedReceipt = selectedReceipt;
    }

    public String getGoToReceiptInput() {
        return goToReceiptInput;
    }

    public void setGoToReceiptInput(String goToReceiptInput) {
        this.goToReceiptInput = goToReceiptInput;
    }

    public String getGoToReceiptType() {
        return goToReceiptType;
    }

    public void setGoToReceiptType(String goToReceiptType) {
        this.goToReceiptType = goToReceiptType;
    }

    public String getReceiptTime() {
        if (selectedReceipt != null) {
            receiptTime = selectedReceipt.getReceiptTime();
        }

        return receiptTime;
    }

    public void setReceiptTime(String receiptTime) {
        this.receiptTime = receiptTime;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public BigDecimal getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(BigDecimal productQuantity) {
        this.productQuantity = productQuantity;
    }

    public List<AutocompleteProduct> getAutocompleteProducts() {
        return autocompleteProducts;
    }

    public void setAutocompleteProducts(List<AutocompleteProduct> autocompleteProducts) {
        this.autocompleteProducts = autocompleteProducts;
    }

    public String getAutocompleteProduct() {
        return autocompleteProduct;
    }

    public void setAutocompleteProduct(String autocompleteProduct) {
        this.autocompleteProduct = autocompleteProduct;
    }

    public StreamedContent getExportFile() {
        InputStream stream = exportService.createExportFile(selectedStore);
        exportFile = new DefaultStreamedContent(stream, "text/plain", selectedStore.getStoreId() + "_export.txt");

        return exportFile;
    }

    public void setStoresService(StoresService storesService) {
        this.storesService = storesService;
    }

    public void setReceiptsService(ReceiptsService receiptsService) {
        this.receiptsService = receiptsService;
    }

    public void setProductsService(ProductsService productsService) {
        this.productsService = productsService;
    }

    public void setExportService(ExportService exportService) {
        this.exportService = exportService;
    }

    public void setAutocompleteService(AutocompleteService autocompleteService) {
        this.autocompleteService = autocompleteService;
    }

    public String getImportDates() {
        JSONArray importDates = new JSONArray();
        List<Date> importDatesList = new ArrayList<>();
        for (StorePlain store : allStores) {
            Date importDate = store.getImportDate();
            if (!importDatesList.contains(importDate)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String importDateStr = sdf.format(importDate);

                importDatesList.add(importDate);
                importDates.put(importDateStr);
            }
        }

        return importDates.toString();
    }

    private Date filterImportDate;

    public Date getFilterImportDate() {
        return filterImportDate;
    }

    public void setFilterImportDate(Date filterImportDate) {
        this.filterImportDate = filterImportDate;
    }

    public void filterDataTable() {
        allStores = storesService.loadAllStores();

        List<StorePlain> newFilteredStores = new ArrayList<>();
        if (filterImportDate != null) {
            for (StorePlain store : allStores) {
                if (store.getImportDate().equals(filterImportDate)) {
                    newFilteredStores.add(store);
                }
            }

            allStores = newFilteredStores;
        }
    }

    public static boolean isWarningAlreadyShown() {
        return warningAlreadyShown;
    }

    public static void setWarningAlreadyShown(boolean warningAlreadyShown) {
        StoreView.warningAlreadyShown = warningAlreadyShown;
    }

    public String getReceiptTotalToCompare() {
        return receiptTotalToCompare;
    }

    public void setReceiptTotalToCompare(String receiptTotalToCompare) {
        this.receiptTotalToCompare = receiptTotalToCompare;
    }
}
