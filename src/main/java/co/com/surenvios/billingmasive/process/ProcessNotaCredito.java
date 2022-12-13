package co.com.surenvios.billingmasive.process;

import co.com.surenvios.librarycommon.dto.internal.ResolucionInterna;
import org.apache.logging.log4j.*;
import org.springframework.stereotype.*;

import co.com.surenvios.librarycommon.database.entity.*;
import co.com.surenvios.librarycommon.database.view.Emisor;
import co.com.surenvios.librarycommon.dto.facture.helper.*;
import co.com.surenvios.librarycommon.dto.facture.request.nota.NotaCreditoRequest;
import co.com.surenvios.librarycommon.dto.facture.response.common.ResponseFacture;
import co.com.surenvios.librarycommon.exception.*;

@Component("processNotaCredito")
public class ProcessNotaCredito extends Process {

	private static final Logger logger = LogManager.getLogger(ProcessNotaCredito.class);

	@Override
	public void process(Resolucion resolucion, NumeracionNcNd numeracionNcNd, Emisor emisor, Acumulado acumulado,
			String tokenFacture) {
		try {
			NotaCreditoRequest notaCreditoRequest = HelperNota.createNotaCredito(emisor, acumulado, numeracionNcNd);
			ResolucionInterna resolucionInterna = this.getNumeroDocumentoNcNd(numeracionNcNd);
			notaCreditoRequest.getCabecera().setNumeroFactura(resolucionInterna.numeroDocumento());
			this.updateNumberDocument(acumulado, resolucionInterna);
			String xml = this.convertXml(notaCreditoRequest);
			logger.info(xml);
			ResponseFacture response = this.sendNota(xml, tokenFacture, numeracionNcNd.getPrefijo());
			this.updateSent(acumulado);
			this.saveResponseFacture(response);
			this.updateFinally(acumulado);
			this.updateEntregaDocumento(acumulado, resolucionInterna);
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
			NotaCreditoRequest notaCreditoRequest = HelperNota.createNotaCredito(emisor, acumulado, numeracionNcNd);
			notaCreditoRequest.getCabecera().setNumeroFactura(acumulado.getNumeroDocumento());
			String xml = this.convertXml(notaCreditoRequest);
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
