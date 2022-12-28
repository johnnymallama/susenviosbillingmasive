package co.com.surenvios.billingmasive.process;

import co.com.surenvios.librarycommon.dto.internal.ResolucionInterna;
import org.springframework.stereotype.*;

import co.com.surenvios.librarycommon.database.entity.*;
import co.com.surenvios.librarycommon.database.view.Emisor;
import co.com.surenvios.librarycommon.dto.facture.helper.*;
import co.com.surenvios.librarycommon.dto.facture.request.facturaventa.*;
import co.com.surenvios.librarycommon.dto.facture.response.common.ResponseFacture;
import co.com.surenvios.librarycommon.exception.*;

import static co.com.surenvios.billingmasive.util.LogUtil.trackError;
import static co.com.surenvios.billingmasive.util.LogUtil.trackInfo;

@Component("processFacturaVenta")
public class ProcessFacturaVenta extends Process {

    private static final String SOURCE_PROCESS = "ProcessFacturaVenta.process";
    private static final String SOURCE_REPROCESS = "ProcessFacturaVenta.reprocess";
    private static final String MESSAGE_ERROR = "Documento = %1s, Detail = %2s";

    @Override
    public void process(Resolucion resolucion, NumeracionNcNd numeracionNcNd, Emisor emisor, Acumulado acumulado, String tokenFacture) {
        try {
            FacturaVentaRequest facturaVentaRequest = HelperFacturaVenta.create(resolucion, emisor, acumulado);
            ResolucionInterna resolucionInterna = this.getNumeroDocumentoFacturaVenta(resolucion);
            facturaVentaRequest.getCabecera().setNumeroFactura(resolucionInterna.numeroDocumento());
            this.updateNumberDocument(acumulado, resolucionInterna);
            String xml = this.convertXml(facturaVentaRequest);
            trackInfo(SOURCE_PROCESS, xml);
            ResponseFacture facturaVentaResponse = this.sendFacturaVenta(xml, tokenFacture,
                    resolucion.getLlaveTecnica());
            this.updateSent(acumulado);
            this.saveResponseFacture(facturaVentaResponse);
            this.updateFinally(acumulado);
            this.updateEntregaDocumento(acumulado);
            this.updateT040009(acumulado, resolucionInterna);
        } catch (ExceptionSaveAcumuladoEstado e) {
            trackError(SOURCE_PROCESS, e.getClass().getName(), String.format(MESSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage()), e);
            this.updateFinallyError(acumulado, e);
        } catch (ExceptionSend e) {
            trackError(SOURCE_PROCESS, e.getClass().getName(), String.format(MESSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage()), e);
            this.updateErrorAcumulado(acumulado, e);
        } catch (ExceptionGetNumberDocument | ExceptionSaveEntity | ExceptionConverter e) {
            trackError(SOURCE_PROCESS, e.getClass().getName(), String.format(MESSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage()), e);
            this.updateRollbackInProcessing(acumulado);
        } catch (Exception e) {
            trackError(SOURCE_PROCESS, e.getClass().getName(), String.format(MESSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage()), e);
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
            trackInfo(SOURCE_REPROCESS, xml);
            ResponseFacture facturaVentaResponse = this.sendFacturaVenta(xml, tokenFacture,
                    resolucion.getLlaveTecnica());
            this.updateSent(acumulado);
            this.saveResponseFacture(facturaVentaResponse);
            this.updateFinally(acumulado);
            this.updateEntregaDocumento(acumulado);
            this.updateT040009(acumulado, resolucionInterna);
        } catch (ExceptionSaveAcumuladoEstado e) {
            trackError(SOURCE_REPROCESS, e.getClass().getName(), String.format(MESSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage()), e);
            this.updateFinallyError(acumulado, e);
        } catch (ExceptionSend e) {
            trackError(SOURCE_REPROCESS, e.getClass().getName(), String.format(MESSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage()), e);
            this.updateErrorAcumulado(acumulado, e);
        } catch (ExceptionGetNumberDocument | ExceptionSaveEntity | ExceptionConverter e) {
            trackError(SOURCE_REPROCESS, e.getClass().getName(), String.format(MESSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage()), e);
            this.updateRollbackInProcessing(acumulado);
        } catch (Exception e) {
            trackError(SOURCE_REPROCESS, e.getClass().getName(), String.format(MESSAGE_ERROR, acumulado.getNumeroDocumento(), e.getMessage()), e);
        }
    }

}
