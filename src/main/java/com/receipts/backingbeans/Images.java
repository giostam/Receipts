/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.receipts.backingbeans;

import com.receipts.services.ReceiptsImageService;
import java.io.IOException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author StamaterisG
 */
@ManagedBean(name = "images")
@RequestScoped
public class Images {

    /**
     * Creates a new instance of Images
     */
    public Images() {
    }

    @ManagedProperty("#{receiptsImageService}")
    private ReceiptsImageService service;

    public StreamedContent getImage() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();

        String id = context.getExternalContext().getRequestParameterMap().get("id");
        
        if (id == null || id.isEmpty() || context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        } else {
            // So, browser is requesting the image. Return a real StreamedContent with the image bytes.
            return new DefaultStreamedContent(service.getReceiptsImage(Integer.parseInt(id)));
        }
    }
    
    public void setService(ReceiptsImageService service) {
        this.service = service;
    }

}
