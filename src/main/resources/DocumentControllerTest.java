package java.com.documentos.document_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.documentos.document_service.dto.InvoiceDocumentRequest;
import com.documentos.document_service.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.ServletException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
public class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DocumentService documentService;

    @Test
    void generarFactura_datosValidos_devuelve200OOk() throws Exception {
        InvoiceDocumentRequest request = new InvoiceDocumentRequest();
        byte[] pdfMock = "PDF-BYTE-STREAM-MOCK".getBytes();

        when(documentService.generateInvoice(any(InvoiceDocumentRequest.class))).thenReturn(pdfMock);

        mockMvc.perform(post("/documents/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void generarFactura_datosInvalidos_devuelveExcepcionDeNegocio() throws Exception {
        InvoiceDocumentRequest request = new InvoiceDocumentRequest();

        when(documentService.generateInvoice(any(InvoiceDocumentRequest.class)))
                .thenThrow(new IllegalArgumentException("Datos de factura inválidos o incompletos"));

        ServletException exception = assertThrows(ServletException.class, () -> {
            mockMvc.perform(post("/documents/invoice")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });

        assertThat(exception.getCause()).isInstanceOf(IllegalArgumentException.class);
        assertThat(exception.getCause().getMessage()).contains("Datos de factura inválidos o incompletos");
    }
}
