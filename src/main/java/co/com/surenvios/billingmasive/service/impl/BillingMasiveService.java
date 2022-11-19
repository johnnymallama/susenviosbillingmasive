package co.com.surenvios.billingmasive.service.impl;

import co.com.surenvios.billingmasive.scheduler.SchedulerFactureProcess;
import co.com.surenvios.billingmasive.scheduler.SchedulerFactureReprocess;
import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;

import co.com.surenvios.billingmasive.scheduler.SchedulerFacture;
import co.com.surenvios.billingmasive.service.IBillingMasiveService;
import co.com.surenvios.librarycommon.exception.*;

@Service("billingMasiveService")
public class BillingMasiveService implements IBillingMasiveService {

    private static final Logger logger = LogManager.getLogger(BillingMasiveService.class);

    @Autowired
    private SchedulerFactureProcess schedulerFactureProcess;

    @Autowired
    private SchedulerFactureReprocess schedulerFactureReprocess;

    @Override
    public boolean start() throws ExceptionGeneral {
        try {
            this.schedulerFactureProcess.setStartFlag(true);
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    @Override
    public boolean stop() throws ExceptionGeneral {
        try {
            this.schedulerFactureProcess.setStartFlag(false);
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    @Override
    public boolean startReprocess() throws ExceptionGeneral {
        try {
            this.schedulerFactureReprocess.setStartReprocessFlag(true);
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    @Override
    public boolean stopReprocess() throws ExceptionGeneral {
        try {
            this.schedulerFactureReprocess.setStartReprocessFlag(false);
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

}
