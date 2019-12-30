import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cathaypacific.ng.logger.CxLogManager;
import com.cathaypacific.ng.logger.CxLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @ClassName: JwtJoseUtils
 * @Description: version for jose
 * @author: wei.yue
 * @date: Dec 7, 2018 9:57:05 AM
 * @version: V1.2[api.versionCpp]
 */
@PropertySource("classpath:/jwt.properties")
@Component
public class JwtJoseUtils {
	private static Pattern humpPattern = Pattern.compile("[A-Z]");
	private static CxLogger logger = CxLogManager.getLogger(JwtJoseUtils.class);
	@Value("${cpp.signature.aepSignIbePrivateKey1}")
	private String aepSignIbePrivateKey1;
	@Value("${cpp.signature.aepSignIbePrivateKey2}")
	private String aepSignIbePrivateKey2;
	@Value("${cpp.signature.aepSignIbePrivateKey3}")
	private String aepSignIbePrivateKey3;
	@Value("${cpp.signature.aepSignMmbPrivateKey1}")
	private String aepSignMmbPrivateKey1;
	@Value("${cpp.signature.aepSignMmbPrivateKey2}")
	private String aepSignMmbPrivateKey2;
	@Value("${cpp.signature.aepSignMmbPrivateKey3}")
	private String aepSignMmbPrivateKey3;
	@Value("${cpp.signature.aepSignNdcPrivateKey1}")
	private String aepSignNdcPrivateKey1;
	@Value("${cpp.signature.aepSignNdcPrivateKey2}")
	private String aepSignNdcPrivateKey2;
	@Value("${cpp.signature.aepSignNdcPrivateKey3}")
	private String aepSignNdcPrivateKey3;
	@Value("${cpp.signature.aepSignOLCPrivateKey1}")
	private String aepSignOLCPrivateKey1;
	@Value("${cpp.signature.aepSignOLCPrivateKey2}")
	private String aepSignOLCPrivateKey2;
	@Value("${cpp.signature.aepSignOLCPrivateKey3}")
	private String aepSignOLCPrivateKey3;

	@Value("${cpp.signature.cppSignCppPublicKey1}")
	private String cppSignCppPublicKey1;
	@Value("${cpp.signature.cppSignCppPublicKey2}")
	private String cppSignCppPublicKey2;
	@Value("${cpp.signature.cppSignCppPublicKey3}")
	private String cppSignCppPublicKey3;
	

	@Value("${cpp.signature.cppSignatureEnable}")
	private boolean cppSignatureEnable;

	@Value("${cpp.signature.headerAlg}")
	private String headerAlg;
	@Value("${cpp.signature.headerTyp}")
	private String headerTyp;
//	@Value("${cpp.signature.headerIss}")
//	private String headerIss;

	@Autowired
	public ObjectMapper objectMapper;
	public static final String CPP_SIGNATURE = "cpp_signature";

	/**
	 * @param obj
	 * @param appCode
	 * @return
	 * @throws Exception
	 * @Title: generate cpp signature
	 * @Description: TODO
	 * @return: String
	 */
	public String generateCppSignature(Object obj, String appCode) throws Exception {
		String kidAndKey = getRandomAEPPrivateKey(appCode);
//		logger.info("appCode {} ,use privateKey {}", appCode, kidAndKey);
		if (kidAndKey.isEmpty()) {
			logger.error("Unable to Generate AEP Signature Because no valid AEP Private Key");
			return "";
		}
		String[] kidAndKeyArray = kidAndKey.split(" ");
		String kid = kidAndKeyArray[0];
		String cppPrivateKey = kidAndKeyArray[1];
		PrivateKey privateKey = getPrivateKey(cppPrivateKey);
		// Generate the payload
		JsonWebSignature jws = new JsonWebSignature();
		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
		jws.setPayload(JsonUtils.translateToJson(obj));
		jws.setHeader("alg", headerAlg);
		jws.setHeader("typ", headerTyp);
		jws.setHeader("iss", appCode);
		jws.setKeyIdHeaderValue(kid);
		// Sign using the private key
		jws.setKey(privateKey);
		try {
			logger.info("generate cpp signature : kid use {}", kid);
			return jws.getCompactSerialization();
		} catch (JoseException e) {
			logger.error(e.toString());
			return "";
		}
	}

