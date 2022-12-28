package co.com.surenvios.billingmasive.scheduler;

import co.com.surenvios.librarycommon.database.entity.Acumulado;
import co.com.surenvios.librarycommon.exception.ExceptionScheduled;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static co.com.surenvios.billingmasive.util.LogUtil.trackInfo;
import static co.com.surenvios.billingmasive.util.LogUtil.trackError;

import java.util.List;

@Component("schedulerFactureReprocess")
public class SchedulerFactureReprocess extends SchedulerFacture {

    @Scheduled(cron = "${scheduled.billing.reprocess}")
    public void startReporcess() {
        try {
            if (!isStartReprocessFlag()) {
                throw new ExceptionScheduled("Scheduled BillingMasive Reprocess No Activo");
            }
            this.validExecutionThread("REPROCESS");
            List<Acumulado> listDocumentoReprocess = this.findDocumentoReprocess();
            if (listDocumentoReprocess.isEmpty()) {
                throw new ExceptionScheduled("No document to re process");
            }
            processDocument(false, listDocumentoReprocess);
        } catch (ExceptionScheduled e) {
            trackInfo("SchedulerFactureReprocess.startReporcess", e.getMessage());
        } catch (Exception e) {
            trackError("SchedulerFactureReprocess.startReporcess", e.getClass().getName(), e.getMessage(), e);
        }
    }
}
