using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
namespace Assets.RSC.Models
{
	using Assets.RSC.Models;

	public class Message
    {
        public MessageType MessageType { get; set; }
        public string MessageText { get; set; }
        public string SentBy { get; set; }
        private string rawMessageData { get; set; }

        /// <summary>
        /// The first 8 bytes after the packet ID are the username. 
        /// You then need to revert the bytes and pass them to the hashToName function.
        /// </summary>
        /// <param name="type"></param>
        /// <param name="rawMessageData"></param>
        public Message(MessageType type, byte[] rawMessageData)
        {
            this.MessageType = type;

            byte[] tmpByte = new byte[8];

            for (int i = 0; i < tmpByte.Length; i++)
            {
                tmpByte[i] = rawMessageData[i+1];
            }

            Array.Reverse(tmpByte, 0, tmpByte.Length);
            var hashValue = BitConverter.ToInt64(tmpByte, 0);
			this.SentBy = IO.DataOperations.hashToName(hashValue);

            switch (type)
            {
                case MessageType.LOCAL_MESSAGE:
                    {
                        var result = ChatMessage.bytesToString(rawMessageData, 1, rawMessageData.Length - 1);
                        MessageText = result;
                        break;
                    }
                case MessageType.PRIVATE_MESSAGE:
                    {
                        break;
                    }
            }
        }

        public byte[] longToBytes(long x)
        {
            return BitConverter.GetBytes(x);
        }
    }
}
