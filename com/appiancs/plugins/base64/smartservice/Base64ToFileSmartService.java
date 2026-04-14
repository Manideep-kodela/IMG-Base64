package com.appiancs.plugins.base64.smartservice;

import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.content.ContentUploadOutputStream;
import com.appiancorp.suiteapi.knowledge.Document;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;
import com.appiancorp.suiteapi.process.palette.DocumentManagement;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@PaletteInfo(paletteCategory = "Document Management", palette = "Document Management")
public class Base64ToFileSmartService extends AppianSmartService {

    private final ContentService contentService;

    private String base64Input;
    private String fileName;
    private Long targetFolderId;
    private Long documentId;

    public Base64ToFileSmartService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Override
    public void run() throws SmartServiceException {
        if (base64Input == null || base64Input.trim().isEmpty()) {
            throw new SmartServiceException.Builder(getClass(), new IllegalArgumentException("Base64 string cannot be empty.")).build();
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new SmartServiceException.Builder(getClass(), new IllegalArgumentException("File name cannot be empty.")).build();
        }
        try {
            byte[] fileBytes = Base64.getDecoder().decode(base64Input.trim());

            Document doc = new Document();
            doc.setName(fileName);
            doc.setParent(targetFolderId);

            try (ContentUploadOutputStream out = contentService.uploadDocument(doc, null);
                 ByteArrayInputStream bis = new ByteArrayInputStream(fileBytes)) {
                byte[] chunk = new byte[8192];
                int bytesRead;
                while ((bytesRead = bis.read(chunk)) != -1) {
                    out.write(chunk, 0, bytesRead);
                }
            }
            documentId = doc.getId();
        } catch (Exception e) {
            throw new SmartServiceException.Builder(getClass(), e).build();
        }
    }

    @Input(required = Required.ALWAYS)
    @Name("base64Input")
    public void setBase64Input(String base64Input) { this.base64Input = base64Input; }

    @Input(required = Required.ALWAYS)
    @Name("fileName")
    public void setFileName(String fileName) { this.fileName = fileName; }

    @Input(required = Required.ALWAYS)
    @Name("targetFolderId")
    public void setTargetFolderId(Long targetFolderId) { this.targetFolderId = targetFolderId; }

    @Name("documentId")
    public Long getDocumentId() { return documentId; }
}
