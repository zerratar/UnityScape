using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Assets.RSC.Managers
{
	using System.IO;
	using System.Net.Mime;

	using Assets.RSC.IO;
	using Assets.RSC.Models;

	using UnityEngine;

	public class TextureData
	{
		public Texture2D Texture { get; set; }
		public int TextureIndex { get; set; }

		public TextureData(int index, Texture2D tex)
		{
			this.Texture = tex;
			this.TextureIndex = index;
		}
	}
	public class TextureManager
	{
		public static List<TextureData> Textures = new List<TextureData>();

		public static int _textureCount = 4000;

		public static Texture2D TextureAtlas;

		public static AtlasRect[] TextureAtlasRects;

		public static void GenerateTextureAtlas()
		{

			var uniqueTextures = Textures.Where(t => t.TextureIndex != Engine.NULLINTEGER && t.Texture != null).DistinctBy(i => i.TextureIndex).ToArray();
			if (uniqueTextures.Length > 0)
			{
				// Debug.Log("uniqueTexturesCount: " + uniqueTextures.Length);
				// GenerateTextureAtlas(uniqueTextures.Select(f => f.Texture).ToArray());

				// TextureAtlas = new Texture2D();

				var textures = uniqueTextures.Select(f => f.Texture).ToArray();


				


				var tex = new Texture2D(2048, 2048, TextureFormat.ARGB32, false);


		

				var rects = tex.PackTextures(textures, 0, 2048);// TextureManager.GenerateTextureAtlas(1024, textures, ref TextureAtlas);
				
				TextureAtlas = tex;

				if (rects.Length > 0)
				{
				//	Debug.Log("rects Found: " + rects.Length);

					// TextureAtlas = new Texture2D(2048, 2048);
					// var rects = TextureAtlas.PackTextures(textures, 2, 2048);

					var rects2 = new List<AtlasRect>();
					for (int i = 0; i < uniqueTextures.Length; i++)
					{
						rects2.Add(new AtlasRect() { Rect = rects[i], TextureIndex = uniqueTextures[i].TextureIndex });
					}
					TextureAtlasRects = rects2.ToArray();
				}
			}
		}

		public static Rect[] GenerateTextureAtlas(int maxWidth, Texture2D[] textures, ref Texture2D TextureAtlas)
		{
			List<Rect> rects = new List<Rect>();
			List<TextureHolder> targetPanel = new List<TextureHolder>();

			int highestThisRow = 0;

			int count = 0, total = 0, rowCount = 1;
			float totalHeight = 0f;

			int maxRowWidth = maxWidth;
			int highestLastRow = 0;

			foreach (var sprite in textures)
			{
				TextureHolder pb = new TextureHolder();
				pb.Width = sprite.width;
				pb.Height = sprite.height;
				pb.Texture = sprite;
				if (targetPanel.Count > 0)
				{
					var lastControl = targetPanel[targetPanel.Count - 1];
					var loc = lastControl.Location;

					var w = lastControl.Width;
					var h = lastControl.Height;

					var outOfBounds = ((w + loc.X + 1 + pb.Width) > maxRowWidth);

					if (outOfBounds)
					{
						count = 0;
						rowCount++;
						if (highestThisRow < h)
							highestThisRow = (int)h;

						pb.Location = new Point(0, loc.Y + highestThisRow + 1);

						totalHeight += highestThisRow + 1;

						highestLastRow = highestThisRow;
						highestThisRow = 0;

					}
					else
					{
						if (count == 0)
							highestThisRow = highestLastRow;

						if (highestThisRow < h)
							highestThisRow = (int)h;

						if (rowCount == 1) totalHeight = highestThisRow + 1;

						// var newY = loc.Y;
						// if (total > 10) newY = loc.Y + size.Height;
						pb.Location = new Point(loc.X + w + 1, loc.Y);


					}
				}

				targetPanel.Add(pb);

				count++;
				total++;
			}

			foreach (var pb in targetPanel)
			{

				// 0,1       1,1
				//  |-\ \-----|
				//  |  \ \    |
				//  |   \ \   |
				//  |    \ \  |
				//  |-----\ \-|
				// 0,0       1,0


				// 
				// X: 400
				// Y: 200
				//
				// 


				var uvX = pb.Location.X / (float)maxWidth;

				var uvY = (pb.Location.Y / (float)totalHeight);

				var uvX2 = ((pb.Width + pb.Location.X) / (float)maxWidth);

				var uvY2 = ((pb.Height + pb.Location.Y) / (float)totalHeight);

				// Debug.Log();

				rects.Add(new Rect(uvX, uvY, uvX2, uvY2));

				Debug.Log("UV: " + uvX + "," + uvY + "," + uvX2 + "," + uvY2);
			}



			TextureAtlas = new Texture2D(maxWidth, (int)totalHeight, TextureFormat.ARGB32, false);
			foreach (var t in targetPanel)
			{
				var pixels = t.Texture.GetPixels();
				// Debug.Log("Set Pixels At: " + t.Location.X + "," + t.Location.Y);

				TextureAtlas.SetPixels((int)t.Location.X, (int)t.Location.Y, (int)t.Width, (int)t.Height, pixels);
			}



			//var reversed = TextureAtlas.GetPixels().Reverse();
			//TextureAtlas.SetPixels(reversed.ToArray());
			TextureAtlas.Apply();


			//TextureAtlas = Rotate(TextureAtlas, RotateFlipType.RotateNoneFlipY);


			var data = TextureAtlas.EncodeToPNG();
			File.WriteAllBytes(@"c:\textures2\texturepack.png", data);
			return rects.ToArray();
		}

		public static Texture2D[] CreateTexture(int baseOffset, sbyte[] imageBytes, sbyte[] sourceBytes, int animationFrameCount, Texture2D backtexture = null)
		{
			int i = DataOperations.getShort(imageBytes, 0);
			int k = DataOperations.getShort(sourceBytes, i);
			i += 2;
			int l = DataOperations.getShort(sourceBytes, i);
			i += 2;
			int i1 = sourceBytes[i++] & 0xff;
			int[] colorRange = new int[i1];



			colorRange[0] = 0xff00ff;

			for (int j1 = 0; j1 < i1 - 1; j1++)
			{
				var r = (sourceBytes[j1] & 0xff) << 16;
				var g = sourceBytes[j1 + 1] & 0xff << 8;
				var b = sourceBytes[j1 + 2] & 0xff;



				//clr.Add(new Color(r, g, b, 255));

				colorRange[j1 + 1] = ((sourceBytes[i] & 0xff) << 16) + ((sourceBytes[i + 1] & 0xff) << 8) + (sourceBytes[i + 2] & 0xff);

				i += 3;
			}






			// UnpackedImages[_pixels] = new Texture2D(graphics, y, _w);
			// UnpackedImages[_pixels].SetData(ai);

			var textures = new List<Texture2D>();

			int createdCount = 0;

			int k1 = 2;
			for (int l1 = baseOffset; l1 < baseOffset + animationFrameCount; l1++)
			{
				if (l1 >= _textureCount) break;
				var pictureOffsetX = sourceBytes[i++] & 0xff;
				var pictureOffsetY = sourceBytes[i++] & 0xff;
				var pictureWidth = DataOperations.getShort(sourceBytes, i);
				i += 2;
				var pictureHeight = DataOperations.getShort(sourceBytes, i);
				i += 2;
				int imageType = sourceBytes[i++] & 0xff;
				int pixelCount = pictureWidth * pictureHeight;
				var pictureColorIndexes = new sbyte[pixelCount];
				var pixelBuffer = colorRange;
				var pictureAssumedWidth = k; // USE THIS PLX
				var pictureAssumedHeight = l;
				int[] outputPixels = null;

				bool isSubTexture = pictureOffsetX != 0 || pictureOffsetY != 0;

				if (imageType == 0)
				{
					for (int k2 = 0; k2 < pixelCount; k2++)
					{
						// clr[k2] = y[k1];
						var val = imageBytes[k1++];

						pictureColorIndexes[k2] = val;

						if (pictureColorIndexes[k2] == 0)
							isSubTexture = true;
					}

				}
				else if (imageType == 1)
				{
					for (int l2 = 0; l2 < pictureWidth; l2++)
					{
						for (int i3 = 0; i3 < pictureHeight; i3++)
						{
							var val = imageBytes[k1++];

							pictureColorIndexes[l2 + i3 * pictureWidth] = val;

							if (pictureColorIndexes[l2 + i3 * pictureWidth] == 0)
								isSubTexture = true;
						}

					}

				}
				bool hasBackTexture = false;
				Color[] pixels = null;
				//if (backtexture != null)
				//{
				//	uint[] outPixels = null; //= new uint[pictureWidth * pictureHeight];

				//	mergeImages(pictureWidth, pictureHeight, pictureColorIndexes, pixelBuffer, backtexture, out outPixels);

				//	pixels = createColorArray(outPixels);

				//	hasBackTexture = true;
				//	// finalizeTexture(pictureWidth, pictureHeight, outputPixels, pictureColorIndexes);
				//}
				//else
				if (isSubTexture && backtexture != null)
				{
					//int[] outPixels = null;

					//mergeImages(pictureWidth, pictureHeight, pictureColorIndexes, pixelBuffer, backtexture,
					//	pictureOffsetX, pictureOffsetY, pictureAssumedWidth, pictureAssumedHeight, out outPixels);

					var pBuffer = readPixels(pictureWidth, pictureHeight, pictureColorIndexes, pixelBuffer);

					pixels = createColorArray(pBuffer, ref pictureWidth, ref pictureHeight);

					if (pixels != null)
					{
						// var t2d = CreateTexture(pictureWidth, pictureHeight, pixels);

						var texture = CreateTexture(pictureWidth, pictureHeight, pixels);


						//var pngData = texture.EncodeToPNG();
						//File.WriteAllBytes(@"c:\textures2\" + (l1) + ".png", pngData);






						//var bakerman = new TextureBaker(Game1.DeviceInstance, new Vector2(pictureAssumedWidth, pictureAssumedHeight));

						//// reverse the offsets. Stupid rotations,..

						var backtexturePixels = backtexture.GetPixels();

						Texture2D newTex = null;

						if ((pictureAssumedHeight * pictureAssumedWidth) < backtexturePixels.Length)
							newTex = new Texture2D(backtexture.width, backtexture.height, TextureFormat.ARGB32, false);
						else
							newTex = new Texture2D(pictureAssumedWidth, pictureAssumedHeight, TextureFormat.ARGB32, false);



						newTex.SetPixels(0, 0, backtexture.width, backtexture.height, backtexturePixels);

						var offsetY = pictureOffsetY;
						var offsetX = pictureOffsetX;



						newTex.SetPixels(offsetX, offsetY, texture.width, texture.height, texture.GetPixels());
						newTex.Apply();


						newTex = Rotate(newTex, RotateFlipType.Rotate270FlipNone);

						//bakerman.BakeTexture(backtexture);
						//bakerman.BakeTexture(t2d, new Rectangle(offsetX, offsetY, pictureWidth, pictureHeight));

						//var texture = bakerman.GetTexture();

						//bakerman = new TextureBaker(Game1.DeviceInstance, new Vector2(pictureAssumedWidth, pictureAssumedHeight));
						//bakerman.BakeTexture(texture, RotateFlipType.Rotate270FlipNone);

						//texture = bakerman.GetTexture();



						//var clrs = new Color[pictureAssumedWidth * pictureAssumedHeight];
						//texture.GetData(clrs);
						//clrs = makeTransparent(clrs, Microsoft.Xna.Framework.Color.FromNonPremultiplied(0, 255, 0, 255));
						//texture.SetData(clrs);




						textures.Add(newTex);

						pictureWidth = newTex.width;
						pictureHeight = newTex.height;
					}


					//bakerman

					//var target = new RenderTarget2D(Game1.DeviceInstance, pictureAssumedWidth, pictureAssumedHeight);

				}
				else
				{
					var pBuffer = readPixels(pictureWidth, pictureHeight, pictureColorIndexes, pixelBuffer);
					// ROTATE THE BITCH
					pixels = createColorArray(pBuffer, ref pictureWidth, ref pictureHeight);

					if (pixels != null)
					{
						var t2d = CreateTexture(pictureWidth, pictureHeight, pixels);




						//var pngData = t2d.EncodeToPNG();
						//File.WriteAllBytes(@"c:\textures2\" + (l1)+ ".png", pngData);

						/*
						var clrs = new Microsoft.Xna.Framework.Color[pictureWidth * pictureHeight];
						t2d.GetData(clrs);
						clrs = makeTransparent(clrs, Microsoft.Xna.Framework.Color.FromNonPremultiplied(0, 255, 0, 255));
						t2d.SetData(clrs);
						*/
						textures.Add(t2d);
					}
				}
				if (textures != null && textures.Count > 0)
				{
					//var t = textures.LastOrDefault();
					//pixels = new Color[t.width * t.height];
					//t.SetPixels(pixels);

				}

				//var sys_colorsFinal = nen Color[pixels.Count()];

				//for (int j = 0; j < sys_colorsFinal.Length; j++)
				//{
				//	sys_colorsFinal[j] = Color.FromArgb(pixels[j].A, pixels[j].R, pixels[j].G, pixels[j].B);
				//}



				//using (var stream = System.IO.File.Create("c:/jpg/" + (l1 + (textureLoadIndex++)) + ".png"))
				//{

				//	Bitmap bmp = new Bitmap(pictureWidth, pictureHeight);

				//	for (int x = 0; x < bmp.Width; x++)
				//	{
				//		for (int y = 0; y < bmp.Height; y++)
				//		{
				//			var color = sys_colorsFinal[x + y * pictureWidth];
				//			bmp.SetPixel(x, y, color);

				//		}
				//	}

				//	if (isSubTexture)
				//	{
				//		var c = Utils.GetColorStandard(pixelBuffer[1]);
				//		var clr = Color.FromArgb(c.A, 0, 0, 0); //Color.FromArgb(c.A, c.R, c.G, c.B);
				//		bmp.MakeTransparent(clr);


				//	}
				//	if (!isSubTexture)
				//	{

				//	}

				//	//	bmp.RotateFlip(RotateFlipType.Rotate270FlipNone);

				//	bmp.Save(stream, ImageFormat.Png);
				//	stream.Close();
				//}
			}


			return textures.ToArray(); // FOR NAOW!
		}



		private static Texture2D CreateTexture(int pictureWidth, int pictureHeight, Color[] pixels)
		{
			var t2d = new Texture2D(pictureWidth, pictureHeight, TextureFormat.ARGB32, false);
			t2d.SetPixels(pixels);
			t2d.Apply();

			return t2d;
		}

		public static Color GetColorStandardTexture(int color)
		{
			int blue = color >> 16 & 0xff;
			int green = color >> 8 & 0xff;
			int red = color & 0xff;

			var c = new Color(blue / 255f, green / 255f, red / 255f, 1f);

			if (c == Color.magenta || c == Color.green)
				c = new Color(0, 0, 0, 0);

			return c;
		}


		public static Color[] createColorArray(int[] pictureColors, ref int width, ref int height)
		{
			var colors = new List<Color>();
			//for (int j = 0; j + 3 < outputPixels.Length; j += 3)
			for (int j = 0; j < pictureColors.Length; j++)
			{

				/*var r = outputPixels[j + 2] & 0xff;
				var g = outputPixels[j + 1] & 0xff;
				var b = outputPixels[j + 0] & 0xff;*/
				var color = GetColorStandardTexture(pictureColors[j]);

				if (pictureColors[j] == Engine.NULLINTEGER || color == Color.magenta)//new Color(1, 0, 1)/*|| color == Microsoft.Xna.Framework.Color.FromNonPremultiplied(0, 255, 0, 255) */)
					color = new Color(0, 0, 0, 0);

				colors.Add(color); //Microsoft.Xna.Framework.Color.FromNonPremultiplied(r, g, b, 255));
				//clrs[p++] = colors.Last();
			}


			// Color[] pixels = new Microsoft.Xna.Framework.Color[colors.Count];




			//{
			//	// Rotates the texture 90 degrees counter-clockwise.

			//	for (int row = 0; row < height; row++)
			//	{
			//		for (int column = 0; column < width; column++)
			//		{
			//			pixels[row * width + column] = colors[(width - 1) + (((row * width + column) % height) * width) - (row * width + column) / height];
			//		}
			//	}
			//	var w = width;
			//	var h = height;

			//	width = h;
			//	height = w;

			//}
			////	colors.Reverse();
			//return pixels; //colors.ToArray();
			return colors.ToArray();
		}

		//public static Color[] Rotate90CounterClockwise(Color[] colors,
		//ref int width, ref int height)
		//{
		//	var pixels = new Color[width * height];
		//	for (int row = 0; row < height; row++)
		//	{
		//		for (int column = 0; column < width; column++)
		//		{
		//			pixels[row * width + column] = colors[(width - 1) + (((row * width + column) % height) * width) - (row * width + column) / height];
		//		}
		//	}
		//	var w = width;
		//	var h = height;

		//	width = h;
		//	height = w;
		//	return pixels;
		//}


		public static int[] readPixels(int pictureWidth, int pictureHeight, sbyte[] pictureColorIndexes, int[] pixelBuffer)
		{
			if (pictureColorIndexes == null) return new int[0];
			int i = pictureWidth * pictureHeight;
			sbyte[] abyte0 = pictureColorIndexes;
			int[] ai = pixelBuffer;
			int[] ai1 = new int[i];
			for (int k = 0; k < i; k++)
			{
				int l = ai[abyte0[k] & 0xff];
				if (l == 0) // if black, we will replace it a super-dark-gray
					l = 1;
				//else 
				//	if (l == 0xff00ff) // if transparent, fill with black. Stupid?
				//		l = 0;
				ai1[k] = l;
			}
			return ai1;
		}


		public static Texture2D GetWallTexture(int index) //, int wallType)
		{
			int k2 = GetWallTextureIndex(index);
			var t = Textures[k2];//FirstOrDefault(i => i.TextureIndex - 3220 == k2 + 1);
			if (t != null) return t.Texture;


			return null;
		}

		public static int GetWallTextureIndex(int index)
		{

			if (index > 12000) index -= 12000;
			int j2 = Data.wallObjectModel_TextureIndex[index];
			int k2 = 0;
			if (index == 1) k2 = 2;
			else if (index == 3) k2 = 4;
			else if (index == 4) k2 = 5;
			else if (index == 5) k2 = 10;
			else if (index == 6) k2 = 12;
			else if (index == 7) k2 = 18;
			else if (index == 8) k2 = 2;
			else if (index == 9) k2 = 4;
			else if (index == 14) k2 = 7;
			else if (index == 15) k2 = 21;
			else if (index == 16) k2 = 22;
			else if (index == 19) k2 = 23;
			else if (index == 25) k2 = 26;
			else if (index == 42) k2 = 28;
			else if (index == 63) k2 = 28;
			else k2 = Data.wallObjectModel_ColorIndex[index];

			if (k2 == 12345678) k2 = j2;
			if (k2 == 12345678) k2 = 2;

			if (index == 128) k2 = 10;
			return k2;
		}

		internal static AtlasRect GetWallTextureFromAtlas(int wallObjIndex)
		{
			var textureIndex = GetWallTextureIndex(wallObjIndex);
			if (textureIndex < TextureAtlasRects.Length)
				return TextureAtlasRects[textureIndex];
			return null;
		}

		public static Point RotateXY(Point source, double degrees,
									   int offsetX, int offsetY)
		{
			Point result = new Point();

			result.X = (int)(Math.Round((source.X - offsetX) *
					   Math.Cos(degrees) - (source.Y - offsetY) *
					   Math.Sin(degrees))) + offsetX;


			result.Y = (int)(Math.Round((source.X - offsetX) *
					   Math.Sin(degrees) + (source.Y - offsetY) *
					   Math.Cos(degrees))) + offsetY;


			return result;
		}

		internal static Texture2D Rotate(Texture2D sourceBitmap, RotateFlipType flipRotateType)
		{
			var pixels = sourceBitmap.GetPixels();
			var w = sourceBitmap.width;
			var h = sourceBitmap.height;
			var newWidth = w;
			var newHeight = h;
			switch (flipRotateType)
			{
				case RotateFlipType.Rotate90FlipY:
				case RotateFlipType.Rotate90FlipX:
				case RotateFlipType.Rotate90FlipNone:
					pixels = Rotate90Clockwise(pixels, w, h);

					if (flipRotateType == RotateFlipType.Rotate90FlipX || flipRotateType == RotateFlipType.Rotate90FlipXY)
						pixels = FlipHorizontal(pixels, w, h);
					if (flipRotateType == RotateFlipType.Rotate90FlipY || flipRotateType == RotateFlipType.Rotate90FlipXY)
						pixels = FlipVertical(pixels, w, h);

					newWidth = h;
					newHeight = w;
					break;
				case RotateFlipType.Rotate180FlipY:
				case RotateFlipType.Rotate180FlipX:
				case RotateFlipType.Rotate180FlipNone:
					pixels = Rotate180(pixels, w, h);

					if (flipRotateType == RotateFlipType.Rotate180FlipX || flipRotateType == RotateFlipType.Rotate180FlipXY)
						pixels = FlipHorizontal(pixels, w, h);
					if (flipRotateType == RotateFlipType.Rotate180FlipY || flipRotateType == RotateFlipType.Rotate180FlipXY)
						pixels = FlipVertical(pixels, w, h);

					break;
				case RotateFlipType.Rotate270FlipY:
				case RotateFlipType.Rotate270FlipX:
				case RotateFlipType.Rotate270FlipNone:
					pixels = Rotate270(pixels, w, h);

					if (flipRotateType == RotateFlipType.Rotate270FlipX || flipRotateType == RotateFlipType.Rotate270FlipXY)
						pixels = FlipHorizontal(pixels, w, h);
					if (flipRotateType == RotateFlipType.Rotate270FlipY || flipRotateType == RotateFlipType.Rotate270FlipXY)
						pixels = FlipVertical(pixels, w, h);

					newWidth = h;
					newHeight = w;
					break;

				case RotateFlipType.RotateNoneFlipXY:
					pixels = FlipHorizontal(pixels, w, h);
					pixels = FlipVertical(pixels, w, h);
					break;
				case RotateFlipType.RotateNoneFlipX:
					pixels = FlipHorizontal(pixels, w, h);
					break;
				case RotateFlipType.RotateNoneFlipY:
					pixels = FlipVertical(pixels, w, h);
					break;
			}

			var newTexture = new Texture2D(newWidth, newHeight, TextureFormat.ARGB32, false);
			newTexture.SetPixels(pixels);
			newTexture.Apply();

			return newTexture;
		}





		/// <summary>
		/// Rotates the 2d array 90 Degrees Counter Clockwise
		///	  _______          _______
		///	 |  a  a |        |  a  b |
		///	 |  b  b | -->    |  a  b |
		///	 |_______|        |_______|
		/// </summary>
		/// <param name="source"></param>
		/// <param name="width"></param>
		/// <param name="height"></param>
		/// <returns></returns>
		public static T[] Rotate90<T>(T[] source, int width, int height)
		{
			T[] destination = new T[source.Length];
			int index = 0;
			for (int x = width - 1; x > 0; x--)
			{
				for (int y = 0; y < height; y++)
				{
					destination[index++] = source[x + y * width];
				}
			}
			return destination;
		}

		/// <summary>
		/// Rotates the 2d array 90 Degrees Clockwise
		///	  _______          _______
		///	 |  a  a |        |  b  a |
		///	 |  b  b | -->    |  b  a |
		///	 |_______|        |_______|
		/// </summary>
		/// <param name="source"></param>
		/// <param name="width"></param>
		/// <param name="height"></param>
		/// <returns></returns>
		public static T[] Rotate90Clockwise<T>(T[] source, int width, int height)
		{
			var tw = width;
			var th = height;

			var temp = Rotate90(source, width, height);

			width = th;
			height = tw;

			temp = Rotate90(temp, width, height);

			width = tw;
			height = th;

			temp = Rotate90(temp, width, height);

			return temp;
		}



		/// <summary>
		/// Rotates the 2d array 270 Degrees Clockwise
		///	  _______          _______
		///	 |  a  a |        |  b  a |
		///	 |  b  b | -->    |  b  a |
		///	 |_______|        |_______|
		/// </summary>
		/// <param name="source"></param>
		/// <param name="width"></param>
		/// <param name="height"></param>
		/// <returns></returns>
		public static T[] Rotate270<T>(T[] source, int width, int height)
		{
			T[] destination = new T[source.Length];
			int index = 0;
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					destination[index++] = source[x + y * width];
				}
			}
			return destination;
		}

		///  <summary>
		/// Rotates the 2d array 180 Degrees
		/// 	  _______          _______
		/// 	 |  a  a |        |  b  b |
		/// 	 |  b  b | -->    |  a  a |
		/// 	 |_______|        |_______|
		///  </summary>
		///  <param name="source"></param>
		/// <param name="width"></param>
		/// <param name="height"></param>
		/// <returns></returns>
		public static T[] Rotate180<T>(T[] source, int width, int height)
		{
			var tw = width;
			var th = height;

			var temp = Rotate90(source, width, height);

			width = th;
			height = tw;

			temp = Rotate90(temp, width, height);

			return temp;
		}

		/// <summary>
		/// Flips the 2d array Horizontally
		/// </summary>
		/// <typeparam name="T"></typeparam>
		/// <param name="source"></param>
		/// <param name="width"></param>
		/// <param name="height"></param>
		/// <returns></returns>
		public static T[] FlipHorizontal<T>(T[] source, int width, int height)
		{
			T[] destination = new T[source.Length];

			for (int i = 0, j = source.Length - width; i < source.Length; i += width, j -= width)
			{
				for (int k = 0; k < width; ++k)
				{
					destination[i + k] = source[j + k];
				}
			}

			return destination;
		}

		/// <summary>
		/// Flips the 2d array Vertically
		/// </summary>
		/// <typeparam name="T"></typeparam>
		/// <param name="source"></param>
		/// <param name="width"></param>
		/// <param name="height"></param>
		/// <returns></returns>
		public static T[] FlipVertical<T>(T[] source, int width, int height)
		{
			T[] destination = new T[source.Length];

			for (int i = 0; i < source.Length; i += width)
			{
				for (int j = 0, k = width - 1; j < width; ++j, --k)
				{
					destination[i + j] = source[i + k];
				}
			}

			return destination;
		}
	}

	/// <summary>
	/// Specifies how much 2d array is rotated and the axis used to flip the 2d array.
	/// </summary>
	public enum RotateFlipType
	{
		/// <summary>
		/// Specifies no clockwise rotation and no flipping.
		/// </summary>
		RotateNoneFlipNone,
		/// <summary>
		/// Specifies a 90-degree clockwise rotation without flipping.
		/// </summary>
		Rotate90FlipNone,
		/// <summary>
		/// Specifies a 90-degree clockwise rotation followed by a horizontal flip.
		/// </summary>
		Rotate90FlipX,
		/// <summary>
		/// Specifies a 90-degree clockwise rotation followed by a vertical flip.
		/// </summary>
		Rotate90FlipY,
		/// <summary>
		/// Specifies a 90-degree clockwise rotation followed by a horizontal and vertical flip.
		/// </summary>
		Rotate90FlipXY,
		/// <summary>
		/// Specifies a 180-degree clockwise rotation without flipping.
		/// </summary>
		Rotate180FlipNone,
		/// <summary>
		/// Specifies a 180-degree clockwise rotation followed by a horizontal flip.
		/// </summary>
		Rotate180FlipX,
		/// <summary>
		/// Specifies a 180-degree clockwise rotation followed by a vertical flip.
		/// </summary>
		Rotate180FlipY,
		/// <summary>
		/// Specifies a 180-degree clockwise rotation followed by a horizontal and vertical flip.
		/// </summary>
		Rotate180FlipXY,
		/// <summary>
		/// Specifies a 270-degree clockwise rotation without flipping.
		/// </summary>
		Rotate270FlipNone,
		/// <summary>
		/// Specifies a 270-degree clockwise rotation followed by a horizontal flip.
		/// </summary>
		Rotate270FlipX,
		/// <summary>
		/// Specifies a 270-degree clockwise rotation followed by a vertical flip.
		/// </summary>
		Rotate270FlipY,
		/// <summary>
		/// Specifies a 270-degree clockwise rotation followed by a horizontal and vertical flip.
		/// </summary>
		Rotate270FlipXY,
		/// <summary>
		/// Specifies no clockwise rotation followed by a horizontal flip.
		/// </summary>
		RotateNoneFlipX,
		/// <summary>
		/// Specifies no clockwise rotation followed by a vertical flip.
		/// </summary>
		RotateNoneFlipY,
		/// <summary>
		/// Specifies no clockwise rotation followed by a horizontal and vertical flip.
		/// </summary>
		RotateNoneFlipXY
	}
}
