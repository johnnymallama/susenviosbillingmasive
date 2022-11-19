package co.com.surenvios.billingmasive.service;

import co.com.surenvios.librarycommon.exception.ExceptionGeneral;

public interface IBillingMasiveService {

	public boolean start() throws ExceptionGeneral;

	public boolean stop() throws ExceptionGeneral;

	public boolean startReprocess() throws ExceptionGeneral;

	public boolean stopReprocess() throws ExceptionGeneral;

}
