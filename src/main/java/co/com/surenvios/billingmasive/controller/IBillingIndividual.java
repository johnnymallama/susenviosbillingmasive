package co.com.surenvios.billingmasive.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import co.com.surenvios.librarycommon.ws.request.*;
import co.com.surenvios.librarycommon.ws.response.*;

@RestController
@RequestMapping(path = "/v1/individual")
public interface IBillingIndividual {

	@PostMapping(path = "/sendDocument", consumes = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<BillingDocument> sendDocument(@RequestBody SendDocument sendDocument);

}
