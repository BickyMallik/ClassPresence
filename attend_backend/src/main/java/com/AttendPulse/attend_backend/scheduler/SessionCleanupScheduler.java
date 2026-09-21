package com.AttendPulse.attend_backend.scheduler;

import com.AttendPulse.attend_backend.entity.AttendanceSession;
import com.AttendPulse.attend_backend.entity.Subject;
import com.AttendPulse.attend_backend.repository.AttendanceRecordRepository;
import com.AttendPulse.attend_backend.repository.AttendanceSessionRepository;
import com.AttendPulse.attend_backend.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class SessionCleanupScheduler {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private AttendanceSessionRepository sessionRepository;

    @Autowired
    private AttendanceRecordRepository recordRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredSessions(){
        System.out.println("Running semester cleanup job...");

        List<Subject> allSujects = subjectRepository.findAll();

        for (Subject subject : allSujects){
            if (subject.getSessionLabel() != null && subject.getSessionLabel().endsWith("_EXPIRED")){
                List<AttendanceSession> sessions = sessionRepository.findBySubjectId(subject.getId());

                for (AttendanceSession session : sessions){
                    recordRepository.deleteAll(recordRepository.findBySessionId(session.getId()));
                }

                sessionRepository.deleteAll(sessions);

                System.out.println("Cleaned up subject : "+ subject.getName());
            }
        }
        System.out.println("Semester cleanup job completed.");
    }
}
