package co.com.surenvios.billingmasive.scheduler;

import co.com.surenvios.librarycommon.database.entity.Acumulado;
import co.com.surenvios.librarycommon.exception.ExceptionScheduled;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import static co.com.surenvios.billingmasive.util.LogUtil.trackInfo;
import static co.com.surenvios.billingmasive.util.LogUtil.trackError;

import java.util.List;

@Component("schedulerFactureProcess")
public class SchedulerFactureProcess extends SchedulerFacture {

    @Scheduled(cron = "${scheduled.billing.masive}")
    public void startProcess() {
        try {
            if (!isStartFlag()) {
                throw new ExceptionScheduled("Scheduled BillingMasive No Activo");
            }
            this.validExecutionThread("PROCESS-");
            List<Acumulado> listDocumentProcess = this.findDocumentProcess();
            if (listDocumentProcess.isEmpty()) {
                throw new ExceptionScheduled("No document to process");
            }
            processDocument(true, listDocumentProcess);
        } catch (ExceptionScheduled e) {
            trackInfo("SchedulerFactureProcess.startProcess", e.getMessage());
        } catch (Exception e) {
            trackError("SchedulerFactureProcess.startProcess", e.getClass().getName(), e.getMessage(), e);
        }
    }
}
