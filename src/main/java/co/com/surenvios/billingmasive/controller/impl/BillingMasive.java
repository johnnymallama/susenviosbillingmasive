package co.com.surenvios.billingmasive.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import co.com.surenvios.billingmasive.controller.IBillingMasive;
import co.com.surenvios.billingmasive.service.IBillingMasiveService;

@Component("billingMasive")
public class BillingMasive implements IBillingMasive {

	@Autowired
	private IBillingMasiveService iBillingMasiveService;

	@Override
	public ResponseEntity<String> start() {
		try {
			if (this.iBillingMasiveService.start()) {
				return new ResponseEntity<>(Boolean.TRUE.toString(), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(Boolean.FALSE.toString(), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<String> stop() {
		try {
			if (this.iBillingMasiveService.stop()) {
				return new ResponseEntity<>(Boolean.TRUE.toString(), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(Boolean.FALSE.toString(), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<String> startReprocess() {
		try {
			if (this.iBillingMasiveService.startReprocess()) {
				return new ResponseEntity<>(Boolean.TRUE.toString(), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(Boolean.FALSE.toString(), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<String> stopReprocess() {
		try {
			if (this.iBillingMasiveService.stopReprocess()) {
				return new ResponseEntity<>(Boolean.TRUE.toString(), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(Boolean.FALSE.toString(), HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
