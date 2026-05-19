package com.rit.placement.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rit.placement.dao.ReportDAO;
import com.rit.placement.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servlet to export placement data as CSV
 * URL: /admin/export/csv
 */
@WebServlet("/admin/export/csv")
public class ExportCSVServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ExportCSVServlet.class);
    
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
            // 3. Fetch student data
            List<ReportDAO.StudentReportData> students = reportDAO.getStudentReportData();
            
            // 4. Set response headers for CSV download
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "placement_report_" + timestamp + ".csv";
            
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            
            // 5. Write CSV data
            PrintWriter writer = response.getWriter();
            
            // Create CSV printer with headers
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("Name", "USN", "Branch", "CGPA", "Applications", "Status"));
            
            // Write student records
            for (ReportDAO.StudentReportData student : students) {
                csvPrinter.printRecord(
                    student.getName(),
                    student.getUsn(),
                    student.getBranch(),
                    String.format("%.2f", student.getCgpa()),
                    student.getApplications(),
                    student.getStatus()
                );
            }
            
            csvPrinter.flush();
            csvPrinter.close();
            
            System.out.println("CSV export successful: " + filename + " (" + students.size() + " records)");
            
        } catch (Exception e) {
            logger.error("Error exporting CSV: " + e.getMessage());
            logger.error("Exception occurred: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Failed to generate CSV report: " + e.getMessage());
        }
    }
}
