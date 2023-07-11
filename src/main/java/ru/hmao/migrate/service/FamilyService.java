package ru.hmao.migrate.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.hmao.migrate.dao.target.TargetDzpApplicantLogRepository;
import ru.hmao.migrate.dao.target.TargetDzpApplicantPartLogRepository;
import ru.hmao.migrate.entity.target.TargetDzpApplicantLog;
import ru.hmao.migrate.entity.target.TargetDzpApplicantPartLog;
import ru.hmao.migrate.processors.FamilyProcessor;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyService {
    private AtomicBoolean isRun = new AtomicBoolean(false);

    private final FamilyProcessor familyProcessor;
    private final TargetDzpApplicantLogRepository targetDzpApplicantLogRepository;
    private final TargetDzpApplicantPartLogRepository targetDzpApplicantPartLogRepository;

    @SneakyThrows
    //@Async
    public void migrateFamily() {
        if (isRun.get()) {
            log.debug("migrate family: already running");
        } else {
            isRun.set(true);
            int count = 0;
            Instant start = Instant.now();
            log.info("start migrate family");
            for (TargetDzpApplicantLog targetDzpApplicantLog : targetDzpApplicantLogRepository.findAll()) {
                try {
                    count += familyProcessor.processFamily(targetDzpApplicantLog.getIdapplicant(), targetDzpApplicantLog.getClientId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (TargetDzpApplicantPartLog targetDzpApplicantPartLog : targetDzpApplicantPartLogRepository.findAll()) {
                try {
                    count += familyProcessor.processFamily(targetDzpApplicantPartLog.getIdapplicant(), targetDzpApplicantPartLog.getClientId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Instant end = Instant.now();
            log.info("family ended at {}. It took {}. Total count: {}",
                    LocalDateTime.now(), Duration.between(start, end), count);
        }
        isRun.set(false);
    }
}
