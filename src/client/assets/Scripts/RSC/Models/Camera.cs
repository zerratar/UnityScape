using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
namespace Assets.RSC.Models
{
	public class Camera
	{
		public static int[] rotationDampingValue = new int[2048];

		static Camera()
		{
			for (int j1 = 0; j1 < 1024; j1++)
			{
				var val = (int)(Math.Sin((double)j1 * 0.00613592315D) * 32768D);
				rotationDampingValue[j1] = (int)(Math.Sin((double)j1 * 0.00613592315D) * 32768D);
				rotationDampingValue[j1 + 1024] = (int)(Math.Cos((double)j1 * 0.00613592315D) * 32768D);
			}
		}

		public static int ToTextureColor(int r, int g, int b)
		{
			return -1 - (r / 8) * 1024 - (g / 8) * 32 - b / 8;
		}
	}
}
