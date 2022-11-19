package co.com.surenvios.billingmasive.process;

import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import co.com.surenvios.billingmasive.external.ws.facture.FactureApi;
import co.com.surenvios.billingmasive.repository.*;
import co.com.surenvios.librarycommon.database.entity.*;
import co.com.surenvios.librarycommon.database.helper.*;
import co.com.surenvios.librarycommon.database.view.*;
import co.com.surenvios.librarycommon.dto.facture.response.common.ResponseFacture;
import co.com.surenvios.librarycommon.exception.*;
import co.com.surenvios.librarycommon.util.XmlConverter;

@Component("process")
public abstract class Process {

	private static final Logger logger = LogManager.getLogger(Process.class);

	@Autowired
	private FactureApi factureApi;

	@Autowired
	private ResolucionRepository resolucionRepository;

	@Autowired
	private NumeracionNcNdRepository numeracionNcNdRepository;

	@Autowired
	private AcumuladoRepository acumuladoRepository;

	@Autowired
	private AcumuladoEstadoRepository acumuladoEstadoRepository;

	public abstract void process(Resolucion resolucion, NumeracionNcNd numeracionNcNd, Emisor emisor,
			Acumulado acumulado, String tokenFacture);

	public abstract void reprocess(Emisor emisor, Acumulado acumulado, String tokenFacture);

	protected String getNumeroDocumentoFacturaVenta(Resolucion resolucion) throws ExceptionGetNumberDocument {
		String numeroFactura = "";
		try {
			Integer proximoConsecutivo = this.resolucionRepository
					.findProximoConsecutivo(resolucion.getNumeroResolucion());
			numeroFactura = resolucion.getPrefijo().concat(String.valueOf(proximoConsecutivo));
		} catch (Exception e) {
			throw new ExceptionGetNumberDocument("Error en obtener numero consecutivo de documento Resolucion ["
					.concat(resolucion.getNumeroResolucion()).concat("]"), e);
		}
		return numeroFactura;
	}

	protected String getNumeroDocumentoNcNd(NumeracionNcNd numeracionNcNd) throws ExceptionGetNumberDocument {
		String numeroFactura = "";
		try {
			Integer proximoConsecutivo = this.numeracionNcNdRepository
					.findProximoConsecutivo(numeracionNcNd.getTipoDocumento());
			numeroFactura = numeracionNcNd.getPrefijo().concat(String.valueOf(proximoConsecutivo));
		} catch (Exception e) {
			throw new ExceptionGetNumberDocument("Error en obtener numero consecutivo de documento tipo ["
					.concat(numeracionNcNd.getTipoDocumento()).concat("]"), e);
		}
		return numeroFactura;
	}

	protected Resolucion findResolucionNumber(String numberDocument) throws ExceptionGetNumberDocument {
		Resolucion retorno = null;
		try {
			retorno = this.resolucionRepository.findResolucionNumber(numberDocument);
		} catch (Exception e) {
			throw new ExceptionGetNumberDocument(
					"Error en obtener resolucion para numero de documento [".concat(numberDocument).concat("]"), e);
		}
		return retorno;
	}

	protected NumeracionNcNd findNumeracionNcNdNumber(String numberDocument) throws ExceptionGetNumberDocument {
		NumeracionNcNd retorno = null;
		try {
			retorno = this.numeracionNcNdRepository.findNumeracionNcNdNumberDocument(numberDocument);
		} catch (Exception e) {
			throw new ExceptionGetNumberDocument(
					"Error en obtener Numeracion Nota para numero de documento [".concat(numberDocument).concat("]"),
					e);
		}
		return retorno;
	}

	protected void updateRollbackInProcessing(Acumulado acumulado) {
		try {
			acumulado.setProcesar(0);
			this.acumuladoRepository.save(acumulado);
		} catch (Exception e) {
			logger.warn(e);
		}
	}

	protected void updateNumberDocument(Acumulado acumulado, String numeroFactura, Resolucion resolucion) throws ExceptionSaveEntity {
		try {
			acumulado.setNumeroDocumento(numeroFactura);
			if (acumulado.getTipoDocumento().equals("FV")) {
				acumulado.setNumeroResolucion(resolucion.getNumeroResolucion());	
			}
			this.acumuladoRepository.save(acumulado);
		} catch (Exception e) {
			throw new ExceptionSaveEntity(
					"Error Actualizando numero documento [".concat(acumulado.getNumeroGuia()).concat("]"), e);
		}
	}

	protected String convertXml(Object object) throws ExceptionConverter {
		return XmlConverter.convertClassToString(object);
	}

	protected ResponseFacture sendFacturaVenta(String xml, String tokenFacture, String tecnicalKey)
			throws ExceptionSend {
		return this.factureApi.sendFacturaVenta(xml, tokenFacture, tecnicalKey);
	}

	protected ResponseFacture sendNota(String xml, String tokenFacture, String tipoDocumento) throws ExceptionSend {
		return this.factureApi.sendNota(xml, tokenFacture, tipoDocumento);
	}

	protected void updateErrorAcumulado(Acumulado acumulado, Throwable error) {
		try {
			acumulado.setProcesar(0);
			acumulado.setEstadoDocumento(3);
			acumulado.setDescripcionEstadoDocumento(error.getMessage());
			this.acumuladoRepository.save(acumulado);
		} catch (Exception e) {
			logger.warn(e);
		}
	}

	protected void updateSent(Acumulado acumulado) throws ExceptionSaveEntity {
		try {
			acumulado.setEstadoDocumento(1);
			this.acumuladoRepository.save(acumulado);
		} catch (Exception e) {
			throw new ExceptionSaveEntity(
					"Error Actualizando estado enviada [".concat(acumulado.getNumeroGuia()).concat("]"), e);
		}
	}

	protected void saveResponseFacture(ResponseFacture facturaVentaResponse) throws ExceptionSaveAcumuladoEstado {
		try {
			AcumuladoEstado acumuladoEstado = HelperAcumuladoEstado.createAcumuladoEstado(facturaVentaResponse);
			this.acumuladoEstadoRepository.save(acumuladoEstado);
		} catch (Exception e) {
			throw new ExceptionSaveAcumuladoEstado("Error registrando estado del documento ["
					.concat(facturaVentaResponse.getDocumentNumber()).concat("]"), e);
		}
	}

	protected void updateFinallyError(Acumulado acumulado, Throwable error) {
		try {
			acumulado.setProcesar(2);
			acumulado.setEstadoDocumento(3);
			acumulado.setDescripcionEstadoDocumento(error.getMessage());
			this.acumuladoRepository.save(acumulado);
		} catch (Exception e) {
			logger.warn(e);
		}
	}

	protected void updateFinally(Acumulado acumulado) throws ExceptionSaveEntity {
		try {
			acumulado.setProcesar(2);
			acumulado.setEstadoDocumento(2);
			this.acumuladoRepository.save(acumulado);
		} catch (Exception e) {
			throw new ExceptionSaveEntity(
					"Error Actualizando estado exitosa [".concat(acumulado.getNumeroGuia()).concat("]"), e);
		}
	}

	public void updateInProcessing(Acumulado acumulado) throws ExceptionGeneral {
		try {
			acumulado.setProcesar(1);
			this.acumuladoRepository.save(acumulado);
		} catch (Exception e) {
			throw new ExceptionGeneral(
					"Error Actualizando estado en proceso [".concat(acumulado.getNumeroGuia()).concat("]"), e);
		}
	}

}
