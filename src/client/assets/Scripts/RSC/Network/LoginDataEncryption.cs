namespace Assets.RSC.Network
{
	using System;
	using System.Globalization;
	/* using System.Numerics;*/
	using System.Linq;
	using System.Security.Cryptography;
	using System.Text;

	public class LoginDataEncryption
	{
		public void addByte(int i)
		{
			packet[offset++] = (byte)i;
		}

		public void addInt(int i)
		{
			packet[offset++] = (byte)(i >> 24);
			packet[offset++] = (byte)(i >> 16);
			packet[offset++] = (byte)(i >> 8);
			packet[offset++] = (byte)i;
		}


		public void addString(String s)
		{

			var bytes0 = Encoding.UTF8.GetBytes(s);
			Array.Copy(bytes0, 0, packet, offset, bytes0.Length);

			//s.getBytes(0, s.length(), packet, offset);
			offset += bytes0.Length;
			packet[offset++] = 10;
		}

		public void addBytes(byte[] bytes, int off, int length)
		{
			for (int i = off; i < off + length; i++)
				packet[this.offset++] = bytes[i];

		}

		public int getByte()
		{
			return packet[offset++] & 0xff;
		}

		public int getShort()
		{
			offset += 2;
			return ((packet[offset - 2] & 0xff) << 8) + (packet[offset - 1] & 0xff);
		}

		public int getInt()
		{
			offset += 4;
			return ((packet[offset - 4] & 0xff) << 24) + ((packet[offset - 3] & 0xff) << 16) + ((packet[offset - 2] & 0xff) << 8) + (packet[offset - 1] & 0xff);
		}

		public void getBytes(byte[] arg0, int arg1, int arg2)
		{
			for (int i = arg1; i < arg1 + arg2; i++)
				arg0[i] = packet[offset++];

		}


		public void getBytes(byte[] arg0, int arg1, int arg2, ref int offsetter)
		{
			for (int i = arg1; i < arg1 + arg2; i++)
				arg0[i] = packet[offsetter++];
		}
		//public byte[] encrypt(byte[] text)
		//{
		//	byte[] cipherText = null;
		//	//Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		//	//cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		//	//cipherText = cipher.doFinal(text);

		//	//return Crypto.Encrypt(text, false);
		//	return text;

		//	//return cipherText;
		//}

		public LoginDataEncryption(byte[] abyte0)
		{
			packet = abyte0;
			offset = 0;
			//try
			//{
			//	// keyFactory = KeyFactory.getInstance("RSA");



			//	Crypto = new RSACryptoServiceProvider();
			//	pubKey = Crypto.ExportParameters(false);


			//	//parms.
			//	//var key =
			//	//    BigInteger.Parse(
			//	//        "258483531987721813854435365666199783121097212864526576114955744050873252978581213214062885665119329089273296913884093898593877564098511382732309048889240854054459372263273672334107564088395710980478911359605768175143527864461996266529749955416370971506195317045377519645018157466830930794446490944537605962330090699836840861268493872513762630835769942133970804813091619416385064187784658945")
			//	//        .ToByteArray();
			//	//pubKey = keyFactory.generatePublic(new X509EncodedKeySpec());
			//}
			//catch (Exception e) { }
		}



		public void encryptPacketWithKeys(BigInteger key, BigInteger modulus)
		{

			/* // Java Version, IKVM required
			var key1 = new java.math.BigInteger("1370158896620336158431733257575682136836100155721926632321599369132092701295540721504104229217666225601026879393318399391095704223500673696914052239029335");
			var modulus1 = new java.math.BigInteger("1549611057746979844352781944553705273443228154042066840514290174539588436243191882510185738846985723357723362764835928526260868977814405651690121789896823");
			
			int i1 = offset;
			offset = 0;
			byte[] dummyPacket1 = new byte[i1];
			getBytes(dummyPacket1, 0, i1);
			java.math.BigInteger biginteger31 = new java.math.BigInteger(dummyPacket1).modPow(key1, modulus1);
			byte[] encryptedPacket1 = biginteger31.toByteArray();
			offset = 0;
			var output1 = string.Join(Environment.NewLine, encryptedPacket1);
			addByte(encryptedPacket1.Length);
			addBytes(encryptedPacket1, 0, encryptedPacket1.Length);
			*/

			int i = offset;
			offset = 0;
			byte[] dummyPacket = new byte[i];
			getBytes(dummyPacket, 0, i);
			BigInteger biginteger3 = new BigInteger(dummyPacket).modPow(key, modulus);
			byte[] encryptedPacket = biginteger3.getBytes();
			offset = 0;
			addByte(encryptedPacket.Length);
			addBytes(encryptedPacket, 0, encryptedPacket.Length);


		}

		// static Crc32 crc = new Crc32();

		public byte[] packet;
		public int offset;
		// public RSACryptoServiceProvider Crypto;
		//private KeyFactory keyFactory;
		// private RSAParameters pubKey;
	}
}