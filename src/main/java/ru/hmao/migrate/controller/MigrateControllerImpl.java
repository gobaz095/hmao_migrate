package ru.hmao.migrate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.hmao.migrate.enums.ApplicantsType;
import ru.hmao.migrate.enums.ClientType;
import ru.hmao.migrate.service.ApplicantsService;
import ru.hmao.migrate.service.ClientsService;
import ru.hmao.migrate.service.ContractsService;
import ru.hmao.migrate.service.FamilyService;
import ru.hmao.migrate.service.PlacementsService;

@RestController
@RequestMapping("migrate")
@RequiredArgsConstructor
public class MigrateControllerImpl implements MigrateController {
    private static final String STARTED = "started. running ...";
    private static final String STOPPED = "stopping ...";

    private final ClientsService clientsService;
    private final ApplicantsService applicantsService;
    private final FamilyService familyService;
    private final PlacementsService placementsService;
    private final ContractsService contractsService;

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
    public ResponseEntity<String> migrateApplicants(ApplicantsType applicantsType) {
            applicantsService.migrateApplicants(applicantsType);
        return ResponseEntity.ok(STARTED);
    }

    @Override
    @GetMapping("/migrateFamily")
    public ResponseEntity<String> migrateFamily() {
        familyService.migrateFamily();
        return ResponseEntity.ok(STARTED);
    }

    @Override
    @GetMapping("/migratePlacements")
    public ResponseEntity<String> migratePlacements() {
        placementsService.migratePlacements();
        return ResponseEntity.ok(STARTED);
    }
//
//    @Override
//    @GetMapping("/migrateContracts")
//    public ResponseEntity<String> migrateContracts() {
//        contractsService.migrateContracts();
//        return ResponseEntity.ok(STARTED);
//    }

    @Override
    @GetMapping("/migrateAll")
    public ResponseEntity<String> migrateAll() {
        clientsService.migrateLegalClients(ClientType.LEGAL);
        clientsService.migrateLegalClients(ClientType.INDIVIDUAL);
        applicantsService.migrateApplicants(ApplicantsType.RECIPIENT);
        applicantsService.migrateApplicants(ApplicantsType.PARTICIPANT);
        placementsService.migratePlacements();
        familyService.migrateFamily();
        return ResponseEntity.ok(STARTED);
    }

    @Override
    @GetMapping("/renameLegal")
    public ResponseEntity<String> renameLegal() {
        clientsService.renameLegal();
        return ResponseEntity.ok(STARTED);
    }

}
