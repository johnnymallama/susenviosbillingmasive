package co.com.surenvios.billingmasive.scheduler;

import co.com.surenvios.billingmasive.external.ws.facture.FactureApi;
import co.com.surenvios.billingmasive.process.ProcessDocument;
import co.com.surenvios.billingmasive.repository.*;
import co.com.surenvios.librarycommon.database.entity.Acumulado;
import co.com.surenvios.librarycommon.database.entity.NumeracionNcNd;
import co.com.surenvios.librarycommon.database.entity.Resolucion;
import co.com.surenvios.librarycommon.database.view.Emisor;
import co.com.surenvios.librarycommon.dto.facture.request.login.LoginFactureRequest;
import co.com.surenvios.librarycommon.dto.facture.response.login.LoginFactureResponse;
import co.com.surenvios.librarycommon.exception.ExceptionGeneral;
import co.com.surenvios.librarycommon.exception.ExceptionLogin;
import co.com.surenvios.librarycommon.exception.ExceptionScheduled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component("schedulerFactureProcess")
public class SchedulerFactureProcess extends SchedulerFacture {

    private static final Logger logger = LogManager.getLogger(SchedulerFactureProcess.class);

    @Scheduled(cron = "${scheduled.billing.masive}")
    public void startProcess() {
        try {
            if (!isStartFlag()) {
                throw new ExceptionScheduled("Scheduled BillingMasive No Activo");
            }
            this.validExecutionThread();
            List<Acumulado> listDocumentoProcess = this.findDocumentProcess();
            if (listDocumentoProcess.isEmpty()) {
                throw new ExceptionScheduled("No document to process");
            }
            processDocument(true, listDocumentoProcess);
        } catch (ExceptionScheduled e) {
            logger.info(e.getMessage());
        } catch (Exception e) {
            logger.error("Error SchedulerFactureProcess::startProcess ", e);
        }
    }
}
