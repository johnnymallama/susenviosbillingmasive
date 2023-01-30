package co.com.surenvios.billingmasive.scheduler;

import java.util.List;

import co.com.surenvios.billingmasive.util.LogUtil;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import co.com.surenvios.billingmasive.external.ws.facture.*;
import co.com.surenvios.billingmasive.process.ProcessDocument;
import co.com.surenvios.billingmasive.repository.*;
import co.com.surenvios.librarycommon.database.entity.*;
import co.com.surenvios.librarycommon.database.view.*;
import co.com.surenvios.librarycommon.dto.facture.request.login.LoginFactureRequest;
import co.com.surenvios.librarycommon.dto.facture.response.login.LoginFactureResponse;
import co.com.surenvios.librarycommon.exception.*;

@Component("schedulerFacture")
public class SchedulerFacture {

    private boolean startFlag;

    private boolean startReprocessFlag;

    @Autowired
    private FactureApi factureApi;

    @Autowired
    private ResolucionRepository resolucionRepository;

    @Autowired
    private EmisorRepository emisorRepository;

    @Autowired
    private AcumuladoRepository acumuladoRepository;

    @Autowired
    private ProcessDocument processDocument;

    @Autowired
    private NumeracionNcNdRepository numeracionNcNdRepository;

    @Autowired
    private EjecucionRepository ejecucionRepository;

    @Value("${factureApi.user}")
    private String user;

    @Value("${factureApi.pass}")
    private String password;

    @Value("${max.document.send}")
    private Integer maxDocumentSend;

    @Value("${count.thread}")
    private Integer countThread;

    public boolean isStartFlag() {
        return startFlag;
    }

    public void setStartFlag(boolean startFlag) {
        this.startFlag = startFlag;
    }

    public boolean isStartReprocessFlag() {
        return startReprocessFlag;
    }

    public void setStartReprocessFlag(boolean startReprocessFlag) {
        this.startReprocessFlag = startReprocessFlag;
    }

    @Cacheable("token")
    protected String generateToken() throws ExceptionLogin {
        LoginFactureRequest loginFactureRequest = new LoginFactureRequest(this.user, this.password);
        LoginFactureResponse loginFactureResponse = this.factureApi.login(loginFactureRequest);
        return loginFactureResponse.getAccessToken();
    }

    @Cacheable("emisor")
    protected Emisor findEmisor(String origen) throws ExceptionGeneral {
        try {
            return this.emisorRepository.findEmisorByOrigen(origen);
        } catch (Exception e) {
            throw new ExceptionGeneral("Error consultando Emisor.", e);
        }
    }

    protected void validExecutionThread(String prefixHilo) throws ExceptionScheduled {
        Integer hiloEjecucion = this.ejecucionRepository.findThreadExecuting(prefixHilo);
        if (hiloEjecucion > 0) {
            throw new ExceptionScheduled(String.format("Hilos [%s] en ejecucion [%d]", prefixHilo, hiloEjecucion));
        }
    }

    protected Resolucion findResolutionVigent(String origen) {
        return this.resolucionRepository.findResolucionActive(origen);
    }

    protected List<NumeracionNcNd> findResolucionNcNdVigent(String origen) {
        return this.numeracionNcNdRepository.findAllActiveByOrigen(origen);
    }

    protected List<Acumulado> findDocumentProcess(String origen) {
        return this.acumuladoRepository.findDocumentoProcess(origen);
    }

    protected List<Acumulado> findDocumentoReprocess(String origen) {
        return this.acumuladoRepository.findDocumentoReprocess(origen);
    }

    protected void processDocument(boolean typeDocument, List<Acumulado> listDocumentoProcess, String origen) throws ExceptionLogin, ExceptionGeneral {
        int sizeDocument = listDocumentoProcess.size();
        if (sizeDocument > this.maxDocumentSend) {
            for (int i = 0; i < this.countThread; i++) {
                int from = i == 0 ? 0 : i * this.maxDocumentSend;
                int to = from + this.maxDocumentSend;
                sendProcessDocument(typeDocument, listDocumentoProcess.subList(from, to), origen);
            }
        } else {
            sendProcessDocument(typeDocument, listDocumentoProcess, origen);
        }
    }

    private void sendProcessDocument(boolean typeDocument, List<Acumulado> subList, String origen) throws ExceptionLogin, ExceptionGeneral {
        String message = String.format("[typeDocument]= %1b, [sizeList]= %2d", typeDocument, subList.size());
        LogUtil.trackInfo("SchedulerFacture.sendProcessDocument", message);
        if (typeDocument) {
            this.processDocument.run(subList, generateToken(), findResolutionVigent(origen), findEmisor(origen), findResolucionNcNdVigent(origen));
        } else {
            this.processDocument.runReprocess(subList, generateToken(), findEmisor(origen));
        }
    }

}
