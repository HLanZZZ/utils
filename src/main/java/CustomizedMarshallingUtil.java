import com.cathaypacific.ng.logger.CxLogManager;
import com.cathaypacific.ng.logger.CxLogger;
import org.springframework.oxm.*;
import org.springframework.util.xml.StaxUtils;
import org.springframework.ws.WebServiceMessage;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.bind.*;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomizedMarshallingUtil {

	private static Map<String, JAXBContext> jaxbContextMap = new ConcurrentHashMap<>();
	private static CxLogger logger = CxLogManager.getLogger(CustomizedMarshallingUtil.class);

	public static JAXBContext getJAXBContext(String contextPath) throws JAXBException {
		if (jaxbContextMap.containsKey(contextPath)) {
			return jaxbContextMap.get(contextPath);
		}
		return createJAXBContext(contextPath);
	}

	public static synchronized JAXBContext createJAXBContext(String contextPath) throws JAXBException {
		if (jaxbContextMap.containsKey(contextPath)) {
			return jaxbContextMap.get(contextPath);
		}
		logger.info("Creating JAXBContext with context path [{}]", contextPath);
		JAXBContext jaxbContext = JAXBContext.newInstance(contextPath);
		jaxbContextMap.put(contextPath, jaxbContext);
		return jaxbContext;
	}

	public static Object unmarshal(WebServiceMessage message, String contexPath) throws XmlMappingException {
		Source payload = message.getPayloadSource();
		if (payload == null) {
			return null;
		}
		try {
			payload = processSource(payload);
			Unmarshaller unmarshaller = getJAXBContext(contexPath).createUnmarshaller();
			if (StaxUtils.isStaxSource(payload)) {
				return unmarshalStaxSource(unmarshaller, payload);
			}
			return unmarshaller.unmarshal(payload);
		} catch (NullPointerException ex) {
			throw ex;
		} catch (JAXBException ex) {
			throw convertJaxbException(ex);
		}
	}

	protected static Object unmarshalStaxSource(Unmarshaller jaxbUnmarshaller, Source staxSource) throws JAXBException {
		XMLStreamReader streamReader = StaxUtils.getXMLStreamReader(staxSource);
		if (streamReader != null) {
			return jaxbUnmarshaller.unmarshal(streamReader);
		} else {
			XMLEventReader eventReader = StaxUtils.getXMLEventReader(staxSource);
			if (eventReader != null) {
				return jaxbUnmarshaller.unmarshal(eventReader);
			} else {
				throw new IllegalArgumentException("StaxSource contains neither XMLStreamReader nor XMLEventReader");
			}
		}
	}

	private static XmlMappingException convertJaxbException(JAXBException ex) {
		if (ex instanceof ValidationException) {
			return new ValidationFailureException("JAXB validation exception", ex);
		} else if (ex instanceof MarshalException) {
			return new MarshallingFailureException("JAXB marshalling exception", ex);
		} else if (ex instanceof UnmarshalException) {
			return new UnmarshallingFailureException("JAXB unmarshalling exception", ex);
		} else {
			// fallback
			return new UncategorizedMappingException("Unknown JAXB exception", ex);
		}
	}

	private static Source processSource(Source source) {
		if (StaxUtils.isStaxSource(source) || source instanceof DOMSource) {
			return source;
		}

		XMLReader xmlReader = null;
		InputSource inputSource = null;

		if (source instanceof SAXSource) {
			SAXSource saxSource = (SAXSource) source;
			xmlReader = saxSource.getXMLReader();
			inputSource = saxSource.getInputSource();
		} else if (source instanceof StreamSource) {
			StreamSource streamSource = (StreamSource) source;
			if (streamSource.getInputStream() != null) {
				inputSource = new InputSource(streamSource.getInputStream());
			} else if (streamSource.getReader() != null) {
				inputSource = new InputSource(streamSource.getReader());
			} else {
				inputSource = new InputSource(streamSource.getSystemId());
			}
		}

		try {
			if (xmlReader == null) {
				xmlReader = XMLReaderFactory.createXMLReader();
			}
			xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
			xmlReader.setEntityResolver(NO_OP_ENTITY_RESOLVER);
			return new SAXSource(xmlReader, inputSource);
		} catch (SAXException ex) {
			logger.warn("Processing of external entities could not be disabled", ex);
			return source;
		}
	}

	private static final EntityResolver NO_OP_ENTITY_RESOLVER = new EntityResolver() {
		@Override
		public InputSource resolveEntity(String publicId, String systemId) {
			return new InputSource(new StringReader(""));
		}
	};
}
