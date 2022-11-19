package co.com.surenvios.billingmasive.controller.impl;

import org.springframework.http.*;
import org.springframework.stereotype.*;

import co.com.surenvios.billingmasive.controller.*;
import co.com.surenvios.librarycommon.ws.request.*;
import co.com.surenvios.librarycommon.ws.response.*;

@Component("billingIndividual")
public class BillingIndividual implements IBillingIndividual {

	@Override
	public ResponseEntity<BillingDocument> sendDocument(SendDocument sendDocument) {
		try {
			return new ResponseEntity<>(null, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
