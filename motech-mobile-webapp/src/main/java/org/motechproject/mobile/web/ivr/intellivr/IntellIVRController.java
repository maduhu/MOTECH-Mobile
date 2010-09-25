package org.motechproject.mobile.web.ivr.intellivr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.motechproject.mobile.omp.manager.intellivr.GetIVRConfigRequestHandler;
import org.motechproject.mobile.omp.manager.intellivr.IVRCallStatsProvider;
import org.motechproject.mobile.omp.manager.intellivr.ReportHandler;
import org.motechproject.mobile.omp.manager.intellivr.AutoCreate;
import org.motechproject.mobile.omp.manager.intellivr.ErrorCodeType;
import org.motechproject.mobile.omp.manager.intellivr.GetIVRConfigRequest;
import org.motechproject.mobile.omp.manager.intellivr.ResponseType;
import org.motechproject.mobile.omp.manager.intellivr.StatusType;

public class IntellIVRController extends AbstractController implements ResourceLoaderAware {

	private ResourceLoader resourceLoader;
	private DocumentBuilder parser;
	private Validator validator;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;
	private GetIVRConfigRequestHandler ivrConfigHandler;
	private ReportHandler reportHandler;
	private IVRCallStatsProvider ivrStatsProvider;
	
	private Log log = LogFactory.getLog(IntellIVRController.class);
	
	public void init() {		
		Resource schemaResource = resourceLoader.getResource("classpath:intellivr-in.xsd");
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		try {
			Schema schema = factory.newSchema(schemaResource.getFile());
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			validator = schema.newValidator();
		} catch (SAXException e) {
			log.error("Error initializing controller:", e);
		} catch (IOException e) {
			log.error("Error initializing controller:", e);
		} catch (ParserConfigurationException e) {
			log.error("Error initializing controller:", e);
		} catch (FactoryConfigurationError e) {
			log.error("Error initializing controller:", e);
		}
		try {
			JAXBContext jaxbc = JAXBContext.newInstance("org.motechproject.mobile.omp.manager.intellivr");
			marshaller = jaxbc.createMarshaller();
			unmarshaller = jaxbc.createUnmarshaller();
		} catch (JAXBException e) {
			log.error("Error initializing controller:", e);
		}
	}
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		if ( request.getMethod().equalsIgnoreCase(METHOD_GET) ) {
			
			ModelAndView mav = new ModelAndView("ivrstats");
			
			mav.addObject("fiveMinuteSessionCount", ivrStatsProvider.getCountIVRSessionsInLastMinutes(5));
			mav.addObject("oneHourSessionCount", ivrStatsProvider.getCountIVRCallSessionsInLastHours(1));
			mav.addObject("oneDaySessionCount", ivrStatsProvider.getCountIVRCallSessionsInLastDays(1));
			mav.addObject("allSessionCount", ivrStatsProvider.getCountIVRCallSessions());
			
			return mav;
			
		} else {

			String content = getContent(request);

			log.debug("Received: " + content);

			Object output = null;

			if ( !contentIsValid(content) ) {
				log.debug("Received invalid content");
				AutoCreate ac = new AutoCreate();
				ResponseType rt = new ResponseType();
				rt.setStatus(StatusType.ERROR);
				rt.setErrorCode(ErrorCodeType.MOTECH_MALFORMED_XML);
				rt.setErrorString("Malformed XML content");
				ac.setResponse(rt);
				output = ac;
			} else {

				try {
					Object obj = unmarshaller.unmarshal(new ByteArrayInputStream(content.getBytes()));
					if ( obj instanceof AutoCreate ) {
						AutoCreate ac = new AutoCreate();
						ac.setResponse(reportHandler.handleReport(((AutoCreate)obj).getReport()));
						log.info("Received valid call report");
						output = ac;
					}
					if ( obj instanceof GetIVRConfigRequest ) {
						AutoCreate ac = new AutoCreate();
						ac.setResponse(ivrConfigHandler.handleRequest((GetIVRConfigRequest)obj));
						log.info("Received valid ivr config request");
						output = ac;
					}
				} catch ( Exception e ) {
					log.error("Error unmarshaling content: " + content, e);
				}


			}

			if ( output == null ) {
				AutoCreate ac = new AutoCreate();
				ResponseType rt = new ResponseType();
				rt.setStatus(StatusType.ERROR);
				rt.setErrorCode(ErrorCodeType.MOTECH_UNKNOWN_ERROR);
				rt.setErrorString("An unknown error has occured");
				ac.setResponse(rt);
				output = ac;
			}

			PrintWriter out = response.getWriter();

			try {
				response.setContentType("text/xml");
				marshaller.marshal(output, out);
				ByteArrayOutputStream debugOut = new ByteArrayOutputStream();
				marshaller.marshal(output, debugOut);
				log.debug("Responded with: " + debugOut.toString());
			} catch (JAXBException e) {
				log.error("Error marshalling object: " + output.toString(), e);
			}

			return null;
			
		}

	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public GetIVRConfigRequestHandler getIvrConfigHandler() {
		return ivrConfigHandler;
	}

	public void setIvrConfigHandler(GetIVRConfigRequestHandler ivrConfigHandler) {
		this.ivrConfigHandler = ivrConfigHandler;
	}

	public ReportHandler getReportHandler() {
		return reportHandler;
	}

	public void setReportHandler(ReportHandler reportHandler) {
		this.reportHandler = reportHandler;
	}

	public IVRCallStatsProvider getIvrStatsProvider() {
		return ivrStatsProvider;
	}

	public void setIvrStatsProvider(IVRCallStatsProvider ivrStatsProvider) {
		this.ivrStatsProvider = ivrStatsProvider;
	}

	private String getContent(HttpServletRequest request) throws Exception {
		InputStream in = request.getInputStream();
		int len = 4096;
		byte[] buffer = new byte[len];
		int off = 0;
		int read = 0;
		while ( (read = in.read(buffer, off, len)) != -1) {
			off += read;
			len -= off;
		}
		return new String(buffer, 0, off);
	}
	
	private boolean contentIsValid(String content) {
		
		try {
			Document doc = parser.parse(new ByteArrayInputStream(content.getBytes()));
			validator.validate(new DOMSource(doc));
			return true;
		} catch (SAXException e) {
			log.error("",e);
		} catch (IOException e) {
			log.error("",e);			
		}
		
		return false;
	}
	
}
