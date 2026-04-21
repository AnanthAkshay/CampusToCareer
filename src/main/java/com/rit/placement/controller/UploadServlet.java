package com.rit.placement.controller;

import com.rit.placement.dao.DocumentDAO;
import com.rit.placement.model.Document;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Upload Servlet - Handles document uploads (resume, certificates)
 * POST: Upload file and store path in database
 */
@WebServlet("/student/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class UploadServlet extends HttpServlet {

    private final DocumentDAO documentDAO = new DocumentDAO();
    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Validate session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Integer userId = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");

        // 2. Validate role (only STUDENT can upload)
        if (!"STUDENT".equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only students can upload documents");
            return;
        }

        try {
            // 3. Get document type parameter
            String docType = req.getParameter("doc_type");
            if (docType == null || docType.trim().isEmpty()) {
                session.setAttribute("errorMessage", "Document type is required");
                resp.sendRedirect(req.getContextPath() + "/student/profile");
                return;
            }

            // 4. Get uploaded file
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                session.setAttribute("errorMessage", "Please select a file to upload");
                resp.sendRedirect(req.getContextPath() + "/student/profile");
                return;
            }

            // 5. Validate file size
            if (filePart.getSize() > MAX_FILE_SIZE) {
                session.setAttribute("errorMessage", "File size exceeds 10MB limit");
                resp.sendRedirect(req.getContextPath() + "/student/profile");
                return;
            }

            // 6. Validate file type (PDF only for resume)
            String fileName = getFileName(filePart);
            String fileExtension = getFileExtension(fileName);
            
            if ("resume".equalsIgnoreCase(docType) && !fileExtension.equalsIgnoreCase("pdf")) {
                session.setAttribute("errorMessage", "Resume must be a PDF file");
                resp.sendRedirect(req.getContextPath() + "/student/profile");
                return;
            }

            // 7. Create upload directory if it doesn't exist
            String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 8. Generate unique filename
            String usn = (String) session.getAttribute("usn");
            String uniqueFileName = usn + "_" + docType + "_" + System.currentTimeMillis() + "." + fileExtension;
            String filePath = uploadPath + File.separator + uniqueFileName;

            // 9. Save file to disk
            Path targetPath = Paths.get(filePath);
            Files.copy(filePart.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 10. Store relative path in database
            String relativePath = UPLOAD_DIR + "/" + uniqueFileName;
            
            // 11. Update or insert document record
            Document document = documentDAO.getDocumentByStudentId(userId);
            if (document == null) {
                // Create new document record
                document = new Document();
                document.setStudentId(userId);
                if ("resume".equalsIgnoreCase(docType)) {
                    document.setResumePath(relativePath);
                } else if ("certificate".equalsIgnoreCase(docType)) {
                    document.setCertificatesPath(relativePath);
                }
                documentDAO.insertDocument(document);
            } else {
                // Update existing document record
                if ("resume".equalsIgnoreCase(docType)) {
                    documentDAO.updateResumePath(userId, relativePath);
                } else if ("certificate".equalsIgnoreCase(docType)) {
                    documentDAO.updateCertificatesPath(userId, relativePath);
                }
            }

            // 12. Set success message and redirect
            session.setAttribute("successMessage", "Document uploaded successfully!");
            resp.sendRedirect(req.getContextPath() + "/student/profile");

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Error uploading document: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/student/profile");
        }
    }

    /**
     * Extract filename from Part header
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String content : contentDisposition.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
