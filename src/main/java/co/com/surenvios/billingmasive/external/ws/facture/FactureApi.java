package co.com.surenvios.billingmasive.external.ws.facture;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import co.com.surenvios.librarycommon.dto.facture.request.login.*;
import co.com.surenvios.librarycommon.dto.facture.response.common.*;
import co.com.surenvios.librarycommon.dto.facture.response.login.*;
import co.com.surenvios.librarycommon.exception.*;

@Component("factureApi")
public class FactureApi {

	private static final String MSG_CREDENTIAL = "Error en credenciales de acceso.";

	private static final String HEAD_REQUEST_ID = "REQUEST-ID";
	private static final String HEAD_DOCUMENTTYPE = "X-REF-DOCUMENTTYPE";
	private static final String HEAD_ASYNC = "X-ASYNC";
	private static final String HEAD_KEYCONTROL = "X-KEYCONTROL";
	private static final String HEAD_AUTHORIZATION = "Authorization";
	private static final String PREFIX_AUTH = "Bearer ";

	@Value("${factureApi.protocol}")
	private String protocol;

	@Value("${factureApi.host}")
	private String host;

	@Value("${factureApi.url.login}")
	private String urlLogin;

	@Value("${factureApi.url.sendDocument}")
	private String sendDocumento;

	@Value("${factureApi.facturaVenta}")
	private String codigoFacturaVenta;

	@Value("${factureApi.notasufix}")
	private String codigoNotaSufijo;

	public LoginFactureResponse login(LoginFactureRequest loginFactureRequest) throws ExceptionLogin {
		LoginFactureResponse retorno = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			ResponseEntity<String> response = this.sendPost(this.urlLogin, headers,
					new ObjectMapper().writeValueAsString(loginFactureRequest));
			switch (response.getStatusCode()) {
			case OK:
				retorno = new Gson().fromJson(response.getBody(), LoginFactureResponse.class);
				break;
			default:
				ErrorFactureResponse error = new Gson().fromJson(response.getBody(), ErrorFactureResponse.class);
				throw new ExceptionLogin(error.getEventItems().toString());
			}
		} catch (HttpServerErrorException e) {
			ErrorFactureResponse error = new Gson().fromJson(e.getResponseBodyAsString(), ErrorFactureResponse.class);
			for (ErrorFactureResponse.EventItem eventItem : error.getEventItems()) {
				if (eventItem.getDetailDescription() != null) {
					throw new ExceptionLogin(error.getEventItems().get(0).getDetailDescription().get(0));
				} else {
					throw new ExceptionLogin(error.getEventItems().get(0).getShortDescription());
				}
			}
		} catch (ExceptionLogin e) {
			throw e;
		} catch (Exception e) {
			throw new ExceptionLogin(e);
		}
		return retorno;
	}

	public ResponseFacture sendFacturaVenta(String body, String token, String tecnicalKey) throws ExceptionSend {
		ResponseFacture retorno = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add(HEAD_REQUEST_ID, UUID.randomUUID().toString());
			headers.add(HEAD_DOCUMENTTYPE, this.codigoFacturaVenta);
			headers.add(HEAD_ASYNC, Boolean.FALSE.toString());
			headers.add(HEAD_KEYCONTROL, tecnicalKey);
			headers.add(HEAD_AUTHORIZATION, PREFIX_AUTH.concat(token));
			headers.setContentType(MediaType.APPLICATION_XML);
			ResponseEntity<String> response = this.sendPost(this.sendDocumento, headers, body);
			switch (response.getStatusCode()) {
			case OK:
				retorno = new Gson().fromJson(response.getBody(), ResponseFacture.class);
				break;
			case UNAUTHORIZED:
				throw new ExceptionSend(MSG_CREDENTIAL);
			default:
				ErrorFactureResponse error = new Gson().fromJson(response.getBody(), ErrorFactureResponse.class);
				throw new ExceptionSend(error.getEventItems().toString());
			}
		} catch (ExceptionSend e) {
			throw e;
		} catch (Exception e) {
			throw new ExceptionSend(e);
		}
		return retorno;
	}

	public ResponseFacture sendNota(String body, String token, String tipoDocumento) throws ExceptionSend {
		ResponseFacture retorno = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add(HEAD_REQUEST_ID, UUID.randomUUID().toString());
			headers.add(HEAD_DOCUMENTTYPE, tipoDocumento.concat(this.codigoNotaSufijo));
			headers.add(HEAD_ASYNC, Boolean.FALSE.toString());
			headers.add(HEAD_AUTHORIZATION, PREFIX_AUTH.concat(token));
			headers.setContentType(MediaType.APPLICATION_XML);
			ResponseEntity<String> response = this.sendPost(this.sendDocumento, headers, body);
			switch (response.getStatusCode()) {
			case OK:
				retorno = new Gson().fromJson(response.getBody(), ResponseFacture.class);
				break;
			default:
				ErrorFactureResponse error = new Gson().fromJson(response.getBody(), ErrorFactureResponse.class);
				throw new ExceptionSend(error.getEventItems().toString());
			}
		} catch (ExceptionSend e) {
			throw e;
		} catch (Exception e) {
			throw new ExceptionSend(e);
		}
		return retorno;
	}

	private ResponseEntity<String> sendPost(String resource, HttpHeaders headers, String body)
			throws ExceptionGeneral, ExceptionLogin, ExceptionSend {
		ResponseEntity<String> retorno = null;
		try {
			String urlString = this.protocol.concat("://").concat(this.host).concat("/").concat(resource);
			URI url = new URI(urlString);
			HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
			RestTemplate rest = new RestTemplate();
			retorno = rest.exchange(url, HttpMethod.POST, httpEntity, String.class);
		} catch (HttpServerErrorException | HttpClientErrorException e) {
			switch (e.getStatusCode()) {
			case UNAUTHORIZED:
				throw new ExceptionLogin(MSG_CREDENTIAL);
			default:
				this.getResponseError(e);
				break;
			}

		} catch (Exception e) {
			throw new ExceptionGeneral(e);
		}
		return retorno;
	}

	private void getResponseError(HttpStatusCodeException e) throws ExceptionSend {
		ErrorFactureResponse error = new Gson().fromJson(e.getResponseBodyAsString(), ErrorFactureResponse.class);
		for (ErrorFactureResponse.EventItem eventItem : error.getEventItems()) {
			if (eventItem.getDetailDescription() != null) {
				throw new ExceptionSend(error.getEventItems().get(0).getDetailDescription().get(0));
			} else {
				throw new ExceptionSend(error.getEventItems().get(0).getShortDescription());
			}
		}
	}

}
