package co.com.surenvios.billingmasive.process;

import co.com.surenvios.librarycommon.dto.internal.ResolucionInterna;
import org.apache.logging.log4j.*;
import org.springframework.stereotype.*;

import co.com.surenvios.librarycommon.database.entity.*;
import co.com.surenvios.librarycommon.database.view.Emisor;
import co.com.surenvios.librarycommon.dto.facture.helper.*;
import co.com.surenvios.librarycommon.dto.facture.request.facturaventa.*;
import co.com.surenvios.librarycommon.dto.facture.response.common.ResponseFacture;
import co.com.surenvios.librarycommon.exception.*;

@Component("processFacturaVenta")
public class ProcessFacturaVenta extends Process {

	private static final Logger logger = LogManager.getLogger(ProcessFacturaVenta.class);

	@Override
	public void process(Resolucion resolucion, NumeracionNcNd numeracionNcNd, Emisor emisor, Acumulado acumulado, String tokenFacture) {
		try {
			FacturaVentaRequest facturaVentaRequest = HelperFacturaVenta.create(resolucion, emisor, acumulado);
			ResolucionInterna resolucionInterna = this.getNumeroDocumentoFacturaVenta(resolucion);
			facturaVentaRequest.getCabecera().setNumeroFactura(resolucionInterna.numeroDocumento());
			this.updateNumberDocument(acumulado, resolucionInterna);
			String xml = this.convertXml(facturaVentaRequest);
			logger.info(xml);
			ResponseFacture facturaVentaResponse = this.sendFacturaVenta(xml, tokenFacture,
					resolucion.getLlaveTecnica());
			this.updateSent(acumulado);
			this.saveResponseFacture(facturaVentaResponse);
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
			Resolucion resolucion = this.findResolucionNumber(acumulado.getNumeroDocumento());
			Integer consecutivo = Integer.parseInt(acumulado.getNumeroDocumento().split(resolucion.getPrefijo())[1]);
			ResolucionInterna resolucionInterna = new ResolucionInterna(consecutivo, resolucion);
			FacturaVentaRequest facturaVentaRequest = HelperFacturaVenta.create(resolucion, emisor, acumulado);
			facturaVentaRequest.getCabecera().setNumeroFactura(acumulado.getNumeroDocumento());
			String xml = this.convertXml(facturaVentaRequest);
			logger.info(xml);
			ResponseFacture facturaVentaResponse = this.sendFacturaVenta(xml, tokenFacture,
					resolucion.getLlaveTecnica());
			this.updateSent(acumulado);
			this.saveResponseFacture(facturaVentaResponse);
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

}
