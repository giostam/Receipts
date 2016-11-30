/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.receipts.datatypes;

import java.util.Comparator;

/**
 *
 * @author StamaterisG
 */
public class ReceiptComparator implements Comparator<Receipt> {
    @Override
    public int compare(Receipt receipt1, Receipt receipt2) {
        return receipt1.getId().compareTo(receipt2.getId());
    }
}