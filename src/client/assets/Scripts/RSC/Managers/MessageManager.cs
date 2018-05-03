namespace Assets.RSC.Managers
{
	using System.Collections.Generic;

	using Assets.RSC.Models;

	public class MessageManager
	{
		public List<Message> MessageList { get; set; }

		private MessageManager()
		{
			MessageList = new List<Message>();
		}
	}
}
