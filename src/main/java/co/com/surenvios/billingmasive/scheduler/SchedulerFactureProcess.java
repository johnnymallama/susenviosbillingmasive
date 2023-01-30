package co.com.surenvios.billingmasive.scheduler;

import co.com.surenvios.librarycommon.database.entity.Acumulado;
import co.com.surenvios.librarycommon.exception.ExceptionScheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import static co.com.surenvios.billingmasive.util.LogUtil.trackInfo;
import static co.com.surenvios.billingmasive.util.LogUtil.trackError;
import static co.com.surenvios.billingmasive.util.Constants.PREFIX_NAME_THREAD_PROCESS;
import static co.com.surenvios.billingmasive.util.Constants.createNameThread;

import java.util.List;

@Component("schedulerFactureProcess")
public class SchedulerFactureProcess extends SchedulerFacture {

    @Value("${origen.data.xue}")
    private String origen;

    @Scheduled(cron = "${scheduled.billing.masive}")
    public void startProcess() {
        try {
            if (!isStartFlag()) {
                throw new ExceptionScheduled("Scheduled BillingMasive No Activo");
            }
            this.validExecutionThread(createNameThread(PREFIX_NAME_THREAD_PROCESS, this.origen));
            List<Acumulado> listDocumentProcess = this.findDocumentProcess(this.origen);
            if (listDocumentProcess.isEmpty()) {
                throw new ExceptionScheduled("No document to process");
            }
            processDocument(true, listDocumentProcess, this.origen);
        } catch (ExceptionScheduled e) {
            trackInfo("SchedulerFactureProcess.startProcess", e.getMessage());
        } catch (Exception e) {
            trackError("SchedulerFactureProcess.startProcess", e.getClass().getName(), e.getMessage(), e);
        }
    }
}
