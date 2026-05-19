package com.rit.placement.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.rit.placement.dao.ReportDAO;
import com.rit.placement.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servlet to export placement data as PDF
 * URL: /admin/export/pdf
 */
@WebServlet("/admin/export/pdf")
public class ExportPDFServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ExportPDFServlet.class);
    
    private final ReportDAO reportDAO = new ReportDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Validate session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login to access this resource");
            return;
        }
        
        // 2. Validate role - only ADMIN and COORDINATOR can export
        User user = (User) session.getAttribute("user");
        String role = user.getRole();
        if (!"ADMIN".equals(role) && !"COORDINATOR".equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied. Only coordinators can export reports.");
            return;
        }
        
        try {
            // 3. Fetch data
            List<ReportDAO.StudentReportData> students = reportDAO.getStudentReportData();
            Map<String, Object> stats = reportDAO.getSummaryStats();
            
            // 4. Generate PDF in memory
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter pdfWriter = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDoc);
            
            // 5. Add title
            Paragraph title = new Paragraph("Placement Report")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
            document.add(title);
            
            // 6. Add subtitle with date
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
            Paragraph subtitle = new Paragraph("RIT ISE Department - Generated on " + dateStr)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(subtitle);
            
            // 7. Add summary statistics
            Paragraph summaryTitle = new Paragraph("Summary Statistics")
                .setFontSize(16)
                .setBold()
                .setMarginBottom(10);
            document.add(summaryTitle);
            
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);
            
            addSummaryRow(summaryTable, "Total Students", 
                String.valueOf(stats.getOrDefault("totalStudents", 0)));
            addSummaryRow(summaryTable, "Placed Students", 
                String.valueOf(stats.getOrDefault("placedStudents", 0)));
            addSummaryRow(summaryTable, "Total Applications", 
                String.valueOf(stats.getOrDefault("totalApplications", 0)));
            addSummaryRow(summaryTable, "Total Companies", 
                String.valueOf(stats.getOrDefault("totalCompanies", 0)));
            addSummaryRow(summaryTable, "Placement Percentage", 
                String.format("%.2f%%", stats.getOrDefault("placementPercentage", 0.0)));
            
            document.add(summaryTable);
            
            // 8. Add student data table
            Paragraph studentTitle = new Paragraph("Student Details")
                .setFontSize(16)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
            document.add(studentTitle);
            
            // Create table with 6 columns
            Table studentTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 1.5f, 1.5f, 2}))
                .useAllAvailableWidth();
            
            // Add header row
            addHeaderCell(studentTable, "Name");
            addHeaderCell(studentTable, "USN");
            addHeaderCell(studentTable, "Branch");
            addHeaderCell(studentTable, "CGPA");
            addHeaderCell(studentTable, "Apps");
            addHeaderCell(studentTable, "Status");
            
            // Add student rows
            for (ReportDAO.StudentReportData student : students) {
                studentTable.addCell(new Cell().add(new Paragraph(student.getName()).setFontSize(9)));
                studentTable.addCell(new Cell().add(new Paragraph(student.getUsn()).setFontSize(9)));
                studentTable.addCell(new Cell().add(new Paragraph(student.getBranch()).setFontSize(9)));
                studentTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", student.getCgpa())).setFontSize(9)));
                studentTable.addCell(new Cell().add(new Paragraph(String.valueOf(student.getApplications())).setFontSize(9)));
                
                // Color-code status
                Cell statusCell = new Cell().add(new Paragraph(student.getStatus()).setFontSize(9));
                if ("PLACED".equals(student.getStatus())) {
                    statusCell.setBackgroundColor(ColorConstants.GREEN).setFontColor(ColorConstants.WHITE);
                } else if ("SHORTLISTED".equals(student.getStatus())) {
                    statusCell.setBackgroundColor(ColorConstants.BLUE).setFontColor(ColorConstants.WHITE);
                } else if ("APPLIED".equals(student.getStatus())) {
                    statusCell.setBackgroundColor(ColorConstants.YELLOW);
                }
                studentTable.addCell(statusCell);
            }
            
            document.add(studentTable);
            
            // 9. Add footer
            Paragraph footer = new Paragraph("End of Report - Total Records: " + students.size())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20);
            document.add(footer);
            
            // 10. Close document
            document.close();
            
            // 11. Set response headers for PDF download
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "placement_report_" + timestamp + ".pdf";
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(baos.size());
            
            // 12. Write PDF to response
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
            
            System.out.println("PDF export successful: " + filename + " (" + students.size() + " records)");
            
        } catch (Exception e) {
            logger.error("Error exporting PDF: " + e.getMessage());
            logger.error("Exception occurred: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to generate PDF report: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to add summary row
     */
    private void addSummaryRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(11)));
        table.addCell(new Cell().add(new Paragraph(value).setFontSize(11)));
    }
    
    /**
     * Helper method to add header cell
     */
    private void addHeaderCell(Table table, String text) {
        Cell cell = new Cell()
            .add(new Paragraph(text).setBold().setFontSize(10))
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(cell);
    }
}
