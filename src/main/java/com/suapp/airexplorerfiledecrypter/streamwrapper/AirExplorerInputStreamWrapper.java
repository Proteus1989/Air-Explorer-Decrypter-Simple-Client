package com.suapp.airexplorerfiledecrypter.streamwrapper;

import com.suapp.airexplorerfiledecrypter.utils.Document;
import com.suapp.airexplorerfiledecrypter.utils.Base64;
import com.suapp.airexplorerfiledecrypter.utils.PasswordDeriveBytes;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AirExplorerInputStreamWrapper extends InputStreamWrapper
{

    private long counter = 0;
    private Cipher rijndael;
    private byte[] secret;
    private byte[] salt;

    public AirExplorerInputStreamWrapper(InputStream in, Document params) throws IllegalArgumentException, IOException, ShortBufferException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
    {
        super(in, params);

        try
        {
            String filename = params.get("filename", String.class);
            String password = params.get("password", String.class);

            switch (filename.substring(filename.lastIndexOf(".") + 1))
            {
                case "cloudencoded":
                    setProcessedName(filename.replace(".cloudencoded", ""));
                    break;
                case "cloudencoded2":
                    setProcessedName(decryptName(filename, password));
                    break;
                default:
                    throw new IllegalArgumentException("File extension not allowed");
            }

            DataInputStream binaryReader = new DataInputStream(in);

            byte[] versionArray = new byte[8];
            binaryReader.readFully(versionArray);

            ByteBuffer.wrap(revert(versionArray)).getLong();

            long value = ByteBuffer.wrap(revert(versionArray)).getLong();
            if (value != 58261948629778432L) //58261948629778432L
                throw new IOException("Corrupted file");

            byte[] one1Array = new byte[8];
            binaryReader.readFully(one1Array);

            value = ByteBuffer.wrap(revert(one1Array)).getLong();
            if (value != 1L) //1L
                throw new IOException("Corrupted file");

            byte[] firstBlock = new byte[64];
            binaryReader.readFully(firstBlock);

            byte[] numArray2 = new byte[16];
            binaryReader.readFully(numArray2);

            byte[] rgbSalt = new byte[]
            {
                (byte) 38, (byte) 25, (byte) 129, (byte) 78, (byte) 160, (byte) 109, (byte) 149, (byte) 52, (byte) 38, (byte) 117, (byte) 100, (byte) 5, (byte) 246
            };

            String strPassword = password;

            byte[] hash;
            secret = new PasswordDeriveBytes(strPassword, rgbSalt).GetBytes(32);
            salt = new byte[]
            {
                secret[2], secret[1], secret[4], secret[3], secret[5], secret[6], secret[7], secret[8], (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0
            };

            this.rijndael = createRijndaelCipher(secret);

            byte[] decrytptedContent = decryptBlock(firstBlock);
            hash = MessageDigest.getInstance("MD5").digest(decrytptedContent);
            if (!Arrays.equals(hash, numArray2))
                throw new IllegalArgumentException("Wrong password");
        } catch (IllegalArgumentException | IOException | ShortBufferException ex)
        {
            throw ex;
        } catch (DigestException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex)
        {
            // This exceptions can never be thrown.
            // Doing nothing
        }

    }

    public Cipher createRijndaelCipher(byte[] secret) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
    {
        final SecretKeySpec skeySpec = new SecretKeySpec(secret, "Rijndael");
        Cipher rijndaelManaged = Cipher.getInstance("Rijndael/ECB/NoPadding");
        rijndaelManaged.init(Cipher.ENCRYPT_MODE, skeySpec);
        return rijndaelManaged;
    }

    public static String decryptName(String name, String password) throws IllegalFormatException, IOException
    {
        try
        {
            byte[] buffer = Base64.decode(name.replace("_", "+").replace("-", "/").replace(".cloudencoded2", ""));
            byte[] bytes = new PasswordDeriveBytes(password, null).GetBytes(32);

            final SecretKeySpec skeySpec = new SecretKeySpec(bytes, "Rijndael");
            Cipher rijndaelManaged = Cipher.getInstance("Rijndael/CBC/NoPadding");
            rijndaelManaged.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec("za893eji340y89hn".getBytes(StandardCharsets.US_ASCII)));

            String out = new String(rijndaelManaged.doFinal(buffer), "utf-8").trim();
            if (out.endsWith("-vrfy-"))
                out = out.substring(0, out.length() - "-vrfy-".length());

            return out;
        } catch (UnsupportedEncodingException | DigestException | InvalidAlgorithmParameterException | InvalidKeyException
                | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e)
        {
            throw new IOException("Error decrypting filename");
        }

    }

    public byte[] decryptBlock(byte[] numArray1) throws ShortBufferException
    {
        byte[] numArray = new byte[numArray1.length];

        int inputCount = rijndael.getBlockSize();
        byte[] temporal = new byte[salt.length];
        System.arraycopy(salt, 0, temporal, 0, salt.length);

        long num = (long) (numArray1.length / inputCount + (numArray1.length % inputCount > 0 ? 1 : 0));
        for (long index1 = 0; index1 < num; ++index1)
        {
            byte[] outputBuffer = new byte[inputCount];
//            showUnsigned(outputBuffer);
            byte[] inputBuffer = temporal;
//            showUnsigned(inputBuffer);
            byte[] longBytes = revert(longToBytes(counter));
            longBytes = revert(longBytes);
            System.arraycopy(longBytes, 0, inputBuffer, inputCount - 8, inputBuffer.length - (inputCount - 8));

            rijndael.update(inputBuffer, 0, inputCount, outputBuffer, 0);

            int index2 = (int) index1 * inputCount;

            try
            {
                byte[] tmp = decryptArray(numArray1, index2, outputBuffer);
                System.arraycopy(tmp, 0, numArray, index2, tmp.length);
            } catch (Exception e)
            {
                e.printStackTrace();
                throw e;
            }

            ++counter;
        }

        return numArray;
    }

    private byte[] decryptArray(byte[] _param0, int _param1, byte[] _param2)
    {
        int length = Math.min(_param0.length - _param1, _param2.length);
        byte[] numArray = new byte[length];
        for (int index = 0; index < length; ++index)
            numArray[index] = (byte) (_param0[_param1 + index] ^ _param2[index]);
        return numArray;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException
    {
        int totalRead = 0;
        int read;

        int toBeRead = len;
        do
        {
            read = in.read(b, off + totalRead, toBeRead - totalRead);
            totalRead += read != -1 ? read : 0;
        } while (read > -1 && totalRead < toBeRead);

        try
        {
            byte[] tmp = new byte[len];
            System.arraycopy(b, off, tmp, 0, len);
            tmp = decryptBlock(tmp);
            System.arraycopy(tmp, 0, b, off, len);
        } catch (Exception ex)
        {
            Logger.getLogger(AirExplorerInputStreamWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return totalRead != 0 ? totalRead : -1;

    }

    private static byte[] revert(byte[] value)
    {
        final int length = value.length;
        byte[] res = new byte[length];
        for (int i = 0; i < length; i++)
            res[length - i - 1] = value[i];
        return res;
    }

    public byte[] longToBytes(long x)
    {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    @Override
    public void close() throws IOException
    {
        super.close(); //To change body of generated methods, choose Tools | Templates.
        in.close();
    }

}
