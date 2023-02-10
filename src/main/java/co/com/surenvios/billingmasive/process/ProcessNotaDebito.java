package co.com.surenvios.billingmasive.process;

import co.com.surenvios.librarycommon.database.view.DataNote;
import co.com.surenvios.librarycommon.dto.internal.ResolucionInterna;
import org.springframework.stereotype.*;

import co.com.surenvios.librarycommon.database.entity.*;
import co.com.surenvios.librarycommon.database.view.Emisor;
import co.com.surenvios.librarycommon.dto.facture.helper.*;
import co.com.surenvios.librarycommon.dto.facture.request.nota.NotaDebitoRequest;
import co.com.surenvios.librarycommon.dto.facture.response.common.ResponseFacture;
import co.com.surenvios.librarycommon.exception.*;

import static co.com.surenvios.billingmasive.util.LogUtil.trackError;
import static co.com.surenvios.billingmasive.util.LogUtil.trackInfo;

@Component("processNotaDebito")
public class ProcessNotaDebito extends Process {

    private static final String SOURCE_PROCESS = "ProcessNotaDebito.process";
    private static final String SOURCE_REPROCESS = "ProcessNotaDebito.reprocess";
    private static final String MESSAGE_ERROR = "Documento = %1s, Detail = %2s";

    @Override
    public void process(Resolucion resolucion, NumeracionNcNd numeracionNcNd, Emisor emisor, Acumulado acumulado,
                        String tokenFacture) {
        try {
            DataNote dataNote = this.findDataNoteByIdentity(acumulado.getIdFacturaVenta());
            NotaDebitoRequest notaDebitoRequest = HelperNota.createNotaDebito(emisor, acumulado, numeracionNcNd, dataNote);
            ResolucionInterna resolucionInterna = this.getNumeroDocumentoNcNd(numeracionNcNd);
            notaDebitoRequest.getCabecera().setNumeroFactura(resolucionInterna.numeroDocumento());
            this.updateNumberDocument(acumulado, resolucionInterna);
            String xml = this.convertXml(notaDebitoRequest);
            trackInfo(SOURCE_PROCESS, xml);
            ResponseFacture response = this.sendNota(xml, tokenFacture, numeracionNcNd.getPrefijo());
            this.updateSent(acumulado);
            this.saveResponseFacture(response);
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
            DataNote dataNote = this.findDataNoteByIdentity(acumulado.getIdFacturaVenta());
            NumeracionNcNd numeracionNcNd = this.findNumeracionNcNdNumber(acumulado.getNumeroDocumento(), acumulado.getOrigen());
            Integer consecutivo = Integer.parseInt(acumulado.getNumeroDocumento().split(numeracionNcNd.getPrefijo())[1]);
            ResolucionInterna resolucionInterna = new ResolucionInterna(consecutivo, numeracionNcNd);
            NotaDebitoRequest notaDebitoRequest = HelperNota.createNotaDebito(emisor, acumulado, numeracionNcNd, dataNote);
            notaDebitoRequest.getCabecera().setNumeroFactura(acumulado.getNumeroDocumento());
            String xml = this.convertXml(notaDebitoRequest);
            trackInfo(SOURCE_REPROCESS, xml);
            ResponseFacture response = this.sendNota(xml, tokenFacture, numeracionNcNd.getPrefijo());
            this.updateSent(acumulado);
            this.saveResponseFacture(response);
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