	/**
	 * @return
	 * @Title: getRandomAEPPrivateKey
	 * @Description: Random AEP PrivateKey
	 * @return: String
	 */
	private String getRandomAEPPrivateKey(String appCode) {
		List<String> strList = new ArrayList<String>();

		switch (appCode) {
		case "IBE":
			if (!aepSignIbePrivateKey1.isEmpty() && aepSignIbePrivateKey1.split(" ").length == 2) {
				strList.add(aepSignIbePrivateKey1);
			}
			if (!aepSignIbePrivateKey2.isEmpty() && aepSignIbePrivateKey2.split(" ").length == 2) {
				strList.add(aepSignIbePrivateKey2);
			}
			if (!aepSignIbePrivateKey3.isEmpty() && aepSignIbePrivateKey3.split(" ").length == 2) {
				strList.add(aepSignIbePrivateKey3);
			}
			break;
		case "MMB":
			if (!aepSignMmbPrivateKey1.isEmpty() && aepSignMmbPrivateKey1.split(" ").length == 2) {
				strList.add(aepSignMmbPrivateKey1);
			}
			if (!aepSignMmbPrivateKey2.isEmpty() && aepSignMmbPrivateKey2.split(" ").length == 2) {
				strList.add(aepSignMmbPrivateKey2);
			}
			if (!aepSignMmbPrivateKey3.isEmpty() && aepSignMmbPrivateKey3.split(" ").length == 2) {
				strList.add(aepSignMmbPrivateKey3);
			}
			break;
		case "OLC":
			if (!aepSignOLCPrivateKey1.isEmpty() && aepSignOLCPrivateKey1.split(" ").length == 2) {
				strList.add(aepSignOLCPrivateKey1);
			}
			if (!aepSignOLCPrivateKey2.isEmpty() && aepSignOLCPrivateKey2.split(" ").length == 2) {
				strList.add(aepSignOLCPrivateKey2);
			}
			if (!aepSignOLCPrivateKey3.isEmpty() && aepSignOLCPrivateKey3.split(" ").length == 2) {
				strList.add(aepSignOLCPrivateKey3);
			}
			break;
		case "NDC":
			if (!aepSignNdcPrivateKey1.isEmpty() && aepSignNdcPrivateKey1.split(" ").length == 2) {
				strList.add(aepSignNdcPrivateKey1);
			}
			if (!aepSignNdcPrivateKey2.isEmpty() && aepSignNdcPrivateKey2.split(" ").length == 2) {
				strList.add(aepSignNdcPrivateKey2);
			}
			if (!aepSignNdcPrivateKey3.isEmpty() && aepSignNdcPrivateKey3.split(" ").length == 2) {
				strList.add(aepSignNdcPrivateKey3);
			}
			break;
		default:
			break;
		}
		String[] strArr = new String[strList.size()];
		for (int i = 0; i < strList.size(); i++) {
			strArr[i] = strList.get(i);
		}
		int random = (int) (Math.random() * 3);
		// System.out.println(strArr[random] + random);

		return strArr[random];

	}


	/**
	 * @param cppSignature
	 * @param appCode
	 * @return
	 * @Title: getTpPublicKey
	 * @Description: get Tp PublicKey
	 * @return: String
	 */
	private String getTpPublicKey(String cppSignature) {
		DecodedJWT jwt = null;
		try {
			jwt = JWT.decode(cppSignature);
			jwt.getClaims();
		} catch (JWTDecodeException exception) {
			return "";
		}
		logger.info("validate cpp signature : kid use {}", jwt.getKeyId());
		List<String> listKidAndKey = new ArrayList<>();
		if (!cppSignCppPublicKey1.isEmpty() && cppSignCppPublicKey1.split(" ").length == 2) {
			listKidAndKey.add(cppSignCppPublicKey1);
		}
		if (!cppSignCppPublicKey2.isEmpty() && cppSignCppPublicKey2.split(" ").length == 2) {
			listKidAndKey.add(cppSignCppPublicKey2);
		}
		if (!cppSignCppPublicKey3.isEmpty() && cppSignCppPublicKey3.split(" ").length == 2) {
			listKidAndKey.add(cppSignCppPublicKey3);
		}

		for (String kidAndKey : listKidAndKey) {
			if (kidAndKey.split(" ")[0].equals(jwt.getKeyId())) {
				return kidAndKey.split(" ")[1];
			}
		}
		return "";
	}

