package co.com.surenvios.billingmasive.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/masive")
public interface IBillingMasive {

	@GetMapping(path = "/start")
	public ResponseEntity<String> start();

	@GetMapping(path = "/stop")
	public ResponseEntity<String> stop();

	@GetMapping(path = "/startReprocess")
	public ResponseEntity<String> startReprocess();

	@GetMapping(path = "/stopReprocess")
	public ResponseEntity<String> stopReprocess();

	@GetMapping(path = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> statusProcess();

}
