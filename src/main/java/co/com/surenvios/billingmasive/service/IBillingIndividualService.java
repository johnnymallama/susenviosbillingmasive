package co.com.surenvios.billingmasive.service;

import co.com.surenvios.librarycommon.exception.*;
import co.com.surenvios.librarycommon.ws.request.*;
import co.com.surenvios.librarycommon.ws.response.*;

public interface IBillingIndividualService {

	public BillingDocument sendDocument(SendDocument sendDocument) throws ExceptionGeneral;

}
