package ru.hmao.migrate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hmao.migrate.service.LegalClientsService;

@RestController
@RequestMapping("migrate")
@RequiredArgsConstructor
public class MigrateControllerImpl implements MigrateController {
    private static final String STARTED = "started. running ...";
    private static final String STOPPED = "stopping ...";

    private final LegalClientsService legalClientsService;

    @Override
    @GetMapping("/migrateLegalClients")
    public ResponseEntity<String> migrateLegalClients() {
        legalClientsService.migrateLegalClients();
        return ResponseEntity.ok(STARTED);
    }

}
