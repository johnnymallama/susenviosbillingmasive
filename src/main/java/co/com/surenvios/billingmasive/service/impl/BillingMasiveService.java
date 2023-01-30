package co.com.surenvios.billingmasive.service.impl;

import co.com.surenvios.billingmasive.scheduler.SchedulerFactureProcess;
import co.com.surenvios.billingmasive.scheduler.SchedulerFactureReprocess;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;

import co.com.surenvios.billingmasive.service.IBillingMasiveService;
import co.com.surenvios.librarycommon.exception.*;

import static co.com.surenvios.billingmasive.util.LogUtil.trackInfo;
import static co.com.surenvios.billingmasive.util.LogUtil.trackError;

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
            trackError("BillingMasiveService.start", e.getClass().getName(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean stop() throws ExceptionGeneral {
        try {
            this.schedulerFactureProcess.setStartFlag(false);
            return true;
        } catch (Exception e) {
            trackError("BillingMasiveService.stop", e.getClass().getName(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean startReprocess() throws ExceptionGeneral {
        try {
            this.schedulerFactureReprocess.setStartReprocessFlag(true);
            return true;
        } catch (Exception e) {
            trackError("BillingMasiveService.startReprocess", e.getClass().getName(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean stopReprocess() throws ExceptionGeneral {
        try {
            this.schedulerFactureReprocess.setStartReprocessFlag(false);
            return true;
        } catch (Exception e) {
            trackError("BillingMasiveService.stopReprocess", e.getClass().getName(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String status() throws ExceptionGeneral, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        try {
            boolean statusProcess = this.schedulerFactureProcess.isStartFlag();
            boolean statusReprocess = this.schedulerFactureReprocess.isStartReprocessFlag();
            result.put("statusProcess", statusProcess);
            result.put("statusReprocess", statusReprocess);
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            trackError("BillingMasiveService.status", e.getClass().getName(), e.getMessage(), e);
            result.put("error", e.getMessage());
            return mapper.writeValueAsString(result);
        }
    }

}
