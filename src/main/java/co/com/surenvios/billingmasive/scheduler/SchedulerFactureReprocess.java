package co.com.surenvios.billingmasive.scheduler;

import co.com.surenvios.librarycommon.database.entity.Acumulado;
import co.com.surenvios.librarycommon.database.view.Emisor;
import co.com.surenvios.librarycommon.exception.ExceptionScheduled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("schedulerFactureReprocess")
public class SchedulerFactureReprocess extends SchedulerFacture {

    private static final Logger logger = LogManager.getLogger(SchedulerFactureReprocess.class);

    @Scheduled(cron = "${scheduled.billing.reprocess}")
    public void startReporcess() {
        try {
            if (!isStartReprocessFlag()) {
                throw new ExceptionScheduled("Scheduled BillingMasive Reprocess No Activo");
            }
            List<Acumulado> listDocumentoReprocess = this.findDocumentoReprocess();
            if (listDocumentoReprocess.isEmpty()) {
                throw new ExceptionScheduled("No document to re process");
            }
            processDocument(false, listDocumentoReprocess);
        } catch (ExceptionScheduled e) {
            logger.info(e.getMessage());
        } catch (Exception e) {
            logger.error("Error SchedulerFactureReprocess::startReporcess ", e);
        }
    }
}