	/**
	 * @param token
	 * @param tpPublicKeyString
	 * @return
	 * @throws Exception
	 * @Title: decryptCppSignature
	 * @Description: decrypt Cpp Signature
	 * @return: JwtClaims
	 */
	private JwtClaims decryptCppSignature(String token, String tpPublicKeyString) throws Exception {
		PublicKey publicKey;
		try {
			publicKey = getPublicKey(tpPublicKeyString);
			JwtConsumer jwtConsumer = new JwtConsumerBuilder().setVerificationKey(publicKey).build();
			return jwtConsumer.processToClaims(token);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidJwtException e) {
			return null;
		}

	}

	/**
	 * @param key
	 * @return
	 * @throws Exception
	 * @Title: getPublicKey
	 * @Description: get PublicKey
	 * @return: RSAPublicKey
	 */
	private static RSAPublicKey getPublicKey(String key) throws Exception {
		try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			e.toString();
			return null;
		}
	}

	/**
	 * @param key
	 * @return
	 * @throws Exception
	 * @Title: getPrivateKey
	 * @Description: get PrivateKey
	 * @return: RSAPrivateKey
	 */
	private static RSAPrivateKey getPrivateKey(String key) throws Exception {
		try {
			if(null == java.security.Security.getProvider(BouncyCastleProvider.PROVIDER_NAME))
				java.security.Security.addProvider(new BouncyCastleProvider());
            byte[] keyBytes = Base64.getDecoder().decode(key);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			e.toString();
			return null;
		}
	}

	public boolean validateCppSignature(JsonNode jsonNodeFromCPP) throws Exception {
		 JsonNode jnSignature = jsonNodeFromCPP.get(CPP_SIGNATURE);
		 String cppSignature = (jnSignature==null?"":jnSignature.asText());
		 String publicKeyString = getTpPublicKey(cppSignature);
//		logger.info("use publicKey {}" , publicKeyString);
		if (publicKeyString.isEmpty()) {
			logger.info("public key is empty, validate fail");
			return false;
		}
		JwtClaims jwtClaims = decryptCppSignature(cppSignature, publicKeyString);
		if (jwtClaims == null) {
			logger.info("jwtClaims to decrypt cpp signature is null, validate fail");
			return false;
		}
		String rawJson = jwtClaims.getRawJson();
		JsonNode rawJsonNode = objectMapper.readTree(rawJson);
		if(rawJsonNode.size()!=(jsonNodeFromCPP.size()-1)){
			logger.info("cpp signature size not match with rawJson key value pairs, validate fail");
			return false;
		}
		Iterator<String> rawKeys = rawJsonNode.fieldNames();
		while(rawKeys.hasNext()){
			String rawKey = rawKeys.next();
			Iterator<String> keys = jsonNodeFromCPP.fieldNames();
			if(!checkKeyExist(keys,rawKey)) {
				logger.info("Fail to verify CPP signature with key:{},exists in signature, NOT exists in cpp response"+rawKey);
				return false;
			}
			if(!objectMapper.writeValueAsString(jsonNodeFromCPP.findValue(rawKey)).equals(objectMapper.writeValueAsString(rawJsonNode.findValue(rawKey)))){
				logger.info("Fail to verify CPP signature with key:{}, signature value:{}, response value: {}",
						rawKey, objectMapper.writeValueAsString(rawJsonNode.findValue(rawKey)),
						objectMapper.writeValueAsString(jsonNodeFromCPP.findValue(rawKey)));
				return false;
			}
		}
		logger.info("cpp signature validation success");
		return true;
		
	}

	private boolean checkKeyExist(Iterator<String> keys, String rawKey) {
		while(keys.hasNext()){
			if(keys.next().equals(rawKey)){
				return true;
			}
		}
		return false;
	}
	
	
}