package org.motechproject.mobile.web.ivr.intellivr;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.motechproject.omp.manager.intellivr.AutoCreate;
import org.motechproject.omp.manager.intellivr.ErrorCodeType;
import org.motechproject.omp.manager.intellivr.ResponseType;
import org.motechproject.omp.manager.intellivr.StatusType;

public class IntellIVRServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		InputStream in = req.getInputStream();
		int length = req.getContentLength();
		byte[] buffer = new byte[length];
		int read = in.read(buffer, 0, length);
		
		String content = new String(buffer);
		
		resp.setContentType("text/xml");
		PrintWriter out = resp.getWriter();
		
		AutoCreate ac = new AutoCreate();
		ResponseType rt = new ResponseType();
		rt.setStatus(StatusType.ERROR);
		rt.setErrorCode(ErrorCodeType.MOTECH_MALFORMED_XML);
		rt.setErrorString("");
		ac.setResponse(rt);
	
		try {
			JAXBContext jac = JAXBContext.newInstance("org.motechproject.omp.manager.intellivr");
			Marshaller m = jac.createMarshaller();
			m.marshal(ac, out);
		} catch (JAXBException e) {
			e.printStackTrace();
			out.println("<test>jaxb error</test>");
		}
		
	}

}