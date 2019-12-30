import com.cathaypacific.Common.ApplicationException;
import com.cathaypacific.Common.ErrorResponseException;
import com.cathaypacific.Config.AmadeusServiceConfig;
import com.cathaypacific.Config.CpstConfig;
import com.cathaypacific.Entity.AmadeusSecurityInfo;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;


public class SoapHeaderUtil {
	  public static final String TRANSACTION_CODE_START = "Start";
	  public static final String TRANSACTION_CODE_INSERIES = "InSeries";
	  public static final String TRANSACTION_CODE_END = "End";

	  private SoapHeaderUtil() {}

//Start
	  public static void generatePNRSoapHeader(SaajSoapMessage soapMessage, AmadeusServiceConfig amadeusServiceConfig,String officeID, boolean stateLess) throws ApplicationException {
	    try {
	      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
	      soapEnvelope.addNamespaceDeclaration("wsa", "http://www.w3.org/2005/08/addressing");
	      soapEnvelope.addNamespaceDeclaration("ses", "http://xml.amadeus.com/2010/06/Session_v3");
	      soapEnvelope.addNamespaceDeclaration("sec", "http://xml.amadeus.com/2010/06/Security_v1");
	      soapEnvelope.addNamespaceDeclaration("typ", "http://xml.amadeus.com/2010/06/Types_v1");
	      soapEnvelope.addNamespaceDeclaration("iat", "http://www.iata.org/IATA/2007/00/IATA2010.1");
	      soapEnvelope.addNamespaceDeclaration("wsse",
	          "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
	      soapEnvelope.addNamespaceDeclaration("wsu",
	          "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
	      SOAPHeader headerElement = soapEnvelope.getHeader();

	      // AMA_SecurityHostedUser
	      SOAPElement securityHostedUserElement = headerElement.addChildElement("AMA_SecurityHostedUser", "sec");
	      SOAPElement userIDElement = securityHostedUserElement.addChildElement("UserID", "sec");
	      userIDElement.setAttribute("POS_Type", amadeusServiceConfig.getPosType());
	      userIDElement.setAttribute("RequestorType", amadeusServiceConfig.getRequestorType());
	      userIDElement.setAttribute("AgentDutyCode", amadeusServiceConfig.getAgentDutyCode());
	      userIDElement.setAttribute("PseudoCityCode", officeID);    
	      SOAPElement requestorIDElement = userIDElement.addChildElement("RequestorID", "typ");
	      requestorIDElement.addChildElement("CompanyName", "iat").setValue(amadeusServiceConfig.getCompanyName());

	      // -------Security Element
	      // get Security Information
	      AmadeusSecurityInfo securityInfo = SecurityUtil.generateSecurityInfo(amadeusServiceConfig.getWssPassword());

	      SOAPElement securityElement = soapEnvelope.getHeader().addChildElement("Security", "wsse");
	      SOAPElement usernameTokenElement = securityElement.addChildElement("UsernameToken", "wsse");

	      // Username
	      usernameTokenElement.addChildElement("Username", "wsse").setValue(amadeusServiceConfig.getWssUsername());

	      // password
	      SOAPElement passwordElement = usernameTokenElement.addChildElement("Password", "wsse");
	      passwordElement.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");
	      passwordElement.setValue(securityInfo.getPasswdDigest());

	      // Nonce
	      SOAPElement nonceElement = usernameTokenElement.addChildElement("Nonce", "wsse");
	      nonceElement.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
	      nonceElement.setValue(securityInfo.getNonce());
	      // Created
	      usernameTokenElement.addChildElement("Created", "wsu").setValue(securityInfo.getCreated());
	      if(!stateLess)
	      {
	      // Start Session Elements
	        SOAPElement sessionElement = soapEnvelope.getHeader().addChildElement("Session", "ses");
	        sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_START);
	      }

	    } catch (Exception e) {
	      throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
	    }
	  }
	  
	//end PNR inserious
	  public static void generatePNRSoapHeader(SaajSoapMessage soapMessage, AmadeusServiceConfig amadeusServiceConfig,String sessionID, String securityToken, int sequence) throws ErrorResponseException, ApplicationException {
	    try {
	      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
	      soapEnvelope.addNamespaceDeclaration("awsec", "http://xml.amadeus.com/2010/06/Session_v3");
	      SOAPHeader headerElement = soapEnvelope.getHeader();
	      // AMA_SecurityHostedUser
	      SOAPElement sessionElement = headerElement.addChildElement("Session", "awsec");
	      sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_END);
	      sessionElement.addChildElement("SessionId", "awsec").setValue(sessionID);
	      sessionElement.addChildElement("SequenceNumber", "awsec").setValue(String.valueOf(sequence));
	      sessionElement.addChildElement("SecurityToken", "awsec").setValue(securityToken);
	    } catch (Exception e) {
	    	throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
	    }
	  }
	
	  
/**
 * FETCH PRODUCT: TransactionStatusCode:End<br>
 * BOOK ORDER: TransactionStatusCode:InSeries
 * 
 * @param soapMessage
 * @param amadeusServiceConfig
 * @param sessionID
 * @param securityToken
 * @param end
 * @throws ErrorResponseException
 * @throws ApplicationException
 */
		  public static void generateCatelogSoapHeader(SaajSoapMessage soapMessage, AmadeusServiceConfig amadeusServiceConfig,String sessionID, String securityToken, boolean end) throws ErrorResponseException, ApplicationException {
		    try {
		      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
		      soapEnvelope.addNamespaceDeclaration("awsec", "http://xml.amadeus.com/2010/06/Session_v3");
		      SOAPHeader headerElement = soapEnvelope.getHeader();
		      // AMA_SecurityHostedUser
		      SOAPElement sessionElement = headerElement.addChildElement("Session", "awsec");
		      if (end) {
		    	  sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_END);		    	  
		      } else {
		    	  sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_INSERIES);
		      }
		      sessionElement.addChildElement("SessionId", "awsec").setValue(sessionID);
		      sessionElement.addChildElement("SequenceNumber", "awsec").setValue("2");
		      sessionElement.addChildElement("SecurityToken", "awsec").setValue(securityToken);
		    } catch (Exception e) {
		    	throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
		    }
		  }
		  
