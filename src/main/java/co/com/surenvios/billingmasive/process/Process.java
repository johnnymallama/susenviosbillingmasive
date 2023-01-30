package co.com.surenvios.billingmasive.process;


import co.com.surenvios.billingmasive.util.Constants;
import co.com.surenvios.librarycommon.dto.internal.ResolucionInterna;
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

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Optional;

import static co.com.surenvios.billingmasive.util.LogUtil.trackError;
import static co.com.surenvios.librarycommon.enume.EstadoProcesar.NO_PROCESADO;
import static co.com.surenvios.librarycommon.enume.EstadoProcesar.EN_PROCESO;
import static co.com.surenvios.librarycommon.enume.EstadoProcesar.PROCESADO;
import static co.com.surenvios.librarycommon.enume.EstadoDocumento.ENVIADO;
import static co.com.surenvios.librarycommon.enume.EstadoDocumento.EXITOSA;
import static co.com.surenvios.librarycommon.enume.EstadoDocumento.ERROR;

@Component("process")
public abstract class Process {

    private static final String MESSSAGE_ERROR = "Documento = %1s, Detail = %2s";

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

    @Autowired
    private T0400009Repository t0400009Repository;

    public abstract void process(Resolucion resolucion, NumeracionNcNd numeracionNcNd, Emisor emisor,
                                 Acumulado acumulado, String tokenFacture);

    public abstract void reprocess(Emisor emisor, Acumulado acumulado, String tokenFacture);

    protected ResolucionInterna getNumeroDocumentoFacturaVenta(Resolucion resolucion, String origen) throws ExceptionGetNumberDocument {
        try {
            Integer proximoConsecutivo = this.resolucionRepository
                    .findProximoConsecutivo(resolucion.getNumeroResolucion(), origen);
            return new ResolucionInterna(proximoConsecutivo, resolucion);
        } catch (Exception e) {
            throw new ExceptionGetNumberDocument("Error en obtener numero consecutivo de documento Resolucion ["
                    .concat(resolucion.getNumeroResolucion()).concat("]"), e);
        }
    }

    protected ResolucionInterna getNumeroDocumentoNcNd(NumeracionNcNd numeracionNcNd) throws ExceptionGetNumberDocument {
        try {
            Integer proximoConsecutivo = this.numeracionNcNdRepository
                    .findProximoConsecutivo(numeracionNcNd.getTipoDocumento(), numeracionNcNd.getOrigen());
            return new ResolucionInterna(proximoConsecutivo, numeracionNcNd);
        } catch (Exception e) {
            throw new ExceptionGetNumberDocument("Error en obtener numero consecutivo de documento tipo ["
                    .concat(numeracionNcNd.getTipoDocumento()).concat("]"), e);
        }
    }

    protected Resolucion findResolucionNumber(String numberDocument, String origen) throws ExceptionGetNumberDocument {
        try {
            return this.resolucionRepository.findResolucionNumber(numberDocument, origen);
        } catch (Exception e) {
            throw new ExceptionGetNumberDocument(
                    "Error en obtener resolucion para numero de documento [".concat(numberDocument).concat("]"), e);
        }
    }

    protected NumeracionNcNd findNumeracionNcNdNumber(String numberDocument, String origen) throws ExceptionGetNumberDocument {
        try {
            return this.numeracionNcNdRepository.findNumeracionNcNdNumberDocument(numberDocument, origen);
        } catch (Exception e) {
            throw new ExceptionGetNumberDocument(
                    "Error en obtener Numeracion Nota para numero de documento [".concat(numberDocument).concat("]"),
                    e);
        }
    }

    protected void updateRollbackInProcessing(Acumulado acumulado) {
        try {
            acumulado.setProcesar(NO_PROCESADO.getCodigo());
            this.acumuladoRepository.save(acumulado);
        } catch (Exception e) {
            String message = String.format(MESSSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage());
            trackError("Process.updateRollbackInProcessing", e.getClass().getName(), message, e);
        }
    }

