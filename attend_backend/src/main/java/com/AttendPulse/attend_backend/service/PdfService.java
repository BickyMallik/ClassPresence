package com.AttendPulse.attend_backend.service;

import com.AttendPulse.attend_backend.entity.*;
import com.AttendPulse.attend_backend.repository.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    @Autowired private StudentRepository studentRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private AttendanceSessionRepository sessionRepository;
    @Autowired private AttendanceRecordRepository recordRepository;
    @Autowired private EnrollmentRepository enrollmentRepository;

    public byte[] generateAttendanceReport(Long subjectId) throws Exception {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found!"));

        List<AttendanceSession> sessions =
                sessionRepository.findBySubjectId(subjectId);
        List<Enrollment> enrollments =
                enrollmentRepository.findBySubjectId(subjectId);

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Title
        Font titleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("AttendPulse - Attendance Report\n\n",
                titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Subject Info
        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        document.add(new Paragraph("Subject: " + subject.getName(), infoFont));
        document.add(new Paragraph("Code: " + subject.getCode(), infoFont));
        document.add(new Paragraph("Semester: " + subject.getSemester(), infoFont));
        document.add(new Paragraph("Total Sessions: " + sessions.size(), infoFont));
        document.add(new Paragraph(" "));

        // Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        // Header
        Font headerFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
        String[] headers = {"Roll No", "Student Name", "Attended", "Percentage"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new BaseColor(26, 115, 232));
            cell.setPadding(8);
            table.addCell(cell);
        }

        // Data rows
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            List<AttendanceRecord> records = recordRepository
                    .findByStudentIdAndSession_Subject_Id(
                            student.getId(), subjectId);
            int attended = records.size();
            double percentage = sessions.size() == 0 ? 0 :
                    ((double) attended / sessions.size()) * 100;

            table.addCell(new PdfPCell(
                    new Phrase(student.getRollNo(), dataFont)));
            table.addCell(new PdfPCell(
                    new Phrase(student.getUser().getName(), dataFont)));
            table.addCell(new PdfPCell(
                    new Phrase(String.valueOf(attended), dataFont)));

            // Color code percentage
            PdfPCell percentCell = new PdfPCell(
                    new Phrase(String.format("%.1f%%", percentage), dataFont));
            if (percentage >= 75) {
                percentCell.setBackgroundColor(
                        new BaseColor(200, 230, 201)); // green
            } else if (percentage >= 60) {
                percentCell.setBackgroundColor(
                        new BaseColor(255, 243, 205)); // yellow
            } else {
                percentCell.setBackgroundColor(
                        new BaseColor(255, 205, 210)); // red
            }
            table.addCell(percentCell);
        }

        document.add(table);
        document.close();
        return out.toByteArray();
    }
}