//end
			  public static void generatePricingSoapHeader(SaajSoapMessage soapMessage, AmadeusServiceConfig amadeusServiceConfig,String sessionID, String securityToken, int sequence,boolean end) throws ErrorResponseException, ApplicationException {
			    try {
			      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
			      soapEnvelope.addNamespaceDeclaration("awsec", "http://xml.amadeus.com/2010/06/Session_v3");
			      SOAPHeader headerElement = soapEnvelope.getHeader();
			      // AMA_SecurityHostedUser
			      SOAPElement sessionElement = headerElement.addChildElement("Session", "awsec");
			      if(end)
			      sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_END);
			      else
			    	  sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_INSERIES);
			      sessionElement.addChildElement("SessionId", "awsec").setValue(sessionID);
			      sessionElement.addChildElement("SequenceNumber", "awsec").setValue(String.valueOf(sequence));
			      sessionElement.addChildElement("SecurityToken", "awsec").setValue(securityToken);
			    } catch (Exception e) {
			      throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
			    }
			  }
			  // continue
			  public static void generatePnrCancelSoapHeader(SaajSoapMessage soapMessage, AmadeusServiceConfig amadeusServiceConfig,String sessionID, String securityToken) throws ErrorResponseException, ApplicationException {
				    try {
				    	
				      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
				      soapEnvelope.addNamespaceDeclaration("awsec", "http://xml.amadeus.com/2010/06/Session_v3");
				      SOAPHeader headerElement = soapEnvelope.getHeader();
				      // AMA_SecurityHostedUser
				      SOAPElement sessionElement = headerElement.addChildElement("Session", "awsec");
				      sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_INSERIES);
				      sessionElement.addChildElement("SessionId", "awsec").setValue(sessionID);
				      sessionElement.addChildElement("SequenceNumber", "awsec").setValue("2");
				      sessionElement.addChildElement("SecurityToken", "awsec").setValue(securityToken);
				    } catch (Exception e) {
				      throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
				    }
				  }
			  
			  // start
			  public static void generateQueuePnrSoapHeader(SaajSoapMessage soapMessage, AmadeusServiceConfig amadeusServiceConfig) throws ErrorResponseException, ApplicationException {
				  try {
				      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
				      soapEnvelope.addNamespaceDeclaration("wsa", "http://www.w3.org/2005/08/addressing");
				      soapEnvelope.addNamespaceDeclaration("ses", "http://xml.amadeus.com/2010/06/Session_v3");
				      soapEnvelope.addNamespaceDeclaration("sec", "http://xml.amadeus.com/2010/06/Security_v1");
				      soapEnvelope.addNamespaceDeclaration("typ", "http://xml.amadeus.com/2010/06/Types_v1");
				      soapEnvelope.addNamespaceDeclaration("iat", "http://www.iata.org/IATA/2007/00/IATA2010.1");
				      soapEnvelope.addNamespaceDeclaration("wsse",
				          "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
				      soapEnvelope.addNamespaceDeclaration("wsu",
				          "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
				      SOAPHeader headerElement = soapEnvelope.getHeader();

				      // AMA_SecurityHostedUser
				      SOAPElement securityHostedUserElement = headerElement.addChildElement("AMA_SecurityHostedUser", "sec");
				      SOAPElement userIDElement = securityHostedUserElement.addChildElement("UserID", "sec");
				      userIDElement.setAttribute("POS_Type", amadeusServiceConfig.getPosType());
				      userIDElement.setAttribute("RequestorType", amadeusServiceConfig.getRequestorType());
				      userIDElement.setAttribute("AgentDutyCode", amadeusServiceConfig.getAgentDutyCode());
				      userIDElement.setAttribute("PseudoCityCode", amadeusServiceConfig.getQueuePnrOffice());    
				      SOAPElement requestorIDElement = userIDElement.addChildElement("RequestorID", "typ");
				      requestorIDElement.addChildElement("CompanyName", "iat").setValue(amadeusServiceConfig.getCompanyName());

				      // -------Security Element
				      // get Security Information
				      AmadeusSecurityInfo securityInfo = SecurityUtil.generateSecurityInfo(amadeusServiceConfig.getWssPassword());

				      SOAPElement securityElement = soapEnvelope.getHeader().addChildElement("Security", "wsse");
				      SOAPElement usernameTokenElement = securityElement.addChildElement("UsernameToken", "wsse");

				      // Username
				      usernameTokenElement.addChildElement("Username", "wsse").setValue(amadeusServiceConfig.getWssUsername());

				      // password
				      SOAPElement passwordElement = usernameTokenElement.addChildElement("Password", "wsse");
				      passwordElement.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");
				      passwordElement.setValue(securityInfo.getPasswdDigest());

				      // Nonce
				      SOAPElement nonceElement = usernameTokenElement.addChildElement("Nonce", "wsse");
				      nonceElement.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
				      nonceElement.setValue(securityInfo.getNonce());
				      // Created
				      usernameTokenElement.addChildElement("Created", "wsu").setValue(securityInfo.getCreated());
				    } catch (Exception e) {
				      throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
				    }
			  }
			  
			  // continue
			  public static void generateMultiElementSoapHeader(SaajSoapMessage soapMessage, AmadeusServiceConfig amadeusServiceConfig,String sessionID, String securityToken,String sequence,boolean end) throws ErrorResponseException, ApplicationException {
				    try {
				    	
				      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
				      soapEnvelope.addNamespaceDeclaration("awsec", "http://xml.amadeus.com/2010/06/Session_v3");
				      SOAPHeader headerElement = soapEnvelope.getHeader();
				      // AMA_SecurityHostedUser
				      SOAPElement sessionElement = headerElement.addChildElement("Session", "awsec");
				      if(end)
				    	  sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_END);
				      else
				      sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_INSERIES);
				      sessionElement.addChildElement("SessionId", "awsec").setValue(sessionID);
				      sessionElement.addChildElement("SequenceNumber", "awsec").setValue(sequence);
				      sessionElement.addChildElement("SecurityToken", "awsec").setValue(securityToken);
				    } catch (Exception e) {
				      throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
				    }
				  }
			  
			 
			  public static void generateCreateTSMSoapHeader(SaajSoapMessage soapMessage, AmadeusServiceConfig amadeusServiceConfig,String sessionID, String securityToken, int sequence) throws ErrorResponseException, ApplicationException
			  {
				  try {
				      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
				      soapEnvelope.addNamespaceDeclaration("awsec", "http://xml.amadeus.com/2010/06/Session_v3");
				      SOAPHeader headerElement = soapEnvelope.getHeader();
				      // AMA_SecurityHostedUser
				      SOAPElement sessionElement = headerElement.addChildElement("Session", "awsec");
				      sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_INSERIES);
				      sessionElement.addChildElement("SessionId", "awsec").setValue(sessionID);
				      sessionElement.addChildElement("SequenceNumber", "awsec").setValue(String.valueOf(sequence));
				      sessionElement.addChildElement("SecurityToken", "awsec").setValue(securityToken);
				    } catch (Exception e) {
				      throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
				    }
			  }  
			  
			  public static void generateCreateFOPSoapHeader(SaajSoapMessage soapMessage, AmadeusServiceConfig amadeusServiceConfig,String sessionID, String securityToken, int sequence) throws ErrorResponseException, ApplicationException
			  {
				  try {
				      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
				      soapEnvelope.addNamespaceDeclaration("awsec", "http://xml.amadeus.com/2010/06/Session_v3");
				      SOAPHeader headerElement = soapEnvelope.getHeader();
				      // AMA_SecurityHostedUser
				      SOAPElement sessionElement = headerElement.addChildElement("Session", "awsec");
				      sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_INSERIES);
				      sessionElement.addChildElement("SessionId", "awsec").setValue(sessionID);
				      sessionElement.addChildElement("SequenceNumber", "awsec").setValue(String.valueOf(sequence));
				      sessionElement.addChildElement("SecurityToken", "awsec").setValue(securityToken);
				    } catch (Exception e) {
				      throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
				    }
			  }  
			  
			  //CPST
			  public static void generateCpstSoapHeader(SaajSoapMessage soapMessage, CpstConfig cpstConfig) throws ApplicationException {
			    try {
			      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
			      soapEnvelope.addNamespaceDeclaration("v1", "http://model.cpst.cathaypacific.com/schema/cpstservice/v1");
			      soapEnvelope.addNamespaceDeclaration("v2", "http://www.cathaypacific.com/infra/fabricxheader/v2");
			      soapEnvelope.addNamespaceDeclaration("wsse",
				          "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
				      soapEnvelope.addNamespaceDeclaration("wsu",
				          "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
			      SOAPHeader headerElement = soapEnvelope.getHeader();
		
			      // security
			      SOAPElement securityElement = headerElement.addChildElement("Security", "wsse");
			      securityElement.setAttribute("SOAP-ENV:mustUnderstand", "1");
			      SOAPElement usernameTokenElement = securityElement.addChildElement("UsernameToken", "wsse");
			      //usernameTokenElement.setAttribute("wsu:Id", "UsernameToken-119607E6656C4B27B114926543600491");
			      //username
			      usernameTokenElement.addChildElement("Username", "wsse").setValue(cpstConfig.getUsername());
			      // password
/*			      AmadeusSecurityInfo securityInfo = SecurityUtil.generateSecurityInfo(cpstConfig.getPassword());*/
			      SOAPElement passwordElement = usernameTokenElement.addChildElement("Password", "wsse");
			      passwordElement.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
			      passwordElement.setValue(cpstConfig.getPassword());

			      //FabriCXHeader
			      SOAPElement fabriElement = headerElement.addChildElement("FabriCXHeader", "v2");
			      //applicationName
			      SOAPElement applicationNameElement= fabriElement.addChildElement("applicationName","v2");
			      applicationNameElement.setValue(cpstConfig.getApplicationName());
			      SOAPElement correlationIdElement= fabriElement.addChildElement("correlationIdElement","v2");
			      correlationIdElement.setValue(cpstConfig.getCorrelationId());
			    } catch (Exception e) {
			      throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
			    }
			  }
			  
			 //issue EMD END
			  public static void generateEmdIssuancSoapHeadere(SaajSoapMessage soapMessage, AmadeusServiceConfig amadeusServiceConfig,String sessionID, String securityToken, int sequence) throws ApplicationException
			  {
				  try {
				      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
				      soapEnvelope.addNamespaceDeclaration("awsec", "http://xml.amadeus.com/2010/06/Session_v3");
				      SOAPHeader headerElement = soapEnvelope.getHeader();
				      // AMA_SecurityHostedUser
				      SOAPElement sessionElement = headerElement.addChildElement("Session", "awsec");
				      sessionElement.setAttribute("TransactionStatusCode", TRANSACTION_CODE_END);
				      sessionElement.addChildElement("SessionId", "awsec").setValue(sessionID);
				      sessionElement.addChildElement("SequenceNumber", "awsec").setValue(String.valueOf(sequence));
				      sessionElement.addChildElement("SecurityToken", "awsec").setValue(securityToken);
				    } catch (Exception e) {
				      throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
				    }
			  }
			 
			  
			  public static void generateTicketSoapHeader(SaajSoapMessage soapMessage, AmadeusServiceConfig amadeusServiceConfig,String officeID) throws ApplicationException {
				    try {
				      SOAPEnvelope soapEnvelope = soapMessage.getSaajMessage().getSOAPPart().getEnvelope();
				      soapEnvelope.addNamespaceDeclaration("wsa", "http://www.w3.org/2005/08/addressing");
				      soapEnvelope.addNamespaceDeclaration("ses", "http://xml.amadeus.com/2010/06/Session_v3");
				      soapEnvelope.addNamespaceDeclaration("sec", "http://xml.amadeus.com/2010/06/Security_v1");
				      soapEnvelope.addNamespaceDeclaration("typ", "http://xml.amadeus.com/2010/06/Types_v1");
				      soapEnvelope.addNamespaceDeclaration("iat", "http://www.iata.org/IATA/2007/00/IATA2010.1");
				      soapEnvelope.addNamespaceDeclaration("wsse",
				          "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
				      soapEnvelope.addNamespaceDeclaration("wsu",
				          "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
				      SOAPHeader headerElement = soapEnvelope.getHeader();

				      // AMA_SecurityHostedUser
				      SOAPElement securityHostedUserElement = headerElement.addChildElement("AMA_SecurityHostedUser", "sec");
				      SOAPElement userIDElement = securityHostedUserElement.addChildElement("UserID", "sec");
				      userIDElement.setAttribute("POS_Type", amadeusServiceConfig.getPosType());
				      userIDElement.setAttribute("RequestorType", amadeusServiceConfig.getRequestorType());
				      userIDElement.setAttribute("AgentDutyCode", amadeusServiceConfig.getAgentDutyCode());
				      userIDElement.setAttribute("PseudoCityCode", officeID);    
				      SOAPElement requestorIDElement = userIDElement.addChildElement("RequestorID", "typ");
				      requestorIDElement.addChildElement("CompanyName", "iat").setValue(amadeusServiceConfig.getCompanyName());

				      // -------Security Element
				      // get Security Information
				      AmadeusSecurityInfo securityInfo = SecurityUtil.generateSecurityInfo(amadeusServiceConfig.getWssPassword());

				      SOAPElement securityElement = soapEnvelope.getHeader().addChildElement("Security", "wsse");
				      SOAPElement usernameTokenElement = securityElement.addChildElement("UsernameToken", "wsse");

				      // Username
				      usernameTokenElement.addChildElement("Username", "wsse").setValue(amadeusServiceConfig.getWssUsername());

				      // password
				      SOAPElement passwordElement = usernameTokenElement.addChildElement("Password", "wsse");
				      passwordElement.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");
				      passwordElement.setValue(securityInfo.getPasswdDigest());

				      // Nonce
				      SOAPElement nonceElement = usernameTokenElement.addChildElement("Nonce", "wsse");
				      nonceElement.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
				      nonceElement.setValue(securityInfo.getNonce());
				      // Created
				      usernameTokenElement.addChildElement("Created", "wsu").setValue(securityInfo.getCreated());

				    } catch (Exception e) {
				      throw new ApplicationException(e.getMessage(), ErrorResponseException.ERR_CONSTRUCT_REQUEST_HEADER);
				    }
				  }

			  
	}


