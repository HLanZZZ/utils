import com.cathaypacific.Entity.AmadeusSecurityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.TimeZone;

public class SecurityUtil {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** Constant of Date Format */
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	/** Secure random SHA1PRNG */
	private static final String SECURE_RANDOM_SHA1PRNG = "SHA1PRNG";

	/** Encoding UTF-8 */
	private static final String ENCODING_UTF8 = "UTF-8";

	/** Algorithm SHA-1 */
	private static final String ALGORITHM_SHA1 = "SHA-1";

	/** Time zone Zulu */
	private static final String TIME_ZONE_ZULU = "Zulu";

	/**
	 * Empty constructor.
	 */
	private SecurityUtil() {}

	/**
	 * This method will return a new SecurityInfo for 1A header 4.0
	 * 
	 * @return AmadeusSecurityInfo
	 */
	public static AmadeusSecurityInfo generateSecurityInfo(String password) {
		String created = generateCreated();
		String nonce = generateNonce();
		String uniqueId=generateUniqueId();
		String messageId=generateMessageId();
		String passwordDigest = passwordDigest(passwordEncrypt(ALGORITHM_SHA1, password), nonce, created);

		return new AmadeusSecurityInfo(passwordDigest, created, nonce,uniqueId,messageId);
	}

	/**
	 * Generate New Nonce.
	 * 
	 * @return new nonce.
	 */
	private static String generateNonce() {
		SecureRandom random;
		byte[] nonceValue = null;
		try {
			random = SecureRandom.getInstance(SECURE_RANDOM_SHA1PRNG);
			random.setSeed(System.currentTimeMillis());
			nonceValue = new byte[16];
			random.nextBytes(nonceValue);
		} catch (NoSuchAlgorithmException e) {
			log.error("Generate nonce failed.", e);
		}

		return Base64.getEncoder().encodeToString(nonceValue);
	}

	/**
	 * Generate New Created, format yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
	 * 
	 * @return
	 */
	private static String generateCreated() {
		Calendar calendar = Calendar.getInstance();
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_ZULU));

		return DATE_FORMAT.format(calendar.getTime());
	}

	/**
	 * Generate new password encrypt.
	 * 
	 * @return new password encrypt.
	 */
	private static byte[] passwordEncrypt(String algorithm, String securityPassword1a) {

		byte[] encryptedPassword = null;
		try {
			MessageDigest shaDigest = MessageDigest.getInstance(algorithm);
			shaDigest.reset();
			if (securityPassword1a != null) {
				shaDigest.update(securityPassword1a.getBytes(ENCODING_UTF8));
			}
			encryptedPassword = shaDigest.digest();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			log.error("Digest security password failed.", e);
		}

		return encryptedPassword;
	}

	/**
	 * Generate New Password Digest.
	 * 
	 * @return new password digest.
	 */
	private static String passwordDigest(byte[] password, String nonce, String created) {

		String passwdDigest = null;
		byte[] noceBytes = nonce != null ? Base64.getDecoder().decode(nonce) : new byte[0];
		byte[] createdBytes = null;
		try {
			createdBytes = created != null ? created.getBytes(ENCODING_UTF8) : new byte[0];
		} catch (UnsupportedEncodingException e) {
			log.error("Get UTF-8 bytes from created failed.", e);
			createdBytes = new byte[0];
		}
		byte[] passwordBytes = password;
		byte[] totalBytes = new byte[noceBytes.length + createdBytes.length + passwordBytes.length];
		byte offset = 0;
		System.arraycopy(noceBytes, 0, totalBytes, offset, noceBytes.length);
		offset += noceBytes.length;
		System.arraycopy(createdBytes, 0, totalBytes, offset, createdBytes.length);
		offset += createdBytes.length;
		System.arraycopy(passwordBytes, 0, totalBytes, offset, passwordBytes.length);

		try {
			MessageDigest shaDigest = MessageDigest.getInstance(ALGORITHM_SHA1);
			shaDigest.reset();
			shaDigest.update(totalBytes);

			passwdDigest = Base64.getEncoder().encodeToString(shaDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			log.error("Digest password failed.", e);
		}

		return passwdDigest;
	}
	
	
	private static String generateUniqueId() {
		SecureRandom random;
		byte[] uniqueId = null;
		try {
			random = SecureRandom.getInstance(SECURE_RANDOM_SHA1PRNG);
			random.setSeed(System.currentTimeMillis());
			uniqueId = new byte[16];
			random.nextBytes(uniqueId);
		} catch (NoSuchAlgorithmException e) {
			log.error("Generate nonce failed.", e);
		}
		return Base64.getEncoder().encodeToString(uniqueId);
	}
	
	private static String generateMessageId() {
		SecureRandom random;
		byte[] messageId = null;
		try {
			random = SecureRandom.getInstance(SECURE_RANDOM_SHA1PRNG);
			random.setSeed(System.currentTimeMillis());
			messageId = new byte[16];
			random.nextBytes(messageId);
		} catch (NoSuchAlgorithmException e) {
			log.error("Generate nonce failed.", e);
		}
		return Base64.getEncoder().encodeToString(messageId);
	}
}
