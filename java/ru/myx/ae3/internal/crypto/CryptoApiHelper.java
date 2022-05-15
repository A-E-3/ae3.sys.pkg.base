package ru.myx.ae3.internal.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import ru.myx.ae3.binary.TransferCopier;
import ru.myx.ae3.know.WhirlpoolDigest;
import ru.myx.crypto.EllipticCurveSecp256r1;
import ru.myx.crypto.SignatureECDSA;
import ru.myx.util.Base58;

/** @author myx */
public class CryptoApiHelper {

	/**
	 *
	 */
	public static final Class<EllipticCurveSecp256r1> EllipticCurveSecp256r1 = EllipticCurveSecp256r1.class;
	/**
	 *
	 */
	public static final Class<SignatureECDSA> SignatureECDSA = SignatureECDSA.class;

	/** @return
	 * @throws NoSuchAlgorithmException */
	public static final MessageDigest createDigestMd5() throws NoSuchAlgorithmException {

		return MessageDigest.getInstance("MD5");
	}

	/** @return
	 * @throws NoSuchAlgorithmException */
	public static final MessageDigest createDigestSha256() throws NoSuchAlgorithmException {

		return MessageDigest.getInstance("SHA-256");
	}

	/** @return
	 * @throws NoSuchAlgorithmException */
	public static final MessageDigest createDigestWhirlpool() throws NoSuchAlgorithmException {

		return new WhirlpoolDigest();
		// return MessageDigest.getInstance("Whirlpool");
	}

	/** @param text
	 * @param privateKeyObject
	 * @return
	 * @throws GeneralSecurityException */
	public static final String signStringUtfSha256WithEcdsaAsBase58(final String text, final Object privateKeyObject) throws GeneralSecurityException {

		final PrivateKey privateKey;
		if (privateKeyObject instanceof TransferCopier) {
			privateKey = ru.myx.crypto.EllipticCurveSecp256r1.parsePrivateKeyFromBytesPKCS8(((TransferCopier) privateKeyObject).nextDirectArray());
		} else //
		if (privateKeyObject instanceof byte[]) {
			privateKey = ru.myx.crypto.EllipticCurveSecp256r1.parsePrivateKeyFromBytesPKCS8((byte[]) privateKeyObject);
		} else {
			throw new GeneralSecurityException("Unsupported privateKeyObject format!");
		}

		final Signature signature = ru.myx.crypto.SignatureECDSA.prepareSignSHA256withECDSA(privateKey);
		signature.update(text.getBytes(StandardCharsets.UTF_8));
		return Base58.encode(signature.sign());
	}

	/** @param text
	 * @param publicKeyObject
	 *            (base58)
	 * @param signatureBase58
	 * @return
	 * @throws GeneralSecurityException */
	public static final boolean verifyStringUtfSha256WithEcdsaAsBase58(final String text, final Object publicKeyObject, final String signatureBase58)
			throws GeneralSecurityException {

		final PublicKey publicKey;
		if (publicKeyObject instanceof CharSequence) {
			final String publicKeyString = publicKeyObject.toString();
			if (publicKeyString.length() == 66) {
				publicKey = ru.myx.crypto.EllipticCurveSecp256r1.parsePublicKeyFromHexCompressed(publicKeyString);
			} else {
				publicKey = ru.myx.crypto.EllipticCurveSecp256r1.parsePublicKeyFromBytesCompressed(Base58.decode(publicKeyString));
			}
		} else //
		if (publicKeyObject instanceof TransferCopier) {
			publicKey = ru.myx.crypto.EllipticCurveSecp256r1.parsePublicKeyFromBytesCompressed(((TransferCopier) publicKeyObject).nextDirectArray());
		} else //
		if (publicKeyObject instanceof byte[]) {
			publicKey = ru.myx.crypto.EllipticCurveSecp256r1.parsePublicKeyFromBytesCompressed((byte[]) publicKeyObject);
		} else {
			throw new GeneralSecurityException("Unsupported publicKeyObject format!");
		}

		final Signature signature = ru.myx.crypto.SignatureECDSA.prepareVerifySHA256withECDSA(publicKey);
		signature.update(text.getBytes(StandardCharsets.UTF_8));
		return signature.verify(Base58.decode(signatureBase58));
	}
}
