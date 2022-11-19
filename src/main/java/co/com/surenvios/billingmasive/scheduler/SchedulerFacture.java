package co.com.surenvios.billingmasive.scheduler;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
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
    protected Emisor findEmisor() throws ExceptionGeneral {
        Emisor retorno = null;
        try {
            Iterable<Emisor> iterableEmisor = this.emisorRepository.findAll();
            Iterator<Emisor> iterator = iterableEmisor.iterator();
            while (iterator.hasNext() && retorno == null) {
                retorno = iterator.next();
            }
        } catch (Exception e) {
            throw new ExceptionGeneral("Error consultando Emisor.", e);
        }
        return retorno;
    }

    protected void validExecutionThread() throws ExceptionScheduled {
        Integer hiloEjecucion = this.ejecucionRepository.findThreadExecuting();
        if (hiloEjecucion > 0) {
            throw new ExceptionScheduled("Hilos en ejecucion");
        }
    }

    protected Resolucion findResolutionVigent() {
        return this.resolucionRepository.findResolucionActive();
    }

    protected List<NumeracionNcNd> findResolucionNcNdVigent() {
        return this.numeracionNcNdRepository.findAllActive();
    }

    protected List<Acumulado> findDocumentProcess() {
        return this.acumuladoRepository.findDocumentoProcess();
    }

    protected List<Acumulado> findDocumentoReprocess() {
        return this.acumuladoRepository.findDocumentoReprocess();
    }

    protected void processDocument(boolean typeDocument, List<Acumulado> listDocumentoProcess) throws ExceptionLogin, ExceptionGeneral {
        int sizeDocument = listDocumentoProcess.size();
        if (sizeDocument > this.maxDocumentSend) {
            for (int i = 0; i < this.countThread; i++) {
                int from = i == 0 ? 0 : i * this.maxDocumentSend;
                int to = from + this.maxDocumentSend;
                sendProcessDocument(typeDocument, listDocumentoProcess.subList(from, to));
            }
        } else {
            sendProcessDocument(typeDocument, listDocumentoProcess);
        }
    }

    private void sendProcessDocument(boolean typeDocument, List<Acumulado> subList) throws ExceptionLogin, ExceptionGeneral {
        if (typeDocument) {
            this.processDocument.run(subList, generateToken(), findResolutionVigent(), findEmisor(), findResolucionNcNdVigent());
        } else {
            this.processDocument.runReprocess(subList, generateToken(), findEmisor());
        }
    }

}
