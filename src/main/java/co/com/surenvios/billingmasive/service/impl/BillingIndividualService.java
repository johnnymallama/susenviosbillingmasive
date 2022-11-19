package co.com.surenvios.billingmasive.service.impl;

import org.apache.logging.log4j.*;
import org.springframework.stereotype.Service;

import co.com.surenvios.billingmasive.service.IBillingIndividualService;
import co.com.surenvios.librarycommon.exception.*;
import co.com.surenvios.librarycommon.ws.request.*;
import co.com.surenvios.librarycommon.ws.response.*;

@Service("billingIndividualService")
public class BillingIndividualService implements IBillingIndividualService {

	private static final Logger logger = LogManager.getLogger(BillingIndividualService.class);

	@Override
	public BillingDocument sendDocument(SendDocument sendDocument) throws ExceptionGeneral {
		BillingDocument retorno = null;
		try {

		} catch (Exception e) {
			logger.error(e);
			throw new ExceptionGeneral(e);
		}
		return retorno;
	}

}
