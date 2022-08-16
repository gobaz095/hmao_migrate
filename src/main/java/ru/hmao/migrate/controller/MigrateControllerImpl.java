package ru.hmao.migrate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hmao.migrate.enums.ClientType;
import ru.hmao.migrate.service.ApplicantsService;
import ru.hmao.migrate.service.ClientsService;

@RestController
@RequestMapping("migrate")
@RequiredArgsConstructor
public class MigrateControllerImpl implements MigrateController {
    private static final String STARTED = "started. running ...";
    private static final String STOPPED = "stopping ...";

    private final ClientsService clientsService;
    private final ApplicantsService applicantsService;

    @Override
    @GetMapping("/migrateClients")
    public ResponseEntity<String> migrateClients(ClientType clientType) {
        if (clientType != null) {
            clientsService.migrateLegalClients(clientType);
        }
        return ResponseEntity.ok(STARTED);
    }

    @Override
    @GetMapping("/migrateApplicants")
    public ResponseEntity<String> migrateApplicants() {
            applicantsService.migrateApplicants();
        return ResponseEntity.ok(STARTED);
    }

}
