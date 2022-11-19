package co.com.surenvios.billingmasive.process;

import org.apache.logging.log4j.*;
import org.springframework.stereotype.*;

import co.com.surenvios.librarycommon.database.entity.*;
import co.com.surenvios.librarycommon.database.view.Emisor;
import co.com.surenvios.librarycommon.dto.facture.helper.*;
import co.com.surenvios.librarycommon.dto.facture.request.nota.NotaDebitoRequest;
import co.com.surenvios.librarycommon.dto.facture.response.common.ResponseFacture;
import co.com.surenvios.librarycommon.exception.*;

@Component("processNotaDebito")
public class ProcessNotaDebito extends Process {

	private static final Logger logger = LogManager.getLogger(ProcessNotaDebito.class);

	@Override
	public void process(Resolucion resolucion, NumeracionNcNd numeracionNcNd, Emisor emisor, Acumulado acumulado,
			String tokenFacture) {
		try {
			NotaDebitoRequest notaDebitoRequest = HelperNota.createNotaDebito(emisor, acumulado, numeracionNcNd);
			String numeroFactura = this.getNumeroDocumentoNcNd(numeracionNcNd);
			notaDebitoRequest.getCabecera().setNumeroFactura(numeroFactura);
			this.updateNumberDocument(acumulado, numeroFactura, resolucion);
			String xml = this.convertXml(notaDebitoRequest);
			logger.info(xml);
			ResponseFacture response = this.sendNota(xml, tokenFacture, numeracionNcNd.getPrefijo());
			this.updateSent(acumulado);
			this.saveResponseFacture(response);
			this.updateFinally(acumulado);
		} catch (ExceptionSaveAcumuladoEstado e) {
			logger.error(e);
			this.updateFinallyError(acumulado, e);
		} catch (ExceptionSend e) {
			logger.error(e);
			this.updateErrorAcumulado(acumulado, e);
		} catch (ExceptionGetNumberDocument | ExceptionSaveEntity | ExceptionConverter e) {
			logger.error(e);
			this.updateRollbackInProcessing(acumulado);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public void reprocess(Emisor emisor, Acumulado acumulado, String tokenFacture) {
		try {
			NumeracionNcNd numeracionNcNd = this.findNumeracionNcNdNumber(acumulado.getNumeroDocumento());
			NotaDebitoRequest notaDebitoRequest = HelperNota.createNotaDebito(emisor, acumulado, numeracionNcNd);
			notaDebitoRequest.getCabecera().setNumeroFactura(acumulado.getNumeroDocumento());
			String xml = this.convertXml(notaDebitoRequest);
			logger.info(xml);
			ResponseFacture response = this.sendNota(xml, tokenFacture, numeracionNcNd.getPrefijo());
			this.updateSent(acumulado);
			this.saveResponseFacture(response);
			this.updateFinally(acumulado);
		} catch (ExceptionSaveAcumuladoEstado e) {
			logger.error(e);
			this.updateFinallyError(acumulado, e);
		} catch (ExceptionSend e) {
			logger.error(e);
			this.updateErrorAcumulado(acumulado, e);
		} catch (ExceptionGetNumberDocument | ExceptionSaveEntity | ExceptionConverter e) {
			logger.error(e);
			this.updateRollbackInProcessing(acumulado);
		} catch (Exception e) {
			logger.error(e);
		}
	}

}