    protected void updateNumberDocument(Acumulado acumulado, ResolucionInterna resolucionInterna) throws ExceptionSaveEntity {
        try {
            acumulado.setNumeroDocumento(resolucionInterna.numeroDocumento());
            acumulado.setFechaDocumento(resolucionInterna.getFechaDocumento());
            if (acumulado.getTipoDocumento().equals(Constants.FACTURA_VENTA)) {
                acumulado.setNumeroResolucion(resolucionInterna.getResolucion().getNumeroResolucion());
            }
            this.acumuladoRepository.save(acumulado);
        } catch (Exception e) {
            throw new ExceptionSaveEntity(
                    "Error Actualizando numero documento [".concat(acumulado.getNumeroGuia()).concat("]"), e);
        }
    }

    protected void updateEntregaDocumento(Acumulado acumulado) throws ExceptionSaveEntity {
        try {
            Calendar calendarNow = Calendar.getInstance();
            acumulado.setFechaEntrega(calendarNow);
            this.acumuladoRepository.save(acumulado);
        } catch (Exception e) {
            throw new ExceptionSaveEntity(
                    "Error Actualizando Fecha Entrega [".concat(acumulado.getNumeroGuia()).concat("]"), e);
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
            acumulado.setProcesar(NO_PROCESADO.getCodigo());
            acumulado.setEstadoDocumento(ERROR.getCodigo());
            acumulado.setDescripcionEstadoDocumento(error.getMessage());
            this.acumuladoRepository.save(acumulado);
        } catch (Exception e) {
            String message = String.format(MESSSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage());
            trackError("Process.updateErrorAcumulado", e.getClass().getName(), message, e);
        }
    }

    protected void updateSent(Acumulado acumulado) throws ExceptionSaveEntity {
        try {
            acumulado.setEstadoDocumento(ENVIADO.getCodigo());
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
            acumulado.setProcesar(PROCESADO.getCodigo());
            acumulado.setEstadoDocumento(ERROR.getCodigo());
            acumulado.setDescripcionEstadoDocumento(error.getMessage());
            this.acumuladoRepository.save(acumulado);
        } catch (Exception e) {
            String message = String.format(MESSSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage());
            trackError("Process.updateFinallyError", e.getClass().getName(), message, e);
        }
    }

    protected void updateFinally(Acumulado acumulado) throws ExceptionSaveEntity {
        try {
            acumulado.setProcesar(PROCESADO.getCodigo());
            acumulado.setEstadoDocumento(EXITOSA.getCodigo());
            this.acumuladoRepository.save(acumulado);
        } catch (Exception e) {
            throw new ExceptionSaveEntity(
                    "Error Actualizando estado exitosa [".concat(acumulado.getNumeroGuia()).concat("]"), e);
        }
    }

    public void updateInProcessing(Acumulado acumulado) throws ExceptionGeneral {
        try {
            acumulado.setProcesar(EN_PROCESO.getCodigo());
            this.acumuladoRepository.save(acumulado);
        } catch (Exception e) {
            throw new ExceptionGeneral(
                    "Error Actualizando estado en proceso [".concat(acumulado.getNumeroGuia()).concat("]"), e);
        }
    }

    public void updateT040009(Acumulado acumulado, ResolucionInterna resolucionInterna) {
        try {
            Calendar calendarNow = Calendar.getInstance();
            T040009 t040009 = this.findT040009ById(acumulado.getNumeroGuia());
            t040009.setCodRegionalFExterna(1);
            t040009.setConsFExterna(new BigInteger(String.valueOf(resolucionInterna.getConsecutivo())));
            t040009.setFecFExterna(calendarNow);
            t040009.setPrefFactura(resolucionInterna.getPrefijo());
            this.t0400009Repository.save(t040009);
        } catch (Exception e) {
            String message = String.format(MESSSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage());
            trackError("Process.updateT040009", e.getClass().getName(), message, e);
        }
    }

    private T040009 findT040009ById(String numeroGuia) throws IllegalArgumentException, ExceptionGeneral {
        Optional<T040009> responseQuery = this.t0400009Repository.findById(new IdT040009("1", numeroGuia, 0));
        if (responseQuery.isPresent()) {
            return responseQuery.get();
        } else {
            throw new ExceptionGeneral("Error Find T040009 no present with Guia " + numeroGuia);
        }
    }

}
