package com.techtammina.plugins.imagetobase64.smartservice;

import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.framework.SmartServiceContext;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;
import com.appiancorp.suiteapi.security.external.SecureCredentialsStore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

@PaletteInfo(paletteCategory = "Document Management", palette = "Document Management")
public class ImageToBase64SmartService extends AppianSmartService {

    private final ContentService contentService;
    private final SmartServiceContext smartServiceContext;
    private final SecureCredentialsStore secureCredentialsStore;

    private Long documentId;
    private String base64Output;
    private String fileName;

    public ImageToBase64SmartService(
            ContentService contentService,
            SmartServiceContext smartServiceContext,
            SecureCredentialsStore secureCredentialsStore) {
        this.contentService = contentService;
        this.smartServiceContext = smartServiceContext;
        this.secureCredentialsStore = secureCredentialsStore;
    }

    @Override
    public void run() throws SmartServiceException {
        if (documentId == null || documentId <= 0) {
            throw new SmartServiceException.Builder(getClass(),
                new IllegalArgumentException("A valid Document ID is required.")).build();
        }
        try {
            fileName = contentService.getContent(documentId).getName();

            try (InputStream is = contentService.getDocumentInputStream(documentId);
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] chunk = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(chunk)) != -1) {
                    buffer.write(chunk, 0, bytesRead);
                }
                base64Output = Base64.getEncoder().encodeToString(buffer.toByteArray());
            }
        } catch (Exception e) {
            throw new SmartServiceException.Builder(getClass(), e).build();
        }
    }

    @Input(required = Required.ALWAYS)
    @Name("documentId")
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    @Name("base64Output")
    public String getBase64Output() { return base64Output; }

    @Name("fileName")
    public String getFileName() { return fileName; }
}
