package no.fintlabs;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;

@RestController
@RequestMapping(INTERNAL_API + "/authorization")
public class AuthorizationController {

    public AuthorizationController() {
    }

    @GetMapping()
    public ResponseEntity<?> checkAuthorization() {
        return ResponseEntity.ok("OK");
    }

}
