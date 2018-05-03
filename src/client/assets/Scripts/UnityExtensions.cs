using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Assets
{
	using System.Runtime.Remoting.Messaging;

	using UnityEngine;
	public static class UnityExtensions
	{
		public static T Find<T>(this GameObject dummy, string name)
		{
			var obj = GameObject.Find(name);
			
			return obj.GetComponent<T>();
		}
	}
}
