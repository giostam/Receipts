/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.receipts.datatypes;

import com.receipts.utils.DbUtils;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import org.primefaces.context.RequestContext;

/**
 *
 * @author StamaterisG
 */
@Named(value = "user")
@SessionScoped
public class User implements Serializable {

    private boolean loggedIn = false;
    private int id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private boolean canImport;
    private boolean canExport;
    private boolean canDelete;

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public boolean isCanImport() {
        return canImport;
    }

    public void setCanImport(boolean canImport) {
        this.canImport = canImport;
    }

    public boolean isCanExport() {
        return canExport;
    }

    public void setCanExport(boolean canExport) {
        this.canExport = canExport;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public void login(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage message = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (!getUser(username, password)) {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Σφάλμα", "Λανθασμένα στοιχεία εισόδου");
            facesContext.addMessage(null, message);
        }
        context.addCallbackParam("loggedIn", loggedIn);
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect(ec.getRequestContextPath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private boolean getUser(String username, String password) {
        boolean userExists = false;

        String sql = "select id, firstname, surname, can_import, can_export, can_delete from users where username = ? and password = ?";
        try (Connection conn = DbUtils.getDbConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userExists = true;
                    id = rs.getInt("id");
                    firstName = rs.getString("firstname");
                    lastName = rs.getString("surname");
                    canImport = rs.getInt("can_import") == 1;
                    canExport = rs.getInt("can_export") == 1;
                    canDelete = rs.getInt("can_delete") == 1;
                    loggedIn = true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return userExists;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}
