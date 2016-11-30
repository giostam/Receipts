/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.receipts.validators;

import com.receipts.backingbeans.StoreView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.primefaces.validate.ClientValidator;

/**
 *
 * @author StamaterisG
 */
@FacesValidator("custom.totalReceiptAmountValidator")
public class TotalReceiptAmountValidator implements Validator, ClientValidator {

    private final static DecimalFormat df = new DecimalFormat("###0.00");

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        df.setParseBigDecimal(true);
        df.setDecimalFormatSymbols(symbols);
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }

        if (StoreView.isWarningAlreadyShown()) {
            return;
        }

        String valueStr = value.toString().replace(".", ",");
        try {
            BigDecimal valueToCompare = ((BigDecimal) df.parse(valueStr)).setScale(2, RoundingMode.HALF_UP);

            UIInput receiptTotalInput = (UIInput) context.getViewRoot().findComponent("productsDataTable:receiptTotal");
            if (receiptTotalInput.getValue() != null) {
                BigDecimal receiptTotal = ((BigDecimal) receiptTotalInput.getValue()).setScale(2, RoundingMode.HALF_UP);

                if (!receiptTotal.equals(valueToCompare)) {
                    StoreView.setWarningAlreadyShown(true);
                    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_WARN, "Προειδοποίηση",
                            "Διαφορά στο σύνολο της απόδειξης (" + df.format(receiptTotal) + ")" + " με το απαιτούμενο (" + df.format(value) + ")"));
                }

            }
        } catch (ParseException ex) {
            StoreView.setWarningAlreadyShown(true);
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_WARN, "Προειδοποίηση",
                    "Πρόβλημα στην μορφή της τιμής: " + valueStr));
        }
    }

    @Override
    public Map<String, Object> getMetadata() {
        return null;
    }

    @Override
    public String getValidatorId() {
        return "custom.totalReceiptAmountValidator";
    }
}
