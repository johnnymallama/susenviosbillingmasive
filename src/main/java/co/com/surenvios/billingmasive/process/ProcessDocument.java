package co.com.surenvios.billingmasive.process;

import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.*;

import co.com.surenvios.billingmasive.repository.EjecucionRepository;
import co.com.surenvios.librarycommon.database.entity.*;
import co.com.surenvios.librarycommon.database.helper.HelperEjecucion;
import co.com.surenvios.librarycommon.database.view.*;
import co.com.surenvios.librarycommon.exception.*;

import static co.com.surenvios.billingmasive.util.LogUtil.trackError;


@Component
public class ProcessDocument {

    @Autowired
    private ProcessFacturaVenta processFacturaVenta;

    @Autowired
    private ProcessNotaCredito processNotaCredito;

    @Autowired
    private ProcessNotaDebito processNotaDebito;

    @Autowired
    private EjecucionRepository ejecucionRepository;

    @Async("threadTaskExecutorProcess")
    public void run(List<Acumulado> listAcumulado, String tokenFacture, Resolucion resolucion, Emisor emisor,
                    List<NumeracionNcNd> listNumeracionNcNd) {
        Ejecucion ejecucion = this.startExecution(listAcumulado.size());
        if (ejecucion != null) {
            for (Acumulado acumulado : listAcumulado) {
                this.processRun(acumulado, tokenFacture, resolucion, emisor, listNumeracionNcNd);
            }
            this.endExecution(ejecucion);
        }
    }

    private synchronized void processRun(Acumulado acumulado, String tokenFacture, Resolucion resolucion, Emisor emisor,
                                         List<NumeracionNcNd> listNumeracionNcNd) {
        try {
            switch (acumulado.getTipoDocumento()) {
                case "FV":
                    this.processFacturaVenta.updateInProcessing(acumulado);
                    this.processFacturaVenta.process(resolucion, null, emisor, acumulado, tokenFacture);
                    break;
                case "CV":
                    NumeracionNcNd numeracionNc = null;
                    Optional<NumeracionNcNd> optionalNc = listNumeracionNcNd.stream()
                            .filter(numeracion -> numeracion.getTipoDocumento().equals("CV")).findFirst();
                    if (optionalNc.isPresent()) {
                        numeracionNc = optionalNc.get();
                    }
                    this.processNotaCredito.updateInProcessing(acumulado);
                    this.processNotaCredito.process(resolucion, numeracionNc, emisor, acumulado, tokenFacture);
                    break;
                case "DV":
                    NumeracionNcNd numeracionNd = null;
                    Optional<NumeracionNcNd> optionalNd = listNumeracionNcNd.stream()
                            .filter(numeracion -> numeracion.getTipoDocumento().equals("DV")).findFirst();
                    if (optionalNd.isPresent()) {
                        numeracionNd = optionalNd.get();
                    }
                    this.processNotaDebito.updateInProcessing(acumulado);
                    this.processNotaDebito.process(resolucion, numeracionNd, emisor, acumulado, tokenFacture);
                    break;
                default:
                    throw new ExceptionGeneral(
                            "Tipo de documento no valido [".concat(acumulado.getTipoDocumento()).concat("]"));
            }
        } catch (Exception e) {
            trackError("ProcessDocument.processRun", e.getClass().getName(), e.getMessage(), e);
        }
    }

    @Async("threadTaskExecutorReprocess")
    public void runReprocess(List<Acumulado> listAcumulado, String tokenFacture, Emisor emisor) {
        Ejecucion ejecucion = this.startExecution(listAcumulado.size());
        if (ejecucion != null) {
            this.reprocessRun(listAcumulado, tokenFacture, emisor);
            this.endExecution(ejecucion);
        }
    }

    private void reprocessRun(List<Acumulado> listAcumulado, String tokenFacture, Emisor emisor) {
        for (Acumulado acumulado : listAcumulado) {
            try {
                switch (acumulado.getTipoDocumento()) {
                    case "FV":
                        this.processFacturaVenta.updateInProcessing(acumulado);
                        this.processFacturaVenta.reprocess(emisor, acumulado, tokenFacture);
                        break;
                    case "CV":
                        this.processNotaCredito.reprocess(emisor, acumulado, tokenFacture);
                        break;
                    case "DV":
                        this.processNotaDebito.updateInProcessing(acumulado);
                        this.processNotaDebito.reprocess(emisor, acumulado, tokenFacture);
                        break;
                    default:
                        throw new ExceptionGeneral(
                                "Tipo de documento no valido [".concat(acumulado.getTipoDocumento()).concat("]"));
                }
            } catch (Exception e) {
                trackError("ProcessDocument.reprocessRun", e.getClass().getName(), e.getMessage(), e);
            }
        }
    }

    private Ejecucion startExecution(int totalRegister) {
        Ejecucion ejecucion = null;
        try {
            ejecucion = this.ejecucionRepository
                    .save(HelperEjecucion.create(totalRegister, Thread.currentThread().getName()));
        } catch (Exception e) {
            trackError("ProcessDocument.startExecution", e.getClass().getName(), e.getMessage(), e);
        }
        return ejecucion;
    }

    private void endExecution(Ejecucion ejecucion) {
        try {
            ejecucion.setFechaFin();
            this.ejecucionRepository.save(ejecucion);
        } catch (Exception e) {
            trackError("ProcessDocument.endExecution", e.getClass().getName(), e.getMessage(), e);
        }
    }

}
